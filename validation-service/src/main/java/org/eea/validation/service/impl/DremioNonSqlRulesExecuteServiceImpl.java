package org.eea.validation.service.impl;

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
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.exception.DremioValidationException;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.validation.service.DremioRulesExecuteService;
import org.eea.validation.service.DremioRulesService;
import org.eea.validation.service.RulesService;
import org.eea.validation.util.GeoJsonValidationUtils;
import org.eea.validation.util.GeometryValidationUtils;
import org.eea.validation.util.ValidationHelper;
import org.locationtech.jts.io.ParseException;
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
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.eea.utils.LiteralConstants.*;

@ImportDataLakeCommons
@Service
public class DremioNonSqlRulesExecuteServiceImpl implements DremioRulesExecuteService {

    @Value("${parquet.file.path}")
    private String parquetFilePath;
    @Value("${validation.parquet.max.file.size}")
    private Integer validationParquetMaxFileSize;
    @Value("${validation.split.parquet}")
    private boolean validationSplitParquet;
    private final JdbcTemplate dremioJdbcTemplate;
    private final S3Service s3Service;
    private final RulesService rulesService;
    private final DatasetSchemaControllerZuul datasetSchemaControllerZuul;
    private final DremioRulesService dremioRulesService;
    private final S3Helper s3Helper;
    private final DremioHelperService dremioHelperService;
    private final ValidationHelper validationHelper;
    private final SpatialDataHandling spatialDataHandling;

    private static final String DREMIO_NON_SQL_VALIDATION_UTILS = "org.eea.validation.util.datalake.DremioNonSQLValidationUtils";
    private static final String VALIDATION_DROOLS_UTILS = "org.eea.validation.util.ValidationDroolsUtils";
    private static final String GEOMETRY_VALIDATION_UTILS = "org.eea.validation.util.GeometryValidationUtils";
    private static final String GEO_JSON_VALIDATION_UTILS = "org.eea.validation.util.GeoJsonValidationUtils";
    private static final String IS_MULTI_SELECT_CODE_LIST_VALIDATE = "isMultiSelectCodelistValidate";
    private static final String MULTI_SELECT_CODE_LIST_VALIDATE = "multiSelectCodelistValidate";
    private static final String IS_CODE_LIST_INSENSITIVE = "isCodelistInsensitive";
    private static final String CODE_LIST_VALIDATE = "codelistValidate";
    private static final String IS_GEOMETRY = "isGeometry";
    private static final String CHECK_EPSGSRID = "checkEPSGSRID";
    private static final String CHECK_EPSGSRID_VALIDATION = "checkEPSGSRIDValidation";
    private static final String VALIDATE_GEOMETRY_DREMIO = "validateGeometryDremio";
    private static final String FALSE = "false";
    private static final Logger LOG = LoggerFactory.getLogger(DremioNonSqlRulesExecuteServiceImpl.class);

    @Autowired
    public DremioNonSqlRulesExecuteServiceImpl(@Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, S3Service s3Service, RulesService rulesService,
                                               DatasetSchemaControllerZuul datasetSchemaControllerZuul, DremioRulesService dremioRulesService, S3Helper s3Helper,
                                               DremioHelperService dremioHelperService, ValidationHelper validationHelper, SpatialDataHandling spatialDataHandling) {
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.s3Service = s3Service;
        this.rulesService = rulesService;
        this.datasetSchemaControllerZuul = datasetSchemaControllerZuul;
        this.dremioHelperService = dremioHelperService;
        this.dremioRulesService = dremioRulesService;
        this.s3Helper = s3Helper;
        this.validationHelper = validationHelper;
        this.spatialDataHandling = spatialDataHandling;
    }

    @Override
    public void execute(Long dataflowId, Long datasetId, String datasetSchemaId, String tableName, String tableSchemaId, String ruleId, Long dataProviderId,
                        Long taskId, boolean createParquetWithSQL) throws DremioValidationException {
        try {
            //if the dataset to validate is of reference type, then the table path should be changed
            S3PathResolver dataTableResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, tableName);
            if (!s3Helper.checkFolderExist(dataTableResolver, S3_TABLE_NAME_FOLDER_PATH)) {
                return;
            }
            S3PathResolver validationResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, S3_VALIDATION);

