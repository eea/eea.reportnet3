package org.eea.orchestrator.service.impl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.JobsVO;
import org.eea.interfaces.vo.orchestrator.enums.FmeJobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.validation.TaskVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.orchestrator.mapper.JobMapper;
import org.eea.orchestrator.persistence.domain.Job;
import org.eea.orchestrator.persistence.repository.JobRepository;
import org.eea.orchestrator.service.JobHistoryService;
import org.eea.orchestrator.service.JobProcessService;
import org.eea.orchestrator.service.JobService;
import org.eea.orchestrator.utils.JobUtils;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

@Service
public class JobServiceImpl implements JobService {

    private static final Logger LOG = LoggerFactory.getLogger(JobServiceImpl.class);

    @Value(value = "${scheduling.inProgress.import.maximum.jobs}")
    private Long maximumNumberOfInProgressImportJobs;

    @Value(value = "${scheduling.inProgress.validation.maximum.jobs}")
    private Long maximumNumberOfInProgressValidationJobs;

    @Value(value = "${scheduling.inProgress.release.maximum.jobs}")
    private Long maximumNumberOfInProgressReleaseJobs;

    @Value(value = "${scheduling.inProgress.copyToEUDataset.maximum.jobs}")
    private Long maximumNumberOfInProgressCopyToEuDatasetJobs;

    @Value(value = "${scheduling.inProgress.export.maximum.jobs}")
    private Long maximumNumberOfInProgressExportJobs;

    @Value("${importPath}")
    private String importPath;

    /**
     * The admin user.
     */
    @Value("${eea.keycloak.admin.user}")
    private String adminUser;

    /**
     * The admin pass.
     */
    @Value("${eea.keycloak.admin.password}")
    private String adminPass;

    private static final String ETL_EXPORT = "/etlExport/";

    @Autowired
    private DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul;

    @Autowired
    private ValidationControllerZuul validationControllerZuul;

    @Autowired
    private EUDatasetControllerZuul euDatasetControllerZuul;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private JobHistoryService jobHistoryService;

    @Autowired
    private KafkaSenderUtils kafkaSenderUtils;

    @Autowired
    private JobProcessService jobProcessService;

    @Autowired
    private ProcessControllerZuul processControllerZuul;

    @Autowired
    private DataSetControllerZuul dataSetControllerZuul;

    @Autowired
    private UserManagementControllerZull userManagementControllerZull;

    /**
     * The job utils.
     */
    @Autowired
    private JobUtils jobUtils;

    private static final String BEARER = "Bearer ";
    private static final String CANCELED_BY_ADMIN_ERROR = "cancelled by admin";

    @Override
    public JobsVO getJobs(Pageable pageable, boolean asc, String sortedColumn, Long jobId, String jobTypes, Long dataflowId, String dataflowName, Long providerId,
                          Long datasetId, String datasetName, String creatorUsername, String jobStatuses) {
        String sortedTableColumn = jobUtils.getJobColumnNameByObjectName(sortedColumn);
        String remainingJobsStatusFilter = "IN_PROGRESS,QUEUED";
        List<Job> jobs = jobRepository.findJobsPaginated(pageable, asc, sortedTableColumn, jobId, jobTypes, dataflowId, dataflowName, providerId, datasetId, datasetName, creatorUsername, jobStatuses);
        List<JobVO> jobVOList = jobMapper.entityListToClass(jobs);
        JobsVO jobsVO = new JobsVO();
        jobsVO.setTotalRecords(jobRepository.count());
        jobsVO.setFilteredRecords(jobRepository.countJobsPaginated(asc, sortedTableColumn, jobId, jobTypes, dataflowId, dataflowName, providerId, datasetId, datasetName, creatorUsername, jobStatuses));
        jobsVO.setRemainingJobs(jobRepository.countJobsPaginated(asc, sortedTableColumn, jobId, jobTypes, dataflowId, dataflowName, providerId, datasetId, datasetName, creatorUsername, remainingJobsStatusFilter));
        jobsVO.setJobsList(jobVOList);

        return jobsVO;
    }

    @Override
    public List<JobVO> getJobsByStatus(JobStatusEnum status) {
        List<Job> jobs = jobRepository.findAllByJobStatusOrderById(status);
        return jobMapper.entityListToClass(jobs);
    }

