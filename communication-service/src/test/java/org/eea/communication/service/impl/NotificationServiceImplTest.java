package org.eea.communication.service.impl;

import static org.junit.Assert.assertNotNull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.eea.communication.mapper.UserNotificationMapper;
import org.eea.communication.persistence.repository.UserNotificationRepository;
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
}
