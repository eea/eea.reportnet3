package org.eea.validation.persistence.data.repository;

import java.math.BigInteger;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.eea.thread.ThreadPropertiesManager;
import org.springframework.stereotype.Repository;

/**
 * The Class TableValidationQuerysDroolsRepository.
 */
@Repository
public class TableValidationQuerysDroolsRepository {


  /** The entity manager. */
  @PersistenceContext
  private EntityManager entityManager;



  /**
   * Table validation DR 01 AB query.
   *
   * @param QUERY the query
   * @param previous the previous
   * @return the boolean
   */
  @SuppressWarnings("unchecked")
  public Boolean tableValidationDR01ABQuery(String queryValidate, Boolean previous) {

    Query query = entityManager.createNativeQuery(queryValidate);
    List<String> value = query.getResultList();
    if (null == value || value.isEmpty()) {
      return true;
    }
    if (value.size() > 1 && Boolean.FALSE.equals(previous)) {
      return false;
    }
    if (value.size() == 1 && Boolean.TRUE.equals(previous)) {
      Integer localDateYear =
          Integer.valueOf(ThreadPropertiesManager.getVariable("dataCallYear").toString());
      Integer yearSession;
      try {
        yearSession = Integer.valueOf(value.get(0));
      } catch (NumberFormatException e) {
        return true;
      }
      if (yearSession < localDateYear) {
        return false;
      }
    }
    return true;
  }

  /**
   * Table validation query non return result.
   *
   * @param QUERY the query
   * @return the boolean
   */
  @SuppressWarnings("unchecked")
  public Boolean tableValidationQueryNonReturnResult(String queryValidate) {

    Query query = entityManager.createNativeQuery(queryValidate);
    List<String> value = query.getResultList();
    if (null == value || value.isEmpty()) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Table validation query period monitoring.
   *
   * @param QUERY the query
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<BigInteger> tableValidationQueryReturnListIds(String queryValidate) {

    Query query = entityManager.createNativeQuery(queryValidate);
    List<BigInteger> value = query.getResultList();
    if (null == value || value.isEmpty()) {
      return null;
    } else {
      return value;
    }
  }


  /**
   * Table validation query non return result.
   *
   * @param QUERY the query
   * @return the boolean
   */
  @SuppressWarnings("unchecked")
  public Boolean tableValidationQueryReturnResult(String queryRecieve) {

    Query query = entityManager.createNativeQuery(queryRecieve);
    List<BigInteger> value = query.getResultList();
    if (null == value || value.isEmpty() || value.get(0).longValue() == 0L) {
      return false;
    } else {
      return true;
    }
  }
}
