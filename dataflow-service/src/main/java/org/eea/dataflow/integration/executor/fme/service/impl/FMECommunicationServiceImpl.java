package org.eea.dataflow.integration.executor.fme.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eea.dataflow.integration.executor.fme.domain.FMEAsyncJob;
import org.eea.dataflow.integration.executor.fme.domain.FMECollection;
import org.eea.dataflow.integration.executor.fme.domain.FileSubmitResult;
import org.eea.dataflow.integration.executor.fme.domain.PublishedParameter;
import org.eea.dataflow.integration.executor.fme.domain.SubmitResult;
import org.eea.dataflow.integration.executor.fme.mapper.FMECollectionMapper;
import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
import org.eea.dataflow.persistence.domain.FMEJob;
import org.eea.dataflow.persistence.domain.FMEUser;
import org.eea.dataflow.persistence.repository.FMEJobRepository;
import org.eea.dataflow.persistence.repository.FMEUserRepository;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAUnauthorizedException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.FMEJobstatus;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The Class FMECommunicationServiceImpl.
 */
@Service
public class FMECommunicationServiceImpl implements FMECommunicationService {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(FMECommunicationServiceImpl.class);

  /** The Constant APPLICATION_JSON: {@value}. */
  private static final String APPLICATION_JSON = "application/json";

  /** The Constant CONTENT_TYPE: {@value}. */
  private static final String CONTENT_TYPE = "Content-Type";

  /** The Constant ACCEPT: {@value}. */
  private static final String ACCEPT = "Accept";

  /** The Constant DATASETID: {@value}. */
  private static final String DATASETID = "datasetId";

  /** The Constant PROVIDERID: {@value}. */
  private static final String PROVIDERID = "providerId";

  /** The fme host. */
  @Value("${integration.fme.host}")
  private String fmeHost;

  /** The fme scheme. */
  @Value("${integration.fme.scheme}")
  private String fmeScheme;

  /** The fme token. */
  @Value("${integration.fme.token}")
  private String fmeToken;

  /** The fme collection mapper. */
  @Autowired
  private FMECollectionMapper fmeCollectionMapper;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The rest template. */
  @Autowired
  private RestTemplate restTemplate;

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The fme job repository. */
  @Autowired
  private FMEJobRepository fmeJobRepository;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The dataflow service. */
  @Autowired
  private DataflowService dataflowService;

  /** The fme user repository. */
  @Autowired
  private FMEUserRepository fmeUserRepository;


