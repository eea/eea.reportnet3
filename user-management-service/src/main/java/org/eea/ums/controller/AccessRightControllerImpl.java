package org.eea.ums.controller;

import java.util.List;
import org.eea.interfaces.controller.ums.AccessRightController;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
  @DeleteMapping(value = "/deleteRole")
  @Override
  public void deleteRole(@RequestBody List<ResourceInfoVO> resourceInfoVO) {

  }


}
