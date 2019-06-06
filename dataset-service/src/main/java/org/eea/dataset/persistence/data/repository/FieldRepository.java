package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.data.domain.FieldValue;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FieldRepository extends PagingAndSortingRepository<FieldValue, Integer> {

  FieldValue findByIdAndRecord_TableValue_DatasetId_Id(Long id, Long idDataset);
  
}
