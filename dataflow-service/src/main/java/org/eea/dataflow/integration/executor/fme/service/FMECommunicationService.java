package org.eea.dataflow.integration.executor.fme.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eea.dataflow.integration.executor.fme.domain.FMEAsyncJob;
import org.eea.dataflow.integration.executor.fme.domain.FMECollection;
import org.eea.dataflow.integration.executor.fme.domain.FileSubmitResult;
import org.eea.dataflow.integration.executor.fme.domain.SubmitResult;
import org.eea.dataflow.integration.executor.fme.mapper.FMECollectionMapper;
import org.eea.dataflow.persistence.domain.FMEJob;
import org.eea.dataflow.persistence.repository.FMEJobRepository;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAUnauthorizedException;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.enums.FMEJobstatus;
import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.thread.ThreadPropertiesManager;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class FMECommunicationService.
 */
@Service
public class FMECommunicationService {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(FMECommunicationService.class);

  /** The Constant APPLICATION_JSON: {@value}. */
  private static final String APPLICATION_JSON = "application/json";

  /** The Constant CONTENT_TYPE: {@value}. */
  private static final String CONTENT_TYPE = "Content-Type";

  /** The Constant ACCEPT: {@value}. */
  private static final String ACCEPT = "Accept";

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

  /**
   * Submit async job.
   *
   * @param repository the repository
   * @param workspace the workspace
   * @param fmeAsyncJob the fme async job
   *
   * @return the integer
   */
  public Integer submitAsyncJob(String repository, String workspace, FMEAsyncJob fmeAsyncJob) {

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
      HttpEntity<FMEAsyncJob> request = createHttpRequest(fmeAsyncJob, uriParams, headerInfo);
      String textBody = fmeAsyncJob.toString();
      ObjectMapper mapper = new ObjectMapper();
      String jsonString = null;
      try {
        jsonString = mapper.writeValueAsString(fmeAsyncJob);
      } catch (JsonProcessingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      checkResult = this.restTemplate.exchange(uriComponentsBuilder.scheme(fmeScheme).host(fmeHost)
          .path("fmerest/v3/transformations/submit/{repository}/{workspace}")
          .buildAndExpand(uriParams).toString(), HttpMethod.POST, request, SubmitResult.class);
      LOG.info("FME called successfully: HTTP:{}", checkResult.getStatusCode());
      result = checkResult != null && checkResult.getBody() != null
          && checkResult.getBody().getId() != null ? checkResult.getBody().getId() : 0;
    } catch (HttpStatusCodeException exception) {
      LOG_ERROR.error("Status code: {} message: {}", exception.getStatusCode().value(),
          exception.getMessage());
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
  public FileSubmitResult sendFile(byte[] file, Long idDataset, String idProvider,
      String fileName) {

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put("datasetId", String.valueOf(idDataset));
    String auxURL =
        "fmerest/v3/resources/connections/Reportnet3/filesys/{datasetId}/design?createDirectories=true&overwrite=true";

    if (null != idProvider) {
      uriParams.put("providerId", idProvider);
      auxURL =
          "fmerest/v3/resources/connections/Reportnet3/filesys/{datasetId}/{providerId}?createDirectories=true&overwrite=true";
    }
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    headerInfo.put(CONTENT_TYPE, "application/octet-stream");
    headerInfo.put(ACCEPT, APPLICATION_JSON);
    HttpEntity<byte[]> request = createHttpRequest(file, uriParams, headerInfo);
    String url = uriComponentsBuilder.scheme(fmeScheme).host(fmeHost).path(auxURL)
        .buildAndExpand(uriParams).toString();
    ResponseEntity<FileSubmitResult> checkResult =
        this.restTemplate.exchange(url, HttpMethod.POST, request, FileSubmitResult.class);

    FileSubmitResult result = new FileSubmitResult();
    if (null != checkResult.getBody()) {
      result = checkResult.getBody();
    }
    return result;

  }

  /**
   * Creates the directory.
   *
   * @param idDataset the id dataset
   * @param idProvider the id provider
   * @return the http status
   */
  public HttpStatus createDirectory(Long idDataset, String idProvider) {

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put("datasetId", String.valueOf(idDataset));
    String auxURL = "fmerest/v3/resources/connections/Reportnet3/filesys/{datasetId}/design";
    if (null != idProvider) {
      uriParams.put("providerId", idProvider);
      auxURL = "fmerest/v3/resources/connections/Reportnet3/filesys/{datasetId}/{providerId}";
    }
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put(CONTENT_TYPE, "application/x-www-form-urlencoded");
    headerInfo.put(ACCEPT, APPLICATION_JSON);

    String body = "directoryname=ExportFiles";

    HttpEntity<byte[]> request = createHttpRequest(body.getBytes(), uriParams, headerInfo);
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
   * @return the file submit result
   */
  public InputStream receiveFile(Long idDataset, Long providerId, String fileName) {

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put("datasetId", String.valueOf(idDataset));
    String auxURL =
        "fmerest/v3/resources/connections/Reportnet3/filesys/{datasetId}/design/ExportFiles/{fileName}?accept=contents&disposition=attachment";
    if (null != providerId) {
      uriParams.put("providerId", providerId.toString());
      auxURL =
          "fmerest/v3/resources/connections/Reportnet3/filesys/{datasetId}/{providerId}/ExportFiles/{fileName}?accept=contents&disposition=attachment";
    }
    uriParams.put("fileName", fileName);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    Map<String, String> headerInfo = new HashMap<>();

    headerInfo.put(ACCEPT, "application/octet-stream");

    HttpEntity<MultiValueMap<String, Object>> request =
        createHttpRequest(null, uriParams, headerInfo);


    ResponseEntity<byte[]> checkResult = null;
    try {
      checkResult = this.restTemplate.exchange(uriComponentsBuilder.scheme(fmeScheme).host(fmeHost)
          .path(auxURL).buildAndExpand(uriParams).toString(), HttpMethod.GET, request,
          byte[].class);
    } catch (HttpClientErrorException e) {
      LOG_ERROR.info("Error downloading file: {}  from FME", fileName);
    }


    InputStream initialStream = new ByteArrayInputStream(checkResult.getBody());
    return initialStream;

  }

  /**
   * Find repository.
   *
   * @return the collection
   */
  public FMECollectionVO findRepository() {

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put("limit", String.valueOf(-1));
    uriParams.put("offset", String.valueOf(-1));
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put(ACCEPT, APPLICATION_JSON);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    HttpEntity<Void> request = createHttpRequest(null, uriParams, headerInfo);

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
   *
   * @return the collection
   */
  public FMECollectionVO findItems(String repository) {

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put("repository", repository);
    uriParams.put("type", "WORKSPACE");
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put(ACCEPT, APPLICATION_JSON);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    HttpEntity<Void> request = createHttpRequest(null, uriParams, headerInfo);

    ResponseEntity<FMECollection> responseEntity = this.restTemplate.exchange(uriComponentsBuilder
        .scheme(fmeScheme).host(fmeHost).path("fmerest/v3/repositories/{repository}/items")
        .buildAndExpand(uriParams).toString(), HttpMethod.GET, request, FMECollection.class);

    FMECollection result =
        Optional.ofNullable(responseEntity).map(ResponseEntity::getBody).orElse(null);

    return fmeCollectionMapper.entityToClass(result);

  }

  /**
   * Authenticate and authorize a request against the given fmeJobId. Returns the full FMEJob object
   * if the process completes successfully.
   *
   * @param apiKey the api key
   * @param rn3JobId the rn 3 job id
   * @return the FME job
   * @throws EEAException the EEA exception
   */
  public FMEJob authenticateAndAuthorize(String apiKey, Long rn3JobId) throws EEAException {

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
      ThreadPropertiesManager.setVariable("user", userName);

      // Authorization
      FMEJob fmeJob = fmeJobRepository.findById(rn3JobId).orElse(null);
      if (!(null != fmeJob && userName.equals(fmeJob.getUserName()))) {
        LOG_ERROR.error("User not allowed: userName={}, fmeJobId={}", userName, rn3JobId);
        throw new EEAForbiddenException(EEAErrorMessage.FORBIDDEN);
      }

      LOG.info("Succefully logged in: userName={}, apiKey={}, fmeJob={}", userName, apiKey, fmeJob);
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
   * @throws EEAException the EEA exception
   */
  public void releaseNotifications(FMEJob fmeJob, long statusNumber) throws EEAException {

    // Build the major notification
    EventType eventType;
    boolean isReporting = null != fmeJob.getProviderId();
    boolean isStatusCompleted = statusNumber == 0L;
    NotificationVO notificationVO =
        NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
            .datasetId(fmeJob.getDatasetId()).dataflowId(fmeJob.getDataflowId())
            .fileName(fmeJob.getFileName()).providerId(fmeJob.getProviderId()).build();

    // Set the notification EventType
    switch (fmeJob.getOperation()) {
      case IMPORT:
        eventType = importNotification(isReporting, isStatusCompleted);
        kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION,
            fmeJob.getDatasetId());
        break;
      case EXPORT:
        eventType = exportNotification(isReporting, isStatusCompleted);
        break;
      case EXPORT_EU_DATASET:
        eventType = exportEUDatasetNotification(isStatusCompleted);
        break;
      case IMPORT_FROM_OTHER_SYSTEM:
        eventType = importFromOtherSystemNotification(isReporting);
        break;
      default:
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Release the notification
    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, null, notificationVO);
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
   *
   * @return the http entity
   */
  private <T> HttpEntity<T> createHttpRequest(T body, Map<String, String> uriParams,
      Map<String, String> headerInfo) {
    headerInfo.put(LiteralConstants.AUTHORIZATION_HEADER, fmeToken);
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
   * @param isReporting the is provider
   * @param isStatusCompleted the is status completed
   * @return the event type
   */
  private EventType importNotification(boolean isReporting, boolean isStatusCompleted) {
    EventType eventType;
    if (isStatusCompleted) {
      if (isReporting) {
        eventType = EventType.EXTERNAL_IMPORT_REPORTING_COMPLETED_EVENT;
      } else {
        eventType = EventType.EXTERNAL_IMPORT_DESIGN_COMPLETED_EVENT;
      }
    } else {
      if (isReporting) {
        eventType = EventType.EXTERNAL_IMPORT_REPORTING_FAILED_EVENT;
      } else {
        eventType = EventType.EXTERNAL_IMPORT_DESIGN_FAILED_EVENT;
      }
    }
    return eventType;
  }

  /**
   * Export notification.
   *
   * @param isReporting the is provider
   * @param isStatusCompleted the is status completed
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
   * @return the event type
   */
  private EventType importFromOtherSystemNotification(boolean isReporting) {
    EventType eventType;
    if (isReporting) {
      eventType = EventType.EXTERNAL_IMPORT_REPORTING_FROM_OTHER_SYSTEM_COMPLETED_EVENT;
    } else {
      eventType = EventType.EXTERNAL_IMPORT_DESIGN_FROM_OTHER_SYSTEM_COMPLETED_EVENT;
    }
    return eventType;
  }
}
