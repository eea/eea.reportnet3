package org.eea.validation.service.impl;

import org.bson.types.ObjectId;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.service.DremioRulesExecuteService;
import org.eea.validation.service.DremioRulesService;
import org.eea.validation.service.RulesService;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.FKValidationUtils;
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
    private ValidationService validationService;
    private DremioRulesService dremioRulesService;
    private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;
    private SchemasRepository schemasRepository;
    private DataSetControllerZuul dataSetControllerZuul;

    private static final Logger LOG = LoggerFactory.getLogger(DremioSqlRulesExecuteServiceImpl.class);

    @Autowired
    public DremioSqlRulesExecuteServiceImpl(@Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, S3Service s3Service, RulesService rulesService,
                                            DatasetSchemaControllerZuul datasetSchemaControllerZuul, ValidationService validationService, DremioRulesService dremioRulesService,
                                            DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul, SchemasRepository schemasRepository, DataSetControllerZuul dataSetControllerZuul) {
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.s3Service = s3Service;
        this.rulesService = rulesService;
        this.datasetSchemaControllerZuul = datasetSchemaControllerZuul;
        this.validationService = validationService;
        this.dremioRulesService = dremioRulesService;
        this.dataSetMetabaseControllerZuul = dataSetMetabaseControllerZuul;
        this.schemasRepository = schemasRepository;
        this.dataSetControllerZuul = dataSetControllerZuul;
    }

    @Override
    public void execute(Long dataflowId, Long datasetId, String datasetSchemaId, String tableName, String tableSchemaId, String ruleId, Long dataProviderId, Long taskId) throws Exception {
        try {
            S3PathResolver dataTableResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, tableName);
            String tablePath = s3Service.getTableAsFolderQueryPath(dataTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
            S3PathResolver validationResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, S3_VALIDATION);
            RuleVO ruleVO = rulesService.findRule(datasetSchemaId, ruleId);
            int startIndex = ruleVO.getWhenConditionMethod().indexOf("(");
            int endIndex = ruleVO.getWhenConditionMethod().indexOf(")");
            String ruleMethodName = ruleVO.getWhenConditionMethod().substring(0, startIndex);
            List<String> parameters = dremioRulesService.processRuleMethodParameters(ruleVO, startIndex, endIndex);
            String fieldName = datasetSchemaControllerZuul.getFieldName(datasetSchemaId, tableSchemaId, parameters, ruleVO.getReferenceId(), ruleVO.getReferenceFieldSchemaPKId());
            StringBuilder validationQuery = dremioRulesService.getS3RuleFolderQueryBuilder(datasetId, tableName, dataTableResolver, validationResolver, ruleVO, fieldName);

            Class<?> cls = Class.forName("org.eea.validation.util.datalake.DremioSQLValidationUtils");
            Field[] fields = cls.getDeclaredFields();
            Object object = cls.newInstance();
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

                    String pkDatasetSchemaId = dataSetMetabaseControllerZuul.findDatasetSchemaIdById(datasetIdRefered);
                    DataSetSchema datasetSchemaPK =
                            schemasRepository.findByIdDataSetSchema(new ObjectId(pkDatasetSchemaId));
                    String foreignKey = fkFieldSchema.getHeaderName();
                    String primaryKey = null, optionalFK = null, optionalPK = null, pkTableName = null;
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
                    S3PathResolver pkTableResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetIdRefered, pkTableName);
                    String pkTablePath = s3Service.getTableAsFolderQueryPath(pkTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
                    recordIds = (List<String>) method.invoke(object, fkFieldSchema, pkMustBeUsed, tablePath, pkTablePath, foreignKey, primaryKey, optionalFK, optionalPK);  //isfieldFK
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
                    valQuery = valQuery.replace("from " + s3Service.getTableAsFolderQueryPath(dataTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH) + " where record_id in ('pkNotUsed')", "");
                    valQuery = valQuery.replace(",record_id",",\'\'");
                    valQuery = valQuery.replace(fieldName, "");
                }
                dremioJdbcTemplate.execute(valQuery.toString());
            }
        } catch (Exception e) {
            LOG.error("Error creating validation folder for ruleId {}, datasetId {} and tableName {}", ruleId, datasetId, tableName);
            throw e;
        }
    }

}





















