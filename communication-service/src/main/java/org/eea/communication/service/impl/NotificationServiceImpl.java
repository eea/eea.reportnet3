package org.eea.communication.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eea.communication.persistence.UserNotification;
import org.eea.communication.persistence.repository.UserNotificationRepository;
import org.eea.communication.service.NotificationService;
import org.eea.communication.service.model.Notification;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.communication.UserNotificationVO;
import org.eea.kafka.domain.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * The Class NotificationServiceImpl.
 */
@Service("notificationService")
public class NotificationServiceImpl implements NotificationService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(NotificationServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The template. */
  @Autowired
  private SimpMessagingTemplate template;

  /** The logger. */
  private final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

  /** The user notification repository. */
  @Autowired
  private UserNotificationRepository userNotificationRepository;

  /**
   * Send.
   *
   * @param user the user
   * @param type the type
   * @param notification the notification
   * @return true, if successful
   */
  @Override
  public boolean send(String user, EventType type, Map<String, Object> notification) {
    if (user != null && !user.isEmpty() && notification != null) {
      logger.info("Notification sent to user: user={}, type={}, message={}", user, type,
          notification);
      template.convertAndSendToUser(user, "/queue/notifications",
          new Notification(type, notification));
      return true;
    }
    return false;
  }

  /**
   * Creates the user notification.
   *
   * @param userNotificationVO the user notification VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void createUserNotification(UserNotificationVO userNotificationVO) throws EEAException {
    try {
      UserNotification userNotification = new UserNotification();
      userNotification.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
      userNotification.setEventType(EventType.valueOf(userNotificationVO.getEventType()));
      userNotification.setInsertDate(userNotificationVO.getInsertDate());
      userNotification.setDataflowId(userNotificationVO.getContent().getDataflowId());
      userNotification.setDataflowName(userNotificationVO.getContent().getDataflowName());
      userNotification.setProviderId(userNotificationVO.getContent().getProviderId());
      userNotification.setDataProviderName(userNotificationVO.getContent().getDataProviderName());
      userNotification.setDatasetId(userNotificationVO.getContent().getDatasetId());
      userNotification.setDatasetName(userNotificationVO.getContent().getDatasetName());
      userNotification.setTypeStatus(userNotificationVO.getContent().getTypeStatus());
      userNotificationRepository.save(userNotification);
      LOG.info("User Notification created succesfully in mongo");
    } catch (IllegalArgumentException e) {
      LOG_ERROR.error("Error creating a User Notification");
      throw new EEAException(e.getMessage());
    }
  }

  /**
   * Find user notifications by user.
   *
   * @return the list
   */
  @Override
  public List<UserNotificationVO> findUserNotificationsByUser() {

    List<UserNotification> listUserNotification = userNotificationRepository
        .findByUserId(SecurityContextHolder.getContext().getAuthentication().getName());
    List<UserNotificationVO> listUserNotificationVO = new ArrayList<>();
    for (UserNotification userNotification : listUserNotification) {
      UserNotificationVO userNotificationVO = new UserNotificationVO();
      userNotificationVO.setInsertDate(userNotification.getInsertDate());
      userNotificationVO.setEventType(userNotification.getEventType().toString());
      UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
      userNotificationContentVO.setDataflowId(userNotification.getDataflowId());
      userNotificationContentVO.setDataflowName(userNotification.getDataflowName());
      userNotificationContentVO.setDatasetId(userNotification.getDatasetId());
      userNotificationContentVO.setDatasetName(userNotification.getDatasetName());
      userNotificationContentVO.setProviderId(userNotification.getProviderId());
      userNotificationContentVO.setDataProviderName(userNotification.getDataProviderName());
      userNotificationContentVO.setTypeStatus(userNotification.getTypeStatus());
      userNotificationVO.setContent(userNotificationContentVO);
      listUserNotificationVO.add(userNotificationVO);
    }
    return listUserNotificationVO;
  }
}
