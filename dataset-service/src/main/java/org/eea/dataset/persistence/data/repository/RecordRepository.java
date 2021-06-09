package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;


/**
 * The Interface RecordRepository.
 */
public interface RecordRepository
    extends PagingAndSortingRepository<RecordValue, Integer>, RecordExtendedQueriesRepository {

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
  @Query("SELECT rv from RecordValue rv INNER JOIN rv.tableValue tv INNER JOIN FETCH  rv.fields "
      + "WHERE tv.idTableSchema = :idTableSchema")
  List<RecordValue> findByTableValueIdTableSchema(@Param("idTableSchema") String idTableSchema);


  /**
   * Find by id and table value dataset id id.
   *
   * @param id the id
   * @param idDataset the id dataset
   *
   * @return the record value
   */
  RecordValue findByIdAndTableValue_DatasetId_Id(String id, Long idDataset);

  /**
   * Delete record with id.
   *
   * @param recordId the record id
   */
  @Modifying
  @Query("delete from RecordValue record where record.id = ?1")
  void deleteRecordWithId(String recordId);

  /**
   * Delete record with id in.
   *
   * @param recordId the record id
   */
  @Modifying
  @Query("delete from RecordValue record where record.id IN ?1")
  void deleteRecordWithIdIn(List<String> recordId);

  /**
   * Delete record values to restore snapshot.
   *
   * @param partitionDatasetId the partition dataset id
   */
  @Modifying
  @Query("delete from RecordValue r where r.datasetPartitionId= :idPartition")
  void deleteRecordValuesToRestoreSnapshot(@Param("idPartition") Long partitionDatasetId);


  /**
   * Delete record with id table schema.
   *
   * @param idTableSchema the id table schema
   */
  @Modifying
  @Query("delete from RecordValue r where r.tableValue in "
      + "(select t from TableValue t where t.idTableSchema= :idTableSchema)")
  void deleteRecordWithIdTableSchema(@Param("idTableSchema") String idTableSchema);


  /**
   * Delete by data provider code.
   *
   * @param dataProviderCode the data provider code
   */
  @Modifying
  @Query("delete from RecordValue r where r.dataProviderCode = :dataProviderCode")
  void deleteByDataProviderCode(@Param("dataProviderCode") String dataProviderCode);



  /**
   * Find by id.
   *
   * @param id the id
   * @return the record value
   */
  RecordValue findById(String id);

  /**
   * Count by table schema.
   *
   * @param idTableSchema the id table schema
   * @return the long
   */
  @Query("select count(r.id) from RecordValue r where r.tableValue.idTableSchema= :idTableSchema")
  Long countByTableSchema(@Param("idTableSchema") String idTableSchema);

}
