package org.eea.dataset.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ImportFieldSizeExceedsLimitWarningEvent implements NotificableEventHandler {
  @Override
  public EventType getEventType() {
    return EventType.IMPORT_FIELD_SIZE_EXCEEDS_LIMIT_WARNING_EVENT;
  }

  @Override
  public Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException {
    Long dataflowId = notificationVO.getDataflowId();
    Long datasetId = notificationVO.getDatasetId();

    Map<String, Object> notification = new HashMap<>();
    notification.put("dataflowId", dataflowId);
    notification.put("datasetId", datasetId);
    notification.put("recordLines", notificationVO.getRecordLines());

    return notification;
  }
}
