package org.eea.dataset.io.notification.events;

import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
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

public class DeleteDatasetSchemaCompletedEventTest {

  /** The copy dataset schema completed event. */
  @InjectMocks
  private DeleteDatasetSchemaCompletedEvent deleteDatasetSchemaCompletedEvent;

  /** The dataset metabase service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The dataflow controller zuul. */
  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The dataset service. */
  @Mock
  private DatasetService datasetService;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test get event type.
   */
  @Test
  public void testGetEventType() {
    Assert.assertEquals(EventType.DELETE_DATASET_SCHEMA_COMPLETED_EVENT,
        deleteDatasetSchemaCompletedEvent.getEventType());
  }

  /**
   * Test get map.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetMap() throws EEAException {

    DataFlowVO dataFlowVO = new DataFlowVO();
    dataFlowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(1L)).thenReturn(dataFlowVO);
    Assert.assertEquals("datasetName",
        deleteDatasetSchemaCompletedEvent
            .getMap(NotificationVO.builder().user("user").datasetId(1L).dataflowId(1L)
                .datasetName("datasetName").error("error").dataflowName("dataflowName").build())
            .get("datasetName"));
  }

  /**
   * Test get map nulls.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetMapNulls() throws EEAException {
    DataFlowVO dataFlowVO = new DataFlowVO();
    dataFlowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(1L)).thenReturn(dataFlowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.any()))
        .thenReturn(new DataSetMetabaseVO());
    Assert.assertNull(deleteDatasetSchemaCompletedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).error("error").build())
        .get("datasetName"));
  }
}
