package org.eea.dataflow.integration.executor.fme.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataflow.integration.executor.fme.domain.FMEAsyncJob;
import org.eea.dataflow.integration.executor.fme.domain.SubmitResult;
import org.eea.utils.LiteralConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class FMEFeignService {

  // fme.discomap.eea.europa.eu
  @Value("${integration.fme.host}")
  private String fmeHost;

  // https
  @Value("${integration.fme.scheme}")
  private String fmeScheme;

  // Basic UmVwb3J0bmV0MzpSZXBvcnRuZXQzXzIwMjAh
  @Value("${integration.fme.token}")
  private String fmeToken;

  /**
   * Submit async job.
   *
   * @param repository the repository
   * @param workspace the workspace
   * @param fmeAsyncJob the fme async job
   * @return the integer
   */
  public Integer submitAsyncJob(String repository, String workspace, FMEAsyncJob fmeAsyncJob) {

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put("repository", repository);
    uriParams.put("workspace", workspace);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    HttpEntity<FMEAsyncJob> request = createHttpRequest(fmeAsyncJob, uriParams);
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<SubmitResult> checkResult =
        restTemplate.exchange(
            uriComponentsBuilder.scheme(fmeScheme).host(fmeHost)
                .path("fmerest/v3/transformations/submit/{repository}/{workspace}")
                .buildAndExpand(uriParams).toString(),
            HttpMethod.POST, request, SubmitResult.class);


    Integer result = 0;
    if (null != checkResult && null != checkResult.getBody()) {
      result = checkResult.getBody().getId();
    }
    return result;
  }

  /**
   * Creates the http request.
   *
   * @param <T> the generic type
   * @param body the body
   * @param uriParams the uri params
   * @return the http entity
   */
  private <T> HttpEntity<T> createHttpRequest(T body, Map<String, String> uriParams) {
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put(LiteralConstants.AUTHORIZATION_HEADER, fmeToken);
    headerInfo.put("Content-Type", "application/json");
    HttpHeaders headers = createBasicHeaders(headerInfo);

    HttpEntity<T> request = new HttpEntity<>(body, headers);
    return request;
  }

  /**
   * Creates the basic headers.
   *
   * @param headersInfo the headers info
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
