package org.eea.validation.persistence.repository;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * The Interface ExtendedRulesRepository.
 */
public interface ExtendedRulesRepository {


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
  Document getRulesWithActiveCriteria(ObjectId idDatasetSchema, Boolean enable);
}
