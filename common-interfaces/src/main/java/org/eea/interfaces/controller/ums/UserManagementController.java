package org.eea.interfaces.controller.ums;

import java.io.IOException;
import java.util.List;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
   * Generate token token vo based on authorization code.
   *
   * @param code the code
   *
   * @return the token vo
   */
  @RequestMapping(value = "/generateTokenByCode", method = RequestMethod.POST)
  TokenVO generateToken(@RequestParam("code") String code);

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
      @RequestParam("resourceType") ResourceTypeEnum resourceType);

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
  List<ResourceAccessVO> getResourcesByUser(
      @RequestParam("resourceType") ResourceTypeEnum resourceType,
      @RequestParam("securityRole") SecurityRoleEnum securityRole);

  /**
   * Do log out invalidating the user session.
   *
   * @param refreshToken the refresh token
   */
  @RequestMapping(value = "/logout", method = RequestMethod.POST)
  void doLogOut(@RequestParam("refreshToken") String refreshToken);

  /**
   * Add invoking user to a resource. User must be authenticated
   *
   * @param idResource the id resource
   * @param resourceGroupEnum the resource group enum
   */
  @RequestMapping(value = "/add_user_to_resource", method = RequestMethod.PUT)
  void addUserToResource(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum);

  /**
   * Sets the users.
   *
   * @param file the file
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @RequestMapping(value = "/createUsers", method = RequestMethod.POST)
  void createUsers(@RequestParam("file") MultipartFile file) throws IOException;


  /**
   * Gets the users.
   *
   * @return the users
   */
  @RequestMapping(value = "/getUsers", method = RequestMethod.GET)
  List<UserRepresentationVO> getUsers();

  /**
   * Add a contributor to resource.
   *
   * @param idResource the id resource
   * @param resourceGroupEnum the resource group enum
   * @param userMail the user mail
   */
  @RequestMapping(value = "/add_contributor_to_resource", method = RequestMethod.PUT)
  void addContributorToResource(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum,
      @RequestParam("userMail") String userMail);


  /**
   * Adds the contributors to resources.
   *
   * @param resources the resources
   */
  @RequestMapping(value = "/add_contributors_to_resources", method = RequestMethod.PUT)
  void addContributorsToResources(@RequestBody List<ResourceAssignationVO> resources);

  /**
   * Adds the user to resources.
   *
   * @param resources the resources
   */
  @RequestMapping(value = "/add_user_to_resources", method = RequestMethod.PUT)
  void addUserToResources(@RequestBody List<ResourceAssignationVO> resources);


}
