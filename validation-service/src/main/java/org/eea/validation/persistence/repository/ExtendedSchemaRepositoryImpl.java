package org.eea.validation.persistence.repository;

import java.util.ArrayList;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import com.mongodb.client.MongoDatabase;

/**
 * The Class ExtendedSchemaRepositoryImpl.
 */
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

}
