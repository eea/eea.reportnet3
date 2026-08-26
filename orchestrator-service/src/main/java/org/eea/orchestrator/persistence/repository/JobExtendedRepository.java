package org.eea.orchestrator.persistence.repository;

import org.eea.orchestrator.persistence.domain.Job;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobExtendedRepository {

    /**
     * Retrieves paginated jobs
     * @param pageable
     * @param asc
     * @param sortedColumn
     * @param jobId
     * @param jobTypes
     * @param dataflowId
     * @param dataflowName
     * @param providerId
     * @param providerName
     * @param datasetId
     * @param datasetName
     * @param creatorUsername
     * @param jobStatuses
     * @return
     */
    List<Job> findJobsPaginated(Pageable pageable, boolean asc, String sortedColumn, Long jobId, String jobTypes, Long dataflowId, String dataflowName, Long providerId,
                                String providerName, Long datasetId, String datasetName, String creatorUsername, String jobStatuses, String preparationCode);

    /**
     * Count jobs paginated.
     * @param asc
     * @param sortedColumn
     * @param jobId
     * @param jobTypes
     * @param dataflowId
     * @param dataflowName
     * @param providerId
     * @param providerName
     * @param datasetId
     * @param datasetName
     * @param creatorUsername
     * @param jobStatuses
     * @return
     */
    Long countJobsPaginated(boolean asc, String sortedColumn, Long jobId, String jobTypes, Long dataflowId, String dataflowName, Long providerId, String providerName, Long datasetId, String datasetName, String creatorUsername, String jobStatuses);

    /**
     * Save and commit changes to db
     * @param job
     * @return the saved job
     */
    Job saveAndFlushJobManually(Job job);
}
