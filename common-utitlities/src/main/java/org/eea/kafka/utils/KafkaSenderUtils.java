package org.eea.kafka.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.communication.UserNotificationVO;
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
    UserNotificationVO userNotificationVO = new UserNotificationVO();
    userNotificationVO.setEventType(eventType.toString());
    userNotificationVO.setInsertDate(new Date());
    UserNotificationContentVO content = new UserNotificationContentVO();
    content.setDataflowId(notificationVO.getDataflowId());
    content.setDataflowName(notificationVO.getDataflowName());
    content.setDatasetId(notificationVO.getDatasetId());
    content.setDatasetName(notificationVO.getDatasetName());
    content.setProviderId(notificationVO.getProviderId());
    userNotificationVO.setContent(content);

    notificationControllerZuul.createUserNotificationPrivate(userNotificationVO);

    if (value == null) {
      value = new HashMap<>();
    }
    value.put("notification",
        notificableEventFactory.getNotificableEventHandler(eventType).getMap(notificationVO));
    releaseKafkaEvent(eventType, value);
    LOG.info("released kafaka event {}", eventType);
  }
}
