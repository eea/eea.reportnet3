package org.eea.dataflow.controller;

import java.util.Arrays;
import java.util.List;
import org.eea.dataflow.integration.executor.IntegrationExecutorFactory;
import org.eea.dataflow.service.IntegrationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.IntegrationController;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.eea.interfaces.vo.dataset.schemas.CopySchemaVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.lock.service.LockService;
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

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Find all integrations by criteria.
   *
   * @param integrationVO the integration VO
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD','LEAD_REPORTER') OR secondLevelAuthorize(#integrationVO.internalParameters['dataflowId'],'DATAFLOW_EDITOR_WRITE','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_READ')")
  @PutMapping(value = "/listIntegrations", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find all Integrations by Integration Criteria",
      produces = MediaType.APPLICATION_JSON_VALUE, response = IntegrationVO.class,
      responseContainer = "List")
  @ApiResponse(code = 500, message = "Internal Server Error")
  public List<IntegrationVO> findAllIntegrationsByCriteria(@ApiParam(type = "Object",
      value = "IntegrationVO Object") @RequestBody IntegrationVO integrationVO) {
    try {
      return integrationService.getAllIntegrationsByCriteria(integrationVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Error finding integrations: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Find expor EU dataset integration by dataset id.
   *
   * @param datasetSchemaId the dataset schema id
   *
   * @return the integration VO
   */
  @Override
  @PreAuthorize("hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD','LEAD_REPORTER')")
  @GetMapping("/findExportEUDatasetIntegration")
  @ApiOperation(value = "Find EU Dataset Export Integration by its Schema Id",
      produces = MediaType.APPLICATION_JSON_VALUE, response = IntegrationVO.class)
  public IntegrationVO findExportEUDatasetIntegration(
      @ApiParam(value = "Schema Id") @RequestParam("datasetSchemaId") String datasetSchemaId) {
    return integrationService.getExportEUDatasetIntegration(datasetSchemaId);
  }

  /**
   * Creates the integration.
   *
   * @param integration the integration
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') OR secondLevelAuthorize(#integration.internalParameters['dataflowId'],'DATAFLOW_STEWARD','DATAFLOW_EDITOR_WRITE', 'DATAFLOW_CUSTODIAN')")
  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Create Integration")
  @ApiResponse(code = 500, message = "Internal Server Error")
  public void createIntegration(@ApiParam(type = "Object",
      value = "IntegrationVO Object") @RequestBody IntegrationVO integration) {
    try {
      integrationService.createIntegration(integration);
    } catch (EEAException e) {
      LOG_ERROR.error("Error creating integration. Message: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
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
  @PreAuthorize("hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') OR secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_EDITOR_WRITE', 'DATAFLOW_CUSTODIAN')")
  @DeleteMapping(value = "/{integrationId}/dataflow/{dataflowId}")
  @ApiOperation(value = "Delete Integration by its Integration Id")
  @ApiResponse(code = 500, message = "Internal Server Error")
  public void deleteIntegration(
      @ApiParam(value = "Integration id",
          example = "0") @PathVariable("integrationId") Long integrationId,
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    try {
      integrationService.deleteIntegration(integrationId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting an integration. Message: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Update integration.
   *
   * @param integration the integration
   *
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') OR secondLevelAuthorize(#integration.internalParameters['dataflowId'],'DATAFLOW_STEWARD','DATAFLOW_EDITOR_WRITE', 'DATAFLOW_CUSTODIAN')")
  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Update Integration")
  @ApiResponse(code = 500, message = "Internal Server Error")
  public void updateIntegration(@ApiParam(type = "Object",
      value = "IntegrationVO Object") @RequestBody IntegrationVO integration) {
    try {
      integrationService.updateIntegration(integration);
    } catch (EEAException e) {
      LOG_ERROR.error("Error updating an integration. Message: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
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
  @PreAuthorize("hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD','LEAD_REPORTER') OR secondLevelAuthorize(#integrationVO.internalParameters['dataflowId'],'DATAFLOW_STEWARD','DATAFLOW_EDITOR_WRITE','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR')")
  @PutMapping(value = "/listExtensionsOperations", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find Integrations and Operations by Integration Criteria",
      produces = MediaType.APPLICATION_JSON_VALUE, response = IntegrationVO.class,
      responseContainer = "List")
  @ApiResponse(code = 500, message = "Internal Server Error")
  public List<IntegrationVO> findExtensionsAndOperations(@ApiParam(type = "Object",
      value = "IntegrationVO Object") @RequestBody IntegrationVO integrationVO) {
    try {
      return integrationService.getOnlyExtensionsAndOperations(
          integrationService.getAllIntegrationsByCriteria(integrationVO));
    } catch (EEAException e) {
      LOG_ERROR.error("Error finding integrations: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Execute integration process.
   *
   * @param integrationToolTypeEnum the integration tool type enum
   * @param integrationOperationTypeEnum the integration operation type enum
   * @param file the file
   * @param datasetId the dataset id
   * @param integration the integration
   *
   * @return the execution result VO
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/private/executeIntegration")
  @ApiOperation(value = "Find Integrations and Operations by Integration Criteria",
      response = ExecutionResultVO.class, hidden = true)
  public ExecutionResultVO executeIntegrationProcess(@ApiParam(type = "Object",
      value = "IntegrationEnum Object") @RequestParam("integrationTool") IntegrationToolTypeEnum integrationToolTypeEnum,
      @ApiParam(type = "Object",
          value = "OperationEnum Object") @RequestParam("operation") IntegrationOperationTypeEnum integrationOperationTypeEnum,
      @ApiParam(type = "file", value = "File") @RequestParam(name = "file",
          required = false) final String file,
      @ApiParam(value = "Dataset id", example = "0") @RequestParam("datasetId") Long datasetId,
      @ApiParam(type = "Object",
          value = "IntegrationVO Object") @RequestBody IntegrationVO integration) {
    return integrationExecutorFactory.getExecutor(integrationToolTypeEnum)
        .execute(integrationOperationTypeEnum, file, datasetId, integration);
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
  @PreAuthorize("hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD')  OR (checkApiKey(#dataflowId,0L))")
  @LockMethod
  @PostMapping(value = "/executeEUDatasetExport")
  @ApiOperation(value = "Execute EUDataset Export", response = ExecutionResultVO.class,
      responseContainer = "List")
  @ApiResponse(code = 500, message = "Internal Server Error")
  public List<ExecutionResultVO> executeEUDatasetExport(
      @LockCriteria(name = "dataflowId") @RequestParam("dataflowId") Long dataflowId) {
    List<ExecutionResultVO> results = null;
    try {
      integrationService.addPopulateEUDatasetLock(dataflowId);
      results = integrationService.executeEUDatasetExport(dataflowId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error executing the export from EUDataset with message: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    } finally {
      integrationService.releasePopulateEUDatasetLock(dataflowId);
    }
    return results;
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
      LOG_ERROR.error("Error copying integrations: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
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
          example = "0") @RequestParam("datasetSchemaId") String datasetSchemaId) {
    try {
      integrationService.createDefaultIntegration(dataflowId, datasetSchemaId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error creating default integration. Message: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
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
  public IntegrationVO findExportIntegration(
      @ApiParam(value = "Dataschema Id",
          example = "0") @RequestParam("datasetSchemaId") String datasetSchemaId,
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
  public void deleteSchemaIntegrations(@RequestParam("datasetSchemaId") String datasetSchemaId) {
    integrationService.deleteSchemaIntegrations(datasetSchemaId);
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
  @ApiOperation(
      value = "Run an external integration process providing the integration id and the dataset where applies",
      response = ExecutionResultVO.class)
  public void executeExternalIntegration(@PathVariable(value = "integrationId") Long integrationId,
      @LockCriteria(name = "datasetId") @PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "replace", defaultValue = "false") Boolean replace) {

    try {
      integrationService.addLocks(datasetId);
      integrationService.executeExternalIntegration(datasetId, integrationId,
          IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM, replace);
    } catch (EEAException e) {
      LOG_ERROR.error(
          "Error executing an external integration with id {} on the datasetId {}, with message: {}",
          integrationId, datasetId, e.getMessage());
      lockService.removeLockByCriteria(
          Arrays.asList(LockSignature.EXECUTE_EXTERNAL_INTEGRATION.getValue(), datasetId));
      integrationService.releaseLocks(datasetId);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

  }


  /**
   * Creates the integrations.
   *
   * @param integrations the integrations
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD')")
  @PostMapping("/private/createIntegrations")
  @ApiOperation(value = "Create Integrations")
  @ApiResponse(code = 500, message = "Internal Server Error")
  public void createIntegrations(@ApiParam(type = "Object",
      value = "List<IntegrationVO> Object") @RequestBody List<IntegrationVO> integrations) {
    try {
      integrationService.createIntegrations(integrations);
    } catch (EEAException e) {
      LOG_ERROR.error("Error creating integrations. Message: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }
}
