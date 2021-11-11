package org.eea.interfaces.controller.communication;

import java.util.List;
import org.eea.interfaces.vo.communication.SystemNotificationVO;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.communication.UserNotificationListVO;
import org.eea.interfaces.vo.communication.UserNotificationVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
   * @param eventType the event type
   * @param content the content
   */
  @PostMapping(value = "/private/createUserNotification")
  void createUserNotificationPrivate(@RequestParam("eventType") String eventType,
      @RequestBody UserNotificationContentVO content);

  /**
   * Creates the system notification.
   *
   * @param systemNotificationVO the system notification VO
   */
  @PostMapping(value = "/createSystemNotification")
  void createSystemNotification(@RequestBody SystemNotificationVO systemNotificationVO);

  /**
   * Delete system notification.
   *
   * @param systemNotificationId the system notification id
   */
  @DeleteMapping(value = "/deleteSystemNotification/{systemNotificationId}")
  public void deleteSystemNotification(
      @PathVariable("systemNotificationId") String systemNotificationId);

  /**
   * Update system notification.
   *
   * @param systemNotificationVO the system notification VO
   */
  @PutMapping(value = "/updateSystemNotification")
  public void updateSystemNotification(@RequestBody SystemNotificationVO systemNotificationVO);

  /**
   * Find user notifications by user.
   *
   * @param pageNum the page num
   * @param pageSize the page size
   * @return the list
   */
  @GetMapping(value = "/findUserNotifications")
  UserNotificationListVO findUserNotificationsByUser(
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", required = false) Integer pageSize);

  /**
   * Find system notifications.
   *
   * @return the list
   */
  @GetMapping(value = "/findSystemNotifications")
  List<SystemNotificationVO> findSystemNotifications();

}
