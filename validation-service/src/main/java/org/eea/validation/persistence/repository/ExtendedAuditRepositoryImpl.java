package org.eea.validation.persistence.repository;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.util.Strings;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.validation.persistence.schemas.audit.Audit;
import org.eea.validation.persistence.schemas.audit.RuleHistoricInfo;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.google.gson.Gson;

public class ExtendedAuditRepositoryImpl implements ExtendedAuditRepository {

  /** The mongo template. */
  @Autowired
  private MongoTemplate mongoTemplate;

  /**
   * Creates the audit.
   *
   * @param rule the rule
   * @param user the user
   */
  @Override
  public void createAudit(Rule rule, UserRepresentationVO user) {
    Audit audit = new Audit();
    RuleHistoricInfo ruleHistoricInfo = new RuleHistoricInfo();
    ruleHistoricInfo.setRuleInfoId(new ObjectId());
    ruleHistoricInfo.setMetadata(true);
    ruleHistoricInfo.setExpression(true);
    ruleHistoricInfo.setStatus(true);
    ruleHistoricInfo.setUser(user.getEmail());
    ruleHistoricInfo.setTimestamp(new Date());
    ruleHistoricInfo.setRuleId(rule.getRuleId());
    ruleHistoricInfo.setRuleBefore(Strings.EMPTY);
    audit.setHistoric(Arrays.asList(ruleHistoricInfo));
    mongoTemplate.save(audit);
  }

  /**
   * Gets the audit by rule id.
   *
   * @param ruleId the rule id
   * @return the audit by rule id
   */
  @Override
  public Audit getAuditByRuleId(ObjectId ruleId) {
    Audit audit = new Audit();
    List<Audit> audits = mongoTemplate.findAll(Audit.class);
    for (Audit auditExpected : audits) {
      if (auditExpected.getHistoric().get(0).getRuleId().equals(ruleId)) {
        audit = auditExpected;
      }
    }
    return audit;
  }

  /**
   * Update audit.
   *
   * @param audit the audit
   * @param user the user
   * @param rule the rule
   * @param status the status
   * @param expression the expression
   * @param metadata the metadata
   */
  @Override
  public void updateAudit(Audit audit, UserRepresentationVO user, Rule rule, boolean status,
      boolean expression, boolean metadata) {
    RuleHistoricInfo ruleHistoricInfo = new RuleHistoricInfo();
    ruleHistoricInfo.setRuleInfoId(new ObjectId());
    ruleHistoricInfo.setExpression(expression);
    ruleHistoricInfo.setMetadata(metadata);
    ruleHistoricInfo.setStatus(status);
    ruleHistoricInfo.setUser(user.getEmail());
    ruleHistoricInfo.setTimestamp(new Date());
    Gson gson = new Gson();
    ruleHistoricInfo.setRuleBefore(gson.toJson(rule));
    ruleHistoricInfo.setRuleId(rule.getRuleId());
    Update update = new Update();
    update.addToSet("historic", ruleHistoricInfo);
    Criteria criteria = Criteria.where("_id").is(audit.getIdAudit());
    mongoTemplate.updateFirst(Query.query(criteria), update, "Audit");
  }

}
