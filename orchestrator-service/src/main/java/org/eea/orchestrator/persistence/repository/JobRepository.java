package org.eea.orchestrator.persistence.repository;

import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.orchestrator.persistence.domain.Job;
import org.eea.orchestrator.persistence.domain.JobHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
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

    /**
     *
     * Deletes jobs based on statuses and duration
     * @param statuses the statuses
     * @param duration the duration
     */
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "DELETE FROM jobs WHERE jobStatus in :statuses AND dateStatusChanged < DATEADD(MINUTE, :duration, GETDATE())")
    void deleteJobsBasedOnStatusAndDuration(@Param("statuses") List<JobStatusEnum> statuses, @Param("duration") Long duration);

    /**
     *
     * Retrieves number of jobs based on status and type
     *
     * @param jobStatus the job status
     * @param jobType the job type
     * @return the number of entries
     */
    Integer countByJobStatusAndJobType(JobStatusEnum jobStatus, JobTypeEnum jobType);
}

