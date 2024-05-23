package org.eea.communication.controller;

import java.util.List;
import org.eea.communication.service.NotificationService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController;
import org.eea.interfaces.vo.communication.SystemNotificationVO;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.communication.UserNotificationListVO;
import org.eea.interfaces.vo.communication.UserNotificationVO;
import org.eea.lock.annotation.LockCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(NotificationControllerImpl.class);

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
      LOG.error("Creating user notification produced an error: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.CREATING_NOTIFICATION);
    } catch (Exception e) {
      String eventType = (userNotificationVO != null) ? userNotificationVO.getEventType() : null;
      Long dataflowId = (userNotificationVO != null && userNotificationVO.getContent() != null) ? userNotificationVO.getContent().getDataflowId() : null;
      LOG.error("Unexpected error! Error creating user notification of type {} for dataflowId {} Message: {}", eventType, dataflowId, e.getMessage());
      throw e;
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
      userNotificationVO.setContent(content);
      notificationService.createUserNotification(userNotificationVO);
    } catch (EEAException e) {
      LOG.error("Creating user notification produced an error: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.CREATING_NOTIFICATION);
    } catch (Exception e) {
      Long dataflowId = (content != null) ? content.getDataflowId() : null;
      LOG.error("Unexpected error! Error creating private user notification of type {} for dataflowId {} Message: {}", eventType, dataflowId, e.getMessage());
      throw e;
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
      LOG.error("Creating system notification produced an error: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.CREATING_SYSTEM_NOTIFICATION);
    } catch (Exception e) {
      String notificationId = (systemNotificationVO != null) ? systemNotificationVO.getId() : null;
      String message = (systemNotificationVO != null) ? systemNotificationVO.getMessage() : null;
      LOG.error("Unexpected error! Error creating system notification with id {} and message {}. Error Message: {}", notificationId, message, e.getMessage());
      throw e;
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
      LOG.error("Deleting system notification produced an error: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DELETING_SYSTEM_NOTIFICATION);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error deleting system notification with id {} Message: {}", systemNotificationId, e.getMessage());
      throw e;
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
      LOG.error("Updating system notification produced an error: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.UPDATING_SYSTEM_NOTIFICATION);
    } catch (Exception e) {
      String notificationId = (systemNotificationVO != null) ? systemNotificationVO.getId() : null;
      String message = (systemNotificationVO != null) ? systemNotificationVO.getMessage() : null;
      LOG.error("Unexpected error! Error updating system notification with id {} and message {}. Error Message: {}", notificationId, message, e.getMessage());
      throw e;
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
    try {
      return notificationService.findUserNotificationsByUserPaginated(pageNum, pageSize);
    } catch (Exception e) {
      String user = SecurityContextHolder.getContext().getAuthentication().getName();
      LOG.error("Unexpected error! Error retrieving notifications for user {}. Message: {}", user, e.getMessage());
      throw e;
    }
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
    try{
      return notificationService.findSystemNotifications();
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving system notifications. Message: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Check any system notification enabled.
   *
   * @return true, if successful
   */
  @Override
  @PreAuthorize("isAuthenticated()")
  @GetMapping(value = "/checkAnySystemNotificationEnabled")
  @ApiOperation(value = "Check any system notifications is enabled", hidden = true)
  public boolean checkAnySystemNotificationEnabled() {
    return notificationService.checkAnySystemNotificationEnabled();
  }
}
