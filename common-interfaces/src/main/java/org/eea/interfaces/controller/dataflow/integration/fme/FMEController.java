package org.eea.interfaces.controller.dataflow.integration.fme;

import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.eea.interfaces.vo.integration.fme.FMEOperationInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/** The Interface FMEController. */
public interface FMEController {

  /**
   * The Interface FMEControllerZuul.
   */
  @FeignClient(value = "dataflow", contextId = "fme", path = "/fme")
  interface FMEControllerZuul extends FMEController {

  }

  /**
   * Find repositories.
   *
   * @param datasetId the dataset id
   * @return the collection
   */
  @GetMapping(value = "/findRepositories", produces = MediaType.APPLICATION_JSON_VALUE)
  FMECollectionVO findRepositories(@RequestParam("datasetId") Long datasetId);

  /**
   * Find items.
   *
   * @param datasetId the dataset id
   * @param repository the repository
   * @return the collection VO
   */
  @GetMapping(value = "/findItems", produces = MediaType.APPLICATION_JSON_VALUE)
  FMECollectionVO findItems(@RequestParam("datasetId") Long datasetId,
      @RequestParam("repository") String repository);

  /**
   * Operation finished.
   *
   * @param fmeOperationInfoVO the fme operation info VO
   */
  @PostMapping("/operationFinished")
  void operationFinished(@RequestBody FMEOperationInfoVO fmeOperationInfoVO);

  /**
   * Download export file.
   *
   * @param datasetId the dataset id
   * @param providerId the provider id
   * @param fileName the file name
   * @return the response entity
   */
  @GetMapping(value = "/downloadExportFile")
  ResponseEntity<StreamingResponseBody> downloadExportFile(
      @RequestParam("datasetId") Long datasetId,
      @RequestParam(value = "providerId", required = false) Long providerId,
      @RequestParam("fileName") String fileName);

  /**
   * Update job status by id.
   *
   * @param jobId the job id
   * @param status the status
   */
  @GetMapping("/private/updateJobStatusById")
  void updateJobStatusById(@RequestParam("jobId") Long jobId, @RequestParam("status") Long status);
}
