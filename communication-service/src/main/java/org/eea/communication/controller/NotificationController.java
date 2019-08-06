package org.eea.communication.controller;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {

  @Autowired
  private SimpMessagingTemplate template;

  public void sendNotification(EEAEventVO eeaEventVO) throws EEAException {

    if (eeaEventVO != null && eeaEventVO.getData() != null) {

      // String userId = (String) eeaEventVO.getData().get("userId");
      String userId = "user1";
      EventType message = eeaEventVO.getEventType();

      if (userId != null && message != null) {
        template.convertAndSendToUser(userId, "/queue/notifications", new Notification(message));
        return;
      }
    }

    throw new EEAException("Bad fulfilled event");
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
