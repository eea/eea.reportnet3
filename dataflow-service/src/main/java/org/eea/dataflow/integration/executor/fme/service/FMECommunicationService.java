package org.eea.dataflow.integration.executor.fme.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eea.dataflow.integration.executor.fme.domain.FMEAsyncJob;
import org.eea.dataflow.integration.executor.fme.domain.FMECollection;
import org.eea.dataflow.integration.executor.fme.domain.FileSubmitResult;
import org.eea.dataflow.integration.executor.fme.domain.SubmitResult;
import org.eea.dataflow.integration.executor.fme.mapper.FMECollectionMapper;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.eea.interfaces.vo.integration.fme.FMEOperationInfoVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The Class FMECommunicationService.
 */
@Service
public class FMECommunicationService {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");
  private static final Logger LOG = LoggerFactory.getLogger(FMECommunicationService.class);

  /**
   * The fme host.
   */
  // fme.discomap.eea.europa.eu
  @Value("${integration.fme.host}")
  private String fmeHost;

  /**
   * The fme scheme.
   */
  // https
  @Value("${integration.fme.scheme}")
  private String fmeScheme;

  /**
   * The fme token.
   */
  // Basic UmVwb3J0bmV0MzpSZXBvcnRuZXQzXzIwMjAh
  @Value("${integration.fme.token}")
  private String fmeToken;

  /**
   * The fme collection mapper.
   */
  @Autowired
  private FMECollectionMapper fmeCollectionMapper;

  /**
   * The kafka sender utils.
   */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  @Autowired
  private RestTemplate restTemplate;


  /**
   * The Constant APPLICATION_JSON: {@value}.
   */
  private static final String APPLICATION_JSON = "application/json";

  /**
   * The Constant CONTENT_TYPE: {@value}.
   */
  private static final String CONTENT_TYPE = "Content-Type";

  /**
   * The Constant ACCEPT: {@value}.
   */
  private static final String ACCEPT = "Accept";

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

    ResponseEntity<SubmitResult> checkResult = null;
    try {
      HttpEntity<FMEAsyncJob> request = createHttpRequest(fmeAsyncJob, uriParams, headerInfo);
      checkResult = this.restTemplate.exchange(uriComponentsBuilder.scheme(fmeScheme).host(fmeHost)
          .path("fmerest/v3/transformations/submit/{repository}/{workspace}")
          .buildAndExpand(uriParams).toString(), HttpMethod.POST, request, SubmitResult.class);
    } catch (HttpStatusCodeException exception) {
      LOG_ERROR.error("Status code: {} message: {}", exception.getStatusCode().value(),
          exception.getMessage());
      return 0;
    }
    LOG.info("FME called successfully: HTTP:{}", checkResult.getStatusCode());

    return checkResult.getBody() != null ? checkResult.getBody().getId() : 0;
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
    String path =
        "fmerest/v3/resources/connections/Reportnet3/filesys/{datasetId}?createDirectories=true&overwrite=true";
    if (null != idProvider) {
      uriParams.put("providerId", idProvider);
      path =
          "fmerest/v3/resources/connections/Reportnet3/filesys/{datasetId}/{providerId}?createDirectories=true&overwrite=true";
    }
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    headerInfo.put(CONTENT_TYPE, "application/octet-stream");
    headerInfo.put(ACCEPT, APPLICATION_JSON);
    HttpEntity<byte[]> request = createHttpRequest(file, uriParams, headerInfo);
    ResponseEntity<FileSubmitResult> checkResult =
        this.restTemplate.exchange(
            uriComponentsBuilder.scheme(fmeScheme).host(fmeHost).path(path)
                .buildAndExpand(uriParams).toString(),
            HttpMethod.POST, request, FileSubmitResult.class);

    FileSubmitResult result = new FileSubmitResult();
    if (null != checkResult.getBody()) {
      result = checkResult.getBody();
    }
    return result;

  }


  /**
   * Receive file.
   *
   * @param file the file
   * @param idDataset the id dataset
   * @param idProvider the id provider
   * @param fileName the file name
   *
   * @return the file submit result
   */
  public FileSubmitResult receiveFile(byte[] file, Long idDataset, String idProvider,
      String fileName) {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

    body.add("file", file);

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put("datasetId", String.valueOf(idDataset));
    uriParams.put("providerId", idProvider);
    uriParams.put("fileName", fileName);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    headerInfo.put(CONTENT_TYPE, "application/octet-stream");

    MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter =
        new MappingJackson2HttpMessageConverter();
    mappingJackson2HttpMessageConverter.setSupportedMediaTypes(
        Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM));

    HttpEntity<MultiValueMap<String, Object>> request =
        createHttpRequest(body, uriParams, headerInfo);

    this.restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

    ResponseEntity<FileSubmitResult> checkResult = this.restTemplate.exchange(uriComponentsBuilder
        .scheme(fmeScheme).host(fmeHost)
        .path(
            "fmerest/v3/resources/connections/Reportnet3/download/{datasetId}/{providerId}/{fileName}")
        .buildAndExpand(uriParams).toString(), HttpMethod.POST, request, FileSubmitResult.class);

    FileSubmitResult result = new FileSubmitResult();
    if (null != checkResult.getBody()) {
      result = checkResult.getBody();
    }
    return result;

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
   * Operation finished.
   *
   * @param fmeOperationInfoVO the fme operation info VO
   */
  public void operationFinished(FMEOperationInfoVO fmeOperationInfoVO) {

    EventType eventType;
    Long datasetId = fmeOperationInfoVO.getDatasetId();
    String user = (String) ThreadPropertiesManager.getVariable("user");
    NotificationVO notificationVO = NotificationVO.builder().user(user).datasetId(datasetId)
        .dataflowId(fmeOperationInfoVO.getDataflowId()).fileName(fmeOperationInfoVO.getFileName())
        .build();

    LOG.info("Setting operation {} coming from FME as finished", fmeOperationInfoVO);
    switch (fmeOperationInfoVO.getFmeOperation()) {
      case IMPORT:
        eventType = null != fmeOperationInfoVO.getProviderId()
            ? EventType.EXTERNAL_IMPORT_REPORTING_COMPLETED_EVENT
            : EventType.EXTERNAL_IMPORT_DESIGN_COMPLETED_EVENT;
        break;
      case EXPORT:
        eventType = null != fmeOperationInfoVO.getProviderId()
            ? EventType.EXTERNAL_EXPORT_REPORTING_COMPLETED_EVENT
            : EventType.EXTERNAL_EXPORT_DESIGN_COMPLETED_EVENT;
        break;
      case EXPORT_EU_DATASET:
        eventType = EventType.EXTERNAL_EXPORT_EUDATASET_COMPLETED_EVENT;
        break;
      default:
        throw new UnsupportedOperationException("Not yet implemented");
    }

    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, null, notificationVO);
      kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, datasetId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error realeasing event {}", eventType, e);
    }
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


}
