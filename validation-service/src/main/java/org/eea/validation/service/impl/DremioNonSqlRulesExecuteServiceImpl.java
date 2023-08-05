package org.eea.validation.service.impl;

import org.eea.datalake.service.S3Helper;
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
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.eea.utils.LiteralConstants.*;

@ImportDataLakeCommons
@Service
public class DremioNonSqlRulesExecuteServiceImpl implements DremioRulesExecuteService {

    private JdbcTemplate dremioJdbcTemplate;
    private S3Service s3Service;
    private RulesService rulesService;
    private DatasetSchemaControllerZuul datasetSchemaControllerZuul;
    private ValidationService validationService;
    private DremioRulesService dremioRulesService;
    private S3Helper s3Helper;

    private static final Logger LOG = LoggerFactory.getLogger(DremioNonSqlRulesExecuteServiceImpl.class);

    @Autowired
    public DremioNonSqlRulesExecuteServiceImpl(@Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, S3Service s3Service, RulesService rulesService,
                                               DatasetSchemaControllerZuul datasetSchemaControllerZuul, ValidationService validationService, DremioRulesService dremioRulesService, S3Helper s3Helper) {
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.s3Service = s3Service;
        this.rulesService = rulesService;
        this.datasetSchemaControllerZuul = datasetSchemaControllerZuul;
        this.validationService = validationService;
        this.dremioRulesService = dremioRulesService;
        this.s3Helper = s3Helper;
    }

    @Override
    public void execute(Long dataflowId, Long datasetId, String datasetSchemaId, String tableName, String tableSchemaId, String ruleId, Long dataProviderId, Long taskId) throws Exception {
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
            if (ruleMethodName.equals("isMultiSelectCodelistValidate")) {
                    ruleMethodName = "multiSelectCodelistValidate";
            } else if (ruleMethodName.equals("isCodelistInsensitive")) {
                    ruleMethodName = "codelistValidate";
                    parameters.add("false");
            }
            String fieldName = datasetSchemaControllerZuul.getFieldName(datasetSchemaId, tableSchemaId, parameters, ruleVO.getReferenceId(), ruleVO.getReferenceFieldSchemaPKId());

            query.append("select record_id,").append(fieldName != null ? fieldName : "").append(" from ").append(s3Service.getTableAsFolderQueryPath(dataTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH));
            SqlRowSet rs = dremioJdbcTemplate.queryForRowSet(query.toString());
            int count = 0;
            boolean createRuleFolder = false;
            StringBuilder validationQuery = dremioRulesService.getS3RuleFolderQueryBuilder(datasetId, tableName, dataTableResolver, validationResolver, ruleVO, fieldName);

            Method method = null;
            List<String> classes = new ArrayList<>(Arrays.asList("org.eea.validation.util.datalake.DremioNonSQLValidationUtils", "org.eea.validation.util.ValidationDroolsUtils"));
            Class<?> cls = null;
            for (String className : classes) {
                cls = Class.forName(className);
                method = dremioRulesService.getRuleMethodFromClass(ruleMethodName, cls);
                if (method != null) {
                    break;
                }
            }
            Method factoryMethod = cls.getDeclaredMethod("getInstance");
            Object object = factoryMethod.invoke(null, null);
            int parameterLength = method.getParameters().length;
            while (rs.next()) {
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
                    if (count != 0) {
                        validationQuery.append(",'");
                    }
                    validationQuery.append(rs.getString("record_id")).append("'");
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
        } catch (Exception e) {
            LOG.error("Error creating validation folder for ruleId {}, datasetId {} and tableName {}", ruleId, datasetId, tableName);
            throw e;
        }
    }

}
