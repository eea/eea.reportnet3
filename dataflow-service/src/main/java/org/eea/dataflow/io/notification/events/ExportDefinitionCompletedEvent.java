package org.eea.dataflow.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ExportSchemaInformationFailedEvent.
 */
@Component
public class ExportDefinitionCompletedEvent implements NotificableEventHandler {

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.EXPORT_DEFINITION_COMPLETED_EVENT;
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
    Long dataflowId = notificationVO.getDataflowId();
    String nameFile = notificationVO.getFileName();


    Map<String, Object> notification = new HashMap<>();
    notification.put("datasetId", notificationVO.getDatasetId());
    notification.put("fileName", notificationVO.getFileName());
    return notification;
  }

}
