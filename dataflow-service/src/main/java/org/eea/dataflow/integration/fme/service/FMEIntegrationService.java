package org.eea.dataflow.integration.fme.service;

import org.eea.dataflow.integration.fme.domain.FMEStatus;

public interface FMEIntegrationService {
  FMEStatus checkHealt();

  FMEStatus getRepositoryItems();

}
