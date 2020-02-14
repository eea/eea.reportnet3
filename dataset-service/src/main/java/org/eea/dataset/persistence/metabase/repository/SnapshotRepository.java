package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import javax.transaction.Transactional;
import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * The Interface SnapshotRepository.
 */
public interface SnapshotRepository extends CrudRepository<Snapshot, Long> {

  /**
   * Find by reporting dataset id.
   *
   * @param idDataset the id dataset
   * @return the list
   */
  List<Snapshot> findByReportingDatasetIdOrderByCreationDateDesc(
      @Param("idReportingDataset") Long idDataset);


  /**
   * Release snaphot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   */
  @Transactional
  @Modifying
  @Query(nativeQuery = true,
      value = "UPDATE snapshot SET release=false WHERE reporting_dataset_id=:idDataset ; "
          + "UPDATE snapshot SET release=true, date_released=CURRENT_TIMESTAMP WHERE id=:idSnapshot")
  void releaseSnaphot(@Param("idDataset") Long idDataset, @Param("idSnapshot") Long idSnapshot);



  /**
   * Find by reporting dataset and release.
   *
   * @param datasetIds the dataset ids
   * @return the list
   */
  @Query(
      value = "select s from Snapshot s where s.release=true AND  s.reportingDataset.id IN :datasetIds")
  List<Snapshot> findByReportingDatasetAndRelease(@Param("datasetIds") List<Long> datasetIds);

}
