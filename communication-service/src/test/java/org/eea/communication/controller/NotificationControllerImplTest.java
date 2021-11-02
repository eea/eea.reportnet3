package org.eea.communication.controller;

import static org.junit.Assert.assertNull;
import org.eea.communication.mapper.UserNotificationMapper;
import org.eea.communication.service.NotificationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NotificationControllerImplTest {


  @InjectMocks
  private NotificationControllerImpl notificationControllerImpl;

  @Mock
  private NotificationService notificationService;

  @Mock
  private UserNotificationMapper userNotificationMapper;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testFindUserNotificationsByUser() {
    assertNull(notificationControllerImpl.findUserNotificationsByUser(0, 10));
  }

}
