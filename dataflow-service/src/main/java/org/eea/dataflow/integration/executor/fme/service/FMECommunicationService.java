package org.eea.dataflow.integration.executor.fme.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.eea.dataflow.integration.executor.fme.domain.FMEAsyncJob;
import org.eea.dataflow.integration.executor.fme.domain.FileSubmitResult;
import org.eea.dataflow.integration.executor.fme.domain.SubmitResult;
import org.eea.utils.LiteralConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class FMECommunicationService {

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
   *
   * @return the integer
   */
  public Integer submitAsyncJob(String repository, String workspace, FMEAsyncJob fmeAsyncJob) {

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put("repository", repository);
    uriParams.put("workspace", workspace);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    HttpEntity<FMEAsyncJob> request =
        createHttpRequest(fmeAsyncJob, uriParams, new HashMap<String, String>());
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

  public FileSubmitResult sendFile(InputStream file, Long idDataset, Long idProvider,
      String fileName) {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    try {
      body.add("file", getFile(file));
    } catch (IOException e) {
      e.printStackTrace();
    }
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put("datasetId", String.valueOf(idDataset));
    uriParams.put("providerId", String.valueOf(idProvider));
    uriParams.put("fileName", fileName);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Content-Disposition", "attachment; filename=" + fileName);

    HttpEntity<MultiValueMap<String, Object>> request =
        createHttpRequest(body, uriParams, headerInfo);
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<FileSubmitResult> checkResult = restTemplate.exchange(uriComponentsBuilder
        .scheme(fmeScheme).host(fmeHost)
        .path(
            "fmerest/v3/resources/connections/Reportnet3/{datasetId}/{providerId}/{fileName}?createDirectories=true&overwrite=true")
        .buildAndExpand(uriParams).toString(), HttpMethod.POST, request, FileSubmitResult.class);

    FileSubmitResult result = new FileSubmitResult();
    if (null != checkResult && null != checkResult.getBody()) {
      result = checkResult.getBody();
    }
    return result;

  }

  private Resource getFile(InputStream is) throws IOException {
    Path file = Files.createTempFile("test-file", ".txt");
    System.out.println("Creating and Uploading Test File: " + file);
    Files.write(file, IOUtils.toByteArray(is));
    return new FileSystemResource(file.toFile());
  }

  /**
   * Creates the http request.
   *
   * @param <T> the generic type
   * @param body the body
   * @param uriParams the uri params
   *
   * @return the http entity
   */
  private <T> HttpEntity<T> createHttpRequest(T body, Map<String, String> uriParams,
      Map<String, String> headerInfo) {
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
