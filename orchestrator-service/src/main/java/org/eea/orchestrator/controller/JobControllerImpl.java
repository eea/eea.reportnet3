package org.eea.orchestrator.controller;

import io.swagger.annotations.Api;
import org.eea.interfaces.controller.orchestrator.JobController;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.orchestrator.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@Api(tags = "Orchestrator: Job handling")
@ApiIgnore
public class JobControllerImpl implements JobController {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(JobControllerImpl.class);

    /** The job service. */
    @Autowired
    private JobService jobService;

    @Override
    @GetMapping("/{status}")
    public List<JobVO> getJobsByStatus(@PathVariable("status") JobStatusEnum status){
        try{
            LOG.info("Retrieving jobs for status {}", status.getValue());
            return jobService.getJobsByStatus(status);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not save retrieve jobs that have status {}. Message: {}", status.getValue(), e.getMessage());
            throw e;
        }
    }

    /**
     * Adds a validation job.
     */
    @PostMapping("/addValidation/{datasetId}/{released}/{creator}")
    public void addValidationJob(@PathVariable("datasetId") Long datasetId, @PathVariable("released") Boolean released, @PathVariable("creator") String creator){
        try {
            LOG.info("Adding validation job for datasetId {} and released {} for creator {}", datasetId, released, creator);
            jobService.addValidationJob(datasetId, released, creator);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not add validation job for datasetId {}, released {} and creator {}. Message: {}", datasetId, released, creator, e.getMessage());
            throw e;
        }
    }

    /**
     * Adds a release job.
     */
    @PostMapping("/addRelease/{dataflowId}/{dataProviderId}/{restrictFromPublic}/{validate}/{creator}")
    public void addReleaseJob(@PathVariable("dataflowId") Long dataflowId, @PathVariable("dataProviderId") Long dataProviderId, @PathVariable("restrictFromPublic") Boolean restrictFromPublic,
                              @PathVariable("validate") Boolean validate, @PathVariable("creator") String creator){
        try {
            LOG.info("Adding release job for dataflowId={}, dataProviderId={}, restrictFromPublic={}, validate={} and creator={}", dataflowId, dataProviderId, restrictFromPublic, validate, creator);
            jobService.addReleaseJob(dataflowId, dataProviderId, restrictFromPublic, validate, creator);
        } catch (Exception e){
            LOG.error("Unexpected error! release job for dataflowId={}, dataProviderId={}, restrictFromPublic={}, validate={} and creator={}. Message: {}", dataflowId, dataProviderId, restrictFromPublic, validate, creator, e.getMessage());
            throw e;
        }
    }

    /**
     * Updates a job status by process id
     */
    @PostMapping(value = "/updateStatus/{status}/{processId}")
    public void updateStatusByProcessId(@PathVariable("status") JobStatusEnum status, @PathVariable("processId") String processId){
        try {
            LOG.info("Updating status of job with processId {} to status {}", processId, status);
            jobService.updateJobStatusByProcessId(status, processId);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not update status {} for processId {}. Message: {}", status, processId, e.getMessage());
            throw e;
        }
    }

    /**
     * Updates job's status
     */
    @PostMapping(value = "/updateJobStatus/{id}/{status}/{processId}")
    public void updateJobStatus(@PathVariable("id") Long jobId, @PathVariable("status") JobStatusEnum status, @PathVariable("processId") String processId){
        try {
            LOG.info("Updating job to in progress for id {} and processId {}", jobId, processId);
            jobService.updateJobStatus(jobId, status, processId);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not update job to in progress for id {} processId {} and status {}. Message: {}", jobId, processId, status.getValue(), e.getMessage());
            throw e;
        }
    }

}
