/**
 * 
 */
package org.eea.dataset.persistence.schemas.repository;

import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Mario Severa
 *
 */
public interface SchemasRepository extends CrudRepository<DataSetSchema, String> {

}
