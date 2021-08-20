package org.eea.dataset.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;


/**
 * The Class DisableSqlRulesErrorEventTest.
 */
public class DisableRulesErrorEventTest {


  /** The disable sql rules error event. */
  @InjectMocks
  private DisableRulesErrorEvent disableRulesErrorEvent;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.DISABLE_RULES_ERROR_EVENT, disableRulesErrorEvent.getEventType());
  }

  /**
   * Gets the map test.
   *
   * @return the map test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getMapTest() throws EEAException {
    Assert.assertEquals(5, disableRulesErrorEvent
        .getMap(NotificationVO.builder().user("user").dataflowId(1L).build()).size());
  }

  /**
   * Gets the map test 1.
   *
   * @return the map test 1
   * @throws EEAException the EEA exception
   */
  @Test
  public void getMapFromMinimumDataTest() throws EEAException {
    Assert.assertEquals(5, disableRulesErrorEvent
        .getMap(NotificationVO.builder().user("user").dataflowId(1L).build()).size());
  }
}
