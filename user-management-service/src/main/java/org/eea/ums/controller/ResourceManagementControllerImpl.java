package org.eea.ums.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import java.util.List;
import org.eea.interfaces.controller.ums.ResourceManagementController;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
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

/**
 * The type Resource management controller implementation.
 */
@RestController
@RequestMapping(value = "/resource")
public class ResourceManagementControllerImpl implements ResourceManagementController {

  @Autowired
  private SecurityProviderInterfaceService securityProviderInterfaceService;

  @Override
  @HystrixCommand
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  public void createResource(@RequestBody ResourceInfoVO resourceInfoVO) {
    securityProviderInterfaceService.createResourceInstance(resourceInfoVO);
  }

  @Override
  @HystrixCommand
  @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.OK)
  public void deleteResource(@RequestBody List<ResourceInfoVO> resourceInfoVO) {
    securityProviderInterfaceService.deleteResourceInstances(resourceInfoVO);
  }

  @Override
  @HystrixCommand
  @RequestMapping(value = "/delete/", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.OK)
  public void deleteResourceByName(@RequestParam("resourceNames") List<String> resourceName) {
    securityProviderInterfaceService.deleteResourceInstancesByName(resourceName);
  }

  @Override
  @HystrixCommand
  @GetMapping("/details")
  public ResourceInfoVO getResourceDetail(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum) {
    return securityProviderInterfaceService
        .getResourceDetails(resourceGroupEnum.getGroupName(idResource));
  }
}
