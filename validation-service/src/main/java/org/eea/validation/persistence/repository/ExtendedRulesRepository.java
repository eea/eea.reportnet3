package org.eea.validation.persistence.repository;

import org.bson.types.ObjectId;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;

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
   * @param datasetSchemaId the dataset schema id
   * @param ruleId the rule id
   * @return true, if successful
   */
  boolean deleteRuleById(String datasetSchemaId, String ruleId);

  /**
   * Delete rule by reference id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return true, if successful
   */
  boolean deleteRuleByReferenceId(String datasetSchemaId, String referenceId);

  /**
   * Delete rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return true, if successful
   */
  boolean deleteRuleRequired(String datasetSchemaId, String referenceId);

  /**
   * Creates the new rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @return true, if successful
   */
  boolean createNewRule(String datasetSchemaId, Rule rule);

  /**
   * Update rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @return true, if successful
   */
  boolean updateRule(String datasetSchemaId, Rule rule);

  /**
   * Exists rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return true, if successful
   */
  boolean existsRuleRequired(String datasetSchemaId, String referenceId);

  /**
   * Insert rule in position.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @param position the position
   * @return true, if successful
   */
  boolean insertRuleInPosition(String datasetSchemaId, Rule rule, int position);

  /**
   * Find rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param ruleId the rule id
   * @return the rule
   */
  Rule findRule(String datasetSchemaId, String ruleId);

  /**
   * Gets the rules with active criteria.
   *
   * @param datasetSchemaId the dataset schema id
   * @param enable the enable
   * @return the rules with active criteria
   */
  RulesSchema getRulesWithActiveCriteria(ObjectId datasetSchemaId, boolean enable);

  /**
   * Gets the rules with type rule criteria.
   *
   * @param datasetSchemaId the dataset schema id
   * @param required the required
   * @return the rules with type rule criteria
   */
  RulesSchema getRulesWithTypeRuleCriteria(ObjectId datasetSchemaId, boolean required);
}
