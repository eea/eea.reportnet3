package org.eea.dataset.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class LoadDataFailedEvent.
 */
@Component
public class LoadSchemaFailedEvent implements NotificableEventHandler {

  /** The dataset service. */
  @Autowired
  private DatasetService datasetService;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.LOAD_SCHEMA_FAILED_EVENT;
  }

  /**
   * Gets the map.
   *
   * @param notificationVO the notification VO
   * @return the map
   * @throws EEAException
   */
  @Override
  public Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException {
    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("datasetId", notificationVO.getDatasetId());
    notification.put("dataflowId",
        notificationVO.getDataflowId() != null ? notificationVO.getDataflowId()
            : datasetService.getDataFlowIdById(notificationVO.getDatasetId()));
    notification.put("tableSchemaId", notificationVO.getTableSchemaId());
    notification.put("fileName", notificationVO.getFileName());
    notification.put("error", notificationVO.getError());
    return notification;
  }
}
