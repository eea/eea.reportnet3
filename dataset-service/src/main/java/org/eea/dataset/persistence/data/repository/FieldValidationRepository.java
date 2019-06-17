package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.FieldValidation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * The interface Field validation repository.
 */
public interface FieldValidationRepository extends CrudRepository<FieldValidation, Integer> {


  /**
   * Find by field value record id list.
   *
   * @param recordIds the record ids
   *
   * @return the list
   */
  @Query("SELECT fv FROM FieldValidation fv INNER JOIN FETCH fv.validation INNER JOIN FETCH fv.fieldValue field "
      + "WHERE field.record.id in (:recordIds)")
  List<FieldValidation> findByFieldValue_RecordIdIn(@Param("recordIds") List<Long> recordIds);



  /**
   * Find field validations by id dataset and id table.
   *
   * @param datasetId the dataset id
   * @param idTable the id table
   * @return the list
   */
  @Query("SELECT fv FROM FieldValidation fv INNER JOIN FETCH fv.validation INNER JOIN fv.fieldValue field "
      + "INNER JOIN field.record rc INNER JOIN rc.tableValue tab WHERE tab.datasetId.id=?1 and tab.id=?2")
  List<FieldValidation> findFieldValidationsByIdDatasetAndIdTable(Long datasetId, Long idTable);


  /**
   * Find field validations by id dataset and id table schema.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @return the list
   */
  @Query("SELECT fv FROM FieldValidation fv INNER JOIN FETCH fv.validation INNER JOIN fv.fieldValue field "
      + "INNER JOIN field.record rc INNER JOIN rc.tableValue tab WHERE tab.datasetId.id=?1 and tab.idTableSchema=?2")
  List<FieldValidation> findFieldValidationsByIdDatasetAndIdTableSchema(Long datasetId,
      String idTableSchema);



  /**
   * Find field validations by id dataset.
   *
   * @param datasetId the dataset id
   * @return the list
   */
  @Query("SELECT fv FROM FieldValidation fv INNER JOIN FETCH fv.validation INNER JOIN fv.fieldValue field "
      + "INNER JOIN field.record rc INNER JOIN rc.tableValue tab WHERE tab.datasetId.id=?1")
  List<FieldValidation> findFieldValidationsByIdDataset(Long datasetId);

}
