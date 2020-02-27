package org.eea.validation.persistence.repository;

import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import com.mongodb.client.result.UpdateResult;

/**
 * The Interface ExtendedRulesRepository.
 */
public interface ExtendedRulesRepository {


  /**
   * Delete by id dataset schema.
   *
   * @param rulesSchemaId the rules schema id
   */
  void deleteByIdDatasetSchema(ObjectId rulesSchemaId);

  /**
   * Delete rule by id.
   *
   * @param idDatasetSchema the id dataset schema
   * @param ruleId the rule id
   */
  void deleteRuleById(String idDatasetSchema, String ruleId);



  /**
   * Delete rule by reference id.
   *
   * @param idDatasetSchema the id dataset schema
   * @param referenceId the reference id
   */
  void deleteRuleByReferenceId(String idDatasetSchema, String referenceId);

  /**
   * Gets the rules with active criteria.
   *
   * @param idDatasetSchema the id dataset schema
   * @param enable the enable
   * @return the rules with active criteria
   */
  RulesSchema getRulesWithActiveCriteria(ObjectId idDatasetSchema, Boolean enable);


  /**
   * Creates the new rule.
   *
   * @param idDatasetSchema the id dataset schema
   * @param rule the rule
   * @return the update result
   * @throws EEAException the EEA exception
   */
  void createNewRule(String idDatasetSchema, Rule rule) throws EEAException;

  /**
   * Delete rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return the update result
   */
  UpdateResult deleteRuleRequired(String datasetSchemaId, String referenceId);

  /**
   * Exists rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return the boolean
   */
  Boolean existsRuleRequired(String datasetSchemaId, String referenceId);


  /**
   * Update rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @return the update result
   */
  boolean updateRule(String datasetSchemaId, Rule rule);

  /**
   * Insert rule in position.
   *
   * @param idDatasetSchema the id dataset schema
   * @param rule the rule
   * @param position the position
   * @return true, if successful
   */
  boolean insertRuleInPosition(String idDatasetSchema, Rule rule, int position);

  /**
   * Delete rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param ruleId the rule id
   * @return true, if successful
   */
  boolean deleteRule(String datasetSchemaId, String ruleId);

  /**
   * Find rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param ruleId the rule id
   * @return the rule
   */
  Rule findRule(String datasetSchemaId, String ruleId);

  /**
   * Gets the rules with type rule criteria.
   *
   * @param idDatasetSchema the id dataset schema
   * @param required the required
   * @return the rules with type rule criteria
   */
  RulesSchema getRulesWithTypeRuleCriteria(ObjectId idDatasetSchema, Boolean required);
}
