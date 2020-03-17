package org.eea.validation.persistence.data.repository;

import java.util.List;

/**
 * The Interface DatasetExtendedRepository.
 */
public interface DatasetExtendedRepository {



  /**
   * Query execution.
   *
   * @param generatedQuery the generated query
   * @param pageable the pageable
   * @return the list
   */
  List<String> queryExecution(String generatedQuery);

}
