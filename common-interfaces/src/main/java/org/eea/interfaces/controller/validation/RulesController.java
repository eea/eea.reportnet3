/*
 * 
 */
package org.eea.interfaces.controller.validation;

import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
   * @param datasetSchemaId the dataset schema id
   * @param rulesSchemaId the rules schema id
   */
  @PostMapping("/private/createEmptyRulesSchema")
  void createEmptyRulesSchema(@RequestParam("idDataSetSchema") String datasetSchemaId,
      @RequestParam("idRulesSchema") String rulesSchemaId);

  /**
   * Find rule schema by dataset id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the rules schema VO
   */
  @GetMapping(value = "/{datasetSchemaId}", produces = MediaType.APPLICATION_JSON_VALUE)
  RulesSchemaVO findRuleSchemaByDatasetId(@PathVariable("datasetSchemaId") String datasetSchemaId);

  /**
   * Find active rule schema by dataset id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the rules schema VO
   */
  @GetMapping(value = "/{datasetSchemaId}/actives", produces = MediaType.APPLICATION_JSON_VALUE)
  RulesSchemaVO findActiveRuleSchemaByDatasetId(
      @PathVariable("datasetSchemaId") String datasetSchemaId);

  /**
   * Delete rules schema.
   *
   * @param datasetSchemaId the dataset schema id
   */
  @DeleteMapping("/private/deleteRulesSchema")
  void deleteRulesSchema(@RequestParam("idDataSetSchema") String datasetSchemaId);

  /**
   * Delete rule by id.
   *
   * @param datasetId the dataset id
   * @param ruleId the rule id
   */
  @DeleteMapping("/deleteRule")
  void deleteRuleById(@RequestParam("datasetId") long datasetId,
      @RequestParam("ruleId") String ruleId);

  /**
   * Delete rule by reference id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   */
  @DeleteMapping("/private/deleteRuleByReferenceId")
  void deleteRuleByReferenceId(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("referenceId") String referenceId);

  /**
   * Delete rule by Reference field schema PK id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceFieldSchemaPKId the reference field schema PK id
   */
  @DeleteMapping("/private/deleteRuleByReferenceFieldSchemaPKId")
  void deleteRuleByReferenceFieldSchemaPKId(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("referenceFieldSchemaPKId") String referenceFieldSchemaPKId);

  /**
   * Creates the new rule.
   *
   * @param datasetId the dataset id
   * @param ruleVO the rule VO
   * @return the response entity
   */
  @PutMapping("/createNewRule")
  ResponseEntity<?> createNewRule(@RequestParam("datasetId") long datasetId,
      @RequestBody RuleVO ruleVO);


  /**
   * Creates the automatic rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @param typeData the type data
   * @param typeEntityEnum the type entity enum
   * @param datasetId the dataset id
   * @param requiredRule the required rule
   */
  @PutMapping("/private/createAutomaticRule")
  void createAutomaticRule(@RequestParam("idDatasetSchema") String datasetSchemaId,
      @RequestParam("referenceId") String referenceId, @RequestParam("typeData") DataType typeData,
      @RequestParam("typeEntityEnum") EntityTypeEnum typeEntityEnum,
      @RequestParam("datasetId") Long datasetId,
      @RequestParam("requiredRule") boolean requiredRule);

  /**
   * Update rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   */
  @PutMapping("/private/deleteRuleRequired")
  void deleteRuleRequired(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("referenceId") String referenceId);

  /**
   * Exists rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return the boolean
   */
  @PutMapping("/private/existsRuleRequired")
  public boolean existsRuleRequired(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("referenceId") String referenceId);

  /**
   * Update rule.
   *
   * @param datasetId the dataset id
   * @param ruleVO the rule VO
   */
  @PutMapping("/updateRule")
  void updateRule(@RequestParam("datasetId") long datasetId, @RequestBody RuleVO ruleVO);

  /**
   * Insert rule in position.
   *
   * @param ruleId the rule id
   * @param position the position
   * @param datasetSchemaId the dataset schema id
   */
  @PutMapping("/updatePositionRule")
  public void insertRuleInPosition(@RequestParam("ruleId") String ruleId,
      @RequestParam("position") int position,
      @RequestParam("datasetSchemaId") String datasetSchemaId);

  /**
   * Update automatic rule.
   *
   * @param datasetId the dataset id
   * @param ruleVO the rule VO
   */
  @PutMapping("/updateAutomaticRule/{datasetId}")
  void updateAutomaticRule(@PathVariable("datasetId") long datasetId, @RequestBody RuleVO ruleVO);
}
