package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
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


  List<FieldValue> findByIdFieldSchema(String idFieldSchema);

  List<FieldValue> findByRecord(RecordValue record);

}
