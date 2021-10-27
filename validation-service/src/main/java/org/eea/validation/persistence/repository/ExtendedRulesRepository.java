package org.eea.validation.persistence.repository;

import java.util.List;
import javax.annotation.CheckForNull;
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
   * Delete rule byreference field schema PK id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceFieldSchemaPKId the reference field schema PK id
   * @return true, if successful
   */
  boolean deleteRuleByReferenceFieldSchemaPKId(ObjectId datasetSchemaId,
      ObjectId referenceFieldSchemaPKId);

  /**
   * Delete rule point required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return true, if successful
   */
  boolean deleteRulePointRequired(ObjectId datasetSchemaId, ObjectId referenceId);

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
  @CheckForNull
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

  /**
   * Gets the active and verified rules.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the active and verified rules
   */
  RulesSchema getActiveAndVerifiedRules(ObjectId datasetSchemaId);

  /**
   * Delete by unique constraint id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param uniqueConstraintId the unique constraint id
   * @return true, if successful
   */
  boolean deleteByUniqueConstraintId(ObjectId datasetSchemaId, ObjectId uniqueConstraintId);

  /**
   * Delete rule high level like.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaLike the field schema like
   * @return true, if successful
   */
  boolean deleteRuleHighLevelLike(ObjectId datasetSchemaId, String fieldSchemaLike);

  /**
   * Delete not empty rule.
   *
   * @param tableSchemaId the table schema id
   * @param datasetSchemaId the dataset schema id
   * @return true, if successful
   */
  boolean deleteNotEmptyRule(ObjectId tableSchemaId, ObjectId datasetSchemaId);


  /**
   * Find sql rules.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the list
   */
  List<Rule> findSqlRules(ObjectId datasetSchemaId);

  /**
   * Gets the all disabled rules.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the all disabled rules
   */
  RulesSchema getAllDisabledRules(ObjectId datasetSchemaId);

  /**
   * Gets the all unchecked rules.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the all unchecked rules
   */
  RulesSchema getAllUncheckedRules(ObjectId datasetSchemaId);


  /**
   * Delete automatic rule by reference id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return true, if successful
   */
  boolean deleteAutomaticRuleByReferenceId(ObjectId datasetSchemaId, ObjectId referenceId);


  /**
   * Empty rules of schema by dataset schema id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return true, if successful
   */
  boolean emptyRulesOfSchemaByDatasetSchemaId(ObjectId datasetSchemaId);

  /**
   * Find rules byreference id.
   *
   * @param referenceId the reference id
   * @return the rule
   */
  RulesSchema findRulesByreferenceId(ObjectId datasetSchemaId, ObjectId referenceId);
}
