package org.eea.dataset.io.notification;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ExportHistoricReleasesFailedEvent implements NotificableEventHandler {

    /**
     * Gets the event type.
     *
     * @return the event type
     */
    @Override
    public EventType getEventType() {
        return EventType.EXPORT_HISTORIC_RELEASES_FAILED_EVENT;
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
        String processId = notificationVO.getProcessId();
        String nameFile = notificationVO.getFileName();


        Map<String, Object> notification = new HashMap<>();
        notification.put("user", notificationVO.getUser());
        notification.put("datasetId", datasetId);
        notification.put("dataflowId", dataflowId);
        notification.put("nameFile", nameFile);
        notification.put("processId", processId);
        notification.put("error", notificationVO.getError());
        return notification;
    }

}
