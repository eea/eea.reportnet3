package org.eea.validation.service;

import java.sql.SQLException;
import java.util.List;
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
   * @param ruleVO the rule VO
   */
  void validateSQLRuleFromDatacollection(Long datasetId, String datasetSchemaId, RuleVO ruleVO);

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
   * @param rule
   * @return the table value
   * @throws SQLException the SQL exception
   */
  TableValue retrieveTableData(String query, Long datasetId, Rule rule, Boolean ischeckDC)
      throws SQLException;


  /**
   * Retrive first result.
   *
   * @param query the query
   * @param datasetId the dataset id
   * @return the object
   */
  List<Object> retriveFirstResult(String query, Long datasetId);



}
