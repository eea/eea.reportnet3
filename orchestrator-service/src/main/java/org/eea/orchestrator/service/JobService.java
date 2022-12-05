package org.eea.orchestrator.service;

import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.JobsVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface JobService {
    JobsVO getJobs(Pageable pageable, boolean asc, String sortedColumn, Long jobId, String jobTypes, Long dataflowId, Long providerId,
                   Long datasetId, String creatorUsername, String jobStatuses);

    List<JobVO> getJobsByStatus(JobStatusEnum status);

    Boolean canJobBeExecuted(JobVO job);

    JobStatusEnum checkEligibilityOfJob(String jobType, Long dataflowId, Long dataProviderId, List<Long> datasetId, boolean release);

    void addValidationJob(Long datasetId, Map<String, Object> parameters, String creator, JobStatusEnum statusToInsert);

    void addReleaseJob(Long dataflowId, Long dataProviderId, Map<String, Object> parameters, String creator, JobStatusEnum statusToInsert);

    void prepareAndExecuteValidationJob(JobVO jobVO);

    void prepareAndExecuteReleaseJob(JobVO jobVO);

    void deleteFinishedJobsBasedOnDuration();

    void updateJobStatus(Long jobId, JobStatusEnum status);

    void deleteJob(JobVO jobVO);

    JobVO save(JobVO jobVO);

    JobVO findById(Long jobId);
}
