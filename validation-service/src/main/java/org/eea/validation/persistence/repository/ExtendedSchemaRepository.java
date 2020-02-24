package org.eea.validation.persistence.repository;

import org.bson.Document;

/**
 * The Interface ExtendedSchemaRepository.
 */
public interface ExtendedSchemaRepository {

  /**
   * Find field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @return the document
   */
  Document findFieldSchema(String datasetSchemaId, String fieldSchemaId);
}
