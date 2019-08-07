package org.eea.communication.service.impl;

import org.eea.communication.service.NotificationService;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service("notificationService")
public class NotificationServiceImpl implements NotificationService {

  @Autowired
  private SimpMessagingTemplate template;

  private final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

  @Override
  public boolean sendNotification(EEAEventVO eeaEventVO) {

    if (eeaEventVO != null && eeaEventVO.getData() != null) {

      String userId = "user1";
      // String userId = (String) eeaEventVO.getData().get("userId");
      EventType message = eeaEventVO.getEventType();

      if (userId != null && !userId.isEmpty() && message != null) {
        logger.info("Event sent to user: user={}, event={}", userId, message);
        template.convertAndSendToUser(userId, "/queue/notifications", new Notification(message));
        return true;
      }
    }

    return false;
  }

  class Notification {
    private String content;

    public Notification(EventType eventType) {
      this.content = eventType.toString();
    }

    public String getContent() {
      return content;
    }
  }
}
