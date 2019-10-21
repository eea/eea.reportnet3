package org.eea.dataset.persistence.schemas.repository;

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
   * @param schemaId the schema id
   */
  void deleteDatasetSchemaById(String schemaId);
}
