package org.eea.validation.persistence.data.repository;

/**
 * The Interface RecordRepository.
 */
public interface TableRepositoryExtended {

  /**
   * Find table value by field schema id. With that method we find the tableschema by a
   * idFieldSchema
   * 
   * @param datasetId the dataset id
   * @param idFieldSchema the id field schema
   * @return the string
   */
  String findTableValueByFieldSchemaId(Long datasetId, String idFieldSchema);


}
