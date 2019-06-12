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
  @Query("SELECT fv FROM FieldValidation fv INNER JOIN FETCH fv.validation INNER JOIN FETCH fv.fieldValue field WHERE field.record.id in (:recordIds)")
  List<FieldValidation> findByFieldValue_RecordIdIn(@Param("recordIds") List<Long> recordIds);


}
