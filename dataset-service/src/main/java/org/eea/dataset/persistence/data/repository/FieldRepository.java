package org.eea.dataset.persistence.data.repository;

import java.util.List;
import javax.transaction.Transactional;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.service.model.FieldValueWithLabelProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * The Interface FieldRepository.
 */
public interface FieldRepository extends PagingAndSortingRepository<FieldValue, Integer> {


  /**
   * Find by id and record table value dataset id id.
   *
   * @param id the id
   * @param idDataset the id dataset
   * @return the field value
   */
  FieldValue findByIdAndRecord_TableValue_DatasetId_Id(String id, Long idDataset);

  /**
   * Find by id field schema.
   *
   * @param idFieldSchema the id field schema
   * @return the list
   */
  List<FieldValue> findByIdFieldSchema(String idFieldSchema);

  /**
   * Find by id field schema and value.
   *
   * @param idFieldSchema the id field schema
   * @param value the value
   * @return the list
   */
  FieldValue findFirstByIdFieldSchemaAndValue(String idFieldSchema, String value);

  /**
   * Find by id field schema and value.
   *
   * @param idFieldSchema the id field schema
   * @param value the value
   * @return the list
   */
  List<FieldValue> findByIdFieldSchemaAndValue(String idFieldSchema, String value);

  /**
   * Find by id field schema in.
   *
   * @param fieldSchemaIds the field schema ids
   * @return the list
   */
  List<FieldValue> findByIdFieldSchemaIn(List<String> fieldSchemaIds);

  /**
   * Find by record.
   *
   * @param record the record
   * @return the list
   */
  List<FieldValue> findByRecord(RecordValue record);

  /**
   * Find first type by id field schema.
   *
   * @param nameField the name field
   * @return the field value
   */
  FieldValue findFirstTypeByIdFieldSchema(String nameField);

  /**
   * Save value.
   *
   * @param id the id
   * @param value the value
   */
  @Modifying
  @Query(nativeQuery = true, value = "update field_value set value = :value where id = :id")
  void saveValue(@Param("id") String id, @Param("value") String value);

  /**
   * Delete by field schema id.
   *
   * @param fieldSchemaId the field schema id
   */
  void deleteByIdFieldSchema(@Param("idFieldSchema") String fieldSchemaId);

  /**
   * Delete by id field schema native.
   *
   * @param fieldSchemaId the field schema id
   */
  @Modifying
  @Transactional
  @Query(nativeQuery = true,
      value = "delete from field_value where id_field_schema = :idFieldSchema")
  void deleteByIdFieldSchemaNative(@Param("idFieldSchema") String fieldSchemaId);

  /**
   * Update field value type.
   *
   * @param fieldSchemaId the field schema id
   * @param type the type
   */
  @Modifying
  @Query(nativeQuery = true,
      value = "update field_value set type = :type where id_field_schema = :fieldSchemaId")
  void updateFieldValueType(@Param("fieldSchemaId") String fieldSchemaId,
      @Param("type") String type);



  /**
   * Find by id.
   *
   * @param fieldId the field id
   * @return the field value
   */
  FieldValue findById(String fieldId);


  /**
   * Clear field value.
   *
   * @param fieldSchemaId the field schema id
   */
  @Modifying
  @Query(nativeQuery = true,
      value = "update field_value set value = '' where id_field_schema = :fieldSchemaId")
  void clearFieldValue(@Param("fieldSchemaId") String fieldSchemaId);



  /**
   * Find by id field schema and conditional with tag.
   *
   * @param fieldSchemaId the field schema id
   * @param labelId the label id
   * @param conditionalId the conditional id
   * @param conditionalValue the conditional value
   * @param searchValueText the search value text
   * @param pageable the pageable
   * @return the list
   */
  @Query(
      value = "SELECT DISTINCT fv as fieldValue, tag as label FROM FieldValue fv, FieldValue tag, FieldValue cond WHERE fv.idFieldSchema = :fieldSchemaId "
          + "AND tag.idFieldSchema = :labelId AND fv.record.id = tag.record.id "
          + "AND fv.value <> '' "
          + "AND (cond.idFieldSchema = :conditionalId AND cond.value = :conditionalValue AND cond.record.id = fv.record.id or :conditionalId IS NULL) "
          + "AND (:searchText IS NULL or fv.value like CONCAT('%',:searchText,'%') or tag.value like CONCAT('%',:searchText,'%') ) ")
  List<FieldValueWithLabelProjection> findByIdFieldSchemaAndConditionalWithTag(
      @Param("fieldSchemaId") String fieldSchemaId, @Param("labelId") String labelId,
      @Param("conditionalId") String conditionalId,
      @Param("conditionalValue") String conditionalValue,
      @Param("searchText") String searchValueText, Pageable pageable);

  /**
   * Find all cascade list of single pams.
   *
   * @param idListOfSinglePamsField the id list of single pams field
   * @param fieldValueInRecord the field value in record
   * @return the list
   */
  @Query("SELECT DISTINCT fv  FROM FieldValue fv WHERE fv.idFieldSchema =:idListOfSinglePamsField"
      + " AND fv.value IN (SELECT fv2.value FROM FieldValue fv2 "
      + "WHERE fv2.value =:fieldValueInRecord"
      + " OR fv2.value LIKE CONCAT(:fieldValueInRecord,',%')"
      + " OR fv2.value LIKE CONCAT('%,',:fieldValueInRecord,',%')"
      + " OR fv2.value LIKE CONCAT('%, ',:fieldValueInRecord,',%')"
      + " OR fv2.value LIKE CONCAT('%, ',:fieldValueInRecord)"
      + " OR fv2.value LIKE CONCAT('%,',:fieldValueInRecord))")
  List<FieldValue> findAllCascadeListOfSinglePams(
      @Param("idListOfSinglePamsField") String idListOfSinglePamsField,
      @Param("fieldValueInRecord") String fieldValueInRecord);
}
