package org.eea.communication.service;

import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.communication.UserNotificationListVO;
import org.eea.interfaces.vo.communication.UserNotificationVO;
import org.eea.kafka.domain.EventType;

/**
 * The Interface NotificationService.
 */
public interface NotificationService {

  /**
   * Send.
   *
   * @param user the user
   * @param type the type
   * @param notification the notification
   * @return true, if successful
   */
  boolean send(String user, EventType type, Map<String, Object> notification);


  /**
   * Creates the user notification.
   *
   * @param userNotificationVO the user notification VO
   * @throws EEAException the EEA exception
   */
  void createUserNotification(UserNotificationVO userNotificationVO) throws EEAException;

  /**
   * Find user notifications by user.
   *
   * @param pagenNum the pagen num
   * @param pageSize the page size
   * @return the list
   */
  public UserNotificationListVO findUserNotificationsByUserPaginated(Integer pagenNum,
      Integer pageSize);
}
