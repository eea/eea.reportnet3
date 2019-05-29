/**
 * 
 */
package org.eea.dataset.persistence.schemas.repository;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * @author Mario Severa
 *
 */
public interface SchemasRepository extends MongoRepository<DataSetSchema, ObjectId> {

  @Query("{'idDataFlow': ?0}")
  DataSetSchema findSchemaByIdFlow(Long idFlow);
  
}
