package org.eea.validation.persistence.data.repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * The Class DatasetExtendedRepositoryImpl.
 */
public class DatasetExtendedRepositoryImpl implements DatasetExtendedRepository {

  /** The entity manager. */
  @PersistenceContext
  private EntityManager entityManager;


  /**
   * Find completed.
   *
   * @param generatedQuery the generated query
   * @return the list
   */
  @Override
  public List<String> queryExecution(String generatedQuery) {

    Query query = entityManager.createQuery(generatedQuery);
    return query.getResultList();

  }


}
