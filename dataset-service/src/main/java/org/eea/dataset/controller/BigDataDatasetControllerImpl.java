package org.eea.dataset.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.eea.interfaces.controller.dataset.BigDataDatasetController;
import org.eea.lock.annotation.LockCriteria;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public class BigDataDatasetControllerImpl implements BigDataDatasetController {

    /**
     * Import big data.
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param tableSchemaId the table schema id
     * @param file the file
     * @param replace the replace
     * @param integrationId the integration id
     * @param delimiter the delimiter
     */
    @Override
    @HystrixCommand(commandProperties = {@HystrixProperty(
            name = "execution.isolation.thread.timeoutInMilliseconds", value = "7200000")})
    @PostMapping("/importBigData/{datasetId}")
    @ApiOperation(value = "Import file to dataset data (Large files)",
            notes = "Allowed roles: \n\n Reporting dataset: LEAD REPORTER, REPORTER WRITE, NATIONAL COORDINATOR \n\n Data collection: CUSTODIAN, STEWARD\n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE\n\n EU dataset: CUSTODIAN, STEWARD")
    @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully imported file"),
            @ApiResponse(code = 400, message = "Error importing file"),
            @ApiResponse(code = 500, message = "Error importing file")})
    public void importBigData(
            @ApiParam(type = "Long", value = "Dataset id", example = "0") @LockCriteria(
                    name = "datasetId") @PathVariable("datasetId") Long datasetId,
            @ApiParam(type = "Long", value = "Dataflow id",
                    example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
            @ApiParam(type = "Long", value = "Provider id",
                    example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
            @ApiParam(type = "String", value = "Table schema id",
                    example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
                    required = false) String tableSchemaId,
            @ApiParam(value = "File to upload") @RequestParam("file") MultipartFile file,
            @ApiParam(type = "boolean", value = "Replace current data",
                    example = "true") @RequestParam(value = "replace", required = false) boolean replace,
            @ApiParam(type = "Long", value = "Integration id", example = "0") @RequestParam(
                    value = "integrationId", required = false) Long integrationId,
            @ApiParam(type = "String", value = "File delimiter",
                    example = ",") @RequestParam(value = "delimiter", required = false) String delimiter,
            @ApiParam(type = "String", value = "Fme Job Id",
                    example = ",") @RequestParam(value = "fmeJobId", required = false) String fmeJobId) {

        /*
        * Part 1:
        * Lets say we got a zip file
        *
        * extract it
        *
        * convert csv files to parquet
        *
        * send parquet files to s3
        *
        * */

        /*
        * Part 2:
        *
        * Add job and handle it
        * */

        /*
         * Part 3:
         *
         * Add checks for wrong filenames or sth
         * */

        /*
         * Part 4:
         *
         * Case where zip file is in s3 and we need to download it first
         * */

        /*
         * Part 5:
         *
         * Case where we get notification from s3 that zip file has been uploaded
         * */



        /*JobStatusEnum jobStatus = JobStatusEnum.IN_PROGRESS;
        Long jobId = null;
        try {
            if (dataflowId == null){
                dataflowId = datasetService.getDataFlowIdById(datasetId);
            }

            JobVO job = null;
            if (fmeJobId!=null) {
                jobControllerZuul.updateFmeCallbackJobParameter(fmeJobId, true);
                job = jobControllerZuul.findJobByFmeJobId(fmeJobId);
                if (job!=null && (job.getJobStatus().equals(JobStatusEnum.CANCELED) || job.getJobStatus().equals(JobStatusEnum.CANCELED_BY_ADMIN))) {
                    LOG.info("Job {} is cancelled. Exiting import!", job.getId());
                    return;
                }
            }
            if(job!=null){
                jobId = job.getId();
                LOG.info("Incoming Fme Related Import job with fmeJobId {}, jobId {} and datasetId {}", fmeJobId, jobId, datasetId);
            }else{
                //check if there is already an import job with status IN_PROGRESS for the specific datasetId
                List<Long> datasetIds = new ArrayList<>();
                datasetIds.add(datasetId);
                jobStatus = jobControllerZuul.checkEligibilityOfJob(JobTypeEnum.IMPORT.getValue(), false, dataflowId, providerId, datasetIds);
                jobId = jobControllerZuul.addImportJob(datasetId, dataflowId, providerId, tableSchemaId, file.getOriginalFilename(), replace, integrationId, delimiter, jobStatus, fmeJobId);
                if(jobStatus.getValue().equals(JobStatusEnum.REFUSED.getValue())){
                    LOG.info("Added import job with id {} for datasetId {} with status REFUSED", jobId, datasetId);
                    datasetService.releaseImportRefusedNotification(datasetId, dataflowId, tableSchemaId, file.getOriginalFilename());
                    throw new ResponseStatusException(HttpStatus.LOCKED, EEAErrorMessage.IMPORTING_FILE_DATASET);
                }
            }

            LOG.info("Importing big file for dataflowId {}, datasetId {} and tableSchemaId {}. ReplaceData is {}", dataflowId, datasetId, tableSchemaId, replace);
            fileTreatmentHelper.importFileData(datasetId,dataflowId, tableSchemaId, file, replace, integrationId, delimiter, jobId);
            LOG.info("Successfully imported big file for dataflowId {}, datasetId {} and tableSchemaId {}. ReplaceData was {}", dataflowId, datasetId, tableSchemaId, replace);
        } catch (EEAException e) {
            LOG.error(
                    "File import failed: dataflowId={} datasetId={}, tableSchemaId={}, fileName={}. Message: {}", dataflowId, datasetId,
                    tableSchemaId, file.getOriginalFilename(), e.getMessage(), e);
            if (jobId!=null) {
                jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);
            }
            Map<String, Object> importFileData = new HashMap<>();
            importFileData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_BIG_FILE_DATA.getValue());
            importFileData.put(LiteralConstants.DATASETID, datasetId);
            lockService.removeLockByCriteria(importFileData);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    EEAErrorMessage.IMPORTING_FILE_DATASET);
        } catch (Exception e) {
            String fileName = (file != null) ? file.getName() : null;
            LOG.error("Unexpected error! Error importing big file {} for datasetId {} providerId {} and tableSchemaId {} Message: {}", fileName, datasetId, providerId, tableSchemaId, e.getMessage());
            if (jobId!=null && jobStatus != JobStatusEnum.REFUSED) {
                jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);
            }
            Map<String, Object> importFileData = new HashMap<>();
            importFileData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_BIG_FILE_DATA.getValue());
            importFileData.put(LiteralConstants.DATASETID, datasetId);
            lockService.removeLockByCriteria(importFileData);
            throw e;
        }*/
    }

}
