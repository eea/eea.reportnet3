package org.eea.validation.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ValueVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.validation.exception.EEAInvalidSQLException;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.util.model.QueryVO;
import org.json.simple.parser.ParseException;

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
   * @param queryVO the query VO
   * @param ischeckDC the ischeck DC
   * @return the table value
   * @throws EEAInvalidSQLException the EEA invalid SQL exception
   */
  QueryVO retrieveTableData(String query, QueryVO queryVO, Boolean ischeckDC)
      throws EEAInvalidSQLException;

  /**
   * Validate SQL rules.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param showNotification the show notification
   */
  void validateSQLRules(Long datasetId, String datasetSchemaId, Boolean showNotification);

  /**
   * Run SQL rule with limited results.
   *
   * @param datasetId the dataset id
   * @param sqlRule the sql rule about to be run
   * @param showInternalFields the show internal fields
   * @return the list containing the rows
   * @throws EEAException the EEA exception
   */
  List<List<ValueVO>> runSqlRule(Long datasetId, String sqlRule, boolean showInternalFields)
      throws EEAException;

  /**
   * Evaluate sql rule.
   *
   * @param datasetId the dataset id
   * @param sqlRule the sql rule
   * @return the double
   * @throws EEAException the EEA exception
   * @throws ParseException the parse exception
   */
  Double evaluateSqlRule(Long datasetId, String sqlRule) throws EEAException, ParseException;

  /**
   * Query table.
   *
   * @param newQuery the new query
   * @param queryVO the query VO
   * @return the table value
   * @throws EEAInvalidSQLException the EEA invalid SQL exception
   */
  TableValue queryTable(String newQuery, QueryVO queryVO) throws EEAInvalidSQLException;

}
