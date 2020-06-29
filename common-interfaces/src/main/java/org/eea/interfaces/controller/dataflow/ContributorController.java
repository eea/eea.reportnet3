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
   * Delete.
   *
   * @param dataflowId the dataflow id
   * @param account the account
   */
  @DeleteMapping(value = "/dataflow/{dataflowId}/user/{account}")
  void delete(@PathVariable("dataflowId") Long dataflowId, @PathVariable String account);

  /**
   * Find role users by group.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<ContributorVO> findContributorsByGroup(@PathVariable("dataflowId") Long dataflowId);

  /**
   * Update role user.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the role user VO
   * @return the response entity
   */
  @PutMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity update(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody ContributorVO contributorVO);

  /**
   * Creates the role user.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the role user VO
   * @return the long
   */
  @PostMapping("/dataflow/{dataflowId}")
  Long createContributor(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody ContributorVO contributorVO);
}
