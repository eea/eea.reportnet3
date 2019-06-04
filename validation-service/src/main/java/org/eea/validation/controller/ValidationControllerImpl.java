package org.eea.validation.controller;

import java.util.List;
import java.util.Map;
import org.eea.interfaces.controller.validation.ValidationController;
import org.eea.validation.persistence.rules.model.DataFlowRule;
import org.eea.validation.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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
   * Gets the questions.
   *
   * @param type the type
   * @return the questions
   */
  @RequestMapping(value = "/getDataFlowRule", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void getDataFlowRule(@RequestParam(required = true) String type) {
    DataFlowRule rule = new DataFlowRule();
    validationService.getDataFlowRule(rule);
  }


  /**
   * Gets the all rules.
   *
   * @return the all rules
   */
  @RequestMapping(value = "/getRules", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getAllRules() {
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
  @RequestMapping(value = "/setRules", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void setNewRules(@RequestParam(required = true) String ruleName,
      @RequestParam(required = true) String ruleAtrtibute,
      @RequestParam(required = true) String ruleCondition,
      @RequestParam(required = true) String ruleAction) {
    validationService.saveRule(new DataFlowRule());

  }



}
