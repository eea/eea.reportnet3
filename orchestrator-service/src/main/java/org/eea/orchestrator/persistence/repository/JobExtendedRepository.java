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
     * @param providerId
     * @param datasetId
     * @param creatorUsername
     * @param jobStatuses
     * @return
     */
    List<Job> findJobsPaginated(Pageable pageable, boolean asc, String sortedColumn, Long jobId, String jobTypes, Long dataflowId, Long providerId,
                                Long datasetId, String creatorUsername, String jobStatuses);

    /**
     * Count jobs paginated.
     * @param asc
     * @param sortedColumn
     * @param jobId
     * @param jobTypes
     * @param dataflowId
     * @param providerId
     * @param datasetId
     * @param creatorUsername
     * @param jobStatuses
     * @return
     */
    Long countJobsPaginated(boolean asc, String sortedColumn, Long jobId, String jobTypes, Long dataflowId, Long providerId, Long datasetId, String creatorUsername, String jobStatuses);
}
