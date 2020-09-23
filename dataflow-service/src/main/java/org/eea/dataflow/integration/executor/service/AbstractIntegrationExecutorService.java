package org.eea.dataflow.integration.executor.service;

import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;

/**
 * The Class AbstractIntegrationExecutorService.
 */
public abstract class AbstractIntegrationExecutorService implements IntegrationExecutorService {

  /**
   * Gets the executor type.
   *
   * @return the executor type
   */
  public abstract IntegrationToolTypeEnum getExecutorType();

  /**
   * Execute.
   *
   * @param integrationOperationTypeEnum the integration operation type enum
   * @param executionParams the execution params
   * @return the execution result VO
   */
  @Override
  public abstract ExecutionResultVO execute(
      IntegrationOperationTypeEnum integrationOperationTypeEnum, Object... executionParams);


}
