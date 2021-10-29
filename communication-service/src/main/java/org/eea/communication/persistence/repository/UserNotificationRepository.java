package org.eea.communication.persistence.repository;

import java.util.List;
import org.bson.types.ObjectId;
import org.eea.communication.persistence.UserNotification;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The Interface UserNotificationRepository.
 */
public interface UserNotificationRepository extends MongoRepository<UserNotification, ObjectId> {

  /**
   * Find by user id.
   *
   * @param userId the user id
   * @return the list
   */
  List<UserNotification> findByUserId(String userId);
}
