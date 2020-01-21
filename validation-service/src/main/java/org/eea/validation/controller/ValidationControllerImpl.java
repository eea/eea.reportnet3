package org.eea.validation.controller;

import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.validation.ValidationController;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
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
import org.springframework.cache.annotation.CacheEvict;
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

  /**
   * The validation service.
   */
  @Autowired
  @Qualifier("proxyValidationService")
  private ValidationService validationService;

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
   */
  @Override
  @PutMapping(value = "/dataset/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @CacheEvict(value = {"cacheStatistics_" + "#datasetId"})
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_PROVIDER','DATASCHEMA_CUSTODIAN')")
  @LockMethod(removeWhenFinish = false)
  public void validateDataSetData(
      @LockCriteria(name = "datasetId") @PathVariable("id") Long datasetId) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    validationHelper.executeValidation(datasetId, UUID.randomUUID().toString());
  }

  /**
   * Gets the failed validations by id dataset.
   *
   * @param datasetId the dataset id
   * @param pageNum the page num
   * @param pageSize the page size
   * @param fields the fields
   * @param asc the asc
   * @param levelErrorsFilter the level errors filter
   * @param typeEntitiesFilter the type entities filter
   * @param originsFilter the origins filter
   * @return the failed validations by id dataset
   */
  @Override
  @GetMapping(value = "listValidations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public FailedValidationsDatasetVO getFailedValidationsByIdDataset(
      @PathVariable("id") Long datasetId,
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
      @RequestParam(value = "fields", required = false) String fields,
      @RequestParam(value = "asc", defaultValue = "true", required = false) Boolean asc,
      @RequestParam(value = "levelErrorsFilter",
          required = false) List<TypeErrorEnum> levelErrorsFilter,
      @RequestParam(value = "typeEntitiesFilter",
          required = false) List<TypeEntityEnum> typeEntitiesFilter,
      @RequestParam(value = "originsFilter", required = false) String originsFilter) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    FailedValidationsDatasetVO validations = null;
    Pageable pageable = null;
    if (StringUtils.isNotBlank(fields)) {
      fields = fields.replace("tableSchemaName", "originName");
      fields = fields.replace("entityType", "typeEntity");
      Sort order = asc ? Sort.by(fields).ascending() : Sort.by(fields).descending();
      PageRequest.of(pageNum, pageSize, order);
      pageable = PageRequest.of(pageNum, pageSize, order);
    } else {
      pageable = PageRequest.of(pageNum, pageSize);
    }
    try {
      validations = loadValidationsHelper.getListValidations(datasetId, pageable, fields, asc,
          levelErrorsFilter, typeEntitiesFilter, originsFilter);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }

    return validations;
  }

}
