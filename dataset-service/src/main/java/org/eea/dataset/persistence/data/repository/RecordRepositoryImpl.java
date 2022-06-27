package org.eea.dataset.persistence.data.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.RecordNoValidationMapper;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.util.SortField;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.vo.dataset.ExportFilterVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.multitenancy.TenantResolver;
import org.eea.utils.LiteralConstants;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.QueryHints;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.copy.CopyOut;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class RecordRepositoryImpl.
 */
public class RecordRepositoryImpl implements RecordExtendedQueriesRepository {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant MAX_FILTERS. */
  private static final int MAX_FILTERS = 5;

  /** The record no validation mapper. */
  @Autowired
  private RecordNoValidationMapper recordNoValidationMapper;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The dataset repository. */
  @Autowired
  private DatasetRepository datasetRepository;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(RecordRepositoryImpl.class);

  /** The entity manager. */
  @PersistenceContext
  private EntityManager entityManager;

  /** The import path. */
  @Value("${importPath}")
  private String importPath;

  /** The record store controller zuul. */
  @Autowired
  private RecordStoreControllerZuul recordStoreControllerZuul;


  /** The Constant WHERE_ID_TABLE_SCHEMA: {@value}. */
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

  /** The Constant LIKE_APPEND_QUERY_NO_FIELD_SCHEMA: {@value}. */
  private static final String LIKE_APPEND_QUERY_NO_FIELD_SCHEMA =
      "AND rv.id IN (SELECT fieldV.record from FieldValue fieldV where fieldV.value LIKE :fieldValue and fieldV.type NOT IN ('POINT', 'LINESTRING', 'POLYGON', 'MULTIPOINT', 'MULTILINESTRING', 'MULTIPOLYGON')) ";

  /** The Constant MASTER_QUERY: {@value}. */
  private static final String MASTER_QUERY =
      "SELECT rv %s from RecordValue rv INNER JOIN rv.tableValue tv " + WHERE_ID_TABLE_SCHEMA;

  /** The Constant MASTER_QUERY_NO_ORDER: {@value}. */
  private static final String MASTER_QUERY_NO_ORDER =
      "SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv " + WHERE_ID_TABLE_SCHEMA;

  /** The Constant MASTER_QUERY_COUNT: {@value}. */
  private static final String MASTER_QUERY_COUNT =
      "SELECT count(rv.id) from RecordValue rv INNER JOIN rv.tableValue tv "
          + WHERE_ID_TABLE_SCHEMA;

  /** The Constant FINAL_MASTER_QUERY: {@value}. */
  private static final String FINAL_MASTER_QUERY = " order by %s";

  /** The Constant QUERY_UNSORTERED: {@value}. */
  private static final String QUERY_UNSORTERED =
      "SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv  "
          + "WHERE tv.idTableSchema = :idTableSchema order by rv.dataPosition";

  /** The Constant QUERY_UNSORTERED_FIELDS: {@value}. */
  private static final String QUERY_UNSORTERED_FIELDS =
      "SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv INNER JOIN FETCH  rv.fields  "
          + "WHERE tv.idTableSchema = :idTableSchema order by rv.dataPosition";

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

  /** The Constant TABLE_NAME: {@value}. */
  private static final String TABLE_NAME = "tableName";

  /** The Constant TOTAL_RECORDS: {@value}. */
  private static final String TOTAL_RECORDS = "totalRecords";

  /** The Constant RECORDS: {@value}. */
  private static final String RECORDS = "records";

  /** The Constant CORRECT: {@value}. */
  private static final String CORRECT = "CORRECT";

  /** The Constant ETL_EXPORT. */
  private static final String ETL_EXPORT = "/etlExport/";


  /** The Constant RESERVED_SQL_WORDS. */
  private static final String[] RESERVED_SQL_WORDS = {"ABORT", "ABSOLUTE", "ACCESS", "ACTION",
      "ADD", "AGGREGATE", "ALTER", "ANALYSE", "ANALYZE", "ANY", "ARRAY", "ASSERTION", "ASSIGNMENT",
      "AT", "AUTHORIZATION", "BACKWARD", "BIGINT", "BINARY", "BIT", "BOOLEAN", "BOTH", "CACHE",
      "CALLED", "CAST", "CHAIN", "CHAR", "CHARACTER", "CHARACTERISTICS", "CHECKPOINT", "CLASS",
      "CLOSE", "CLUSTER", "COALESCE", "COLUMN", "COMMENT", "COMMITTED", "CONSTRAINTS", "CONVERSION",
      "CONVERT", "COPY", "CREATEDB", "CREATEUSER", "CURRENT_DATE", "CURRENT_TIME",
      "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "CYCLE", "DAY", "DEALLOCATE", "DEC", "DECIMAL",
      "DECLARE", "DEFAULTS", "DEFERRABLE", "DEFERRED", "DEFINER", "DELIMITER", "DELIMITERS", "DO",
      "DOMAIN", "DOUBLE", "EACH", "ENCODING", "ENCRYPTED", "ESCAPE", "EXCEPT", "EXCLUDING",
      "EXCLUSIVE", "EXECUTE", "EXISTS", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FIRST", "FLOAT",
      "FORCE", "FORWARD", "FREEZE", "FUNCTION", "GLOBAL", "GRANT", "HANDLER", "HOLD", "HOUR",
      "ILIKE", "IMMEDIATE", "IMMUTABLE", "IMPLICIT", "INCLUDING", "INCREMENT", "INHERITS",
      "INITIALLY", "INOUT", "INPUT", "INSENSITIVE", "INSTEAD", "INT", "INTERSECT", "INTERVAL",
      "INVOKER", "ISNULL", "ISOLATION", "LANCOMPILER", "LANGUAGE", "LAST", "LEADING", "LEVEL",
      "LISTEN", "LOAD", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOCATION", "LOCK", "MAXVALUE",
      "MINUTE", "MINVALUE", "MODE", "MONTH", "MOVE", "NAMES", "NATIONAL", "NCHAR", "NEW", "NEXT",
      "NO", "NOCREATEDB", "NOCREATEUSER", "NONE", "NOTHING", "NOTIFY", "NOTNULL", "NULLIF",
      "NUMERIC", "OF", "OFF", "OIDS", "OLD", "ONLY", "OPERATOR", "OPTION", "OUT", "OVERLAPS",
      "OVERLAY", "OWNER", "PARTIAL", "PASSWORD", "PATH", "PENDANT", "PLACING", "POSITION",
      "PRECISION", "PREPARE", "PRESERVE", "PRIOR", "PRIVILEGES", "PROCEDURAL", "PROCEDURE", "READ",
      "REAL", "RECHECK", "REINDEX", "RELATIVE", "RENAME", "RESET", "RESTART", "RETURNS", "REVOKE",
      "ROWS", "RULE", "SCHEMA", "SCROLL", "SECOND", "SECURITY", "SEQUENCE", "SERIALIZABLE",
      "SESSION", "SESSION_USER", "SETOF", "SHARE", "SHOW", "SIMPLE", "SMALLINT", "SOME", "STABLE",
      "START", "STATEMENT", "STATISTICS", "STDIN", "STDOUT", "STORAGE", "STRICT", "SUBSTRING",
      "SYSID", "TEMP", "TEMPLATE", "TIME", "TIMESTAMP", "TOAST", "TRAILING", "TREAT", "TRIGGER",
      "TRIM", "TRUE", "TRUNCATE", "TRUSTED", "TYPE", "UNENCRYPTED", "UNKNOWN", "UNLISTEN", "UNTIL",
      "USAGE", "USER", "VACUUM", "VALID", "VALIDATOR", "VARCHAR", "VARYING", "VERBOSE", "VERSION",
      "VIEW", "VOLATILE", "WITH", "WITHOUT", "WORK", "WRITE", "YEAR", "ZONE"};

