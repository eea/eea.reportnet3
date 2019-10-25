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
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.ums.service.keycloak.admin.TokenMonitor;
import org.eea.ums.service.keycloak.model.CheckResourcePermissionRequest;
import org.eea.ums.service.keycloak.model.CheckResourcePermissionResult;
import org.eea.ums.service.keycloak.model.ClientInfo;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.model.Resource;
import org.eea.ums.service.keycloak.model.ResourceInfo;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The type Keycloak connector service.
 */
@Service
public class KeycloakConnectorServiceImpl implements KeycloakConnectorService {

  @Value("${eea.keycloak.realmName}")
  private String realmName;

  @Value("${eea.keycloak.secret}")
  private String secret;

  @Value("${eea.keycloak.clientId}")
  private String clientId;

  @Value("${eea.keycloak.host}")
  private String keycloakHost;

  @Value("${eea.keycloak.scheme}")
  private String keycloakScheme;

  @Value("${eea.keycloak.redirect_uri}")
  private String redirectUri;


  @Value("${eea.keycloak.admin.user}")
  private String adminUser;
  @Value("${eea.keycloak.admin.password}")
  private String adminPass;


  private String internalClientId;
  private Map<String, String> resourceTypes;

  @Autowired
  private RestTemplate restTemplate;

  private static final String GENERATE_TOKEN_URL = "/auth/realms/{realm}/protocol/openid-connect/token";
  private static final String LOGOUT_URL = "/auth/realms/{realm}/protocol/openid-connect/logout";
  private static final String LIST_USERS_URL = "";
  private static final String LIST_USER_GROUPS_URL = "";
  private static final String LIST_GROUPS_URL = "/auth/admin/realms/{realm}/groups";
  private static final String GROUP_DETAIL_URL = "/auth/admin/realms/{realm}/groups/{groupId}";
  private static final String CREATE_USER_GROUP_URL = "/auth/admin/realms/{realm}/groups/";
  private static final String ADD_USER_TO_USER_GROUP_URL = "/auth/admin/realms/Reportnet/users/{userId}/groups/{groupId}";
  private static final String CHECK_USER_PERMISSION = "/auth/admin/realms/{realm}/clients/{clientInterenalId}/authz/resource-server/policy/evaluate";
  private static final String GET_CLIENT_ID = "/auth/admin/realms/{realm}/clients/";
  private static final String GET_RESOURCE_SET = "/auth/realms/{realm}/authz/protection/resource_set";
  private static final String GET_RESOURCE_INFO = "/auth/realms/{realm}/authz/protection/resource_set/{resourceId}";
  private static final String GET_GROUPS_BY_USER = "/auth/admin/realms/{realm}/users/{userId}/groups";
  private static final String URI_PARAM_REALM = "realm";
  private static final String URI_PARAM_RESOURCE_ID = "resourceId";
  private static final String URI_PARAM_USER_ID = "userId";
  private static final String URI_PARAM_GROUP_ID = "groupId";


  @PostConstruct
  private void initKeycloakContext() {
    //As TokenMonitor has not been created yet (it depends on KeycloakConnectorService) it is necessary to get one adminToken to retrieve necessary information
    //such as clientId, resources...
    TokenInfo tokenInfo = this.generateToken(adminUser, adminPass);

    String adminToken = Optional.ofNullable(tokenInfo).map(info -> info.getAccessToken())
        .orElse("");
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

    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Authorization", "Bearer " + TokenMonitor.getToken());

    HttpHeaders headers = createBasicHeaders(headerInfo);
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    uriParams.put("clientInterenalId", internalClientId);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    CheckResourcePermissionRequest checkResourceInfo = new CheckResourcePermissionRequest();
    Map<String, String> authDetails = (Map<String, String>) SecurityContextHolder.getContext()
        .getAuthentication().getDetails();

    checkResourceInfo.setUserId(authDetails.get("userId"));
    List<Resource> resources = new ArrayList<>();
    Resource resource = new Resource();
    resource.setName(resourceName);
    resource.setScopes(Arrays.asList(scopes).stream().map(AccessScopeEnum::getScope).collect(
        Collectors.toList()));
    resources.add(resource);
    resource.setType(this.resourceTypes.get(resourceName));
    checkResourceInfo.setResources(resources);

    HttpEntity<CheckResourcePermissionRequest> request = new HttpEntity<>(
        checkResourceInfo, headers);
    ResponseEntity<CheckResourcePermissionResult> checkResult = this.restTemplate
        .exchange(
            uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
                .path(CHECK_USER_PERMISSION)
                .buildAndExpand(uriParams).toString(), HttpMethod.POST, request,
            CheckResourcePermissionResult.class);
    CheckResourcePermissionResult result = new CheckResourcePermissionResult();
    if (null != checkResult && null != checkResult.getBody()) {
      result = checkResult.getBody();
    }
    String permission = null != result && null != result.getStatus() ? result.getStatus() : "DENY";

    return permission;
  }

