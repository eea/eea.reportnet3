package org.eea.communication.persistence.repository;

import org.eea.communication.persistence.SystemNotification;

/**
 * The Interface ExtendedSystemNotificationRepository.
 */
public interface ExtendedSystemNotificationRepository {

  /**
   * Delete system notfication by id.
   *
   * @param systemNotificationId the system notification id
   */
  void deleteSystemNotficationById(String systemNotificationId);

  /**
   * Update system notfication by id.
   *
   * @param systemNotification the system notification
   */
  void updateSystemNotficationById(SystemNotification systemNotification);
}
