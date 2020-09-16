package org.eea.validation.util;

import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.service.SqlRulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class SQLValitaionUtils.
 */
@Component
public class SQLValitaionUtils {

  private static SqlRulesService sqlRulesService;

  @Autowired
  synchronized void setSqlRulesService(SqlRulesService sqlRulesService) {
    SQLValitaionUtils.sqlRulesService = sqlRulesService;
  }

  public static void executeValidationSQLRule(Long datasetId, String ruleId) {
    // retrive the rule
    Rule rule = sqlRulesService.getRule(datasetId, ruleId);
    // retrive sql sentence
    String query = rule.getSqlSentence();
    // adapt query to our data model
    String preparedStatement = sqlRulesService.queryTreat(query);
    // Execute query
    sqlRulesService.retrivedata(preparedStatement);

  }


}
