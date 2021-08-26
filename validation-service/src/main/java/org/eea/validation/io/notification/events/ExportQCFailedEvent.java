package org.eea.validation.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.stereotype.Component;


/**
 * The Class ExportQCFailedEvent.
 */
@Component
public class ExportQCFailedEvent implements NotificableEventHandler {


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.EXPORT_QC_FAILED_EVENT;
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
    Long datasetId = notificationVO.getDatasetId();
    String nameFile = notificationVO.getFileName();


    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("datasetId", datasetId);
    notification.put("nameFile", nameFile);
    notification.put("error", notificationVO.getError());
    return notification;
  }


}
