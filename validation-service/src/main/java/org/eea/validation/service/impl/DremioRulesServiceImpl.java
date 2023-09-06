package org.eea.validation.service.impl;

import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.validation.service.DremioRulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.eea.utils.LiteralConstants.*;

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
        int ruleIdLength = ruleVO.getRuleId().length();
        String message = ruleVO.getThenCondition().get(0);
        message = message.replace("\'","\"");
        validationQuery.append("create table ").append(s3Service.getTableAsFolderQueryPath(validationResolver, S3_TABLE_AS_FOLDER_QUERY_PATH)).append(".")
                .append("\"").append(ruleVO.getShortCode()).append("-").append(ruleVO.getRuleId().substring(ruleIdLength-3, ruleIdLength)).append("\"").append(" as (select RIGHT(RANDOM(),15) as pk,record_id as record_id,")
                .append("'").append(ruleVO.getThenCondition().get(1)).append("'").append(" as validation_level,").append("'").append(ruleVO.getType())
                .append("'").append(" as validation_area,").append("'").append(message).append("'").append(" as message,")
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

    @Override
    public Map<String, String> createValidationParquetHeaderMap(Long datasetId, String tableName, RuleVO ruleVO, String fieldName, String message) {
        Map<String, String> headerMap = new LinkedHashMap<>();
        headerMap.put(VALIDATION_LEVEL, ruleVO.getThenCondition().get(1));
        headerMap.put(VALIDATION_AREA, ruleVO.getType().getValue());
        headerMap.put(MESSAGE, message);
        headerMap.put(TABLE_NAME, tableName);
        headerMap.put(FIELD_NAME, fieldName);
        headerMap.put(DATASET_ID, datasetId.toString());
        headerMap.put(QC_CODE, ruleVO.getShortCode());
        return headerMap;
    }
}
