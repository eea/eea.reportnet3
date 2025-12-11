package org.eea.dataset.io.notification.events;

import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DownloadImportedFileFinishedEvent implements NotificableEventHandler {

    @Autowired
    private DatasetMetabaseService datasetMetabaseService;

    @Override
    public EventType getEventType() {
        return EventType.DOWNLOAD_IMPORTED_FILE_FINISHED_EVENT;
    }

    @Override
    public Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException {
        Long dataflowId = notificationVO.getDataflowId();
        Long datasetId = notificationVO.getDatasetId();
        String user = notificationVO.getUser();
        String filename = notificationVO.getFileName();


        Map<String, Object> notification = new HashMap<>();
        notification.put("dataflowId", dataflowId);
        notification.put("datasetId", datasetId);
        notification.put("user", user);
        notification.put("fileName", filename);
        return notification;
    }
}