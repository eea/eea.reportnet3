package org.eea.dataset.persistence.data.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.util.SortField;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

/**
 * The Class RecordRepositoryImpl.
 */
public class RecordRepositoryImpl implements RecordExtendedQueriesRepository {


  /** The Constant LOG. */
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

  /** The Constant DEFAULT_NUMERIC_SORT_CRITERIA. */
  private static final String DEFAULT_NUMERIC_SORT_CRITERIA = "0";

  /** The Constant DEFAULT_DATE_SORT_CRITERIA. */
  private static final String DEFAULT_DATE_SORT_CRITERIA = "cast('01/01/1970' as java.sql.Date)";

  /**
   * The Constant SORT_QUERY.
   */
  private final static String SORT_STRING_QUERY =
      "COALESCE(( select distinct fv.value from FieldValue fv where fv.record.id=rv.id and fv.idFieldSchema ='%s' ), "
          + DEFAULT_STRING_SORT_CRITERIA + ") as order_criteria_%s";

  /** The Constant SORT_NUMERIC_QUERY. */
  private final static String SORT_NUMERIC_QUERY =
      "COALESCE((select distinct case when is_numeric(fv.value) = true "
          + "then CAST(fv.value as java.math.BigDecimal) when is_numeric( fv.value)=false "
          + "then 0 end from FieldValue fv where fv.record.id = rv.id and fv.idFieldSchema = '%s'),"
          + DEFAULT_NUMERIC_SORT_CRITERIA + ") as order_criteria_%s ";

  /** The Constant SORT_COORDINATE_QUERY. */
  private final static String SORT_COORDINATE_QUERY =
      "COALESCE((select distinct case when is_double(fv.value) = true "
          + "then CAST(fv.value as java.lang.Double) when is_double( fv.value)=false "
          + "then 0 end from FieldValue fv where fv.record.id = rv.id and fv.idFieldSchema = '%s'),"
          + DEFAULT_NUMERIC_SORT_CRITERIA + ") as order_criteria_%s ";

  /** The Constant SORT_DATE_QUERY. */
  private final static String SORT_DATE_QUERY =
      "COALESCE((select distinct case when is_date(fv.value) = true "
          + "then CAST(fv.value as java.sql.Date) " + "when is_date( fv.value)=false "
          + "then cast('01/01/1970' as java.sql.Date) " + "end from FieldValue fv "
          + "where fv.record.id = rv.id and fv.idFieldSchema = '%s')," + DEFAULT_DATE_SORT_CRITERIA
          + ") as order_criteria_%s ";

  /** The Constant WARNING_APPEND_QUERY. */
  private final static String WARNING_APPEND_QUERY =
      "AND (EXISTS (SELECT recval FROM RecordValidation recval "
          + "where recval.recordValue.id = rv.id "
          + "and recval.validation.levelError = 'WARNING') "
          + "OR EXISTS (SELECT fvval FROM FieldValidation fvval "
          + "where fvval.fieldValue.record.id = rv.id and fvval.validation.levelError = 'WARNING')) ";

  /** The Constant WARNING_ERROR_APPEND_QUERY. */
  private final static String WARNING_ERROR_APPEND_QUERY =
      "AND (EXISTS (SELECT recval FROM RecordValidation recval "
          + "where recval.recordValue.id = rv.id "
          + "and recval.validation.levelError IN ('ERROR','WARNING') ) "
          + "OR EXISTS (SELECT fvval FROM FieldValidation fvval "
          + "where fvval.fieldValue.record.id = rv.id and fvval.validation.levelError IN ('ERROR','WARNING') )) ";

  /** The Constant ERROR_APPEND_QUERY. */
  private final static String ERROR_APPEND_QUERY =
      "AND (EXISTS (SELECT recval FROM RecordValidation recval "
          + "where recval.recordValue.id = rv.id " + "and recval.validation.levelError = 'ERROR') "
          + "OR EXISTS (SELECT fvval FROM FieldValidation fvval "
          + "where fvval.fieldValue.record.id = rv.id and fvval.validation.levelError = 'ERROR')) ";

