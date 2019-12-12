package org.eea.notification.factory;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.eea.kafka.domain.EventType;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class NotificableEventFactoryImpl.
 */
@Component
public class NotificableEventFactoryImpl implements NotificableEventFactory {

  /** The events. */
  @Autowired(required = false)
  private Set<NotificableEventHandler> events;

  /** The notificable events. */
  private Map<EventType, NotificableEventHandler> notificableEvents;

  /**
   * Inits the.
   */
  @PostConstruct
  private void init() {
    notificableEvents = new EnumMap<>(EventType.class);
    if (null != events) {
      events.stream().forEach(event -> notificableEvents.put(event.getEventType(), event));
    }
  }

  /**
   * Gets the notificable event handler.
   *
   * @param eventType the event type
   * @return the notificable event handler
   */
  @Override
  public NotificableEventHandler getNotificableEventHandler(EventType eventType) {
    NotificableEventHandler notificableEventHandler = null;
    if (notificableEvents.containsKey(eventType)) {
      notificableEventHandler = notificableEvents.get(eventType);
    }
    return notificableEventHandler;
  }
}
