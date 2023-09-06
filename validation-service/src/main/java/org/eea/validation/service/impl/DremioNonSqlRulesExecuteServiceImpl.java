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
    public void execute(Long dataflowId, Long datasetId, String datasetSchemaId, String tableName, String tableSchemaId, String ruleId, Long dataProviderId, Long taskId) throws Exception {
        String parquetFile = null;
        try {
            S3PathResolver dataTableResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, tableName);
            if (!s3Helper.checkFolderExist(dataTableResolver, S3_TABLE_NAME_FOLDER_PATH)) {
                return;
            }
            S3PathResolver validationResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, S3_VALIDATION);

            StringBuilder query = new StringBuilder();
            RuleVO ruleVO = rulesService.findRule(datasetSchemaId, ruleId);
            s3Helper.deleteRuleFolderIfExists(validationResolver, ruleVO);
            int startIndex = ruleVO.getWhenConditionMethod().indexOf("(");
            int endIndex = ruleVO.getWhenConditionMethod().indexOf(")");
            String ruleMethodName = ruleVO.getWhenConditionMethod().substring(0, startIndex);
            List<String> parameters = dremioRulesService.processRuleMethodParameters(ruleVO, startIndex, endIndex);
            if (ruleMethodName.equals(IS_MULTI_SELECT_CODE_LIST_VALIDATE)) {
                ruleMethodName = MULTI_SELECT_CODE_LIST_VALIDATE;
            } else if (ruleMethodName.equals(IS_CODE_LIST_INSENSITIVE)) {
                ruleMethodName = CODE_LIST_VALIDATE;
                parameters.add(FALSE);
            }

            String fieldName = datasetSchemaControllerZuul.getFieldName(datasetSchemaId, tableSchemaId, parameters, ruleVO.getReferenceId(), ruleVO.getReferenceFieldSchemaPKId());
            int ruleIdLength = ruleVO.getRuleId().length();
            String message = ruleVO.getThenCondition().get(0);
            message = message.replace("\'", "\"");
            String fileName = tableName + "_" + ruleVO.getShortCode() + PARQUET_TYPE;
            parquetFile = parquetFilePath + fileName;
            StringBuilder pathBuilder = new StringBuilder();
            String s3FilePath = pathBuilder.append(s3Service.getTableAsFolderQueryPath(validationResolver, S3_VALIDATION_TABLE_PATH)).append("/").append(ruleVO.getShortCode()).append("-").append(ruleVO.getRuleId().substring(ruleIdLength - 3, ruleIdLength))
                    .append("/").append(UUID.randomUUID()).append("/").append(fileName).toString();
            Map<String, String> headerMap = dremioRulesService.createValidationParquetHeaderMap(datasetId, tableName, ruleVO, fieldName, message);

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
            int parameterLength = method.getParameters().length;

            dremioHelperService.deleteFileFromR3IfExists(parquetFile);
            createParquetAndUploadToS3(datasetId, tableName, ruleId, parquetFile, parameters, fieldName, s3FilePath, headerMap, rs, method, object, parameterLength);
        } catch (Exception e1) {
            LOG.error("Error creating validation folder for ruleId {}, datasetId {} and tableName {}", ruleId, datasetId, tableName);
            throw e1;
        } finally {
            if (parquetFile!=null) {
                dremioHelperService.deleteFileFromR3IfExists(parquetFile);
            }
        }
    }

    private void createParquetAndUploadToS3(Long datasetId, String tableName, String ruleId, String parquetFile, List<String> parameters, String fieldName, String s3FilePath, Map<String, String> headerMap, SqlRowSet rs, Method method, Object object, int parameterLength) throws IllegalAccessException, InvocationTargetException, IOException {
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
                createRuleFolder = isRuleFolderCreated(parameters, fieldName, headerMap, rs, method, object, parameterLength, schema, createRuleFolder, writer);
            }
        } catch (Exception e) {
            LOG.error("Error creating parquet file {} for ruleId {}, datasetId {} and tableName {}", parquetFile, ruleId, datasetId, tableName);
            throw e;
        }
        if (createRuleFolder) {
            s3Helper.uploadFileToBucket(s3FilePath, parquetFile);
        }
    }

    private static boolean isRuleFolderCreated(List<String> parameters, String fieldName, Map<String, String> headerMap, SqlRowSet rs, Method method, Object object, int parameterLength, Schema schema, boolean createRuleFolder, ParquetWriter<GenericRecord> writer) throws IllegalAccessException, InvocationTargetException, IOException {
        boolean isValid = false;
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
        if (!isValid) {
            GenericRecord record = new GenericData.Record(schema);
            for (Map.Entry<String,String> entry : headerMap.entrySet()) {
                record.put(entry.getKey(), entry.getValue());
            }
            record.put(PK, UUID.randomUUID().toString());
            record.put(PARQUET_RECORD_ID_COLUMN_HEADER, rs.getString(PARQUET_RECORD_ID_COLUMN_HEADER));
            writer.write(record);
            createRuleFolder = true;
        }
        return createRuleFolder;
    }
}
