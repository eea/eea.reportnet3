package org.eea.dataset.io.notification.events;

import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetTableService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.Map;

@Component
public class IcebergToParquetFailedActiveEditingByOtherUser implements NotificableEventHandler {

    @Autowired
    private DatasetMetabaseService datasetMetabaseService;

    @Autowired
    private DatasetTableService datasetTableService;

    @Override
    public EventType getEventType() {
        return EventType.ICEBERG_TO_PARQUET_FAILED_ACTIVE_EDITING_BY_OTHER_USER;
    }

    @Override
    public Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException {
        String datasetName = notificationVO.getDatasetName();

        if(datasetName == null){
            DataSetMetabaseVO datasetVO = datasetMetabaseService.findDatasetMetabase(notificationVO.getDatasetId());
            datasetName = datasetVO.getDataSetName();
        }

        String currentEditor = datasetTableService.getDatasetEditingUsername(
                notificationVO.getDatasetId(),
                notificationVO.getPreparationCode());
        String message= "Iceberg το Parquet conversion failed.";

        if (!(currentEditor == null)) {
            message= message + "Dataset is locked for editing by " + currentEditor;
        }

        Map<String, Object> notification = new HashMap<>();
        notification.put("user", notificationVO.getUser());
        notification.put("datasetId", notificationVO.getDatasetId());
        notification.put("preparationCode", notificationVO.getPreparationCode());
        notification.put("dataflowId", notificationVO.getDataflowId());
        notification.put("datasetName", datasetName);
        notification.put("message", message);
        return notification;
    }
}
