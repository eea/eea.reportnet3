package org.eea.notification.factory;

import org.eea.kafka.domain.EventType;
import org.eea.notification.event.NotificableEventHandler;

public interface NotificableEventFactory {

  public NotificableEventHandler getNotificableEventHandler(EventType eventType);
}
