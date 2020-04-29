package org.eea.ums.controller;

import java.io.IOException;
import java.io.InputStream;
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
import org.eea.ums.mapper.UserRepresentationMapper;
import org.eea.ums.service.BackupManagmentService;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
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
   * The backup managment controler service.
   */
  @Autowired
  private BackupManagmentService backupManagmentControlerService;


  /**
   * The keycloak connector service.
   */
  @Autowired
  private KeycloakConnectorService keycloakConnectorService;

  /**
   * The user representation mapper.
   */
  @Autowired
  private UserRepresentationMapper userRepresentationMapper;

  /**
   * The Constant LOG_ERROR.
   */
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
    return securityProviderInterfaceService.doLogin(username, password, false);
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
  @RequestMapping(value = "/add_user_to_resource", method = RequestMethod.PUT)
  public void addUserToResource(@RequestParam("idResource") Long idResource,
      @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum) {
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get("userId");
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
   * @param dataflowId the dataflow resourceId
   *
   * @return the string
   */
  @RequestMapping(value = "/test-security", method = RequestMethod.GET)
  @HystrixCommand
  // @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_REQUESTER','DATAFLOW_PROVIDER') AND
  // checkPermission('Dataflow','READ')")
  @PreAuthorize("checkApiKey(#dataflowId,#provider) AND secondLevelAuthorize(#dataflowId,'DATAFLOW_REQUESTER','DATAFLOW_PROVIDER')")
  public String testSecuredService(@RequestParam("dataflowId") Long dataflowId,
      @RequestParam("providerId") Long provider) {
    return "OLEEEEE";
  }

  /**
   * Sets the users.
   *
   * @param file the file
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/createUsers", method = RequestMethod.POST)
  public void createUsers(@RequestParam("file") MultipartFile file) throws IOException {
    InputStream is = file.getInputStream();
    backupManagmentControlerService.readAndSaveUsers(is);

  }

  /**
   * Gets the users.
   *
   * @return the users
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @RequestMapping(value = "/getUsers", method = RequestMethod.GET)
  public List<UserRepresentationVO> getUsers() {

    UserRepresentation[] a = keycloakConnectorService.getUsers();

    ArrayList<UserRepresentation> arrayList = new ArrayList<>(Arrays.asList(a));

    return userRepresentationMapper.entityListToClass(arrayList);
  }

  /**
   * Gets the user by email.
   *
   * @param email the email
   *
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
   * Gets the email by user id.
   *
   * @param userId the user id
   *
   * @return the email by user id
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
  @RequestMapping(value = "/updateAttributes", method = RequestMethod.PUT)
  public void updateUserAttributes(@RequestBody Map<String, List<String>> attributes) {

    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get("userId");

    UserRepresentation user = keycloakConnectorService.getUser(userId);
    if (user != null) {
      user.setAttributes(attributes);
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
  @RequestMapping(value = "/getAttributes", method = RequestMethod.GET)
  public Map<String, List<String>> getUserAttributes() {

    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get("userId");

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
  @RequestMapping(value = "/add_contributor_to_resource", method = RequestMethod.PUT)
  public void addContributorToResource(Long idResource, ResourceGroupEnum resourceGroupEnum,
      String userMail) {
    try {
      securityProviderInterfaceService.addContributorToUserGroup(null, userMail,
          resourceGroupEnum.getGroupName(idResource));
    } catch (EEAException e) {
      LOG_ERROR.error("Error adding contributor to resource. Message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.PERMISSION_NOT_CREATED);
    }
  }


  @Override
  @RequestMapping(value = "/add_contributors_to_resources", method = RequestMethod.PUT)
  public void addContributorsToResources(@RequestBody List<ResourceAssignationVO> resources) {
    try {
      securityProviderInterfaceService.addContributorsToUserGroup(resources);
    } catch (EEAException e) {
      LOG_ERROR.error("Error adding contributor to resource. Message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.PERMISSION_NOT_CREATED);
    }
  }


  @Override
  @RequestMapping(value = "/add_user_to_resources", method = RequestMethod.PUT)
  public void addUserToResources(@RequestBody List<ResourceAssignationVO> resources) {
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get("userId");
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

  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_PROVIDER')")
  @PostMapping("/createApiKey")
  public String createApiKey(@RequestParam("dataflowId") final Long dataflowId,
      @RequestParam("dataProvider") final Long dataProvider) {

    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get("userId");

    try {
      return securityProviderInterfaceService.createApiKey(userId, dataflowId, dataProvider);
    } catch (EEAException e) {
      LOG_ERROR.error("Error adding ApiKey to user. Message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.PERMISSION_NOT_CREATED, e);
    }

  }

  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_PROVIDER')")
  @GetMapping("/getApiKey")
  public String getApiKey(@RequestParam("dataflowId") final Long dataflowId,
      @RequestParam("dataProvider") final Long dataProvider) {
    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get("userId");
    return retrieveApiKey(userId, dataflowId, dataProvider);

  }

  @Override
  @HystrixCommand
  @GetMapping("/{userId}/getApiKey")
  public String getApiKey(@PathVariable("userId") final String userId,
      @RequestParam("dataflowId") final Long dataflowId,
      @RequestParam("dataProvider") final Long dataProvider) {
    return retrieveApiKey(userId, dataflowId, dataProvider);
  }

  @Override
  @HystrixCommand
  @PostMapping(value = "/authenticateByApiKey/{apiKey}")
  public TokenVO authenticateUserByApiKey(@PathVariable("apiKey") final String apiKey) {
    return securityProviderInterfaceService.authenticateApiKey(apiKey);
  }

  private String retrieveApiKey(final String userId, final Long dataflowId,
      final Long dataProvider) {
    try {
      return securityProviderInterfaceService.getApiKey(userId, dataflowId, dataProvider);
    } catch (EEAException e) {
      LOG_ERROR.error("Error adding ApiKey to user. Message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.PERMISSION_NOT_CREATED, e);
    }
  }
}
