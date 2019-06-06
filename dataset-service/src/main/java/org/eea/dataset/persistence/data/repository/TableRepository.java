package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.data.domain.TableValue;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * The Interface TableRepository.
 */
public interface TableRepository extends PagingAndSortingRepository<TableValue, Integer> {

  /**
   * Find by id and dataset id id.
   *
   * @param id the id
   * @param idDataset the id dataset
   * @return the table value
   */
  TableValue findByIdAndDatasetId_Id(Long id, Long idDataset);
  
}
