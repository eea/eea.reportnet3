package org.eea.interfaces.controller.ums;

import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The interface Resource management controller.
 */
public interface ResourceManagementController {

  /**
   * The interface Resource management controller zull.
   */
  @FeignClient(value = "ums", contextId = "resource", path = "/resource")
  interface ResourceManagementControllerZull extends ResourceManagementController {

  }

  /**
   * Create resource.
   *
   * @param resourceInfoVO the resource info vo
   */
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  void createResource(@RequestBody ResourceInfoVO resourceInfoVO);

  /**
   * Gets resource detail.
   *
   * @param idResource the resourceId resource
   * @param resourceGroupEnum the resource group enum
   *
   * @return the resource detail
   */
  @GetMapping("/details")
  ResourceInfoVO getResourceDetail(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum);
}
