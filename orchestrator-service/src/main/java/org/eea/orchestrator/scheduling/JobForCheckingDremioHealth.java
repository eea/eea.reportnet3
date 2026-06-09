package org.eea.orchestrator.scheduling;

import feign.FeignException;
import org.eea.interfaces.controller.communication.EmailController;
import org.eea.interfaces.controller.dremio.controller.DremioApiController;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.communication.EmailVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.orchestrator.configuration.DremioConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Scheduled job that monitors the availability and responsiveness of the Dremio instance.
 *
 * <p>This job runs every 10 minutes and performs a two-step health check:
 * <ol>
 *   <li><b>Server status check:</b> calls {@code GET apiv2/server_status} via the
 *       {@link DremioApiController} Feign client to verify that the Dremio HTTP server
 *       is reachable and returning HTTP 200.</li>
 *   <li><b>Functional SQL check:</b> executes a {@code SELECT 1} query via JDBC to confirm
 *       that the Dremio query engine is operational end-to-end.</li>
 * </ol>
 *
 * <p>Step 2 is only executed if step 1 succeeds. If either step fails, the error is logged
 * and an email notification is sent via {@link EmailController.EmailControllerZuul}.
 * Additionally, if the total cycle duration exceeds the configured slow-response threshold,
 * a warning is logged and an email is sent even if both checks passed.
 *
 * <p>This job is intentionally silent on healthy runs - it only produces output when
 * something is wrong.
 *
 * <p>HTTP connectivity to Dremio is handled by the {@link DremioApiController} Feign client,
 * which reads its base URL from
 * {@code spring.cloud.openfeign.client.config.dremioClient.url}. No additional URL
 * configuration is needed in this job.
 *
 * <p>Authentication is handled by obtaining a Keycloak token for the configured admin user
 * via {@link UserManagementController.UserManagementControllerZull} and registering it in
 * the Spring {@link SecurityContextHolder} before each run, following the same pattern
 * used by other jobs in this package.
 *
 * <p>Required configuration properties:
 * <ul>
 *   <li>{@code eea.keycloak.admin.user} - Keycloak admin username</li>
 *   <li>{@code eea.keycloak.admin.password} - Keycloak admin password</li>
 * </ul>
 *
 * <p>Optional configuration properties (with defaults):
 * <ul>
 *   <li>{@code dremio.health-check.slow-threshold-ms} - maximum acceptable cycle duration
 *       in ms before a slowness warning is raised (default: {@code 5000})</li>
 * </ul>
 *
 * @see DremioConfiguration
 * @see DremioApiController
 */
@Import(DremioConfiguration.class)
@Component
public class JobForCheckingDremioHealth {

    private static final Logger LOG = LoggerFactory.getLogger(JobForCheckingDremioHealth.class);

    /** Bearer token prefix used when populating the Spring security context. */
    private static final String BEARER = "Bearer ";

    /**
     * Maximum acceptable duration (ms) for the full health check cycle.
     * If the combined time of both checks exceeds this value, a slowness warning is raised.
     * Defaults to 5000 ms (5 seconds).
     */
    @Value("${dremio.health-check.slow-threshold-ms:5000}")
    private long slowThresholdMs;

    /**
     * Keycloak admin username used to authenticate before each health check run.
     * Injected from {@code eea.keycloak.admin.user}.
     */
    @Value("${eea.keycloak.admin.user}")
    private String adminUser;

    /**
     * Keycloak admin password used to authenticate before each health check run.
     * Injected from {@code eea.keycloak.admin.password}.
     */
    @Value("${eea.keycloak.admin.password}")
    private String adminPass;

    /**
     * JDBC template configured for the Dremio datasource.
     * Used in step 2 to execute the {@code SELECT 1} functional check.
     * Qualified as {@code "dremioJdbcTemplate"} to avoid conflicts with other datasource beans.
     */
    private final JdbcTemplate dremioJdbcTemplate;

    /**
     * Feign client for Dremio REST API calls.
     * Used in step 1 to call {@code GET apiv2/server_status}.
     * Its base URL is configured via
     * {@code spring.cloud.openfeign.client.config.dremioClient.url}.
     */
    private final DremioApiController dremioApiController;

