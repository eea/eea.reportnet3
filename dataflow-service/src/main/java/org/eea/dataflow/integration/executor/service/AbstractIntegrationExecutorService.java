package org.eea.dataflow.integration.executor.service;

import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;

public abstract class AbstractIntegrationExecutorService implements IntegrationExecutorService {

  public abstract IntegrationToolTypeEnum getExecutorType();

  @Override
  public abstract ExecutionResultVO execute(
      IntegrationOperationTypeEnum integrationOperationTypeEnum, Object... executionParams);


}
