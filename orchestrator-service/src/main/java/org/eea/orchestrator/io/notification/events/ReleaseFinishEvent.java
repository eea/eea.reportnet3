package org.eea.orchestrator.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class ReleaseFinishEvent.
 */
@Component
public class ReleaseFinishEvent implements NotificableEventHandler {

    /**
     * The DatasetController is an injected dependency used to access and manage operations
     * related to datasets. It provides functionalities such as retrieving dataset information
     * and dataflow associations.
     */
    @Autowired
    private DatasetController datasetController;

    /** The representative controller zuul. */
    @Autowired
    private RepresentativeControllerZuul representativeControllerZuul;

    /**
     * Gets the event type.
     *
     * @return the event type
     */
    @Override
    public EventType getEventType() {
        return EventType.RELEASE_COMPLETED_EVENT;
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
        Long dataflowId = notificationVO.getDataflowId() != null ? notificationVO.getDataflowId()
                : datasetController.getDataFlowIdById(notificationVO.getDatasetId());

        String dataProviderLabel = "";
        if (null != notificationVO.getProviderId()) {
            DataProviderVO dataProviderVO =
                    representativeControllerZuul.findDataProviderById(notificationVO.getProviderId());
            dataProviderLabel = dataProviderVO.getLabel();
        }
        Map<String, Object> notification = new HashMap<>();
        notification.put("user", notificationVO.getUser());
        notification.put("dataflowId", dataflowId);
        notification.put("dataflowName", notificationVO.getDataflowName());
        notification.put("dataProviderName", dataProviderLabel);
        return notification;
    }

}

