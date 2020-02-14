package org.eea.validation.persistence.repository;

/**
 * The Interface ExtendedRulesRepository.
 */
public interface ExtendedRulesRepository {


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


}
