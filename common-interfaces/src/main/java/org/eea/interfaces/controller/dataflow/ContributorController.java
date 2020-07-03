package org.eea.interfaces.controller.dataflow;

import java.util.List;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
   * Delete editor.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   */
  @DeleteMapping(value = "/editor/dataflow/{dataflowId}")
  void deleteEditor(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody ContributorVO contributorVO);

  /**
   * Delete reporter.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param contributorVO the contributor VO
   */
  @DeleteMapping(value = "/reporter/dataflow/{dataflowId}/provider/{dataproviderId}")
  void deleteReporter(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataproviderId") Long dataproviderId,
      @RequestBody ContributorVO contributorVO);

  /**
   * Find editors by group.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @GetMapping(value = "/editor/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<ContributorVO> findEditorsByGroup(@PathVariable("dataflowId") Long dataflowId);

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
   * Update editor.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @return the response entity
   */
  @PutMapping(value = "/editor/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<?> updateEditor(@PathVariable("dataflowId") Long dataflowId,
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
  ResponseEntity<?> updateReporter(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataProviderId") Long dataproviderId,
      @RequestBody ContributorVO contributorVO);

}
