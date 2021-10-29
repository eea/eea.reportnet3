package org.eea.kafka.utils;

import java.util.HashMap;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.io.KafkaSender;
import org.eea.notification.event.NotificableEventHandler;
import org.eea.notification.factory.NotificableEventFactory;
import org.eea.thread.ThreadPropertiesManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * The Class KafkaSenderUtilsTest.
 */
public class KafkaSenderUtilsTest {

  /** The kafka sender utils. */
  @InjectMocks
  private KafkaSenderUtils kafkaSenderUtils;

  /** The notificable event factory. */
  @Mock
  private NotificableEventFactory notificableEventFactory;

  /** The notificable event handler. */
  @Mock
  private NotificableEventHandler notificableEventHandler;

  /** The kafka sender. */
  @Mock
  private KafkaSender kafkaSender;

  /** The notification controller zuul. */
  @Mock
  private NotificationControllerZuul notificationControllerZuul;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    ThreadPropertiesManager.setVariable("user", "user");
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Release dataset kafka event test.
   */
  @Test
  public void releaseDatasetKafkaEventTest() {
    kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, 1l);
    Mockito.verify(kafkaSender, Mockito.times(1)).sendMessage(Mockito.any());
  }

  /**
   * Release kafka event test.
   */
  @Test
  public void releaseKafkaEventTest() {
    kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, new HashMap<>());
    Mockito.verify(kafkaSender, Mockito.times(1)).sendMessage(Mockito.any());
  }

  /**
   * Release notificable kafka event test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void releaseNotificableKafkaEventTest() throws EEAException {
    Mockito.when(notificableEventFactory.getNotificableEventHandler(Mockito.any()))
        .thenReturn(notificableEventHandler);
    Mockito.when(notificableEventHandler.getMap(Mockito.any())).thenReturn(null);
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.any());
    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION,
        new HashMap<>(), new NotificationVO());
    Mockito.verify(kafkaSender, Mockito.times(1)).sendMessage(Mockito.any());
  }
}
