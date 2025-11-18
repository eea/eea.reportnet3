package org.eea.dataset.io.notification.events;

import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController;
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
public class ImportMultilineTextCharLimitWarningEvent implements NotificableEventHandler {

    /**
     * Gets the event type.
     *
     * @return the event type
     */
    @Override
    public EventType getEventType() {
        return EventType.IMPORT_MULTILINE_TEXT_CHAR_LIMIT_WARNING_EVENT;
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
      Long datasetId = notificationVO.getDatasetId();

      Map<String, Object> notification = new HashMap<>();
      notification.put("dataflowId", dataflowId);
      notification.put("datasetId", datasetId);
      notification.put("recordLines", notificationVO.getRecordLines());
      notification.put("tableName", notificationVO.getTableName());
      notification.put("fieldName", notificationVO.getFieldName());
        return notification;
    }
}
