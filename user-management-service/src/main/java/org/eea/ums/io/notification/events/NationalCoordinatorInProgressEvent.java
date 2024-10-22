package org.eea.ums.io.notification.events;

import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.eea.kafka.domain.EventType.NATIONAL_COORDINATOR_ANOTHER_PROCESS_IN_PROGRESS_EVENT;

@Component
public class NationalCoordinatorInProgressEvent implements NotificableEventHandler {

  @Override
  public EventType getEventType() {
    return NATIONAL_COORDINATOR_ANOTHER_PROCESS_IN_PROGRESS_EVENT;
  }

  @Override
  public Map<String, Object> getMap(NotificationVO notificationVO) {
    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    return notification;
  }
}