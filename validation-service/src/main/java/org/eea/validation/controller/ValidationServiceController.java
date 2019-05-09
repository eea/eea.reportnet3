package org.eea.validation.controller;

import java.util.List;
import java.util.Map;
import org.eea.validation.model.Element;
import org.eea.validation.model.Rules;
import org.eea.validation.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/validation")
public class ValidationServiceController {

  private static final String REGULATION_TEMPLATE_FILE =
      "src/main/resources/ruletemplate/template01.drl";

  private final ValidationService validationService;

  @Autowired
  public ValidationServiceController(ValidationService validationService) {
    this.validationService = validationService;
  }



  @RequestMapping(value = "/getLenght", method = RequestMethod.GET, produces = "application/json")
  public Element getQuestions(@RequestParam(required = true) String type) {

    Element element = new Element();
    element.setType(type);
    validationService.getElementLenght(element);
    return element;
  }


  @RequestMapping(value = "/getRules", method = RequestMethod.GET, produces = "application/json")
  public List<Map<String, String>> getAllRules() {

    Rules rules = new Rules();
    List<Map<String, String>> ruleAttributes = validationService.getRules(rules);

    return ruleAttributes;
  }

  @RequestMapping(value = "/setRules", method = RequestMethod.GET, produces = "application/json")
  public void setNewRules(@RequestParam(required = true) String ruleName,
      @RequestParam(required = true) String ruleAtrtibute,
      @RequestParam(required = true) String ruleCondition,
      @RequestParam(required = true) String ruleAction) {

    Rules rules = new Rules();
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
