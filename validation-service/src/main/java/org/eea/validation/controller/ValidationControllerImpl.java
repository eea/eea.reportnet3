package org.eea.validation.controller;

import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.validation.ValidationController;
import org.eea.kafka.io.KafkaSender;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.ValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
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

  /** The validation service. */
  @Autowired
  @Qualifier("proxyValidationService")
  private ValidationService validationService;

  /** The kafka sender. */
  @Autowired
  private KafkaSender kafkaSender;

  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void validateDataSetData(@RequestParam("id") Long datasetId) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    ValidationHelper.executeValidation(kafkaSender, this.validationService, datasetId);

  }

}
