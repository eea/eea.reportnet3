package org.eea.ums.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type User management controller.
 */
@RestController
@RequestMapping(value = "/user")
public class UserManagementControllerImpl implements UserManagementController {

  @Autowired
  private SecurityProviderInterfaceService securityProviderInterfaceService;

  @Override
  @HystrixCommand
  @RequestMapping(value = "/generateToken", method = RequestMethod.POST)
  public TokenVO generateToken(@RequestParam("username") String username,
      @RequestParam("password") String password) {
    return securityProviderInterfaceService.doLogin(username, password);
  }

  @Override
  @HystrixCommand
  @RequestMapping(value = "/refreshToken", method = RequestMethod.POST)
  public TokenVO refreshToken(@RequestParam("refreshToken") String refreshToken) {
    return securityProviderInterfaceService.refreshToken(refreshToken);
  }

  @Override
  @HystrixCommand
  @RequestMapping(value = "/checkAccess", method = RequestMethod.GET)
  public Boolean checkResourceAccessPermission(@RequestParam("resource") String resource,
      @RequestParam("scopes") AccessScopeEnum[] scopes) {
    return securityProviderInterfaceService.checkAccessPermission(resource, scopes);
  }

  @Override
  @HystrixCommand
  @RequestMapping(value = "/resources", method = RequestMethod.GET)
  public List<ResourceAccessVO> getResourcesByUser() {
    Map<String, String> details = (Map<String, String>) SecurityContextHolder.getContext()
        .getAuthentication().getDetails();
    String userId = "";
    if (null != details && details.size() > 0) {
      userId = details.get("userId");
    }
    return securityProviderInterfaceService.getResourcesByUser(userId);
  }

  @Override
  @HystrixCommand
  @RequestMapping(value = "/resources_by_type", method = RequestMethod.GET)
  public List<ResourceAccessVO> getResourcesByUser(
      @RequestParam("resourceType") ResourceEnum resourceType) {
    return getResourcesByUser().stream()
        .filter(resource -> resource.getResource().equals(resourceType)).collect(
            Collectors.toList());
  }

  @Override
  @HystrixCommand
  @RequestMapping(value = "/resources_by_role", method = RequestMethod.GET)
  public List<ResourceAccessVO> getResourcesByUser(
      @RequestParam("securityRole") SecurityRoleEnum securityRole) {
    return getResourcesByUser().stream().filter(resource -> resource.getRole().equals(securityRole))
        .collect(
            Collectors.toList());
  }

  @Override
  @HystrixCommand
  @RequestMapping(value = "/resources_by_type_role", method = RequestMethod.GET)
  public List<ResourceAccessVO> getResourcesByUser(
      @RequestParam("resourceType") ResourceEnum resourceType,
      @RequestParam("securityRole") SecurityRoleEnum securityRole) {
    return getResourcesByUser().stream().filter(
        resource -> resource.getRole().equals(securityRole) && resource.getResource()
            .equals(resourceType)).collect(
        Collectors.toList());
  }

  @RequestMapping(value = "/test-security", method = RequestMethod.GET)
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_REQUESTOR','DATAFLOW_PROVIDER') AND checkPermission('Dataflow','READ')")
  public String testSecuredService(@RequestParam("dataflowId") Long dataflowId) {
    return "OLEEEEE";
  }
}
