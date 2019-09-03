package org.eea.validation.controller;

import org.apache.commons.lang3.StringUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.validation.ValidationController;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

  /** The validation helper. */
  @Autowired
  private ValidationHelper validationHelper;


  @Autowired
  private LoadValidationsHelper loadValidationsHelper;

  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   */
  @Override
  @RequestMapping(value = "/dataset/{id}", method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void validateDataSetData(@PathVariable("id") Long datasetId) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      validationHelper.executeValidation(datasetId);
    } catch (EEAException e) {
      LOG_ERROR.info(e.getMessage());
    }
  }

  /**
   * Gets the failed validations by id dataset.
   *
   * @param datasetId the dataset id
   * @param pageNum the page num
   * @param pageSize the page size
   * @param fields the fields
   * @param asc the asc
   *
   * @return the failed validations by id dataset
   */
  @Override
  @GetMapping(value = "listValidations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public FailedValidationsDatasetVO getFailedValidationsByIdDataset(
      @PathVariable("id") Long datasetId,
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
      @RequestParam(value = "fields", required = false) String fields,
      @RequestParam(value = "asc", defaultValue = "true", required = false) Boolean asc) {
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
      validations = loadValidationsHelper.getListValidations(datasetId, pageable, fields, asc);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }

    return validations;
  }

}
