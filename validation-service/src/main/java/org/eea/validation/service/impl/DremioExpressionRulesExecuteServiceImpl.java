package org.eea.validation.service.impl;

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
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.dto.dataset.schemas.rule.RuleExpressionDTO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.service.DremioRulesExecuteService;
import org.eea.validation.service.DremioRulesService;
import org.eea.validation.service.RulesService;
import org.eea.validation.util.RuleOperators;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static org.eea.utils.LiteralConstants.*;

@ImportDataLakeCommons
@Service
public class DremioExpressionRulesExecuteServiceImpl implements DremioRulesExecuteService {

    @Value("${parquet.file.path}")
    private String parquetFilePath;
    private JdbcTemplate dremioJdbcTemplate;
    private S3Service s3Service;
    private RulesService rulesService;
    private DatasetSchemaControllerZuul datasetSchemaControllerZuul;
    private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;
    private DremioRulesService dremioRulesService;
    private RepresentativeControllerZuul representativeControllerZuul;
    private S3Helper s3Helper;
    private DremioHelperService dremioHelperService;

    private static final Logger LOG = LoggerFactory.getLogger(DremioExpressionRulesExecuteServiceImpl.class);
    private static final String RECORD_IF_THEN = "recordIfThen";
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

    @Autowired
    public DremioExpressionRulesExecuteServiceImpl(@Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, S3Service s3Service, RulesService rulesService, DatasetSchemaControllerZuul datasetSchemaControllerZuul,
                                                   DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul, DremioRulesService dremioRulesService, RepresentativeControllerZuul representativeControllerZuul,
                                                   S3Helper s3Helper, DremioHelperService dremioHelperService) {
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.s3Service = s3Service;
        this.rulesService = rulesService;
        this.datasetSchemaControllerZuul = datasetSchemaControllerZuul;
        this.dataSetMetabaseControllerZuul = dataSetMetabaseControllerZuul;
        this.dremioRulesService = dremioRulesService;
        this.representativeControllerZuul = representativeControllerZuul;
        this.s3Helper = s3Helper;
        this.dremioHelperService = dremioHelperService;
    }

    @Override
    public void execute(Long dataflowId, Long datasetId, String datasetSchemaId, String tableName, String tableSchemaId, String ruleId, Long dataProviderId, Long taskId, boolean createParquetWithSQL) throws Exception {
        try {
            S3PathResolver dataTableResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, tableName);

            if (!s3Helper.checkFolderExist(dataTableResolver, S3_TABLE_NAME_FOLDER_PATH)) {
                return;
            }

            S3PathResolver validationResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, S3_VALIDATION);
            DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
            String providerCode = getProviderCode(dataset);
            StringBuilder query = new StringBuilder();
            RuleVO ruleVO = rulesService.findRule(datasetSchemaId, ruleId);
            s3Helper.deleteRuleFolderIfExists(validationResolver, ruleVO);

            List<Object> parameters = new ArrayList<>();
            String fieldName = getFieldName(datasetSchemaId, tableSchemaId, ruleVO);
            String fileName = datasetId + UNDERSCORE + tableName + UNDERSCORE + ruleVO.getShortCode() + PARQUET_TYPE;
            String parquetFile = parquetFilePath + fileName;

            Map<String, List<String>> headerNames = new HashMap<>();  //map of method as key and list of field names (that exist as parameters in method) as values

            query.append("select record_id");
            if (!fieldName.equals("")) {
                query.append(COMMA).append(fieldName);
            }

            createHeaders(datasetSchemaId, query, parameters, fieldName, headerNames, ruleVO.getWhenCondition());

            query.append(" from ").append(s3Service.getTableAsFolderQueryPath(dataTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH));
            SqlRowSet rs = dremioJdbcTemplate.queryForRowSet(query.toString());

