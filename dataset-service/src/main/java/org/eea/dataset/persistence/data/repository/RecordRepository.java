package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;


/**
 * The Interface RecordRepository.
 */
public interface RecordRepository extends PagingAndSortingRepository<RecordValue, Integer> {

  /**
   * Find by table value id.
   *
   * @param tableId the table id
   * @param pageable the pageable
   * @return the list
   */
  List<RecordValue> findByTableValue_id(Long tableId, Pageable pageable);

  /**
   * Find by table value id mongo.
   *
   * @param idMongo the id mongo
   * @param pageable the pageable
   * @return the list
   */
  List<RecordValue> findByTableValue_idMongo(String idMongo, Pageable pageable);

  /**
   * Count by table value id.
   *
   * @param idTableValue the id table value
   * @return the long
   */
  Long countByTableValue_id(Long idTableValue);

}
