package org.eea.validation.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DataSetSchemaControllerZuul;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class ValidatedQCRuleEvent.
 */
@Component
public class ValidatedQCRuleEvent implements NotificableEventHandler {

  /** The dataset schema controller. */
  @Autowired
  private DataSetSchemaControllerZuul datasetSchemaController;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.VALIDATED_QC_RULE_EVENT;
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
    String datasetSchemaId = notificationVO.getDatasetSchemaId();
    String datasetName = notificationVO.getDatasetName() != null ? notificationVO.getDatasetName()
        : datasetSchemaController.findDataSchemaById(datasetSchemaId).getNameDatasetSchema();

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("datasetSchemaId", datasetSchemaId);
    notification.put("datasetName", datasetName);
    notification.put("shortCode", notificationVO.getShortCode());
    return notification;
  }
}