            StringBuilder query = new StringBuilder();
            RuleVO ruleVO = rulesService.findRule(datasetSchemaId, ruleId);
            deleteRuleFolderIfExists(validationResolver, ruleVO);
            int startIndex = ruleVO.getWhenConditionMethod().indexOf(OPEN_PARENTHESIS);
            int endIndex = ruleVO.getWhenConditionMethod().indexOf(CLOSE_PARENTHESIS);
            String ruleMethodName = ruleVO.getWhenConditionMethod().substring(0, startIndex);
            List<String> parameters = dremioRulesService.processRuleMethodParameters(ruleVO, startIndex, endIndex);
          switch (ruleMethodName) {
            case IS_MULTI_SELECT_CODE_LIST_VALIDATE:
              ruleMethodName = MULTI_SELECT_CODE_LIST_VALIDATE;
              break;
            case IS_CODE_LIST_INSENSITIVE:
              ruleMethodName = CODE_LIST_VALIDATE;
              parameters.add(FALSE);
              break;
            case IS_GEOMETRY:
              ruleMethodName = VALIDATE_GEOMETRY_DREMIO;
              break;
            case CHECK_EPSGSRID:
              ruleMethodName = CHECK_EPSGSRID_VALIDATION;
              break;
          }

            String fieldName = datasetSchemaControllerZuul.getFieldName(datasetSchemaId, tableSchemaId, parameters, ruleVO.getReferenceId(), ruleVO.getReferenceFieldSchemaPKId());
            String fileName = datasetId + UNDERSCORE + tableName + UNDERSCORE + ruleVO.getShortCode();

            query.append("select record_id,").append(fieldName != null ? fieldName : "").append(" from ").append(s3Service.getTableAsFolderQueryPath(dataTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH));
            SqlRowSet rs = dremioJdbcTemplate.queryForRowSet(query.toString());

            Method method = null;
            List<String> classes = new ArrayList<>(Arrays.asList(DREMIO_NON_SQL_VALIDATION_UTILS, VALIDATION_DROOLS_UTILS, GEOMETRY_VALIDATION_UTILS, GEO_JSON_VALIDATION_UTILS));
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

