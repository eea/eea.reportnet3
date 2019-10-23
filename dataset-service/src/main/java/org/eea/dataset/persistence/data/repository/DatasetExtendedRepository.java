package org.eea.dataset.persistence.data.repository;

public interface DatasetExtendedRepository {

  /**
   * Delete schema.
   *
   * @param schemaName the schema name
   */
  void deleteSchema(String schema);
}
