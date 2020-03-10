package org.eea.validation.service;

import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;

/**
 * The Class ValidationService.
 */
public interface RulesService {

  /**
   * Creates the empty rules schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rulesSchemaId the rules schema id
   */
  void createEmptyRulesSchema(String datasetSchemaId, String rulesSchemaId);

  /**
   * Gets the rules schema by dataset id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the rules schema by dataset id
   */
  RulesSchemaVO getRulesSchemaByDatasetId(String datasetSchemaId);

  /**
   * Gets the active rules schema by dataset id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the active rules schema by dataset id
   */
  RulesSchemaVO getActiveRulesSchemaByDatasetId(String datasetSchemaId);

  /**
   * Delete empty rules schema.
   *
   * @param datasetSchemaId the dataset schema id
   */
  void deleteEmptyRulesSchema(String datasetSchemaId);

  /**
   * Delete rule by id.
   *
   * @param datasetId the dataset id
   * @param ruleId the rule id
   * @throws EEAException the EEA exception
   */
  void deleteRuleById(long datasetId, String ruleId) throws EEAException;

  /**
   * Delete rule by reference id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   */
  void deleteRuleByReferenceId(String datasetSchemaId, String referenceId);

  /**
   * Creates the new rule.
   *
   * @param datasetId the dataset id
   * @param ruleVO the rule VO
   * @throws EEAException the EEA exception
   */
  void createNewRule(long datasetId, RuleVO ruleVO) throws EEAException;

  /**
   * Update rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param ruleVO the rule VO
   * @throws EEAException the EEA exception
   */
  void updateRule(String datasetSchemaId, RuleVO ruleVO) throws EEAException;

  /**
   * Creates the automatic rules.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @param typeData the type data
   * @param typeEntityEnum the type entity enum
   * @param required the required
   * @throws EEAException the EEA exception
   */
  void createAutomaticRules(String datasetSchemaId, String referenceId, DataType typeData,
      EntityTypeEnum typeEntityEnum, boolean required) throws EEAException;

  /**
   * Delete rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   */
  void deleteRuleRequired(String datasetSchemaId, String referenceId);

  /**
   * Exists rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return the boolean
   */
  boolean existsRuleRequired(String datasetSchemaId, String referenceId);

  /**
   * Insert rule in position.
   *
   * @param datasetSchemaId the dataset schema id
   * @param ruleId the rule id
   * @param position the position
   * @return true, if successful
   */
  boolean insertRuleInPosition(String datasetSchemaId, String ruleId, int position);
}
