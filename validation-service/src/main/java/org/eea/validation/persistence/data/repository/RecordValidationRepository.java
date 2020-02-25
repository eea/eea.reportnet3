package org.eea.validation.persistence.data.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.validation.persistence.data.domain.RecordValidation;
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
  @Query("SELECT rv FROM RecordValidation rv INNER JOIN FETCH rv.validation INNER JOIN FETCH rv.recordValue record "
      + "WHERE record.id in (:recordIds)")
  List<RecordValidation> findByRecordValueIdIn(@Param("recordIds") List<Long> recordIds);



  /**
   * Find record validations by id dataset and id table.
   *
   * @param datasetId the dataset id
   * @param idTable the id table
   * @return the list
   */
  @Query("SELECT rval FROM RecordValidation rval INNER JOIN FETCH rval.validation INNER JOIN rval.recordValue rv "
      + "INNER JOIN rv.tableValue tab WHERE tab.datasetId.id=?1 and tab.id=?2")
  List<RecordValidation> findRecordValidationsByIdDatasetAndIdTable(Long datasetId, Long idTable);



  /**
   * Find record validations by id dataset and id table schema.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @return the list
   */
  @Query("SELECT rval FROM RecordValidation rval INNER JOIN FETCH rval.validation INNER JOIN rval.recordValue rv "
      + "INNER JOIN rv.tableValue tab WHERE tab.datasetId.id=?1 and tab.idTableSchema=?2")
  List<RecordValidation> findRecordValidationsByIdDatasetAndIdTableSchema(Long datasetId,
      String idTableSchema);


  /**
   * Count record validations by id dataset and id table schema and type error.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param typeError the type error
   * @return the long
   */
  @Query("SELECT COUNT (rv.id) FROM RecordValidation rv  "
      + " WHERE rv.recordValue.tableValue.datasetId.id=?1 AND rv.recordValue.tableValue.idTableSchema=?2 "
      + " AND rv.validation.levelError=?3")
  Long countRecordValidationsByIdDatasetAndIdTableSchemaAndTypeError(Long datasetId,
      String idTableSchema, ErrorTypeEnum typeError);


  /**
   * Find record validations by id dataset.
   *
   * @param datasetId the dataset id
   * @return the list
   */
  @Query("SELECT rval FROM RecordValidation rval INNER JOIN FETCH rval.validation INNER JOIN rval.recordValue rv "
      + "INNER JOIN rv.tableValue tab WHERE tab.datasetId.id=?1")
  List<RecordValidation> findRecordValidationsByIdDataset(Long datasetId);


  /**
   * Find record id with validations.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @return the hash set
   */
  @Query("SELECT fv.record.id from FieldValue fv WHERE fv.fieldValidations IS NOT EMPTY "
      + " AND fv.record.tableValue.datasetId.id=?1 and fv.record.tableValue.idTableSchema=?2")
  Set<Long> findRecordIdWithValidations(Long datasetId, String idTableSchema);


  /**
   * Find record id from record with validations by level error.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param typeError the type error
   * @return the hash set
   */
  @Query("SELECT rv.recordValue.id FROM RecordValidation rv  "
      + " WHERE rv.recordValue.tableValue.datasetId.id=:datasetId "
      + " AND rv.recordValue.tableValue.idTableSchema=:idTableSchema "
      + " AND rv.validation.levelError=:typeError")
  Set<Long> findRecordIdFromRecordWithValidationsByLevelError(@Param("datasetId") Long datasetId,
      @Param("idTableSchema") String idTableSchema, @Param("typeError") ErrorTypeEnum typeError);


  /**
   * Find record id from field with validations by level error.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param typeError the type error
   * @return the hash set
   */
  @Query("SELECT fv.fieldValue.record.id FROM FieldValidation fv  "
      + " WHERE  fv.fieldValue.record.tableValue.datasetId.id=:datasetId "
      + " AND fv.fieldValue.record.tableValue.idTableSchema=:idTableSchema "
      + " AND fv.validation.levelError=:typeError")
  Set<Long> findRecordIdFromFieldWithValidationsByLevelError(@Param("datasetId") Long datasetId,
      @Param("idTableSchema") String idTableSchema, @Param("typeError") ErrorTypeEnum typeError);


  /**
   * Find by validation ids.
   *
   * @param ids the ids
   * @return the list
   */
  @Query("SELECT rv FROM RecordValidation rv  WHERE rv.validation.id in(:ids) ")
  List<RecordValidation> findByValidationIds(@Param("ids") List<Long> ids);

  /**
   * Find failed records.
   *
   * @return the list
   */
  @Query(nativeQuery = true,
      value = "select fv.id_record as id, validations.origin as origin, validations.error as error from field_value fv inner join (select fval.id_field as idfield,val.level_error as error,val.origin_name as origin from field_validation fval inner join validation val on fval.id_validation = val.id) as validations on idfield = fv.id group by fv.id_record,validations.error, validations.origin order by validations.error desc")
  List<EntityErrors> findFailedRecords();

  /**
   * Find failed tables.
   *
   * @return the list
   */
  @Query(nativeQuery = true,
      value = "select rv.id_table as id, validations.origin as origin, validations.error as error from record_value rv inner join (select rval.id_record as idrecord,val.level_error as error,val.origin_name as origin from record_validation rval inner join validation val on rval.id_validation = val.id) as validations on idrecord = rv.id group by rv.id_table,validations.error, validations.origin order by validations.error desc")
  List<EntityErrors> findFailedTables();

  /**
   * Find failed datasets.
   *
   * @return the list
   */
  @Query(nativeQuery = true,
      value = "select tv.dataset_id as id, validations.origin as origin, validations.error as error from table_value tv inner join (select tval.id_table as idtable,val.level_error as error,val.origin_name as origin from table_validation tval inner join validation val on tval.id_validation = val.id) as validations on idtable = tv.id group by tv.dataset_id,validations.error, validations.origin order by validations.error desc")
  List<EntityErrors> findFailedDatasets();

  /**
   * The Interface EntityErrors.
   */
  public interface EntityErrors {

    /**
     * Gets the id.
     *
     * @return the id
     */
    BigInteger getId();

    /**
     * Gets the origin.
     *
     * @return the origin
     */
    String getOrigin();

    /**
     * Gets the error.
     *
     * @return the error
     */
    String getError();

  }
}