  /**
   * Submit async job.
   *
   * @param repository the repository
   * @param workspace the workspace
   * @param fmeAsyncJob the fme async job
   * @param dataflowId the dataflow id
   * @return the integer
   */
  @Override
  public Integer submitAsyncJob(String repository, String workspace, FMEAsyncJob fmeAsyncJob,
      Long dataflowId) {

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put("repository", repository);
    uriParams.put("workspace", workspace);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put(CONTENT_TYPE, APPLICATION_JSON);
    headerInfo.put(ACCEPT, APPLICATION_JSON);

    ResponseEntity<SubmitResult> checkResult = null;
    Integer result = 0;
    try {
      HttpEntity<FMEAsyncJob> request =
          createHttpRequest(fmeAsyncJob, uriParams, headerInfo, dataflowId);
      checkResult = this.restTemplate.exchange(uriComponentsBuilder.scheme(fmeScheme).host(fmeHost)
          .path("fmerest/v3/transformations/submit/{repository}/{workspace}")
          .buildAndExpand(uriParams).toString(), HttpMethod.POST, request, SubmitResult.class);

      if (null != checkResult && null != checkResult.getBody()
          && null != checkResult.getBody().getId()) { // NOSONAR check result and body are verified
                                                      // not to be null. false positive
        LOG.info("FME called successfully: HTTP:{}", checkResult.getStatusCode());
        result = checkResult.getBody().getId(); // NOSONAR check result and body are verified not to
                                                // be null. false positive
      } else {
        throw new IllegalStateException("Error submitting job to FME, no result retrieved");
      }

    } catch (HttpStatusCodeException exception) {
      LOG_ERROR.error("Status code: {} message: {}", exception.getStatusCode().value(),
          exception.getMessage(), exception);
      Map<String, PublishedParameter> mapParameters = fmeAsyncJob.getPublishedParameters().stream()
          .collect(Collectors.toMap(PublishedParameter::getName, Function.identity()));

      Long datasetId = null;
      String fileName = "";
      if (mapParameters.containsKey("datasetId")) {
        datasetId = Long.valueOf(mapParameters.get("datasetId").getValue().toString());
      }
      if (mapParameters.containsKey("inputfile")) {
        fileName = mapParameters.get("inputfile").getValue().toString();
      }
      String user = SecurityContextHolder.getContext().getAuthentication().getName();
      NotificationVO notificationVO =
          NotificationVO.builder().user(user).error("Error calling to FME").datasetId(datasetId)
              .dataflowId(dataflowId).fileName(fileName).build();
      try {
        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.CALL_FME_PROCESS_FAILED_EVENT, null,
            notificationVO);
      } catch (EEAException e1) {
        LOG_ERROR.error("Failed sending kafka notification due to an error calling FME: {}",
            e1.getMessage(), e1);
      }
    }
    return result;
  }

  /**
   * Send file.
   *
   * @param file the file
   * @param idDataset the id dataset
   * @param idProvider the id provider
   * @param fileName the file name
   *
   * @return the file submit result
   */
  @Override
  public FileSubmitResult sendFile(byte[] file, Long idDataset, String idProvider,
      String fileName) {

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(DATASETID, String.valueOf(idDataset));
    String auxURL =
        "fmerest/v3/resources/connections/Reportnet3/filesys/{datasetId}/design?createDirectories=true&overwrite=true";

    if (null != idProvider) {
      uriParams.put(PROVIDERID, idProvider);
      auxURL =
          "fmerest/v3/resources/connections/Reportnet3/filesys/{datasetId}/{providerId}?createDirectories=true&overwrite=true";
    }
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    Map<String, String> headerInfo = new HashMap<>();

    try {
      headerInfo.put("Content-Disposition",
          "attachment; filename*=UTF-8''\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
    } catch (UnsupportedEncodingException e) {
      LOG_ERROR.error("Error encoding file: {}", fileName);
    }
    headerInfo.put(CONTENT_TYPE, "application/octet-stream");
    headerInfo.put(ACCEPT, APPLICATION_JSON);
    DataSetMetabaseVO dataset = datasetMetabaseControllerZuul.findDatasetMetabaseById(idDataset);
    HttpEntity<byte[]> request =
        createHttpRequest(file, uriParams, headerInfo, dataset.getDataflowId());
    String url = uriComponentsBuilder.scheme(fmeScheme).host(fmeHost).path(auxURL)
        .buildAndExpand(uriParams).toString();
    ResponseEntity<FileSubmitResult> checkResult =
        this.restTemplate.exchange(url, HttpMethod.POST, request, FileSubmitResult.class);

    FileSubmitResult result = new FileSubmitResult();
    if (null != checkResult && null != checkResult.getBody()) {
      result = checkResult.getBody();
    }
    return result;

  }

  /**
   * Creates the directory.
   *
   * @param idDataset the id dataset
   * @param idProvider the id provider
   *
   * @return the http status
   */
  @Override
  public HttpStatus createDirectory(Long idDataset, String idProvider) {

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(DATASETID, String.valueOf(idDataset));
    String auxURL = "fmerest/v3/resources/connections/Reportnet3/filesys/{datasetId}/design";
    if (null != idProvider) {
      uriParams.put(PROVIDERID, idProvider);
      auxURL = "fmerest/v3/resources/connections/Reportnet3/filesys/{datasetId}/{providerId}";
    }
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put(CONTENT_TYPE, "application/x-www-form-urlencoded");
    headerInfo.put(ACCEPT, APPLICATION_JSON);

    String body = "directoryname=ExportFiles";
    DataSetMetabaseVO dataset = datasetMetabaseControllerZuul.findDatasetMetabaseById(idDataset);
    HttpEntity<byte[]> request =
        createHttpRequest(body.getBytes(), uriParams, headerInfo, dataset.getDataflowId());
    String url = uriComponentsBuilder.scheme(fmeScheme).host(fmeHost).path(auxURL)
        .buildAndExpand(uriParams).toString();

    ResponseEntity<FileSubmitResult> checkResult = null;
    try {
      checkResult =
          this.restTemplate.exchange(url, HttpMethod.POST, request, FileSubmitResult.class);
    } catch (HttpClientErrorException e) {
      LOG.info("FME called successfully but directory already exist");
      return e.getStatusCode();
    }
    return checkResult.getStatusCode();

  }


  /**
   * Receive file.
   *
   * @param idDataset the id dataset
   * @param providerId the provider id
   * @param fileName the file name
   *
   * @return the file submit result
   */
  @Override
  public InputStream receiveFile(Long idDataset, Long providerId, String fileName) {

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(DATASETID, String.valueOf(idDataset));
    String auxURL =
        "fmerest/v3/resources/connections/Reportnet3/filesys/{datasetId}/design/ExportFiles/{fileName}?accept=contents&disposition=attachment";
    if (null != providerId) {
      uriParams.put(PROVIDERID, providerId.toString());
      auxURL =
          "fmerest/v3/resources/connections/Reportnet3/filesys/{datasetId}/{providerId}/ExportFiles/{fileName}?accept=contents&disposition=attachment";
    }
    uriParams.put("fileName", fileName);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    Map<String, String> headerInfo = new HashMap<>();

    headerInfo.put(ACCEPT, "application/octet-stream");

    DataSetMetabaseVO dataset = datasetMetabaseControllerZuul.findDatasetMetabaseById(idDataset);
    HttpEntity<MultiValueMap<String, Object>> request =
        createHttpRequest(null, uriParams, headerInfo, dataset.getDataflowId());

    ResponseEntity<byte[]> checkResult = null;
    try {
      checkResult = this.restTemplate.exchange(uriComponentsBuilder.scheme(fmeScheme).host(fmeHost)
          .path(auxURL).buildAndExpand(uriParams).toString(), HttpMethod.GET, request,
          byte[].class);
    } catch (HttpClientErrorException e) {
      LOG_ERROR.info("Error downloading file: {}  from FME", fileName, e);
    }
    InputStream stream = null;
    if (null != checkResult && null != checkResult.getBody()) {
      stream = new ByteArrayInputStream(checkResult.getBody()); // NOSONAR check result and body are
                                                                // verified not to be null. false
                                                                // positive
    } else {
      stream = new ByteArrayInputStream(new byte[0]);
    }
    return stream;
  }


  /**
   * Find repository.
   *
   * @param datasetId the dataset id
   * @return the FME collection VO
   */
  @Override
  public FMECollectionVO findRepository(Long datasetId) {

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put("limit", String.valueOf(-1));
    uriParams.put("offset", String.valueOf(-1));
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put(ACCEPT, APPLICATION_JSON);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    DataSetMetabaseVO dataset = datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    HttpEntity<Void> request =
        createHttpRequest(null, uriParams, headerInfo, dataset.getDataflowId());

    ResponseEntity<FMECollection> responseEntity =
        this.restTemplate.exchange(
            uriComponentsBuilder.scheme(fmeScheme).host(fmeHost).path("fmerest/v3/repositories")
                .buildAndExpand(uriParams).toString(),
            HttpMethod.GET, request, FMECollection.class);

    FMECollection result =
        Optional.ofNullable(responseEntity).map(ResponseEntity::getBody).orElse(null);

    return fmeCollectionMapper.entityToClass(result);
  }


  /**
   * Find items.
   *
   * @param repository the repository
   * @param datasetId the dataset id
   * @return the FME collection VO
   */
  @Override
  public FMECollectionVO findItems(String repository, Long datasetId) {

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put("repository", repository);
    uriParams.put("type", "WORKSPACE");
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put(ACCEPT, APPLICATION_JSON);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    DataSetMetabaseVO dataset = datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    HttpEntity<Void> request =
        createHttpRequest(null, uriParams, headerInfo, dataset.getDataflowId());

    ResponseEntity<FMECollection> responseEntity = this.restTemplate.exchange(uriComponentsBuilder
        .scheme(fmeScheme).host(fmeHost).path("fmerest/v3/repositories/{repository}/items")
        .buildAndExpand(uriParams).toString(), HttpMethod.GET, request, FMECollection.class);

    FMECollection result =
        Optional.ofNullable(responseEntity).map(ResponseEntity::getBody).orElse(null);

    return fmeCollectionMapper.entityToClass(result);

  }

  /**
   * Authenticate and authorize.
   *
   * @param apiKey the api key
   * @param rn3JobId the rn 3 job id
   *
   * @return the FME job
   *
   * @throws EEAForbiddenException the EEA forbidden exception
   * @throws EEAUnauthorizedException the EEA unauthorized exception
   */
  @Override
  public FMEJob authenticateAndAuthorize(String apiKey, Long rn3JobId)
      throws EEAForbiddenException, EEAUnauthorizedException {

    TokenVO tokenVO;
    if (null != apiKey && !apiKey.isEmpty()
        && null != (tokenVO = userManagementControllerZull.authenticateUserByApiKey(apiKey))) {

      // Authentication
      String userName = tokenVO.getPreferredUsername();
      Set<String> roles = tokenVO.getRoles();
      Set<String> groups = tokenVO.getGroups();
      if (null != groups && !groups.isEmpty()) {
        groups.stream().map(group -> {
          if (group.startsWith("/")) {
            group = group.substring(1);
          }
          return group.toUpperCase();
        }).forEach(roles::add);
      }
      UserDetails userDetails = EeaUserDetails.create(userName, roles);
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails, "ApiKey " + apiKey, userDetails.getAuthorities());
      Map<String, String> details = new HashMap<>();
      details.put(AuthenticationDetails.USER_ID, tokenVO.getUserId());
      authentication.setDetails(details);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      // Authorization
      FMEJob fmeJob = fmeJobRepository.findById(rn3JobId).orElse(null);
      if (!(null != fmeJob && userName.equals(fmeJob.getUserName()))) {
        LOG_ERROR.error("User not allowed: userName={}, fmeJobId={}", userName, rn3JobId);
        throw new EEAForbiddenException(EEAErrorMessage.FORBIDDEN);
      }

      LOG.info("Successfully logged in: userName={}, apiKey={}, fmeJob={}", userName, apiKey,
          fmeJob);
      return fmeJob;
    }
    LOG_ERROR.error("Invalid apiKey: {}", apiKey);
    throw new EEAUnauthorizedException(EEAErrorMessage.UNAUTHORIZED);
  }

  /**
   * Release notifications.
   *
   * @param fmeJob the fme job
   * @param statusNumber the status number
   * @param notificationRequired the notification required
   */
  @Override
  public void releaseNotifications(FMEJob fmeJob, long statusNumber, boolean notificationRequired) {

    // Build the major notification
    EventType eventType = null;
    boolean isReporting = null != fmeJob.getProviderId();
    boolean isStatusCompleted = statusNumber == 0L;
    NotificationVO notificationVO = NotificationVO.builder().user(fmeJob.getUserName())
        .datasetId(fmeJob.getDatasetId()).dataflowId(fmeJob.getDataflowId())
        .fileName(fmeJob.getFileName()).providerId(fmeJob.getProviderId()).build();

    // Set the notification EventType
    switch (fmeJob.getOperation()) {
      case IMPORT:
        eventType = importNotification(isReporting, isStatusCompleted, fmeJob.getDatasetId(),
            fmeJob.getUserName(), notificationRequired);
        break;
      case EXPORT:
        eventType = exportNotification(isReporting, isStatusCompleted);
        break;
      case EXPORT_EU_DATASET:
        eventType = exportEUDatasetNotification(isStatusCompleted);
        break;
      case IMPORT_FROM_OTHER_SYSTEM:
        eventType = importFromOtherSystemNotification(isReporting, isStatusCompleted,
            fmeJob.getDatasetId(), fmeJob.getUserName());
        break;
      default:
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Release the notification
    try {
      if (null != eventType) {
        notificationVO.setDatasetName(datasetMetabaseControllerZuul
            .findDatasetMetabaseById(fmeJob.getDatasetId()).getDataSetName());
        notificationVO
            .setDataflowName(dataflowService.getMetabaseById(fmeJob.getDataflowId()).getName());

        kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, null, notificationVO);
      }
    } catch (EEAException e) {
      LOG_ERROR.error("Error realeasing event: FMEJob={}", fmeJob, e);
    }
  }

  /**
   * Update job status.
   *
   * @param fmeJob the fme job
   * @param statusNumber the status number
   */
  @Override
  @Transactional
  public void updateJobStatus(FMEJob fmeJob, long statusNumber) {
    fmeJob.setStatus(statusNumber == 0L ? FMEJobstatus.SUCCESS : FMEJobstatus.FAILURE);
    fmeJobRepository.save(fmeJob);
  }


  /**
   * Creates the http request.
   *
   * @param <T> the generic type
   * @param body the body
   * @param uriParams the uri params
   * @param headerInfo the header info
   * @param dataflowId the dataflow id
   * @return the http entity
   */
  private <T> HttpEntity<T> createHttpRequest(T body, Map<String, String> uriParams,
      Map<String, String> headerInfo, Long dataflowId) {
    DataFlowVO dataflowVO = null;
    String token = fmeToken;
    if (null != dataflowId) {
      try {
        dataflowVO = dataflowService.getMetabaseById(dataflowId);
      } catch (EEAException e) {
      }
      if (null != dataflowVO && null != dataflowVO.getFmeUserId()) {
        FMEUser fmeUser = fmeUserRepository.findById(dataflowVO.getFmeUserId()).orElse(null);
        if (null != fmeUser) {
          String userPass = fmeUser.getUsername() + ":" + fmeUser.getPassword();
          token = "Basic " + Base64.getEncoder().encodeToString(userPass.getBytes());
        }
      }
    }
    headerInfo.put(LiteralConstants.AUTHORIZATION_HEADER, token);
    HttpHeaders headers = createBasicHeaders(headerInfo);
    return new HttpEntity<>(body, headers);
  }

  /**
   * Creates the basic headers.
   *
   * @param headersInfo the headers info
   *
   * @return the http headers
   */
  private HttpHeaders createBasicHeaders(Map<String, String> headersInfo) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    if (null != headersInfo && headersInfo.size() > 0) {
      headersInfo.entrySet().forEach(entry -> headers.set(entry.getKey(), entry.getValue()));
    }
    return headers;
  }

  /**
   * Import notification.
   *
   * @param isReporting the is reporting
   * @param isStatusCompleted the is status completed
   * @param datasetId the dataset id
   * @param userName the user name
   * @param notificationRequired the notification required
   *
   * @return the event type
   */
  private EventType importNotification(boolean isReporting, boolean isStatusCompleted,
      Long datasetId, String userName, boolean notificationRequired) {

    EventType eventType = null;
    // Release lock related to Releasing Process
    releaseLockReleasingProcess(datasetId);
    if (isStatusCompleted) {
      if (notificationRequired) {
        if (isReporting) {
          eventType = EventType.EXTERNAL_IMPORT_REPORTING_COMPLETED_EVENT;
        } else {
          eventType = EventType.EXTERNAL_IMPORT_DESIGN_COMPLETED_EVENT;
        }
        launchValidationProcess(datasetId, userName);
      }
    } else {
      if (isReporting) {
        eventType = EventType.EXTERNAL_IMPORT_REPORTING_FAILED_EVENT;
      } else {
        eventType = EventType.EXTERNAL_IMPORT_DESIGN_FAILED_EVENT;
      }
    }

    Map<String, Object> importFileData = new HashMap<>();
    importFileData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_FILE_DATA.getValue());
    importFileData.put(LiteralConstants.DATASETID, datasetId);
    lockService.removeLockByCriteria(importFileData);

    return eventType;
  }

  /**
   * Export notification.
   *
   * @param isReporting the is provider
   * @param isStatusCompleted the is status completed
   *
   * @return the event type
   */
  private EventType exportNotification(boolean isReporting, boolean isStatusCompleted) {
    EventType eventType;
    if (isStatusCompleted) {
      if (isReporting) {
        eventType = EventType.EXTERNAL_EXPORT_REPORTING_COMPLETED_EVENT;
      } else {
        eventType = EventType.EXTERNAL_EXPORT_DESIGN_COMPLETED_EVENT;
      }
    } else {
      if (isReporting) {
        eventType = EventType.EXTERNAL_EXPORT_REPORTING_FAILED_EVENT;
      } else {
        eventType = EventType.EXTERNAL_EXPORT_DESIGN_FAILED_EVENT;
      }
    }
    return eventType;
  }

  /**
   * Export EU dataset notification.
   *
   * @param isStatusCompleted the is status completed
   *
   * @return the event type
   */
  private EventType exportEUDatasetNotification(boolean isStatusCompleted) {
    EventType eventType;
    if (isStatusCompleted) {
      eventType = EventType.EXTERNAL_EXPORT_EUDATASET_COMPLETED_EVENT;
    } else {
      eventType = EventType.EXTERNAL_EXPORT_EUDATASET_FAILED_EVENT;
    }
    return eventType;
  }


  /**
   * Import from other system notification.
   *
   * @param isReporting the is reporting
   * @param isStatusCompleted the is status completed
   * @param datasetId the dataset id
   * @param userName the user name
   *
   * @return the event type
   */
  private EventType importFromOtherSystemNotification(boolean isReporting,
      boolean isStatusCompleted, Long datasetId, String userName) {
    EventType eventType;
    // Release lock related to Releasing Process
    releaseLockReleasingProcess(datasetId);
    if (isStatusCompleted) {
      if (isReporting) {
        eventType = EventType.EXTERNAL_IMPORT_REPORTING_FROM_OTHER_SYSTEM_COMPLETED_EVENT;
      } else {
        eventType = EventType.EXTERNAL_IMPORT_DESIGN_FROM_OTHER_SYSTEM_COMPLETED_EVENT;
      }
      launchValidationProcess(datasetId, userName);
    } else {
      if (isReporting) {
        eventType = EventType.EXTERNAL_IMPORT_REPORTING_FROM_OTHER_SYSTEM_FAILED_EVENT;
      } else {
        eventType = EventType.EXTERNAL_IMPORT_DESIGN_FROM_OTHER_SYSTEM_FAILED_EVENT;
      }
    }
    return eventType;
  }

  /**
   * Launch validation process.
   *
   * @param datasetId the dataset id
   * @param userName the user name
   */
  private void launchValidationProcess(Long datasetId, String userName) {
    Map<String, Object> values = new HashMap<>();
    values.put(LiteralConstants.DATASET_ID, datasetId);
    values.put(LiteralConstants.USER, userName);
    kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, values);
  }

  /**
   * Release lock releasing process.
   *
   * @param datasetId the dataset id
   */
  private void releaseLockReleasingProcess(Long datasetId) {
    // Release lock to the releasing process
    DataSetMetabaseVO datasetMetabaseVO =
        datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    if (datasetMetabaseVO.getDataProviderId() != null) {
      Map<String, Object> releaseSnapshots = new HashMap<>();
      releaseSnapshots.put(LiteralConstants.SIGNATURE, LockSignature.RELEASE_SNAPSHOTS.getValue());
      releaseSnapshots.put(LiteralConstants.DATAFLOWID, datasetMetabaseVO.getDataflowId());
      releaseSnapshots.put(LiteralConstants.DATAPROVIDERID, datasetMetabaseVO.getDataProviderId());
      lockService.removeLockByCriteria(releaseSnapshots);
    }
  }

  /**
   * Update job status by id.
   *
   * @param jobId the job id
   * @param status the status
   */
  @Override
  @Transactional
  public void updateJobStatusById(Long jobId, Long status) {
    FMEJob fmeJob = fmeJobRepository.findById(jobId).orElse(null);
    if (null != fmeJob) {
      updateJobStatus(fmeJob, status);
    }
  }
}
