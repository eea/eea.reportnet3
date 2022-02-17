package org.eea.recordstore.persistence.repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.recordstore.persistence.domain.Process;
import org.springframework.data.domain.Pageable;


/**
 * The Class ProcessExtendedRepositoryImpl.
 */
public class ProcessExtendedRepositoryImpl implements ProcessExtendedRepository {

  /** The Constant PROCESS_QUERY: {@value}. */
  private static final String PROCESS_QUERY = "select * from process";

  /** The Constant COUNT_PROCESS_QUERY: {@value}. */
  private static final String COUNT_PROCESS_QUERY = "select count(*) from process";

  /** The entity manager. */
  @PersistenceContext(name = "recordStoreEntityManagerFactory")
  private EntityManager entityManager;

  /**
   * Gets the processes paginated.
   *
   * @param pageable the pageable
   * @param asc the asc
   * @param status the status
   * @param dataflowId the dataflow id
   * @param user the user
   * @param type the type
   * @param header the header
   * @return the processes paginated
   */
  @Override
  public List<Process> getProcessesPaginated(Pageable pageable, boolean asc, String status,
      Long dataflowId, String user, ProcessTypeEnum type, String header) {
    StringBuilder stringQuery = new StringBuilder();
    Query query = constructQuery(asc, status, dataflowId, user, type, header, stringQuery, false);

    if (null != pageable) {
      query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
      query.setMaxResults(pageable.getPageSize());
    }
    return query.getResultList();
  }

  /**
   * Count processes paginated.
   *
   * @param asc the asc
   * @param status the status
   * @param dataflowId the dataflow id
   * @param user the user
   * @param type the type
   * @param header the header
   * @return the long
   */
  @Override
  public Long countProcessesPaginated(boolean asc, String status, Long dataflowId, String user,
      ProcessTypeEnum type, String header) {
    StringBuilder stringQuery = new StringBuilder();
    Query query = constructQuery(asc, status, dataflowId, user, type, header, stringQuery, true);

    return Long.valueOf(query.getResultList().get(0).toString());
  }

  /**
   * Construct query.
   *
   * @param asc the asc
   * @param status the status
   * @param dataflowId the dataflow id
   * @param user the user
   * @param type the type
   * @param header the header
   * @param stringQuery the string query
   * @param countQuery the count query
   * @return the query
   */
  private Query constructQuery(boolean asc, String status, Long dataflowId, String user,
      ProcessTypeEnum type, String header, StringBuilder stringQuery, boolean countQuery) {
    stringQuery.append(countQuery ? COUNT_PROCESS_QUERY : PROCESS_QUERY);
    addFilters(stringQuery, status, dataflowId, user);
    if (!countQuery) {
      stringQuery.append(" order by " + header);
      stringQuery.append(asc ? " asc" : " desc");
    }
    Query query = countQuery ? entityManager.createNativeQuery(stringQuery.toString())
        : entityManager.createNativeQuery(stringQuery.toString(), Process.class);

    addParameters(query, status, dataflowId, user, type);
    return query;
  }

  /**
   * Adds the filters.
   *
   * @param query the query
   * @param status the status
   * @param dataflowId the dataflow id
   * @param user the user
   */
  private void addFilters(StringBuilder query, String status, Long dataflowId, String user) {
    query.append(" where process_type = :process_type ");
    query.append(StringUtils.isNotBlank(status) ? " and status = :status " : "");
    query.append(dataflowId != null ? " and dataflow_id = :dataflowId " : "");
    query.append(StringUtils.isNotBlank(user) ? " and username = :user " : "");

  }

  /**
   * Adds the parameters.
   *
   * @param query the query
   * @param status the status
   * @param dataflowId the dataflow id
   * @param user the user
   * @param type the type
   */
  private void addParameters(Query query, String status, Long dataflowId, String user,
      ProcessTypeEnum type) {
    query.setParameter("process_type", type.toString());

    if (StringUtils.isNotBlank(status)) {
      query.setParameter("status", status);
    }
    if (dataflowId != null) {
      query.setParameter("dataflowId", dataflowId);
    }
    if (StringUtils.isNotBlank(user)) {
      query.setParameter("user", user);
    }
  }
}