  /** The Constant FILE_PATTERN_NAME: {@value}. */
  private static final String FILE_PATTERN_NAME = "etlExport_%s%s";

  /**
   * Find by table value with order.
   *
   * @param datasetId the dataset id
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
      recordsCalc(idTableSchema, result, filter, errorList, idRules, fieldSchema, fieldValue,
          datasetId);

      queryOrder(idTableSchema, pageable, sortQueryBuilder, directionQueryBuilder, result, filter,
          errorList, idRules, fieldSchema, fieldValue, datasetId, sortFields);
    }
    return result;
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
   * Find by table value no order optimized.
   *
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @return the list
   */
  /*
   * Find by table value no order optimized.
   *
   * @param idTableSchema the id table schema
   *
   * @param pageable the pageable
   *
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
   * Find ordered native record.
   *
   * @param idTable the id table
   * @param datasetId the dataset id
   * @param pageable the pageable
   * @param filters the filters
   * @return the list
   * @throws HibernateException the hibernate exception
   */
  @Override
  @Transactional
  public List<RecordValue> findOrderedNativeRecord(Long idTable, Long datasetId, Pageable pageable,
      ExportFilterVO filters) throws HibernateException {
    Session session = (Session) entityManager.getDelegate();
    return session.doReturningWork(
        conn -> findByTableValueOrdered(conn, idTable, datasetId, pageable, filters));
  }

