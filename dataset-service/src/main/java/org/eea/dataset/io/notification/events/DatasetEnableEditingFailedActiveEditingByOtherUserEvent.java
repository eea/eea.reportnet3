package org.eea.dataset.io.notification.events;

import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.dataset.service.DatasetTableService;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DatasetEnableEditingFailedActiveEditingByOtherUserEvent implements NotificableEventHandler {

    @Autowired
    private DatasetMetabaseService datasetMetabaseService;

    @Autowired
    private DatasetTableService datasetTableService;

    @Override
    public EventType getEventType() {
        return EventType.DATASET_ENABLE_EDITING_FAILED_ACTIVE_EDITING_BY_OTHER_USER_EVENT;
    }

    @Override
    public Map<String, Object> getMap(NotificationVO vo) throws EEAException {
        String datasetName = vo.getDatasetName();

        if (datasetName == null) {
            DataSetMetabaseVO ds = datasetMetabaseService.findDatasetMetabase(vo.getDatasetId());
            datasetName = ds.getDataSetName();
        }
        String currentEditor = datasetTableService.getDatasetEditingUsername(vo.getDatasetId());
        String message= "Dataset was not edited successfully.";

        if (!(currentEditor == null)) {
            message= message + "Dataset is locked for editing by " + currentEditor;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("user", vo.getUser());
        map.put("datasetId", vo.getDatasetId());
        map.put("dataflowId", vo.getDataflowId());
        map.put("datasetName", datasetName);
        map.put("error", vo.getError());
        map.put("message", message);
        return map;
    }
}
