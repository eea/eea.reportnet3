/**
 * 
 */
package org.eea.dataset.persistence.metabase.repository;

import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.springframework.data.repository.CrudRepository;

/**
 * The Interface DataSetMetabaseRepository.
 *
 * @author Mario Severa
 */
public interface DataSetMetabaseRepository extends CrudRepository<DataSetMetabase, Long> {

  /**
   * Find dataflow id by id.
   *
   * @param datasetId the dataset id
   * @return the long
   */
  Long findDataflowIdById(Long datasetId);

}