            dremioHelperService.deleteFileFromR3IfExists(parquetFile);
            runRuleAndCreateParquet(createParquetWithSQL, parquetFile, providerCode, ruleVO, fieldName, fileName, headerNames, rs,  dataTableResolver, validationResolver);
        } catch (Exception e1) {
            LOG.error("Error creating validation folder for ruleId {}, datasetId {} and tableName {}", ruleId, datasetId, tableName);
            throw e1;
        }
    }

    /**
     * Runs rule and creates parquet in S3
     * @param createParquetWithSQL
     * @param parquetFile
     * @param providerCode
     * @param ruleVO
     * @param fieldName
     * @param headerNames
     * @param rs
     * @param dataTableResolver
     * @param validationResolver
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IOException
     */
    private void runRuleAndCreateParquet(boolean createParquetWithSQL, String parquetFile, String providerCode, RuleVO ruleVO, String fieldName, String fileName, Map<String, List<String>> headerNames, SqlRowSet rs,
                                         S3PathResolver dataTableResolver, S3PathResolver validationResolver) throws Exception {
        int ruleIdLength = ruleVO.getRuleId().length();
        Class<?> cls = Class.forName(RULE_OPERATORS);
        Method factoryMethod = cls.getDeclaredMethod(GET_INSTANCE);
        Object object = factoryMethod.invoke(null, null);
        if (createParquetWithSQL) {
            int count = 0;
            boolean createRuleFolder = false;
            StringBuilder validationQuery = dremioRulesService.getS3RuleFolderQueryBuilder(dataTableResolver.getDatasetId(), dataTableResolver.getTableName(), dataTableResolver, validationResolver, ruleVO, fieldName);
            while (rs.next()) {
                boolean isValid = isRecordValid(providerCode, ruleVO, fieldName, headerNames, rs, cls, object);
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
            if (createRuleFolder) {
                validationQuery.append("))");
                dremioJdbcTemplate.execute(validationQuery.toString());
            }
        } else {
            Map<String, String> headerMap = dremioRulesService.createValidationParquetHeaderMap(dataTableResolver.getDatasetId(), dataTableResolver.getTableName(), ruleVO, fieldName);
            StringBuilder pathBuilder = new StringBuilder();
            String s3FilePath = pathBuilder.append(s3Service.getTableAsFolderQueryPath(validationResolver, S3_VALIDATION_TABLE_PATH)).append(SLASH).append(ruleVO.getShortCode()).append(DASH).append(ruleVO.getRuleId().substring(ruleIdLength - 3, ruleIdLength))
                    .append(SLASH).append(fileName).toString();
            createParquetAndUploadToS3(parquetFile, providerCode, ruleVO, fieldName, s3FilePath, headerMap, headerNames, rs, cls, object);
        }
    }

    /**
     * Creates parquet and uploads it to S3
     * @param parquetFile
     * @param providerCode
     * @param ruleVO
     * @param fieldName
     * @param s3FilePath
     * @param headerMap
     * @param headerNames
     * @param rs
     * @param cls
     * @param object
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IOException
     */
    private void createParquetAndUploadToS3(String parquetFile, String providerCode, RuleVO ruleVO, String fieldName, String s3FilePath, Map<String, String> headerMap, Map<String, List<String>> headerNames, SqlRowSet rs, Class<?> cls, Object object) throws Exception {
        try {
            //Defining schema
            List<String> parquetHeaders = Arrays.asList(PK, PARQUET_RECORD_ID_COLUMN_HEADER, VALIDATION_LEVEL, VALIDATION_AREA, MESSAGE, TABLE_NAME, FIELD_NAME, DATASET_ID, QC_CODE);
            List<Schema.Field> fields = new ArrayList<>();
            for (String header : parquetHeaders) {
                fields.add(new Schema.Field(header, Schema.create(Schema.Type.STRING), null, null));
            }
            Schema schema = Schema.createRecord("Data", null, null, false, fields);

            boolean createRuleFolder = false;
            try (ParquetWriter<GenericRecord> writer = AvroParquetWriter
                    .<GenericRecord>builder(new Path(parquetFile))
                    .withSchema(schema)
                    .withCompressionCodec(CompressionCodecName.SNAPPY)
                    .withPageSize(4 * 1024)
                    .withRowGroupSize(16 * 1024)
                    .build()) {
                while (rs.next()) {
                    createRuleFolder = createParquetGenericRecord(providerCode, ruleVO, fieldName, headerMap, headerNames, rs, cls, object, schema, writer, createRuleFolder);
                }
            } catch (Exception e) {
                LOG.error("Error creating parquet file {}", parquetFile);
                throw e;
            }
            if (createRuleFolder) {
                s3Helper.uploadFileToBucket(s3FilePath, parquetFile);
            }
        } finally {
            dremioHelperService.deleteFileFromR3IfExists(parquetFile);
        }
    }

    /**
     * creates parquet generic record
     * @param providerCode
     * @param ruleVO
     * @param fieldName
     * @param headerMap
     * @param headerNames
     * @param rs
     * @param cls
     * @param object
     * @param schema
     * @param writer
     * @param createRuleFolder
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IOException
     */
    private boolean createParquetGenericRecord(String providerCode, RuleVO ruleVO, String fieldName, Map<String, String> headerMap, Map<String, List<String>> headerNames, SqlRowSet rs, Class<?> cls,
                                            Object object, Schema schema, ParquetWriter<GenericRecord> writer, boolean createRuleFolder) throws IllegalAccessException, InvocationTargetException, IOException {
        boolean isValid = isRecordValid(providerCode, ruleVO, fieldName, headerNames, rs, cls, object);
        if (!isValid) {
            GenericRecord genericRecord = new GenericData.Record(schema);
            for (Map.Entry<String,String> entry : headerMap.entrySet()) {
                genericRecord.put(entry.getKey(), entry.getValue());
            }
            genericRecord.put(PK, UUID.randomUUID().toString());
            genericRecord.put(PARQUET_RECORD_ID_COLUMN_HEADER, rs.getString(PARQUET_RECORD_ID_COLUMN_HEADER));
            writer.write(genericRecord);
            if (!createRuleFolder) {
                createRuleFolder = true;
            }
        }
        return createRuleFolder;
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
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private boolean isRecordValid(String providerCode, RuleVO ruleVO, String fieldName, Map<String, List<String>> headerNames, SqlRowSet rs, Class<?> cls, Object object) throws IllegalAccessException, InvocationTargetException {
        List<Object> parameters;
        RuleExpressionDTO ruleExpressionDTO = ruleVO.getWhenCondition();
        String ruleMethodName = ruleExpressionDTO.getOperator().getFunctionName();
        boolean isValid = false;
        AtomicBoolean nestedExpression = new AtomicBoolean(false);
        Method method = dremioRulesService.getRuleMethodFromClass(ruleMethodName, cls);
        List<Object> internalResults = new ArrayList<>();
        AtomicBoolean record = new AtomicBoolean(false);
        executeRule(providerCode, ruleVO, fieldName, headerNames, rs, cls, object, nestedExpression, internalResults, record);

        if (internalResults.size()>0 && nestedExpression.get()) {
            if (internalResults.size() == 1) {
                    isValid = (boolean) method.invoke(object, internalResults.get(0));
            } else if (internalResults.size() == 2) {
                    isValid = (boolean) method.invoke(object, internalResults.get(0), internalResults.get(1));
            }
        } else {
            record.set(isRecord(method.getName()));
            parameters = ruleExpressionDTO.getParams();
            isValid = (Boolean) getMethodExecutionResult(ruleVO, fieldName, headerNames, rs, object, record.get(), method.getName(), method, parameters, providerCode);
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
     */
    private void executeRule(String providerCode, RuleVO ruleVO, String fieldName, Map<String, List<String>> headerNames, SqlRowSet rs, Class<?> cls, Object object, AtomicBoolean nestedExpression, List<Object> internalResults, AtomicBoolean record) {
        ruleVO.getWhenCondition().getParams().forEach(param -> {
            String functionName;
            List<Object> params = new ArrayList<>();
            if (param instanceof RuleExpressionDTO) {
                nestedExpression.set(true);
                RuleExpressionDTO ruleExpression = (RuleExpressionDTO) param;
                ruleExpression.getParams().forEach(p -> {
                    Object result;
                    if (p instanceof RuleExpressionDTO) {
                        result = getResult(providerCode, ruleVO, fieldName, headerNames, rs, cls, object, record, (RuleExpressionDTO) p);
                        params.add(result);
                    } else {
                        params.add(p);
                    }
                });
                functionName = ruleExpression.getOperator().getFunctionName();
                record.set(isRecord(functionName));
                Method md = dremioRulesService.getRuleMethodFromClass(functionName, cls);
                try {
                    Object result = getMethodExecutionResult(ruleVO, fieldName, headerNames, rs, object, record.get(), functionName, md, params, providerCode);
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
     */
    private void createHeaders(String datasetSchemaId, StringBuilder query, List<Object> parameters, String fieldName, Map<String, List<String>> headerNames, RuleExpressionDTO ruleExpressionDTO) {
        String ruleMethodName = ruleExpressionDTO.getOperator().getFunctionName();
        int idx = ruleMethodName.indexOf(RECORD);
        List<String> hNames = new ArrayList<>();
        if (idx!=-1) {
            //record type
            //e.g. RuleOperators.recordStringLengthGreaterThanOrEqualsThanRecord("64ac0c0de5f082645bab2f07", "64ac0c1ae5f082645bab2f09")
            parameters = ruleExpressionDTO.getParams();
            parameters.forEach(p -> {
                FieldSchemaVO fieldSchema = datasetSchemaControllerZuul.getFieldSchema(datasetSchemaId, (String) p);
                hNames.add(fieldSchema.getName());
                query.append(COMMA).append(fieldSchema.getName());
            });
            headerNames.put(ruleMethodName, hNames);
        } else {
            boolean containsExpression = ruleExpressionDTO.getParams().stream().anyMatch(RuleExpressionDTO.class::isInstance);
            if (!containsExpression) {
                List<Object> finalParameters = parameters;
                ruleExpressionDTO.getParams().forEach(finalParameters::add);
            } else {
                extractFieldHeaders(ruleExpressionDTO, headerNames, fieldName, datasetSchemaId, query);
            }
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
     * @return
     */
    private Object getResult(String providerCode, RuleVO ruleVO, String fieldName, Map<String, List<String>> headerNames, SqlRowSet rs, Class<?> cls, Object object, AtomicBoolean record, RuleExpressionDTO p) {
        RuleExpressionDTO ruleExp = p;
        String functionName = ruleExp.getOperator().getFunctionName();
        record.set(isRecord(functionName));
        Method md = dremioRulesService.getRuleMethodFromClass(functionName, cls);
        List<Object> values = new ArrayList<>();
        ruleExp.getParams().stream().forEach(pm -> {
            if (!(pm instanceof RuleExpressionDTO)) {
                values.add(pm);
            } else {
                Object value = getResult(providerCode, ruleVO, fieldName, headerNames, rs, cls, object, record, (RuleExpressionDTO) pm);
                values.add(value);
            }
        });
        Object result;
        try {
            result = getMethodExecutionResult(ruleVO, fieldName, headerNames, rs, object, record.get(), functionName, md, values, providerCode);
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
     */
    private void extractFieldHeaders(RuleExpressionDTO ruleExpressionDTO, Map<String, List<String>> headerNames, String fieldName, String datasetSchemaId, StringBuilder query) {
        List<Object> params= ruleExpressionDTO.getParams();
        params.forEach(p -> {
            if (p instanceof RuleExpressionDTO) {
                extractFieldHeaders((RuleExpressionDTO) p, headerNames, fieldName, datasetSchemaId, query);
            } else {
                if (p instanceof String) {
                    createHeaderNames((String) p, ruleExpressionDTO.getOperator().getFunctionName(), headerNames, fieldName, datasetSchemaId, query);
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
     */
    private void createHeaderNames(String parameter, String methodName, Map<String, List<String>> headerNames, String fieldName, String datasetSchemaId, StringBuilder query) {
        parameter = parameter.trim();
        if (parameter.equalsIgnoreCase(VALUE)) {
            List<String> list = headerNames.get(methodName);
            if (list!=null && !list.contains(fieldName)) {
                list.add(fieldName);
            } else {
                list = new ArrayList<>();
                list.add(fieldName);
            }
            headerNames.put(methodName, list);
        } else if (!isNumeric(parameter) && !parameter.startsWith(OPEN_BRACKET)) {
            FieldSchemaVO fieldSchema = datasetSchemaControllerZuul.getFieldSchema(datasetSchemaId, parameter);
            List<String> list = headerNames.get(methodName);
            if (list!=null) {
                list.add(fieldSchema.getName());
            } else {
                list = new ArrayList<>();
                list.add(fieldSchema.getName());
            }
            headerNames.put(methodName, list);
            query.append(COMMA).append(fieldSchema.getName());
        }
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
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Object getMethodExecutionResult(RuleVO ruleVO, String fieldName, Map<String, List<String>> headerNames, SqlRowSet rs, Object object,
                                             boolean record, String methodName, Method md, List<Object> pm, String providerCode) throws IllegalAccessException, InvocationTargetException {
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
            createField(ruleVO, fieldName, rs, fields, recordValue);
            result = md.invoke(object, pm.get(0) instanceof String && ((String) pm.get(0)).contains(VALUE) ? rs.getString(fieldName) : pm.get(0));
        } else if (pm.size()==2) {
            if (record) {
                creatFields(rs, pm, fields, recordValue, intHeaders);
                result = md.invoke(object, pm.get(0), pm.get(1));
            } else {
                String firstValue;
                FieldValue fieldValue = new FieldValue();
                if (pm.get(0) instanceof String && ((String) pm.get(0)).equalsIgnoreCase(VALUE)) {
                    firstValue = rs.getString(fieldName);
                    fieldValue.setIdFieldSchema(ruleVO.getReferenceId());
                    fieldValue.setValue(firstValue);
                } else if (intHeaders!=null && intHeaders.size()>0) {
                    firstValue = (String) pm.get(0);
                    fieldValue.setIdFieldSchema((String) pm.get(0));
                    fieldValue.setValue(rs.getString(intHeaders.get(0)));
                } else {
                    firstValue = (String) pm.get(0);
                    fieldValue.setIdFieldSchema(ruleVO.getReferenceId());
                    fieldValue.setValue(firstValue);
                }
                fields.add(fieldValue);
                recordValue.setFields(fields);
                fieldValue.setRecord(recordValue);
                RuleOperators.setEntity(fieldValue);
                RuleOperators.setEntity(recordValue);
                if (methodName.contains(NUMBER)) {
                    result = md.invoke(object, firstValue, pm.get(1));
                } else if (methodName.contains(LENGTH)) {
                    result = md.invoke(object, firstValue, pm.get(1));
                } else if (methodName.contains(DAY) || methodName.contains(MONTH) || methodName.contains(YEAR)) {
                    result = md.invoke(object, firstValue, pm.get(1));
                } else {
                    result = md.invoke(object, firstValue, pm.get(1));
                }
            }
        }
        return result;
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

    private void createField(RuleVO ruleVO, String fieldName, SqlRowSet rs, List<FieldValue> fields, RecordValue recordValue) {
        FieldValue fieldValue = new FieldValue();
        fieldValue.setValue(rs.getString(fieldName));
        fieldValue.setIdFieldSchema(ruleVO.getReferenceId());
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