    @Override
    public List<JobVO> getJobsByStatusAndTypeAndMaxDuration(JobTypeEnum jobType, JobStatusEnum status, Long maxDuration, Long maxFMEDuration) {
        List<Job> jobs = jobRepository.findByJobStatusAndJobType(status, jobType);
        List<Job> longRunningJobs = new ArrayList<>();
        for (Job job : jobs) {
            Long durationOfJob = new Date().getTime() - job.getDateStatusChanged().getTime();
            if (StringUtils.isNotBlank(job.getFmeJobId()) && durationOfJob > maxFMEDuration) {
                longRunningJobs.add(job);
            } else if (StringUtils.isBlank(job.getFmeJobId()) && durationOfJob > maxDuration) {
                longRunningJobs.add(job);
            }
        }
        return jobMapper.entityListToClass(longRunningJobs);
    }

    @Override
    public List<JobVO> getJobsByTypeAndStatus(JobTypeEnum type, JobStatusEnum status) {
        List<Job> jobs = jobRepository.findByJobTypeAndJobStatus(type, status);
        return jobMapper.entityListToClass(jobs);
    }

    @Transactional
    @Override
    public Long addJob(Long dataflowId, Long dataProviderId, Long datasetId, Map<String, Object> parameters, JobTypeEnum jobType, JobStatusEnum jobStatus, boolean release, String fmeJobId, String dataflowName, String datasetName) {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Job job = new Job(null, jobType, jobStatus, ts, ts, parameters, SecurityContextHolder.getContext().getAuthentication().getName(), release, dataflowId, dataProviderId, datasetId, fmeJobId, dataflowName, datasetName, null, null);
        job = jobRepository.save(job);
        jobHistoryService.saveJobHistory(job);
        return job.getId();
    }

