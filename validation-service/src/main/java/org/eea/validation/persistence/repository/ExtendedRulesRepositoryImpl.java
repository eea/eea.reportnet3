package org.eea.validation.persistence.repository;

import org.bson.types.ObjectId;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.BasicDBObject;

/**
 * The Class ExtendedRulesRepositorysitoryImpl.
 */
public class ExtendedRulesRepositoryImpl implements ExtendedRulesRepository {


  /** The mongo operations. */
  @Autowired
  private MongoOperations mongoOperations;

  /**
   * Delete rule by id.
   *
   * @param ruleId the rule id
   */
  @Override
  public void deleteRuleById(String ruleId) {
    Update update = new Update().pull("rules", new BasicDBObject("_id", new ObjectId(ruleId)));
    mongoOperations.updateMulti(new Query(), update, RulesSchema.class);
  }


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

}
