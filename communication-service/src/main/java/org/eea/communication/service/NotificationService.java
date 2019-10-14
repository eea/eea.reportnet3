package org.eea.communication.service;

/**
 * The Interface NotificationService.
 */
public interface NotificationService {

  /**
   * Process the event to get the message and a user to send it. EEAEventVO should has set keys
   * "userId" and "message" on data property.
   *
   * @param userId the user id
   * @param message the message
   * @return true, if successful
   */
  boolean send(String userId, String message);
}
