package org.eea.validation.controller;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.validation.RulesController;
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

  private static final Logger LOG = LoggerFactory.getLogger(RulesControllerImpl.class);
  /** The rules service. */
  @Autowired
  private RulesService rulesService;


  /**
   * Creates the empty rules schema.
   *
   * @param idDataSetSchema the id data set schema
   * @param idRulesSchema the id rules schema
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/createEmptyRulesSchema")
  public void createEmptyRulesSchema(@RequestParam("idDataSetSchema") String idDataSetSchema,
      @RequestParam("idRulesSchema") String idRulesSchema) {
    rulesService.createEmptyRulesSchema(new ObjectId(idDataSetSchema), new ObjectId(idRulesSchema));
  }

  /**
   * Find rule schema by dataset id.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the rules schema VO
   */
  @Override
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

  @Override
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
  @DeleteMapping(value = "/deleteRulesSchema")
  public void deleteRulesSchema(String idDataSetSchema) {
    rulesService.deleteEmptyRulesScehma(new ObjectId(idDataSetSchema));

  }

  /**
   * Delete rule by id.
   *
   * @param idDatasetSchema the id dataset schema
   * @param ruleId the rule id
   */
  @Override
  @DeleteMapping(value = "{idDatasetSchema}/deleteRuleById/{ruleId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void deleteRuleById(
      @PathVariable(name = "idDatasetSchema", required = true) String idDatasetSchema,
      @PathVariable(name = "ruleId", required = true) String ruleId) {
    if (StringUtils.isBlank(idDatasetSchema)) {
      LOG_ERROR.error(
          "Error deleting  rule with ruleId {} in schema {} because idDatasetSchema is incorrect",
          ruleId, idDatasetSchema);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.IDDATASETSCHEMA_INCORRECT);
    }
    if (StringUtils.isBlank(ruleId)) {
      LOG_ERROR.error(
          "Error deleting  rule with ruleId {} in schema {} because ruleId is incorrect", ruleId,
          idDatasetSchema);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.RULEID_INCORRECT);
    }
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
  @DeleteMapping(value = "{idDatasetSchema}/deleteRuleByReferenceId/{referenceId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void deleteRuleByReferenceId(
      @PathVariable(name = "idDatasetSchema", required = true) String idDatasetSchema,
      @PathVariable(name = "referenceId", required = true) String referenceId) {
    if (StringUtils.isBlank(idDatasetSchema)) {
      LOG_ERROR.error(
          "Error deleting all rules with referenceid {} in schema {} because idDatasetSchema is incorrect",
          referenceId, idDatasetSchema);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.IDDATASETSCHEMA_INCORRECT);
    }

    if (StringUtils.isBlank(referenceId)) {
      LOG_ERROR.error(
          "Error deleting all rules with referenceid {} in schema {} because referenceId is incorrect",
          referenceId, idDatasetSchema);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.REFERENCEID_INCORRECT);
    }
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

}
