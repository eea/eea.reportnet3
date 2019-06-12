package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.FieldValidation;
import org.springframework.data.repository.CrudRepository;

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
  List<FieldValidation> findByFieldValue_RecordIdIn(List<Long> recordIds);
}
