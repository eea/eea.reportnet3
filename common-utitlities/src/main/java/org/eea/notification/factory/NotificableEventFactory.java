package org.eea.notification.factory;

import org.eea.kafka.domain.EventType;
import org.eea.notification.event.NotificableEventHandler;

/**
 * A factory for creating NotificableEvent objects.
 */
public interface NotificableEventFactory {

  /**
   * Gets the notificable event handler.
   *
   * @param eventType the event type
   * @return the notificable event handler
   */
  public NotificableEventHandler getNotificableEventHandler(EventType eventType);
}
