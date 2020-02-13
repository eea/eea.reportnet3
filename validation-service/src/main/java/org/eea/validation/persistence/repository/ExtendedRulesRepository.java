package org.eea.validation.persistence.repository;

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
}
