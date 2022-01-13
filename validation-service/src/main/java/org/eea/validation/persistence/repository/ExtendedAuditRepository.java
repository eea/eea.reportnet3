package org.eea.validation.persistence.repository;

import java.util.List;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.validation.persistence.schemas.audit.Audit;
import org.eea.validation.persistence.schemas.rule.Rule;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * The Interface ExtendedAuditRepository.
 */
public interface ExtendedAuditRepository {

  /**
   * Creates the audit.
   *
   * @param rule the rule
   * @param user the user
   * @param datasetId the dataset id
   */
  void createAudit(Rule rule, UserRepresentationVO user, Long datasetId);

  /**
   * Gets the audit by rule id.
   *
   * @param ruleId the rule id
   * @return the audit by rule id
   */
  Audit getAuditByRuleId(ObjectId ruleId);

  /**
   * Update audit.
   *
   * @param audit the audit
   * @param user the user
   * @param rule the rule
   * @param status the status
   * @param expression the expression
   * @param metadata the metadata
   * @throws JsonProcessingException the json processing exception
   */
  void updateAudit(Audit audit, UserRepresentationVO user, Rule rule, boolean status,
      boolean expression, boolean metadata) throws JsonProcessingException;


  /**
   * Gets the audits by dataset id.
   *
   * @param datasetId the dataset id
   * @return the audits by dataset id
   */
  List<Audit> getAuditsByDatasetId(Long datasetId);

}
