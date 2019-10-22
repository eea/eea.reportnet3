package org.eea.dataset.persistence.schemas.repository;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.BasicDBObject;

/**
 * The Class ExtendedSchemaRepositoryImpl.
 */
public class ExtendedSchemaRepositoryImpl implements ExtendedSchemaRepository {

  /** The mongo operations. */
  @Autowired
  private MongoOperations mongoOperations;

  /**
   * Delete table schema by id.
   *
   * @param idTableSchema the id table schema
   */
  @Override
  public void deleteTableSchemaById(String idTableSchema) {
    Update update =
        new Update().pull("tableSchemas", new BasicDBObject("_id", new ObjectId(idTableSchema)));
    mongoOperations.updateMulti(new Query(), update, DataSetSchema.class);
  }

  /**
   * Insert table schema.
   *
   * @param table the table
   * @param idDatasetSchema the id dataset schema
   */
  @Override
  public void insertTableSchema(TableSchema table, String idDatasetSchema) {
    Update update = new Update().push("tableSchemas", table);
    Query query = new Query();
    query.addCriteria(new Criteria("_id").is(idDatasetSchema));
    mongoOperations.updateMulti(query, update, DataSetSchema.class);
  }

}
