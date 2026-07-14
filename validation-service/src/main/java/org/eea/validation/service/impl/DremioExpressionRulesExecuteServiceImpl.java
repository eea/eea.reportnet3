package org.eea.validation.service.impl;

import cdjd.org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.exception.DremioValidationException;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.dto.dataset.schemas.rule.RuleExpressionDTO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.validation.TaskVO;
import org.eea.lock.redis.RedisLockService;
import org.eea.utils.UtilityClass;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.metabase.domain.Task;
import org.eea.validation.persistence.data.metabase.repository.TaskRepository;
import org.eea.validation.service.DremioRulesExecuteService;
import org.eea.validation.service.DremioRulesService;
import org.eea.validation.service.RulesService;
import org.eea.validation.util.RuleOperators;
import org.eea.validation.util.TaskJsonUtils;
import org.eea.validation.util.ValidationHelper;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static org.eea.utils.LiteralConstants.*;

@ImportDataLakeCommons
@Service
public class DremioExpressionRulesExecuteServiceImpl implements DremioRulesExecuteService {

    @Value("${parquet.file.path}")
    private String parquetFilePath;
    @Value("${validation.parquet.max.file.size}")
    private Integer validationParquetMaxFileSize;
    @Value("${validation.split.parquet}")
    private boolean validationSplitParquet;

    /** The max errors. */
    @Value(value = "${validation.maximumErrors}")
    private int maxErrors;
    private JdbcTemplate dremioJdbcTemplate;
    private S3Service s3Service;
    private RulesService rulesService;
    private DatasetSchemaControllerZuul datasetSchemaControllerZuul;
    private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;
    private DremioRulesService dremioRulesService;
    private RepresentativeControllerZuul representativeControllerZuul;
    private S3Helper s3Helper;
    private DremioHelperService dremioHelperService;
    private ValidationHelper validationHelper;
    private TaskRepository taskRepository;
    private RedisLockService redisLockService;

    private static final Logger LOG = LoggerFactory.getLogger(DremioExpressionRulesExecuteServiceImpl.class);
    private static final String RECORD_IF_THEN = "recordIfThen";
    private static final String CLASS_RECORD_IF_THEN = "RuleOperators.recordIfThen";
    private  static final String RECORD_AND = "recordAnd";
    private static final String RECORD_OR = "recordOr";
    private static final String FIELD_AND = "fieldAnd";
    private static final String FIELD_OR = "fieldOr";
    private static final String RULE_OPERATORS = "org.eea.validation.util.RuleOperators";
    private static final String RECORD = "Record";
    private static final String NUMBER = "Number";
    private static final String LENGTH = "Length";
    private static final String DAY = "Day";
    private static final String MONTH = "Month";
    private static final String YEAR = "Year";
    private static final String COMMA = ",";
    private static final String OPEN_BRACKET = "[";
    private static final String SINGLE_QUOTE ="\'";
    private static final String DOUBLE_QUOTE ="\"";

    @Autowired
    public DremioExpressionRulesExecuteServiceImpl(@Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, S3Service s3Service, RulesService rulesService, DatasetSchemaControllerZuul datasetSchemaControllerZuul,
                                                   DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul, DremioRulesService dremioRulesService, RepresentativeControllerZuul representativeControllerZuul,
                                                   S3Helper s3Helper, DremioHelperService dremioHelperService, ValidationHelper validationHelper,
                                                   TaskRepository taskRepository, RedisLockService redisLockService) {
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.s3Service = s3Service;
        this.rulesService = rulesService;
        this.datasetSchemaControllerZuul = datasetSchemaControllerZuul;
        this.dataSetMetabaseControllerZuul = dataSetMetabaseControllerZuul;
        this.dremioRulesService = dremioRulesService;
        this.representativeControllerZuul = representativeControllerZuul;
        this.s3Helper = s3Helper;
        this.dremioHelperService = dremioHelperService;
        this.validationHelper = validationHelper;
        this.taskRepository = taskRepository;
        this.redisLockService = redisLockService;
    }

    @Override
    public void execute(Long dataflowId, Long datasetId, String datasetSchemaId, String tableName, String tableSchemaId, String ruleId, Long dataProviderId,
                        Long taskId, boolean createParquetWithSQL, String preparationCode, boolean useViews) throws DremioValidationException {
        try {
            //TODO Fix preparationCode
            //if the dataset to validate is of reference type, then the table path should be changed
            S3PathResolver dataTableResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, tableName);
            dataTableResolver.setPreparationCode(preparationCode);
            DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
            String path;
            if (dataset.getDatasetTypeEnum().equals(DatasetTypeEnum.REFERENCE)) {
                path = S3_DATAFLOW_REFERENCE_QUERY_PATH;
            } else if (StringUtils.isNotBlank(preparationCode)) {
                path = S3_PREPARATION_TABLE_AS_FOLDER_QUERY_PATH;
            } else {
                path = S3_TABLE_AS_FOLDER_QUERY_PATH;
            }
            String tablePath = s3Service.getTableAsFolderQueryPath(dataTableResolver, path);
            String numberOfRecordsQuery = "SELECT COUNT (*) FROM " + tablePath;
            Long rowCount = dremioJdbcTemplate.queryForObject(numberOfRecordsQuery, Long.class);
            if (rowCount == 0) {
                return;
            }

            S3PathResolver validationResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, S3_VALIDATION);
            validationResolver.setPreparationCode(preparationCode);

