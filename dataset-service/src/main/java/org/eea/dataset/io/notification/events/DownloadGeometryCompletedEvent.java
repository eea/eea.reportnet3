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

@Component
public class DownloadGeometryCompletedEvent implements NotificableEventHandler {

    @Autowired
    private DatasetMetabaseService datasetMetabaseService;

    @Override
    public EventType getEventType() {
        return EventType.DOWNLOAD_GEOMETRY_COMPLETED_EVENT;
    }

    @Override
    public Map<String, Object> getMap(NotificationVO vo) throws EEAException {

        String datasetName = vo.getDatasetName();

        if (datasetName == null) {
            DataSetMetabaseVO ds =
                    datasetMetabaseService.findDatasetMetabase(vo.getDatasetId());
            datasetName = ds.getDataSetName();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("user", vo.getUser());
        map.put("datasetId", vo.getDatasetId());
        map.put("dataflowId", vo.getDataflowId());
        map.put("datasetName", datasetName);
        map.put("message", "Geometry download has been completed successfully");

        return map;
    }
}
