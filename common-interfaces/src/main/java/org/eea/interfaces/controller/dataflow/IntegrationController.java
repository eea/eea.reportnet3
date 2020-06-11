package org.eea.interfaces.controller.dataflow;

import java.util.List;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


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
  @DeleteMapping(value = "/{integrationId}", produces = MediaType.APPLICATION_JSON_VALUE)
  void deleteIntegration(@PathVariable("integrationId") Long integrationId);

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
  List<IntegrationVO> findExtensionsAndOperations(IntegrationVO integrationVO);



}
