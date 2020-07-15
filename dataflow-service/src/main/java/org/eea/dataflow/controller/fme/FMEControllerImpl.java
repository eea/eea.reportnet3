package org.eea.dataflow.controller.fme;

import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
import org.eea.interfaces.controller.dataflow.integration.fme.FMEController;
import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.eea.interfaces.vo.integration.fme.FMEOperationInfoVO;
import org.eea.thread.ThreadPropertiesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class FMEControllerImpl.
 */
@RestController
@RequestMapping("/fme")
public class FMEControllerImpl implements FMEController {

  /** The FME communication service. */
  @Autowired
  private FMECommunicationService fmeCommunicationService;

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
  public FMECollectionVO findRepositories(@RequestParam("datasetId") Long datasetId) {
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
  public FMECollectionVO findItems(@RequestParam("datasetId") Long datasetId,
      @RequestParam("repository") String repository) {
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
  public void operationFinished(@RequestBody FMEOperationInfoVO fmeOperationInfoVO) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    fmeCommunicationService.operationFinished(fmeOperationInfoVO);
  }

}
