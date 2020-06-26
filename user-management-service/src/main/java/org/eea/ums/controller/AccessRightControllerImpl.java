package org.eea.ums.controller;

import java.util.List;
import org.eea.interfaces.controller.ums.AccessRightController;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class UserManagementControllerImpl.
 */
@RestController
@RequestMapping("/accessRight")
public class AccessRightControllerImpl implements AccessRightController {


  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(AccessRightControllerImpl.class);


  /**
   * Delete resource.
   *
   * @param resourceInfoVO the resource info vo
   */
  @DeleteMapping(value = "/delete")
  @Override
  public void deleteRoleUser(@RequestBody RepresentativeVO representativeVO) {
    // we can only remove role of editor, reporter or reporter partition type
  }

  /**
   * Find role users by group.
   *
   * @param dataflowId the dataflow id
   * @param role the role
   * @return the list
   */
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Override
  public List<RepresentativeVO> findRoleUsersByGroup(@PathVariable("dataflowId") Long dataflowId) {
    // we can find editors, reporters or reporter partition roles based on the dataflow state
    return null;
  }

  /**
   * Update role user.
   *
   * @param representativeVO the representative VO
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/update/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity updateRoleUser(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody RepresentativeVO representativeVO) {
    // we can only update an editor, reporter or reporter partition role
    return null;
  }

  /**
   * Creates the representative.
   *
   * @param dataflowId the dataflow id
   * @param representativeVO the representative VO
   * @return the long
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN')")
  @PostMapping("/{dataflowId}")
  public Long createRoleUser(@PathVariable("dataflowId") Long dataflowId,
      @RequestBody RepresentativeVO representativeVO) {
    // we can only assign an editor, reporter or reporter partition role
    return null;
  }
}
