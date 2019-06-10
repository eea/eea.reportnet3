package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * The Interface DatasetRepository.
 */
public interface DatasetRepository extends JpaRepository<DatasetValue, Long> {

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
   * Find validations by id dataset.
   *
   * @param datasetId the dataset id
   * @param idTable the id table
   * @return the list
   */
  @Query("SELECT rval FROM DatasetValue dat INNER JOIN dat.tableValues tv INNER JOIN tv.records rv "
      + "INNER JOIN rv.recordValidations rval WHERE dat.id=?1 and tv.id=?2")
  List<RecordValidation> findValidationsByIdDataset(Long datasetId, Long idTable);
}
