package org.eea.validation.persistence.data.repository;

import java.util.List;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;


/**
 * The Interface RecordRepository.
 */
public interface RecordRepository extends PagingAndSortingRepository<RecordValue, Integer> {

  /**
   * Find by table value id.
   *
   * @param tableId the table id
   * @param pageable the pageable
   *
   * @return the list
   */
  List<RecordValue> findByTableValue_id(Long tableId, Pageable pageable);

  /**
   * Find by table value id mongo.
   *
   * @param idMongo the id mongo
   * @param pageable the pageable
   *
   * @return the list
   */
  List<RecordValue> findByTableValue_IdTableSchema(String idMongo, Pageable pageable);

  /**
   * Find by table value id mongo. No sorting nor paging is performed
   *
   * @param idTableSchema the id table schema
   *
   * @return the list
   */
  @Query("SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv INNER JOIN FETCH  rv.fields WHERE tv.idTableSchema = :idTableSchema")
  List<RecordValue> findByTableValue_IdTableSchema(@Param("idTableSchema") String idTableSchema);



  /**
   * Find all records by table value.
   *
   * @param tableId the table id
   * @param pageable the pageable
   * @return the list
   */
  @Query("SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv INNER JOIN FETCH  rv.fields WHERE tv.id = :id")
  List<RecordValue> findAllRecords_ByTableValueId(@Param("id") Long tableId);

}
