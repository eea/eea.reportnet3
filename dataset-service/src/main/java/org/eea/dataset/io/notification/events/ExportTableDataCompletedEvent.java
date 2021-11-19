package org.eea.dataset.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.stereotype.Component;

@Component
public class ExportTableDataCompletedEvent implements NotificableEventHandler {

  @Override
  public EventType getEventType() {
    return EventType.EXPORT_TABLE_DATA_COMPLETED_EVENT;
  }

  @Override
  public Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException {
    Map<String, Object> notification = new HashMap<>();
    notification.put("datasetId", notificationVO.getDatasetId());
    notification.put("datasetSchemaId", notificationVO.getDatasetSchemaId());
    notification.put("fileName", notificationVO.getFileName());
    return notification;
  }

}
