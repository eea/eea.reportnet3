package org.eea.validation.persistence.data.repository;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.springframework.data.domain.Pageable;


/**
 * The Interface RecordRepository.
 */
public class RecordRepositoryPaginatedImpl implements RecordRepositoryPaginated {

  /** The Constant QUERY_UNSORTERED: {@value}. */
  private static final String QUERY_UNSORTERED =
      "SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv order by rv.dataPosition";
  /**
   * The entity manager.
   */
  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Find all records by table value.
   *
   * @param tableId the table id
   * @param pageable the pageable
   * @return the list
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<RecordValue> findAllRecordsByTableValueIdPaginated(Long tableId, Pageable pageable) {
    Query query = entityManager
        .createQuery("SELECT rv.id from RecordValue rv WHERE rv.tableValue.id = :tableId");

    query.setParameter("tableId", tableId);
    if (null != pageable) {
      query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
      query.setMaxResults(pageable.getPageSize());
    }
    List<Long> ids = query.getResultList();
    if (ids == null || ids.isEmpty()) {
      return new ArrayList<>();
    }
    Query query2 = entityManager.createQuery(
        "SELECT rv from RecordValue rv INNER JOIN FETCH rv.fields WHERE rv.id in :ids");
    query2.setParameter("ids", ids);
    return query2.getResultList();
  }

  /**
   * Find all records.
   *
   * @param pageable the pageable
   * @return the list
   */
  public List<RecordValue> findRecordsPageable(Pageable pageable) {
    Query query = entityManager.createQuery(QUERY_UNSORTERED);
    if (null != pageable) {
      query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
      query.setMaxResults(pageable.getPageSize());
    }
    return query.getResultList();
  }

}
