package org.eea.orchestrator.service;

import org.eea.exception.EEAException;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.JobsVO;
import org.eea.interfaces.vo.orchestrator.enums.FmeJobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.orchestrator.persistence.domain.Job;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface JobService {
    JobsVO getJobs(Pageable pageable, boolean asc, String sortedColumn, Long jobId, String jobTypes, Long dataflowId, String dataflowName, Long providerId,
                   Long datasetId, String datasetName, String creatorUsername, String jobStatuses);

    List<JobVO> getJobsByStatus(JobStatusEnum status);

    List<JobVO> getJobsByStatusAndTypeAndMaxDuration(JobTypeEnum jobType, JobStatusEnum status, Long maxDuration, Long maxFMEDuration);


    List<JobVO> getJobsByTypeAndStatus(JobTypeEnum type, JobStatusEnum status);

    Boolean canJobBeExecuted(JobVO job);

    JobStatusEnum checkEligibilityOfJob(String jobType, Long dataflowId, Long dataProviderId, List<Long> datasetId, boolean release);

    Long addJob(Long dataflowId, Long dataProviderId, Long datasetId, Map<String, Object> parameters, JobTypeEnum jobType, JobStatusEnum jobStatus, boolean release, String fmeJobId, String dataflowName, String datasetName);

    void prepareAndExecuteValidationJob(JobVO jobVO);

    void prepareAndExecuteReleaseJob(JobVO jobVO);

    void prepareAndExecuteCopyToEUDatasetJob(JobVO jobVO);

    void prepareAndExecuteFileExportJob(JobVO jobVO) throws Exception;

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

    void updateJobAndProcess(Long jobId, JobStatusEnum jobStatus, ProcessStatusEnum processStatus);

    void cancelJob(Long jobId) throws EEAException;

    List<JobVO> getFMEImportJobsForPolling();

    void updateFmeStatus(Long jobId, FmeJobStatusEnum fmeStatus);

    List<JobVO> findByJobTypeInAndJobStatusInAndRelease(List<JobTypeEnum> jobType, List<JobStatusEnum> jobStatus, boolean release);

    void updateFmeCallbackJobParameter(String fmeJobId, Boolean fmeCallback);

    File downloadEtlExportedFile(Long jobId, String fileName) throws EEAException;

    void updateJobInfo(Long jobId, JobInfoEnum jobInfo, Integer lineNumber);

    Long findProviderIdById(Long jobId);

    Boolean canValidationBeExecutedDueToReferenceIcebergTables(JobVO job);
}
