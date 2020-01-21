package org.eea.notification.event;

import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;

/**
 * The Interface NotificableEventHandler.
 */
public interface NotificableEventHandler {

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  EventType getEventType();

  /**
   * Gets the map.
   *
   * @param notificationVO the notification VO
   * @return the map
   * @throws EEAException the EEA exception
   */
  Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException;
}
