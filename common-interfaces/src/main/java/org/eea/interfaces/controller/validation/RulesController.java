/*
 *
 */
package org.eea.interfaces.controller.validation;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.ValueVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.CopySchemaVO;
import org.eea.interfaces.vo.dataset.schemas.ImportSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
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

/** The Interface RulesController. */
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
   * @param dataflowId the dataflow id
   * @return the rules schema VO
   */
  @GetMapping(value = "/{datasetSchemaId}/dataflow/{dataflowId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  RulesSchemaVO findRuleSchemaByDatasetId(@PathVariable("datasetSchemaId") String datasetSchemaId,
      @PathVariable("dataflowId") Long dataflowId);

  /**
   * Find rule schema by dataset id private.
   *
   * @param datasetSchemaId the dataset schema id
   * @param dataflowId the dataflow id
   * @return the rules schema VO
   */
  @GetMapping(value = "/private/{datasetSchemaId}/dataflow/{dataflowId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  RulesSchemaVO findRuleSchemaByDatasetIdPrivate(
      @PathVariable("datasetSchemaId") String datasetSchemaId,
      @PathVariable("dataflowId") Long dataflowId);

  /**
   * Delete rules schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   */
  @DeleteMapping("/private/deleteRulesSchema")
  void deleteRulesSchema(@RequestParam("idDataSetSchema") String datasetSchemaId,
      @RequestParam("datasetId") Long datasetId);

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
   */
  @PutMapping("/createNewRule")
  void createNewRule(@RequestParam("datasetId") long datasetId, @RequestBody RuleVO ruleVO);

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
   * @param typeData the type data
   */
  @PutMapping("/private/deleteRuleRequired")
  void deleteRuleRequired(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("referenceId") String referenceId, @RequestParam("typeData") DataType typeData);

  /**
   * Exists rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return the boolean
   */
  @PutMapping("/private/existsRuleRequired")
  boolean existsRuleRequired(@RequestParam("datasetSchemaId") String datasetSchemaId,
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
   * Update automatic rule.
   *
   * @param datasetId the dataset id
   * @param ruleVO the rule VO
   */
  @PutMapping("/updateAutomaticRule/{datasetId}")
  void updateAutomaticRule(@PathVariable("datasetId") long datasetId, @RequestBody RuleVO ruleVO);

  /**
   * Creates the unique constraint.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @param uniqueId the unique id
   */
  @PostMapping("/private/createUniqueConstraintRule")
  void createUniqueConstraintRule(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("tableSchemaId") String tableSchemaId,
      @RequestParam("uniqueId") String uniqueId);

  /**
   * Delete unique constraint.
   *
   * @param datasetSchemaId the dataset schema id
   * @param uniqueId the unique id
   */
  @DeleteMapping("/private/deleteUniqueConstraintRule")
  void deleteUniqueConstraintRule(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("uniqueId") String uniqueId);


  /**
   * Delete rule high level like.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   */
  @DeleteMapping("/private/deleteRuleHighLevelLike")
  void deleteRuleHighLevelLike(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("fieldSchemaId") String fieldSchemaId);

  /**
   * Delete dataset rule and integrity by field schema id.
   *
   * @param fieldSchemaId the field schema id
   * @param datasetId the dataset id
   */
  @DeleteMapping("/private/deleteDatasetRuleAndIntegrityByIdFieldSchema")
  void deleteDatasetRuleAndIntegrityByFieldSchemaId(
      @RequestParam("fieldSchemaId") String fieldSchemaId,
      @RequestParam("datasetId") Long datasetId);

  /**
   * Delete dataset rule and integrity by dataset schema id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   */
  @DeleteMapping("/private/deleteDatasetRuleAndIntegrityByDatasetSchemaId")
  void deleteDatasetRuleAndIntegrityByDatasetSchemaId(
      @RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("datasetId") Long datasetId);

  /**
   * Copy rules schema.
   *
   * @param copy the copy
   * @return the map
   */
  @PostMapping("/private/copyRulesSchema")
  Map<String, String> copyRulesSchema(@RequestBody CopySchemaVO copy);

  /**
   * Delete not empty rule.
   *
   * @param tableSchemaId the table schema id
   * @param datasetId the dataset id
   */
  @GetMapping("/private/deleteNotEmptyRule")
  void deleteNotEmptyRule(@RequestParam("tableSchemaId") String tableSchemaId,
      @RequestParam("datasetId") Long datasetId);

  /**
   * Update sequence.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the long
   */
  @GetMapping("/private/updateSequence")
  Long updateSequence(@RequestParam("datasetSchemaId") String datasetSchemaId);


  /**
   * Delete not empty rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the list
   */
  @GetMapping("/private/findSqlSentencesByDatasetSchemaId")
  List<RuleVO> findSqlSentencesByDatasetSchemaId(
      @RequestParam("datasetSchemaId") String datasetSchemaId);

  /**
   * Validate sql rule data collection.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param ruleVO the rule VO
   * @return true, if successful
   */
  @PostMapping("/private/validateSqlRuleDataCollection")
  boolean validateSqlRuleDataCollection(@RequestParam("datasetId") Long datasetId,
      @RequestParam("datasetSchemaId") String datasetSchemaId, @RequestBody RuleVO ruleVO);


  /**
   * Validate sql rule.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param ruleVO the rule VO
   */
  @PostMapping("/validateSqlRule")
  void validateSqlRule(@RequestParam("datasetId") Long datasetId,
      @RequestParam("datasetSchemaId") String datasetSchemaId, @RequestBody RuleVO ruleVO);



  /**
   * Validate sql rules.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param showNotification the show notification
   */
  @PostMapping("/validateSqlRules")
  void validateSqlRules(@RequestParam("datasetId") Long datasetId,
      @RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam(value = "showNotification", required = false,
          defaultValue = "true") Boolean showNotification);



  /**
   * Gets the all disabled rules.
   *
   * @param dataflowId the dataflow id
   * @param designs the designs
   * @return the all disabled rules
   */
  @PostMapping("/private/getAllDisabledRules")
  Integer getAllDisabledRules(@RequestParam("dataflowId") Long dataflowId,
      @RequestBody List<DesignDatasetVO> designs);

  /**
   * Gets the all unchecked rules.
   *
   * @param dataflowId the dataflow id
   * @param designs the designs
   * @return the all unchecked rules
   */
  @PostMapping("/private/getAllUncheckedRules")
  Integer getAllUncheckedRules(@RequestParam("dataflowId") Long dataflowId,
      @RequestBody List<DesignDatasetVO> designs);



  /**
   * Delete automatic rule by reference id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   */
  @DeleteMapping("/private/deleteAutomaticRuleByReferenceId")
  void deleteAutomaticRuleByReferenceId(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("referenceId") String referenceId);


  /**
   * Gets the integrity rules by dataset schema id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the integrity rules by dataset schema id
   */
  @GetMapping("/private/getIntegrityRules/{datasetSchemaId}")
  List<IntegrityVO> getIntegrityRulesByDatasetSchemaId(
      @PathVariable("datasetSchemaId") String datasetSchemaId);


  /**
   * Insert integrity schema.
   *
   * @param integritiesVO the integrities VO
   */
  @PostMapping("/private/insertIntegrities")
  void insertIntegritySchema(@RequestBody List<IntegrityVO> integritiesVO);


  /**
   * Import rules schema.
   *
   * @param importRules the import rules
   * @return the map
   */
  @PostMapping("/private/importRulesSchema")
  Map<String, String> importRulesSchema(@RequestBody ImportSchemaVO importRules);



  /**
   * Export QCCSV.
   *
   * @param datasetId the dataset id
   */
  @PostMapping(value = "/exportQC/{datasetId}")
  void exportQCCSV(@PathVariable("datasetId") Long datasetId);


  /**
   * Download QCCSV.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param response the response
   */
  @GetMapping("/downloadQC/{datasetId}")
  void downloadQCCSV(@PathVariable Long datasetId, @RequestParam String fileName,
      HttpServletResponse response);

  /**
   * Run SQL rule with limited results.
   *
   * @param datasetId the dataset id
   * @param sqlRule the sql rule about to be run
   * @param showInternalFields the show internal fields
   * @return the string formatted as JSON
   */
  @PostMapping(value = "/runSqlRule", produces = MediaType.APPLICATION_JSON_VALUE)
  List<List<ValueVO>> runSqlRule(@RequestParam("datasetId") Long datasetId,
      @RequestParam String sqlRule, @RequestParam boolean showInternalFields);


  /**
   * Evaluates the sql rule and returns its total cost using the query plan.
   *
   * @param datasetId the dataset id
   * @param sqlRule the sql rule
   * @return the string containing the SQL total cost
   */
  @PostMapping(value = "/evaluateSqlRule")
  Double evaluateSqlRule(@RequestParam("datasetId") Long datasetId, @RequestParam String sqlRule);


}
