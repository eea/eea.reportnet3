package org.eea.orchestrator.controller;

import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.vo.orchestrator.*;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.orchestrator.service.JobService;
import org.eea.orchestrator.service.JobHistoryService;
import org.eea.orchestrator.service.impl.JobProcessServiceImpl;
import org.eea.orchestrator.utils.JobUtils;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.thread.ThreadPropertiesManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
    public void testFindCanceledValidationTasksFromJob() {
        JobVO job = new JobVO();
        job.setId(JOB_ID);
        job.setJobType(JobTypeEnum.VALIDATION);
        when(jobService.findById(JOB_ID)).thenReturn(job);
        when(jobProcessServiceImpl.findProcessesByJobId(JOB_ID)).thenReturn(List.of("process1"));
        when(processControllerZuul.findTasksByProcessIdsAndStatus(List.of("process1"), 0, 10))
                .thenReturn(List.of(new JobCanceledValidationTasksVO()));

        List<JobCanceledValidationTasksVO> result = jobController.findCanceledValidationTasksByJobId(JOB_ID, 0, 10);
        assertEquals(1, result.size());
    }

    @Test
    public void testFindCanceledValidationTasksFromHistory() {
        when(jobService.findById(JOB_ID)).thenReturn(null);
        JobHistoryVO history = new JobHistoryVO();
        history.setJobType(JobTypeEnum.VALIDATION);
        when(jobHistoryService.getJobHistory(JOB_ID)).thenReturn(List.of(history));
        when(jobProcessServiceImpl.findProcessesByJobId(JOB_ID)).thenReturn(List.of("p1"));
        when(processControllerZuul.findTasksByProcessIdsAndStatus(List.of("p1"), 0 , 10)).thenReturn(Collections.emptyList());

        List<JobCanceledValidationTasksVO> result = jobController.findCanceledValidationTasksByJobId(JOB_ID, 0,10);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testJobNotFound() {
        when(jobService.findById(JOB_ID)).thenReturn(null);
        when(jobHistoryService.getJobHistory(JOB_ID)).thenReturn(Collections.emptyList());

        List<JobCanceledValidationTasksVO> result = jobController.findCanceledValidationTasksByJobId(JOB_ID, 0 ,10);

        assertTrue(result.isEmpty()); // ✅ No exception, just empty list
    }

    @Test
    public void testInvalidJobType() {
        JobVO job = new JobVO();
        job.setId(JOB_ID);
        job.setJobType(JobTypeEnum.IMPORT);
        when(jobService.findById(JOB_ID)).thenReturn(job);

        List<JobCanceledValidationTasksVO> result = jobController.findCanceledValidationTasksByJobId(JOB_ID, 0 , 10);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testReleaseJobWithMissingValidationId() {
        JobVO job = new JobVO();
        job.setId(JOB_ID);
        job.setJobType(JobTypeEnum.RELEASE);
        job.setParameters(new HashMap<>()); // missing validationJobId
        when(jobService.findById(JOB_ID)).thenReturn(job);

        List<JobCanceledValidationTasksVO> result = jobController.findCanceledValidationTasksByJobId(JOB_ID, 0 ,10);
        assertTrue(result.isEmpty());
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
        when(processControllerZuul.findTasksByProcessIdsAndStatus(List.of("p123"), 0 , 10))
                .thenReturn(List.of(new JobCanceledValidationTasksVO()));

        List<JobCanceledValidationTasksVO> result = jobController.findCanceledValidationTasksByJobId(JOB_ID, 0 , 10);
        assertEquals(1, result.size());
    }
}
