package org.eea.validation.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.CopySchemaVO;
import org.eea.interfaces.vo.dataset.schemas.audit.RuleHistoricInfoVO;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.multitenancy.DatasetId;

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
   * @param datasetId the dataset id
   */
  void deleteEmptyRulesSchema(String datasetSchemaId, Long datasetId);

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
   * @param typeData the type data
   */
  void deleteRuleRequired(String datasetSchemaId, String referenceId, DataType typeData);

  /**
   * Exists rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return the boolean
   */
  boolean existsRuleRequired(String datasetSchemaId, String referenceId);


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
   * Find sql sentences by dataset schema id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the list
   */
  List<RuleVO> findSqlSentencesByDatasetSchemaId(String datasetSchemaId);


  /**
   * Gets the all disabled rules.
   *
   * @param dataflowId the dataflow id
   * @param designs the designs
   * @return the all disabled rules
   */
  Integer getAllDisabledRules(Long dataflowId, List<DesignDatasetVO> designs);

  /**
   * Gets the all unchecked rules.
   *
   * @param dataflowId the dataflow id
   * @param designs the designs
   * @return the all unchecked rules
   */
  Integer getAllUncheckedRules(Long dataflowId, List<DesignDatasetVO> designs);

  /**
   * Delete automatic rule by reference id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   */
  void deleteAutomaticRuleByReferenceId(String datasetSchemaId, String referenceId);


  /**
   * Gets the integrity schemas.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the integrity schemas
   */
  List<IntegrityVO> getIntegritySchemas(String datasetSchemaId);

  /**
   * Insert integrity schemas.
   *
   * @param integritiesVO the integrities VO
   */
  void insertIntegritySchemas(List<IntegrityVO> integritiesVO);


  /**
   * Import rules schema.
   *
   * @param qcRulesBytes the qc rules bytes
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param integrities the integrities
   * @return the map
   * @throws EEAException the EEA exception
   */
  Map<String, String> importRulesSchema(List<byte[]> qcRulesBytes,
      Map<String, String> dictionaryOriginTargetObjectId, List<IntegrityVO> integrities)
      throws EEAException;

  /**
   * Export QCCSV.
   *
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void exportQCCSV(@DatasetId Long datasetId) throws EEAException, IOException;

  /**
   * Download QCCSV.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @return the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  File downloadQCCSV(Long datasetId, String fileName) throws IOException;

  /**
   * Gets the rule historic info.
   *
   * @param datasetId the dataset id
   * @param ruleId the rule id
   * @return the rule historic info
   * @throws EEAException the EEA exception
   */
  List<RuleHistoricInfoVO> getRuleHistoricInfo(Long datasetId, String ruleId) throws EEAException;

}
