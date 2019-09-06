package org.eea.validation.persistence.data.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface DatasetExtendedQueriesRepository {

  public Boolean datasetValidationQuery(String QUERY);

}
