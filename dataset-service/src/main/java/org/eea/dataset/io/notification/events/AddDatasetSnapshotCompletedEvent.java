package org.eea.dataset.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class LoadDataCompletedEvent.
 */
@Component
public class AddDatasetSnapshotCompletedEvent implements NotificableEventHandler {

  /** The dataset service. */
  @Autowired
  private DatasetService datasetService;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.ADD_DATASET_SNAPSHOT_COMPLETED_EVENT;
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
    Long dataflowId = notificationVO.getDataflowId() != null ? notificationVO.getDataflowId()
        : datasetService.getDataFlowIdById(notificationVO.getDatasetId());
    String datasetName = notificationVO.getDatasetName() != null ? notificationVO.getDatasetName()
        : datasetMetabaseService.findDatasetMetabase(datasetId).getDataSetName();
    String dataflowName =
        notificationVO.getDataflowName() != null ? notificationVO.getDataflowName()
            : dataflowControllerZuul.getMetabaseById(dataflowId).getName();

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("datasetId", datasetId);
    notification.put("dataflowId", dataflowId);
    notification.put("datasetName", datasetName);
    notification.put("dataflowName", dataflowName);
    return notification;
  }
}
