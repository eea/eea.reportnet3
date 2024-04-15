package org.eea.interfaces.controller.orchestrator;

import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.JobsVO;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
     * @param released the released
     * @return
     */
    @PutMapping(value = "/addValidationJob/{datasetId}")
    void addValidationJob(@PathVariable("datasetId") Long datasetId, @RequestParam(value = "released", required = false) boolean released);

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
                       @RequestParam(name = "silentRelease", required = false, defaultValue = "false") boolean silentRelease);

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
     * @return the job id
     */
    @PostMapping(value = "/addImport/{datasetId}")
    Long addImportJob(@PathVariable("datasetId") Long datasetId,
                      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
                       @RequestParam(value = "providerId", required = false) Long providerId,
                       @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
                       @RequestParam("fileName") String fileName,
                       @RequestParam(value = "replace", required = false) boolean replace,
                       @RequestParam(value = "integrationId", required = false) Long integrationId,
                       @RequestParam(value = "delimiter", required = false) String delimiter,
                      @RequestParam(value = "jobStatus", required = false) JobStatusEnum jobStatus,
                      @RequestParam(value = "fmeJobId", required = false) String fmeJobId);

    /**
     * Adds an etl import job
     *
     * @param datasetId the id of the dataset
     * @param dataflowId the id of the dataflow
     * @param providerId the id of the provider
     * @param jobStatus the status of the job
     * @return the job id
     */
    @PostMapping(value = "/addEtlImport/{datasetId}")
    Long addEtlImportJob(@PathVariable("datasetId") Long datasetId,
                         @RequestParam(value = "dataflowId", required = false) Long dataflowId,
                         @RequestParam(value = "providerId", required = false) Long providerId,
                         @RequestParam(value = "jobStatus", required = false) JobStatusEnum jobStatus);

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
                                  @RequestParam(value = "dataProviderCodes", required = false) String dataProviderCodes);

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

    @GetMapping(value = "/private/pollForJobStatus/{jobId}")
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
    void cancelJob(@PathVariable("jobId") Long jobId) throws Exception;

    /**
     * Updates job info value
     * @param jobId
     * @param jobInfo
     */
    @PostMapping(value = "/private/updateJobInfo/{jobId}")
    void updateJobInfo(@PathVariable("jobId") Long jobId,  @RequestParam(value = "jobInfo") JobInfoEnum jobInfo);


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
     * Retrieves the status of a job
     *
     * @param jobId the job id
     * @return
     */
    @GetMapping(value = "/getJobStatusByJobId/{jobId}")
    JobStatusEnum getJobStatusByJobId(@PathVariable("jobId") Long jobId);
}










