package org.eea.orchestrator.service.impl;

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

    @Override
    public JobsVO getJobs(Pageable pageable, boolean asc, String sortedColumn, Long jobId,
                               String jobTypes, String processId, String creatorUsername, String jobStatuses){
        String sortedTableColumn = jobUtils.getJobColumnNameByObjectName(sortedColumn);
        List<Job> jobs = jobRepository.findJobsPaginated(pageable, asc, sortedTableColumn, jobId, jobTypes, processId, creatorUsername, jobStatuses);
        List<JobVO> jobVOList = jobMapper.entityListToClass(jobs);
        JobsVO jobsVO = new JobsVO();
        jobsVO.setTotalRecords(jobRepository.count());
        jobsVO.setFilteredRecords(jobRepository.countJobsPaginated(asc, sortedTableColumn, jobId, jobTypes, processId, creatorUsername, jobStatuses));
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
    public void addValidationJob(Map<String, Object> parameters, String creator, JobStatusEnum statusToInsert){
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Job validationJob = new Job(null, JobTypeEnum.VALIDATION, statusToInsert, ts, ts, parameters, creator, false);
        jobRepository.save(validationJob);
        jobHistoryService.saveJobHistory(validationJob);
    }

    @Transactional
    @Override
    public void addReleaseJob(Map<String, Object> parameters, String creator, JobStatusEnum statusToInsert){
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Job releaseJob = new Job(null, JobTypeEnum.VALIDATION, statusToInsert, ts, ts, parameters, creator, true);
        jobRepository.save(releaseJob);
        jobHistoryService.saveJobHistory(releaseJob);
    }

    @Override
    public Boolean canJobBeExecuted(JobVO job){
        JobTypeEnum jobType = job.getJobType();
        Integer numberOfCurrentJobs = jobRepository.countByJobStatusAndJobType(JobStatusEnum.IN_PROGRESS, jobType);
        if(job.getJobType() == JobTypeEnum.IMPORT && numberOfCurrentJobs < maximumNumberOfInProgressImportJobs){
            return true;
        }
        else if(jobType == JobTypeEnum.VALIDATION && numberOfCurrentJobs < maximumNumberOfInProgressValidationJobs){
            return true;
        }
        else if(jobType == JobTypeEnum.RELEASE && numberOfCurrentJobs < maximumNumberOfInProgressReleaseJobs){
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
    public JobStatusEnum checkEligibilityOfJob(String jobType, boolean release, Map<String, Object> parameters){
        //TODO implement check for all jobTypes
        if (jobType == JobTypeEnum.RELEASE.toString()) {
            Long dataflowId = (Long) parameters.get("dataflowId");
            Long dataProviderId = (Long) parameters.get("dataProviderId");
            List<Job> jobList = jobRepository.findByJobTypeInAndJobStatusInAndRelease(Arrays.asList(JobTypeEnum.RELEASE, JobTypeEnum.VALIDATION), Arrays.asList(JobStatusEnum.QUEUED, JobStatusEnum.IN_PROGRESS), release);
            for(Job job: jobList){
                Map<String, Object> insertedParameters = job.getParameters();
                Long insertedDataflowId = Long.valueOf((Integer) insertedParameters.get("dataflowId"));
                Long insertedDataProviderId = Long.valueOf((Integer) insertedParameters.get("dataProviderId"));
                if(dataflowId == insertedDataflowId && dataProviderId == insertedDataProviderId){
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
        jobVO.setJobStatus(JobStatusEnum.IN_PROGRESS);
        save(jobVO);
        dataSetSnapshotControllerZuul.createReleaseSnapshots(dataflowId, dataProviderId, restrictFromPublic, validate, jobVO.getId());
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

}
