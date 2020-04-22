package org.eea.dataset.persistence.data.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.eea.dataset.mapper.RecordNoValidationMapper;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.util.SortField;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

/**
 * The Class RecordRepositoryImpl.
 */
public class RecordRepositoryImpl implements RecordExtendedQueriesRepository {


  /**
   * The record no validation.
   */
  @Autowired
  private RecordNoValidationMapper recordNoValidationMapper;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetServiceImpl.class);
  /**
   * The entity manager.
   */
  @PersistenceContext
  private EntityManager entityManager;

  /**
   * The Constant DEFAULT_SORT_CRITERIA.
   */
  private static final String DEFAULT_STRING_SORT_CRITERIA = "' '";

  /**
   * The Constant DEFAULT_NUMERIC_SORT_CRITERIA.
   */
  private static final String DEFAULT_NUMERIC_SORT_CRITERIA = "0";

  /**
   * The Constant DEFAULT_DATE_SORT_CRITERIA.
   */
  private static final String DEFAULT_DATE_SORT_CRITERIA = "cast('01/01/1970' as java.sql.Date)";

  /**
   * The Constant SORT_QUERY.
   */
  private final static String SORT_STRING_QUERY =
      "COALESCE(( select distinct fv.value from FieldValue fv where fv.record.id=rv.id and fv.idFieldSchema ='%s' ), "
          + DEFAULT_STRING_SORT_CRITERIA + ") as order_criteria_%s";

  /**
   * The Constant SORT_NUMERIC_QUERY.
   */
  private final static String SORT_NUMERIC_QUERY =
      "COALESCE((select distinct case when is_numeric(fv.value) = true "
          + "then CAST(fv.value as java.math.BigDecimal) when is_numeric( fv.value)=false "
          + "then 0 end from FieldValue fv where fv.record.id = rv.id and fv.idFieldSchema = '%s'),"
          + DEFAULT_NUMERIC_SORT_CRITERIA + ") as order_criteria_%s ";

  /**
   * The Constant SORT_COORDINATE_QUERY.
   */
  private final static String SORT_COORDINATE_QUERY =
      "COALESCE((select distinct case when is_double(fv.value) = true "
          + "then CAST(fv.value as java.lang.Double) when is_double( fv.value)=false "
          + "then 0 end from FieldValue fv where fv.record.id = rv.id and fv.idFieldSchema = '%s'),"
          + DEFAULT_NUMERIC_SORT_CRITERIA + ") as order_criteria_%s ";

  /**
   * The Constant SORT_DATE_QUERY.
   */
  private final static String SORT_DATE_QUERY =
      "COALESCE((select distinct case when is_date(fv.value) = true "
          + "then CAST(fv.value as java.sql.Date) " + "when is_date( fv.value)=false "
          + "then cast('01/01/1970' as java.sql.Date) " + "end from FieldValue fv "
          + "where fv.record.id = rv.id and fv.idFieldSchema = '%s')," + DEFAULT_DATE_SORT_CRITERIA
          + ") as order_criteria_%s ";

  /**
   * The Constant CORRECT_APPEND_QUERY.
   */
  private final static String CORRECT_APPEND_QUERY =
      "AND not EXISTS (SELECT recval FROM RecordValidation recval "
          + "where rv.id = recval.recordValue.id) "
          + "AND not EXISTS (SELECT fvval FROM FieldValidation fvval "
          + "where rv.id = fvval.fieldValue.record.id) ";

  /**
   * The Constant WARNING_ERROR_CORRECT_APPEND_QUERY.
   */
  private final static String WARNING_ERROR_INFO_BLOCKER_CORRECT_APPEND_QUERY =
      "AND ((EXISTS (SELECT recval FROM RecordValidation recval "
          + "where rv.id = recval.recordValue.id "
          + "and recval.validation.levelError IN ( :errorList )) "
          + "OR EXISTS (SELECT fvval FROM FieldValidation fvval "
          + "where rv.id = fvval.fieldValue.record.id and fvval.validation.levelError IN ( :errorList ))) "
          + " OR " + " (not EXISTS (SELECT recval FROM RecordValidation recval "
          + "where rv.id=recval.recordValue.id)  "
          + "AND not EXISTS (SELECT fvval FROM FieldValidation fvval "
          + "where rv.id = fvval.fieldValue.record.id))) ";

  /**
   * The Constant WARNING_ERROR_APPEND_QUERY.
   */
  private final static String WARNING_ERROR_INFO_BLOCKER_APPEND_QUERY =
      "AND (EXISTS (SELECT recval FROM RecordValidation recval "
          + "where rv.id = recval.recordValue.id "
          + "and recval.validation.levelError IN ( :errorList )) "
          + "OR EXISTS (SELECT fvval FROM FieldValidation fvval "
          + "where rv.id = fvval.fieldValue.record.id and fvval.validation.levelError IN ( :errorList ))) ";

  /**
   * The Constant MASTER_QUERY.
   */
  private final static String MASTER_QUERY =
      "SELECT rv %s from RecordValue rv INNER JOIN rv.tableValue tv "
          + "WHERE tv.idTableSchema = :idTableSchema ";

  /**
   * The Constant MASTER_QUERY_NO_ORDER.
   */
  private final static String MASTER_QUERY_NO_ORDER =
      "SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv "
          + "WHERE tv.idTableSchema = :idTableSchema ";

  /**
   * The Constant MASTER_QUERY_COUNT.
   */
  private final static String MASTER_QUERY_COUNT =
      "SELECT count(rv) from RecordValue rv INNER JOIN rv.tableValue tv "
          + "WHERE tv.idTableSchema = :idTableSchema ";


  /**
   * The Constant FINAL_MASTER_QUERY.
   */
  private final static String FINAL_MASTER_QUERY = " order by %s";

  /**
   * The Constant QUERY_UNSORTERED.
   */
  private final static String QUERY_UNSORTERED =
      "SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv  "
          + "WHERE tv.idTableSchema = :idTableSchema";


  /**
   * Find by table value with order.
   *
   * @param idTableSchema the id table schema
   * @param levelErrorList the level error list
   * @param pageable the pageable
   * @param sortFields the sort fields
   * @return the list
   */
  @Override
  public TableVO findByTableValueWithOrder(String idTableSchema, List<ErrorTypeEnum> levelErrorList,
      Pageable pageable, SortField... sortFields) {
    StringBuilder sortQueryBuilder = new StringBuilder();
    StringBuilder directionQueryBuilder = new StringBuilder();
    int criteriaNumber = 0;
    TableVO result = new TableVO();
    createSorterQuery(sortQueryBuilder, directionQueryBuilder, criteriaNumber, sortFields);
    String filter = "";
    Boolean containsCorrect = false;
    List<ErrorTypeEnum> errorList = new ArrayList<>();
    // Filter Query by level Error if we havent lv error we do other things
    if (!levelErrorList.isEmpty()) {

      switch (levelErrorList.size()) {
        case 1:
          if (levelErrorList.contains(ErrorTypeEnum.CORRECT)) {
            filter = CORRECT_APPEND_QUERY;
            containsCorrect = true;
          } else {
            filter = WARNING_ERROR_INFO_BLOCKER_APPEND_QUERY;
            errorList.add(levelErrorList.get(0));
          }
          break;
        default:
          if (levelErrorList.contains(ErrorTypeEnum.CORRECT)) {
            filter = WARNING_ERROR_INFO_BLOCKER_CORRECT_APPEND_QUERY;
            containsCorrect = true;
          } else {
            filter = WARNING_ERROR_INFO_BLOCKER_APPEND_QUERY;
          }
          for (int i = 0; i < levelErrorList.size(); i++) {
            if (!levelErrorList.get(i).equals(ErrorTypeEnum.CORRECT)) {
              errorList.add(levelErrorList.get(i));
            }
          }
          break;
      }
    }
    result.setTotalFilteredRecords(0L);
    // we put that condition because we wont to do any query if the filter is empty and return a new
    // result object
    if (levelErrorList.size() != 0) {
      // Total records calc.
      if (!filter.isEmpty()) {
        Query query2;
        query2 = entityManager.createQuery(String.format(MASTER_QUERY_COUNT + filter));
        query2.setParameter("idTableSchema", idTableSchema);
        if (!filter.isEmpty()
            && (containsCorrect == false || (containsCorrect == true && errorList.size() > 0))) {
          query2.setParameter("errorList", errorList);
          query2.setParameter("errorList", errorList);
        }
        Long recordsCount = Long.valueOf(query2.getResultList().get(0).toString());
        result.setTotalFilteredRecords(recordsCount);
      }

      Query query;
      // Query without order.
      if (null == sortFields) {
        query = entityManager.createQuery(String.format(MASTER_QUERY_NO_ORDER + filter));
        query.setParameter("idTableSchema", idTableSchema);
        if (!filter.isEmpty()
            && (containsCorrect == false || (containsCorrect == true && errorList.size() > 0))) {
          query.setParameter("errorList", errorList);
          query.setParameter("errorList", errorList);
        }
        query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
        query.setMaxResults(pageable.getPageSize());
        List<RecordValue> a = query.getResultList();

        List<RecordVO> recordVOs = recordNoValidationMapper.entityListToClass(sanitizeRecords(a));
        result.setRecords(recordVOs);
      } else {
        // Query with order.
        query = entityManager.createQuery(String.format(MASTER_QUERY + filter + FINAL_MASTER_QUERY,
            sortQueryBuilder.toString(), directionQueryBuilder.toString().substring(1)));
        query.setParameter("idTableSchema", idTableSchema);
        if (!filter.isEmpty()
            && (containsCorrect == false || (containsCorrect == true && errorList.size() > 0))) {
          query.setParameter("errorList", errorList);
          query.setParameter("errorList", errorList);
        }
        query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
        query.setMaxResults(pageable.getPageSize());
        List<Object[]> a = query.getResultList();
        List<RecordVO> recordVOs = recordNoValidationMapper
            .entityListToClass(this.sanitizeOrderedRecords(a, sortFields[0].getAsc()));
        result.setRecords(recordVOs);

      }
    }
    return result;

  }


  /**
   * Creates the sorter query.
   *
   * @param sortQueryBuilder the sort query builder
   * @param directionQueryBuilder the direction query builder
   * @param criteriaNumber the criteria number
   * @param sortFields the sort fields
   */
  private void createSorterQuery(StringBuilder sortQueryBuilder,
      StringBuilder directionQueryBuilder, int criteriaNumber, SortField... sortFields) {
    // Multisorting Query accept n fields and order asc(1)/desc(0)
    String sortQuery = "";

    if (null != sortFields) {
      LOG.info("Init Order");
      for (SortField field : sortFields) {
        switch (field.getTypefield()) {
          case COORDINATE_LAT:
          case COORDINATE_LONG:
            sortQuery = SORT_COORDINATE_QUERY;
            break;
          case NUMBER_INTEGER:
          case NUMBER_DECIMAL:
            sortQuery = SORT_NUMERIC_QUERY;
            break;
          case DATE:
            sortQuery = SORT_DATE_QUERY;
            break;
          default:
            sortQuery = SORT_STRING_QUERY;
            break;
        }
        sortQueryBuilder.append(",")
            .append(String.format(sortQuery, field.getFieldName(), criteriaNumber)).append(" ");
        directionQueryBuilder.append(",").append(" order_criteria_").append(criteriaNumber)
            .append(" ").append(field.getAsc() ? "asc" : "desc");
      }

    }
  }


  /**
   * Find by table value no order. Allows null pageable and in that case all de records will be
   * retrieved
   *
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   *
   * @return the list
   */
  @Override
  public List<RecordValue> findByTableValueNoOrder(String idTableSchema, Pageable pageable) {
    Query query = entityManager.createQuery(QUERY_UNSORTERED);
    query.setParameter("idTableSchema", idTableSchema);
    if (null != pageable) {
      query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
      query.setMaxResults(pageable.getPageSize());
    }
    return query.getResultList();
  }

  /**
   * Find by table value all records.
   *
   * @param idTableSchema the id table schema
   * @return the list
   */
  @Override
  public List<RecordValue> findByTableValueAllRecords(String idTableSchema) {
    Query query = entityManager.createQuery(QUERY_UNSORTERED);
    query.setParameter("idTableSchema", idTableSchema);
    return query.getResultList();
  }


  /**
   * Sanitize ordered records.
   *
   * @param queryResults the query results
   * @param asc the asc
   *
   * @return the list
   */
  private List<RecordValue> sanitizeOrderedRecords(List<Object[]> queryResults, Boolean asc) {

    // First: Copy sortCriteria into de records variables
    List<RecordValue> records = queryResults.stream()
        .map(resultRecord -> (RecordValue) resultRecord[0]).collect(Collectors.toList());
    return sanitizeRecords(records);
  }

  /**
   * Sanitize records.
   *
   * @param records the records
   *
   * @return the list
   */
  private List<RecordValue> sanitizeRecords(List<RecordValue> records) {
    List<RecordValue> sanitizedRecords = new ArrayList<>();
    Set<String> processedRecords = new HashSet<>();
    for (RecordValue recordValue : records) {
      if (!processedRecords.contains(recordValue.getId())) {
        processedRecords.add(recordValue.getId());
        recordValue.getFields().stream().forEach(field -> field.setFieldValidations(null));
        sanitizedRecords.add(recordValue);
      }

    }
    return sanitizedRecords;

  }

}

