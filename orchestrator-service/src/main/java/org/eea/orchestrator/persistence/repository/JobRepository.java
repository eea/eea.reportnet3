package org.eea.orchestrator.persistence.repository;

import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.orchestrator.persistence.domain.Job;
import org.eea.orchestrator.persistence.domain.JobHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * The Interface JobRepository.
 */
public interface JobRepository extends JpaRepository<Job, Long> {

    /**
     *
     * Retrieves jobs that have specific status
     *
     * @param jobStatus the job status
     * @return the list of job entries
     */
    List<Job> findAllByJobStatus(JobStatusEnum jobStatus);

    /**
     *
     * Retrieves job that has specific process id
     *
     * @param processId the process id
     * @return a job
     */
    Job findOneByProcessId(String processId);
}

