package org.eea.validation.service;

import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.validation.persistence.schemas.rule.Rule;

/**
 * The Class ValidationService.
 */
public interface RulesService {

  /**
   * Creates the empty rules scehma.
   *
   * @param schemaId the schema id
   * @param ruleSchemaId the rule schema id
   */
  void createEmptyRulesSchema(ObjectId schemaId, ObjectId ruleSchemaId);

  /**
   * Gets the rules schema by dataset id.
   *
   * @param idDatasetSchema the dataset id
   * @return the rules schema by dataset id
   */
  RulesSchemaVO getRulesSchemaByDatasetId(String idDatasetSchema);

  /**
   * Gets the active rules schema by dataset id.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the active rules schema by dataset id
   */
  RulesSchemaVO getActiveRulesSchemaByDatasetId(String idDatasetSchema);

  /**
   * Creates the empty rules scehma.
   *
   * @param schemaId the schema id
   */
  void deleteEmptyRulesScehma(ObjectId schemaId);



  /**
   * Delete rule by id.
   *
   * @param idDatasetSchema the id dataset schema
   * @param ruleId the rule id
   * @throws EEAException the EEA exception
   */
  void deleteRuleById(String idDatasetSchema, String ruleId) throws EEAException;


  /**
   * Delete rule by reference id.
   *
   * @param idDatasetSchema the id dataset schema
   * @param referenceId the reference id
   * @throws EEAException the EEA exception
   */
  void deleteRuleByReferenceId(String idDatasetSchema, String referenceId) throws EEAException;

  /**
   * Creates the new rule.
   *
   * @param idDatasetSchema the id dataset schema
   * @param rule the rule
   * @throws EEAException the EEA exception
   */
  void createNewRule(String idDatasetSchema, Rule rule) throws EEAException;

  /**
   * Creates the automatic rules.
   *
   * @param idDatasetSchema the id dataset schema
   * @param referenceId the reference id
   * @param typeEntityEnum the type entity enum
   * @param typeData the type data
   * @param required the required
   * @throws EEAException the EEA exception
   */
  void createAutomaticRules(String idDatasetSchema, String referenceId,
      TypeEntityEnum typeEntityEnum, TypeData typeData, Boolean required) throws EEAException;

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
  Boolean existsRuleRequired(String datasetSchemaId, String referenceId);
}
