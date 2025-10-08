package org.eea.dataset.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ExportDefinitionCompletedEvent.
 */
@Component
public class ExportDefinitionCompletedEvent implements NotificableEventHandler {

    /**
     * Gets the event type.
     *
     * @return the event type
     */
    @Override
    public EventType getEventType() {
        return EventType.EXPORT_DEFINITION_COMPLETED_EVENT;
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
        String user = notificationVO.getUser();
        Long dataflowId = notificationVO.getDataflowId();
        Long datasetId = notificationVO.getDatasetId();
        String datasetName = notificationVO.getDatasetName();
        String tableSchemaId = notificationVO.getTableSchemaId();
        String tableSchemaName = notificationVO.getTableSchemaName();

        Map<String, Object> notification = new HashMap<>();
        notification.put("user", user);
        notification.put("dataflowId", dataflowId);
        notification.put("datasetId", datasetId);
        notification.put("datasetName", datasetName);
        notification.put("tableSchemaId", tableSchemaId);
        notification.put("tableSchemaName", tableSchemaName);
        return notification;
    }

}
