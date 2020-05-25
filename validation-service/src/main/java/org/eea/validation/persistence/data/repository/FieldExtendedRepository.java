package org.eea.validation.persistence.data.repository;

import java.util.List;
import org.eea.validation.persistence.data.domain.RecordValue;

/**
 * The Interface DatasetExtendedRepository.
 */
public interface FieldExtendedRepository {



  /**
   * Query execution.
   *
   * @param generatedQuery the generated query
   * @param pageable the pageable
   * @return the list
   */
  List<String> queryExecution(String generatedQuery);


  /**
   * Gets the duplicated records by fields.
   *
   * @param fieldSchemaIds the field schema ids
   * @return the duplicated records by fields
   */
  List<RecordValue> getDuplicatedRecordsByFields(List<String> fieldSchemaIds);


}
