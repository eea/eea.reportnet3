package org.eea.orchestrator.service;

import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;
import java.util.List;

public interface JobService {

    List<JobVO> getJobsByStatus(JobStatusEnum status);

    Boolean canJobBeExecuted(JobVO job);

    void addValidationJob(Long datasetId, Boolean released, String creator);

    void addReleaseJob(Long dataflowId, Long dataProviderId, Boolean restrictFromPublic, Boolean validate, String creator);

    void prepareAndExecuteValidationJob(JobVO jobVO);

    void prepareAndExecuteReleaseJob(JobVO jobVO);

    void updateJobStatusByProcessId(JobStatusEnum status, String processId);

    void deleteFinishedJobsBasedOnDuration(Long duration);

    void updateJobStatus(Long jobId, JobStatusEnum status, String processId);

    void deleteJob(JobVO jobVO);
}
