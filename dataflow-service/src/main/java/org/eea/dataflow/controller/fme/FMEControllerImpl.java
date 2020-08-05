package org.eea.dataflow.controller.fme;

import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
import org.eea.interfaces.controller.dataflow.integration.fme.FMEController;
import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.eea.interfaces.vo.integration.fme.FMEOperationInfoVO;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
 * The Class FMEControllerImpl.
 */
@RestController
@RequestMapping("/fme")
@Api(tags = "FME : FME Manager")
public class FMEControllerImpl implements FMEController {

  /** The FME communication service. */
  @Autowired
  private FMECommunicationService fmeCommunicationService;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Find repositories.
   *
   * @param datasetId the dataset id
   * @return the collection VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE') OR (hasRole('DATA_CUSTODIAN')) OR (hasRole('DATA_STEWARD'))")
  @GetMapping(value = "/findRepositories", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find FME repositories", produces = MediaType.APPLICATION_JSON_VALUE,
      response = FMECollectionVO.class)
  public FMECollectionVO findRepositories(
      @ApiParam(value = "Dataset id", example = "0") @RequestParam("datasetId") Long datasetId) {
    return fmeCommunicationService.findRepository();
  }

  /**
   * Find items.
   *
   * @param datasetId the dataset id
   * @param repository the repository
   * @return the collection VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE') OR (hasRole('DATA_CUSTODIAN')) OR (hasRole('DATA_STEWARD'))")
  @GetMapping(value = "/findItems", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find FME items", produces = MediaType.APPLICATION_JSON_VALUE,
      response = FMECollectionVO.class)
  public FMECollectionVO findItems(
      @ApiParam(value = "Dataset id", example = "0") @RequestParam("datasetId") Long datasetId,
      @ApiParam(value = "Repository name") @RequestParam("repository") String repository) {
    return fmeCommunicationService.findItems(repository);
  }

  /**
   * Operation finished.
   *
   * @param fmeOperationInfoVO the fme operation info VO
   */
  @Override
  @PostMapping("/operationFinished")
  @PreAuthorize("checkApiKey(#fmeOperationInfoVO.dataflowId, #fmeOperationInfoVO.providerId)")
  @ApiOperation(value = "FME Operation finished")
  @ApiResponse(code = 400, message = "Internal Server Error")
  public void operationFinished(@ApiParam(value = "FME Operation info",
      type = "Object") @RequestBody FMEOperationInfoVO fmeOperationInfoVO) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    try {
      fmeCommunicationService.operationFinished(fmeOperationInfoVO);
    } catch (Exception e) {
      LOG_ERROR.error("Error in the operationFinished controller with the message: {}",
          e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

}
