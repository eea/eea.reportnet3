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
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.validation.service.DremioRulesExecuteService;
import org.eea.validation.service.DremioRulesService;
import org.eea.validation.service.RulesService;
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

import static org.eea.utils.LiteralConstants.*;

@ImportDataLakeCommons
@Service
public class DremioNonSqlRulesExecuteServiceImpl implements DremioRulesExecuteService {

    @Value("${parquet.file.path}")
    private String parquetFilePath;

    private JdbcTemplate dremioJdbcTemplate;
    private S3Service s3Service;
    private RulesService rulesService;
    private DatasetSchemaControllerZuul datasetSchemaControllerZuul;
    private DremioRulesService dremioRulesService;
    private S3Helper s3Helper;
    private DremioHelperService dremioHelperService;

    private static final String DREMIO_NON_SQL_VALIDATION_UTILS = "org.eea.validation.util.datalake.DremioNonSQLValidationUtils";
    private static final String VALIDATION_DROOLS_UTILS = "org.eea.validation.util.ValidationDroolsUtils";
    private static final String IS_MULTI_SELECT_CODE_LIST_VALIDATE = "isMultiSelectCodelistValidate";
    private static final String MULTI_SELECT_CODE_LIST_VALIDATE = "multiSelectCodelistValidate";
    private static final String IS_CODE_LIST_INSENSITIVE = "isCodelistInsensitive";
    private static final String CODE_LIST_VALIDATE = "codelistValidate";
    private static final String FALSE = "false";
    private static final Logger LOG = LoggerFactory.getLogger(DremioNonSqlRulesExecuteServiceImpl.class);

    @Autowired
    public DremioNonSqlRulesExecuteServiceImpl(@Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, S3Service s3Service, RulesService rulesService,
                                               DatasetSchemaControllerZuul datasetSchemaControllerZuul, DremioRulesService dremioRulesService, S3Helper s3Helper, DremioHelperService dremioHelperService) {
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.s3Service = s3Service;
        this.rulesService = rulesService;
        this.datasetSchemaControllerZuul = datasetSchemaControllerZuul;
        this.dremioHelperService = dremioHelperService;
        this.dremioRulesService = dremioRulesService;
        this.s3Helper = s3Helper;
    }

    @Override
    public void execute(Long dataflowId, Long datasetId, String datasetSchemaId, String tableName, String tableSchemaId, String ruleId, Long dataProviderId, Long taskId, boolean createParquetWithSQL) throws Exception {
        try {
            //if the dataset to validate is of reference type, then the table path should be changed
            S3PathResolver dataTableResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, tableName);
            if (!s3Helper.checkFolderExist(dataTableResolver, S3_TABLE_NAME_FOLDER_PATH)) {
                return;
            }
            S3PathResolver validationResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, S3_VALIDATION);

            StringBuilder query = new StringBuilder();
            RuleVO ruleVO = rulesService.findRule(datasetSchemaId, ruleId);
            s3Helper.deleteRuleFolderIfExists(validationResolver, ruleVO);
            int startIndex = ruleVO.getWhenConditionMethod().indexOf(OPEN_PARENTHESIS);
            int endIndex = ruleVO.getWhenConditionMethod().indexOf(CLOSE_PARENTHESIS);
            String ruleMethodName = ruleVO.getWhenConditionMethod().substring(0, startIndex);
            List<String> parameters = dremioRulesService.processRuleMethodParameters(ruleVO, startIndex, endIndex);
            if (ruleMethodName.equals(IS_MULTI_SELECT_CODE_LIST_VALIDATE)) {
                ruleMethodName = MULTI_SELECT_CODE_LIST_VALIDATE;
            } else if (ruleMethodName.equals(IS_CODE_LIST_INSENSITIVE)) {
                ruleMethodName = CODE_LIST_VALIDATE;
                parameters.add(FALSE);
            }

            String fieldName = datasetSchemaControllerZuul.getFieldName(datasetSchemaId, tableSchemaId, parameters, ruleVO.getReferenceId(), ruleVO.getReferenceFieldSchemaPKId());
            String fileName = datasetId + UNDERSCORE + tableName + UNDERSCORE + ruleVO.getShortCode() + PARQUET_TYPE;
            String parquetFile = parquetFilePath + fileName;

            query.append("select record_id,").append(fieldName != null ? fieldName : "").append(" from ").append(s3Service.getTableAsFolderQueryPath(dataTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH));
            SqlRowSet rs = dremioJdbcTemplate.queryForRowSet(query.toString());

            Method method = null;
            List<String> classes = new ArrayList<>(Arrays.asList(DREMIO_NON_SQL_VALIDATION_UTILS, VALIDATION_DROOLS_UTILS));
            Class<?> cls = null;
            for (String className : classes) {
                cls = Class.forName(className);
                method = dremioRulesService.getRuleMethodFromClass(ruleMethodName, cls);
                if (method != null) {
                    break;
                }
            }
            Method factoryMethod = cls.getDeclaredMethod(GET_INSTANCE);
            Object object = factoryMethod.invoke(null, null);

