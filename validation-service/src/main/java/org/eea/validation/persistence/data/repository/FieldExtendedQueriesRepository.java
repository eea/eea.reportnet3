package org.eea.validation.persistence.data.repository;

import org.springframework.stereotype.Repository;


/**
 * The Interface RecordRepository.
 */
@Repository
public interface FieldExtendedQueriesRepository {



  /**
   * Find all field values by field schem and name data set.
   *
   * @param idFieldSchemaReference the id field schema reference
   * @param idDatasetReference the id dataset reference
   * @param recordCoordinateReference the record coordinate reference
   * @param columnCoordinateReference the column coordinate reference
   * @return the string
   */
  String findAllFieldValuesByFieldSchemAndNameDataSet(String idFieldSchemaReference,
      Long idDatasetReference, Long recordCoordinateReference, Long columnCoordinateReference);

}
