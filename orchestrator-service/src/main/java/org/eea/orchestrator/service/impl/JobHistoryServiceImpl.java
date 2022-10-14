package org.eea.orchestrator.service.impl;

import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.orchestrator.mapper.JobHistoryMapper;
import org.eea.orchestrator.persistence.domain.JobHistory;
import org.eea.orchestrator.persistence.repository.JobHistoryRepository;
import org.eea.orchestrator.service.JobHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    @Override
    public void testSaveJobHistory(){
        JobHistory entry1 = new JobHistory(null, 1L, JobTypeEnum.IMPORT, JobStatusEnum.CREATED, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), null, "testUser");
        jobHistoryRepository.save(entry1);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("a",1);
        parameters.put("b","2");
        Map<String, Object> parameters2 = new HashMap<>();
        parameters2.put("c",3);
        parameters2.put("d","4");
        parameters.put("e", parameters2);
        JobHistory entry2 = new JobHistory(null, 2L, JobTypeEnum.RELEASE, JobStatusEnum.IN_PROGRESS, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), parameters, "testUser2");
        jobHistoryRepository.save(entry2);
    }

    @Override
    public List<JobHistoryVO> testRetrieveJobHistory(Long jobId){
        List<JobHistory> entries = jobHistoryRepository.findAllByJobId(jobId);
        return jobHistoryMapper.entityListToClass(entries);
    }
}
