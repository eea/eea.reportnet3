package org.eea.orchestrator.persistence.repository;

import org.eea.orchestrator.persistence.domain.JobHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * The Interface JobHistoryRepository.
 */
public interface JobHistoryRepository extends JobHistoryExtendedRepository, JpaRepository<JobHistory, Long> {

    /**
     *
     * Retrieves history of a job
     *
     * @param jobId the job id
     * @return the list of the history entries
     */
    List<JobHistory> findAllByJobIdOrderById(Long jobId);

    /**
     *
     * Retrieves last history entry of a job
     *
     * @param jobId the job id
     * @return the optional history entry
     */
    Optional<JobHistory> findFirstByJobIdOrderByIdDesc(Long jobId);
}
