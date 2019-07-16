package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.data.domain.FieldValue;
import org.springframework.data.repository.PagingAndSortingRepository;


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
   * Find first type by idfieldchema.
   *
   * @param idFieldSchema the id field schema
   * @return the string
   */
  FieldValue findFirstTypeByIdFieldSchema(String idFieldSchema);

}
