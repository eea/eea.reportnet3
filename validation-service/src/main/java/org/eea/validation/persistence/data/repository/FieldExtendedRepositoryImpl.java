package org.eea.validation.persistence.data.repository;

import java.math.BigInteger;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.hibernate.annotations.QueryHints;
import org.springframework.transaction.annotation.Transactional;

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
    return query.getResultList();

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



  /**
   * Query PK native field value.
   *
   * @param generatedQuery the generated query
   * @return the list
   */
  @Override
  public List<FieldValue> queryPKNativeFieldValue(String generatedQuery) {
    Query query = entityManager.createNativeQuery(generatedQuery, FieldValue.class);
    return query.setHint(QueryHints.READ_ONLY, true).getResultList();

  }

  /**
   * Query PK execution.
   *
   * @param generatedQuery the generated query
   * @return the list
   */
  @Override
  public List<Object[]> queryPKExecution(String generatedQuery) {
    Query query = entityManager.createNativeQuery(generatedQuery);
    return query.getResultList();

  }

  /**
   * Single F kscount.
   *
   * @param generatedQuery the generated query
   * @return the integer
   */
  @Override
  public Long getCount(String generatedQuery) {
    Query query = entityManager.createNativeQuery(generatedQuery);
    BigInteger result = (BigInteger) query.setHint(QueryHints.READ_ONLY, true).getSingleResult();
    return result.longValue();
  }


  /**
   * Flush.
   */
  @Transactional
  public void flush() {
    entityManager.flush();
  }

}
