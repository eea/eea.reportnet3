package org.eea.orchestrator.service.impl;

import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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

    @Autowired
    private DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul;

    @Autowired
    private ValidationControllerZuul validationControllerZuul;

    @Autowired
    private EUDatasetControllerZuul euDatasetControllerZuul;

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

    @Override
    public List<JobVO> getJobsByStatusAndTypeAndMaxDuration(JobTypeEnum jobType, JobStatusEnum status, Long maxDuration){
        List<Job> jobs = jobRepository.findByJobStatusAndJobType(status, jobType);
        List<Job> longRunningJobs = new ArrayList<>();
        for(Job job : jobs){
            Long durationOfJob = new Date().getTime() - job.getDateStatusChanged().getTime();
            if (durationOfJob > maxDuration){
                longRunningJobs.add(job);
            }
        }
        return jobMapper.entityListToClass(longRunningJobs);
    }

    @Override
    public List<JobVO> getJobsByTypeAndStatus(JobTypeEnum type, JobStatusEnum status){
        List<Job> jobs = jobRepository.findByJobTypeAndJobStatus(type, status);
        return jobMapper.entityListToClass(jobs);
    }

    @Transactional
    @Override
    public Long addJob(Long dataflowId, Long dataProviderId, Long datasetId, Map<String, Object> parameters, JobTypeEnum jobType, JobStatusEnum jobStatus, boolean release) {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Job job = new Job(null, jobType, jobStatus, ts, ts, parameters, SecurityContextHolder.getContext().getAuthentication().getName(), release, dataflowId, dataProviderId, datasetId,null);
        job = jobRepository.save(job);
        jobHistoryService.saveJobHistory(job);
        return job.getId();
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
        } else if (jobType.equals(JobTypeEnum.VALIDATION.toString()) && !release) {
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
        } else if (jobType.equals(JobTypeEnum.COPY_TO_EU_DATASET.toString())) {
            List<Job> jobList = jobRepository.findByJobTypeAndJobStatusInAndRelease(JobTypeEnum.COPY_TO_EU_DATASET, Arrays.asList(JobStatusEnum.QUEUED, JobStatusEnum.IN_PROGRESS), release);
            for (Job job : jobList) {
                Map<String, Object> insertedParameters = job.getParameters();
                Long insertedDataflowId = Long.valueOf((Integer) insertedParameters.get("dataflowId"));
                if (dataflowId.equals(insertedDataflowId)) {
                    return JobStatusEnum.REFUSED;
                }
            }
        }
        else if (jobType.equals(JobTypeEnum.IMPORT.toString())) {
            List<Job> jobList = jobRepository.findByJobStatusAndJobTypeInAndDatasetId(JobStatusEnum.IN_PROGRESS, Arrays.asList(JobTypeEnum.IMPORT, JobTypeEnum.RELEASE, JobTypeEnum.VALIDATION), datasetIds.get(0));
            if(jobList != null && jobList.size() > 0){
                return JobStatusEnum.REFUSED;
            }
            else{
                return JobStatusEnum.IN_PROGRESS;
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
        dataSetSnapshotControllerZuul.createReleaseSnapshots(dataflowId, dataProviderId, restrictFromPublic, validate, jobVO.getId());
    }

    @Override
    public void prepareAndExecuteCopyToEUDatasetJob(JobVO jobVO) {
        Job job = jobMapper.classToEntity(jobVO);
        Map<String, Object> parameters = job.getParameters();
        Long dataflowId = Long.valueOf((Integer) parameters.get("dataflowId"));
        euDatasetControllerZuul.populateDataFromDataCollection(dataflowId, job.getId());
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
        jobRepository.deleteJobsBasedOnStatusAndDuration(new HashSet<>(Arrays.asList(JobStatusEnum.FINISHED.getValue(), JobStatusEnum.REFUSED.getValue(), JobStatusEnum.FAILED.getValue(), JobStatusEnum.CANCELED.getValue())));
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

    @Override
    public JobVO findByFmeJobId(String fmeJobId) {
        Optional<Job> job = jobRepository.findJobByFmeJobId(fmeJobId);
        if (job.isPresent()){
            return jobMapper.entityToClass(job.get());
        }
        return null;
    }

    @Override
    public void updateFmeJobId(Long jobId, String fmeJobId) {
        Optional<Job> job = jobRepository.findById(jobId);
        if(job.isPresent()){
            job.get().setFmeJobId(fmeJobId);
            jobRepository.save(job.get());
            jobHistoryService.saveJobHistory(job.get());
        }
        else{
            LOG.info("Could not update fmeJobId for jobId {} because the id does not exist", jobId);
        }
    }

    @Override
    public List<BigInteger> listJobsThatExceedTimeWithSpecificStatus(String status, long timeInMinutes) {
        return jobRepository.findJobsThatExceedTimeWithSpecificStatus(status, timeInMinutes);
    }

}
