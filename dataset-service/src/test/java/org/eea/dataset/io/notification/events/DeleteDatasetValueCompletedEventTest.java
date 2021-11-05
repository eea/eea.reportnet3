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

public class DeleteDatasetValueCompletedEventTest {

  /** The copy dataset schema completed event. */
  @InjectMocks
  private DeleteDatasetValueCompletedEvent deleteDatasetValueCompletedEvent;

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
    Assert.assertEquals(EventType.DELETE_DATASET_DATA_COMPLETED_EVENT,
        deleteDatasetValueCompletedEvent.getEventType());
  }

  /**
   * Test get map.
   *
   * @throws EEAException the EEA exception
   */
  // @Test
  public void testGetMap() throws EEAException {
    Assert.assertEquals("datasetName",
        deleteDatasetValueCompletedEvent
            .getMap(NotificationVO.builder().user("user").datasetId(1L).dataflowId(1L)
                .datasetName("datasetName").error("error").dataflowName("dataflowName").build())
            .get("datasetName"));
  }

  /**
   * Test get map nulls.
   *
   * @throws EEAException the EEA exception
   */
  // @Test
  public void testGetMapNulls() throws EEAException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.any()))
        .thenReturn(new DataSetMetabaseVO());
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any()))
        .thenReturn(new DataFlowVO());
    Assert.assertNull(deleteDatasetValueCompletedEvent
        .getMap(NotificationVO.builder().user("user").datasetId(1L).error("error").build())
        .get("datasetName"));
  }
}
