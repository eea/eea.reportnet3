package org.eea.validation.persistence.repository;

import java.util.ArrayList;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.utils.LiteralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import com.mongodb.client.MongoDatabase;

/** The Class ExtendedSchemaRepositoryImpl. */
public class ExtendedSchemaRepositoryImpl implements ExtendedSchemaRepository {

  /** The Constant TABLESCHEMAS_ID: {@value}. */
  private static final String TABLESCHEMAS_ID = "tableSchemas._id";

  /** The Constant TABLESCHEMAS: {@value}. */
  private static final String TABLESCHEMAS = "tableSchemas.$";

  /** The mongo database. */
  @Autowired
  private MongoDatabase mongoDatabase;

  /**
   * Find table schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @return the document
   */
  @Override
  public Document findTableSchema(String datasetSchemaId, String tableSchemaId) {
    Document document;
    if (null != datasetSchemaId) {
      document = mongoDatabase.getCollection(LiteralConstants.DATASET_SCHEMA)
          .find(new Document("_id", new ObjectId(datasetSchemaId)).append(TABLESCHEMAS_ID,
              new ObjectId(tableSchemaId)))
          .projection(new Document("_id", 0).append(TABLESCHEMAS, 1)).first();
    } else {
      document = mongoDatabase.getCollection(LiteralConstants.DATASET_SCHEMA)
          .find(new Document(TABLESCHEMAS_ID, new ObjectId(tableSchemaId)))
          .projection(new Document("_id", 0).append(TABLESCHEMAS, 1)).first();
    }
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

    Document document = mongoDatabase.getCollection("DataSetSchema")
        .find(new Document("_id", new ObjectId(datasetSchemaId))
            .append("tableSchemas.recordSchema.fieldSchemas._id", new ObjectId(fieldSchemaId)))
        .projection(new Document("_id", 0).append(TABLESCHEMAS, 1)).first();
    if (document != null) {
      Object tableSchemas = document.get("tableSchemas");
      if (tableSchemas != null && tableSchemas.getClass().equals(ArrayList.class)) {
        Object tableSchema = ((ArrayList<?>) tableSchemas).get(0);
        if (tableSchema != null && tableSchema.getClass().equals(Document.class)) {
          Object recordSchema = ((Document) tableSchema).get("recordSchema");
          if (recordSchema != null && recordSchema.getClass().equals(Document.class)) {
            return filterFields(fieldSchemaId, ((Document) recordSchema).get("fieldSchemas"));
          }
        }
      }
    }

    return null;
  }

  private Document filterFields(String fieldSchemaId, Object fieldSchemas) {
    Document fields = null;
    if (fieldSchemas != null && fieldSchemas.getClass().equals(ArrayList.class)) {
      fields = (Document) ((ArrayList<?>) fieldSchemas).stream()
          .filter(
              fieldSchema -> ((Document) fieldSchema).get("_id").toString().equals(fieldSchemaId))
          .findFirst().orElse(null);
    }
    return fields;
  }

  @Override
  public Document findRecordSchema(String datasetSchemaId, String recordSchemaId) {

    Object document = mongoDatabase.getCollection(LiteralConstants.DATASET_SCHEMA)
        .find(new Document("_id", new ObjectId(datasetSchemaId))
            .append("tableSchemas.recordSchema._id", new ObjectId(recordSchemaId)))
        .projection(new Document("_id", 0).append(TABLESCHEMAS, 1)).first();

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
}
