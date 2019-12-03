package org.eea.dataset.persistence.schemas.repository;

import org.bson.Document;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.exception.EEAException;
import com.mongodb.client.result.UpdateResult;

/**
 * The Interface ExtendedSchemaRepository.
 */
public interface ExtendedSchemaRepository {
  /**
   * Delete table schema by id.
   *
   * @param idTableSchema the id table schema
   */
  void deleteTableSchemaById(String idTableSchema);

  /**
   * Delete dataset schema by id.
   *
   * @param idSchema the id schema
   */
  void deleteDatasetSchemaById(String idSchema);

  /**
   * Insert table schema.
   *
   * @param table the table
   * @param idDatasetSchema the id dataset schema
   */
  void insertTableSchema(TableSchema table, String idDatasetSchema);

  /**
   * Delete field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @return the update result
   * @throws EEAException the EEA exception
   */
  UpdateResult deleteFieldSchema(String datasetSchemaId, String fieldSchemaId) throws EEAException;

  /**
   * Update field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchema the field schema
   * @return the update result
   * @throws EEAException the EEA exception
   */
  UpdateResult updateFieldSchema(String datasetSchemaId, FieldSchema fieldSchema)
      throws EEAException;

  /**
   * Creates the field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchema the field schema
   * @return the update result
   * @throws EEAException the EEA exception
   */
  UpdateResult createFieldSchema(String datasetSchemaId, FieldSchema fieldSchema)
      throws EEAException;

  /**
   * Update table schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchema the table schema
   * @return the update result
   * @throws EEAException the EEA exception
   */
  UpdateResult updateTableSchema(String datasetSchemaId, TableSchema tableSchema)
      throws EEAException;

  /**
   * Insert field in position.
   *
   * @param idDatasetSchema the id dataset schema
   * @param fieldSchema the field schema
   * @param position the position
   * @return the update result
   * @throws EEAException the EEA exception
   */
  UpdateResult insertFieldInPosition(String idDatasetSchema, Document fieldSchema, int position)
      throws EEAException;

  /**
   * Insert table in position.
   *
   * @param idDatasetSchema the id dataset schema
   * @param tableSchema the table schema
   * @param position the position
   * @return the update result
   * @throws EEAException the EEA exception
   */
  UpdateResult insertTableInPosition(String idDatasetSchema, Document tableSchema, int position)
      throws EEAException;

  /**
   * Find table schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @return the document
   */
  Document findTableSchema(String datasetSchemaId, String tableSchemaId);


  /**
   * Find field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @return the document
   */
  Document findFieldSchema(String datasetSchemaId, String fieldSchemaId);
}
