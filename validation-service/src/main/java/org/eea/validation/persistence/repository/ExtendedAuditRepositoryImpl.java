package org.eea.validation.persistence.repository;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.validation.mapper.RuleMapper;
import org.eea.validation.persistence.schemas.audit.Audit;
import org.eea.validation.persistence.schemas.audit.RuleHistoricInfo;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExtendedAuditRepositoryImpl implements ExtendedAuditRepository {

  /** The mongo template. */
  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private RuleMapper ruleMapper;

  /**
   * Creates the audit.
   *
   * @param rule the rule
   * @param user the user
   * @param datasetId the dataset id
   * @param status the status
   * @param expression the expression
   * @param metadata the metadata
   */
  @Override
  public void createAudit(Rule rule, UserRepresentationVO user, Long datasetId, boolean status,
      boolean expression, boolean metadata) throws JsonProcessingException {
    Audit audit = new Audit();
    audit.setDatasetId(datasetId);
    RuleHistoricInfo ruleHistoricInfo = new RuleHistoricInfo();
    ruleHistoricInfo.setRuleInfoId(new ObjectId());
    ruleHistoricInfo.setMetadata(metadata);
    ruleHistoricInfo.setExpression(expression);
    ruleHistoricInfo.setStatus(status);
    ruleHistoricInfo.setUser(user.getEmail());
    ruleHistoricInfo.setTimestamp(new Date());
    ruleHistoricInfo.setRuleId(rule.getRuleId());
    ObjectMapper mapper = new ObjectMapper();
    ruleHistoricInfo.setRuleBefore(mapper.writeValueAsString(ruleMapper.entityToClass(rule)));
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
    Audit audit = null;
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
   * @throws JsonProcessingException
   */
  @Override
  public void updateAudit(Audit audit, UserRepresentationVO user, Rule rule, boolean status,
      boolean expression, boolean metadata) throws JsonProcessingException {
    RuleHistoricInfo ruleHistoricInfo = new RuleHistoricInfo();
    ruleHistoricInfo.setRuleInfoId(new ObjectId());
    ruleHistoricInfo.setExpression(expression);
    ruleHistoricInfo.setMetadata(metadata);
    ruleHistoricInfo.setStatus(status);
    ruleHistoricInfo.setUser(user.getEmail());
    ruleHistoricInfo.setTimestamp(new Date());
    ObjectMapper mapper = new ObjectMapper();
    ruleHistoricInfo.setRuleBefore(mapper.writeValueAsString(ruleMapper.entityToClass(rule)));
    ruleHistoricInfo.setRuleId(rule.getRuleId());
    Update update = new Update();
    update.addToSet("historic", ruleHistoricInfo);
    Criteria criteria = Criteria.where("_id").is(audit.getIdAudit());
    mongoTemplate.updateFirst(Query.query(criteria), update, "Audit");
  }

  /**
   * Gets the audits by dataset id.
   *
   * @param datasetId the dataset id
   * @return the audits by dataset id
   */
  @Override
  public List<Audit> getAuditsByDatasetId(Long datasetId) {
    Query query = new Query();
    query.addCriteria(Criteria.where("datasetId").is(datasetId));
    return mongoTemplate.find(query, Audit.class);
  }


}
