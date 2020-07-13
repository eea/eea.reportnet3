package org.eea.dataflow.controller;

import java.util.List;
import org.eea.dataflow.integration.executor.IntegrationExecutorFactory;
import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
import org.eea.dataflow.service.IntegrationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.IntegrationController;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.eea.interfaces.vo.dataset.schemas.CopySchemaVO;
import org.eea.interfaces.vo.integration.CollectionVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
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


/**
 * The Class IntegrationControllerImpl.
 */
@RestController
@RequestMapping("/integration")
public class IntegrationControllerImpl implements IntegrationController {


  /** The integration service. */
  @Autowired
  private IntegrationService integrationService;

  /** The FME integration executor factory. */
  @Autowired
  private IntegrationExecutorFactory integrationExecutorFactory;

  /** The FME communication service. */
  @Autowired
  private FMECommunicationService fmeCommunicationService;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * Find all integrations by criteria.
   *
   * @param integrationVO the integration VO
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR hasRole('LEAD_REPORTER') OR secondLevelAuthorize(#integrationVO.internalParameters['dataflowId'],'DATAFLOW_EDITOR_WRITE','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_READ')")
  @PutMapping(value = "/listIntegrations", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<IntegrationVO> findAllIntegrationsByCriteria(
      @RequestBody IntegrationVO integrationVO) {

    try {
      List<IntegrationVO> integrations =
          integrationService.getAllIntegrationsByCriteria(integrationVO);
      return integrations;
    } catch (EEAException e) {
      LOG_ERROR.error("Error finding integrations: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }



  /**
   * Creates the integration.
   *
   * @param integration the integration
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR secondLevelAuthorize(#integration.internalParameters['dataflowId'],'DATAFLOW_EDITOR_WRITE', 'DATAFLOW_CUSTODIAN')")
  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  public void createIntegration(@RequestBody IntegrationVO integration) {

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
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR secondLevelAuthorize(#dataflowId,'DATAFLOW_EDITOR_WRITE', 'DATAFLOW_CUSTODIAN')")
  @DeleteMapping(value = "/{integrationId}/dataflow/{dataflowId}")
  public void deleteIntegration(@PathVariable("integrationId") Long integrationId,
      @PathVariable("dataflowId") Long dataflowId) {

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
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR secondLevelAuthorize(#integration.internalParameters['dataflowId'],'DATAFLOW_EDITOR_WRITE', 'DATAFLOW_CUSTODIAN')")
  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  public void updateIntegration(@RequestBody IntegrationVO integration) {

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
   * @return the list
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR hasRole('LEAD_REPORTER') OR secondLevelAuthorize(#integrationVO.internalParameters['dataflowId'],'DATAFLOW_EDITOR_WRITE','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_READ')")
  @PutMapping(value = "/listExtensionsOperations", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<IntegrationVO> findExtensionsAndOperations(@RequestBody IntegrationVO integrationVO) {
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
   * @param integrationOperationTypeEnum the integration operation type enum
   * @param file the file
   * @param integration the integration
   * @return the execution result VO
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/private/executeIntegration")
  public ExecutionResultVO executeIntegrationProcess(
      @RequestParam("integrationTool") IntegrationToolTypeEnum integrationToolTypeEnum,
      @RequestParam("operation") IntegrationOperationTypeEnum integrationOperationTypeEnum,
      @RequestParam(name = "file", required = false) final String file,
      @RequestParam("datasetId") Long datasetId, @RequestBody IntegrationVO integration) {
    return integrationExecutorFactory.getExecutor(integrationToolTypeEnum)
        .execute(integrationOperationTypeEnum, file, datasetId, integration);
  }


  /**
   * Copy integrations.
   *
   * @param copyVO the copy VO
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/private/copyIntegrations", produces = MediaType.APPLICATION_JSON_VALUE)
  public void copyIntegrations(@RequestBody CopySchemaVO copyVO) {
    try {
      integrationService.copyIntegrations(copyVO.getDataflowIdDestination(),
          copyVO.getOriginDatasetSchemaIds(), copyVO.getDictionaryOriginTargetObjectId());
    } catch (EEAException e) {
      LOG_ERROR.error("Error copying integrations: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }


  /**
   * Find repositories.
   *
   * @return the collection VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR hasRole('LEAD_REPORTER') OR secondLevelAuthorize(#integrationVO.internalParameters['dataflowId'],'DATAFLOW_EDITOR_WRITE','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_READ')")
  @GetMapping(value = "/findRepositories", produces = MediaType.APPLICATION_JSON_VALUE)
  public CollectionVO findRepositories() {
    return fmeCommunicationService.findRepository();
  }

  /**
   * Find items.
   *
   * @param repository the repository
   * @return the collection VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR hasRole('LEAD_REPORTER') OR secondLevelAuthorize(#integrationVO.internalParameters['dataflowId'],'DATAFLOW_EDITOR_WRITE','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_READ')")
  @GetMapping(value = "/findItems", produces = MediaType.APPLICATION_JSON_VALUE)
  public CollectionVO findItems(@RequestParam("repository") String repository) {
    return fmeCommunicationService.findItems(repository);
  }

}