  /**
   * Find and generate ETL json.
   *
   * @param datasetId the dataset id
   * @param outputStream the output stream
   * @param tableSchemaId the table schema id
   * @param limit the limit
   * @param offset the offset
   * @param filterValue the filter value
   * @param columnName the column name
   * @param dataProviderCodes the data provider codes
   * @return the string
   * @throws EEAException the EEA exception
   */
  @Override
  public String findAndGenerateETLJson(Long datasetId, OutputStream outputStream,
      String tableSchemaId, Integer limit, Integer offset, String filterValue, String columnName,
      String dataProviderCodes) throws EEAException {
    checkSql(filterValue);
    checkSql(columnName);
    String datasetSchemaId = datasetRepository.findIdDatasetSchemaById(datasetId);
    DataSetSchema datasetSchema = schemasRepository.findById(new ObjectId(datasetSchemaId))
        .orElseThrow(() -> new EEAException(EEAErrorMessage.SCHEMA_NOT_FOUND));
    List<TableSchema> tableSchemaList = datasetSchema.getTableSchemas();
    String tableName = "";

    // create primary json
    JSONObject resultjson = new JSONObject();
    JSONArray tables = new JSONArray();
    if (tableSchemaId != null) {
      tableSchemaList = tableSchemaList.stream()
          .filter(tableSchema -> tableSchema.getIdTableSchema().equals(new ObjectId(tableSchemaId)))
          .collect(Collectors.toList());
    }
    if (offset == 0) {
      offset = 1;
    }

    Map<String, Long> totalRecordsByTableSchema = new HashMap<>();
    // First loop to fill the temporary table according to the filter
    for (TableSchema tableSchema : tableSchemaList) {

      Long totalRecords = getCount(
          totalRecordsQuery(datasetId, tableSchema, filterValue, columnName, dataProviderCodes),
          columnName, filterValue);

      totalRecordsByTableSchema.put(tableSchema.getIdTableSchema().toString(), totalRecords);

      String filterChain = tableSchema.getIdTableSchema().toString();
      if (StringUtils.isNotBlank(tableSchemaId) || StringUtils.isNotBlank(columnName)
          || StringUtils.isNotBlank(filterValue) || StringUtils.isNotBlank(dataProviderCodes)) {
        filterChain =
            filterChain + "_" + Stream.of(tableSchemaId, columnName, filterValue, dataProviderCodes)
                .filter(s -> StringUtils.isNotBlank(s)).collect(Collectors.joining(","));
      }


      if (totalRecords != null && totalRecords > 0L) {

        StringBuilder stringQuery = new StringBuilder();
        stringQuery.append("select ").append("'" + filterChain + "'").append(
            " as filters, cast(records as text) from ( select json_build_object('id_table_schema',id_table_schema,'id_record', id_record, 'countryCode',data_provider_code,'fields',json_agg(fields)) as records from ( ")
            .append(
                " select data_provider_code,id_table_schema,id_record,rdata_position,json_build_object('fieldName',\"fieldName\",'value',value,'field_value_id',field_value_id) as fields from( ")
            .append(" select case ");
        String fieldSchemaQueryPart = " when fv.id_field_schema = '%s' then '%s' ";
        for (FieldSchema field : tableSchema.getRecordSchema().getFieldSchema()) {
          stringQuery.append(
              String.format(fieldSchemaQueryPart, field.getIdFieldSchema(), field.getHeaderName()));
        }
        stringQuery.append(String.format(
            " end as \"fieldName\", fv.value as \"value\", case when fv.\"type\" = 'ATTACHMENT' then fv.id else null end as \"field_value_id\", tv.id_table_schema, rv.id as id_record , rv.data_provider_code, rv.data_position as rdata_position from dataset_%s.field_value fv inner join dataset_%s.record_value rv on fv.id_record = rv.id inner join dataset_%s.table_value tv on tv.id = rv.id_table order by fv.data_position ) fieldsAux",
            datasetId, datasetId, datasetId));
        if (null != tableSchemaId) {
          stringQuery.append(" where ")
              .append(null != tableSchemaId
                  ? String.format(" id_table_schema = '%s' and ", tableSchemaId)
                  : "");
          stringQuery.delete(stringQuery.lastIndexOf("and "), stringQuery.length() - 1);
        }

        stringQuery.append(") records where ");
        if (StringUtils.isNotBlank(dataProviderCodes)) {
          List<String> countryCodesList =
              new ArrayList<>(Arrays.asList(dataProviderCodes.split(",")));
          StringBuilder countries = new StringBuilder();
          for (int i = 0; i < countryCodesList.size(); i++) {
            countries.append("'" + countryCodesList.get(i) + "'");
            if (i + 1 != countryCodesList.size()) {
              countries.append(",");
            }
          }
          stringQuery.append(
              null != countryCodesList ? String.format("data_provider_code in (%s) and ", countries)
                  : "");
        }
        stringQuery.append(String.format(
            "id_table_schema = '%s' group by id_table_schema,id_record,data_provider_code, rdata_position order by rdata_position ",
            tableSchema.getIdTableSchema().toString()));

        if (null != filterValue || null != columnName) {
          stringQuery.append(
              ") as tableAux where exists (select * from jsonb_array_elements(cast(records as jsonb) -> 'fields') as x(o) where ")
              .append(null != columnName ? " x.o ->> 'fieldName' = '" + columnName + "' and " : "")
              .append(null != filterValue ? " x.o ->> 'value' = '" + filterValue + "' and " : "");
          stringQuery.delete(stringQuery.lastIndexOf("and "), stringQuery.length() - 1);
          stringQuery.append(" ) ");
        } else {
          stringQuery.append(" ) tableAux");
        }
        LOG.info("Query: {} ", stringQuery);

        Object resultPosition = null;
        // We need to know which is the first position in the temp table to take the results
        // If there's no position that means we have to import the data from that request
        String queryPosition = "SELECT id from dataset_" + datasetId + ".temp_etlexport "
            + "WHERE filter_value='" + filterChain + "' order by id limit 1";
        Query queryPositionResult = entityManager.createNativeQuery(queryPosition);
        try {
          resultPosition = queryPositionResult.getSingleResult();

        } catch (NoResultException nre) {
          LOG.info("temp table etlexport empty for this filter. Have to fill it");
          fileTempEtlExport(datasetId, stringQuery.toString(), filterChain);
          fileTempEtlImport(datasetId, filterChain);
          resultPosition = queryPositionResult.getSingleResult();
        }
      }
    }


    // Second loop. Now the temp table is filled and we have to take the data
    GsonJsonParser gsonparser = new GsonJsonParser();
    // get json for each table requested
    for (TableSchema tableSchema : tableSchemaList) {

      JSONObject resultTable = new JSONObject();
      tableName = tableSchema.getNameTableSchema();
      resultTable.put("tableName", tableName);
      int nHeaders = tableSchema.getRecordSchema().getFieldSchema().size();
      Long limitAux = (limit / nHeaders > 0 ? Long.valueOf(limit) / ((nHeaders + 1) / 2) : 1) * 2;
      JSONArray tableRecords = new JSONArray();

      Long totalRecords = totalRecordsByTableSchema.get(tableSchema.getIdTableSchema().toString());

      if (StringUtils.isNotBlank(tableSchemaId) || StringUtils.isNotBlank(columnName)
          || StringUtils.isNotBlank(filterValue) || StringUtils.isNotBlank(dataProviderCodes)) {
        resultTable.put("totalRecords", totalRecords);
      }
      Integer offsetAux = (limit * offset) - limit;
      if (offsetAux < 0) {
        offsetAux = 0;
      }

      String filterChain = tableSchema.getIdTableSchema().toString();
      if (StringUtils.isNotBlank(tableSchemaId) || StringUtils.isNotBlank(columnName)
          || StringUtils.isNotBlank(filterValue) || StringUtils.isNotBlank(dataProviderCodes)) {
        filterChain =
            filterChain + "_" + Stream.of(tableSchemaId, columnName, filterValue, dataProviderCodes)
                .filter(s -> StringUtils.isNotBlank(s)).collect(Collectors.joining(","));
      }


      if (totalRecords != null && totalRecords > 0L) {

        Object resultPosition = null;
        Object result = null;


        // We need to know which is the first position in the temp table to take the results
        // If there's no position that means we have to import the data from that request
        String queryPosition = "SELECT id from dataset_" + datasetId + ".temp_etlexport "
            + "WHERE filter_value='" + filterChain + "' order by id limit 1";
        Query queryPositionResult = entityManager.createNativeQuery(queryPosition);
        try {
          resultPosition = queryPositionResult.getSingleResult();
        } catch (NoResultException nre) {
          LOG.info("temp table etlexport empty for this filter");
          break;
        }
        LOG.info("First position in the temp_etlexport {}", resultPosition.toString());

        Long firstPosition = Long.valueOf(resultPosition.toString());
        Long initExtract = (Long.valueOf(offset - 1) * limit) + firstPosition;
        for (Long offsetAux2 = initExtract; offsetAux2 < initExtract + limit
            && offsetAux2 < initExtract + totalRecords; offsetAux2 += limitAux) {
          if (offsetAux2 + limitAux > initExtract + limit) {
            limitAux = initExtract + limit - offsetAux2;
          }

          String queryFromTemp = "SELECT record_json from dataset_" + datasetId + ".temp_etlexport "
              + "WHERE filter_value='" + filterChain + "' and id>= " + offsetAux2 + " and id<"
              + (offsetAux2 + limitAux);
          LOG.info("Partial query from the temp_etlexport table: {}", queryFromTemp);
          Query queryResult = entityManager.createNativeQuery(queryFromTemp);
          try {
            result = queryResult.getResultList();
          } catch (NoResultException nre) {
            LOG.info("no result, ignore message");
          }
          // add to table's records list
          if (result != null) {
            tableRecords.addAll(gsonparser.parseList(result.toString()));
          }
          result = null;
          System.gc();
        }
      }
      resultTable.put("records", tableRecords);
      // add table to resultjson tables list
      tables.add(resultTable);
    }
    resultjson.put("tables", tables);
    System.gc();

    return resultjson.toString();
  }

