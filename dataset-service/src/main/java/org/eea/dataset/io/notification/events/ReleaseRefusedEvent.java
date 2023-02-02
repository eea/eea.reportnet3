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
public class ReleaseRefusedEvent implements NotificableEventHandler {

    /** The representative controller zuul. */
    @Autowired
    private RepresentativeController.RepresentativeControllerZuul representativeControllerZuul;


    /** The dataflow controller zuul. */
    @Autowired
    private DataFlowController.DataFlowControllerZuul dataFlowControllerZuul;

    /**
     * Gets the event type.
     *
     * @return the event type
     */
    @Override
    public EventType getEventType() {
        return EventType.RELEASE_REFUSED_EVENT;
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
        Long dataflowId = notificationVO.getDataflowId();
        Long providerId = notificationVO.getProviderId();

        DataFlowVO dataflow = dataFlowControllerZuul.findById(dataflowId, providerId);

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
        notification.put("error", notificationVO.getError());
        return notification;
    }

}
