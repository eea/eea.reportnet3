package org.eea.validation.service;

import java.sql.SQLException;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
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
   * @param rule the rule
   */
  void validateSQLRuleFromDatacollection(Long datasetId, String datasetSchemaId, RuleVO ruleVO);

  /**
   * Query treat.
   *
   * @param query the query
   * @param datasetId the dataset id
   * @return the string
   */
  String queryTreat(String query, Long datasetId);

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
   * @param datasetId the dataset id
   * @return the table value
   * @throws SQLException the SQL exception
   */
  TableValue retrivedata(String query, Long datasetId) throws SQLException;


}
