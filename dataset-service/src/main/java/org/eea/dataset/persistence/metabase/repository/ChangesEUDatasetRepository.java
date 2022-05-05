package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import org.eea.dataset.persistence.metabase.domain.ChangesEUDataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


/**
 * The Interface ChangesEUDatasetRepository.
 */
public interface ChangesEUDatasetRepository extends JpaRepository<ChangesEUDataset, Long> {


  /**
   * Find distinct by datacollection.
   *
   * @param dataCollectionId the data collection id
   * @return the list
   */
  List<ChangesEUDataset> findDistinctByDatacollection(Long dataCollectionId);

  /**
   * Delete by datacollection.
   *
   * @param dataCollectionId the data collection id
   */
  @Transactional
  @Modifying
  @Query("DELETE FROM ChangesEUDataset WHERE datacollection = :dataCollectionId")
  void deleteByDatacollection(@Param("dataCollectionId") Long dataCollectionId);
}
