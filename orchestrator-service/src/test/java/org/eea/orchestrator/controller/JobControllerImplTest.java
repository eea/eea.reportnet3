package org.eea.orchestrator.controller;

import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.orchestrator.JobCanceledValidationTaskVO;
import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.orchestrator.JobCanceledValidationTasksVO;
import org.eea.orchestrator.service.JobHistoryService;
import org.eea.orchestrator.service.JobService;
import org.eea.orchestrator.service.impl.JobProcessServiceImpl;
import org.eea.orchestrator.utils.JobUtils;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.thread.ThreadPropertiesManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JobControllerImplTest {

    @InjectMocks
    private JobControllerImpl jobController;

    @Mock
    private JobService jobService;

    @Mock
    private JobHistoryService jobHistoryService;

    @Mock
    private JobProcessServiceImpl jobProcessServiceImpl;

    @Mock
    private ProcessControllerZuul processControllerZuul;

    @Mock
    private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

    @Mock
    private DataFlowControllerZuul dataFlowControllerZuul;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private JobUtils jobUtils;

    private static final Long JOB_ID = 1L;

    @Before
    public void setUp() {
        ThreadPropertiesManager.setVariable("user", "user");
        Set<String> roles = new HashSet<>();
        UserDetails userDetails = EeaUserDetails.create("user", roles);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testAddValidationPreparationJob_jobCreated() {

        final Long datasetId = 12345L;
        final Long dataflowId = 1414L;
        final Long providerId = 57L;
        final boolean released = false;
        final boolean createParquetWithSQL = false;
        final String validateAsProviderCode = "57";
        final String preparationCode = "EPEIRUS";
        final String dataflowName = "SNR3";
        final String datasetName = "TEST";
        final Long jobId = 1234L;
        final Map<String, String> authenticationDetails = new HashMap<>();
        authenticationDetails.put("user", "testUser");

        final DataSetMetabaseVO dataSetMetabaseVOMock = new DataSetMetabaseVO();
        dataSetMetabaseVOMock.setId(datasetId);
        dataSetMetabaseVOMock.setDataflowId(dataflowId);
        dataSetMetabaseVOMock.setDataProviderId(providerId);
        dataSetMetabaseVOMock.setDatasetTypeEnum(DatasetTypeEnum.PREPARATION);
        dataSetMetabaseVOMock.setDataSetName(datasetName);

        mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(authenticationDetails);
        mockStatic(ThreadPropertiesManager.class);
        when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId)).thenReturn(dataSetMetabaseVOMock);
        when(dataFlowControllerZuul.findDataflowNameById(dataSetMetabaseVOMock.getDataflowId())).thenReturn(dataflowName);
        when(jobService.checkEligibilityOfPreparationJob(JobTypeEnum.VALIDATION.toString(), datasetId, preparationCode)).thenReturn(JobStatusEnum.IN_PROGRESS);
        when(jobService.addJob(
                Mockito.eq(dataflowId),
                Mockito.eq(providerId),
                Mockito.eq(datasetId),
                Mockito.any(),
                Mockito.eq(JobTypeEnum.VALIDATION),
                Mockito.eq(JobStatusEnum.IN_PROGRESS),
                Mockito.eq(released),
                Mockito.eq(null),
                Mockito.eq(dataflowName),
                Mockito.eq(datasetName),
                Mockito.eq(preparationCode)))
                .thenReturn(jobId);

        final Long actualResult = jobController.addValidationJob(datasetId, dataflowId, providerId, released, createParquetWithSQL, validateAsProviderCode, preparationCode);

        verify(dataSetMetabaseControllerZuul).findDatasetMetabaseById(datasetId);
        verify(dataFlowControllerZuul).findDataflowNameById(dataflowId);
        verify(jobService).checkEligibilityOfPreparationJob(JobTypeEnum.VALIDATION.toString(), datasetId, preparationCode);
        verify(jobService).addJob(
                Mockito.eq(dataflowId),
                Mockito.eq(providerId),
                Mockito.eq(datasetId),
                Mockito.any(),
                Mockito.eq(JobTypeEnum.VALIDATION),
                Mockito.eq(JobStatusEnum.IN_PROGRESS),
                Mockito.eq(released),
                Mockito.eq(null),
                Mockito.eq(dataflowName),
                Mockito.eq(datasetName),
                Mockito.eq(preparationCode));
        verifyNoMoreInteractions(jobService);

        assertNotNull(actualResult);
        assertEquals(jobId, actualResult);
    }

    @Test(expected = ResponseStatusException.class)
    public void testAddValidationPreparationJob_JobRefused() {

        final Long datasetId = 12345L;
        final Long dataflowId = 1414L;
        final Long providerId = 57L;
        final boolean released = false;
        final boolean createParquetWithSQL = false;
        final String validateAsProviderCode = "57";
        final String preparationCode = "EPEIRUS";
        final String dataflowName = "SNR3";
        final String datasetName = "TEST";
        final Long jobId = 1234L;
        final Map<String, String> authenticationDetails = new HashMap<>();
        authenticationDetails.put("user", "testUser");

        final DataSetMetabaseVO dataSetMetabaseVOMock = new DataSetMetabaseVO();
        dataSetMetabaseVOMock.setId(datasetId);
        dataSetMetabaseVOMock.setDataflowId(dataflowId);
        dataSetMetabaseVOMock.setDataProviderId(providerId);
        dataSetMetabaseVOMock.setDatasetTypeEnum(DatasetTypeEnum.PREPARATION);
        dataSetMetabaseVOMock.setDataSetName(datasetName);

        mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(authenticationDetails);
        mockStatic(ThreadPropertiesManager.class);
        when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId)).thenReturn(dataSetMetabaseVOMock);
        when(dataFlowControllerZuul.findDataflowNameById(dataSetMetabaseVOMock.getDataflowId())).thenReturn(dataflowName);
        when(jobService.checkEligibilityOfPreparationJob(JobTypeEnum.VALIDATION.toString(), datasetId, preparationCode)).thenReturn(JobStatusEnum.REFUSED);
        when(jobService.addJob(
                Mockito.eq(dataflowId),
                Mockito.eq(providerId),
                Mockito.eq(datasetId),
                Mockito.any(),
                Mockito.eq(JobTypeEnum.VALIDATION),
                Mockito.eq(JobStatusEnum.IN_PROGRESS),
                Mockito.eq(released),
                Mockito.eq(null),
                Mockito.eq(dataflowName),
                Mockito.eq(datasetName),
                Mockito.eq(preparationCode)))
                .thenReturn(jobId);

        final Long actualResult = jobController.addValidationJob(datasetId, dataflowId, providerId, released, createParquetWithSQL, validateAsProviderCode, preparationCode);

        verify(dataSetMetabaseControllerZuul).findDatasetMetabaseById(datasetId);
        verify(dataFlowControllerZuul).findDataflowNameById(dataflowId);
        verify(jobService).checkEligibilityOfPreparationJob(JobTypeEnum.VALIDATION.toString(), datasetId, preparationCode);
        verify(jobService).addJob(
                Mockito.eq(dataflowId),
                Mockito.eq(providerId),
                Mockito.eq(datasetId),
                Mockito.any(),
                Mockito.eq(JobTypeEnum.VALIDATION),
                Mockito.eq(JobStatusEnum.IN_PROGRESS),
                Mockito.eq(released),
                Mockito.eq(null),
                Mockito.eq(dataflowName),
                Mockito.eq(datasetName),
                Mockito.eq(preparationCode));
        verify(jobService).releaseValidationRefusedNotification(jobId, "testUser", datasetId);
        verifyNoMoreInteractions(jobService);
    }

    @Test
    public void testFindCanceledValidationTasksFromJob() {
        JobVO job = new JobVO();
        job.setId(JOB_ID);
        job.setJobType(JobTypeEnum.VALIDATION);
        when(jobService.findById(JOB_ID)).thenReturn(job);

        when(jobProcessServiceImpl.findProcessesByJobId(JOB_ID)).thenReturn(List.of("process1"));

        List<JobCanceledValidationTaskVO> mutableList = new ArrayList<>();
        mutableList.add(new JobCanceledValidationTaskVO());

        JobCanceledValidationTasksVO canceledResponse = new JobCanceledValidationTasksVO();
        canceledResponse.setTasksList(mutableList);
        canceledResponse.setTotalRecords(1L);
        canceledResponse.setFilteredRecords((long) mutableList.size());
        canceledResponse.setRemainingTasks(0L);

        when(processControllerZuul.findTasksByProcessIdsAndStatus(List.of("process1"), 0, 10))
                .thenReturn(canceledResponse);

        JobCanceledValidationTasksVO result = jobController.findCanceledValidationTasksByJobId(
                JOB_ID, 0, 10, true, "ruleCode"
        );

        assertNotNull(result);
        assertEquals(1, result.getTasksList().size());
        assertEquals(Long.valueOf(1), result.getTotalRecords());
    }

    @Test
    public void testFindCanceledValidationTasksFromHistory() {
        when(jobService.findById(JOB_ID)).thenReturn(null);

        JobHistoryVO history = new JobHistoryVO();
        history.setJobType(JobTypeEnum.VALIDATION);
        when(jobHistoryService.getJobHistory(JOB_ID)).thenReturn(List.of(history));

        when(jobProcessServiceImpl.findProcessesByJobId(JOB_ID)).thenReturn(List.of("p1"));

        JobCanceledValidationTasksVO canceledResponse = new JobCanceledValidationTasksVO(
                Collections.emptyList(), 0L, 0L, 0L
        );
        when(processControllerZuul.findTasksByProcessIdsAndStatus(List.of("p1"), 0, 10))
                .thenReturn(canceledResponse);

        JobCanceledValidationTasksVO result = jobController.findCanceledValidationTasksByJobId(
                JOB_ID, 0, 10, true, "ruleCode"
        );

        assertNotNull(result);
        assertTrue(result.getTasksList().isEmpty());
        assertEquals(Long.valueOf(0), result.getTotalRecords());
    }

    @Test
    public void testJobNotFound() {
        when(jobService.findById(JOB_ID)).thenReturn(null);
        when(jobHistoryService.getJobHistory(JOB_ID)).thenReturn(Collections.emptyList());

        JobCanceledValidationTasksVO result = jobController.findCanceledValidationTasksByJobId(
                JOB_ID, 0, 10, true, "ruleCode"
        );

        assertNotNull(result);
        assertTrue(result.getTasksList().isEmpty());
        assertEquals(Long.valueOf(0), result.getTotalRecords());
    }

    @Test
    public void testInvalidJobType() {
        JobVO job = new JobVO();
        job.setId(JOB_ID);
        job.setJobType(JobTypeEnum.IMPORT);
        when(jobService.findById(JOB_ID)).thenReturn(job);

        JobCanceledValidationTasksVO result = jobController.findCanceledValidationTasksByJobId(
                JOB_ID, 0, 10, true, "ruleCode"
        );

        assertNotNull(result);
        assertTrue(result.getTasksList().isEmpty());
        assertEquals(Long.valueOf(0), result.getTotalRecords());
    }

    @Test
    public void testReleaseJobWithMissingValidationId() {
        JobVO job = new JobVO();
        job.setId(JOB_ID);
        job.setJobType(JobTypeEnum.RELEASE);
        job.setParameters(new HashMap<>()); // no validationJobId
        when(jobService.findById(JOB_ID)).thenReturn(job);

        JobCanceledValidationTasksVO result = jobController.findCanceledValidationTasksByJobId(
                JOB_ID, 0, 10, true, "ruleCode"
        );

        assertNotNull(result);
        assertTrue(result.getTasksList().isEmpty());
        assertEquals(Long.valueOf(0), result.getTotalRecords());
    }

    @Test
    public void testReleaseJobWithValidationId() {
        JobVO job = new JobVO();
        job.setId(JOB_ID);
        job.setJobType(JobTypeEnum.RELEASE);

        Map<String, Object> params = new HashMap<>();
        params.put("validationJobId", 5L);
        job.setParameters(params);

        when(jobService.findById(JOB_ID)).thenReturn(job);
        when(jobProcessServiceImpl.findProcessesByJobId(5L)).thenReturn(List.of("p123"));

        List<JobCanceledValidationTaskVO> mutableList = new ArrayList<>();
        mutableList.add(new JobCanceledValidationTaskVO());

        JobCanceledValidationTasksVO canceledResponse = new JobCanceledValidationTasksVO();
        canceledResponse.setTasksList(mutableList);
        canceledResponse.setTotalRecords(1L);
        canceledResponse.setFilteredRecords((long) mutableList.size());
        canceledResponse.setRemainingTasks(0L);

        when(processControllerZuul.findTasksByProcessIdsAndStatus(List.of("p123"), 0, 10))
                .thenReturn(canceledResponse);

        JobCanceledValidationTasksVO result = jobController.findCanceledValidationTasksByJobId(
                JOB_ID, 0, 10, true, "ruleCode"
        );

        assertNotNull(result);
        assertEquals(1, result.getTasksList().size());
        assertEquals(Long.valueOf(1), result.getTotalRecords());
    }
}
