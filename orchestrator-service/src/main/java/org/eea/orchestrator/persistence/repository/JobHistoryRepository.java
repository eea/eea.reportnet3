package org.eea.orchestrator.persistence.repository;

import org.eea.orchestrator.persistence.domain.JobHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * The Interface JobHistoryRepository.
 */
public interface JobHistoryRepository extends JpaRepository<JobHistory, Long> {

    /**
     *
     * Retrieves history of a job
     *
     * @param jobId the job id
     * @return the list of the history entries
     */
    List<JobHistory> findAllByJobId(Long jobId);
}
