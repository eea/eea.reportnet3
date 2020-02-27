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
   * Gets the rules with type rule criteria.
   *
   * @param idDatasetSchema the id dataset schema
   * @param type the type
   * @return the rules with type rule criteria
   */
  @Override
  public RulesSchema getRulesWithTypeRuleCriteria(ObjectId idDatasetSchema, Boolean required) {
    List<RulesSchema> result;
    if (Boolean.TRUE.equals(required)) {
      Document filterExpression = new Document();
      filterExpression.append("input", "$rules");
      filterExpression.append("as", "rule");
      // look for FC rules
      filterExpression.append("cond",
          new Document("$lte", Arrays.asList("$$rule.shortCode", "FT")));
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
      Document filterExpression = new Document();
      filterExpression.append("input", "$rules");
      filterExpression.append("as", "rule");
      filterExpression.append("cond", new Document("$gt", Arrays.asList("$$rule.shortCode", "FT")));
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
  public boolean insertRuleInPosition(String datasetSchemaId, Rule rule, int position) {
    Query query =
        new Query().addCriteria(new Criteria("idDatasetSchema").is(new ObjectId(datasetSchemaId)));
    Update update = new Update().push("rules").atPosition(position).each(rule);
    return mongoTemplate.updateFirst(query, update, Rule.class, "RulesSchema")
        .getModifiedCount() == 1;
  }



  /**
   * Find rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param ruleId the rule id
   * @return the rule
   */
  @CheckForNull
  @Override
  public Rule findRule(String datasetSchemaId, String ruleId) {
    ObjectId datasetSchemaOId = new ObjectId(datasetSchemaId);
    ObjectId ruleOId = new ObjectId(ruleId);
    Document filterExpression = new Document();
    filterExpression.append("input", "$rules");
    filterExpression.append("as", "rule");
    filterExpression.append("cond", new Document("$eq", Arrays.asList("$$rule._id", ruleOId)));
    Document filter = new Document("$filter", filterExpression);
    RulesSchema rulesSchema = mongoTemplate.aggregate(
        Aggregation.newAggregation(
            Aggregation.match(Criteria.where("idDatasetSchema").is(datasetSchemaOId)),
            Aggregation.project().and(aggregationOperationContext -> filter).as("rules")),
        RulesSchema.class, RulesSchema.class).getUniqueMappedResult();
    if (rulesSchema != null) {
      List<Rule> rules = rulesSchema.getRules();
      if (rules != null && !rules.isEmpty()) {
        return rules.get(0);
      }
    }
    return null;
  }

  /**
   * Delete rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param ruleId the rule id
   * @return true, if successful
   */
  @Override
  public boolean deleteRule(String datasetSchemaId, String ruleId) {
    ObjectId datasetSchemaOId = new ObjectId(datasetSchemaId);
    ObjectId ruleOId = new ObjectId(ruleId);
    Query query = new Query(new Criteria("idDatasetSchema").is(datasetSchemaOId));
    Update update = new Update().pull("rules", new Document("_id", ruleOId));
    return mongoTemplate.updateFirst(query, update, RulesSchema.class).getModifiedCount() == 1;
  }


  /**
   * Update rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @return the update result
   * @throws EEAException the EEA exception
   */
  @Override
  public boolean updateRule(String datasetSchemaId, Rule rule) {
    rule.setAutomatic(false);
    return mongoTemplate.updateFirst(
        new Query(new Criteria("idDatasetSchema").is(new ObjectId(datasetSchemaId)))
            .addCriteria(new Criteria("rules.referenceId").is(rule.getReferenceId())),
        new Update().set("rules.$", rule), RulesSchema.class).getModifiedCount() == 1;
  }


}
