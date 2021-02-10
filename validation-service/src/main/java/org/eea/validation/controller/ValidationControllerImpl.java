package org.eea.validation.controller;

import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.validation.ValidationController;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class ValidationServiceController.
 */
@RestController
@RequestMapping(value = "/validation")
public class ValidationControllerImpl implements ValidationController {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ValidationControllerImpl.class);

  /**
   * The validation service.
   */
  @Autowired
  @Qualifier("proxyValidationService")
  private ValidationService validationService;

  /** The validation helper. */
  @Autowired
  private ValidationHelper validationHelper;

  /** The load validations helper. */
  @Autowired
  private LoadValidationsHelper loadValidationsHelper;

  /**
   * Validate data set data. The lock should be released on
   * ValidationHelper.checkFinishedValidations(..)
   *
   * @param datasetId the dataset id
   * @param released the released
   */
  @Override
  @PutMapping(value = "/dataset/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN')")
  @LockMethod(removeWhenFinish = false)
  public void validateDataSetData(
      @LockCriteria(name = "datasetId") @PathVariable("id") Long datasetId,
      @RequestParam(value = "released", required = false) boolean released) {

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
    validationHelper.executeValidation(datasetId, UUID.randomUUID().toString(), released, true);
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
  @GetMapping(value = "listValidations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public FailedValidationsDatasetVO getFailedValidationsByIdDataset(
      @PathVariable("id") Long datasetId,
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
      @RequestParam(value = "headers", required = false) String headers,
      @RequestParam(value = "asc", defaultValue = "true") boolean asc,
      @RequestParam(value = "levelErrorsFilter",
          required = false) List<ErrorTypeEnum> levelErrorsFilter,
      @RequestParam(value = "typeEntitiesFilter",
          required = false) List<EntityTypeEnum> typeEntitiesFilter,
      @RequestParam(value = "tableFilter", required = false) String tableFilter,
      @RequestParam(value = "fieldValueFilter", required = false) String fieldValueFilter) {
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
  public FailedValidationsDatasetVO getGroupFailedValidationsByIdDataset(
      @PathVariable("id") Long datasetId,
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
      @RequestParam(value = "headers", required = false) String headers,
      @RequestParam(value = "asc", defaultValue = "true") boolean asc,
      @RequestParam(value = "levelErrorsFilter",
          required = false) List<ErrorTypeEnum> levelErrorsFilter,
      @RequestParam(value = "typeEntitiesFilter",
          required = false) List<EntityTypeEnum> typeEntitiesFilter,
      @RequestParam(value = "tableFilter", required = false) String tableFilter,
      @RequestParam(value = "fieldValueFilter", required = false) String fieldValueFilter) {
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


}
