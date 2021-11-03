package org.eea.kafka.utils;

import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.io.KafkaSender;
import org.eea.notification.factory.NotificableEventFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class KafkaSenderHelper.
 *
 * @author ruben.lozano
 */
@Component
public class KafkaSenderUtils {

  /**
   * The kafka sender.
   */
  @Autowired
  private KafkaSender kafkaSender;

  @Autowired
  private NotificableEventFactory notificableEventFactory;

  @Autowired
  private NotificationControllerZuul notificationControllerZuul;

  private static final Logger LOG = LoggerFactory.getLogger(KafkaSenderUtils.class);

  /**
   * Release Dataset kafka event.
   *
   * @param eventType the event type
   * @param datasetId the dataset id
   */
  public void releaseDatasetKafkaEvent(final EventType eventType, final Long datasetId) {
    final Map<String, Object> dataOutput = new HashMap<>();
    dataOutput.put("dataset_id", datasetId);
    releaseKafkaEvent(eventType, dataOutput);
  }

  /**
   * Release kafka event.
   *
   * @param eventType the event type
   * @param value the value
   */
  public void releaseKafkaEvent(final EventType eventType, final Map<String, Object> value) {
    final EEAEventVO event = new EEAEventVO();
    event.setEventType(eventType);
    event.setData(value);
    kafkaSender.sendMessage(event);
  }

  /**
   * Release kafka event.
   *
   * @param event the event
   */
  public void releaseKafkaEvent(EEAEventVO event) {
    kafkaSender.sendMessage(event);
  }

  /**
   * Release notificable kafka event.
   *
   * @param eventType the event type
   * @param value the value
   * @param notificationVO the notification VO
   *
   * @throws EEAException the EEA exception
   */
  public void releaseNotificableKafkaEvent(final EventType eventType, Map<String, Object> value,
      final NotificationVO notificationVO) throws EEAException {

    if (value == null) {
      value = new HashMap<>();
    }
    Map<String, Object> notificationMap =
        notificableEventFactory.getNotificableEventHandler(eventType).getMap(notificationVO);

    saveUserNotification(eventType.toString(), notificationMap);
    value.put("notification", notificationMap);
    releaseKafkaEvent(eventType, value);
    LOG.info("released kafaka event {}", eventType);
  }

  /**
   * Save user notification.
   *
   * @param eventType the event type
   * @param notificationMap the notification map
   */
  private void saveUserNotification(String eventType, Map<String, Object> notificationMap) {
    Long dataflowId = (notificationMap.get("dataflowId") != null)
        ? Long.parseLong(notificationMap.get("dataflowId").toString())
        : null;
    String dataflowName = (notificationMap.get("dataflowName") != null)
        ? notificationMap.get("dataflowName").toString()
        : null;
    Long datasetId = (notificationMap.get("datasetId") != null)
        ? Long.parseLong(notificationMap.get("datasetId").toString())
        : null;
    String datasetName =
        (notificationMap.get("datasetName") != null) ? notificationMap.get("datasetName").toString()
            : null;
    String dataProviderName = (notificationMap.get("dataProviderName") != null)
        ? notificationMap.get("dataProviderName").toString()
        : null;

    UserNotificationContentVO content = new UserNotificationContentVO();
    content.setDataflowId(dataflowId);
    content.setDataflowName(dataflowName);
    content.setDatasetId(datasetId);
    content.setDatasetName(datasetName);
    content.setDataProviderName(dataProviderName);
    content.setTypeStatus((notificationMap.get("typeStatus") != null)
        ? TypeStatusEnum.valueOf(notificationMap.get("typeStatus").toString())
        : null);
    notificationControllerZuul.createUserNotificationPrivate(eventType, content);
    LOG.info("Save user notification, eventType: {}, notification content: {}", eventType, content);

  }

}
