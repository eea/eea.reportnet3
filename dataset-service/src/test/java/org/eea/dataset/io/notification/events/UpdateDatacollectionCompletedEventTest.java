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

/** The Class UpdateDatacollectionCompletedEventTest. */
public class UpdateDatacollectionCompletedEventTest {

  /** The update datacollection completed event. */
  @InjectMocks
  private UpdateDatacollectionCompletedEvent updateDatacollectionCompletedEvent;

  /** The dataset service. */
  @Mock
  private DatasetService datasetService;

  /** The dataset metabase service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The dataset metabase VO. */
  @Mock
  private DataSetMetabaseVO datasetMetabaseVO;

  /** The dataflow controller zuul. */
  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The dataflow VO. */
  @Mock
  private DataFlowVO dataflowVO;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.UPDATE_DATACOLLECTION_COMPLETED_EVENT,
        updateDatacollectionCompletedEvent.getEventType());
  }

  /**
   * Gets the map test 1.
   *
   * @return the map test 1
   * @throws EEAException the EEA exception
   */
  @Test
  public void getMapTest() throws EEAException {
    Assert.assertEquals(3, updateDatacollectionCompletedEvent.getMap(
        NotificationVO.builder().user("user").dataflowId(1L).dataflowName("dataflowName").build())
        .size());
  }

  /**
   * Gets the map test 2.
   *
   * @return the map test 2
   * @throws EEAException the EEA exception
   */
  @Test
  public void getMapFromMinimumDataTest() throws EEAException {
    Mockito.when(dataflowControllerZuul.findById(Mockito.any())).thenReturn(dataflowVO);
    Assert.assertEquals(3, updateDatacollectionCompletedEvent
        .getMap(NotificationVO.builder().user("user").dataflowId(1L).build()).size());
  }
}