  /**
   * File temp etl export.
   *
   * @param datasetId the dataset id
   * @param query the query
   * @param filter the filter
   */
  private void fileTempEtlExport(Long datasetId, String query, String filter) {

    ConnectionDataVO connectionDataVO = recordStoreControllerZuul
        .getConnectionToDataset(LiteralConstants.DATASET_PREFIX + datasetId);

    try (Connection con = DriverManager.getConnection(connectionDataVO.getConnectionString(),
        connectionDataVO.getUser(), connectionDataVO.getPassword())) {

      File fileFolder = new File(importPath, "etlExport");
      fileFolder.mkdirs();

      CopyManager cm = new CopyManager((BaseConnection) con);

      // Copy
      String nameFile = importPath + ETL_EXPORT
          + String.format(FILE_PATTERN_NAME, datasetId, "_" + filter + ".snap");
      String copyQueryDataset = "COPY (" + query + ") to STDOUT";
      LOG.info("EtlExport copy query: {}", copyQueryDataset);
      printToFile(nameFile, copyQueryDataset, cm);
    } catch (SQLException | IOException e) {
      LOG_ERROR.error("Error creating a file into the temp_etlexport from dataset {}", datasetId,
          e);
    }
  }

  /**
   * File temp etl import.
   *
   * @param datasetId the dataset id
   * @param filter the filter
   */
  private void fileTempEtlImport(Long datasetId, String filter) {

    ConnectionDataVO connectionDataVO = recordStoreControllerZuul
        .getConnectionToDataset(LiteralConstants.DATASET_PREFIX + datasetId);

    try (
        Connection con = DriverManager.getConnection(connectionDataVO.getConnectionString(),
            connectionDataVO.getUser(), connectionDataVO.getPassword());
        Statement stmt = con.createStatement()) {
      con.setAutoCommit(true);

      CopyManager cm = new CopyManager((BaseConnection) con);

      String nameFile = importPath + ETL_EXPORT
          + String.format(FILE_PATTERN_NAME, datasetId, "_" + filter + ".snap");
      LOG.info("File {} to restore into temp table in dataset {}", nameFile, datasetId);
      String copyQuery =
          "COPY dataset_" + datasetId + ".temp_etlexport(filter_value, record_json) FROM STDIN";
      copyFromFile(copyQuery, nameFile, cm);

      // wait to finish the copy into the temp_table
      Thread.sleep(5000);

    } catch (SQLException | IOException | InterruptedException e) {
      LOG_ERROR.error("Error restoring a file into the temp_etlexport from dataset {}", datasetId,
          e);
    }
  }

  /**
   * Copy from file.
   *
   * @param query the query
   * @param fileName the file name
   * @param copyManager the copy manager
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  private void copyFromFile(String query, String fileName, CopyManager copyManager)
      throws IOException, SQLException {
    Path path = Paths.get(fileName);
    // bufferFile it's a size in bytes defined in consul variable. It can be 65536
    char[] cbuf = new char[65536];
    int len = 0;
    CopyIn cp = copyManager.copyIn(query);
    // Copy the data from the file by chunks
    try (FileReader from = new FileReader(path.toString())) {
      while ((len = from.read(cbuf)) > 0) {
        byte[] buf = new String(cbuf, 0, len).getBytes();
        cp.writeToCopy(buf, 0, buf.length);
      }
      cp.endCopy();
      if (cp.isActive()) {
        cp.cancelCopy();
      }
    } finally {
      Files.deleteIfExists(path);
    }
    LOG.info("File {} imported into the temp_etlexport table", fileName);
  }

  /**
   * Prints the to file.
   *
   * @param fileName the file name
   * @param query the query
   * @param copyManager the copy manager
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void printToFile(String fileName, String query, CopyManager copyManager)
      throws SQLException, IOException {
    byte[] buffer;
    CopyOut copyOut = copyManager.copyOut(query);

    try (OutputStream to = new FileOutputStream(fileName)) {
      while ((buffer = copyOut.readFromCopy()) != null) {
        to.write(buffer);
      }
    } finally {
      if (copyOut.isActive()) {
        copyOut.cancelCopy();
      }
    }
    LOG.info("File {} to restore into the temp_etlexport table", fileName);
  }



  /**
   * Gets the count.
   *
   * @param generatedQuery the generated query
   * @param columnName the column name
   * @param filterValue the filter value
   * @return the count
   */
  private Long getCount(String generatedQuery, String columnName, String filterValue) {
    Query query = entityManager.createNativeQuery(generatedQuery);
    if (null != filterValue) {
      query.setParameter(1, filterValue);
    }
    BigInteger result = (BigInteger) query.setHint(QueryHints.READ_ONLY, true).getSingleResult();
    return result.longValue();
  }

