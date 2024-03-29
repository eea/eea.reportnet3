package org.eea.orchestrator.service;

import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface JobService {

    List<JobVO> getAllJobs();

    List<JobVO> getJobsByStatus(JobStatusEnum status);

    Boolean canJobBeExecuted(JobVO job);

    JobStatusEnum checkEligibilityOfJob(JobTypeEnum jobType, Map<String, Object> parameters);

    void addValidationJob(Map<String, Object> parameters, String creator, JobStatusEnum statusToInsert);

    void addReleaseJob(Map<String, Object> parameters, String creator, JobStatusEnum statusToInsert);

    void prepareAndExecuteValidationJob(JobVO jobVO);

    void prepareAndExecuteReleaseJob(JobVO jobVO);

    void updateJobStatusByProcessId(JobStatusEnum status, String processId);

    void deleteFinishedJobsBasedOnDuration();

    void updateJobStatus(Long jobId, JobStatusEnum status, String processId);

    void deleteJob(JobVO jobVO);
}
