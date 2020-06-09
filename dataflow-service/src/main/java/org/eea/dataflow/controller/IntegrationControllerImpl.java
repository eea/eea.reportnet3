package org.eea.dataflow.controller;

import java.util.ArrayList;
import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.IntegrationController;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


/**
 * The Class IntegrationControllerImpl.
 */
@RestController
@RequestMapping("/integration")
public class IntegrationControllerImpl implements IntegrationController {

  /*
   * @Autowired private IntegrationService integrationService;
   */

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * Find all integrations by criteria.
   *
   * @param integration the integration
   * @return the list
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/listIntegrations", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<IntegrationVO> findAllIntegrationsByCriteria(@RequestBody IntegrationVO integration) {
    IntegrationVO integration1 = new IntegrationVO();
    integration1.setId(1L);
    integration1.setName("Integration dummy 1");
    integration1.setDescription("This is a description");
    integration1.setOperation(IntegrationOperationTypeEnum.EXPORT);
    integration1.setTool(IntegrationToolTypeEnum.FME);
    integration1.getInternalParameters().put("fileExtension", "csv");
    integration1.getInternalParameters().put("datasetSchemaId", "5ce524fad31fc52540abae73");
    integration1.getExternalParameters().put("name1", "value1");

    IntegrationVO integration2 = new IntegrationVO();
    integration2.setId(2L);
    integration2.setName("Integration dummy 2");
    integration2.setDescription("This is a description");
    integration2.setOperation(IntegrationOperationTypeEnum.IMPORT);
    integration2.setTool(IntegrationToolTypeEnum.FME);
    integration2.getInternalParameters().put("datasetSchemaId", "5ce524fad31fc52540abae73");
    integration2.getInternalParameters().put("parameter2Name", "paramValue2");
    integration2.getExternalParameters().put("name1", "value1");
    integration2.getExternalParameters().put("name2", "value2");

    List<IntegrationVO> integrations = new ArrayList<>();
    integrations.add(integration1);
    integrations.add(integration2);
    return integrations;
  }

  /**
   * Find integration by id.
   *
   * @param idIntegration the id integration
   * @return the integration VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{idIntegration}", produces = MediaType.APPLICATION_JSON_VALUE)
  public IntegrationVO findIntegrationById(@PathVariable("idIntegration") Long idIntegration) {
    IntegrationVO integration1 = new IntegrationVO();

    integration1.setId(idIntegration);
    integration1.setName("Integration dummy 3");
    integration1.setDescription("This is a description");
    integration1.setOperation(IntegrationOperationTypeEnum.EXPORT);
    integration1.setTool(IntegrationToolTypeEnum.FME);
    integration1.getInternalParameters().put("datasetSchemaId", "5ce524fad31fc52540abae73");
    integration1.getInternalParameters().put("fileExtension", "json");
    integration1.getExternalParameters().put("name1", "value1");
    integration1.getExternalParameters().put("name2", "value2");
    integration1.getExternalParameters().put("name3", "value3");

    return integration1;
  }

  /**
   * Creates the integration.
   *
   * @param integration the integration
   * @throws EEAException
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  public void createIntegration(@RequestBody IntegrationVO integration) {

    /*
     * try { integrationService.createIntegration(integration); } catch (EEAException e) {
     * LOG_ERROR.error("error"); throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
     * e.getMessage(), e); }
     */
  }


  /**
   * Delete integration.
   *
   * @param integrationId the integration id
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/{idIntegration}/delete")
  public void deleteIntegration(@PathVariable("idIntegration") Long integrationId) {

  }

  /**
   * Update integration.
   *
   * @param integration the integration
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity updateIntegration(@RequestBody IntegrationVO integration) {
    return null;
  }


}
