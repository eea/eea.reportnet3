package org.eea.orchestrator.persistence.repository;

import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.orchestrator.persistence.domain.Job;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
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


    Optional<Job> findJobByFmeJobId(String fmeJobId);

    /**
     *
     * Deletes jobs based on statuses and duration
     * @param statuses the statuses
     */
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "DELETE FROM jobs WHERE job_status in :statuses AND date_status_changed < now() - interval '3 days'")
    void deleteJobsBasedOnStatusAndDuration(@Param("statuses") Set<String> statuses);

    /**
     *
     * @param jobStatus
     * @param jobType
     * @return
     */
    Integer countByJobStatusAndJobType(JobStatusEnum jobStatus, JobTypeEnum jobType);

    /**
     *
     * @param jobStatus
     * @param jobType
     * @return
     */
    List<Job> findByJobStatusAndJobType(JobStatusEnum jobStatus, JobTypeEnum jobType);

    /**
     *
     * @param jobStatus
     * @param jobType
     * @param datasetId
     * @return
     */
    List<Job> findByJobStatusInAndJobTypeInAndDatasetId(List<JobStatusEnum> jobStatus, List<JobTypeEnum> jobType, Long datasetId);

    /**
     *
     * Retrieves number of jobs based on status and type
     *
     * @param jobStatus the job status
     * @param jobType the job type
     * @param release the release
     * @return the number of entries
     */
    Integer countByJobStatusAndJobTypeAndRelease(JobStatusEnum jobStatus, JobTypeEnum jobType, boolean release);

    /**
     *
     * Retrieves jobs based on status and type
     *
     * @param jobType the job type
     * @param jobStatus the job status
     * @return the entries
     */
    List<Job> findByJobTypeAndJobStatus(JobTypeEnum jobType, JobStatusEnum jobStatus);

    /**
     *
     * Retrieves jobs based on statuses, type and release
     *
     * @param jobType the job type
     * @param jobStatus the job status
     * @return the entries
     */
    List<Job> findByJobTypeInAndJobStatusIn(List<JobTypeEnum> jobType, List<JobStatusEnum> jobStatus);

    /**
     * Retrieves jobs based on statuses and types
     * @param jobType
     * @param jobStatus
     * @return
     */
    List<Job> findByJobTypeInAndJobStatusInAndRelease(List<JobTypeEnum> jobType, List<JobStatusEnum> jobStatus, boolean release);

    /**
     * Finds jobs by dataflowId, jobType, job status and release
     * @param dataflowId
     * @param jobType
     * @param jobStatus
     * @param release
     * @return
     */
    List<Job> findByDataflowIdAndJobTypeInAndJobStatusAndRelease(Long dataflowId, List<JobTypeEnum> jobType, JobStatusEnum jobStatus, boolean release);

    /**
     * Finds jobs that are in a specific status for more than timeInMinutes
     * @param status
     * @param timeInMinutes
     * @return
     */
    @Query(nativeQuery = true,
            value = "select id from jobs where job_status= :status and (extract(epoch from LOCALTIMESTAMP - date_status_changed) / 60) > :timeInMinutes")
    List<BigInteger> findJobsThatExceedTimeWithSpecificStatus(@Param("status") String status, @Param("timeInMinutes") long timeInMinutes);

    /**
     *
     * Updates fme job status column based on job id
     * @param jobId the id
     * @param fmeJobStatus the fme_status value
     */
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE jobs SET fme_status= :fmeJobStatus where id= :jobId")
    void updateFmeStatus(@Param("jobId") Long jobId, @Param("fmeJobStatus") String fmeJobStatus);

    /**
     * Finds import fme jobs in progress
     * @return
     */
    @Query(nativeQuery = true,
            value = "select * from jobs where job_status= 'IN_PROGRESS' and job_type = 'IMPORT' and fme_job_id is not null")
    List<Job> findFmeJobsToPollForStatus();

    /**
     * Find job status by job id
     * @return
     */
    @Query(nativeQuery = true, value = "select j.job_status from jobs j where j.id= :jobId")
    String findStatusByJobId(Long jobId);


    /**
     *
     * Updates job info column based on job id
     * @param jobId the id
     * @param jobInfo the job_info value
     */
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE jobs SET job_info= :jobInfo where id= :jobId")
    void updateJobInfo(@Param("jobId") Long jobId, @Param("jobInfo") String jobInfo);
}





















