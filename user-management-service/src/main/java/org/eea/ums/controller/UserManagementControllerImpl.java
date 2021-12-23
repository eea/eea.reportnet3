package org.eea.ums.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.UserRoleVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.eea.ums.mapper.UserRepresentationMapper;
import org.eea.ums.service.BackupManagmentService;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.eea.ums.service.UserRoleService;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import springfox.documentation.annotations.ApiIgnore;

/**
 * The Class UserManagementControllerImpl.
 */
@RestController
@RequestMapping("/user")
@Api(tags = "Users Management : Users Management  Manager")
@ApiIgnore
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

  @Autowired
  private UserRoleService userRoleService;

  @Autowired
  private NotificationControllerZuul notificationControllerZuul;

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  private static final Logger LOG = LoggerFactory.getLogger(UserManagementControllerImpl.class);

  /**
   * The Constant ERROR_ADDING_CONTRIBUTOR.
   */
  private static final String ERROR_ADDING_CONTRIBUTOR =
      "Error adding contributor to resource. Message: {}";

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
  @PostMapping("/generateToken")
  @ApiOperation(value = "Generate an Access Token (valid only for 5 minutes)",
      response = TokenVO.class, hidden = true)
  public TokenVO generateToken(
      @ApiParam(value = "User Name") @RequestParam("username") String username,
      @ApiParam(value = "User Password") @RequestParam("password") String password) {
    return securityProviderInterfaceService.doLogin(username, password, false);
  }

  /**
   * Generate token.
   *
   * @param code the code
   *
   * @return the token VO
   */
  @Override
  @HystrixCommand
  @PostMapping("/generateTokenByCode")
  @ApiOperation(value = "Generate an Access Token based on a Keycloak's Code",
      response = TokenVO.class, hidden = true)
  public TokenVO generateToken(@ApiParam(value = "Code") @RequestParam("code") String code) {
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
  @PostMapping("/refreshToken")
  @ApiOperation(value = "Refresh an Access Token by Refresh Token", response = TokenVO.class,
      hidden = true)
  public TokenVO refreshToken(
      @ApiParam(value = "Refresh Token") @RequestParam("refreshToken") String refreshToken) {
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
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/checkAccess")
  @ApiOperation(value = "Check Resource Permission", response = Boolean.class, hidden = true)
  public Boolean checkResourceAccessPermission(
      @ApiParam(value = "Resource Name") @RequestParam("resource") String resource, @ApiParam(
          value = "Access Scope Enum Array") @RequestParam("scopes") AccessScopeEnum[] scopes) {
    return securityProviderInterfaceService.checkAccessPermission(resource, scopes);
  }

  /**
   * Gets the resources by user.
   *
   * @return the resources by user
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/resources")
  @ApiOperation(value = "Get logged User's Resources", response = ResourceAccessVO.class,
      responseContainer = "List", hidden = true)
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
   *
   * @return the resources by user
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/resources_by_type")
  @ApiOperation(value = "Get logged User's Resources by their Types",
      response = ResourceAccessVO.class, responseContainer = "List", hidden = true)
  public List<ResourceAccessVO> getResourcesByUser(@ApiParam(
      value = "Resource Type Enum") @RequestParam("resourceType") ResourceTypeEnum resourceType) {
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
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/resources_by_role")
  @ApiOperation(value = "Get logged User's Resources by User Security Role",
      response = ResourceAccessVO.class, responseContainer = "List", hidden = true)
  public List<ResourceAccessVO> getResourcesByUser(@ApiParam(
      value = "Security Role Enum") @RequestParam("securityRole") SecurityRoleEnum securityRole) {
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
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/resources_by_type_role")
  @ApiOperation(value = "Get logged User's Resources by Type and Security Role",
      response = ResourceAccessVO.class, responseContainer = "List", hidden = true)
  public List<ResourceAccessVO> getResourcesByUser(
      @ApiParam(
          value = "Resource Type Enum") @RequestParam("resourceType") ResourceTypeEnum resourceType,
      @ApiParam(
          value = "Security Role Enum") @RequestParam("securityRole") SecurityRoleEnum securityRole) {
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
  @ApiOperation(value = "Logout a User", hidden = true)
  public void doLogOut(
      @ApiParam(value = "RefreshToken") @RequestParam("refreshToken") String refreshToken) {
    securityProviderInterfaceService.doLogout(refreshToken);
  }

  /**
   * Adds the user to resource.
   *
   * @param idResource the id resource
   * @param resourceGroupEnum the resource group enum
   */
  @Override
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/add_user_to_resource")
  @ApiOperation(value = "Add one Resource for the logged User", hidden = true)
  @ApiResponse(code = 500, message = EEAErrorMessage.PERMISSION_NOT_CREATED)
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
   *
   * @return the string
   */
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/test-security")
  @ApiOperation(value = "Test Secured Service", response = String.class, hidden = true)
  public String testSecuredService(
      @ApiParam(value = "Dataflow id", example = "0") @RequestParam("dataflowId") Long dataflowId,
      @ApiParam(value = "Data provider id",
          example = "0") @RequestParam("providerId") Long provider) {
    return "OLEEEEE";
  }

  /**
   * Creates the users.
   *
   * @param file the file
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @PostMapping("/createUsers")
  @ApiOperation(value = "Create Users By File", hidden = true)
  @ApiResponse(code = 500, message = "Internal Server Error")
  public void createUsers(@ApiParam(value = "File with users", type = "MultipartFile",
      hidden = true) @RequestBody MultipartFile file) {
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
  @GetMapping("/private/getUsers")
  @ApiOperation(value = "Get all Users", response = UserRepresentationVO.class,
      responseContainer = "List", hidden = true)
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
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/getUserByEmail")
  @ApiOperation(value = "Get Users by Email", response = UserRepresentationVO.class, hidden = true)
  public UserRepresentationVO getUserByEmail(
      @ApiParam(value = "User Email") @RequestParam("email") String email) {
    UserRepresentationVO user = null;
    UserRepresentation[] users = keycloakConnectorService.getUsersByEmail(email);
    if (users != null && users.length == 1 && StringUtils.isNotBlank(email)
        && StringUtils.isNotBlank(users[0].getEmail())
        && email.equalsIgnoreCase(users[0].getEmail())) {
      user = userRepresentationMapper.entityToClass(users[0]);
    }
    return user;
  }

  /**
   * Gets the user by user id.
   *
   * @param userId the user id
   *
   * @return the user by user id
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/getUserByUserId")
  @ApiOperation(value = "Get Users by Id", response = UserRepresentationVO.class, hidden = true)
  public UserRepresentationVO getUserByUserId(
      @ApiParam(value = "User id") @RequestParam("userId") String userId) {
    UserRepresentationVO userVO = null;
    if (!(((Map<String, String>) SecurityContextHolder.getContext().getAuthentication()
        .getDetails()).get(AuthenticationDetails.USER_ID).equals(userId))) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.USER_NOTFOUND);
    }
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
  @ApiOperation(value = "Update User Attributes", hidden = true)
  @ApiResponse(code = 500, message = EEAErrorMessage.USER_NOTFOUND)
  public void updateUserAttributes(@ApiParam(value = "Map with Attributes",
      type = "Map<String, List<String>>") @RequestBody Map<String, List<String>> attributes) {

    String userId =
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID);

    // Check if the user image it's a valid one
    if (attributes.containsKey("userImage")) {
      List<String> imageList = attributes.get("userImage");
      if (CollectionUtils.isNotEmpty(imageList) && !imageList.get(0)
          .matches("^000~data:image/(png|jpg|gif|jpeg|bmp);base64,([A-Za-z0-9+/])*?$")) {
        LOG_ERROR.error(
            "Error updating the attributes of the user with id {}. The image is not valid", userId);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
            String.format(EEAErrorMessage.FILE_FORMAT));
      }
    }
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
  @ApiOperation(value = "Get logged User Attributes", response = String.class,
      responseContainer = "Map<String, List<String>>", hidden = true)
  @ApiResponse(code = 500, message = EEAErrorMessage.USER_NOTFOUND)
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
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/add_contributor_to_resource")
  @ApiOperation(value = "Add one Contributor to a Resource", hidden = true)
  @ApiResponse(code = 500, message = EEAErrorMessage.PERMISSION_NOT_CREATED)
  public void addContributorToResource(
      @ApiParam(value = "Resource id", example = "0") @RequestParam("idResource") Long idResource,
      @ApiParam(
          value = "Resource group Enum") @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum,
      @ApiParam(
          value = "User email to add contributor") @RequestParam("userMail") String userMail) {
    try {
      securityProviderInterfaceService.addContributorToUserGroup(null, userMail,
          resourceGroupEnum.getGroupName(idResource));
    } catch (EEAException e) {
      LOG_ERROR.error(ERROR_ADDING_CONTRIBUTOR, e.getMessage(), e);
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
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/remove_contributor_from_resource")
  @ApiOperation(value = "Remove one Contributor from a Resource", hidden = true)
  @ApiResponse(code = 500, message = EEAErrorMessage.PERMISSION_NOT_CREATED)
  public void removeContributorFromResource(
      @ApiParam(value = "Resource id", example = "0") @RequestParam("idResource") Long idResource,
      @ApiParam(
          value = "Resource group Enum") @RequestParam("resourceGroup") ResourceGroupEnum resourceGroupEnum,
      @ApiParam(
          value = "User email to add contributor") @RequestParam("userMail") String userMail) {
    try {
      securityProviderInterfaceService.removeContributorFromUserGroup(null, userMail,
          resourceGroupEnum.getGroupName(idResource));
    } catch (EEAException e) {
      LOG_ERROR.error(ERROR_ADDING_CONTRIBUTOR, e.getMessage(), e);
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
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/add_contributors_to_resources")
  @ApiResponse(code = 500, message = EEAErrorMessage.PERMISSION_NOT_CREATED)
  @ApiOperation(value = "Add Contributors to Resources", hidden = true)
  public void addContributorsToResources(@ApiParam(value = "Resources List",
      type = "List") @RequestBody List<ResourceAssignationVO> resources) {
    try {
      securityProviderInterfaceService.addContributorsToUserGroup(resources);
    } catch (EEAException e) {
      LOG_ERROR.error(ERROR_ADDING_CONTRIBUTOR, e.getMessage(), e);
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
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/remove_contributors_from_resources")
  @ApiResponse(code = 500, message = EEAErrorMessage.PERMISSION_NOT_CREATED)
  @ApiOperation(value = "Remove Contributors from Resources", hidden = true)
  public void removeContributorsFromResources(@ApiParam(value = "Resources List",
      type = "List") @RequestBody List<ResourceAssignationVO> resources) {
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
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/add_user_to_resources")
  @ApiOperation(value = "Add Resources to Users", hidden = true)
  @ApiResponse(code = 500, message = EEAErrorMessage.PERMISSION_NOT_CREATED)
  public void addUserToResources(@ApiParam(value = "Resources List",
      type = "List") @RequestBody List<ResourceAssignationVO> resources) {
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
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/remove_user_from_resources")
  @ApiOperation(value = "Remove Resources from Users", hidden = true)
  @ApiResponse(code = 500, message = EEAErrorMessage.PERMISSION_NOT_CREATED)
  public void removeUserFromResources(@ApiParam(value = "Resources List",
      type = "List") @RequestBody List<ResourceAssignationVO> resources) {
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
   *
   * @return the string
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_READ','DATAFLOW_REPORTER_WRITE','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_NATIONAL_COORDINATOR','DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_CUSTODIAN_SUPPORT') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId))")
  @PostMapping("/createApiKey")
  @ApiOperation(value = "Create ApiKey for the logged User", response = String.class, hidden = true)
  @ApiResponse(code = 500, message = EEAErrorMessage.PERMISSION_NOT_CREATED)
  public String createApiKey(
      @ApiParam(value = "Dataflow id", example = "0") @RequestParam("dataflowId") Long dataflowId,
      @ApiParam(value = "Data provider id", example = "0") @RequestParam(value = "dataProvider",
          required = false) Long dataProvider) {

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
   *
   * @return the api key
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#dataflowId,'DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_READ','DATAFLOW_REPORTER_WRITE','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_NATIONAL_COORDINATOR','DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_CUSTODIAN_SUPPORT') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId))")
  @GetMapping("/getApiKey")
  @ApiOperation(value = "Get logged User ApiKey by Dataflow Id and Dataprovider Id",
      response = String.class, hidden = true)
  public String getApiKey(
      @ApiParam(value = "Dataflow id", example = "0") @RequestParam("dataflowId") Long dataflowId,
      @ApiParam(value = "Data provider id", example = "0") @RequestParam(value = "dataProvider",
          required = false) Long dataProvider) {
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
   *
   * @return the api key
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/{userId}/getApiKey")
  @ApiOperation(value = "Create ApiKey by User Id, Dataflow Id and Dataprovider Id",
      response = String.class, hidden = true)
  public String getApiKey(@ApiParam(value = "User wanted id") @PathVariable("userId") String userId,
      @ApiParam(value = "Dataflow id", example = "0") @RequestParam("dataflowId") Long dataflowId,
      @ApiParam(value = "Data provider id", example = "0") @RequestParam(value = "dataProvider",
          required = false) Long dataProvider) {
    return retrieveApiKey(userId, dataflowId, dataProvider);
  }

  /**
   * Authenticate user by api key.
   *
   * @param apiKey the api key
   *
   * @return the token VO
   */
  @Override
  @HystrixCommand
  @PostMapping("/authenticateByApiKey/{apiKey}")
  @ApiOperation(value = "Authenticate an User by an Api Key", response = TokenVO.class,
      hidden = true)
  public TokenVO authenticateUserByApiKey(
      @ApiParam(value = "Apikey") @PathVariable("apiKey") String apiKey) {
    return securityProviderInterfaceService.authenticateApiKey(apiKey);
  }

  @Override
  @HystrixCommand
  @PostMapping("/authenticateByEmail")
  @ApiOperation(value = "Authenticate an User by its email.", response = TokenVO.class,
      hidden = true)
  public TokenVO authenticateUserByEmail(@RequestParam("email") String email) {
    return securityProviderInterfaceService.authenticateEmail(email);
  }

  /**
   * Gets the users by group.
   *
   * @param group the group
   *
   * @return the users by group
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/getUsersByGroup/{group}")
  @ApiOperation(value = "Get a List of Users by Group", response = UserRepresentationVO.class,
      responseContainer = "List", hidden = true)
  public List<UserRepresentationVO> getUsersByGroup(
      @ApiParam(value = "Group Resource") @PathVariable("group") String group) {
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
   * Gets the user roles by dataflow and country.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the user roles by dataflow and country
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_LEAD_REPORTER','DATAFLOW_OBSERVER','DATAFLOW_CUSTODIAN_SUPPORT','DATAFLOW_REPORTER_READ','DATAFLOW_REPORTER_WRITE','DATAFLOW_NATIONAL_COORDINATOR','DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD')")
  @GetMapping("/getUserRolesByDataflow/{dataflowId}/dataProviderId/{dataProviderId}")
  @ApiOperation(value = "Get a List of Users by Dataflow", response = UserRoleVO.class,
      responseContainer = "List", hidden = true)
  public List<UserRoleVO> getUserRolesByDataflowAndCountry(
      @ApiParam(value = "dataflowId") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(value = "dataProviderId") @PathVariable("dataProviderId") Long dataProviderId) {

    return userRoleService.getUserRolesByDataflowCountry(dataflowId, dataProviderId);
  }

  /**
   * Gets the user roles by dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the user roles by dataflow
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_OBSERVER','DATAFLOW_CUSTODIAN_SUPPORT')")
  @GetMapping("/userRoles/dataflow/{dataflowId}")
  @ApiOperation(value = "Get a List of User roles by Dataflow", response = UserRoleVO.class,
      responseContainer = "List", hidden = true)
  public List<UserRoleVO> getUserRolesByDataflow(
      @ApiParam(value = "dataflowId", example = "1") @PathVariable("dataflowId") Long dataflowId) {

    return userRoleService.getUserRolesByDataflow(dataflowId);
  }

  /**
   * Gets the resources by user email.
   *
   * @param email the email
   *
   * @return the resources by user email
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/private/resourcesByMail")
  @ApiOperation(value = "Get all Resources by User Email", response = ResourceAccessVO.class,
      responseContainer = "List", hidden = true)
  public List<ResourceAccessVO> getResourcesByUserEmail(
      @ApiParam(value = "Email User") String email) {
    // Recover user id from email
    String userId = "";
    UserRepresentation[] users = keycloakConnectorService.getUsersByEmail(email);
    if (users != null && users.length == 1) {
      userId = users[0].getId();
    }
    return securityProviderInterfaceService.getResourcesByUser(userId);
  }

  /**
   * Retrieve api key.
   *
   * @param userId the user id
   * @param dataflowId the dataflow id
   * @param dataProvider the data provider
   *
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

  /**
   * Export users by country.
   *
   * @param dataflowId the dataflow id
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_OBSERVER','DATAFLOW_CUSTODIAN_SUPPORT')")
  @PostMapping("/exportUsersByCountry/dataflow/{dataflowId}")
  @ApiOperation(value = "Export all users by country into a CSV file", hidden = true)
  public void exportUsersByCountry(@ApiParam(
      value = "Dataflow id used in the export process.") @PathVariable("dataflowId") Long dataflowId) {
    LOG.info("Export users by country from dataflow {}, with type csv.", dataflowId);
    UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
    userNotificationContentVO.setDataflowId(dataflowId);
    notificationControllerZuul.createUserNotificationPrivate("DOWNLOAD_USERS_BY_COUNTRY_START",
        userNotificationContentVO);
    try {
      userRoleService.exportUsersByCountry(dataflowId);
    } catch (IOException | EEAException e) {
      LOG_ERROR.error("Error exporting users by country from dataflow {}. Message: {}", dataflowId,
          e.getMessage());
    }
  }

  /**
   * Download users by country.
   *
   * @param dataflowId the dataflow id
   * @param fileName the file name
   * @param response the response
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_OBSERVER','DATAFLOW_CUSTODIAN_SUPPORT')")
  @GetMapping("/downloadUsersByCountry/{dataflowId}")
  @ApiOperation(value = "Download the generated CSV file containing the users by country",
      hidden = true)
  @ApiResponse(code = 404, message = "Couldn't find a file with the specified name")
  public void downloadUsersByCountry(
      @ApiParam(value = "Dataflow id used in the export process.",
          example = "10") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(
          value = "The filename the export process asigned to the Users by country export file.",
          example = "dataflow-10-UsersByCountry.csv") @RequestParam String fileName,
      HttpServletResponse response) {
    try {
      LOG.info(
          "Downloading file generared when exporting users by country. Dataflow Id {}. Filename {}.",
          dataflowId, fileName);
      File file = userRoleService.downloadUsersByCountry(dataflowId, fileName);
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

      OutputStream out = response.getOutputStream();
      FileInputStream in = new FileInputStream(file);

      IOUtils.copyLarge(in, out);
      out.close();
      in.close();

      FileUtils.forceDelete(file);

    } catch (IOException | ResponseStatusException e) {
      LOG_ERROR.error(
          "Downloading file generated when exporting Users by country. Dataflow Id {}. Filename {}. Error message: {}",
          dataflowId, fileName, e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(
          "Trying to download a file generated during the export users by country process but the file is not found, dataflowId: %s + filename: %s + message: %s ",
          dataflowId, fileName, e.getMessage()), e);
    }
  }


}
