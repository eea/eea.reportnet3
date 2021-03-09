package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import java.util.Optional;
import org.eea.dataset.persistence.metabase.domain.TestDataset;
import org.springframework.data.repository.CrudRepository;



/**
 * The Interface EUDatasetRepository.
 */
public interface TestDatasetRepository extends CrudRepository<TestDataset, Long> {


  /**
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<TestDataset> findByDataflowId(Long dataflowId);

  /**
   * Find by dataflow id and dataset schema.
   *
   * @param dataflowId the dataflow id
   * @param datasetSchema the dataset schema
   * @return the list
   */
  List<TestDataset> findByDataflowIdAndDatasetSchema(Long dataflowId, String datasetSchema);

  /**
   * Find first by dataset schema.
   *
   * @param datasetSchema the dataset schema
   * @return the optional
   */
  Optional<TestDataset> findFirstByDatasetSchema(String datasetSchema);


}
