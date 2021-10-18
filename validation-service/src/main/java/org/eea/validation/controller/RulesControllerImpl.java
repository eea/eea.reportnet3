package org.eea.validation.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.validation.RulesController;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.CopySchemaVO;
import org.eea.interfaces.vo.dataset.schemas.ImportSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.validation.mapper.RuleMapper;
import org.eea.validation.service.RulesService;
import org.eea.validation.service.SqlRulesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;

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

  /** The Constant DELETE_RULES_SUCCESSFULLY: {@value}. */
  private static final String DELETE_RULES_SUCCESSFULLY =
      "Delete the rules with referenceId {} in datasetSchema {} successfully";

  /** The rules service. */
  @Autowired
  private RulesService rulesService;

  /** The sql rules service. */
  @Autowired
  private SqlRulesService sqlRulesService;

  /** The rule mapper. */
  @Autowired
  private RuleMapper ruleMapper;


  /**
   * Creates the empty rules schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rulesSchemaId the rules schema id
   */
  @Override
  @HystrixCommand
  @PostMapping("/private/createEmptyRulesSchema")
  @ApiOperation(value = "Creates an empty rules schema", hidden = true)
  public void createEmptyRulesSchema(@ApiParam(value = "Id dataset schema",
      example = "5cf0e9b3b793310e9ceca190") @RequestParam("idDataSetSchema") String datasetSchemaId,
      @ApiParam(value = "Id rules schema",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("idRulesSchema") String rulesSchemaId) {
    rulesService.createEmptyRulesSchema(datasetSchemaId, rulesSchemaId);
    LOG.info("Creating Schema rules with id {} in datasetSchema {} successfully", rulesSchemaId,
        datasetSchemaId);
  }


  /**
   * Find rule schema by dataset id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param dataflowId the dataflow id
   * @return the rules schema VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{datasetSchemaId}/dataflow/{dataflowId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_READ','DATAFLOW_REPORTER_WRITE','DATAFLOW_NATIONAL_COORDINATOR','DATAFLOW_OBSERVER') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId))")
  @ApiOperation(value = "Retrieves a rules schema based on a given dataset id", hidden = true)
  public RulesSchemaVO findRuleSchemaByDatasetId(@ApiParam(
      value = "Dataset schema id used in the search",
      example = "5cf0e9b3b793310e9ceca190") @PathVariable("datasetSchemaId") String datasetSchemaId,
      @ApiParam(value = "Dataflow Id",
          example = "5cf0e9b3b793310e9ceca190") @PathVariable("dataflowId") Long dataflowId) {
    return rulesService.getRulesSchemaByDatasetId(datasetSchemaId);
  }

  /**
   * Find rule schema by dataset id private.
   *
   * @param datasetSchemaId the dataset schema id
   * @param dataflowId the dataflow id
   * @return the rules schema VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/{datasetSchemaId}/dataflow/{dataflowId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Retrieves a rules schema based on a given dataset id", hidden = true)
  public RulesSchemaVO findRuleSchemaByDatasetIdPrivate(@ApiParam(
      value = "Dataset schema id used in the search",
      example = "5cf0e9b3b793310e9ceca190") @PathVariable("datasetSchemaId") String datasetSchemaId,
      @ApiParam(value = "Dataflow Id",
          example = "5cf0e9b3b793310e9ceca190") @PathVariable("dataflowId") Long dataflowId) {
    return rulesService.getRulesSchemaByDatasetId(datasetSchemaId);
  }

  /**
   * Delete rules schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   */
  @Override
  @HystrixCommand
  @DeleteMapping("/private/deleteRulesSchema")
  @ApiOperation(value = "Deletes a schema using a schema Id as criteria.", hidden = true)
  public void deleteRulesSchema(
      @ApiParam(value = "Dataset schema id used in the delete process.",
          example = "5cf0e9b3b793310e9ceca190") String datasetSchemaId,
      @ApiParam(value = "Dataset id used in the delete process", example = "5") Long datasetId) {
    rulesService.deleteEmptyRulesSchema(datasetSchemaId, datasetId);
  }

  /**
   * Delete rule by id.
   *
   * @param datasetId the dataset id
   * @param ruleId the rule id
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @DeleteMapping("/deleteRule")
  @ApiOperation(value = "Deletes a rule using a rule id as criteria.", hidden = true)
  @ApiResponse(code = 400, message = "Couldn't delete the rule using the specified criteria.")
  public void deleteRuleById(
      @ApiParam(value = "Dataset id used in the delete process",
          example = "5") @RequestParam("datasetId") long datasetId,
      @ApiParam(value = "Rule id used in the delete process",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("ruleId") String ruleId) {
    try {
      rulesService.deleteRuleById(datasetId, ruleId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting rule: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Delete rule by reference id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   */
  @Override
  @HystrixCommand
  @DeleteMapping("/private/deleteRuleByReferenceId")
  @ApiOperation(value = "Deletes a rule using a rule reference id as criteria.", hidden = true)
  public void deleteRuleByReferenceId(@ApiParam(
      value = "Dataset schema id used in the delete process",
      example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId,
      @ApiParam(value = "Rule reference id used in the delete process",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("referenceId") String referenceId) {
    rulesService.deleteRuleByReferenceId(datasetSchemaId, referenceId);
    LOG.info(DELETE_RULES_SUCCESSFULLY, referenceId, datasetSchemaId);
  }

  /**
   * Delete rule by reference field schema PK id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceFieldSchemaPKId the reference field schema PK id
   */
  @Override
  @HystrixCommand
  @DeleteMapping("/private/deleteRuleByReferenceFieldSchemaPKId")
  @ApiOperation(value = "Deletes a rule using a reference field schema primary key id as criteria.",
      hidden = true)
  public void deleteRuleByReferenceFieldSchemaPKId(@ApiParam(
      value = "Dataset schema id used in the delete process",
      example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId,
      @ApiParam(value = "Reference field schema primary key id used in the delete process",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("referenceFieldSchemaPKId") String referenceFieldSchemaPKId) {
    rulesService.deleteRuleByReferenceFieldSchemaPKId(datasetSchemaId, referenceFieldSchemaPKId);
    LOG.info(DELETE_RULES_SUCCESSFULLY, referenceFieldSchemaPKId, datasetSchemaId);
  }

  /**
   * Creates the new rule.
   *
   * @param datasetId the dataset id
   * @param ruleVO the rule VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @PutMapping("/createNewRule")
  @ApiOperation(value = "Creates a new rule using a rule object.", hidden = true)
  @ApiResponse(code = 400, message = "Couldn't create a new rule with given parameters.")
  public void createNewRule(
      @ApiParam(value = "Dataset id used in the creation process",
          example = "15") @RequestParam("datasetId") long datasetId,
      @ApiParam(value = "Rule object used in the creation process") @RequestBody RuleVO ruleVO) {
    try {
      // Set the user name on the thread
      ThreadPropertiesManager.setVariable("user",
          SecurityContextHolder.getContext().getAuthentication().getName());

      rulesService.createNewRule(datasetId, ruleVO);
    } catch (EEAException e) {
      LOG_ERROR.error(
          "Error creating rule: {} - referenceId={} - description={} - ruleName={} - whenCondition={} - thenCondition={} - shortCode={} - type={}",
          e.getMessage(), ruleVO.getReferenceId(), ruleVO.getDescription(), ruleVO.getRuleName(),
          ruleVO.getWhenCondition(), ruleVO.getThenCondition(), ruleVO.getShortCode(),
          ruleVO.getType(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

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
  @Override
  @HystrixCommand
  @PutMapping("/private/createAutomaticRule")
  @ApiOperation(value = "Creates an automatic rule for the given parameters.", hidden = true)
  @ApiResponse(code = 400, message = EEAErrorMessage.ERROR_CREATING_RULE)
  public void createAutomaticRule(@ApiParam(
      value = "Dataset schema id used in the creation process",
      example = "5cf0e9b3b793310e9ceca190") @RequestParam("idDatasetSchema") String datasetSchemaId,
      @ApiParam(value = "Reference id used in the creation process",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("referenceId") String referenceId,
      @ApiParam(value = "Data type used in the creation process",
          example = "BOOLEAN") @RequestParam("typeData") DataType typeData,
      @ApiParam(value = "Entity type used in the creation process",
          example = "DATASET") @RequestParam("typeEntityEnum") EntityTypeEnum typeEntityEnum,
      @ApiParam(value = "Dataset id used in the creation process",
          example = "5") @RequestParam("datasetId") Long datasetId,
      @ApiParam(value = "Is the rule required?",
          example = "true") @RequestParam("requiredRule") boolean requiredRule) {

    // we use the required value to differentiate if the rule to create is a required rule or if the
    // rules is a automatic rule for any type (boolean, number)
    try {
      rulesService.createAutomaticRules(datasetSchemaId, referenceId, typeData, typeEntityEnum,
          datasetId, requiredRule);
    } catch (EEAException e) {
      if (requiredRule) {
        LOG_ERROR.error(
            "Error creating the required rule for idDatasetSchema {} and field with id {} ",
            datasetSchemaId, referenceId);
      } else {
        LOG_ERROR.error(
            "Error creating the automatic rule for idDatasetSchema {} and field with id {} for a {} ",
            datasetSchemaId, referenceId, typeData);
      }
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.ERROR_CREATING_RULE,
          e);
    }
    LOG.info("creation automatic rule for a type {} at lv of {} successfully", typeData,
        typeEntityEnum);
  }

  /**
   * Update rule.
   *
   * @param datasetId the dataset id
   * @param ruleVO the rule VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @PutMapping("/updateRule")
  @ApiOperation(value = "Updates an existing rule with the given rule object.", hidden = true)
  @ApiResponse(code = 400, message = "Error updating the rule with the given object.")
  public void updateRule(
      @ApiParam(value = "Dataset id used in the update process",
          example = "15") @RequestParam("datasetId") long datasetId,
      @ApiParam(value = "Rule object used in the update process") @RequestBody RuleVO ruleVO) {
    try {
      // Set the user name on the thread
      ThreadPropertiesManager.setVariable("user",
          SecurityContextHolder.getContext().getAuthentication().getName());

      rulesService.updateRule(datasetId, ruleVO);
    } catch (EEAException e) {
      LOG_ERROR.error(
          "Error updating rule: {} - ruleId={} - referenceId={} - description={} - ruleName={} - whenCondition={} - thenCondition={} - shortCode={} - type={}",
          e.getMessage(), ruleVO.getRuleId(), ruleVO.getReferenceId(), ruleVO.getDescription(),
          ruleVO.getRuleName(), ruleVO.getWhenCondition(), ruleVO.getThenCondition(),
          ruleVO.getShortCode(), ruleVO.getType(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Update automatic rule.
   *
   * @param datasetId the dataset id
   * @param ruleVO the rule VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @PutMapping("/updateAutomaticRule/{datasetId}")
  @ApiOperation(value = "Updates an automatic rule with the given rule object.", hidden = true)
  @ApiResponse(code = 400, message = "Error updating the automatic rule with the given object.")
  public void updateAutomaticRule(
      @ApiParam(value = "Dataset id used in the update process",
          example = "15") @PathVariable("datasetId") long datasetId,
      @ApiParam(value = "Rule object used in the update process") @RequestBody RuleVO ruleVO) {
    try {
      rulesService.updateAutomaticRule(datasetId, ruleVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Error updating automatic rule: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
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
  @ApiOperation(value = "Returns if the given rule exists.", hidden = true)
  public boolean existsRuleRequired(@ApiParam(
      value = "Dataset schema id used to search for the rule",
      example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId,
      @ApiParam(value = "Reference id used to search for the rule",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("referenceId") String referenceId) {
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
  @ApiOperation(value = "Deletes the rule if exists based on the parameters given", hidden = true)
  public void deleteRuleRequired(@ApiParam(value = "Dataset schema id used to delete the rule",
      example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId,
      @ApiParam(value = "Reference id used to delete the rule",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("referenceId") String referenceId,
      @ApiParam(value = "Data type of the rule",
          example = "POINT") @RequestParam("typeData") DataType typeData) {
    rulesService.deleteRuleRequired(datasetSchemaId, referenceId, typeData);
  }

  /**
   * Creates the unique constraint.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @param uniqueId the unique id
   */
  @Override
  @PostMapping("/private/createUniqueConstraintRule")
  @ApiOperation(value = "Creates a unique constraint rule.", hidden = true)
  public void createUniqueConstraintRule(
      @ApiParam(value = "Dataset schema id used to create the rule",
          example = "5cf0e9b3b793310e9ceca190") String datasetSchemaId,
      @ApiParam(value = "Table schema id used to create the rule",
          example = "5cf0e9b3b793310e9ceca190") String tableSchemaId,
      @ApiParam(value = "Unique id used to create the rule", example = "1") String uniqueId) {
    rulesService.createUniqueConstraint(datasetSchemaId, tableSchemaId, uniqueId);
  }

  /**
   * Delete unique constraint.
   *
   * @param datasetSchemaId the dataset schema id
   * @param uniqueId the unique id
   */
  @Override
  @DeleteMapping("/private/deleteUniqueConstraintRule")
  @ApiOperation(value = "Deletes a unique constraint rule.", hidden = true)
  public void deleteUniqueConstraintRule(@ApiParam(
      value = "Dataset schema id used to delete the rule",
      example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId,
      @ApiParam(value = "Unique id used to delete the rule",
          example = "1") @RequestParam("uniqueId") String uniqueId) {

    rulesService.deleteUniqueConstraint(datasetSchemaId, uniqueId);
  }


  /**
   * Delete rule high level like.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   */
  @Override
  @DeleteMapping("/private/deleteRuleHighLevelLike")
  @ApiOperation(value = "Deletes a high level rule.", hidden = true)
  public void deleteRuleHighLevelLike(@ApiParam(value = "Dataset schema id used to delete the rule",
      example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId,
      @ApiParam(value = "Field schema id used to delete the rule",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("fieldSchemaId") String fieldSchemaId) {
    rulesService.deleteRuleHighLevelLike(datasetSchemaId, fieldSchemaId);
  }

  /**
   * Delete dataset rule and integrity by id field schema.
   *
   * @param fieldSchemaId the field schema id
   * @param datasetId the dataset id
   */
  @Override
  @DeleteMapping("/private/deleteDatasetRuleAndIntegrityByIdFieldSchema")
  @ApiOperation(value = "Deletes a rule and its integrity based on a field schema id",
      hidden = true)
  public void deleteDatasetRuleAndIntegrityByFieldSchemaId(
      @ApiParam(value = "Field schema id used to delete the rule",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("fieldSchemaId") String fieldSchemaId,
      @ApiParam(value = "Dataset id used to delete the rule",
          example = "1") @RequestParam("datasetId") Long datasetId) {
    rulesService.deleteDatasetRuleAndIntegrityByFieldSchemaId(fieldSchemaId, datasetId);
  }

  /**
   * Delete dataset rule and integrity by dataset schema id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   */
  @Override
  @DeleteMapping("/private/deleteDatasetRuleAndIntegrityByDatasetSchemaId")
  @ApiOperation(value = "Deletes a rule and its integrity based on a dataset schema id",
      hidden = true)
  public void deleteDatasetRuleAndIntegrityByDatasetSchemaId(@ApiParam(
      value = "Dataset schema id used to delete the rule",
      example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId,
      @ApiParam(value = "Dataset id used to delete the rule",
          example = "1") @RequestParam("datasetId") Long datasetId) {
    rulesService.deleteDatasetRuleAndIntegrityByDatasetSchemaId(datasetSchemaId, datasetId);

  }

  /**
   * Copy rules schema.
   *
   * @param copy the copy
   * @return the map
   */
  @Override
  @PostMapping("/private/copyRulesSchema")
  @ApiOperation(value = "Copies a rule schema based on a copy schema object", hidden = true)
  @ApiResponse(code = 400, message = "Error copying the rule provided in the object")
  public Map<String, String> copyRulesSchema(
      @ApiParam(value = "Copy schema used to copy the rule") @RequestBody CopySchemaVO copy) {
    try {
      // Set the user name on the thread
      ThreadPropertiesManager.setVariable("user",
          SecurityContextHolder.getContext().getAuthentication().getName());

      return rulesService.copyRulesSchema(copy);
    } catch (EEAException e) {
      LOG_ERROR.error("Error copying rule: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  /**
   * Delete not empty rule.
   *
   * @param tableSchemaId the table schema id
   * @param datasetId the dataset id
   */
  @Override
  @GetMapping("/private/deleteNotEmptyRule")
  @ApiOperation(value = "Deletes a non empty rule based on a dataset and tableschema ids",
      hidden = true)
  public void deleteNotEmptyRule(
      @ApiParam(value = "Table schema id used on the delete process",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("tableSchemaId") String tableSchemaId,
      @ApiParam(value = "Dataset id used on the delete process",
          example = "1") @RequestParam("datasetId") Long datasetId) {
    rulesService.deleteNotEmptyRule(tableSchemaId, datasetId);
  }

  /**
   * Update sequence.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the long
   */
  @Override
  @GetMapping("/private/updateSequence")
  @ApiOperation(value = "Updates the sequence", hidden = true)
  public Long updateSequence(@ApiParam(value = "Dataset schema id used on the update process",
      example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId) {
    return rulesService.updateSequence(datasetSchemaId);
  }

  /**
   * Find sql sentences by dataset schema id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the list
   */
  @Override
  @GetMapping("/private/findSqlSentencesByDatasetSchemaId")
  @ApiOperation(value = "Returns a SQL rule based on a dataset schema id", hidden = true)
  public List<RuleVO> findSqlSentencesByDatasetSchemaId(@ApiParam(
      value = "Dataset schema id used on the search process",
      example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId) {
    return rulesService.findSqlSentencesByDatasetSchemaId(datasetSchemaId);
  }

  /**
   * Validate sql rule data collection.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param ruleVO the rule VO
   * @return true, if successful
   */
  @Override
  @PostMapping("/private/validateSqlRuleDataCollection")
  @ApiOperation(
      value = "Validates the SQL rule pertaining to a dataset when executing data collection",
      hidden = true)
  public boolean validateSqlRuleDataCollection(
      @ApiParam(value = "Dataset id used on the validation process",
          example = "1") @RequestParam("datasetId") Long datasetId,
      @ApiParam(value = "Dataset schema id used on the validation process",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId,
      @ApiParam(value = "Rule object about to be validated") @RequestBody RuleVO ruleVO) {
    return sqlRulesService.validateSQLRuleFromDatacollection(datasetId, datasetSchemaId, ruleVO);
  }


  /**
   * Validate sql rule.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param ruleVO the rule VO
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @PostMapping("/validateSqlRule")
  @ApiOperation(value = "Validates the SQL rule pertaining to a dataset", hidden = true)
  public void validateSqlRule(
      @ApiParam(value = "Dataset id used on the validation process",
          example = "1") @RequestParam("datasetId") Long datasetId,
      @ApiParam(value = "Dataset schema id used on the validation process",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId,
      @ApiParam(value = "Rule object about to be validated") @RequestBody RuleVO ruleVO) {

    sqlRulesService.validateSQLRule(datasetId, datasetSchemaId, ruleMapper.classToEntity(ruleVO));
  }



  /**
   * Validate sql rule.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param showNotification the show notification
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  @PostMapping("/validateSqlRules")
  @ApiOperation(value = "Validates all the SQL rules pertaining to a dataset", hidden = true)
  public void validateSqlRules(
      @ApiParam(value = "Dataset id used on the validation process",
          example = "1") @RequestParam("datasetId") Long datasetId,
      @ApiParam(value = "Dataset schema id used on the validation process",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId,
      @ApiParam(value = "Should a notification be sent?", example = "false",
          defaultValue = "true") @RequestParam(value = "showNotification", required = false,
              defaultValue = "true") Boolean showNotification) {
    sqlRulesService.validateSQLRules(datasetId, datasetSchemaId, showNotification);
  }

  /**
   * Gets the all disabled rules.
   *
   * @param dataflowId the dataflow id
   * @param designs the designs
   * @return the all disabled rules
   */
  @Override
  @PostMapping("/private/getAllDisabledRules")
  @ApiOperation(value = "Returns all the disabled rules pertaining to a dataflow id", hidden = true)
  public Integer getAllDisabledRules(
      @ApiParam(value = "Dataflow id used on the get process",
          example = "1") @RequestParam("dataflowId") Long dataflowId,
      @RequestBody List<DesignDatasetVO> designs) {
    return rulesService.getAllDisabledRules(dataflowId, designs);
  }


  /**
   * Gets the all unchecked rules.
   *
   * @param dataflowId the dataflow id
   * @param designs the designs
   * @return the all unchecked rules
   */
  @Override
  @PostMapping("/private/getAllUncheckedRules")
  @ApiOperation(value = "Returns all the unchecked rules pertaining to a dataflow id",
      hidden = true)
  public Integer getAllUncheckedRules(
      @ApiParam(value = "Dataflow id used on the get process",
          example = "1") @RequestParam("dataflowId") Long dataflowId,
      @RequestBody List<DesignDatasetVO> designs) {
    return rulesService.getAllUncheckedRules(dataflowId, designs);
  }



  /**
   * Delete automatic rule by reference id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   */
  @Override
  @HystrixCommand
  @DeleteMapping("/private/deleteAutomaticRuleByReferenceId")
  @ApiOperation(value = "Deletes an automatic rule by reference id", hidden = true)
  public void deleteAutomaticRuleByReferenceId(@ApiParam(
      value = "Dataset schema id used on the delete process",
      example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId,
      @ApiParam(value = "Reference id used on the delete process",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("referenceId") String referenceId) {
    rulesService.deleteAutomaticRuleByReferenceId(datasetSchemaId, referenceId);
    LOG.info(DELETE_RULES_SUCCESSFULLY, referenceId, datasetSchemaId);
  }


  /**
   * Gets the integrity rules by dataset schema id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the integrity rules by dataset schema id
   */
  @Override
  @HystrixCommand
  @GetMapping("/private/getIntegrityRules/{datasetSchemaId}")
  @ApiOperation(value = "Gets the integrity schemas of a rule based on a dataset schema id",
      hidden = true)
  public List<IntegrityVO> getIntegrityRulesByDatasetSchemaId(@ApiParam(
      value = "Dataset schema id used on the delete process",
      example = "5cf0e9b3b793310e9ceca190") @PathVariable("datasetSchemaId") String datasetSchemaId) {
    return rulesService.getIntegritySchemas(datasetSchemaId);
  }


  /**
   * Insert integrity schema.
   *
   * @param integritiesVO the integrities VO
   */
  @Override
  @HystrixCommand
  @PostMapping("/private/insertIntegrities")
  @ApiOperation(value = "Inserts the integrity schemas", hidden = true)
  public void insertIntegritySchema(@ApiParam(
      value = "Integrities object to be inserted") @RequestBody List<IntegrityVO> integritiesVO) {
    rulesService.insertIntegritySchemas(integritiesVO);
  }


  /**
   * Import rules schema.
   *
   * @param importRules the import rules
   * @return the map
   */
  @Override
  @HystrixCommand
  @PostMapping("/private/importRulesSchema")
  @ApiOperation(value = "Imports all the data from a rule based on a schema", hidden = true)
  @ApiResponse(code = 400, message = "Couldn't retrieve a rule using the provided object.")
  public Map<String, String> importRulesSchema(@ApiParam(
      value = "Schema object used in the import process.") @RequestBody ImportSchemaVO importRules) {
    try {

      // Set the user name on the thread
      ThreadPropertiesManager.setVariable("user",
          SecurityContextHolder.getContext().getAuthentication().getName());

      return rulesService.importRulesSchema(importRules.getQcRulesBytes(),
          importRules.getDictionaryOriginTargetObjectId(), importRules.getIntegritiesVO());
    } catch (EEAException e) {
      LOG_ERROR.error("Error importing the rules: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  /**
   * Export QCCSV.
   *
   * @param datasetId the dataset id
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_OBSERVER','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASET_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD','REFERENCEDATASET_OBSERVER') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @PostMapping(value = "/exportQC/{datasetId}")
  @ApiOperation(value = "Exports all the QCs into a CSV file", hidden = true)
  public void exportQCCSV(@ApiParam(value = "Dataset id used in the export process.",
      example = "10") @PathVariable("datasetId") Long datasetId) {
    LOG.info("Export dataset QC from datasetId {}, with type .csv", datasetId);
    try {
      rulesService.exportQCCSV(datasetId);
    } catch (EEAException | IOException e) {
      LOG_ERROR.error("Error exporting QCS from the dataset {}.  Message: {}", datasetId,
          e.getMessage());
    }
  }

  /**
   * Download QCCSV.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param response the response
   */
  @Override
  @GetMapping("/downloadQC/{datasetId}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_OBSERVER','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASET_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD','REFERENCEDATASET_OBSERVER') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiOperation(value = "Download the generated CSV file containing the QCs", hidden = true)
  @ApiResponse(code = 404, message = "Couldn't find a file with the specified name.")
  public void downloadQCCSV(
      @ApiParam(value = "Dataset id used in the export process.",
          example = "10") @PathVariable Long datasetId,
      @ApiParam(value = "The filename the export process assigned to the QC Export file.",
          example = "dataset-10-QCS-yyyy-MM-dd HH.mm.ss") @RequestParam String fileName,
      HttpServletResponse response) {
    try {
      LOG.info("Downloading file generated when exporting QC Rules. DatasetId {}. Filename {}",
          datasetId, fileName);
      File file = rulesService.downloadQCCSV(datasetId, fileName);
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

      OutputStream out = response.getOutputStream();
      FileInputStream in = new FileInputStream(file);
      // copy from in to out
      IOUtils.copyLarge(in, out);
      out.close();
      in.close();
      // delete the file after downloading it
      FileUtils.forceDelete(file);
    } catch (IOException | ResponseStatusException e) {
      LOG_ERROR.error(
          "Downloading file generated when exporting QC Rules. DatasetId {}. Filename {}. Error message: {}",
          datasetId, fileName, e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(
          "Trying to download a file generated during the export QC Rules process but the file is not found, datasetID: %s + filename: %s + message: %s ",
          datasetId, fileName, e.getMessage()), e);
    }
  }
}
