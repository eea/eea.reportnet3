package org.eea.communication.controller;

import java.util.Date;
import java.util.List;
import org.eea.communication.service.NotificationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.communication.UserNotificationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * The Class NotificationControllerImpl.
 */
@RestController
@RequestMapping("/notification")
@Api(tags = "Notification : Notification Manager")
public class NotificationControllerImpl implements NotificationController {

  /** The notification service. */
  @Autowired
  private NotificationService notificationService;

  /**
   * Creates the user notification.
   *
   * @param userNotificationVO the user notification VO
   */
  @Override
  @PreAuthorize("isAuthenticated()")
  @PostMapping(value = "/createUserNotification")
  @ApiOperation(value = "Create User Notification", hidden = true)
  public void createUserNotification(@ApiParam(
      value = "Notification Schema containing the data") @RequestBody UserNotificationVO userNotificationVO) {
    try {
      notificationService.createUserNotification(userNotificationVO);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  /**
   * Creates the user notification private.
   *
   * @param eventType the event type
   * @param content the content
   */
  @Override
  @PostMapping(value = "/private/createUserNotification")
  @ApiOperation(value = "Create User Notification Private", hidden = true)
  public void createUserNotificationPrivate(@RequestParam("eventType") String eventType,
      @RequestBody UserNotificationContentVO content) {
    try {
      UserNotificationVO userNotificationVO = new UserNotificationVO();
      userNotificationVO.setEventType(eventType);
      userNotificationVO.setInsertDate(new Date());
      userNotificationVO.setContent(content);
      notificationService.createUserNotification(userNotificationVO);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  /**
   * Find user notifications by user.
   *
   * @return the list
   */
  @Override
  @PreAuthorize("isAuthenticated()")
  @GetMapping(value = "/findUserNotifications")
  @ApiOperation(value = "Retrieves all user notifications form a user", hidden = true)
  public List<UserNotificationVO> findUserNotificationsByUser() {

    return notificationService.findUserNotificationsByUser();
  }
}
