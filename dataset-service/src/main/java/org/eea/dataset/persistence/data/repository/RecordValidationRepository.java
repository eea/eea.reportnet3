package org.eea.dataset.persistence.data.repository;

import java.util.List;
import java.util.Set;
import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
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
  List<RecordValidation> findByRecordValueIdIn(@Param("recordIds") List<String> recordIds);


  /**
   * Find record validations by id dataset and id table.
   *
   * @param datasetId the dataset id
   * @param idTable the id table
   *
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
   *
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
   *
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
   *
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
   *
   * @return the hash set
   */
  @Query("SELECT fv.record.id from FieldValue fv WHERE fv.fieldValidations IS NOT EMPTY "
      + " AND fv.record.tableValue.datasetId.id=?1 and fv.record.tableValue.idTableSchema=?2")
  Set<Long> findRecordIdWithValidations(Long datasetId, String idTableSchema);

  /**
   * Count record id from record with error validations.
   *
   * @param idTableSchema the id table schema
   * @return the long
   */
  @Query(nativeQuery = true, value = "select count(rv.id_record) "
      + "from record_validation rv join validation v on rv.id_validation=v.id join record_value rval on rv.id_record=rval.id "
      + "join table_value tv on rval.id_table=tv.id and v.level_error='ERROR' and tv.id_table_schema=:idTableSchema")
  Long countRecordIdFromRecordWithErrorValidations(@Param("idTableSchema") String idTableSchema);


  /**
   * Count record id from record with warning validations.
   *
   * @param idTableSchema the id table schema
   * @return the long
   */
  @Query(nativeQuery = true, value = "select count(ids.id_record) from (" + "(select rv.id_record "
      + "from record_validation rv join validation v on rv.id_validation=v.id join record_value rval on rv.id_record=rval.id "
      + "join table_value tv on rval.id_table=tv.id and v.level_error='WARNING' and tv.id_table_schema=:idTableSchema) "
      + "except " + "(select rv.id_record "
      + "from record_validation rv join validation v on rv.id_validation=v.id join record_value rval on rv.id_record=rval.id "
      + "join table_value tv on rval.id_table=tv.id and v.level_error='ERROR' and tv.id_table_schema=:idTableSchema)) ids")
  Long countRecordIdFromRecordWithWarningValidations(@Param("idTableSchema") String idTableSchema);

  /**
   * Find by validation ids.
   *
   * @param ids the ids
   * @return the list
   */
  @Query("SELECT rv FROM RecordValidation rv  WHERE rv.validation.id in(:ids) ")
  List<RecordValidation> findByValidationIds(@Param("ids") List<Long> ids);


  /**
   * Find record id from record with validations by level error.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @return the map
   */
  @Query("SELECT rv.recordValue.id AS id,rv.validation.levelError AS levelError FROM RecordValidation rv  "
      + " WHERE rv.recordValue.tableValue.datasetId.id=:datasetId "
      + " AND rv.recordValue.tableValue.idTableSchema=:idTableSchema ")
  List<IDError> findRecordIdFromRecordWithValidationsByLevelError(
      @Param("datasetId") Long datasetId, @Param("idTableSchema") String idTableSchema);

  /**
   * Find record id from field with validations by level error.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @return the map
   */
  @Query("SELECT fv.fieldValue.record.id AS id, fv.validation.levelError AS levelError FROM FieldValidation fv  "
      + " WHERE  fv.fieldValue.record.tableValue.datasetId.id=:datasetId "
      + " AND fv.fieldValue.record.tableValue.idTableSchema=:idTableSchema ")
  List<IDError> findRecordIdFromFieldWithValidationsByLevelError(@Param("datasetId") Long datasetId,
      @Param("idTableSchema") String idTableSchema);

  /**
   * The Interface IDError.
   */
  public interface IDError {
    String getId();

    String getLevelError();
  }
}
