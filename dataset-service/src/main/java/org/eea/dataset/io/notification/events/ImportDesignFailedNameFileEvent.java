package org.eea.dataset.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


/**
 * The Class ImportDesignFailedNameFileEvent.
 */
@Component
public class ImportDesignFailedNameFileEvent implements NotificableEventHandler {


  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  @Lazy
  @Autowired
  private DatasetSchemaService dataschemaService;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.IMPORT_DESIGN_FAILED_NAMEFILE_EVENT;
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
    String tableSchemaId = notificationVO.getTableSchemaId();
    String tableSchemaName = notificationVO.getTableSchemaName();

    if (null == tableSchemaName && null != tableSchemaId) {
      tableSchemaName =
          dataschemaService.getTableSchemaName(datasetVO.getDatasetSchema(), tableSchemaId);
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
    notification.put("error", notificationVO.getError());
    return notification;
  }
}
