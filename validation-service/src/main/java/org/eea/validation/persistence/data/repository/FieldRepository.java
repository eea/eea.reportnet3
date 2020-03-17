package org.eea.validation.persistence.data.repository;

import java.util.List;
import org.eea.validation.persistence.data.domain.FieldValue;
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
  @Query("select * from field_value v where v.id_field_schema = :idFieldSchema")
  List<FieldValue> findByIdFieldSchema(@Param("idFieldSchema") String idFieldSchema);

}
