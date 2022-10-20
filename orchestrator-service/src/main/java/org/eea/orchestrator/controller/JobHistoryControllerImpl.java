package org.eea.orchestrator.controller;

import io.swagger.annotations.Api;
import org.eea.interfaces.controller.orchestrator.JobHistoryController;
import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.orchestrator.persistence.domain.Job;
import org.eea.orchestrator.service.JobHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import java.sql.Timestamp;

import java.util.List;

@RestController
@RequestMapping("/jobHistory")
@Api(tags = "Orchestrator: Job history handling")
@ApiIgnore
public class JobHistoryControllerImpl implements JobHistoryController {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(JobHistoryControllerImpl.class);

    /** The job service. */
    @Autowired
    private JobHistoryService jobHistoryService;

    @Override
    @GetMapping("job/{id}")
    public List<JobHistoryVO> getJobHistory(@PathVariable("id") Long jobId){
        try{
            return jobHistoryService.testRetrieveJobHistory(jobId);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not save retrieve job history for jobId {}. Message: {}", jobId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/add")
    public void addJobHistoryEntry(){
        try {
            Job job1 = new Job(null, JobTypeEnum.IMPORT, JobStatusEnum.CREATED, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), null, "testUser", null);
            jobHistoryService.saveJobHistory(job1);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not save job history entry. Message: {}", e.getMessage());
            throw e;
        }
    }
}
