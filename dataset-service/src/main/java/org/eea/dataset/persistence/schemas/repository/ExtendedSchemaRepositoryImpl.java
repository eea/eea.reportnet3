package org.eea.dataset.persistence.schemas.repository;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;

/**
 * The Class ExtendedSchemaRepositoryImpl.
 */
public class ExtendedSchemaRepositoryImpl implements ExtendedSchemaRepository {

  /** The mongo operations. */
  @Autowired
  private MongoOperations mongoOperations;

  /** The mongo template. */
  @Autowired
  private MongoTemplate mongoTemplate;

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
   * Delete dataset schema by id.
   *
   * @param idSchema the id schema
   */
  @Override
  public void deleteDatasetSchemaById(String idSchema) {
    mongoTemplate.findAndRemove(new Query(Criteria.where("_id").is(idSchema)), DataSetSchema.class);
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
    query.addCriteria(new Criteria("_id").is(new ObjectId(idDatasetSchema)));
    mongoOperations.updateMulti(query, update, DataSetSchema.class);
  }

  /**
   * Find by id table schema.
   *
   * @param idTableSchema the id table schema
   * @return the data set schema
   */
  @Override
  public DataSetSchema findByIdTableSchema(String idTableSchema) {
    Query query = new Query();
    query.addCriteria(Criteria.where("tableSchemas._id").is(new ObjectId(idTableSchema)));
    query.fields().include("tableSchemas.$");
    return mongoTemplate.findOne(query, DataSetSchema.class);
  }

  /**
   * Delete field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @return the update result
   */
  @Override
  public UpdateResult deleteFieldSchema(String datasetSchemaId, String fieldSchemaId) {
    Update update = new Update().pull("tableSchemas.$.recordSchema.fieldSchemas",
        new BasicDBObject("_id", new ObjectId(fieldSchemaId)));
    Query query = new Query();
    query.addCriteria(new Criteria("_id").is(new ObjectId(datasetSchemaId)));
    query.addCriteria(
        new Criteria("tableSchemas.recordSchema.fieldSchemas._id").is(new ObjectId(fieldSchemaId)));
    return mongoOperations.updateMulti(query, update, DataSetSchema.class);
  }
}
