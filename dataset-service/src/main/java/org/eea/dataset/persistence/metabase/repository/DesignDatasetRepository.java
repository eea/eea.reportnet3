package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import java.util.Optional;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.springframework.data.repository.CrudRepository;

/**
 * The Interface DesignDatasetRepository.
 */
public interface DesignDatasetRepository extends CrudRepository<DesignDataset, Long> {


  /**
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<DesignDataset> findByDataflowId(Long dataflowId);

  /**
   * Find first by dataset schema.
   *
   * @param datasetSchema the dataset schema
   * @return the optional
   */
  Optional<DesignDataset> findFirstByDatasetSchema(String datasetSchema);

}
