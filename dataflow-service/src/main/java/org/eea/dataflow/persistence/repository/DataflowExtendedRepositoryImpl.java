package org.eea.dataflow.persistence.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;


/**
 * The Class DataflowExtendedRepositoryImpl.
 */
public class DataflowExtendedRepositoryImpl implements DataflowExtendedRepository {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant DATE_RELEASED. */
  private static final String DATE_RELEASED = "date_released";

  /** The Constant DELIVERY_STATUS. */
  private static final String DELIVERY_STATUS = "delivery_status";

  /** The Constant COUNTRY_CODE. */
  private static final String COUNTRY_CODE = "countryCode";

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
      + "cast((docaux ->> 'legalInstrument' )as json)->>'sourceTitle' as legal_instrument,\r\n"
      + "(docaux ->> 'client' ) as client,\r\n" + "(docaux ->> 'countries' ) as countries,\r\n"
      + "(docaux ->> 'issues' ) as issues,\r\n" + "(docaux ->> 'reportFreq' ) as reportFreq,\r\n"
      + "(docaux ->> 'reportFreqDetail' ) as reportFreqDetail\r\n" + "from doc)\r\n"
      + "select d.*,ot.legal_instrument,ot.obligation from obligationtable ot right join dataflow d \r\n"
      + "on d.obligation_id  = cast(ot.obligation_id as integer)";

  /** The Constant QUERY_JSON_COUNTRY. */
  private static final String QUERY_JSON_COUNTRY = "with doc as (    \r\n"
      + "select json_array_elements(asjsonconverter) as docaux from cast (:aux as json) asjsonconverter),\r\n"
      + "obligationtable as (select (docaux ->> 'obligationId' ) as obligationId,\r\n"
      + "(docaux ->> 'oblTitle' ) as obligation,\r\n"
      + "(docaux ->> 'description' ) as description,\r\n"
      + "(docaux ->> 'validSince' ) as validSince,\r\n" + "(docaux ->> 'validTo' ) as validTo,\r\n"
      + "(docaux ->> 'comment' ) as comment,\r\n"
      + "(docaux ->> 'nextDeadline' ) as nextDeadline,\r\n"
      + "cast((docaux ->> 'legalInstrument' )as json)->>'sourceTitle' as legal_Instrument,\r\n"
      + "(docaux ->> 'client' ) as client,\r\n" + "(docaux ->> 'countries' ) as countries,\r\n"
      + "(docaux ->> 'issues' ) as issues,\r\n" + "(docaux ->> 'reportFreq' ) as reportFreq,\r\n"
      + "(docaux ->> 'reportFreqDetail' ) as reportFreqDetail\r\n" + "from doc),\r\n"
      + "dataset_aux as (select dataflowid, MAX(status) as delivery_status, MAX(date_released) as date_released from dataset d2 right join (select reporting_dataset_id, date_released from \"snapshot\" s2 group by reporting_dataset_id, date_released) as snapshot_aux on snapshot_aux.reporting_dataset_id = d2.id group by dataflowid)"
      + "select d.*,ot.legal_Instrument, ot.obligation, dataset_aux.delivery_status, dataset_aux.date_released from obligationtable ot RIGHT join dataflow d \r\n"
      + "on d.obligation_id  = cast(ot.obligationId as integer)";

  /** The Constant COUNTRY_CODE. */
  private static final String COUNTRY_CODE_CONDITION = " dp.code = :countryCode ";

  /** The Constant HAS_DATASETS. */
  private static final String HAS_DATASETS = " r.has_datasets = TRUE ";

  /** The Constant JOIN_REPRESENTATIVE_DATA_PROVIDER_AND_DATASET_AUX. */
  private static final String JOIN_REPRESENTATIVE_DATA_PROVIDER_AND_DATASET_AUX =
      " inner join representative r on d.id = r.dataflow_id "
          + "inner join data_provider dp on r.data_provider_id = dp.id "
          + "left join dataset_aux on d.id = dataset_aux.dataflowid ";

  /** The Constant AND. */
  private static final String AND = " and ";

  /** The Constant DATAFLOW_PUBLIC. */
  private static final String DATAFLOW_PUBLIC = " show_public_info = :public ";

  /** The Constant LIKE. */
  private static final String LIKE = " lower(%s) LIKE lower(:%s) ";

  /** The Constant DATE_FROM. */
  private static final String DATE_FROM = " %s >= :%s ";

  /** The Constant DATE_TO. */
  private static final String DATE_TO = " %s <= :%s ";

  /** The Constant ORDER_BY. */
  private static final String ORDER_BY = " order by %s %s";

  /** The Constant DATAFLOW_TYPE. */
  private static final String DATAFLOW_TYPE = " type = :dataflowType ";

