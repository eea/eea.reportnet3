package org.eea.dataflow.integration.executor.fme;

import org.eea.dataflow.integration.executor.fme.repository.FMEFeignRepository;
import org.eea.dataflow.integration.executor.service.AbstractIntegrationExecutorService;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FMEIntegrationExecutorService extends AbstractIntegrationExecutorService {

  @Autowired
  private FMEFeignRepository fmeFeignRepository;


  @Override
  public IntegrationToolTypeEnum getExecutorType() {
    return IntegrationToolTypeEnum.FME;
  }

  @Override
  public ExecutionResultVO execute(IntegrationOperationTypeEnum integrationOperationTypeEnum,
      Object... executionParams) {
    // TODO Auto-generated method stub
    return null;

  }

}
