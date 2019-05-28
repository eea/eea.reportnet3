package org.eea.validation.controller;

import java.util.List;
import java.util.Map;
import org.eea.validation.model.rules.Rule;
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
public class ValidationServiceController {

  /** The Constant REGULATION_TEMPLATE_FILE. */
  private static final String REGULATION_TEMPLATE_FILE =
      "src/main/resources/ruletemplate/template01.drl";

  /** The validation service. */
  @Autowired
  private ValidationService validationService;



  /**
   * Gets the questions.
   *
   * @param type the type
   * @return the questions
   */
  @RequestMapping(value = "/getLenght", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void getQuestions(@RequestParam(required = true) String type) {

    // Element element = new Element();
    // element.setType(type);
  }


  /**
   * Gets the all rules.
   *
   * @return the all rules
   */
  @RequestMapping(value = "/getRules", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getAllRules() {

    Rule rules = new Rule();
    List<Map<String, String>> ruleAttributes = validationService.getRules(rules);

    return ruleAttributes;
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

    Rule rules = new Rule();
    rules.setRulesBase("KieTest");
    rules.setRulesSession("TestKieSession");
    rules.setRulesAgent("TestKieAgent");
    rules.setName(ruleName);
    rules.setAttribute(ruleAtrtibute);
    rules.setConditionalElement(ruleCondition);
    rules.setAction(ruleAction);

    validationService.setNewRules(rules);
    validationService.loadNewRules(rules);
  }


}
