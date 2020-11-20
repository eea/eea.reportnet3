package org.eea.validation.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidateRulesFinishEvent implements NotificableEventHandler {


  /** The dataset metabase controller. */
  @Autowired
  private DatasetMetabaseController datasetMetabaseController;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.VALIDATE_RULES_COMPLETED_EVENT;
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
        datasetMetabaseController.findDatasetMetabaseById(datasetId).getDataSetName();

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("datasetId", datasetId);
    notification.put("datasetName", datasetName);
    notification.put("dataflowId", dataflowId);
    return notification;
  }


}
