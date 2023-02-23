package org.eea.orchestrator.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.swagger.annotations.*;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.JobsVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.lock.annotation.LockMethod;
import org.eea.orchestrator.service.JobService;
import org.eea.security.jwt.utils.AuthenticationDetails;
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

    /** The dataset metabase controller zuul */
    @Autowired
    private DataFlowControllerZuul dataFlowControllerZuul;

    /** The valid columns. */
    List<String> validColumns = Arrays.asList("jobId", "creatorUsername", "jobType", "dataflowId", "providerId", "datasetId",
            "jobStatus", "dateAdded", "dateStatusChanged", "fmeJobId", "dataflowName", "datasetName");

    @Override
    @HystrixCommand
    @GetMapping
    @ApiOperation(value = "Gets the jobs", response = JobVO.class, responseContainer = "List", hidden = false)
    @PreAuthorize("isAuthenticated()")
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
            LOG.error("Unexpected error! Could not retrieve jobs that have status {}. ", status.getValue(), e);
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
            String dataflowName = null;
            try{
                dataflowName = dataFlowControllerZuul.findDataflowNameById(dataset.getDataflowId());
            }
            catch (Exception e) {
                LOG.error("Error when trying to receive dataflow name for dataflowId {} ", dataset.getDataflowId(), e);
            }

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("dataflowId", dataset.getDataflowId());
            Long dataProvider = null;
            if (dataset.getDataProviderId()!=null) {
                dataProvider = dataset.getDataProviderId();
                parameters.put("dataProviderId", dataProvider);
            }
            parameters.put("datasetId", datasetId);
            parameters.put("released", released);
            String userId = ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails()).get(AuthenticationDetails.USER_ID);
            parameters.put("userId", userId);
            JobStatusEnum statusToInsert = jobService.checkEligibilityOfJob(JobTypeEnum.VALIDATION.toString(), dataset.getDataflowId(), dataProvider, Arrays.asList(datasetId), false);
            LOG.info("Adding validation job for datasetId {} and released {} for creator {} with status {}", datasetId, released, username, statusToInsert);
            Long jobId = jobService.addJob(dataset.getDataflowId(), dataProvider, datasetId, parameters, JobTypeEnum.VALIDATION, statusToInsert, released, null, dataflowName, dataset.getDataSetName());
            LOG.info("Successfully added validation job for datasetId {}, released {} and creator {} with status {}", datasetId, released, username, statusToInsert);
            if (statusToInsert == JobStatusEnum.REFUSED) {
                //send Refused notification
                LOG.info("Added validation job with id {} for datasetId {} with status REFUSED", jobId, datasetId);
                jobService.releaseValidationRefusedNotification(jobId, username, datasetId);
                throw new ResponseStatusException(HttpStatus.LOCKED, EEAErrorMessage.DUPLICATE_JOB);
            }
        } catch (Exception e){
            LOG.error("Unexpected error! Could not add validation job for datasetId {}, released {} and creator {}. Error: {}", datasetId, released, username, e);
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

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ThreadPropertiesManager.setVariable("user",
                SecurityContextHolder.getContext().getAuthentication().getName());

        try {
            String dataflowName = null;
            try{
                dataflowName = dataFlowControllerZuul.findDataflowNameById(dataflowId);
            }
            catch (Exception e) {
                LOG.error("Error when trying to receive dataflow name for dataflowId {} ", dataflowId, e);
            }

            List<Long> datasetIds = dataSetMetabaseControllerZuul.getDatasetIdsByDataflowIdAndDataProviderId(dataflowId, dataProviderId);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("dataflowId", dataflowId);
            parameters.put("dataProviderId", dataProviderId);
            parameters.put("restrictFromPublic", restrictFromPublic);
            parameters.put("validate", validate);
            parameters.put("datasetId", datasetIds);
            String userId = ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails()).get(AuthenticationDetails.USER_ID);
            parameters.put("userId", userId);
            JobStatusEnum statusToInsert = jobService.checkEligibilityOfJob(JobTypeEnum.RELEASE.toString(), dataflowId, dataProviderId, datasetIds, true);

            LOG.info("Adding release job for dataflowId={}, dataProviderId={}, restrictFromPublic={}, validate={} and creator={} with status {}", dataflowId, dataProviderId, restrictFromPublic, validate, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
            Long jobId = jobService.addJob(dataflowId, dataProviderId, null, parameters, JobTypeEnum.VALIDATION, statusToInsert, true, null, dataflowName, null);
            LOG.info("Successfully added release job for dataflowId={}, dataProviderId={}, restrictFromPublic={}, validate={} and creator={} with status {}", dataflowId, dataProviderId, restrictFromPublic, validate, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
            if (statusToInsert == JobStatusEnum.REFUSED) {
                //send Refused notification
                LOG.info("Added release job with id {} for dataflowId {} and providerId {} with status REFUSED", jobId, dataflowId, dataProviderId);
                jobService.releaseReleaseRefusedNotification(jobId, username, dataflowId, dataProviderId);
                throw new ResponseStatusException(HttpStatus.LOCKED, EEAErrorMessage.DUPLICATE_RELEASE_JOB);
            }
        } catch (Exception e) {
            LOG.error("Unexpected error! Could not add release job for dataflowId {}, providerId {} and creator {}. Error: {}", dataflowId, dataProviderId, username, e);
            throw e;
        }
    }

    @Override
    @HystrixCommand(commandProperties = {@HystrixProperty(
            name = "execution.isolation.thread.timeoutInMilliseconds", value = "300000")})
    @PostMapping(value = "/addImport/{datasetId}")
    @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
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
            example = ",") @RequestParam(value = "delimiter", required = false) String delimiter,
                              @RequestParam(value = "jobStatus", required = false) JobStatusEnum jobStatus,
                              @ApiParam(type = "String", value = "Fme Job Id",
                                      example = ",") @RequestParam(value = "fmeJobId", required = false) String fmeJobId) {

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
        if(jobStatus != null){
            statusToInsert = jobStatus;
        }


        String dataflowName = null;
        try{
            dataflowName = dataFlowControllerZuul.findDataflowNameById(dataflowId);
        }
        catch (Exception e) {
            LOG.error("Error when trying to receive dataflow name for dataflowId {} ", dataflowId, e);
        }

        String datasetName = null;
        try{
            datasetName = dataSetMetabaseControllerZuul.findDatasetNameById(datasetId);
        }
        catch (Exception e) {
            LOG.error("Error when trying to receive dataset name for datasetId {} ", datasetId, e);
        }

        LOG.info("Adding import job for dataflowId={}, datasetId={}, providerId={}, tableSchemaId={}, replace={}, integrationId={} and creator={}", dataflowId, datasetId, providerId, tableSchemaId, replace, integrationId, SecurityContextHolder.getContext().getAuthentication().getName());
        Long jobId = jobService.addJob(dataflowId, providerId, datasetId, parameters, JobTypeEnum.IMPORT, statusToInsert, false, fmeJobId, dataflowName, datasetName);
        LOG.info("Successfully added import job for dataflowId={}, datasetId={}, providerId={}, tableSchemaId={}, replace={}, integrationId={} and creator={}", dataflowId, datasetId, providerId, tableSchemaId, replace, integrationId, SecurityContextHolder.getContext().getAuthentication().getName());
        return jobId;
    }

    /**
     * Adds a release job.
     */
    @Override
    @HystrixCommand
    @PostMapping(value = "/addCopyToEUDataset/populateData/dataflow/{dataflowId}")
    @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN')  OR (checkApiKey(#dataflowId,null,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN'))")
    @ApiOperation(value = "Copy data collections data to EU datasets by dataflow id",
            notes = "Allowed roles: CUSTODIAN, STEWARD")
    public void addCopyToEUDatasetJob(@ApiParam(type = "Long", value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ThreadPropertiesManager.setVariable("user", username);

        try {

            String dataflowName = null;
            try{
                dataflowName = dataFlowControllerZuul.findDataflowNameById(dataflowId);
            }
            catch (Exception e) {
                LOG.error("Error when trying to receive dataflow name for dataflowId {} ", dataflowId, e);
            }

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("dataflowId", dataflowId);
            String userId = ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails()).get(AuthenticationDetails.USER_ID);
            parameters.put("userId", userId);
            JobStatusEnum statusToInsert = jobService.checkEligibilityOfJob(JobTypeEnum.COPY_TO_EU_DATASET.toString(), dataflowId, null, null, false);
            LOG.info("Adding copy to eudataset job for dataflowId={}", dataflowId);
            Long jobId = jobService.addJob(dataflowId, null, null, parameters, JobTypeEnum.COPY_TO_EU_DATASET, statusToInsert, false, null, dataflowName, null);
            LOG.info("Successfully added copy to eudataset job with id {} for dataflowId={}", jobId, dataflowId);
            if (statusToInsert == JobStatusEnum.REFUSED) {
                //send Refused notification
                LOG.info("Added copyToEuDataset job with id {} for dataflowId {} with status REFUSED", jobId, dataflowId);
                jobService.releaseCopyToEuDatasetRefusedNotification(jobId, username, dataflowId);
                throw new ResponseStatusException(HttpStatus.LOCKED, EEAErrorMessage.DUPLICATE_COPY_TO_EU_DATASET_JOB);
            }
        } catch (Exception e) {
            LOG.error("Unexpected error! Could not add copy to eudataset job for dataflowId {}, creator {}. Error: {}", dataflowId, username, e);
            throw e;
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
            LOG.error("Unexpected error! Could not update job to in progress for id {} and status {}.", jobId, status.getValue(), e);
            throw e;
        }
    }

    @PostMapping(value = "/updateFmeJobId/{jobId}/{fmeJobId}")
    public void updateFmeJobId(@PathVariable("jobId") Long jobId, @PathVariable("fmeJobId") String fmeJobId) {
        jobService.updateFmeJobId(jobId,fmeJobId);
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
    public JobStatusEnum checkEligibilityOfJob(@RequestParam("jobType") String jobType, @RequestParam("release") boolean release,
                                               @RequestParam("dataflowId") Long dataflowId, @RequestParam(value="dataProviderID", required = false) Long dataProviderId, @RequestParam("datasets") List<Long> datasets) {
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

    @GetMapping(value = "/findJobByFmeJobId/{fmeJobId}")
    public JobVO findJobByFmeJobId(String fmeJobId) {
        return jobService.findByFmeJobId(fmeJobId);
    }

    /**
     * Update job, process and task status
     *
     * @param jobId the job id
     * @param jobStatus the job's status
     * @param processStatus the process's status
     * @return
     */
    @PostMapping(value = "/private/updateJobAndProcess/{id}")
    public void updateJobAndProcess(@PathVariable("id") Long jobId, @RequestParam(value = "jobStatus") JobStatusEnum jobStatus,
                                  @RequestParam(value = "processStatus") ProcessStatusEnum processStatus){
        try {
            jobService.updateJobAndProcess(jobId, jobStatus, processStatus);
        }
        catch (Exception e){
            LOG.error("Could not update job and process for job id {} with jobStatus {} and processStatus {}", jobId, jobStatus.getValue(), processStatus.toString(), e);
            throw e;
        }
    }
}






