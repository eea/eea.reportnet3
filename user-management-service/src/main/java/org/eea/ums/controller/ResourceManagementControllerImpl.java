package org.eea.ums.controller;

import java.util.List;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.ums.ResourceManagementController;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The type Resource management controller implementation.
 */
@RestController
@RequestMapping(value = "/resource")
public class ResourceManagementControllerImpl implements ResourceManagementController {

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The security provider interface service.
   */
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
    try {
      securityProviderInterfaceService.createResourceInstance(resourceInfoVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Error creating resource {} due to reason {}", resourceInfoVO.getName(),
          e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.PERMISSION_NOT_CREATED, e);
    }
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
   * Delete resource by dataset id.
   *
   * @param datasetIds the dataset ids
   */
  @Override
  @HystrixCommand
  @DeleteMapping("/delete_by_dataset_id")
  public void deleteResourceByDatasetId(@RequestParam("datasetIds") List<Long> datasetIds) {
    securityProviderInterfaceService.deleteResourceInstancesByDatasetId(datasetIds);
  }

  /**
   * Gets the resource detail.
   *
   * @param idResource the id resource
   * @param resourceGroupEnum the resource group enum
   *
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
   *
   * @return the groups by id resource type
   */
  @Override
  @HystrixCommand
  @GetMapping("/getResourceInfoVOByResource")
  public List<ResourceInfoVO> getGroupsByIdResourceType(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceType") ResourceTypeEnum resourceType) {
    return securityProviderInterfaceService.getGroupsByIdResourceType(idResource, resourceType);
  }


  /**
   * Creates the resources.
   *
   * @param resourceInfoVOs the resource info V os
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/createList", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  public void createResources(@RequestBody List<ResourceInfoVO> resourceInfoVOs) {
    try {
      securityProviderInterfaceService.createResourceInstance(resourceInfoVOs);
    } catch (EEAException e) {
      LOG_ERROR.error("Error creating resources due to reason {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.PERMISSION_NOT_CREATED, e);
    }
  }
}
