package org.eea.dataset.persistence.data.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.datalake.service.impl.SpatialDataHandlingImpl;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.mapper.DremioRecordMapper;
import org.eea.dataset.mapper.RecordNoValidationMapper;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.util.SortField;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.*;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.eea.utils.LiteralConstants.*;

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

  @Autowired
  private ProcessControllerZuul processControllerZuul;

  @Autowired
  private JobProcessControllerZuul jobProcessControllerZuul;

  @Autowired
  private JobControllerZuul jobControllerZuul;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  @Autowired
  private S3Service s3Service;

  @Autowired
  @Qualifier("dremioJdbcTemplate")
  private JdbcTemplate dremioJdbcTemplate;

  /**
   * The parse common.
   */
  @Lazy
  @Autowired
  private FileCommonUtils fileCommon;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowController.DataFlowControllerZuul dataflowControllerZuul;

  /**
   * The connection url.
   */
  @Value("${spring.datasource.url}")
  private String connectionUrl;

  /**
   * The connection username.
   */
  @Value("${spring.datasource.dataset.username}")
  private String connectionUsername;

  /**
   * The connection password.
   */
  @Value("${spring.datasource.dataset.password}")
  private String connectionPassword;

  /**
   * The connection driver.
   */
  @Value("${spring.datasource.driverClassName}")
  private String connectionDriver;

  /**
   * The path export DL.
   */
  @Value("${exportDLPath}")
  private String exportDLPath;

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
  private static final String FILE_PATTERN_NAME_V2 = "etlExport_%s";
  private static final String ZIP = ".zip";
  private static final String JSON = ".json";

  private int defaultFileExportProcessPriority = 20;

  private static final Integer ETL_EXPORT_MIN_LIMIT = 200000;

  /** The Constant RECORD_COUNT_QUERY: {@value}. */
  private static final String RECORD_COUNT_QUERY = "SELECT count(id) AS recordCount from dataset_%s.temp_etlexport"
      + " WHERE filter_value= ?";

  /** The Constant POSITION_QUERY: {@value}. */
  private static final String POSITION_QUERY = "SELECT id from dataset_%s.temp_etlexport WHERE filter_value= ?"
      + " order by id limit 1";

  /** The Constant RECORD_JSON_QUERY: {@value}. */
  private static final String RECORD_JSON_QUERY = "SELECT record_json from dataset_%s.temp_etlexport "
      + "WHERE filter_value= ? and id>= ? and id< ?";

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
      String dataProviderCodes) throws EEAException, SQLException {
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

        StringBuilder stringQuery = createEtlExportQuery(true, limit, offset, datasetId, tableSchemaId, filterValue, columnName, dataProviderCodes, tableSchema, filterChain);

        // We need to know which is the first position in the temp table to take the results
        // If there's no position that means we have to import the data from that request
        try {

          int recordsTmpExport = queryGetRecordCountbyFilterChain(RECORD_COUNT_QUERY, datasetId, filterChain);
          LOG.info("Table temp_etlexport has {} rows for filterChain {}. Total records : {}", recordsTmpExport, filterChain, totalRecords);

          while (recordsTmpExport != totalRecords) {
            if (recordsTmpExport != 0) {
              do {
                LOG.info("Table temp_etlexport has {} rows for filterChain {}. Total records : {}. Deleting old records", recordsTmpExport, filterChain, totalRecords);
                deleteTempEtlExportByFilterValue(datasetId, filterChain, recordsTmpExport);

                recordsTmpExport = queryGetRecordCountbyFilterChain(RECORD_COUNT_QUERY, datasetId, filterChain);
                LOG.info("Table temp_etlexport has {} rows for filterChain {}. Records stored {}", recordsTmpExport, filterChain, recordsTmpExport);
              } while (recordsTmpExport != 0);
            }
            exportAndImportToEtlExportTable(datasetId, filterChain, stringQuery);
            recordsTmpExport = queryGetRecordCountbyFilterChain(RECORD_COUNT_QUERY, datasetId, filterChain);
          }
        } catch (Exception e) {
          LOG_ERROR.error("Error creating a file into the temp_etlexport from dataset {}", datasetId, e);
          throw e;
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


        // We need to know which is the first position in the temp table to take the results
        // If there's no position that means we have to import the data from that request
        resultPosition = queryGetRecordCountbyFilterChain(POSITION_QUERY, datasetId, filterChain);
        LOG.info("First position in the temp_etlexport {}", resultPosition.toString());

        Long firstPosition = Long.valueOf(resultPosition.toString());
        Long initExtract = (Long.valueOf(offset - 1) * limit) + firstPosition;
        for (Long offsetAux2 = initExtract; offsetAux2 < initExtract + limit
            && offsetAux2 < initExtract + totalRecords; offsetAux2 += limitAux) {
          if (offsetAux2 + limitAux > initExtract + limit) {
            limitAux = initExtract + limit - offsetAux2;
          }

          List recordJsons = queryGetRecordJSONbyFilterChain(RECORD_JSON_QUERY, datasetId, filterChain, offsetAux2, limitAux);

          // add to table's records list
          if (recordJsons != null) {
            tableRecords.addAll(gsonparser.parseList(recordJsons.toString()));
          }
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

  @Override
  public File findAndGenerateETLJsonDL(Long datasetId, String tableSchemaId, Integer limit,
      Integer offset, String filterValue, String columnName, String dataProviderCodes,
      File jsonFile) throws EEAException, SQLException, IOException {
    checkSql(filterValue);
    checkSql(columnName);
    String datasetSchemaId = datasetMetabaseService.findDatasetSchemaIdById(datasetId);
    DataSetSchema datasetSchema = schemasRepository.findById(new ObjectId(datasetSchemaId))
        .orElseThrow(() -> new EEAException(EEAErrorMessage.SCHEMA_NOT_FOUND));
    List<TableSchema> tableSchemaList = datasetSchema.getTableSchemas();

    // create primary json
    if (tableSchemaId != null) {
      tableSchemaList = tableSchemaList.stream()
          .filter(tableSchema -> tableSchema.getIdTableSchema().equals(new ObjectId(tableSchemaId)))
          .collect(Collectors.toList());
    }
    try (FileWriter fw = new FileWriter(jsonFile);
        BufferedWriter bw = new BufferedWriter(fw)) {

      // get json for each table requested
      bw.write("{\"tables\":[");
      for (int i = 0; i< tableSchemaList.size(); i++) {
        TableSchema tableSchema = tableSchemaList.get(i);

        Long totalRecords = getCountDL(totalRecordsQueryDL(datasetId, tableSchema, filterValue, columnName, dataProviderCodes, limit, offset, true));

        if (totalRecords != null && totalRecords > 0L) {
          String query = totalRecordsQueryDL(datasetId, tableSchema, filterValue, columnName, dataProviderCodes, limit, offset, false);
          getAllRecordsDL(query, tableSchema, bw, datasetId);
          bw.write("],\"tableName\":\"" + tableSchema.getNameTableSchema() + "\"");
        }
        if (StringUtils.isNotBlank(tableSchemaId) || StringUtils.isNotBlank(columnName)
            || StringUtils.isNotBlank(filterValue) || StringUtils.isNotBlank(dataProviderCodes)) {
          bw.write(",\"totalRecords\":\"" + totalRecords + "\"");
        }
        if (i == tableSchemaList.size() - 1) {
          bw.write("}");
        } else {
          bw.write("},");
        }
      }
      bw.write("]}");
      bw.flush();
    } catch (Exception e) {
      LOG.error("Error in convert method for jsonOutputFile {} and tableSchemaId {}", jsonFile, tableSchemaId, e);
      throw e;
    }

    return jsonFile;
  }

  private Integer queryGetRecordCountbyFilterChain(String query, Long datasetId, String filterChain)
      throws SQLException {
    Connection connection = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      connection = DriverManager.getConnection(connectionUrl, connectionUsername, connectionPassword);
      query = String.format(query, datasetId);
      LOG.info("Partial query from the temp_etlexport table: {}", query);

      pstmt = connection.prepareStatement(query);
      pstmt.setString(1, filterChain);
      LOG.info("Partial query from the temp_etlexport table: {}", pstmt);

      rs = pstmt.executeQuery();
      rs.next();

      return rs.getInt(1);
    } catch (Exception e) {
      LOG.error(
          "Unexpected error! Error in queryGetRecordCountbyFilterChain for datasetId {} with filter_value {}",
          datasetId, filterChain, e);
    } finally {
      if (rs != null)
        rs.close();
      if (pstmt != null)
        pstmt.close();
      if (connection != null)
        connection.close();
    }

    return null;
  }

  private List queryGetRecordJSONbyFilterChain(String query, Long datasetId, String filterChain,
      Long offsetAux2, Long limitAux)
      throws SQLException {

    Connection connection = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    List list = new ArrayList<>();
    try {
      connection = DriverManager.getConnection(connectionUrl, connectionUsername, connectionPassword);
      query = String.format(query, datasetId);

      pstmt = connection.prepareStatement(query);
      pstmt.setString(1, filterChain);
      pstmt.setLong(2, offsetAux2);
      pstmt.setLong(3, offsetAux2 + limitAux);
      LOG.info("Partial query from the temp_etlexport table: {}", pstmt);

      rs = pstmt.executeQuery();
      while (rs.next()) {
        list.add(rs.getString("record_json"));
      }

    } catch (Exception e) {
      LOG.error(
          "Unexpected error! Error in queryGetRecordCountbyFilterChain for datasetId {} with filter_value {}",
          datasetId, filterChain, e);
    } finally {
      if (rs != null)
        rs.close();
      if (pstmt != null)
        pstmt.close();
      if (connection != null)
        connection.close();
    }

    return list;
  }

  private void exportAndImportToEtlExportTable(Long datasetId, String filterChain, StringBuilder stringQuery) {
    try {
      LOG.info("Export data to snap file for datasetId {} with filter_value {}", datasetId, filterChain);
      fileTempEtlExport(datasetId, stringQuery.toString(), filterChain);

      LOG.info("Exported data successfully. Import data from snap file to temp_etlexport for datasetId {} with filter_value {}", datasetId, filterChain);
      fileTempEtlImport(datasetId, filterChain);

      LOG.info("Import data has been imported successfully from snap file to temp_etlexport for datasetId {} with filter_value {}", datasetId, filterChain);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error in exportAndImportToEtlExportTable for datasetId {} with filter_value {}", datasetId, filterChain, e);
      throw e;
    }
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
    } catch (Exception e) {
      LOG.error("Unexpected error! Error in fileTempEtlExport for query {} and datasetId {}. Message: {}", query, datasetId, e.getMessage());
      throw e;
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
    } catch (Exception e) {
      LOG.error("Unexpected error! Error in fileTempEtlImport for datasetId {}. Message: {}", datasetId, e.getMessage());
      throw e;
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
    } catch (Exception e) {
      LOG_ERROR.error("Unexpected error! Error copying from file {} with query {} Message: {}", fileName, query, e.getMessage());
      throw e;
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
    } catch (Exception e) {
      LOG.error("Unexpected error! Error in printToFile for fileName {} and query {}. Message: {}", fileName, query, e.getMessage());
      throw e;
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


  /** Gets the count DL.
   *
   * @param totalRecords
   * @return
   */
  private Long getCountDL(String totalRecords) {
    return dremioJdbcTemplate.queryForObject(totalRecords, Long.class);
  }

  /** Gets the count DL.
   *
   * @param totalRecords
   * @return
   */
  private void getAllRecordsDL(String totalRecords, TableSchema tableSchema, BufferedWriter bw, Long datasetId)
      throws SQLException, IOException, EEAException {

    DremioRecordMapper recordMapper = new DremioRecordMapper();
    DataSetMetabaseVO dataset = datasetMetabaseService.findDatasetMetabase(datasetId);
    String datasetSchemaId = dataset.getDatasetSchema();
    TableSchemaVO tableSchemaVO = getTableSchemaVO(tableSchema.getIdTableSchema().toString(), datasetSchemaId);
    SpatialDataHandling spatialDataHandling = new SpatialDataHandlingImpl(tableSchemaVO);
    String columnsFormat = "%s %s %s ";
    totalRecords = totalRecords.replace("*", columnsFormat);
    recordMapper.setRecordSchemaVO(tableSchemaVO.getRecordSchema()).setDatasetSchemaId(datasetSchemaId).setTableSchemaId(tableSchemaVO.getIdTableSchema());
    if (spatialDataHandling.geoJsonHeadersAreNotEmpty(true)) {
      totalRecords =  String.format(totalRecords, spatialDataHandling.getSimpleHeaders(), "," ,spatialDataHandling.getGeoJsonHeaders());
    } else {
      totalRecords = String.format(totalRecords, spatialDataHandling.getSimpleHeaders(), org.apache.commons.lang.StringUtils.EMPTY, org.apache.commons.lang.StringUtils.EMPTY);
    }
    List<RecordVO> recordVOS = dremioJdbcTemplate.query(totalRecords, recordMapper);
    bw.write("{\"records\":[");
    for (int i = 0; i < recordVOS.size(); i++) {
      bw.write("{\"fields\":[");
      RecordVO recordVO = recordVOS.get(i);
      int fieldsSize = recordVO.getFields().size();
      for (int j = 0; j < recordVO.getFields().size(); j++) {
        FieldVO fieldVO = recordVO.getFields().get(j);
        bw.write("{\"fieldName\":\"" + fieldVO.getName() + "\",");
        if (fieldVO.getValue().contains("\"")) {
          String noQuotes = fieldVO.getValue().replaceAll("\"", "");
          noQuotes = "\\\"" + noQuotes + "\\\"";
          bw.write("\"value\":\"" + noQuotes + "\",");
        } else {
          bw.write("\"value\":\"" + fieldVO.getValue() + "\",");
        }
        bw.write("\"field_value_id\":\"" + fieldVO.getIdFieldSchema() + "\"");
        if (j == fieldsSize - 1) {
          bw.write("}");
        } else {
          bw.write("},");
        }
      }
      bw.write("],");
      bw.write("\"id_table_schema\":\"" + tableSchemaVO.getIdTableSchema() + "\",");
      bw.write("\"id_record\":\"" + recordVO.getId() + "\",");
      bw.write("\"countryCode\":\"" + recordVO.getDataProviderCode() + "\"");
      if (i == recordVOS.size() - 1) {
        bw.write("}");
      } else {
        bw.write("},");
      }
    }
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
      StringBuilder builder = new StringBuilder(MASTER_QUERY_COUNT);
      query2 = entityManager.createQuery(builder.append(filter).toString());
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
    LOG.info("Dataset id {} tableId {}", datasetId, tableId);

    int parameterPosition = 1;
    ErrorTypeEnum[] levelErrorsArray = filters.getLevelError();

    conn.setSchema("dataset_" + datasetId);
    List<RecordValue> records = new ArrayList<>();

    try (PreparedStatement stmt = conn.prepareStatement(stringQuery)) {

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
    } catch (Exception e) {
      LOG.error("Unexpected error! Error in findByTableValueOrdered for datasetId {} and tableId {}. Message: {}", datasetId, tableId, e.getMessage());
      throw e;
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

    LOG.info("TotalRecordsQuery: {}", stringQuery);
    return stringQuery.toString();
  }

  /**
   * Total records query DL.
   *
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   * @param filterValue the filter value
   * @param columnName the column name
   * @param dataProviderCodes the data provider codes
   * @param getCount get count
   * @return the string
   */
  private String totalRecordsQueryDL(Long datasetId, TableSchema tableSchema, String filterValue,
      String columnName, String dataProviderCodes, Integer limit, Integer offset, boolean getCount) {

    StringBuilder stringQuery = new StringBuilder();

    DataSetMetabaseVO dataset = datasetMetabaseService.findDatasetMetabase(datasetId);
    S3PathResolver s3PathResolver = new S3PathResolver(dataset.getDataflowId(), tableSchema.getNameTableSchema());
    String table = setUpS3PathResolver(datasetId, dataset, s3PathResolver);

    if (getCount) {
      stringQuery.append("select count(record_id) from " + table);
    } else {
      stringQuery.append("select * from " + table);
    }

    if ((StringUtils.isNotEmpty(filterValue) && StringUtils.isNotEmpty(columnName))
        || StringUtils.isNotEmpty(dataProviderCodes)) {
      stringQuery.append(" where ");
      if (StringUtils.isNotEmpty(filterValue) && StringUtils.isNotEmpty(columnName)) {
        stringQuery.append(columnName + " = '"+ filterValue + "' and ");
      }
      if (StringUtils.isNotEmpty(dataProviderCodes)) {
        List<String> countryCodesList = new ArrayList<>(Arrays.asList(dataProviderCodes.split(",")));
        StringBuilder countries = new StringBuilder();
        for (int i = 0; i < countryCodesList.size(); i++) {
          countries.append("'" + countryCodesList.get(i) + "'");
          if (i + 1 != countryCodesList.size()) {
            countries.append(",");
          }
        }
        stringQuery.append(String.format(" data_provider_code in (%s) ", countries));
      } else {
        stringQuery.delete(stringQuery.lastIndexOf(" and "), stringQuery.length() - 1);
      }
    }

    boolean hasLimit = limit != null && limit != 0 && !getCount;
    boolean hasOffset = offset != null && offset != 0 && !getCount;
    boolean hasLimitOrOffset = (hasLimit) || (hasOffset);

    if (hasLimitOrOffset) {
      stringQuery.append(" order by ").append(LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER);
    }
    if (hasLimit) {
      stringQuery.append(" limit ").append(limit);
    }
    if (hasOffset) {
      stringQuery.append(" offset ").append(offset);
    }

    LOG.info("TotalRecords Query: {}", stringQuery);
    return stringQuery.toString();
  }

  /**
   * Truncate dataset by dataset id
   *
   * @param datasetId
   * @return
   */
  @Override
  @Transactional
  public boolean truncateDataset(Long datasetId) {

    LOG.info("Method truncateDataset called for datasetId {}", datasetId);
    boolean deleted;

    try {
      String sql = new StringBuilder("truncate table dataset_").append(datasetId)
              .append(".record_value cascade").toString();

      Query query = entityManager.createNativeQuery(sql);
      entityManager.joinTransaction();
       query.executeUpdate();
       deleted = true;
      LOG.info("Dataset id {} has been truncated with query: {}", datasetId, sql);
    } catch (Exception e) {
      LOG_ERROR.error(
          "Error in method truncateDataset for datasetId {} with error",
          datasetId, e);
      throw e;
    }

    return deleted;
  }

  /** Delete from temp_etlexport by filter_value
   * @param datasetId
   * @param filterValue
   * @param totalCountOfRecords
   */
  @Transactional
  public void deleteTempEtlExportByFilterValue(Long datasetId, String filterValue, int totalCountOfRecords) {
    try {
      LOG.info("Delete totalCountOfRecords {} from table temp_etlexport for datasetId {} with filter_value {}", totalCountOfRecords, datasetId, filterValue);
      String datasetName = "dataset_" + datasetId;
      int loops = (int) Math.ceil(totalCountOfRecords / 100000);
      LOG.info("DatasetId loops {}", loops);
      DriverManagerDataSource dataSource = new DriverManagerDataSource();
      dataSource.setDriverClassName(connectionDriver);
      dataSource.setUrl(connectionUrl);
      dataSource.setUsername(connectionUsername);
      dataSource.setPassword(connectionPassword);
      for (int i = 0; i <= loops; i++) {
        LOG.info("Delete from table temp_etlexport 100.000 records for datasetId {} loop No.: {}", datasetId, i);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        StringBuilder deleteSql = new StringBuilder("WITH rows AS (SELECT id FROM ");
        deleteSql.append(datasetName).append(".temp_etlexport where filter_value = '").append(filterValue).append("' LIMIT 100000) ");
        deleteSql.append("DELETE FROM ");
        deleteSql.append(datasetName).append(".temp_etlexport tmp ");
        deleteSql.append("USING rows WHERE tmp.id = rows.id;");

        jdbcTemplate.update(deleteSql.toString());
        LOG.info("Deleted from table temp_etlexport 100.000 records for datasetId {}", datasetId);
      }
      LOG.info("Delete operation of table temp_etlexport for datasetId {} has finished", datasetId);
    } catch (Exception er) {
      LOG.error("Error executing delete operation of table temp_etlexport for datasetId {} with filter_value {}", datasetId, filterValue, er);
      throw er;
    }
  }

  @Async
  @Override
  public void findAndGenerateETLJsonV3(Long datasetId, String tableSchemaId,
                                  Integer limit, Integer offset, String filterValue, String columnName,
                                  String dataProviderCodes, Long jobId, Long dataflowId, String user, String processUUID) throws EEAException, IOException, SQLException {
    try {
      processControllerZuul.updateProcess(datasetId,dataflowId, ProcessStatusEnum.IN_QUEUE, ProcessTypeEnum.FILE_EXPORT,
              processUUID, user, defaultFileExportProcessPriority, false);
      if (jobId!=null) {
        JobProcessVO jobProcessVO = new JobProcessVO(null, jobId, processUUID);
        jobProcessControllerZuul.save(jobProcessVO);
      }
      processControllerZuul.updateProcess(datasetId,dataflowId, ProcessStatusEnum.IN_PROGRESS, ProcessTypeEnum.FILE_EXPORT,
              processUUID, user, defaultFileExportProcessPriority, false);

      checkSql(filterValue);
      checkSql(columnName);
      String datasetSchemaId = datasetMetabaseService.findDatasetSchemaIdById(datasetId);
      DataSetSchema datasetSchema = schemasRepository.findById(new ObjectId(datasetSchemaId)).orElseThrow(() -> new EEAException(EEAErrorMessage.SCHEMA_NOT_FOUND));

      List<TableSchema> tableSchemaList = datasetSchema.getTableSchemas();
      String tableName = "";

      if (tableSchemaId != null) {
        tableSchemaList = tableSchemaList.stream().filter(tableSchema -> tableSchema.getIdTableSchema().equals(new ObjectId(tableSchemaId)))
                .collect(Collectors.toList());
      }

      String fileName = String.format(FILE_PATTERN_NAME_V2, jobId);
      File fileFolder = new File(importPath, "etlExport");
      fileFolder.mkdirs();
      String filePath = importPath + ETL_EXPORT + fileName;
      String jsonFile = filePath + JSON;
      Integer tableCount = 0;

      DataFlowVO dataFlowVO = dataflowControllerZuul.getMetabaseById(dataflowId);
      if (dataFlowVO.getBigData()) {
        findAndGenerateETLJsonDL(datasetId, tableSchemaId, limit, offset, filterValue,
            columnName, dataProviderCodes, new File(jsonFile));

      } else {
        if (offset == 0) {
          offset = 1;
        }

        for (TableSchema tableSchema : tableSchemaList) {
          tableCount++;
          tableName = tableSchema.getNameTableSchema();
          Long totalRecords = getCount(totalRecordsQuery(datasetId, tableSchema, filterValue, columnName, dataProviderCodes),
              columnName, filterValue);

          String filterChain = tableSchema.getIdTableSchema().toString();
          if (StringUtils.isNotBlank(tableSchemaId) || StringUtils.isNotBlank(columnName) || StringUtils.isNotBlank(filterValue) || StringUtils.isNotBlank(dataProviderCodes)) {
            filterChain = filterChain + "_" + Stream.of(tableSchemaId, columnName, filterValue,
                dataProviderCodes).filter(s -> StringUtils.isNotBlank(s)).collect(Collectors.joining(","));
          }

          createJsonRecordsForTable(datasetId, tableSchemaId, filterValue, columnName,
              dataProviderCodes, tableSchemaList, tableName, tableCount, totalRecords, jsonFile,
              jobId, limit, offset, filterChain, tableSchema);

        }
      }

      createZipFromJson(filePath, datasetId, jobId);
      processControllerZuul.updateProcess(datasetId, dataflowId, ProcessStatusEnum.FINISHED, ProcessTypeEnum.FILE_EXPORT,
              processUUID, user, defaultFileExportProcessPriority, false);
      if (jobId !=null) {
        jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FINISHED);
      }
    } catch (Exception er) {
      processControllerZuul.updateProcess(datasetId, dataflowId, ProcessStatusEnum.CANCELED, ProcessTypeEnum.FILE_EXPORT,
              processUUID, user, defaultFileExportProcessPriority, false);
      if (jobId !=null) {
        jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);
      }
      throw er;
    }
  }

  private void createZipFromJson(String filePath, Long datasetId, Long jobId) throws IOException {
    String jsonFile = filePath+JSON;
    Path path = Paths.get(jsonFile);
    String zipFile = filePath+ZIP;
    LOG.info("Starting creation of FILE_EXPORT file {}, for datasetId {} and jobId {}", zipFile, datasetId, jobId);
    try (ZipOutputStream out =
                 new ZipOutputStream(new FileOutputStream(zipFile));
         FileInputStream fis = new FileInputStream(jsonFile)) {
      ZipEntry entry = new ZipEntry(jobId+JSON);
      out.putNextEntry(entry);
      IOUtils.copyLarge(fis, out);
      LOG.info("Created FILE_EXPORT file {}, for datasetId {} and jobId {}", zipFile, datasetId, jobId);
    } catch (Exception e) {
      LOG.error("Error creating file {} for datasetId {} and jobId {}. Message: ", zipFile, datasetId, jobId, e);
      throw e;
    } finally {
      try {
        Files.deleteIfExists(path);
      } catch (Exception er) {
        LOG.error("Error while deleting file " + path, er);
      }
    }
  }

  private void createJsonRecordsForTable(Long datasetId, String tableSchemaId, String filterValue, String columnName, String dataProviderCodes, List<TableSchema> tableSchemaList, String tableName,
                                                Integer tableCount, Long totalRecords, String jsonFile, Long jobId, Integer limit, Integer offset, String filterChain, TableSchema tableSchema) throws IOException, SQLException {
    try (FileWriter bw = new FileWriter(jsonFile, true)) {
      LOG.info("Starting creation of json file {} for datasetId {} and jobId {}", jsonFile, datasetId, jobId);
      if (tableCount == 1) {
        bw.write("{\n\"tables\": [\n");
      }
      bw.write("{");
      if (StringUtils.isNotBlank(tableSchemaId) || StringUtils.isNotBlank(columnName)
              || StringUtils.isNotBlank(filterValue) || StringUtils.isNotBlank(dataProviderCodes)) {
        bw.write("\"totalRecords\":");
        bw.write(totalRecords.toString());
        bw.write(",\n");
      }
      bw.write("\"tableName\":");
      bw.write("\"" + tableName + "\"");
      bw.write(",\n");
      bw.write("\"records\": [\n");
      if (totalRecords > 0) {
        if (limit == null) {
          limit = totalRecords.intValue();
        }
        Integer initExtract = (offset - 1) * limit;
        Integer limitAux = ETL_EXPORT_MIN_LIMIT;
        Integer recordCount = 0;
        CopyOut copyOut;
        CopyManager copyManager;
        byte[] buffer;
        for (Integer offsetAux2 = initExtract; offsetAux2 < initExtract + limit
                && offsetAux2 < initExtract + totalRecords; offsetAux2 += limitAux) {
          if (offsetAux2 + limitAux > initExtract + limit) {
            limitAux = initExtract + limit - offsetAux2;
          }
          StringBuilder stringQuery = createEtlExportQuery(false, limitAux, offsetAux2, datasetId, tableSchemaId, filterValue, columnName, dataProviderCodes, tableSchema, filterChain);
          String formattedQuery = "COPY (" + stringQuery + ") to STDOUT";
          try (Connection con = DriverManager.getConnection(connectionUrl, connectionUsername, connectionPassword)) {
            copyManager = new CopyManager((BaseConnection) con);
            copyOut = copyManager.copyOut(formattedQuery);
            while ((buffer = copyOut.readFromCopy()) != null) {
              if (recordCount != 0) {
                bw.write(",");
              }
              bw.write("\n");
              bw.write(new String(buffer, StandardCharsets.UTF_8));
              if (recordCount==0) {
                recordCount++;
              }
            }
            bw.flush();
            System.gc();
          } catch (Exception e) {
            throw e;
          }
        }
      }
      bw.write("\n");
      bw.write("]\n");
      bw.write("\n}");
      if (tableCount < tableSchemaList.size()) {
        bw.write(",");
      }
      bw.write("\n");
      if (tableCount == tableSchemaList.size()) {
        bw.write("]\n" + "}");
      }
      System.gc();
      LOG.info("Created json file {} for datasetId {} and jobId {}", jsonFile, datasetId, jobId);
    } catch (Exception e) {
      LOG.error("Error writing file {} for datasetId {} and jobId {}. Message: ", jsonFile, datasetId, jobId, e);
      throw e;
    }
  }

  private static StringBuilder createEtlExportQuery(boolean useTempTable,  Integer limit, Integer offset, Long datasetId, String tableSchemaId, String filterValue,
                                                    String columnName, String dataProviderCodes, TableSchema tableSchema, String filterChain) {
    StringBuilder stringQuery = new StringBuilder();
    stringQuery.append("select ");
    if (useTempTable) {
      stringQuery.append("'" + filterChain + "'").append(
              " as filters, ");
    }
    stringQuery.append(
                    "cast(records as text) from ( select json_build_object('id_table_schema',id_table_schema,'id_record', id_record, 'countryCode',data_provider_code,'fields',json_agg(fields)) as records from ( ")
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
    if (!useTempTable) {
      if (limit!=null && limit!=0) {
        stringQuery.append(" limit ").append(limit);
      }
      if (offset!=null && offset!=0 && offset!=1) {
        stringQuery.append(" offset ").append(offset);
      }
    }
    return stringQuery;
  }

  @Override
  public Long countByTableSchema(Long datasetId, String idTableSchema) throws SQLException {
    Connection connection = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      String query = "select count(r.id) from dataset_%s.record_value r, dataset_%s.table_value t where "
          + "t.id = r.id_table and t.id_table_schema=?";

      ConnectionDataVO connectionDataVO = recordStoreControllerZuul
          .getConnectionToDataset(LiteralConstants.DATASET_PREFIX + datasetId);

      connection = DriverManager.getConnection(connectionDataVO.getConnectionString(),
          connectionDataVO.getUser(), connectionDataVO.getPassword());
      query = String.format(query, datasetId, datasetId);
      LOG.info("countByTableSchema query: {}", query);

      pstmt = connection.prepareStatement(query);
      pstmt.setString(1, idTableSchema);
      LOG.info("countByTableSchema query ps: {}", pstmt);

      rs = pstmt.executeQuery();
      rs.next();

      return rs.getLong(1);
    } catch (Exception e) {
      LOG.error(
          "Unexpected error! Error in countByTableSchema for datasetId {} with filter_value {}",
          datasetId, e);
    } finally {
      if (rs != null)
        rs.close();
      if (pstmt != null)
        pstmt.close();
      if (connection != null)
        connection.close();
    }

    return 0L;
  }


  private String setUpS3PathResolver(Long datasetId, DataSetMetabaseVO dataset, S3PathResolver s3PathResolver) {
    String path = null;
    switch (dataset.getDatasetTypeEnum()) {
      case REPORTING:
        s3PathResolver.setPath(S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3PathResolver.setDataProviderId(dataset.getDataProviderId());
        s3PathResolver.setDatasetId(datasetId);
        path = s3Service.getS3Path(s3PathResolver);
        break;
      case DESIGN:
        s3PathResolver.setPath(S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3PathResolver.setDataProviderId(0L);
        s3PathResolver.setDatasetId(datasetId);
        path = s3Service.getS3Path(s3PathResolver);
        break;
      case COLLECTION:
        s3PathResolver.setDatasetId(datasetId);
        path = s3Service.getTableDCAsFolderQueryPath(s3PathResolver, S3_TABLE_NAME_DC_QUERY_PATH);
        break;
      case TEST:
        break;
      case EUDATASET:
        break;
      case REFERENCE:
        path = s3Service.getTableDCAsFolderQueryPath(s3PathResolver, S3_DATAFLOW_REFERENCE_QUERY_PATH);
        break;
      default:
        LOG.info("Dataset Type does not exist!");
        break;
    }

    LOG.info("Method setUpS3PathResolver returns : {}", path);
    return path;
  }

  /**
   * finds tableSchemaVO
   * @param idTableSchema
   * @param datasetSchemaId
   * @return
   * @throws EEAException
   */
  private TableSchemaVO getTableSchemaVO(String idTableSchema, String datasetSchemaId) throws EEAException {
    DataSetSchemaVO dataSetSchemaVO;
    try {
      dataSetSchemaVO = fileCommon.getDataSetSchemaVO(datasetSchemaId);
    } catch (EEAException e) {
      LOG.error("Error retrieving dataset schema for datasetSchemaId {}", datasetSchemaId);
      throw new EEAException(e);
    }
    Optional<TableSchemaVO> tableSchemaOptional = dataSetSchemaVO.getTableSchemas().stream().filter(t -> t.getIdTableSchema().equals(idTableSchema)).findFirst();
    TableSchemaVO tableSchemaVO = null;
    if (!tableSchemaOptional.isPresent()) {
      LOG.error("table with id {} not found in mongo results", idTableSchema);
      throw new EEAException("Error retrieving table with id " + idTableSchema);
    }
    tableSchemaVO = tableSchemaOptional.get();
    return tableSchemaVO;
  }
}
