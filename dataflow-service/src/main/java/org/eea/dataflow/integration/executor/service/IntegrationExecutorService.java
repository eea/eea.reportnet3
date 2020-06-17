package org.eea.dataflow.integration.executor.service;

import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;

public interface IntegrationExecutorService {


  ExecutionResultVO execute(IntegrationOperationTypeEnum integrationOperationTypeEnum,
      Object... executionParams);

}
