package org.eea.ums.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.eea.ums.mapper.UserRepresentationMapper;
import org.eea.ums.service.BackupManagmentService;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class UserManagementControllerImpl.
 */
@RestController
@RequestMapping("/user")
public class UserManagementControllerImpl implements UserManagementController {

  /** The security provider interface service. */
  @Autowired
  private SecurityProviderInterfaceService securityProviderInterfaceService;

  /** The backup managment controler service. */
  @Autowired
  private BackupManagmentService backupManagmentControlerService;

  /** The keycloak connector service. */
  @Autowired
  private KeycloakConnectorService keycloakConnectorService;

  /** The user representation mapper. */
  @Autowired
  private UserRepresentationMapper userRepresentationMapper;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Generate token.
   *
   * @param username the username
   * @param password the password
   * @return the token VO
   */
  @Override
  @HystrixCommand
  @PostMapping("/generateToken")
  public TokenVO generateToken(@RequestParam("username") String username,
      @RequestParam("password") String password) {
    return securityProviderInterfaceService.doLogin(username, password, false);
  }

  /**
   * Generate token.
   *
   * @param code the code
   * @return the token VO
   */
  @Override
  @HystrixCommand
  @PostMapping("/generateTokenByCode")
  public TokenVO generateToken(@RequestParam("code") String code) {
    return securityProviderInterfaceService.doLogin(code);
  }

  /**
   * Refresh token.
   *
   * @param refreshToken the refresh token
   * @return the token VO
   */
  @Override
  @HystrixCommand
  @PostMapping("/refreshToken")
  public TokenVO refreshToken(@RequestParam("refreshToken") String refreshToken) {
    return securityProviderInterfaceService.refreshToken(refreshToken);
  }

