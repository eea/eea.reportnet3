package org.eea.orchestrator.persistence.repository;

import org.eea.orchestrator.persistence.domain.JobHistory;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobHistoryExtendedRepository {

    List<JobHistory> findJobHistoryPaginated(Pageable pageable, boolean asc, String sortedColumn, Long jobId, String jobTypes, Long dataflowId, String dataflowName, Long providerId,
                                       Long datasetId, String datasetName, String creatorUsername, String jobStatuses);

    Long countJobHistoryPaginated(boolean asc, String sortedColumn, Long jobId, String jobTypes, Long dataflowId, String dataflowName, Long providerId, Long datasetId, String datasetName, String creatorUsername, String jobStatuses);

    Long countFilteredJobs(Long jobId, String jobType, Long dataflowId, String dataflowName, Long providerId, Long datasetId, String datasetName, String creatorUsername, String jobStatus);
}
