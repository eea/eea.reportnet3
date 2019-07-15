package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.springframework.data.jpa.repository.Query;
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
  @Query("SELECT d.dataflowId FROM DataSetMetabase d where d.id = ?1")
  Long findDataflowIdById(Long datasetId);


  /**
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<DataSetMetabase> findByDataflowId(Long dataflowId);

}
