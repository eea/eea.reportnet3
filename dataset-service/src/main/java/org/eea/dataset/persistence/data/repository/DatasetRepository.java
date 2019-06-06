package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * The Interface DatasetRepository.
 */
public interface DatasetRepository extends CrudRepository<DatasetValue, Long> {


  /**
   * Remove dataset data including all data inside tables and tables themselves.
   *
   * @param dataSetId the data set id
   */
  @Modifying
  @Query(nativeQuery = true, value = "delete from dataset_value")
  void removeDatasetData(Long dataSetId);
}
