package org.eea.interfaces.controller.orchestrator;

import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.JobsVO;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.orchestrator.JobCanceledValidationTasksVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The Interface JobController. */
public interface JobController {

    @FeignClient(value = "orchestrator", contextId = "jobs", path = "/jobs")
    interface JobControllerZuul extends JobController {
    }

    /**
     * Get jobs
     * @param pageNum
     * @param pageSize
     * @param asc
     * @param sortedColumn
     * @param jobId
     * @param jobTypes
     * @param dataflowId
     * @param dataflowName
     * @param providerId
     * @param datasetId
     * @param datasetName
     * @param creatorUsername
     * @param jobStatuses
     * @return
     */
    @GetMapping()
    JobsVO getJobs(
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
            @RequestParam(value = "jobStatus", required = false) String jobStatuses);

    /**
     * Get jobs based on status
     *
     * @param status the job status
     * @return a list of job entries
     */
    @GetMapping(value = "/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    List<JobVO> getJobsByStatus(@PathVariable("status") JobStatusEnum status);

    /**
     * Adds a validation job
     *
     * @param datasetId the id of the dataset that will be validated
     * @param dataflowId the id of the dataflow
     * @param providerId the id of the provider
     * @param released the released
     * @return the jobId
     */
    @PutMapping(value = "/addValidationJob/{datasetId}")
    Long addValidationJob(@PathVariable("datasetId") Long datasetId, @RequestParam(value = "dataflowId", required = false) Long dataflowId,
                          @RequestParam(value = "providerId", required = false) Long providerId, @RequestParam(value = "released", required = false) boolean released,
                          @RequestParam(value = "createParquetWithSQL", required = false) boolean createParquetWithSQL,
                          @RequestParam(value = "validateAsProviderCode", required = false) String validateAsProviderCode);

    /**
     * Adds a release job
     *
     * @param dataflowId the id of the dataflow
     * @param dataProviderId the dataProviderId
     * @param restrictFromPublic the restrictFromPublic
     * @param validate the validate
     * @return
     */
    @PostMapping(value = "/addRelease/{dataflowId}/dataProvider/{dataProviderId}/release", produces = MediaType.APPLICATION_JSON_VALUE)
    void addReleaseJob(@PathVariable(value = "dataflowId", required = true) Long dataflowId,
                       @PathVariable(value = "dataProviderId", required = true) Long dataProviderId,
                       @RequestParam(name = "restrictFromPublic", required = true,
                               defaultValue = "false") boolean restrictFromPublic,
                       @RequestParam(name = "validate", required = false, defaultValue = "true") boolean validate,
                       @RequestParam(name = "silentRelease", required = false, defaultValue = "false") boolean silentRelease,
                       @RequestParam(value = "createParquetWithSQL", required = false) boolean createParquetWithSQL);

    /**
     * Adds an import job
     *
     * @param datasetId the id of the dataset
     * @param dataflowId the id of the dataflow
     * @param providerId the id of the provider
     * @param tableSchemaId the schema id of the table
     * @param fileName the imported file name
     * @param replace the replace
     * @param integrationId the id of the integration
     * @param delimiter the delimiter
     * @param jobStatus the status of the job
     * @param fmeJobId the fme job id
     * @param filePathInS3 the file path in s3
     * @return the job id
     */
    @PostMapping(value = "/addImport/{datasetId}")
    Long addImportJob(@PathVariable("datasetId") Long datasetId,
                      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
                       @RequestParam(value = "providerId", required = false) Long providerId,
                       @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
                       @RequestParam(value = "fileName", required = false) String fileName,
                       @RequestParam(value = "replace", required = false) boolean replace,
                       @RequestParam(value = "integrationId", required = false) Long integrationId,
                      @RequestParam(value = "preparationCode", required = false) String preparationCode,
                      @RequestParam(value = "delimiter", required = false) String delimiter,
                      @RequestParam(value = "jobStatus", required = false) JobStatusEnum jobStatus,
                      @RequestParam(value = "fmeJobId", required = false) String fmeJobId,
                      @RequestParam(value = "filePathInS3", required = false) String filePathInS3);

    /**
     * Adds an etl import job
     *
     * @param datasetId the id of the dataset
     * @param dataflowId the id of the dataflow
     * @param providerId the id of the provider
     * @param replace the status of the job
     * @param tableSchemaId the id of the provider
     * @param delimiter the status of the job
     * @param filePathInS3 the status of the job
     * @return the job id
     */
    @PostMapping(value = "/addEtlImport/{datasetId}")
    Long addEtlImportJob(@PathVariable("datasetId") Long datasetId,
                         @RequestParam(value = "dataflowId", required = false) Long dataflowId,
                         @RequestParam(value = "providerId", required = false) Long providerId,
                         @RequestParam(value = "jobStatus", required = false) JobStatusEnum jobStatus,
                         @RequestParam(value = "replace", required = false) Boolean replace,
                         @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
                         @RequestParam(value = "delimiter", required = false) String delimiter,
                         @RequestParam(value = "filePathInS3", required = false) String filePathInS3);

    /**
     * Adds a delete data job
     *
     * @param datasetId the ID of the dataset
     * @param tableSchemaId the table schema id
     * @param dataflowId the of the dataflow
     * @param providerId the id of the provider
     * @param deletePrefilledTables whether it will delete the prefilled tables or not
     * @param jobStatus the status of the job
     * @return the job id
     */
    @PostMapping(value="/addDeleteData/{datasetId}")
    Long addDeleteDataJob(@PathVariable("datasetId") Long datasetId,
                          @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
                          @RequestParam(value = "dataflowId", required = false) Long dataflowId,
                          @RequestParam(value = "providerId", required = false) Long providerId,
                          @RequestParam(value = "deletePrefilledTables", defaultValue = "false",
                                  required = false) Boolean deletePrefilledTables,
                          @RequestParam(value = "jobStatus", required = false) JobStatusEnum jobStatus);


    /**
     * Adds a copy to eu dataset job
     * @param dataflowId
     */
    @PostMapping(value = "/addCopyToEUDataset/populateData/dataflow/{dataflowId}")
    void addCopyToEUDatasetJob(@PathVariable("dataflowId") Long dataflowId);

    /**
     * Adds a file export job
     * @param dataflowId
     */
    @PostMapping(value = "/addFileExport/{datasetId}")
    Long addFileExportJob (@PathVariable("datasetId") Long datasetId,
                                  @RequestParam("dataflowId") Long dataflowId,
                                  @RequestParam(value = "providerId", required = false) Long providerId,
                                  @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
                                  @RequestParam(value = "limit", required = false) Integer limit,
                                  @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
                                  @RequestParam(value = "filterValue", required = false) String filterValue,
                                  @RequestParam(value = "columnName", required = false) String columnName,
                                  @RequestParam(value = "dataProviderCodes", required = false) String dataProviderCodes,
                                  @RequestParam(value = "exportCsv", required = false) Boolean exportCsv,
                                  @RequestParam(value = "exportParquet", required = false) Boolean exportParquet,
                                  @RequestParam(value = "includeAttachments", required = false) Boolean includeAttachments);

    /**
     * Update job's status
     *
     * @param jobId the job id
     * @param status the job's status
     * @return
     */
    @PostMapping(value = "/private/updateJobStatus/{id}/{status}")
    void updateJobStatus(@PathVariable("id") Long jobId, @PathVariable("status") JobStatusEnum status);


    @PostMapping(value = "/private/updateFmeJobId/{jobId}/{fmeJobId}")
    void updateFmeJobId(@PathVariable("jobId") Long jobId, @PathVariable("fmeJobId") String fmeJobId);

    @GetMapping(value = "/pollForJobStatus/{jobId}")
    Map<String, Object> pollForJobStatus(@PathVariable("jobId") Long jobId, @RequestParam("datasetId") Long datasetId,
                                        @RequestParam("dataflowId") Long dataflowId,
                                        @RequestParam(value = "providerId", required = false) Long providerId);

    @GetMapping(value = "/downloadEtlExportedFile/{jobId}")
    void downloadEtlExportedFile(@PathVariable("jobId") Long jobId, @RequestParam("datasetId") Long datasetId,
                                 @RequestParam("dataflowId") Long dataflowId,
                                 @RequestParam(value = "providerId", required = false) Long providerId, HttpServletResponse response) throws Exception;

    /**
     * Saves job
     * @param jobVO
     * @return
     */
    @PostMapping(value = "/private/saveJob")
    JobVO save(@RequestBody JobVO jobVO);

    /**
     *
     * @param jobType
     * @param release
     * @param dataflowId
     * @param dataProviderId
     * @return
     */
    @GetMapping(value = "/checkEligibility")
    JobStatusEnum checkEligibilityOfJob(@RequestParam("jobType") String jobType, @RequestParam("release") boolean release, @RequestParam("dataflowId") Long dataflowId,
                                        @RequestParam(value="dataProviderID", required = false) Long dataProviderId, @RequestParam("datasets") List<Long> datasets);

    /**
     * Finds job by id
     * @param jobId
     * @return
     */
    @GetMapping(value = "/findJobById/{jobId}")
    JobVO findJobById(@PathVariable("jobId") Long jobId);

    @GetMapping(value = "/findJobByFmeJobId/{fmeJobId}")
    JobVO findJobByFmeJobId(@PathVariable("fmeJobId") String fmeJobId);

    /**
     * Update job, process and task status
     *
     * @param jobId the job id
     * @param jobStatus the job's status
     * @param processStatus the process's status
     * @return
     */
    @PostMapping(value = "/private/updateJobAndProcess/{id}")
    void updateJobAndProcess(@PathVariable("id") Long jobId, @RequestParam(value = "jobStatus") JobStatusEnum jobStatus,
                             @RequestParam(value = "processStatus") ProcessStatusEnum processStatus);

    /**
     * Cancels job
     * @param jobId
     */
    @PutMapping(value = "/cancelJob/{jobId}")
    void cancelJob(@PathVariable("jobId") Long jobId, @RequestParam(value = "dataflowId") Long dataflowId, @RequestParam(value = "datasetId", required = false) Long datasetId,
                   @RequestParam(value="jobInfo", required = false) JobInfoEnum jobInfo, @RequestParam(value="jobShouldFail", required = false) Boolean jobShouldFail) throws Exception;

    /**
     * Updates job info value
     * @param jobId
     * @param jobInfo
     * @param lineNumber
     */
    @PostMapping(value = "/private/updateJobInfo/{jobId}")
    void updateJobInfo(@PathVariable("jobId") Long jobId,  @RequestParam(value = "jobInfo") JobInfoEnum jobInfo,
                       @RequestParam(value = "lineNumber") Integer lineNumber);


    /**
     * Update the fmeCallback job parameter
     *
     * @param fmeJobId the fme job id
     * @param fmeCallback true or false
     * @return
     */
    @PostMapping(value = "/private/updateFmeCallbackJobParameter/{fmeJobId}")
    void updateFmeCallbackJobParameter(@PathVariable("fmeJobId") String fmeJobId, @RequestParam(value = "fmeCallback") Boolean fmeCallback);

    /**
     * Sends a fme import failed notification
     *
     * @param jobVO the job object
     * @return
     */
    @PostMapping(value = "/private/sendFmeImportFailedNotification")
    void sendFmeImportFailedNotification(@RequestBody JobVO jobVO);

    /**
     * Sends a fme import failed no file returned notification
     *
     * @param jobVO the job object
     * @return
     */
    @PostMapping(value = "/private/sendFmeImportFailedNoFileReturnedNotification")
    void sendFmeImportFailedNoFileReturnedNotification(@RequestBody JobVO jobVO);

    /**
     * Finds provider id by job id
     * @param jobId
     * @return
     */
    @GetMapping(value = "/findProviderIdById/{jobId}")
    Long findProviderIdById(@PathVariable("jobId") Long jobId);

    /**
     * Retrieves the status of a job
     *
     * @param jobId the job id
     * @return
     */
    @GetMapping(value = "/getJobStatusByJobId/{jobId}")
    JobStatusEnum getJobStatusByJobId(@PathVariable("jobId") Long jobId);

    /**
     * fails stuck queued import job
     *
     * @param jobId the job id
     * @param error the error
     * @return
     */
    @PostMapping(value = "/handleStuckImportJob/{jobId}")
    void handleStuckImportJob(@PathVariable("jobId") Long jobId, @RequestBody String error) throws Exception;

    @GetMapping(value = "/canceledValidationTasks/{jobId}")
    JobCanceledValidationTasksVO findCanceledValidationTasksByJobId(
            @PathVariable("jobId") Long jobId,
            @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "asc", defaultValue = "true", required = false) boolean asc,
            @RequestParam(value = "sortedColumn", defaultValue = "ruleCode", required = false) String sortedColumn);

