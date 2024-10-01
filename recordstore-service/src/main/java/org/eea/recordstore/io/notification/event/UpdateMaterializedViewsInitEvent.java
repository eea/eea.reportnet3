package org.eea.recordstore.io.notification.event;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UpdateMaterializedViewsInitEvent implements NotificableEventHandler {

    @Override
    public EventType getEventType() {
        return EventType.UPDATE_MATERIALIZED_VIEWS_INIT_EVENT;
    }

    @Override
    public Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException {
        String user = notificationVO.getUser();
        Long datasetId = notificationVO.getDatasetId();
        String datasetName = notificationVO.getDatasetName();

        Map<String, Object> notification = new HashMap<>();
        notification.put("user", user);
        notification.put("datasetId", datasetId);
        notification.put("datasetName", datasetName);
        notification.put("error", notificationVO.getError());

        return notification;
    }
}
