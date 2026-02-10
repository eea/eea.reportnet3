package org.eea.dataset.io.notification.events;

import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.interfaces.controller.dataflow.RepresentativeController;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PreparationDatasetCreationCompletedEvent implements NotificableEventHandler {

    /** The representative controller zuul. */
    @Autowired
    private RepresentativeController.RepresentativeControllerZuul representativeControllerZuul;

    /** The dataflow controller zuul. */
    @Autowired
    private DataFlowController.DataFlowControllerZuul dataFlowControllerZuul;

    @Override
    public EventType getEventType() {
        return EventType.PREPARATION_DATASET_CREATION_COMPLETED_EVENT;
    }

    @Override
    public Map<String, Object> getMap(NotificationVO notificationVO) throws EEAException {
        Long dataflowId = notificationVO.getDataflowId();
        Long providerId = notificationVO.getProviderId();

        DataFlowVO dataflow = dataFlowControllerZuul.getMetabaseById(dataflowId);

        String dataProviderLabel = "";
        if (null != providerId) {
            DataProviderVO dataProviderVO =
                    representativeControllerZuul.findDataProviderById(notificationVO.getProviderId());
            dataProviderLabel = dataProviderVO.getLabel();
        }

        Map<String, Object> notification = new HashMap<>();
        notification.put("user", notificationVO.getUser());
        notification.put("dataflowId", dataflowId);
        notification.put("dataflowName", dataflow.getName());
        notification.put("providerId", providerId);
        notification.put("dataProvider", dataProviderLabel);

        return notification;
    }
}
