package org.eea.orchestrator.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class RestoreDatasetSnapshotFailedEvent.
 */
@Component
public class CopyDataToEUDatasetCanceledEvent implements NotificableEventHandler {

  @Autowired
  private DataFlowControllerZuul dataFlowControllerZuul;


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COPY_DATA_TO_EUDATASET_CANCELED_EVENT;
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
    DataFlowVO dataflowVO = dataFlowControllerZuul.getMetabaseById(notificationVO.getDataflowId());

    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("dataflowId", notificationVO.getDataflowId());
    notification.put("dataflowName", dataflowVO.getName());
    notification.put("error", notificationVO.getError());
    return notification;
  }

}
