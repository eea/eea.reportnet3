package org.eea.dataflow.integration.executor;

import org.eea.dataflow.integration.executor.service.IntegrationExecutorService;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;

public interface IntegrationExecutorFactory {

  IntegrationExecutorService getExecutor(IntegrationToolTypeEnum integrationToolTypeEnum);

}
