package org.eea.interfaces.controller.dataflow;

import java.util.List;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Class AccessRightController.
 */
public interface AccessRightController {


  /**
   * The interface Resource management controller zull.
   */
  @FeignClient(value = "ums", contextId = "accessRight", path = "/accessRight")
  interface AccessRightControllerZuul extends AccessRightController {

  }

  /**
   * Delete resource.
   *
   * @param resourceInfoVO the resource info vo
   */
  @DeleteMapping(value = "/delete")
  void deleteRoleUser(@RequestBody RepresentativeVO representativeVO,
      @RequestParam Long dataflowId);

  /**
   * Find role users by group.
   *
   * @param dataflowId the dataflow id
   * @param role the role
   * @return the list
   */
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<RepresentativeVO> findRoleUsersByGroup(@PathVariable("dataflowId") Long dataflowId);

  /**
   * Update role user.
   *
   * @param representativeVO the representative VO
   * @return the response entity
   */
  @PutMapping(value = "/update/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity updateRoleUser(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody RepresentativeVO representativeVO);

  /**
   * Creates the role user.
   *
   * @param dataflowId the dataflow id
   * @param representativeVO the representative VO
   * @return the long
   */
  @PostMapping("/{dataflowId}")
  Long createRoleUser(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody RepresentativeVO representativeVO);
}
