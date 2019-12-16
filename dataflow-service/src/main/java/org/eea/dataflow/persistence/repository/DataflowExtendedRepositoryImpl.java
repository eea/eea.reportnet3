package org.eea.dataflow.persistence.repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.springframework.data.domain.Pageable;

/**
 * The Class DataflowExtendedRepositoryImpl.
 */
public class DataflowExtendedRepositoryImpl implements DataflowExtendedRepository {

  /** The entity manager. */
  @PersistenceContext
  private EntityManager entityManager;

  /** The Constant QUERY_FIND_COMPLETED. */
  private static final String QUERY_FIND_COMPLETED =
      "SELECT df from Dataflow df INNER JOIN df.userRequests ur WHERE ur.requestType = 'ACCEPTED' "
          + " AND ur.userRequester = :idRequester AND df.status = 'COMPLETED' ORDER BY df.deadlineDate ASC";


  /**
   * Find completed.
   *
   * @param userIdRequester the user id requester
   * @param pageable the pageable
   * @return the list
   */
  @Override
  public List<Dataflow> findCompleted(String userIdRequester, Pageable pageable) {

    Query query = entityManager.createQuery(QUERY_FIND_COMPLETED);
    query.setParameter("idRequester", userIdRequester);
    if (null != pageable) {
      query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
      query.setMaxResults(pageable.getPageSize());
    }
    return query.getResultList();

  }


}
