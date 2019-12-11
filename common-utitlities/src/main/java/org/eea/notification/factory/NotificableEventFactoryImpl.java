package org.eea.notification.factory;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.eea.kafka.domain.EventType;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificableEventFactoryImpl implements NotificableEventFactory {

  @Autowired(required = false)
  private Set<NotificableEventHandler> events;

  private Map<EventType, NotificableEventHandler> notificableEvents;

  @PostConstruct
  private void init() {
    notificableEvents = new EnumMap<>(EventType.class);
    if (null != events) {
      events.stream().forEach(event -> notificableEvents.put(event.getEventType(), event));
    }
  }

  @Override
  public NotificableEventHandler getNotificableEventHandler(EventType eventType) {
    NotificableEventHandler notificableEventHandler = null;
    if (notificableEvents.containsKey(eventType)) {
      notificableEventHandler = notificableEvents.get(eventType);
    }
    return notificableEventHandler;
  }
}
