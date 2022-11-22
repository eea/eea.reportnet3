package org.eea.orchestrator.persistence.repository;

import org.eea.orchestrator.persistence.domain.Job;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobExtendedRepository {

    /**
     *
     * Retrieves paginated jobs
     * @param pageable the pageable
     * @param asc the asc
     * @param sortedColumn the sortedColumn
     * @param jobId the jobId
     * @param jobTypes the jobTypes
     * @param creatorUsername the creatorUsername
     * @param processId the processId
     * @param jobStatuses the jobStatuses
     * @return the list of job entries
     */
    List<Job> findJobsPaginated(Pageable pageable, boolean asc, String sortedColumn, Long jobId,
                                String jobTypes, String processId, String creatorUsername, String jobStatuses);

    /**
     * Count jobs paginated.
     *
     * @param asc the asc
     * @param sortedColumn the sortedColumn
     * @param jobId the jobId
     * @param jobTypes the jobTypes
     * @param creatorUsername the creatorUsername
     * @param processId the processId
     * @param jobStatuses the jobStatuses
     * @return the long
     */
    Long countJobsPaginated(boolean asc, String sortedColumn, Long jobId,
                            String jobTypes, String processId, String creatorUsername, String jobStatuses);
}
