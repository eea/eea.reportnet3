package org.eea.dataflow.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


/**
 * The Class CreateOrganizationCompletedEvent.
 */
@Component
public class UpdateOrganizationCompletedEvent implements NotificableEventHandler {

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.UPDATE_ORGANIZATION_COMPLETED_EVENT;
  }

  /**
   * Gets the map.
   *
   * @param notificationVO the notification VO
   * @return the map
   * @throws EEAException the EEA exception
   */
  @Override
  public Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException {

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("providerId", notificationVO.getProviderId());
    notification.put("providerLabel", notificationVO.getProviderLabel());

    return notification;
  }
}
