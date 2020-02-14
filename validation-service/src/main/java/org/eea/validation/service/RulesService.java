package org.eea.validation.service;

import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;

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
   * @throws EEAException
   */
  void deleteRuleById(String idDatasetSchema, String ruleId) throws EEAException;


  /**
   * Delete rule by reference id.
   *
   * @param idDatasetSchema the id dataset schema
   * @param referenceId the reference id
   */
  void deleteRuleByReferenceId(String idDatasetSchema, String referenceId) throws EEAException;
}
