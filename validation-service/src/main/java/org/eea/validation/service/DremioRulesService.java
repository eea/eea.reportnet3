package org.eea.validation.service;

import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;

import java.lang.reflect.Method;
import java.util.List;

public interface DremioRulesService {

    /**
     * Processes rule method parameters
     * @param ruleVO
     * @param startIndex
     * @param endIndex
     * @return
     */
    List<String> processRuleMethodParameters(RuleVO ruleVO, int startIndex, int endIndex);

    /**
     * Builds query for creating validation rule folder in S3
     * @param datasetId
     * @param tableName
     * @param dataTableResolver
     * @param validationResolver
     * @param ruleVO
     * @param fieldName
     * @return
     */
    StringBuilder getS3RuleFolderQueryBuilder(Long datasetId, String tableName, S3PathResolver dataTableResolver,
                                              S3PathResolver validationResolver, RuleVO ruleVO, String fieldName);

    /**
     * Finds rule method from class using reflection
     * @param ruleMethodName
     * @param cls
     * @return
     */
    Method getRuleMethodFromClass(String ruleMethodName, Class<?> cls);
}
