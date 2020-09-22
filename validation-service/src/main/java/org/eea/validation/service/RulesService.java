package org.eea.validation.service;

import java.util.List;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.CopySchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
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
   * @param datasetId the dataset id
   * @param ruleVO the rule VO
   * @throws EEAException the EEA exception
   */
  void updateRule(long datasetId, RuleVO ruleVO) throws EEAException;


  /**
   * Creates the automatic rules.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @param typeData the type data
   * @param typeEntityEnum the type entity enum
   * @param datasetId the dataset id
   * @param required the required
   * @throws EEAException the EEA exception
   */
  void createAutomaticRules(String datasetSchemaId, String referenceId, DataType typeData,
      EntityTypeEnum typeEntityEnum, Long datasetId, boolean required) throws EEAException;

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

  /**
   * Delete rule by reference field schema PK id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceFieldSchemaPKId the reference field schema PK id
   */
  void deleteRuleByReferenceFieldSchemaPKId(String datasetSchemaId,
      String referenceFieldSchemaPKId);

  /**
   * Update automatic rule.
   *
   * @param datasetId the dataset id
   * @param ruleVO the rule VO
   * @throws EEAException the EEA exception
   */
  void updateAutomaticRule(long datasetId, RuleVO ruleVO) throws EEAException;

  /**
   * Creates the unique constraint.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @param uniqueId the unique id
   */
  void createUniqueConstraint(String datasetSchemaId, String tableSchemaId, String uniqueId);

  /**
   * Creates the unique constraint.
   *
   * @param datasetSchemaId the dataset schema id
   * @param uniqueId the unique id
   */
  void deleteUniqueConstraint(String datasetSchemaId, String uniqueId);



  /**
   * Delete rule high level like.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   */
  void deleteRuleHighLevelLike(String datasetSchemaId, String fieldSchemaId);

  /**
   * Gets the integrity constraint.
   *
   * @param integrityId the integrity id
   * @return the integrity constraint
   */
  IntegrityVO getIntegrityConstraint(String integrityId);


  /**
   * Delete dataset rule and integrity by id field schema.
   *
   * @param fieldSchemaId the field schema id
   * @param datasetId the dataset id
   */
  void deleteDatasetRuleAndIntegrityByFieldSchemaId(String fieldSchemaId, Long datasetId);

  /**
   * Delete dataset rule and integrity by dataset schema id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   */
  void deleteDatasetRuleAndIntegrityByDatasetSchemaId(String datasetSchemaId, Long datasetId);


  /**
   * Copy rules schema.
   *
   * @param rules the rules
   * @return the map
   * @throws EEAException the EEA exception
   */
  Map<String, String> copyRulesSchema(CopySchemaVO rules) throws EEAException;

  /**
   * Delete not empty rule.
   *
   * @param tableSchemaId the table schema id
   * @param datasetId the dataset id
   */
  void deleteNotEmptyRule(String tableSchemaId, Long datasetId);

  /**
   * Update sequence.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the long
   */
  Long updateSequence(String datasetSchemaId);


  /**
   * Update sequence.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the list
   */
  List<RuleVO> findSqlSentencesByDatasetSchemaId(String datasetSchemaId);
}
