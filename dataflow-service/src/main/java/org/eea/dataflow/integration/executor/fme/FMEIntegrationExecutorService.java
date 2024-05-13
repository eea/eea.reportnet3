/*
 *
 */
package org.eea.dataflow.integration.executor.fme;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import org.apache.commons.lang.StringUtils;
import org.eea.dataflow.integration.executor.fme.domain.Directive;
import org.eea.dataflow.integration.executor.fme.domain.FMEAsyncJob;
import org.eea.dataflow.integration.executor.fme.domain.NMDirectives;
import org.eea.dataflow.integration.executor.fme.domain.PublishedParameter;
import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
import org.eea.dataflow.integration.executor.service.AbstractIntegrationExecutorService;
import org.eea.dataflow.persistence.domain.FMEJob;
import org.eea.dataflow.persistence.domain.Integration;
import org.eea.dataflow.persistence.repository.FMEJobRepository;
import org.eea.dataflow.persistence.repository.IntegrationRepository;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.enums.FMEJobstatus;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.eea.interfaces.vo.dataflow.integration.IntegrationParams;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The Class FMEIntegrationExecutorService.
 */
@Component
public class FMEIntegrationExecutorService extends AbstractIntegrationExecutorService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(FMEIntegrationExecutorService.class);

  /** The r 3 base. */
  @Value("${integration.fme.callback.urlbase}")
  private String r3base;

  /** The default repository. */
  @Value("${integration.fme.default.repository}")
  private String defaultRepository;

  /** The EU job. */
  @Value("${integration.fme.eu.job}")
  private String euDatasetJob;

  /** The topic. */
  @Value("${integration.fme.topic}")
  private String topic;

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
  private UserManagementControllerZull userManagementControllerZuul;

  /** The FME job repository. */
  @Autowired
  private FMEJobRepository fmeJobRepository;

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The integration repository. */
  @Autowired
  private IntegrationRepository integrationRepository;

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
  @Transactional
  public ExecutionResultVO execute(IntegrationOperationTypeEnum integrationOperationTypeEnum,
      Object... executionParams) {

    Long datasetId = null;
    String fileName = null;
    IntegrationVO integration = new IntegrationVO();
    JobVO job = null;
    String jobId = null;

    try {
      for (Object param : executionParams) {
        if (null != param && null != param.getClass().getTypeName()) {
          if (param instanceof Long) {
            datasetId = ((Long) param);
          } else if (param instanceof String) {
            fileName = ((String) param);
          } else if (param instanceof IntegrationVO) {
            integration = ((IntegrationVO) param);
          } else if (param instanceof JobVO) {
            job = ((JobVO) param);
            jobId = job.getId().toString();
          }

        }
      }
    } catch (IllegalArgumentException | SecurityException e) {
      LOG.error("Error getting params in FME Integration Executor for jobId {} : {} ", jobId, e.getMessage());
    }
    LOG.info("Extracted params in FME Integration Executor. jobId {} datasetId= {}, fileName= {}", jobId, datasetId, fileName);
    if (IntegrationOperationTypeEnum.EXPORT.equals(integrationOperationTypeEnum)) {
      String extension = integration.getInternalParameters().get(IntegrationParams.FILE_EXTENSION);
      extension = null != extension ? extension.toLowerCase() : FileTypeEnum.XLSX.getValue();
      fileName = LocalDateTime.now().toString("yyyyMMddhhmmss") + "." + extension;
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
        apiKey, fmeAsyncJob, integrationOperationParams, fmeParams, job);
  }

  /**
   * Switch integration operator enum.
   *
   * @param operation the integration operation type enum
   * @param fileName the file name
   * @param integration the integration
   * @param apiKey the api key
   * @param fmeAsyncJob the fme async job
   * @param integrationOperationParams the integration operation params
   * @param fmeParams the fme params
   * @param jobVO the job object
   * @return the execution result VO
   */
  private ExecutionResultVO switchIntegrationOperatorEnum(IntegrationOperationTypeEnum operation,
      String fileName, IntegrationVO integration, String apiKey, FMEAsyncJob fmeAsyncJob,
      Map<String, Long> integrationOperationParams, Map<String, String> fmeParams, JobVO jobVO) {

    String jobId = (jobVO != null) ? jobVO.getId().toString() : null;

    Long datasetId = integrationOperationParams.get(IntegrationParams.DATASET_ID);
    Long dataflowId = integrationOperationParams.get(IntegrationParams.DATAFLOW_ID);
    Long providerId = integrationOperationParams.get(IntegrationParams.PROVIDER_ID);
    String paramDataProvider = null != providerId ? providerId.toString() : "design";
    LOG.info("Called method switchIntegrationOperatorEnum jobId {} datasetId= {}, fileName= {}, providerId= {}", jobId, datasetId, fileName, providerId);

    FMEJob fmeJob = new FMEJob();
    fmeJob.setDatasetId(integrationOperationParams.get(IntegrationParams.DATASET_ID));
    fmeJob.setDataflowId(dataflowId);
    fmeJob.setProviderId(providerId);
    fmeJob.setFileName(fileName);
    fmeJob.setOperation(operation);
    fmeJob.setUserName(SecurityContextHolder.getContext().getAuthentication().getName());
    fmeJob.setStatus(FMEJobstatus.CREATED);
    fmeJob = fmeJobRepository.save(fmeJob);

    Directive apiKeyDirective = new Directive();
    apiKeyDirective.setName(IntegrationParams.APIKEY_PROPERTY);
    apiKeyDirective.setValue(apiKey);
    Directive rn3JobId = new Directive();
    rn3JobId.setName(IntegrationParams.RN3_JOB_ID);
    rn3JobId.setValue(fmeJob.getId().toString());
    Directive notificationRequired = new Directive();
    notificationRequired.setName(IntegrationParams.NOTIFICATION_REQUIRED);
    notificationRequired.setValue("true".equals(
        integration.getInternalParameters().get(IntegrationParams.NOTIFICATION_REQUIRED)) ? "true"
            : "false");
    Directive datasetIdDirective = new Directive();
    datasetIdDirective.setName(IntegrationParams.DATASET_ID);
    datasetIdDirective.setValue(datasetId.toString());

    List<String> topics = Arrays.asList(topic);
    NMDirectives nmDirectives = new NMDirectives();
    nmDirectives.setSuccessTopics(topics);
    nmDirectives.setFailureTopics(topics);
    nmDirectives.setDirectives(
        Arrays.asList(apiKeyDirective, rn3JobId, notificationRequired, datasetIdDirective));

    fmeAsyncJob.setNmDirectives(nmDirectives);

    List<PublishedParameter> parameters = new ArrayList<>();
    parameters.add(saveParameter(IntegrationParams.DATAFLOW_ID,
        integrationOperationParams.get(IntegrationParams.DATAFLOW_ID)));
    parameters.add(saveParameter(IntegrationParams.DATASET_ID,
        integrationOperationParams.get(IntegrationParams.DATASET_ID)));
    parameters.add(saveParameter(IntegrationParams.APIKEY_PROPERTY, "ApiKey " + apiKey));
    parameters.add(saveParameter(IntegrationParams.BASE_URL, r3base));


    Integer fmeJobId = null;
    switch (operation) {
      case EXPORT:
        parameters.add(saveParameter(IntegrationParams.EXPORT_FILE_NAME, fileName));
        parameters.add(saveParameter(IntegrationParams.PROVIDER_ID, paramDataProvider));
        parameters.add(saveParameter(IntegrationParams.FOLDER,
            integrationOperationParams.get(IntegrationParams.DATASET_ID) + "/"
                + paramDataProvider));
        parameters.addAll(addExternalParametersToFMEExecution(integration));
        fmeAsyncJob.setPublishedParameters(parameters);

        LOG.info("Creating Export FS in FME for jobId {} datasetId {} and filename {}", jobId, datasetId, fileName);
        if (fmeCommunicationService
            .createDirectory(integrationOperationParams.get(IntegrationParams.DATASET_ID),
                paramDataProvider)
            .equals(HttpStatus.CONFLICT)) {
          LOG.info("Directory already exists. jobId {} datasetId {} and filename {}", jobId, datasetId, fileName);
        }

        LOG.info("Executing FME Export: jobId {} fmeAsyncJob={} datasetId {}", jobId, fmeAsyncJob, datasetId);
        fmeJobId = executeSubmit(fmeParams.get(IntegrationParams.REPOSITORY),
            fmeParams.get(IntegrationParams.WORKSPACE), fmeAsyncJob, dataflowId, jobVO);
        break;
      case IMPORT:
        parameters.add(saveParameter(IntegrationParams.PROVIDER_ID, paramDataProvider));
        File file = new File(fileName);
        if (StringUtils.isNotBlank(file.getName())) {
          parameters.add(saveParameter(IntegrationParams.INPUT_FILE, file.getName()));
        }
        parameters
            .add(saveParameter(IntegrationParams.FOLDER, datasetId + "/" + paramDataProvider));

        parameters.addAll(addExternalParametersToFMEExecution(integration));
        fmeAsyncJob.setPublishedParameters(parameters);

        LOG.info("Upload {} to FME for jobId {} datasetId {}", fileName, jobId, datasetId);
        fmeCommunicationService.sendFile(datasetId, paramDataProvider, fileName);
        LOG.info("File {} uploaded to FME for jobId {} datasetId {}", fileName, jobId, datasetId);

        LOG.info("Executing FME Import: jobId {} fmeAsyncJob={}  datasetId {}", jobId, fmeAsyncJob, datasetId);
        fmeJobId = executeSubmit(fmeParams.get(IntegrationParams.REPOSITORY),
            fmeParams.get(IntegrationParams.WORKSPACE), fmeAsyncJob, dataflowId, jobVO);
        break;
      case IMPORT_FROM_OTHER_SYSTEM:
        String countryCode = null != providerId
            ? representativeControllerZuul.findDataProviderById(providerId).getCode()
            : "XX";
        parameters.add(saveParameter(IntegrationParams.COUNTRY_CODE, countryCode));
        parameters.add(saveParameter(IntegrationParams.PROVIDER_ID, paramDataProvider));
        parameters.addAll(addExternalParametersToFMEExecution(integration));
        fmeAsyncJob.setPublishedParameters(parameters);
        LOG.info("Executing FME Import to other system: jobId {} fmeAsyncJob={} datasetId {}", jobId, fmeAsyncJob, datasetId);
        fmeJobId = executeSubmit(fmeParams.get(IntegrationParams.REPOSITORY),
            fmeParams.get(IntegrationParams.WORKSPACE), fmeAsyncJob, dataflowId, jobVO);
        break;
      case EXPORT_EU_DATASET:
        parameters.addAll(addExternalParametersToFMEExecution(integration));
        fmeAsyncJob.setPublishedParameters(parameters);
        LOG.info("Executing FME Export EU Dataset: jobId {} fmeAsyncJob={} datasetId {}", jobId, fmeAsyncJob, datasetId);
        fmeJobId = executeSubmit(fmeParams.get(IntegrationParams.REPOSITORY),
            fmeParams.get(IntegrationParams.WORKSPACE), fmeAsyncJob, dataflowId, jobVO);
        break;
      default:
        fmeJobId = null;
        break;
    }
    ExecutionResultVO executionResultVO = new ExecutionResultVO();
    Map<String, Object> executionResultParams = new HashMap<>();
    executionResultParams.put("id", fmeJobId);
    executionResultVO.setExecutionResultParams(executionResultParams);

    // Update FMEJob
    if (null != fmeJobId) {
      fmeJob.setJobId(Long.valueOf(fmeJobId));
      fmeJob.setStatus(FMEJobstatus.QUEUED);
    } else {
      fmeJob.setStatus(FMEJobstatus.ABORTED);
      fmeCommunicationService.releaseNotifications(fmeJob, -1L, true);
    }
    fmeJobRepository.save(fmeJob);
    LOG.info("Saved fme job for jobId {} datasetId= {}, fileName= {}, providerId= {}", jobId, datasetId, fileName, providerId);
    return executionResultVO;
  }

  /**
   * Adds the external parameters to FME execution.
   *
   * @param integration the integration
   * @return the list
   */
  private List<PublishedParameter> addExternalParametersToFMEExecution(IntegrationVO integration) {
    List<PublishedParameter> parameters = new ArrayList<>();
    Integration integrationAux = integrationRepository.findById(integration.getId()).orElse(null);
    if (null != integrationAux && null != integrationAux.getExternalParameters()) {
      integrationAux.getExternalParameters().stream().forEach(external -> {
        if (!external.getParameter().equals(IntegrationParams.FILE_IS)) {
          PublishedParameter parameter = new PublishedParameter();
          parameter.setName(external.getParameter());
          parameter.setValue(external.getValue());
          parameters.add(parameter);
        }
      });
    }
    return parameters;
  }

  /**
   * Gets the api key.
   *
   * @param dataflowId the dataflow id
   * @param dataproviderId the dataprovider id
   * @return the api key
   */
  private String getApiKey(Long dataflowId, Long dataproviderId) {

    String apiKey = userManagementControllerZuul.getApiKey(dataflowId, dataproviderId);

    if (null == apiKey) {
      LOG.info("ApiKey not exits");
      apiKey = userManagementControllerZuul.createApiKey(dataflowId, dataproviderId);
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
   * @param dataflowId the dataflow id
   * @param jobVO the job
   * @return the integer
   */
  private Integer executeSubmit(String repository, String workspace, FMEAsyncJob fmeAsyncJob,
      Long dataflowId, JobVO jobVO) {
    Integer idFMEJob = null;
    try {
      idFMEJob =
          fmeCommunicationService.submitAsyncJob(repository, workspace, fmeAsyncJob, dataflowId, jobVO);
    } catch (Exception e) {
      LOG.error("Error invoking FME due to reason {}", e.getMessage());
    }
    return idFMEJob;
  }
}
