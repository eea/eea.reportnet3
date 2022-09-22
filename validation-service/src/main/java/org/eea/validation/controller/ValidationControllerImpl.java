package org.eea.validation.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.controller.validation.ValidationController;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetRunningStatusEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.validation.service.ValidationService;
import org.eea.validation.service.impl.LoadValidationsHelper;
import org.eea.validation.util.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;

/**
 * The Class ValidationControllerImpl.
 */
@RestController
@RequestMapping(value = "/validation")
public class ValidationControllerImpl implements ValidationController {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ValidationControllerImpl.class);

  /** The validation service. */
  @Autowired
  @Qualifier("proxyValidationService")
  private ValidationService validationService;

  /** The validation helper. */
  @Autowired
  private ValidationHelper validationHelper;

  /** The load validations helper. */
  @Autowired
  private LoadValidationsHelper loadValidationsHelper;

  /** The notification controller zuul. */
  @Autowired
  private NotificationControllerZuul notificationControllerZuul;

  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The process controller zuul. */
  @Autowired
  private ProcessControllerZuul processControllerZuul;


  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   * @param released the released
   */
  @Override
  @PutMapping(value = "/dataset/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD')  OR hasAnyRole('ADMIN')")
  @LockMethod(removeWhenFinish = false)
  @ApiOperation(value = "Validates dataset data for a given dataset id", hidden = true)
  @ApiResponse(code = 400, message = EEAErrorMessage.DATASET_INCORRECT_ID)
  public void validateDataSetData(
      @LockCriteria(name = "datasetId") @ApiParam(
          value = "Dataset id whose data is going to be validated",
          example = "15") @PathVariable("id") Long datasetId,
      @ApiParam(value = "Is the dataset released?", example = "true",
          required = false) @RequestParam(value = "released", required = false) boolean released) {

    LOG.info(
        "The user invoking ValidationControllerImpl.validateDataSetData is {} and the datasetId {}",
        SecurityContextHolder.getContext().getAuthentication().getName(), datasetId);


    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    DataSetMetabaseVO dataset = datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    String uuid = UUID.randomUUID().toString();
    int priority = validationHelper.getPriority(dataset);
    if (!released) {
      processControllerZuul.updateProcess(datasetId, dataset.getDataflowId(),
          ProcessStatusEnum.IN_QUEUE, ProcessTypeEnum.VALIDATION, uuid,
          SecurityContextHolder.getContext().getAuthentication().getName(), priority, released);

    } else {
      // obtain datasets to be released
      List<Long> datasets =
          datasetMetabaseControllerZuul.getDatasetIdsByDataflowIdAndDataProviderId(
              dataset.getDataflowId(), dataset.getDataProviderId());
      // queue validations
      processControllerZuul.updateProcess(datasetId, dataset.getDataflowId(),
          ProcessStatusEnum.IN_QUEUE, ProcessTypeEnum.VALIDATION, uuid,
          SecurityContextHolder.getContext().getAuthentication().getName(), priority, released);
      datasets.remove(datasetId);
      for (Long datasetToReleaseId : datasets) {
        processControllerZuul.updateProcess(datasetToReleaseId, dataset.getDataflowId(),
            ProcessStatusEnum.IN_QUEUE, ProcessTypeEnum.VALIDATION, UUID.randomUUID().toString(),
            SecurityContextHolder.getContext().getAuthentication().getName(), priority, released);
      }
    }
    try {
      LOG.info("Executing validation for datasetId {}", datasetId);
      validationHelper.executeValidation(datasetId, uuid, released, true);
      LOG.info("Successfully executed validation for datasetId {}", datasetId);

      // Add lock to the release process if necessary
      validationHelper.addLockToReleaseProcess(datasetId);
    } catch (EEAException e) {
      datasetMetabaseControllerZuul.updateDatasetRunningStatus(datasetId,
          DatasetRunningStatusEnum.ERROR_IN_VALIDATION);
      LOG_ERROR.error("Error validating datasetId {}. Message {}", datasetId, e.getMessage(), e);
      validationHelper.deleteLockToReleaseProcess(datasetId);
    }

  }

  /**
   * Gets the failed validations by id dataset.
   *
   * @param datasetId the dataset id
   * @param pageNum the page num
   * @param pageSize the page size
   * @param headers the headers
   * @param asc the asc
   * @param levelErrorsFilter the level errors filter
   * @param typeEntitiesFilter the type entities filter
   * @param tableFilter the table filter
   * @param fieldValueFilter the field value filter
   * @return the failed validations by id dataset
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT') OR checkAccessReferenceEntity('DATASET',#datasetId)")
  @GetMapping(value = "listValidations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Gets all the failed validations for a given dataset", hidden = true)
  @ApiResponse(code = 400, message = EEAErrorMessage.DATASET_INCORRECT_ID)
  public FailedValidationsDatasetVO getFailedValidationsByIdDataset(
      @ApiParam(value = "Dataset id used in the retrieval process",
          example = "1") @PathVariable("id") Long datasetId,
      @ApiParam(value = "Page number the filtering starts in.", example = "0", defaultValue = "0",
          required = false) @RequestParam(value = "pageNum", defaultValue = "0",
              required = false) Integer pageNum,
      @ApiParam(value = "How many records are going to be shown per page.", example = "10",
          defaultValue = "20", required = false) @RequestParam(value = "pageSize",
              defaultValue = "20", required = false) Integer pageSize,
      @ApiParam(value = "The headers used in the retrieval process") @RequestParam(
          value = "headers", required = false) String headers,
      @ApiParam(value = "Are the validations going to be ordered in ascending order?",
          example = "false",
          defaultValue = "true") @RequestParam(value = "asc", defaultValue = "true") boolean asc,
      @ApiParam(value = "The level of error the validations are going to be filtered with",
          required = false) @RequestParam(value = "levelErrorsFilter",
              required = false) List<ErrorTypeEnum> levelErrorsFilter,
      @ApiParam(value = "The types of entities used in the retrieval process", example = "DATASET",
          required = false) @RequestParam(value = "typeEntitiesFilter",
              required = false) List<EntityTypeEnum> typeEntitiesFilter,
      @ApiParam(value = "The table filter used in the retrieval process",
          required = false) @RequestParam(value = "tableFilter",
              required = false) String tableFilter,
      @ApiParam(value = "The filtered field value used in the retrieval process",
          required = false) @RequestParam(value = "fieldValueFilter",
              required = false) String fieldValueFilter) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    FailedValidationsDatasetVO validations = null;
    Pageable pageable = null;
    if (StringUtils.isNotBlank(headers)) {
      headers = headers.replace("tableSchemaName", "tableName");
      headers = headers.replace("fieldSchemaName", "fieldName");
      headers = headers.replace("entityType", "typeEntity");
      Sort order = asc ? Sort.by(headers).ascending() : Sort.by(headers).descending();
      PageRequest.of(pageNum, pageSize, order);
      pageable = PageRequest.of(pageNum, pageSize, order);
    } else {
      pageable = PageRequest.of(pageNum, pageSize);
    }
    try {
      validations = loadValidationsHelper.getListValidations(datasetId, pageable, headers, asc,
          levelErrorsFilter, typeEntitiesFilter, tableFilter, fieldValueFilter);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }

    return validations;
  }

  /**
   * Gets the group failed validations by id dataset.
   *
   * @param datasetId the dataset id
   * @param pageNum the page num
   * @param pageSize the page size
   * @param headers the headers
   * @param asc the asc
   * @param levelErrorsFilter the level errors filter
   * @param typeEntitiesFilter the type entities filter
   * @param tableFilter the table filter
   * @param fieldValueFilter the field value filter
   * @return the group failed validations by id dataset
   */
  @Override
  @GetMapping(value = "listGroupValidations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("checkAccessSuperUser('DATASET',#datasetId)")
  @ApiOperation(value = "Gets all the failed validations for a given dataset grouped by code",
      hidden = true)
  @ApiResponse(code = 400, message = EEAErrorMessage.DATASET_INCORRECT_ID)
  public FailedValidationsDatasetVO getGroupFailedValidationsByIdDataset(
      @ApiParam(value = "Dataset id used in the retrieval process",
          example = "1") @PathVariable("id") Long datasetId,
      @ApiParam(value = "Page number the filtering starts in.", example = "0", defaultValue = "0",
          required = false) @RequestParam(value = "pageNum", defaultValue = "0",
              required = false) Integer pageNum,
      @ApiParam(value = "How many records are going to be shown per page.", example = "10",
          defaultValue = "20", required = false) @RequestParam(value = "pageSize",
              defaultValue = "20", required = false) Integer pageSize,
      @ApiParam(value = "The headers used in the retrieval process") @RequestParam(
          value = "headers", required = false) String headers,
      @ApiParam(value = "Are the validations going to be ordered in ascending order?",
          example = "false",
          defaultValue = "true") @RequestParam(value = "asc", defaultValue = "true") boolean asc,
      @ApiParam(value = "The level of error the validations are going to be filtered with",
          required = false) @RequestParam(value = "levelErrorsFilter",
              required = false) List<ErrorTypeEnum> levelErrorsFilter,
      @ApiParam(value = "The types of entities used in the retrieval process", example = "DATASET",
          required = false) @RequestParam(value = "typeEntitiesFilter",
              required = false) List<EntityTypeEnum> typeEntitiesFilter,
      @ApiParam(value = "The table filter used in the retrieval process",
          required = false) @RequestParam(value = "tableFilter",
              required = false) String tableFilter,
      @ApiParam(value = "The filtered field value used in the retrieval process",
          required = false) @RequestParam(value = "fieldValueFilter",
              required = false) String fieldValueFilter) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    FailedValidationsDatasetVO validations = null;
    Pageable pageable = null;
    if (StringUtils.isNotBlank(headers)) {
      headers = headers.replace("tableSchemaName", "tableName");
      headers = headers.replace("fieldSchemaName", "fieldName");
      headers = headers.replace("entityType", "typeEntity");
      Sort order = asc ? Sort.by(headers).ascending() : Sort.by(headers).descending();
      PageRequest.of(pageNum, pageSize, order);
      pageable = PageRequest.of(pageNum, pageSize, order);
    } else {
      pageable = PageRequest.of(pageNum, pageSize);
    }
    try {
      validations = loadValidationsHelper.getListGroupValidations(datasetId, pageable,
          levelErrorsFilter, typeEntitiesFilter, tableFilter, fieldValueFilter, headers, asc);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }

    return validations;
  }

  /**
   * Export validation data CSV.
   *
   * @param datasetId the dataset id
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASET_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @PostMapping(value = "/export/{datasetId}")
  @ApiOperation(value = "Export all the validations for a given dataset grouped by code",
      hidden = true)
  public void exportValidationDataCSV(@ApiParam(value = "Dataset id used in the export process",
      example = "1") @PathVariable("datasetId") Long datasetId) {
    LOG.info("Export dataset validation data from datasetId {}, with type .csv", datasetId);
    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDatasetId(datasetId);
    notificationControllerZuul.createUserNotificationPrivate("DOWNLOAD_VALIDATIONS_START",
        userNotificationContentVO);

    try {
      validationService.exportValidationFile(datasetId);
    } catch (EEAException | IOException e) {
      LOG_ERROR.error("Error exporting validation data from the dataset {}.  Message: {}",
          datasetId, e.getMessage());
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
  @GetMapping(value = "/downloadFile/{datasetId}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASET_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','EUDATASET_STEWARD_SUPPORT','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','DATACOLLECTION_STEWARD_SUPPORT','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD_SUPPORT') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiOperation(
      value = "Download the file created in the export validations for a given dataset grouped by code",
      hidden = true)
  @ApiResponse(code = 404,
      message = "Could not download the validation export file for the given dataset id")
  public void downloadFile(
      @ApiParam(value = "Dataset id that was used in the export process",
          example = "1") @PathVariable Long datasetId,
      @ApiParam(value = "Filename for the file that was generated during the export process.",
          example = "dataset-3-validations") @RequestParam String fileName,
      HttpServletResponse response) {
    try {
      LOG.info("Downloading file generated from export dataset. DatasetId {} Filename {}",
          datasetId, fileName);
      File file =
          validationService.downloadExportedFile(datasetId, FilenameUtils.getName(fileName));
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
          "attachment; filename=" + FilenameUtils.getName(fileName));

      OutputStream out = response.getOutputStream();
      FileInputStream in = new FileInputStream(file);
      // copy from in to out
      IOUtils.copyLarge(in, out);
      out.close();
      in.close();
      // delete the file after downloading it
      FileUtils.forceDelete(file);
    } catch (IOException | ResponseStatusException e) {
      LOG_ERROR.error(
          "Error downloading file generated from export from the datasetId {}. Filename {}. Message: {}",
          datasetId, fileName, e.getMessage());

      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(
          "Trying to download a file generated during the export dataset validation data process but the file is not found, datasetID: %s + filename: %s",
          datasetId, fileName));

    }
  }

  @Override
  @PutMapping(value = "/restartTask/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAnyRole('ADMIN')")
  @ApiOperation(value = "Sets the status to IN_QUEUE for a given task id", hidden = true)
  @ApiResponse(code = 400, message = EEAErrorMessage.TASK_INCORRECT_ID)
  public void restartTask(@ApiParam(
          value = "Task id of task to restart",
          example = "15") @PathVariable("taskId") Long taskId) {
      LOG.info("Restarting task with id " + taskId);
      try {
        validationHelper.updateTaskStatus(taskId, ProcessStatusEnum.IN_QUEUE);
        LOG.info("Task with id " + taskId + " restarted.");
      } catch (Exception e) {
        LOG.error("Error restarting task with id " + taskId);
      }
  }
}
