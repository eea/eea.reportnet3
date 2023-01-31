package org.eea.orchestrator.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
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
public class ReleaseCanceledEvent implements NotificableEventHandler {

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The dataflow controller zuul */
  @Autowired
  private DataFlowControllerZuul dataFlowControllerZuul;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.RELEASE_CANCELED_EVENT;
  }

  /**
   * Gets the map.
   *
   * @param notificationVO the notification VO
   * @return the map
   * @throws EEAException the EEA exception
   */
  @Override
  public Map<String, Object> getMap(NotificationVO notificationVO) {
    DataFlowVO dataflowVO = dataFlowControllerZuul.getMetabaseById(notificationVO.getDataflowId());

    String dataProviderLabel = "";
    if (null != notificationVO.getProviderId()) {
      DataProviderVO dataProviderVO =
              representativeControllerZuul.findDataProviderById(notificationVO.getProviderId());
      dataProviderLabel = dataProviderVO.getLabel();
    }
    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("dataflowId", notificationVO.getDataflowId());
    notification.put("providerId", notificationVO.getProviderId());
    notification.put("dataflowName", dataflowVO.getName());
    notification.put("dataProviderName", dataProviderLabel);
    notification.put("error", notificationVO.getError());
    return notification;
  }

}
