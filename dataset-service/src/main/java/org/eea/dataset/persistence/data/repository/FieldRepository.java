package org.eea.dataset.persistence.data.repository;

import java.util.List;
import javax.transaction.Transactional;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
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
  FieldValue findByIdAndRecord_TableValue_DatasetId_Id(Long id, Long idDataset);

  /**
   * Find by id field schema.
   *
   * @param idFieldSchema the id field schema
   * @return the list
   */
  List<FieldValue> findByIdFieldSchema(String idFieldSchema);

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

}
