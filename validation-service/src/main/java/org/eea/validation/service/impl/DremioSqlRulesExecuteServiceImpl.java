package org.eea.validation.service.impl;

import com.google.common.collect.Lists;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.bson.types.ObjectId;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.exception.DremioValidationException;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.service.DremioRulesExecuteService;
import org.eea.validation.service.DremioRulesService;
import org.eea.validation.service.RulesService;
import org.eea.validation.service.SqlRulesService;
import org.eea.validation.util.FKValidationUtils;
import org.eea.validation.util.UniqueValidationUtils;
import org.eea.validation.util.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.eea.utils.LiteralConstants.*;

@ImportDataLakeCommons
@Service
public class DremioSqlRulesExecuteServiceImpl implements DremioRulesExecuteService {

    @Value("${parquet.file.path}")
    private String parquetFilePath;
    @Value("${validation.parquet.max.file.size}")
    private Integer validationParquetMaxFileSize;
    @Value("${validation.split.parquet}")
    private boolean validationSplitParquet;
    private JdbcTemplate dremioJdbcTemplate;
    private S3Service s3Service;
    private RulesService rulesService;
    private DatasetSchemaControllerZuul datasetSchemaControllerZuul;
    private DremioRulesService dremioRulesService;
    private S3Helper s3Helper;
    private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;
    private SchemasRepository schemasRepository;
    private DataSetControllerZuul dataSetControllerZuul;
    private SqlRulesService sqlRulesService;
    private DremioHelperService dremioHelperService;
    private RepresentativeControllerZuul representativeControllerZuul;
    private ValidationHelper validationHelper;

    private static final Logger LOG = LoggerFactory.getLogger(DremioSqlRulesExecuteServiceImpl.class);
    private static final String IS_TABLE_EMPTY = "isTableEmpty";
    private static final String TABLE_EMPTY = "tableEmpty";
    private static final String AS_MESSAGE = " as message";
    private static final String COMMA_RECORD_ID = ",record_id";
    private static final String DOUBLE_QUOTATION_MARK = ",\'\'";
    private static final String DREMIO_SQL_VALIDATION_UTILS = "org.eea.validation.util.datalake.DremioSQLValidationUtils";


    @Autowired
    public DremioSqlRulesExecuteServiceImpl(@Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, S3Service s3Service, RulesService rulesService,
                                            DatasetSchemaControllerZuul datasetSchemaControllerZuul, DremioRulesService dremioRulesService, S3Helper s3Helper,
                                            DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul, SchemasRepository schemasRepository,
                                            DataSetControllerZuul dataSetControllerZuul, SqlRulesService sqlRulesService, DremioHelperService dremioHelperService,
                                            RepresentativeControllerZuul representativeControllerZuul, ValidationHelper validationHelper) {
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.s3Service = s3Service;
        this.rulesService = rulesService;
        this.datasetSchemaControllerZuul = datasetSchemaControllerZuul;
        this.dremioRulesService = dremioRulesService;
        this.dataSetMetabaseControllerZuul = dataSetMetabaseControllerZuul;
        this.schemasRepository = schemasRepository;
        this.dataSetControllerZuul = dataSetControllerZuul;
        this.s3Helper = s3Helper;
        this.sqlRulesService = sqlRulesService;
        this.dremioHelperService = dremioHelperService;
        this.representativeControllerZuul = representativeControllerZuul;
        this.validationHelper = validationHelper;
    }

