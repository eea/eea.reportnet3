package org.eea.interfaces.controller.dataflow;

import org.eea.interfaces.vo.dataflow.FMEStatusVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

public interface FMEIntegrationController {

  @FeignClient(value = "fme", path = "/integration")
  interface FMEIntegrationControllerZuul extends DataFlowController {
  }

  @GetMapping(value = "/integration", produces = MediaType.APPLICATION_JSON_VALUE)
  FMEStatusVO checkHealt();

  @GetMapping(value = "/integration", produces = MediaType.APPLICATION_JSON_VALUE)
  FMEStatusVO getRepositoryItems();

}
