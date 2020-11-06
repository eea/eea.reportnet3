package org.eea.recordstore.io.notification.event;

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
 * The Class RestoreDatasetSnapshotFailedEvent.
 */
@Component
public class ReleaseDatasetSnapshotFailedEvent implements NotificableEventHandler {


  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.RELEASE_FAILED_EVENT;
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
        : datasetMetabaseControllerZuul.findDatasetMetabaseById(snapshotId).getDataSetName();

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("snapshotId", snapshotId);
    notification.put("snapshotName", datasetName);
    notification.put("error", notificationVO.getError());
    return notification;
  }

}
