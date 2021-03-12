package org.eea.dataset.persistence.data.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.bson.Document;
import org.eea.dataset.mapper.RecordNoValidationMapper;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.util.SortField;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

/**
 * The Class RecordRepositoryImpl.
 */
public class RecordRepositoryImpl implements RecordExtendedQueriesRepository {

  /** The Constant MAX_FILTERS. */
  private static final int MAX_FILTERS = 5;

  /** The record no validation mapper. */
  @Autowired
  private RecordNoValidationMapper recordNoValidationMapper;

  @Autowired
  private SchemasRepository schemasRepository;

  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(RecordRepositoryImpl.class);

  /** The entity manager. */
  @PersistenceContext
  private EntityManager entityManager;

  /** The Constant WHERE_TV: {@value}. */
  private static final String WHERE_ID_TABLE_SCHEMA = "WHERE tv.idTableSchema = :idTableSchema ";

  /** The Constant AS_ORDER_CRITERIA: {@value}. */
  private static final String AS_ORDER_CRITERIA = ") as order_criteria_%s ";

  /** The Constant WHERE_RECORDVALUE: {@value}. */
  private static final String WHERE_RECORDVALUE = "where rv.id = recval.recordValue.id ";

  /** The Constant OR_EXISTS: {@value}. */
  private static final String OR_EXISTS = "OR EXISTS (SELECT fvval FROM FieldValidation fvval ";

  /** The Constant DEFAULT_STRING_SORT_CRITERIA: {@value}. */
  private static final String DEFAULT_STRING_SORT_CRITERIA = "' '";

  /** The Constant DEFAULT_NUMERIC_SORT_CRITERIA: {@value}. */
  private static final String DEFAULT_NUMERIC_SORT_CRITERIA = "0";

  /** The Constant DEFAULT_DATE_SORT_CRITERIA: {@value}. */
  private static final String DEFAULT_DATE_SORT_CRITERIA = "cast('01/01/1970' as java.sql.Date)";

  /** The Constant SORT_STRING_QUERY: {@value}. */
  private static final String SORT_STRING_QUERY =
      "COALESCE(( select distinct fv.value from FieldValue fv where fv.record.id=rv.id and fv.idFieldSchema ='%s' ), "
          + DEFAULT_STRING_SORT_CRITERIA + ") as order_criteria_%s";

  /** The Constant SORT_NUMERIC_QUERY: {@value}. */
  private static final String SORT_NUMERIC_QUERY =
      "COALESCE((select distinct case when is_numeric(fv.value) = true "
          + "then CAST(fv.value as java.math.BigDecimal) when is_numeric( fv.value)=false "
          + "then 0 end from FieldValue fv where fv.record.id = rv.id and fv.idFieldSchema = '%s'),"
          + DEFAULT_NUMERIC_SORT_CRITERIA + AS_ORDER_CRITERIA;

  /** The Constant SORT_DATE_QUERY: {@value}. */
  private static final String SORT_DATE_QUERY =
      "COALESCE((select distinct case when is_date(fv.value) = true "
          + "then CAST(fv.value as java.sql.Date) " + "when is_date( fv.value)=false "
          + "then cast('01/01/1970' as java.sql.Date) " + "end from FieldValue fv "
          + "where fv.record.id = rv.id and fv.idFieldSchema = '%s')," + DEFAULT_DATE_SORT_CRITERIA
          + AS_ORDER_CRITERIA;

  /** The Constant CORRECT_APPEND_QUERY: {@value}. */
  private static final String CORRECT_APPEND_QUERY =
      "AND not EXISTS (SELECT recval FROM RecordValidation recval "
          + "where rv.id = recval.recordValue.id) "
          + "AND not EXISTS (SELECT fvval FROM FieldValidation fvval "
          + "where rv.id = fvval.fieldValue.record.id) ";

  /** The Constant WARNING_ERROR_INFO_BLOCKER_CORRECT_APPEND_QUERY: {@value}. */
  private static final String WARNING_ERROR_INFO_BLOCKER_CORRECT_APPEND_QUERY =
      "AND ((EXISTS (SELECT recval FROM RecordValidation recval " + WHERE_RECORDVALUE
          + "and recval.validation.levelError IN ( :errorList )) " + OR_EXISTS
          + "where rv.id = fvval.fieldValue.record.id and fvval.validation.levelError IN ( :errorList ))) "
          + " OR " + " (not EXISTS (SELECT recval FROM RecordValidation recval "
          + "where rv.id=recval.recordValue.id)  "
          + "AND not EXISTS (SELECT fvval FROM FieldValidation fvval "
          + "where rv.id = fvval.fieldValue.record.id))) ";

