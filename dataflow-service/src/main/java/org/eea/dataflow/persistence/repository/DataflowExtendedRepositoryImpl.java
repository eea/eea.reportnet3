package org.eea.dataflow.persistence.repository;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
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

  /** The Constant QUERY_JSON. */
  private static final String QUERY_JSON = "with doc as (    \r\n"
      + "select json_array_elements(asjsonconverter) as docaux from cast (:aux as json) asjsonconverter),\r\n"
      + "obligationtable as (select (docaux ->> 'obligationId' ) as obligation_id,\r\n"
      + "(docaux ->> 'oblTitle' ) as obligation,\r\n"
      + "(docaux ->> 'description' ) as description,\r\n"
      + "(docaux ->> 'validSince' ) as validSince,\r\n" + "(docaux ->> 'validTo' ) as validTo,\r\n"
      + "(docaux ->> 'comment' ) as comment,\r\n"
      + "(docaux ->> 'nextDeadline' ) as nextDeadline,\r\n"
      + "(docaux ->> 'legalInstrument' ) as legal_instrument,\r\n"
      + "(docaux ->> 'client' ) as client,\r\n" + "(docaux ->> 'countries' ) as countries,\r\n"
      + "(docaux ->> 'issues' ) as issues,\r\n" + "(docaux ->> 'reportFreq' ) as reportFreq,\r\n"
      + "(docaux ->> 'reportFreqDetail' ) as reportFreqDetail\r\n" + "from doc)\r\n"
      + "select d.*,ot.legal_instrument,ot.obligation from obligationtable ot inner join dataflow d \r\n"
      + "on d.obligation_id  = cast(ot.obligation_id as integer)";

  /** The Constant DATAFLOW_PUBLIC. */
  private static final String DATAFLOW_PUBLIC = " show_public_info = true";

  /** The Constant LIKE. */
  private static final String LIKE = " %s LIKE '%%%s%%'";

  /** The Constant ORDER_BY. */
  private static final String ORDER_BY = " order by %s %s";


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

  /**
   * Find completed.
   *
   * @param json the json
   * @param pageable the pageable
   * @param isPublic the is public
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @return the list
   */
  @Override
  public List<Dataflow> findPaginated(String json, Pageable pageable, boolean isPublic,
      Map<String, String> filters, String orderHeader, boolean asc) {

    StringBuilder stringQuery = new StringBuilder();
    stringQuery.append(QUERY_JSON);
    createQuery(isPublic, filters, orderHeader, asc, stringQuery);
    Query query = entityManager.createNativeQuery(stringQuery.toString(), Dataflow.class);
    query.setParameter("aux", json);
    if (null != pageable) {
      query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
      query.setMaxResults(pageable.getPageSize());
    }
    return (List<Dataflow>) query.getResultList();

  }

  /**
   * Count paginated.
   *
   * @param json the json
   * @param pageable the pageable
   * @param isPublic the is public
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @return the long
   */
  @Override
  public Long countPaginated(String json, Pageable pageable, boolean isPublic,
      Map<String, String> filters, String orderHeader, boolean asc) {
    StringBuilder stringQuery = new StringBuilder();
    stringQuery.append("with tableAux as (");
    stringQuery.append(QUERY_JSON);
    createQuery(isPublic, filters, orderHeader, asc, stringQuery);
    stringQuery.append(")select count(*) from tableaux");
    Query query = entityManager.createNativeQuery(stringQuery.toString());
    query.setParameter("aux", json);
    return Long.valueOf(query.getResultList().get(0).toString());
  }

  /**
   * Creates the query.
   *
   * @param isPublic the is public
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @param stringQuery the string query
   */
  private void createQuery(boolean isPublic, Map<String, String> filters, String orderHeader,
      boolean asc, StringBuilder stringQuery) {
    boolean addAnd = false;
    if (MapUtils.isNotEmpty(filters) || StringUtils.isNotBlank(orderHeader) || isPublic) {
      stringQuery.append(" where ");


      if (MapUtils.isNotEmpty(filters)) {
        for (String key : filters.keySet()) {
          addAnd(stringQuery, addAnd);
          stringQuery.append(String.format(LIKE, key, filters.get(key)));
          addAnd = true;
        }
      }
      if (isPublic) {
        addAnd(stringQuery, addAnd);
        stringQuery.append(DATAFLOW_PUBLIC);
      }

      if (StringUtils.isNotBlank(orderHeader)) {
        stringQuery.append(String.format(ORDER_BY, orderHeader, asc ? "asc" : "desc"));
      }

    }
  }

  /**
   * Adds the and.
   *
   * @param stringQuery the string query
   * @param addAnd the add and
   */
  private void addAnd(StringBuilder stringQuery, boolean addAnd) {
    if (addAnd) {
      stringQuery.append(" AND");
    }
  }


}