            String providerCode = getProviderCode(dataset);
            // If validateAsProviderCode exists in task parameters, it must drive macro replacement in RuleOperators unless providerCode != null.
            String override = resolveValidateAsProviderCodeFromTask(taskId);
            if (override != null && (providerCode == null || providerCode.isBlank())) {
                providerCode = override;
            }

            StringBuilder query = new StringBuilder();
            RuleVO ruleVO = rulesService.findRule(datasetSchemaId, ruleId);
            deleteRuleFolderIfExists(validationResolver, ruleVO);

            List<Object> parameters = new ArrayList<>();

            String fieldName = getFieldName(datasetSchemaId, tableSchemaId, ruleVO);

            String fileName = datasetId + UNDERSCORE + tableName + UNDERSCORE + ruleVO.getShortCode();
            Map<String, List<String>> headerNames = new HashMap<>();  //map of method as key and list of field names (that exist as parameters in method) as values

            query.append("select record_id");
            if (!fieldName.equals("")) {
                query.append(COMMA).append(UtilityClass.addQuotesToFieldNames(fieldName));
            }
            Map<String, String> fieldSchemaIdNameMap = new HashMap<>();
            createHeaders(datasetSchemaId, query, parameters, fieldName, headerNames, ruleVO.getWhenCondition(), fieldSchemaIdNameMap);

            query.append(" from ").append(s3Service.getTableAsFolderQueryPath(dataTableResolver, path));
            SqlRowSet rs = dremioJdbcTemplate.queryForRowSet(query.toString());

            Task task = taskRepository.findById(taskId).orElse(null);

