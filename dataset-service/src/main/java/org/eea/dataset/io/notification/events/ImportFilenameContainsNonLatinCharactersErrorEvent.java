package org.eea.dataset.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ImportFilenameContainsNonLatinCharactersErrorEvent implements NotificableEventHandler {


    @Override
    public EventType getEventType() {
        return EventType.IMPORT_FILENAME_CONTAINS_NON_LATIN_CHARACTERS_ERROR_EVENT;
    }

    @Override
    public Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException {
        Long dataflowId = notificationVO.getDataflowId();
        Long datasetId = notificationVO.getDatasetId();
        String user = notificationVO.getUser();
        String filename = notificationVO.getFileName();
        String nonLatinCharacters = notificationVO.getNonLatinCharacters();

        Map<String, Object> notification = new HashMap<>();
        notification.put("dataflowId", dataflowId);
        notification.put("datasetId", datasetId);
        notification.put("user", user);
        notification.put("fileName", filename);
        notification.put("nonLatinCharacters", nonLatinCharacters);
        return notification;
    }
}
