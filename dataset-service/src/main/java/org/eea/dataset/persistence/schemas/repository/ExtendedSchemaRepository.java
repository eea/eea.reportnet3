package org.eea.dataset.persistence.schemas.repository;

import org.eea.dataset.persistence.schemas.domain.TableSchema;
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
   */
  UpdateResult deleteFieldSchema(String datasetSchemaId, String fieldSchemaId);
}