  /** The Constant CORRECT_APPEND_QUERY. */
  private final static String CORRECT_APPEND_QUERY =
      "AND not EXISTS (SELECT recval FROM RecordValidation recval "
          + "where recval.recordValue.id = rv.id "
          + "and recval.validation.levelError IN ('ERROR','WARNING')) "
          + "AND not EXISTS (SELECT fvval FROM FieldValidation fvval "
          + "where fvval.fieldValue.record.id = rv.id and fvval.validation.levelError IN ('ERROR','WARNING')) ";

  /** The Constant WARNING_CORRECT_APPEND_QUERY. */
  private final static String WARNING_CORRECT_APPEND_QUERY =
      "AND (EXISTS (SELECT recval FROM RecordValidation recval "
          + "where recval.recordValue.id = rv.id "
          + "and recval.validation.levelError = 'WARNING' ) "
          + "OR EXISTS (SELECT fvval FROM FieldValidation fvval "
          + "where fvval.fieldValue.record.id = rv.id and fvval.validation.levelError = 'WARNING' )) "
          + " OR " + " (not EXISTS (SELECT recval FROM RecordValidation recval "
          + "where recval.recordValue.id = rv.id "
          + "and recval.validation.levelError IN ('ERROR','WARNING') ) "
          + "AND not EXISTS (SELECT fvval FROM FieldValidation fvval "
          + "where fvval.fieldValue.record.id = rv.id and fvval.validation.levelError IN ('ERROR','WARNING'))) ";

  /** The Constant ERROR_CORRECT_APPEND_QUERY. */
  private final static String ERROR_CORRECT_APPEND_QUERY =
      "AND (EXISTS (SELECT recval FROM RecordValidation recval "
          + "where recval.recordValue.id = rv.id " + "and recval.validation.levelError = 'ERROR' ) "
          + "OR not EXISTS (SELECT fvval FROM FieldValidation fvval "
          + "where fvval.fieldValue.record.id = rv.id and fvval.validation.levelError = 'ERROR' )) "
          + " OR " + " (not EXISTS (SELECT recval FROM RecordValidation recval "
          + "where recval.recordValue.id = rv.id "
          + "and recval.validation.levelError IN ('ERROR','WARNING') ) "
          + "AND not EXISTS (SELECT fvval FROM FieldValidation fvval "
          + "where fvval.fieldValue.record.id = rv.id and fvval.validation.levelError IN ('ERROR','WARNING') )) ";

  /**
   * The Constant MASTER_QUERY.
   */
  private final static String MASTER_QUERY =
      "SELECT rv %s from RecordValue rv INNER JOIN rv.tableValue tv "
          + "WHERE tv.idTableSchema = :idTableSchema ";

  /** The Constant MASTER_QUERY_NO_ORDER. */
  private final static String MASTER_QUERY_NO_ORDER =
      "SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv "
          + "WHERE tv.idTableSchema = :idTableSchema ";


