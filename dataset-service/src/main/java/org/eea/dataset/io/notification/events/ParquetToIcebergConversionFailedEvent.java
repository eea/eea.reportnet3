package org.eea.dataset.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.stereotype.Component;


/**
 * The Class ParquetToIcebergConversionFailedEvent.
 */
@Component
public class ParquetToIcebergConversionFailedEvent implements NotificableEventHandler {

  @Override
  public EventType getEventType() {
    return EventType.PARQUET_TO_ICEBERG_CONVERSION_FAILED_EVENT;
  }

  @Override
  public Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException {
    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("datasetId", notificationVO.getDatasetId());
    notification.put("dataflowId", notificationVO.getDataflowId());
    notification.put("message", "Parquet to Iceberg conversion failed.");
    return notification;
  }
}
