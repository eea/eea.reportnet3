package org.eea.ums.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceEnum;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The type User management controller.
 */
@RestController
@RequestMapping(value = "/user")
public class UserManagementControllerImpl implements UserManagementController {

  /**
   * The security provider interface service.
   */
  @Autowired
  private SecurityProviderInterfaceService securityProviderInterfaceService;

  /**
   * Generate token.
   *
   * @param username the username
   * @param password the password
   *
   * @return the token VO
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/generateToken", method = RequestMethod.POST)
  public TokenVO generateToken(@RequestParam("username") String username,
      @RequestParam("password") String password) {
    return securityProviderInterfaceService.doLogin(username, password);
  }

  /**
   * Generate token based on authorization code.
   *
   * @param code the code
   *
   * @return the token VO
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/generateTokenByCode", method = RequestMethod.POST)
  public TokenVO generateToken(@RequestParam("code") String code) {
    return securityProviderInterfaceService.doLogin(code);
  }

  /**
   * Refresh token.
   *
   * @param refreshToken the refresh token
   *
   * @return the token VO
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/refreshToken", method = RequestMethod.POST)
  public TokenVO refreshToken(@RequestParam("refreshToken") String refreshToken) {
    return securityProviderInterfaceService.refreshToken(refreshToken);
  }

  /**
   * Check resource access permission.
   *
   * @param resource the resource
   * @param scopes the scopes
   *
   * @return the boolean
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/checkAccess", method = RequestMethod.GET)
  public Boolean checkResourceAccessPermission(@RequestParam("resource") String resource,
      @RequestParam("scopes") AccessScopeEnum[] scopes) {
    return securityProviderInterfaceService.checkAccessPermission(resource, scopes);
  }

  /**
   * Gets the resources by user.
   *
   * @return the resources by user
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/resources", method = RequestMethod.GET)
  public List<ResourceAccessVO> getResourcesByUser() {
    Map<String, String> details =
        (Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails();
    String userId = "";
    if (null != details && details.size() > 0) {
      userId = details.get("userId");
    }
    return securityProviderInterfaceService.getResourcesByUser(userId);
  }

  /**
   * Gets the resources by user.
   *
   * @param resourceType the resource type
   *
   * @return the resources by user
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/resources_by_type", method = RequestMethod.GET)
  public List<ResourceAccessVO> getResourcesByUser(
      @RequestParam("resourceType") ResourceEnum resourceType) {
    return getResourcesByUser().stream()
        .filter(resource -> resource.getResource().equals(resourceType))
        .collect(Collectors.toList());
  }

  /**
   * Gets the resources by user.
   *
   * @param securityRole the security role
   *
   * @return the resources by user
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/resources_by_role", method = RequestMethod.GET)
  public List<ResourceAccessVO> getResourcesByUser(
      @RequestParam("securityRole") SecurityRoleEnum securityRole) {
    return getResourcesByUser().stream().filter(resource -> resource.getRole().equals(securityRole))
        .collect(Collectors.toList());
  }

  /**
   * Gets the resources by user.
   *
   * @param resourceType the resource type
   * @param securityRole the security role
   *
   * @return the resources by user
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/resources_by_type_role", method = RequestMethod.GET)
  public List<ResourceAccessVO> getResourcesByUser(
      @RequestParam("resourceType") ResourceEnum resourceType,
      @RequestParam("securityRole") SecurityRoleEnum securityRole) {
    return getResourcesByUser().stream().filter(resource -> resource.getRole().equals(securityRole)
        && resource.getResource().equals(resourceType)).collect(Collectors.toList());
  }

  /**
   * Do log out.
   *
   * @param refreshToken the refresh token
   */
  @Override
  @RequestMapping(value = "/logout", method = RequestMethod.POST)
  public void doLogOut(@RequestParam("refreshToken") String refreshToken) {
    securityProviderInterfaceService.doLogout(refreshToken);
  }

  /**
   * Adds the contributor to resource.
   *
   * @param idResource the id resource
   * @param resourceGroupEnum the resource group enum
   */
  @Override
  @RequestMapping(value = "/add_contributtor_to_resource", method = RequestMethod.PUT)
  public void addContributorToResource(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum) {
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get("userId");
    securityProviderInterfaceService.addUserToUserGroup(userId,
        resourceGroupEnum.getGroupName(idResource));
  }

  /**
   * Test secured service.
   *
   * @param dataflowId the dataflow id
   *
   * @return the string
   */
  @RequestMapping(value = "/test-security", method = RequestMethod.GET)
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_REQUESTOR','DATAFLOW_PROVIDER') AND checkPermission('Dataflow','READ')")
  public String testSecuredService(@RequestParam("dataflowId") Long dataflowId) {
    return "OLEEEEE";
  }

  @Override
  @HystrixCommand
  @GetMapping("/resource/details")
  public ResourceInfoVO getResourceDetail(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum) {
    return securityProviderInterfaceService
        .getGroupDetail(resourceGroupEnum.getGroupName(idResource));
  }
}
