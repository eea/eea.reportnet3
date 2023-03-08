package org.eea.orchestrator.io.notification.events;


import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class FmeJobFailedEvent.
 */
@Component
public class FmeImportJobFailedEvent implements NotificableEventHandler {

    /**
     * Gets the event type.
     *
     * @return the event type
     */
    @Override
    public EventType getEventType() {
        return EventType.FME_IMPORT_JOB_FAILED_EVENT;
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
        Long dataflowId = notificationVO.getDataflowId();
        String datasetName = notificationVO.getDatasetName() ;
        String dataflowName = notificationVO.getDataflowName();
        String tableSchemaId = notificationVO.getTableSchemaId();
        String tableSchemaName = notificationVO.getTableSchemaName();


        Map<String, Object> notification = new HashMap<>();
        notification.put("user", notificationVO.getUser());
        notification.put("datasetId", datasetId);
        notification.put("dataflowId", dataflowId);
        notification.put("tableSchemaId", tableSchemaId);
        notification.put("datasetName", datasetName);
        notification.put("dataflowName", dataflowName);
        notification.put("tableSchemaName", tableSchemaName);
        notification.put("fileName", notificationVO.getFileName());
        notification.put("error", notificationVO.getError());
        return notification;
    }
}