    @Override
    public void execute(Long dataflowId, Long datasetId, String datasetSchemaId, String tableName, String tableSchemaId, String ruleId, Long dataProviderId,
                        Long taskId, boolean createParquetWithSQL) throws DremioValidationException {
        try {
            //if the dataset to validate is of reference type, then the table path should be changed
            S3PathResolver dataTableResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, tableName);
            String tablePath = s3Service.getTableAsFolderQueryPath(dataTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
            S3PathResolver validationResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, S3_VALIDATION);
            RuleVO ruleVO = rulesService.findRule(datasetSchemaId, ruleId);
            deleteRuleFolderIfExists(validationResolver, ruleVO);
            int startIndex = ruleVO.getWhenConditionMethod().indexOf(OPEN_PARENTHESIS);
            int endIndex = ruleVO.getWhenConditionMethod().indexOf(CLOSE_PARENTHESIS);
            String ruleMethodName = ruleVO.getWhenConditionMethod().substring(0, startIndex);
            List<String> recordIds = new ArrayList<>();

            if (!s3Helper.checkFolderExist(dataTableResolver, S3_TABLE_NAME_FOLDER_PATH)) {
                if (ruleMethodName.equals(IS_TABLE_EMPTY)) {
                    recordIds.add(TABLE_EMPTY);
                } else {
                    return;
                }
            }

            List<String> parameters = dremioRulesService.processRuleMethodParameters(ruleVO, startIndex, endIndex);
            String fieldName = datasetSchemaControllerZuul.getFieldName(datasetSchemaId, tableSchemaId, parameters, ruleVO.getReferenceId(), ruleVO.getReferenceFieldSchemaPKId());
            if (fieldName==null) {
                fieldName = "";
            }

            String fileName = datasetId + UNDERSCORE + tableName + UNDERSCORE + ruleVO.getShortCode();

            if (!ruleMethodName.equals(IS_TABLE_EMPTY)) {
                Class<?> cls = Class.forName(DREMIO_SQL_VALIDATION_UTILS);
                Field[] fields = cls.getDeclaredFields();
                Method factoryMethod = cls.getDeclaredMethod(GET_INSTANCE);
                Object object = factoryMethod.invoke(null, null);
                Field field = Arrays.stream(fields).findFirst().get();
                field.setAccessible(true);
                field.set(object, dremioJdbcTemplate);
                Method method = dremioRulesService.getRuleMethodFromClass(ruleMethodName, cls);

                recordIds = getRecordIds(dataTableResolver, tableSchemaId, tablePath, ruleVO, recordIds, parameters, fieldName, object, method);
            }

            if (recordIds.size() > 0) {
                runRuleAndCreateParquet(createParquetWithSQL, dataTableResolver, validationResolver, ruleVO, recordIds, fieldName, fileName);
            }
        } catch (Exception e) {
            LOG.error("Error creating validation folder for ruleId {}, datasetId {} and tableName {},{}", ruleId, datasetId, tableName, e.getMessage());
            throw new DremioValidationException(e.getMessage());
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
     * runs rule and creates parquet in S3
     * @param createParquetWithSQL
     * @param dataTableResolver
     * @param validationResolver
     * @param ruleVO
     * @param recordIds
     * @param fieldName
     * @throws IOException
     */
    private void runRuleAndCreateParquet(boolean createParquetWithSQL, S3PathResolver dataTableResolver, S3PathResolver validationResolver, RuleVO ruleVO, List<String> recordIds,
                                         String fieldName, String fileName) throws Exception {
        int ruleIdLength = ruleVO.getRuleId().length();
        if (createParquetWithSQL) {
            //if the dataset to validate is of reference type, then the validation path should be changed
            StringBuilder validationQuery = dremioRulesService.getS3RuleFolderQueryBuilder(dataTableResolver.getDatasetId(), dataTableResolver.getTableName(), dataTableResolver, validationResolver, ruleVO, fieldName);
            int count = 0;
            for (String recordId : recordIds) {
                if (count != 0) {
                    validationQuery.append(",'");
                }
                validationQuery.append(recordId).append("'");
                if (count == 0) {
                    count++;
                }
            }
            validationQuery.append("))");
            String valQuery = validationQuery.toString();
            if (recordIds.size() > 0) {
                valQuery = processValidationQuery(dataTableResolver, ruleVO, fieldName, ruleIdLength, valQuery);
                dremioHelperService.executeSqlStatement(valQuery);
            }
        } else {
            //Defining schema
            List<String> parquetHeaders = Arrays.asList(PK, PARQUET_RECORD_ID_COLUMN_HEADER, VALIDATION_LEVEL, VALIDATION_AREA, MESSAGE, TABLE_NAME, FIELD_NAME, DATASET_ID, QC_CODE);
            List<Schema.Field> fields = new ArrayList<>();
            for (String header : parquetHeaders) {
                fields.add(new Schema.Field(header, Schema.create(Schema.Type.STRING), null, null));
            }
            Schema schema = Schema.createRecord("Data", null, null, false, fields);
            Map<String, String> headerMap = dremioRulesService.createValidationParquetHeaderMap(dataTableResolver.getDatasetId(), dataTableResolver.getTableName(), ruleVO, fieldName);
            if (recordIds.size()>0) {
                createParquetAndUploadToS3(ruleVO, recordIds, headerMap, schema, fileName, validationResolver);
            }
        }
    }

    /**
     * processes validation query
     * @param dataTableResolver
     * @param ruleVO
     * @param fieldName
     * @param ruleIdLength
     * @param valQuery
     * @return
     */
    private String processValidationQuery(S3PathResolver dataTableResolver, RuleVO ruleVO, String fieldName, int ruleIdLength, String valQuery) {
        if (valQuery.contains(PK_NOT_USED)) {
            valQuery = getModifiedQuery(dataTableResolver, valQuery, "('pkNotUsed')");
            valQuery = valQuery.replace(COMMA_RECORD_ID, DOUBLE_QUOTATION_MARK);
            valQuery = valQuery.replace(fieldName, "");
        } else if (valQuery.contains(TABLE_EMPTY)) {
            valQuery = getModifiedQuery(dataTableResolver, valQuery, "('tableEmpty')");
            valQuery = valQuery.replace(COMMA_RECORD_ID, DOUBLE_QUOTATION_MARK);
        } else if (valQuery.contains(OMISSION)) {
            if (valQuery.contains("('OMISSION','COMISSION')")) {
                valQuery = getModifiedQuery(dataTableResolver, valQuery, "('OMISSION','COMISSION')");
                valQuery = valQuery.replace(COMMA_RECORD_ID, DOUBLE_QUOTATION_MARK);
                valQuery = valQuery.replace("'" + ruleVO.getThenCondition().get(0) + "'" + AS_MESSAGE, "'" + ruleVO.getThenCondition().get(0) + " (COMISSION)'" + AS_MESSAGE);
                dremioJdbcTemplate.execute(valQuery);
                String ruleFolderName = ruleVO.getShortCode()+"-"+ ruleVO.getRuleId().substring(ruleIdLength -3, ruleIdLength);
                valQuery = valQuery.replace(ruleFolderName, ruleFolderName+"-om");
                valQuery = valQuery.replace(COMISSION, OMISSION);
                dremioJdbcTemplate.execute(valQuery);
                return null;
            } else if (valQuery.contains(COMISSION)) {
                valQuery = getModifiedQuery(dataTableResolver, valQuery, "('COMISSION')");
                valQuery = valQuery.replace(COMMA_RECORD_ID, DOUBLE_QUOTATION_MARK);
                valQuery = valQuery.replace("'" + ruleVO.getThenCondition().get(0) + "'" + AS_MESSAGE, "'" + ruleVO.getThenCondition().get(0) + " (COMISSION)'" + AS_MESSAGE);
            } else {
                valQuery = getModifiedQuery(dataTableResolver, valQuery, "('OMISSION')");
                valQuery = valQuery.replace(COMMA_RECORD_ID, DOUBLE_QUOTATION_MARK);
                valQuery = valQuery.replace("'" + ruleVO.getThenCondition().get(0) + "'" + AS_MESSAGE, "'" + ruleVO.getThenCondition().get(0) + " (OMISSION)'" + AS_MESSAGE);

            }
        }
        return valQuery;
    }

    private String getModifiedQuery(S3PathResolver dataTableResolver, String valQuery, String replace) {
        valQuery = valQuery.replace("from " + s3Service.getTableAsFolderQueryPath(dataTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH) + " where record_id in " + replace, "");
        return valQuery;
    }

    /**
     * Gets recordIds
     * @param datatableResolver
     * @param tableSchemaId
     * @param tablePath
     * @param ruleVO
     * @param recordIds
     * @param parameters
     * @param fieldName
     * @param object
     * @param method
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private List<String> getRecordIds(S3PathResolver datatableResolver, String tableSchemaId, String tablePath, RuleVO ruleVO, List<String> recordIds, List<String> parameters, String fieldName, Object object, Method method) throws IllegalAccessException, InvocationTargetException {
        int parameterLength = method.getParameters().length;
        switch (parameterLength) {
            case 1:
                DataSetMetabaseVO dataSetMetabaseVO = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datatableResolver.getDatasetId());
                String sqlCode = sqlRulesService.proccessQuery(dataSetMetabaseVO, ruleVO.getSqlSentence());
                sqlCode = sqlRulesService.replaceTableNamesWithS3Path(sqlCode);
                String providerCode = "XX";
                if (dataSetMetabaseVO.getDataProviderId()!=null && dataSetMetabaseVO.getDataProviderId()!=0) {
                    DataProviderVO provider = representativeControllerZuul.findDataProviderById(dataSetMetabaseVO.getDataProviderId());
                    providerCode = provider.getCode();
                }
                sqlCode = sqlCode.replace("{%R3_COUNTRY_CODE%}", providerCode);
                sqlCode = sqlCode.replace("{%R3_COMPANY_CODE%}", providerCode);
                sqlCode = sqlCode.replace("{%R3_ORGANIZATION_CODE%}", providerCode);
                recordIds = (List<String>) method.invoke(object, sqlCode);    //isSQLSentenceWithCode
                break;
            case 2:
                recordIds = (List<String>) method.invoke(object, fieldName, tablePath);  //isUniqueConstraint
                break;
            case 5:
                //checkIntegrityConstraint
                recordIds = getDheckIntegrityConstraintRecordIds(datatableResolver.getDataflowId(), datatableResolver.getDatasetId(), datatableResolver.getDataProviderId(), parameters, object, method);
                break;
            case 8:
                //isfieldFK
                recordIds = getIsFieldFKRecordIds(datatableResolver.getDataflowId(), datatableResolver.getDatasetId(), tableSchemaId, datatableResolver.getDataProviderId(), tablePath, parameters, object, method);
                break;
        }
        return recordIds;
    }

    /**
     * Gets recordsIds in case of isfieldFK rule
     * @param dataflowId
     * @param datasetId
     * @param tableSchemaId
     * @param dataProviderId
     * @param tablePath
     * @param parameters
     * @param object
     * @param method
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private List<String> getIsFieldFKRecordIds(Long dataflowId, Long datasetId, String tableSchemaId, Long dataProviderId, String tablePath, List<String> parameters, Object object, Method method) throws IllegalAccessException, InvocationTargetException {
        List<String> recordIds;
        String fkDatasetSchemaId = dataSetMetabaseControllerZuul.findDatasetSchemaIdById(datasetId);
        String idFieldSchema = parameters.get(1);
        boolean pkMustBeUsed = Boolean.parseBoolean(parameters.get(3));
        DataSetSchema datasetSchemaFK =
                schemasRepository.findByIdDataSetSchema(new ObjectId(fkDatasetSchemaId));
        String idFieldSchemaPKString = FKValidationUtils.getIdFieldSchemaPK(idFieldSchema, datasetSchemaFK);
        FieldSchema fkFieldSchema = FKValidationUtils.getPKFieldFromFKField(datasetSchemaFK, idFieldSchema);
        Long datasetIdRefered =
                dataSetControllerZuul.getReferencedDatasetId(datasetId, idFieldSchemaPKString);

        DataSetSchema datasetSchemaPK = null;
        if (datasetId ==datasetIdRefered) {
            datasetSchemaPK = datasetSchemaFK;
        } else {
            String pkDatasetSchemaId = dataSetMetabaseControllerZuul.findDatasetSchemaIdById(datasetIdRefered);
            datasetSchemaPK =
                    schemasRepository.findByIdDataSetSchema(new ObjectId(pkDatasetSchemaId));
        }
        String foreignKey = fkFieldSchema.getHeaderName();
        List<String> pkAndFkDetailsList = getPkAndFkHeaderValues(datasetSchemaPK, datasetSchemaFK, fkFieldSchema, tableSchemaId);
        String pkTableName = pkAndFkDetailsList.get(0);
        String primaryKey = pkAndFkDetailsList.get(1);
        String optionalPK = pkAndFkDetailsList.get(2);
        String optionalFK = pkAndFkDetailsList.get(3);
        S3PathResolver pkTableResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetIdRefered, pkTableName);
        String pkTablePath = s3Service.getTablePathByDatasetType(datasetSchemaPK.getIdDataFlow(), datasetIdRefered, pkTableName, pkTableResolver);
        recordIds = (List<String>) method.invoke(object, fkFieldSchema, pkMustBeUsed, tablePath, pkTablePath, foreignKey, primaryKey, optionalFK, optionalPK);  //isfieldFK
        return recordIds;
    }

    /**
     * Gets recordIds in case of checkIntegrityConstraint rule
     * @param dataflowId
     * @param datasetId
     * @param dataProviderId
     * @param parameters
     * @param object
     * @param method
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private List<String> getDheckIntegrityConstraintRecordIds(Long dataflowId, Long datasetId, Long dataProviderId, List<String> parameters, Object object, Method method) throws IllegalAccessException, InvocationTargetException {
        List<String> recordIds;
        List<String> origFieldNames = new ArrayList<>();
        List<String> referFieldNames = new ArrayList<>();
        DataSetSchema originDatasetSchema;
        DataSetSchema referDatasetSchema;
        IntegrityVO integrityVO = rulesService.getIntegrityConstraint(parameters.get(1));
        long datasetIdOrigin = datasetId;
        String originSchemaId = integrityVO.getOriginDatasetSchemaId();
        String referencedSchemaId = integrityVO.getReferencedDatasetSchemaId();
        long datasetIdReferenced;
        originDatasetSchema = schemasRepository.findByIdDataSetSchema(new ObjectId(originSchemaId));
        if (originSchemaId.equals(referencedSchemaId)) {
            datasetIdReferenced = datasetIdOrigin;
            referDatasetSchema = originDatasetSchema;
        } else {
            datasetIdReferenced = dataSetMetabaseControllerZuul.getIntegrityDatasetId(datasetIdOrigin,
                    integrityVO.getOriginDatasetSchemaId(), integrityVO.getReferencedDatasetSchemaId());
            referDatasetSchema = schemasRepository.findByIdDataSetSchema(new ObjectId(referencedSchemaId));
        }
        TableSchema originTableSchema = UniqueValidationUtils.getTableSchemaFromIdFieldSchema(originDatasetSchema, integrityVO.getOriginFields().get(0));
        TableSchema referencedTableSchema = UniqueValidationUtils.getTableSchemaFromIdFieldSchema(referDatasetSchema, integrityVO.getReferencedFields().get(0));
        integrityVO.getOriginFields().forEach(originField -> {
            FieldSchemaVO fieldSchema = datasetSchemaControllerZuul.getFieldSchema(originSchemaId, originField);
            origFieldNames.add(fieldSchema.getName());
        });
        integrityVO.getReferencedFields().forEach(referField -> {
            FieldSchemaVO fieldSchema = datasetSchemaControllerZuul.getFieldSchema(referencedSchemaId, referField);
            referFieldNames.add(fieldSchema.getName());
        });
        S3PathResolver origTableTableResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetIdOrigin, originTableSchema.getNameTableSchema());
        //if the dataset to validate is of reference type, then the table path should be changed
        String originTablePath = s3Service.getTableAsFolderQueryPath(origTableTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
        S3PathResolver referTableResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetIdReferenced, referencedTableSchema.getNameTableSchema());
        String referTablePath = s3Service.getTablePathByDatasetType(dataflowId, datasetIdReferenced, referencedTableSchema.getNameTableSchema(), referTableResolver);
        recordIds =  (List<String>) method.invoke(object, originTablePath, referTablePath, origFieldNames, referFieldNames, integrityVO.getIsDoubleReferenced());  //checkIntegrityConstraint
        return recordIds;
    }

    /**
     * creates parquet file
     * @param ruleVO
     * @param recordIds
     * @param headerMap
     * @param schema
     * @throws Exception
     */
    private void createParquetAndUploadToS3(RuleVO ruleVO, List<String> recordIds, Map<String, String> headerMap, Schema schema, String fileName,
                                            S3PathResolver validationResolver) throws Exception {
        if (validationSplitParquet) {
            createSplitParquetFilesAndUploadToS3(ruleVO, recordIds, headerMap, schema, fileName, validationResolver);
        } else {
            createParquetFileAndUploadToS3(ruleVO, recordIds, headerMap, schema, fileName, validationResolver);
        }
    }

    /**
     * Creates split parquet files
     * @param ruleVO
     * @param recordIds
     * @param headerMap
     * @param schema
     * @param fileName
     * @param validationResolver
     * @throws Exception
     */
    private void createSplitParquetFilesAndUploadToS3(RuleVO ruleVO, List<String> recordIds, Map<String, String> headerMap, Schema schema, String fileName,
                                                      S3PathResolver validationResolver) throws Exception {
        int count = 1;
        int ruleIdLength = ruleVO.getRuleId().length();
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
                        GenericRecord record = createParquetGenericRecord(ruleVO, headerMap, schema, recordId);
                        writer.write(record);
                    }
                } catch (Exception e1) {
                    LOG.error("Error creating parquet file {},{]", parquetFile, e1.getMessage());
                    throw e1;
                }
                validationHelper.uploadValidationParquetToS3(ruleVO, validationResolver, subFile, ruleIdLength, parquetFile);
                count++;
            } finally {
                dremioHelperService.deleteFileFromR3IfExists(parquetFile);
            }
        }
    }

    /**
     * Creates parquet file
     * @param ruleVO
     * @param recordIds
     * @param headerMap
     * @param schema
     * @param fileName
     * @param validationResolver
     * @throws Exception
     */
    private void createParquetFileAndUploadToS3(RuleVO ruleVO, List<String> recordIds, Map<String, String> headerMap, Schema schema, String fileName,
                                                S3PathResolver validationResolver) throws Exception {
        String file = fileName + PARQUET_TYPE;
        String parquetFile = parquetFilePath + file;
        int ruleIdLength = ruleVO.getRuleId().length();
        try {
            dremioHelperService.deleteFileFromR3IfExists(parquetFile);
            try (ParquetWriter<GenericRecord> writer = AvroParquetWriter
                    .<GenericRecord>builder(new Path(parquetFile))
                    .withSchema(schema)
                    .withCompressionCodec(CompressionCodecName.SNAPPY)
                    .withPageSize(4 * 1024)
                    .withRowGroupSize(16 * 1024)
                    .build()) {

                for (String recordId : recordIds) {
                    GenericRecord record = createParquetGenericRecord(ruleVO, headerMap, schema, recordId);
                    writer.write(record);
                }
            } catch (Exception e1) {
                LOG.error("Error creating parquet file {},{]", parquetFile, e1.getMessage());
                throw e1;
            }
           validationHelper.uploadValidationParquetToS3(ruleVO, validationResolver, file, ruleIdLength, parquetFile);
        } finally {
            dremioHelperService.deleteFileFromR3IfExists(parquetFile);
        }
    }

    /**
     * Creates parquet generic record
     * @param ruleVO
     * @param headerMap
     * @param schema
     * @param recordId
     * @return
     */
    private static GenericRecord createParquetGenericRecord(RuleVO ruleVO, Map<String, String> headerMap, Schema schema, String recordId) {
        GenericRecord record = new GenericData.Record(schema);

        if (recordId.equals(PK_NOT_USED) || recordId.equals(TABLE_EMPTY)) {
            recordId = "";
        } else if (recordId.equals(COMISSION)) {
            recordId = "";
            headerMap.put(MESSAGE, ruleVO.getThenCondition().get(0) + " (COMISSION)");
        } else if (recordId.equals(OMISSION)) {
            recordId = "";
            headerMap.put(MESSAGE, ruleVO.getThenCondition().get(0) + " (OMISSION)");
        }

        for (Map.Entry<String,String> entry : headerMap.entrySet()) {
            record.put(entry.getKey(), entry.getValue());
        }
        record.put(PK, UUID.randomUUID().toString());
        record.put(PARQUET_RECORD_ID_COLUMN_HEADER, recordId);
        return record;
    }

    /**
     * Finds primary key and foreign key field name values
     * @param datasetSchemaPK
     * @param datasetSchemaFK
     * @param fkFieldSchema
     * @param tableSchemaId
     * @return
     */
    private List<String> getPkAndFkHeaderValues(DataSetSchema datasetSchemaPK, DataSetSchema datasetSchemaFK, FieldSchema fkFieldSchema, String tableSchemaId) {
        String primaryKey = null;
        String optionalFK = null;
        String optionalPK = null;
        String pkTableName = null;
        List<String> pkAndFkDetailsList = new ArrayList<>();
        for (TableSchema tableSchema : datasetSchemaPK.getTableSchemas()) {
            for (FieldSchema pKFieldSchema : tableSchema.getRecordSchema().getFieldSchema()) {
                if (pKFieldSchema.getIdFieldSchema().toString().equals(fkFieldSchema.getReferencedField().getIdPk().toString())) {
                    pkTableName = tableSchema.getNameTableSchema();
                    primaryKey = pKFieldSchema.getHeaderName();
                    ObjectId optionalPKId = fkFieldSchema.getReferencedField().getLinkedConditionalFieldId();
                    if (optionalPKId!=null) {
                        Optional<FieldSchema> optionalPKValue = tableSchema.getRecordSchema().getFieldSchema().stream().filter(f -> f.getIdFieldSchema().toString().equals(optionalPKId.toString())).findFirst();
                        if (optionalPKValue.isPresent()) {
                            optionalPK = optionalPKValue.get().getHeaderName();
                        }
                    }
                    ObjectId optionalFKId = fkFieldSchema.getReferencedField().getMasterConditionalFieldId();
                    if (optionalFKId!=null) {
                        Optional<FieldSchema> optionalFKValue = datasetSchemaFK.getTableSchemas().stream().filter(t -> t.getIdTableSchema().toString().equals(tableSchemaId))
                                .findFirst().get().getRecordSchema().getFieldSchema().stream().filter(f -> f.getIdFieldSchema().toString().equals(optionalFKId.toString())).findFirst();
                        if (optionalFKValue.isPresent()) {
                            optionalFK = optionalFKValue.get().getHeaderName();
                        }
                    }
                    break;
                }
            }
        }
        pkAndFkDetailsList.add(pkTableName);
        pkAndFkDetailsList.add(primaryKey);
        pkAndFkDetailsList.add(optionalPK);
        pkAndFkDetailsList.add(optionalFK);
        return pkAndFkDetailsList;
    }
}





















