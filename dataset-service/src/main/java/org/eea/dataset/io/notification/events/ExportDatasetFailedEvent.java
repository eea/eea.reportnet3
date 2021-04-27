package org.eea.dataset.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



/**
 * The Class ExportDatasetFailedEvent.
 */
@Component
public class ExportDatasetFailedEvent implements NotificableEventHandler {


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
    return EventType.EXPORT_DATASET_FAILED_EVENT;
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

    DataSetMetabaseVO dataset =
        datasetMetabaseService.findDatasetMetabase(notificationVO.getDatasetId());

    String dataflowName =
        notificationVO.getDataflowName() != null ? notificationVO.getDataflowName()
            : dataflowControllerZuul.getMetabaseById(dataset.getDataflowId()).getName();


    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("dataflowId", notificationVO.getDataflowId());
    notification.put("dataflowName", dataflowName);
    notification.put("datasetId", notificationVO.getDatasetId());
    notification.put("error", notificationVO.getError());
    notification.put("datasetType", notificationVO.getDatasetType());
    notification.put("datasetName", dataset.getDataSetName());
    return notification;
  }
}
