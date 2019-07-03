package org.eea.dataset.persistence.data.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.util.SortField;

/**
 * The Class RecordRepositoryImpl.
 */
public class RecordRepositoryImpl implements RecordExtendedQueriesRepository {

  /** The entity manager. */
  @PersistenceContext
  private EntityManager entityManager;

  /** The Constant DEFAULT_SORT_CRITERIA. */
  private static final String DEFAULT_SORT_CRITERIA = "' '";

  /** The Constant SORT_QUERY. */
  private final static String SORT_QUERY =
      "COALESCE((select fv.value from FieldValue fv where fv.id=fields.id and fv.idFieldSchema ='%s' ), "
          + DEFAULT_SORT_CRITERIA + ") as order_criteria_%s";

  /** The Constant MASTER_QUERY. */
  private final static String MASTER_QUERY =
      "SELECT rv %s from RecordValue rv INNER JOIN rv.tableValue tv INNER JOIN FETCH  rv.fields fields "
          + "WHERE tv.idTableSchema = :idTableSchema order by %s";

  /** The Constant QUERY_UNSORTERED. */
  private final static String QUERY_UNSORTERED =
      "SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv INNER JOIN FETCH  rv.fields "
          + "WHERE tv.idTableSchema = :idTableSchema";


  /**
   * Find by table value with order.
   *
   * @param idTableSchema the id table schema
   * @param sortFields the sort fields
   * @return the list
   */
  @Override
  public List<RecordValue> findByTableValueWithOrder(String idTableSchema,
      SortField... sortFields) {
    // TODO Como alternativa se puede hacer una query sin criterios de ordenacion y paginar y luego
    // ordenar fuera mediante codigo. Estudiar

    StringBuilder sortQueryBuilder = new StringBuilder();
    StringBuilder directionQueryBuilder = new StringBuilder();
    int criteriaNumber = 0;
    for (SortField field : sortFields) {
      sortQueryBuilder.append(",")
          .append(String.format(SORT_QUERY, field.getFieldName(), criteriaNumber)).append(" ");
      directionQueryBuilder.append(",").append(" order_criteria_").append(criteriaNumber)
          .append(" ").append(field.getAsc() ? "asc" : "desc");
    }
    Query query = entityManager.createQuery(String.format(MASTER_QUERY, sortQueryBuilder.toString(),
        directionQueryBuilder.toString().substring(1)));// adding sort address criteria. (Removing
                                                        // first , char)
    query.setParameter("idTableSchema", idTableSchema);

    return this.sanitizeOrderedRecords(query.getResultList(), sortFields[0].getAsc());
  }

  /**
   * Find by table value no order.
   *
   * @param idTableSchema the id table schema
   * @return the list
   */
  @Override
  public List<RecordValue> findByTableValueNoOrder(String idTableSchema) {
    Query query = entityManager.createQuery(QUERY_UNSORTERED);
    query.setParameter("idTableSchema", idTableSchema);
    return sanitizeUnOrderedRecords(query.getResultList());
  }

  /**
   * Removes duplicated records in the query.
   *
   * @param records the records
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
   * @return the list
   */
  private List<RecordValue> sanitizeOrderedRecords(List<Object[]> queryResults, Boolean asc) {

    // First: Copy sortCriteria into de records variables
    List<RecordValue> records = queryResults.stream().map(resultRecord -> {
      RecordValue record = (RecordValue) resultRecord[0];
      StringBuilder sortCriteriaBuilder = new StringBuilder();
      for (int i = 1; i < resultRecord.length; i++) {
        sortCriteriaBuilder.append(resultRecord[i]); // concat all sort criteria values as they will
                                                     // be used to discriminate
      }
      record.setSortCriteria(sortCriteriaBuilder.toString());
      return record;
    }).collect(Collectors.toList());

    Map<Long, RecordValue> processedRecords = new LinkedHashMap<>();
    // Once records has the values it is necessary to remove duplicated records preserving the order
    // (LinkedHashMap helps on this as it keeps the entry order and records are already ordered from
    // query)
    for (int i = 0; i < records.size(); i++) {
      RecordValue recordValue = records.get(i);
      if (!processedRecords.containsKey(recordValue.getId())) {
        processedRecords.put(recordValue.getId(), recordValue);
      } else if (!" ".equals(recordValue.getSortCriteria())) {
        processedRecords.remove(recordValue.getId());// it's necessary to remove because we don't
                                                     // want the previous record (it has an invalid
                                                     // sortCriteria, so wrong order)
        processedRecords.put(recordValue.getId(), recordValue);
      }
    }
    return processedRecords.values().stream().collect(Collectors.toList());
  }
}

