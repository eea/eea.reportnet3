package org.eea.dataflow.controller;

import org.eea.dataflow.integration.fme.mapper.FMEStatusMapper;
import org.eea.dataflow.integration.fme.service.FMEIntegrationService;
import org.eea.interfaces.controller.dataflow.FMEIntegrationController;
import org.eea.interfaces.vo.dataflow.FMEStatusVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/integration")
public class FMEIntegrationControllerImlp implements FMEIntegrationController {

  @Autowired
  FMEIntegrationService fmeIntegrationservice;

  @Autowired
  FMEStatusMapper fmeStatusMapper;

  @Override
  @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  public FMEStatusVO checkHealt() {
    return fmeStatusMapper.entityToClass(fmeIntegrationservice.checkHealt());

  }


  @Override
  @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  public FMEStatusVO getRepositoryItems() {
    return fmeStatusMapper.entityToClass(fmeIntegrationservice.checkHealt());

  }

}
