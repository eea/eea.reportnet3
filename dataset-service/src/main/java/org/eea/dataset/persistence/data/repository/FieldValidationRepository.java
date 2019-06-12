package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.FieldValidation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * The interface Field validation repository.
 */
public interface FieldValidationRepository extends CrudRepository<FieldValidation, Integer> {

  /**
   * Find by field value record table value id list.
   *
   * @param idTableSchema the id table Schema
   *
   * @return the list
   */
  List<FieldValidation> findByFieldValue_Record_TableValueIdTableSchema(String idTableSchema);
  
  
  /**
   * Find field validations by id dataset and id table.
   *
   * @param datasetId the dataset id
   * @param idTable the id table
   *
   * @return the list
   */
  @Query("SELECT fval FROM DatasetValue dat INNER JOIN dat.tableValues tv INNER JOIN tv.records rv "
      + "INNER JOIN rv.fields flds INNER JOIN flds.fieldValidations fval WHERE dat.id=?1 and tv.id=?2")
  List<FieldValidation> findFieldValidationsByIdDatasetAndIdTable(Long datasetId, Long idTable);
}
