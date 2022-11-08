package org.eea.orchestrator.controller;

import io.swagger.annotations.Api;
import org.eea.interfaces.controller.orchestrator.JobHistoryController;
import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
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
    @Autowired
    private JobHistoryService jobHistoryService;

    @Override
    @GetMapping("/{jobId}")
    public List<JobHistoryVO> getJobHistory(@PathVariable("jobId") Long jobId){
        try{
            LOG.info("Retrieving job history for job {}", jobId);
            return jobHistoryService.getJobHistory(jobId);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not retrieve job history for job {}. Message: {}", jobId, e.getMessage());
            throw e;
        }
    }

}