  /** The Constant WARNING_ERROR_INFO_BLOCKER_APPEND_QUERY: {@value}. */
  private static final String WARNING_ERROR_INFO_BLOCKER_APPEND_QUERY =
      "AND (EXISTS (SELECT recval FROM RecordValidation recval " + WHERE_RECORDVALUE
          + "and recval.validation.levelError IN ( :errorList )) " + OR_EXISTS
          + "where rv.id = fvval.fieldValue.record.id and fvval.validation.levelError IN ( :errorList ))) ";

  /** The Constant RULE_ID_APPEND_QUERY: {@value}. */
  private static final String RULE_ID_APPEND_QUERY =
      "AND (EXISTS (SELECT recval FROM RecordValidation recval " + WHERE_RECORDVALUE
          + "and recval.validation.idRule IN ( :ruleIdList )) " + OR_EXISTS
          + "where rv.id = fvval.fieldValue.record.id and fvval.validation.idRule IN ( :ruleIdList ))) ";

  /** The Constant LIKE_APPEND_QUERY: {@value}. */
  private static final String LIKE_APPEND_QUERY =
      "AND rv.id IN (SELECT fieldV.record from FieldValue fieldV where fieldV.idFieldSchema = :fieldSchema and fieldV.value LIKE :fieldValue) ";

  /** The Constant MASTER_QUERY: {@value}. */
  private static final String MASTER_QUERY =
      "SELECT rv %s from RecordValue rv INNER JOIN rv.tableValue tv " + WHERE_ID_TABLE_SCHEMA;

  /** The Constant MASTER_QUERY_NO_ORDER: {@value}. */
  private static final String MASTER_QUERY_NO_ORDER =
      "SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv " + WHERE_ID_TABLE_SCHEMA;

  /** The Constant MASTER_QUERY_COUNT: {@value}. */
  private static final String MASTER_QUERY_COUNT =
      "SELECT count(rv) from RecordValue rv INNER JOIN rv.tableValue tv " + WHERE_ID_TABLE_SCHEMA;

  /** The Constant FINAL_MASTER_QUERY: {@value}. */
  private static final String FINAL_MASTER_QUERY = " order by %s";

  /** The Constant QUERY_UNSORTERED: {@value}. */
  private static final String QUERY_UNSORTERED =
      "SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv  "
          + "WHERE tv.idTableSchema = :idTableSchema";

  /** The Constant QUERY_UNSORTERED_FIELDS: {@value}. */
  private static final String QUERY_UNSORTERED_FIELDS =
      "SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv INNER JOIN FETCH  rv.fields  "
          + "WHERE tv.idTableSchema = :idTableSchema";

  /** The Constant ID_TABLE_SCHEMA: {@value}. */
  private static final String ID_TABLE_SCHEMA = "idTableSchema";

  /** The Constant ERROR_LIST: {@value}. */
  private static final String ERROR_LIST = "errorList";

  /** The Constant RULE_ID_LIST: {@value}. */
  private static final String RULE_ID_LIST = "ruleIdList";

  /** The Constant FIELD_VALUE: {@value}. */
  private static final String FIELD_VALUE = "fieldValue";

  /** The Constant FIELD_SCHEMA: {@value}. */
  private static final String FIELD_SCHEMA = "fieldSchema";

  /**
   * Find by table value with order.
   *
   * @param idTableSchema the id table schema
   * @param levelErrorList the level error list
   * @param pageable the pageable
   * @param idRules the id rules
   * @param fieldSchema the field schema
   * @param fieldValue the field value
   * @param sortFields the sort fields
   * @return the table VO
   */
  @Override
  public TableVO findByTableValueWithOrder(Long datasetId, String idTableSchema,
      List<ErrorTypeEnum> levelErrorList, Pageable pageable, List<String> idRules,
      String fieldSchema, String fieldValue, SortField... sortFields) {

    StringBuilder sortQueryBuilder = new StringBuilder();
    StringBuilder directionQueryBuilder = new StringBuilder();
    int criteriaNumber = 0;
    TableVO result = new TableVO();
    createSorterQuery(datasetId, sortQueryBuilder, directionQueryBuilder, criteriaNumber,
        sortFields);
    String filter = "";
    List<ErrorTypeEnum> errorList = new ArrayList<>();
    result.setTotalFilteredRecords(0L);
    boolean levelErrorListFilled = !levelErrorList.isEmpty();
    boolean idRulesListFilled = null != idRules && !idRules.isEmpty();

    // Compose the query filtering by level ERROR
    filter = composeFilterByError(levelErrorList, fieldSchema, fieldValue, filter, errorList,
        levelErrorListFilled, idRulesListFilled);
    // We don't want to do any query if the filter is empty and then return a new
    // result object
    if (levelErrorListFilled || idRulesListFilled) {
      // Total records calculated.
      recordsCalc(idTableSchema, result, filter, errorList, idRules, fieldSchema, fieldValue);

      queryOrder(idTableSchema, pageable, sortQueryBuilder, directionQueryBuilder, result, filter,
          errorList, idRules, fieldSchema, fieldValue, sortFields);
    }
    return result;
  }

