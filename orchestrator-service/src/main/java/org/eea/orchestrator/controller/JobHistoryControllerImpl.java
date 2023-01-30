package org.eea.orchestrator.controller;

import io.swagger.annotations.Api;
import org.eea.interfaces.controller.orchestrator.JobHistoryController;
import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.orchestrator.mapper.JobMapper;
import org.eea.orchestrator.service.JobHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/jobHistory")
@Api(tags = "Orchestrator: Job history handling")
@ApiIgnore
public class JobHistoryControllerImpl implements JobHistoryController {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(JobHistoryControllerImpl.class);

    /** The job service. */
    private JobHistoryService jobHistoryService;
    private JobMapper jobMapper;

    @Autowired
    public JobHistoryControllerImpl(JobHistoryService jobHistoryService, JobMapper jobMapper) {
        this.jobHistoryService = jobHistoryService;
        this.jobMapper = jobMapper;
    }

    @Override
    @GetMapping("/{jobId}")
    public List<JobHistoryVO> getJobHistory(@PathVariable("jobId") Long jobId){
        try{
            LOG.info("Retrieving job history for job {}", jobId);
            return jobHistoryService.getJobHistory(jobId);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not retrieve job history for job {}. Error: {}", jobId, e);
            throw e;
        }
    }

    /**
     * Saves job history for job
     * @param jobVO
     * @return
     */
    @Override
    @PostMapping(value = "/save")
    public void save(@RequestBody JobVO jobVO) {
        jobHistoryService.saveJobHistory(jobMapper.classToEntity(jobVO));
    }

}
