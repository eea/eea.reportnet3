package org.eea.validation.persistence.data.repository;

import java.util.List;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * The Interface RecordRepository.
 */
@Repository
public interface FieldRepository
    extends PagingAndSortingRepository<FieldValue, Long>, FieldExtendedRepository {


  /**
   * Find by id and id field schema.
   *
   * @param id the id
   * @param idFieldSchema the id field schema
   * @return the string
   */
  @Query(nativeQuery = true,
      value = "select v.value FROM field_value v where v.id_field_schema = :idFieldSchema and v.id_record = :id")
  String findByIdAndIdFieldSchema(@Param("id") Long id,
      @Param("idFieldSchema") String idFieldSchema);

  /**
   * Find by id and id field schema.
   *
   * @param idFieldSchema the id field schema
   * @return the list
   */
  @Query("select fv from FieldValue fv where fv.idFieldSchema = :idFieldSchema")
  List<FieldValue> findByIdFieldSchema(@Param("idFieldSchema") String idFieldSchema);


  /**
   * Find by ids.
   *
   * @param ids the ids
   * @return the list
   */
  @Query("SELECT fv from FieldValue fv WHERE  fv.id in (:ids) ")
  List<FieldValue> findByIds(@Param("ids") List<String> ids);

  /**
   * Count fields dataset.
   *
   * @return the integer
   */
  @Query(nativeQuery = true, value = "SELECT count(*) from field_value")
  Integer countFieldsDataset();

  /**
   * Count empty fields dataset.
   *
   * @return the integer
   */
  @Query(nativeQuery = true, value = "SELECT count(*) from field_value WHERE value=''")
  Integer countEmptyFieldsDataset();

  /**
   * Find empty fields.
   *
   * @param pageable the pageable
   * @return the page
   */
  @Query("SELECT fv from FieldValue fv WHERE value='' ")
  Page<FieldValue> findEmptyFields(Pageable pageable);

}
