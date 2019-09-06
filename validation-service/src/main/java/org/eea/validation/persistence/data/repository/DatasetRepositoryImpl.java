package org.eea.validation.persistence.data.repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class DatasetRepositoryImpl implements DatasetExtendedQueriesRepository {

  @PersistenceContext
  private EntityManager entityManager;


  @SuppressWarnings("unchecked")
  @Override
  public Boolean datasetValidationQuery(String QUERY) {

    Query query = entityManager.createNativeQuery(QUERY);
    List<String> value = query.getResultList();
    if (null != value && !value.isEmpty()) {
      return false;
    } else {
      return true;
    }

  }
}
