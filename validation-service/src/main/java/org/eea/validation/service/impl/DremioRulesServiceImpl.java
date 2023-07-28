package org.eea.validation.service.impl;

import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.validation.service.DremioRulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.eea.utils.LiteralConstants.S3_TABLE_AS_FOLDER_QUERY_PATH;

@ImportDataLakeCommons
@Service
public class DremioRulesServiceImpl implements DremioRulesService {

    private S3Service s3Service;

    @Autowired
    public DremioRulesServiceImpl(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @Override
    public List<String> processRuleMethodParameters(RuleVO ruleVO, int startIndex, int endIndex) {
        String parameterString = ruleVO.getWhenConditionMethod().substring(startIndex +1, endIndex);
        parameterString = parameterString.replace("'","");
        List<String> parameters = new ArrayList<>(Arrays.asList(parameterString.split(",")));
        return parameters;
    }

    @Override
    public StringBuilder getS3RuleFolderQueryBuilder(Long datasetId, String tableName, S3PathResolver dataTableResolver,
                                                     S3PathResolver validationResolver, RuleVO ruleVO, String fieldName) {
        StringBuilder validationQuery = new StringBuilder();
        validationQuery.append("create table ").append(s3Service.getTableAsFolderQueryPath(validationResolver, S3_TABLE_AS_FOLDER_QUERY_PATH)).append(".")
                .append("\"").append(ruleVO.getShortCode()).append("\"").append(" as (select RIGHT(RANDOM(),15) as pk,record_id as record_id,")
                .append("'").append(ruleVO.getThenCondition().get(1)).append("'").append(" as validation_level,").append("'").append(ruleVO.getType())
                .append("'").append(" as validation_area,").append("'").append(ruleVO.getThenCondition().get(0)).append("'").append(" as message,")
                .append("'").append(tableName).append("'").append(" as table_name,").append("'").append(fieldName).append("'").append(" as field_name,")
                .append("'").append(datasetId).append("'").append(" as dataset_id,").append("'").append(ruleVO.getShortCode()).append("'").append(" as qc_code from ")
                .append(s3Service.getTableAsFolderQueryPath(dataTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH)).append(" where record_id in ('");
        return validationQuery;
    }

    @Override
    public Method getRuleMethodFromClass(String ruleMethodName, Class<?> cls) {
        Method method = null;
        Method[] methods = cls.getDeclaredMethods();
        List<String> methodNames = Arrays.stream(cls.getDeclaredMethods()).map(m -> m.getName()).collect(Collectors.toList());
        if (methodNames.contains(ruleMethodName)) {
            method = Arrays.stream(methods).filter(m -> m.getName().equals(ruleMethodName)).findFirst().get();
        }
        return method;
    }
}
