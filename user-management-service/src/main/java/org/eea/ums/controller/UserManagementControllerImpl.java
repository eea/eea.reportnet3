package org.eea.ums.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.ums.service.BackupManagmentService;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
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

  /** The backup managment controler service. */
  @Autowired
  private BackupManagmentService backupManagmentControlerService;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

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
    // Recover user id from Security context
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
      @RequestParam("resourceType") ResourceTypeEnum resourceType) {
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
      @RequestParam("resourceType") ResourceTypeEnum resourceType,
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
   * @param idResource the resourceId resource
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
   * @param dataflowId the dataflow resourceId
   *
   * @return the string
   */
  @RequestMapping(value = "/test-security", method = RequestMethod.GET)
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_REQUESTER','DATAFLOW_PROVIDER') AND checkPermission('Dataflow','READ')")
  public String testSecuredService(@RequestParam("dataflowId") Long dataflowId) {
    return "OLEEEEE";
  }

  /**
   * Sets the users.
   *
   * @param file the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/createUsers", method = RequestMethod.POST)
  public void createUsers(@RequestParam("File") MultipartFile file) throws IOException {
    InputStream is = file.getInputStream();
    backupManagmentControlerService.readExcelDatatoKeyCloack(is);

  }


}
