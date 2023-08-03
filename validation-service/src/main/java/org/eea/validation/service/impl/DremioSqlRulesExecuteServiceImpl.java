package org.eea.validation.service.impl;

import org.bson.types.ObjectId;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
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
import org.eea.validation.util.FKValidationUtils;
import org.eea.validation.util.UniqueValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.eea.utils.LiteralConstants.S3_TABLE_AS_FOLDER_QUERY_PATH;
import static org.eea.utils.LiteralConstants.S3_VALIDATION;

@ImportDataLakeCommons
@Service
public class DremioSqlRulesExecuteServiceImpl implements DremioRulesExecuteService {


    private JdbcTemplate dremioJdbcTemplate;
    private S3Service s3Service;
    private RulesService rulesService;
    private DatasetSchemaControllerZuul datasetSchemaControllerZuul;
    private DremioRulesService dremioRulesService;
    private S3Helper s3Helper;
    private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;
    private SchemasRepository schemasRepository;
    private DataSetControllerZuul dataSetControllerZuul;

    private static final Logger LOG = LoggerFactory.getLogger(DremioSqlRulesExecuteServiceImpl.class);

    @Autowired
    public DremioSqlRulesExecuteServiceImpl(@Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, S3Service s3Service, RulesService rulesService,
                                            DatasetSchemaControllerZuul datasetSchemaControllerZuul, DremioRulesService dremioRulesService, S3Helper s3Helper,
                                            DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul, SchemasRepository schemasRepository, DataSetControllerZuul dataSetControllerZuul) {
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.s3Service = s3Service;
        this.rulesService = rulesService;
        this.datasetSchemaControllerZuul = datasetSchemaControllerZuul;
        this.dremioRulesService = dremioRulesService;
        this.dataSetMetabaseControllerZuul = dataSetMetabaseControllerZuul;
        this.schemasRepository = schemasRepository;
        this.dataSetControllerZuul = dataSetControllerZuul;
        this.s3Helper = s3Helper;
    }

    @Override
    public void execute(Long dataflowId, Long datasetId, String datasetSchemaId, String tableName, String tableSchemaId, String ruleId, Long dataProviderId, Long taskId) throws Exception {
        try {
            S3PathResolver dataTableResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, tableName);
            String tablePath = s3Service.getTableAsFolderQueryPath(dataTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
            S3PathResolver validationResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, S3_VALIDATION);
            RuleVO ruleVO = rulesService.findRule(datasetSchemaId, ruleId);
            s3Helper.deleteRuleFolderIfExists(validationResolver, ruleVO);
            int startIndex = ruleVO.getWhenConditionMethod().indexOf("(");
            int endIndex = ruleVO.getWhenConditionMethod().indexOf(")");
            String ruleMethodName = ruleVO.getWhenConditionMethod().substring(0, startIndex);
            List<String> parameters = dremioRulesService.processRuleMethodParameters(ruleVO, startIndex, endIndex);
            String fieldName = datasetSchemaControllerZuul.getFieldName(datasetSchemaId, tableSchemaId, parameters, ruleVO.getReferenceId(), ruleVO.getReferenceFieldSchemaPKId());
            if (fieldName==null) {
                fieldName = "";
            }
            StringBuilder validationQuery = dremioRulesService.getS3RuleFolderQueryBuilder(datasetId, tableName, dataTableResolver, validationResolver, ruleVO, fieldName);

            Class<?> cls = Class.forName("org.eea.validation.util.datalake.DremioSQLValidationUtils");
            Field[] fields = cls.getDeclaredFields();
            Method factoryMethod = cls.getDeclaredMethod("getInstance");
            Object object = factoryMethod.invoke(null, null);
            Field field = Arrays.stream(fields).findFirst().get();
            field.setAccessible(true);
            field.set(object, dremioJdbcTemplate);
            Method method = dremioRulesService.getRuleMethodFromClass(ruleMethodName, cls);

            int parameterLength = method.getParameters().length;
            List<String> recordIds = new ArrayList<>();
            switch (parameterLength) {
                case 1:
                    String sql = ruleVO.getSqlSentence().replace(tableName, tablePath);
                    recordIds = (List<String>) method.invoke(object, sql);    //isSQLSentenceWithCode
                    break;
                case 2:
                    recordIds = (List<String>) method.invoke(object, fieldName, tablePath);  //isUniqueConstraint
                    break;
                case 8:
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
                    if (datasetId==datasetIdRefered) {
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
                    break;
                case 9:
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
                    recordIds =  (List<String>) method.invoke(object, datasetIdOrigin, datasetIdReferenced, originSchemaId, referencedSchemaId, originTablePath,
                            referTablePath, origFieldNames, referFieldNames, integrityVO.getIsDoubleReferenced());  //checkIntegrityConstraint
                    break;
            }

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
                if (valQuery.contains("pkNotUsed")) {
                    valQuery = getModifiedQuery(dataTableResolver, valQuery, "('pkNotUsed')");
                    valQuery = valQuery.replace(",record_id", ",\'\'");
                    valQuery = valQuery.replace(fieldName, "");
                } else if (valQuery.contains("OMISSION")) {
                    if (valQuery.contains("('OMISSION','COMISSION')")) {
                        valQuery = getModifiedQuery(dataTableResolver, valQuery, "('OMISSION','COMISSION')");
                        valQuery = valQuery.replace(",record_id", ",\'\'");
                        valQuery = valQuery.replace("'" + ruleVO.getThenCondition().get(0) + "'" + " as message", "'" + ruleVO.getThenCondition().get(0) + " (COMISSION)'" + " as message");
                        dremioJdbcTemplate.execute(valQuery.toString());
                        int ruleIdLength = ruleVO.getRuleId().length();
                        String ruleFolderName = ruleVO.getShortCode()+"-"+ruleVO.getRuleId().substring(ruleIdLength-3, ruleIdLength);
                        valQuery = valQuery.replace(ruleFolderName, ruleFolderName+"-om");
                        valQuery = valQuery.replace("COMISSION", "OMISSION");
                        dremioJdbcTemplate.execute(valQuery.toString());
                        return;
                    } else if (valQuery.contains("COMISSION")) {
                        valQuery = getModifiedQuery(dataTableResolver, valQuery, "('COMISSION')");
                        valQuery = valQuery.replace(",record_id", ",\'\'");
                        valQuery = valQuery.replace("'" + ruleVO.getThenCondition().get(0) + "'" + " as message", "'" + ruleVO.getThenCondition().get(0) + " (COMISSION)'" + " as message");
                    } else {
                        valQuery = getModifiedQuery(dataTableResolver, valQuery, "('OMISSION')");
                        valQuery = valQuery.replace(",record_id", ",\'\'");
                        valQuery = valQuery.replace("'" + ruleVO.getThenCondition().get(0) + "'" + " as message", "'" + ruleVO.getThenCondition().get(0) + " (OMISSION)'" + " as message");

                    }
                }
                dremioJdbcTemplate.execute(valQuery.toString());
            }
        } catch (Exception e) {
            LOG.error("Error creating validation folder for ruleId {}, datasetId {} and tableName {}", ruleId, datasetId, tableName);
            throw e;
        }
    }

    private String getModifiedQuery(S3PathResolver dataTableResolver, String valQuery, String replace) {
        valQuery = valQuery.replace("from " + s3Service.getTableAsFolderQueryPath(dataTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH) + " where record_id in " + replace, "");
        return valQuery;
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





















