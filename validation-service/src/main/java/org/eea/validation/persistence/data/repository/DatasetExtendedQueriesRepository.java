package org.eea.validation.persistence.data.repository;

import org.springframework.stereotype.Repository;

/**
 * The Interface DatasetExtendedQueriesRepository.
 */
@Repository
public interface DatasetExtendedQueriesRepository {


  /**
   * Dataset validation query.
   *
   * @param QUERY the query
   * @return the boolean
   */
  Boolean datasetValidationQuery(String QUERY);


}
