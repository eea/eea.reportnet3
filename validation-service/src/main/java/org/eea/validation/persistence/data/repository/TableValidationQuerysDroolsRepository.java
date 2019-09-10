package org.eea.validation.persistence.data.repository;

import java.math.BigInteger;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.joda.time.LocalDate;
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
  public Boolean tableValidationDR01ABQuery(String QUERY, Boolean previous) {

    Query query = entityManager.createNativeQuery(QUERY);
    List<String> value = query.getResultList();
    if (null == value || value.isEmpty()) {
      return true;
    }
    if (value.size() > 1 && Boolean.FALSE.equals(previous)) {
      return false;
    }
    if (value.size() == 1 && Boolean.TRUE.equals(previous)) {
      Integer localDateYear = new LocalDate().getYear();
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
  public Boolean tableValidationQueryNonReturnResult(String QUERY) {

    Query query = entityManager.createNativeQuery(QUERY);
    List<String> value = query.getResultList();
    if (null == value || value.isEmpty()) {
      return true;
    } else {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  public List<BigInteger> tableValidationQueryPeriodMonitoring(String QUERY) {

    Query query = entityManager.createNativeQuery(QUERY);
    List<BigInteger> value = query.getResultList();
    if (null == value || value.isEmpty()) {
      return null;
    } else {
      return value;
    }
  }

  @SuppressWarnings("unchecked")
  public String queryRecordsFields(String QUERY) {

    Query query = entityManager.createNativeQuery(QUERY);
    String value = query.getResultList().get(0).toString();
    if (null == value || value.isEmpty()) {
      return "";
    } else {
      return value;
    }
  }
}
