package org.eea.validation.service.impl;

import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.validation.service.DremioRulesExecuteService;
import org.eea.validation.service.DremioRulesService;
import org.eea.validation.service.RulesService;
import org.eea.validation.service.ValidationService;
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

    private static final Logger LOG = LoggerFactory.getLogger(DremioSqlRulesExecuteServiceImpl.class);

    @Autowired
    public DremioSqlRulesExecuteServiceImpl(@Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, S3Service s3Service, RulesService rulesService,
                                            DatasetSchemaControllerZuul datasetSchemaControllerZuul, ValidationService validationService, DremioRulesService dremioRulesService) {
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.s3Service = s3Service;
        this.rulesService = rulesService;
        this.datasetSchemaControllerZuul = datasetSchemaControllerZuul;
        this.validationService = validationService;
        this.dremioRulesService = dremioRulesService;
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
            if (recordIds.size() > 0) {
                dremioJdbcTemplate.execute(validationQuery.toString());
            }
        } catch (Exception e) {
            LOG.error("Error creating validation folder for ruleId {}, datasetId {} and tableName {}", ruleId, datasetId, tableName);
            throw e;
        }
    }

}





















