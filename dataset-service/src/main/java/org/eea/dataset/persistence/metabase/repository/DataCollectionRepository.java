package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.springframework.data.repository.CrudRepository;


/**
 * The Interface DataCollectionRepository.
 */
public interface DataCollectionRepository extends CrudRepository<DataCollection, Long> {


  /**
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<DataCollection> findByDataflowId(Long dataflowId);


}
