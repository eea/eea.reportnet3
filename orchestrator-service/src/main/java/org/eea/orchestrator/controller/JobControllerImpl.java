package org.eea.orchestrator.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.orchestrator.JobController;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.lock.annotation.LockMethod;
import org.eea.orchestrator.service.JobService;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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
    @Override
    @PutMapping(value = "/addValidationJob/{datasetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')  OR hasAnyRole('ADMIN')")
    @LockMethod(removeWhenFinish = false)
    @ApiOperation(value = "Validates dataset data for a given dataset id", hidden = true)
    @ApiResponse(code = 400, message = EEAErrorMessage.DATASET_INCORRECT_ID)
    public void addValidationJob(@ApiParam(value = "Dataset id whose data is going to be validated", example = "15") @PathVariable("datasetId") Long datasetId,
                                 @ApiParam(value = "Is the dataset released?", example = "true", required = false) @RequestParam(value = "released", required = false) boolean released) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // Set the user name on the thread
        ThreadPropertiesManager.setVariable("user", SecurityContextHolder.getContext().getAuthentication().getName());
        if (datasetId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DATASET_INCORRECT_ID);
        }
        try {
            Boolean isJobEligible = jobService.checkEligibilityOfJob(JobTypeEnum.VALIDATION, datasetId);
            if(!isJobEligible){
                //TODO do something else
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DATASET_INCORRECT_ID);
            }
            LOG.info("Adding validation job for datasetId {} and released {} for creator {}", datasetId, released, username);
            jobService.addValidationJob(datasetId, released, username);
            LOG.info("Successfully added validation job for datasetId {}, released {} and creator {}", datasetId, released, username);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not add validation job for datasetId {}, released {} and creator {}. Message: {}", datasetId, released, username, e.getMessage());
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
