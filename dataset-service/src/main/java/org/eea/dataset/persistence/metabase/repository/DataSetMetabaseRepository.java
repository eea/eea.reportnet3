package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import javax.transaction.Transactional;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * The Interface DataSetMetabaseRepository.
 *
 * @author Mario Severa
 */
public interface DataSetMetabaseRepository extends CrudRepository<DataSetMetabase, Long> {

  /**
   * Find dataflow id by id.
   *
   * @param datasetId the dataset id
   * @return the long
   */
  @Query("SELECT d.dataflowId FROM DataSetMetabase d where d.id = ?1")
  Long findDataflowIdById(Long datasetId);

  /**
   * Find dataset schema id by id.
   *
   * @param datasetId the dataset id
   * @return the long
   */
  @Query("SELECT d.datasetSchema FROM DataSetMetabase d where d.id = ?1")
  String findDatasetSchemaIdById(Long datasetId);

  /**
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<DataSetMetabase> findByDataflowId(Long dataflowId);

  /**
   * Delete snapshot dataset by id snapshot.
   *
   * @param idSnapshot the id snapshot
   */
  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = "delete from Dataset where id=:idSnapshot")
  void deleteSnapshotDatasetByIdSnapshot(@Param("idSnapshot") Long idSnapshot);

}
