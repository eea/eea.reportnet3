package org.eea.validation.persistance.rules.repository;

import org.bson.types.ObjectId;
import org.eea.validation.persistance.rules.model.DataFlowRules;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The Interface DataSetSchemaRepository.
 */
public interface DataFlowRulesRepository extends MongoRepository<DataFlowRules, ObjectId> {

}
