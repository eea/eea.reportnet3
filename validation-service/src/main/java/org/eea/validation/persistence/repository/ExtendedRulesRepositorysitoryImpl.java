package org.eea.validation.persistence.repository;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;

/**
 * The Class ExtendedRulesRepositorysitoryImpl.
 */
public class ExtendedRulesRepositorysitoryImpl implements ExtendedRulesRepository {

  /** The mongo operations. */
  @Autowired
  private MongoOperations mongoOperations;

  /** The mongo template. */
  @Autowired
  private MongoTemplate mongoTemplate;

  /** The mongo database. */
  @Autowired
  private MongoDatabase mongoDatabase;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Delete rule by reference id.
   *
   * @param referenceId the reference id
   */
  @Override
  public void deleteRuleByReferenceId(String referenceId) {
    Update update =
        new Update().pull("rules", new BasicDBObject("referenceId", new ObjectId(referenceId)));
    mongoOperations.updateMulti(new Query(), update, RulesSchema.class);

  }

  /**
   * Delete rule by id.
   *
   * @param idRuleSchema the id rule schema
   */
  @Override
  public void deleteRuleById(String idRuleSchema) {
    Update update =
        new Update().pull("rules", new BasicDBObject("_id", new ObjectId(idRuleSchema)));
    mongoOperations.updateMulti(new Query(), update, RulesSchema.class);

  }

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

}
