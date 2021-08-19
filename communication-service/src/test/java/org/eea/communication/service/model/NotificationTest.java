package org.eea.communication.service.model;

import org.eea.kafka.domain.EventType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

/**
 * The Class NotificationTest.
 */
public class NotificationTest {

  /** The notificication. */
  @InjectMocks
  private Notification notificication;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Notification test.
   */
  @Test
  public void notificationTest() {
    Notification notification = new Notification(EventType.IMPORT_REPORTING_COMPLETED_EVENT, null);
    Assert.assertEquals(EventType.IMPORT_REPORTING_COMPLETED_EVENT, notification.getType());
    Assert.assertNull(notification.getContent());
  }
}
