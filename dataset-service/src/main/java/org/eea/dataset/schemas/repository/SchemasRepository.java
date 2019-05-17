/**
 * 
 */
package org.eea.dataset.schemas.repository;

import org.eea.dataset.schemas.domain.DataSetSchema;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Mario Severa
 *
 */
public interface SchemasRepository extends CrudRepository<DataSetSchema, String> {

}
