package org.eea.interfaces.controller.ums;

import java.util.List;
import java.util.Map;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.UserRoleVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * The Interface UserManagementController.
 */
public interface UserManagementController {

  /**
   * The Interface UserManagementControllerZull.
   */
  @FeignClient(value = "ums", path = "/user")
  interface UserManagementControllerZull extends UserManagementController {

  }

  /**
   * Generate token.
   *
   * @param username the username
   * @param password the password
   *
   * @return the token VO
   */
  @PostMapping("/generateToken")
  TokenVO generateToken(@RequestParam("username") String username,
      @RequestParam("password") String password);

  /**
   * Generate token.
   *
   * @param code the code
   *
   * @return the token VO
   */
  @PostMapping("/generateTokenByCode")
  TokenVO generateToken(@RequestParam("code") String code);

  /**
   * Refresh token.
   *
   * @param refreshToken the refresh token
   *
   * @return the token VO
   */
  @PostMapping("/refreshToken")
  TokenVO refreshToken(@RequestParam("refreshToken") String refreshToken);

  /**
   * Check resource access permission.
   *
   * @param resource the resource
   * @param scopes the scopes
   *
   * @return the boolean
   */
  @GetMapping("/checkAccess")
  Boolean checkResourceAccessPermission(@RequestParam("resource") String resource,
      @RequestParam("scopes") AccessScopeEnum[] scopes);

  /**
   * Gets the resources by user.
   *
   * @return the resources by user
   */
  @GetMapping("/resources")
  List<ResourceAccessVO> getResourcesByUser();

  /**
   * Gets the resources by user.
   *
   * @param resourceType the resource type
   *
   * @return the resources by user
   */
  @GetMapping("/resources_by_type")
  List<ResourceAccessVO> getResourcesByUser(
      @RequestParam("resourceType") ResourceTypeEnum resourceType);

  /**
   * Gets the resources by user.
   *
   * @param securityRole the security role
   *
   * @return the resources by user
   */
  @GetMapping("/resources_by_role")
  List<ResourceAccessVO> getResourcesByUser(
      @RequestParam("securityRole") SecurityRoleEnum securityRole);

  /**
   * Gets the resources by user.
   *
   * @param resourceType the resource type
   * @param securityRole the security role
   *
   * @return the resources by user
   */
  @GetMapping("/resources_by_type_role")
  List<ResourceAccessVO> getResourcesByUser(
      @RequestParam("resourceType") ResourceTypeEnum resourceType,
      @RequestParam("securityRole") SecurityRoleEnum securityRole);

  /**
   * Do log out.
   *
   * @param refreshToken the refresh token
   */
  @PostMapping("/logout")
  void doLogOut(@RequestParam("refreshToken") String refreshToken);

  /**
   * Adds the user to resource.
   *
   * @param idResource the id resource
   * @param resourceGroupEnum the resource group enum
   */
  @PutMapping("/add_user_to_resource")
  void addUserToResource(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum);

  /**
   * Creates the users.
   *
   * @param file the file
   */
  @PostMapping("/createUsers")
  void createUsers(@RequestBody MultipartFile file);

  /**
   * Gets the users.
   *
   * @return the users
   */
  @GetMapping("/getUsers")
  List<UserRepresentationVO> getUsers();

  /**
   * Gets the user by email.
   *
   * @param email the email
   *
   * @return the user by email
   */
  @GetMapping("/getUserByEmail")
  UserRepresentationVO getUserByEmail(@RequestParam("email") String email);

  /**
   * Adds the contributor to resource.
   *
   * @param idResource the id resource
   * @param resourceGroupEnum the resource group enum
   * @param userMail the user mail
   */
  @PutMapping("/add_contributor_to_resource")
  void addContributorToResource(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum,
      @RequestParam("userMail") String userMail);

  /**
   * Adds the contributors to resources.
   *
   * @param resources the resources
   */
  @PutMapping("/add_contributors_to_resources")
  void addContributorsToResources(@RequestBody List<ResourceAssignationVO> resources);

  /**
   * Adds the user to resources.
   *
   * @param resources the resources
   */
  @PutMapping("/add_user_to_resources")
  void addUserToResources(@RequestBody List<ResourceAssignationVO> resources);

