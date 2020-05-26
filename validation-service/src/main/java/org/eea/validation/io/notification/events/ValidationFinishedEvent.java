package org.eea.validation.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class ValidationFinishedEvent.
 */
@Component
public class ValidationFinishedEvent implements NotificableEventHandler {

  /**
   * The dataset controller zuul.
   */
  @Autowired
  private DataSetControllerZuul dataSetControllerZuul;

  /**
   * The dataset metabase controller zuul.
   */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseController;

  /**
   * The dataflow controller zuul.
   */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.VALIDATION_FINISHED_EVENT;
  }

  /**
   * Gets the map.
   *
   * @param notificationVO the notification VO
   *
   * @return the map
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException {
    Long datasetId = notificationVO.getDatasetId();

    DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseController
        .findDatasetMetabaseById(datasetId);
    String datasetName = dataSetMetabaseVO.getDataSetName();
    DatasetTypeEnum type = dataSetMetabaseVO.getDatasetTypeEnum();

    String dataflowName = dataflowControllerZuul.getMetabaseById(dataSetMetabaseVO.getDataflowId())
        .getName();

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("datasetId", datasetId);
    notification.put("dataflowId", dataSetMetabaseVO.getDataflowId());
    notification.put("datasetName", datasetName);
    notification.put("dataflowName", dataflowName);
    notification.put("type", type);
    return notification;
  }
}
