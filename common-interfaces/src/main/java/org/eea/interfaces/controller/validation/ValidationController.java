/*
 * 
 */
package org.eea.interfaces.controller.validation;

import java.util.List;
import java.util.Map;
import org.eea.interfaces.vo.validation.RuleScopeVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface ValidationController.
 */
public interface ValidationController {

  @FeignClient(value = "validation", path = "/validation")
  interface DataSetControllerZuul extends ValidationController {

  }

  /**
   * Gets the all rules.
   *
   * @return the all rules
   */
  @RequestMapping(value = "/getRules", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<Map<String, String>> getAllRules(Long dataflowId);

  /**
   * Sets the new rules.
   *
   * @param ruleName the rule name
   * @param ruleAtrtibute the rule atrtibute
   * @param ruleCondition the rule condition
   * @param ruleAction the rule action
   */
  @RequestMapping(value = "/setNewRule", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  void setNewRules(@RequestParam(required = true) RuleScopeVo ruleScope,
      @RequestParam(required = true) String ruleName,
      @RequestParam(required = true) String whenCondition,
      @RequestParam(required = true) String thenCondition);

  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   * @return the data set VO
   */
  @RequestMapping(value = "/dataset/{id}", method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE)
  void validateDataSetData(Long datasetId);

}
