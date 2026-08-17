package org.eea.dataset.io.notification.events;

import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class AnotherConversionIsRunningFailedEvent.
 */
@Component
public class AnotherConversionIsRunningFailedEvent implements NotificableEventHandler {

    @Autowired
    private DatasetMetabaseService datasetMetabaseService;


    @Override
    public EventType getEventType() {
        return EventType.ANOTHER_CONVERSION_IS_RUNNING_FAILED_EVENT;
    }

    @Override
    public Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException {
        String datasetName = notificationVO.getDatasetName();

        if(datasetName == null){
            DataSetMetabaseVO datasetVO = datasetMetabaseService.findDatasetMetabase(notificationVO.getDatasetId());
            datasetName = datasetVO.getDataSetName();
        }

        Map<String, Object> notification = new HashMap<>();
        notification.put("user", notificationVO.getUser());
        notification.put("datasetId", notificationVO.getDatasetId());
        notification.put("dataflowId", notificationVO.getDataflowId());
        notification.put("datasetName", datasetName);
        notification.put("preparationCode", notificationVO.getPreparationCode());
        notification.put("message", "Iceberg το Parquet conversion failed because there are queued or in progress jobs for the same datasetId.");
        return notification;
    }
}
