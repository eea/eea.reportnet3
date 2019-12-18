package org.eea.ums.controller;

import java.util.List;
import org.eea.interfaces.controller.ums.ResourceManagementController;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The type Resource management controller implementation.
 */
@RestController
@RequestMapping(value = "/resource")
public class ResourceManagementControllerImpl implements ResourceManagementController {

  /** The security provider interface service. */
  @Autowired
  private SecurityProviderInterfaceService securityProviderInterfaceService;

  /**
   * Creates the resource.
   *
   * @param resourceInfoVO the resource info VO
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  public void createResource(@RequestBody ResourceInfoVO resourceInfoVO) {
    securityProviderInterfaceService.createResourceInstance(resourceInfoVO);
  }

  /**
   * Delete resource.
   *
   * @param resourceInfoVO the resource info VO
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.OK)
  public void deleteResource(@RequestBody List<ResourceInfoVO> resourceInfoVO) {
    securityProviderInterfaceService.deleteResourceInstances(resourceInfoVO);
  }

  /**
   * Delete resource by name.
   *
   * @param resourceName the resource name
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/delete_by_name", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.OK)
  public void deleteResourceByName(@RequestParam("resourceNames") List<String> resourceName) {
    securityProviderInterfaceService.deleteResourceInstancesByName(resourceName);
  }

  /**
   * Gets the resource detail.
   *
   * @param idResource the id resource
   * @param resourceGroupEnum the resource group enum
   * @return the resource detail
   */
  @Override
  @HystrixCommand
  @GetMapping("/details")
  public ResourceInfoVO getResourceDetail(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum) {
    return securityProviderInterfaceService
        .getResourceDetails(resourceGroupEnum.getGroupName(idResource));
  }


  /**
   * Gets the groups by id resource type.
   *
   * @param idResource the id resource
   * @param resourceType the resource type
   * @return the groups by id resource type
   */
  @Override
  @HystrixCommand
  @GetMapping("/getResourceInfoVOByResource")
  public List<ResourceInfoVO> getGroupsByIdResourceType(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceType") ResourceTypeEnum resourceType) {
    return securityProviderInterfaceService.getGroupsByIdResourceType(idResource, resourceType);
  }
}
