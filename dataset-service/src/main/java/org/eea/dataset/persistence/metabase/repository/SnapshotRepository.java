package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import javax.transaction.Transactional;
import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SnapshotRepository extends CrudRepository<Snapshot, Long> {

  List<Snapshot> findByReportingDatasetId(@Param("idReportingDataset") Long idDataset);


  @Transactional
  @Modifying
  @Query(nativeQuery = true,
      value = "DELETE FROM snapshot WHERE id=:idSnapshot AND reporting_dataset_id=:idDataset")
  void removeSnaphot(@Param("idDataset") Long idDataset, @Param("idSnapshot") Long idSnapshot);

}
