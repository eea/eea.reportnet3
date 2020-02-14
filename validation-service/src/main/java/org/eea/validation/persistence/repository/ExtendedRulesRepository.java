package org.eea.validation.persistence.repository;

/**
 * The Interface ExtendedRulesRepository.
 */
public interface ExtendedRulesRepository {


  /**
   * Delete rule by id.
   *
   * @param ruleId the rule id
   */
  void deleteRuleById(String ruleId);


  /**
   * Delete rule by reference id.
   *
   * @param referenceId the reference id
   */
  void deleteRuleByReferenceId(String referenceId);


}
