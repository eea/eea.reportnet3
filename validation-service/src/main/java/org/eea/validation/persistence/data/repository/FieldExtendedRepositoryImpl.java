package org.eea.validation.persistence.data.repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * The Class DatasetExtendedRepositoryImpl.
 */
public class FieldExtendedRepositoryImpl implements FieldExtendedRepository {

  /** The entity manager. */
  @PersistenceContext(name = "dataSetsEntityManagerFactory")
  private EntityManager entityManager;


  /**
   * Find completed.
   *
   * @param generatedQuery the generated query
   * @return the list
   */
  @Override
  public List<String> queryExecution(String generatedQuery) {

    Query query = entityManager.createNativeQuery(generatedQuery);
    List<String> resultList = query.getResultList();
    return resultList;

  }


}
