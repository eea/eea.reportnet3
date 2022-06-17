package org.eea.recordstore.persistence.repository;

import org.eea.recordstore.persistence.domain.EEAProcess;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

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
   * @param processId the process id
   * @return true, if is process finished
   */
  @Query(nativeQuery = true,
      value = "select case when (select count(p.id) from process p join dataset d on p.dataset_id = d.id where p.dataflow_id =:dataflowId and d.data_provider_id = :dataProviderId and p.status not in ('FINISHED','CANCELED'))>1 then false else true end")
  boolean isProcessFinished(@Param("dataflowId") Long dataflowId,
      @Param("dataProviderId") Long dataProviderId);

  /**
   * Find next process.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param datasetId the dataset id
   * @return the EEA process
   */
  @Query(nativeQuery = true,
      value = "select p.* from process p join dataset d on p.dataset_id = d.id where p.dataflow_id =:dataflowId and d.data_provider_id = :dataProviderId and p.status='IN_QUEUE' and d.id <> :datasetId limit 1")
  EEAProcess findNextProcess(@Param("dataflowId") Long dataflowId,
      @Param("dataProviderId") Long dataProviderId, @Param("datasetId") Long datasetId);
}
