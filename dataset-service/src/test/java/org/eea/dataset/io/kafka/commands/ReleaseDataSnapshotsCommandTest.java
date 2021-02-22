package org.eea.dataset.io.kafka.commands;

import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
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

/**
 * The Class RestoreDataCollectionSnapshotCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReleaseDataSnapshotsCommandTest {

  /** The restore data collection snapshot command. */
  @InjectMocks
  private ReleaseDataSnapshotsCommand releaseDataSnapshotsCommand;

  /** The dataset snapshot service. */
  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The eu dataset service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private DatasetSnapshotService datasetSnapshotService;

  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;


  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The data. */
  private Map<String, Object> data;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.RELEASE_ONEBYONE_COMPLETED_EVENT);
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test execute without blockers.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecuteFinish() throws EEAException {
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
    Mockito.when(dataSetMetabaseRepository.findById(1L)).thenReturn(Optional.of(datasetMetabase));
    Mockito.when(datasetMetabaseService.getLastDatasetValidationForRelease(1L)).thenReturn(null);

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setName("dataflowName");
    dataflowVO.setShowPublicInfo(false);
    Mockito.when(dataflowControllerZuul.findById(Mockito.anyLong())).thenReturn(dataflowVO);
    releaseDataSnapshotsCommand.execute(eeaEventVO);
    Mockito.verify(datasetMetabaseService, times(1)).getLastDatasetValidationForRelease(1L);

  }

  /**
   * Test execute with blockers.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecuteContinue() throws EEAException {
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
    releaseDataSnapshotsCommand.execute(eeaEventVO);
    Mockito.verify(datasetMetabaseService, times(1)).getLastDatasetValidationForRelease(1L);

  }

}