    @Override
    public Boolean canJobBeExecuted(JobVO job) {
        JobTypeEnum jobType = job.getJobType();
        Integer numberOfCurrentJobs = 0;
        if (jobType == JobTypeEnum.VALIDATION && job.isRelease() || jobType == JobTypeEnum.RELEASE) {
            numberOfCurrentJobs = jobRepository.countByJobStatusAndJobTypeAndRelease(JobStatusEnum.IN_PROGRESS, JobTypeEnum.VALIDATION, true);
            Integer numberOfCurrentValidationJobsWithReleaseFalse = jobRepository.countByJobStatusAndJobTypeAndRelease(JobStatusEnum.IN_PROGRESS, JobTypeEnum.VALIDATION, false);
            Integer numberOfCurrentReleaseJobs = jobRepository.countByJobStatusAndJobTypeAndRelease(JobStatusEnum.IN_PROGRESS, JobTypeEnum.RELEASE, true);
            if (jobType == JobTypeEnum.VALIDATION && numberOfCurrentJobs + numberOfCurrentValidationJobsWithReleaseFalse < maximumNumberOfInProgressValidationJobs && numberOfCurrentReleaseJobs + numberOfCurrentJobs < maximumNumberOfInProgressReleaseJobs) {
                return true;
            } else if (jobType == JobTypeEnum.RELEASE && numberOfCurrentReleaseJobs + numberOfCurrentJobs < maximumNumberOfInProgressReleaseJobs) {
                return true;
            }
        } else {
            numberOfCurrentJobs = jobRepository.countByJobStatusAndJobType(JobStatusEnum.IN_PROGRESS, jobType);
        }
        if (job.getJobType() == JobTypeEnum.IMPORT && numberOfCurrentJobs < maximumNumberOfInProgressImportJobs) {
            return true;
        } else if (jobType == JobTypeEnum.VALIDATION && !job.isRelease() && numberOfCurrentJobs < maximumNumberOfInProgressValidationJobs) {
            return true;
        } else if (jobType == JobTypeEnum.COPY_TO_EU_DATASET && numberOfCurrentJobs < maximumNumberOfInProgressCopyToEuDatasetJobs) {
            return true;
        } else if (jobType == JobTypeEnum.EXPORT && numberOfCurrentJobs < maximumNumberOfInProgressExportJobs) {
            return true;
        } else if (jobType == JobTypeEnum.FILE_EXPORT && numberOfCurrentJobs < maximumNumberOfInProgressExportJobs) {
            //if there is a file_export for the same dataset id and is IN_PROGRESS the job should remain in status QUEUED
            List<Job> fileExports = jobRepository.findByJobStatusInAndJobTypeInAndDatasetId(Arrays.asList(JobStatusEnum.IN_PROGRESS), Arrays.asList(JobTypeEnum.FILE_EXPORT), job.getDatasetId());
            if(fileExports != null && fileExports.size() > 0){
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public JobStatusEnum checkEligibilityOfJob(String jobType, Long dataflowId, Long dataProviderId, List<Long> datasetIds, boolean release) {
        if (jobType.equals(JobTypeEnum.VALIDATION.toString()) || jobType.equals(JobTypeEnum.RELEASE.toString())) {
            List<Job> jobsList = jobRepository.findByJobTypeInAndJobStatusIn(Arrays.asList(JobTypeEnum.VALIDATION, JobTypeEnum.RELEASE, JobTypeEnum.IMPORT), Arrays.asList(JobStatusEnum.QUEUED, JobStatusEnum.IN_PROGRESS));
            for (Job job : jobsList) {
                Map<String, Object> insertedParameters = job.getParameters();
                if (job.getDatasetId()!=null) {
                    if (datasetIds.contains(job.getDatasetId())) {
                        return JobStatusEnum.REFUSED;
                    }
                } else {
                    List<Integer> insertedDatasetIds = (List<Integer>) insertedParameters.get("datasetId");
                    if (insertedDatasetIds != null && insertedDatasetIds.size() > 0) {
                        for (Integer insertedDatasetId : insertedDatasetIds) {
                            if (datasetIds.contains(insertedDatasetId.longValue())) {
                                return JobStatusEnum.REFUSED;
                            }
                        }
                    }
                }
            }
        } else if (jobType.equals(JobTypeEnum.COPY_TO_EU_DATASET.toString())) {
            //we shouldn't add the job if there is another queued or in progress copy to eu dataset for the same dataflow id
            List<Job> jobList = jobRepository.findByJobTypeInAndJobStatusIn(Arrays.asList(JobTypeEnum.COPY_TO_EU_DATASET), Arrays.asList(JobStatusEnum.QUEUED, JobStatusEnum.IN_PROGRESS));
            for (Job job : jobList) {
                Map<String, Object> insertedParameters = job.getParameters();
                Long insertedDataflowId = Long.valueOf((Integer) insertedParameters.get("dataflowId"));
                if (dataflowId.equals(insertedDataflowId)) {
                    return JobStatusEnum.REFUSED;
                }
            }
        } else if (jobType.equals(JobTypeEnum.IMPORT.toString())) {
            //we shouldn't add the job if there is another queued or in progress import, validation or release for the same datasetId
            List<Job> jobList = jobRepository.findByJobStatusInAndJobTypeInAndDatasetId(Arrays.asList(JobStatusEnum.QUEUED, JobStatusEnum.IN_PROGRESS), Arrays.asList(JobTypeEnum.IMPORT, JobTypeEnum.RELEASE, JobTypeEnum.VALIDATION), datasetIds.get(0));
            if (jobList != null && jobList.size() > 0) {
                return JobStatusEnum.REFUSED;
            } else {
                List<Job> releasesAndValidations = jobRepository.findByJobTypeInAndJobStatusInAndRelease(Arrays.asList(JobTypeEnum.RELEASE, JobTypeEnum.VALIDATION), Arrays.asList(JobStatusEnum.QUEUED, JobStatusEnum.IN_PROGRESS), true);
                for (Job job : releasesAndValidations) {
                    Map<String, Object> insertedParameters = job.getParameters();
                    if (insertedParameters.get("datasetId") != null) {
                        List<Long> insertedDatasetIds = (List<Long>) insertedParameters.get("datasetId");
                        if (insertedDatasetIds.contains(datasetIds.get(0).intValue())) {
                            return JobStatusEnum.REFUSED;
                        }
                    }
                }
                return JobStatusEnum.IN_PROGRESS;
            }
        }
        else if (jobType.equals(JobTypeEnum.FILE_EXPORT.toString())){
            //we shouldn't add the job if there is another queued or in progress import or release for the same datasetId
            List<Job> jobList = jobRepository.findByJobStatusInAndJobTypeInAndDatasetId(Arrays.asList(JobStatusEnum.QUEUED, JobStatusEnum.IN_PROGRESS), Arrays.asList(JobTypeEnum.IMPORT), datasetIds.get(0));
            if (jobList != null && jobList.size() > 0) {
                return JobStatusEnum.REFUSED;
            } else {
                List<Job> releasesAndValidations = jobRepository.findByJobTypeInAndJobStatusInAndRelease(Arrays.asList(JobTypeEnum.RELEASE, JobTypeEnum.VALIDATION), Arrays.asList(JobStatusEnum.QUEUED, JobStatusEnum.IN_PROGRESS), true);
                for (Job job : releasesAndValidations) {
                    Map<String, Object> insertedParameters = job.getParameters();
                    if (insertedParameters.get("datasetId") != null) {
                        List<Long> insertedDatasetIds = (List<Long>) insertedParameters.get("datasetId");
                        if (insertedDatasetIds.contains(datasetIds.get(0).intValue())) {
                            return JobStatusEnum.REFUSED;
                        }
                    }
                }
                return JobStatusEnum.QUEUED;
            }
        }
        return JobStatusEnum.QUEUED;
    }

    @Override
    public void prepareAndExecuteValidationJob(JobVO jobVO) {
        Job job = jobMapper.classToEntity(jobVO);
        Map<String, Object> parameters = job.getParameters();
        Long datasetId = Long.valueOf((Integer) parameters.get("datasetId"));
        Boolean released = (Boolean) parameters.get("released");
        validationControllerZuul.validateDataSetData(datasetId, released, job.getId());
    }

    @Override
    public void prepareAndExecuteReleaseJob(JobVO jobVO) {
        Job job = jobMapper.classToEntity(jobVO);
        Map<String, Object> parameters = job.getParameters();
        Long dataflowId = Long.valueOf((Integer) parameters.get("dataflowId"));
        Long dataProviderId = Long.valueOf((Integer) parameters.get("dataProviderId"));
        Boolean restrictFromPublic = (Boolean) parameters.get("restrictFromPublic");
        Boolean validate = (Boolean) parameters.get("validate");
        dataSetSnapshotControllerZuul.createReleaseSnapshots(dataflowId, dataProviderId, restrictFromPublic, validate, jobVO.getId());
    }

    @Override
    public void prepareAndExecuteCopyToEUDatasetJob(JobVO jobVO) {
        Job job = jobMapper.classToEntity(jobVO);
        Map<String, Object> parameters = job.getParameters();
        Long dataflowId = Long.valueOf((Integer) parameters.get("dataflowId"));
        euDatasetControllerZuul.populateDataFromDataCollection(dataflowId, job.getId());
    }

    @Override
    public void prepareAndExecuteFileExportJob(JobVO jobVO) throws Exception {
        Job job = jobMapper.classToEntity(jobVO);
        Map<String, Object> parameters = job.getParameters();
        Long dataflowId = Long.valueOf((Integer) parameters.get("dataflowId"));
        Long datasetId = Long.valueOf((Integer) parameters.get("datasetId"));
        Long dataProviderId = (parameters.get("dataProviderId") != null) ? Long.valueOf((Integer) parameters.get("dataProviderId")) : null;
        String tableSchemaId = (parameters.get("tableSchemaId") != null) ? (String) parameters.get("tableSchemaId") : null;
        Integer limit = (parameters.get("limit") != null) ? (Integer) parameters.get("limit") : null;
        Integer offset = (parameters.get("offset") != null) ? (Integer) parameters.get("offset") : null;
        String filterValue = (parameters.get("filterValue") != null) ? (String) parameters.get("filterValue") : null;
        String columnName = (parameters.get("columnName") != null) ? (String) parameters.get("columnName") : null;
        String dataProviderCodes = (parameters.get("dataProviderCodes") != null) ? (String) parameters.get("dataProviderCodes") : null;

        dataSetControllerZuul.createFileForEtlExport(datasetId, dataflowId, dataProviderId, tableSchemaId, limit, offset, filterValue, columnName, dataProviderCodes, jobVO.getId());
    }

    @Transactional
    @Override
    public void updateJobStatus(Long jobId, JobStatusEnum status) {
        Optional<Job> job = jobRepository.findById(jobId);
        if (job.isPresent()) {
            job.get().setJobStatus(status);
            job.get().setDateStatusChanged(new Timestamp(System.currentTimeMillis()));
            jobRepository.save(job.get());
            jobHistoryService.saveJobHistory(job.get());
        } else {
            LOG.info("Could not update status for jobId {} because the id does not exist", jobId);
        }
    }

    @Transactional
    @Override
    public void deleteFinishedJobsBasedOnDuration() {
        jobRepository.deleteJobsBasedOnStatusAndDuration(new HashSet<>(Arrays.asList(JobStatusEnum.FINISHED.getValue(), JobStatusEnum.REFUSED.getValue(), JobStatusEnum.FAILED.getValue(), JobStatusEnum.CANCELED.getValue(), JobStatusEnum.CANCELED_BY_ADMIN.getValue())));
    }

    @Transactional
    @Override
    public void deleteJob(JobVO jobVO) {
        jobRepository.deleteById(jobVO.getId());
        LOG.info("Removed job with id {} and type {}", jobVO.getId(), jobVO.getJobType().getValue());
    }

    @Override
    public JobVO save(JobVO jobVO) {
        Job job = jobMapper.classToEntity(jobVO);
        return jobMapper.entityToClass(jobRepository.save(job));
    }

    @Override
    public JobVO findById(Long jobId) {
        Optional<Job> job = jobRepository.findById(jobId);
        if (job.isPresent()) {
            return jobMapper.entityToClass(job.get());
        }
        return null;
    }

    @Override
    public boolean canExecuteReleaseOnDataflow(Long dataflowId) {
        List<Job> jobs = jobRepository.findByDataflowIdAndJobTypeInAndJobStatusAndRelease(dataflowId, Arrays.asList(JobTypeEnum.VALIDATION, JobTypeEnum.RELEASE), JobStatusEnum.IN_PROGRESS, true);
        if (jobs.size() > 0) {
            return false;
        }
        return true;
    }

    @Override
    public JobVO findByFmeJobId(String fmeJobId) {
        Optional<Job> job = jobRepository.findJobByFmeJobId(fmeJobId);
        if (job.isPresent()) {
            return jobMapper.entityToClass(job.get());
        }
        return null;
    }

    @Override
    public void updateFmeJobId(Long jobId, String fmeJobId) {
        Optional<Job> job = jobRepository.findById(jobId);
        if (job.isPresent()) {
            job.get().setFmeJobId(fmeJobId);
            jobRepository.save(job.get());
            jobHistoryService.saveJobHistory(job.get());
        } else {
            LOG.info("Could not update fmeJobId for jobId {} because the id does not exist", jobId);
        }
    }

    @Override
    public List<BigInteger> listJobsThatExceedTimeWithSpecificStatus(String status, long timeInMinutes) {
        return jobRepository.findJobsThatExceedTimeWithSpecificStatus(status, timeInMinutes);
    }

    @Override
    public List<JobVO> findByStatusAndJobType(JobStatusEnum status, JobTypeEnum jobType) {
        return jobMapper.entityListToClass(jobRepository.findByJobStatusAndJobType(status, jobType));
    }

    @Override
    public void releaseValidationRefusedNotification(Long jobId, String user, Long datasetId){
        Map<String, Object> value = new HashMap<>();
        value.put("user", user);
        value.put("validation_job_id", jobId);
        value.put(LiteralConstants.DATASET_ID, datasetId);
        try {
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.VALIDATION_REFUSED_EVENT, value,
                    NotificationVO.builder().user(user).datasetId(datasetId).error("There is another job with status QUEUED or IN_PROGRESS for the datasetId " + datasetId).build());
        } catch (EEAException e) {
            LOG.error("Could not release VALIDATION_REFUSED_EVENT for jobId {} , datasetId {} and user {}. Error Message: ", jobId, datasetId, user, e);
        }
    }

    @Override
    public void releaseReleaseRefusedNotification(Long jobId, String user, Long dataflowId, Long providerId){
        Map<String, Object> value = new HashMap<>();
        value.put(LiteralConstants.USER, user);
        value.put("release_job_id", jobId);
        try {
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_REFUSED_EVENT, value,
                    NotificationVO.builder().user(user).dataflowId(dataflowId).providerId(providerId)
                            .error("There is another job with status QUEUED or IN_PROGRESS for dataflowId " + dataflowId + " and providerId " + providerId).build());
        } catch (EEAException e) {
            LOG.error("Could not release RELEASE_REFUSED_EVENT for jobId {} , dataflowId {} , providerId {} and user {}. Error Message: ", jobId, dataflowId, providerId, user, e);
        }
    }

    @Override
    public void releaseCopyToEuDatasetRefusedNotification(Long jobId, String user, Long dataflowId) {
        Map<String, Object> value = new HashMap<>();
        value.put(LiteralConstants.USER, user);
        value.put("copyToEuDataset_job_id", jobId);
        try {
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.COPY_DATA_TO_EUDATASET_REFUSED_EVENT, value,
                    NotificationVO.builder().user(user).dataflowId(dataflowId)
                            .error("There is another job with status QUEUED or IN_PROGRESS for dataflowId " + dataflowId).build());
        } catch (EEAException e) {
            LOG.error("Could not release RELEASE_REFUSED_EVENT for jobId {} , dataflowId {} and user {}. Error Message: ", jobId, dataflowId, user, e);
        }
    }

