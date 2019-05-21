/**
 * 
 */
package org.eea.dataset.persistence.schemas.repository;

import java.util.List;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Mario Severa
 *
 */
public interface SchemasRepository extends CrudRepository<DataSetSchema, String> {

  List<DataSetSchema> findByIdDataFlow(Long idDataFlow);

}
