package org.eea.validation.persistence.data.repository;

import java.util.List;
import java.util.Optional;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;


/**
 * The Interface RecordRepository.
 */
public interface RecordRepository extends PagingAndSortingRepository<RecordValue, Integer>,
    RecordRepositoryPaginated, FieldExtendedRepository {

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
   *
   * @return the list
   */
  @Query("SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv INNER JOIN FETCH rv.fields WHERE tv.id = :id")
  List<RecordValue> findAllRecordsByTableValueId(@Param("id") Long tableId);

  /**
   * Find all records and fields by table value id.
   *
   * @param tableId the table id
   * @return the list
   */
  @Query("SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv WHERE tv.id = :id")
  List<RecordValue> findAllRecordsAndFieldsByTableValueId(@Param("id") Long tableId);

  /**
   * Find all records and fields by table value id.
   *
   * @param recordId the record id
   * @return the list
   */
  @Query("SELECT rv from RecordValue rv WHERE rv.id = :id")
  Optional<RecordValue> findByIdValidation(@Param("id") Long recordId);

  /**
   * Find by id.
   *
   * @param id the id
   * @return the record value
   */
  @Query("SELECT rv from RecordValue rv INNER JOIN FETCH rv.fields WHERE rv.id = :id")
  RecordValue findFieldsByIdRecord(@Param("id") String recordId);

  /**
   * Count records dataset.
   *
   * @return the integer
   */
  @Query(nativeQuery = true, value = "SELECT count(*) from record_value")
  Integer countRecordsDataset();

}
