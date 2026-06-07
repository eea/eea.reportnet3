package org.eea.dataset.service;

import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.metabase.domain.Task;
import org.eea.dataset.persistence.metabase.repository.TaskRepository;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReleasePrecheckServiceTest {

  @InjectMocks
  private ReleasePrecheckService releasePrecheckService;

  @Mock
  private JobControllerZuul jobControllerZuul;

  @Mock
  private JobProcessControllerZuul jobProcessControllerZuul;

  @Mock
  private DataFlowControllerZuul dataFlowControllerZuul;

  @Mock
  private JdbcTemplate dataSetsJdbcTemplate;

  @Mock
  private ValidationRepository validationRepository;

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private org.eea.datalake.service.S3Helper s3Helper;

  @Mock
  private org.eea.datalake.service.S3Service s3Service;

  @Mock
  private DatasetService datasetService;

  private Map<String, Object> parameters;

  @Before
  public void setUp() {
    parameters = new HashMap<>();
    parameters.put("dataflowId", 61);
    parameters.put("dataProviderId", 2);
    parameters.put("datasetId", Arrays.asList(750L));
    parameters.put("validationJobId", 1892);

    JobVO releaseJob = new JobVO(1947L, null, null, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), parameters, "user1", true, 61L, 2L, null, null, null, null, null, null, null);

    Mockito.when(jobControllerZuul.findJobById(1947L)).thenReturn(releaseJob);
    Mockito.when(dataFlowControllerZuul.isBigDataflow(61L)).thenReturn(false);
  }

  @Test
  public void testWithBlockers() {
    Mockito.when(datasetService.hasBlockersInCurrentTenant()).thenReturn(true);
    try {
      releasePrecheckService.precheckOrThrow(1947L);
      fail("Expected ResponseStatusException to be thrown");
    } catch (ResponseStatusException e) {
      // expected
    }

    verify(datasetService, times(1)).hasBlockersInCurrentTenant();
    Mockito.verifyNoInteractions(jobProcessControllerZuul);
    Mockito.verify(jobControllerZuul, never()).updateJobInfo(anyLong(), any(), any());
  }

  @Test
  public void testWithCanceledBlockerTasks() {
    Mockito.when(jobProcessControllerZuul.findProcessesByJobId(1892L)).thenReturn(Collections.singletonList("proc-1"));

    Task canceledBlockerTask = new Task();
    Mockito.when(taskRepository.findAllByProcessIdAndStatusAndLevelErrorBlocker("proc-1", ProcessStatusEnum.CANCELED.toString())).thenReturn(Collections.singletonList(canceledBlockerTask));

    try {
      releasePrecheckService.precheckOrThrow(1947L);
      fail("Expected ResponseStatusException to be thrown");
    } catch (ResponseStatusException e) {
      // expected
    }

    Mockito.verify(jobControllerZuul, times(1)).updateJobInfo(eq(1947L), eq(JobInfoEnum.ERROR_RELEASE_CANCELED_BLOCKERS), isNull());
  }

  @Test
  public void testWithCanceledNonBlockerTasks() {
    Mockito.when(jobProcessControllerZuul.findProcessesByJobId(1892L)).thenReturn(Collections.singletonList("proc-1"));

    Mockito.when(taskRepository.findAllByProcessIdAndStatusAndLevelErrorBlocker("proc-1", ProcessStatusEnum.CANCELED.toString())).thenReturn(Collections.emptyList());

    Task canceledTask = new Task();
    Mockito.when(taskRepository.findFirstByProcessIdInAndStatus(anyList(), eq(ProcessStatusEnum.CANCELED))).thenReturn(canceledTask);

    releasePrecheckService.precheckOrThrow(1947L);

    Mockito.verify(jobControllerZuul, times(1)).updateJobInfo(eq(1947L), eq(JobInfoEnum.WARNING_HAS_CANCELED_VALIDATION_TASKS), isNull());
  }

  @Test
  public void testWithoutIssues() {
    Mockito.when(datasetService.hasBlockersInCurrentTenant()).thenReturn(false);
    Mockito.when(jobProcessControllerZuul.findProcessesByJobId(1892L)).thenReturn(Collections.singletonList("proc-1"));

    Mockito.when(taskRepository.findAllByProcessIdAndStatusAndLevelErrorBlocker("proc-1", ProcessStatusEnum.CANCELED.toString())).thenReturn(Collections.emptyList());

    Mockito.when(taskRepository.findFirstByProcessIdInAndStatus(anyList(), eq(ProcessStatusEnum.CANCELED))).thenReturn(null);

    releasePrecheckService.precheckOrThrow(1947L);

    Mockito.verify(jobControllerZuul, never()).updateJobInfo(eq(1947L), eq(JobInfoEnum.ERROR_RELEASE_CANCELED_BLOCKERS), isNull());
    Mockito.verify(jobControllerZuul, never()).updateJobInfo(eq(1947L), eq(JobInfoEnum.WARNING_HAS_CANCELED_VALIDATION_TASKS), isNull());
  }
}