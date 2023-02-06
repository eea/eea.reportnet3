package org.eea.orchestrator.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ImportDesignCompletedEvent implements NotificableEventHandler {

    @Autowired
    private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

    @Lazy
    @Autowired
    private DatasetSchemaControllerZuul datasetSchemaControllerZuul;


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
        return EventType.IMPORT_DESIGN_COMPLETED_EVENT;
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
        DataSetMetabaseVO datasetVO = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
        Long dataflowId = notificationVO.getDataflowId() != null ? notificationVO.getDataflowId()
                : datasetVO.getDataflowId();
        DataFlowVO dataFlowVO = dataflowControllerZuul.getMetabaseById(dataflowId);

        String datasetName = notificationVO.getDatasetName() != null ? notificationVO.getDatasetName()
                : datasetVO.getDataSetName();
        String dataflowName =
                notificationVO.getDataflowName() != null ? notificationVO.getDataflowName()
                        : dataFlowVO.getName();
        String tableSchemaId = notificationVO.getTableSchemaId();
        String tableSchemaName = notificationVO.getTableSchemaName();

        if (null == tableSchemaName && null != tableSchemaId) {
            tableSchemaName =
                    datasetSchemaControllerZuul.getTableSchemaName(datasetVO.getDatasetSchema(), tableSchemaId);
        }

        Map<String, Object> notification = new HashMap<>();
        notification.put("user", notificationVO.getUser());
        notification.put("datasetId", datasetId);
        notification.put("dataflowId", dataflowId);
        notification.put("tableSchemaId", tableSchemaId);
        notification.put("datasetName", datasetName);
        notification.put("dataflowName", dataflowName);
        notification.put("tableSchemaName", tableSchemaName);
        notification.put("fileName", notificationVO.getFileName());
        notification.put("typeStatus", dataFlowVO.getStatus().toString());
        return notification;
    }
}