package org.eea.validation.controller;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.validation.RulesController;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.validation.mapper.RuleMapper;
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
@RequestMapping(value = "/rules")
public class RulesControllerImpl implements RulesController {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(RulesControllerImpl.class);

  /** The rules service. */
  @Autowired
  private RulesService rulesService;


  /** The rule mapper. */
  @Autowired
  private RuleMapper ruleMapper;


  /**
   * Creates the empty rules schema.
   *
   * @param idDatasetSchema the id dataset schema
   * @param idRulesSchema the id rules schema
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/createEmptyRulesSchema")
  public void createEmptyRulesSchema(@RequestParam("idDataSetSchema") String idDatasetSchema,
      @RequestParam("idRulesSchema") String idRulesSchema) {
    rulesService.createEmptyRulesSchema(new ObjectId(idDatasetSchema), new ObjectId(idRulesSchema));
    LOG.info("Creating Schema rules with id {} in datasetSchema {} successfully", idRulesSchema,
        idDatasetSchema);

  }

  /**
   * Find rule schema by dataset id.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the rules schema VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{idDatasetSchema}", produces = MediaType.APPLICATION_JSON_VALUE)
  public RulesSchemaVO findRuleSchemaByDatasetId(
      @PathVariable("idDatasetSchema") String idDatasetSchema) {
    if (StringUtils.isBlank(idDatasetSchema)) {
      LOG_ERROR.error(
          "Error find datasetschema with idDatasetSchema {} because idDatasetSchema is incorrect",
          idDatasetSchema);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.IDDATASETSCHEMA_INCORRECT);

    } else {
      return rulesService.getRulesSchemaByDatasetId(idDatasetSchema);
    }
  }

  /**
   * Find active rule schema by dataset id.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the rules schema VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{idDatasetSchema}/actives", produces = MediaType.APPLICATION_JSON_VALUE)
  public RulesSchemaVO findActiveRuleSchemaByDatasetId(
      @PathVariable("idDatasetSchema") String idDatasetSchema) {
    if (StringUtils.isBlank(idDatasetSchema)) {
      LOG_ERROR.error(
          "Error find active datasetschema with idDatasetSchema {} because idDatasetSchema is incorrect",
          idDatasetSchema);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.IDDATASETSCHEMA_INCORRECT);

    } else {
      return rulesService.getActiveRulesSchemaByDatasetId(idDatasetSchema);
    }
  }

  /**
   * Creates the empty rules schema.
   *
   * @param idDataSetSchema the id data set schema
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/deleteRulesSchema")
  public void deleteRulesSchema(String idDataSetSchema) {
    if (StringUtils.isBlank(idDataSetSchema)) {
      LOG_ERROR.error(
          "Error find active datasetschema with idDatasetSchema {} because idDatasetSchema is incorrect",
          idDataSetSchema);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.IDDATASETSCHEMA_INCORRECT);

    } else {
      rulesService.deleteEmptyRulesScehma(new ObjectId(idDataSetSchema));
    }
  }

  /**
   * Delete rule by id.
   *
   * @param idDatasetSchema the id dataset schema
   * @param ruleId the rule id
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "{idDatasetSchema}/deleteRuleById/{ruleId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void deleteRuleById(
      @PathVariable(name = "idDatasetSchema", required = true) String idDatasetSchema,
      @PathVariable(name = "ruleId", required = true) String ruleId) {
    try {
      rulesService.deleteRuleById(idDatasetSchema, ruleId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting the rule  with id {} in datasetSchema {}", ruleId,
          idDatasetSchema);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.ERROR_DELETING_RULE, e);
    }
    LOG.info("Delete the rule with id {} in datasetSchema {} successfully", ruleId,
        idDatasetSchema);
  }

  /**
   * Delete rule by reference id.
   *
   * @param idDatasetSchema the id dataset schema
   * @param referenceId the reference id
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "{idDatasetSchema}/deleteRuleByReferenceId/{referenceId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void deleteRuleByReferenceId(
      @PathVariable(name = "idDatasetSchema", required = true) String idDatasetSchema,
      @PathVariable(name = "referenceId", required = true) String referenceId) {
    try {
      rulesService.deleteRuleByReferenceId(idDatasetSchema, referenceId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting the rules  with referenceId {} in datasetSchema {}",
          referenceId, idDatasetSchema);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.ERROR_DELETING_RULE, e);
    }

    LOG.info("Delete thes rules with referenceId {} in datasetSchema {} successfully", referenceId,
        idDatasetSchema);
  }

  /**
   * Creates the new rule.
   *
   * @param idDatasetSchema the id dataset schema
   * @param ruleVO the rule VO
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/createNewRule", produces = MediaType.APPLICATION_JSON_VALUE)
  public void createNewRule(@RequestParam(name = "idDatasetSchema") String idDatasetSchema,
      @RequestBody RuleVO ruleVO) {
    try {
      rulesService.createNewRule(idDatasetSchema, ruleMapper.classToEntity(ruleVO));
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting the rules  with idDatasetSchema {} ", idDatasetSchema);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.ERROR_DELETING_RULE,
          e);
    }

  }



  /**
   * Creates the automatic rule.
   *
   * @param idDatasetSchema the id dataset schema
   * @param referenceId the reference id
   * @param typeData the type data
   * @param typeEntityEnum the type entity enum
   * @param requiredRule the required rule
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/createAutomaticRule", produces = MediaType.APPLICATION_JSON_VALUE)
  public void createAutomaticRule(@RequestParam(name = "idDatasetSchema") String idDatasetSchema,
      @RequestParam(name = "referenceId") String referenceId,
      @RequestParam(name = "typeData") DataType typeData,
      @RequestParam(name = "typeEntityEnum") EntityTypeEnum typeEntityEnum,
      @RequestParam(name = "requiredRule") Boolean requiredRule) {

    // we use the required value to differentiate if the rule to create is a required rule or if the
    // rules is a automatic rule for any type (boolean, number)
    if (Boolean.TRUE.equals(requiredRule)) {
      try {
        rulesService.createAutomaticRules(idDatasetSchema, referenceId, null, typeEntityEnum,
            Boolean.TRUE);
      } catch (EEAException e) {
        LOG_ERROR.error(
            "Error creating the required rule for idDatasetSchema {} and field with id {} ",
            idDatasetSchema, referenceId);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.ERROR_CREATING_RULE, e);
      }
    } else {
      try {
        rulesService.createAutomaticRules(idDatasetSchema, referenceId, typeData, typeEntityEnum,
            Boolean.FALSE);
      } catch (EEAException e) {
        LOG_ERROR.error(
            "Error creating the automatic rule for idDatasetSchema {} and field with id {} for a {} ",
            idDatasetSchema, referenceId, typeData);
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
   * @param idDatasetSchema the id dataset schema
   * @param ruleVO the rule VO
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/updateRule", produces = MediaType.APPLICATION_JSON_VALUE)
  public void updateRule(@RequestParam(name = "idDatasetSchema") String idDatasetSchema,
      @RequestBody RuleVO ruleVO) {
    if (!rulesService.updateRule(idDatasetSchema, ruleMapper.classToEntity(ruleVO))) {
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
  @PutMapping(value = "/updatePositionRule", produces = MediaType.APPLICATION_JSON_VALUE)
  public void insertRuleInPosition(@RequestParam(name = "ruleId") String ruleId,
      @RequestParam(name = "position") int position,
      @RequestParam(name = "datasetSchemaId") String datasetSchemaId) {
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
   * @return the boolean
   */
  @Override
  @PutMapping("/private/existsRuleRequired")
  public Boolean existsRuleRequired(@RequestParam("datasetSchemaId") String datasetSchemaId,
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
