package org.eea.communication.service.impl;

import java.util.Map;
import org.eea.communication.service.NotificationService;
import org.eea.communication.service.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * The Class NotificationServiceImpl.
 */
@Service("notificationService")
public class NotificationServiceImpl implements NotificationService {

  /**
   * The messaging template.
   */
  @Autowired
  private SimpMessagingTemplate template;

  /**
   * The logger.
   */
  private final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

  /**
   * Send a message to the given user.
   *
   * @param userId the user that should receive the message.
   * @param message the message to send to the user.
   *
   * @return true, if successful
   */
  @Override
  public boolean send(String user, Map<?, ?> notification) {
    if (user != null && !user.isEmpty() && notification != null) {
      logger.info("Notification sent to user: userId={}, message={}", user, notification);
      template.convertAndSendToUser(user, "/queue/notifications", new Notification(notification));
      return true;
    }
    return false;
  }


}
