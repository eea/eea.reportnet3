package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import org.eea.dataset.persistence.metabase.domain.EUDataset;
import org.springframework.data.repository.CrudRepository;



/**
 * The Interface EUDatasetRepository.
 */
public interface EUDatasetRepository extends CrudRepository<EUDataset, Long> {


  /**
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<EUDataset> findByDataflowId(Long dataflowId);

  /**
   * Find by dataflow id and dataset schema.
   *
   * @param dataflowId the dataflow id
   * @param datasetSchema the dataset schema
   * @return the list
   */
  List<EUDataset> findByDataflowIdAndDatasetSchema(Long dataflowId, String datasetSchema);

}
