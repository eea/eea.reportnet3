package org.eea.dataflow.integration.executor;

import org.eea.dataflow.integration.executor.service.IntegrationExecutorService;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;

/**
 * A factory for creating IntegrationExecutor objects.
 */
public interface IntegrationExecutorFactory {

  /**
   * Gets the executor.
   *
   * @param integrationToolTypeEnum the integration tool type enum
   * @return the executor
   */
  IntegrationExecutorService getExecutor(IntegrationToolTypeEnum integrationToolTypeEnum);

}
