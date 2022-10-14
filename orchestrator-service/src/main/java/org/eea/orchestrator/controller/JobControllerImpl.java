package org.eea.orchestrator.controller;

import io.swagger.annotations.Api;
import org.eea.interfaces.controller.orchestrator.JobController;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.orchestrator.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/jobs")
@Api(tags = "Orchestrator: Job handling")
@ApiIgnore
public class JobControllerImpl implements JobController {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(JobControllerImpl.class);

    /** The Constant LOG_ERROR. */
    private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

    /** The job service. */
    @Autowired
    private JobService jobService;

    @Override
    @GetMapping("/{id}")
    public JobVO getJob(@PathVariable("id") Long id){
        try{
            return jobService.testRetrieveJob(id);
        } catch (Exception e){
            LOG.error("Could not save retrieve job info for jobId {}. Message: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Adds a job.
     **
     */
    @PostMapping("/add")
    public void addJob(){
        try {
            jobService.testSaveJob();
        } catch (Exception e){
            LOG.error("Could not save job entry. Message: {}", e.getMessage());
            throw e;
        }
    }

}