    /**
     * Feign client for sending email notifications via the communication microservice.
     * Invoked when a health check step fails or the slowness threshold is exceeded.
     */
    private final EmailController.EmailControllerZuul emailControllerZuul;

    /**
     * Feign client for the user management microservice.
     * Used to obtain a Keycloak access token for the admin user before each run.
     */
    private final UserManagementController.UserManagementControllerZull userManagementControllerZull;

    /**
     * Constructs the job with all required dependencies via constructor injection.
     *
     * @param dremioJdbcTemplate           JDBC template for the Dremio datasource
     * @param dremioApiController          Feign client for Dremio REST API calls
     * @param emailControllerZuul          Feign client for sending email notifications
     * @param userManagementControllerZull Feign client for obtaining Keycloak tokens
     */
    public JobForCheckingDremioHealth(
            @Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate,
            DremioApiController dremioApiController,
            EmailController.EmailControllerZuul emailControllerZuul,
            UserManagementController.UserManagementControllerZull userManagementControllerZull) {
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.dremioApiController = dremioApiController;
        this.emailControllerZuul = emailControllerZuul;
        this.userManagementControllerZull = userManagementControllerZull;
    }

    /**
     * Registers the health check task with a dedicated scheduler after bean construction.
     *
     * <p>A {@link ThreadPoolTaskScheduler} is created and the job is scheduled using the
     * cron expression {@code 0 *{@literal /}10 * * * *}, which fires on every 10-minute
     * boundary of the clock (e.g. 00:00, 00:10, 00:20, ...).
     */
    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(this::checkDremioHealth, new CronTrigger("0 */10 * * * *"));
    }

    /**
     * Main health check entry point, invoked every 10 minutes by the scheduler.
     *
     * <p>Execution flow:
     * <ol>
     *   <li>Records the start time for slowness detection.</li>
     *   <li>Authenticates as the configured Keycloak admin user.</li>
     *   <li>Calls {@link #isServerStatusOk()} - exits early on failure.</li>
     *   <li>Calls {@link #isSelectOneOk()} - exits early on failure.</li>
     *   <li>Checks total elapsed time; logs a warning and sends an email if it exceeds
     *       {@code slowThresholdMs}.</li>
     * </ol>
     *
     * <p>A top-level {@code catch} prevents any unhandled exception from silently killing
     * the scheduler thread.
     */
    public void checkDremioHealth() {
        try {
            LOG.info("Running scheduled task checkDremioHealth");

            // Captured here so elapsed time covers both checks
            Instant start = Instant.now();

            authenticateAsAdmin();

            // Step 1 - server reachability via Feign REST client
            if (!isServerStatusOk()) {
                return;
            }

            // Step 2 - query engine functionality via JDBC
            if (!isSelectOneOk()) {
                return;
            }

            // Step 3 - slowness detection across the full cycle
            long elapsedMs = Duration.between(start, Instant.now()).toMillis();
            if (elapsedMs > slowThresholdMs) {
                String message = String.format(
                        "Dremio health-check passed but was SLOW - elapsed %d ms (threshold %d ms).",
                        elapsedMs, slowThresholdMs);
                LOG.warn(message);
                notifyByEmail("Dremio Health Warning - Slow Response", message);
            }

        } catch (Exception e) {
            LOG.error("Error while running scheduled task checkDremioHealth", e);
        }
    }

    /**
     * Performs step 1 of the health check by calling {@code GET apiv2/server_status}
     * via the {@link DremioApiController} Feign client.
     *
     * <p>Verifies that the Dremio HTTP server is reachable and returning HTTP 200.
     * The response body is not validated - a 200 status is considered sufficient evidence
     * that the server process is running and accepting connections.
     *
     * <p>Failure cases handled:
     * <ul>
     *   <li>Non-200 HTTP status - logs error, sends email, returns {@code false}.</li>
     *   <li>{@link FeignException} - covers all Feign failure scenarios including 4xx/5xx
     *       responses, timeouts, and connection refused. {@link FeignException#status()}
     *       is included in the log message; a value of {@code -1} indicates no response
     *       was received (e.g. connection refused or timeout).</li>
     *   <li>Any other unexpected exception - logs error, sends email, returns
     *       {@code false}.</li>
     * </ul>
     *
     * @return {@code true} if Dremio returned HTTP 200; {@code false} on any failure
     */
    private boolean isServerStatusOk() {
        try {
            ResponseEntity<String> response = dremioApiController.getServerStatus();

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                String msg = "Dremio server_status returned unexpected HTTP " + response.getStatusCode();
                LOG.error(msg);
                notifyByEmail("Dremio Health Alert - Unhealthy Status", msg);
                return false;
            }

            // Response body should be "OK"
            String body = response.getBody() != null ? response.getBody().replace("\"", "").trim() : "";
            if (!"OK".equalsIgnoreCase(body)) {
                String msg = "Dremio server_status returned unexpected body: " + response.getBody();
                LOG.error(msg);
                notifyByEmail("Dremio Health Alert - Unhealthy Status", msg);
                return false;
            }
            return true;

        } catch (FeignException e) {
            String msg = "Dremio server_status unreachable or failed with status "
                    + e.status() + ": " + e.getMessage();
            LOG.error(msg, e);
            notifyByEmail("Dremio Health Alert - Unreachable", msg);
            return false;

        } catch (Exception e) {
            String msg = "Unexpected error calling Dremio server_status: " + e.getMessage();
            LOG.error(msg, e);
            notifyByEmail("Dremio Health Alert - Unexpected Error", msg);
            return false;
        }
    }

    /**
     * Performs step 2 of the health check by executing {@code SELECT 1} via JDBC.
     *
     * <p>Verifies that the Dremio query engine is operational end-to-end. A successful
     * result confirms that Dremio accepted, planned, and executed a trivial query.
     * This step is only reached if {@link #isServerStatusOk()} returned {@code true}.
     *
     * <p>Failure cases handled:
     * <ul>
     *   <li>{@link DataAccessException} (JDBC / SQL error) - logs error, sends email,
     *       returns {@code false}.</li>
     *   <li>Any other unexpected exception - logs error, sends email, returns
     *       {@code false}.</li>
     * </ul>
     *
     * @return {@code true} if {@code SELECT 1} executed successfully; {@code false} on any failure
     */
    private boolean isSelectOneOk() {
        try {
            dremioJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;

        } catch (DataAccessException e) {
            String msg = "Dremio SELECT 1 failed via JDBC: " + e.getMessage();
            LOG.error(msg, e);
            notifyByEmail("Dremio Health Alert - SQL Check Failed", msg);
            return false;

        } catch (Exception e) {
            String msg = "Unexpected error running Dremio SELECT 1: " + e.getMessage();
            LOG.error(msg, e);
            notifyByEmail("Dremio Health Alert - SQL Error", msg);
            return false;
        }
    }

    /**
     * Authenticates as the configured Keycloak admin user and stores the resulting
     * token in the Spring {@link SecurityContextHolder}.
     *
     * <p>This ensures that outbound Feign calls (e.g. to the email microservice) carry
     * a valid Bearer token, following the same pattern used by other scheduled jobs
     * in this package.
     */
    private void authenticateAsAdmin() {
        TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        adminUser, BEARER + tokenVo.getAccessToken(), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Sends an email notification via the communication microservice.
     *
     * <p>This method is intentionally non-throwing - any failure to deliver the
     * notification is logged separately and never allowed to propagate, ensuring that
     * email delivery issues do not mask the original health check problem.
     *
     * @param subject the email subject line describing the alert type
     * @param body    the email body containing the detailed error or warning message
     */
    private void notifyByEmail(String subject, String body) {
        try {
            EmailVO emailVO = new EmailVO();
            emailVO.setSubject(subject);
            emailVO.setText(body);
            emailControllerZuul.sendMessage(emailVO);
        } catch (Exception e) {
            LOG.error("Failed to send Dremio health-check notification email: {}", e.getMessage(), e);
        }
    }
}