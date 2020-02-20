package org.eea.dataset.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



/**
 * The Class ReleaseBlockedEvent is a notification used when unreleased the release because has
 * blocker errors.
 */
@Component
public class ReleaseBlockedEvent implements NotificableEventHandler {


  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseController;


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.RELEASE_BLOCKED;
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
    Long snapshotId = notificationVO.getDatasetId();
    String datasetName = notificationVO.getDatasetName() != null ? notificationVO.getDatasetName()
        : datasetMetabaseController.findDatasetMetabaseById(snapshotId).getDataSetName();

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("snapshotId", snapshotId);
    notification.put("snapshotName", datasetName);
    notification.put("error", notificationVO.getError());
    return notification;
  }

}
