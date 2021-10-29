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

  /**
   * Find record schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param recordSchemaId the record schema id
   * @return the document
   */
  Document findRecordSchema(String datasetSchemaId, String recordSchemaId);

  /**
   * Find table schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @return the document
   */
  Document findTableSchema(String datasetSchemaId, String tableSchemaId);
}
