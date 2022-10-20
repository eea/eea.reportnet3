package org.eea.orchestrator.service.impl;

import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.orchestrator.mapper.JobMapper;
import org.eea.orchestrator.persistence.domain.Job;
import org.eea.orchestrator.persistence.repository.JobRepository;
import org.eea.orchestrator.service.JobHistoryService;
import org.eea.orchestrator.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class JobServiceImpl implements JobService {

    private static final Logger LOG = LoggerFactory.getLogger(JobServiceImpl.class);

    @Autowired
    private ValidationControllerZuul validationControllerZuul;

    @Autowired
    private JobRepository jobRepository;

    /** The job mapper. */
    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private JobHistoryService jobHistoryService;

    @Transactional
    @Override
    public void testSaveJob(){
        Job job1 = new Job(null, JobTypeEnum.IMPORT, JobStatusEnum.CREATED, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), null, "testUser", null);
        jobRepository.save(job1);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("a",1);
        parameters.put("b","2");
        Map<String, Object> parameters2 = new HashMap<>();
        parameters2.put("c",3);
        parameters2.put("d","4");
        parameters.put("e", parameters2);
        Job job2 = new Job(null, JobTypeEnum.VALIDATION, JobStatusEnum.IN_PROGRESS, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), parameters, "testUser2", null);
        jobRepository.save(job2);
    }

    @Override
    public JobVO testRetrieveJob(Long id){
        Optional<Job> job = jobRepository.findById(id);
        if(job.isPresent()){
            return jobMapper.entityToClass(job.get());
        }
        return null;
    }

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
        //maybe queued should be changed to created ?
        Job validationJob = new Job(null, JobTypeEnum.VALIDATION, JobStatusEnum.QUEUED, ts, ts, parameters, creator, null);
        jobRepository.save(validationJob);
        jobHistoryService.saveJobHistory(validationJob);
    }

    @Override
    public Boolean canJobBeExecuted(JobVO job){
        // TODO implement this
        return true;
    }

    @Override
    public void prepareAndExecuteValidationJob(JobVO jobVO){
        Job job = jobMapper.classToEntity(jobVO);
        Map<String, Object> parameters = job.getParameters();
        Long datasetId = Long.valueOf((Integer) parameters.get("datasetId"));
        Boolean released = (Boolean) parameters.get("released");
        validationControllerZuul.validateDataSetData(datasetId, released, job.getId());
    }

    @Transactional
    @Override
    public void updateStatusByProcessId(String status, String processId){
        LOG.info("When updating job status for process id {} status was {}", processId, status);
        JobStatusEnum jobStatus;
        if(status.equals(ProcessStatusEnum.FINISHED.toString())){
            jobStatus = JobStatusEnum.SUCCESS;
        }
        else if(status.equals(ProcessStatusEnum.CANCELED.toString())){
            jobStatus = JobStatusEnum.ABORTED;
        }
        else{
            return;
        }
        Job job = jobRepository.findOneByProcessId(processId);
        job.setJobStatus(jobStatus);
        job.setDateStatusChanged(new Timestamp(System.currentTimeMillis()));
        jobRepository.save(job);
        jobHistoryService.saveJobHistory(job);
    }

    @Transactional
    @Override
    public void updateJobInProgress(Long jobId, String processId){
        Job job = jobRepository.getOne(jobId);
        job.setJobStatus(JobStatusEnum.IN_PROGRESS);
        job.setDateStatusChanged(new Timestamp(System.currentTimeMillis()));
        job.setProcessId(processId);
        jobRepository.save(job);
        jobHistoryService.saveJobHistory(job);
    }
}
