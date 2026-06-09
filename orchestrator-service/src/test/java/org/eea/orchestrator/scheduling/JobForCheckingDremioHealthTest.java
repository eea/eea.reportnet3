package org.eea.orchestrator.scheduling;

import feign.FeignException;
import org.eea.interfaces.controller.communication.EmailController;
import org.eea.interfaces.controller.dremio.controller.DremioApiController;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.communication.EmailVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Unit tests for {@link JobForCheckingDremioHealth}.
 *
 * <p>Covers all execution paths of the two-step health check:
 * <ul>
 *   <li>Step 1 - {@code GET apiv2/server_status} via Feign: healthy, unhealthy HTTP status,
 *       unexpected body, {@link FeignException}, unexpected exception.</li>
 *   <li>Step 2 - {@code SELECT 1} via JDBC: healthy,
 *       {@link org.springframework.dao.DataAccessException}, unexpected exception.</li>
 *   <li>Slowness detection - elapsed time exceeds the configured threshold.</li>
 *   <li>Email notification resilience - delivery failures do not propagate.</li>
 * </ul>
 *
 * <p>All dependencies are mocked via Mockito. {@code FeignException} is mocked directly
 * following the same pattern used in other Dremio-related tests in this project.
 */
public class JobForCheckingDremioHealthTest {

    @InjectMocks
    private JobForCheckingDremioHealth job;

    @Mock
    private JdbcTemplate dremioJdbcTemplate;

    @Mock
    private DremioApiController dremioApiController;

    @Mock
    private EmailController.EmailControllerZuul emailControllerZuul;

    @Mock
    private UserManagementController.UserManagementControllerZull userManagementControllerZull;

    @Mock
    private FeignException feignException;

    @Before
    public void initMocks() {
        MockitoAnnotations.openMocks(this);

        // Inject @Value fields that Spring would normally populate
        ReflectionTestUtils.setField(job, "slowThresholdMs", 5000L);
        ReflectionTestUtils.setField(job, "adminUser", "admin");
        ReflectionTestUtils.setField(job, "adminPass", "password");

        // Stub admin authentication for all tests
        TokenVO token = new TokenVO();
        token.setAccessToken("test-token");
        Mockito.when(userManagementControllerZull.generateToken(anyString(), anyString()))
                .thenReturn(token);
    }

    /**
     * Both steps pass - no emails sent.
     */
    @Test
    public void checkDremioHealth_bothStepsOk_noEmailSent() {
        Mockito.when(dremioApiController.getServerStatus())
                .thenReturn(ResponseEntity.ok("\"OK\""));
        Mockito.when(dremioJdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenReturn(1);

        job.checkDremioHealth();

        Mockito.verify(emailControllerZuul, Mockito.never()).sendMessage(any());
    }

    /**
     * Server status returns non-200 HTTP status - email sent, step 2 skipped.
     */
    @Test
    public void checkDremioHealth_serverStatusNon200_emailSentAndStep2Skipped() {
        Mockito.when(dremioApiController.getServerStatus())
                .thenReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(""));

        job.checkDremioHealth();

        Mockito.verify(emailControllerZuul, Mockito.times(1)).sendMessage(any(EmailVO.class));
        Mockito.verify(dremioJdbcTemplate, Mockito.never()).queryForObject(anyString(), eq(Integer.class));
    }

    /**
     * Server status returns HTTP 200 but with an unexpected body - email sent, step 2 skipped.
     */
    @Test
    public void checkDremioHealth_serverStatusUnexpectedBody_emailSentAndStep2Skipped() {
        Mockito.when(dremioApiController.getServerStatus())
                .thenReturn(ResponseEntity.ok("\"STARTING\""));

        job.checkDremioHealth();

        Mockito.verify(emailControllerZuul, Mockito.times(1)).sendMessage(any(EmailVO.class));
        Mockito.verify(dremioJdbcTemplate, Mockito.never()).queryForObject(anyString(), eq(Integer.class));
    }

    /**
     * Feign throws an exception (e.g. timeout, connection refused, 5xx) -
     * email sent, step 2 skipped.
     */
    @Test
    public void checkDremioHealth_serverStatusFeignException_emailSentAndStep2Skipped() {
        Mockito.when(feignException.status()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE.value());
        Mockito.when(dremioApiController.getServerStatus()).thenThrow(feignException);

        job.checkDremioHealth();

        Mockito.verify(emailControllerZuul, Mockito.times(1)).sendMessage(any(EmailVO.class));
        Mockito.verify(dremioJdbcTemplate, Mockito.never()).queryForObject(anyString(), eq(Integer.class));
    }

    /**
     * Server status throws an unexpected runtime exception - email sent, step 2 skipped.
     */
    @Test
    public void checkDremioHealth_serverStatusUnexpectedException_emailSentAndStep2Skipped() {
        Mockito.when(dremioApiController.getServerStatus())
                .thenThrow(new RuntimeException("Unexpected"));

        job.checkDremioHealth();

        Mockito.verify(emailControllerZuul, Mockito.times(1)).sendMessage(any(EmailVO.class));
        Mockito.verify(dremioJdbcTemplate, Mockito.never()).queryForObject(anyString(), eq(Integer.class));
    }

    /**
     * Server status passes but JDBC {@code SELECT 1} throws a
     * {@link org.springframework.dao.DataAccessException} - email sent.
     */
    @Test
    public void checkDremioHealth_selectOneDataAccessException_emailSent() {
        Mockito.when(dremioApiController.getServerStatus())
                .thenReturn(ResponseEntity.ok("\"OK\""));
        Mockito.when(dremioJdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new DataAccessResourceFailureException("Connection lost"));

        job.checkDremioHealth();

        Mockito.verify(emailControllerZuul, Mockito.times(1)).sendMessage(any(EmailVO.class));
    }

    /**
     * Server status passes but JDBC {@code SELECT 1} throws an unexpected exception -
     * email sent.
     */
    @Test
    public void checkDremioHealth_selectOneUnexpectedException_emailSent() {
        Mockito.when(dremioApiController.getServerStatus())
                .thenReturn(ResponseEntity.ok("\"OK\""));
        Mockito.when(dremioJdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new RuntimeException("Unexpected JDBC error"));

        job.checkDremioHealth();

        Mockito.verify(emailControllerZuul, Mockito.times(1)).sendMessage(any(EmailVO.class));
    }

    /**
     * Both steps pass but the cycle exceeds the slow threshold (set to 0 ms) -
     * a warning email is sent.
     */
    @Test
    public void checkDremioHealth_slowCycle_warningEmailSent() {
        ReflectionTestUtils.setField(job, "slowThresholdMs", 0L);
        Mockito.when(dremioApiController.getServerStatus())
                .thenReturn(ResponseEntity.ok("\"OK\""));
        Mockito.when(dremioJdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenReturn(1);

        job.checkDremioHealth();

        Mockito.verify(emailControllerZuul, Mockito.times(1)).sendMessage(any(EmailVO.class));
    }

    /**
     * Even if the email service throws an exception, the health check job
     * does not propagate it - the scheduler thread must never die.
     */
    @Test
    public void checkDremioHealth_emailDeliveryFails_noExceptionPropagated() {
        Mockito.when(feignException.status()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());
        Mockito.when(dremioApiController.getServerStatus()).thenThrow(feignException);
        Mockito.doThrow(new RuntimeException("Mail server down"))
                .when(emailControllerZuul).sendMessage(any());

        // Must not throw
        job.checkDremioHealth();
    }
}