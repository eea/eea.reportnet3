package org.eea.recordstore.io.notification.event;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class LoadDataCompletedEvent.
 */
@Component
public class ExportFileCompleteEvent implements NotificableEventHandler {

  @Override
  public EventType getEventType() {
    return EventType.EXPORT_FILE_COMPLETE_EVENT;
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
    notification.put("datasetId", notificationVO.getDatasetId());
    notification.put("fileName", notificationVO.getFileName());
    return notification;
  }
}