            dremioHelperService.deleteFileFromR3IfExists(parquetFile);
            runRuleAndCreateParquet(createParquetWithSQL, parameters, fieldName, fileName, rs, dataTableResolver, validationResolver, ruleVO, method, object);
        } catch (Exception e1) {
            LOG.error("Error creating validation folder for ruleId {}, datasetId {} and tableName {}", ruleId, datasetId, tableName);
            throw e1;
        }
    }

    /**
     * Runs rule and creates parquet in S3
     * @param createParquetWithSQL
     * @param parameters
     * @param fieldName
     * @param rs
     * @param dataTableResolver
     * @param validationResolver
     * @param ruleVO
     * @param method
     * @param object
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IOException
     */
    private void runRuleAndCreateParquet(boolean createParquetWithSQL, List<String> parameters, String fieldName, String fileName, SqlRowSet rs,
                                        S3PathResolver dataTableResolver, S3PathResolver validationResolver, RuleVO ruleVO, Method method, Object object) throws Exception {
        if (createParquetWithSQL) {
            int count = 0;
            boolean createRuleFolder = false;
            //if the dataset to validate is of reference type, then the validation path should be changed
            StringBuilder validationQuery = dremioRulesService.getS3RuleFolderQueryBuilder(dataTableResolver.getDatasetId(), dataTableResolver.getTableName(), dataTableResolver, validationResolver, ruleVO, fieldName);
            while (rs.next()) {
                boolean isValid = isRecordValid(parameters, fieldName, rs, method, object);
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
                dremioHelperService.executeSqlStatement(validationQuery.toString());
            }
        } else {
            Map<String, String> headerMap = dremioRulesService.createValidationParquetHeaderMap(dataTableResolver.getDatasetId(), dataTableResolver.getTableName(), ruleVO, fieldName);
            StringBuilder pathBuilder = new StringBuilder();
            int ruleIdLength = ruleVO.getRuleId().length();
            //if the dataset to validate is of reference type, then the validation path should be changed
            String s3FilePath = pathBuilder.append(s3Service.getTableAsFolderQueryPath(validationResolver, S3_VALIDATION_TABLE_PATH)).append(SLASH).append(ruleVO.getShortCode()).append(DASH).append(ruleVO.getRuleId().substring(ruleIdLength - 3, ruleIdLength))
                    .append(SLASH).append(fileName).toString();
            String parquetFile = parquetFilePath + fileName;
            createParquetAndUploadToS3(parquetFile, parameters, fieldName, s3FilePath, headerMap, rs, method, object);
        }
    }

    /**
     * Creates parquet file and uploads it to S3
     * @param parquetFile
     * @param parameters
     * @param fieldName
     * @param s3FilePath
     * @param headerMap
     * @param rs
     * @param method
     * @param object
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IOException
     */
    private void createParquetAndUploadToS3(String parquetFile, List<String> parameters, String fieldName, String s3FilePath, Map<String, String> headerMap, SqlRowSet rs, Method method, Object object) throws Exception {
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
                    createRuleFolder = createParquetGenericRecord(parameters, fieldName, headerMap, rs, method, object, schema, writer, createRuleFolder);
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
     * @param parameters
     * @param fieldName
     * @param headerMap
     * @param rs
     * @param method
     * @param object
     * @param schema
     * @param writer
     * @param createRuleFolder
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IOException
     */
    private static boolean createParquetGenericRecord(List<String> parameters, String fieldName, Map<String, String> headerMap, SqlRowSet rs, Method method, Object object, Schema schema,
                                                      ParquetWriter<GenericRecord> writer, boolean createRuleFolder) throws IllegalAccessException, InvocationTargetException, IOException {
        boolean isValid = isRecordValid(parameters, fieldName, rs, method, object);
        if (!isValid) {
            GenericRecord record = new GenericData.Record(schema);
            for (Map.Entry<String,String> entry : headerMap.entrySet()) {
                record.put(entry.getKey(), entry.getValue());
            }
            record.put(PK, UUID.randomUUID().toString());
            record.put(PARQUET_RECORD_ID_COLUMN_HEADER, rs.getString(PARQUET_RECORD_ID_COLUMN_HEADER));
            writer.write(record);
            if (!createRuleFolder) {
                createRuleFolder = true;
            }
        }
        return createRuleFolder;
    }

    /**
     * Checks if record is valid
     * @param parameters
     * @param fieldName
     * @param rs
     * @param method
     * @param object
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static boolean isRecordValid(List<String> parameters, String fieldName, SqlRowSet rs, Method method, Object object) throws IllegalAccessException, InvocationTargetException {
        boolean isValid = false;
        int parameterLength = method.getParameters().length;
        switch (parameterLength) {
            case 1:
                isValid = (boolean) method.invoke(object, rs.getString(fieldName));  //DremioValidationUtils methods
                break;
            case 2:
                isValid = (boolean) method.invoke(object, rs.getString(fieldName), parameters.get(1));  //ValidationDroolsUtils methods
                break;
            case 3:
                isValid = (boolean) method.invoke(object, rs.getString(fieldName), parameters.get(1), Boolean.parseBoolean(parameters.get(2)));  //ValidationDroolsUtils codelistValidate method
                break;
        }
        return isValid;
    }
}
