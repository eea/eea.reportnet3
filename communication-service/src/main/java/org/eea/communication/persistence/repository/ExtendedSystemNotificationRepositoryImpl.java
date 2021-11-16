package org.eea.communication.persistence.repository;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.eea.communication.persistence.SystemNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import com.mongodb.client.MongoDatabase;

/**
 * The Class ExtendedSystemNotificationRepositoryImpl.
 */
public class ExtendedSystemNotificationRepositoryImpl
    implements ExtendedSystemNotificationRepository {

  /** The mongo template. */
  @Autowired
  private MongoTemplate mongoTemplate;

  /** The mongo database. */
  @Autowired
  private MongoDatabase mongoDatabase;

  /**
   * Delete system notfication by id.
   *
   * @param systemNotificationId the system notification id
   */
  @Override
  public void deleteSystemNotficationById(String systemNotificationId) {
    mongoTemplate.findAndRemove(new Query(Criteria.where("_id").is(systemNotificationId)),
        SystemNotification.class);
  }

  /**
   * Update system notfication by id.
   *
   * @param systemNotification the system notification
   */
  @Override
  public void updateSystemNotficationById(SystemNotification systemNotification) {
    Bson filter = new Document("_id", systemNotification.getId());
    Bson fieldsUpdate = new Document("$set",
        new Document("message", systemNotification.getMessage())
            .append("enabled", systemNotification.isEnabled())
            .append("level", systemNotification.getLevel().toString()));

    mongoDatabase.getCollection("SystemNotification").updateOne(filter, fieldsUpdate);

  }

}
