package org.eea.validation.persistence.rules.repository;

import org.bson.types.ObjectId;
import org.eea.validation.persistence.rules.model.DataFlowRules;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * The Interface DataSetSchemaRepository.
 */
@Repository
public interface DataFlowRulesRepository extends MongoRepository<DataFlowRules, ObjectId> {

}
