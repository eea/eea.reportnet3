package org.eea.validation.persistence.data.repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Repository;

@Repository
public class TableValidationQuerysDroolsRepository {


  @PersistenceContext
  private EntityManager entityManager;



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

}
