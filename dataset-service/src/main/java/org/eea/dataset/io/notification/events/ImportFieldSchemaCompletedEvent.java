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
 * The Class ImportFieldSchemaCompletedEvent.
 */
@Component
public class ImportFieldSchemaCompletedEvent implements NotificableEventHandler {


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
    return EventType.IMPORT_FIELD_SCHEMA_COMPLETED_EVENT;
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

    DataSetMetabaseVO datasetMetabase =
        datasetMetabaseService.findDatasetMetabase(notificationVO.getDatasetId());
    Long dataflowId = notificationVO.getDataflowId() != null ? notificationVO.getDataflowId()
        : datasetMetabase.getDataflowId();

    String dataflowName =
        notificationVO.getDataflowName() != null ? notificationVO.getDataflowName()
            : dataflowControllerZuul.getMetabaseById(dataflowId).getName();

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("dataflowId", dataflowId);
    notification.put("dataflowName", dataflowName);
    notification.put("datasetId", notificationVO.getDatasetId());
    notification.put("datasetName", datasetMetabase.getDataSetName());
    notification.put("tableSchemaName", notificationVO.getTableSchemaName());
    return notification;
  }
}
