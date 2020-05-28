package org.eea.validation.persistence.data.repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.eea.validation.persistence.data.domain.RecordValue;

/**
 * The Class FieldExtendedRepositoryImpl.
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

  /**
   * Query execution record.
   *
   * @param queryString the query string
   * @return the list
   */
  @Override
  public List<RecordValue> queryExecutionRecord(String queryString) {
    Query query = entityManager.createNativeQuery(queryString, RecordValue.class);
    return query.getResultList();
  }

}
