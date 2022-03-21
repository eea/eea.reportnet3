package org.eea.recordstore.persistence.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.recordstore.persistence.domain.EEAProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The Class ProcessExtendedRepositoryImpl.
 */
public class ProcessExtendedRepositoryImpl implements ProcessExtendedRepository {

  /** The Constant PROCESS_QUERY: {@value}. */
  private static final String PROCESS_QUERY =
      "select cast(json_agg(row_to_json(table_aux)) as text) as processList from (\r\n"
          + "   select\r\n" + "        process.id,\r\n"
          + "        process.dataset_id as \"datasetId\",\r\n"
          + "        process.dataflow_id as \"dataflowId\",\r\n"
          + "        process.date_start as \"processStartingDate\",\r\n"
          + "        process.date_finish as \"processFinishingDate\",\r\n"
          + "        process.queued_date as \"queuedDate\",\r\n"
          + "        process.process_id as \"processId\",\r\n"
          + "        lower(process.username) as \"user\",\r\n"
          + "        process.process_type as \"processType\",\r\n" + "        process.status,\r\n"
          + "        d.\"name\" as \"dataflowName\",\r\n"
          + "        d2.dataset_name as \"datasetName\", process.priority as \"priority\""
          + " from\r\n" + "    process\r\n" + "left join dataflow d on\r\n"
          + "    process.dataflow_id = d.id\r\n" + "left join dataset d2 on\r\n"
          + "    process.dataset_id = d2.id";

  /** The Constant COUNT_PROCESS_QUERY: {@value}. */
  private static final String COUNT_PROCESS_QUERY = "select count(*) from process";

  /** The entity manager. */
  @PersistenceContext(name = "recordStoreEntityManagerFactory")
  private EntityManager entityManager;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ProcessExtendedRepositoryImpl.class);

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
   * @throws JsonProcessingException the json processing exception
   */
  @Override
  public List<EEAProcess> getProcessesPaginated(Pageable pageable, boolean asc, String status,
      Long dataflowId, String user, ProcessTypeEnum type, String header)
      throws JsonProcessingException {
    StringBuilder stringQuery = new StringBuilder();
    List<EEAProcess> processList = new ArrayList<>();
    Query query =
        constructQuery(asc, status, dataflowId, user, type, header, stringQuery, false, pageable);

    ObjectMapper mapper = new ObjectMapper();
    try {
      String json = (String) query.getSingleResult();
      processList = json != null ? Arrays.asList(mapper.readValue(json, EEAProcess[].class))
          : new ArrayList<>();
    } catch (NoResultException e) {
      LOG.info(String.format(
          "No processes found with provided filters: status = %s, dataflowId = %s, user = %s, type = %s",
          status, dataflowId, user, type));
    }

    LOG.info(String.format(
        "Retrieved process list with provided filters: status = %s, dataflowId = %s, user = %s, type = %s",
        status, dataflowId, user, type));
    return processList;
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
    Query query =
        constructQuery(asc, status, dataflowId, user, type, header, stringQuery, true, null);

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
   * @param pageable the pageable
   * @return the query
   */
  private Query constructQuery(boolean asc, String status, Long dataflowId, String user,
      ProcessTypeEnum type, String header, StringBuilder stringQuery, boolean countQuery,
      Pageable pageable) {
    stringQuery.append(countQuery ? COUNT_PROCESS_QUERY : PROCESS_QUERY);
    addFilters(stringQuery, status, dataflowId, user);
    if (!countQuery) {
      stringQuery.append(" order by " + header);
      stringQuery.append(asc ? " asc" : " desc");
      if (null != pageable) {
        stringQuery.append(" LIMIT " + pageable.getPageSize());
        stringQuery.append(" OFFSET " + pageable.getOffset());
      }
      stringQuery.append(") table_aux");
    }
    Query query = entityManager.createNativeQuery(stringQuery.toString());

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
    query.append(StringUtils.isNotBlank(status) ? " and process.status IN :status " : "");
    query.append(dataflowId != null ? " and dataflow_id = :dataflowId " : "");
    query.append(StringUtils.isNotBlank(user) ? " and username LIKE lower(:user) " : "");

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
      query.setParameter("status", Arrays.asList(status.split(",")));
    }
    if (dataflowId != null) {
      query.setParameter("dataflowId", dataflowId);
    }
    if (StringUtils.isNotBlank(user)) {
      query.setParameter("user", "%" + user + "%");
    }
  }

  /**
   * Flush.
   */
  @Override
  @Transactional
  public void flush() {
    entityManager.flush();
  }
}
