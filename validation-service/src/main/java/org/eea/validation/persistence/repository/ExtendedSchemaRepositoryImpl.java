package org.eea.validation.persistence.repository;

import java.util.ArrayList;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.utils.LiteralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import com.mongodb.client.MongoDatabase;

/** The Class ExtendedSchemaRepositoryImpl. */
public class ExtendedSchemaRepositoryImpl implements ExtendedSchemaRepository {

  /** The mongo database. */
  @Autowired
  private MongoDatabase mongoDatabase;

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
        .projection(new Document("_id", 0).append("tableSchemas.$", 1)).first();

    if (document != null) {
      Object tableSchemas = document.get("tableSchemas");
      if (tableSchemas != null && tableSchemas.getClass().equals(ArrayList.class)) {
        Object tableSchema = ((ArrayList<?>) tableSchemas).get(0);
        if (tableSchema != null && tableSchema.getClass().equals(Document.class)) {
          Object recordSchema = ((Document) tableSchema).get("recordSchema");
          if (recordSchema != null && recordSchema.getClass().equals(Document.class)) {
            Object fieldSchemas = ((Document) recordSchema).get("fieldSchemas");
            if (fieldSchemas != null && fieldSchemas.getClass().equals(ArrayList.class)) {
              return (Document) ((ArrayList<?>) fieldSchemas).stream()
                  .filter(fieldSchema -> ((Document) fieldSchema).get("_id").toString()
                      .equals(fieldSchemaId))
                  .findFirst().orElse(null);
            }
          }
        }
      }
    }

    return null;
  }

  @Override
  public Document findRecordSchema(String datasetSchemaId, String recordSchemaId) {

    Object document = mongoDatabase.getCollection(LiteralConstants.DATASET_SCHEMA)
        .find(new Document("_id", new ObjectId(datasetSchemaId))
            .append("tableSchemas.recordSchema._id", new ObjectId(recordSchemaId)))
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
}
