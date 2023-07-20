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
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.*;

import static org.eea.utils.LiteralConstants.S3_TABLE_AS_FOLDER_QUERY_PATH;
import static org.eea.utils.LiteralConstants.S3_VALIDATION;

@ImportDataLakeCommons
@Service
public class DremioExpressionRulesExecuteServiceImpl implements DremioRulesExecuteService {

    private JdbcTemplate dremioJdbcTemplate;
    private S3Service s3Service;
    private RulesService rulesService;
    private DatasetSchemaControllerZuul datasetSchemaControllerZuul;
    private ValidationService validationService;
    private DremioRulesService dremioRulesService;

    private static final Logger LOG = LoggerFactory.getLogger(DremioExpressionRulesExecuteServiceImpl.class);

    @Autowired
    public DremioExpressionRulesExecuteServiceImpl(@Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, S3Service s3Service, RulesService rulesService,
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
            S3PathResolver validationResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, S3_VALIDATION);
            StringBuilder query = new StringBuilder();
            RuleVO ruleVO = rulesService.findRule(datasetSchemaId, ruleId);
            int parameterStartIndex = ruleVO.getWhenConditionMethod().indexOf("(");
            int parameterEndIndex = ruleVO.getWhenConditionMethod().indexOf(")");
            int methodStartIndex = ruleVO.getWhenConditionMethod().indexOf(".");
            String ruleMethodName = ruleVO.getWhenConditionMethod().substring(methodStartIndex+1, parameterStartIndex);
            List<String> parameters = dremioRulesService.processRuleMethodParameters(ruleVO, parameterStartIndex, parameterEndIndex);
            Map<String, List<String>> parameterMethods = new HashMap<>();
            parameters.forEach(parameter -> {
                if (parameter.contains("RuleOperators")) {
                    String parameterMethodName = parameter.substring(methodStartIndex+1, parameterStartIndex);
                    String internalParameters = parameter.substring(parameterStartIndex+1, parameterEndIndex);
                    parameterMethods.put(parameterMethodName, new ArrayList<>(Arrays.asList(internalParameters.split(","))));
                }
            });

            //8a prepei kapou na kalw k to RuleOperators.setEntity
            //an kp parameter exei fieldSchemaId 8a prepei na vriskw to fieldSchema k na kanw setEntity mallon h kt tetoio

            String fieldName = datasetSchemaControllerZuul.getFieldName(datasetSchemaId, tableSchemaId, parameters, ruleVO.getReferenceId(), ruleVO.getReferenceFieldSchemaPKId());
            query.append("select record_id,").append(fieldName != null ? fieldName : "").append(" from ").append(s3Service.getTableAsFolderQueryPath(dataTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH));
            SqlRowSet rs = dremioJdbcTemplate.queryForRowSet(query.toString());
            int count = 0;
            boolean createRuleFolder = false;
            StringBuilder validationQuery = dremioRulesService.getS3RuleFolderQueryBuilder(datasetId, tableName, dataTableResolver, validationResolver, ruleVO, fieldName);

            Class<?> cls = Class.forName("org.eea.validation.util.RuleOperators");
            Object object = cls.newInstance();

            Map<String, Boolean> results = new HashMap<>();
            //8a prepei kapou na kalw k to RuleOperators.setEntity
            //an kp parameter exei fieldSchemaId 8a prepei na vriskw to fieldSchema k na kanw setEntity mallon h kt tetoio
            for (Map.Entry entry : parameterMethods.entrySet()) {
                Method method = dremioRulesService.getRuleMethodFromClass((String) entry.getKey(), cls);
                List<String> pm = (List<String>) entry.getValue();
                Boolean result = (Boolean) method.invoke(object, pm.get(0), pm.get(1));
                results.put((String) entry.getKey(), result);
            }

            while (rs.next()) {
                boolean isValid = false;
                Method method = dremioRulesService.getRuleMethodFromClass(ruleMethodName, cls);
                if (results.size()>0) {
                    switch (results.size()) {
                        case 1:
                            isValid = (boolean) method.invoke(object, results.get(0));
                            break;
                        case 2:
                            isValid = (boolean) method.invoke(object, results.get(0), results.get(1));
                            break;
                    }
                } else {
                    int parameterLength = method.getParameters().length;
                    String firstParam = parameters.get(0).equals("value") ? rs.getString(fieldName) : parameters.get(0);
                    switch (parameterLength) {
                        case 1:
                            isValid = (boolean) method.invoke(object, firstParam);
                            break;
                        case 2:
                            isValid = (boolean) method.invoke(object, firstParam, parameters.get(1));
                            break;
                    }
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
