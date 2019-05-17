package org.eea.validation.repository;

import org.bson.types.ObjectId;
import org.eea.validation.model.DataSetSchema;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DataSetSchemaRepository extends MongoRepository<DataSetSchema, ObjectId> {

  default void deleteByTableSchemasNameSchema(String name) {}



}
