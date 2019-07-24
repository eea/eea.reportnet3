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
   * Removes the snaphot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   */
  @Transactional
  @Modifying
  @Query(nativeQuery = true,
      value = "DELETE FROM snapshot WHERE id=:idSnapshot AND reporting_dataset_id=:idDataset ; "
          + "DELETE FROM dataset where id=:idSnapshot")
  void removeSnaphot(@Param("idDataset") Long idDataset, @Param("idSnapshot") Long idSnapshot);

}
