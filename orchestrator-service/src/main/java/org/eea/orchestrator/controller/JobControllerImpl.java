package org.eea.orchestrator.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.*;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.orchestrator.JobController;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.JobsVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.lock.annotation.LockMethod;
import org.eea.orchestrator.service.JobService;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Arrays;
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

    /** The valid columns. */
    List<String> validColumns = Arrays.asList("jobId", "processId", "creatorUsername", "jobType",
            "jobStatus", "dateAdded", "dateStatusChanged");

    @Override
    @HystrixCommand
    @GetMapping
    @ApiOperation(value = "Gets the jobs", response = JobVO.class, responseContainer = "List", hidden = false)
    @PreAuthorize("hasAnyRole('ADMIN','DATA_CUSTODIAN')")
    public JobsVO getJobs(
            @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
            @RequestParam(value = "asc", defaultValue = "true") boolean asc,
            @RequestParam(value = "sortedColumn", defaultValue = "jobId") String sortedColumn,
            @RequestParam(value = "jobId", required = false) Long jobId,
            @RequestParam(value = "jobType", required = false) String jobTypes,
            @RequestParam(value = "creatorUsername", required = false) String creatorUsername,
            @RequestParam(value = "jobStatus", required = false) String jobStatuses){
        try {

            Pageable pageable = PageRequest.of(pageNum, pageSize);
            if (!validColumns.contains(sortedColumn)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong sorting header provided.");
            }
            return jobService.getJobs(pageable, asc, sortedColumn, jobId, jobTypes, creatorUsername, jobStatuses);
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
            JobStatusEnum statusToInsert = jobService.checkEligibilityOfJob(JobTypeEnum.VALIDATION.toString(), false, parameters);
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
        JobStatusEnum statusToInsert = jobService.checkEligibilityOfJob(JobTypeEnum.RELEASE.toString(), true, parameters);

        LOG.info("Adding release job for dataflowId={}, dataProviderId={}, restrictFromPublic={}, validate={} and creator={} with status {}", dataflowId, dataProviderId, restrictFromPublic, validate, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
        jobService.addReleaseJob(parameters, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
        LOG.info("Successfully added release job for dataflowId={}, dataProviderId={}, restrictFromPublic={}, validate={} and creator={} with status {}", dataflowId, dataProviderId, restrictFromPublic, validate, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
        if (statusToInsert == JobStatusEnum.REFUSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DUPLICATE_RELEASE_JOB);
        }
    }

    /**
     * Updates job's status
     */
    @PostMapping(value = "/updateJobStatus/{id}/{status}")
    public void updateJobStatus(@PathVariable("id") Long jobId, @PathVariable("status") JobStatusEnum status){
        try {
            LOG.info("Updating job with id {} to status {}", jobId, status.getValue());
            jobService.updateJobStatus(jobId, status);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not update job to in progress for id {} and status {}. Message: {}", jobId, status.getValue(), e.getMessage());
            throw e;
        }
    }

    /**
     * Saves job
     * @param jobVO
     * @return
     */
    @Override
    @PostMapping(value = "/saveJob")
    public JobVO save(@RequestBody JobVO jobVO) {
       return jobService.save(jobVO);
    }

    /**
     *
     * @param jobType
     * @param release
     * @param dataflowId
     * @param dataProviderId
     * @return
     */
    @Override
    @GetMapping(value = "/checkEligibility")
    public JobStatusEnum checkEligibilityOfJob(@RequestParam("jobType") String jobType, @RequestParam("release") boolean release, @RequestParam("dataflowId") Long dataflowId, @RequestParam("dataProviderID") Long dataProviderId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dataflowId", dataflowId);
        parameters.put("dataProviderId", dataProviderId);
        return jobService.checkEligibilityOfJob(jobType, release, parameters);
    }

}
