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
  @Query("SELECT rv FROM RecordValidation rv INNER JOIN FETCH rv.validation INNER JOIN FETCH rv.recordValue record WHERE record.id in (:recordIds)")
  List<RecordValidation> findByRecordValue_IdIn(@Param("recordIds") List<Long> recordIds);
}
