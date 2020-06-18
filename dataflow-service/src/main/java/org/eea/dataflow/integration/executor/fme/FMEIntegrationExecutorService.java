package org.eea.dataflow.integration.executor.fme;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.eea.dataflow.integration.executor.fme.domain.FMEAsyncJob;
import org.eea.dataflow.integration.executor.fme.domain.PublishedParameter;
import org.eea.dataflow.integration.executor.fme.service.FMEFeignService;
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

/**
 * The Class FMEIntegrationExecutorService.
 */
@Component
public class FMEIntegrationExecutorService extends AbstractIntegrationExecutorService {

  /** The fme feign service. */
  @Autowired
  private FMEFeignService fmeFeignService;

  /** The data set metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  /** The data set controller zuul. */
  @Autowired
  private DataSetControllerZuul dataSetControllerZuul;

  /** The user management controller. */
  @Autowired
  private UserManagementController userManagementController;


  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(FMEIntegrationExecutorService.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR =
      LoggerFactory.getLogger(FMEIntegrationExecutorService.class);



  /**
   * Gets the executor type.
   *
   * @return the executor type
   */
  @Override
  public IntegrationToolTypeEnum getExecutorType() {
    return IntegrationToolTypeEnum.FME;
  }

  /**
   * Execute.
   *
   * @param integrationOperationTypeEnum the integration operation type enum
   * @param executionParams the execution params
   * @return the execution result VO
   */
  @Override
  @Async
  public ExecutionResultVO execute(IntegrationOperationTypeEnum integrationOperationTypeEnum,
      Object... executionParams) {

    // 1- Long datasetId
    Long datasetId = null;
    // 2- MultipartFile
    String fileName = null;
    // 3- IntegrationVO
    IntegrationVO integration = new IntegrationVO();

    try {
      for (Object param : executionParams) {
        if (null != param && null != param.getClass().getTypeName()) {
          if (param instanceof Long) {
            datasetId = ((Long) param);
          } else if (param instanceof String) {
            fileName = ((String) param);
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

    if (null == apiKey) {
      apiKey = userManagementController.createApiKey(dataflowId, dataproviderId);
    }


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

    switch (integrationOperationTypeEnum) {
      case EXPORT:
        // dataflowId
        parameters.add(saveParameter("dataflowId", dataflowId));
        // providerId
        parameters.add(saveParameter("providerId", dataproviderId));
        // datasetDataId
        parameters.add(saveParameter("datasetDataId", datasetId));
        // folder
        parameters.add(saveParameter("folder", folder));
        // apikey
        parameters.add(saveParameter("apiKey", "ApiKey " + apiKey));

        fmeAsyncJob.setPublishedParameters(parameters);
        return executeSubmit(repository, workspace, fmeAsyncJob);

      case IMPORT:

        // dataflowId
        parameters.add(saveParameter("dataflowId", dataflowId));
        // providerId
        parameters.add(saveParameter("providerId", dataproviderId));
        // datasetDataId
        parameters.add(saveParameter("datasetDataId", datasetId));
        // inputfile
        parameters.add(saveParameter("inputfile", fileName));
        // folder
        parameters.add(saveParameter("folder", folder));
        // apikey
        parameters.add(saveParameter("apiKey", "ApiKey " + apiKey));

        fmeAsyncJob.setPublishedParameters(parameters);

        // try to send file to FME Repository
        InputStream file = IOUtils.toInputStream(integration.getExternalParameters().get("fileIS"));
        fmeFeignService.sendFile(file, datasetId, dataproviderId, fileName);

        return executeSubmit(repository, workspace, fmeAsyncJob);
      default:
        return null;

    }
  }

  /**
   * Save parameter.
   *
   * @param name the name
   * @param value the value
   * @return the published parameter
   */
  private PublishedParameter saveParameter(String name, Object value) {
    PublishedParameter parameter = new PublishedParameter();
    parameter.setName(name);
    parameter.setValue(value);
    return parameter;
  }



  /**
   * Execute submit.
   *
   * @param repository the repository
   * @param workspace the workspace
   * @param fmeAsyncJob the fme async job
   * @return the execution result VO
   */
  private ExecutionResultVO executeSubmit(String repository, String workspace,
      FMEAsyncJob fmeAsyncJob) {
    Map<String, Object> executionResultParams = new HashMap<>();
    ExecutionResultVO executionResultVO = new ExecutionResultVO();

    Integer executionResult = null;
    try {
      executionResult = fmeFeignService.submitAsyncJob(repository, workspace, fmeAsyncJob);
    } catch (Exception e) {
      e.getMessage();
      e.printStackTrace();
    }
    executionResultParams.put("id", executionResult);
    executionResultVO.setExecutionResultParams(executionResultParams);

    return executionResultVO;
  }



}
