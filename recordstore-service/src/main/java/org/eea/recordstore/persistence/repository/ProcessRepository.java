package org.eea.recordstore.persistence.repository;

import org.eea.recordstore.persistence.domain.EEAProcess;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The Interface ProcessRepository.
 */
public interface ProcessRepository
    extends PagingAndSortingRepository<EEAProcess, Long>, ProcessExtendedRepository {

  /**
   * Find one by process id.
   *
   * @param processId the process id
   * @return the optional
   */
  @Query(nativeQuery = true, value = "select * from process where process_id = :processId limit 1")
  EEAProcess findOneByProcessId(@Param("processId") String processId);

  /**
   * Count processes.
   *
   * @return the long
   */
  @Query(nativeQuery = true,
      value = "select count(*) from process where process_type = 'VALIDATION'")
  Long countProcesses();

  /**
   * Checks if is process finished.
   *
   * @param dataflowId
   * @param dataProviderId
   * @return true, if is process finished
   */
  @Query(nativeQuery = true,
      value = "select case when (select count(p.id) from process p join dataset d on p.dataset_id = d.id where p.dataflow_id =:dataflowId and d.data_provider_id = :dataProviderId and p.process_type='VALIDATION' and p.status not in ('FINISHED','CANCELED'))>1 then false else true end")
  boolean isProcessFinished(@Param("dataflowId") Long dataflowId,
      @Param("dataProviderId") Long dataProviderId);

  /**
   * Find next validation process.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param datasetId the dataset id
   * @return the EEA process
   */
  @Query(nativeQuery = true,
      value = "select p.* from process p join dataset d on p.dataset_id = d.id where p.dataflow_id =:dataflowId and d.data_provider_id = :dataProviderId and p.process_type='VALIDATION' and p.status='IN_QUEUE' and d.id <> :datasetId limit 1")
  EEAProcess findNextValidationProcess(@Param("dataflowId") Long dataflowId,
                                       @Param("dataProviderId") Long dataProviderId, @Param("datasetId") Long datasetId);

  /**
   * Finds processes by dataset id and process type and status
   * @param datasetId
   * @param processType
   * @param status
   * @return
   */
  @Query(nativeQuery = true,
          value = "select p.process_id from process p where p.dataset_id= :datasetId and p.process_type= :processType and p.status in (:status) ")
  List<String> findByDatasetIdAndProcessTypeAndStatus(@Param("datasetId") Long datasetId, @Param("processType") String processType, @Param("status") List<String> status);

  /**
   *
   * @param sagaTransactionId
   * @param aggregateId
   * @param processId
   */
  @Modifying
  @Transactional
  @Query(nativeQuery = true,
          value = "update process set saga_transaction_id=:sagaTransactionId , aggregate_id=:aggregateId where process_id=:processId ")
  void insertSagaTransactionIdAndAggregateId(@Param("sagaTransactionId") String sagaTransactionId, @Param("aggregateId") String aggregateId, @Param("processId") String processId);

  /**
   * Finds processes by dataflow and dataset with specific status
   * @param dataflowId
   * @param datasetId
   * @param status
   * @return
   */
  @Query(nativeQuery = true,
          value = "select p.* from process p where p.dataflow_id =:dataflowId and p.dataset_id = :datasetId and p.status in (:status)")
  List<EEAProcess> findProcessByDataflowAndDataset(Long dataflowId, Long datasetId, List<String> status);
}
