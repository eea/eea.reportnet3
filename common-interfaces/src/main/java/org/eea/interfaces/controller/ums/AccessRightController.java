package org.eea.interfaces.controller.ums;

import java.util.List;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The Class AccessRightController.
 */
public interface AccessRightController {


  /**
   * The interface Resource management controller zull.
   */
  @FeignClient(value = "ums", contextId = "accessRight", path = "/accessRight")
  interface AccessRightControllerZull extends AccessRightController {

  }


  /**
   * Delete resource.
   *
   * @param resourceInfoVO the resource info vo
   */
  @DeleteMapping(value = "/deleteRole")
  void deleteRole(@RequestBody List<ResourceInfoVO> resourceInfoVO);
}
