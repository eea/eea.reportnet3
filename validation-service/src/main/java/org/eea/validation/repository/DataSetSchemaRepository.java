package org.eea.validation.repository;

import org.bson.types.ObjectId;
import org.eea.validation.model.DataSetSchema;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The Interface DataSetSchemaRepository.
 */
public interface DataSetSchemaRepository extends MongoRepository<DataSetSchema, ObjectId> {

}
