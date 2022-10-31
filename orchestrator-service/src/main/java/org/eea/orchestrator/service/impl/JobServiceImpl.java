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

@Service
public class JobServiceImpl implements JobService {

    private static final Logger LOG = LoggerFactory.getLogger(JobServiceImpl.class);

    @Value(value = "${scheduling.inProgress.maximum.jobs}")
    private Long maximumNumberOfInProgressJobs;

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
    public List<JobVO> getJobsByStatus(JobStatusEnum status){
        List<Job> jobs = jobRepository.findAllByJobStatus(status);
        return jobMapper.entityListToClass(jobs);
    }

    @Transactional
    @Override
    public void addValidationJob(Long datasetId, Boolean released, String creator){
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("datasetId", datasetId);
        parameters.put("released", released);
        Job validationJob = new Job(null, JobTypeEnum.VALIDATION, JobStatusEnum.QUEUED, ts, ts, parameters, creator, null);
        jobRepository.save(validationJob);
        jobHistoryService.saveJobHistory(validationJob);
        LOG.info("Added validation job with id {} for datasetId {}", validationJob.getId(), datasetId);
    }

    @Transactional
    @Override
    public void addReleaseJob(Long dataflowId, Long dataProviderId, Boolean restrictFromPublic, Boolean validate, String creator){
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dataflowId", dataflowId);
        parameters.put("dataProviderId", dataProviderId);
        parameters.put("restrictFromPublic", restrictFromPublic);
        parameters.put("validate", validate);
        Job releaseJob = new Job(null, JobTypeEnum.RELEASE, JobStatusEnum.QUEUED, ts, ts, parameters, creator, null);
        jobRepository.save(releaseJob);
        jobHistoryService.saveJobHistory(releaseJob);
        LOG.info("Added release job with id {} for dataflowId {} and dataProviderId {}", releaseJob.getId(), dataflowId, dataProviderId);
    }

    @Override
    public Boolean canJobBeExecuted(JobVO job){
        // TODO implement this
        if( jobRepository.countByJobStatus(JobStatusEnum.IN_PROGRESS) <= maximumNumberOfInProgressJobs ){
            return true;
        }
        return false;
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
    public void deleteFinishedJobsBasedOnDuration(Long duration){
        jobRepository.deleteJobsBasedOnStatusAndDuration(Arrays.asList(JobStatusEnum.FINISHED, JobStatusEnum.ABORTED, JobStatusEnum.FAILED), duration * (-1));
    }

    @Transactional
    @Override
    public void deleteJob(JobVO jobVO){
        jobRepository.deleteById(jobVO.getId());
        LOG.info("Removed job with id {} and type {}", jobVO.getId(), jobVO.getJobType().getValue());
    }

}
