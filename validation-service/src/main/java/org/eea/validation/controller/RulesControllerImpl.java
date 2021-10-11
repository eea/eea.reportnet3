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
   * Delete rules schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   */
  @Override
  @HystrixCommand
  @DeleteMapping("/private/deleteRulesSchema")
  public void deleteRulesSchema(String datasetSchemaId, Long datasetId) {
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
  public void deleteRuleById(@RequestParam("datasetId") long datasetId,
      @RequestParam("ruleId") String ruleId) {
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
  public void deleteRuleByReferenceId(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("referenceId") String referenceId) {
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
  public void deleteRuleByReferenceFieldSchemaPKId(
      @RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("referenceFieldSchemaPKId") String referenceFieldSchemaPKId) {
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
  public void createNewRule(@RequestParam("datasetId") long datasetId, @RequestBody RuleVO ruleVO) {
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
  public void createAutomaticRule(@RequestParam("idDatasetSchema") String datasetSchemaId,
      @RequestParam("referenceId") String referenceId, @RequestParam("typeData") DataType typeData,
      @RequestParam("typeEntityEnum") EntityTypeEnum typeEntityEnum,
      @RequestParam("datasetId") Long datasetId,
      @RequestParam("requiredRule") boolean requiredRule) {

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
  public void updateRule(@RequestParam("datasetId") long datasetId, @RequestBody RuleVO ruleVO) {
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
  public void updateAutomaticRule(@PathVariable("datasetId") long datasetId,
      @RequestBody RuleVO ruleVO) {
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
      @RequestParam("referenceId") String referenceId,
      @RequestParam("typeData") DataType typeData) {
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
  public void createUniqueConstraintRule(String datasetSchemaId, String tableSchemaId,
      String uniqueId) {
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
  public void deleteUniqueConstraintRule(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("uniqueId") String uniqueId) {

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
  public void deleteRuleHighLevelLike(@RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("fieldSchemaId") String fieldSchemaId) {
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
  public void deleteDatasetRuleAndIntegrityByFieldSchemaId(
      @RequestParam("fieldSchemaId") String fieldSchemaId,
      @RequestParam("datasetId") Long datasetId) {
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
  public void deleteDatasetRuleAndIntegrityByDatasetSchemaId(
      @RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("datasetId") Long datasetId) {
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
  public Map<String, String> copyRulesSchema(@RequestBody CopySchemaVO copy) {
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
  public void deleteNotEmptyRule(@RequestParam("tableSchemaId") String tableSchemaId,
      @RequestParam("datasetId") Long datasetId) {
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
  public Long updateSequence(@RequestParam("datasetSchemaId") String datasetSchemaId) {
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
  public List<RuleVO> findSqlSentencesByDatasetSchemaId(
      @RequestParam("datasetSchemaId") String datasetSchemaId) {
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
  public boolean validateSqlRuleDataCollection(@RequestParam("datasetId") Long datasetId,
      @RequestParam("datasetSchemaId") String datasetSchemaId, @RequestBody RuleVO ruleVO) {
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
  public void validateSqlRule(@RequestParam("datasetId") Long datasetId,
      @RequestParam("datasetSchemaId") String datasetSchemaId, @RequestBody RuleVO ruleVO) {

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
  public void validateSqlRules(@RequestParam("datasetId") Long datasetId,
      @RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam(value = "showNotification", required = false,
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
  public Integer getAllDisabledRules(@RequestParam("dataflowId") Long dataflowId,
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
  public Integer getAllUncheckedRules(@RequestParam("dataflowId") Long dataflowId,
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
  public void deleteAutomaticRuleByReferenceId(
      @RequestParam("datasetSchemaId") String datasetSchemaId,
      @RequestParam("referenceId") String referenceId) {
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
  public List<IntegrityVO> getIntegrityRulesByDatasetSchemaId(
      @PathVariable("datasetSchemaId") String datasetSchemaId) {
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
  public void insertIntegritySchema(@RequestBody List<IntegrityVO> integritiesVO) {
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
  public Map<String, String> importRulesSchema(@RequestBody ImportSchemaVO importRules) {
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
  public void exportQCCSV(@PathVariable("datasetId") Long datasetId) {
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
  public void downloadQCCSV(@PathVariable Long datasetId, @RequestParam String fileName,
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
