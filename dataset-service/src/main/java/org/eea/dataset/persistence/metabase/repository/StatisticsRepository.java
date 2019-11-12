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
   * Find statistics by id dataset.
   *
   * @param idDataset the id dataset
   * @return the list
   */
  @Query("SELECT s from Statistics s WHERE s.dataset.id = :idDataset")
  List<Statistics> findStatisticsByIdDataset(@Param("idDataset") Long idDataset);


  /**
   * Find statistics by id datasets.
   *
   * @param ids the ids
   * @return the list
   */
  @Query("SELECT s from Statistics s WHERE s.dataset.id in(:ids)")
  List<Statistics> findStatisticsByIdDatasets(@Param("ids") List<Long> ids);

  /**
   * Delete stats by id dataset.
   *
   * @param idDataset the id dataset
   */
  @Modifying
  @Transactional
  @Query(nativeQuery = true, value = "delete from Statistics where id_Dataset=:idDataset")
  void deleteStatsByIdDataset(@Param("idDataset") Long idDataset);

  /**
   * Find statistics by id dataset schema.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the list
   */
  @Query("SELECT s from Statistics s WHERE s.dataset.datasetSchema=:idDatasetSchema")
  List<Statistics> findStatisticsByIdDatasetSchema(
      @Param("idDatasetSchema") String idDatasetSchema);

}
