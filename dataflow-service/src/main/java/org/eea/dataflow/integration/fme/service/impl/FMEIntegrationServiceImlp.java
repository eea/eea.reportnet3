package org.eea.dataflow.integration.fme.service.impl;

import org.eea.dataflow.integration.fme.domain.FMEStatus;
import org.eea.dataflow.integration.fme.repository.FMEFeignRepository;
import org.eea.dataflow.integration.fme.service.FMEIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("fmeIntegrationService")
public class FMEIntegrationServiceImlp implements FMEIntegrationService {

  @Autowired
  FMEFeignRepository fmeFeignRepository;

  @Override
  public FMEStatus checkHealt() {
    return fmeFeignRepository.find();

  }

  @Override
  public FMEStatus getRepositoryItems() {
    // TODO Auto-generated method stub
    return null;
  }

}
