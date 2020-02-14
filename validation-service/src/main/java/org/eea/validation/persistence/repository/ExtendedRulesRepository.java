package org.eea.validation.persistence.repository;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * The Interface ExtendedRulesRepository.
 */
public interface ExtendedRulesRepository {

  /**
   * Delete rule by reference id.
   *
   * @param referenceId the reference id
   */
  void deleteRuleByReferenceId(String referenceId);

  /**
   * Delete rule by id.
   *
   * @param idRuleSchema the id rule schema
   */
  void deleteRuleById(String idRuleSchema);

  /**
   * Gets the rules with active criteria.
   *
   * @param idDatasetSchema the id dataset schema
   * @param enable the enable
   * @return the rules with active criteria
   */
  Document getRulesWithActiveCriteria(ObjectId idDatasetSchema, Boolean enable);
}
