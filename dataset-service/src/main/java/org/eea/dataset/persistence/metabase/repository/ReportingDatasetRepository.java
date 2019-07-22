package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * The Interface DataSetMetabaseRepository.
 *
 * @author Mario Severa
 */
public interface ReportingDatasetRepository extends CrudRepository<ReportingDataset, Long> {

  /**
   * Find dataflow id by id.
   *
   * @param datasetId the dataset id
   * @return the long
   */
  @Query("SELECT d.dataflowId FROM ReportingDataset d where d.id = ?1")
  Long findDataflowIdById(Long datasetId);


  /**
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<ReportingDataset> findByDataflowId(Long dataflowId);

}
