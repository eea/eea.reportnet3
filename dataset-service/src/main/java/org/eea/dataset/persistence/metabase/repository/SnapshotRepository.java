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
   * Find by reporting dataset id and enabled true order by creation date desc.
   *
   * @param idDataset the id dataset
   * @return the list
   */
  List<Snapshot> findByReportingDatasetIdAndEnabledTrueOrderByCreationDateDesc(
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
      value = "UPDATE snapshot SET dc_released=false WHERE reporting_dataset_id=:idDataset ; "
          + "UPDATE snapshot SET dc_released=true WHERE id=:idSnapshot")
  void releaseSnaphot(@Param("idDataset") Long idDataset, @Param("idSnapshot") Long idSnapshot);



  /**
   * Find by reporting dataset and release.
   *
   * @param datasetIds the dataset ids
   * @param released the released
   * @return the list
   */
  @Query(
      value = "select s from Snapshot s where s.dcReleased=:released AND s.reportingDataset.id IN :datasetIds")
  List<Snapshot> findByReportingDatasetAndRelease(@Param("datasetIds") List<Long> datasetIds,
      @Param("released") Boolean released);

  /**
   * Find by reporting dataset id.
   *
   * @param reportingId the reporting id
   * @return the list
   */
  @Query(value = "select max(s) from Snapshot s where s.reportingDataset.id= :reportingId")
  Snapshot findFirstByReportingDatasetId(@Param("reportingId") Long reportingId);

  /**
   * Release EU inactive snapshots.
   *
   * @param inactiveSnapshots the inactive snapshots
   */
  @Transactional
  @Modifying
  @Query(nativeQuery = true,
      value = "UPDATE snapshot SET eu_released=false WHERE id in :inactiveSnapshots")
  void releaseEUInactiveSnapshots(@Param("inactiveSnapshots") List<Long> inactiveSnapshots);

  /**
   * Release EU active snapshots.
   *
   * @param activeSnapshots the active snapshots
   */
  @Transactional
  @Modifying
  @Query(nativeQuery = true,
      value = "UPDATE snapshot SET eu_released=true WHERE id in :activeSnapshots ; ")
  void releaseEUActiveSnapshots(@Param("activeSnapshots") List<Long> activeSnapshots);

  /**
   * Find by data collection id order by creation date desc.
   *
   * @param idDataset the id dataset
   * @return the list
   */
  List<Snapshot> findByDataCollectionIdOrderByCreationDateDesc(@Param("idDataset") Long idDataset);

  /**
   * Update snapshot enabled false.
   *
   * @param idDataset the id dataset
   */
  @Transactional
  @Modifying
  @Query(nativeQuery = true,
      value = "UPDATE snapshot SET enabled=false WHERE reporting_dataset_id=:idDataset")
  void updateSnapshotEnabledFalse(@Param("idDataset") Long idDataset);

  /**
   * Delete snapshot by dataset id and date released is null.
   *
   * @param idDataset the id dataset
   */
  @Transactional
  @Modifying
  @Query(nativeQuery = true,
      value = "DELETE FROM snapshot WHERE reporting_dataset_id=:idDataset AND date_released is null ")
  void deleteSnapshotByDatasetIdAndDateReleasedIsNull(@Param("idDataset") Long idDataset);
}
