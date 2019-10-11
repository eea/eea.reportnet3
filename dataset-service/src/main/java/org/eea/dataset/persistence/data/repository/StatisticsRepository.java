package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.Statistics;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;


/**
 * The Interface StatisticsRepository.
 */
public interface StatisticsRepository extends PagingAndSortingRepository<Statistics, Long> {

  /**
   * Find all statistics.
   *
   * @return the list
   */
  @Query("SELECT s from Statistics s")
  List<Statistics> findAllStatistics();

}