            runRuleAndCreateParquet(createParquetWithSQL, providerCode, ruleVO, fieldName, fileName, headerNames, rs,  dataTableResolver, validationResolver, fieldSchemaIdNameMap, task, preparationCode);
        } catch (Exception e1) {
            LOG.error("Error creating validation folder for ruleId {}, datasetId {} and taskId {},{}", ruleId, datasetId, taskId, e1.getMessage());
            throw new DremioValidationException(e1.getMessage());
        }
    }

    /**
     * Deletes rule folder if exists
     * @param validationResolver
     * @param ruleVO
     */
    private void deleteRuleFolderIfExists(S3PathResolver validationResolver, RuleVO ruleVO) {
        int ruleIdLength = ruleVO.getRuleId().length();
        String ruleFolderName = ruleVO.getShortCode() + DASH + ruleVO.getRuleId().substring(ruleIdLength-3, ruleIdLength);
        validationResolver.setFilename(ruleFolderName);
        //TODO Check what happens with this path
        if (StringUtils.isNotBlank(validationResolver.getPreparationCode())) {
            s3Helper.deleteFolder(validationResolver, S3_PREPARATION_TABLE_NAME_PATH);
        }
        else {
            s3Helper.deleteFolder(validationResolver, S3_TABLE_NAME_PATH);
        }

    }

    /**
     * Runs rule and creates parquet in S3
     * @param createParquetWithSQL
     * @param providerCode
     * @param ruleVO
     * @param fieldName
     * @param headerNames
     * @param rs
     * @param dataTableResolver
     * @param validationResolver
     * @param fieldSchemaIdNameMap
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IOException
     */
    private void runRuleAndCreateParquet(boolean createParquetWithSQL, String providerCode, RuleVO ruleVO, String fieldName, String fileName, Map<String, List<String>> headerNames, SqlRowSet rs,
                                         S3PathResolver dataTableResolver, S3PathResolver validationResolver, Map<String, String> fieldSchemaIdNameMap, Task task, String preparationCode) throws Exception {
        Class<?> cls = Class.forName(RULE_OPERATORS);
        Method factoryMethod = cls.getDeclaredMethod(GET_INSTANCE);
        Object object = factoryMethod.invoke(null, null);
        if (createParquetWithSQL) {
            int count = 0;
            boolean createRuleFolder = false;
            //if the dataset to validate is of reference type, then the validation path should be changed
            StringBuilder validationQuery = dremioRulesService.getS3RuleFolderQueryBuilder(dataTableResolver.getDatasetId(), dataTableResolver.getTableName(), dataTableResolver, validationResolver, ruleVO, fieldName);
            while (rs.next()) {
                boolean isValid = isRecordValid(providerCode, ruleVO, fieldName, headerNames, rs, cls, object, fieldSchemaIdNameMap);
                if (!isValid) {
                    //We run again the same validation for if-then cases because we found a bug on ticket #285141
                    if (ruleVO.getWhenConditionMethod().contains(CLASS_RECORD_IF_THEN)) {
                        isValid = isRecordValid(providerCode, ruleVO, fieldName, headerNames, rs, cls, object, fieldSchemaIdNameMap);
                    }
                    if (!isValid) {
                        if (count != 0) {
                            validationQuery.append(",'");
                        }
                        validationQuery.append(rs.getString(PARQUET_RECORD_ID_COLUMN_HEADER)).append("'");
                        if (count == 0) {
                            count++;
                            createRuleFolder = true;
                        }
                    }
                }
            }
            if (createRuleFolder) {
                boolean blocker = TaskJsonUtils.isBlockerTask(task.getJson());
                if (blocker) {
                    LOG.info("Adding blocker for dataset " + TaskJsonUtils.getDatasetId(task.getJson())
                            + " process id "  + TaskJsonUtils.getProcessId(task.getJson())
                            + " preparationCode "  + preparationCode);
                    redisLockService.setBlocker(TaskJsonUtils.getDatasetId(task.getJson()),
                            TaskJsonUtils.getProcessId(task.getJson()), preparationCode);
                }

                validationQuery.append("))");
                dremioHelperService.executeSqlStatement(validationQuery.toString());
            }
        } else {
            Map<String, String> headerMap = dremioRulesService.createValidationParquetHeaderMap(dataTableResolver.getDatasetId(), dataTableResolver.getTableName(), ruleVO, fieldName);
            createParquetAndUploadToS3(fileName, providerCode, ruleVO, fieldName, headerMap, headerNames, rs, cls, object, validationResolver, fieldSchemaIdNameMap);
        }
    }

    /**
     * Creates parquet and uploads it to S3
     * @param fileName
     * @param providerCode
     * @param ruleVO
     * @param fieldName
     * @param headerMap
     * @param headerNames
     * @param rs
     * @param cls
     * @param object
     * @param validationResolver
     * @param fieldSchemaIdNameMap
     * @throws Exception
     */
    private void createParquetAndUploadToS3(String fileName, String providerCode, RuleVO ruleVO, String fieldName, Map<String, String> headerMap, Map<String, List<String>> headerNames,
                                            SqlRowSet rs, Class<?> cls, Object object, S3PathResolver validationResolver, Map<String, String> fieldSchemaIdNameMap) throws Exception {
        if (validationSplitParquet) {
            createSplitParquetFilesAndUploadToS3(fileName, providerCode, ruleVO, fieldName, headerMap, headerNames, rs, cls, object, validationResolver, fieldSchemaIdNameMap);
        } else {
            createParquetFileAndUploadToS3(fileName, providerCode, ruleVO, fieldName, headerMap, headerNames, rs, cls, object, validationResolver, fieldSchemaIdNameMap);
        }
    }

    /**
     * Creates split parquet files
     * @param fileName
     * @param providerCode
     * @param ruleVO
     * @param fieldName
     * @param headerMap
     * @param headerNames
     * @param rs
     * @param cls
     * @param object
     * @param validationResolver
     * @param fieldSchemaIdNameMap
     * @throws Exception
     */
    private void createSplitParquetFilesAndUploadToS3(String fileName, String providerCode, RuleVO ruleVO, String fieldName, Map<String, String> headerMap, Map<String, List<String>> headerNames,
                                                      SqlRowSet rs, Class<?> cls, Object object, S3PathResolver validationResolver, Map<String, String> fieldSchemaIdNameMap) throws Exception {
        List<String> recordIds = new ArrayList<>();
        Schema schema = getSchema();
        while (rs.next()) {
            boolean isValid = isRecordValid(providerCode, ruleVO, fieldName, headerNames, rs, cls, object, fieldSchemaIdNameMap);
            if (!isValid) {
                //We run again the same validation for if-then cases because we found a bug on ticket #285141
                if (ruleVO.getWhenConditionMethod().contains(CLASS_RECORD_IF_THEN)) {
                    isValid = isRecordValid(providerCode, ruleVO, fieldName, headerNames, rs, cls, object, fieldSchemaIdNameMap);
                }
                if (!isValid) {
                    recordIds.add(rs.getString(PARQUET_RECORD_ID_COLUMN_HEADER));
                    if (recordIds.size() == maxErrors) break;
                }
            }
        }
        int count = 1;
        for (List<String> recordSubList : Lists.partition(recordIds, validationParquetMaxFileSize)) {
            String subFile = fileName + "_" + count + PARQUET_TYPE;
            String parquetFile = parquetFilePath + subFile;
            try {
                dremioHelperService.deleteFileFromR3IfExists(parquetFile);
                try (ParquetWriter<GenericRecord> writer = AvroParquetWriter
                        .<GenericRecord>builder(new Path(parquetFile))
                        .withSchema(schema)
                        .withCompressionCodec(CompressionCodecName.SNAPPY)
                        .build()) {

                    for (String recordId : recordSubList) {
                        GenericRecord record = createParquetGenericRecord(headerMap, recordId, schema);
                        writer.write(record);
                    }
                } catch (Exception e1) {
                    LOG.error("Error creating parquet file {},{]", parquetFile, e1.getMessage());
                    throw e1;
                }
                validationHelper.uploadValidationParquetToS3(ruleVO, validationResolver, subFile, ruleVO.getRuleId().length(), parquetFile);
                count++;
            } finally {
                dremioHelperService.deleteFileFromR3IfExists(parquetFile);
            }
        }
    }

    /**
     * Creates parquet file
     * @param fileName
     * @param providerCode
     * @param ruleVO
     * @param fieldName
     * @param headerMap
     * @param headerNames
     * @param rs
     * @param cls
     * @param object
     * @param validationResolver
     * @param fieldSchemaIdNameMap
     * @throws Exception
     */
    private void createParquetFileAndUploadToS3(String fileName, String providerCode, RuleVO ruleVO, String fieldName, Map<String, String> headerMap, Map<String, List<String>> headerNames,
                                                SqlRowSet rs, Class<?> cls, Object object, S3PathResolver validationResolver, Map<String, String> fieldSchemaIdNameMap) throws Exception {
        Schema schema = getSchema();
        String file = fileName + PARQUET_TYPE;
        String parquetFile = parquetFilePath + file;
        dremioHelperService.deleteFileFromR3IfExists(parquetFile);
        try (ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(new Path(parquetFile)).withSchema(schema)
                .withCompressionCodec(CompressionCodecName.SNAPPY).withPageSize(4 * 1024).withRowGroupSize(16 * 1024).build()) {
            int parquetRecordCount = 0;
            while (rs.next()) {
                boolean isValid = isRecordValid(providerCode, ruleVO, fieldName, headerNames, rs, cls, object, fieldSchemaIdNameMap);
                if (!isValid) {
                    //We run again the same validation for if-then cases because we found a bug on ticket #285141
                    if (ruleVO.getWhenConditionMethod().contains(CLASS_RECORD_IF_THEN)) {
                        isValid = isRecordValid(providerCode, ruleVO, fieldName, headerNames, rs, cls, object, fieldSchemaIdNameMap);
                    }
                    if (!isValid) {
                        if (parquetRecordCount==0) {
                            parquetRecordCount++;
                        }
                        GenericRecord record = createParquetGenericRecord(headerMap, rs.getString(PARQUET_RECORD_ID_COLUMN_HEADER), schema);
                        writer.write(record);
                    }
                }
            }
            if (parquetRecordCount > 0) {
                writer.close();
                validationHelper.uploadValidationParquetToS3(ruleVO, validationResolver, file, ruleVO.getRuleId().length(), parquetFile);
            }
        } catch (Exception e) {
            LOG.error("Error creating parquet file {}", parquetFile);
            throw e;
        } finally {
            dremioHelperService.deleteFileFromR3IfExists(parquetFile);
        }
    }

    /**
     * Gets parquet schema
     * @return
     */
    private static Schema getSchema() {
        List<String> parquetHeaders = Arrays.asList(PK, PARQUET_RECORD_ID_COLUMN_HEADER, VALIDATION_LEVEL, VALIDATION_AREA, MESSAGE, TABLE_NAME, FIELD_NAME, DATASET_ID, QC_CODE, ID_RULE);
        List<Schema.Field> fields = new ArrayList<>();
        for (String header : parquetHeaders) {
            fields.add(new Schema.Field(header, Schema.create(Schema.Type.STRING), null, null));
        }
        Schema schema = Schema.createRecord("Data", null, null, false, fields);
        return schema;
    }

    /**
     * creates parquet generic record
     * @param headerMap
     * @param recordId
     * @param schema
     * @return
     */
    private GenericRecord createParquetGenericRecord(Map<String, String> headerMap, String recordId, Schema schema) {
        GenericRecord genericRecord = new GenericData.Record(schema);
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            genericRecord.put(entry.getKey(), entry.getValue());
        }
        genericRecord.put(PK, UUID.randomUUID().toString());
        genericRecord.put(PARQUET_RECORD_ID_COLUMN_HEADER, recordId);
        return genericRecord;
    }

    /**
     * Checks if record is valid
     * @param providerCode
     * @param ruleVO
     * @param fieldName
     * @param headerNames
     * @param rs
     * @param cls
     * @param object
     * @param fieldSchemaIdNameMap
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private boolean isRecordValid(String providerCode, RuleVO ruleVO, String fieldName, Map<String, List<String>> headerNames, SqlRowSet rs, Class<?> cls, Object object, Map<String, String> fieldSchemaIdNameMap)
            throws IllegalAccessException, InvocationTargetException {
        List<Object> parameters;

        //remove "" "" added to field name for safe sql expressions
        //fieldName = dremioHelperService.removeQuotesFromFieldNames(fieldName);

        RuleExpressionDTO ruleExpressionDTO = ruleVO.getWhenCondition();
        String ruleMethodName = ruleExpressionDTO.getOperator().getFunctionName();
        boolean isValid = false;
        AtomicBoolean nestedExpression = new AtomicBoolean(false);
        Method method = dremioRulesService.getRuleMethodFromClass(ruleMethodName, cls);
        List<Object> internalResults = new ArrayList<>();
        AtomicBoolean record = new AtomicBoolean(false);
        executeRule(providerCode, ruleVO, fieldName, headerNames, rs, cls, object, nestedExpression, internalResults, record, fieldSchemaIdNameMap);

        if (internalResults.size()>0 && nestedExpression.get()) {
            if (internalResults.size() == 1) {
                    isValid = (boolean) method.invoke(object, internalResults.get(0));
            } else if (internalResults.size() == 2) {
                    isValid = (boolean) method.invoke(object, internalResults.get(0), internalResults.get(1));
            }
        } else {
            record.set(isRecord(method.getName()));
            parameters = ruleExpressionDTO.getParams();
            isValid = (Boolean) getMethodExecutionResult(ruleVO, fieldName, headerNames, rs, object, record.get(), method.getName(), method, parameters, providerCode, fieldSchemaIdNameMap);
        }
        return isValid;
    }

    /**
     * Gets field name
     * @param datasetSchemaId
     * @param tableSchemaId
     * @param ruleVO
     * @return
     */
    private String getFieldName(String datasetSchemaId, String tableSchemaId, RuleVO ruleVO) {
        String fieldName;
        if (ruleVO.getType().equals(EntityTypeEnum.FIELD)) {
            fieldName = datasetSchemaControllerZuul.getFieldName(datasetSchemaId, tableSchemaId, new ArrayList<>(), ruleVO.getReferenceId(), ruleVO.getReferenceFieldSchemaPKId());
        } else {
            fieldName = "";
        }
        return fieldName;
    }

    /**
     * Gets provider code
     * @param dataset
     * @return
     */
    private String getProviderCode(DataSetMetabaseVO dataset) {
        String providerCode;
        if (dataset.getDataProviderId()!=null) {
            DataProviderVO provider = representativeControllerZuul.findDataProviderById(dataset.getDataProviderId());
            providerCode = provider.getCode();
        } else {
            providerCode = null;
        }
        return providerCode;
    }

    /**
     * Executes rule
     * @param providerCode
     * @param ruleVO
     * @param fieldName
     * @param headerNames
     * @param rs
     * @param cls
     * @param object
     * @param nestedExpression
     * @param internalResults
     * @param record
     * @param fieldSchemaIdNameMap
     */
    private void executeRule(String providerCode, RuleVO ruleVO, String fieldName, Map<String, List<String>> headerNames, SqlRowSet rs, Class<?> cls, Object object,
                             AtomicBoolean nestedExpression, List<Object> internalResults, AtomicBoolean record, Map<String, String> fieldSchemaIdNameMap) {

        ruleVO.getWhenCondition().getParams().forEach(param -> {
            String functionName;
            List<Object> params = new ArrayList<>();
            if (param instanceof RuleExpressionDTO) {
                nestedExpression.set(true);
                RuleExpressionDTO ruleExpression = (RuleExpressionDTO) param;
                ruleExpression.getParams().forEach(p -> {
                    Object result;
                    if (p instanceof RuleExpressionDTO) {
                        result = getResult(providerCode, ruleVO, fieldName, headerNames, rs, cls, object, record, (RuleExpressionDTO) p, fieldSchemaIdNameMap);
                        params.add(result);
                    } else {
                        params.add(p);
                    }
                });
                functionName = ruleExpression.getOperator().getFunctionName();
                record.set(isRecord(functionName));
                Method md = dremioRulesService.getRuleMethodFromClass(functionName, cls);
                try {
                    Object result = getMethodExecutionResult(ruleVO, fieldName, headerNames, rs, object, record.get(), functionName, md, params, providerCode, fieldSchemaIdNameMap);
                    internalResults.add(result);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else {
                internalResults.add(param);
            }

        });
    }

    /**
     * Finds field names that should be retrieved from dremio
     * @param datasetSchemaId
     * @param query
     * @param parameters
     * @param fieldName
     * @param headerNames
     * @param ruleExpressionDTO
     * @param fieldSchemaIdNameMap
     */
    private void createHeaders(String datasetSchemaId, StringBuilder query, List<Object> parameters, String fieldName, Map<String, List<String>> headerNames,
                               RuleExpressionDTO ruleExpressionDTO, Map<String, String> fieldSchemaIdNameMap) {
        String ruleMethodName = ruleExpressionDTO.getOperator().getFunctionName();
        int idx = ruleMethodName.indexOf(RECORD);
        List<String> hNames = new ArrayList<>();
        if (idx!=-1) {
            //record type
            //e.g. RuleOperators.recordStringLengthGreaterThanOrEqualsThanRecord("64ac0c0de5f082645bab2f07", "64ac0c1ae5f082645bab2f09")
            parameters = ruleExpressionDTO.getParams();
            parameters.forEach(p -> {
                FieldSchemaVO fieldSchema = datasetSchemaControllerZuul.getFieldSchema(datasetSchemaId, (String) p);
                query.append(COMMA).append(UtilityClass.addQuotesToFieldNames(fieldSchema.getName()));
                fieldSchemaIdNameMap.put(fieldSchema.getId(), fieldSchema.getName());
            });
            headerNames.put(ruleMethodName, hNames);
        } else {
            extractFieldHeaders(ruleExpressionDTO, headerNames, fieldName, datasetSchemaId, query, fieldSchemaIdNameMap);
        }
    }

    /**
     * Gets result
     * @param providerCode
     * @param ruleVO
     * @param fieldName
     * @param headerNames
     * @param rs
     * @param cls
     * @param object
     * @param record
     * @param p
     * @param fieldSchemaIdNameMap
     * @return
     */
    private Object getResult(String providerCode, RuleVO ruleVO, String fieldName, Map<String, List<String>> headerNames, SqlRowSet rs, Class<?> cls, Object object, AtomicBoolean record,
                             RuleExpressionDTO p, Map<String, String> fieldSchemaIdNameMap) {
        RuleExpressionDTO ruleExp = p;
        String functionName = ruleExp.getOperator().getFunctionName();
        record.set(isRecord(functionName));
        Method md = dremioRulesService.getRuleMethodFromClass(functionName, cls);
        List<Object> values = new ArrayList<>();
        ruleExp.getParams().stream().forEach(pm -> {
            if (!(pm instanceof RuleExpressionDTO)) {
                values.add(pm);
            } else {
                Object value = getResult(providerCode, ruleVO, fieldName, headerNames, rs, cls, object, record, (RuleExpressionDTO) pm, fieldSchemaIdNameMap);
                values.add(value);
            }
        });
        Object result;
        try {
            result = getMethodExecutionResult(ruleVO, fieldName, headerNames, rs, object, record.get(), functionName, md, values, providerCode, fieldSchemaIdNameMap);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Extracts field header names
     * @param ruleExpressionDTO
     * @param headerNames
     * @param fieldName
     * @param datasetSchemaId
     * @param query
     * @param fieldSchemaIdNameMap
     */
    private void extractFieldHeaders(RuleExpressionDTO ruleExpressionDTO, Map<String, List<String>> headerNames, String fieldName,
                                     String datasetSchemaId, StringBuilder query, Map<String, String> fieldSchemaIdNameMap) {
        List<Object> params= ruleExpressionDTO.getParams();
        params.forEach(p -> {
            if (p instanceof RuleExpressionDTO) {
                extractFieldHeaders((RuleExpressionDTO) p, headerNames, fieldName, datasetSchemaId, query, fieldSchemaIdNameMap);
            } else {
                if (p instanceof String) {
                    createHeaderNames((String) p, ruleExpressionDTO.getOperator().getFunctionName(), headerNames, fieldName, datasetSchemaId, query, fieldSchemaIdNameMap);
                }
            }
        });
    }

    /**
     * Creates headerNames map
     * @param parameter
     * @param methodName
     * @param headerNames
     * @param fieldName
     * @param datasetSchemaId
     * @param query
     * @param fieldSchemaIdNameMap
     */
    private void createHeaderNames(String parameter, String methodName, Map<String, List<String>> headerNames, String fieldName, String datasetSchemaId, StringBuilder query, Map<String, String> fieldSchemaIdNameMap) {
        parameter = parameter.trim();
        List<String> list = headerNames.get(methodName);
        if (list==null) {
            list = new ArrayList<>();
        }
        if (parameter.equalsIgnoreCase(VALUE)) {
            if (!list.contains(fieldName)) {
                list.add(fieldName);
            }
            headerNames.put(methodName, list);
        } else if (!isNumeric(parameter) && !parameter.startsWith(OPEN_BRACKET) && !parameter.startsWith(SINGLE_QUOTE) && !parameter.startsWith(DOUBLE_QUOTE)) {
            if (!isDate(parameter)) {
                FieldSchemaVO fieldSchema = datasetSchemaControllerZuul.getFieldSchema(datasetSchemaId, parameter);
                if (!list.contains(fieldSchema.getName())) {
                    list.add(fieldSchema.getName());
                }
                headerNames.put(methodName, list);
                query.append(COMMA).append(UtilityClass.addQuotesToFieldNames(fieldSchema.getName()));
                fieldSchemaIdNameMap.put(fieldSchema.getId(), fieldSchema.getName());
            }
        }
    }

    /**
     * Checks if string is valid date
     * @param inDate
     * @return
     */
    private boolean isDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException e) {
           return false;
        }
        return true;
    }

    /**
     * Checks if rule method name contains "Record" string
     * e.g. RuleOperators.recordStringLengthGreaterThanOrEqualsThanRecord("64ac0c0de5f082645bab2f07", "64ac0c1ae5f082645bab2f09")
     * @param value
     * @return
     */
    private static boolean isRecord(String value) {
        boolean record = false;
        int idx = value.indexOf(RECORD);
        if (idx!=-1) {
            //record type
            record = true;
        }
        return record;
    }

    /**
     * Executes method
     * @param ruleVO
     * @param fieldName
     * @param headerNames
     * @param rs
     * @param object
     * @param record
     * @param methodName
     * @param md
     * @param pm
     * @param providerCode
     * @param fieldSchemaIdNameMap
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Object getMethodExecutionResult(RuleVO ruleVO, String fieldName, Map<String, List<String>> headerNames, SqlRowSet rs, Object object,
                                             boolean record, String methodName, Method md, List<Object> pm, String providerCode, Map<String, String> fieldSchemaIdNameMap) throws IllegalAccessException, InvocationTargetException {
        Object result = null;
        if (methodName.equals(RECORD_IF_THEN) || methodName.equals(RECORD_AND) || methodName.equals(RECORD_OR) ||
                methodName.equals(FIELD_AND) || methodName.equals(FIELD_OR)) {
            if (pm.size() == 1) {
                return md.invoke(object, pm.get(0));
            } else if (pm.size() == 2) {
                return md.invoke(object, pm.get(0), pm.get(1));
            }
        }

        List<FieldValue> fields = new ArrayList<>();
        RecordValue recordValue = new RecordValue();
        recordValue.setDataProviderCode(providerCode);
        List<String> intHeaders = headerNames.get(md.getName());
        if (pm.size()==1) {
            String fieldValue = "";
            String fieldSchemaId = "";
            if (ruleVO.getType().equals(EntityTypeEnum.RECORD)) {
                fieldSchemaId = (String) pm.get(0);
            } else {
                fieldSchemaId = ruleVO.getReferenceId();
            }
            if (fieldName.isEmpty()) {
                /*
                The following code is a fix for #280121 Previously when having the same condition multiple times connected with AND/OR
                e.g. NOT_NULL condition (field1 is NOT_NULL AND field2 is NOT_NULL), we only checked the first field.
                With the following we use map that contains field schema ids and their names and retrieve the correct field name each time
                 */
                if(fieldSchemaIdNameMap != null && fieldSchemaIdNameMap.get(fieldSchemaId) != null){
                    fieldValue = rs.getString(fieldSchemaIdNameMap.get(fieldSchemaId));
                }
                else{
                    fieldValue = rs.getString(headerNames.get(methodName).get(0));
                }
            }
            createField(fieldName, rs, fields, recordValue, fieldValue, fieldSchemaId);
            result = md.invoke(object, pm.get(0) instanceof String && ((String) pm.get(0)).equalsIgnoreCase(VALUE) ? rs.getString(fieldName) : pm.get(0));
        } else if (pm.size()==2) {
            if (record) {
                creatFields(rs, pm, fields, recordValue, intHeaders);
                result = md.invoke(object, pm.get(0), pm.get(1));
            } else {
                result = invokeMethodIfNotRecord(ruleVO.getReferenceId(), fieldName, rs, object, methodName, md, pm, fields, recordValue, intHeaders);
            }
        }
        return result;
    }

    /**
     * Invoke rule method in case method name doesn't contain "Record"
     * @param referenceId
     * @param fieldName
     * @param rs
     * @param object
     * @param methodName
     * @param md
     * @param pm
     * @param fields
     * @param recordValue
     * @param intHeaders
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static Object invokeMethodIfNotRecord(String referenceId, String fieldName, SqlRowSet rs, Object object, String methodName, Method md, List<Object> pm,
                                                  List<FieldValue> fields, RecordValue recordValue, List<String> intHeaders) throws IllegalAccessException, InvocationTargetException {
        Object result;
        String firstValue = setRecordAndFieldsAndGetParameterForMethodInvocation(referenceId, fieldName, rs, pm, fields, recordValue, intHeaders);
        if (!methodName.contains(NUMBER) && !methodName.contains(LENGTH) && !methodName.contains(DAY) && !methodName.contains(MONTH) && !methodName.contains(YEAR)) {
            if (pm.get(1) instanceof String && (((String) pm.get(1)).startsWith(SINGLE_QUOTE) || ((String) pm.get(1)).startsWith(DOUBLE_QUOTE))) {
                processParameterList(pm, (String) pm.get(1));
            }
        }
        result = md.invoke(object, firstValue, pm.get(1));
        return result;
    }

    /**
     * Sets record and field values and returns parameter for method invocation
     * @param referenceId
     * @param fieldName
     * @param rs
     * @param pm
     * @param fields
     * @param recordValue
     * @param intHeaders
     * @return
     */
    private static String setRecordAndFieldsAndGetParameterForMethodInvocation(String referenceId, String fieldName, SqlRowSet rs, List<Object> pm, List<FieldValue> fields,
                                                                               RecordValue recordValue, List<String> intHeaders) {
        String firstValue;
        FieldValue fieldValue = new FieldValue();
        if (pm.get(0) instanceof String && ((String) pm.get(0)).equalsIgnoreCase(VALUE)) {
            firstValue = rs.getString(fieldName);
            fieldValue.setIdFieldSchema(referenceId);
            fieldValue.setValue(firstValue);
        } else if (intHeaders !=null && intHeaders.size()>0) {
            firstValue = (String) pm.get(0);
            fieldValue.setIdFieldSchema((String) pm.get(0));
            fieldValue.setValue(rs.getString(intHeaders.get(0)));
        } else {
            firstValue = (String) pm.get(0);
            fieldValue.setIdFieldSchema(referenceId);
            fieldValue.setValue(firstValue);
        }
        fields.add(fieldValue);
        recordValue.setFields(fields);
        fieldValue.setRecord(recordValue);
        RuleOperators.setEntity(fieldValue);
        RuleOperators.setEntity(recordValue);
        return firstValue;
    }

    private String resolveValidateAsProviderCodeFromTask(Long taskId) {
        if (taskId == null) return null;

        TaskVO task = validationHelper.findTaskById(taskId);
        if (task == null || task.getJson() == null || task.getJson().isBlank()) return null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(task.getJson());
            JsonNode data = root.get("data");
            if (data == null) return null;

            JsonNode node = data.get("validateAsProviderCode");
            if (node == null || node.isNull()) return null;

            String code = node.asText().trim();
            return code.isEmpty() ? null : code;
        } catch (Exception e) {
            LOG.warn("Could not parse validateAsProviderCode from task json for taskId {}: {}", taskId, e.getMessage());
            return null;
        }
    }

    /**
     * Processes parameter list
     * @param pm
     * @param paramToRemove
     */
    private static void processParameterList(List<Object> pm, String paramToRemove) {
        String parameter = paramToRemove.substring(1, paramToRemove.length() - 1);
        pm.remove(paramToRemove);
        pm.add(parameter);
    }

    private static void creatFields(SqlRowSet rs, List<Object> pm, List<FieldValue> fields, RecordValue recordValue, List<String> intHeaders) {
        FieldValue fieldValue1 = new FieldValue();
        fieldValue1.setValue(rs.getString(intHeaders.get(0)));
        fieldValue1.setIdFieldSchema((String) pm.get(0));
        FieldValue fieldValue2 = new FieldValue();
        fieldValue2.setValue(rs.getString(intHeaders.get(1)));
        fieldValue2.setIdFieldSchema((String) pm.get(1));
        fields.add(fieldValue1);
        fields.add(fieldValue2);
        recordValue.setFields(fields);
        RuleOperators.setEntity(recordValue);
    }

    private void createField(String fieldName, SqlRowSet rs, List<FieldValue> fields, RecordValue recordValue, String fieldVal, String fieldSchemaId) {
        FieldValue fieldValue = new FieldValue();
        if (fieldName.isEmpty()) {
            fieldValue.setValue(fieldVal);
        } else {
            fieldValue.setValue(rs.getString(fieldName));
        }
        fieldValue.setIdFieldSchema(fieldSchemaId);
        fields.add(fieldValue);
        recordValue.setFields(fields);
        fieldValue.setRecord(recordValue);
        RuleOperators.setEntity(fieldValue);
        RuleOperators.setEntity(recordValue);
    }

    /**
     * Checks if value is numeric
     * @param value
     * @return
     */
    private boolean isNumeric(String value) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (value == null) {
            return false;
        }
        return pattern.matcher(value.trim()).matches();
    }
}
