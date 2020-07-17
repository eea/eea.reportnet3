package org.eea.dataflow.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * The Class ExternalExportEUDatasetCompletedEvent.
 */
@Component
public class ExternalExportEUDatasetCompletedEvent implements NotificableEventHandler {

  /** The dataflow service. */
  @Autowired
  private DataflowService dataflowService;


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.EXTERNAL_EXPORT_EUDATASET_COMPLETED_EVENT;
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
    String dataflowName = dataflowService.getById(dataflowId).getName();

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("dataflowId", dataflowId);
    notification.put("dataflowName", dataflowName);
    if (StringUtils.isNotBlank(notificationVO.getFileName())) {
      notification.put("fileName", notificationVO.getFileName());
    }
    return notification;
  }
}
