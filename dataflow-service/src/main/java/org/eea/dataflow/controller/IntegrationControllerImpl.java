
package org.eea.dataflow.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.dataflow.integration.executor.IntegrationExecutorFactory;
import org.eea.dataflow.service.IntegrationService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataflow.IntegrationController;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.schemas.CopySchemaVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.lock.service.LockService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;

/**
 * The Class IntegrationControllerImpl.
 */
@RestController
@RequestMapping("/integration")
@Api(tags = "Integrations : Integrations Manager")
public class IntegrationControllerImpl implements IntegrationController {

  /** The Constant ERROR_FINDING_INTEGRATIONS: {@value}. */
  private static final String ERROR_FINDING_INTEGRATIONS = "Error finding integrations: {}";

  /**
   * The integration service.
   */
  @Autowired
  private IntegrationService integrationService;

  /**
   * The FME integration executor factory.
   */
  @Autowired
  private IntegrationExecutorFactory integrationExecutorFactory;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The notification controller zuul. */
  @Autowired
  private NotificationControllerZuul notificationControllerZuul;

  /** The data set metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  /** The job controller zuul. */
  @Autowired
  private JobControllerZuul jobControllerZuul;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(IntegrationControllerImpl.class);

  /**
   * Find all integrations by criteria.
   *
   * @param integrationVO the integration VO
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#integrationVO.internalParameters['dataflowId'],'DATAFLOW_EDITOR_WRITE','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_READ','DATAFLOW_LEAD_REPORTER','DATAFLOW_STEWARD')")
  @PutMapping(value = "/listIntegrations", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find all Integrations by Integration Criteria",
          produces = MediaType.APPLICATION_JSON_VALUE, response = IntegrationVO.class,
          responseContainer = "List", hidden = true)
  @ApiResponse(code = 500, message = "Internal Server Error")
  public List<IntegrationVO> findAllIntegrationsByCriteria(@ApiParam(type = "Object",
          value = "IntegrationVO Object") @RequestBody IntegrationVO integrationVO) {
    try {
      return integrationService.getAllIntegrationsByCriteria(integrationVO);
    } catch (EEAException e) {
      LOG.error(ERROR_FINDING_INTEGRATIONS, e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.RETRIEVING_INTEGRATIONS);
    } catch (Exception e){
      Long integrationId = (integrationVO != null) ? integrationVO.getId() : null;
      String integrationName = (integrationVO != null) ? integrationVO.getName() : null;
      LOG.error("Unexpected error! Could not retrieve integrations by criteria. id is {} and name is {}. Message: {}", integrationId, integrationName, e.getMessage());
      throw e;
    }
  }

  /**
   * Find export EU dataset integration by dataset id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param dataflowId the dataflow id
   * @return the integration VO
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_LEAD_REPORTER', 'DATAFLOW_CUSTODIAN')")
  @GetMapping("/findExportEUDatasetIntegration")
  @ApiOperation(value = "Find EU Dataset Export Integration by its Schema Id",
          produces = MediaType.APPLICATION_JSON_VALUE, response = IntegrationVO.class, hidden = true)
  public IntegrationVO findExportEUDatasetIntegration(@ApiParam(value = "Dataset Schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId,
                                                      @ApiParam(value = "Dataflow id", example = "0") @RequestParam("dataflowId") Long dataflowId) {
    return integrationService.getExportEUDatasetIntegration(datasetSchemaId);
  }

  /**
   * Creates the integration.
   *
   * @param integration the integration
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#integration.internalParameters['dataflowId'],'DATAFLOW_STEWARD','DATAFLOW_EDITOR_WRITE', 'DATAFLOW_CUSTODIAN')")
  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Create Integration", hidden = true)
  @ApiResponse(code = 500, message = "Internal Server Error")
  public void createIntegration(@ApiParam(type = "Object",
          value = "IntegrationVO Object") @RequestBody IntegrationVO integration) {
    try {
      integrationService.createIntegration(integration);
    } catch (EEAException e) {
      LOG.error("Error creating integration. Message: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.CREATING_INTEGRATION);
    } catch (Exception e){
      Long integrationId = (integration != null) ? integration.getId() : null;
      String integrationName = (integration != null) ? integration.getName() : null;
      LOG.error("Unexpected error! Could not create integration with id {} and name {}. Message: {}", integrationId, integrationName, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete integration.
   *
   * @param integrationId the integration id
   * @param dataflowId the dataflow id
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_EDITOR_WRITE', 'DATAFLOW_CUSTODIAN')")
  @DeleteMapping(value = "/{integrationId}/dataflow/{dataflowId}")
  @ApiOperation(value = "Delete Integration by its Integration Id", hidden = true)
  @ApiResponse(code = 500, message = "Internal Server Error")
  public void deleteIntegration(
          @ApiParam(value = "Integration id",
                  example = "0") @PathVariable("integrationId") Long integrationId,
          @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    try {
      LOG.info("Deleting integration with id {} for dataflowId {}", integrationId, dataflowId);
      integrationService.deleteIntegration(integrationId);
      LOG.info("Successfully deleted integration with id {} for dataflowId {}", integrationId, dataflowId);
    } catch (EEAException e) {
      LOG.error("Error deleting an integration with id {} for dataflowId {}. Message: {}", integrationId, dataflowId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.DELETING_INTEGRATION);
    } catch (Exception e){
      LOG.error("Unexpected error! Could not delete integration with id {} for dataflowId {}. Message: {}", integrationId, dataflowId, e.getMessage());
      throw e;
    }
  }


  /**
   * Update integration.
   *
   * @param integration the integration
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#integration.internalParameters['dataflowId'],'DATAFLOW_STEWARD','DATAFLOW_EDITOR_WRITE', 'DATAFLOW_CUSTODIAN')")
  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Update Integration", hidden = true)
  @ApiResponse(code = 500, message = "Internal Server Error")
  public void updateIntegration(@ApiParam(type = "Object",
          value = "IntegrationVO Object") @RequestBody IntegrationVO integration) {
    try {
      integrationService.updateIntegration(integration);
    } catch (EEAException e) {
      LOG.error("Error updating an integration. Message: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.DELETING_INTEGRATION);
    } catch (Exception e){
      Long integrationId = (integration != null) ? integration.getId() : null;
      String integrationName = (integration != null) ? integration.getName() : null;
      LOG.error("Unexpected error! Could not update integration with id {} and name {}. Message: {}", integrationId, integrationName, e.getMessage());
      throw e;
    }
  }

  /**
   * Find extensions and operations.
   *
   * @param integrationVO the integration VO
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#integrationVO.internalParameters['dataflowId'],'DATAFLOW_STEWARD','DATAFLOW_EDITOR_WRITE','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR','DATAFLOW_OBSERVER','DATAFLOW_STEWARD_SUPPORT','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_READ','DATAFLOW_REPORTER_WRITE') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#integrationVO.internalParameters['dataflowId']))")
  @PutMapping(value = "/listExtensionsOperations", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find Integrations and Operations by Integration Criteria",
          produces = MediaType.APPLICATION_JSON_VALUE, response = IntegrationVO.class,
          responseContainer = "List", hidden = true)
  @ApiResponse(code = 500, message = "Internal Server Error")
  public List<IntegrationVO> findExtensionsAndOperations(@ApiParam(type = "Object",
          value = "IntegrationVO Object") @RequestBody IntegrationVO integrationVO) {
    try {
      return integrationService.getOnlyExtensionsAndOperations(
              integrationService.getAllIntegrationsByCriteria(integrationVO));
    } catch (EEAException e) {
      LOG.error(ERROR_FINDING_INTEGRATIONS, e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.RETRIEVING_INTEGRATIONS);
    } catch (Exception e){
      Long integrationId = (integrationVO != null) ? integrationVO.getId() : null;
      String integrationName = (integrationVO != null) ? integrationVO.getName() : null;
      LOG.error("Unexpected error! Could not find Integrations and Operations by Integration Criteria with id {} and name {}. Message: {}", integrationId, integrationName, e.getMessage());
      throw e;
    }
  }


  /**
   * Find extensions and operations private.
   *
   * @param integrationVO the integration VO
   * @return the list
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/private/listPublicExtensionsOperations",
          produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find Integrations and Operations by Integration Criteria",
          produces = MediaType.APPLICATION_JSON_VALUE, response = IntegrationVO.class,
          responseContainer = "List", hidden = true)
  @ApiResponse(code = 500, message = "Internal Server Error")
  public List<IntegrationVO> findExtensionsAndOperationsPrivate(@ApiParam(type = "Object",
          value = "IntegrationVO Object") @RequestBody IntegrationVO integrationVO) {
    try {
      return integrationService.getOnlyExtensionsAndOperations(
              integrationService.getAllIntegrationsByCriteria(integrationVO));
    } catch (EEAException e) {
      LOG.error(ERROR_FINDING_INTEGRATIONS, e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.RETRIEVING_INTEGRATIONS);
    } catch (Exception e){
      Long integrationId = (integrationVO != null) ? integrationVO.getId() : null;
      String integrationName = (integrationVO != null) ? integrationVO.getName() : null;
      LOG.error("Unexpected error! Could not find Integrations and Operations by Integration Criteria with id {} and name {}. Message: {}", integrationId, integrationName, e.getMessage());
      throw e;
    }
  }

  /**
   * Execute integration process.
   *
   * @param integrationToolTypeEnum the integration tool type enum
   * @param integrationOperationTypeEnum the integration operation type enum
   * @param file the file
   * @param datasetId the dataset id
   * @param jobId the job id
   * @param integration the integration
   *
   * @return the execution result VO
   */
  @Override
  @HystrixCommand(commandProperties = {@HystrixProperty(
          name = "execution.isolation.thread.timeoutInMilliseconds", value = "7200000")})
  @PostMapping(value = "/private/executeIntegration")
  @ApiOperation(value = "Find Integrations and Operations by Integration Criteria",
          response = ExecutionResultVO.class, hidden = true)
  public ExecutionResultVO executeIntegrationProcess(@ApiParam(type = "Object",
          value = "IntegrationEnum Object",
          example = "FME") @RequestParam("integrationTool") IntegrationToolTypeEnum integrationToolTypeEnum,
                                                     @ApiParam(type = "Object", value = "OperationEnum Object",
                                                             example = "EXPORT") @RequestParam("operation") IntegrationOperationTypeEnum integrationOperationTypeEnum,
                                                     @ApiParam(type = "file", value = "File") @RequestParam(name = "file",
                                                             required = false) final String file,
                                                     @ApiParam(value = "Dataset id", example = "0") @RequestParam("datasetId") Long datasetId,
                                                     @ApiParam(type = "Object",
                                                             value = "IntegrationVO Object") @RequestBody IntegrationVO integration,
                                                     @RequestParam(name = "jobId", required = false) final String jobId) {
    try{
      JobVO jobVO = null;
      if(jobId != null) {
        jobVO = jobControllerZuul.findJobById(Long.parseLong(jobId));
        LOG.info("Retrieved job with id {}", jobId);
      }
      return integrationExecutorFactory.getExecutor(integrationToolTypeEnum)
              .execute(integrationOperationTypeEnum, file, datasetId, integration, jobVO);
    } catch (Exception e) {
      LOG.error("Unexpected error! Could not execute integration process for file {} and datasetId {}. Message: {}", file, datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Execute EU dataset export.
   *
   * @param dataflowId the dataflow id
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD') OR (checkApiKey(#dataflowId,null,#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD'))")
  @LockMethod
  @PostMapping(value = "/v1/executeEUDatasetExport")
  @ApiOperation(value = "Execute EU dataset export", response = ExecutionResultVO.class,
          responseContainer = "List", notes = "Allowed roles: CUSTODIAN, STEWARD")
  @ApiResponse(code = 500, message = "Internal Server Error")
  public List<ExecutionResultVO> executeEUDatasetExport(
          @ApiParam(value = "Dataflow id", example = "0") @LockCriteria(
                  name = "dataflowId") @RequestParam("dataflowId") Long dataflowId) {
    List<ExecutionResultVO> results = null;
    try{
      UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
      userNotificationContentVO.setDataflowId(dataflowId);
      notificationControllerZuul.createUserNotificationPrivate("EXPORT_EU_DATASET_INIT",
              userNotificationContentVO);

      integrationService.addPopulateEUDatasetLock(dataflowId);
      LOG.info("Executing EU dataset export for dataflowId {}", dataflowId);
      results = integrationService.executeEUDatasetExport(dataflowId);
      LOG.info("Successfully executed EU dataset export for dataflowId {}", dataflowId);
    } catch (EEAException e) {
      LOG.error("Error executing the export from EUDataset for dataflowId {} with message: {}", dataflowId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.EXPORTING_EU_DATASET);
    } catch (Exception e){
      LOG.error("Unexpected error! Could not execute EU dataset export for dataflowId {}. Message: {}", dataflowId, e.getMessage());
      throw e;
    } finally {
      integrationService.releasePopulateEUDatasetLock(dataflowId);
    }
    return results;
  }

  /**
   * Execute EU dataset export legacy.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD') OR (checkApiKey(#dataflowId,null,#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD'))")
  @LockMethod
  @PostMapping(value = "/executeEUDatasetExport")
  @ApiOperation(value = "Execute EUDataset Export", response = ExecutionResultVO.class,
          hidden = true, responseContainer = "List")
  @ApiResponse(code = 500, message = "Internal Server Error")
  public List<ExecutionResultVO> executeEUDatasetExportLegacy(
          @ApiParam(value = "Dataflow Id", example = "0") @LockCriteria(
                  name = "dataflowId") @RequestParam("dataflowId") Long dataflowId) {
    return this.executeEUDatasetExport(dataflowId);
  }

  /**
   * Copy integrations.
   *
   * @param copyVO the copy VO
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/private/copyIntegrations", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Copy Integrations", response = ExecutionResultVO.class, hidden = true)
  @ApiResponse(code = 500, message = "Internal Server Error")
  public void copyIntegrations(
          @ApiParam(type = "Object", value = "CopySchemaVO Object") @RequestBody CopySchemaVO copyVO) {
    try {
      integrationService.copyIntegrations(copyVO.getDataflowIdDestination(),
              copyVO.getOriginDatasetSchemaIds(), copyVO.getDictionaryOriginTargetObjectId());
    } catch (EEAException e) {
      if(copyVO != null){
        LOG.error("Error copying integrations to dataflowId {}. Message: {}", copyVO.getDataflowIdDestination(), e.getMessage());
      }else{
        LOG.error("Error copying integrations because object is null. Message: {}", e.getMessage());
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.COPYING_INTEGRATIONS);
    } catch (Exception e){
      Long dataflowId = (copyVO != null) ? copyVO.getDataflowIdDestination() : null;
      LOG.error("Unexpected error! Could not copy integrations to dataflowId {}. Message: {}", dataflowId, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the default integration.
   *
   * @param dataflowId the dataflow id
   * @param datasetSchemaId the dataset schema id
   */
  @Override
  @PostMapping("/private/createDefaultIntegration")
  @ApiOperation(value = "Create a Default Integration", hidden = true)
  public void createDefaultIntegration(
          @ApiParam(value = "Dataflow id", example = "0") @RequestParam("dataflowId") Long dataflowId,
          @ApiParam(value = "Dataset Schema id",
                  example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId) {
    try {
      LOG.info("Creating default integration for dataflowId {} and datasetSchemaId {}", dataflowId, datasetSchemaId);
      integrationService.createDefaultIntegration(dataflowId, datasetSchemaId);
      LOG.info("Successfully created default integration for dataflowId {} and datasetSchemaId {}", dataflowId, datasetSchemaId);
    } catch (EEAException e) {
      LOG.error("Error creating default integration for dataflowId {} and datasetSchemaId {}. Message: {}", dataflowId, datasetSchemaId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.CREATING_INTEGRATION);
    } catch (Exception e){
      LOG.error("Unexpected error! Could not create default integration for dataflowId {} and datasetSchemaId {}. Message: {}", dataflowId, datasetSchemaId, e.getMessage());
      throw e;
    }
  }


  /**
   * Find export integration.
   *
   * @param datasetSchemaId the dataset schema id
   * @param integrationId the integration id
   * @return the integration VO
   */
  @Override
  @GetMapping("/private/findExportIntegration")
  @ApiOperation(
          value = "Find Integration for data export processes based on their Schema and file extension",
          hidden = true)
  public IntegrationVO findExportIntegration(@ApiParam(value = "Dataschema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId,
                                             @ApiParam(value = "Integration Id",
                                                     example = "1") @RequestParam("integrationId") Long integrationId) {
    return integrationService.getExportIntegration(datasetSchemaId, integrationId);
  }

  /**
   * Delete schema integrations.
   *
   * @param datasetSchemaId the dataset schema id
   */
  @Override
  @DeleteMapping("/private/deleteSchemaIntegrations")
  @ApiOperation(value = "Delete an Integration from its Schema", hidden = true)
  public void deleteSchemaIntegrations(@ApiParam(value = "Dataschema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("datasetSchemaId") String datasetSchemaId) {
    try {
      LOG.info("Deleting schema integrations for datasetSchemaId {}", datasetSchemaId);
      integrationService.deleteSchemaIntegrations(datasetSchemaId);
      LOG.info("Successfully deleted schema integrations for datasetSchemaId {}", datasetSchemaId);
    } catch (Exception e){
      LOG.error("Unexpected error! Could not delete schema integration for datasetSchemaId {} Message: {}", datasetSchemaId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete export eu dataset integration.
   *
   * @param datasetSchemaId the dataset schema id
   */
  @Override
  @DeleteMapping("/private/deleteExportEuDataset")
  @ApiOperation(value = "Delete an export EU Dataset Integration", hidden = true)
  public void deleteExportEuDatasetIntegration(
          @RequestParam("datasetSchemaId") String datasetSchemaId) {
    try {
      LOG.info("Deleting export eu dataset integration for datasetSchemaId {}", datasetSchemaId);
      integrationService.deleteExportEuDataset(datasetSchemaId);
      LOG.info("Deleting export eu dataset integration for datasetSchemaId {}", datasetSchemaId);
    } catch (EEAException e) {
      LOG.error(
              "Error deleting and export eu dataset integration with the datasetSchemaId {}, with message: {}",
              datasetSchemaId, e.getMessage());
    } catch (Exception e){
      LOG.error("Unexpected error! Could not delete an export EU Dataset Integration for datasetSchemaId {}. Message: {}", datasetSchemaId, e.getMessage());
      throw e;
    }
  }


  /**
   * Execute external integration.
   *
   * @param integrationId the integration id
   * @param datasetId the dataset id
   * @param replace the replace
   */
  @Override
  @HystrixCommand
  @LockMethod(removeWhenFinish = false)
  @PostMapping("/{integrationId}/runIntegration/dataset/{datasetId}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','DATASET_LEAD_REPORTER','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD_SUPPORT','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_LEAD_REPORTER','REFERENCEDATASET_STEWARD','DATAFLOW_REPORTER_WRITE','DATASET_REPORTER_WRITE')")
  @ApiOperation(
          value = "Run an external integration process providing the integration id and the dataset where applies",
          response = ExecutionResultVO.class, hidden = true)
  public void executeExternalIntegration(
          @ApiParam(value = "Integration Id",
                  example = "1") @PathVariable(value = "integrationId") Long integrationId,
          @ApiParam(value = "Dataset Id", example = "150") @LockCriteria(
                  name = "datasetId") @PathVariable("datasetId") Long datasetId,
          @ApiParam(value = "Should the external integration replace the existing data?",
                  example = "true", defaultValue = "false") @RequestParam(value = "replace",
                  defaultValue = "false") Boolean replace) {

    try {
      UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
      userNotificationContentVO.setDatasetId(datasetId);
      DataSetMetabaseVO datasetMetabaseVO =
              dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
      userNotificationContentVO.setDatasetName(datasetMetabaseVO.getDataSetName());
      notificationControllerZuul.createUserNotificationPrivate("DATASET_IMPORT_INIT",
              userNotificationContentVO);

      integrationService.addLocks(datasetId);
      LOG.info("Executing external integration with id {} for datasetId {}", integrationId, datasetId);
      integrationService.executeExternalIntegration(datasetId, integrationId,
              IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM, replace);
      LOG.info("Successfully executed external integration with id {} for datasetId {}", integrationId, datasetId);
    } catch (EEAException e) {
      LOG.error(
              "Error executing an external integration with id {} on the datasetId {}, with message: {}",
              integrationId, datasetId, e.getMessage());
      Map<String, Object> lockCriteria = new HashMap<>();
      lockCriteria.put(LiteralConstants.SIGNATURE,
              LockSignature.EXECUTE_EXTERNAL_INTEGRATION.getValue());
      lockCriteria.put(LiteralConstants.DATASETID, datasetId);
      lockService.removeLockByCriteria(lockCriteria);
      integrationService.releaseLocks(datasetId);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.EXECUTING_INTEGRATIONS);
    } catch (Exception e){
      LOG.error("Unexpected error! Could not execute external integration with id {} for datasetId {}. Message: {}", integrationId, datasetId, e.getMessage());
      throw e;
    }

  }


  /**
   * Creates the integrations.
   *
   * @param integrations the integrations
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @PostMapping("/private/createIntegrations")
  @ApiOperation(value = "Create Integrations", hidden = true)
  @ApiResponse(code = 500, message = "Internal Server Error")
  public void createIntegrations(@ApiParam(type = "Object",
          value = "List<IntegrationVO> Object") @RequestBody List<IntegrationVO> integrations) {
    try {
      integrationService.createIntegrations(integrations);
    } catch (EEAException e) {
      LOG.error("Error creating integrations. Message: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.CREATING_INTEGRATION);
    } catch (Exception e){
      LOG.error("Unexpected error! Could not create integrations. Message: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Find integration by id.
   *
   * @param integrationId the integration id
   * @return the integration VO
   */
  @Override
  @GetMapping("/private/findIntegration/{integrationId}")
  @ApiOperation(value = "Find Integration using his id", hidden = true)
  public IntegrationVO findIntegrationById(@ApiParam(value = "Integration Id",
          example = "1") @RequestParam("integrationId") Long integrationId) {
    return integrationService.getIntegration(integrationId);
  }
}
