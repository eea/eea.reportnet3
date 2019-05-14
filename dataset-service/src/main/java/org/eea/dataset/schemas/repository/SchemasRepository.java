/**
 * 
 */
package org.eea.dataset.schemas.repository;

import org.eea.dataset.schemas.domain.DatasetSchema;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Mario Severa
 *
 */
public interface SchemasRepository extends MongoRepository<DatasetSchema, String> {

}
