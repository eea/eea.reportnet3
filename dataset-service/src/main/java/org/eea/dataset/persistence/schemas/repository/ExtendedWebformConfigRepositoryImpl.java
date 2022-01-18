package org.eea.dataset.persistence.schemas.repository;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfig;
import org.springframework.beans.factory.annotation.Autowired;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;

/**
 * The Class ExtendedWebformConfigRepositoryImpl.
 */
public class ExtendedWebformConfigRepositoryImpl implements ExtendedWebformConfigRepository {

  /** The mongo database. */
  @Autowired
  private MongoDatabase mongoDatabase;

  /**
   * Update web form config.
   *
   * @param webform the webform
   */
  @Override
  public void updateWebFormConfig(WebformConfig webform) {

    Bson updates = Updates.combine(Updates.set("file", webform.getFile()),
        Updates.set("name", webform.getName()));

    mongoDatabase.getCollection("WebformConfig")
        .updateOne(new Document("idReferenced", webform.getIdReferenced()), updates);

  }

}
