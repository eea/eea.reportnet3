package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * The Interface DatasetRepository.
 */
public interface DatasetRepository
    extends JpaRepository<DatasetValue, Long>, DatasetExtendedRepository {

  /**
   * Remove dataset data including all data inside tables and tables themselves.
   *
   * @param dataSetId the data set id
   */
  @Modifying
  @Query(nativeQuery = true,
      value = "truncate table field_validation, field_value, record_validation, record_value, table_validation, dataset_validation, validation")
  void removeDatasetData(Long dataSetId);

  /**
   * Find id dataset schema by id.
   *
   * @param datasetId the dataset id
   * @return the datasetSchemaId
   */
  @Query("SELECT d.idDatasetSchema from DatasetValue d where id=?1")
  String findIdDatasetSchemaById(Long datasetId);

  /**
   * Update check view.
   *
   * @param datasetId the dataset id
   * @param updated the updated
   */
  @Modifying
  @Query("UPDATE DatasetValue SET viewUpdated=:updated WHERE id=:datasetId")
  void updateCheckView(@Param("datasetId") Long datasetId, @Param("updated") Boolean updated);

  /**
   * Find view updated by id.
   *
   * @param datasetId the dataset id
   * @return the boolean
   */
  @Query("SELECT viewUpdated FROM DatasetValue WHERE id=:datasetId")
  Boolean findViewUpdatedById(@Param("datasetId") Long datasetId);
}
