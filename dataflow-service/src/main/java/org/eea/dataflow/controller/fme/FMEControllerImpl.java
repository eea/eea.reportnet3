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
   * @return the collection VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR hasRole('LEAD_REPORTER') OR secondLevelAuthorize(#integrationVO.internalParameters['dataflowId'],'DATAFLOW_EDITOR_WRITE','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_READ')")
  @GetMapping(value = "/findRepositories", produces = MediaType.APPLICATION_JSON_VALUE)
  public FMECollectionVO findRepositories() {
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
  public FMECollectionVO findItems(@RequestParam("repository") String repository) {
    return fmeCommunicationService.findItems(repository);
  }

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
