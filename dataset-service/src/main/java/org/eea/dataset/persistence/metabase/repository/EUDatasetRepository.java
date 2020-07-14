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



}