  /**
   * Compose filter by error.
   *
   * @param levelErrorList the level error list
   * @param fieldSchema the field schema
   * @param fieldValue the field value
   * @param filter the filter
   * @param errorList the error list
   * @param levelErrorListFilled the level error list filled
   * @param idRulesListFilled the id rules list filled
   * @return the filter
   */
  private String composeFilterByError(List<ErrorTypeEnum> levelErrorList, String fieldSchema,
      String fieldValue, String filter, List<ErrorTypeEnum> errorList, boolean levelErrorListFilled,
      boolean idRulesListFilled) {
    if (levelErrorListFilled && levelErrorList.size() != MAX_FILTERS) {
      filter = WARNING_ERROR_INFO_BLOCKER_APPEND_QUERY;
      if (levelErrorList.size() == 1) {
        if (levelErrorList.contains(ErrorTypeEnum.CORRECT)) {
          filter = CORRECT_APPEND_QUERY;
        } else {
          errorList.add(levelErrorList.get(0));
        }
      } else {
        if (levelErrorList.contains(ErrorTypeEnum.CORRECT)) {
          filter = WARNING_ERROR_INFO_BLOCKER_CORRECT_APPEND_QUERY;
        }
        errorList.addAll(levelErrorList);
      }
    }
    if (idRulesListFilled) {
      filter = filter + RULE_ID_APPEND_QUERY;
    }
    if (fieldSchema != null && fieldValue != null) {
      filter = filter + LIKE_APPEND_QUERY;
    }
    return filter;
  }

  /**
   * Records calc.
   *
   * @param idTableSchema the id table schema
   * @param result the result
   * @param filter the filter
   * @param containsCorrect the contains correct
   * @param errorList the error list
   * @param idRules the id rules
   * @param fieldSchema the field schema
   * @param fieldValue the field value
   */
  private void recordsCalc(String idTableSchema, TableVO result, String filter,
      List<ErrorTypeEnum> errorList, List<String> idRules, String fieldSchema, String fieldValue) {
    if (!filter.isEmpty()) {
      Query query2;
      query2 = entityManager.createQuery(MASTER_QUERY_COUNT + filter);
      query2.setParameter(ID_TABLE_SCHEMA, idTableSchema);
      if (null != idRules && !idRules.isEmpty()) {
        query2.setParameter(RULE_ID_LIST, idRules);
        query2.setParameter(RULE_ID_LIST, idRules);
      }
      if (null != fieldSchema && null != fieldValue) {
        query2.setParameter(FIELD_SCHEMA, fieldSchema);
        query2.setParameter(FIELD_VALUE, fieldValue);
      }
      if (!errorList.isEmpty()) {
        query2.setParameter(ERROR_LIST, errorList);
        query2.setParameter(ERROR_LIST, errorList);
      }
      Long recordsCount = Long.valueOf(query2.getResultList().get(0).toString());
      result.setTotalFilteredRecords(recordsCount);
    }
  }

  /**
   * Query order.
   *
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @param sortQueryBuilder the sort query builder
   * @param directionQueryBuilder the direction query builder
   * @param result the result
   * @param filter the filter
   * @param containsCorrect the contains correct
   * @param errorList the error list
   * @param idRules the id rules
   * @param fieldSchema the field schema
   * @param fieldValue the field value
   * @param sortFields the sort fields
   */
  private void queryOrder(String idTableSchema, Pageable pageable, StringBuilder sortQueryBuilder,
      StringBuilder directionQueryBuilder, TableVO result, String filter,
      List<ErrorTypeEnum> errorList, List<String> idRules, String fieldSchema, String fieldValue,
      SortField... sortFields) {

    // Query without order or with it
    Query query = entityManager.createQuery(null == sortFields ? MASTER_QUERY_NO_ORDER + filter
        : String.format(MASTER_QUERY + filter + FINAL_MASTER_QUERY, sortQueryBuilder.toString(),
            directionQueryBuilder.toString().substring(1)));

    query.setParameter(ID_TABLE_SCHEMA, idTableSchema);
    if (null != idRules && !idRules.isEmpty()) {
      query.setParameter(RULE_ID_LIST, idRules);
      query.setParameter(RULE_ID_LIST, idRules);
    }
    if (!filter.isEmpty() && !errorList.isEmpty()) {
      query.setParameter(ERROR_LIST, errorList);
      query.setParameter(ERROR_LIST, errorList);
    }
    if (null != fieldSchema && null != fieldValue) {
      query.setParameter(FIELD_SCHEMA, fieldSchema);
      query.setParameter(FIELD_VALUE, fieldValue);
    }
    query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
    query.setMaxResults(pageable.getPageSize());

    List<RecordVO> recordVOs = null;
    if (null == sortFields) {
      // Query without order.
      List<RecordValue> a = query.getResultList();
      recordVOs = recordNoValidationMapper.entityListToClass(sanitizeRecords(a));
      result.setRecords(recordVOs);
    } else {
      // Query with order.
      List<Object[]> a = query.getResultList();
      recordVOs = recordNoValidationMapper.entityListToClass(sanitizeOrderedRecords(a));
    }
    result.setRecords(recordVOs);
  }

