package org.eea.ums.controller;

import java.util.List;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.ums.ResourceManagementController;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;

/**
 * The type Resource management controller implementation.
 */
@RestController
@RequestMapping(value = "/resource")
@Api(tags = "Resources : Resources Manager")
public class ResourceManagementControllerImpl implements ResourceManagementController {

  /**
   * The Constant LOG_ERROR.
   */
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
  @PreAuthorize("isAuthenticated()")
  @PostMapping(value = "/create")
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(value = "Create a Resource", hidden = true)
  @ApiResponse(code = 500, message = EEAErrorMessage.PERMISSION_NOT_CREATED)
  public void createResource(@ApiParam(type = "Object",
      value = "ResourceInfoVO Object") @RequestBody ResourceInfoVO resourceInfoVO) {
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
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping(value = "/delete")
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(value = "Delete a list of Resources", hidden = true)
  public void deleteResource(@ApiParam(type = "List<Object>",
      value = "ResourceInfoVO Object List") @RequestBody List<ResourceInfoVO> resourceInfoVO) {
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    securityProviderInterfaceService.deleteResourceInstances(resourceInfoVO);
  }

  /**
   * Delete resource by name.
   *
   * @param resourceName the resource name
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping(value = "/delete_by_name")
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(value = "Delete a list of Resources by their Names", hidden = true)
  public void deleteResourceByName(@ApiParam(type = "List<String>",
      value = "Resource name String List ") @RequestParam("resourceNames") List<String> resourceName) {
    securityProviderInterfaceService.deleteResourceInstancesByName(resourceName);
  }

  /**
   * Delete resource by dataset id.
   *
   * @param datasetIds the dataset ids
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/delete_by_dataset_id")
  @ApiOperation(value = "Delete a Resource its Dataset Id", hidden = true)
  public void deleteResourceByDatasetId(@ApiParam(type = "Object",
      value = "Dataset ids Long list") @RequestParam("datasetIds") List<Long> datasetIds) {
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
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/details")
  @ApiOperation(value = "Get a Resource details", hidden = true)
  public ResourceInfoVO getResourceDetail(
      @ApiParam(value = "Resource id", example = "0") @RequestParam("idResource") Long idResource,
      @ApiParam(type = "Object",
          value = "Resource group enum") @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum) {
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
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/getResourceInfoVOByResource")
  @ApiOperation(value = "Get Resources by their Type", response = ResourceInfoVO.class,
      responseContainer = "List", hidden = true)
  public List<ResourceInfoVO> getGroupsByIdResourceType(
      @ApiParam(value = "Resource id", example = "0") @RequestParam("idResource") Long idResource,
      @ApiParam(type = "Object",
          value = "Resource type enum") @RequestParam("resourceType") ResourceTypeEnum resourceType) {
    return securityProviderInterfaceService.getGroupsByIdResourceType(idResource, resourceType);
  }


  /**
   * Creates the resources.
   *
   * @param resourceInfoVOs the resource info V os
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @PostMapping(value = "/createList")
  @ResponseStatus(HttpStatus.CREATED)
  @ApiOperation(value = "Create Resources at same time", hidden = true)
  @ApiResponse(code = 500, message = EEAErrorMessage.PERMISSION_NOT_CREATED)
  public void createResources(@ApiParam(type = "List<Objects>",
      value = "ResourceInfoVOs List objects") @RequestBody List<ResourceInfoVO> resourceInfoVOs) {
    try {
      securityProviderInterfaceService.createResourceInstance(resourceInfoVOs);
    } catch (EEAException e) {
      LOG_ERROR.error("Error creating resources due to reason {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.PERMISSION_NOT_CREATED, e);
    }
  }
}
