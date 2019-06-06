package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.data.domain.TableValue;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TableRepository extends PagingAndSortingRepository<TableValue, Integer> {

  TableValue findByIdAndDatasetId_Id(Long id, Long idDataset);
  
}
