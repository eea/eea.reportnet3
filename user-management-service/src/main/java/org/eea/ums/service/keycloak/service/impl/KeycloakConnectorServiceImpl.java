package org.eea.ums.service.keycloak.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.eea.ums.service.keycloak.admin.TokenMonitor;
import org.eea.ums.service.keycloak.model.CheckResourcePermissionRequest;
import org.eea.ums.service.keycloak.model.CheckResourcePermissionResult;
import org.eea.ums.service.keycloak.model.ClientInfo;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.model.Resource;
import org.eea.ums.service.keycloak.model.ResourceInfo;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


/**
 * The type Keycloak connector service.
 */
@Service
public class KeycloakConnectorServiceImpl implements KeycloakConnectorService {

  /**
   * The realm name.
   */
  @Value("${eea.keycloak.realmName}")
  private String realmName;

  /**
   * The secret.
   */
  @Value("${eea.keycloak.secret}")
  private String secret;

  /**
   * The client id.
   */
  @Value("${eea.keycloak.clientId}")
  private String clientId;

  /**
   * The keycloak host.
   */
  @Value("${eea.keycloak.host}")
  private String keycloakHost;

  /**
   * The keycloak scheme.
   */
  @Value("${eea.keycloak.scheme}")
  private String keycloakScheme;

  /**
   * The redirect uri.
   */
  @Value("${eea.keycloak.redirect_uri}")
  private String redirectUri;


  /**
   * The admin user.
   */
  @Value("${eea.keycloak.admin.user}")
  private String adminUser;

  /**
   * The admin pass.
   */
  @Value("${eea.keycloak.admin.password}")
  private String adminPass;


  /**
   * The internal client id.
   */
  private String internalClientId;

  /**
   * The resource types.
   */
  private Map<String, String> resourceTypes;

  /**
   * The rest template.
   */
  @Autowired
  private RestTemplate restTemplate;

  /**
   * The Constant GENERATE_TOKEN_URL.
   */
  private static final String GENERATE_TOKEN_URL =
      "/auth/realms/{realm}/protocol/openid-connect/token";

  /**
   * The Constant LOGOUT_URL.
   */
  private static final String LOGOUT_URL = "/auth/realms/{realm}/protocol/openid-connect/logout";

  /**
   * The Constant LIST_USERS_URL.
   */
  private static final String LIST_USERS_URL = "/auth/admin/realms/{realm}/users?max=500";

  /**
   * The Constant GET_USER_BY_EMAIL_URL.
   */
  private static final String GET_USER_BY_EMAIL_URL =
      "/auth/admin/realms/{realm}/users?email={email}";

  /**
   * The Constant LIST_GROUPS_URL.
   */
  private static final String LIST_GROUPS_URL = "/auth/admin/realms/{realm}/groups";

  /**
   * The Constant GROUP_DETAIL_URL.
   */
  private static final String GROUP_DETAIL_URL = "/auth/admin/realms/{realm}/groups/{groupId}";

  /**
   * The Constant CREATE_USER_GROUP_URL.
   */
  private static final String CREATE_USER_GROUP_URL = "/auth/admin/realms/{realm}/groups/";

  /**
   * The Constant DELETE_USER_GROUP_URL.
   */
  private static final String DELETE_USER_GROUP_URL = "/auth/admin/realms/{realm}/groups/{groupId}";

  /**
   * The Constant ADD_USER_TO_USER_GROUP_URL.
   */
  private static final String ADD_USER_TO_USER_GROUP_URL =
      "/auth/admin/realms/Reportnet/users/{userId}/groups/{groupId}";

  /**
   * The Constant CHECK_USER_PERMISSION.
   */
  private static final String CHECK_USER_PERMISSION =
      "/auth/admin/realms/{realm}/clients/{clientInterenalId}/authz/resource-server/policy/evaluate";

  /**
   * The Constant GET_CLIENT_ID.
   */
  private static final String GET_CLIENT_ID = "/auth/admin/realms/{realm}/clients/";

