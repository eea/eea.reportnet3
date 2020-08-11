package org.eea.dataset.io.kafka.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.EUDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
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

@RunWith(MockitoJUnitRunner.class)
public class RestoreDataCollectionSnapshotCommandTest {

  @InjectMocks
  private RestoreDataCollectionSnapshotCommand restoreDataCollectionSnapshotCommand;

  /** The dataset snapshot service. */
  @Mock
  private DatasetSnapshotService datasetSnapshotService;

  /** The eu dataset service. */
  @Mock
  private EUDatasetService euDatasetService;

  /** The dataset metabase service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The data. */
  private Map<String, Object> data;

  @Before
  public void initMocks() {
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.RESTORE_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT);
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testExecute() throws EEAException {
    data = new HashMap<>();
    data.put("dataset_id", 1L);
    data.put("user", "user1");
    eeaEventVO.setData(data);
    when(datasetMetabaseService.findDatasetMetabase(Mockito.any()))
        .thenReturn(new DataSetMetabaseVO());
    when(euDatasetService.removeLocksRelatedToPopulateEU(Mockito.any())).thenReturn(Boolean.TRUE);
    restoreDataCollectionSnapshotCommand.execute(eeaEventVO);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

  }

  @Test
  public void testExecuteSecondRun() throws EEAException {
    data = new HashMap<>();
    data.put("dataset_id", 1L);
    data.put("user", "user1");
    eeaEventVO.setData(data);
    when(datasetMetabaseService.findDatasetMetabase(Mockito.any()))
        .thenReturn(new DataSetMetabaseVO());
    when(euDatasetService.removeLocksRelatedToPopulateEU(Mockito.any())).thenReturn(Boolean.FALSE);
    restoreDataCollectionSnapshotCommand.execute(eeaEventVO);
    Mockito.verify(kafkaSenderUtils, times(0)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

  }

  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.RESTORE_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT,
        restoreDataCollectionSnapshotCommand.getEventType());
  }

}
