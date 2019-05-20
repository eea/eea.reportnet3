/**
 * 
 */
package org.eea.dataset.schemas.repository;

import org.bson.types.ObjectId;
import org.eea.dataset.schemas.domain.DataSetSchema;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Mario Severa
 *
 */
public interface SchemasRepository extends MongoRepository<DataSetSchema, ObjectId> {

}