  /**
   * Check sql.
   *
   * @param text the text
   * @return true, if successful
   * @throws EEAException the EEA exception
   */
  private boolean checkSql(String text) throws EEAException {
    List<String> illegalwords = Arrays.asList(RESERVED_SQL_WORDS);
    boolean isAllowed = false;
    if (null != text && !text.isEmpty()) {
      if (!illegalwords.contains(text.toUpperCase())) {
        isAllowed = true;
      } else {
        LOG_ERROR.error("Param {} is illegal.", text);
        throw new EEAException("Unprocessable Entity",
            new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY));
      }
    } else {
      isAllowed = true;
    }
    return isAllowed;
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
   * @return the string
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
    if (fieldSchema != null && StringUtils.isNotBlank(fieldValue)) {
      filter = filter + LIKE_APPEND_QUERY;
    } else if (fieldSchema == null && StringUtils.isNotBlank(fieldValue)) {
      filter = filter + LIKE_APPEND_QUERY_NO_FIELD_SCHEMA;
    }

    return filter;
  }

  /**
   * Records calc.
   *
   * @param idTableSchema the id table schema
   * @param result the result
   * @param filter the filter
   * @param errorList the error list
   * @param idRules the id rules
   * @param fieldSchema the field schema
   * @param fieldValue the field value
   * @param datasetId the dataset id
   */
  private void recordsCalc(String idTableSchema, TableVO result, String filter,
      List<ErrorTypeEnum> errorList, List<String> idRules, String fieldSchema, String fieldValue,
      Long datasetId) {
    if (!filter.isEmpty()) {
      Query query2;
      query2 = entityManager.createQuery(MASTER_QUERY_COUNT + filter);
      query2.setParameter(ID_TABLE_SCHEMA, idTableSchema);
      if (null != idRules && !idRules.isEmpty()) {
        query2.setParameter(RULE_ID_LIST, idRules);
        query2.setParameter(RULE_ID_LIST, idRules);
      }
      if (null != fieldSchema && StringUtils.isNotBlank(fieldValue)) {
        query2.setParameter(FIELD_SCHEMA, fieldSchema);
        query2.setParameter(FIELD_VALUE, fieldValue);
      }
      // Searches in the table occurrences where any column value matches fieldValue
      else if (null == fieldSchema && StringUtils.isNotBlank(fieldValue)) {
        query2.setParameter(FIELD_VALUE, "%" + escapeSpecialCharacters(fieldValue) + "%");
      }
      if (!errorList.isEmpty()) {
        query2.setParameter(ERROR_LIST, errorList);
        query2.setParameter(ERROR_LIST, errorList);
      }
      TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, datasetId));
      Long recordsCount = Long.valueOf(query2.getResultList().get(0).toString());

      LOG.info(
          "Filtering the table in dataset {} by fieldValue as : %{}%, by idTableSchema as {}, by idRules as {}, by errorList as {}, by fieldSchema as {}",
          datasetId, fieldValue, idTableSchema, idRules, errorList, fieldSchema);
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
   * @param errorList the error list
   * @param idRules the id rules
   * @param fieldSchema the field schema
   * @param fieldValue the field value
   * @param datasetId the dataset id
   * @param sortFields the sort fields
   */
  private void queryOrder(String idTableSchema, Pageable pageable, StringBuilder sortQueryBuilder,
      StringBuilder directionQueryBuilder, TableVO result, String filter,
      List<ErrorTypeEnum> errorList, List<String> idRules, String fieldSchema, String fieldValue,
      Long datasetId, SortField... sortFields) {

    // Query without order or with it
    Query query = entityManager.createQuery(
        null == sortFields ? MASTER_QUERY_NO_ORDER + filter + " order by rv.dataPosition"
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
    if (null != fieldSchema && StringUtils.isNotBlank(fieldValue)) {
      query.setParameter(FIELD_SCHEMA, fieldSchema);
      query.setParameter(FIELD_VALUE, fieldValue);
    }
    // Searches in the table occurrences where any column value matches fieldValue
    else if (null == fieldSchema && StringUtils.isNotBlank(fieldValue)) {
      query.setParameter(FIELD_VALUE, "%" + escapeSpecialCharacters(fieldValue) + "%");
    }
    query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
    query.setMaxResults(pageable.getPageSize());

    List<RecordVO> recordVOs = null;
    TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, datasetId));
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
    LOG.info(
        "Filtering the table in dataset {} by fieldValue as : %{}%, by idTableSchema as {}, by idRules as {}, by errorList as {}, by fieldSchema as {}, with PageSize {} and PageNumber {}",
        datasetId, fieldValue, idTableSchema, idRules, errorList, fieldSchema,
        pageable.getPageSize(), pageable.getPageNumber());
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
      String datasetSchemaId = dataSetMetabaseRepository.findDatasetSchemaIdById(datasetId);
      for (SortField field : sortFields) {
        // we check if the field is link and look the linked type to sort in the table
        if (DataType.LINK.equals(field.getTypefield())
            || DataType.EXTERNAL_LINK.equals(field.getTypefield())) {
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
   * Find by table value ordered.
   *
   * @param conn the conn
   * @param tableId the table id
   * @param datasetId the dataset id
   * @param pageable the pageable
   * @param filters the filters
   * @return the list
   * @throws SQLException the SQL exception
   */
  private List<RecordValue> findByTableValueOrdered(Connection conn, Long tableId, Long datasetId,
      Pageable pageable, ExportFilterVO filters) throws SQLException {

    String stringQuery = buildQueryWithExportFilters(datasetId, tableId, pageable, filters);

    int parameterPosition = 1;
    ErrorTypeEnum[] levelErrorsArray = filters.getLevelError();

    conn.setSchema("dataset_" + datasetId);
    List<RecordValue> records = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(stringQuery);) {

      // Parametrize the levelError based on how many LevelErrors are in the enum, CORRECT mustn't
      // be included
      if (filters.getLevelError() != null && filters.getLevelError().length > 0) {
        for (int i = 0; i < levelErrorsArray.length; i++) {
          if (!levelErrorsArray[i].getValue().equals(CORRECT)) {
            stmt.setString(parameterPosition, levelErrorsArray[i].getValue());
            parameterPosition++;
          }
        }
      }
      if (StringUtils.isNotBlank(filters.getIdRules())) {
        stmt.setString(parameterPosition, filters.getIdRules());
        parameterPosition++;
      }
      if (StringUtils.isNotBlank(filters.getFieldValue())) {
        stmt.setString(parameterPosition, "%" + filters.getFieldValue() + "%");
      }
      LOG.info("executing query in findByTableValueOrdered: {}", stmt);
      ResultSet rs = stmt.executeQuery();
      ObjectMapper mapper = new ObjectMapper();
      while (rs.next()) {
        RecordValue record = new RecordValue();
        record.setId(rs.getString("id"));
        record.setIdRecordSchema(rs.getString("id_record_schema"));
        record.setDatasetPartitionId(rs.getLong("dataset_partition_id"));
        record.setDataProviderCode(rs.getString("data_provider_code"));
        String fieldsSerieazlie = rs.getString("fields");
        try {
          if (fieldsSerieazlie != null) {
            record.setFields(Arrays.asList(mapper.readValue(fieldsSerieazlie, FieldValue[].class)));
          }
        } catch (JsonProcessingException e) {
          LOG.error("Json cannot be proccessed for the dataset {} with the error: ", datasetId, e);
        }
        records.add(record);
      }
    }
    return records;
  }

  /**
   * Builds the query with export filters.
   *
   * @param datasetId the dataset id
   * @param tableId the table id
   * @param pageable the pageable
   * @param filters the filters
   * @return the string
   */
  private String buildQueryWithExportFilters(Long datasetId, Long tableId, Pageable pageable,
      ExportFilterVO filters) {

    String initialQuery = buildInitialQueryExportFilters(datasetId, tableId, filters);

    ErrorTypeEnum[] levelErrorsArray = filters.getLevelError();
    boolean errorLevelFilterNotNull = levelErrorsArray != null;
    boolean hasCorrectLevelError =
        searchLevelErrorForCorrect(levelErrorsArray, errorLevelFilterNotNull);


    String filterFieldValue = StringUtils.isNotBlank(filters.getFieldValue())
        ? " and exists (select * from (select jsonb_array_elements as field from jsonb_array_elements(cast(fields as jsonb))) as json_aux where field ->> 'value' LIKE ?)"
        : "";

    String filterByErrorOrIdRule = (errorLevelFilterNotNull && levelErrorsArray.length > 0)
        || StringUtils.isNotBlank(filters.getIdRules()) ? " and " : "";

    String filterByLevelOrError =
        " (exists (select * from (select jsonb_array_elements as field from jsonb_array_elements(cast(fields as jsonb))) as json_aux where field ->> 'id' in (select id_field from id_field_validations)) or id in (select id from record_value_aux) or id_table in (select id from table_value_aux))";

    String filterByCorrectLevelError = hasCorrectLevelError
        ? "(not exists (select * from (select jsonb_array_elements as field from jsonb_array_elements(cast(fields as jsonb))) as json_aux where field ->> 'id' in (select id_field from id_field_validations)) and (id not in (select id from record_value_aux) and id_table not in (select id from table_value_aux)))"
        : "";

    initialQuery = paginateExportWithFilterQuery(pageable, initialQuery);

    initialQuery =
        initialQuery + " where fields notnull" + filterFieldValue + filterByErrorOrIdRule;

    // If we have levelErrors which include the CORRECT filter, add an or to the clause, otherwise,
    // if the filters only have a CORRECT use an and
    if ((errorLevelFilterNotNull && levelErrorsArray.length > 0
        && !(levelErrorsArray.length == 1 && hasCorrectLevelError))
        || (StringUtils.isNotBlank(filters.getIdRules()))) {
      initialQuery = initialQuery + filterByLevelOrError;

    } else if (errorLevelFilterNotNull && levelErrorsArray.length == 1 && hasCorrectLevelError) {
      initialQuery = initialQuery + filterByCorrectLevelError;
    }

    if (errorLevelFilterNotNull && levelErrorsArray.length > 1 && hasCorrectLevelError) {
      initialQuery = initialQuery + " or " + filterByCorrectLevelError;
    }

    return initialQuery;
  }

  /**
   * Paginate export with filter query.
   *
   * @param pageable the pageable
   * @param initialQuery the initial query
   * @return the string
   */
  private String paginateExportWithFilterQuery(Pageable pageable, String initialQuery) {
    String paginationPart = " offset %s limit %s ) as table_aux";

    if (null != pageable && 0 != pageable.getPageNumber() && 0 != pageable.getPageSize()) {
      Integer offsetAux =
          (pageable.getPageSize() * pageable.getPageNumber()) - pageable.getPageSize();
      if (offsetAux < 0) {
        offsetAux = 0;
      }

      initialQuery =
          initialQuery + String.format(paginationPart, offsetAux, pageable.getPageSize());
    } else {
      initialQuery = initialQuery + " ) as table_aux ";
    }
    return initialQuery;
  }

  /**
   * Search level error for correct.
   *
   * @param levelErrorsArray the level errors array
   * @param errorLevelFilterNotNull the error level filter not null
   * @return true, if successful
   */
  private boolean searchLevelErrorForCorrect(ErrorTypeEnum[] levelErrorsArray,
      boolean errorLevelFilterNotNull) {
    boolean hasCorrectLevelError = false;
    // Check if LevelError contains the CORRECT levelError
    if (errorLevelFilterNotNull) {
      for (int i = 0; i < levelErrorsArray.length; i++) {
        if (levelErrorsArray[i].getValue().equals(CORRECT)) {
          hasCorrectLevelError = true;
        }
      }
    }
    return hasCorrectLevelError;
  }

  /**
   * Builds the initial query export filters.
   *
   * @param datasetId the dataset id
   * @param tableId the table id
   * @param filters the filters
   * @return the string
   */
  private String buildInitialQueryExportFilters(Long datasetId, Long tableId,
      ExportFilterVO filters) {

    String levelErrorQuery;
    String idRulesQuery;

    ErrorTypeEnum[] levelErrorsArray = filters.getLevelError();
    boolean errorLevelFilterNotNull = levelErrorsArray != null;
    String idRules = filters.getIdRules();

    if (errorLevelFilterNotNull && levelErrorsArray.length > 0
        && !(levelErrorsArray.length == 1 && levelErrorsArray[0].getValue().equals(CORRECT))) {

      levelErrorQuery = " where level_error in (";

      StringBuilder sb = new StringBuilder(levelErrorQuery);

      parametrizeErrorLevelFilter(filters, sb);

      levelErrorQuery = sb.toString();

      idRulesQuery = StringUtils.isNotBlank(idRules) ? " and v2.id_rule = ?)" : ")";

    } else {
      levelErrorQuery = "";
      idRulesQuery = StringUtils.isNotBlank(idRules) ? " where v2.id_rule = ?)" : ")";
    }

    // Adds the auxiliar tables if there's a LevelError or an idRule
    String auxTables = (errorLevelFilterNotNull && levelErrorsArray.length > 0) || (StringUtils
        .isNotBlank(filters.getIdRules())) ? "with validation_aux as (select * from dataset_"
            + datasetId + ".validation v2" + levelErrorQuery + idRulesQuery
            + ", id_field_validations as (select id_field from validation_aux v inner join dataset_"
            + datasetId + ".field_validation fval on v.id = fval.id_validation), "
            + "id_record_validations as (select id_record from validation_aux v inner join dataset_"
            + datasetId + ".record_validation rv on v.id = rv.id_validation), "
            + "record_value_aux as (select * from dataset_" + datasetId
            + ".record_value rv2 inner join id_record_validations on id_record = rv2.id),"
            + "id_table_validations as (select id_table from validation_aux v inner join dataset_"
            + datasetId + ".table_validation tv2 on v.id = tv2.id_validation),"
            + "table_value_aux as (select * from dataset_" + datasetId
            + ".table_value tv inner join id_table_validations on id_table = tv.id)" : "";

    return auxTables
        + "select * from (select rv.*, (select cast(json_agg(row_to_json(fieldsAux))as text )as fields from "
        + "( select fv.id as id, fv.id_field_schema as \"idFieldSchema\", fv.type as type, fv.value as value from dataset_"
        + datasetId
        + ".field_value fv where fv.id_record = rv.id order by fv.data_position ) as fieldsAux) "
        + " from dataset_" + datasetId + ".record_value rv where rv.id_table= " + tableId
        + " order by rv.data_position";
  }

  /**
   * Parametrize error level filter.
   *
   * @param filters the filters
   * @param sb the sb
   */
  private void parametrizeErrorLevelFilter(ExportFilterVO filters, StringBuilder sb) {
    // Add ? to parametrize the levelError based on how many level errors are in the enum
    for (int i = 0; i < filters.getLevelError().length; i++) {
      if (!filters.getLevelError()[i].getValue().equals(CORRECT)) {
        sb.append("?");
        if (i < filters.getLevelError().length - 1)
          sb.append(",");
      }
    }
    sb.append(")");
  }

  /**
   * Retrieve json with no data.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaList the table schema list
   * @param tableSchemaId the table schema id
   * @param filterValue the filter value
   * @param columnName the column name
   * @return the string
   * @throws EEAException the EEA exception
   */
  @SuppressWarnings("unchecked")
  private String retrieveJsonWithNoData(Long datasetId, String datasetSchemaId,
      List<TableSchema> tableSchemaList, String tableSchemaId, String filterValue,
      String columnName) throws EEAException {
    checkSql(filterValue);
    checkSql(columnName);
    JSONObject tables = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    String tableName = "";
    if (null != tableSchemaList) {
      if (null != tableSchemaId) {
        Document tableSchema = schemasRepository.findTableSchema(datasetSchemaId, tableSchemaId);
        if (tableSchema != null) {
          tableName = (String) tableSchema.get("nameTableSchema");
          JSONObject jsonTable = new JSONObject();
          jsonTable.put(TABLE_NAME, tableName);
          jsonTable.put(TOTAL_RECORDS, getTotalRecordsWithNoData(datasetId, tableSchemaId,
              tableSchemaList, filterValue, columnName));
          jsonTable.put(RECORDS, new JSONArray());
          jsonArray.add(jsonTable);
        }
      } else {
        for (TableSchema tableAux : tableSchemaList) {
          JSONObject jsonTable = new JSONObject();
          jsonTable.put(TABLE_NAME, tableAux.getNameTableSchema());
          jsonTable.put(TOTAL_RECORDS, getTotalRecordsWithNoData(datasetId,
              tableAux.getIdTableSchema().toString(), tableSchemaList, filterValue, columnName));
          jsonTable.put(RECORDS, new JSONArray());
          jsonArray.add(jsonTable);
        }
      }
    }
    tables.put("tables", jsonArray);
    return tables.toString();
  }

  /**
   * Gets the total records with no data.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param tableSchemaList the table schema list
   * @param filterValue the filter value
   * @param columnName the column name
   * @return the total records with no data
   */
  private int getTotalRecordsWithNoData(Long datasetId, String tableSchemaId,
      List<TableSchema> tableSchemaList, String filterValue, String columnName) {

    String totalRecords = "";

    if (null != tableSchemaId) {
      totalRecords = String.format(
          "select count(rv.id) from dataset_%s.record_value rv where  (select tv.id from dataset_%s.table_value tv where tv.id_table_schema = '%s') = rv.id_table ",
          datasetId, datasetId, tableSchemaId);
    }

    if (null != columnName || null != filterValue) {
      totalRecords = resultTotalRecordsQuery(datasetId, tableSchemaList, tableSchemaId, filterValue,
          columnName);
    }

    LOG.info("Query Count: {} ", totalRecords);
    Query query = entityManager.createNativeQuery(totalRecords);

    if (null != columnName && null != filterValue) {
      query.setParameter(1, columnName);
      query.setParameter(2, filterValue);
    } else if (null != columnName && null == filterValue) {
      query.setParameter(1, columnName);
    } else if (null == columnName && null != filterValue) {
      query.setParameter(1, filterValue);
    }

    Object result = null;
    try {
      result = query.setHint(QueryHints.READ_ONLY, true).getSingleResult();
    } catch (NoResultException nre) {
      LOG.info("no result, ignore message");
      result = "0";
    }
    return Integer.parseInt(result.toString());
  }

  /**
   * Result total records query.
   *
   * @param datasetId the dataset id
   * @param tableSchemaList the table schema list
   * @param tableSchemaId the table schema id
   * @param filterValue the filter value
   * @param columnName the column name
   * @return the string
   */
  private String resultTotalRecordsQuery(Long datasetId, List<TableSchema> tableSchemaList,
      String tableSchemaId, String filterValue, String columnName) {
    StringBuilder stringQuery = new StringBuilder();
    stringQuery.append(
        " select count(*)  as \"totalRecords\" from ( select id_table_schema, id_record, json_build_object('countryCode', data_provider_code, 'fields', json_agg(fields)) as records from ( ")
        .append(
            " select data_provider_code, id_table_schema, id_record ,json_build_object('fieldName', \"fieldName\", 'value', value, 'field_value_id', field_value_id) as fields from ( ")
        .append(" select case ");
    String fieldSchemaQueryPart = " when fv.id_field_schema = '%s' then '%s' ";
    for (TableSchema table : tableSchemaList) {
      for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
        stringQuery.append(
            String.format(fieldSchemaQueryPart, field.getIdFieldSchema(), field.getHeaderName()));
      }
    }
    stringQuery.append(String.format(
        " end as \"fieldName\", fv.value as \"value\", case when fv.\"type\" = 'ATTACHMENT' and fv.value != '' then fv.id else null end as \"field_value_id\", tv.id_table_schema, rv.id as id_record , rv.data_provider_code from dataset_%s.field_value fv inner join dataset_%s.record_value rv on fv.id_record = rv.id inner join dataset_%s.table_value tv on tv.id = rv.id_table) fieldsAux",
        datasetId, datasetId, datasetId));

    if (null != tableSchemaId) {
      stringQuery.append(" where ")
          .append(String.format(" id_table_schema like '%s' and ", tableSchemaId));
      stringQuery.delete(stringQuery.lastIndexOf("and "), stringQuery.length() - 1);
    }
    stringQuery.append(") records group by id_table_schema,id_record,data_provider_code ");
    stringQuery.append(" ) tablesAux ");
    if (null != filterValue || null != columnName) {
      stringQuery.append(
          " where exists (select * from jsonb_array_elements(cast(records as jsonb) -> 'fields') as x(o) where ")
          .append(null != columnName ? " x.o ->> 'fieldName' = ? and " : "")
          .append(null != filterValue ? " x.o ->> 'value' = ? and " : "");
      stringQuery.delete(stringQuery.lastIndexOf("and "), stringQuery.length() - 1);
      stringQuery.append(" ) ");
    }
    return stringQuery.toString();
  }

  /**
   * Escape special characters.
   *
   * @param fieldValue the field value
   * @return the string
   */
  private String escapeSpecialCharacters(String fieldValue) {

    /*
     * We're replacing '\\' with '\' because a single '\' works as escape character, so we need '\\'
     * to insert a single '\' that's why we're replacing '\\' with '\\\\' so it replaces '\' with
     * '\\' in the fieldValue and the query works properly
     */

    if (fieldValue.contains("\\"))
      fieldValue = fieldValue.replace("\\", "\\\\");

    return fieldValue;
  }



  /**
   * Total records query.
   *
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   * @param filterValue the filter value
   * @param columnName the column name
   * @param dataProviderCodes the data provider codes
   * @return the string
   */
  private String totalRecordsQuery(Long datasetId, TableSchema tableSchema, String filterValue,
      String columnName, String dataProviderCodes) {

    StringBuilder stringQuery = new StringBuilder();
    String tableSchemaIdString = tableSchema.getIdTableSchema().toString();

    if (null != filterValue || StringUtils.isNotBlank(columnName)) {
      String selectQueryPart1 =
          "with fieldValueAux as (select * from dataset_%s.field_value fv2 where ";
      stringQuery.append(String.format(selectQueryPart1, datasetId));
      if (StringUtils.isNotBlank(columnName)) {
        String selectQueryPart2 = "fv2.id_field_schema = '%s' and ";
        if (tableSchema.getRecordSchema().getFieldSchema().stream()
            .anyMatch(f -> f.getHeaderName().equals(columnName))) {
          String fieldSchemaId = tableSchema.getRecordSchema().getFieldSchema().stream()
              .filter(f -> f.getHeaderName().equals(columnName)).findFirst().get()
              .getIdFieldSchema().toString();
          stringQuery.append(String.format(selectQueryPart2, fieldSchemaId));
        }
      }
      if (null != filterValue) {
        String selectQueryPart3 = "fv2.value = ?) ";
        stringQuery.append(String.format(selectQueryPart3, filterValue));
      } else {
        stringQuery.delete(stringQuery.lastIndexOf("and "), stringQuery.length() - 1);
        stringQuery.append(") ");
      }
    }
    stringQuery.append("select count(rv.id)  as \"totalRecords\" from ");

    String recordQueryPart = "dataset_%s.record_value rv ";
    stringQuery.append(String.format(recordQueryPart, datasetId));

    String firstJoinPart = "inner join dataset_%s.table_value tv on rv.id_table = tv.id ";
    stringQuery.append(String.format(firstJoinPart, datasetId));

    if (null != filterValue || StringUtils.isNotBlank(columnName)) {
      stringQuery.append("inner join fieldValueAux fv on rv.id = fv.id_record ");
    }

    stringQuery.append(" where ");

    if (StringUtils.isNotBlank(tableSchemaIdString)) {
      stringQuery.append(String.format(" id_table_schema = '%s' and ", tableSchemaIdString));
    }

    if (StringUtils.isNotBlank(dataProviderCodes)) {
      List<String> countryCodesList = new ArrayList<>(Arrays.asList(dataProviderCodes.split(",")));
      StringBuilder countries = new StringBuilder();
      for (int i = 0; i < countryCodesList.size(); i++) {
        countries.append("'" + countryCodesList.get(i) + "'");
        if (i + 1 != countryCodesList.size()) {
          countries.append(",");
        }
      }
      stringQuery.append(
          null != countryCodesList ? String.format(" rv.data_provider_code in (%s) ", countries)
              : "");
    } else {
      stringQuery.delete(stringQuery.lastIndexOf("and "), stringQuery.length() - 1);
    }

    LOG.info(stringQuery.toString());
    return stringQuery.toString();
  }

}
