package org.eea.dataset.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class DataCollectionCreationFailedEvent.
 */
@Component
public class DataCollectionCreationFailedEvent implements NotificableEventHandler {

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
    return EventType.DATA_COLLECTION_CREATION_FAILED_EVENT;
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
    Long dataflowId = notificationVO.getDataflowId();
    String dataflowName =
        notificationVO.getDataflowName() != null ? notificationVO.getDataflowName()
            : dataflowControllerZuul.findById(dataflowId).getName();

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("dataflowId", dataflowId);
    notification.put("dataflowName", dataflowName);
    notification.put("error", notificationVO.getError());
    return notification;
  }
}
