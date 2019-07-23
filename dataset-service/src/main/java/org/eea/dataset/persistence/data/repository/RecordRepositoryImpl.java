package org.eea.dataset.persistence.data.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.util.SortField;
import org.springframework.data.domain.Pageable;

/**
 * The Class RecordRepositoryImpl.
 */
public class RecordRepositoryImpl implements RecordExtendedQueriesRepository {

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

  /**
   * The Constant MASTER_QUERY.
   */
  private final static String MASTER_QUERY =
      "SELECT rv %s from RecordValue rv INNER JOIN rv.tableValue tv  "
          + "WHERE tv.idTableSchema = :idTableSchema order by %s";

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
   * @param sortFields the sort fields
   *
   * @return the list
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<RecordValue> findByTableValueWithOrder(String idTableSchema, Pageable pageable,
      SortField... sortFields) {
    // TODO Como alternativa se puede hacer una query sin criterios de ordenacion y paginar y luego
    // ordenar fuera mediante codigo. Estudiar

    StringBuilder sortQueryBuilder = new StringBuilder();
    StringBuilder directionQueryBuilder = new StringBuilder();
    int criteriaNumber = 0;
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

      }
    }
    Query query = entityManager.createQuery(String.format(MASTER_QUERY, sortQueryBuilder.toString(),
        directionQueryBuilder.toString().substring(1)));
    query.setParameter("idTableSchema", idTableSchema);
    query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
    query.setMaxResults(pageable.getPageSize());

    return this.sanitizeOrderedRecords(query.getResultList(), sortFields[0].getAsc());
  }



  /**
   * Find by table value no order. Allows null pageable and in that case all de records will be
   * retrieved
   *
   * @param idTableSchema the id table schema
   * @param pageable the pageable
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
   * Removes duplicated records in the query.
   *
   * @param records the records
   *
   * @return the list
   */
  private List<RecordValue> sanitizeUnOrderedRecords(List<RecordValue> records) {
    List<RecordValue> sanitizedRecords = new ArrayList<>();
    Set<Long> processedRecords = new HashSet<>();
    for (RecordValue recordValue : records) {
      if (!processedRecords.contains(recordValue.getId())) {
        processedRecords.add(recordValue.getId());
        sanitizedRecords.add(recordValue);
      }

    }
    return sanitizedRecords;

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

