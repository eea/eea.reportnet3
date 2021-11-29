package org.eea.communication.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.List;
import org.eea.communication.mapper.UserNotificationMapper;
import org.eea.communication.service.NotificationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.communication.SystemNotificationVO;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

  @Test
  public void testCreateUserNotification() throws EEAException {
    Mockito.doNothing().when(notificationService).createUserNotification(Mockito.any());
    notificationControllerImpl.createUserNotification(Mockito.any());
    Mockito.verify(notificationService, times(1)).createUserNotification(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testCreateUserNotificationEEAException() throws EEAException {
    Mockito.doThrow(EEAException.class).when(notificationService)
        .createUserNotification(Mockito.any());
    try {
      notificationControllerImpl.createUserNotification(Mockito.any());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test
  public void testCreateUserNotificationPrivate() throws EEAException {
    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    Mockito.doNothing().when(notificationService).createUserNotification(Mockito.any());
    notificationControllerImpl.createUserNotificationPrivate(Mockito.anyString(),
        userNotificationContentVO);
    Mockito.verify(notificationService, times(1)).createUserNotification(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testCreateUserNotificationPrivateEEAException() throws EEAException {
    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    Mockito.doThrow(EEAException.class).when(notificationService)
        .createUserNotification(Mockito.any());
    try {
      notificationControllerImpl.createUserNotificationPrivate(Mockito.anyString(),
          userNotificationContentVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test
  public void testCreateSystemNotification() throws EEAException {
    Mockito.doNothing().when(notificationService).createSystemNotification(Mockito.any());
    notificationControllerImpl.createSystemNotification(Mockito.any());
    Mockito.verify(notificationService, times(1)).createSystemNotification(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testCreateSystemNotificationEEAException() throws EEAException {
    Mockito.doThrow(EEAException.class).when(notificationService)
        .createSystemNotification(Mockito.any());
    try {
      notificationControllerImpl.createSystemNotification(Mockito.any());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test
  public void testDeleteSystemNotification() throws EEAException {
    Mockito.doNothing().when(notificationService).deleteSystemNotification(Mockito.any());
    notificationControllerImpl.deleteSystemNotification(Mockito.any());
    Mockito.verify(notificationService, times(1)).deleteSystemNotification(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testDeleteSystemNotificationEEAException() throws EEAException {
    Mockito.doThrow(EEAException.class).when(notificationService)
        .deleteSystemNotification(Mockito.any());
    try {
      notificationControllerImpl.deleteSystemNotification(Mockito.any());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test
  public void testUpdateSystemNotification() throws EEAException {
    Mockito.doNothing().when(notificationService).updateSystemNotification(Mockito.any());
    notificationControllerImpl.updateSystemNotification(Mockito.any());
    Mockito.verify(notificationService, times(1)).updateSystemNotification(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testUpdateSystemNotificationEEAException() throws EEAException {
    Mockito.doThrow(EEAException.class).when(notificationService)
        .updateSystemNotification(Mockito.any());
    try {
      notificationControllerImpl.updateSystemNotification(Mockito.any());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test
  public void testFindSystemNotifications() {
    List<SystemNotificationVO> listSystemNotificationVO = new ArrayList<>();
    assertEquals(listSystemNotificationVO, notificationControllerImpl.findSystemNotifications());
  }

  @Test
  public void testCheckAnySystemNotificationEnabled() {
    notificationControllerImpl.checkAnySystemNotificationEnabled();

    Mockito.verify(notificationService, times(1)).checkAnySystemNotificationEnabled();
  }
}
