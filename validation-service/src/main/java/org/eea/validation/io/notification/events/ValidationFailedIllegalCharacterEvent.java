package org.eea.validation.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ValidationFailedIllegalCharacterEvent.
 */
@Component
public class ValidationFailedIllegalCharacterEvent implements NotificableEventHandler {


    /**
     * The dataset metabase controller zuul.
     */
    @Autowired
    private DataSetMetabaseControllerZuul datasetMetabaseController;

    /**
     * The dataflow controller zuul.
     */
    @Autowired
    private DataFlowControllerZuul dataflowControllerZuul;

    /**
     * Gets the event type.
     *
     * @return the event type
     */
    @Override
    public EventType getEventType() {
        return EventType.VALIDATION_FAILED_ILLEGAL_CHARACTER_EVENT;
    }

    /**
     * Gets the map.
     *
     * @param notificationVO the notification VO
     *
     * @return the map
     *
     * @throws EEAException the EEA exception
     */
    @Override
    public Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException {
        Long datasetId = notificationVO.getDatasetId();

        DataSetMetabaseVO dataSetMetabaseVO =
                datasetMetabaseController.findDatasetMetabaseById(datasetId);
        List<DesignDatasetVO> designDataset = datasetMetabaseController
                .findDesignDataSetIdByDataflowId(dataSetMetabaseVO.getDataflowId());

        String datasetName = "";
        for (DesignDatasetVO designDatasetVO : designDataset) {
            if (designDatasetVO.getDatasetSchema()
                    .equalsIgnoreCase(dataSetMetabaseVO.getDatasetSchema())) {
                datasetName = designDatasetVO.getDataSetName();
            }
        }

        DataFlowVO dataFlowVO =
                dataflowControllerZuul.getMetabaseById(dataSetMetabaseVO.getDataflowId());
        String dataflowName = dataFlowVO.getName();

        String tableName = notificationVO.getTableName();
        String fieldName = notificationVO.getFieldName();

        Map<String, Object> notification = new HashMap<>();
        notification.put("user", notificationVO.getUser());
        notification.put("datasetId", datasetId);
        notification.put("dataflowId", dataSetMetabaseVO.getDataflowId());
        notification.put("datasetName", datasetName);
        notification.put("dataflowName", dataflowName);
        notification.put("tableName", tableName);
        notification.put("fieldName", fieldName);

        return notification;
    }
}
