package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.springframework.data.repository.CrudRepository;

/**
 * The interface Field validation repository.
 */
public interface RecordValidationRepository extends CrudRepository<RecordValidation, Integer> {

  /**
   * Find by field value record table value id list.
   *
   * @param idTableSchema the id table Schema
   *
   * @return the list
   */
  List<RecordValidation> findByRecordValue_TableValueIdTableSchema(String idTableSchema);
}
