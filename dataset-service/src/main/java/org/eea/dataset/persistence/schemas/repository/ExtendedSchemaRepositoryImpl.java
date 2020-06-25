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
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
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

  /** The mongo converter. */
  @Autowired
  private MongoConverter mongoConverter;

  /**
   * Delete table schema by id.
   *
   * @param idTableSchema the id table schema
   */
  @Override
  public void deleteTableSchemaById(String idTableSchema) {
    Update update = new Update().pull(LiteralConstants.TABLE_SCHEMAS,
        new BasicDBObject("_id", new ObjectId(idTableSchema)));
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
    Update update = new Update().push(LiteralConstants.TABLE_SCHEMAS, table);
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
   * @throws EEAException the EEA exception
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
  public UpdateResult updateFieldSchema(String datasetSchemaId, Document fieldSchema)
      throws EEAException {
    try {
      return mongoDatabase.getCollection(LiteralConstants.DATASET_SCHEMA).updateMany(
          new Document("_id", new ObjectId(datasetSchemaId))
              .append("tableSchemas.recordSchema.fieldSchemas._id", fieldSchema.get("_id")),
          new Document("$set",
              new Document("tableSchemas.$.recordSchema.fieldSchemas.$[fieldSchemaId]",
                  fieldSchema)),
          new UpdateOptions().arrayFilters(
              Arrays.asList(new Document("fieldSchemaId._id", fieldSchema.get("_id")))));
    } catch (IllegalArgumentException e) {
      LOG_ERROR.error("error updating field: ", e);
      throw new EEAException(e);
    }
  }

  /**
   * Creates the field schema.
   *
   * @param datasetSchemaId the dataset schema id
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
  public UpdateResult updateTableSchema(String datasetSchemaId, Document tableSchema)
      throws EEAException {
    try {
      return mongoDatabase.getCollection(LiteralConstants.DATASET_SCHEMA).updateOne(
          new Document("_id", new ObjectId(datasetSchemaId)).append("tableSchemas._id",
              tableSchema.get("_id")),
          new Document("$set", new Document("tableSchemas.$[tableSchemaId]", tableSchema)),
          new UpdateOptions().arrayFilters(
              Arrays.asList(new Document("tableSchemaId._id", tableSchema.get("_id")))));
    } catch (IllegalArgumentException e) {
      LOG_ERROR.error("error updating table: ", e);
      throw new EEAException(e);
    }
  }

  /**
   * Insert table in position.
   *
   * @param idDatasetSchema the id dataset schema
   * @param tableSchema the table schema
   * @param position the position
   * @return the update result
   * @throws EEAException the EEA exception
   */
  @Override
  public UpdateResult insertTableInPosition(String idDatasetSchema, Document tableSchema,
      int position) throws EEAException {
    try {
      List<Document> list = new ArrayList<>();
      list.add(tableSchema);
      return mongoDatabase.getCollection(LiteralConstants.DATASET_SCHEMA).updateOne(
          new Document("_id", new ObjectId(idDatasetSchema)),
          new Document("$push", new Document(LiteralConstants.TABLE_SCHEMAS,
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
   * @param fieldSchema the field schema
   * @param position the position
   * @return the update result
   * @throws EEAException the EEA exception
   */
  @Override
  public UpdateResult insertFieldInPosition(String idDatasetSchema, Document fieldSchema,
      int position) throws EEAException {
    try {
      List<Document> list = new ArrayList<>();
      list.add(fieldSchema);
      return mongoDatabase.getCollection(LiteralConstants.DATASET_SCHEMA).updateMany(
          new Document("_id", new ObjectId(idDatasetSchema)).append("tableSchemas.recordSchema._id",
              fieldSchema.get("idRecord")),
          new Document("$push", new Document("tableSchemas.$.recordSchema.fieldSchemas",
              new Document("$each", list).append("$position", position))));
    } catch (IllegalArgumentException e) {
      LOG_ERROR.error("error inserting field: ", e);
      throw new EEAException(e);
    }
  }

  /**
   * Find table schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @return the document
   */
  @Override
  public Document findTableSchema(String datasetSchemaId, String tableSchemaId) {

    Document document = mongoDatabase.getCollection(LiteralConstants.DATASET_SCHEMA)
        .find(new Document("_id", new ObjectId(datasetSchemaId)).append("tableSchemas._id",
            new ObjectId(tableSchemaId)))
        .projection(new Document("_id", 0).append("tableSchemas.$", 1)).first();

    if (document != null) {
      Object tableSchemas = document.get(LiteralConstants.TABLE_SCHEMAS);
      if (tableSchemas != null && tableSchemas.getClass().equals(ArrayList.class)) {
        Object tableSchema =
            !((ArrayList<?>) tableSchemas).isEmpty() ? ((ArrayList<?>) tableSchemas).get(0) : null;
        if (tableSchema != null && tableSchema.getClass().equals(Document.class)) {
          return (Document) tableSchema;
        }
      }
    }

    return null;
  }

  /**
   * Find field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @return the document
   */
  @Override
  public Document findFieldSchema(String datasetSchemaId, String fieldSchemaId) {

    Object document = mongoDatabase.getCollection(LiteralConstants.DATASET_SCHEMA)
        .find(new Document("_id", new ObjectId(datasetSchemaId))
            .append("tableSchemas.recordSchema.fieldSchemas._id", new ObjectId(fieldSchemaId)))
        .projection(new Document("_id", 0).append("tableSchemas.$", 1)).first();

    // Null check, secure data type casting and secure array access by index can be avoid as the
    // query would return null if the requested structure does not match.

    if (null != document) {
      // Get TableSchemas
      document = ((Document) document).get(LiteralConstants.TABLE_SCHEMAS);

      // Get the TableSchema
      document = ((ArrayList<?>) document).get(0);

      // Get the RecordSchema
      document = ((Document) document).get("recordSchema");

      // Get FieldSchemas
      document = ((Document) document).get("fieldSchemas");

      // Get the FieldSchema
      document = ((ArrayList<?>) document).stream()
          .filter(fs -> ((Document) fs).get("_id").toString().equals(fieldSchemaId)).findFirst()
          .orElse(null);
    }

    return null != document ? (Document) document : null;
  }

  /**
   * Update dataset schema description.
   *
   * @param datasetSchemaId the dataset schema id
   * @param description the description
   * @return the update result
   */
  @Override
  public UpdateResult updateDatasetSchemaDescription(String datasetSchemaId, String description) {
    return mongoDatabase.getCollection(LiteralConstants.DATASET_SCHEMA).updateOne(
        new Document("_id", new ObjectId(datasetSchemaId)),
        new Document("$set", new Document("description", description)));
  }

  /**
   * Find record schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @return the document
   */
  @Override
  public Document findRecordSchema(String datasetSchemaId, String tableSchemaId) {

    Object document = mongoDatabase.getCollection(LiteralConstants.DATASET_SCHEMA)
        .find(new Document("_id", new ObjectId(datasetSchemaId)).append("tableSchemas._id",
            new ObjectId(tableSchemaId)))
        .projection(new Document("_id", 0).append("tableSchemas.$", 1)).first();

    // Null check, secure data type casting and secure array access by index can be avoid as the
    // query would return null if the requested structure does not match.

    if (null != document) {
      // Get TableSchemas
      document = ((Document) document).get(LiteralConstants.TABLE_SCHEMAS);

      // Get the TableSchema
      document = ((ArrayList<?>) document).get(0);

      // Get the RecordSchema
      document = ((Document) document).get("recordSchema");
    }

    return null != document ? (Document) document : null;
  }


  /**
   * Update schema document.
   *
   * @param schema the schema
   */
  @Override
  public void updateSchemaDocument(DataSetSchema schema) {
    Document document = new Document();
    mongoConverter.write(schema, document);
    mongoTemplate.getCollection(LiteralConstants.DATASET_SCHEMA).replaceOne(
        Filters.eq("_id", schema.getIdDataSetSchema()), document,
        new ReplaceOptions().upsert(true));

  }
}
