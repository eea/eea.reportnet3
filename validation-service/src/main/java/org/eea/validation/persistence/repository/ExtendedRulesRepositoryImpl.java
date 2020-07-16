package org.eea.validation.persistence.repository;

import java.util.Arrays;
import java.util.List;
import javax.annotation.CheckForNull;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.utils.LiteralConstants;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.client.result.UpdateResult;

/**
 * The Class ExtendedRulesRepositoryImpl.
 */
public class ExtendedRulesRepositoryImpl implements ExtendedRulesRepository {
  

  /** The Constant RULES: {@value}. */
  private static final String RULES = "$rules";

  /** The Constant INPUT: {@value}. */
  private static final String INPUT = "input";

  /** The Constant FILTER: {@value}. */
  private static final String FILTER = "$filter";

  /** The Constant WHENCONDITION: {@value}. */
  private static final String WHEN_CONDITION = "whenCondition";

  /** The Constant REFERENCEID: {@value}. */
  private static final String REFERENCE_ID = "referenceId";

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
  public boolean deleteRuleById(ObjectId datasetSchemaId, ObjectId ruleId) {
    Document pullCriteria = new Document("_id", ruleId);
    Update update = new Update().pull(LiteralConstants.RULES, pullCriteria);
    Query query = new Query(new Criteria(LiteralConstants.ID_DATASET_SCHEMA).is(datasetSchemaId));
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
  public boolean deleteRuleByReferenceId(ObjectId datasetSchemaId, ObjectId referenceId) {
    Document pullCriteria = new Document(REFERENCE_ID, referenceId);
    Update update = new Update().pull(LiteralConstants.RULES, pullCriteria);
    Query query = new Query(new Criteria(LiteralConstants.ID_DATASET_SCHEMA).is(datasetSchemaId));
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
  public boolean deleteRuleRequired(ObjectId datasetSchemaId, ObjectId referenceId) {
    Document pullCriteria =
        new Document(REFERENCE_ID, referenceId).append(WHEN_CONDITION, "isBlank(value)");
    Update update = new Update().pull(LiteralConstants.RULES, pullCriteria);
    Query query = new Query(new Criteria(LiteralConstants.ID_DATASET_SCHEMA).is(datasetSchemaId));
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
  public boolean createNewRule(ObjectId datasetSchemaId, Rule rule) {
    Update update = new Update().push(LiteralConstants.RULES, rule);
    Query query = new Query();
    query.addCriteria(new Criteria(LiteralConstants.ID_DATASET_SCHEMA).is(datasetSchemaId));
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
  public boolean updateRule(ObjectId datasetSchemaId, Rule rule) {
    UpdateResult result = mongoTemplate.updateFirst(
        new Query(new Criteria(LiteralConstants.ID_DATASET_SCHEMA).is(datasetSchemaId))
            .addCriteria(new Criteria("rules._id").is(rule.getRuleId())),
        new Update().set("rules.$", rule), RulesSchema.class);
    return result.getMatchedCount() > 0 || result.getModifiedCount() > 0;
  }

  /**
   * Exists rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return true, if successful
   */
  @Override
  public boolean existsRuleRequired(ObjectId datasetSchemaId, ObjectId referenceId) {
    Query query = new Query(new Criteria(LiteralConstants.ID_DATASET_SCHEMA).is(datasetSchemaId))
        .addCriteria(Criteria.where(LiteralConstants.RULES).elemMatch(
            Criteria.where(REFERENCE_ID).is(referenceId).and(WHEN_CONDITION).is("isBlank(value)")));
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
  public boolean insertRuleInPosition(ObjectId datasetSchemaId, Rule rule, int position) {
    Query query = new Query(new Criteria(LiteralConstants.ID_DATASET_SCHEMA).is(datasetSchemaId));
    Update update = new Update().push(LiteralConstants.RULES).atPosition(position).each(rule);
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
  public Rule findRule(ObjectId datasetSchemaId, ObjectId ruleId) {
    Document filterExpression = new Document();
    filterExpression.append(INPUT, RULES);
    filterExpression.append("as", "rule");
    filterExpression.append("cond", new Document("$eq", Arrays.asList("$$rule._id", ruleId)));
    Document filter = new Document(FILTER, filterExpression);
    RulesSchema rulesSchema = mongoTemplate.aggregate(Aggregation.newAggregation(
        Aggregation.match(Criteria.where(LiteralConstants.ID_DATASET_SCHEMA).is(datasetSchemaId)),
        Aggregation.project().and(aggregationOperationContext -> filter)
            .as(LiteralConstants.RULES)),
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
      filterExpression.append(INPUT, RULES);
      filterExpression.append("as", "rule");
      filterExpression.append("cond", new Document("$eq", Arrays.asList("$$rule.enabled", true)));
      Document filter = new Document(FILTER, filterExpression);
      result = mongoTemplate.aggregate(Aggregation.newAggregation(
          Aggregation.match(Criteria.where(LiteralConstants.ID_DATASET_SCHEMA).is(datasetSchemaId)),
          Aggregation.project(LiteralConstants.ID_DATASET_SCHEMA)
              .and(aggregationOperationContext -> filter).as("rules")),
          RulesSchema.class, RulesSchema.class).getMappedResults();
    } else {
      Query query = new Query();
      query.addCriteria(new Criteria(LiteralConstants.ID_DATASET_SCHEMA).is(datasetSchemaId));
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
      filterExpression.append(INPUT, RULES);
      filterExpression.append("as", "rule");
      // look for FC rules
      filterExpression.append("cond",
          new Document("$lte", Arrays.asList("$$rule.shortCode", "FT")));
      Document filter = new Document(FILTER, filterExpression);
      result = mongoTemplate.aggregate(Aggregation.newAggregation(
          Aggregation.match(Criteria.where(LiteralConstants.ID_DATASET_SCHEMA).is(idDatasetSchema)),
          Aggregation.project(LiteralConstants.ID_DATASET_SCHEMA)
              .and(aggregationOperationContext -> filter).as(LiteralConstants.RULES)),
          RulesSchema.class, RulesSchema.class).getMappedResults();
    } else {
      Document filterExpression = new Document();
      filterExpression.append(INPUT, RULES);
      filterExpression.append("as", "rule");
      filterExpression.append("cond", new Document("$gt", Arrays.asList("$$rule.shortCode", "FT")));
      Document filter = new Document(FILTER, filterExpression);
      result = mongoTemplate.aggregate(Aggregation.newAggregation(
          Aggregation.match(Criteria.where(LiteralConstants.ID_DATASET_SCHEMA).is(idDatasetSchema)),
          Aggregation.project(LiteralConstants.ID_DATASET_SCHEMA)
              .and(aggregationOperationContext -> filter).as(LiteralConstants.RULES)),
          RulesSchema.class, RulesSchema.class).getMappedResults();
    }

    return result.isEmpty() ? null : result.get(0);
  }

  /**
   * Delete rule by reference field schema PK id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceFieldSchemaPKId the reference field schema PK id
   * @return true, if successful
   */
  @Override
  public boolean deleteRuleByReferenceFieldSchemaPKId(ObjectId datasetSchemaId,
      ObjectId referenceFieldSchemaPKId) {
    Document pullCriteria = new Document("referenceFieldSchemaPKId", referenceFieldSchemaPKId);
    Update update = new Update().pull(LiteralConstants.RULES, pullCriteria);
    Query query = new Query(new Criteria(LiteralConstants.ID_DATASET_SCHEMA).is(datasetSchemaId));
    return mongoTemplate.updateMulti(query, update, RulesSchema.class).getModifiedCount() == 1;
  }

  /**
   * Gets the active and verified rules.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the active and verified rules
   */
  @Override
  @CheckForNull
  public RulesSchema getActiveAndVerifiedRules(ObjectId datasetSchemaId) {
    List<RulesSchema> result;

    Document enabled = new Document("$eq", Arrays.asList("$$rule.enabled", true));
    Document verified = new Document("$eq", Arrays.asList("$$rule.verified", true));
    Document filterExpression = new Document();
    filterExpression.append(INPUT, RULES);
    filterExpression.append("as", "rule");
    filterExpression.append("cond", new Document("$and", Arrays.asList(enabled, verified)));
    Document filter = new Document(FILTER, filterExpression);
    result = mongoTemplate.aggregate(Aggregation.newAggregation(
        Aggregation.match(Criteria.where(LiteralConstants.ID_DATASET_SCHEMA).is(datasetSchemaId)),
        Aggregation.project(LiteralConstants.ID_DATASET_SCHEMA)
            .and(aggregationOperationContext -> filter).as(LiteralConstants.RULES)),
        RulesSchema.class, RulesSchema.class).getMappedResults();

    return result.isEmpty() ? null : result.get(0);
  }

  /**
   * Delete by unique constraint id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param uniqueConstraintId the unique constraint id
   * @return true, if successful
   */
  @Override
  public boolean deleteByUniqueConstraintId(ObjectId datasetSchemaId, ObjectId uniqueConstraintId) {
    Document pullCriteria = new Document("uniqueConstraintId", uniqueConstraintId);
    Update update = new Update().pull("rules", pullCriteria);
    Query query = new Query(new Criteria(LiteralConstants.ID_DATASET_SCHEMA).is(datasetSchemaId));
    return mongoTemplate.updateMulti(query, update, RulesSchema.class).getModifiedCount() == 1;
  }

  /**
   * Delete rule high level like.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaLike the field schema like
   * @return true, if successful
   */
  @Override
  public boolean deleteRuleHighLevelLike(ObjectId datasetSchemaId, String fieldSchemaLike) {
    Document pullCriteria =
        new Document(WHEN_CONDITION, java.util.regex.Pattern.compile(fieldSchemaLike));
    Update update = new Update().pull(LiteralConstants.RULES, pullCriteria);
    Query query = new Query(new Criteria(LiteralConstants.ID_DATASET_SCHEMA).is(datasetSchemaId));
    return mongoTemplate.updateMulti(query, update, RulesSchema.class).getModifiedCount() == 1;
  }
}
