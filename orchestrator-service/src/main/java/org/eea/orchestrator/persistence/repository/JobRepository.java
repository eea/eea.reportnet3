package org.eea.orchestrator.persistence.repository;

import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.orchestrator.persistence.domain.Job;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * The Interface JobRepository.
 */
public interface JobRepository extends PagingAndSortingRepository<Job, Long>, JobExtendedRepository {

    /**
     *
     * Retrieves jobs that have specific status
     *
     * @param jobStatus the job status
     * @return the list of job entries
     */
    List<Job> findAllByJobStatusOrderById(JobStatusEnum jobStatus);

    /**
     *
     * Deletes jobs based on statuses and duration
     * @param statuses the statuses
     */
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "DELETE FROM jobs WHERE job_status in :statuses AND date_status_changed < now() - interval '1 days'")
    void deleteJobsBasedOnStatusAndDuration(@Param("statuses") Set<String> statuses);

    /**
     *
     * Retrieves number of jobs based on status and type
     *
     * @param jobStatus the job status
     * @param jobType the job type
     * @return the number of entries
     */
    Integer countByJobStatusAndJobType(JobStatusEnum jobStatus, JobTypeEnum jobType);

    /**
     *
     * Retrieves jobs based on statuses, type and release
     *
     * @param jobType the job type
     * @param jobStatus the job status
     * @param release the release
     * @return the entries
     */
    List<Job> findByJobTypeAndJobStatusInAndRelease(JobTypeEnum jobType, List<JobStatusEnum> jobStatus, boolean release);

    /**
     * Retrieves jobs based on statuses and types
     * @param jobType
     * @param jobStatus
     * @return
     */
    List<Job> findByJobTypeInAndJobStatusInAndRelease(List<JobTypeEnum> jobType, List<JobStatusEnum> jobStatus, boolean release);
}

