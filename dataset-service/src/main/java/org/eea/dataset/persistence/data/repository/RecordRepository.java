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
  List<RecordValue> findByTableValue_Id(Long tableId, Pageable pageable);
}