  /**
   * Creates the sorter query.
   *
   * @param datasetId the dataset id
   * @param sortQueryBuilder the sort query builder
   * @param directionQueryBuilder the direction query builder
   * @param criteriaNumber the criteria number
   * @param sortFields the sort fields
   */
  private void createSorterQuery(Long datasetId, StringBuilder sortQueryBuilder,
      StringBuilder directionQueryBuilder, int criteriaNumber, SortField... sortFields) {
    // Multisorting Query accept n fields and order asc(1)/desc(0)
    String sortQuery = "";

    if (null != sortFields) {
      LOG.info("Init Order");
      for (SortField field : sortFields) {
        // we check if the field is link and look the linked type to sort in the table
        if (field.getTypefield().equals(DataType.LINK)) {
          String datasetSchemaId = dataSetMetabaseRepository.findDatasetSchemaIdById(datasetId);
          Document documentField =
              schemasRepository.findFieldSchema(datasetSchemaId, field.getFieldName());
          Document documentReference = (Document) documentField.get("referencedField");
          Document documentFieldReferenced =
              schemasRepository.findFieldSchema(documentReference.get("idDatasetSchema").toString(),
                  documentReference.get("idPk").toString());

          DataType typeData = DataType.valueOf(documentFieldReferenced.get("typeData").toString());
          field.setTypefield(typeData);
        }
        switch (field.getTypefield()) {
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
            .append(" ").append(Boolean.TRUE.equals(field.getAsc()) ? "asc" : "desc");
      }
    }
  }

  /**
   * Find by table value no order.
   *
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @return the list
   */
  @Override
  public List<RecordValue> findByTableValueNoOrder(String idTableSchema, Pageable pageable) {
    Query query = entityManager.createQuery(QUERY_UNSORTERED);
    query.setParameter(ID_TABLE_SCHEMA, idTableSchema);
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
    query.setParameter(ID_TABLE_SCHEMA, idTableSchema);
    return query.getResultList();
  }

  /**
   * Find by table value no order optimized.
   *
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @return the list
   */
  @Override
  public List<RecordValue> findByTableValueNoOrderOptimized(String idTableSchema,
      Pageable pageable) {
    Query query = entityManager.createQuery(QUERY_UNSORTERED_FIELDS);
    query.setParameter(ID_TABLE_SCHEMA, idTableSchema);
    if (null != pageable) {
      query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
      query.setMaxResults(pageable.getPageSize());
    }
    return sanitizeRecords(query.getResultList());
  }

  /**
   * Sanitize ordered records.
   *
   * @param queryResults the query results
   * @return the list
   */
  private List<RecordValue> sanitizeOrderedRecords(List<Object[]> queryResults) {
    // First: Copy sortCriteria into de records variables
    List<RecordValue> records = queryResults.stream()
        .map(resultRecord -> (RecordValue) resultRecord[0]).collect(Collectors.toList());
    return sanitizeRecords(records);
  }

  /**
   * Sanitize records.
   *
   * @param records the records
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

  /**
   * Find last record in the db table.
   *
   * @return the record value
   */
  @Override
  public RecordValue findLastRecord() {
    RecordValue result = null;
    Query query2 = entityManager.createQuery("SELECT count(rv) from RecordValue rv");

    int recordsCount = Integer.parseInt(query2.getResultList().get(0).toString());
    recordsCount = recordsCount == 0 ? 0 : recordsCount - 1;
    try {
      Query query = entityManager.createQuery("select rv from RecordValue rv ");
      query.setMaxResults(1);
      query.setFirstResult(recordsCount);
      result = (RecordValue) query.getSingleResult();
    } catch (NoResultException e) {
      LOG.info("no result, ignore message");
    }
    return result;
  }

}
