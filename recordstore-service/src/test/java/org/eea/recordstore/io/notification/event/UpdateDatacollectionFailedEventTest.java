package org.eea.recordstore.io.notification.event;

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

/** The Class UpdateDatacollectionFailedEventTest. */
public class UpdateDatacollectionFailedEventTest {

  /** The update datacollection failed event. */
  @InjectMocks
  private UpdateDatacollectionFailedEvent updateDatacollectionFailedEvent;

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
    Assert.assertEquals(EventType.UPDATE_DATACOLLECTION_FAILED_EVENT,
        updateDatacollectionFailedEvent.getEventType());
  }

  /**
   * Gets the map test 1.
   *
   * @return the map test 1
   * @throws EEAException the EEA exception
   */
  @Test
  public void getMapTest1() throws EEAException {
    Assert.assertEquals(4, updateDatacollectionFailedEvent.getMap(NotificationVO.builder()
        .user("user").dataflowId(1L).dataflowName("dataflowName").error("error").build()).size());
  }

  /**
   * Gets the map test 2.
   *
   * @return the map test 2
   * @throws EEAException the EEA exception
   */
  @Test
  public void getMapTest2() throws EEAException {
    Mockito.when(dataflowControllerZuul.findById(Mockito.any())).thenReturn(dataflowVO);
    Mockito.when(dataflowVO.getName()).thenReturn("dataflowName");
    Assert.assertEquals(4,
        updateDatacollectionFailedEvent
            .getMap(NotificationVO.builder().user("user").dataflowId(1L).error("error").build())
            .size());
  }
}
