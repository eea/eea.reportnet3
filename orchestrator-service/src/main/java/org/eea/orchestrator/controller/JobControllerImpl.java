package org.eea.orchestrator.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.swagger.annotations.*;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
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

    /** The dataset metabase controller zuul */
    @Autowired
    private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

    /** The valid columns. */
    List<String> validColumns = Arrays.asList("jobId", "creatorUsername", "jobType", "dataflowId", "providerId", "datasetId",
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
            @RequestParam(value = "dataflowId", required = false) Long dataflowId,
            @RequestParam(value = "providerId", required = false) Long providerId,
            @RequestParam(value = "datasetId", required = false) Long datasetId,
            @RequestParam(value = "creatorUsername", required = false) String creatorUsername,
            @RequestParam(value = "jobStatus", required = false) String jobStatuses){
        try {

            Pageable pageable = PageRequest.of(pageNum, pageSize);
            if (!validColumns.contains(sortedColumn)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong sorting header provided.");
            }
            return jobService.getJobs(pageable, asc, sortedColumn, jobId, jobTypes, dataflowId, providerId, datasetId, creatorUsername, jobStatuses);
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
            DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("dataflowId", dataset.getDataflowId());
            if (dataset.getDataProviderId()!=null) {
                parameters.put("dataProviderId", dataset.getDataProviderId());
            }
            parameters.put("datasetId", datasetId);
            parameters.put("released", released);
            JobStatusEnum statusToInsert = jobService.checkEligibilityOfJob(JobTypeEnum.VALIDATION.toString(), null, null, Arrays.asList(datasetId), false);
            LOG.info("Adding validation job for datasetId {} and released {} for creator {} with status {}", datasetId, released, username, statusToInsert);
            jobService.addValidationJob(dataset.getDataflowId(), dataset.getDataProviderId(), datasetId, parameters, username, statusToInsert);
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

        List<Long> datasetIds = dataSetMetabaseControllerZuul.getDatasetIdsByDataflowIdAndDataProviderId(dataflowId, dataProviderId);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dataflowId", dataflowId);
        parameters.put("dataProviderId", dataProviderId);
        parameters.put("restrictFromPublic", restrictFromPublic);
        parameters.put("validate", validate);
        parameters.put("datasetId", datasetIds);
        JobStatusEnum statusToInsert = jobService.checkEligibilityOfJob(JobTypeEnum.RELEASE.toString(), dataflowId, dataProviderId, datasetIds, true);

        LOG.info("Adding release job for dataflowId={}, dataProviderId={}, restrictFromPublic={}, validate={} and creator={} with status {}", dataflowId, dataProviderId, restrictFromPublic, validate, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
        jobService.addReleaseJob(dataflowId, dataProviderId, parameters, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
        LOG.info("Successfully added release job for dataflowId={}, dataProviderId={}, restrictFromPublic={}, validate={} and creator={} with status {}", dataflowId, dataProviderId, restrictFromPublic, validate, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
        if (statusToInsert == JobStatusEnum.REFUSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DUPLICATE_RELEASE_JOB);
        }
    }

    @Override
    @HystrixCommand(commandProperties = {@HystrixProperty(
            name = "execution.isolation.thread.timeoutInMilliseconds", value = "300000")})
    @PostMapping(value = "/addImport/{datasetId}")
    @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_LEAD_REPORTER') OR hasAnyRole('ADMIN')")
    @ApiOperation(value = "Import file", hidden = true)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully create"),
            @ApiResponse(code = 400, message = "Execution error"),
            @ApiResponse(code = 412, message = "Dataflow not releasable")})
    public Long addImportJob (@ApiParam(type = "Long", value = "Dataset id", example = "0")
            @PathVariable("datasetId") Long datasetId,
            @ApiParam(type = "Long", value = "Dataflow id",
                    example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
            @ApiParam(type = "Long", value = "Provider id",
                    example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
            @ApiParam(type = "String", value = "Table schema id",
                    example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
                    required = false) String tableSchemaId,
            @ApiParam(value = "File to upload") @RequestParam("fileName") String fileName,
            @ApiParam(type = "boolean", value = "Replace current data",
                    example = "true") @RequestParam(value = "replace", required = false) boolean replace,
            @ApiParam(type = "Long", value = "Integration id", example = "0") @RequestParam(
                    value = "integrationId", required = false) Long integrationId,
            @ApiParam(type = "String", value = "File delimiter",
            example = ",") @RequestParam(value = "delimiter", required = false) String delimiter) {

        ThreadPropertiesManager.setVariable("user",
                SecurityContextHolder.getContext().getAuthentication().getName());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dataflowId", dataflowId);
        parameters.put("datasetId", datasetId);
        parameters.put("dataProviderId", providerId);
        parameters.put("tableSchemaId", tableSchemaId);
        parameters.put("fileName", fileName);
        parameters.put("dataProviderId", providerId);
        parameters.put("replace", replace);
        parameters.put("integrationId", integrationId);
        parameters.put("delimiter", delimiter);
        JobStatusEnum statusToInsert = JobStatusEnum.IN_PROGRESS;

        LOG.info("Adding import job for dataflowId={}, datasetId={}, providerId={}, tableSchemaId={}, replace={}, integrationId={} and creator={}", dataflowId, datasetId, providerId, tableSchemaId, replace, integrationId, SecurityContextHolder.getContext().getAuthentication().getName());
        Long jobId = jobService.addImportJob(dataflowId, providerId, datasetId, parameters, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
        LOG.info("Successfully added import job for dataflowId={}, datasetId={}, providerId={}, tableSchemaId={}, replace={}, integrationId={} and creator={}", dataflowId, datasetId, providerId, tableSchemaId, replace, integrationId, SecurityContextHolder.getContext().getAuthentication().getName());
        return jobId;
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
     * @param datasets
     * @return
     */
    @Override
    @GetMapping(value = "/checkEligibility")
    public JobStatusEnum checkEligibilityOfJob(@RequestParam("jobType") String jobType, @RequestParam("release") boolean release, @RequestParam("dataflowId") Long dataflowId, @RequestParam("dataProviderID") Long dataProviderId, @RequestParam("datasets") List<Long> datasets) {
        return jobService.checkEligibilityOfJob(jobType, dataflowId, dataProviderId, datasets, release);
    }

    /**
     * Finds job by id
     * @param jobId
     * @return
     */
    @GetMapping(value = "/findJobById/{jobId}")
    public JobVO findJobById(@PathVariable("jobId") Long jobId) {
        return jobService.findById(jobId);
    }
}






