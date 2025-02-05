package org.eea.dataflow.persistence.repository;

import org.eea.dataflow.persistence.domain.Dataflow;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DataflowCleanupRepository extends JpaRepository<Dataflow, Long>, DataflowExtendedRepository {

    @Transactional
    @Modifying
    @CacheEvict(value = "dataflowVO", key = "#idDataflow")
    @Query(nativeQuery = true, value = "DELETE FROM dataflow WHERE id = :idDataflow")
    void deleteNativeDataflow(@Param("idDataflow") Long idDataflow);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM dataset WHERE dataflowid = :dataflowId")
    void deleteDatasets(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM snapshot WHERE reporting_dataset_id IN (SELECT id FROM dataset WHERE dataflowid = :dataflowId)")
    void deleteSnapshots(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM snapshot_schema WHERE design_dataset_id IN (SELECT id FROM dataset WHERE dataflowid = :dataflowId)")
    void deleteSnapshotSchemas(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM eu_dataset WHERE id IN (SELECT id FROM dataset WHERE dataflowid = :dataflowId)")
    void deleteEuDatasets(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM data_collection WHERE id IN (SELECT id FROM dataset WHERE dataflowid = :dataflowId)")
    void deleteDataCollections(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM reporting_dataset WHERE id IN (SELECT id FROM dataset WHERE dataflowid = :dataflowId)")
    void deleteReportingDatasets(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM partition_dataset WHERE id IN (SELECT id FROM dataset WHERE dataflowid = :dataflowId)")
    void deletePartitionDatasets(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM test_dataset WHERE id IN (SELECT id FROM dataset WHERE dataflowid = :dataflowId)")
    void deleteTestDatasets(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM design_dataset WHERE id IN (SELECT id FROM dataset WHERE dataflowid = :dataflowId)")
    void deleteDesignDatasets(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM reference_dataset WHERE id IN (SELECT id FROM dataset WHERE dataflowid = :dataflowId)")
    void deleteReferenceDatasets(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM statistics WHERE id_dataset IN (SELECT id FROM dataset WHERE dataflowid = :dataflowId)")
    void deleteStatistics(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM weblink WHERE dataflow_id = :dataflowId")
    void deleteWeblinks(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM document WHERE dataflow_id = :dataflowId")
    void deleteDocuments(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM integration_operation_parameters WHERE integration_id IN (SELECT id FROM integration WHERE dataflow_id = :dataflowId)")
    void deleteIntegrationOperationParams(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM integration WHERE dataflow_id = :dataflowId")
    void deleteIntegrations(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM representative WHERE dataflow_id = :dataflowId")
    void deleteRepresentatives(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM foreign_relations WHERE dataset_id_origin IN (SELECT id FROM dataset WHERE dataflowid = :dataflowId)")
    void deleteForeignRelationsOrigin(@Param("dataflowId") Long dataflowId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM foreign_relations WHERE dataset_id_destination IN (SELECT id FROM dataset WHERE dataflowid = :dataflowId)")
    void deleteForeignRelationsDestination(@Param("dataflowId") Long dataflowId);
}
