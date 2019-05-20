/**
 * 
 */
package org.eea.dataset.persistance.schemas.repository;

import org.eea.dataset.persistance.schemas.domain.DataSetSchema;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Mario Severa
 *
 */
public interface SchemasRepository extends CrudRepository<DataSetSchema, String> {

}
