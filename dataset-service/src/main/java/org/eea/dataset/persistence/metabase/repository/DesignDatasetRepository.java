package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
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

}
