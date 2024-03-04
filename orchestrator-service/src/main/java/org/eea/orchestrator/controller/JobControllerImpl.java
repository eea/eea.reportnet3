package org.eea.orchestrator.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.swagger.annotations.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.JobsVO;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.kafka.domain.EventType;
import org.eea.lock.annotation.LockMethod;
import org.eea.orchestrator.service.JobService;
import org.eea.orchestrator.utils.JobUtils;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
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

    @Autowired
    private JobUtils jobUtils;

    /** The valid columns. */
    List<String> validColumns = Arrays.asList("jobId", "creatorUsername", "jobType", "dataflowId", "providerId", "datasetId",
            "jobStatus", "dateAdded", "dateStatusChanged", "fmeJobId", "dataflowName", "datasetName");

    private static final String FILE_PATTERN_NAME_V2 = "etlExport_%s";


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
            @RequestParam(value = "dataflowName", required = false) String dataflowName,
            @RequestParam(value = "providerId", required = false) Long providerId,
            @RequestParam(value = "datasetId", required = false) Long datasetId,
            @RequestParam(value = "datasetName", required = false) String datasetName,
            @RequestParam(value = "creatorUsername", required = false) String creatorUsername,
            @RequestParam(value = "jobStatus", required = false) String jobStatuses){
        try {

            Pageable pageable = PageRequest.of(pageNum, pageSize);
            if (!validColumns.contains(sortedColumn)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong sorting header provided.");
            }
            return jobService.getJobs(pageable, asc, sortedColumn, jobId, jobTypes, dataflowId, dataflowName, providerId, datasetId, datasetName, creatorUsername, jobStatuses);
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
                                 @ApiParam(value = "Is the dataset released?", example = "true", required = false) @RequestParam(value = "released", required = false) boolean released,
                                 @RequestParam(value = "createParquetWithSQL", required = false) boolean createParquetWithSQL) {
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
            parameters.put("createParquetWithSQL", createParquetWithSQL);
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
        @ApiParam(type = "boolean", value = "Execute validations", example = "true") @RequestParam(name = "validate", required = false, defaultValue = "true") boolean validate,
        @RequestParam(name = "silentRelease", required = false, defaultValue = "false") boolean silentRelease, @RequestParam(value = "createParquetWithSQL", required = false) boolean createParquetWithSQL) {

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
            parameters.put("silentRelease", silentRelease);
            String userId = ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails()).get(AuthenticationDetails.USER_ID);
            parameters.put("userId", userId);
            parameters.put("createParquetWithSQL", createParquetWithSQL);
            JobStatusEnum statusToInsert = jobService.checkEligibilityOfJob(JobTypeEnum.RELEASE.toString(), dataflowId, dataProviderId, datasetIds, true);

            LOG.info("Adding release job for dataflowId={}, dataProviderId={}, restrictFromPublic={}, validate={} and creator={} with status {}", dataflowId, dataProviderId, restrictFromPublic, validate, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
            Long jobId = jobService.addJob(dataflowId, dataProviderId, null, parameters, JobTypeEnum.VALIDATION, statusToInsert, true, null, dataflowName, null);
            LOG.info("Successfully added release job for dataflowId={}, dataProviderId={}, restrictFromPublic={}, validate={} and creator={} with status {}", dataflowId, dataProviderId, restrictFromPublic, validate, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
            if (statusToInsert == JobStatusEnum.REFUSED) {
                //send Refused notification
                LOG.info("Added release job with id {} for dataflowId {} and providerId {} with status REFUSED", jobId, dataflowId, dataProviderId);
                if(!silentRelease){
                    jobService.releaseReleaseRefusedNotification(jobId, username, dataflowId, dataProviderId);
                }
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
            @ApiParam(value = "File to upload") @RequestParam(value = "fileName", required = false) String fileName,
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
        parameters.put("fmeCallback", false);
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

    @Override
    @HystrixCommand(commandProperties = {@HystrixProperty(
            name = "execution.isolation.thread.timeoutInMilliseconds", value = "300000")})
    @PostMapping(value = "/addFileExport/{datasetId}")
    @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','EUDATASET_STEWARD','DATACOLLECTION_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','DATACOLLECTION_CUSTODIAN','DATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','TESTDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT')")
    @ApiOperation(value = "Export data by dataset id",
            notes = "Allowed roles: \n\n Reporting dataset: CUSTODIAN, STEWARD, OBSERVER, REPORTER WRITE, REPORTER READ, LEAD REPORTER, STEWARD SUPPORT \n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE, EDITOR READ\n\n EU dataset: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT\n\n Data collection: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully exported"),
            @ApiResponse(code = 500, message = "Error exporting data"),
            @ApiResponse(code = 403, message = "Error dataset not belong dataflow")})
    public Long addFileExportJob (@ApiParam(type = "Long", value = "Dataset id",
                                              example = "0") @PathVariable("datasetId") Long datasetId,
                                  @ApiParam(type = "Long", value = "Dataflow id",
                                          example = "0") @RequestParam("dataflowId") Long dataflowId,
                                  @ApiParam(type = "Long", value = "Provider id",
                                          example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
                                  @ApiParam(type = "String", value = "Table schema id",
                                          example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
                                          required = false) String tableSchemaId,
                                  @ApiParam(type = "Integer", value = "Limit", example = "0") @RequestParam(value = "limit", required = false) Integer limit,
                                  @ApiParam(type = "Integer", value = "Offset", example = "0") @RequestParam(value = "offset", required = false,
                                          defaultValue = "0") Integer offset,
                                  @ApiParam(type = "String", value = "Filter value", example = "value") @RequestParam(
                                          value = "filterValue", required = false) String filterValue,
                                  @ApiParam(type = "String", value = "Filter column name", example = "column") @RequestParam(
                                          value = "columnName", required = false) String columnName,
                                  @ApiParam(type = "String", value = "Data provider codes", example = "BE,DK") @RequestParam(
                                          value = "dataProviderCodes", required = false) String dataProviderCodes) {

        ThreadPropertiesManager.setVariable("user", SecurityContextHolder.getContext().getAuthentication().getName());
        String userId = ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails()).get(AuthenticationDetails.USER_ID);


        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dataflowId", dataflowId);
        parameters.put("datasetId", datasetId);
        parameters.put("dataProviderId", providerId);
        parameters.put("tableSchemaId", tableSchemaId);
        parameters.put("limit", limit);
        parameters.put("offset", offset);
        parameters.put("filterValue", filterValue);
        parameters.put("columnName", columnName);
        parameters.put("dataProviderCodes", dataProviderCodes);
        parameters.put("userId", userId);

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

        JobStatusEnum statusToInsert = jobService.checkEligibilityOfJob(JobTypeEnum.FILE_EXPORT.toString(), dataflowId, providerId, Arrays.asList(datasetId), false);

        LOG.info("Adding file export job for dataflowId={}, datasetId={}, providerId={}, tableSchemaId={} and creator={} with status {}", dataflowId, datasetId, providerId, tableSchemaId, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert.getValue());
        Long jobId = jobService.addJob(dataflowId, providerId, datasetId, parameters, JobTypeEnum.FILE_EXPORT, statusToInsert, false, null, dataflowName, datasetName);
        LOG.info("Successfully added file export job for dataflowId={}, datasetId={}, providerId={}, tableSchemaId={} and creator={} with status {}", dataflowId, datasetId, providerId, tableSchemaId, SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert.getValue());
        return jobId;
    }

    /**
     * Updates job's status
     */
    @PostMapping(value = "/private/updateJobStatus/{id}/{status}")
    public void updateJobStatus(@PathVariable("id") Long jobId, @PathVariable("status") JobStatusEnum status){
        try {
            LOG.info("Updating job with id {} to status {}", jobId, status.getValue());
            jobService.updateJobStatus(jobId, status);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not update job to in progress for id {} and status {}.", jobId, status.getValue(), e);
            throw e;
        }
    }

    @PostMapping(value = "/private/updateFmeJobId/{jobId}/{fmeJobId}")
    public void updateFmeJobId(@PathVariable("jobId") Long jobId, @PathVariable("fmeJobId") String fmeJobId) {
        jobService.updateFmeJobId(jobId,fmeJobId);
    }

    /**
     * Saves job
     * @param jobVO
     * @return
     */
    @Override
    @PostMapping(value = "/private/saveJob")
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

    /**
     * Cancels job
     * @param jobId
     */
    @Override
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping(value = "/cancelJob/{jobId}")
    public void cancelJob(@PathVariable("jobId") Long jobId) throws Exception {
        try {
            jobService.cancelJob(jobId);
        } catch (Exception e) {
            LOG.error("Error while cancelling job with id {}, error is {}", jobId, e.getMessage());
            throw e;
        }
    }

    /**
     * Updates job info value
     * @param jobId
     * @param jobInfo
     * @param lineNumber
     */
    @Override
    @PostMapping(value = "/private/updateJobInfo/{jobId}")
    public void updateJobInfo(@PathVariable("jobId") Long jobId,  @RequestParam(value = "jobInfo") JobInfoEnum jobInfo,
                              @RequestParam(value = "lineNumber", required = false) Integer lineNumber){
        try {
            jobService.updateJobInfo(jobId, jobInfo, lineNumber);
        } catch (Exception e) {
            LOG.error("Error while updating job info for jobId {} and jobInfo {}", jobId, jobInfo.getValue(lineNumber), e);
            throw e;
        }
    }

    /**
     * Update the fmeCallback job parameter
     *
     * @param fmeJobId the fme job id
     * @param fmeCallback true or false
     * @return
     */
    @Override
    @PostMapping(value = "/private/updateFmeCallbackJobParameter/{fmeJobId}")
    public void updateFmeCallbackJobParameter(@PathVariable("fmeJobId") String fmeJobId, @RequestParam(value = "fmeCallback") Boolean fmeCallback){
        jobService.updateFmeCallbackJobParameter(fmeJobId, fmeCallback);
    }

    @Override
    @GetMapping(value = "/private/pollForJobStatus/{jobId}")
    @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','EUDATASET_STEWARD','DATACOLLECTION_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','DATACOLLECTION_CUSTODIAN','DATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','TESTDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT')")
    public Map<String, Object> pollForJobStatus(@PathVariable("jobId") Long jobId,
                                                @ApiParam(type = "Long", value = "Dataset id",
                                                        example = "0") @RequestParam("datasetId") Long datasetId,
                                                @ApiParam(type = "Long", value = "Dataflow id",
                                                        example = "0") @RequestParam("dataflowId") Long dataflowId,
                                                @ApiParam(type = "Long", value = "Provider id",
                                                        example = "0") @RequestParam(value = "providerId", required = false) Long providerId){
        Map<String, Object> result = new HashMap<>();

        try {
            JobVO job = jobService.findById(jobId);
            if (job == null) {
                LOG.error("No job found for jobId {}", jobId);
                result.put("error", "Could not find job with id " + jobId);
            }
            else {
                result.put("status", job.getJobStatus().getValue());
                if (job.getJobType() == JobTypeEnum.FILE_EXPORT && job.getJobStatus() == JobStatusEnum.FINISHED) {
                    String downloadUrl = "/orchestrator/jobs/downloadEtlExportedFile/" + jobId + "?datasetId=" + datasetId + "&dataflowId=" + dataflowId;
                    if(providerId != null){
                        downloadUrl+= "&providerId=" + providerId;
                    }
                    result.put("downloadUrl", downloadUrl);
                }
            }
        }
        catch (Exception e){
            LOG.error("Unexpected error! There was an error when polling for status of jobId {}", jobId, e);
            result.put("error", "There was an error when polling for status of job " + jobId);
        }
        finally {
            return result;
        }


    }

    @Override
    @GetMapping(value = "/downloadEtlExportedFile/{jobId}")
    @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','EUDATASET_STEWARD','DATACOLLECTION_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','DATACOLLECTION_CUSTODIAN','DATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','TESTDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT')")
    public void downloadEtlExportedFile(@PathVariable("jobId") Long jobId,
                                        @ApiParam(type = "Long", value = "Dataset id",
                                                example = "0") @RequestParam("datasetId") Long datasetId,
                                        @ApiParam(type = "Long", value = "Dataflow id",
                                                example = "0") @RequestParam("dataflowId") Long dataflowId,
                                        @ApiParam(type = "Long", value = "Provider id",
                                                example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
                                        @ApiParam(value = "response") HttpServletResponse response) throws Exception {
        String fileName = String.format(FILE_PATTERN_NAME_V2, jobId) + ".zip";
        try {
            LOG.info("Downloading file generated from v3 etl export for jobId {}", jobId);
            File file = jobService.downloadEtlExportedFile(jobId, fileName);
            LOG.info("Successfully downloaded file generated from v3 etl export for jobId {}", jobId);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

            OutputStream out = response.getOutputStream();
            try (FileInputStream in = new FileInputStream(file)) {
                // copy from in to out
                IOUtils.copyLarge(in, out);
                // delete the file after downloading it ?
                //FileUtils.forceDelete(file);
            } catch (Exception e) {
                LOG.error("Unexpected error! Error in copying large etl exported file {} for jobId {}. Message: {}", fileName, jobId, e.getMessage());
                throw e;
            }
            finally {
                out.close();
            }
        }
        catch (Exception e) {
            LOG.error("Unexpected error! Error downloading file {} from v3 etl export for jobId {} Message: {}", fileName, jobId, e.getMessage());
            throw e;
        }
    }



    /**
     * Sends a fme import failed notification
     *
     * @param jobVO the job object
     * @return
     */
    @Override
    @PostMapping(value = "/private/sendFmeImportFailedNotification")
    public void sendFmeImportFailedNotification(@RequestBody JobVO jobVO){
        jobUtils.sendKafkaImportNotification(jobVO, EventType.FME_IMPORT_JOB_FAILED_EVENT, "Fme job failed");
        LOG.info("Sent notification FME_IMPORT_JOB_FAILED_EVENT for jobId {} and fmeJobId {}", jobVO.getId(), jobVO.getFmeJobId());
    }

    /**
     * Finds provider id by job id
     * @param jobId
     * @return
     */
    @GetMapping(value = "/findProviderIdById/{jobId}")
    public Long findProviderIdById(@PathVariable("jobId") Long jobId) {
        return jobService.findProviderIdById(jobId);
    }
}






