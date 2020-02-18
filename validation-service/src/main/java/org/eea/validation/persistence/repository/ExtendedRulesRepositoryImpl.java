package org.eea.validation.persistence.repository;

import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;

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
    mongoOperations.updateMulti(new Query(), update, RulesSchema.class);
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

}
