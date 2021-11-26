package org.eea.communication.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.eea.communication.mapper.SystemNotificationMapper;
import org.eea.communication.mapper.UserNotificationMapper;
import org.eea.communication.persistence.SystemNotification;
import org.eea.communication.persistence.UserNotification;
import org.eea.communication.persistence.repository.SystemNotificationRepository;
import org.eea.communication.persistence.repository.UserNotificationRepository;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.communication.SystemNotificationVO;
import org.eea.interfaces.vo.communication.UserNotificationVO;
import org.eea.kafka.domain.EventType;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;


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

  /** The user notification repository. */
  @Mock
  private UserNotificationRepository userNotificationRepository;

  /** The user notification mapper. */
  @Mock
  private UserNotificationMapper userNotificationMapper;

  /** The system notification repository. */
  @Mock
  private SystemNotificationRepository systemNotificationRepository;

  /** The system notification mapper. */
  @Mock
  private SystemNotificationMapper systemNotificationMapper;

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
    Assert.assertTrue(notificationServiceImpl.send("user",
        EventType.IMPORT_REPORTING_COMPLETED_EVENT, new HashMap<String, Object>()));
  }

  /**
   * Send test 2.
   */
  @Test
  public void sendTest2() {
    Mockito.doNothing().when(template).convertAndSendToUser(Mockito.anyString(),
        Mockito.anyString(), Mockito.any());
    Assert.assertFalse(notificationServiceImpl.send(null,
        EventType.IMPORT_REPORTING_COMPLETED_EVENT, new HashMap<String, Object>()));
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

  /**
   * Find user notifications by user paginated test.
   */
  @Test
  public void findUserNotificationsByUserPaginatedTest() {
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken("user", "123", new HashSet<>());
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userIdTest");
    authentication.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    assertNotNull(notificationServiceImpl.findUserNotificationsByUserPaginated(0, 10));
  }

  @Test
  public void createUserNotificationTest() throws EEAException {
    UserNotificationVO userNotificationVO = new UserNotificationVO();
    UserNotification userNotification = new UserNotification();
    Mockito.when(userNotificationMapper.classToEntity(Mockito.any())).thenReturn(userNotification);

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken("user", "123", new HashSet<>());
    Map<String, String> details = new HashMap<>();
    details.put(AuthenticationDetails.USER_ID, "userIdTest");
    authentication.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    Mockito.when(userNotificationRepository.save(Mockito.any())).thenReturn(userNotification);
    notificationServiceImpl.createUserNotification(userNotificationVO);
    Mockito.verify(userNotificationRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void createSystemNotificationTest() throws EEAException {
    SystemNotificationVO systemNotificationVO = new SystemNotificationVO();
    SystemNotification systemNotification = new SystemNotification();
    systemNotification.setMessage("message");
    Mockito.when(systemNotificationMapper.classToEntity(Mockito.any()))
        .thenReturn(systemNotification);

    Mockito.when(systemNotificationRepository.save(Mockito.any())).thenReturn(systemNotification);
    notificationServiceImpl.createSystemNotification(systemNotificationVO);
    Mockito.verify(systemNotificationRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void deleteSystemNotificationTest() throws EEAException {
    Mockito.doNothing().when(systemNotificationRepository)
        .deleteSystemNotficationById(Mockito.anyString());
    notificationServiceImpl.deleteSystemNotification(Mockito.any());
    Mockito.verify(systemNotificationRepository, times(1))
        .deleteSystemNotficationById(Mockito.any());
  }

  @Test
  public void deleteSystemNotificationNotEmptyTest() throws EEAException {
    List<SystemNotification> listSystemNotification = new ArrayList<>();
    SystemNotification systemNotification = new SystemNotification();
    listSystemNotification.add(systemNotification);
    Mockito.when(systemNotificationRepository.findByEnabledTrue())
        .thenReturn(listSystemNotification);
    Mockito.doNothing().when(systemNotificationRepository)
        .deleteSystemNotficationById(Mockito.anyString());
    notificationServiceImpl.deleteSystemNotification(Mockito.any());
    Mockito.verify(systemNotificationRepository, times(1))
        .deleteSystemNotficationById(Mockito.any());
  }

  @Test
  public void updateSystemNotificationTest() throws EEAException {
    SystemNotificationVO systemNotificationVO = new SystemNotificationVO();
    SystemNotification systemNotification = new SystemNotification();
    systemNotification.setMessage("message");
    Mockito.when(systemNotificationMapper.classToEntity(Mockito.any()))
        .thenReturn(systemNotification);

    Mockito.doNothing().when(systemNotificationRepository)
        .updateSystemNotficationById(Mockito.any());
    notificationServiceImpl.updateSystemNotification(systemNotificationVO);
    Mockito.verify(systemNotificationRepository, times(1))
        .updateSystemNotficationById(Mockito.any());
    Mockito.verify(systemNotificationRepository, times(1)).findByEnabledTrue();
  }

  @Test
  public void updateSystemNotificationNotEmptyTest() throws EEAException {
    SystemNotificationVO systemNotificationVO = new SystemNotificationVO();
    SystemNotification systemNotification = new SystemNotification();
    systemNotification.setMessage("message");
    List<SystemNotification> listSystemNotification = new ArrayList<>();
    listSystemNotification.add(systemNotification);
    Mockito.when(systemNotificationMapper.classToEntity(Mockito.any()))
        .thenReturn(systemNotification);
    Mockito.when(systemNotificationRepository.findByEnabledTrue())
        .thenReturn(listSystemNotification);

    Mockito.doNothing().when(systemNotificationRepository)
        .updateSystemNotficationById(Mockito.any());
    notificationServiceImpl.updateSystemNotification(systemNotificationVO);
    Mockito.verify(systemNotificationRepository, times(1))
        .updateSystemNotficationById(Mockito.any());
    Mockito.verify(systemNotificationRepository, times(1)).findByEnabledTrue();
  }

  @Test
  public void updateSystemNotificationNotEnabledTest() throws EEAException {
    SystemNotificationVO systemNotificationVO = new SystemNotificationVO();
    SystemNotification systemNotification = new SystemNotification();
    systemNotification.setMessage("message");
    systemNotification.setEnabled(false);

    Mockito.when(systemNotificationMapper.classToEntity(Mockito.any()))
        .thenReturn(systemNotification);

    Mockito.doNothing().when(systemNotificationRepository)
        .updateSystemNotficationById(Mockito.any());
    notificationServiceImpl.updateSystemNotification(systemNotificationVO);
    Mockito.verify(systemNotificationRepository, times(1))
        .updateSystemNotficationById(Mockito.any());
    Mockito.verify(systemNotificationRepository, times(1)).findByEnabledTrue();
  }

  @Test
  public void updateSystemNotificationEnabledTest() throws EEAException {
    SystemNotificationVO systemNotificationVO = new SystemNotificationVO();
    SystemNotification systemNotification = new SystemNotification();
    systemNotification.setMessage("message");
    systemNotification.setEnabled(true);

    Mockito.when(systemNotificationMapper.classToEntity(Mockito.any()))
        .thenReturn(systemNotification);

    Mockito.doNothing().when(systemNotificationRepository)
        .updateSystemNotficationById(Mockito.any());
    notificationServiceImpl.updateSystemNotification(systemNotificationVO);
    Mockito.verify(systemNotificationRepository, times(1))
        .updateSystemNotficationById(Mockito.any());
  }

  @Test
  public void findSystemNotificationsTest() {
    List<SystemNotificationVO> listSystemNotificationVO = new ArrayList<>();
    assertEquals(listSystemNotificationVO, notificationServiceImpl.findSystemNotifications());
    Mockito.verify(systemNotificationRepository, times(1)).findByEnabledTrue();
  }

  @Test
  public void testCheckAnySystemNotificationEnabled() {
    notificationServiceImpl.checkAnySystemNotificationEnabled();

    Mockito.verify(systemNotificationRepository, times(1)).existsByEnabledTrue();
  }
}
