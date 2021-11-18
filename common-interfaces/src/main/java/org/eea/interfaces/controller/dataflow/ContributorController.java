package org.eea.interfaces.controller.dataflow;

import java.util.List;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * The Interface ContributorController.
 */
public interface ContributorController {

  /**
   * The Interface ContributorControllerZuul.
   */
  @FeignClient(value = "dataflow", contextId = "contributor", path = "/contributor")
  interface ContributorControllerZuul extends ContributorController {

  }

  /**
   * Delete requester.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   */
  @DeleteMapping(value = "/requester/dataflow/{dataflowId}")
  void deleteRequester(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody ContributorVO contributorVO);

  /**
   * Delete reporter.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param contributorVO the contributor VO
   */
  @DeleteMapping(value = "/reporter/dataflow/{dataflowId}/provider/{dataProviderId}")
  void deleteReporter(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataProviderId") Long dataProviderId,
      @RequestBody ContributorVO contributorVO);

  /**
   * Find requesters by group.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @GetMapping(value = "/requester/dataflow/{dataflowId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<ContributorVO> findRequestersByGroup(@PathVariable("dataflowId") Long dataflowId);

  /**
   * Find reporters by group.
   *
   * @param dataflowId the dataflow id
   * @param dataproviderId the dataprovider id
   * @return the list
   */
  @GetMapping(value = "/reporter/dataflow/{dataflowId}/provider/{dataproviderId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<ContributorVO> findReportersByGroup(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataproviderId") Long dataproviderId);

  /**
   * Update requester.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @return the response entity
   */
  @PutMapping(value = "/requester/dataflow/{dataflowId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity updateRequester(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody ContributorVO contributorVO);

  /**
   * Update reporter.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param contributorVO the contributor VO
   * @return the response entity
   */
  @PutMapping(value = "/reporter/dataflow/{dataflowId}/provider/{dataProviderId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity updateReporter(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataProviderId") Long dataProviderId,
      @RequestBody ContributorVO contributorVO);

  /**
   * Updates the reporters permissions checking if they are registered in the system.
   *
   * @param dataflowId the dataflow ID
   * @param dataProviderId the data provider ID
   * @return the response entity
   */
  @PutMapping(value = "/validateReporters/dataflow/{dataflowId}/provider/{dataProviderId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity validateReporters(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataProviderId") Long dataProviderId);

  /**
   * Creates the associated permissions.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the dataset id
   */
  @PostMapping("/private/dataflow/{dataflowId}/createAssociatedPermissions/{datasetId}")
  void createAssociatedPermissions(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("datasetId") Long datasetId);
}
