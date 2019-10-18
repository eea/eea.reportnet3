package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

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

  @Query(nativeQuery = true, value = "SELECT count(*)!=0 FROM dataflow d WHERE d.id=:idDataFlow")
  boolean findDataFlowById(@Param("idDataFlow") Long idDataFlow);

  /**
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<DataSetMetabase> findByDataflowId(Long dataflowId);

}