  /**
   * The Constant GET_RESOURCE_SET.
   */
  private static final String GET_RESOURCE_SET =
      "/auth/realms/{realm}/authz/protection/resource_set";

  /**
   * The Constant GET_RESOURCE_INFO.
   */
  private static final String GET_RESOURCE_INFO =
      "/auth/realms/{realm}/authz/protection/resource_set/{resourceId}";

  /**
   * The Constant GET_GROUPS_BY_USER.
   */
  private static final String GET_GROUPS_BY_USER =
      "/auth/admin/realms/{realm}/users/{userId}/groups";

  /**
   * The Constant URI_PARAM_REALM.
   */
  private static final String URI_PARAM_REALM = "realm";

  /**
   * The Constant URI_PARAM_RESOURCE_ID.
   */
  private static final String URI_PARAM_RESOURCE_ID = "resourceId";

  /**
   * The Constant URI_PARAM_USER_ID.
   */
  private static final String URI_PARAM_USER_ID = "userId";

  /**
   * The Constant URI_PARAM_GROUP_ID.
   */
  private static final String URI_PARAM_GROUP_ID = "groupId";

  /**
   * The Constant URI_PARAM_EMAIL.
   */
  private static final String URI_PARAM_EMAIL = "email";

  /**
   * The Constant LIST_ROLE_BY_REALM.
   */
  private static final String LIST_ROLE_BY_REALM = "/auth/admin/realms/{realm}/roles";

  /**
   * The Constant ADD_ROLE_TO_USER.
   */
  private static final String ADD_ROLE_TO_USER =
      "/auth/admin/realms/{realm}/users/{userId}/role-mappings/realm";

  /**
   * The Constant USER_URL.
   */
  private static final String USER_URL = "/auth/admin/realms/{realm}/users/{userId}";

  /**
   * The Constant USER_ROLES_URL.
   */
  private static final String USER_ROLES_URL =
      "/auth/admin/realms/{realm}/users/{userId}/role-mappings/realm/composite";
  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * Inits the keycloak context.
   */
  @PostConstruct
  private void initKeycloakContext() {
    // As TokenMonitor has not been created yet (it depends on KeycloakConnectorService) it is
    // necessary to get one adminToken to retrieve necessary information
    // such as clientId, resources...
    TokenInfo tokenInfo = this.generateToken(adminUser, adminPass);

    String adminToken = Optional.ofNullable(tokenInfo).map(TokenInfo::getAccessToken).orElse("");
    this.internalClientId = getReportnetClientInfo(adminToken).getId();
    List<ResourceInfo> resources = this.getResourceInfo(adminToken);
    resourceTypes = new HashMap<>();
    resources.stream()
        .forEach(resource -> resourceTypes.put(resource.getName(), resource.getType()));

  }


  /**
   * Check user permision boolean.
   *
   * @param resourceName the resource name
   * @param scopes the scopes
   *
   * @return the boolean
   */
  @Override
  public String checkUserPermision(String resourceName, AccessScopeEnum... scopes) {

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    uriParams.put("clientInterenalId", internalClientId);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    CheckResourcePermissionRequest checkResourceInfo = new CheckResourcePermissionRequest();
    Map<String, String> authDetails =
        (Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails();

    checkResourceInfo.setUserId(authDetails.get(AuthenticationDetails.USER_ID));
    List<Resource> resources = new ArrayList<>();
    Resource resource = new Resource();
    resource.setName(resourceName);
    resource.setScopes(
        Arrays.asList(scopes).stream().map(AccessScopeEnum::getScope).collect(Collectors.toList()));
    resources.add(resource);
    resource.setType(this.resourceTypes.get(resourceName));
    checkResourceInfo.setResources(resources);

    HttpEntity<CheckResourcePermissionRequest> request =
        createHttpRequest(checkResourceInfo, uriParams);
    ResponseEntity<CheckResourcePermissionResult> checkResult = this.restTemplate.exchange(
        uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost).path(CHECK_USER_PERMISSION)
            .buildAndExpand(uriParams).toString(),
        HttpMethod.POST, request, CheckResourcePermissionResult.class);
    CheckResourcePermissionResult result = new CheckResourcePermissionResult();
    if (null != checkResult && null != checkResult.getBody()) {
      result = checkResult.getBody();
    }
    String permission = null != result && null != result.getStatus() ? result.getStatus() : "DENY";

    return permission;
  }

