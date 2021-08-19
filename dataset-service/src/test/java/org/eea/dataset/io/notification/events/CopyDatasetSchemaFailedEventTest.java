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

public class CopyDatasetSchemaFailedEventTest {

  /** The copy dataset schema completed event. */
  @InjectMocks
  private CopyDatasetSchemaFailedEvent copyDatasetSchemaFailedEvent;

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
    Assert.assertEquals(EventType.COPY_DATASET_SCHEMA_FAILED_EVENT,
        copyDatasetSchemaFailedEvent.getEventType());
  }

  /**
   * Test get map.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetMap() throws EEAException {
    Assert.assertEquals("dataflowName",
        copyDatasetSchemaFailedEvent
            .getMap(NotificationVO.builder().user("user").datasetId(1L).dataflowId(1L)
                .datasetName("datasetName").dataflowName("dataflowName").error("error").build())
            .get("dataflowName"));
  }

  /**
   * Test get map nulls.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetMapNulls() throws EEAException {
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any()))
        .thenReturn(new DataFlowVO());
    Assert.assertNull(copyDatasetSchemaFailedEvent.getMap(NotificationVO.builder().user("user")
        .datasetId(1L).datasetName("datasetName").error("error").build()).get("dataflowName"));
  }

}
