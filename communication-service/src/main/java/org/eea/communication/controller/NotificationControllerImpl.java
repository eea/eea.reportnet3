package org.eea.communication.controller;

import java.util.Date;
import java.util.List;
import org.eea.communication.service.NotificationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController;
import org.eea.interfaces.vo.communication.SystemNotificationVO;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.communication.UserNotificationListVO;
import org.eea.interfaces.vo.communication.UserNotificationVO;
import org.eea.lock.annotation.LockCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

/**
 * The Class NotificationControllerImpl.
 */
@RestController
@RequestMapping("/notification")
@ApiIgnore
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
      value = "Notification containing the data") @RequestBody UserNotificationVO userNotificationVO) {
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
   * Creates the system notification.
   *
   * @param systemNotificationVO the system notification VO
   */
  @Override
  @PreAuthorize("hasAnyRole('ADMIN')")
  @PostMapping(value = "/createSystemNotification")
  @ApiOperation(value = "Create System Notification", hidden = true)
  public void createSystemNotification(@ApiParam(
      value = "Notification Schema containing the data") @RequestBody SystemNotificationVO systemNotificationVO) {
    try {
      notificationService.createSystemNotification(systemNotificationVO);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  /**
   * Delete system notification.
   *
   * @param systemNotificationId the system notification id
   */
  @Override
  @PreAuthorize("hasAnyRole('ADMIN')")
  @DeleteMapping(value = "/deleteSystemNotification/{systemNotificationId}")
  @ApiOperation(value = "Delete System Notification", hidden = true)
  public void deleteSystemNotification(@ApiParam(type = "String", value = "system notification Id",
      example = "5cf0e9b3b793310e9ceca190") @LockCriteria(
          name = "systemNotificationId") @PathVariable("systemNotificationId") String systemNotificationId) {
    try {
      notificationService.deleteSystemNotification(systemNotificationId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  /**
   * Update system notification.
   *
   * @param systemNotificationVO the system notification VO
   */
  @Override
  @PreAuthorize("hasAnyRole('ADMIN')")
  @PutMapping(value = "/updateSystemNotification")
  @ApiOperation(value = "Update System Notification", hidden = true)
  public void updateSystemNotification(@ApiParam(
      value = "System Notification containing the data") @RequestBody SystemNotificationVO systemNotificationVO) {
    try {
      notificationService.updateSystemNotification(systemNotificationVO);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
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
  @PreAuthorize("isAuthenticated()")
  @GetMapping(value = "/findUserNotifications")
  @ApiOperation(value = "Retrieves all user notifications form a user", hidden = true)
  public UserNotificationListVO findUserNotificationsByUser(
      @ApiParam(type = "Integer", value = "page number", example = "0") @RequestParam(
          value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @ApiParam(type = "Integer", value = "page size",
          example = "0") @RequestParam(value = "pageSize", required = false) Integer pageSize) {

    return notificationService.findUserNotificationsByUserPaginated(pageNum, pageSize);
  }

  /**
   * Find system notifications.
   *
   * @return the list
   */
  @Override
  @PreAuthorize("isAuthenticated()")
  @GetMapping(value = "/findSystemNotifications")
  @ApiOperation(value = "Retrieves all system notifications", hidden = true)
  public List<SystemNotificationVO> findSystemNotifications() {

    return notificationService.findSystemNotifications();
  }
}
