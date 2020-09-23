package org.eea.validation.persistence.data.repository;


import org.eea.validation.persistence.data.domain.DatasetValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * The Interface DatasetRepository.
 */
public interface DatasetRepository
    extends JpaRepository<DatasetValue, Long>, DatasetExtendedRepository {

  /**
   * Empties the dataset.
   *
   * @param dataSetId the data set id
   */
  @Modifying
  @Query(nativeQuery = true, value = "delete from dataset_value")
  void empty(Long dataSetId);

  /**
   * Find id dataset schema by id.
   *
   * @param datasetId the dataset id
   * @return the string
   */
  @Query("SELECT d.idDatasetSchema from DatasetValue d where id=?1")
  String findIdDatasetSchemaById(Long datasetId);


  /**
   * Delete validation table.
   */
  @Modifying
  @Query(nativeQuery = true,
      value = "truncate table validation, dataset_validation, field_validation, record_validation, table_validation")
  void deleteValidationTable();

}
