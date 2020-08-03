package org.eea.interfaces.controller.dataflow;

import java.util.List;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.eea.interfaces.vo.dataset.schemas.CopySchemaVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface IntegrationController.
 */
public interface IntegrationController {

  /**
   * The Interface IntegrationControllerZuul.
   */
  @FeignClient(value = "dataflow", contextId = "integration", path = "/integration")
  interface IntegrationControllerZuul extends IntegrationController {

  }

  /**
   * Find all integrations by criteria.
   *
   * @param integration the integration
   * @return the list
   */
  @PutMapping(value = "/listIntegrations", produces = MediaType.APPLICATION_JSON_VALUE)
  List<IntegrationVO> findAllIntegrationsByCriteria(@RequestBody IntegrationVO integration);

  /**
   * Creates the integration.
   *
   * @param integration the integration
   */
  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  void createIntegration(@RequestBody IntegrationVO integration);

  /**
   * Delete integration.
   *
   * @param integrationId the integration id
   */
  @DeleteMapping(value = "/{integrationId}/dataflow/{dataflowId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  void deleteIntegration(@PathVariable("integrationId") Long integrationId,
      @PathVariable("dataflowId") Long dataflowId);

  /**
   * Update integration.
   *
   * @param integration the integration
   * @return the response entity
   */
  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  void updateIntegration(@RequestBody IntegrationVO integration);

  /**
   * Find extensions and operations.
   *
   * @param integrationVO the integration VO
   * @return the list
   */
  @PutMapping(value = "/listExtensionsOperations", produces = MediaType.APPLICATION_JSON_VALUE)
  List<IntegrationVO> findExtensionsAndOperations(IntegrationVO integrationVO);

  /**
   * Execute integration process.
   *
   * @param integrationOperationTypeEnum the integration operation type enum
   * @param file the file
   * @param datasetId
   * @param integration the integration
   * @return the execution result VO
   */
  @PostMapping(value = "/private/executeIntegration")
  ExecutionResultVO executeIntegrationProcess(
      @RequestParam("integrationTool") IntegrationToolTypeEnum integrationToolTypeEnum,
      @RequestParam("operation") IntegrationOperationTypeEnum integrationOperationTypeEnum,
      @RequestParam("file") final String file, @RequestParam("datasetId") Long datasetId,
      @RequestBody IntegrationVO integration);

  /**
   * Execute EU dataset export.
   *
   * @param dataflowId the dataflow id
   * @return the execution result VO
   */
  @PostMapping(value = "/executeEUDatasetExport")
  List<ExecutionResultVO> executeEUDatasetExport(@RequestParam("dataflowId") Long dataflowId);

  /**
   * Copy integrations.
   *
   * @param copyVO the copy VO
   */
  @PostMapping(value = "/private/copyIntegrations", produces = MediaType.APPLICATION_JSON_VALUE)
  void copyIntegrations(@RequestBody CopySchemaVO copyVO);

  /**
   * Creates the integration.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the dataset id
   */
  @PostMapping("/private/createDeafult")
  void createDefaultIntegration(@RequestParam("dataflowId") Long dataflowId,
      @RequestParam("datasetId") Long datasetId);
}