  /**
   * Gets the groups by user.
   *
   * @param userId the user id
   *
   * @return the groups by user
   */
  @Override
  public GroupInfo[] getGroupsByUser(String userId) {

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    uriParams.put(URI_PARAM_USER_ID, userId);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    HttpEntity<Void> request = createHttpRequest(null, uriParams);

    ResponseEntity<GroupInfo[]> responseEntity =
        this.restTemplate
            .exchange(
                uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
                    .path(GET_GROUPS_BY_USER).buildAndExpand(uriParams).toString(),
                HttpMethod.GET, request, GroupInfo[].class);

    return Optional.ofNullable(responseEntity).map(ResponseEntity::getBody).orElse(null);
  }


  /**
   * Generate token string.
   *
   * @param username the username
   * @param password the password
   *
   * @return the string
   */
  @Override
  public TokenInfo generateToken(String username, String password) {
    MultiValueMap<String, String> map = getTokenGenerationMap(username, password, false);
    return retrieveTokenFromKeycloak(map);
  }

  /**
   * Generate admin token token info.
   *
   * @param username the username
   * @param password the password
   *
   * @return the token info
   */
  @Override
  public TokenInfo generateAdminToken(String username, String password) {
    MultiValueMap<String, String> map = getTokenGenerationMap(username, password, true);
    return retrieveTokenFromKeycloak(map);
  }

  /**
   * Gets the token generation map.
   *
   * @param username the username
   * @param password the password
   * @param admin the admin
   *
   * @return the token generation map
   */
  private MultiValueMap<String, String> getTokenGenerationMap(String username, String password,
      Boolean admin) {
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("username", username);
    map.add("grant_type", "password");
    map.add("password", password);
    map.add("client_secret", secret);
    map.add("client_id", clientId);
    if (admin) {
      map.add("scope", "openid info offline_access");
    }
    return map;
  }

  /**
   * Generate token string based on cas code.
   *
   * @param code the code
   *
   * @return the string
   */
  @Override
  public TokenInfo generateToken(String code) {

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("code", code);
    map.add("grant_type", "authorization_code");
    map.add("client_secret", secret);
    map.add("client_id", clientId);
    map.add("redirect_uri", redirectUri);

    return retrieveTokenFromKeycloak(map);

  }

  /**
   * Refresh token.
   *
   * @param refreshToken the refresh token
   *
   * @return the token info
   */
  @Override
  public TokenInfo refreshToken(String refreshToken) {
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("refresh_token", refreshToken);
    map.add("grant_type", "refresh_token");
    map.add("client_secret", secret);
    map.add("client_id", clientId);

    return retrieveTokenFromKeycloak(map);
  }

  /**
   * Logout.
   *
   * @param refreshToken the refresh token
   */
  @Override
  public void logout(String refreshToken) {
    HttpHeaders headers = createBasicHeaders(null);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("refresh_token", refreshToken);
    map.add("client_secret", secret);
    map.add("client_id", clientId);
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
    this.restTemplate.postForEntity(uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
        .path(LOGOUT_URL).buildAndExpand(uriParams).toString(), request, Void.class);
  }

  /**
   * Gets the groups.
   *
   * @return the groups
   */
  @Override
  public GroupInfo[] getGroups() {
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    HttpEntity<Void> request = createHttpRequest(null, uriParams);
    ResponseEntity<GroupInfo[]> responseEntity =
        this.restTemplate
            .exchange(
                uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost).path(LIST_GROUPS_URL)
                    .buildAndExpand(uriParams).toString(),
                HttpMethod.GET, request, GroupInfo[].class);

