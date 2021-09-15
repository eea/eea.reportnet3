package org.eea.dataflow.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * The Class LoadDataCompletedEvent.
 */
@Component
public class ExternalExportDesignCompletedEvent implements NotificableEventHandler {

  /** The dataflow service. */
  @Lazy
  @Autowired
  private DataflowService dataflowService;

  /** The data set metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.EXTERNAL_EXPORT_DESIGN_COMPLETED_EVENT;
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
    Long dataflowId = notificationVO.getDataflowId();
    String datasetName =
        dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId).getDataSetName();
    String dataflowName = dataflowService.getMetabaseById(dataflowId).getName();

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("datasetId", datasetId);
    notification.put("dataflowId", dataflowId);
    notification.put("datasetName", datasetName);
    notification.put("dataflowName", dataflowName);
    notification.put("fileName", notificationVO.getFileName());

    return notification;
  }
}
