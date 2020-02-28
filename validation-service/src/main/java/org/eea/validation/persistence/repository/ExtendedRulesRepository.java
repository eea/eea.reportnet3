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
  boolean deleteRuleById(ObjectId datasetSchemaId, ObjectId ruleId);

  /**
   * Delete rule by reference id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return true, if successful
   */
  boolean deleteRuleByReferenceId(ObjectId datasetSchemaId, ObjectId referenceId);

  /**
   * Delete rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return true, if successful
   */
  boolean deleteRuleRequired(ObjectId datasetSchemaId, ObjectId referenceId);

  /**
   * Creates the new rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @return true, if successful
   */
  boolean createNewRule(ObjectId datasetSchemaId, Rule rule);

  /**
   * Update rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @return true, if successful
   */
  boolean updateRule(ObjectId datasetSchemaId, Rule rule);

  /**
   * Exists rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return true, if successful
   */
  boolean existsRuleRequired(ObjectId datasetSchemaId, ObjectId referenceId);

  /**
   * Insert rule in position.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @param position the position
   * @return true, if successful
   */
  boolean insertRuleInPosition(ObjectId datasetSchemaId, Rule rule, int position);

  /**
   * Find rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param ruleId the rule id
   * @return the rule
   */
  Rule findRule(ObjectId datasetSchemaId, ObjectId ruleId);

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
