/**
 * 
 */
package org.eea.dataset.persistance.schemas.repository;

import org.bson.types.ObjectId;
import org.eea.dataset.persistance.schemas.domain.DataSetSchema;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Mario Severa
 *
 */
public interface SchemasRepository extends MongoRepository<DataSetSchema, ObjectId> {

}
