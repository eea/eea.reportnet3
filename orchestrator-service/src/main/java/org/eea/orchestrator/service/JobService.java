package org.eea.orchestrator.service;

import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface JobService {

    void testSaveJob();

    JobVO testRetrieveJob(Long id);

    List<JobVO> getJobsByStatus(JobStatusEnum status);

    Boolean canJobBeExecuted(JobVO job);

    void addValidationJob(Long datasetId, Boolean released, String creator);

    void prepareAndExecuteValidationJob(JobVO jobVO);

    void updateStatusByProcessId(String status, String processId);

    void updateJobInProgress(Long jobId, String processId);
}
