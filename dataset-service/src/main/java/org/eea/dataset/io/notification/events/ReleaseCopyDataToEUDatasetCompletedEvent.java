package org.eea.dataset.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class RestoreDatasetSnapshotFailedEvent.
 */
@Component
public class ReleaseCopyDataToEUDatasetCompletedEvent implements NotificableEventHandler {


  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COPY_DATA_TO_EUDATASET_COMPLETED_EVENT;
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
    String datasetName = notificationVO.getDatasetName() != null ? notificationVO.getDatasetName()
        : dataSetMetabaseRepository.findById(datasetId).orElse(new DataSetMetabase())
            .getDataSetName();

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("datasetId", datasetId);
    notification.put("datasetName", datasetName);
    return notification;
  }

}
