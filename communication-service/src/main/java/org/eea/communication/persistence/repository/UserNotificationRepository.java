package org.eea.communication.persistence.repository;

import java.util.List;
import org.bson.types.ObjectId;
import org.eea.communication.persistence.UserNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * The Interface UserNotificationRepository.
 */
public interface UserNotificationRepository
    extends PagingAndSortingRepository<UserNotification, ObjectId> {

  /**
   * Find by user id.
   *
   * @param userId the user id
   * @param pageable the pageable
   * @return the list
   */
  List<UserNotification> findByUserId(String userId, Pageable pageable);
}
