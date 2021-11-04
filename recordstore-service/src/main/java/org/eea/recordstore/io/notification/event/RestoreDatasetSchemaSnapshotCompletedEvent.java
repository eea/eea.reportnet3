package org.eea.recordstore.io.notification.event;

import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class RestoreDatasetSchemaSnapshotCompletedEvent.
 */
@Component
public class RestoreDatasetSchemaSnapshotCompletedEvent implements NotificableEventHandler {

  /**
   * The dataset service.
   */
  @Autowired
  private DataSetControllerZuul dataSetControllerZuul;

  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseController;

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
    return EventType.RESTORE_DATASET_SCHEMA_SNAPSHOT_COMPLETED_EVENT;
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
        : dataSetControllerZuul.getDataFlowIdById(notificationVO.getDatasetId());
    DataFlowVO dataFlowVO = dataflowControllerZuul.getMetabaseById(dataflowId);

    String datasetName = notificationVO.getDatasetName() != null ? notificationVO.getDatasetName()
        : datasetMetabaseController.findDatasetMetabaseById(datasetId).getDataSetName();
    String dataflowName =
        notificationVO.getDataflowName() != null ? notificationVO.getDataflowName()
            : dataFlowVO.getName();

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("datasetId", datasetId);
    notification.put("dataflowId", dataflowId);
    notification.put("datasetName", datasetName);
    notification.put("dataflowName", dataflowName);
    notification.put("typeStatus", dataFlowVO.getStatus().toString());
    return notification;
  }
}
