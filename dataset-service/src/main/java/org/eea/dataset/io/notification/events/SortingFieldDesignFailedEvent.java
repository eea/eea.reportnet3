package org.eea.dataset.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class SortingFieldFailedEvent.
 */
@Component
public class SortingFieldDesignFailedEvent implements NotificableEventHandler {

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;


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
    return EventType.SORT_FIELD_DESIGN_FAILED_EVENT;
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
    Long dataflowId = notificationVO.getDataflowId() != null ? notificationVO.getDataflowId()
        : datasetService.getDataFlowIdById(notificationVO.getDatasetId());
    String datasetName =
        datasetMetabaseService.findDatasetMetabase(notificationVO.getDatasetId()).getDataSetName();

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("dataflowId", dataflowId);
    notification.put("datasetId", notificationVO.getDatasetId());
    notification.put("datasetName", datasetName);
    notification.put("tableSchemaId", notificationVO.getTableSchemaId());
    notification.put("tableSchemaName", notificationVO.getTableSchemaName());
    notification.put("fieldSchemaId", notificationVO.getFieldSchemaId());
    notification.put("fieldSchemaName", notificationVO.getFieldSchemaName());
    return notification;
  }

}
