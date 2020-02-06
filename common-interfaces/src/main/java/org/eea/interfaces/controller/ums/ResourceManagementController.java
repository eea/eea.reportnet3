package org.eea.interfaces.controller.ums;

import java.util.List;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

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
   * Delete resource.
   *
   * @param resourceInfoVO the resource info vo
   */
  @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.OK)
  void deleteResource(@RequestBody List<ResourceInfoVO> resourceInfoVO);


  /**
   * Delete resource by name.
   *
   * @param resourceNames the resource names
   */
  @RequestMapping(value = "/delete_by_name", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.OK)
  void deleteResourceByName(@RequestParam("resourceNames") List<String> resourceNames);

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



  /**
   * Gets the groups by id resource type.
   *
   * @param idResource the id resource
   * @param resourceType the resource type
   * @return the groups by id resource type
   */
  @GetMapping("/getResourceInfoVOByResource")
  List<ResourceInfoVO> getGroupsByIdResourceType(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceType") ResourceTypeEnum resourceType);


  /**
   * Creates the resources.
   *
   * @param resourceInfoVOs the resource info V os
   */
  @RequestMapping(value = "/createList", method = RequestMethod.POST)
  void createResources(@RequestBody List<ResourceInfoVO> resourceInfoVOs);
}
