package org.eea.dataset.io.notification.events;

import org.eea.dataset.service.DatasetMetabaseService;
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

/**
 * The Class ExportDatasetFailedEventTest.
 */
public class ExportDatasetFailedEventTest {

  @InjectMocks
  private ExportDatasetFailedEvent exportDatasetFailedEvent;

  @Mock
  private DatasetMetabaseService datasetService;

  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.EXPORT_DATASET_FAILED_EVENT,
        exportDatasetFailedEvent.getEventType());
  }

  @Test
  public void getMapTest() throws EEAException {
    Mockito.when(datasetService.findDatasetMetabase(Mockito.any()))
        .thenReturn(new DataSetMetabaseVO());
    Assert.assertEquals(7, exportDatasetFailedEvent.getMap(NotificationVO.builder().user("user")
        .dataflowId(1L).dataflowName("dataflowName").error("error").build()).size());
  }

  @Test
  public void getMapFromMinimumDataTest() throws EEAException {
    Mockito.when(datasetService.findDatasetMetabase(Mockito.any()))
        .thenReturn(new DataSetMetabaseVO());
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any()))
        .thenReturn(new DataFlowVO());
    Assert.assertEquals(7, exportDatasetFailedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).error("error").build()).size());
  }

}
