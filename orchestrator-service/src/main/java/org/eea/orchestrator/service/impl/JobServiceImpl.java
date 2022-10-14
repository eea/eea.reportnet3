package org.eea.orchestrator.service.impl;

import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.orchestrator.mapper.JobMapper;
import org.eea.orchestrator.persistence.domain.Job;
import org.eea.orchestrator.persistence.repository.JobRepository;
import org.eea.orchestrator.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private JobRepository jobRepository;

    /** The job mapper. */
    @Autowired
    private JobMapper jobMapper;

    @Override
    public void testSaveJob(){
        Job job1 = new Job(null, JobTypeEnum.IMPORT, JobStatusEnum.CREATED, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), null, "testUser");
        jobRepository.save(job1);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("a",1);
        parameters.put("b","2");
        Map<String, Object> parameters2 = new HashMap<>();
        parameters2.put("c",3);
        parameters2.put("d","4");
        parameters.put("e", parameters2);
        Job job2 = new Job(null, JobTypeEnum.VALIDATION, JobStatusEnum.IN_PROGRESS, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), parameters, "testUser2");
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
}
