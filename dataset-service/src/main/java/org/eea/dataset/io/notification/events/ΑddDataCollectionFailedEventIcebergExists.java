package org.eea.dataset.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ΑddDataCollectionFailedEventIcebergExists.
 */
@Component
public class ΑddDataCollectionFailedEventIcebergExists implements NotificableEventHandler {

    /** The dataflow controller zuul. */
    @Autowired
    private DataFlowController.DataFlowControllerZuul dataflowControllerZuul;

    /**
     * Gets the event type.
     *
     * @return the event type
     */
    @Override
    public EventType getEventType() {
        return EventType.ADD_DATACOLLECTION_FAILED_EVENT_ICEBERG_EXISTS;
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
                        : dataflowControllerZuul.getMetabaseById(dataflowId).getName();

        Map<String, Object> notification = new HashMap<>();
        notification.put("user", notificationVO.getUser());
        notification.put("dataflowId", dataflowId);
        notification.put("dataflowName", dataflowName);
        notification.put("error", notificationVO.getError());
        return notification;
    }
}
