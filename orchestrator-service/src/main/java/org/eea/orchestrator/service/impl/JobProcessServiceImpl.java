package org.eea.orchestrator.service.impl;

import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.eea.orchestrator.mapper.JobProcessMapper;
import org.eea.orchestrator.persistence.domain.JobProcess;
import org.eea.orchestrator.persistence.repository.JobProcessRepository;
import org.eea.orchestrator.persistence.repository.JobRepository;
import org.eea.orchestrator.service.JobProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobProcessServiceImpl implements JobProcessService {

    private JobProcessRepository jobProcessRepository;
    private JobProcessMapper jobProcessMapper;
    private JobRepository jobRepository;

    @Autowired
    public JobProcessServiceImpl(JobProcessRepository jobProcessRepository, JobProcessMapper jobProcessMapper) {
        this.jobProcessRepository = jobProcessRepository;
        this.jobProcessMapper = jobProcessMapper;
    }

    /**
     * Saves jobProcessVO
     * @param jobProcessVO
     * @return
     */
    @Override
    public JobProcessVO saveJobProcess(JobProcessVO jobProcessVO) {
        JobProcess jobProcess = jobProcessRepository.save(jobProcessMapper.classToEntity(jobProcessVO));
        return jobProcessMapper.entityToClass(jobProcess);
    }

    /**
     * Finds jobId by processId
     * @param processId
     * @return
     */
    @Override
    public Long findJobIdByProcessId(String processId) {
        JobProcess jobProcess = jobProcessRepository.findByProcessId(processId);
        if (jobProcess!=null) {
            return jobProcess.getJobId();
        }
        return null;
    }

    @Override
    public List<String> findProcessesByJobId(Long jobId) {
        return jobProcessRepository.findProcessesByJobId(jobId);
    }

    @Override
    public void deleteJobProcessByProcessId(String processId){
        jobProcessRepository.deleteJobProcessByProcessId(processId);
    }

    @Override
    public String findStatusByJobId(Long jobId) {
        return jobRepository.findStatusByJobId(jobId);
    }
}
