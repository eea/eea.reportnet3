package org.eea.orchestrator.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.*;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JobVO> getAllJobs(){
        try {
            return jobService.getAllJobs();
        } catch (Exception e){
            LOG.error("Unexpected error! Could not retrieve all jobs");
            throw e;
        }
    }

    @Override
    @GetMapping("/{status}")
    public List<JobVO> getJobsByStatus(@PathVariable("status") JobStatusEnum status){
        try{
            LOG.info("Retrieving jobs for status {}", status.getValue());
            return jobService.getJobsByStatus(status);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not retrieve jobs that have status {}. Message: {}", status.getValue(), e.getMessage());
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
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("datasetId", datasetId);
            parameters.put("released", released);
            JobStatusEnum statusToInsert = jobService.checkEligibilityOfJob(JobTypeEnum.VALIDATION, parameters);
            LOG.info("Adding validation job for datasetId {} and released {} for creator {} with status {}", datasetId, released, username, statusToInsert);
            jobService.addValidationJob(parameters, username, statusToInsert);
            LOG.info("Successfully added validation job for datasetId {}, released {} and creator {} with status {}", datasetId, released, username, statusToInsert);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not add validation job for datasetId {}, released {} and creator {}. Message: {}", datasetId, released, username, e.getMessage());
            throw e;
        }
    }

    /**
     * Adds a release job.
     */
    @Override
    @LockMethod(removeWhenFinish = false)
    @HystrixCommand
    @PostMapping(value = "/addRelease/dataflow/{dataflowId}/dataProvider/{dataProviderId}/release",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_LEAD_REPORTER') OR hasAnyRole('ADMIN')")
    @ApiOperation(value = "Create release snapshots", hidden = true)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully create"),
            @ApiResponse(code = 400, message = "Execution error"),
            @ApiResponse(code = 412, message = "Dataflow not releasable")})
    public void addReleaseJob (@ApiParam(type = "Long", value = "Dataflow Id", example = "0") @PathVariable(value = "dataflowId", required = true) Long dataflowId,
        @ApiParam(type = "Long", value = "Provider Id", example = "0") @PathVariable(value = "dataProviderId", required = true) Long dataProviderId,
        @ApiParam(type = "boolean", value = "Restric from public", example = "true") @RequestParam(name = "restrictFromPublic", required = true, defaultValue = "false") boolean restrictFromPublic,
        @ApiParam(type = "boolean", value = "Execute validations", example = "true") @RequestParam(name = "validate", required = false, defaultValue = "true") boolean validate) {

        ThreadPropertiesManager.setVariable("user",
                SecurityContextHolder.getContext().getAuthentication().getName());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dataflowId", dataflowId);
        parameters.put("dataProviderId", dataProviderId);
        parameters.put("restrictFromPublic", restrictFromPublic);
        parameters.put("validate", validate);
        JobStatusEnum statusToInsert = jobService.checkEligibilityOfJob(JobTypeEnum.RELEASE, parameters);

        LOG.info("Adding release job for dataflowId={}, dataProviderId={}, restrictFromPublic={}, validate={} and creator={} with status {}", dataflowId, dataProviderId, restrictFromPublic, validate, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
        jobService.addReleaseJob(parameters, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
        LOG.info("Successfully added release job for dataflowId={}, dataProviderId={}, restrictFromPublic={}, validate={} and creator={} with status {}", dataflowId, dataProviderId, restrictFromPublic, validate, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
        if (statusToInsert == JobStatusEnum.ABORTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DUPLICATE_RELEASE_JOB);
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
            LOG.info("Updating job with id {} and processId {} to status {}", jobId, processId, status.getValue());
            jobService.updateJobStatus(jobId, status, processId);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not update job to in progress for id {} processId {} and status {}. Message: {}", jobId, processId, status.getValue(), e.getMessage());
            throw e;
        }
    }

}
