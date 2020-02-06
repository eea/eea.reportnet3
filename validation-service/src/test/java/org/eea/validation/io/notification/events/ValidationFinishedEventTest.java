package org.eea.validation.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
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

public class ValidationFinishedEventTest {

  @InjectMocks
  private ValidationFinishedEvent validationFinishedEvent;

  @Mock
  private DataSetControllerZuul dataSetControllerZuul;

  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseController;

  @Mock
  private DataSetMetabaseVO datasetVO;

  @Mock
  private DataFlowVO dataflowVO;

  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.VALIDATION_FINISHED_EVENT,
        validationFinishedEvent.getEventType());
  }

  @Test
  public void getMapTest1() throws EEAException {
    Assert.assertEquals(5,
        validationFinishedEvent.getMap(NotificationVO.builder().user("user").datasetId(1L)
            .dataflowId(1L).datasetName("datasetName").dataflowName("dataflowName").build())
            .size());
  }

  @Test
  public void getMapTest2() throws EEAException {
    Mockito.when(dataSetControllerZuul.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(datasetVO);
    Mockito.when(datasetVO.getDataSetName()).thenReturn("datasetName");
    Mockito.when(dataflowControllerZuul.findById(Mockito.any())).thenReturn(dataflowVO);
    Mockito.when(dataflowVO.getName()).thenReturn("dataflowName");
    Assert.assertEquals(5, validationFinishedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).build()).size());
  }
}
