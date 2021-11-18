package org.eea.dataflow.io.notification.events;

import java.util.HashMap;
import java.util.Map;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.notification.event.NotificableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;



/**
 * The Class ExternalIntegrationReportingFailedEvent.
 */
@Component
public class ExternalIntegrationReportingFailedEvent implements NotificableEventHandler {

  /** The dataflow service. */
  @Lazy
  @Autowired
  private DataflowService dataflowService;

  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.EXTERNAL_IMPORT_REPORTING_FROM_OTHER_SYSTEM_FAILED_EVENT;
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
    String dataflowName = dataflowService.getMetabaseById(dataflowId).getName();
    String datasetName = "";
    if (datasetId != null) {
      datasetName =
          datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId).getDataSetName();
    }
    Map<String, Object> notification = new HashMap<>();
    notification.put("user", notificationVO.getUser());
    notification.put("dataflowId", dataflowId);
    notification.put("datasetId", notificationVO.getDatasetId());
    notification.put("datasetName", datasetName);
    notification.put("dataflowName", dataflowName);

    return notification;
  }
}
