package org.eea.orchestrator.service;

import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.JobsVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.springframework.data.domain.Pageable;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface JobService {
    JobsVO getJobs(Pageable pageable, boolean asc, String sortedColumn, Long jobId, String jobTypes, Long dataflowId, Long providerId,
                   Long datasetId, String creatorUsername, String jobStatuses);

    List<JobVO> getJobsByStatus(JobStatusEnum status);

    List<JobVO> getJobsByStatusAndTypeAndMaxDuration(JobTypeEnum jobType, JobStatusEnum status, Long maxDuration, Long maxFMEDuration);


    List<JobVO> getJobsByTypeAndStatus(JobTypeEnum type, JobStatusEnum status);

    Boolean canJobBeExecuted(JobVO job);

    JobStatusEnum checkEligibilityOfJob(String jobType, Long dataflowId, Long dataProviderId, List<Long> datasetId, boolean release);

    Long addJob(Long dataflowId, Long dataProviderId, Long datasetId, Map<String, Object> parameters, JobTypeEnum jobType, JobStatusEnum jobStatus, boolean release, String fmeJobId);
    Long addJob(Long dataflowId, Long dataProviderId, Long datasetId, Map<String, Object> parameters, JobTypeEnum jobType, JobStatusEnum jobStatus, boolean release, String dataflowName, String datasetName);

    void prepareAndExecuteValidationJob(JobVO jobVO);

    void prepareAndExecuteReleaseJob(JobVO jobVO);

    void prepareAndExecuteCopyToEUDatasetJob(JobVO jobVO);

    void deleteFinishedJobsBasedOnDuration();

    void updateJobStatus(Long jobId, JobStatusEnum status);

    void deleteJob(JobVO jobVO);

    JobVO save(JobVO jobVO);

    JobVO findById(Long jobId);

    boolean canExecuteReleaseOnDataflow(Long dataflowId);

    JobVO findByFmeJobId(String fmeJobId);

    void updateFmeJobId(Long jobId,String fmeJobId);

    List<BigInteger> listJobsThatExceedTimeWithSpecificStatus(String status, long timeInMinutes);

    void releaseValidationRefusedNotification(Long jobId, String user, Long datasetId);

    void releaseReleaseRefusedNotification(Long jobId, String user, Long dataflowId, Long providerId);

    void releaseCopyToEuDatasetRefusedNotification(Long jobId, String user, Long dataflowId);

    List<JobVO> findByStatusAndJobType(JobStatusEnum status, JobTypeEnum jobType);
}
