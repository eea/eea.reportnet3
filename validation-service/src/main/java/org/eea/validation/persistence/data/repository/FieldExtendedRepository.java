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
   * @return the list
   */
  List<String> queryExecution(String generatedQuery);

  /**
   * Query execution record.
   *
   * @param query the query
   * @return the list
   */
  List<RecordValue> queryExecutionRecord(String query);

  /**
   * Query PK execution.
   *
   * @param generatedQuery the generated query
   * @return the list
   */
  List<Object[]> queryPKExecution(String generatedQuery);

}
