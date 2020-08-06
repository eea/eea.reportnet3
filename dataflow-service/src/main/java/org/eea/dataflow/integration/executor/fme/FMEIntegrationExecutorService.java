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
import org.eea.dataflow.integration.utils.IntegrationParams;
import org.eea.dataflow.persistence.domain.FMEJob;
import org.eea.dataflow.persistence.repository.FMEJobRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The Class FMEIntegrationExecutorService.
 */
@Component
public class FMEIntegrationExecutorService extends AbstractIntegrationExecutorService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(FMEIntegrationExecutorService.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The r 3 base. */
  @Value("${integration.fme.callback.urlbase}")
  private String r3base;

  /** The default repository. */
  @Value("${integration.fme.default.repository}")
  private String defaultRepository;

  /** The EU job. */
  @Value("${integration.fme.eu.job}")
  private String euDatasetJob;

  /** The fme communication service. */
  @Autowired
  private FMECommunicationService fmeCommunicationService;

  /** The data set metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  /** The data set controller zuul. */
  @Autowired
  private DataSetControllerZuul dataSetControllerZuul;

  /** The user management controller. */
  @Autowired
  private UserManagementController userManagementController;


  /** The FME job repository. */
  @Autowired
  FMEJobRepository fmeJobRepository;

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
    if (null != integration.getInternalParameters().get(IntegrationParams.REPOSITORY)) {
      dataflowId = dataset.getDataflowId();
    } else {
      dataflowId = dataSetControllerZuul.getDataFlowIdById(datasetId);
    }

    Long dataproviderId = dataset.getDataProviderId();

    String apiKey = getApiKey(dataflowId, dataproviderId);

    FMEAsyncJob fmeAsyncJob = new FMEAsyncJob();
    String workspace = integration.getInternalParameters().get(IntegrationParams.PROCESS_NAME);
    String repository = null;
    if (null != integration.getInternalParameters().get(IntegrationParams.REPOSITORY)) {
      repository = integration.getInternalParameters().get(IntegrationParams.REPOSITORY);
    } else {
      repository = defaultRepository;
    }

    Map<String, Long> integrationOperationParams = new HashMap<>();
    integrationOperationParams.put(IntegrationParams.DATASET_ID, datasetId);
    integrationOperationParams.put(IntegrationParams.DATAFLOW_ID, dataflowId);
    integrationOperationParams.put(IntegrationParams.PROVIDER_ID, dataproviderId);

    Map<String, String> fmeParams = new HashMap<>();
    fmeParams.put(IntegrationParams.WORKSPACE, workspace);
    fmeParams.put(IntegrationParams.REPOSITORY, repository);

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

    Long providerId = integrationOperationParams.get(IntegrationParams.PROVIDER_ID);
    String paramDataProvider = null != providerId ? providerId.toString() : "design";

    List<PublishedParameter> parameters = new ArrayList<>();

    // dataflowId
    parameters.add(saveParameter(IntegrationParams.DATAFLOW_ID,
        integrationOperationParams.get(IntegrationParams.DATAFLOW_ID)));
    // datasetDataId
    parameters.add(saveParameter(IntegrationParams.DATASET_ID,
        integrationOperationParams.get(IntegrationParams.DATASET_ID)));
    // apikey
    parameters.add(saveParameter(IntegrationParams.APIKEY_PROPERTY, "ApiKey " + apiKey));
    // base URL
    parameters.add(saveParameter(IntegrationParams.BASE_URL, r3base));

    Integer idFMEJob = null;
    switch (integrationOperationTypeEnum) {
      case EXPORT:
        // providerId
        parameters.add(saveParameter(IntegrationParams.PROVIDER_ID, paramDataProvider));
        // folder
        parameters.add(saveParameter(IntegrationParams.FOLDER,
            integrationOperationParams.get(IntegrationParams.DATASET_ID) + "/"
                + paramDataProvider));

        fmeAsyncJob.setPublishedParameters(parameters);

        LOG.info("Creating Export FS in FME");
        if (fmeCommunicationService
            .createDirectory(integrationOperationParams.get(IntegrationParams.DATASET_ID),
                paramDataProvider)
            .equals(HttpStatus.CONFLICT)) {
          LOG.info("Directory already exist");
        } else {
          LOG.info("Directory created successful");
        }
        LOG.info("Executing FME Export");
        idFMEJob = executeSubmit(fmeParams.get(IntegrationParams.REPOSITORY),
            fmeParams.get(IntegrationParams.WORKSPACE), fmeAsyncJob);
        break;
      case IMPORT:
        // providerId
        parameters.add(saveParameter(IntegrationParams.PROVIDER_ID, paramDataProvider));
        // inputfile
        parameters.add(saveParameter(IntegrationParams.INPUT_FILE, fileName));
        // folder
        parameters.add(saveParameter(IntegrationParams.FOLDER,
            integrationOperationParams.get(IntegrationParams.DATASET_ID) + "/"
                + paramDataProvider));

        fmeAsyncJob.setPublishedParameters(parameters);

        byte[] decodedBytes = Base64.getDecoder()
            .decode(integration.getExternalParameters().get(IntegrationParams.FILE_IS));

        LOG.info("Upload {} to FME", fileName);
        fmeCommunicationService.sendFile(decodedBytes,
            integrationOperationParams.get(IntegrationParams.DATASET_ID), paramDataProvider,
            fileName);
        LOG.info("File uploaded");
        LOG.info("Executing FME Import");
        idFMEJob = executeSubmit(fmeParams.get(IntegrationParams.REPOSITORY),
            fmeParams.get(IntegrationParams.WORKSPACE), fmeAsyncJob);
        break;
      case EXPORT_EU_DATASET:
        // DataBaseConnectionPublic
        parameters.add(saveParameter(IntegrationParams.DATABASE_CONNECTION_PUBLIC, ""));
        // mode
        parameters.add(saveParameter(IntegrationParams.MODE, ""));

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
      job.setDatasetId(integrationOperationParams.get(IntegrationParams.DATASET_ID));
      job.setOperation(integrationOperationTypeEnum);
      job.setUser(SecurityContextHolder.getContext().getAuthentication().getName().toString());
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
}
