package org.eea.communication.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eea.communication.mapper.SystemNotificationMapper;
import org.eea.communication.mapper.UserNotificationMapper;
import org.eea.communication.persistence.SystemNotification;
import org.eea.communication.persistence.UserNotification;
import org.eea.communication.persistence.repository.SystemNotificationRepository;
import org.eea.communication.persistence.repository.UserNotificationRepository;
import org.eea.communication.service.NotificationService;
import org.eea.communication.service.model.Notification;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.communication.SystemNotificationVO;
import org.eea.interfaces.vo.communication.UserNotificationListVO;
import org.eea.interfaces.vo.communication.UserNotificationVO;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.kafka.domain.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * The Class NotificationServiceImpl.
 */
@Service("notificationService")
public class NotificationServiceImpl implements NotificationService {


  /** The Constant MAX_LENGTH_MESSAGE. */
  private static final int MAX_LENGTH_MESSAGE = 300;

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

  /** The system notification repository. */
  @Autowired
  private SystemNotificationRepository systemNotificationRepository;

  /** The system notification mapper. */
  @Autowired
  SystemNotificationMapper systemNotificationMapper;

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
   * Creates the system notification.
   *
   * @param systemNotificationVO the system notification VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void createSystemNotification(SystemNotificationVO systemNotificationVO)
      throws EEAException {
    try {
      SystemNotification systemNotification =
          systemNotificationMapper.classToEntity(systemNotificationVO);

      systemNotification.setMessage(systemNotification.getMessage().substring(0,
          Math.min(systemNotification.getMessage().length(), MAX_LENGTH_MESSAGE)));

      systemNotification = systemNotificationRepository.save(systemNotification);
      SystemNotificationVO sysNotiVO = systemNotificationMapper.entityToClass(systemNotification);
      template.convertAndSend("/user/queue/systemnotifications", sysNotiVO);
      LOG.info("System Notification created succesfully in mongo");
    } catch (IllegalArgumentException e) {
      LOG_ERROR.error("Error creating a System Notification. {}", e.getMessage(), e);
      throw new EEAException(e.getMessage());
    }
  }

  /**
   * Delete system notification.
   *
   * @param systemNotificationId the system notification id
   * @throws EEAException the EEA exception
   */
  @Override
  @Modifying
  public void deleteSystemNotification(String systemNotificationId) throws EEAException {
    try {
      systemNotificationRepository.deleteSystemNotficationById(systemNotificationId);
      if (systemNotificationRepository.findByEnabledTrue().isEmpty()) {
        template.convertAndSend("/user/queue/systemnotifications",
            new Notification(EventType.NO_ENABLED_SYSTEM_NOTIFICATIONS, Collections.emptyMap()));
      }
      LOG.info("System Notification deleted succesfully in mongo");
    } catch (IllegalArgumentException e) {
      LOG_ERROR.error("Error deleting a System Notification");
      throw new EEAException(e.getMessage());
    }
  }

  /**
   * Update system notification.
   *
   * @param systemNotificationVO the system notification VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateSystemNotification(SystemNotificationVO systemNotificationVO)
      throws EEAException {
    try {
      SystemNotification systemNotification =
          systemNotificationMapper.classToEntity(systemNotificationVO);

      systemNotification.setMessage(systemNotification.getMessage().substring(0,
          Math.min(systemNotification.getMessage().length(), MAX_LENGTH_MESSAGE)));

      systemNotificationRepository.updateSystemNotficationById(systemNotification);
      if (systemNotification != null && systemNotification.isEnabled()) {
        template.convertAndSend("/user/queue/systemnotifications", systemNotificationVO);
      } else {
        if (systemNotificationRepository.findByEnabledTrue().isEmpty()) {
          template.convertAndSend("/user/queue/systemnotifications",
              new Notification(EventType.NO_ENABLED_SYSTEM_NOTIFICATIONS, Collections.emptyMap()));
        }
      }
      LOG.info("System Notification updated succesfully in mongo");

    } catch (IllegalArgumentException e) {
      LOG_ERROR.error("Error updating a System Notification");
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

  /**
   * Find system notifications.
   *
   * @return the list
   */
  @Override
  public List<SystemNotificationVO> findSystemNotifications() {
    List<SystemNotification> listSystemNotification;
    if (isAdmin()) {
      listSystemNotification = systemNotificationRepository.findAll();
    } else {
      listSystemNotification = systemNotificationRepository.findByEnabledTrue();
    }
    return systemNotificationMapper.entityListToClass(listSystemNotification);
  }

  /**
   * Check any system notification enabled.
   *
   * @return true, if successful
   */
  @Override
  public boolean checkAnySystemNotificationEnabled() {
    return systemNotificationRepository.existsByEnabledTrue();
  }

  /**
   * Checks if is admin.
   *
   * @return true, if is admin
   */
  private boolean isAdmin() {
    String roleAdmin = "ROLE_" + SecurityRoleEnum.ADMIN;
    return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
        .anyMatch(role -> roleAdmin.equals(role.getAuthority()));
  }
}