    @Override
    public void updateJobAndProcess(Long jobId, JobStatusEnum jobStatus, ProcessStatusEnum processStatus){
        updateJobStatus(jobId, jobStatus);
        List<String> processIds = jobProcessService.findProcessesByJobId(jobId);
        for (String processId : processIds) {
            ProcessVO processVO = processControllerZuul.findById(processId);
            processControllerZuul.updateProcess(processVO.getDatasetId(), processVO.getDataflowId(), processStatus, ProcessTypeEnum.fromValue(processVO.getProcessType()),
                    processId, processVO.getUser(), processVO.getPriority(), processVO.isReleased());
        }
    }
    @Override
    public void cancelJob(Long jobId) throws EEAException {
        LOG.info("User cancelling job {}", jobId);
        JobVO jobVO = findById(jobId);
        List<String> processIds = jobProcessService.findProcessesByJobId(jobId);
        for (String processId : processIds) {
            List<TaskVO> tasks = dataSetControllerZuul.findTasksByProcessIdAndStatusIn(processId, Arrays.asList(ProcessStatusEnum.IN_PROGRESS, ProcessStatusEnum.IN_QUEUE));
            if (tasks.size()>0) {
                LOG.info("User cancelling tasks for processId {} and job {}", processId, jobId);
                validationControllerZuul.updateTaskStatusByProcessIdAndCurrentStatuses(processId, ProcessStatusEnum.CANCELED, new HashSet<>(Arrays.asList(ProcessStatusEnum.IN_QUEUE.toString(), ProcessStatusEnum.IN_PROGRESS.toString())));
                LOG.info("User cancelled tasks for processId {} and job {}", processId, jobId);
            }
            ProcessVO processVO = processControllerZuul.findById(processId);
            LOG.info("User cancelling process {} for job {}", processId, jobId);
            //update process status
            processControllerZuul.updateProcess(processVO.getDatasetId(), processVO.getDataflowId(),
                    ProcessStatusEnum.CANCELED, ProcessTypeEnum.valueOf(processVO.getProcessType()), processVO.getProcessId(),
                    processVO.getUser(), processVO.getPriority(), processVO.isReleased());
            LOG.info("User cancelled process {} for job {}", processId, jobId);
            if (jobVO.isRelease() && jobVO.getJobType().equals(JobTypeEnum.RELEASE)) {
                LOG.info("Removing historic releases for job {} and datasetId {}", jobId, processVO.getDatasetId());
                dataSetSnapshotControllerZuul.removeHistoricRelease(processVO.getDatasetId());
                LOG.info("Removed historic releases for job {} and datasetId {}", jobId, processVO.getDatasetId());
            } else if (jobVO.isRelease() && jobVO.getJobType().equals(JobTypeEnum.VALIDATION)) {
                validationControllerZuul.deleteLocksToReleaseProcess(processVO.getDatasetId());
            }
        }
        updateJobStatus(jobId, JobStatusEnum.CANCELED_BY_ADMIN);
        LOG.info("Updated job {} to status CANCELED_BY_ADMIN", jobId);
        Map<String, Object> value = new HashMap<>();
        String user = jobVO.getCreatorUsername();
        value.put(LiteralConstants.USER, user);

        TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(adminUser, BEARER + tokenVo.getAccessToken(), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        removeLocksAndSendNotification(jobVO, value, user);
    }

    @Override
    public List<JobVO> getFMEImportJobsForPolling(){
        List<Job> jobs = jobRepository.findFmeJobsToPollForStatus();
        return jobMapper.entityListToClass(jobs);
    }

    @Override
    public void updateFmeStatus(Long jobId, FmeJobStatusEnum fmeStatus){
        jobRepository.updateFmeStatus(jobId, fmeStatus.getValue());
    }

    @Override
    public List<JobVO> findByJobTypeInAndJobStatusInAndRelease(List<JobTypeEnum> jobType, List<JobStatusEnum> jobStatus, boolean release){
        List<Job> jobList = jobRepository.findByJobTypeInAndJobStatusInAndRelease(jobType, jobStatus, release);
        return jobMapper.entityListToClass(jobList);
    }

    @Override
    public void updateJobInfo(Long jobId, JobInfoEnum jobInfo, Integer lineNumber){
        jobRepository.updateJobInfo(jobId, jobInfo.getValue(lineNumber));
        jobHistoryService.updateJobInfoOfLastHistoryEntry(jobId, jobInfo, lineNumber);
    }

    private void removeLocksAndSendNotification(JobVO jobVO, Map<String, Object> value, String user) throws EEAException {
        switch (jobVO.getJobType()) {
            case IMPORT:
                //remove locks
                dataSetControllerZuul.deleteLocksToImportProcess(jobVO.getDatasetId());
                jobUtils.sendKafkaImportNotification(jobVO, EventType.IMPORT_CANCELED_EVENT, CANCELED_BY_ADMIN_ERROR);
                break;
            case VALIDATION:
                if (jobVO.isRelease()) {
                    dataSetSnapshotControllerZuul.releaseLocksFromReleaseDatasets(jobVO.getDataflowId(), jobVO.getProviderId());
                    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_CANCELED_EVENT, value,
                            NotificationVO.builder().dataflowId(jobVO.getDataflowId()).providerId(jobVO.getProviderId()).user(user).error(CANCELED_BY_ADMIN_ERROR).build());
                } else {
                    validationControllerZuul.deleteLocksToReleaseProcess(jobVO.getDatasetId());
                    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.VALIDATION_CANCELED_EVENT, value,
                            NotificationVO.builder().datasetId(jobVO.getDatasetId()).user(user).error(CANCELED_BY_ADMIN_ERROR).build());
                }
                break;
            case RELEASE:
                dataSetSnapshotControllerZuul.releaseLocksFromReleaseDatasets(jobVO.getDataflowId(), jobVO.getProviderId());
                kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_CANCELED_EVENT, value,
                        NotificationVO.builder().dataflowId(jobVO.getDataflowId()).providerId(jobVO.getProviderId()).user(user).error(CANCELED_BY_ADMIN_ERROR).build());
                break;
            case COPY_TO_EU_DATASET:
                euDatasetControllerZuul.removeLocksRelatedToPopulateEU(jobVO.getDataflowId());
                kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.COPY_DATA_TO_EUDATASET_CANCELED_EVENT, value,
                        NotificationVO.builder().dataflowId(jobVO.getDataflowId()).user(user).error(CANCELED_BY_ADMIN_ERROR).build());
                break;
        }
    }

    @Transactional
    @Override
    public void updateFmeCallbackJobParameter(String fmeJobId, Boolean fmeCallback){
        Optional<Job> job = jobRepository.findJobByFmeJobId(fmeJobId);
        if(job.isPresent()){
            Map<String, Object> insertedParameters = job.get().getParameters();
            insertedParameters.put("fmeCallback", fmeCallback);
            jobRepository.save(job.get());
        }
    }

    /**
     * Download etl exported file.
     *
     * @param jobId the job id
     * @param fileName the file name
     * @return the file
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws EEAException the EEA exception
     */
    @Override
    public File downloadEtlExportedFile(Long jobId, String fileName) throws EEAException {
        // we compound the route and create the file
        File file = new File(new File(importPath, ETL_EXPORT), FilenameUtils.getName(fileName));
        if (!file.exists()) {
            LOG.error( "Trying to download a file generated during the export dataset data process for jobId {} but the file {} is not found", jobId, fileName);
            throw new EEAException(EEAErrorMessage.FILE_NOT_FOUND);
        }
        return file;
    }

    @Override
    public Long findProviderIdById(Long jobId) {
        Long providerId = jobRepository.findProviderIdByJobId(jobId);
        LOG.info("Found provider id {} for job {}", providerId, jobId);

        return providerId;
    }
}
