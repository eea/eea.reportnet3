package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * The interface Field validation repository.
 */
public interface RecordValidationRepository extends CrudRepository<RecordValidation, Integer> {

  /**
   * Find by field value record table value id list.
   *
   * @param recordIds the record ids
   *
   * @return the list
   */
  @Query("SELECT rv FROM RecordValidation rv INNER JOIN FETCH rv.validation INNER JOIN FETCH rv.recordValue record "
      + "WHERE record.id in (:recordIds)")
  List<RecordValidation> findByRecordValue_IdIn(@Param("recordIds") List<Long> recordIds);



  /**
   * Find record validations by id dataset and id table.
   *
   * @param datasetId the dataset id
   * @param idTable the id table
   * @return the list
   */
  @Query("SELECT rval FROM RecordValidation rval INNER JOIN FETCH rval.validation INNER JOIN rval.recordValue rv "
      + "INNER JOIN rv.tableValue tab WHERE tab.datasetId.id=?1 and tab.id=?2")
  List<RecordValidation> findRecordValidationsByIdDatasetAndIdTable(Long datasetId, Long idTable);



  /**
   * Find record validations by id dataset and id table schema.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @return the list
   */
  @Query("SELECT rval FROM RecordValidation rval INNER JOIN FETCH rval.validation INNER JOIN rval.recordValue rv "
      + "INNER JOIN rv.tableValue tab WHERE tab.datasetId.id=?1 and tab.idTableSchema=?2")
  List<RecordValidation> findRecordValidationsByIdDatasetAndIdTableSchema(Long datasetId,
      String idTableSchema);


  /**
   * Find record validations by id dataset.
   *
   * @param datasetId the dataset id
   * @return the list
   */
  @Query("SELECT rval FROM RecordValidation rval INNER JOIN FETCH rval.validation INNER JOIN rval.recordValue rv "
      + "INNER JOIN rv.tableValue tab WHERE tab.datasetId.id=?1")
  List<RecordValidation> findRecordValidationsByIdDataset(Long datasetId);

}
