package org.eea.communication.service;

import java.util.Map;
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
}
