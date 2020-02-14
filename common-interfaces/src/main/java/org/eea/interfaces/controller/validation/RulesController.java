package org.eea.interfaces.controller.validation;

import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface RulesController.
 */
public interface RulesController {

  /**
   * The Interface RulesControllerZuul.
   */
  @FeignClient(value = "validation", contextId = "rules", path = "/rules")
  interface RulesControllerZuul extends RulesController {

  }

  /**
   * Creates the empty rules schema.
   *
   * @param idDataSetSchema the id data set schema
   * @param idRulesSchema the id rules schema
   */
  @PostMapping(value = "/createEmptyRulesSchema")
  void createEmptyRulesSchema(@RequestParam("idDataSetSchema") String idDataSetSchema,
      @RequestParam("idRulesSchema") String idRulesSchema);

  /**
   * Find rule schema by dataset id.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the rules schema VO
   */
  @GetMapping(value = "/{idDatasetSchema}", produces = MediaType.APPLICATION_JSON_VALUE)
  RulesSchemaVO findRuleSchemaByDatasetId(@PathVariable("idDatasetSchema") String idDatasetSchema);



  /**
   * Creates the empty rules schema.
   *
   * @param idDataSetSchema the id data set schema
  /**
   * Delete rule by id.
   *
   * @param idDataSetSchema the id data set schema
   * @param ruleId the rule id
   */
  @DeleteMapping(value = "/deleteRulesSchema")
  void deleteRulesSchema(@RequestParam("idDataSetSchema") String idDataSetSchema);
  void deleteRuleById(
      @PathVariable(name = "idDatasetSchema", required = true) String idDatasetSchema,
      @PathVariable(name = "ruleId", required = true) String ruleId);

  /**
   * Delete rule by reference id.
   *
   * @param idDataSetSchema the id data set schema
   * @param referenceId the reference id
   */
  @DeleteMapping(value = "{idDatasetSchema}/deleteRuleByReferenceId/{referenceId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void deleteRuleByReferenceId(
      @PathVariable(name = "idDatasetSchema", required = true) String idDatasetSchema,
      @PathVariable(name = "referenceId", required = true) String referenceId);

}
