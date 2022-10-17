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

    /** The Constant LOG_ERROR. */
    private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

    /** The job service. */
    @Autowired
    private JobHistoryService jobHistoryService;

    @Override
    @GetMapping("job/{id}")
    public List<JobHistoryVO> getJobHistory(@PathVariable("id") Long jobId){
        try{
            return jobHistoryService.testRetrieveJobHistory(jobId);
        } catch (Exception e){
            LOG.error("Could not save retrieve job history for jobId {}. Message: {}", jobId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/add")
    public void addJobHistoryEntry(){
        try {
            jobHistoryService.testSaveJobHistory();
        } catch (Exception e){
            LOG.error("Could not save job history entry. Message: {}", e.getMessage());
            throw e;
        }
    }
}
