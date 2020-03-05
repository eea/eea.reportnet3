package org.eea.validation.controller;

import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.validation.RulesController;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.validation.service.RulesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class RulesControllerImpl.
 */
@RestController
@RequestMapping("/rules")
public class RulesControllerImpl implements RulesController {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(RulesControllerImpl.class);

  /** The rules service. */
  @Autowired
  private RulesService rulesService;

  /**
   * Creates the empty rules schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rulesSchemaId the rules schema id
   */
  @Override
  @HystrixCommand
  @PostMapping("/createEmptyRulesSchema")
  public void createEmptyRulesSchema(@RequestParam("idDataSetSchema") String datasetSchemaId,
      @RequestParam("idRulesSchema") String rulesSchemaId) {
    rulesService.createEmptyRulesSchema(datasetSchemaId, rulesSchemaId);
    LOG.info("Creating Schema rules with id {} in datasetSchema {} successfully", rulesSchemaId,
        datasetSchemaId);
  }

  /**
   * Find rule schema by dataset id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the rules schema VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{datasetSchemaId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public RulesSchemaVO findRuleSchemaByDatasetId(
      @PathVariable("datasetSchemaId") String datasetSchemaId) {
    return rulesService.getRulesSchemaByDatasetId(datasetSchemaId);
  }

  /**
   * Find active rule schema by dataset id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the rules schema VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{datasetSchemaId}/actives", produces = MediaType.APPLICATION_JSON_VALUE)
  public RulesSchemaVO findActiveRuleSchemaByDatasetId(
      @PathVariable("datasetSchemaId") String datasetSchemaId) {
    return rulesService.getActiveRulesSchemaByDatasetId(datasetSchemaId);
  }

  /**
   * Delete rules schema.
   *
   * @param datasetSchemaId the dataset schema id
   */
  @Override
  @HystrixCommand
  @DeleteMapping("/deleteRulesSchema")
  public void deleteRulesSchema(String datasetSchemaId) {
    rulesService.deleteEmptyRulesSchema(datasetSchemaId);
  }

  /**
   * Delete rule by id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param ruleId the rule id
   */
  @Override
  @HystrixCommand
  @DeleteMapping("{datasetSchemaId}/deleteRuleById/{ruleId}")
  public void deleteRuleById(@PathVariable("datasetSchemaId") String datasetSchemaId,
      @PathVariable("ruleId") String ruleId) {
    rulesService.deleteRuleById(datasetSchemaId, ruleId);
    LOG.info("Delete the rule with id {} in datasetSchema {} successfully", ruleId,
        datasetSchemaId);
  }

  /**
   * Delete rule by reference id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   */
  @Override
  @HystrixCommand
  @DeleteMapping("{datasetSchemaId}/deleteRuleByReferenceId/{referenceId}")
  public void deleteRuleByReferenceId(@PathVariable("datasetSchemaId") String datasetSchemaId,
      @PathVariable("referenceId") String referenceId) {
    rulesService.deleteRuleByReferenceId(datasetSchemaId, referenceId);
    LOG.info("Delete thes rules with referenceId {} in datasetSchema {} successfully", referenceId,
        datasetSchemaId);
  }

  /**
   * Creates the new rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param ruleVO the rule VO
   */
  @Override
  @HystrixCommand
  @PutMapping("/createNewRule")
  public void createNewRule(@RequestParam("idDatasetSchema") String datasetSchemaId,
      @RequestBody RuleVO ruleVO) {
    rulesService.createNewRule(datasetSchemaId, ruleVO);
  }

  /**
   * Creates the automatic rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @param typeData the type data
   * @param typeEntityEnum the type entity enum
   * @param requiredRule the required rule
   */
  @Override
  @HystrixCommand
  @PutMapping("/createAutomaticRule")
  public void createAutomaticRule(@RequestParam("idDatasetSchema") String datasetSchemaId,
      @RequestParam("referenceId") String referenceId, @RequestParam("typeData") DataType typeData,
      @RequestParam("typeEntityEnum") EntityTypeEnum typeEntityEnum,
      @RequestParam("requiredRule") boolean requiredRule) {

    // we use the required value to differentiate if the rule to create is a required rule or if the
    // rules is a automatic rule for any type (boolean, number)
    if (requiredRule) {
      try {
        rulesService.createAutomaticRules(datasetSchemaId, referenceId, null, typeEntityEnum, true);
      } catch (EEAException e) {
        LOG_ERROR.error(
            "Error creating the required rule for idDatasetSchema {} and field with id {} ",
            datasetSchemaId, referenceId);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.ERROR_CREATING_RULE, e);
      }
    } else {
      try {
        rulesService.createAutomaticRules(datasetSchemaId, referenceId, typeData, typeEntityEnum,
            false);
      } catch (EEAException e) {
        LOG_ERROR.error(
            "Error creating the automatic rule for idDatasetSchema {} and field with id {} for a {} ",
            datasetSchemaId, referenceId, typeData);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.ERROR_CREATING_RULE, e);
      }
    }
    LOG.info("creation automatic rule for a type {} at lv of {} successfully", typeData,
        typeEntityEnum);
  }

  /**
   * Update rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param ruleVO the rule VO
   */
  @Override
  @HystrixCommand
  @PutMapping("/updateRule")
  public void updateRule(@RequestParam("idDatasetSchema") String datasetSchemaId,
      @RequestBody RuleVO ruleVO) {
    if (!rulesService.updateRule(datasetSchemaId, ruleVO)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.ERROR_UPDATING_RULE);
    }
  }

  /**
   * Insert rule in position.
   *
   * @param ruleId the rule id
   * @param position the position
   * @param datasetSchemaId the dataset schema id
   */
  @Override
  @HystrixCommand
  @PutMapping("/updatePositionRule")
  public void insertRuleInPosition(@RequestParam("ruleId") String ruleId,
      @RequestParam("position") int position,
      @RequestParam("datasetSchemaId") String datasetSchemaId) {
    if (!rulesService.insertRuleInPosition(datasetSchemaId, ruleId, position)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.ERROR_ORDERING_RULE);
    }
  }

  /**
   * Exists rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return true, if successful
   */
  @Override
  @PutMapping("/private/existsRuleRequired")
  public boolean existsRuleRequired(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("referenceId") String referenceId) {
    return rulesService.existsRuleRequired(datasetSchemaId, referenceId);
  }

  /**
   * Delete rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   */
  @Override
  @PutMapping("/private/deleteRuleRequired")
  public void deleteRuleRequired(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("referenceId") String referenceId) {
    rulesService.deleteRuleRequired(datasetSchemaId, referenceId);
  }
}
