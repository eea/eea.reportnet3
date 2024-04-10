package org.eea.dataset.io.notification.events;

import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component

public class ImportEmptyFilesWarningEvent implements NotificableEventHandler {


    /**
     * The dataset metabase service.
     */
    @Autowired
    private DatasetMetabaseService datasetMetabaseService;

    /**
     * The dataflow controller zuul.
     */
    @Autowired
    private DataFlowController.DataFlowControllerZuul dataflowControllerZuul;

    /**
     * Gets the event type.
     *
     * @return the event type
     */
    @Override
    public EventType getEventType() {
        return EventType.IMPORT_EMPTY_FILES_WARNING_EVENT;
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
        DataSetMetabaseVO datasetVO = datasetMetabaseService.findDatasetMetabase(datasetId);
        Long dataflowId = notificationVO.getDataflowId() != null ? notificationVO.getDataflowId()
                : datasetVO.getDataflowId();

        String datasetName = notificationVO.getDatasetName() != null ? notificationVO.getDatasetName()
                : datasetVO.getDataSetName();
        String dataflowName =
                notificationVO.getDataflowName() != null ? notificationVO.getDataflowName()
                        : dataflowControllerZuul.getMetabaseById(dataflowId).getName();


        Map<String, Object> notification = new HashMap<>();
        notification.put("user", notificationVO.getUser());
        notification.put("datasetId", datasetId);
        notification.put("dataflowId", dataflowId);
        notification.put("datasetName", datasetName);
        notification.put("dataflowName", dataflowName);
        notification.put("fileName", notificationVO.getFileName());

        return notification;
    }
}
