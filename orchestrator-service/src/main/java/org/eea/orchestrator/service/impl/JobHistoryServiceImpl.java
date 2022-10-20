package org.eea.orchestrator.service.impl;

import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.orchestrator.mapper.JobHistoryMapper;
import org.eea.orchestrator.persistence.domain.Job;
import org.eea.orchestrator.persistence.domain.JobHistory;
import org.eea.orchestrator.persistence.repository.JobHistoryRepository;
import org.eea.orchestrator.service.JobHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobHistoryServiceImpl implements JobHistoryService {

    @Autowired
    private JobHistoryRepository jobHistoryRepository;

    /** The job history mapper. */
    @Autowired
    private JobHistoryMapper jobHistoryMapper;

    @Transactional
    @Override
    public void saveJobHistory(Job job){
        JobHistory entry = new JobHistory(null, job.getId(), job.getJobType(), job.getJobStatus(), job.getDateAdded(), job.getDateStatusChanged(), job.getParameters(), job.getCreatorUsername(), job.getProcessId());
        jobHistoryRepository.save(entry);
    }

    @Override
    public List<JobHistoryVO> testRetrieveJobHistory(Long jobId){
        List<JobHistory> entries = jobHistoryRepository.findAllByJobId(jobId);
        return jobHistoryMapper.entityListToClass(entries);
    }
}
