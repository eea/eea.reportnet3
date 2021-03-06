package org.eea.validation.service;

import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.validation.exception.EEAInvalidSQLException;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.schemas.rule.Rule;

/**
 * The Interface SqlRulesService.
 */
public interface SqlRulesService {

  /**
   * Validate SQL rule.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   */
  void validateSQLRule(Long datasetId, String datasetSchemaId, Rule rule);

  /**
   * Validate SQL rule from datacollection.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param ruleVO the rule VO
   * @return true, if successful
   */
  boolean validateSQLRuleFromDatacollection(Long datasetId, String datasetSchemaId, RuleVO ruleVO);

  /**
   * Gets the rule.
   *
   * @param datasetId the dataset id
   * @param ruleId the rule id
   * @return the rule
   */
  Rule getRule(Long datasetId, String ruleId);

  /**
   * Retrieve table data.
   *
   * @param query the query
   * @param datasetId the dataset id
   * @param rule the rule
   * @param ischeckDC the ischeck DC
   * @return the table value
   * @throws EEAInvalidSQLException the EEA invalid SQL exception
   */
  TableValue retrieveTableData(String query, Long datasetId, Rule rule, Boolean ischeckDC)
      throws EEAInvalidSQLException;

  /**
   * Validate SQL rules.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param showNotification the show notification
   */
  void validateSQLRules(Long datasetId, String datasetSchemaId, Boolean showNotification);
}
