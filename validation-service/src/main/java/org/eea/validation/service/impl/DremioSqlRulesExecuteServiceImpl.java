package org.eea.validation.service.impl;

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
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
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

    private static final Logger LOG = LoggerFactory.getLogger(DremioSqlRulesExecuteServiceImpl.class);
    private static final String IS_TABLE_EMPTY = "isTableEmpty";
    private static final String TABLE_EMPTY = "tableEmpty";
    private static final String DREMIO_SQL_VALIDATION_UTILS = "org.eea.validation.util.datalake.DremioSQLValidationUtils";


    @Autowired
    public DremioSqlRulesExecuteServiceImpl(@Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, S3Service s3Service, RulesService rulesService,
                                            DatasetSchemaControllerZuul datasetSchemaControllerZuul, DremioRulesService dremioRulesService, S3Helper s3Helper,
                                            DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul, SchemasRepository schemasRepository,
                                            DataSetControllerZuul dataSetControllerZuul, SqlRulesService sqlRulesService, DremioHelperService dremioHelperService) {
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
    }

    @Override
    public void execute(Long dataflowId, Long datasetId, String datasetSchemaId, String tableName, String tableSchemaId, String ruleId, Long dataProviderId, Long taskId) throws Exception {
        String parquetFile = null;
        try {
            S3PathResolver dataTableResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, tableName);
            String tablePath = s3Service.getTableAsFolderQueryPath(dataTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
            S3PathResolver validationResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, S3_VALIDATION);
            RuleVO ruleVO = rulesService.findRule(datasetSchemaId, ruleId);
            s3Helper.deleteRuleFolderIfExists(validationResolver, ruleVO);
            int startIndex = ruleVO.getWhenConditionMethod().indexOf("(");
            int endIndex = ruleVO.getWhenConditionMethod().indexOf(")");
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

            int ruleIdLength = ruleVO.getRuleId().length();
            String message = ruleVO.getThenCondition().get(0);
            message = message.replace("\'", "\"");
            String fileName = tableName + "_" + ruleVO.getShortCode() + PARQUET_TYPE;
            parquetFile = parquetFilePath + fileName;
            StringBuilder pathBuilder = new StringBuilder().append(s3Service.getTableAsFolderQueryPath(validationResolver, S3_VALIDATION_TABLE_PATH)).append("/").append(ruleVO.getShortCode()).append("-").append(ruleVO.getRuleId().substring(ruleIdLength - 3, ruleIdLength));
            String s3FilePath = pathBuilder.append("/").append(UUID.randomUUID()).append("/").append(fileName).toString();
            Map<String, String> headerMap = dremioRulesService.createValidationParquetHeaderMap(datasetId, tableName, ruleVO, fieldName, message);

            if (!ruleMethodName.equals(IS_TABLE_EMPTY)) {
                Class<?> cls = Class.forName(DREMIO_SQL_VALIDATION_UTILS);
                Field[] fields = cls.getDeclaredFields();
                Method factoryMethod = cls.getDeclaredMethod(GET_INSTANCE);
                Object object = factoryMethod.invoke(null, null);
                Field field = Arrays.stream(fields).findFirst().get();
                field.setAccessible(true);
                field.set(object, dremioJdbcTemplate);
                Method method = dremioRulesService.getRuleMethodFromClass(ruleMethodName, cls);

                dremioHelperService.deleteFileFromR3IfExists(parquetFile);

                int parameterLength = method.getParameters().length;
                switch (parameterLength) {
                    case 1:
                        DataSetMetabaseVO dataSetMetabaseVO = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
                        String sqlCode = sqlRulesService.proccessQuery(dataSetMetabaseVO, ruleVO.getSqlSentence());
                        sqlCode = sqlRulesService.replaceTableNamesWithS3Path(sqlCode);
                        recordIds = (List<String>) method.invoke(object, sqlCode);    //isSQLSentenceWithCode
                        break;
                    case 2:
                        recordIds = (List<String>) method.invoke(object, fieldName, tablePath);  //isUniqueConstraint
                        break;
                    case 5:
                        //checkIntegrityConstraint
                        recordIds = getDheckIntegrityConstraintRecordIds(dataflowId, datasetId, dataProviderId, parameters, object, method);
                        break;
                    case 8:
                        //isfieldFK
                        recordIds = getIsFieldFKRecordIds(dataflowId, datasetId, tableSchemaId, dataProviderId, tablePath, parameters, object, method);
                        break;
                }
            }

            if (recordIds.size() > 0) {
                //Defining schema
                List<String> parquetHeaders = Arrays.asList(PK, PARQUET_RECORD_ID_COLUMN_HEADER, VALIDATION_LEVEL, VALIDATION_AREA, MESSAGE, TABLE_NAME, FIELD_NAME, DATASET_ID, QC_CODE);
                List<Schema.Field> fields = new ArrayList<>();
                for (String header : parquetHeaders) {
                    fields.add(new Schema.Field(header, Schema.create(Schema.Type.STRING), null, null));
                }
                Schema schema = Schema.createRecord("Data", null, null, false, fields);
                createParquet(datasetId, tableName, ruleId, parquetFile, ruleVO, recordIds, headerMap, schema);
                if (recordIds.size()>0) {
                    s3Helper.uploadFileToBucket(s3FilePath, parquetFile);
                }
            }
        } catch (Exception e) {
            LOG.error("Error creating validation folder for ruleId {}, datasetId {} and tableName {}", ruleId, datasetId, tableName);
            throw e;
        } finally {
            if (parquetFile!=null) {
                dremioHelperService.deleteFileFromR3IfExists(parquetFile);
            }
        }
    }

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
        String pkTablePath = s3Service.getTableAsFolderQueryPath(pkTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
        recordIds = (List<String>) method.invoke(object, fkFieldSchema, pkMustBeUsed, tablePath, pkTablePath, foreignKey, primaryKey, optionalFK, optionalPK);  //isfieldFK
        return recordIds;
    }

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
        String originTablePath = s3Service.getTableAsFolderQueryPath(origTableTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
        S3PathResolver referTableResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetIdReferenced, referencedTableSchema.getNameTableSchema());
        String referTablePath = s3Service.getTableAsFolderQueryPath(referTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
        recordIds =  (List<String>) method.invoke(object, originTablePath, referTablePath, origFieldNames, referFieldNames, integrityVO.getIsDoubleReferenced());  //checkIntegrityConstraint
        return recordIds;
    }

    private static void createParquet(Long datasetId, String tableName, String ruleId, String parquetFile, RuleVO ruleVO, List<String> recordIds, Map<String, String> headerMap, Schema schema) throws IOException {
        try (ParquetWriter<GenericRecord> writer = AvroParquetWriter
                .<GenericRecord>builder(new Path(parquetFile))
                .withSchema(schema)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withPageSize(4 * 1024)
                .withRowGroupSize(16 * 1024)
                .build()) {

            for (String recordId : recordIds) {
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
                writer.write(record);
            }
        } catch (Exception e1) {
            LOG.error("Error creating parquet file {} for ruleId {}, datasetId {} and tableName {}", parquetFile, ruleId, datasetId, tableName);
            throw e1;
        }
    }

    private List<String> getPkAndFkHeaderValues(DataSetSchema datasetSchemaPK, DataSetSchema datasetSchemaFK, FieldSchema fkFieldSchema, String tableSchemaId) {
        String primaryKey = null, optionalFK = null, optionalPK = null, pkTableName = null;
        List<String> pkAndFkDetailsList = new ArrayList<>();
        for (TableSchema tableSchema : datasetSchemaPK.getTableSchemas()) {
            for (FieldSchema pKFieldSchema : tableSchema.getRecordSchema().getFieldSchema()) {
                if (pKFieldSchema.getIdFieldSchema().toString().equals(fkFieldSchema.getReferencedField().getIdPk().toString())) {
                    pkTableName = tableSchema.getNameTableSchema();
                    primaryKey = pKFieldSchema.getHeaderName();
                    ObjectId optionalPKId = fkFieldSchema.getReferencedField().getLinkedConditionalFieldId();
                    if (optionalPKId!=null) {
                        optionalPK = tableSchema.getRecordSchema().getFieldSchema().stream().filter(f -> f.getIdFieldSchema().toString().equals(optionalPKId.toString())).findFirst().get().getHeaderName();
                    }
                    ObjectId optionalFKId = fkFieldSchema.getReferencedField().getMasterConditionalFieldId();
                    if (optionalFKId!=null) {
                        optionalFK = datasetSchemaFK.getTableSchemas().stream().filter(t -> t.getIdTableSchema().toString().equals(tableSchemaId))
                                .findFirst().get().getRecordSchema().getFieldSchema().stream().filter(f -> f.getIdFieldSchema().toString().equals(optionalFKId.toString())).findFirst().get().getHeaderName();
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





















