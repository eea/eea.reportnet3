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


@RestController
@RequestMapping(value = "/validation")
public class ValidationServiceController {

  private static final String REGULATION_TEMPLATE_FILE =
      "src/main/resources/ruletemplate/template01.drl";

  @Autowired
  private ValidationService validationService;



  @RequestMapping(value = "/getLenght", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void getQuestions(@RequestParam(required = true) String type) {

    // Element element = new Element();
    // element.setType(type);
  }


  @RequestMapping(value = "/getRules", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getAllRules() {

    Rule rules = new Rule();
    List<Map<String, String>> ruleAttributes = validationService.getRules(rules);

    return ruleAttributes;
  }

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
