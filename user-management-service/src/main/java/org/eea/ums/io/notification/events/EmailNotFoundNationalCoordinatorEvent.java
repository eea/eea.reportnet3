package org.eea.ums.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.eea.kafka.domain.EventType.EMAIL_NOT_FOUND_ERROR;

@Component
public class EmailNotFoundNationalCoordinatorEvent implements NotificableEventHandler {
  @Override
  public EventType getEventType() {
    return EMAIL_NOT_FOUND_ERROR;
  }

  @Override
  public Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException {
    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    return notification;
  }
}
