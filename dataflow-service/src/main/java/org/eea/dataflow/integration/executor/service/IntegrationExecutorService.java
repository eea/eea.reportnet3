package org.eea.dataflow.integration.executor.service;

import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;

/**
 * The Interface IntegrationExecutorService.
 */
public interface IntegrationExecutorService {


  /**
   * Execute.
   *
   * @param integrationOperationTypeEnum the integration operation type enum
   * @param executionParams the execution params
   * @return the execution result VO
   */
  ExecutionResultVO execute(IntegrationOperationTypeEnum integrationOperationTypeEnum,
      Object... executionParams);

}
