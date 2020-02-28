package org.eea.validation.persistence.repository;

import java.util.Arrays;
import java.util.List;
import javax.annotation.CheckForNull;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * The Class ExtendedRulesRepositoryImpl.
 */
public class ExtendedRulesRepositoryImpl implements ExtendedRulesRepository {

  /** The mongo template. */
  @Autowired
  private MongoTemplate mongoTemplate;

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
   * @param datasetSchemaId the dataset schema id
   * @param ruleId the rule id
   * @return true, if successful
   */
  @Override
  public boolean deleteRuleById(String datasetSchemaId, String ruleId) {
    Document pullCriteria = new Document("_id", new ObjectId(ruleId));
    Update update = new Update().pull("rules", pullCriteria);
    Query query = new Query(new Criteria("idDatasetSchema").is(new ObjectId(datasetSchemaId)));
    return mongoTemplate.updateFirst(query, update, RulesSchema.class).getModifiedCount() == 1;
  }

  /**
   * Delete rule by reference id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return true, if successful
   */
  @Override
  public boolean deleteRuleByReferenceId(String datasetSchemaId, String referenceId) {
    Document pullCriteria = new Document("referenceId", new ObjectId(referenceId));
    Update update = new Update().pull("rules", pullCriteria);
    Query query = new Query(new Criteria("idDatasetSchema").is(new ObjectId(datasetSchemaId)));
    return mongoTemplate.updateMulti(query, update, RulesSchema.class).getModifiedCount() == 1;
  }

  /**
   * Delete rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return true, if successful
   */
  @Override
  public boolean deleteRuleRequired(String datasetSchemaId, String referenceId) {
    Document pullCriteria = new Document("referenceId", new ObjectId(referenceId))
        .append("whenCondition", "!isBlank(value)");
    Update update = new Update().pull("rules", pullCriteria);
    Query query = new Query(new Criteria("idDatasetSchema").is(new ObjectId(datasetSchemaId)));
    return mongoTemplate.updateFirst(query, update, RulesSchema.class).getModifiedCount() == 1;
  }

  /**
   * Creates the new rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @return true, if successful
   */
  @Override
  public boolean createNewRule(String datasetSchemaId, Rule rule) {
    Update update = new Update().push("rules", rule);
    Query query = new Query();
    query.addCriteria(new Criteria("idDatasetSchema").is(new ObjectId(datasetSchemaId)));
    return mongoTemplate.updateMulti(query, update, RulesSchema.class).getModifiedCount() == 1;
  }

  /**
   * Update rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @return true, if successful
   */
  @Override
  public boolean updateRule(String datasetSchemaId, Rule rule) {
    rule.setAutomatic(false);
    return mongoTemplate.updateFirst(
        new Query(new Criteria("idDatasetSchema").is(new ObjectId(datasetSchemaId)))
            .addCriteria(new Criteria("rules.referenceId").is(rule.getReferenceId())),
        new Update().set("rules.$", rule), RulesSchema.class).getModifiedCount() == 1;
  }

  /**
   * Exists rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return true, if successful
   */
  @Override
  public boolean existsRuleRequired(String datasetSchemaId, String referenceId) {
    Query query = new Query(new Criteria("idDatasetSchema").is(new ObjectId(datasetSchemaId)))
        .addCriteria(new Criteria("rules.referenceId").is(new ObjectId(referenceId)))
        .addCriteria(new Criteria("rules.whenCondition").is("!isBlank(value)"));
    return mongoTemplate.count(query, RulesSchema.class) == 1;
  }

  /**
   * Insert rule in position.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @param position the position
   * @return true, if successful
   */
  @Override
  public boolean insertRuleInPosition(String datasetSchemaId, Rule rule, int position) {
    Query query = new Query(new Criteria("idDatasetSchema").is(new ObjectId(datasetSchemaId)));
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
  @Override
  @CheckForNull
  public Rule findRule(String datasetSchemaId, String ruleId) {
    Document filterExpression = new Document();
    filterExpression.append("input", "$rules");
    filterExpression.append("as", "rule");
    filterExpression.append("cond",
        new Document("$eq", Arrays.asList("$$rule._id", new ObjectId(ruleId))));
    Document filter = new Document("$filter", filterExpression);
    RulesSchema rulesSchema =
        mongoTemplate.aggregate(
            Aggregation.newAggregation(
                Aggregation
                    .match(Criteria.where("idDatasetSchema").is(new ObjectId(datasetSchemaId))),
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
   * Gets the rules with active criteria.
   *
   * @param datasetSchemaId the dataset schema id
   * @param enable the enable
   * @return the rules with active criteria
   */
  @Override
  @CheckForNull
  public RulesSchema getRulesWithActiveCriteria(ObjectId datasetSchemaId, boolean enable) {
    List<RulesSchema> result;
    if (enable) {
      Document filterExpression = new Document();
      filterExpression.append("input", "$rules");
      filterExpression.append("as", "rule");
      filterExpression.append("cond", new Document("$eq", Arrays.asList("$$rule.enabled", true)));
      Document filter = new Document("$filter", filterExpression);
      result =
          mongoTemplate
              .aggregate(
                  Aggregation.newAggregation(
                      Aggregation.match(Criteria.where("idDatasetSchema").is(datasetSchemaId)),
                      Aggregation.project("idDatasetSchema")
                          .and(aggregationOperationContext -> filter).as("rules")),
                  RulesSchema.class, RulesSchema.class)
              .getMappedResults();
    } else {
      Query query = new Query();
      query.addCriteria(new Criteria("idDatasetSchema").is(datasetSchemaId));
      result = mongoTemplate.find(query, RulesSchema.class);
    }

    return result.isEmpty() ? null : result.get(0);
  }

  /**
   * Gets the rules with type rule criteria.
   *
   * @param idDatasetSchema the id dataset schema
   * @param required the required
   * @return the rules with type rule criteria
   */
  @Override
  @CheckForNull
  public RulesSchema getRulesWithTypeRuleCriteria(ObjectId idDatasetSchema, boolean required) {
    List<RulesSchema> result;
    if (required) {
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
}
