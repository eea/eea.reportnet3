package org.eea.communication.persistence.repository;

import java.util.List;
import org.bson.types.ObjectId;
import org.eea.communication.persistence.SystemNotification;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The Interface SystemNotificationRepository.
 */
public interface SystemNotificationRepository
    extends MongoRepository<SystemNotification, ObjectId>, ExtendedSystemNotificationRepository {

  /**
   * Find by enabled true.
   *
   * @return the list
   */
  List<SystemNotification> findByEnabledTrue();

  /**
   * Exists by enabled true.
   *
   * @return true, if successful
   */
  boolean existsByEnabledTrue();
}
