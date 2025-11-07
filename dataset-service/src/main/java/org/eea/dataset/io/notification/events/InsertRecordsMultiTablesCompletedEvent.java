package org.eea.dataset.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InsertRecordsMultiTablesCompletedEvent implements NotificableEventHandler {

    /**
     * Gets the event type.
     *
     * @return the event type
     */
    @Override
    public EventType getEventType() {
        return EventType.INSERT_RECORDS_MULTI_TABLES_COMPLETED;
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
        Map<String, Object> notification = new HashMap<>();
        notification.put("user", notificationVO.getUser());
        notification.put("datasetId", notificationVO.getDatasetId());
        notification.put("dataflowId", notificationVO.getDataflowId());
        return notification;
    }
}