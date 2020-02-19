package org.eea.interfaces.controller.validation;

import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
   * Find active rule schema by dataset id.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the rules schema VO
   */
  @GetMapping(value = "/{idDatasetSchema}/actives", produces = MediaType.APPLICATION_JSON_VALUE)
  RulesSchemaVO findActiveRuleSchemaByDatasetId(
      @PathVariable("idDatasetSchema") String idDatasetSchema);


  /**
   * Creates the empty rules schema.
   *
   * @param idDataSetSchema the id data set schema
   */
  @DeleteMapping(value = "/deleteRulesSchema")
  void deleteRulesSchema(@RequestParam("idDataSetSchema") String idDataSetSchema);



  /**
   * Delete rule by id.
   *
   * @param idDatasetSchema the id dataset schema
   * @param ruleId the rule id
   */
  @DeleteMapping(value = "{idDatasetSchema}/deleteRuleById/{ruleId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  void deleteRuleById(
      @PathVariable(name = "idDatasetSchema", required = true) String idDatasetSchema,
      @PathVariable(name = "ruleId", required = true) String ruleId);

  /**
   * Delete rule by reference id.
   *
   * @param idDatasetSchema the id dataset schema
   * @param referenceId the reference id
   */
  @DeleteMapping(value = "{idDatasetSchema}/deleteRuleByReferenceId/{referenceId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  void deleteRuleByReferenceId(
      @PathVariable(name = "idDatasetSchema", required = true) String idDatasetSchema,
      @PathVariable(name = "referenceId", required = true) String referenceId);


  /**
   * Createnew rule.
   *
   * @param idDatasetSchema the id dataset schema
   * @param ruleVO the rule VO
   */
  @PutMapping(value = "/createNewRule", produces = MediaType.APPLICATION_JSON_VALUE)
  void createNewRule(@RequestParam(name = "idDatasetSchema") String idDatasetSchema,
      @RequestBody RuleVO ruleVO);

  /**
   * Creates the automatic rule.
   *
   * @param idDatasetSchema the id dataset schema
   * @param referenceId the reference id
   * @param typeData the type data
   * @param typeEntityEnum the type entity enum
   * @param requiredRule the required rule
   */
  @PutMapping(value = "/createAutomaticRule", produces = MediaType.APPLICATION_JSON_VALUE)
  void createAutomaticRule(
      @RequestParam(name = "idDatasetSchema", required = true) String idDatasetSchema,
      @RequestParam(name = "referenceId", required = true) String referenceId,
      @RequestParam(name = "typeData", required = true) TypeData typeData,
      @RequestParam(name = "typeEntityEnum", required = true) TypeEntityEnum typeEntityEnum,
      @RequestParam(name = "requiredRule") Boolean requiredRule);

  @PutMapping("/private/deleteRuleRequired")
  void deleteRuleRequired(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("referenceId") String referenceId);

  @PutMapping("/private/existsRuleRequired")
  public Boolean existsRuleRequired(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("referenceId") String referenceId);
}
