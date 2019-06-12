package org.eea.validation.controller;

import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.validation.ValidationController;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.validation.persistence.rules.DataFlowRule;
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
    return validationService.getRulesByDataFlowId(dataflowId);
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
  @RequestMapping(value = "/setNewRules", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void setNewRules(@RequestParam(required = true) TypeEntityEnum typeEntityEnum,
      @RequestParam(required = true) String ruleName,
      @RequestParam(required = true) String whenCondition,
      @RequestParam(required = true) List<String> thenCondition) {

    DataFlowRule dataFlowRule = new DataFlowRule(new ObjectId(), 1L, typeEntityEnum, ruleName,
        whenCondition, thenCondition);
    validationService.saveRule(dataFlowRule);

  }

  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   * @return the list
   */
  @Override
  public void validateDataSetData(@RequestParam("id") Long datasetId) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    validationService.validateDataSetData(datasetId);
  }

}
