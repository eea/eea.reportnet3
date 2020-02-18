package org.eea.validation.persistence.repository;

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
  public Document getRulesWithActiveCriteria(ObjectId idDatasetSchema, Boolean enable) {
    Document document;
    if (Boolean.TRUE.equals(enable)) {
      document = mongoDatabase.getCollection("RulesSchema")
          .find(new Document("_id", idDatasetSchema).append("rules.enable", true)).first();
    } else {
      document = mongoDatabase.getCollection("RulesSchema")
          .find(new Document("_id", idDatasetSchema)).first();
    }

    return document;
  }

  @Override
  public void createNewRule(String idRuleSchema, String idSchema, Rule rule) throws EEAException {
    Update update = new Update().push("rules", rule);
    Query query = new Query();
    query.addCriteria(new Criteria("_id").is(new ObjectId(idRuleSchema)));
    mongoOperations.updateMulti(query, update, RulesSchema.class);
  }


  //
  // UpdateResult updateTableSchema(String datasetSchemaId, Document tableSchema)
  // throws EEAException {
  // try {
  // return mongoDatabase.getCollection("DataSetSchema").updateOne(
  // new Document("_id", new ObjectId(datasetSchemaId)).append("tableSchemas._id",
  // tableSchema.get("_id")),
  // new Document("$set", new Document("tableSchemas.$[tableSchemaId]", tableSchema)),
  // new UpdateOptions().arrayFilters(
  // Arrays.asList(new Document("tableSchemaId._id", tableSchema.get("_id")))));
  // } catch (IllegalArgumentException e) {
  // LOG_ERROR.error("error updating table: ", e);
  // throw new EEAException(e);
  // }
  // }

}
