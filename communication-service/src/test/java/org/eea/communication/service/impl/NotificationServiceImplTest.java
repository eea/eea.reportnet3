package org.eea.communication.service.impl;

import java.util.HashMap;
import org.eea.kafka.domain.EventType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;


/**
 * The Class NotificationServiceImplTest.
 */
public class NotificationServiceImplTest {

  /** The notification service impl. */
  @InjectMocks
  private NotificationServiceImpl notificationServiceImpl;

  /** The template. */
  @Mock
  private SimpMessagingTemplate template;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Send test 1.
   */
  @Test
  public void sendTest1() {
    Mockito.doNothing().when(template).convertAndSendToUser(Mockito.anyString(),
        Mockito.anyString(), Mockito.any());
    Assert.assertTrue(notificationServiceImpl.send("user", EventType.IMPORT_REPORTING_COMPLETED_EVENT,
        new HashMap<String, Object>()));
  }

  /**
   * Send test 2.
   */
  @Test
  public void sendTest2() {
    Mockito.doNothing().when(template).convertAndSendToUser(Mockito.anyString(),
        Mockito.anyString(), Mockito.any());
    Assert.assertFalse(notificationServiceImpl.send(null, EventType.IMPORT_REPORTING_COMPLETED_EVENT,
        new HashMap<String, Object>()));
  }

  /**
   * Send test 3.
   */
  @Test
  public void sendTest3() {
    Mockito.doNothing().when(template).convertAndSendToUser(Mockito.anyString(),
        Mockito.anyString(), Mockito.any());
    Assert.assertFalse(notificationServiceImpl.send("", EventType.IMPORT_REPORTING_COMPLETED_EVENT,
        new HashMap<String, Object>()));
  }

  /**
   * Send test 4.
   */
  @Test
  public void sendTest4() {
    Mockito.doNothing().when(template).convertAndSendToUser(Mockito.anyString(),
        Mockito.anyString(), Mockito.any());
    Assert.assertFalse(
        notificationServiceImpl.send("user", EventType.IMPORT_REPORTING_COMPLETED_EVENT, null));
  }
}