  @Override
  public GroupInfo[] getGroupsByUser(String userId) {

    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Authorization", "Bearer " + TokenMonitor.getToken());

    HttpHeaders headers = createBasicHeaders(headerInfo);
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    uriParams.put(URI_PARAM_USER_ID, userId);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    HttpEntity<Void> request = new HttpEntity<>(
        null, headers);
    ResponseEntity<GroupInfo[]> responseEntity = this.restTemplate
        .exchange(
            uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
                .path(GET_GROUPS_BY_USER)
                .buildAndExpand(uriParams).toString(), HttpMethod.GET, request,
            GroupInfo[].class);
    Optional.ofNullable(responseEntity).map(entity -> entity.getBody())
        .map(entity -> (GroupInfo[]) entity).orElse(null);
    return Optional.ofNullable(responseEntity).map(entity -> entity.getBody())
        .map(entity -> (GroupInfo[]) entity).orElse(null);
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

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("username", username);
    map.add("grant_type", "password");
    map.add("password", password);
    map.add("client_secret", secret);
    map.add("client_id", clientId);

    return retrieveTokenFromKeycloak(map);

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

  @Override
  public TokenInfo refreshToken(String refreshToken) {
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("refresh_token", refreshToken);
    map.add("grant_type", "refresh_token");
    map.add("client_secret", secret);
    map.add("client_id", clientId);

    return retrieveTokenFromKeycloak(map);
  }

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
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(
        map, headers);
    this.restTemplate
        .postForEntity(
            uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost).path(LOGOUT_URL)
                .buildAndExpand(uriParams).toString(),
            request,
            Void.class);
  }

  @Override
  public GroupInfo[] getGroups() {
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Authorization", "Bearer " + TokenMonitor.getToken());

    HttpHeaders headers = createBasicHeaders(headerInfo);
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    HttpEntity<Void> request = new HttpEntity<>(
        null, headers);
    ResponseEntity<GroupInfo[]> responseEntity = this.restTemplate
        .exchange(
            uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
                .path(LIST_GROUPS_URL)
                .buildAndExpand(uriParams).toString(), HttpMethod.GET, request,
            GroupInfo[].class);

    return Optional.ofNullable(responseEntity).map(entity -> entity.getBody())
        .map(entity -> (GroupInfo[]) entity).orElse(null);
  }

  @Override
  public GroupInfo getGroupDetail(String groupId) {
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Authorization", "Bearer " + TokenMonitor.getToken());

    HttpHeaders headers = createBasicHeaders(headerInfo);
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    uriParams.put(URI_PARAM_GROUP_ID, groupId);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    HttpEntity<Void> request = new HttpEntity<>(
        null, headers);
    ResponseEntity<GroupInfo> responseEntity = this.restTemplate
        .exchange(
            uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
                .path(GROUP_DETAIL_URL)
                .buildAndExpand(uriParams).toString(), HttpMethod.GET, request,
            GroupInfo.class);

    return Optional.ofNullable(responseEntity).map(entity -> entity.getBody())
        .map(entity -> (GroupInfo) entity).orElse(null);
  }

  @Override
  public void createGroupDetail(GroupInfo groupInfo) {
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Authorization", "Bearer " + TokenMonitor.getToken());

    HttpHeaders headers = createBasicHeaders(headerInfo);
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    HttpEntity<GroupInfo> request = new HttpEntity<>(
        groupInfo, headers);

    this.restTemplate
        .postForEntity(
            uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
                .path(CREATE_USER_GROUP_URL)
                .buildAndExpand(uriParams).toString(),
            request,
            Void.class);

  }

  @Override
  public void addUserToGroup(String userId, String groupId) {
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Authorization", "Bearer " + TokenMonitor.getToken());

    HttpHeaders headers = createBasicHeaders(headerInfo);
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);
    uriParams.put(URI_PARAM_GROUP_ID, groupId);
    uriParams.put(URI_PARAM_USER_ID, userId);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
    HttpEntity<Void> request = new HttpEntity<>(
        null, headers);
    this.restTemplate
        .exchange(
            uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost)
                .path(ADD_USER_TO_USER_GROUP_URL)
                .buildAndExpand(uriParams).toString(), HttpMethod.PUT, request,
            Void.class);

  }

  private TokenInfo retrieveTokenFromKeycloak(MultiValueMap<String, String> map) {
    HttpHeaders headers = createBasicHeaders(null);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(
        map, headers);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    ResponseEntity<TokenInfo> tokenInfo = this.restTemplate
        .postForEntity(
            uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost).path(GENERATE_TOKEN_URL)
                .buildAndExpand(uriParams).toString(),
            request,
            TokenInfo.class);

    TokenInfo responseBody = null;
    if (null != tokenInfo && null != tokenInfo.getBody()) {
      responseBody = tokenInfo.getBody();
    }
    return responseBody;
  }

  private ClientInfo getReportnetClientInfo(String adminToken) {
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Authorization", "Bearer " + adminToken);
    HttpHeaders headers = createBasicHeaders(headerInfo);
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(
        null, headers);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    ResponseEntity<ClientInfo[]> clientInfo = this.restTemplate
        .exchange(
            uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost).path(GET_CLIENT_ID)
                .buildAndExpand(uriParams).toString(), HttpMethod.GET, request,
            ClientInfo[].class);
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

  private List<ResourceInfo> getResourceInfo(String adminToken) {
    //First Get all the Resource sets
    Map<String, String> headerInfo = new HashMap<>();
    headerInfo.put("Authorization", "Bearer " + adminToken);
    HttpHeaders headers = createBasicHeaders(headerInfo);
    Map<String, String> uriParams = new HashMap<>();
    uriParams.put(URI_PARAM_REALM, realmName);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(
        null, headers);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    ResponseEntity<String[]> resourceSet = this.restTemplate
        .exchange(
            uriComponentsBuilder.scheme(keycloakScheme).host(keycloakHost).path(GET_RESOURCE_SET)
                .buildAndExpand(uriParams).toString(), HttpMethod.GET, request,
            String[].class);
    List<ResourceInfo> result = new ArrayList<>();
    //Second: Once all the resource sets have been retrieved, get information about everyone of them
    if (null != resourceSet && null != resourceSet.getBody()) {
      String[] resourcesetBody = resourceSet.getBody();
      if (null != resourcesetBody) {
        List<String> resources = Arrays.asList(resourcesetBody);
        if (null != resources && resources.size() > 0) {
          resources.forEach(resourceSetId -> {

            Map<String, String> uriRequestParam = new HashMap<>();
            uriRequestParam.put(URI_PARAM_REALM, realmName);
            uriRequestParam.put(URI_PARAM_RESOURCE_ID, resourceSetId);
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
            ResponseEntity<ResourceInfo> resource = this.restTemplate
                .exchange(
                    uriBuilder.scheme(keycloakScheme).host(keycloakHost).path(GET_RESOURCE_INFO)
                        .buildAndExpand(uriRequestParam).toString(), HttpMethod.GET, request,
                    ResourceInfo.class);
            result.add(resource.getBody());
          });
        }
      }
    }
    return result;
  }

  private HttpHeaders createBasicHeaders(Map<String, String> headersInfo) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    if (null != headersInfo && headersInfo.size() > 0) {
      headersInfo.entrySet().forEach(entry -> headers.set(entry.getKey(), entry.getValue()));
    }
    return headers;
  }


}
