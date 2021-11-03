package org.eea.communication.service.impl;

import java.util.List;
import java.util.Map;
import org.eea.communication.mapper.UserNotificationMapper;
import org.eea.communication.persistence.UserNotification;
import org.eea.communication.persistence.repository.UserNotificationRepository;
import org.eea.communication.service.NotificationService;
import org.eea.communication.service.model.Notification;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.communication.UserNotificationListVO;
import org.eea.interfaces.vo.communication.UserNotificationVO;
import org.eea.kafka.domain.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

  /** The user notification mapper. */
  @Autowired
  private UserNotificationMapper userNotificationMapper;

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
      UserNotification userNotification = userNotificationMapper.classToEntity(userNotificationVO);
      userNotification.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
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
   * @param pageNum the page num
   * @param pageSize the page size
   * @return the list
   */
  @Override
  public UserNotificationListVO findUserNotificationsByUserPaginated(Integer pageNum,
      Integer pageSize) {
    Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("insertDate").descending());
    UserNotificationListVO userNotificationListVO = new UserNotificationListVO();
    List<UserNotification> listUserNotification = userNotificationRepository
        .findByUserId(SecurityContextHolder.getContext().getAuthentication().getName(), pageable);
    List<UserNotification> totalRecords = userNotificationRepository
        .findByUserId(SecurityContextHolder.getContext().getAuthentication().getName(), null);
    List<UserNotificationVO> listUserNotificationVO =
        userNotificationMapper.entityListToClass(listUserNotification);
    userNotificationListVO.setUserNotifications(listUserNotificationVO);
    userNotificationListVO.setTotalRecords(
        !CollectionUtils.isEmpty(totalRecords) ? Long.valueOf(totalRecords.size()) : 0);
    return userNotificationListVO;
  }
}
