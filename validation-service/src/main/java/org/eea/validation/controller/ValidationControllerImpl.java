package org.eea.validation.controller;

import java.util.List;
import java.util.Map;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.validation.ValidationController;
import org.eea.validation.persistence.rules.model.DataFlowRule;
import org.eea.validation.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

  /** The validation service. */
  @Autowired
  private ValidationService validationService;


  /**
   * Gets the all rules.
   *
   * @param dataflowId the dataflow id
   * @return the all rules
   */
  @Override
  @RequestMapping(value = "/getRules", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getAllRules(Long dataflowId) {
    return validationService.getRules();
  }

  /**
   * Sets the new rules.
   *
   * @param ruleName the rule name
   * @param ruleAtrtibute the rule atrtibute
   * @param ruleCondition the rule condition
   * @param ruleAction the rule action
   */
  @Override
  @RequestMapping(value = "/setRules", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void setNewRules(@RequestParam(required = true) String ruleName,
      @RequestParam(required = true) String ruleAtrtibute,
      @RequestParam(required = true) String ruleCondition,
      @RequestParam(required = true) String ruleAction) {
    validationService.saveRule(new DataFlowRule());

  }

  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   * @return the list
   */
  @Override
  public void validateDataSetData(Long datasetId) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    validationService.validateDataSetData(datasetId);
  }

}