    return Optional.ofNullable(responseEntity).map(ResponseEntity::getBody).map(entity -> entity)
        .orElse(null);
  }

  /**
   * Gets the group detail.
   *
   * @param groupId the group id
   *
   * @return the group detail
   */
  @Override
  public GroupInfo getGroupDetail(String groupId) {
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    uriParams.put(URI_PARAM_GROUP_ID, groupId);

    HttpEntity<Void> request = createHttpRequest(null, uriParams);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    ResponseEntity<GroupInfo> responseEntity =
        this.restTemplate
            .exchange(
                uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
                    .path(GROUP_DETAIL_URL).buildAndExpand(uriParams).toString(),
                HttpMethod.GET, request, GroupInfo.class);

    return Optional.ofNullable(responseEntity).map(ResponseEntity::getBody).map(entity -> entity)
        .orElse(null);
  }

  /**
   * Update user.
   *
   * @param user the user
   */
  @Override
  public void updateUser(UserRepresentation user) {
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    uriParams.put(URI_PARAM_USER_ID, user.getId());

    HttpEntity<UserRepresentation> request = createHttpRequest(user, uriParams);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    this.restTemplate.exchange(
        uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost).path(USER_URL)
            .buildAndExpand(uriParams).toString(),
        HttpMethod.PUT, request, UserRepresentation.class);

  }

  /**
   * Gets the user.
   *
   * @param userId the user id
   *
   * @return the user
   */
  @Override
  public UserRepresentation getUser(String userId) {
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    uriParams.put(URI_PARAM_USER_ID, userId);

    HttpEntity<UserRepresentation> request = createHttpRequest(null, uriParams);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    ResponseEntity<UserRepresentation> responseEntity = this.restTemplate.exchange(
        uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost).path(USER_URL)
            .buildAndExpand(uriParams).toString(),
        HttpMethod.GET, request, UserRepresentation.class);

    return Optional.ofNullable(responseEntity).map(ResponseEntity::getBody).map(entity -> entity)
        .orElse(null);
  }

  @Override
  public RoleRepresentation[] getUserRoles(String userId) {
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    uriParams.put(URI_PARAM_USER_ID, userId);

    HttpEntity<UserRepresentation> request = createHttpRequest(null, uriParams);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    ResponseEntity<RoleRepresentation[]> responseEntity = this.restTemplate.exchange(
        uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost).path(USER_ROLES_URL)
            .buildAndExpand(uriParams).toString(),
        HttpMethod.GET, request, RoleRepresentation[].class);

    return Optional.ofNullable(responseEntity).map(ResponseEntity::getBody).orElse(null);
  }

  /**
   * Creates the group detail.
   *
   * @param groupInfo the group info
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void createGroupDetail(GroupInfo groupInfo) throws EEAException {
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    HttpEntity<GroupInfo> request = createHttpRequest(groupInfo, uriParams);
    try {
      this.restTemplate.postForEntity(uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
          .path(CREATE_USER_GROUP_URL).buildAndExpand(uriParams).toString(), request, Void.class);
    } catch (Exception e) {
      throw new EEAException(EEAErrorMessage.PERMISSION_NOT_CREATED);
    }
  }

  /**
   * Delete group detail.
   *
   * @param groupId the group id
   */
  @Override
  public void deleteGroupDetail(String groupId) {
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    uriParams.put(URI_PARAM_GROUP_ID, groupId);

    HttpEntity<Void> request = createHttpRequest(null, uriParams);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    this.restTemplate
        .exchange(
            uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
                .path(DELETE_USER_GROUP_URL).buildAndExpand(uriParams).toString(),
            HttpMethod.DELETE, request, Void.class);


  }


  /**
   * Adds the user to group.
   *
   * @param userId the user id
   * @param groupId the group id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void addUserToGroup(String userId, String groupId) throws EEAException {
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    uriParams.put(URI_PARAM_GROUP_ID, groupId);
    uriParams.put(URI_PARAM_USER_ID, userId);
    HttpEntity<Void> request = createHttpRequest(null, uriParams);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    try {
      this.restTemplate.exchange(
          uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
              .path(ADD_USER_TO_USER_GROUP_URL).buildAndExpand(uriParams).toString(),
          HttpMethod.PUT, request, Void.class);
    } catch (Exception e) {
      throw new EEAException(EEAErrorMessage.PERMISSION_NOT_CREATED);
    }

  }

  /**
   * Adds the user.
   *
   * @param body the body
   */
  @Override
  public void addUser(String body) {
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    HttpEntity<String> request = createHttpRequestPOST(body, uriParams);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    this.restTemplate.exchange(uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
        .path(LIST_USERS_URL).buildAndExpand(uriParams).toString(), HttpMethod.POST, request,
        Void.class);
  }

  /**
   * Gets the users.
   *
   * @return the users
   */
  @Override
  public UserRepresentation[] getUsers() {
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    HttpEntity<Void> request = createHttpRequest(null, uriParams);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    ResponseEntity<UserRepresentation[]> responseEntity = this.restTemplate.exchange(
        uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost).path(LIST_USERS_URL)
            .buildAndExpand(uriParams).toString(),
        HttpMethod.GET, request, UserRepresentation[].class);

    return Optional.ofNullable(responseEntity).map(entity -> entity.getBody()).map(entity -> entity)
        .orElse(null);
  }

  /**
   * Gets the users by email.
   *
   * @param email the email
   *
   * @return the users by email
   */
  @Override
  public UserRepresentation[] getUsersByEmail(String email) {
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    uriParams.put(URI_PARAM_EMAIL, email);
    HttpEntity<Void> request = createHttpRequest(null, uriParams);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    return restTemplate.exchange(
        uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost).path(GET_USER_BY_EMAIL_URL)
            .buildAndExpand(uriParams).toString(),
        HttpMethod.GET, request, UserRepresentation[].class).getBody();
  }

  /**
   * Adds the role.
   *
   * @param body the body
   * @param userId the user id
   */
  @Override
  public void addRole(String body, String userId) {
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    uriParams.put(URI_PARAM_USER_ID, userId);
    HttpEntity<String> request = createHttpRequestPOST(body, uriParams);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    this.restTemplate.exchange(uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
        .path(ADD_ROLE_TO_USER).buildAndExpand(uriParams).toString(), HttpMethod.POST, request,
        Void.class);
  }

  /**
   * Gets the roles.
   *
   * @return the roles
   */
  @Override
  public RoleRepresentation[] getRoles() {
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    HttpEntity<Void> request = createHttpRequest(null, uriParams);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    ResponseEntity<RoleRepresentation[]> responseEntity = this.restTemplate.exchange(
        uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost).path(LIST_ROLE_BY_REALM)
            .buildAndExpand(uriParams).toString(),
        HttpMethod.GET, request, RoleRepresentation[].class);

    return Optional.ofNullable(responseEntity).map(ResponseEntity::getBody).orElse(null);
  }

  /**
   * Retrieve token from keycloak.
   *
   * @param map the map
   *
   * @return the token info
   */
  private TokenInfo retrieveTokenFromKeycloak(MultiValueMap<String, String> map) {
    HttpHeaders headers = createBasicHeaders(null);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    TokenInfo responseBody = null;
    try {
      ResponseEntity<TokenInfo> tokenInfo =
          this.restTemplate.postForEntity(
              uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
                  .path(GENERATE_TOKEN_URL).buildAndExpand(uriParams).toString(),
              request, TokenInfo.class);
      if (null != tokenInfo && null != tokenInfo.getBody()) {
        responseBody = tokenInfo.getBody();
      }
    } catch (RestClientException e) {
      LOG_ERROR.error(
          "Error retrieving token from Keycloak host {} due to reason {} with following values {}",
          keycloakHost, e.getMessage(), map, e);
    }

    return responseBody;
  }

  /**
   * Gets the reportnet client info.
   *
   * @param adminToken the admin token
   *
   * @return the reportnet client info
   */
  private ClientInfo getReportnetClientInfo(String adminToken) {
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Authorization", "Bearer " + adminToken);
    HttpHeaders headers = createBasicHeaders(headerInfo);
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    ResponseEntity<ClientInfo[]> clientInfo = this.restTemplate
        .exchange(uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost).path(GET_CLIENT_ID)
            .buildAndExpand(uriParams).toString(), HttpMethod.GET, request, ClientInfo[].class);
    ClientInfo result = null;
    if (null != clientInfo && null != clientInfo.getBody()) {
      ClientInfo[] clientInfos = clientInfo.getBody();
      if (null != clientInfos) {
        for (ClientInfo info : clientInfos) {
          if (clientId.equals(info.getClientId())) {
            result = info;
            break;
          }
        }
      }
    }
    return result;

  }

  /**
   * Gets the resource info.
   *
   * @param adminToken the admin token
   *
   * @return the resource info
   */
  private List<ResourceInfo> getResourceInfo(String adminToken) {
    // First Get all the Resource sets
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Authorization", "Bearer " + adminToken);
    HttpHeaders headers = createBasicHeaders(headerInfo);
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    ResponseEntity<String[]> resourceSet =
        this.restTemplate
            .exchange(
                uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
                    .path(GET_RESOURCE_SET).buildAndExpand(uriParams).toString(),
                HttpMethod.GET, request, String[].class);
    List<ResourceInfo> result = new ArrayList<>();
    // Second: Once all the resource sets have been retrieved, get information about everyone of
    // them
    if (null != resourceSet && null != resourceSet.getBody()) {
      String[] resourcesetBody = resourceSet.getBody();
      if (null != resourcesetBody) {
        List<String> resources = Arrays.asList(resourcesetBody);
        if (null != resources && !resources.isEmpty()) {
          resources.forEach(resourceSetId -> {

            Map<String, String> uriRequestParam = new HashMap<>();
            uriRequestParam.put(URI_PARAM_REALM, realmName);
            uriRequestParam.put(URI_PARAM_RESOURCE_ID, resourceSetId);
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
            ResponseEntity<ResourceInfo> resource = this.restTemplate.exchange(
                uriBuilder.scheme(keycloakScheme).host(keycloakHost).path(GET_RESOURCE_INFO)
                    .buildAndExpand(uriRequestParam).toString(),
                HttpMethod.GET, request, ResourceInfo.class);
            result.add(resource.getBody());
          });
        }
      }
    }
    return result;
  }

  /**
   * Creates the basic headers.
   *
   * @param headersInfo the headers info
   *
   * @return the http headers
   */
  private HttpHeaders createBasicHeaders(Map<String, String> headersInfo) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    if (null != headersInfo && headersInfo.size() > 0) {
      headersInfo.entrySet().forEach(entry -> headers.set(entry.getKey(), entry.getValue()));
    }
    return headers;
  }

  /**
   * Creates the http request.
   *
   * @param <T> the generic type
   * @param body the body
   * @param uriParams the uri params
   *
   * @return the http entity
   */
  private <T> HttpEntity<T> createHttpRequest(T body, Map<String, String> uriParams) {
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Authorization", "Bearer " + TokenMonitor.getToken());

    HttpHeaders headers = createBasicHeaders(headerInfo);

    HttpEntity<T> request = new HttpEntity<>(body, headers);
    return request;
  }

  /**
   * Creates the http request POST.
   *
   * @param <T> the generic type
   * @param body the body
   * @param uriParams the uri params
   *
   * @return the http entity
   */
  private <T> HttpEntity<T> createHttpRequestPOST(T body, Map<String, String> uriParams) {
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Authorization", "Bearer " + TokenMonitor.getToken());
    headerInfo.put("Content-Type", "application/json");
    HttpHeaders headers = createBasicHeaders(headerInfo);

    HttpEntity<T> request = new HttpEntity<>(body, headers);
    return request;
  }


}
