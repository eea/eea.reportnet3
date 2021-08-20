package org.eea.dataset.io.notification.events;

import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AddDatasetSnapshotFailedEventTest {

  @InjectMocks
  private AddDatasetSnapshotFailedEvent addDatasetSnapshotFailedEvent;

  @Mock
  private DatasetService datasetService;

  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  @Mock
  private DataSetMetabaseVO datasetMetabaseVO;

  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  @Mock
  private DataFlowVO dataflowVO;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.ADD_DATASET_SNAPSHOT_FAILED_EVENT,
        addDatasetSnapshotFailedEvent.getEventType());
  }

  @Test
  public void getMapTest() throws EEAException {
    Assert.assertEquals(6,
        addDatasetSnapshotFailedEvent
            .getMap(NotificationVO.builder().user("user").datasetId(1L).dataflowId(1L)
                .datasetName("datasetName").dataflowName("dataflowName").error("error").build())
            .size());
  }

  @Test
  public void getMapFromMinimumDataTest() throws EEAException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.any()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    Mockito.when(datasetMetabaseVO.getDataSetName()).thenReturn("datasetName");
    Mockito.when(dataflowVO.getName()).thenReturn("dataflowName");
    Assert.assertEquals(6, addDatasetSnapshotFailedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).error("error").build()).size());
  }
}