  /** The Constant FINAL_MASTER_QUERY. */
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
   * @param levelError the level error
   * @param pageable the pageable
   * @param sortFields the sort fields
   * @return the list
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<RecordValue> findByTableValueWithOrder(String idTableSchema,
      TypeErrorEnum[] levelError, Pageable pageable, SortField... sortFields) {
    StringBuilder sortQueryBuilder = new StringBuilder();
    StringBuilder directionQueryBuilder = new StringBuilder();
    int criteriaNumber = 0;
    // Multisorting Query accept n fields and order asc(1)/desc(0)
    if (null != sortFields) {
      LOG.info("Init Order");
      for (SortField field : sortFields) {
        switch (field.getTypefield()) {
          case COORDINATE_LAT:
            sortQueryBuilder.append(",")
                .append(String.format(SORT_COORDINATE_QUERY, field.getFieldName(), criteriaNumber))
                .append(" ");
            directionQueryBuilder.append(",").append(" order_criteria_").append(criteriaNumber)
                .append(" ").append(field.getAsc() ? "asc" : "desc");
            break;
          case COORDINATE_LONG:
            sortQueryBuilder.append(",")
                .append(String.format(SORT_COORDINATE_QUERY, field.getFieldName(), criteriaNumber))
                .append(" ");
            directionQueryBuilder.append(",").append(" order_criteria_").append(criteriaNumber)
                .append(" ").append(field.getAsc() ? "asc" : "desc");
            break;
          case NUMBER:
            sortQueryBuilder.append(",")
                .append(String.format(SORT_NUMERIC_QUERY, field.getFieldName(), criteriaNumber))
                .append(" ");
            directionQueryBuilder.append(",").append(" order_criteria_").append(criteriaNumber)
                .append(" ").append(field.getAsc() ? "asc" : "desc");
            break;
          case DATE:
            sortQueryBuilder.append(",")
                .append(String.format(SORT_DATE_QUERY, field.getFieldName(), criteriaNumber))
                .append(" ");
            directionQueryBuilder.append(",").append(" order_criteria_").append(criteriaNumber)
                .append(" ").append(field.getAsc() ? "asc" : "desc");
            break;
          default:
            sortQueryBuilder.append(",")
                .append(String.format(SORT_STRING_QUERY, field.getFieldName(), criteriaNumber))
                .append(" ");
            directionQueryBuilder.append(",").append(" order_criteria_").append(criteriaNumber)
                .append(" ").append(field.getAsc() ? "asc" : "desc");
            break;
        }
      }
    }
    String filter = "";
    // Filter Query by level Error (ERROR,WARNING,CORRECT)
    if (null != levelError) {
      LOG.info("Init Error Filter");
      List<TypeErrorEnum> lvl = new ArrayList<TypeErrorEnum>();
      for (int i = 0; i < levelError.length; i++) {
        lvl.add(levelError[i]);
      }


      switch (levelError.length) {
        case 3:
          break;
        case 1:
          if (lvl.contains(TypeErrorEnum.ERROR)) {
            filter = ERROR_APPEND_QUERY;
          } else if (lvl.contains(TypeErrorEnum.WARNING)) {
            filter = WARNING_APPEND_QUERY;
          } else if (lvl.contains(TypeErrorEnum.CORRECT)) {
            filter = CORRECT_APPEND_QUERY;
          }
          break;
        case 2:
          if (lvl.contains(TypeErrorEnum.WARNING) && lvl.contains(TypeErrorEnum.ERROR)) {
            filter = WARNING_ERROR_APPEND_QUERY;
          } else if (lvl.contains(TypeErrorEnum.ERROR) && lvl.contains(TypeErrorEnum.CORRECT)) {
            filter = ERROR_CORRECT_APPEND_QUERY;
          } else if (lvl.contains(TypeErrorEnum.WARNING) && lvl.contains(TypeErrorEnum.CORRECT)) {
            filter = WARNING_CORRECT_APPEND_QUERY;
          }
          break;
        default:
          List<RecordValue> emptyrecords = new ArrayList<>();
          return emptyrecords;
      }
    }

    Query query;
    if (null == sortFields) {
      query = entityManager.createQuery(String.format(MASTER_QUERY_NO_ORDER + filter));
      query.setParameter("idTableSchema", idTableSchema);
      query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
      query.setMaxResults(pageable.getPageSize());
      List<RecordValue> a = query.getResultList();
      return a;
    } else {
      query = entityManager.createQuery(String.format(MASTER_QUERY + filter + FINAL_MASTER_QUERY,
          sortQueryBuilder.toString(), directionQueryBuilder.toString().substring(1)));
      query.setParameter("idTableSchema", idTableSchema);
      query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
      query.setMaxResults(pageable.getPageSize());
      List<Object[]> a = query.getResultList();
      return this.sanitizeOrderedRecords(a, sortFields[0].getAsc());
    }
  }



  /**
   * Find by table value no order. Allows null pageable and in that case all de records will be
   * retrieved
   *
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @return the list
   */
  @SuppressWarnings("unchecked")
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
    return records;
  }
}

