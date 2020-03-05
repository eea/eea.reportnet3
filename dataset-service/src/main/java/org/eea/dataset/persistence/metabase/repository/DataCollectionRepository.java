package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import java.util.Optional;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


/**
 * The Interface DataCollectionRepository.
 */
public interface DataCollectionRepository extends CrudRepository<DataCollection, Long> {


  /**
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<DataCollection> findByDataflowId(Long dataflowId);

  /**
   * Find first by dataset schema.
   *
   * @param datasetSchema the dataset schema
   * @return the optional
   */
  Optional<DataCollection> findFirstByDatasetSchema(String datasetSchema);

  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = "delete from dataset where id in :datasetIds")
  void deleteDatasetById(@Param("datasetIds") List<Long> datasetIds);

  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = "update dataflow set status = :status where id = :dataflowId")
  void updateDataflowStatus(@Param("dataflowId") Long dataflowId, @Param("status") String status);
}
