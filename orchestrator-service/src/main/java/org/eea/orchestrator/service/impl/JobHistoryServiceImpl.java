package org.eea.orchestrator.service.impl;

import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.orchestrator.mapper.JobHistoryMapper;
import org.eea.orchestrator.persistence.domain.Job;
import org.eea.orchestrator.persistence.domain.JobHistory;
import org.eea.orchestrator.persistence.repository.JobHistoryRepository;
import org.eea.orchestrator.service.JobHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class JobHistoryServiceImpl implements JobHistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(JobHistoryServiceImpl.class);

    @Autowired
    private JobHistoryRepository jobHistoryRepository;

    /** The job history mapper. */
    @Autowired
    private JobHistoryMapper jobHistoryMapper;

    @Transactional
    @Override
    public void saveJobHistory(Job job){
        JobHistory entry = new JobHistory(null, job.getId(), job.getJobType(), job.getJobStatus(), job.getDateAdded(), job.getDateStatusChanged(), job.getParameters(), job.getCreatorUsername(), job.isRelease(), job.getDataflowId(), job.getProviderId(), job.getDatasetId(),job.getFmeJobId(), job.getDataflowName(), job.getDatasetName(), job.getJobInfo(), job.getFmeStatus());
        jobHistoryRepository.save(entry);
    }

    @Override
    public List<JobHistoryVO> getJobHistory(Long jobId){
        List<JobHistory> jobHistoryList = jobHistoryRepository.findAllByJobIdOrderById(jobId);
        return jobHistoryMapper.entityListToClass(jobHistoryList);
    }

    @Override
    public void updateJobInfoOfLastHistoryEntry(Long jobId, JobInfoEnum jobInfo, Integer lineNumber){
        Optional<JobHistory> optionalJobHistory = jobHistoryRepository.findFirstByJobIdOrderByIdDesc(jobId);
        if(optionalJobHistory.isPresent()){
            optionalJobHistory.get().setJobInfo(jobInfo.getValue(lineNumber));
            jobHistoryRepository.save(optionalJobHistory.get());
        }
    }
}