  /** The Constant DATAFLOW_IN. */
  private static final String DATAFLOW_IN = " d.id IN :dataflowList ";

  /** The Constant REFERENCE_IN. */
  private static final String REFERENCE_IN =
      " (status = :status1 or d.id IN :dataflowList and status = :status2) ";

  /** The Constant REFERENCE_STATUS. */
  private static final String REFERENCE_STATUS = " (status = :status1 or status = :status2) ";

  /** The Constant DELIVERY_STATUS_IN. */
  private static final String DELIVERY_STATUS_IN = " %s IN :%s ";

  /** The Constant RELEASABLE. */
  private static final String RELEASABLE = " releasable = :releasable";

  /** The Constant STATUS. */
  private static final String STATUS = " status = :status";


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
   * @param type the type
   * @param dataflowIds the dataflow ids
   * @return the list
   * @throws EEAException
   */
  @Override
  public List<Dataflow> findPaginated(String json, Pageable pageable, boolean isPublic,
      Map<String, String> filters, String orderHeader, boolean asc, TypeDataflowEnum type,
      List<Long> dataflowIds) throws EEAException {
    List<Dataflow> result = new ArrayList<>();
    try {
      StringBuilder stringQuery = new StringBuilder();
      stringQuery.append(QUERY_JSON);
      createQuery(isPublic, filters, orderHeader, asc, stringQuery, type, dataflowIds);
      Query query = entityManager.createNativeQuery(stringQuery.toString(), Dataflow.class);
      setParameters(json, isPublic, filters, query, type, dataflowIds);

      if (null != pageable) {
        query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
        query.setMaxResults(pageable.getPageSize());
      }
      result = query.getResultList();
    } catch (Exception e) {
      LOG_ERROR.error(e.getMessage());
    }

    return result;
  }

  /**
   * Find paginated by country.
   *
   * @param obligationJson the obligation json
   * @param pageable the pageable
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @param countryCode the country code
   * @return the list
   * @throws EEAException
   */
  @Override
  public List<Dataflow> findPaginatedByCountry(String obligationJson, Pageable pageable,
      Map<String, String> filters, String orderHeader, boolean asc, String countryCode)
      throws EEAException {

    StringBuilder sb = new StringBuilder();
    constructPublicDataflowsQuery(sb, orderHeader, asc, filters, true);
    Query query = entityManager.createNativeQuery(sb.toString(), Dataflow.class);

    setParameters(obligationJson, true, filters, query, null, null);

    query.setParameter(COUNTRY_CODE, countryCode);

    if (null != pageable) {
      query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
      query.setMaxResults(pageable.getPageSize());

    }
    return query.getResultList();
  }

  /**
   * Count paginated by country.
   *
   * @param obligationJson the obligation json
   * @param pageable the pageable
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @param countryCode the country code
   * @return the long
   * @throws EEAException
   */
  @Override
  public Long countByCountry(String obligationJson, Map<String, String> filters, String orderHeader,
      boolean asc, String countryCode) throws EEAException {

    StringBuilder sb = new StringBuilder();
    sb.append(" with tableAux as (");
    constructPublicDataflowsQuery(sb, orderHeader, asc, filters, false);
    sb.append(") select count(*) from tableAux");

    Query query = entityManager.createNativeQuery(sb.toString());

    query.setParameter("aux", obligationJson);
    query.setParameter(COUNTRY_CODE, countryCode);
    query.setParameter("public", Boolean.TRUE);

    return Long.valueOf(query.getResultList().get(0).toString());
  }

