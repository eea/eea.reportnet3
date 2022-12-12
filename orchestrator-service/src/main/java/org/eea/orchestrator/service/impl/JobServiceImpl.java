package org.eea.orchestrator.service.impl;

import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.MetaData;
import org.eea.axon.release.commands.CreateReleaseStartNotificationCommand;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.JobsVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.orchestrator.mapper.JobMapper;
import org.eea.orchestrator.persistence.domain.Job;
import org.eea.orchestrator.persistence.repository.JobRepository;
import org.eea.orchestrator.service.JobHistoryService;
import org.eea.orchestrator.service.JobService;
import org.eea.orchestrator.utils.JobUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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

    @Autowired
    private DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul;

    @Autowired
    private ValidationControllerZuul validationControllerZuul;

    @Autowired
    private JobRepository jobRepository;

    /** The job mapper. */
    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private JobHistoryService jobHistoryService;

    /** The job utils. */
    @Autowired
    private JobUtils jobUtils;

    @Autowired
    private transient CommandGateway commandGateway;

    /**
     * enable release with saga implementation
     */
    private boolean enableReleaseSaga = true;

    @Override
    public JobsVO getJobs(Pageable pageable, boolean asc, String sortedColumn, Long jobId, String jobTypes, Long dataflowId, Long providerId,
                          Long datasetId, String creatorUsername, String jobStatuses){
        String sortedTableColumn = jobUtils.getJobColumnNameByObjectName(sortedColumn);
        List<Job> jobs = jobRepository.findJobsPaginated(pageable, asc, sortedTableColumn, jobId, jobTypes, dataflowId, providerId, datasetId, creatorUsername, jobStatuses);
        List<JobVO> jobVOList = jobMapper.entityListToClass(jobs);
        JobsVO jobsVO = new JobsVO();
        jobsVO.setTotalRecords(jobRepository.count());
        jobsVO.setFilteredRecords(jobRepository.countJobsPaginated(asc, sortedTableColumn, jobId, jobTypes, dataflowId, providerId, datasetId, creatorUsername, jobStatuses));
        jobsVO.setJobsList(jobVOList);

        return jobsVO;
    }

    @Override
    public List<JobVO> getJobsByStatus(JobStatusEnum status){
        List<Job> jobs = jobRepository.findAllByJobStatusOrderById(status);
        return jobMapper.entityListToClass(jobs);
    }

    @Transactional
    @Override
    public void addValidationJob(Long dataflowId, Long providerId, Long datasetId, Map<String, Object> parameters, String creator, JobStatusEnum statusToInsert){
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Job validationJob = new Job(null, JobTypeEnum.VALIDATION, statusToInsert, ts, ts, parameters, creator, false, dataflowId, providerId, datasetId);
        jobRepository.save(validationJob);
        jobHistoryService.saveJobHistory(validationJob);
    }

    @Transactional
    @Override
    public void addReleaseJob(Long dataflowId, Long dataProviderId, Map<String, Object> parameters, String creator, JobStatusEnum statusToInsert){
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Job releaseJob = new Job(null, JobTypeEnum.VALIDATION, statusToInsert, ts, ts, parameters, creator, true, dataflowId, dataProviderId, null);
        jobRepository.save(releaseJob);
        jobHistoryService.saveJobHistory(releaseJob);
    }

    @Transactional
    @Override
    public Long addImportJob(Long dataflowId, Long providerId, Long datasetId, Map<String, Object> parameters, String creator, JobStatusEnum statusToInsert){
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Job importJob = new Job(null, JobTypeEnum.IMPORT, statusToInsert, ts, ts, parameters, creator, false, dataflowId, providerId, datasetId);
        jobRepository.save(importJob);
        jobHistoryService.saveJobHistory(importJob);
        return importJob.getId();
    }


    @Override
    public Boolean canJobBeExecuted(JobVO job){
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
        if(job.getJobType() == JobTypeEnum.IMPORT && numberOfCurrentJobs < maximumNumberOfInProgressImportJobs){
            return true;
        }
        else if(jobType == JobTypeEnum.VALIDATION && !job.isRelease() && numberOfCurrentJobs < maximumNumberOfInProgressValidationJobs){
            return true;
        }
        else if(jobType == JobTypeEnum.COPY_TO_EU_DATASET && numberOfCurrentJobs < maximumNumberOfInProgressCopyToEuDatasetJobs){
            return true;
        }
        else if(jobType == JobTypeEnum.EXPORT && numberOfCurrentJobs < maximumNumberOfInProgressExportJobs){
            return true;
        }
        return false;
    }

    @Override
    public JobStatusEnum checkEligibilityOfJob(String jobType, Long dataflowId, Long dataProviderId, List<Long> datasetIds, boolean release){
        //TODO implement check for all jobTypes
        if (jobType.equals(JobTypeEnum.VALIDATION.toString()) && release || jobType.equals(JobTypeEnum.RELEASE.toString())) {
            List<Job> validationJobList = jobRepository.findByJobTypeAndJobStatusInAndRelease(JobTypeEnum.VALIDATION, Arrays.asList(JobStatusEnum.QUEUED, JobStatusEnum.IN_PROGRESS), false);
            for (Job job : validationJobList) {
                Map<String, Object> insertedParameters = job.getParameters();
                Long insertedDatasetId = Long.valueOf((Integer) insertedParameters.get("datasetId"));
                for (Long id : datasetIds) {
                    if(id.equals(insertedDatasetId)){
                        return JobStatusEnum.REFUSED;
                    }
                }
            }
            List<Job> jobsRelatedToReleaseList = jobRepository.findByJobTypeInAndJobStatusInAndRelease(Arrays.asList(JobTypeEnum.RELEASE, JobTypeEnum.VALIDATION), Arrays.asList(JobStatusEnum.QUEUED, JobStatusEnum.IN_PROGRESS), release);
            for(Job job: jobsRelatedToReleaseList){
                Map<String, Object> insertedParameters = job.getParameters();
                Long insertedDataflowId = Long.valueOf((Integer) insertedParameters.get("dataflowId"));
                Long insertedDataProviderId = Long.valueOf((Integer) insertedParameters.get("dataProviderId"));
                if(dataflowId.equals(insertedDataflowId) && dataProviderId.equals(insertedDataProviderId)){
                    return JobStatusEnum.REFUSED;
                }
            }
        } else if (jobType.equals(JobTypeEnum.VALIDATION.toString())) {
            Long datasetId = datasetIds.get(0);
            List<Job> jobList = jobRepository.findByJobTypeAndJobStatusInAndRelease(JobTypeEnum.VALIDATION, Arrays.asList(JobStatusEnum.QUEUED, JobStatusEnum.IN_PROGRESS), release);
            for(Job job: jobList){
                Map<String, Object> insertedParameters = job.getParameters();
                Long insertedDatasetId = Long.valueOf((Integer) insertedParameters.get("datasetId"));
                if(datasetId.equals(insertedDatasetId)){
                    return JobStatusEnum.REFUSED;
                }
            }
            List<Job> jobsRelatedToReleaseList = jobRepository.findByJobTypeInAndJobStatusInAndRelease(Arrays.asList(JobTypeEnum.RELEASE, JobTypeEnum.VALIDATION), Arrays.asList(JobStatusEnum.QUEUED, JobStatusEnum.IN_PROGRESS), true);
            for (Job job : jobsRelatedToReleaseList) {
                Map<String, Object> insertedParameters = job.getParameters();
                Long insertedDataflowId = Long.valueOf((Integer) insertedParameters.get("dataflowId"));
                Long insertedDataProviderId = Long.valueOf((Integer) insertedParameters.get("dataProviderId"));
                if(dataflowId.equals(insertedDataflowId) && dataProviderId!=null && dataProviderId.equals(insertedDataProviderId)){
                    return JobStatusEnum.REFUSED;
                }
            }
        }
        return JobStatusEnum.QUEUED;
    }

    @Override
    public void prepareAndExecuteValidationJob(JobVO jobVO){
        Job job = jobMapper.classToEntity(jobVO);
        Map<String, Object> parameters = job.getParameters();
        Long datasetId = Long.valueOf((Integer) parameters.get("datasetId"));
        Boolean released = (Boolean) parameters.get("released");
        validationControllerZuul.validateDataSetData(datasetId, released, job.getId());
    }

    @Override
    public void prepareAndExecuteReleaseJob(JobVO jobVO){
        Job job = jobMapper.classToEntity(jobVO);
        Map<String, Object> parameters = job.getParameters();
        Long dataflowId = Long.valueOf((Integer) parameters.get("dataflowId"));
        Long dataProviderId = Long.valueOf((Integer) parameters.get("dataProviderId"));
        Boolean restrictFromPublic = (Boolean) parameters.get("restrictFromPublic");
        Boolean validate = (Boolean) parameters.get("validate");
        if (this.enableReleaseSaga) {
            LOG.info("Starting release process for dataflow {}, dataProvider {}", dataflowId, dataProviderId);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CreateReleaseStartNotificationCommand command = CreateReleaseStartNotificationCommand.builder().transactionId(UUID.randomUUID().toString()).releaseAggregateId(UUID.randomUUID().toString())
                    .dataflowId(dataflowId).dataProviderId(dataProviderId).restrictFromPublic(restrictFromPublic).validate(validate).jobId(jobVO.getId()).build();
            commandGateway.send(GenericCommandMessage.asCommandMessage(command).withMetaData(MetaData.with("auth", authentication)));
        } else {
            dataSetSnapshotControllerZuul.createReleaseSnapshots(dataflowId, dataProviderId, restrictFromPublic, validate, jobVO.getId());
        }
    }

    @Transactional
    @Override
    public void updateJobStatus(Long jobId, JobStatusEnum status){
        Optional<Job> job = jobRepository.findById(jobId);
        if(job.isPresent()){
            job.get().setJobStatus(status);
            job.get().setDateStatusChanged(new Timestamp(System.currentTimeMillis()));
            jobRepository.save(job.get());
            jobHistoryService.saveJobHistory(job.get());
        }
        else{
            LOG.info("Could not update status for jobId {} because the id does not exist", jobId);
        }
    }

    @Transactional
    @Override
    public void deleteFinishedJobsBasedOnDuration(){
        jobRepository.deleteJobsBasedOnStatusAndDuration(new HashSet<>(Arrays.asList(JobStatusEnum.FINISHED.getValue(), JobStatusEnum.REFUSED.getValue(), JobStatusEnum.FAILED.getValue(), JobStatusEnum.CANCELLED.getValue())));
    }

    @Transactional
    @Override
    public void deleteJob(JobVO jobVO){
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
        if (job.isPresent()){
            return jobMapper.entityToClass(job.get());
        }
        return null;
    }

    @Override
    public boolean canExecuteReleaseOnDataflow(Long dataflowId) {
        List<Job> jobs = jobRepository.findByDataflowIdAndJobTypeInAndJobStatusAndRelease(dataflowId, Arrays.asList(JobTypeEnum.VALIDATION, JobTypeEnum.RELEASE), JobStatusEnum.IN_PROGRESS, true);
        if (jobs.size()>0) {
            return false;
        }
        return true;
    }

}
