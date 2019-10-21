package org.eea.dataset.persistence.schemas.repository;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.BasicDBObject;

/**
 * The Class ExtendedSchemaRepositoryImpl.
 */
public class ExtendedSchemaRepositoryImpl implements ExtendedSchemaRepository {

  /** The mongo. */
  @Autowired
  MongoOperations mongo;

  /**
   * Delete table schema by id.
   *
   * @param idTableSchema the id table schema
   */
  @Override
  public void deleteTableSchemaById(String idTableSchema) {
    Update update =
        new Update().pull("tableSchemas", new BasicDBObject("_id", new ObjectId(idTableSchema)));
    mongo.updateMulti(new Query(), update, DataSetSchema.class);
  }

}
