package org.eea.dataflow.integration.executor.fme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.dataflow.integration.executor.fme.domain.FMEAsyncJob;
import org.eea.dataflow.integration.executor.fme.domain.PublishedParameter;
import org.eea.dataflow.integration.executor.fme.repository.FMEFeignRepository;
import org.eea.dataflow.integration.executor.service.AbstractIntegrationExecutorService;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class FMEIntegrationExecutorService extends AbstractIntegrationExecutorService {

  @Autowired
  private FMEFeignRepository fmeFeignRepository;

  @Autowired
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  @Autowired
  private DataSetControllerZuul dataSetControllerZuul;

  @Autowired
  private UserManagementController userManagementController;


  private static final Logger LOG = LoggerFactory.getLogger(FMEIntegrationExecutorService.class);

  private static final Logger LOG_ERROR =
      LoggerFactory.getLogger(FMEIntegrationExecutorService.class);



  @Override
  public IntegrationToolTypeEnum getExecutorType() {
    return IntegrationToolTypeEnum.FME;
  }

  @Override
  @Async
  public ExecutionResultVO execute(IntegrationOperationTypeEnum integrationOperationTypeEnum,
      Object... executionParams) {

    // 1- Long datasetId
    Long datasetId = null;
    // 2- MultipartFile
    String file = null;
    // 3- IntegrationVO
    IntegrationVO integration = new IntegrationVO();

    try {
      for (Object param : executionParams) {
        if (param.getClass().getTypeName() != null) {
          if (param instanceof Long) {
            datasetId = ((Long) param);
          } else if (param instanceof String) {
            file = ((String) param);
          } else if (param instanceof IntegrationVO) {
            integration = ((IntegrationVO) param);
          }
        }
      }
    } catch (IllegalArgumentException | SecurityException e) {
      LOG_ERROR.error("Error getting params in FME Integration Executor: ", e.getMessage());
    }

    DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    Long dataflowId = 0L;
    if (null != integration.getInternalParameters().get("repository")) {
      dataflowId = dataset.getDataflowId();
    } else {
      dataflowId = dataSetControllerZuul.getDataFlowIdById(datasetId);
    }

    Long dataproviderId = dataset.getDataProviderId();

    String apiKey = userManagementController.getApiKey(dataflowId, dataproviderId);

    FMEAsyncJob fmeAsyncJob = new FMEAsyncJob();
    String workspace = integration.getInternalParameters().get("processName");
    String repository = null;
    if (null != integration.getInternalParameters().get("repository")) {
      repository = integration.getInternalParameters().get("repository");
    } else {
      repository = "ReportNetTesting";
    }
    String folder = null;
    if (null != integration.getInternalParameters().get("folder")) {
      folder = integration.getInternalParameters().get("folder");
    } else {
      folder = "MMR";
    }


    List<PublishedParameter> parameters = new ArrayList<>();
    // dataflowId
    parameters.add(saveParameter("dataflowId", dataflowId));
    // providerId
    parameters.add(saveParameter("providerId", dataproviderId));
    // datasetDataId
    parameters.add(saveParameter("datasetDataId", datasetId));
    // inputfile
    parameters.add(saveParameter("inputfile", file));
    // folder
    parameters.add(saveParameter("folder", folder));
    // apikey
    parameters.add(saveParameter("apiKey", apiKey));

    fmeAsyncJob.setPublishedParameters(parameters);


    switch (integrationOperationTypeEnum) {
      case EXPORT:
        return executeSubmit(repository, workspace, fmeAsyncJob);
      case IMPORT:
        return executeSubmit(repository, workspace, fmeAsyncJob);
      default:
        return null;

    }
  }

  private PublishedParameter saveParameter(String name, Object value) {
    PublishedParameter parameter = new PublishedParameter();
    parameter.setName(name);
    parameter.setValue(value);
    return parameter;
  }



  private ExecutionResultVO executeSubmit(String repository, String workspace,
      FMEAsyncJob fmeAsyncJob) {
    Map<String, Object> executionResultParams = new HashMap<>();
    ExecutionResultVO executionResultVO = new ExecutionResultVO();

    Integer a = null;
    try {
      a = fmeFeignRepository.submitAsyncJob(repository, workspace, fmeAsyncJob);
    } catch (Exception e) {
      e.getMessage();
      e.printStackTrace();
    }
    executionResultParams.put("id", a);
    executionResultVO.setExecutionResultParams(executionResultParams);

    return executionResultVO;
  }



}
