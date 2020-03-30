package org.eea.validation.persistence.repository;

import org.bson.types.ObjectId;
import org.eea.validation.persistence.schemas.rule.RuleSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * The Class RulesSequenceRepositoryImpl.
 */
public class RulesSequenceRepositoryImpl implements RulesSequenceRepository {

  /** The mongo template. */
  @Autowired
  private MongoTemplate mongoTemplate;

  /** The init seq. */
  private static final long INIT_SEQ = 1;

  /**
   * Creates the sequence.
   *
   * @param ruleSchemaId the rule schema id
   * @return the rule sequence
   */
  @Override
  public RuleSequence createSequence(ObjectId datasetSchemaId) {

    RuleSequence sequence = new RuleSequence();
    sequence.setDatasetSchemaId(datasetSchemaId);
    sequence.setRuleSequenceId(new ObjectId());
    sequence.setSeq(INIT_SEQ);

    return mongoTemplate.save(sequence);

  }


  /**
   * Update sequence.
   *
   * @param ruleSchemaId the rule schema id
   * @return the long
   */
  @Override
  public long updateSequence(ObjectId datasetSchemaId) {
    Query query = new Query(Criteria.where("ruleSchemaId").is(datasetSchemaId));

    /** Increase field sequence by 1 */
    Update update = new Update().inc("seq", 1);

    RuleSequence sequence = mongoTemplate.findAndModify(query, update, RuleSequence.class);

    long seq = INIT_SEQ;
    if (sequence != null) {
      seq = sequence.getSeq() + 1;
    } else {
      createSequence(datasetSchemaId);
    }

    return seq;

  }
}
