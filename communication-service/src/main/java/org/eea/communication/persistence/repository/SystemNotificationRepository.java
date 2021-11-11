package org.eea.communication.persistence.repository;

import org.bson.types.ObjectId;
import org.eea.communication.persistence.SystemNotification;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The Interface SystemNotificationRepository.
 */
public interface SystemNotificationRepository
    extends MongoRepository<SystemNotification, ObjectId>, ExtendedSystemNotificationRepository {

}
