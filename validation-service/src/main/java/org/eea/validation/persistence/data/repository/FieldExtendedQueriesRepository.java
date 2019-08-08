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
   * @param fieldSchema the field schema
   * @param nameDataset the name dataset
   * @return the list
   */
  Integer findAllFieldValuesByFieldSchemAndNameDataSet(String value, String fieldSchema,
      Long idDataset);

}