  /**
   * Update user attributes.
   *
   * @param attributes the attributes
   */
  @PutMapping("/updateAttributes")
  void updateUserAttributes(@RequestBody Map<String, List<String>> attributes);

  /**
   * Gets the user attributes.
   *
   * @return the user attributes
   */
  @GetMapping("/getAttributes")
  Map<String, List<String>> getUserAttributes();

  /**
   * Gets the user by user id.
   *
   * @param userId the user id
   *
   * @return the user by user id
   */
  @GetMapping("/getUserByUserId")
  UserRepresentationVO getUserByUserId(@RequestParam("userId") String userId);

  /**
   * Creates the api key.
   *
   * @param dataflowId the dataflow id
   * @param dataProvider the data provider
   *
   * @return the string
   */
  @PostMapping("/createApiKey")
  String createApiKey(@RequestParam("dataflowId") Long dataflowId,
      @RequestParam(value = "dataProvider", required = false) Long dataProvider);

  /**
   * Gets the api key.
   *
   * @param dataflowId the dataflow id
   * @param dataProvider the data provider
   *
   * @return the api key
   */
  @GetMapping("/getApiKey")
  String getApiKey(@RequestParam("dataflowId") Long dataflowId,
      @RequestParam("dataProvider") Long dataProvider);

  /**
   * Gets the api key.
   *
   * @param userId the user id
   * @param dataflowId the dataflow id
   * @param dataProvider the data provider
   *
   * @return the api key
   */
  @GetMapping("/{userId}/getApiKey")
  String getApiKey(@PathVariable("userId") String userId,
      @RequestParam("dataflowId") Long dataflowId, @RequestParam("dataProvider") Long dataProvider);

  /**
   * Authenticate user by api key.
   *
   * @param apiKey the api key
   *
   * @return the token VO
   */
  @PostMapping("/authenticateByApiKey/{apiKey}")
  TokenVO authenticateUserByApiKey(@PathVariable("apiKey") String apiKey);


  /**
   * Authenticate user by email token vo.
   *
   * @param email the email
   *
   * @return the token vo
   */
  @PostMapping("/authenticateByEmail")
  TokenVO authenticateUserByEmail(@RequestParam("email") String email);

  /**
   * Gets the users by group.
   *
   * @param group the group
   *
   * @return the user by group
   */
  @GetMapping("/getUsersByGroup/{group}")
  List<UserRepresentationVO> getUsersByGroup(@PathVariable("group") String group);

  /**
   * Removes the contributor from resource.
   *
   * @param idResource the id resource
   * @param resourceGroupEnum the resource group enum
   * @param userMail the user mail
   */
  @DeleteMapping("/remove_contributor_from_resource")
  void removeContributorFromResource(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum,
      @RequestParam("userMail") String userMail);

  /**
   * Removes the contributors from resources.
   *
   * @param resources the resources
   */
  @DeleteMapping("/remove_contributors_from_resources")
  void removeContributorsFromResources(@RequestBody List<ResourceAssignationVO> resources);

  /**
   * Removes the user from resources.
   *
   * @param resources the resources
   */
  @DeleteMapping("/remove_user_from_resources")
  void removeUserFromResources(@RequestBody List<ResourceAssignationVO> resources);

  /**
   * Gets the resources by user email.
   *
   * @param email the email
   *
   * @return the resources by user email
   */
  @GetMapping("/private/resourcesByMail")
  List<ResourceAccessVO> getResourcesByUserEmail(@RequestParam("email") String email);


  /**
   * Gets the user roles by dataflow and country.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the user roles by dataflow and country
   */
  @GetMapping("/getUserRolesByDataflow/{dataflowId}/dataProviderId/{dataProviderId}")
  List<UserRoleVO> getUserRolesByDataflowAndCountry(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataProviderId") Long dataProviderId);

  /**
   * Gets the user roles by dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the user roles by dataflow
   */
  @GetMapping("/userRoles/dataflow/{dataflowId}")
  List<UserRoleVO> getUserRolesByDataflow(Long dataflowId);



}
