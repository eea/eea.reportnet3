package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.TableValidation;
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
   * Count records by id table schema.
   *
   * @param idTableSchema the id table schema
   * @return the long
   */
  @Query("SELECT COUNT(rv) FROM TableValue tv INNER JOIN tv.records rv WHERE tv.idTableSchema=?1")
  Long countRecordsByIdTableSchema(String idTableSchema);



  /**
   * Find by id and dataset id id.
   *
   * @param id the id
   * @param idDataset the id dataset
   * @return the table value
   */
  TableValue findByIdAndDatasetId_Id(Long id, Long idDataset);


  /**
   * Find table validations by id dataset.
   *
   * @param datasetId the dataset id
   * @return the list
   */
  @Query("SELECT tval FROM DatasetValue dat INNER JOIN dat.tableValues tv INNER JOIN tv.tableValidations tval "
      + "WHERE dat.id=?1")
  List<TableValidation> findTableValidationsByIdDataset(Long datasetId);


  /**
   * Find id by id table schema.
   *
   * @param idTableSchema the id table schema
   * @return the long
   */
  @Query("SELECT DISTINCT TV.id FROM TableValue TV WHERE TV.idTableSchema=?1 ")
  Long findIdByIdTableSchema(String idTableSchema);
}
