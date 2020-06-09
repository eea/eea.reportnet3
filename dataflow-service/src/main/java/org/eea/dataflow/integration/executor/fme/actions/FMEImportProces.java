package org.eea.dataflow.integration.executor.fme.actions;

import org.eea.dataflow.integration.executor.service.AbstractIntegrationExecutorService;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.springframework.stereotype.Component;

@Component
public class FMEImportProces extends AbstractIntegrationExecutorService {

  @Override
  public IntegrationToolTypeEnum getExecutorType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ExecutionResultVO execute(IntegrationOperationTypeEnum integrationOperationTypeEnum,
      Object... executionParams) {
    // TODO Auto-generated method stub
    return null;
  }

}
