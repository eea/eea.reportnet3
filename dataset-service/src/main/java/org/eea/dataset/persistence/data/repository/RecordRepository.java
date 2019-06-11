package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.eea.dataset.persistence.data.domain.RecordValue;
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
   * Find record validations by id dataset and id table.
   *
   * @param datasetId the dataset id
   * @param idTable the id table
   * @return the list
   */
  @Query("SELECT rval FROM DatasetValue dat INNER JOIN dat.tableValues tv INNER JOIN tv.records rv "
      + "INNER JOIN rv.recordValidations rval WHERE dat.id=?1 and tv.id=?2")
  List<RecordValidation> findRecordValidationsByIdDatasetAndIdTable(Long datasetId, Long idTable);
  

}
