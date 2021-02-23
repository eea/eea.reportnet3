package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * The Interface DataSetMetabaseRepository.
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
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<DataSetMetabase> findByDataflowId(Long dataflowId);

  /**
   * Find dataset schema id by id.
   *
   * @param datasetId the dataset id
   * @return the string
   */
  @Query("SELECT d.datasetSchema FROM DataSetMetabase d where d.id = ?1")
  String findDatasetSchemaIdById(long datasetId);

  /**
   * Delete snapshot dataset by id snapshot.
   *
   * @param idSnapshot the id snapshot
   */
  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = "delete from Dataset where id=:idSnapshot")
  void deleteSnapshotDatasetByIdSnapshot(@Param("idSnapshot") Long idSnapshot);


  /**
   * Delete native dataset.
   *
   * @param datasetId the dataset id
   */
  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = "delete from dataset where id = :datasetId ")
  void deleteNativeDataset(@Param("datasetId") Long datasetId);


  /**
   * Find first by dataset schema and data provider id.
   *
   * @param datasetSchema the dataset schema
   * @param dataProviderId the data provider id
   * @return the data set metabase
   */
  Optional<DataSetMetabase> findFirstByDatasetSchemaAndDataProviderId(
      @Param("datasetSchema") String datasetSchema, @Param("dataProviderId") Long dataProviderId);


  /**
   * Count by data set name ignore case and dataflow id.
   *
   * @param datasetName the dataset name
   * @param dataflowId the dataflow id
   * @return the long
   */
  Long countByDataSetNameIgnoreCaseAndDataflowId(String datasetName, Long dataflowId);

  /**
   * Gets the dataset ids by dataflow id and data provider id.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the dataset ids by dataflow id and data provider id
   */
  @Query("SELECT d.id FROM DataSetMetabase d WHERE d.dataflowId = :dataflowId AND d.dataProviderId = :providerId")
  List<Long> getDatasetIdsByDataflowIdAndDataProviderId(@Param("dataflowId") Long dataflowId,
      @Param("providerId") Long dataProviderId);

  /**
   * Find by dataflow id and provider id not null.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Query("SELECT d FROM DataSetMetabase d WHERE d.dataflowId = :dataflowId AND d.dataProviderId is not null")
  List<DataSetMetabase> findByDataflowIdAndProviderIdNotNull(@Param("dataflowId") Long dataflowId);

  /**
   * Find by dataflow id and data provider id.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the list
   */
  @Query
  List<DataSetMetabase> findByDataflowIdAndDataProviderId(@Param("dataflowId") Long dataflowId,
      @Param("dataProviderId") Long dataProviderId);
}
