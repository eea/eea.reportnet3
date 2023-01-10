package org.eea.dataset.io.kafka.commands;

import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobHistoryController.JobHistoryControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.Timestamp;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;

/**
 * The Class RestoreDataCollectionSnapshotCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckBlockersDataSnapshotCommandTest {

  /** The restore data collection snapshot command. */
  @InjectMocks
  private CheckBlockersDataSnapshotCommand checkBlockersDataSnapshotCommand;

  /** The dataset snapshot service. */
  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The eu dataset service. */
  @Mock
  private ValidationRepository validationRepository;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset snapshot service. */
  @Mock
  private DatasetSnapshotService datasetSnapshotService;

  @Mock
  private JobControllerZuul jobControllerZuul;

  @Mock
  private JobHistoryControllerZuul jobHistoryControllerZuul;

  @Mock
  private ProcessControllerZuul processControllerZuul;

  @Mock
  private JobProcessControllerZuul jobProcessControllerZuul;

  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The data. */
  private Map<String, Object> data;

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.VALIDATION_RELEASE_FINISHED_EVENT);
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test execute without blockers.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecuteWithoutBlockers() throws EEAException {
    data = new HashMap<>();
    data.put("dataset_id", 1L);
    data.put("user", "user1");
    eeaEventVO.setData(data);
    DataSetMetabase datasetMetabase = new DataSetMetabase();
    datasetMetabase.setId(1L);
    datasetMetabase.setDataflowId(1L);
    datasetMetabase.setDataProviderId(1L);
    List<Long> datasetsId = new ArrayList<>();
    datasetsId.add(1L);
    datasetsId.add(2L);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.when(dataSetMetabaseRepository.findById(1L)).thenReturn(Optional.of(datasetMetabase));
    Mockito
        .when(dataSetMetabaseRepository.getDatasetIdsByDataflowIdAndDataProviderId(
            datasetMetabase.getDataflowId(), datasetMetabase.getDataProviderId()))
        .thenReturn(datasetsId);
    Mockito.when(jobControllerZuul.checkEligibilityOfJob(anyString(), anyBoolean(), anyLong(), anyLong(), anyList())).thenReturn(JobStatusEnum.QUEUED);
    JobVO jobVO = new JobVO(Long.valueOf(1), JobTypeEnum.RELEASE, JobStatusEnum.QUEUED, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), null, "test", true, 1L, 1L, 1L, null);
    Mockito.when(jobControllerZuul.save(any(JobVO.class))).thenReturn(jobVO);
    Mockito.doNothing().when(jobHistoryControllerZuul).save(any(JobVO.class));
    Mockito.when(validationRepository.existsByLevelError(ErrorTypeEnum.BLOCKER)).thenReturn(false);
    ProcessVO processVO = new ProcessVO();
    processVO.setProcessId("jkhiuh");
    Mockito.when(processControllerZuul.updateProcess(anyLong(), anyLong(), any(ProcessStatusEnum.class), any(ProcessTypeEnum.class), anyString(), anyString(), anyInt(), anyBoolean())).thenReturn(true);
    JobProcessVO jobProcessVO = new JobProcessVO(1L, 1L, "jkhiuh", 1L, null, null);
    Mockito.when(jobProcessControllerZuul.save(any(JobProcessVO.class))).thenReturn(jobProcessVO);

    checkBlockersDataSnapshotCommand.execute(eeaEventVO);
    Mockito.verify(dataSetMetabaseRepository, times(1)).findById(1L);

  }

  /**
   * Test execute with blockers.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecuteWithBlockers() throws EEAException {
    data = new HashMap<>();
    data.put("dataset_id", 1L);
    data.put("user", "user1");
    eeaEventVO.setData(data);
    DataSetMetabase datasetMetabase = new DataSetMetabase();
    datasetMetabase.setId(1L);
    datasetMetabase.setDataflowId(1L);
    datasetMetabase.setDataProviderId(1L);
    List<Long> datasetsId = new ArrayList<>();
    datasetsId.add(1L);
    datasetsId.add(2L);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.when(dataSetMetabaseRepository.findById(1L)).thenReturn(Optional.of(datasetMetabase));
    Mockito
        .when(dataSetMetabaseRepository.getDatasetIdsByDataflowIdAndDataProviderId(
            datasetMetabase.getDataflowId(), datasetMetabase.getDataProviderId()))
        .thenReturn(datasetsId);
    Mockito.when(jobControllerZuul.checkEligibilityOfJob(anyString(), anyBoolean(), anyLong(), anyLong(), anyList())).thenReturn(JobStatusEnum.QUEUED);
    JobVO jobVO = new JobVO(Long.valueOf(1), JobTypeEnum.RELEASE, JobStatusEnum.QUEUED, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), null, "test", true, 1L, 1L, 1L, null);
    Mockito.when(jobControllerZuul.save(any(JobVO.class))).thenReturn(jobVO);
    Mockito.when(validationRepository.existsByLevelError(ErrorTypeEnum.BLOCKER)).thenReturn(true);
    checkBlockersDataSnapshotCommand.execute(eeaEventVO);
    Mockito.verify(validationRepository, times(1)).existsByLevelError(ErrorTypeEnum.BLOCKER);

  }

}
