package org.eea.validation.persistence.repository;

import org.bson.types.ObjectId;
import org.eea.validation.persistence.schemas.rule.RuleSequence;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RulesSequenceRepository
    extends MongoRepository<RuleSequence, ObjectId>, ExtendedRulesSequenceRepository {

}