            runRuleAndCreateParquet(createParquetWithSQL, parameters, fieldName, fileName, rs, dataTableResolver, validationResolver, ruleVO, method, object);
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
        s3Helper.deleteFolder(validationResolver, S3_TABLE_NAME_PATH);
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
            createParquetAndUploadToS3(ruleVO, validationResolver, fileName, parameters, fieldName, headerMap, rs, method, object);
        }
    }

    /**
     * Creates parquet file and uploads it to S3
     * @param parameters
     * @param fieldName
     * @param headerMap
     * @param rs
     * @param method
     * @param object
     * @throws Exception
     */
    private void createParquetAndUploadToS3(RuleVO ruleVO, S3PathResolver validationResolver, String fileName, List<String> parameters, String fieldName, Map<String,
                     String> headerMap, SqlRowSet rs, Method method, Object object) throws Exception {
        if (validationSplitParquet) {
            createSplitParquetFilesAndUploadToS3(ruleVO, validationResolver, fileName, parameters, fieldName, headerMap, rs, method, object);
        } else {
            createParquetFileAndUploadToS3(ruleVO, validationResolver, fileName, parameters, fieldName, headerMap, rs, method, object);
        }
    }

    /**
     * Creates split parquet files
     * @param ruleVO
     * @param validationResolver
     * @param fileName
     * @param parameters
     * @param fieldName
     * @param headerMap
     * @param rs
     * @param method
     * @param object
     * @throws Exception
     */
    private void createSplitParquetFilesAndUploadToS3(RuleVO ruleVO, S3PathResolver validationResolver, String fileName, List<String> parameters, String fieldName, Map<String,
            String> headerMap, SqlRowSet rs, Method method, Object object) throws Exception {
        Schema schema = getSchema();
        int ruleIdLength = ruleVO.getRuleId().length();
        List<String> recordIds = new ArrayList<>();
        while (rs.next()) {
            boolean isValid = isRecordValid(parameters, fieldName, rs, method, object);
            if (!isValid) {
                recordIds.add(rs.getString(PARQUET_RECORD_ID_COLUMN_HEADER));
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
                    LOG.error("Error creating parquet file {},{}", parquetFile, e1.getMessage());
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
     * @param ruleVO
     * @param validationResolver
     * @param fileName
     * @param parameters
     * @param fieldName
     * @param headerMap
     * @param rs
     * @param method
     * @param object
     * @throws Exception
     */
    private void createParquetFileAndUploadToS3(RuleVO ruleVO, S3PathResolver validationResolver, String fileName, List<String> parameters, String fieldName, Map<String,
            String> headerMap, SqlRowSet rs, Method method, Object object) throws Exception {
        int parquetRecordCount = 0;
        String subFile = fileName + PARQUET_TYPE;
        String parquetFile = parquetFilePath + subFile;
        Schema schema = getSchema();
        dremioHelperService.deleteFileFromR3IfExists(parquetFile);
        try (ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(new Path(parquetFile)).withSchema(schema)
                .withCompressionCodec(CompressionCodecName.SNAPPY).withPageSize(4 * 1024).withRowGroupSize(16 * 1024).build()) {
            while (rs.next()) {
                boolean isValid = isRecordValid(parameters, fieldName, rs, method, object);
                if (!isValid) {
                    if (parquetRecordCount==0) {
                        parquetRecordCount++;
                    }
                    GenericRecord record = createParquetGenericRecord(headerMap, rs.getString(PARQUET_RECORD_ID_COLUMN_HEADER), schema);
                    writer.write(record);
                }
            }
            if (parquetRecordCount > 0) {
                writer.close();
                validationHelper.uploadValidationParquetToS3(ruleVO, validationResolver, subFile, ruleVO.getRuleId().length(), parquetFile);
            }
        } catch (Exception e) {
            LOG.error("Error creating parquet file {},{}", parquetFile, e.getMessage());
            throw e;
        } finally {
            dremioHelperService.deleteFileFromR3IfExists(parquetFile);
        }
    }

    /**
     * Creates schema
     * @return
     */
    private static Schema getSchema() {
        List<String> parquetHeaders = Arrays.asList(PK, PARQUET_RECORD_ID_COLUMN_HEADER, VALIDATION_LEVEL, VALIDATION_AREA, MESSAGE, TABLE_NAME, FIELD_NAME, DATASET_ID, QC_CODE);
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
        GenericRecord record = new GenericData.Record(schema);
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            record.put(entry.getKey(), entry.getValue());
        }
        record.put(PK, UUID.randomUUID().toString());
        record.put(PARQUET_RECORD_ID_COLUMN_HEADER, recordId);
        return record;
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
    private boolean isRecordValid(List<String> parameters, String fieldName, SqlRowSet rs, Method method, Object object) throws IllegalAccessException, InvocationTargetException {
        boolean isValid = false;
        if (object instanceof GeoJsonValidationUtils || object instanceof GeometryValidationUtils) {
            if (object instanceof GeoJsonValidationUtils) {
                try {
                    String modifiedJson = spatialDataHandling.decodeSpatialData((byte[]) rs.getObject(fieldName));
                    if (method.getName().equals(VALIDATE_GEOMETRY_DREMIO)) {
                        //GeoJsonValidationUtils.validateGeometryDremio
                        isValid = (boolean) method.invoke(object, modifiedJson, spatialDataHandling.getGeometryType((byte[]) rs.getObject(fieldName)));
                    }
                } catch (IOException | ParseException e) {
                    LOG.error("Invalid GeoJson!! Tried to decode from binary but failed", e);
                }
            } else {
                try {
                    String modifiedJson = spatialDataHandling.decodeSpatialData((byte[]) rs.getObject(fieldName));
                    if (method.getName().equals(CHECK_EPSGSRID_VALIDATION)){
                        //GeometryValidationUtils.checkEPSGSRIDValidation
                        isValid = (boolean) method.invoke(object, modifiedJson);
                    }
                } catch (IOException | ParseException e) {
                    LOG.error("Invalid GeoJson!! Tried to decode from binary but failed", e);
                }
            }
        } else {
            int parameterLength = method.getParameters().length;
            switch (parameterLength) {
                case 1:
                    isValid = (boolean) method.invoke(object, getConvertedString(rs, fieldName));  //DremioNonSQLValidationUtils methods
                    break;
                case 2:
                    isValid = (boolean) method.invoke(object, getConvertedString(rs, fieldName), parameters.get(1));  //ValidationDroolsUtils methods
                    break;
                case 3:
                    isValid = (boolean) method.invoke(object, getConvertedString(rs, fieldName), parameters.get(1), Boolean.parseBoolean(parameters.get(2)));  //ValidationDroolsUtils codelistValidate method
                    break;
            }
        }
        return isValid;
    }

    private String getConvertedString(SqlRowSet rs, String fieldName) {
        if (rs.getObject(fieldName) instanceof byte[]) {
            return new String((byte[]) rs.getObject(fieldName), StandardCharsets.UTF_8);
        }
            return rs.getString(fieldName);
    }
}
