package org.eea.dataflow.controller.fme;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
import org.eea.dataflow.integration.utils.StreamingUtil;
import org.eea.interfaces.controller.dataflow.integration.fme.FMEController;
import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.eea.interfaces.vo.integration.fme.FMEOperationInfoVO;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class FMEControllerImpl.
 */
@RestController
@RequestMapping("/fme")
public class FMEControllerImpl implements FMEController {

  /** The FME communication service. */
  @Autowired
  private FMECommunicationService fmeCommunicationService;

  /** The streaming util. */
  @Autowired
  StreamingUtil streamingUtil;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Find repositories.
   *
   * @param datasetId the dataset id
   * @return the collection VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE') OR (hasRole('DATA_CUSTODIAN')) OR (hasRole('DATA_STEWARD'))")
  @GetMapping(value = "/findRepositories", produces = MediaType.APPLICATION_JSON_VALUE)
  public FMECollectionVO findRepositories(@RequestParam("datasetId") Long datasetId) {
    return fmeCommunicationService.findRepository();
  }

  /**
   * Find items.
   *
   * @param datasetId the dataset id
   * @param repository the repository
   * @return the collection VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE') OR (hasRole('DATA_CUSTODIAN')) OR (hasRole('DATA_STEWARD'))")
  @GetMapping(value = "/findItems", produces = MediaType.APPLICATION_JSON_VALUE)
  public FMECollectionVO findItems(@RequestParam("datasetId") Long datasetId,
      @RequestParam("repository") String repository) {
    return fmeCommunicationService.findItems(repository);
  }

  /**
   * Operation finished.
   *
   * @param fmeOperationInfoVO the fme operation info VO
   */
  @Override
  @PostMapping("/operationFinished")
  @PreAuthorize("checkApiKey(#fmeOperationInfoVO.dataflowId, #fmeOperationInfoVO.providerId)")
  public void operationFinished(@RequestBody FMEOperationInfoVO fmeOperationInfoVO) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    try {
      fmeCommunicationService.operationFinished(fmeOperationInfoVO);
    } catch (Exception e) {
      LOG_ERROR.error("Error in the operationFinished controller with the message: {}",
          e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }


  /**
   * Download export file.
   *
   * @param datasetId the dataset id
   * @param providerId the provider id
   * @param fileName the file name
   * @return the response entity
   */
  @Override
  @GetMapping(value = "/downloadExportFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE') OR (hasRole('DATA_CUSTODIAN')) OR (hasRole('DATA_STEWARD'))")
  public ResponseEntity<StreamingResponseBody> downloadExportFile(
      @RequestParam("datasetId") Long datasetId,
      @RequestParam(value = "providerId", required = false) Long providerId,
      @RequestParam("fileName") String fileName) {
    // devolver inputstream
    // fmeCommunicationService.receiveFile(datasetId, providerId, fileName);

    StreamingResponseBody stream = out -> {
      // InputStream is = new FileInputStream("");
      String initialString =
          " Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent id consectetur felis. Donec nisi neque, pellentesque non felis non, vestibulum scelerisque lacus. Nam quis lobortis neque. Sed aliquam vehicula diam, pulvinar varius ex imperdiet ut. Maecenas vitae facilisis ex. Mauris non neque accumsan, ultrices arcu nec, vehicula ex. Curabitur ullamcorper justo sit amet eros sollicitudin, eu venenatis elit tincidunt. Mauris venenatis tellus et ipsum semper lobortis. Nulla facilisi. Donec eget augue neque."
              + "Pellentesque semper, velit id ultrices euismod, nisl nisi posuere felis, vel porttitor est nisl vitae velit. Sed fringilla orci a orci varius vulputate. Integer vitae nisl nulla. Proin pulvinar est sit amet neque eleifend lacinia. Duis sit amet accumsan neque. Aliquam ex nunc, maximus nec nisl sed, lacinia tempus odio. Suspendisse potenti. Proin mattis auctor nisi sed efficitur. ";
      InputStream is = new ByteArrayInputStream(initialString.getBytes());
      streamingUtil.copy(is, out);
    };


    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

    return new ResponseEntity<StreamingResponseBody>(stream, httpHeaders, HttpStatus.OK);
  }



}
