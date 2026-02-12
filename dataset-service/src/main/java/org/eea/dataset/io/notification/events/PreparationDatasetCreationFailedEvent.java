package org.eea.dataset.io.notification.events;

import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.interfaces.controller.dataflow.RepresentativeController;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PreparationDatasetCreationFailedEvent implements NotificableEventHandler {

    @Autowired
    private RepresentativeController.RepresentativeControllerZuul representativeControllerZuul;

    @Autowired
    private DataFlowController.DataFlowControllerZuul dataFlowControllerZuul;

    @Autowired
    private DatasetMetabaseService datasetMetabaseService;

    @Override
    public EventType getEventType() {
        return EventType.PREPARATION_DATASET_CREATION_FAILED_EVENT;
    }

    @Override
    public Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException {
        Long dataflowId = notificationVO.getDataflowId();
        Long datasetId = notificationVO.getDatasetId();
        Long providerId = notificationVO.getProviderId();

        DataFlowVO dataflow = dataFlowControllerZuul.getMetabaseById(dataflowId);
        DataSetMetabaseVO datasetVO = datasetMetabaseService.findDatasetMetabase(datasetId);
        String datasetName = datasetVO.getDataSetName();

        String dataProviderLabel = "";
        if (null != providerId) {
            DataProviderVO dataProviderVO =
                    representativeControllerZuul.findDataProviderById(notificationVO.getProviderId());
            dataProviderLabel = dataProviderVO.getLabel();
        }

        Map<String, Object> notification = new HashMap<>();
        notification.put("user", notificationVO.getUser());
        notification.put("dataflowId", dataflowId);
        notification.put("datasetId", datasetId);
        notification.put("datasetName", datasetName);
        notification.put("dataflowName", dataflow.getName());
        notification.put("providerId", providerId);
        notification.put("dataProvider", dataProviderLabel);

        return notification;
    }
}
