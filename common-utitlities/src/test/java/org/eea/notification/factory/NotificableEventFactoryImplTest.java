package org.eea.notification.factory;

import java.io.IOException;
import java.util.Map;
import org.eea.kafka.domain.EventType;
import org.eea.notification.event.NotificableEventHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


/**
 * The Class NotificableEventFactoryImplTest.
 */
public class NotificableEventFactoryImplTest {

  /** The notificable event factory impl. */
  @InjectMocks
  private NotificableEventFactoryImpl notificableEventFactoryImpl;

  /** The notificable events. */
  @Mock
  private Map<EventType, NotificableEventHandler> notificableEvents;

  /** The notificable event handler. */
  @Mock
  private NotificableEventHandler notificableEventHandler;

  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Gets the notificable event handler test 1.
   *
   * @return the notificable event handler test 1
   */
  @Test
  public void getNotificableEventHandlerTest1() {
    Mockito.when(notificableEvents.containsKey(Mockito.any())).thenReturn(false);
    notificableEventFactoryImpl.getNotificableEventHandler(EventType.IMPORT_REPORTING_COMPLETED_EVENT);
    Mockito.when(notificableEventHandler.getEventType()).thenReturn(null);
    Assert.assertNull(notificableEventHandler.getEventType());
  }

  /**
   * Gets the notificable event handler test 2.
   *
   * @return the notificable event handler test 2
   */
  @Test
  public void getNotificableEventHandlerTest2() {
    Mockito.when(notificableEvents.containsKey(Mockito.any())).thenReturn(true);
    Mockito.when(notificableEvents.get(Mockito.any())).thenReturn(notificableEventHandler);
    Mockito.when(notificableEventHandler.getEventType())
        .thenReturn(EventType.IMPORT_REPORTING_COMPLETED_EVENT);
    notificableEventFactoryImpl.getNotificableEventHandler(EventType.IMPORT_REPORTING_COMPLETED_EVENT);
    Assert.assertEquals(EventType.IMPORT_REPORTING_COMPLETED_EVENT,
        notificableEventHandler.getEventType());
  }
}
