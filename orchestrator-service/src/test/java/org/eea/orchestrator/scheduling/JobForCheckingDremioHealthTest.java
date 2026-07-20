package org.eea.orchestrator.scheduling;

import feign.FeignException;
import org.eea.interfaces.controller.communication.EmailController;
import org.eea.interfaces.controller.dremio.controller.DremioApiController;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.communication.EmailVO;
import org.eea.interfaces.vo.dremio.DremioSqlRequestBody;
import org.eea.interfaces.vo.dremio.DremioSqlResponse;
import org.eea.interfaces.vo.ums.TokenVO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Unit tests for {@link JobForCheckingDremioHealth}.
 *
 * <p>Covers all execution paths of the two-step health check:
 *
 * <p>All dependencies are mocked via Mockito. {@code FeignException} is mocked directly
 * following the same pattern used in other Dremio-related tests in this project.
 */
public class JobForCheckingDremioHealthTest {

    private static final String TABLE_PATH =
            "rn3-dataset.rn3-dataset.test.liveness.1234";

    private static final String EXPECTED_SQL =
            "SELECT 1 FROM \"rn3-dataset\".\"rn3-dataset\".\"test\".\"liveness\".\"1234\" LIMIT 1";

    @Mock
    private DremioApiController dremioApiController;

    @Mock
    private EmailController.EmailControllerZuul emailControllerZuul;

    @Mock
    private UserManagementController.UserManagementControllerZull userManagementControllerZull;

    @Mock
    private FeignException feignException;

    @Mock
    private DremioSqlResponse dremioSqlResponse;

    @InjectMocks
    private JobForCheckingDremioHealth job;

    @Before
    public void initMocks() {
        MockitoAnnotations.openMocks(this);

        // Inject @Value fields that Spring would normally populate
        ReflectionTestUtils.setField(job, "slowThresholdMs", 5000L);
        ReflectionTestUtils.setField(job, "techEmailGroup", "tech@example.com");
        ReflectionTestUtils.setField(job, "healthCheckTable", TABLE_PATH);
        ReflectionTestUtils.setField(job, "adminUser", "admin");
        ReflectionTestUtils.setField(job, "adminPass", "password");
        ReflectionTestUtils.setField(job, "dremioUsername", "dremioUser");
        ReflectionTestUtils.setField(job, "dremioPassword", "dremioPass");

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
        // Step 1 passes
        Mockito.when(dremioApiController.getServerStatus())
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        // Step 2: capture the request body
        Mockito.when(dremioSqlResponse.getId()).thenReturn("job-123");
        ArgumentCaptor<DremioSqlRequestBody> captor =
                ArgumentCaptor.forClass(DremioSqlRequestBody.class);
        Mockito.when(dremioApiController.sqlQuery(any(), captor.capture()))
                .thenReturn(dremioSqlResponse);

        job.checkDremioHealth();

        assertEquals(EXPECTED_SQL, captor.getValue().getSql());
    }

    /**
     * Server status returns non-200 HTTP status - email sent, step 2 skipped.
     */
    @Test
    public void checkDremioHealth_serverStatusNon200_emailSentAndStep2Skipped() {
        Mockito.when(dremioApiController.getServerStatus())
                .thenReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(""));

        job.checkDremioHealth();

        Mockito.doNothing().when(emailControllerZuul).sendMessage(Mockito.any(EmailVO.class));
    }

    /**
     * Server status returns HTTP 200 but with an unexpected body - email sent, step 2 skipped.
     */
    @Test
    public void checkDremioHealth_serverStatusUnexpectedBody_emailSentAndStep2Skipped() {
        Mockito.when(dremioApiController.getServerStatus())
                .thenReturn(ResponseEntity.ok("\"STARTING\""));

        job.checkDremioHealth();

        Mockito.doNothing().when(emailControllerZuul).sendMessage(Mockito.any(EmailVO.class));
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

        Mockito.doNothing().when(emailControllerZuul).sendMessage(Mockito.any(EmailVO.class));
    }

    /**
     * Server status throws an unexpected runtime exception - email sent, step 2 skipped.
     */
    @Test
    public void checkDremioHealth_serverStatusUnexpectedException_emailSentAndStep2Skipped() {
        Mockito.when(dremioApiController.getServerStatus())
                .thenThrow(new RuntimeException("Unexpected"));

        job.checkDremioHealth();

        Mockito.doNothing().when(emailControllerZuul).sendMessage(Mockito.any(EmailVO.class));
    }

    /**
     * Server status passes but JDBC {@code SELECT 1} throws a
     * {@link org.springframework.dao.DataAccessException} - email sent.
     */
    @Test
    public void checkDremioHealth_selectOneDataAccessException_emailSent() {
        Mockito.when(dremioApiController.getServerStatus())
                .thenReturn(ResponseEntity.ok("\"OK\""));
        Mockito.when(dremioSqlResponse.getId()).thenReturn("job-123");
        ArgumentCaptor<DremioSqlRequestBody> captor =
                ArgumentCaptor.forClass(DremioSqlRequestBody.class);
        Mockito.when(dremioApiController.sqlQuery(any(), captor.capture()))
                .thenThrow(new DataAccessResourceFailureException("Connection lost"));

        job.checkDremioHealth();

        Mockito.doNothing().when(emailControllerZuul).sendMessage(Mockito.any(EmailVO.class));
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