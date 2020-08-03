package org.eea.dataflow.integration.executor.fme;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.dataflow.integration.executor.fme.domain.FMEAsyncJob;
import org.eea.dataflow.integration.executor.fme.domain.PublishedParameter;
import org.eea.dataflow.integration.executor.fme.persistance.domain.FMEJob;
import org.eea.dataflow.integration.executor.fme.persistance.repository.FMEJobRepository;
import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
import org.eea.dataflow.integration.executor.service.AbstractIntegrationExecutorService;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.dataflow.enums.FMEJobstatus;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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

  /** The default repository. */
  @Value("${integration.fme.default.repository}")
  private String defaultRepository;

  /** The EU job. */
  @Value("${integration.fme.eu.job}")
  private String euDatasetJob;


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant REPOSITORY: {@value}. */
  private static final String REPOSITORY = "repository";

  /** The Constant WORKSPACE: {@value}. */
  private static final String WORKSPACE = "workspace";

  /** The Constant DATASET_ID: {@value}. */
  private static final String DATASET_ID = "datasetId";

  /** The Constant DATAFLOW_ID: {@value}. */
  private static final String DATAFLOW_ID = "dataflowId";

  /** The Constant PROVIDER_ID: {@value}. */
  private static final String PROVIDER_ID = "providerId";

  /** The Constant APIKEY_PROPERTY: {@value}. */
  private static final String APIKEY_PROPERTY = "apiKey";

  /** The Constant APIKEY_TOKEN: {@value}. */
  private static final String APIKEY_TOKEN = "ApiKey ";

  /** The Constant BASE_URL: {@value}. */
  private static final String BASE_URL = "baseUrl";

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


  /** The FME job repository. */
  @Autowired
  FMEJobRepository fmeJobRepository;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(FMEIntegrationExecutorService.class);


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
      LOG_ERROR.error("Error getting params in FME Integration Executor: {} ", e.getMessage());
    }

    DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    Long dataflowId;
    if (null != integration.getInternalParameters().get(REPOSITORY)) {
      dataflowId = dataset.getDataflowId();
    } else {
      dataflowId = dataSetControllerZuul.getDataFlowIdById(datasetId);
    }

    Long dataproviderId = dataset.getDataProviderId();

    String apiKey = getApiKey(dataflowId, dataproviderId);

    FMEAsyncJob fmeAsyncJob = new FMEAsyncJob();
    String workspace = integration.getInternalParameters().get("processName");
    String repository = null;
    if (null != integration.getInternalParameters().get(REPOSITORY)) {
      repository = integration.getInternalParameters().get(REPOSITORY);
    } else {
      repository = defaultRepository;
    }


    Map<String, Long> integrationOperationParams = new HashMap<>();
    integrationOperationParams.put(DATASET_ID, datasetId);
    integrationOperationParams.put(DATAFLOW_ID, dataflowId);
    integrationOperationParams.put(PROVIDER_ID, dataproviderId);


    Map<String, String> fmeParams = new HashMap<>();
    fmeParams.put(WORKSPACE, workspace);
    fmeParams.put(REPOSITORY, repository);


    return switchIntegrationOperatorEnum(integrationOperationTypeEnum, fileName, integration,
        apiKey, fmeAsyncJob, integrationOperationParams, fmeParams);
  }

  /**
   * Switch integration operator enum.
   *
   * @param integrationOperationTypeEnum the integration operation type enum
   * @param fileName the file name
   * @param integration the integration
   * @param apiKey the api key
   * @param fmeAsyncJob the fme async job
   * @param integrationOperationParams the integration operation params
   * @param fmeParams the fme params
   * @return the execution result VO
   */
  private ExecutionResultVO switchIntegrationOperatorEnum(
      IntegrationOperationTypeEnum integrationOperationTypeEnum, String fileName,
      IntegrationVO integration, String apiKey, FMEAsyncJob fmeAsyncJob,
      Map<String, Long> integrationOperationParams, Map<String, String> fmeParams) {

    Long providerId = integrationOperationParams.get(PROVIDER_ID);
    String paramDataProvider = null != providerId ? providerId.toString() : "design";

    List<PublishedParameter> parameters = new ArrayList<>();

    // dataflowId
    parameters.add(saveParameter(DATAFLOW_ID, integrationOperationParams.get(DATAFLOW_ID)));
    // datasetDataId
    parameters.add(saveParameter(DATASET_ID, integrationOperationParams.get(DATASET_ID)));
    // apikey
    parameters.add(saveParameter(APIKEY_PROPERTY, APIKEY_TOKEN + apiKey));
    // base URL
    parameters.add(saveParameter(BASE_URL, r3base));

    Integer idFMEJob = null;
    switch (integrationOperationTypeEnum) {
      case EXPORT:
        // providerId
        parameters.add(saveParameter(PROVIDER_ID, paramDataProvider));
        // folder
        parameters.add(saveParameter("folder",
            integrationOperationParams.get(DATASET_ID) + "/" + paramDataProvider));

        fmeAsyncJob.setPublishedParameters(parameters);
        LOG.info("Executing FME Export");
        idFMEJob = executeSubmit(fmeParams.get(REPOSITORY), fmeParams.get(WORKSPACE), fmeAsyncJob);
        break;
      case IMPORT:
        // providerId
        parameters.add(saveParameter(PROVIDER_ID, paramDataProvider));
        // inputfile
        parameters.add(saveParameter("inputfile", fileName));
        // folder
        parameters.add(saveParameter("folder",
            integrationOperationParams.get(DATASET_ID) + "/" + paramDataProvider));

        fmeAsyncJob.setPublishedParameters(parameters);

        byte[] decodedBytes =
            Base64.getDecoder().decode(integration.getExternalParameters().get("fileIS"));

        LOG.info("Upload file to FME");
        fmeCommunicationService.sendFile(decodedBytes, integrationOperationParams.get(DATASET_ID),
            paramDataProvider, fileName);
        LOG.info("File uploaded");
        LOG.info("Executing FME Import");
        idFMEJob = executeSubmit(fmeParams.get(REPOSITORY), fmeParams.get(WORKSPACE), fmeAsyncJob);
        break;
      case EXPORT_EU_DATASET:

        // DataBaseConnectionPublic
        parameters.add(saveParameter("DataBaseConnectionPublic", ""));
        // mode
        parameters.add(saveParameter("mode", ""));

        fmeAsyncJob.setPublishedParameters(parameters);
        LOG.info("Executing FME Export EU Dataset");
        idFMEJob = executeSubmit(defaultRepository, euDatasetJob, fmeAsyncJob);
        break;
      default:
        idFMEJob = null;
        break;
    }
    ExecutionResultVO executionResultVO = new ExecutionResultVO();
    Map<String, Object> executionResultParams = new HashMap<>();
    executionResultParams.put("id", idFMEJob);
    executionResultVO.setExecutionResultParams(executionResultParams);

    // add save execution id
    if (null != idFMEJob) {
      FMEJob job = new FMEJob();
      job.setIdJob(new Long(idFMEJob));
      job.setDatasetId(integrationOperationParams.get(DATASET_ID));
      job.setOperation(integrationOperationTypeEnum);
      String user = SecurityContextHolder.getContext().getAuthentication().getName();
      job.setUser(user);
      job.setStatus(FMEJobstatus.QUEUED);
      fmeJobRepository.save(job);
    }
    return executionResultVO;
  }

  /**
   * Gets the api key.
   *
   * @param dataflowId the dataflow id
   * @param dataproviderId the dataprovider id
   * @return the api key
   */
  private String getApiKey(Long dataflowId, Long dataproviderId) {

    String apiKey = userManagementController.getApiKey(dataflowId, dataproviderId);

    if (null == apiKey) {
      LOG.info("ApiKey not exits");
      apiKey = userManagementController.createApiKey(dataflowId, dataproviderId);
      if (null != dataproviderId) {
        LOG.info("ApiKey created for Provider ID: {} and Dataflow ID: {} ", dataproviderId,
            dataflowId);
      } else {
        LOG.info("ApiKey created for Dataflow ID: {} ", dataflowId);
      }
    }
    return apiKey;
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
  private Integer executeSubmit(String repository, String workspace, FMEAsyncJob fmeAsyncJob) {
    Integer idFMEJob = null;
    try {
      idFMEJob = fmeCommunicationService.submitAsyncJob(repository, workspace, fmeAsyncJob);
    } catch (Exception e) {
      LOG_ERROR.error("Error invoking FME due to reason {}", e.getMessage());
    }
    return idFMEJob;
  }

  private Boolean checkTopic(String topicName) {
    return null;
  }

  private Boolean createTopic(String topicName) {
    return null;
  }

  private void createSubscription() {

  }

}
