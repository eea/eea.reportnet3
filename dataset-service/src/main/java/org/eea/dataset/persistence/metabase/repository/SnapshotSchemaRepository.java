package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import javax.transaction.Transactional;
import org.eea.dataset.persistence.metabase.domain.SnapshotSchema;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


/**
 * The Interface SnapshotSchemaRepository.
 */
public interface SnapshotSchemaRepository extends CrudRepository<SnapshotSchema, Long> {


  /**
   * Find by design dataset id order by creation date desc.
   *
   * @param idDataset the id dataset
   * @return the list
   */
  List<SnapshotSchema> findByDesignDatasetIdOrderByCreationDateDesc(
      @Param("idDesignDataset") Long idDataset);



  /**
   * Delete snapshot schema by id.
   *
   * @param idSnapshotSchema the id snapshot schema
   */
  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = "delete from Snapshot_Schema where id= :idSnapshot")
  void deleteSnapshotSchemaById(@Param("idSnapshot") Long idSnapshotSchema);


}
