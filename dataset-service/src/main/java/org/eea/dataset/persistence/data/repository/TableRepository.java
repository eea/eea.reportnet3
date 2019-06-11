package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * The Interface TableRepository.
 */
public interface TableRepository extends JpaRepository<TableValue, Long> {


  
  
  /**
   * Find all tables.
   *
   * @return the list
   */
  @Query(value = "SELECT u FROM TableValue u")
  List<TableValue> findAllTables();
  
  /**
   * Count records by id table.
   *
   * @param id the id
   * @return the long
   */
  @Query("SELECT COUNT(rv) FROM TableValue tv INNER JOIN tv.records rv WHERE tv.id=?1")
  Long countRecordsByIdTable(Long id);
  
  
  /**
   * Find record validations by id dataset and id table.
   *
   * @param datasetId the dataset id
   * @param idTable the id table
   * @return the list
   */
  @Query("SELECT rval FROM DatasetValue dat INNER JOIN dat.tableValues tv INNER JOIN tv.records rv "
      + "INNER JOIN rv.recordValidations rval WHERE dat.id=?1 and tv.id=?2")
  List<RecordValidation> findRecordValidationsByIdDatasetAndIdTable(Long datasetId, Long idTable);
  
  
  
  /**
   * Find by id and dataset id id.
   *
   * @param id the id
   * @param idDataset the id dataset
   * @return the table value
   */
  TableValue findByIdAndDatasetId_Id(Long id, Long idDataset);
}
