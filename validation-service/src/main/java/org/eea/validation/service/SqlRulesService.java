package org.eea.validation.service;

import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.schemas.rule.Rule;

/**
 * The Interface SqlRulesService.
 */
public interface SqlRulesService {

  /**
   * Validate SQL rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   */
  void validateSQLRule(String datasetSchemaId, Rule rule);

  /**
   * Query treat.
   *
   * @param query the query
   * @return the string
   */
  String queryTreat(String query);

  /**
   * Gets the rule.
   *
   * @param datasetId the dataset id
   * @param ruleId the rule id
   * @return the rule
   */
  Rule getRule(Long datasetId, String ruleId);

  /**
   * Retrivedata.
   *
   * @param query the query
   * @return the table value
   */
  TableValue retrivedata(String query);

}