    /**
     * Checks if the process is silent release or not
     * @param processId The process id
     * @return True if is Silent release
     */
    @GetMapping(value = "/private/isSilentRelease/{processId}")
    Boolean isSilentRelease(@PathVariable("processId") String processId) ;

    /*
     * restarts import job if possible
     *
     * @param jobId the job id
     * @param sendRestartNotification
     * @return
     */
    @PostMapping(value = "/restartImportJob/{jobId}")
    void restartImportJob(@PathVariable("jobId") Long jobId, @RequestParam(value = "sendRestartNotification", defaultValue = "true", required = false) Boolean sendRestartNotification);


    /**
     * Get jobs statistics
     *
     * @return a dto with jobs statistics
     */
    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    String getJobsStatistics();

    @GetMapping(value = "/private/findActiveJobsRelatedToADatasetId/{datasetId}")
    List<JobVO> findActiveJobsRelatedToADatasetId(@PathVariable("datasetId") Long datasetId, @RequestParam(value = "dataflowId", required = false) Long dataflowId, @RequestParam(value = "providerId", required = false) Long providerId);

    /**
     * Updates job status and info value
     * @param jobId
     * @param jobStatus
     * @param jobInfo
     * @param lineNumber
     */
    @PostMapping(value = "/private/updateJobStatusAndInfo/{jobId}")
    void updateJobStatusAndInfo(@PathVariable("jobId") Long jobId, @RequestParam(value = "jobStatus") JobStatusEnum jobStatus, @RequestParam(value = "jobInfo") JobInfoEnum jobInfo,
                       @RequestParam(value = "lineNumber", required = false) Integer lineNumber);

    /**
     *
     * @param jobType
     * @param datasetId
     * @param preparationCode
     * @return
     */
    @GetMapping(value = "/checkEligibilityForPreparation")
    JobStatusEnum checkEligibilityOfPreparationJob(@RequestParam("jobType") String jobType, @RequestParam("datasetId") Long datasetId, @RequestParam("preparationCode") String preparationCode);
}










