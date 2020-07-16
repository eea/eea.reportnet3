package org.eea.dataflow.integration.executor.fme;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.dataflow.integration.executor.fme.domain.FMEAsyncJob;
import org.eea.dataflow.integration.executor.fme.domain.PublishedParameter;
import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * The Class FMEIntegrationExecutorService.
 */
@Component
public class FMEIntegrationExecutorService extends AbstractIntegrationExecutorService {

  /**
   * The r 3 base.
   */
  @Value("${integration.fme.callback.urlbase}")
  private String r3base;

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The fme feign service.
   */
  @Autowired
  private FMECommunicationService fmeCommunicationService;

  /**
   * The data set metabase controller zuul.
   */
  @Autowired
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  /**
   * The data set controller zuul.
   */
  @Autowired
  private DataSetControllerZuul dataSetControllerZuul;

  /**
   * The user management controller.
   */
  @Autowired
  private UserManagementController userManagementController;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(FMEIntegrationExecutorService.class);

  /** The Constant REPOSITORY: {@value}. */
  private static final String REPOSITORY = "repository";

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
   *
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

    LOG.info("trying to extract params for Execution");
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
    if (null != integration.getInternalParameters().get(REPOSITORY)) {
      dataflowId = dataset.getDataflowId();
    } else {
      dataflowId = dataSetControllerZuul.getDataFlowIdById(datasetId);
    }

    Long dataproviderId = dataset.getDataProviderId();

    String apiKey = userManagementController.getApiKey(dataflowId, dataproviderId);

    if (null == apiKey) {
      LOG.info("ApiKey not exits");
      apiKey = userManagementController.createApiKey(dataflowId, dataproviderId);
      LOG.info("ApiKey created for Provider ID: {} and Dataflow ID: {} ", dataproviderId,
          dataflowId);
    }

    FMEAsyncJob fmeAsyncJob = new FMEAsyncJob();
    String workspace = integration.getInternalParameters().get("processName");
    String repository = null;
    if (null != integration.getInternalParameters().get(REPOSITORY)) {
      repository = integration.getInternalParameters().get(REPOSITORY);
    } else {
      repository = "ReportNetTesting";
    }

    return switchIntegrationOperatorEnum(integrationOperationTypeEnum, datasetId, fileName,
        integration, dataflowId, dataproviderId, apiKey, fmeAsyncJob, workspace, repository);
  }

  /**
   * Switch integration operator enum.
   *
   * @param integrationOperationTypeEnum the integration operation type enum
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param integration the integration
   * @param dataflowId the dataflow id
   * @param dataproviderId the dataprovider id
   * @param apiKey the api key
   * @param fmeAsyncJob the fme async job
   * @param workspace the workspace
   * @param repository the repository
   * @return the execution result VO
   */
  private ExecutionResultVO switchIntegrationOperatorEnum(
      IntegrationOperationTypeEnum integrationOperationTypeEnum, Long datasetId, String fileName,
      IntegrationVO integration, Long dataflowId, Long dataproviderId, String apiKey,
      FMEAsyncJob fmeAsyncJob, String workspace, String repository) {
    List<PublishedParameter> parameters = new ArrayList<>();
    String paramDataProvider =
        StringUtils.isEmpty(dataproviderId) ? "design" : String.valueOf(dataproviderId);
    switch (integrationOperationTypeEnum) {
      case EXPORT:
        LOG.info("");
        // dataflowId
        parameters.add(saveParameter("dataflowId", dataflowId));
        // providerId
        parameters.add(saveParameter("providerId", paramDataProvider));
        // datasetDataId
        parameters.add(saveParameter("datasetId", datasetId));
        // folder
        parameters.add(saveParameter("folder", datasetId + "/" + paramDataProvider));
        // apikey
        parameters.add(saveParameter("apiKey", "ApiKey " + apiKey));
        // base URL
        parameters.add(saveParameter("baseUrl", r3base));

        fmeAsyncJob.setPublishedParameters(parameters);
        LOG.info("Executing FME Export");
        return executeSubmit(repository, workspace, fmeAsyncJob);

      case IMPORT:

        // dataflowId
        parameters.add(saveParameter("dataflowId", dataflowId));
        // providerId
        parameters.add(saveParameter("providerId", paramDataProvider));
        // datasetDataId
        parameters.add(saveParameter("datasetId", datasetId));
        // inputfile
        parameters.add(saveParameter("inputfile", fileName));
        // folder
        parameters.add(saveParameter("folder", datasetId + "/" + paramDataProvider));
        // apikey
        parameters.add(saveParameter("apiKey", "ApiKey " + apiKey));
        // base URL
        parameters.add(saveParameter("baseUrl", r3base));

        fmeAsyncJob.setPublishedParameters(parameters);

        byte[] decodedBytes =
            Base64.getDecoder().decode(integration.getExternalParameters().get("fileIS"));

        LOG.info("Upload file to FME");
        fmeCommunicationService.sendFile(decodedBytes, datasetId, paramDataProvider, fileName);
        LOG.info("File uploaded");
        LOG.info("Executing FME Import");
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
   *
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
   *
   * @return the execution result VO
   */
  private ExecutionResultVO executeSubmit(String repository, String workspace,
      FMEAsyncJob fmeAsyncJob) {
    Map<String, Object> executionResultParams = new HashMap<>();
    ExecutionResultVO executionResultVO = new ExecutionResultVO();

    Integer executionResult = null;
    try {
      executionResult = fmeCommunicationService.submitAsyncJob(repository, workspace, fmeAsyncJob);
    } catch (Exception e) {
      LOG_ERROR.error("Error invoking FME due to reason {}", e, e.getMessage());
    }
    executionResultParams.put("id", executionResult);
    executionResultVO.setExecutionResultParams(executionResultParams);

    return executionResultVO;
  }


}
