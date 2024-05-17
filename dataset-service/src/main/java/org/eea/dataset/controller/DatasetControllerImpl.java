package org.eea.dataset.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.swagger.annotations.*;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eea.datalake.service.DremioHelperService;
import org.eea.dataset.persistence.data.domain.AttachmentValue;
import org.eea.dataset.service.*;
import org.eea.dataset.service.helper.DeleteHelper;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.helper.UpdateRecordHelper;
import org.eea.dataset.service.model.TruncateDataset;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataset.*;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.*;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.orchestrator.JobPresignedUrlInfo;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.validation.TaskVO;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * The Class DatasetControllerImpl.
 */
@RestController
@RequestMapping("/dataset")
@Api(tags = "Datasets : Dataset Manager")
public class DatasetControllerImpl implements DatasetController {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetControllerImpl.class);

  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /** The update record helper. */
  @Autowired
  private UpdateRecordHelper updateRecordHelper;

  /** The delete helper. */
  @Autowired
  private DeleteHelper deleteHelper;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The dataset schema service. */
  @Autowired
  private DatasetSchemaService datasetSchemaService;

  /** The file treatment helper. */
  @Autowired
  private FileTreatmentHelper fileTreatmentHelper;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The notification controller zuul. */
  @Autowired
  private NotificationControllerZuul notificationControllerZuul;

  /** The job controller zuul */
  @Autowired
  private JobControllerZuul jobControllerZuul;


  /** The dataflow controller zuul */
  @Autowired
  private DataFlowControllerZuul dataFlowControllerZuul;

  /** The datalake service */
  @Autowired
  private DataLakeDataRetrieverFactory dataLakeDataRetrieverFactory;

  /** The big data dataset service */
  @Autowired
  private BigDataDatasetService bigDataDatasetService;

  @Autowired
  private DatasetTableService datasetTableService;

  /**
   * Gets the data tables values.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param pageNum the page num
   * @param pageSize the page size
   * @param fields the fields
   * @param levelError the level error
   * @param idRules the id rules
   * @param fieldSchemaId the id field schema
   * @param fieldValue the field value
   * @return the data tables values
   */
  @Override
  @HystrixCommand
  @GetMapping("TableValueDataset/{id}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATACOLLECTION_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATASET_NATIONAL_COORDINATOR','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','DATACOLLECTION_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT','REFERENCEDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiOperation(value = "Get table data", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 400, message = "Dataset id incorrect"),
          @ApiResponse(code = 404, message = "Dataset not found"),
          @ApiResponse(code = 500, message = "Error getting the data")})
  public TableVO getDataTablesValues(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("id") Long datasetId,
          @ApiParam(type = "String", value = "Table schema id",
                  example = "5cf0e9b3b793310e9ceca190") @RequestParam("idTableSchema") String idTableSchema,
          @ApiParam(type = "Integer", value = "Page number", example = "0") @RequestParam(
                  value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
          @ApiParam(type = "Integer", value = "Page size",
                  example = "0") @RequestParam(value = "pageSize", required = false) Integer pageSize,
          @ApiParam(type = "String", value = "Field names",
                  example = "field1") @RequestParam(value = "fields", required = false) String fields,
          @ApiParam(value = "Level error to filter", example = "INFO,WARNING") @RequestParam(
                  value = "levelError", required = false) ErrorTypeEnum[] levelError,
          @ApiParam(value = "List of rule ids to filter",
                  example = "a,b,c") @RequestParam(value = "idRules", required = false) String[] idRules,
          @ApiParam(type = "String", value = "Field schema id",
                  example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "fieldSchemaId",
                  required = false) String fieldSchemaId,
          @ApiParam(type = "String", value = "Value to filter",
                  example = "3") @RequestParam(value = "fieldValue", required = false) String fieldValue) {
    if (null == datasetId || null == idTableSchema) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DATASET_INCORRECT_ID);
    }

    // check if the parameters received from the frontend are the needed to get the table values
    // WITHOUT PAGINATION
    Pageable pageable = null;
    if (pageSize != null) {
      pageable = PageRequest.of(pageNum, pageSize);
    }
    // else pageable will be null, it will be created inside the service
    TableVO result = null;
    try {
      result = datasetService.getTableValuesById(datasetId, idTableSchema, pageable, fields,
              levelError, idRules, fieldSchemaId, fieldValue);
    } catch (EEAException e) {
      LOG.error(e.getMessage());
      if (e.getMessage().equals(EEAErrorMessage.DATASET_NOTFOUND)) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DATASET_NOTFOUND);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.OBTAINING_TABLE_DATA);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving dataTables values for datasetId {} and tableSchemaId {} Message: {}", datasetId, idTableSchema, e.getMessage());
      throw e;
    }

    return result;
  }

  @Override
  @HystrixCommand
  @GetMapping("TableValueDatasetDL/{id}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATACOLLECTION_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATASET_NATIONAL_COORDINATOR','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','DATACOLLECTION_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT','REFERENCEDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiOperation(value = "Get table data", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 400, message = "Dataset id incorrect"),
          @ApiResponse(code = 404, message = "Dataset not found"),
          @ApiResponse(code = 500, message = "Error getting the data")})
  public TableVO getDataTablesValuesDL(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("id") Long datasetId,
          @ApiParam(type = "String", value = "Table schema id",
                  example = "5cf0e9b3b793310e9ceca190") @RequestParam("idTableSchema") String idTableSchema,
          @ApiParam(type = "Integer", value = "Page number", example = "0") @RequestParam(
                  value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
          @ApiParam(type = "Integer", value = "Page size",
                  example = "0") @RequestParam(value = "pageSize", required = false) Integer pageSize,
          @ApiParam(type = "String", value = "Field names",
                  example = "field1") @RequestParam(value = "fields", required = false) String fields,
          @ApiParam(value = "Level error to filter", example = "INFO,WARNING") @RequestParam(
                  value = "levelError", required = false) ErrorTypeEnum[] levelError,
          @ApiParam(value = "List of rule ids to filter",
                  example = "a,b,c") @RequestParam(value = "idRules", required = false) String[] idRules,
          @ApiParam(type = "String", value = "Field schema id",
                  example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "fieldSchemaId",
                  required = false) String fieldSchemaId,
          @ApiParam(type = "String", value = "Value to filter",
                  example = "3") @RequestParam(value = "fieldValue", required = false) String fieldValue,
          @ApiParam(value = "List of qc codes to filter") @RequestParam(value = "qcCodes", required = false) String[] qcCodes) {
    if (null == datasetId || null == idTableSchema) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.DATASET_INCORRECT_ID);
    }

    // check if the parameters received from the frontend are the needed to get the table values
    // WITHOUT PAGINATION
    Pageable pageable = null;
    if (pageSize != null) {
      pageable = PageRequest.of(pageNum, pageSize);
    }
    // else pageable will be null, it will be created inside the service
    TableVO result = null;
    try {
      DataSetMetabaseVO dataset = datasetMetabaseService.findDatasetMetabase(datasetId);
      String datasetSchemaId = dataset.getDatasetSchema();
      TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(idTableSchema, datasetSchemaId);
      result = dataLakeDataRetrieverFactory.getRetriever(datasetId).getTableResult(dataset, tableSchemaVO, pageable, fields, fieldValue, levelError, qcCodes);
    } catch (EEAException e) {
      LOG.error(e.getMessage());
      if (e.getMessage().equals(EEAErrorMessage.DATASET_NOTFOUND)) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DATASET_NOTFOUND);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.OBTAINING_TABLE_DATA);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving datalake table values for datasetId {} and tableSchemaId {} Message: {}", datasetId, idTableSchema, e.getMessage());
      throw e;
    }

    return result;
  }

  /**
   * Update dataset.
   *
   * @param dataset the dataset
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataset.id,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @PutMapping("/update")
  @ApiOperation(value = "Update dataset", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully updated dataset"),
          @ApiResponse(code = 404, message = "Dataset not found"),
          @ApiResponse(code = 400, message = "Dataset not informed"),
          @ApiResponse(code = 500, message = "Error updating  dataset")})
  public void updateDataset(@ApiParam(value = "dataset object") @RequestBody DataSetVO dataset) {
    if (dataset == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DATASET_NOTFOUND);
    }
    try {
      datasetService.updateDataset(dataset.getId(), dataset);
    } catch (EEAException e) {
      LOG.error(e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.UPDATING_DATASET);
    } catch (Exception e) {
      Long datasetId = (dataset != null) ? dataset.getId() : null;
      LOG.error("Unexpected error! Error updating dataset for datasetId {} Message: {}", datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Import file data.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param tableSchemaId the table schema id
   * @param file the file
   * @param replace the replace
   * @param integrationId the integration id
   * @param delimiter the delimiter
   * @param jobId the jobId
   * @param fmeJobId the fmeJobId
   */
  @SneakyThrows
  @Override
  @HystrixCommand(commandProperties = {@HystrixProperty(
          name = "execution.isolation.thread.timeoutInMilliseconds", value = "7200000")})
  @PostMapping("/v2/importFileData/{datasetId}")
  @ApiOperation(value = "Import file to dataset data (Large files)",
          notes = "Allowed roles: \n\n Reporting dataset: LEAD REPORTER, REPORTER WRITE, NATIONAL COORDINATOR \n\n Data collection: CUSTODIAN, STEWARD\n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE\n\n EU dataset: CUSTODIAN, STEWARD")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully imported file"),
          @ApiResponse(code = 400, message = "Error importing file"),
          @ApiResponse(code = 500, message = "Error importing file")})
  public void importBigFileData(
          @ApiParam(type = "Long", value = "Dataset id", example = "0") @LockCriteria(
                  name = "datasetId") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
          @ApiParam(type = "String", value = "Table schema id",
                  example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
                  required = false) String tableSchemaId,
          @ApiParam(value = "File to upload") @RequestParam(value = "file", required = false) MultipartFile file,
          @ApiParam(type = "boolean", value = "Replace current data",
                  example = "true") @RequestParam(value = "replace", required = false) boolean replace,
          @ApiParam(type = "Long", value = "Integration id", example = "0") @RequestParam(
                  value = "integrationId", required = false) Long integrationId,
          @ApiParam(type = "String", value = "File delimiter",
                  example = ",") @RequestParam(value = "delimiter", required = false) String delimiter,
          @ApiParam(type = "Long", value = "Job Id",
                  example = "9706378") @RequestParam(value = "jobId", required = false) Long jobId,
          @ApiParam(type = "String", value = "Fme Job Id",
                  example = "9706378") @RequestParam(value = "fmeJobId", required = false) String fmeJobId) {

    if (dataflowId == null){
      dataflowId = datasetService.getDataFlowIdById(datasetId);
    }
    DataFlowVO dataFlowVO = dataFlowControllerZuul.findById(dataflowId, providerId);
    if(dataFlowVO.getBigData() != null && dataFlowVO.getBigData()){
      try {
        if(StringUtils.isNotBlank(tableSchemaId)){
          String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
          TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);
          if(tableSchemaVO != null && BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable())
                  && BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaVO.getIdTableSchema()))){
            LOG.error("Can not import for datasetId {} because the table is iceberg", datasetId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.IMPORTING_FILE_ICEBERG);
          }
        }
        bigDataDatasetService.importBigData(datasetId, dataflowId, providerId, tableSchemaId, file, replace, integrationId, delimiter, jobId, fmeJobId, dataFlowVO);
      } catch (Exception e) {
        LOG.error("Error when importing data to Dremio for datasetId {}", datasetId, e);
        throw e;
      }
    }
    else{
      JobStatusEnum jobStatus = JobStatusEnum.IN_PROGRESS;
      jobId = null;
      try {
        if(file == null){
          throw new EEAException("Empty file and file path");
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
          jobId = jobControllerZuul.addImportJob(datasetId, dataflowId, providerId, tableSchemaId, file.getOriginalFilename(), replace, integrationId, delimiter, jobStatus, fmeJobId, null);
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
      }
    }
  }

  /**
   * Import file data.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param tableSchemaId the table schema id
   * @param file the file
   * @param replace the replace
   * @param integrationId the integration id
   * @param delimiter the delimiter
   * @param jobId the jobId
   * @param fmeJobId the fmeJobId
   */
  @Override
  @HystrixCommand(commandProperties = {@HystrixProperty(
          name = "execution.isolation.thread.timeoutInMilliseconds", value = "7200000")})
  @LockMethod(removeWhenFinish = false)
  @PostMapping("/v1/{datasetId}/importFileData")
  @ApiOperation(value = "Import file to dataset data (Large files not allowed > 50 MB)",
          notes = "Allowed roles: \n\n Reporting dataset: LEAD REPORTER, REPORTER WRITE, NATIONAL COORDINATOR \n\n Data collection: CUSTODIAN, STEWARD\n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE\n\n EU dataset: CUSTODIAN, STEWARD")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully imported file"),
          @ApiResponse(code = 400, message = "Error importing file"),
          @ApiResponse(code = 500, message = "Error importing file")})
  // @Deprecated but still called by FME
  public void importFileData(
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
          @ApiParam(type = "Long", value = "Job Id",
                  example = "9706378") @RequestParam(value = "jobId", required = false) Long jobId,
          @ApiParam(type = "String", value = "Fme Job Id",
                  example = ",") @RequestParam(value = "fmeJobId", required = false) String fmeJobId) {

    this.importBigFileData(datasetId, dataflowId, providerId, tableSchemaId, file, replace,
            integrationId, delimiter, jobId, fmeJobId);
  }

  /**
   * Import file data legacy.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param tableSchemaId the table schema id
   * @param file the file
   * @param replace the replace
   * @param integrationId the integration id
   * @param delimiter the delimiter
   * @param jobId the jobId
   * @param fmeJobId the fmeJobId
   */
  @Override
  @HystrixCommand(commandProperties = {@HystrixProperty(
          name = "execution.isolation.thread.timeoutInMilliseconds", value = "7200000")})
  @PostMapping("/{datasetId}/importFileData")
  @ApiOperation(value = "Import file to dataset data", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully imported file"),
          @ApiResponse(code = 400, message = "Error importing file"),
          @ApiResponse(code = 500, message = "Error importing file")})
  // @Deprecated
  public void importFileDataLegacy(
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
          @ApiParam(type = "Long", value = "Job Id",
                  example = "9706378") @RequestParam(value = "jobId", required = false) Long jobId,
          @ApiParam(type = "String", value = "Fme Job Id",
                  example = ",") @RequestParam(value = "fmeJobId", required = false) String fmeJobId) {
    this.importBigFileData(datasetId, dataflowId, providerId, tableSchemaId, file, replace,
            integrationId, delimiter, jobId, fmeJobId);
  }

  /**
   * Gets the data flow id by id.
   *
   * @param datasetId the dataset id
   * @return the data flow id by id
   */
  @Override
  @HystrixCommand
  @GetMapping("/private/{id}/dataflow")
  @ApiOperation(value = "get Dataflow id by id", hidden = true)
  public Long getDataFlowIdById(
          @ApiParam(type = "Long", value = "Dataset Id", example = "0") Long datasetId) {
    return datasetService.getDataFlowIdById(datasetId);
  }

  /**
   * Update records.
   *
   * @param datasetId the dataset id
   * @param records the records
   * @param updateCascadePK the update cascade PK
   * @param tableSchemaId the tableSchemaId
   */
  @Override
  @HystrixCommand
  @PutMapping("/{id}/updateRecord")
  @LockMethod
  @ApiOperation(value = "Update records", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD', 'TESTDATASET_STEWARD')")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully updated record"),
          @ApiResponse(code = 404, message = "Record not found in dataset"),
          @ApiResponse(code = 400, message = "record informed not found or table is read only")})
  public void updateRecords(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @LockCriteria(name = "datasetId") @PathVariable("id") Long datasetId,
          @ApiParam(value = "list of records") @RequestBody List<RecordVO> records,
          @ApiParam(type = "boolean", value = "update cascade", example = "true") @RequestParam(
                  value = "updateCascadePK", required = false) boolean updateCascadePK,
          @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId) throws Exception {
    if (datasetId == null || records == null || records.isEmpty()) {
      LOG.error(
              "Error updating records. The datasetId or the records to update are emtpy or null");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.RECORD_NOTFOUND);
    }
    if (datasetService.checkIfDatasetLockedOrReadOnly(datasetId, records.get(0).getIdRecordSchema(),
            EntityTypeEnum.RECORD)) {
      LOG.error("Error updating records in the datasetId {}. The table is read only",
              datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
    }
    try {
      LOG.info("Updating records for datasetId {}", datasetId);
      Long dataflowId = datasetService.getDataFlowIdById(datasetId);
      DataFlowVO dataFlowVO = dataFlowControllerZuul.findById(dataflowId, null);

      if(dataFlowVO.getBigData() != null && dataFlowVO.getBigData()) {
        String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
        Long providerId = datasetService.getDataProviderIdById(datasetId);
        TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);
        if(tableSchemaVO != null && BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable())
                && BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaVO.getIdTableSchema()))) {
          bigDataDatasetService.updateRecords(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), records, updateCascadePK);
        }
        else{
          throw new Exception("The table data are not manually editable or the iceberg table has not been created");
        }
      }
      else{
        updateRecordHelper.executeUpdateProcess(datasetId, records, updateCascadePK);
      }
      LOG.info("Successfully updated records for datasetId {}", datasetId);
    } catch (EEAException e) {
      LOG.error("Error updating records in the datasetId {}. Message: {}", datasetId,
              e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.UPDATING_TABLE_DATA);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error updating records for datasetId {} Message: {}", datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete record.
   *
   * @param datasetId the dataset id
   * @param recordId the record id
   * @param deleteCascadePK the delete cascade PK
   */
  @SneakyThrows
  @Override
  @HystrixCommand
  @DeleteMapping("/{id}/record/{recordId}")
  @LockMethod
  @ApiOperation(value = "Delete record", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully deleted"),
          @ApiResponse(code = 404, message = "record or dataset not found"), @ApiResponse(code = 400,
          message = "error because table is read only or fixed number of records is active")})
  public void deleteRecord(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @LockCriteria(name = "datasetId") @PathVariable("id") Long datasetId,
          @ApiParam(type = "String", value = "record Id",
                  example = "19D0B971B7E0D2FB66B77F2A8DBA4964") @PathVariable("recordId") String recordId,
          @ApiParam(type = "boolean", value = "delete cascade", example = "true") @RequestParam(
                  value = "deleteCascadePK", required = false) boolean deleteCascadePK,
          @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId) {
    if (!DatasetTypeEnum.DESIGN.equals(datasetMetabaseService.getDatasetType(datasetId))
            && Boolean.TRUE.equals(datasetService.getTableFixedNumberOfRecords(datasetId,
            datasetService.findRecordSchemaIdById(datasetId, recordId), EntityTypeEnum.RECORD))) {
      LOG.error(
              "Error deleting record with id {} in the datasetId {}. The table has a fixed number of records",
              recordId, datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              String.format(EEAErrorMessage.FIXED_NUMBER_OF_RECORDS,
                      datasetService.findRecordSchemaIdById(datasetId, recordId)));
    }
    try {
      LOG.info("Deleting record with id {} for datasetId {}", recordId, datasetId);
      Long dataflowId = datasetService.getDataFlowIdById(datasetId);
      DataFlowVO dataFlowVO = dataFlowControllerZuul.findById(dataflowId, null);

      if(dataFlowVO.getBigData() != null && dataFlowVO.getBigData()) {
        String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
        Long providerId = datasetService.getDataProviderIdById(datasetId);
        TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);

        if(tableSchemaVO != null && BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable())
                && BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaVO.getIdTableSchema()))) {

          if (datasetService.checkIfDatasetLockedOrReadOnly(datasetId,
                  tableSchemaVO.getRecordSchema().getIdRecordSchema(), EntityTypeEnum.RECORD)) {
            LOG.error("Error deleting record with id {} in the datasetId {}. The table is read only",
                    recordId, datasetId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
          }

          bigDataDatasetService.deleteRecord(dataflowId, providerId, datasetId, tableSchemaVO, recordId, deleteCascadePK);
        }
        else{
          throw new Exception("The table data are not manually editable or the iceberg table has not been created");
        }
      }
      else {
        if (datasetService.checkIfDatasetLockedOrReadOnly(datasetId,
                datasetService.findRecordSchemaIdById(datasetId, recordId), EntityTypeEnum.RECORD)) {
          LOG.error("Error deleting record with id {} in the datasetId {}. The table is read only",
                  recordId, datasetId);
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
        }
        updateRecordHelper.executeDeleteProcess(datasetId, recordId, deleteCascadePK);
      }
      LOG.info("Successfully deleted record with id {} for datasetId {}", recordId, datasetId);
    } catch (EEAException e) {
      LOG.error("Error deleting record with id {} in the datasetId {}. Message: {}", recordId, datasetId,
              e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DELETING_TABLE_DATA);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error deleting record with id {} for datasetId {} Message: {}", recordId, datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Insert records.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the id table schema
   * @param records the records
   */
  @SneakyThrows
  @Override
  @HystrixCommand
  @PostMapping("/{datasetId}/table/{tableSchemaId}/record")
  @LockMethod
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD', 'TESTDATASET_STEWARD')")
  @ApiOperation(value = "Insert records in table", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully inserted"), @ApiResponse(
          code = 400,
          message = "error because table is read only or fixed number of records is active or inserting")})
  public void insertRecords(
          @ApiParam(type = "Long", value = "Dataset Id", example = "0") @LockCriteria(
                  name = "datasetId") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "String", value = "table schema Id",
                  example = "5cf0e9b3b793310e9ceca190") @PathVariable("tableSchemaId") String tableSchemaId,
          @ApiParam(value = "list of records") @RequestBody List<RecordVO> records) {
    DatasetTypeEnum datasetType = datasetMetabaseService.getDatasetType(datasetId);
    if ((!DatasetTypeEnum.DESIGN.equals(datasetType)
            && !DatasetTypeEnum.REFERENCE.equals(datasetType))
            && Boolean.TRUE.equals(datasetService.getTableFixedNumberOfRecords(datasetId,
            records.get(0).getIdRecordSchema(), EntityTypeEnum.RECORD))) {
      LOG.error("Error inserting record in the datasetId {} and tableSchemaId {}. The table has a fixed number of records",
              datasetId, tableSchemaId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String
              .format(EEAErrorMessage.FIXED_NUMBER_OF_RECORDS, records.get(0).getIdRecordSchema()));
    }
    try {
      if (datasetService.checkIfDatasetLockedOrReadOnly(datasetId, records.get(0).getIdRecordSchema(),
              EntityTypeEnum.RECORD)) {
        LOG.error("Error inserting record in the datasetId {} and tableSchemaId {}. The table is read only",
                datasetId, tableSchemaId);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
      }
      LOG.info("Inserting records for datasetId {} and tableSchemaId {}", datasetId, tableSchemaId);
      Long dataflowId = datasetService.getDataFlowIdById(datasetId);
      DataFlowVO dataFlowVO = dataFlowControllerZuul.findById(dataflowId, null);

      if(dataFlowVO.getBigData() != null && dataFlowVO.getBigData()) {
        String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
        Long providerId = datasetService.getDataProviderIdById(datasetId);
        TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);
        if(tableSchemaVO != null && BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable())
                && BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaVO.getIdTableSchema()))) {
          bigDataDatasetService.insertRecords(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), records);
        }
        else{
          throw new Exception("The table data are not manually editable or the iceberg table has not been created");
        }
      }
      else {
        updateRecordHelper.executeCreateProcess(datasetId, records, tableSchemaId);
      }
      LOG.info("Successfully inserted records for datasetId {} and tableSchemaId {}", datasetId, tableSchemaId);
    } catch (EEAException e) {
      LOG.error("Error inserting records for datasetId {} and tableSchemaId {} Message: {}", datasetId, tableSchemaId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.INSERTING_TABLE_DATA);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error inserting records for datasetId {} and tableSchemaId {} Message: {}", datasetId, tableSchemaId, e.getMessage());
      throw e;
    }
  }

  /**
   * Insert records multi table.
   *
   * @param datasetId the dataset id
   * @param tableRecords the table records
   */
  @Override
  @HystrixCommand
  @PostMapping("/{datasetId}/insertRecordsMultiTable")
  @LockMethod
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @ApiOperation(value = "Insert records in different tables", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully inserted"),
          @ApiResponse(code = 400, message = "error inserting records")})
  public void insertRecordsMultiTable(
          @ApiParam(type = "Long", value = "Dataset Id", example = "0") @LockCriteria(
                  name = "datasetId") @PathVariable("datasetId") Long datasetId,
          @ApiParam(value = "table Records") @RequestBody List<TableVO> tableRecords) {
    try {
      LOG.info("PaM group save: Inserting multiple records for datasetId {}", datasetId);
      updateRecordHelper.executeMultiCreateProcess(datasetId, tableRecords);
      LOG.info("PaM group save: Successfully inserted multiple records for datasetId {}", datasetId);
    } catch (EEAException e) {
      LOG.error("Error inserting records for datasetId {} Message : {}", datasetId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.INSERTING_TABLE_DATA);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error inserting records to multiple tables for datasetId {} Message: {}", datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete import data.
   *
   * @param datasetId the data set id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param deletePrefilledTables the delete prefilled tables
   */
  @SneakyThrows
  @Override
  @HystrixCommand
  @LockMethod(removeWhenFinish = false)
  @DeleteMapping("/v1/{datasetId}/deleteDatasetData")
  @PreAuthorize("secondLevelAuthorize(#datasetId, 'DATASCHEMA_CUSTODIAN', 'DATASCHEMA_STEWARD', 'DATASCHEMA_EDITOR_WRITE', 'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE', 'EUDATASET_CUSTODIAN','EUDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId, #datasetId, 'DATASCHEMA_CUSTODIAN', 'DATASCHEMA_STEWARD', 'DATASCHEMA_EDITOR_WRITE', 'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE', 'EUDATASET_CUSTODIAN','EUDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @ApiOperation(value = "Delete dataset data",
          notes = "Allowed roles: \n\n Reporting dataset: REPORTER WRITE, LEAD REPORTER \n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE\n\n EU dataset: CUSTODIAN, STEWARD")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully deleted"),
          @ApiResponse(code = 403, message = "Dataset not belong dataflow"),
          @ApiResponse(code = 401, message = "Unauthorize"),
          @ApiResponse(code = 500, message = "Error deleting data")})
  public void deleteDatasetData(
          @ApiParam(type = "Long", value = "Dataset id", example = "0") @LockCriteria(
                  name = "datasetId") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
          @ApiParam(type = "boolean", value = "Delete prefilled tables",
                  example = "true") @RequestParam(value = "deletePrefilledTables", defaultValue = "false",
                  required = false) Boolean deletePrefilledTables) {

    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDataflowId(dataflowId);
    userNotificationContentVO.setDatasetId(datasetId);
    userNotificationContentVO.setProviderId(providerId);
    notificationControllerZuul.createUserNotificationPrivate("DELETE_DATASET_DATA_INIT",
            userNotificationContentVO);

    // Rest API only: Check if the dataflow belongs to the dataset
    if (null != dataflowId && !dataflowId.equals(datasetService.getDataFlowIdById(datasetId))) {
      String errorMessage =
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId);
      LOG.error(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId));
    }
    try {
      if (dataflowId == null) {
        dataflowId = datasetService.getDataFlowIdById(datasetId);
      }
      DataFlowVO dataFlowVO = dataFlowControllerZuul.findById(dataflowId, null);

      if (dataFlowVO.getBigData()) {
        LOG.info("Deleting dataset data for big data dataflowId {} and datasetId {} ", dataflowId, datasetId);
        bigDataDatasetService.deleteDatasetData(datasetId, dataflowId, providerId, deletePrefilledTables);
        deleteHelper.releaseDeleteDatasetDataLocksAndSendNotification(datasetId, false);
      }
      else {
        LOG.info("Deleting dataset data for dataflowId {} and datasetId {}", dataflowId, datasetId);
        deleteHelper.executeDeleteDatasetProcess(datasetId, deletePrefilledTables, false);
      }
      LOG.info("Successfully deleted dataset data for dataflowId {} and datasetId {}", dataflowId, datasetId);
    } catch (Exception e) {
      // Release the lock manually
      Map<String, Object> deleteDatasetValues = new HashMap<>();
      deleteDatasetValues.put(LiteralConstants.SIGNATURE,
              LockSignature.DELETE_DATASET_VALUES.getValue());
      deleteDatasetValues.put(LiteralConstants.DATASETID, datasetId);
      lockService.removeLockByCriteria(deleteDatasetValues);
      LOG.error("Unexpected error! Error deleting dataset data for dataflowId {} datasetId {} and providerId {} Message: {}", dataflowId, datasetId, providerId, e.getMessage());
      throw e;
    }
  }

  /**
   * Private delete dataset data.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param technicallyAccepted the technically accepted
   */
  @SneakyThrows
  @Override
  @HystrixCommand
  @LockMethod(removeWhenFinish = true)
  @DeleteMapping("/private/{datasetId}/deleteDatasetData")
  @ApiOperation(value = "Private Delete dataset data", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully deleted"),
          @ApiResponse(code = 403, message = "Dataset not belong dataflow"),
          @ApiResponse(code = 401, message = "Unauthorize"),
          @ApiResponse(code = 500, message = "Error deleting data")})
  public void privateDeleteDatasetData(
          @ApiParam(type = "Long", value = "Dataset id", example = "0") @LockCriteria(
                  name = "datasetId") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
          @ApiParam(type = "Boolean", value = "Technically Accepted") @RequestParam(
                  value = "technicallyAccepted", required = true) boolean technicallyAccepted) {

    // Rest API only: Check if the dataflow belongs to the dataset
    if (null != dataflowId && !dataflowId.equals(datasetService.getDataFlowIdById(datasetId))) {
      String errorMessage =
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId);
      LOG.error(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId));
    }
    try {
      if (dataflowId == null) {
        dataflowId = datasetService.getDataFlowIdById(datasetId);
      }
      DataFlowVO dataFlowVO = dataFlowControllerZuul.findById(dataflowId, null);

      if (dataFlowVO.getBigData()) {
        LOG.info("Privately deleting dataset data for big data dataflowId {} and datasetId {} ", dataflowId, datasetId);
        bigDataDatasetService.deleteDatasetData(datasetId, dataflowId, null, false);
        deleteHelper.releaseDeleteDatasetDataLocksAndSendNotification(datasetId, false);
      }
      else {
        LOG.info("Privately deleting dataset data for dataflowId {} and datasetId {}", dataflowId, datasetId);
        deleteHelper.executeDeleteDatasetProcess(datasetId, false, technicallyAccepted);
      }
      LOG.info("Successfully privately deleted dataset data for dataflowId {} and datasetId {}", dataflowId, datasetId);
    } catch (Exception e) {
      // Release the lock manually
      Map<String, Object> deleteDatasetValues = new HashMap<>();
      deleteDatasetValues.put(LiteralConstants.SIGNATURE,
              LockSignature.DELETE_DATASET_VALUES.getValue());
      deleteDatasetValues.put(LiteralConstants.DATASETID, datasetId);
      lockService.removeLockByCriteria(deleteDatasetValues);
      LOG.error("Unexpected error! Error privately deleting dataset data for dataflowId {} and datasetId {} Message: {}", dataflowId, datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete import data legacy.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param deletePrefilledTables the delete prefilled tables
   */
  @Override
  @HystrixCommand
  @DeleteMapping("/{datasetId}/deleteImportData")
  @PreAuthorize("secondLevelAuthorize(#datasetId, 'DATASCHEMA_CUSTODIAN', 'DATASCHEMA_STEWARD', 'DATASCHEMA_EDITOR_WRITE', 'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE', 'EUDATASET_CUSTODIAN','EUDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId, #datasetId, 'DATASCHEMA_CUSTODIAN', 'DATASCHEMA_STEWARD', 'DATASCHEMA_EDITOR_WRITE', 'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE', 'EUDATASET_CUSTODIAN','EUDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @ApiOperation(value = "Delete dataset data", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully deleted"),
          @ApiResponse(code = 403, message = "Dataset not belong dataflow"),
          @ApiResponse(code = 401, message = "Unauthorize"),
          @ApiResponse(code = 500, message = "Error deleting data")})
  public void deleteImportDataLegacy(
          @ApiParam(type = "Long", value = "Dataset id", example = "0") @LockCriteria(
                  name = "datasetId") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
          @ApiParam(type = "boolean", value = "Delete prefilled tables",
                  example = "true") @RequestParam(value = "deletePrefilledTables", defaultValue = "false",
                  required = false) Boolean deletePrefilledTables) {
    this.deleteDatasetData(datasetId, dataflowId, providerId, deletePrefilledTables);
  }

  /**
   * Delete import table.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   */
  @SneakyThrows
  @Override
  @HystrixCommand
  @LockMethod(removeWhenFinish = false)
  @DeleteMapping("/v1/{datasetId}/deleteTableData/{tableSchemaId}")
  @PreAuthorize("secondLevelAuthorize(#datasetId, 'DATASCHEMA_CUSTODIAN', 'DATASCHEMA_STEWARD', 'DATASCHEMA_EDITOR_WRITE', 'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE', 'EUDATASET_CUSTODIAN','EUDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId, #datasetId, 'DATASCHEMA_CUSTODIAN', 'DATASCHEMA_STEWARD', 'DATASCHEMA_EDITOR_WRITE', 'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE', 'EUDATASET_CUSTODIAN','EUDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @ApiOperation(value = "Delete table data",
          notes = "Allowed roles: \n\n Reporting dataset: REPORTER WRITE, LEAD REPORTER \n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE\n\n EU dataset: CUSTODIAN, STEWARD")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully deleted"),
          @ApiResponse(code = 403, message = "Dataset not belong dataflow"),
          @ApiResponse(code = 401, message = "Unauthorize"),
          @ApiResponse(code = 500, message = "Error deleting data")})
  public void deleteTableData(
          @ApiParam(type = "Long", value = "Dataset id", example = "0") @LockCriteria(
                  name = "datasetId") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "String", value = "Table schema id",
                  example = "5cf0e9b3b793310e9ceca190") @LockCriteria(
                  name = "tableSchemaId") @PathVariable("tableSchemaId") String tableSchemaId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId) {

    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDataflowId(dataflowId);
    userNotificationContentVO.setDatasetId(datasetId);
    userNotificationContentVO.setProviderId(providerId);
    notificationControllerZuul.createUserNotificationPrivate("DELETE_TABLE_DATA_INIT",
            userNotificationContentVO);

    // Rest API only: Check if the dataflow belongs to the dataset
    if (null != dataflowId && !dataflowId.equals(datasetService.getDataFlowIdById(datasetId))) {
      String errorMessage =
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId);
      LOG.error(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId));
    }

    try {
      if (dataflowId == null) {
        dataflowId = datasetService.getDataFlowIdById(datasetId);
      }
      DataFlowVO dataFlowVO = dataFlowControllerZuul.findById(dataflowId, null);
      if (dataFlowVO.getBigData()) {
        LOG.info("Deleting table data for big data dataflowId {}, datasetId {} and tableSchemaId {}", dataflowId, datasetId, tableSchemaId);
        bigDataDatasetService.deleteTableData(datasetId, dataflowId, providerId, tableSchemaId);
        deleteHelper.releaseDeleteTableDataLocksAndSendNotification(datasetId, tableSchemaId);
      }
      else {
        LOG.info("Deleting table data for dataflowId {}, datasetId {} and tableSchemaId {}", dataflowId, datasetId, tableSchemaId);
        // This method will release the lock
        deleteHelper.executeDeleteTableProcess(datasetId, tableSchemaId);
      }
      LOG.info("Successfully deleted table data for dataflowId {}, datasetId {} and tableSchemaId {}", dataflowId, datasetId, tableSchemaId);
    } catch (Exception e) {
      // Release the lock manually
      Map<String, Object> deleteImportTable = new HashMap<>();
      deleteImportTable.put(LiteralConstants.SIGNATURE, LockSignature.DELETE_IMPORT_TABLE.getValue());
      deleteImportTable.put(LiteralConstants.DATASETID, datasetId);
      deleteImportTable.put(LiteralConstants.TABLESCHEMAID, tableSchemaId);
      lockService.removeLockByCriteria(deleteImportTable);
      LOG.error("Unexpected error! Error deleting table data for dataflowId {} datasetId {} tableSchemaId {} and providerId {} Message: {}", dataflowId, datasetId, tableSchemaId, providerId, e.getMessage());
      throw e;
    }
  }


  /**
   * Delete import table legacy.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   */
  @Override
  @HystrixCommand
  @DeleteMapping("/{datasetId}/deleteImportTable/{tableSchemaId}")
  @PreAuthorize("secondLevelAuthorize(#datasetId, 'DATASCHEMA_CUSTODIAN', 'DATASCHEMA_STEWARD', 'DATASCHEMA_EDITOR_WRITE', 'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE', 'EUDATASET_CUSTODIAN','EUDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId, #datasetId, 'DATASCHEMA_CUSTODIAN', 'DATASCHEMA_STEWARD', 'DATASCHEMA_EDITOR_WRITE', 'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE', 'EUDATASET_CUSTODIAN','EUDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @ApiOperation(value = "Delete table data", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully deleted"),
          @ApiResponse(code = 403, message = "Dataset not belong dataflow"),
          @ApiResponse(code = 401, message = "Unauthorize"),
          @ApiResponse(code = 500, message = "Error deleting data")})
  public void deleteImportTableLegacy(
          @ApiParam(type = "Long", value = "Dataset id", example = "0") @LockCriteria(
                  name = "datasetId") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "String", value = "Table schema id",
                  example = "5cf0e9b3b793310e9ceca190") @LockCriteria(
                  name = "tableSchemaId") @PathVariable("tableSchemaId") String tableSchemaId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId) {
    this.deleteTableData(datasetId, tableSchemaId, dataflowId, providerId);
  }

  /**
   * Export file.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param mimeType the mime type
   * @param exportFilterVO the export filter VO
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/exportFile")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASET_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT','REFERENCEDATASET_STEWARD') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiOperation(value = "Export file with data", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully export"),
          @ApiResponse(code = 400, message = "Id table schema is incorrect"),
          @ApiResponse(code = 500, message = "Error exporting file")})
  public void exportFile(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @RequestParam("datasetId") Long datasetId,
          @ApiParam(type = "String", value = "table schema Id",
                  example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
                  required = false) String tableSchemaId,
          @ApiParam(type = "String", value = "mimeType (file extension)",
                  example = "csv") @RequestParam("mimeType") String mimeType,
          @RequestBody ExportFilterVO exportFilterVO) {
    String tableName =
            null != tableSchemaId ? datasetSchemaService.getTableSchemaName(null, tableSchemaId)
                    : datasetMetabaseService.findDatasetMetabase(datasetId).getDataSetName();
    if (null == tableName) {
      LOG.error("tableSchemaId not found: {}", tableSchemaId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.IDTABLESCHEMA_INCORRECT);
    }
    try {
      LOG.info("Exporting table data for datasetId {} and tableSchemaId {}", datasetId, tableSchemaId);
      fileTreatmentHelper.exportFile(datasetId, mimeType, tableSchemaId, tableName, exportFilterVO);
      LOG.info("Successfully exported table data for datasetId {} and tableSchemaId {}", datasetId, tableSchemaId);
    } catch (EEAException | IOException e) {
      LOG.error("Error exporting table data from dataset id {} and tableSchemaId {}. Message: {}", datasetId, tableSchemaId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.EXECUTION_ERROR);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error exporting file for datasetId {} and tableSchemaId {} Message: {}", datasetId, tableSchemaId, e.getMessage());
      throw e;
    }
  }

  /**
   * Export file DL.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param mimeType the mime type
   * @param exportFilterVO the export filter VO
   */
  @SneakyThrows
  @Override
  @HystrixCommand
  @PostMapping(value = "/exportFileDL")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASET_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT','REFERENCEDATASET_STEWARD') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiOperation(value = "Export file with data", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully export"),
          @ApiResponse(code = 400, message = "Id table schema is incorrect"),
          @ApiResponse(code = 500, message = "Error exporting file")})
  public void exportFileDL(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @RequestParam("datasetId") Long datasetId,
          @ApiParam(type = "String", value = "table schema Id",
                  example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
                  required = false) String tableSchemaId,
          @ApiParam(type = "String", value = "mimeType (file extension)",
                  example = "csv") @RequestParam("mimeType") String mimeType,
          @RequestBody ExportFilterVO exportFilterVO) {
    String tableName =
            null != tableSchemaId ? datasetSchemaService.getTableSchemaName(null, tableSchemaId)
                    : datasetMetabaseService.findDatasetMetabase(datasetId).getDataSetName();
    if (null == tableName) {
      LOG.error("tableSchemaId not found: {}", tableSchemaId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              EEAErrorMessage.IDTABLESCHEMA_INCORRECT);
    }
    try {
      LOG.info("Exporting table data for datasetId {} and tableSchemaId {}", datasetId, tableSchemaId);
      fileTreatmentHelper.exportFileDL(datasetId, mimeType, tableSchemaId, tableName, exportFilterVO);
      LOG.info("Successfully exported table data for datasetId {} and tableSchemaId {}", datasetId, tableSchemaId);
    } catch (EEAException | IOException e) {
      LOG.error("Error exporting table data from dataset id {} and tableSchemaId {}. Message: {}", datasetId, tableSchemaId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.EXECUTION_ERROR);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error exporting file for datasetId {} and tableSchemaId {} Message: {}", datasetId, tableSchemaId, e.getMessage());
      throw e;
    }
  }

  /**
   * Export file through integration.
   *
   * @param datasetId the dataset id
   * @param integrationId the integration id
   */
  @Override
  @HystrixCommand
  @GetMapping("/exportFileThroughIntegration")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASET_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT')")
  @ApiOperation(value = "Export file through integration", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully exported"),
          @ApiResponse(code = 500, message = "Error exporting file")})
  public void exportFileThroughIntegration(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @RequestParam("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Integration id",
                  example = "0") @RequestParam("integrationId") Long integrationId) {

    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDatasetId(datasetId);
    DataSetMetabaseVO datasetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
    userNotificationContentVO.setDatasetName(datasetMetabaseVO.getDataSetName());
    notificationControllerZuul.createUserNotificationPrivate("EXTERNAL_EXPORT_DESIGN_INIT",
            userNotificationContentVO);

    try {
      LOG.info("Exporting data through integration for datasetId {} and integrationId {}", datasetId, integrationId);
      datasetService.exportFileThroughIntegration(datasetId, integrationId);
      LOG.info("Successfully exported data through integration for datasetId {} and integrationId {}", datasetId, integrationId);
    } catch (EEAException e) {
      LOG.error("Error exporting file through integration for datasetId {} and integrationId {} Message: {}", datasetId, integrationId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.EXPORTING_FILE_INTEGRATION);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error exporting file through integration  with id {} for datasetId {} Message: {}", integrationId, datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Insert id data schema.
   *
   * @param datasetId the dataset id
   * @param idDatasetSchema the id dataset schema
   */
  @Override
  @HystrixCommand
  @PostMapping("/private/{id}/insertIdSchema")
  @ApiOperation(value = "Insert dataschema id", hidden = true)
  public void insertIdDataSchema(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("id") Long datasetId,
          @ApiParam(type = "String", value = "dataset schema Id",
                  example = "19D0B971B7E0D2FB66B77F2A8DBA4964") @RequestParam("idDatasetSchema") String idDatasetSchema) {
    try {
      LOG.info("Inserting dataSchema for datasetId {} and dataSchemaId {}", datasetId, idDatasetSchema);
      datasetService.insertSchema(datasetId, idDatasetSchema);
      LOG.info("Successfully inserted dataSchema for datasetId {} and dataSchemaId {}", datasetId, idDatasetSchema);
    } catch (EEAException e) {
      LOG.error("Error inserting dataSchema for datasetId {} and dataSchemaId {} Message: {}", datasetId, idDatasetSchema, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.INSERTING_DATASCHEMA);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error inserting dataSchema with id {} for datasetId {} Message: {}", idDatasetSchema, datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Update field.
   *
   * @param datasetId the dataset id
   * @param field the field
   * @param updateCascadePK the update cascade PK
   * @param recordId the recordId
   * @param tableSchemaId the tableSchemaId
   */
  @SneakyThrows
  @Override
  @HystrixCommand
  @PutMapping("/{id}/updateField")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD', 'TESTDATASET_STEWARD')")
  @ApiOperation(value = "Update field", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully updated field"),
          @ApiResponse(code = 404, message = "Error updating field, field not found"),
          @ApiResponse(code = 400, message = "Error updating field, table is read only")})
  public void updateField(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("id") Long datasetId,
          @ApiParam(value = "Field Object") @RequestBody FieldVO field,
          @ApiParam(type = "boolean", value = "update cascade", example = "true") @RequestParam(
                  value = "updateCascadePK", required = false) boolean updateCascadePK,
          @RequestParam(value = "recordId", required = false) String recordId,
          @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId) {
    if (datasetService.checkIfDatasetLockedOrReadOnly(datasetId, field.getIdFieldSchema(),
            EntityTypeEnum.FIELD)) {
      LOG.error("Error updating a field in the dataset {}. The table is read only",
              datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
    }
    try {
      Long dataflowId = datasetService.getDataFlowIdById(datasetId);
      DataFlowVO dataFlowVO = dataFlowControllerZuul.findById(dataflowId, null);

      if(dataFlowVO.getBigData() != null && dataFlowVO.getBigData()) {
        String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
        Long providerId = datasetService.getDataProviderIdById(datasetId);
        TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);
        if(tableSchemaVO != null && BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable())
                && BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaVO.getIdTableSchema()))) {
          bigDataDatasetService.updateField(dataflowId, providerId, datasetId, field, recordId, tableSchemaVO.getNameTableSchema(), updateCascadePK);
        }
        else{
          throw new Exception("The table data are not manually editable or the iceberg table has not been created");
        }
      }
      else {
        updateRecordHelper.executeFieldUpdateProcess(datasetId, field, updateCascadePK);
      }
    } catch (EEAException e) {
      LOG.error("Error updating a field in the dataset {}. Message: {}", datasetId,
              e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.UPDATING_FIELD);
    } catch (Exception e) {
      String fieldId = (field != null) ? field.getId() : null;
      LOG.error("Unexpected error! Error updating field with id {} for datasetId {} Message: {}", fieldId, datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Gets the field values referenced.
   *
   * @param datasetIdOrigin the dataset id origin
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @param conditionalValue the conditional value
   * @param searchValue the search value
   * @param resultsNumber the results number
   * @return the field values referenced
   */
  @Override
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "Get field values referenced", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
          @ApiResponse(code = 500, message = "Error getting data")})
  @GetMapping("/{id}/datasetSchemaId/{datasetSchemaId}/fieldSchemaId/{fieldSchemaId}/getFieldsValuesReferenced")
  public List<FieldVO> getFieldValuesReferenced(@PathVariable("id") Long datasetIdOrigin,
                                                @PathVariable("datasetSchemaId") String datasetSchemaId,
                                                @PathVariable("fieldSchemaId") String fieldSchemaId,
                                                @RequestParam(value = "conditionalValue", required = false) String conditionalValue,
                                                @RequestParam(value = "searchValue", required = false) String searchValue,
                                                @RequestParam(value = "resultsNumber", required = false) Integer resultsNumber) {
    try {
      return datasetService.getFieldValuesReferenced(datasetIdOrigin, datasetSchemaId,
              fieldSchemaId, conditionalValue, searchValue, resultsNumber);
    } catch (EEAException e) {
      LOG.error("Error with dataset id {}  caused {}", datasetIdOrigin, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.RETRIEVING_REFERENCED_FIELD);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving field values referenced for datasetSchemaId {} and fieldSchemaId {} Message: {}", datasetSchemaId, fieldSchemaId, e.getMessage());
      throw e;
    }
  }

  /**
   * Etl export dataset.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param tableSchemaId the table schema id
   * @param limit the limit
   * @param offset the offset
   * @param filterValue the filter value
   * @param columnName the column name
   * @param dataProviderCodes the data provider codes
   * @return the ETL dataset VO
   */
  @Override
  @GetMapping("/v1/{datasetId}/etlExport")
  @HystrixCommand(commandProperties = {@HystrixProperty(
          name = "execution.isolation.thread.timeoutInMilliseconds", value = "7200000")})
  @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','EUDATASET_STEWARD','DATACOLLECTION_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','DATACOLLECTION_CUSTODIAN','DATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','TESTDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT')")
  @ApiOperation(value = "Export data by dataset id",
          notes = "Allowed roles: \n\n Reporting dataset: CUSTODIAN, STEWARD, OBSERVER, REPORTER WRITE, REPORTER READ, LEAD REPORTER, STEWARD SUPPORT \n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE, EDITOR READ\n\n EU dataset: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT\n\n Data collection: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully exported"),
          @ApiResponse(code = 500, message = "Error exporting data"),
          @ApiResponse(code = 403, message = "Error dataset not belong dataflow")})
  public ResponseEntity<StreamingResponseBody> etlExportDataset(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam("dataflowId") Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
          @ApiParam(type = "String", value = "Table schema id",
                  example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
                  required = false) String tableSchemaId,
          @ApiParam(type = "Integer", value = "Limit", example = "0") @RequestParam(value = "limit",
                  defaultValue = "10000") Integer limit,
          @ApiParam(type = "Integer", value = "Offset", example = "0") @RequestParam(value = "offset",
                  defaultValue = "0") Integer offset,
          @ApiParam(type = "String", value = "Filter value", example = "value") @RequestParam(
                  value = "filterValue", required = false) String filterValue,
          @ApiParam(type = "String", value = "Filter column name", example = "column") @RequestParam(
                  value = "columnName", required = false) String columnName,
          @ApiParam(type = "String", value = "Data provider codes", example = "BE,DK") @RequestParam(
                  value = "dataProviderCodes", required = false) String dataProviderCodes) {

    if (!dataflowId.equals(datasetService.getDataFlowIdById(datasetId))) {
      String errorMessage =
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId);
      LOG.error(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId));
    }
    try {
      LOG.info("Calling etlExport for dataflowId {} and datasetId {}", dataflowId, datasetId);
      StreamingResponseBody responsebody = outputStream -> datasetService.etlExportDataset(datasetId,
              outputStream, tableSchemaId, limit, offset, filterValue, columnName, dataProviderCodes);
      LOG.info("Successfully called etlExport for dataflowId {} and datasetId {}",dataflowId, datasetId);
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_STREAM_JSON).body(responsebody);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error in etlExportDataset for datasetId {} and tableSchemaId {} Message: {}", datasetId, tableSchemaId, e.getMessage());
      throw e;
    }
  }


  @Override
  @GetMapping("/v2/etlExport/{datasetId}")
  @HystrixCommand(commandProperties = {@HystrixProperty(
          name = "execution.isolation.thread.timeoutInMilliseconds", value = "7200000")})
  @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','EUDATASET_STEWARD','DATACOLLECTION_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','DATACOLLECTION_CUSTODIAN','DATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','REFERENCEDATASET_CUSTODIAN','TESTDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT')")
  @ApiOperation(value = "Export data by dataset id",
          notes = "Allowed roles: \n\n Reporting dataset: CUSTODIAN, STEWARD, OBSERVER, REPORTER WRITE, REPORTER READ, LEAD REPORTER, STEWARD SUPPORT \n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE, EDITOR READ\n\n EU dataset: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT\n\n Data collection: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully exported"),
          @ApiResponse(code = 500, message = "Error exporting data"),
          @ApiResponse(code = 403, message = "Error dataset not belong dataflow")})
  public ResponseEntity<StreamingResponseBody> etlExportDatasetV2(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam("dataflowId") Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
          @ApiParam(type = "String", value = "Table schema id",
                  example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
                  required = false) String tableSchemaId,
          @ApiParam(type = "Integer", value = "Limit", example = "0") @RequestParam(value = "limit",
                  defaultValue = "10000") Integer limit,
          @ApiParam(type = "Integer", value = "Offset", example = "0") @RequestParam(value = "offset",
                  defaultValue = "0") Integer offset,
          @ApiParam(type = "String", value = "Filter value", example = "value") @RequestParam(
                  value = "filterValue", required = false) String filterValue,
          @ApiParam(type = "String", value = "Filter column name", example = "column") @RequestParam(
                  value = "columnName", required = false) String columnName,
          @ApiParam(type = "String", value = "Data provider codes", example = "BE,DK") @RequestParam(
                  value = "dataProviderCodes", required = false) String dataProviderCodes) {

    if (!dataflowId.equals(datasetService.getDataFlowIdById(datasetId))) {
      String errorMessage =
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId);
      LOG.error(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId));
    }

    try {
      LOG.info("Calling etlExport v2 for dataflowId {} and datasetId {}", dataflowId, datasetId);
      StreamingResponseBody responsebody = outputStream -> datasetService.etlExportDataset(datasetId,
              outputStream, tableSchemaId, limit, offset, filterValue, columnName, dataProviderCodes);
      LOG.info("Successfully called etlExport v2 for dataflowId {} and datasetId {}", dataflowId, datasetId);

      return ResponseEntity.ok().contentType(MediaType.APPLICATION_STREAM_JSON).body(responsebody);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error in etlExportDatasetV2 for datasetId {} and tableSchemaId {} Message: {}", datasetId, tableSchemaId, e.getMessage());
      throw e;
    }
  }

  /**
   * Etl export dataset legacy.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param tableSchemaId the table schema id
   * @param limit the limit
   * @param offset the offset
   * @param filterValue the filter value
   * @param columnName the column name
   * @return the response entity
   */
  @Override
  @GetMapping("/{datasetId}/etlExport")
  @HystrixCommand(commandProperties = {@HystrixProperty(
          name = "execution.isolation.thread.timeoutInMilliseconds", value = "7200000")})
  @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','EUDATASET_STEWARD','DATACOLLECTION_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','DATACOLLECTION_CUSTODIAN','DATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','TESTDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT')")
  @ApiOperation(value = "Export data by dataset id", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully exported"),
          @ApiResponse(code = 500, message = "Error exporting data"),
          @ApiResponse(code = 403, message = "Error dataset not belong dataflow")})
  public ResponseEntity<StreamingResponseBody> etlExportDatasetLegacy(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam("dataflowId") Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
          @ApiParam(type = "String", value = "Table schema id",
                  example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
                  required = false) String tableSchemaId,
          @ApiParam(type = "Integer", value = "Limit", example = "0") @RequestParam(value = "limit",
                  defaultValue = "10000") Integer limit,
          @ApiParam(type = "Integer", value = "Offset", example = "0") @RequestParam(value = "offset",
                  defaultValue = "0") Integer offset,
          @ApiParam(type = "String", value = "Filter value", example = "value") @RequestParam(
                  value = "filterValue", required = false) String filterValue,
          @ApiParam(type = "String", value = "Filter column name", example = "column") @RequestParam(
                  value = "columnName", required = false) String columnName) {
    return this.etlExportDatasetV2(datasetId, dataflowId, providerId, tableSchemaId, limit, offset,
            filterValue, columnName, null);
  }

  /**
   * Etl export dataset.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param tableSchemaId the table schema id
   * @param limit the limit
   * @param offset the offset
   * @param filterValue the filter value
   * @param columnName the column name
   * @param dataProviderCodes the data provider codes
   * @return the ETL dataset VO
   */
  @Override
  @GetMapping("/v3/etlExport/{datasetId}")
  @HystrixCommand(commandProperties = {@HystrixProperty(
          name = "execution.isolation.thread.timeoutInMilliseconds", value = "7200000")})
  @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','EUDATASET_STEWARD','DATACOLLECTION_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','DATACOLLECTION_CUSTODIAN','DATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','TESTDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT')")
  @ApiOperation(value = "Export data by dataset id",
          notes = "Allowed roles: \n\n Reporting dataset: CUSTODIAN, STEWARD, OBSERVER, REPORTER WRITE, REPORTER READ, LEAD REPORTER, STEWARD SUPPORT \n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE, EDITOR READ\n\n EU dataset: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT\n\n Data collection: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully exported"),
          @ApiResponse(code = 500, message = "Error exporting data"),
          @ApiResponse(code = 403, message = "Error dataset not belong dataflow")})
  public Map<String, Object> etlExportDatasetWithJob(
          @ApiParam(type = "Long", value = "Dataset id",
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

    if (!dataflowId.equals(datasetService.getDataFlowIdById(datasetId))) {
      String errorMessage =
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId);
      LOG.error(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId));
    }
    try {
      Long jobId = jobControllerZuul.addFileExportJob(datasetId, dataflowId, providerId, tableSchemaId, limit, offset, filterValue, columnName, dataProviderCodes);
      Map<String, Object> result = new HashMap<>();
      String pollingUrl = "/orchestrator/jobs/pollForJobStatus/" + jobId + "?datasetId=" + datasetId + "&dataflowId=" + dataflowId;
      if(providerId != null){
        pollingUrl+= "&providerId=" + providerId;
      }
      result.put("pollingUrl", pollingUrl);
      result.put("status", "Preparing file");
      return result;
    } catch (Exception e) {
      LOG.error("Unexpected error! Error in v3 etlExportDataset for datasetId {} and tableSchemaId {} Message: {}", datasetId, tableSchemaId, e.getMessage());
      throw e;
    }
  }

  /**
   * Etl import dataset.
   *
   * @param datasetId the dataset id
   * @param etlDatasetVO the etl dataset VO
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   */
  @Override
  @PostMapping("/v1/{datasetId}/etlImport")
  @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @ApiOperation(value = "Import data by dataset id",
          notes = "Allowed roles: \n\n Reporting dataset: REPORTER WRITE, LEAD REPORTER \n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE\n\n EU dataset: CUSTODIAN, STEWARD")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully imported"),
          @ApiResponse(code = 500, message = "Error importing data"),
          @ApiResponse(code = 403, message = "Error dataset not belong dataflow")})
  public void etlImportDataset(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(value = "Data object") @RequestBody ETLDatasetVO etlDatasetVO,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam("dataflowId") Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId) {

    if (!dataflowId.equals(datasetService.getDataFlowIdById(datasetId))) {
      String errorMessage =
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId);
      LOG.error(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId));
    }
    // check if dataset is reportable
    if (!datasetService.isDatasetReportable(datasetId)) {
      LOG.error("The dataset {} is not reportable", datasetId);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              String.format(EEAErrorMessage.DATASET_NOT_REPORTABLE, datasetId));
    }

    try {
      LOG.info("Calling etlImport for dataflowId {} and datasetId {}", dataflowId, datasetId);
      fileTreatmentHelper.etlImportDataset(datasetId, etlDatasetVO, providerId);
      LOG.info("Successfully called etlImport for dataflowId {} and datasetId {}", dataflowId, datasetId);
    } catch (EEAException e) {
      LOG.error("The etlImportDataset failed on dataflowId {} and datasetId {} Message: {}", dataflowId, datasetId,
              e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.IMPORTING_DATA_DATASET);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error in etlImportDataset for dataflowId {} datasetId {} and providerId {} Message: {}", dataflowId, datasetId, providerId, e.getMessage());
      throw e;
    }
  }

  /**
   * Etl import dataset legacy.
   *
   * @param datasetId the dataset id
   * @param etlDatasetVO the etl dataset VO
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   */
  @Override
  @PostMapping("/{datasetId}/etlImport")
  @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @ApiOperation(value = "Import data by dataset id", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully imported"),
          @ApiResponse(code = 500, message = "Error importing data"),
          @ApiResponse(code = 403, message = "Error dataset not belong dataflow")})
  public void etlImportDatasetLegacy(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(value = "Data object") @RequestBody ETLDatasetVO etlDatasetVO,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam("dataflowId") Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId) {
    this.etlImportDataset(datasetId, etlDatasetVO, dataflowId, providerId);
  }

  /**
   * Gets the attachment.
   *
   * @param datasetId the dataset id
   * @param idField the field id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param tableSchemaName the table name
   * @param fieldName the field name
   * @param fileName the file name
   * @param recordId the recordId
   * @return the attachment
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/v1/{datasetId}/field/{fieldId}/attachment",
          produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiOperation(value = "Download attachment by field id",
          notes = "Allowed roles: \n\n Reporting dataset: CUSTODIAN, STEWARD, OBSERVER, REPORTER WRITE, REPORTER READ, LEAD REPORTER, STEWARD SUPPORT \n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE, EDITOR READ\n\n EU dataset: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT\n\n Data collection: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully getted"),
          @ApiResponse(code = 500, message = "Error getting attachment"),
          @ApiResponse(code = 404, message = "Error downloading attachment from dataset")})
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATACOLLECTION_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATASET_NATIONAL_COORDINATOR','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','DATACOLLECTION_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT','REFERENCEDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATACOLLECTION_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATASET_NATIONAL_COORDINATOR','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','DATACOLLECTION_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT','REFERENCEDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD')")
  public ResponseEntity<byte[]> getAttachment(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "String", value = "Field id",
                  example = "19D0B971B7E0D2FB66B77F2A8DBA4964") @PathVariable("fieldId") String idField,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam(value = "dataflowId") Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
          @ApiParam(type = "String", value = "Table schema name", example = "table") @RequestParam(value = "tableSchemaName", required = false) String tableSchemaName,
          @ApiParam(type = "String", value = "Field name", example = "table") @RequestParam(value = "fieldName", required = false) String fieldName,
          @ApiParam(type = "String", value = "File name", example = "file") @RequestParam(value = "fileName", required = false) String fileName,
          @ApiParam(type = "String", value = "Record id", example = "SDHFKSD792812") @RequestParam(value = "recordId", required = false) String recordId) {

    LOG.info("Downloading attachment from the datasetId {}", datasetId);
    try {
      DataFlowVO dataFlowVO = dataFlowControllerZuul.findById(dataflowId, providerId);
      byte[] file = null;
      String filename = null;
      if(dataFlowVO.getBigData() != null && dataFlowVO.getBigData()){
        AttachmentDLVO attachment = bigDataDatasetService.getAttachmentDL(datasetId, dataflowId, providerId, tableSchemaName, fieldName, fileName, recordId);
        file = attachment.getContent();
        filename = attachment.getFileName();
      }
      else{
        AttachmentValue attachment = datasetService.getAttachment(datasetId, idField);
        file = attachment.getContent();
        filename = attachment.getFileName();
      }
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
      return new ResponseEntity<>(file, httpHeaders, HttpStatus.OK);
    } catch (EEAException | IOException e) {
      LOG.error("Error downloading attachment from the datasetId {} and fieldId {}, with message: {}", datasetId, idField, e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DOWNLOADING_ATTACHMENT_IN_A_DATAFLOW);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving attachment for dataflowId {} datasetId {} fieldId {} and providerId {} Message: {}", dataflowId, datasetId, idField, providerId, e.getMessage());
      throw e;
    }
  }

  /**
   * Gets the attachment legacy.
   *
   * @param datasetId the dataset id
   * @param idField the field id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param tableSchemaName the table name
   * @param fieldName the field name
   * @param fileName the file name
   * @param recordId the recordId
   * @return the attachment legacy
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{datasetId}/field/{fieldId}/attachment",
          produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiOperation(value = "Download attachment by field id", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully getted"),
          @ApiResponse(code = 500, message = "Error getting attachments"),
          @ApiResponse(code = 404, message = "Error downloading attachment from dataset")})
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATACOLLECTION_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATASET_NATIONAL_COORDINATOR','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','DATACOLLECTION_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT','REFERENCEDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATACOLLECTION_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATASET_NATIONAL_COORDINATOR','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','DATACOLLECTION_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT','REFERENCEDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD')")
  public ResponseEntity<byte[]> getAttachmentLegacy(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "String", value = "Field id",
                  example = "19D0B971B7E0D2FB66B77F2A8DBA4964") @PathVariable("fieldId") String idField,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam(value = "dataflowId") Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
          @ApiParam(type = "String", value = "Table schema name", example = "table") @RequestParam(value = "tableSchemaName", required = false) String tableSchemaName,
          @ApiParam(type = "String", value = "Field name", example = "table") @RequestParam(value = "fieldName", required = false) String fieldName,
          @ApiParam(type = "String", value = "File name", example = "file") @RequestParam(value = "fileName", required = false) String fileName,
          @ApiParam(type = "String", value = "Record id", example = "SDHFKSD792812") @RequestParam(value = "recordId", required = false) String recordId) {
    return this.getAttachment(datasetId, idField, dataflowId, providerId, tableSchemaName, fieldName, fileName, recordId);
  }

  /**
   * Update attachment.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param idField the id field
   * @param file the file
   * @param tableSchemaName the table name
   * @param fieldName the field name
   * @param recordId the recordId
   * @param previousFileName the previousFileName
   */
  @Override
  @HystrixCommand
  @PutMapping("/v1/{datasetId}/field/{fieldId}/attachment")
  @ApiOperation(value = "Update attachment by field id",
          notes = "Allowed roles: \n\n Reporting dataset: LEAD REPORTER, REPORTER WRITE, NATIONAL COORDINATOR \n\n Data collection: CUSTODIAN, STEWARD\n\n Test dataset: CUSTODIAN, STEWARD , STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE\n\n EU dataset: CUSTODIAN, STEWARD")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully getted"),
          @ApiResponse(code = 500, message = "Error updating attachment"),
          @ApiResponse(code = 400, message = "Table is read only or file format is invalid")})
  public void updateAttachment(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
          @ApiParam(type = "String", value = "Field id",
                  example = "19D0B971B7E0D2FB66B77F2A8DBA4964") @PathVariable("fieldId") String idField,
          @ApiParam(value = "file") @RequestParam("file") MultipartFile file,
          @ApiParam(type = "String", value = "Table schema name", example = "table") @RequestParam(value = "tableSchemaName", required = false) String tableSchemaName,
          @ApiParam(type = "String", value = "Field name", example = "table") @RequestParam(value = "fieldName", required = false) String fieldName,
          @ApiParam(type = "String", value = "Record id", example = "SDHFKSD792812") @RequestParam(value = "recordId", required = false) String recordId,
          @ApiParam(type = "String", value = "Previous File Name", example = "file.txt") @RequestParam(value = "previousFileName", required = false) String previousFileName) {

    try {
      LOG.info("Method updateAttachment was called for dataflowId {} datasetId {} and fieldId {}", dataflowId, datasetId, idField);


      // Remove comma "," character to avoid error with special characters
      String fileName = file.getOriginalFilename();
      fileName = StringUtils.isNotBlank(fileName) ? fileName.replace(",", "") : "";


      LOG.info("Updating attachment for dataflowId {} and datasetId {}", dataflowId, datasetId);
      DataFlowVO dataFlowVO = dataFlowControllerZuul.findById(dataflowId, providerId);
      if(dataFlowVO.getBigData() != null && dataFlowVO.getBigData()){
        //check if table is read only
        String fieldSchemaId = datasetSchemaService.getFieldSchemaIdByDatasetIdTableNameAndFieldName(datasetId, tableSchemaName, fieldName);
        if(StringUtils.isNotBlank(fieldSchemaId)){
          if (datasetService.checkIfDatasetLockedOrReadOnly(datasetId, fieldSchemaId, EntityTypeEnum.FIELD)) {
            LOG.error("Error updating an attachment in the datasetId {}. In table {} field {} is read only",
                    datasetId, tableSchemaName, fieldName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
          }
        }

        //validate attachment
        if (!validateAttachment(datasetId, idField, fileName, file.getSize(), tableSchemaName, fieldName, true)
                || fileName.equals("")) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_FORMAT);
        }

        //upload attachment
        bigDataDatasetService.updateAttachmentDL(datasetId, dataflowId, providerId, tableSchemaName, fieldName, file, recordId, previousFileName);
      }
      else{
        // Not allow insert attachment if the table is marked as read only. This not applies to design datasets
        if (datasetService.checkIfDatasetLockedOrReadOnly(datasetId,
                datasetService.findFieldSchemaIdById(datasetId, idField), EntityTypeEnum.FIELD)) {
          LOG.error("Error updating an attachment in the datasetId {}. The table is read only",
                  datasetId);
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
        }
        if (!validateAttachment(datasetId, idField, fileName, file.getSize(), tableSchemaName, fieldName, false)
                || fileName.equals("")) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_FORMAT);
        }
        InputStream is = file.getInputStream();
        datasetService.updateAttachment(datasetId, idField, fileName, is);
      }

      LOG.info("Successfully updated attachment for dataflowId {} and datasetId {}", dataflowId, datasetId);
    } catch (EEAException | IOException e) {
      LOG.error("Error updating attachment from the dataflowId {} and datasetId {}, with message: {}", dataflowId,
              datasetId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.UPDATING_ATTACHMENT_IN_A_DATAFLOW);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving attachment for dataflowId {} datasetId {} fieldId {} and providerId {} Message: {}", dataflowId, datasetId, idField, providerId, e.getMessage());
      throw e;
    }
  }


  /**
   * Update attachment.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param idField the id field
   * @param file the file
   * @param tableSchemaName the table name
   * @param fieldName the field name
   * @param recordId the recordId
   * @param previousFileName the previousFileName
   */
  @Override
  @HystrixCommand
  @PutMapping("/{datasetId}/field/{fieldId}/attachment")
  @ApiOperation(value = "Update attachment by field id", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully getted"),
          @ApiResponse(code = 500, message = "Error updating attachment"),
          @ApiResponse(code = 400, message = "Table is read only or file format is invalid ")})
  public void updateAttachmentLegacy(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
          @ApiParam(type = "String", value = "Field id",
                  example = "19D0B971B7E0D2FB66B77F2A8DBA4964") @PathVariable("fieldId") String idField,
          @ApiParam(value = "file") @RequestParam("file") MultipartFile file,
          @ApiParam(type = "String", value = "Table schema name", example = "table") @RequestParam(value = "tableSchemaName", required = false) String tableSchemaName,
          @ApiParam(type = "String", value = "Field name", example = "table") @RequestParam(value = "fieldName", required = false) String fieldName,
          @ApiParam(type = "String", value = "Record id", example = "SDHFKSD792812") @RequestParam(value = "recordId", required = false) String recordId,
          @ApiParam(type = "String", value = "Previous File Name", example = "file.txt") @RequestParam(value = "previousFileName", required = false) String previousFileName) {
    this.updateAttachment(datasetId, dataflowId, providerId, idField, file, tableSchemaName, fieldName, recordId, previousFileName);
  }

  /**
   * Delete attachment.
   *
   * @param datasetId the dataset id
   * @param idField the field id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param tableSchemaName the table name
   * @param fieldName the field name
   * @param fileName the file name
   * @param recordId the recordId
   */
  @Override
  @HystrixCommand
  @ApiOperation(value = "Delete attachment by field id",
          notes = "Allowed roles: \n\n Reporting dataset: LEAD REPORTER, REPORTER WRITE, NATIONAL COORDINATOR \n\n Data collection: CUSTODIAN, STEWARD, OBSERVER\n\n Test dataset: CUSTODIAN, STEWARD ,OBSERVER, STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE\n\n EU dataset: CUSTODIAN, STEWARD")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @DeleteMapping("/v1/{datasetId}/field/{fieldId}/attachment")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully deleted"),
          @ApiResponse(code = 404, message = "Error deleting attachment from dataset"),
          @ApiResponse(code = 400, message = "Table is read only")})
  public void deleteAttachment(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
          @ApiParam(type = "String", value = "Field id",
                  example = "19D0B971B7E0D2FB66B77F2A8DBA4964") @PathVariable("fieldId") String idField,
          @ApiParam(type = "String", value = "Table schema name", example = "table") @RequestParam(value = "tableSchemaName", required = false) String tableSchemaName,
          @ApiParam(type = "String", value = "Field name", example = "table") @RequestParam(value = "fieldName", required = false) String fieldName,
          @ApiParam(type = "String", value = "File name", example = "file") @RequestParam(value = "fileName", required = false) String fileName,
          @ApiParam(type = "String", value = "Record id", example = "SDHFKSD792812") @RequestParam(value = "recordId", required = false) String recordId) {

    try {
      LOG.info("Deleting attachment for dataflowId {}, datasetId {} and fieldId {}", dataflowId, datasetId, idField);
      DataFlowVO dataFlowVO = dataFlowControllerZuul.findById(dataflowId, providerId);
      if(dataFlowVO.getBigData() != null && dataFlowVO.getBigData()){
        //check if table is read only
        String fieldSchemaId = datasetSchemaService.getFieldSchemaIdByDatasetIdTableNameAndFieldName(datasetId, tableSchemaName, fieldName);
        if(StringUtils.isNotBlank(fieldSchemaId)){
          if (datasetService.checkIfDatasetLockedOrReadOnly(datasetId, fieldSchemaId, EntityTypeEnum.FIELD)) {
            LOG.error("Error deleting an attachment in the datasetId {}. In table {} field {} is read only",
                    datasetId, tableSchemaName, fieldName);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
          }
        }
        bigDataDatasetService.deleteAttachmentDL(datasetId, dataflowId, providerId, tableSchemaName, fieldName, fileName, recordId);
      }
      else{
        if (datasetService.checkIfDatasetLockedOrReadOnly(datasetId,
                datasetService.findFieldSchemaIdById(datasetId, idField), EntityTypeEnum.FIELD)) {
          LOG.error("Error deleting an attachment in the datasetId {}. The table is read only",
                  datasetId);
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
        }
        datasetService.deleteAttachment(datasetId, idField);
      }
      LOG.info("Successfully deleted attachment for dataflowId {}, datasetId {} and fieldId {}", dataflowId, datasetId, idField);
    } catch (EEAException e) {
      LOG.error("Error deleting attachment from dataflowId {}, datasetId {} and fieldId {}, with message: {}",
              dataflowId, datasetId, idField, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
              EEAErrorMessage.DELETING_ATTACHMENT_IN_A_DATAFLOW);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error deleting attachment for dataflowId {} datasetId {} fieldId {} and providerId {} Message: {}", dataflowId, datasetId, idField, providerId, e.getMessage());
      throw e;
    }
  }


  /**
   * Delete attachment legacy.
   *
   * @param datasetId the dataset id
   * @param idField the field id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param tableSchemaName the table name
   * @param fieldName the field name
   * @param fileName the file name
   * @param recordId the recordId
   */
  @Override
  @HystrixCommand
  @ApiOperation(value = "Delete attachment by field id", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @DeleteMapping("/{datasetId}/field/{fieldId}/attachment")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully deleted"),
          @ApiResponse(code = 404, message = "Error deleting attachment from dataset"),
          @ApiResponse(code = 400, message = "Table is read only")})
  public void deleteAttachmentLegacy(
          @ApiParam(type = "Long", value = "Dataset id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id",
                  example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id",
                  example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
          @ApiParam(type = "String", value = "Field id",
                  example = "19D0B971B7E0D2FB66B77F2A8DBA4964") @PathVariable("fieldId") String idField,
          @ApiParam(type = "String", value = "Table schema name", example = "table") @RequestParam(value = "tableSchemaName", required = false) String tableSchemaName,
          @ApiParam(type = "String", value = "Field name", example = "table") @RequestParam(value = "fieldName", required = false) String fieldName,
          @ApiParam(type = "String", value = "File name", example = "file") @RequestParam(value = "fileName", required = false) String fileName,
          @ApiParam(type = "String", value = "Record id", example = "SDHFKSD792812") @RequestParam(value = "recordId", required = false) String recordId) {
    this.deleteAttachment(datasetId, dataflowId, providerId, idField, tableSchemaName, fieldName, fileName, recordId);
  }

  /**
   * Gets the referenced dataset id.
   *
   * @param datasetIdOrigin the dataset id origin
   * @param idFieldSchema the id field schema
   * @return the referenced dataset id
   */
  @Override
  @GetMapping("/private/getReferencedDatasetId")
  @ApiOperation(value = "Get referenced by dataset id", hidden = true)
  public Long getReferencedDatasetId(
          @ApiParam(type = "Long", value = "Dataset Id origin",
                  example = "0") @PathVariable("dataflowId") @RequestParam("id") Long datasetIdOrigin,
          @ApiParam(type = "String", value = "Field Schema Id",
                  example = "5cf0e9b3b793310e9ceca190") @RequestParam("idFieldSchema") String idFieldSchema) {
    return datasetService.getReferencedDatasetId(datasetIdOrigin, idFieldSchema);
  }

  /**
   * Gets the dataset type.
   *
   * @param datasetId the dataset id
   * @return the dataset type
   */
  @Override
  @GetMapping("/private/datasetType/{datasetId}")
  @ApiOperation(value = "Get Dataset Type by dataset Id", hidden = true)
  public DatasetTypeEnum getDatasetType(@ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("datasetId") Long datasetId) {
    return datasetMetabaseService.getDatasetType(datasetId);
  }



  /**
   * Delete data before replacing.
   *
   * @param datasetId the dataset id
   * @param integrationId the integration id
   * @param operation the operation
   */
  @Override
  @DeleteMapping("/private/{id}/deleteForReplacing")
  @ApiOperation(value = "delete data before replacing", hidden = true)
  public void deleteDataBeforeReplacing(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("id") Long datasetId,
          @ApiParam(type = "Long", value = "Integration Id",
                  example = "0") @RequestParam("integrationId") Long integrationId,
          @ApiParam(
                  value = "Operation Object") @RequestParam("operation") IntegrationOperationTypeEnum operation) {
    // When deleting the data finishes, we send a kafka event to make the FME call to import data
    ThreadPropertiesManager.setVariable("user",
            SecurityContextHolder.getContext().getAuthentication().getName());
    try {
      LOG.info("Deleting data before replacing for datasetId {} and integrationId {}", datasetId, integrationId);
      deleteHelper.executeDeleteImportDataAsyncBeforeReplacing(datasetId, integrationId, operation);
      LOG.info("Successfully deleting data before replacing for datasetId {} and integrationId {}", datasetId, integrationId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error deleting data before replacing for datasetId {} and integrationId {} Message: {}", datasetId, integrationId, e.getMessage());
      throw e;
    }
  }


  /**
   * Export public file.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider I
   * @param fileName the file name
   * @return the http entity
   */
  @Override
  @ApiOperation(value = "export public file", hidden = true)
  @GetMapping("/exportPublicFile/dataflow/{dataflowId}/dataProvider/{dataProviderId}")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully exported file"),
          @ApiResponse(code = 404, message = "File doesn't exist")})
  public ResponseEntity<InputStreamResource> exportPublicFile(
          @ApiParam(type = "Long", value = "Dataflow Id", example = "0") @PathVariable Long dataflowId,
          @ApiParam(type = "Long", value = "provider Id",
                  example = "0") @PathVariable Long dataProviderId,
          @ApiParam(type = "String", value = "File Name",
                  example = "value") @RequestParam String fileName) {

    try {
      LOG.info("Exporting public file {} for dataflowId {}", fileName, dataflowId);
      File zipContent = datasetService.exportPublicFile(dataflowId, dataProviderId, fileName);
      LOG.info("Successfully exported public file {} for dataflowId {}", fileName, dataflowId);
      return createResponseEntity(fileName, zipContent);
    } catch (IOException | EEAException e) {
      LOG.error("File doesn't exist in the route {} for dataflowId {} ", fileName, dataflowId);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error exporting public file {} for dataflowId {} and dataProviderId {} Message: {}", fileName, dataflowId, dataProviderId, e.getMessage());
      throw e;
    }
  }


  /**
   * Export reference dataset file.
   *
   * @param dataflowId the dataflow id
   * @param fileName the file name
   * @return the response entity
   */
  @Override
  @ApiOperation(value = "export reference public file", hidden = true)
  @GetMapping("/exportPublicFile/dataflow/{dataflowId}")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully exported file"),
          @ApiResponse(code = 404, message = "File doesn't exist")})
  public ResponseEntity<InputStreamResource> exportReferenceDatasetFile(
          @ApiParam(type = "Long", value = "Dataflow Id", example = "0") @PathVariable Long dataflowId,
          @ApiParam(type = "String", value = "File name",
                  example = "filename") @RequestParam String fileName) {

    try {
      LOG.info("Exporting reference public file {} for dataflowId {}", fileName, dataflowId);
      File zipContent = datasetService.exportPublicFile(dataflowId, null, fileName);
      LOG.info("Successfully exported reference public file {} for dataflowId {}", fileName, dataflowId);
      return createResponseEntity(fileName, zipContent);
    } catch (IOException | EEAException e) {
      LOG.error("File doesn't exist in the route {} for dataflowId {} ", fileName, dataflowId);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error exporting public file {} for dataflowId {}  Message: {}", fileName, dataflowId, e.getMessage());
      throw e;
    }
  }


  /**
   * Check any schema available in public.
   *
   * @param dataflowId the dataflow id
   * @return true, if successful
   */
  @Override
  @ApiOperation(value = "Check if any schema is public", hidden = true)
  @GetMapping("/private/checkAnySchemaAvailableInPublic")
  public boolean checkAnySchemaAvailableInPublic(@ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @RequestParam("dataflowId") Long dataflowId) {
    return datasetService.checkAnySchemaAvailableInPublic(dataflowId);
  }


  /**
   * Export dataset file.
   *
   * @param datasetId the dataset id
   * @param mimeType the mime type
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{datasetId}/exportDatasetFile")
  @ApiOperation(value = "Export dataset file", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  public void exportDatasetFile(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "String", value = "mime type (extension file)",
                  example = "csv") @RequestParam("mimeType") String mimeType) {
    LOG.info("Exporting dataset data for datasetId {}, with type {}", datasetId, mimeType);
    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDatasetId(datasetId);
    notificationControllerZuul.createUserNotificationPrivate("EXPORT_DATASET_DATA",
            userNotificationContentVO);

    try {
      fileTreatmentHelper.exportDatasetFile(datasetId, mimeType);
      LOG.info("Successfully exported dataset data from datasetId {}, with type {}", datasetId, mimeType);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error exporting dataset file for datasetId {} Message: {}", datasetId, e.getMessage());
      throw e;
    }
  }


  /**
   * Export dataset file DL.
   *
   * @param datasetId the dataset id
   * @param mimeType the mime type
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{datasetId}/exportDatasetFileDL")
  @ApiOperation(value = "Export dataset file", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  public void exportDatasetFileDL(
          @ApiParam(type = "Long", value = "Dataset Id", example = "0") @PathVariable("datasetId")
          Long datasetId,
          @ApiParam(type = "String", value = "mime type (extension file)", example = "csv")
          @RequestParam("mimeType") String mimeType) {
    LOG.info("Exporting dataset data for datasetId {}, with type {}", datasetId, mimeType);
    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDatasetId(datasetId);
    notificationControllerZuul.createUserNotificationPrivate("EXPORT_DATASET_DATA",
            userNotificationContentVO);

    try {
      fileTreatmentHelper.exportDatasetFileDL(datasetId, mimeType);
      LOG.info("Successfully exported dataset data from datasetId {}, with type {}", datasetId, mimeType);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error exporting dataset file for datasetId {} Message: {}", datasetId, e.getMessage());
    }
  }

  /**
   * Download file.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param response the response
   */
  @Override
  @GetMapping(value = "/{datasetId}/downloadFile",
          produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASET_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiOperation(value = "Download file", hidden = true)
  public void downloadFile(
          @ApiParam(type = "Long", value = "Dataset Id", example = "0") @PathVariable Long datasetId,
          @ApiParam(type = "String", value = "File name",
                  example = "file.csv") @RequestParam String fileName,
          @ApiParam(value = "response") HttpServletResponse response) {
    try {
      LOG.info("Downloading file generated from export dataset. DatasetId {} Filename {}",
              datasetId, fileName);
      File file = datasetService.downloadExportedFile(datasetId, fileName);
      LOG.info("Successfully downloaded file generated from export dataset. DatasetId {} Filename {}",
              datasetId, fileName);
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

      OutputStream out = response.getOutputStream();
      try (FileInputStream in = new FileInputStream(file)) {
        // copy from in to out
        IOUtils.copyLarge(in, out);
        out.close();
        in.close();
        // delete the file after downloading it
        FileUtils.forceDelete(file);
      } catch (Exception e) {
        LOG.error("Unexpected error! Error in copying large file {} for datasetId {}. Message: {}", fileName, datasetId, e.getMessage());
        throw e;
      }
    } catch (IOException | EEAException e) {
      LOG.error(
              "Error downloading file generated from export from the datasetId {}. Filename {}. Message: {}",
              datasetId, fileName, e.getMessage());
    } catch (Exception e) {
      LOG.error("Unexpected error! Error downloading file {} for datasetId {} Message: {}", fileName, datasetId, e.getMessage());
      throw e;
    }
  }



  /**
   * Download file DL.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param response the response
   */
  @Override
  @GetMapping(value = "/{datasetId}/downloadFileDL",
          produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASET_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiOperation(value = "Download file", hidden = true)
  public void downloadFileDL(
          @ApiParam(type = "Long", value = "Dataset Id", example = "0") @PathVariable Long datasetId,
          @ApiParam(type = "String", value = "File name", example = "file.csv") @RequestParam
          String fileName, @ApiParam(value = "response") HttpServletResponse response) {
    try {
      LOG.info("Downloading file generated from export dataset. DatasetId {} Filename {}",
              datasetId, fileName);
      File file = datasetService.downloadExportedFileDL(datasetId, fileName);
      LOG.info("Successfully downloaded file generated from export dataset. DatasetId {} Filename {}",
              datasetId, fileName);
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

      OutputStream out = response.getOutputStream();
      try (FileInputStream in = new FileInputStream(file)) {
        // copy from in to out
        IOUtils.copyLarge(in, out);
        out.close();
        in.close();
      } catch (Exception e) {
        LOG.error("Unexpected error! Error in copying large file {} for datasetId {}. Message: {}", fileName, datasetId, e.getMessage(), e);
        throw e;
      }
    } catch (IOException | EEAException e) {
      LOG.error(
              "Error downloading file generated from export from the datasetId {}. Filename {}. Message: {}",
              datasetId, fileName, e.getMessage(), e);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error downloading file {} for datasetId {} Message: {}", fileName, datasetId, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * Update check view.
   *
   * @param datasetId the dataset id
   * @param updated the updated
   */
  @Override
  @PutMapping("/private/viewUpdated/{datasetId}")
  @ApiOperation(value = "Mark the view as updated or not", hidden = true)
  public void updateCheckView(
          @ApiParam(type = "Long", value = "Dataset Id",
                  example = "0") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Boolean", value = "Updated",
                  example = "true/false") @RequestParam Boolean updated) {
    LOG.info("Updating check view for datasetId {}. Value is {}", datasetId, updated);
    datasetService.updateCheckView(datasetId, updated);
    LOG.info("Successfully updated check view for datasetId {}. Value is {}", datasetId, updated);
  }

  /**
   * Gets the check view.
   *
   * @param datasetId the dataset id
   * @return the check view
   */
  @Override
  @GetMapping("/{datasetId}/viewUpdated")
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "Mark the view as updated or not", hidden = true)
  public Boolean getCheckView(@ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("datasetId") Long datasetId) {
    return datasetService.getCheckView(datasetId);
  }

  /**
   * Delete temp etl export.
   *
   * @param datasetId the dataset id
   */
  @Override
  @DeleteMapping("/private/deleteTempEtlExport/{datasetId}")
  @ApiOperation(value = "Empty the temporary etlExport table from the dataset schema DB",
          hidden = true)
  public void deleteTempEtlExport(@PathVariable("datasetId") Long datasetId) {
    try {
      LOG.info("Deleting everything from temp_etlexport table for datasetId {}", datasetId);
      datasetService.deleteTempEtlExport(datasetId);
      LOG.info("Successfully deleted everything from temp_etlexport table for datasetId {}", datasetId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error deleting tempEtlExport table for datasetId {} Message: {}", datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Test import process.
   *
   * @param datasetId the dataset id
   */
  @HystrixCommand
  @GetMapping(value = "/checkImportProcess/{datasetId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Test import file to dataset data (Large files)", notes = "Allowed roles: \n\n Reporting dataset: LEAD REPORTER, REPORTER WRITE, NATIONAL COORDINATOR \n\n Data collection: CUSTODIAN, STEWARD\n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE\n\n EU dataset: CUSTODIAN, STEWARD")
  @PreAuthorize("isAuthenticated()")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "There is no import process in progress"),
          @ApiResponse(code = 400, message = "Error testing import process"),
          @ApiResponse(code = 500, message = "Error testing import process")})
  public ResponseEntity<CheckLockVO> checkImportProcess(
          @ApiParam(type = "Long", value = "Dataset id", example = "0")
          @LockCriteria(name = "datasetId") @PathVariable("datasetId") Long datasetId) {

    LOG.info("Method testImportProcess called for dataset id: {}", datasetId);
    CheckLockVO checkLockVO = new CheckLockVO();

    try {
      Map<String, Object> importData = new HashMap<>();

      // Check if the IMPORT_FILE_DATA process is running and locked
      importData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_FILE_DATA.getValue());
      importData.put(LiteralConstants.DATASETID, datasetId);
      LockVO importDataCriteria = lockService.findByCriteria(importData);

      //Clear map
      importData.clear();

      // Check if the IMPORT_BIG_FILE_DATA process is running and locked
      importData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_BIG_FILE_DATA.getValue());
      importData.put(LiteralConstants.DATASETID, datasetId);
      LockVO importBigDataCriteria = lockService.findByCriteria(importData);

      if (importDataCriteria == null && importBigDataCriteria == null) {
        checkLockVO.setImportInProgress(Boolean.FALSE);
        checkLockVO.setMessage(LiteralConstants.NO_IMPORT_IN_PROGRESS);
      } else {
        checkLockVO.setImportInProgress(Boolean.TRUE);
        checkLockVO.setMessage(LiteralConstants.IMPORT_LOCKED);
      }

      LOG.info("Method testImportProcess result for dateset id: {}, checkLockVO: {}", datasetId, checkLockVO);
    } catch (Exception e) {
      LOG.error("Error while executing method testImportProcess for dataset id: {} with exception: {}", datasetId, e);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(checkLockVO, HttpStatus.OK);
  }


  /**
   * Check all locks with criteria
   *
   * @param datasetId
   * @param dataflowId
   * @param dataProviderId
   * @return ResponseEntity<List<LockVO>>
   */
  @PostMapping(value = "/checkLocks", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get Locks for input data", hidden = true)
  public ResponseEntity<List<LockVO>> checkLocks(
          @ApiParam(type = "Long", value = "Dataset id", example = "0" )@RequestParam("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataset id", example = "0") @RequestParam("dataflowId") Long dataflowId,
          @ApiParam(type = "Long", value = "Dataset id", example = "0") @RequestParam("dataProviderId") Long dataProviderId) {

    LOG.info("Method checkLocks called for datasetId: {}, dataflowId: {}, dataProviderId: {}", datasetId, dataflowId, dataProviderId);

    List<LockVO> results = new ArrayList<>();
    try {

      List<LockVO> locks = lockService.findAll();

      //Get locks by dataset id
      if (datasetId != null) {
        results.addAll(lockService.findAllByCriteria(locks, datasetId));
      }

      //Get locks by dataflow id and then parse these results by data provider id
      if (dataflowId != null) {
        List<LockVO> dataflowLocks = lockService.findAllByCriteria(locks, dataflowId);
        if (dataProviderId != null) {
          results.addAll(lockService.findAllByCriteria(dataflowLocks, dataProviderId));
        } else {
          results.addAll(dataflowLocks);
        }
      }

      LOG.info("Method checkLocks results: {},", results);
    } catch (Exception e) {
      LOG.error("Error while executing method checkLocks for datasetId: {}, dataflowId: {}, dataProviderId: {}", datasetId, dataflowId, dataProviderId, e);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(results, HttpStatus.OK);
  }


  /**
   * Gets dataset Data to be truncated
   *
   * @param datasetId
   * @param dataProviderId
   * @return
   */
  @PostMapping("/private/getDatasetData")
  @ApiOperation(value = "Get dataset data to be truncated", hidden = true)
  public TruncateDataset getDatasetData(
          @RequestParam(value = "datasetId") Long datasetId,
          @RequestParam(value = "dataProviderId") Long dataProviderId) {
    LOG.info("Method getDatasetData called for datasetId: {} and dataProviderId {}", datasetId, dataProviderId);
    if (datasetId == null || dataProviderId == null) {
      return null;
    }

    TruncateDataset datasetDataToBeDeleted = new TruncateDataset();
    try {
      datasetDataToBeDeleted = datasetService.getDatasetDataToBeDeleted(datasetId, dataProviderId);
    } catch (Exception e) {
      LOG.error("Error while executing method getDatasetData for datasetId: {}, dataProviderId: {}", datasetId, dataProviderId, e);
    }

    LOG.info("Method getDatasetData returns truncateDataset: {}", datasetDataToBeDeleted);
    return datasetDataToBeDeleted;
  }

  @PostMapping("/v2/getDatasetData")
  @PreAuthorize("hasAnyRole('ADMIN')")
  @ApiOperation(value = "Get dataset data to be truncated", hidden = true)
  public TruncateDataset getDatasetDataV2(
          @RequestParam(value = "datasetId") Long datasetId,
          @RequestParam(value = "dataProviderId") Long dataProviderId) {
    LOG.info("Method getDatasetData called for datasetId: {} and dataProviderId {}", datasetId, dataProviderId);
    if (datasetId == null || dataProviderId == null) {
      return null;
    }

    TruncateDataset datasetDataToBeDeleted = new TruncateDataset();
    try {
      datasetDataToBeDeleted = datasetService.getDatasetDataToBeDeleted(datasetId, dataProviderId);
    } catch (Exception e) {
      LOG.error("Error while executing method getDatasetData for datasetId: {}, dataProviderId: {}", datasetId, dataProviderId, e);
    }

    LOG.info("Method getDatasetData returns truncateDataset: {}", datasetDataToBeDeleted);
    return datasetDataToBeDeleted;
  }

  @DeleteMapping("/private/truncateDataset")
  @ApiOperation(value = "Truncate dataset by dataset id", hidden = true)
  public Boolean truncateDataset(@RequestParam("datasetId") Long datasetId) {
    LOG.info("Method truncateDataset called for datasetId: {}", datasetId);
    if (datasetId == null) {
      return null;
    }
    return datasetService.truncateDataset(datasetId);
  }

  @DeleteMapping("/v2/truncateDataset")
  @PreAuthorize("hasAnyRole('ADMIN')")
  @ApiOperation(value = "Truncate dataset by dataset id", hidden = true)
  public Boolean truncateDatasetV2(@RequestParam("datasetId") Long datasetId) {
    LOG.info("Method truncateDataset called for datasetId: {}", datasetId);
    if (datasetId == null) {
      return null;
    }
    return datasetService.truncateDataset(datasetId);
  }

  /**
   * Creates the response entity.
   *
   * @param fileName the file name
   * @param content the content
   * @return the response entity
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private ResponseEntity<InputStreamResource> createResponseEntity(String fileName, File content)
          throws IOException {

    InputStreamResource resource = new InputStreamResource(FileUtils.openInputStream(content));
    HttpHeaders header = new HttpHeaders();
    header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
    return ResponseEntity.ok().headers(header).contentLength(content.length())
            .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
  }


  /**
   * Validate attachment.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   * @param originalFilename the original filename
   * @param size the size
   * @return true, if successful
   * @throws EEAException the EEA exception
   */
  private boolean validateAttachment(Long datasetId, String idField, String originalFilename,
                                     Long size, String tableSchemaName, String fieldName, Boolean isBigDataDataflow) throws EEAException {

    LOG.info("Validating attachment for datasetId {}, fieldId {} and fileName {}", datasetId, idField, originalFilename);

    Boolean result = true;
    String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
    if (datasetSchemaId == null) {
      throw new EEAException(EEAErrorMessage.DATASET_SCHEMA_ID_NOT_FOUND);
    }
    String fieldSchemaId = null;
    if(isBigDataDataflow){
      fieldSchemaId = datasetSchemaService.getFieldSchemaIdByDatasetIdTableNameAndFieldName(datasetId, tableSchemaName, fieldName);
    }
    else{
      FieldVO fieldVO = datasetService.getFieldById(datasetId, idField);
      fieldSchemaId = fieldVO.getIdFieldSchema();
    }

    FieldSchemaVO fieldSchema =
            datasetSchemaService.getFieldSchema(datasetSchemaId, fieldSchemaId);
    if (fieldSchema == null || fieldSchema.getId() == null) {
      throw new EEAException(EEAErrorMessage.FIELD_SCHEMA_ID_NOT_FOUND);
    }
    // Validate property maxSize of the file. If the size is 0, it's ok, continue
    if (fieldSchema.getMaxSize() != null && fieldSchema.getMaxSize() != 0
            && fieldSchema.getMaxSize() * 1048576 < size) {
      result = false;
    }
    // Validate property extensions of the file. If no extensions provided, it's ok, continue
    if (fieldSchema.getValidExtensions() != null) {
      List<String> extensions = Arrays.asList(fieldSchema.getValidExtensions());
      if (!extensions.isEmpty()
              && !extensions.contains(datasetService.getExtension(originalFilename))) {
        result = false;
      }
    }
    LOG.info("Successfully validated attachment for datasetId {}, fieldId {} and fileName {} Result: {}", datasetId, idField, originalFilename, result);
    return result;
  }

  /**
   * Deletes the locks related to import
   * @param datasetId
   * @return
   */
  @Override
  @DeleteMapping(value = "/deleteLocksToImportProcess/{datasetId}")
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "Deletes the locks related to import", hidden = true)
  public void deleteLocksToImportProcess(@ApiParam(value = "Dataset id from which locks should be removed",
          example = "15") @PathVariable("datasetId") Long datasetId) {
    try {
      datasetService.deleteLocksToImportProcess(datasetId);
    }
    catch (Exception e) {
      LOG.error("Unexpected error! Error deleting locks related to import process for datasetId {} Message: {}",  datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Finds tasks by processId and status
   * @param processId
   * @param status
   * @return
   */
  @Override
  @GetMapping("/private/findTasksByProcessIdAndStatusIn/{processId}")
  public List<TaskVO> findTasksByProcessIdAndStatusIn(@PathVariable("processId") String processId, @RequestParam("status") List<ProcessStatusEnum> status) {
    try {
      return datasetService.findTasksByProcessIdAndStatusIn(processId, status);
    } catch (Exception e) {
      LOG.error("Error while finding tasks for processId {}, error is {}", processId, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates public files
   *
   * @param dataflowId the dataset id
   * @param providerId the provider id
   */
  @Override
  @PreAuthorize("hasAnyRole('ADMIN')")
  @PostMapping("/createPublicFiles")
  public void createPublicFiles(@RequestParam(value = "dataflowId", required=true) Long dataflowId, @RequestParam(value = "providerId", required=true) Long providerId){
    DataFlowVO dataflowVO = dataFlowControllerZuul.findById(dataflowId, providerId);
    if (dataflowVO.isShowPublicInfo()) {
      try {
        fileTreatmentHelper.savePublicFiles(dataflowId, providerId);
        LOG.info("Successfully created public files for for dataflow {} with dataprovider {}", dataflowId, providerId);
      } catch (Exception e) {
        LOG.error("Unexpected error! Error creating folder for dataflow {} with dataprovider {}", dataflowId, providerId, e);
      }
    }
    else{
      LOG.info("Could not create public files because show public info is false for dataflow with id {}", dataflowId);
    }
  }

  @ExceptionHandler(Exception.class)
  public void handleGenericException(Exception exception, WebRequest webRequest) throws Exception {
    HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();
    String requestUri = request.getRequestURI();
    LOG.error("For request {} ", requestUri);
    request.getParameterMap().forEach((k, v) -> LOG.error("parameter:{}={}", k, v));
    if (requestUri.contains("importFileData")) {
      try {
        DefaultMultipartHttpServletRequest multipartRequest = (DefaultMultipartHttpServletRequest) ((ServletWebRequest) webRequest).getRequest();
        multipartRequest.getMultiFileMap().forEach((k, v) -> LOG.error("multipart parameter:{}={}", k, v.get(0).getOriginalFilename()));
      } catch (Exception e1) {
        LOG.error("Error while extracting multipart parameters: ", e1);
      }
    }
    LOG.error("the following exception occurred: ", exception);
    throw exception;
  }
  @Override
  @GetMapping("/private/etlExport/createFile/{datasetId}")
  @HystrixCommand(commandProperties = {@HystrixProperty(
          name = "execution.isolation.thread.timeoutInMilliseconds", value = "7200000")})
  @ApiOperation(value = "Export data by dataset id",
          notes = "Allowed roles: \n\n Reporting dataset: CUSTODIAN, STEWARD, OBSERVER, REPORTER WRITE, REPORTER READ, LEAD REPORTER, STEWARD SUPPORT \n\n Test dataset: CUSTODIAN, STEWARD, STEWARD SUPPORT\n\n Reference dataset: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT\n\n Design dataset: CUSTODIAN, STEWARD, EDITOR WRITE, EDITOR READ\n\n EU dataset: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT\n\n Data collection: CUSTODIAN, STEWARD, OBSERVER, STEWARD SUPPORT")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully exported"),
          @ApiResponse(code = 500, message = "Error exporting data"),
          @ApiResponse(code = 403, message = "Error dataset not belong dataflow")})
  public void createFileForEtlExport(
          @ApiParam(type = "Long", value = "Dataset id",
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
                  value = "dataProviderCodes", required = false) String dataProviderCodes,
          @ApiParam(type = "Long", value = "Job id", example = "1") @RequestParam(
                  name = "jobId", required = false) Long jobId) throws Exception {

    JobVO jobVO = null;
    if (jobId!=null) {
      jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.IN_PROGRESS);
      jobVO = jobControllerZuul.findJobById(jobId);
    }

    String user = jobVO!=null ? jobVO.getCreatorUsername() : SecurityContextHolder.getContext().getAuthentication().getName();

    if (!dataflowId.equals(datasetService.getDataFlowIdById(datasetId))) {
      String errorMessage =
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId);
      LOG.error(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
              String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId));
    }

    try {
      LOG.info("Creating etlExport File for dataflowId {} and datasetId {}", dataflowId, datasetId);
      datasetService.createFileForEtlExport(datasetId, tableSchemaId, limit, offset, filterValue, columnName, dataProviderCodes, jobId, dataflowId, user);
      LOG.info("Successfully called method for creating etlExport file for dataflowId {} and datasetId {}", dataflowId, datasetId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error in createFileForEtlExport for datasetId {} and jobId {} Message: ", datasetId, jobId, e);
      jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);
      throw e;
    }
  }


  @Override
  @HystrixCommand
  @PutMapping("/{datasetId}/updateGeometry")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD', 'TESTDATASET_STEWARD')")
  public void updateGeometry(@PathVariable("datasetId") Long datasetId) {
    try {
      updateRecordHelper.executeGeometrypdateProcess(datasetId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error updating geometry field withfor datasetId {}", datasetId, e);
    }
  }

  /**
   * Generate s3 presigned Url for import
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param tableSchemaId the tableSchemaId
   * @param replace the replace
   * @param integrationId the integrationId
   * @param delimiter the delimiter
   *
   */
  @Override
  @GetMapping("/{datasetId}/generateImportPresignedUrl")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  public JobPresignedUrlInfo generateImportPresignedUrl (
          @ApiParam(type = "Long", value = "Dataset id", example = "0") @LockCriteria(name = "datasetId") @PathVariable("datasetId") Long datasetId,
          @ApiParam(type = "Long", value = "Dataflow id", example = "0") @RequestParam(value = "dataflowId") Long dataflowId,
          @ApiParam(type = "Long", value = "Provider id", example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
          @ApiParam(type = "String", value = "Table schema id", example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
          @ApiParam(type = "boolean", value = "Replace current data", example = "true") @RequestParam(value = "replace", required = false) boolean replace,
          @ApiParam(type = "Long", value = "Integration id", example = "0") @RequestParam(value = "integrationId", required = false) Long integrationId,
          @ApiParam(type = "String", value = "File delimiter", example = ",") @RequestParam(value = "delimiter", required = false) String delimiter,
          @ApiParam(type = "String", value = "File name", example = "fileName") @RequestParam(value = "fileName", required = false) String fileName){
    JobPresignedUrlInfo info;
    try{
      String preSignedUrl = bigDataDatasetService.generateImportPreSignedUrl(datasetId, dataflowId, providerId, fileName);
      LOG.info("Created presigned url for dataflowId {}, datasetId {} and providerId {}", dataflowId, datasetId, providerId);

      //check eligibility of job and add new import job
      List<Long> datasetIds = new ArrayList<>();
      datasetIds.add(datasetId);
      JobStatusEnum jobStatus = jobControllerZuul.checkEligibilityOfJob(JobTypeEnum.IMPORT.getValue(), false, dataflowId, providerId, datasetIds);
      if(jobStatus == JobStatusEnum.IN_PROGRESS){
        //if this endpoint is called we want to iniatialize an import job with status QUEUED instead of IN_PROGRESS
        jobStatus = JobStatusEnum.QUEUED;
      }
      Long jobId = jobControllerZuul.addImportJob(datasetId, dataflowId, providerId, tableSchemaId, null, replace, integrationId, delimiter, jobStatus, null, preSignedUrl);
      if(jobStatus.getValue().equals(JobStatusEnum.REFUSED.getValue())){
        LOG.info("Added import job with id {} for datasetId {} with status REFUSED", jobId, datasetId);
        datasetService.releaseImportRefusedNotification(datasetId, dataflowId, tableSchemaId, null);
        throw new ResponseStatusException(HttpStatus.LOCKED, EEAErrorMessage.IMPORTING_FILE_DATASET);
      }
      info = new JobPresignedUrlInfo(jobId, preSignedUrl);
    }
    catch (Exception e){
      LOG.error("Could not generate import presigned url for datasetId {}, dataflowId {} and providerId {}", datasetId, dataflowId, providerId);
      throw e;
    }
    return info;
  }

  @Override
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @PostMapping("/convertParquetToIcebergTable/{datasetId}")
  public void convertParquetToIcebergTable(@PathVariable("datasetId") Long datasetId,
                                           @RequestParam(value = "dataflowId") Long dataflowId,
                                           @RequestParam(value = "providerId", required = false) Long providerId,
                                           @RequestParam(value = "tableSchemaId") String tableSchemaId) throws Exception {

    try{
      LOG.info("Converting parquet table to iceberg for dataflowId {}, providerId {}, datasetId {} and tableSchemaId {}", dataflowId, providerId, datasetId, tableSchemaId);
      String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
      TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);
      if(tableSchemaVO != null && BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable())
              && !BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaVO.getIdTableSchema()))) {
        bigDataDatasetService.convertParquetToIcebergTable(datasetId, dataflowId, providerId, tableSchemaVO, datasetSchemaId);
        LOG.info("Converted parquet table to iceberg for dataflowId {}, providerId {}, datasetId {} and tableSchemaId {}", dataflowId, providerId, datasetId, tableSchemaId);
      }
      else{
        throw new Exception("The table data are not manually editable or the iceberg table is already created");
      }
    }
    catch (Exception e){
      LOG.error("Could not convert parquet table to iceberg for dataflowId {}, provider {}, datasetId {}, tableSchemaId {}. Error message: {}", dataflowId,
              providerId, datasetId, tableSchemaId, e.getMessage());
      throw e;
    }
  }

  /**
   * Convert Iceberg To Parquet Table
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param tableSchemaId the tableSchemaId
   *
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')")
  @PostMapping("/convertIcebergToParquetTable/{datasetId}")
  public void convertIcebergToParquetTable(@PathVariable("datasetId") Long datasetId,
                                           @RequestParam(value = "dataflowId") Long dataflowId,
                                           @RequestParam(value = "providerId", required = false) Long providerId,
                                           @RequestParam(value = "tableSchemaId") String tableSchemaId) throws Exception {
    try{
      LOG.info("Converting iceberg table to parquet for dataflowId {}, providerId {}, datasetId {} and tableSchemaId {}", dataflowId, providerId, datasetId, tableSchemaId);
      String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
      TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);
      if(tableSchemaVO != null && BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable())
              && BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaVO.getIdTableSchema()))) {
        bigDataDatasetService.convertIcebergToParquetTable(datasetId, dataflowId, providerId, tableSchemaVO, datasetSchemaId);
        LOG.info("Converted iceberg table to parquet for dataflowId {}, providerId {}, datasetId {} and tableSchemaId {}", dataflowId, providerId, datasetId, tableSchemaId);
      }
      else{
        throw new Exception("The table data are not manually editable or the iceberg table has not been created");
      }
    }
    catch (Exception e){
      LOG.error("Could not convert iceberg table to parquet for dataflowId {}, provider {}, datasetId {}, tableSchemaId {}. Error message: {}", dataflowId,
              providerId, datasetId, tableSchemaId, e.getMessage());
      throw e;
    }
  }

  /**
   * Check if iceberg table is created
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the tableSchemaId
   * @return if the iceberg table is created
   */
  @Override
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/isIcebergTableCreated/{datasetId}/{tableSchemaId}")
  public Boolean isIcebergTableCreated(@PathVariable("datasetId") Long datasetId,
                             @PathVariable("tableSchemaId") String tableSchemaId){
    try{
      return datasetTableService.icebergTableIsCreated(datasetId, tableSchemaId);
    }
    catch (Exception e){
      LOG.error("Could not find if the icebergTableCreated option was enabled for datasetId {}, tableSchemaId {}", datasetId, tableSchemaId);
      throw e;
    }
  }
}
