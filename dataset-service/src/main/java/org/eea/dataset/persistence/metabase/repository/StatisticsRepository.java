package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import javax.transaction.Transactional;
import org.eea.dataset.persistence.metabase.domain.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


/**
 * The Interface StatisticsRepository.
 */
public interface StatisticsRepository extends JpaRepository<Statistics, Long> {

  /**
   * Find all statistics.
   *
   * @return the list
   */
  /*
   * @Query("SELECT s from Statistics s") List<Statistics> findAllStatistics();
   */

  @Query("SELECT s from Statistics s WHERE s.idDataset = :idDataset")
  List<Statistics> findStatisticsByIdDataset(@Param("idDataset") Long idDataset);


  @Modifying
  @Transactional
  @Query(nativeQuery = true, value = "delete from Statistics where id_Dataset=:idDataset")
  void deleteStatsByIdDataset(@Param("idDataset") Long idDataset);

}
