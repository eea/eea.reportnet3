package org.eea.validation.persistence.data.repository;

import java.util.List;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.springframework.data.domain.Pageable;


/**
 * The Interface RecordRepository.
 */
public interface RecordRepositoryPaginated {

  /**
   * Find all records by table value.
   *
   * @param tableId the table id
   * @param pageable the pageable
   *
   * @return the list
   */
  // @Query(
  // value = "SELECT rv from RecordValue rv INNER JOIN FETCH rv.fields INNER JOIN rv.tableValue tv
  // WHERE tv.id = :tableId",
  // countQuery = "SELECT count(rv) from RecordValue rv WHERE rv.tableValue.id = :tableId")
  List<RecordValue> findAllRecordsByTableValueIdPaginated(Long tableId, Pageable pageable);

  /**
   * Find records pageable.
   *
   * @param pageable the pageable
   * @return the list
   */
  List<RecordValue> findRecordsPageable(Pageable pageable);

}
