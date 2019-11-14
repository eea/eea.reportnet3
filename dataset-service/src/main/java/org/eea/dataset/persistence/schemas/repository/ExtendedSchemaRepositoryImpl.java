package org.eea.dataset.persistence.schemas.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.exception.EEAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  /** The mongo database. */
  @Autowired
  private MongoDatabase mongoDatabase;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

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
      LOG_ERROR.error("error deleting field: ", e);
      throw new EEAException(e);
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
      LOG_ERROR.error("error updating field: ", e);
      throw new EEAException(e);
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
  public UpdateResult createFieldSchema(String datasetSchemaId, FieldSchema fieldSchema)
      throws EEAException {
    try {
      return mongoOperations.updateMulti(
          new Query(new Criteria("_id").is(new ObjectId(datasetSchemaId))).addCriteria(
              new Criteria("tableSchemas.recordSchema._id").is(fieldSchema.getIdRecord())),
          new Update().push("tableSchemas.$.recordSchema.fieldSchemas", fieldSchema),
          DataSetSchema.class);
    } catch (IllegalArgumentException e) {
      LOG_ERROR.error("error creating field: ", e);
      throw new EEAException(e);
    }
  }

  /**
   * Update table schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchema the table schema
   * @return the update result
   * @throws EEAException the EEA exception
   */
  @Override
  public UpdateResult updateTableSchema(String datasetSchemaId, TableSchema tableSchema)
      throws EEAException {
    try {
      return mongoDatabase.getCollection("DataSetSchema").updateOne(
          new Document("_id", new ObjectId(datasetSchemaId)).append("tableSchemas._id",
              tableSchema.getIdTableSchema()),
          new Document("$set",
              new Document("tableSchemas.$[tableSchemaId]", Document.parse(tableSchema.toJSON()))),
          new UpdateOptions().arrayFilters(
              Arrays.asList(new Document("tableSchemaId._id", tableSchema.getIdTableSchema()))));
    } catch (IllegalArgumentException e) {
      LOG_ERROR.error("error updating table: ", e);
      throw new EEAException(e);
    }
  }

  /**
   * Insert table in position.
   *
   * @param idDatasetSchema the id dataset schema
   * @param classToEntity the class to entity
   * @param newPosition the new position
   * @return the update result
   * @throws EEAException the EEA exception
   */
  @Override
  public UpdateResult insertTableInPosition(String idDatasetSchema, TableSchema tableSchema,
      int position) throws EEAException {
    try {
      List<Document> list = new ArrayList<>();
      list.add(Document.parse(tableSchema.toJSON()));
      return mongoDatabase.getCollection("DataSetSchema").updateOne(
          new Document("_id", new ObjectId(idDatasetSchema)),
          new Document("$push", new Document("tableSchemas",
              new Document("$each", list).append("$position", position))));
    } catch (IllegalArgumentException e) {
      LOG_ERROR.error("error inserting table: ", e);
      throw new EEAException(e);
    }
  }

  /**
   * Insert field in position.
   *
   * @param idDatasetSchema the id dataset schema
   * @param field the field
   * @param position the position
   * @return the update result
   * @throws EEAException the EEA exception
   */
  @Override
  public UpdateResult insertFieldInPosition(String idDatasetSchema, FieldSchema fieldSchema,
      int position) throws EEAException {
    try {
      List<Document> list = new ArrayList<>();
      list.add(Document.parse(fieldSchema.toJSON()));
      return mongoDatabase.getCollection("DataSetSchema").updateMany(
          new Document("_id", new ObjectId(idDatasetSchema)).append("tableSchemas.recordSchema._id",
              fieldSchema.getIdRecord()),
          new Document("$push", new Document("tableSchemas.$.recordSchema.fieldSchemas",
              new Document("$each", list).append("$position", position))));
    } catch (IllegalArgumentException e) {
      LOG_ERROR.error("error inserting field: ", e);
      throw new EEAException(e);
    }
  }
}
