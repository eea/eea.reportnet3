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
public class FmeImportJobFailedEventNoFileReturned implements NotificableEventHandler {

    /**
     * Gets the event type.
     *
     * @return the event type
     */
    @Override
    public EventType getEventType() {
        return EventType.FME_IMPORT_JOB_FAILED_EVENT_NO_FILE_RETURNED;
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
        Long datasetId = notificationVO.getDatasetId();
        Long dataflowId = notificationVO.getDataflowId();
        String tableSchemaId = notificationVO.getTableSchemaId();
        String datasetName = notificationVO.getDatasetName();
        String dataflowName = notificationVO.getDataflowName();
        String tableSchemaName = notificationVO.getTableSchemaName();
        String fileName = notificationVO.getFileName();
        String error = notificationVO.getError();

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("datasetId", datasetId);
        result.put("dataflowId", dataflowId);
        result.put("tableSchemaId", tableSchemaId);
        result.put("datasetName", datasetName);
        result.put("dataflowName", dataflowName);
        result.put("tableSchemaName", tableSchemaName);
        result.put("fileName", fileName);
        result.put("error", error);

        return result;
    }
}
