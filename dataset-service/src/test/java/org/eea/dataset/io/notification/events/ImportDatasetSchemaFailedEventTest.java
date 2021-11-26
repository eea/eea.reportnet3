package org.eea.dataset.io.notification.events;

import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
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
 * The Class ImportDatasetSchemaFailedEventTest.
 */
public class ImportDatasetSchemaFailedEventTest {

  @InjectMocks
  private ImportDatasetSchemaFailedEvent importDatasetSchemaFailedEvent;

  @Mock
  private DatasetService datasetService;

  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.IMPORT_DATASET_SCHEMA_FAILED_EVENT,
        importDatasetSchemaFailedEvent.getEventType());
  }

  @Test
  public void getMapTest() throws EEAException {
    Assert.assertEquals(4, importDatasetSchemaFailedEvent.getMap(NotificationVO.builder()
        .user("user").dataflowId(1L).dataflowName("dataflowName").error("error").build()).size());
  }

  @Test
  public void getMapFromMinimumDataTest() throws EEAException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any()))
        .thenReturn(new DataFlowVO());
    Assert.assertEquals(4, importDatasetSchemaFailedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).error("error").build()).size());
  }
}
