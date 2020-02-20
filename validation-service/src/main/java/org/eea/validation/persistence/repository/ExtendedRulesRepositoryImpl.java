package org.eea.validation.persistence.repository;

import java.util.Arrays;
import java.util.List;
import javax.annotation.CheckForNull;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

/**
 * The Class ExtendedRulesRepositorysitoryImpl.
 */
public class ExtendedRulesRepositoryImpl implements ExtendedRulesRepository {


  /** The mongo operations. */
  @Autowired
  private MongoOperations mongoOperations;

  /** The mongo template. */
  @Autowired
  private MongoTemplate mongoTemplate;

  /** The mongo database. */
  @Autowired
  private MongoDatabase mongoDatabase;


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * Delete by id dataset schema.
   *
   * @param rulesSchemaId the rules schema id
   */
  @Override
  public void deleteByIdDatasetSchema(ObjectId rulesSchemaId) {
    mongoTemplate.findAndRemove(new Query(Criteria.where("_id").is(rulesSchemaId)),
        RulesSchema.class);
  }

  /**
   * Delete rule by id.
   *
   * @param idDatasetSchema the id dataset schema
   * @param ruleId the rule id
   */
  @Override
  public void deleteRuleById(String idDatasetSchema, String ruleId) {
    Update update = new Update().pull("rules", new BasicDBObject("_id", new ObjectId(ruleId)));
    Query query = new Query();
    query.addCriteria(new Criteria("idDatasetSchema").is(new ObjectId(idDatasetSchema)));
    mongoOperations.updateFirst(query, update, RulesSchema.class);
  }

  /**
   * Delete rule by reference id.
   *
   * @param idDatasetSchema the id dataset schema
   * @param referenceId the reference id
   */
  @Override
  public void deleteRuleByReferenceId(String idDatasetSchema, String referenceId) {
    Update update =
        new Update().pull("rules", new BasicDBObject("referenceId", new ObjectId(referenceId)));
    Query query = new Query();
    query.addCriteria(new Criteria("idDatasetSchema").is(new ObjectId(idDatasetSchema)));
    mongoOperations.updateMulti(query, update, RulesSchema.class);
  }

  /**
   * Delete rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return the update result
   */
  @Override
  public UpdateResult deleteRuleRequired(String datasetSchemaId, String referenceId) {
    Update update =
        new Update().pull("rules", new Document().append("referenceId", new ObjectId(referenceId))
            .append("whenCondition", "!isBlank(value)"));
    Query query = new Query().addCriteria(new Criteria("idDatasetSchema").is(datasetSchemaId));
    return mongoOperations.updateFirst(query, update, RulesSchema.class);
  }

  /**
   * Exists rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return the boolean
   */
  @Override
  public Boolean existsRuleRequired(String datasetSchemaId, String referenceId) {

    Query query = new Query().addCriteria(new Criteria("idDatasetSchema").is(datasetSchemaId))
        .addCriteria(new Criteria("rules.referenceId").is(new ObjectId(referenceId)))
        .addCriteria(new Criteria("rules.whenCondition").is("!isBlank(value)"));
    return mongoTemplate.count(query, RulesSchema.class) > 0;
  }

  /**
   * Gets the rules with active criteria.
   *
   * @param idDatasetSchema the id dataset schema
   * @param enable the enable
   * @return the rules with active criteria
   */

  @Override
  public RulesSchema getRulesWithActiveCriteria(ObjectId idDatasetSchema, Boolean enable) {
    List<RulesSchema> result;
    if (Boolean.TRUE.equals(enable)) {
      Document filterExpression = new Document();
      filterExpression.append("input", "$rules");
      filterExpression.append("as", "rule");
      filterExpression.append("cond", new Document("$eq", Arrays.asList("$$rule.enabled", true)));
      Document filter = new Document("$filter", filterExpression);
      result =
          mongoTemplate
              .aggregate(
                  Aggregation.newAggregation(
                      Aggregation.match(Criteria.where("idDatasetSchema").is(idDatasetSchema)),
                      Aggregation.project("idDatasetSchema")
                          .and(aggregationOperationContext -> filter).as("rules")),
                  RulesSchema.class, RulesSchema.class)
              .getMappedResults();
    } else {
      Query query = new Query();
      query.addCriteria(new Criteria("idDatasetSchema").is(idDatasetSchema));
      result = mongoTemplate.find(query, RulesSchema.class);
    }

    return result.isEmpty() ? null : result.get(0);
  }

  /**
   * Creates the new rule.
   *
   * @param idDatasetSchema the id dataset schema
   * @param rule the rule
   * @throws EEAException the EEA exception
   */
  @Override
  public void createNewRule(String idDatasetSchema, Rule rule) throws EEAException {
    Update update = new Update().push("rules", rule);
    Query query = new Query();
    query.addCriteria(new Criteria("idDatasetSchema").is(new ObjectId(idDatasetSchema)));
    mongoOperations.updateMulti(query, update, RulesSchema.class);
  }

  /**
   * Insert rule in position.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @param position the position
   * @return the update result
   */
  @Override
  public UpdateResult insertRuleInPosition(String datasetSchemaId, Rule rule, int position) {
    Query query =
        new Query().addCriteria(new Criteria("idDatasetSchema").is(new ObjectId(datasetSchemaId)));
    Update update = new Update().push("rules").atPosition(position).each(rule);
    return mongoTemplate.updateFirst(query, update, Rule.class, "RulesSchema");
  }



  /**
   * Find and remove rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return the rule
   */
  @Override
  @CheckForNull
  public Rule findAndRemoveRule(String datasetSchemaId, String referenceId) {
    ObjectId datasetSchemaOId = new ObjectId(datasetSchemaId);
    ObjectId referenceOId = new ObjectId(referenceId);
    RulesSchema rulesSchema = mongoTemplate.findOne(
        new Query(new Criteria("idDatasetSchema").is(datasetSchemaOId)).addCriteria(
            new Criteria("rules").elemMatch(new Criteria("referenceId").is(referenceOId))),
        RulesSchema.class);
    if (rulesSchema != null && rulesSchema.getRules() != null) {
      Query query = new Query(new Criteria("idDatasetSchema").is(datasetSchemaOId));
      Update update = new Update().push("rules", new Document("referenceId", referenceOId));
      mongoTemplate.updateFirst(query, update, Rule.class);
      return rulesSchema.getRules().get(0);
    }
    return null;
  }


  @Override
  public UpdateResult updateRule(String datasetSchemaId, Rule rule) throws EEAException {
    return mongoTemplate.updateFirst(
        new Query(new Criteria("idDatasetSchema").is(new ObjectId(datasetSchemaId)))
            .addCriteria(new Criteria("rules.referenceId").is(rule.getReferenceId())),
        new Update().set("rules.$", rule), RulesSchema.class);
  }


}
