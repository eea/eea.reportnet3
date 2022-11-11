package org.eea.orchestrator.service.impl;

import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.orchestrator.mapper.JobMapper;
import org.eea.orchestrator.persistence.domain.Job;
import org.eea.orchestrator.persistence.repository.JobRepository;
import org.eea.orchestrator.service.JobHistoryService;
import org.eea.orchestrator.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    @Override
    public List<JobVO> getAllJobs(){
        List<Job> jobs = jobRepository.findAllByOrderById();
        return jobMapper.entityListToClass(jobs);
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
        Job validationJob = new Job(null, JobTypeEnum.VALIDATION, statusToInsert, ts, ts, parameters, creator, null);
        jobRepository.save(validationJob);
        jobHistoryService.saveJobHistory(validationJob);
    }

    @Transactional
    @Override
    public void addReleaseJob(Map<String, Object> parameters, String creator, JobStatusEnum statusToInsert){
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Job releaseJob = new Job(null, JobTypeEnum.RELEASE, statusToInsert, ts, ts, parameters, creator, null);
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
    public JobStatusEnum checkEligibilityOfJob(JobTypeEnum jobType, Map<String, Object> parameters){
        //TODO implement check and return something
        if(jobType == JobTypeEnum.RELEASE){
            Long dataflowId = (Long) parameters.get("dataflowId");
            Long dataProviderId = (Long) parameters.get("dataProviderId");
            List<Job> jobList = jobRepository.findByJobTypeAndJobStatusIn(JobTypeEnum.RELEASE, Arrays.asList(JobStatusEnum.QUEUED, JobStatusEnum.IN_PROGRESS));
            for(Job job: jobList){
                Map<String, Object> insertedParameters = job.getParameters();
                Long insertedDataflowId = Long.valueOf((Integer) insertedParameters.get("dataflowId"));
                Long insertedDataProviderId = Long.valueOf((Integer) insertedParameters.get("dataProviderId"));
                if(dataflowId == insertedDataflowId && dataProviderId == insertedDataProviderId){
                    return JobStatusEnum.ABORTED;
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
        dataSetSnapshotControllerZuul.createReleaseSnapshots(dataflowId, dataProviderId, restrictFromPublic, validate);
    }

    @Transactional
    @Override
    public void updateJobStatusByProcessId(JobStatusEnum status, String processId){
        LOG.info("When updating job status for process id {} status was {}", processId, status.getValue());
        Job job = jobRepository.findOneByProcessId(processId);
        if(job == null){
            LOG.info("Could not find job with processId {}", processId);
            return;
        }
        job.setJobStatus(status);
        job.setDateStatusChanged(new Timestamp(System.currentTimeMillis()));
        jobRepository.save(job);
        jobHistoryService.saveJobHistory(job);
    }

    @Transactional
    @Override
    public void updateJobStatus(Long jobId, JobStatusEnum status, String processId){
        Job job = jobRepository.getOne(jobId);
        job.setJobStatus(status);
        job.setDateStatusChanged(new Timestamp(System.currentTimeMillis()));
        if(processId != null) {
            job.setProcessId(processId);
        }
        jobRepository.save(job);
        jobHistoryService.saveJobHistory(job);
    }

    @Transactional
    @Override
    public void deleteFinishedJobsBasedOnDuration(){
        jobRepository.deleteJobsBasedOnStatusAndDuration(new HashSet<>(Arrays.asList(JobStatusEnum.FINISHED.getValue(), JobStatusEnum.ABORTED.getValue(), JobStatusEnum.FAILED.getValue(), JobStatusEnum.CANCELLED.getValue())));
    }

    @Transactional
    @Override
    public void deleteJob(JobVO jobVO){
        jobRepository.deleteById(jobVO.getId());
        LOG.info("Removed job with id {} and type {}", jobVO.getId(), jobVO.getJobType().getValue());
    }

}
