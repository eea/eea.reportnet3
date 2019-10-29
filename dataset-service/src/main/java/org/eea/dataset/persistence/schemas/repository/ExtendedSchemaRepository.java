package org.eea.dataset.persistence.schemas.repository;

import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;

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
   * Find by id table schema.
   *
   * @param idTableSchema the id table schema
   * @return the data set schema
   */
  DataSetSchema findByIdTableSchema(String idTableSchema);
}
