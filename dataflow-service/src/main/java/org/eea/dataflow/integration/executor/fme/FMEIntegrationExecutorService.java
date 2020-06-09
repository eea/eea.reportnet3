package org.eea.dataflow.integration.executor.fme;

import java.util.HashMap;
import java.util.Map;
import org.eea.dataflow.integration.executor.fme.domain.FMEAsyncJob;
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


    FMEAsyncJob fmeAsyncJob = new FMEAsyncJob();
    String workspace = " ";
    String repository = " ";
    ExecutionResultVO executionResultVO = new ExecutionResultVO();
    Map<String, Object> executionResultParams = new HashMap<>();


    switch (integrationOperationTypeEnum) {
      case EXPORT:
        executionResultParams.put("id",
            fmeFeignRepository.submitAsyncJob(repository, workspace, fmeAsyncJob));
        executionResultVO.setExecutionResultParams(executionResultParams);

        return executionResultVO;

      case IMPORT:
        executionResultParams.put("id",
            fmeFeignRepository.submitAsyncJob(repository, workspace, fmeAsyncJob));
        executionResultVO.setExecutionResultParams(executionResultParams);

        return executionResultVO;

      default:
        return null;



    }

  }

  private Integer executemetoht() {
    return null;

  }

}
