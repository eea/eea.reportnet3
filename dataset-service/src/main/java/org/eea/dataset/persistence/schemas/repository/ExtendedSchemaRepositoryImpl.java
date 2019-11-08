package org.eea.dataset.persistence.schemas.repository;

import java.util.Arrays;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.exception.EEAException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
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

  @Autowired
  private MongoDatabase mongoDatabase;

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
   * Delete field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @return the update result
   */
  @Override
  public UpdateResult deleteFieldSchema(String datasetSchemaId, String fieldSchemaId)
      throws EEAException {
    try {
      return mongoOperations.updateMulti(
          new Query(new Criteria("_id").is(new ObjectId(datasetSchemaId)))
              .addCriteria(new Criteria("tableSchemas.recordSchema.fieldSchemas._id")
                  .is(new ObjectId(fieldSchemaId))),
          new Update().pull("tableSchemas.$.recordSchema.fieldSchemas",
              new BasicDBObject("_id", new ObjectId(fieldSchemaId))),
          DataSetSchema.class);
    } catch (IllegalArgumentException e) {
      throw new EEAException(e.getMessage());
    }
  }

  /**
   * Update field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchema the field schema
   * @return the update result
   * @throws EEAException the EEA exception
   */
  @Override
  public UpdateResult updateFieldSchema(String datasetSchemaId, FieldSchema fieldSchema)
      throws EEAException {
    try {
      return mongoDatabase.getCollection("DataSetSchema").updateMany(
          new Document("_id", new ObjectId(datasetSchemaId))
              .append("tableSchemas.recordSchema.fieldSchemas._id", fieldSchema.getIdFieldSchema()),
          new Document("$set",
              new Document("tableSchemas.$.recordSchema.fieldSchemas.$[fieldSchemaId]",
                  Document.parse(fieldSchema.toJSON()))),
          new UpdateOptions().arrayFilters(
              Arrays.asList(new Document("fieldSchemaId._id", fieldSchema.getIdFieldSchema()))));
    } catch (IllegalArgumentException e) {
      throw new EEAException(e.getMessage());
    }
  }

  /**
   * Creates the field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @param fieldSchema the field schema
   * @return the update result
   * @throws EEAException the EEA exception
   */
  @Override
  public UpdateResult createFieldSchema(String datasetSchemaId, String tableSchemaId,
      FieldSchema fieldSchema) throws EEAException {
    try {
      return mongoOperations.updateMulti(
          new Query(new Criteria("_id").is(new ObjectId(datasetSchemaId)))
              .addCriteria(new Criteria("tableSchemas._id").is(new ObjectId(tableSchemaId)))
              .addCriteria(
                  new Criteria("tableSchemas.recordSchema._id").is(fieldSchema.getIdRecord())),
          new Update().push("tableSchemas.$.recordSchema.fieldSchemas", fieldSchema),
          DataSetSchema.class);
    } catch (IllegalArgumentException e) {
      throw new EEAException(e.getMessage());
    }
  }
}
