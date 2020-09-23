package org.eea.validation.util;

import java.sql.SQLException;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.service.SqlRulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class SQLValitaionUtils.
 */
@Component
public class SQLValitaionUtils {

  /** The sql rules service. */
  private static SqlRulesService sqlRulesService;

  /**
   * Sets the sql rules service.
   *
   * @param sqlRulesService the new sql rules service
   */
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
    String preparedStatement = sqlRulesService.queryTreat(query, datasetId);
    // Execute query
    try {
      sqlRulesService.retrivedata(preparedStatement, datasetId);
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }


}
