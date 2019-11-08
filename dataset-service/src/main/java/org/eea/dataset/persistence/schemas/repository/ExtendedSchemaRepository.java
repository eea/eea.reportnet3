package org.eea.dataset.persistence.schemas.repository;

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
   * @param tableSchemaId the table schema id
   * @param fieldSchema the field schema
   * @return the update result
   * @throws EEAException the EEA exception
   */
  UpdateResult createFieldSchema(String datasetSchemaId, String tableSchemaId,
      FieldSchema fieldSchema) throws EEAException;
}
