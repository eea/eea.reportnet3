package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.AttachmentValue;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;


/**
 * The Interface AttachmentRepository.
 */
public interface AttachmentRepository extends PagingAndSortingRepository<AttachmentValue, Integer> {


  /**
   * Find by field value id.
   *
   * @param idField the id field
   * @return the attachment value
   */
  AttachmentValue findByFieldValueId(String idField);


  /**
   * Delete by field value id.
   *
   * @param idField the id field
   */
  void deleteByFieldValueId(String idField);


  /**
   * Delete by field value id field schema.
   *
   * @param idFieldSchema the id field schema
   */
  void deleteByFieldValueIdFieldSchema(String idFieldSchema);


  /**
   * Find all by id field schema and value is not null.
   *
   * @param idFieldSchemas the id field schemas
   * @return the list
   */
  @Query("SELECT attv FROM FieldValue fv, AttachmentValue attv WHERE fv.idFieldSchema = :idFieldSchemas "
      + "AND fv.value is not null AND fv.id = attv.fieldValue")
  List<AttachmentValue> findAllByIdFieldSchemaAndValueIsNotNull(
      @Param("idFieldSchemas") String idFieldSchemas);


}
