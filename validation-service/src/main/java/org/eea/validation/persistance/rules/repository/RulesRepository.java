package org.eea.validation.persistance.rules.repository;

import org.bson.types.ObjectId;
import org.eea.validation.persistance.rules.model.RulesModel;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The Interface DataSetSchemaRepository.
 */
public interface RulesRepository extends MongoRepository<RulesModel, ObjectId> {

}
