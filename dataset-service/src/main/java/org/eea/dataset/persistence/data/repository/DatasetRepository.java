package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * The Interface DatasetRepository.
 */
public interface DatasetRepository extends JpaRepository<DatasetValue, Long> {


  /**
   * Remove dataset data including all data inside tables and tables themselves.
   *
   * @param dataSetId the data set id
   */
  @Modifying
  @Query(nativeQuery = true,
      value = "truncate table field_validation, field_value, record_validation, record_value, table_validation, table_value, dataset_validation, validation, dataset_value")
  void removeDatasetData(Long dataSetId);

  /**
   * Find id dataset schema by id.
   *
   * @param datasetId the dataset id
   * @return the string
   */
  @Query("SELECT d.idDatasetSchema from DatasetValue d where id=?1")
  String findIdDatasetSchemaById(Long datasetId);
}
