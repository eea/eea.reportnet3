package org.eea.interfaces.controller.communication;

import java.util.List;
import org.eea.interfaces.vo.communication.UserNotificationVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The Interface NotificationController.
 */
public interface NotificationController {

  /**
   * The Interface NotificationControllerZuul.
   */
  @FeignClient(value = "communication", contextId = "notification", path = "/notification")
  interface NotificationControllerZuul extends NotificationController {
  }

  /**
   * Creates the user notification.
   *
   * @param userNotificationVO the user notification VO
   */
  @PostMapping(value = "/createUserNotification")
  void createUserNotification(@RequestBody UserNotificationVO userNotificationVO);

  /**
   * Creates the user notification private.
   *
   * @param userNotificationVO the user notification VO
   */
  @PostMapping(value = "/private/createUserNotification")
  void createUserNotificationPrivate(@RequestBody UserNotificationVO userNotificationVO);

  /**
   * Find user notifications by user.
   *
   * @return the list
   */
  @GetMapping(value = "/findUserNotifications")
  List<UserNotificationVO> findUserNotificationsByUser();

}
