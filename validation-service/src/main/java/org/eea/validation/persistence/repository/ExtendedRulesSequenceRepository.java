package org.eea.validation.persistence.repository;

import org.bson.types.ObjectId;
import org.eea.validation.persistence.schemas.rule.RuleSequence;

/**
 * The Interface RulesSequenceRepository.
 */
public interface ExtendedRulesSequenceRepository {

  /**
   * Creates the sequence.
   *
   * @param ruleSchemaId the rule schema id
   * @return the rule sequence
   */
  RuleSequence createSequence(ObjectId ruleSchemaId);

  /**
   * Update sequence.
   *
   * @param ruleSchemaId the rule schema id
   * @return the long
   */
  long updateSequence(ObjectId ruleSchemaId);

  /**
   * Delete by dataset schema id.
   *
   * @param datasetSchemaId the dataset schema id
   */
  void deleteByDatasetSchemaId(ObjectId datasetSchemaId);
}
