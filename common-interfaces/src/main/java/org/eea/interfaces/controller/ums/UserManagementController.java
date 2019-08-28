package org.eea.interfaces.controller.ums;

import java.util.List;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The interface User management controller.
 */
public interface UserManagementController {

  /**
   * The interface User management controller zull.
   */
  @FeignClient(value = "ums", path = "/user")
  interface UserManagementControllerZull extends UserManagementController {

  }

  /**
   * Generate token string.
   *
   * @param username the username
   * @param password the password
   *
   * @return the string
   */
  @RequestMapping(value = "/generateToken", method = RequestMethod.POST)
  TokenVO generateToken(@RequestParam("username") String username,
      @RequestParam("password") String password);

  /**
   * Refresh token token vo.
   *
   * @param refreshToken the refresh token
   *
   * @return the token vo
   */
  @RequestMapping(value = "/refreshToken", method = RequestMethod.POST)
  TokenVO refreshToken(@RequestParam("refreshToken") String refreshToken);

  /**
   * Check resource access permission boolean.
   *
   * @param resource the resource
   * @param scopes the scopes
   *
   * @return the boolean
   */
  @RequestMapping(value = "/checkAccess", method = RequestMethod.GET)
  Boolean checkResourceAccessPermission(@RequestParam("resource") String resource,
      @RequestParam("scopes") AccessScopeEnum[] scopes);

  /**
   * Gets resources by user.
   *
   * @return the resources by user
   */
  @RequestMapping(value = "/resources", method = RequestMethod.GET)
  List<ResourceAccessVO> getResourcesByUser();

  /**
   * Gets resources by user.
   *
   * @param resourceType the resource type
   *
   * @return the resources by user
   */
  @RequestMapping(value = "/resources_by_type", method = RequestMethod.GET)
  List<ResourceAccessVO> getResourcesByUser(
      @RequestParam("resourceType") ResourceEnum resourceType);

  /**
   * Gets resources by user.
   *
   * @param securityRole the security role
   *
   * @return the resources by user
   */
  @RequestMapping(value = "/resources_by_role", method = RequestMethod.GET)
  List<ResourceAccessVO> getResourcesByUser(
      @RequestParam("securityRole") SecurityRoleEnum securityRole);

  /**
   * Gets resources by user.
   *
   * @param resourceType the resource type
   * @param securityRole the security role
   *
   * @return the resources by user
   */
  @RequestMapping(value = "/resources_by_type_role", method = RequestMethod.GET)
  List<ResourceAccessVO> getResourcesByUser(@RequestParam("resourceType") ResourceEnum resourceType,
      @RequestParam("securityRole") SecurityRoleEnum securityRole);

  /**
   * Do log out invalidating the user session.
   *
   * @param refreshToken the refresh token
   */
  @RequestMapping(value = "/logout", method = RequestMethod.POST)
  void doLogOut(@RequestParam("refreshToken") String refreshToken);
}