  /**
   * Check resource access permission.
   *
   * @param resource the resource
   * @param scopes the scopes
   * @return the boolean
   */
  @Override
  @HystrixCommand
  @GetMapping("/checkAccess")
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
  @GetMapping("/resources")
  public List<ResourceAccessVO> getResourcesByUser() {
    // Recover user id from Security context
    Map<String, String> details =
        (Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails();
    String userId = "";
    if (null != details && !details.isEmpty()) {
      userId = details.get(AuthenticationDetails.USER_ID);
    }
    return securityProviderInterfaceService.getResourcesByUser(userId);
  }

  /**
   * Gets the resources by user.
   *
   * @param resourceType the resource type
   * @return the resources by user
   */
  @Override
  @HystrixCommand
  @GetMapping("/resources_by_type")
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
   * @return the resources by user
   */
  @Override
  @HystrixCommand
  @GetMapping("/resources_by_role")
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
   * @return the resources by user
   */
  @Override
  @HystrixCommand
  @GetMapping("/resources_by_type_role")
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
  @PostMapping("/logout")
  public void doLogOut(@RequestParam("refreshToken") String refreshToken) {
    securityProviderInterfaceService.doLogout(refreshToken);
  }

  /**
   * Adds the user to resource.
   *
   * @param idResource the id resource
   * @param resourceGroupEnum the resource group enum
   */
  @Override
  @PutMapping("/add_user_to_resource")
  public void addUserToResource(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum) {
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);
    try {
      securityProviderInterfaceService.addUserToUserGroup(userId,
          resourceGroupEnum.getGroupName(idResource));
    } catch (EEAException e) {
      LOG_ERROR.error("Error adding user to resource. Message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.PERMISSION_NOT_CREATED);
    }
  }

  /**
   * Test secured service.
   *
   * @param dataflowId the dataflow id
   * @param provider the provider
   * @return the string
   */
  @HystrixCommand
  @PreAuthorize("checkApiKey(#dataflowId,#provider) AND secondLevelAuthorize(#dataflowId,'DATAFLOW_REQUESTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_LEAD_REPORTER')")
  @GetMapping("/test-security")
  public String testSecuredService(@RequestParam("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long provider) {
    return "OLEEEEE";
  }

  /**
   * Creates the users.
   *
   * @param file the file
   */
  @Override
  @HystrixCommand
  @PostMapping("/createUsers")
  public void createUsers(@RequestBody MultipartFile file) {
    try {
      backupManagmentControlerService.readAndSaveUsers(file.getInputStream());
    } catch (IOException e) {
      LOG_ERROR.error("Error creating users", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Gets the users.
   *
   * @return the users
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/getUsers")
  public List<UserRepresentationVO> getUsers() {
    UserRepresentation[] a = keycloakConnectorService.getUsers();
    ArrayList<UserRepresentation> arrayList = new ArrayList<>(Arrays.asList(a));
    return userRepresentationMapper.entityListToClass(arrayList);
  }

  /**
   * Gets the user by email.
   *
   * @param email the email
   * @return the user by email
   */
  @Override
  @HystrixCommand
  @GetMapping("/getUserByEmail")
  public UserRepresentationVO getUserByEmail(@RequestParam("email") String email) {
    UserRepresentationVO user = null;
    UserRepresentation[] users = keycloakConnectorService.getUsersByEmail(email);
    if (users != null && users.length == 1) {
      user = userRepresentationMapper.entityToClass(users[0]);
    }
    return user;
  }

  /**
   * Gets the user by user id.
   *
   * @param userId the user id
   * @return the user by user id
   */
  @Override
  @HystrixCommand
  @GetMapping("/getUserByUserId")
  public UserRepresentationVO getUserByUserId(@RequestParam("userId") String userId) {
    UserRepresentationVO userVO = null;
    UserRepresentation user = keycloakConnectorService.getUser(userId);
    if (user != null) {
      userVO = userRepresentationMapper.entityToClass(user);
    }
    return userVO;
  }

  /**
   * Update user attributes.
   *
   * @param attributes the attributes
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/updateAttributes")
  public void updateUserAttributes(@RequestBody Map<String, List<String>> attributes) {

    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);

    UserRepresentation user = keycloakConnectorService.getUser(userId);
    if (user != null) {
      user = securityProviderInterfaceService.setAttributesWithApiKey(user, attributes);
    } else {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          String.format(EEAErrorMessage.USER_NOTFOUND, userId));
    }
    keycloakConnectorService.updateUser(user);
  }

  /**
   * Gets the user attributes.
   *
   * @return the user attributes
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/getAttributes")
  public Map<String, List<String>> getUserAttributes() {

    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);

    UserRepresentation user = securityProviderInterfaceService.getUserWithoutKeys(userId);
    if (user != null) {
      return user.getAttributes();
    } else {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          String.format(EEAErrorMessage.USER_NOTFOUND, userId));
    }
  }

  /**
   * Adds the contributor to resource.
   *
   * @param idResource the id resource
   * @param resourceGroupEnum the resource group enum
   * @param userMail the user mail
   */
  @Override
  @PutMapping("/add_contributor_to_resource")
  public void addContributorToResource(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum,
      @RequestParam("userMail") String userMail) {
    try {
      securityProviderInterfaceService.addContributorToUserGroup(null, userMail,
          resourceGroupEnum.getGroupName(idResource));
    } catch (EEAException e) {
      LOG_ERROR.error("Error adding contributor to resource. Message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.PERMISSION_NOT_CREATED);
    }
  }

  /**
   * Adds the contributor from resource.
   *
   * @param idResource the id resource
   * @param resourceGroupEnum the resource group enum
   * @param userMail the user mail
   */
  @Override
  @DeleteMapping("/remove_contributor_from_resource")
  public void removeContributorFromResource(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum,
      @RequestParam("userMail") String userMail) {
    try {
      securityProviderInterfaceService.removeContributorFromUserGroup(null, userMail,
          resourceGroupEnum.getGroupName(idResource));
    } catch (EEAException e) {
      LOG_ERROR.error("Error adding contributor to resource. Message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.PERMISSION_NOT_CREATED);
    }
  }

  /**
   * Adds the contributors to resources.
   *
   * @param resources the resources
   */
  @Override
  @PutMapping("/add_contributors_to_resources")
  public void addContributorsToResources(@RequestBody List<ResourceAssignationVO> resources) {
    try {
      securityProviderInterfaceService.addContributorsToUserGroup(resources);
    } catch (EEAException e) {
      LOG_ERROR.error("Error adding contributor to resource. Message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.PERMISSION_NOT_CREATED);
    }
  }

  /**
   * Adds the contributors from resources.
   *
   * @param resources the resources
   */
  @Override
  @DeleteMapping("/remove_contributors_from_resources")
  public void removeContributorsFromResources(@RequestBody List<ResourceAssignationVO> resources) {
    try {
      securityProviderInterfaceService.removeContributorsFromUserGroup(resources);
    } catch (EEAException e) {
      LOG_ERROR.error("Error removing contributor to resource. Message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.PERMISSION_NOT_CREATED);
    }
  }

  /**
   * Adds the user to resources.
   *
   * @param resources the resources
   */
  @Override
  @PutMapping("/add_user_to_resources")
  public void addUserToResources(@RequestBody List<ResourceAssignationVO> resources) {
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);
    for (ResourceAssignationVO resource : resources) {
      try {
        securityProviderInterfaceService.addUserToUserGroup(userId,
            resource.getResourceGroup().getGroupName(resource.getResourceId()));
      } catch (EEAException e) {
        LOG_ERROR.error("Error adding user to resource. Message: {}", e.getMessage(), e);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
            EEAErrorMessage.PERMISSION_NOT_CREATED);
      }
    }
  }

  /**
   * Adds the user from resources.
   *
   * @param resources the resources
   */
  @Override
  @DeleteMapping("/remove_user_from_resources")
  public void removeUserFromResources(@RequestBody List<ResourceAssignationVO> resources) {
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);
    for (ResourceAssignationVO resource : resources) {
      try {
        securityProviderInterfaceService.removeUserFromUserGroup(userId,
            resource.getResourceGroup().getGroupName(resource.getResourceId()));
      } catch (EEAException e) {
        LOG_ERROR.error("Error removing user to resource. Message: {}", e.getMessage(), e);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
            EEAErrorMessage.PERMISSION_NOT_CREATED);
      }
    }
  }

  /**
   * Creates the api key.
   *
   * @param dataflowId the dataflow id
   * @param dataProvider the data provider
   * @return the string
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_CUSTODIAN')")
  @PostMapping("/createApiKey")
  public String createApiKey(@RequestParam("dataflowId") Long dataflowId,
      @RequestParam(value = "dataProvider", required = false) Long dataProvider) {

    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);

    try {
      return securityProviderInterfaceService.createApiKey(userId, dataflowId, dataProvider);
    } catch (EEAException e) {
      LOG_ERROR.error("Error adding ApiKey to user. Message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.PERMISSION_NOT_CREATED, e);
    }
  }

  /**
   * Gets the api key.
   *
   * @param dataflowId the dataflow id
   * @param dataProvider the data provider
   * @return the api key
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_CUSTODIAN')")
  @GetMapping("/getApiKey")
  public String getApiKey(@RequestParam("dataflowId") Long dataflowId,
      @RequestParam(value = "dataProvider", required = false) Long dataProvider) {
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);
    return retrieveApiKey(userId, dataflowId, dataProvider);
  }

  /**
   * Gets the api key.
   *
   * @param userId the user id
   * @param dataflowId the dataflow id
   * @param dataProvider the data provider
   * @return the api key
   */
  @Override
  @HystrixCommand
  @GetMapping("/{userId}/getApiKey")
  public String getApiKey(@PathVariable("userId") String userId,
      @RequestParam("dataflowId") Long dataflowId,
      @RequestParam(value = "dataProvider", required = false) Long dataProvider) {
    return retrieveApiKey(userId, dataflowId, dataProvider);
  }

  /**
   * Authenticate user by api key.
   *
   * @param apiKey the api key
   * @return the token VO
   */
  @Override
  @HystrixCommand
  @PostMapping("/authenticateByApiKey/{apiKey}")
  public TokenVO authenticateUserByApiKey(@PathVariable("apiKey") String apiKey) {
    return securityProviderInterfaceService.authenticateApiKey(apiKey);
  }


  /**
   * Gets the users by group.
   *
   * @param group the group
   * @return the users by group
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/getUsersByGroup/{group}")
  public List<UserRepresentationVO> getUsersByGroup(@PathVariable("group") String group) {
    GroupInfo[] groupInfo = keycloakConnectorService.getGroupsWithSearch(group);
    UserRepresentation[] users = null;
    if (groupInfo != null && groupInfo.length != 0) {
      users = keycloakConnectorService.getUsersByGroupId(groupInfo[0].getId());
    }
    return users != null
        ? userRepresentationMapper.entityListToClass(new ArrayList<>(Arrays.asList(users)))
        : null;
  }

  /**
   * Retrieve api key.
   *
   * @param userId the user id
   * @param dataflowId the dataflow id
   * @param dataProvider the data provider
   * @return the string
   */
  private String retrieveApiKey(String userId, Long dataflowId, Long dataProvider) {
    try {
      return securityProviderInterfaceService.getApiKey(userId, dataflowId, dataProvider);
    } catch (EEAException e) {
      LOG_ERROR.error("Error adding ApiKey to user. Message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.PERMISSION_NOT_CREATED, e);
    }
  }
}