  /**
   * Count by country filtered.
   *
   * @param obligationJson the obligation json
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @param countryCode the country code
   * @return the long
   * @throws EEAException
   */
  @Override
  public Long countByCountryFiltered(String obligationJson, Map<String, String> filters,
      String orderHeader, boolean asc, String countryCode) throws EEAException {

    StringBuilder sb = new StringBuilder();
    sb.append(" with tableAux as (");
    constructPublicDataflowsQuery(sb, orderHeader, asc, filters, true);

    sb.append(") select count(*) from tableAux");

    Query query = entityManager.createNativeQuery(sb.toString());

    setParameters(obligationJson, true, filters, query, null, null);

    query.setParameter(COUNTRY_CODE, countryCode);

    return Long.valueOf(query.getResultList().get(0).toString());
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
   * @param type the type
   * @param dataflowIds the dataflow ids
   * @return the long
   * @throws EEAException
   */
  @Override
  public Long countPaginated(String json, Pageable pageable, boolean isPublic,
      Map<String, String> filters, String orderHeader, boolean asc, TypeDataflowEnum type,
      List<Long> dataflowIds) throws EEAException {
    Long result = Long.valueOf(0);
    try {
      StringBuilder stringQuery = new StringBuilder();
      stringQuery.append("with tableAux as (");
      stringQuery.append(QUERY_JSON);
      createQuery(isPublic, filters, orderHeader, asc, stringQuery, type, dataflowIds);
      stringQuery.append(")select count(*) from tableaux");
      Query query = entityManager.createNativeQuery(stringQuery.toString());
      setParameters(json, isPublic, filters, query, type, dataflowIds);
      result = Long.valueOf(query.getResultList().get(0).toString());
    } catch (Exception e) {
      LOG_ERROR.error(e.getMessage());
    }

    return result;
  }

  /**
   * Sets the parameters.
   *
   * @param json the json
   * @param isPublic the is public
   * @param filters the filters
   * @param query the query
   * @param type the type
   * @param dataflowIds the dataflow ids
   */
  private void setParameters(String json, boolean isPublic, Map<String, String> filters,
      Query query, TypeDataflowEnum type, List<Long> dataflowIds) {
    query.setParameter("aux", json);
    if (MapUtils.isNotEmpty(filters)) {
      for (String key : filters.keySet()) {
        setParametersFilters(query, key, filters.get(key));
      }
    }
    if (isPublic) {
      query.setParameter("public", isPublic);
    }

    if (null != type) {
      query.setParameter("dataflowType", type.toString());
    }

    if (CollectionUtils.isNotEmpty(dataflowIds)) {
      query.setParameter("dataflowList", dataflowIds);
      if (null != type && TypeDataflowEnum.REFERENCE.equals(type)) {
        query.setParameter("status1", "DRAFT");
        query.setParameter("status2", "DESIGN");
      }

    } else {
      if (null != type && TypeDataflowEnum.REFERENCE.equals(type)) {
        query.setParameter("status1", "DRAFT");
        query.setParameter("status2", "DESIGN");
      }
    }


  }

  /**
   * Creates the query.
   *
   * @param isPublic the is public
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @param stringQuery the string query
   * @param type the type
   * @param dataflowIds the dataflow ids
   * @throws EEAException
   */
  private void createQuery(boolean isPublic, Map<String, String> filters, String orderHeader,
      boolean asc, StringBuilder stringQuery, TypeDataflowEnum type, List<Long> dataflowIds)
      throws EEAException {
    boolean addAnd = false;
    if (MapUtils.isNotEmpty(filters) || StringUtils.isNotBlank(orderHeader) || isPublic
        || null != type || CollectionUtils.isNotEmpty(dataflowIds)) {
      stringQuery.append(" where ");

      if (MapUtils.isNotEmpty(filters)) {

        for (String key : filters.keySet()) {
          addAnd(stringQuery, addAnd);
          setFilters(stringQuery, key, filters.get(key));
          addAnd = true;
        }
      }

      if (null != type) {
        addAnd(stringQuery, addAnd);
        stringQuery.append(DATAFLOW_TYPE);
        addAnd = true;
      }


      if (CollectionUtils.isNotEmpty(dataflowIds)) {
        if (null != type && TypeDataflowEnum.REFERENCE.equals(type)) {
          addAnd(stringQuery, addAnd);
          stringQuery.append(REFERENCE_IN);
          addAnd = true;
        } else {
          addAnd(stringQuery, addAnd);
          stringQuery.append(DATAFLOW_IN);
          addAnd = true;
        }
      } else {
        if (null != type && TypeDataflowEnum.REFERENCE.equals(type)) {
          addAnd(stringQuery, addAnd);
          stringQuery.append(REFERENCE_STATUS);
          addAnd = true;
        }
      }

      if (isPublic) {
        addAnd(stringQuery, addAnd);
        stringQuery.append(DATAFLOW_PUBLIC);
      }


      if (StringUtils.isNotBlank(orderHeader)) {
        if ("status".equals(orderHeader)) {
          stringQuery.append(
              String.format(ORDER_BY, orderHeader + (asc ? " asc" : " desc"), " ,releasable "));
        } else {
          stringQuery.append(String.format(ORDER_BY, orderHeader, asc ? "asc" : "desc"));
        }
      } else {
        stringQuery.append("order by status, creation_date desc");
      }

    }
  }

  /**
   * Sets the filters.
   *
   * @param stringQuery the string query
   * @param key the key
   * @param value the value
   * @throws EEAException
   */
  private void setFilters(StringBuilder stringQuery, String key, String value) throws EEAException {
    switch (key) {
      case "creation_date_from":
        stringQuery.append(String.format(DATE_FROM, "creation_date", key));
        break;
      case "creation_date_to":
        stringQuery.append(String.format(DATE_TO, "creation_date", key));
        break;
      case "deadline_date_from":
        stringQuery.append(String.format(DATE_FROM, "deadline_date", key));
        break;
      case "deadline_date_to":
        stringQuery.append(String.format(DATE_TO, "deadline_date", key));
        break;
      case "delivery_date_from":
        stringQuery.append(String.format(DATE_FROM, DATE_RELEASED, key));
        break;
      case "delivery_date_to":
        stringQuery.append(String.format(DATE_TO, DATE_RELEASED, key));
        break;
      case DELIVERY_STATUS:
        List<String> deliveryStatus = Arrays.asList(value.split(","));
        stringQuery.append("(");
        if (deliveryStatus.contains("PENDING")) {
          stringQuery.append("(delivery_status is null) or ");
        }
        stringQuery.append(String.format(DELIVERY_STATUS_IN, DELIVERY_STATUS, key + ")"));
        break;
      case "status":
        switch (value) {
          case "OPEN":
          case "CLOSED":
            stringQuery.append(STATUS).append(AND).append(RELEASABLE);
            break;
          case "DESIGN":
            stringQuery.append(STATUS);
            break;
        }
        break;
      default:
        stringQuery.append(String.format(LIKE, getTablePrefix(key), key));
        break;
    }
  }

  /**
   * Sets the parameters filters.
   *
   * @param query the query
   * @param key the key
   * @param value the value
   * @throws EEAException the EEA exception
   */
  private void setParametersFilters(Query query, String key, String value) {
    switch (key) {
      case "creation_date_from":
      case "creation_date_to":
      case "deadline_date_from":
      case "deadline_date_to":
      case "delivery_date_from":
      case "delivery_date_to":
        query.setParameter(key, new Date(Long.valueOf(value)));
        break;
      case DELIVERY_STATUS:
        List<String> deliveryStatus = Arrays.asList(value.split(","));
        query.setParameter(key, Arrays.asList(value.split(",")));
        break;
      case "status":
        switch (value) {
          case "OPEN":
            query.setParameter("status", "DRAFT");
            query.setParameter("releasable", Boolean.TRUE);
            break;
          case "CLOSED":
            query.setParameter("status", "DRAFT");
            query.setParameter("releasable", Boolean.FALSE);
            break;
          case "DESIGN":
            query.setParameter("status", value);
            break;
        }
        break;
      default:
        query.setParameter(key, "%" + value + "%");
        break;
    }
  }

  /**
   * Gets the table prefix.
   *
   * @param key the key
   * @return the table prefix
   * @throws EEAException the EEA exception
   */
  private String getTablePrefix(String key) throws EEAException {
    StringBuilder stringPrefix = new StringBuilder();
    switch (key) {
      case "description":
      case "name":
      case "obligation_id":
        stringPrefix.append("d.");
        break;
      case "obligation":
      case "legal_instrument":
        stringPrefix.append("ot.");
        break;
      default:
        throw new EEAException(EEAErrorMessage.HEADER_NOT_VALID);
    }
    stringPrefix.append(key);

    return stringPrefix.toString();
  }

  /**
   * Adds the and.
   *
   * @param stringQuery the string query
   * @param addAnd the add and
   */
  private void addAnd(StringBuilder stringQuery, boolean addAnd) {
    if (addAnd) {
      stringQuery.append(AND);
    }
  }

  /**
   * Construct public dataflows query.
   *
   * @param sb the sb
   * @param orderHeader the order header
   * @param asc the asc
   * @param filters the filters
   * @param applyFilters the apply filters
   * @throws EEAException the EEA exception
   */
  private void constructPublicDataflowsQuery(StringBuilder sb, String orderHeader, boolean asc,
      Map<String, String> filters, boolean applyFilters) throws EEAException {

    boolean addAnd = true;

    sb.append(QUERY_JSON_COUNTRY);
    sb.append(JOIN_REPRESENTATIVE_DATA_PROVIDER_AND_DATASET_AUX);
    sb.append(" where " + HAS_DATASETS);
    sb.append(AND + DATAFLOW_PUBLIC);
    sb.append(AND + COUNTRY_CODE_CONDITION);

    if (MapUtils.isNotEmpty(filters) && applyFilters) {
      for (String key : filters.keySet()) {
        addAnd(sb, addAnd);
        setFilters(sb, key, filters.get(key));
      }
    }

    if (StringUtils.isNotBlank(orderHeader)) {
      if ("status".equals(orderHeader)) {
        sb.append(String.format(ORDER_BY, orderHeader + (asc ? " asc" : " desc"), " ,releasable "));
      } else {
        orderHeader = orderHeader.equals("delivery_date") ? DATE_RELEASED : orderHeader;
        sb.append(String.format(ORDER_BY, orderHeader, asc ? "asc" : "desc"));
      }
    } else {
      sb.append(" order by status, creation_date desc ");
    }
  }
}
