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

/**
 * The Class DisableNamesTypesRulesErrorEvent.
 */
@Component
public class DisableNamesTypesRulesErrorEvent implements NotificableEventHandler {

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
    return EventType.DISABLE_NAMES_TYPES_RULES_ERROR_EVENT;
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
    Integer disabledRules = notificationVO.getDisabledRules();
    Integer invalidRules = notificationVO.getInvalidRules();
    String datasetName =
        datasetMetabaseController.findDatasetMetabaseById(datasetId).getDataSetName();

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("datasetId", datasetId);
    notification.put("datasetName", datasetName);
    notification.put("disabledRules", disabledRules);
    notification.put("invalidRules", invalidRules);
    notification.put("error", notificationVO.getError());
    return notification;
  }


}
