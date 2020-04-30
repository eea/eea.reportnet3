package org.eea.ums.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.security.jwt.data.CacheTokenVO;
import org.eea.security.jwt.data.TokenDataVO;
import org.eea.security.jwt.utils.JwtTokenProvider;
import org.eea.ums.mapper.GroupInfoMapper;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * The Class KeycloakSecurityProviderInterfaceServiceTest.
 */
public class KeycloakSecurityProviderInterfaceServiceTest {

  /** The keycloak security provider interface service. */
  @InjectMocks
  private KeycloakSecurityProviderInterfaceService keycloakSecurityProviderInterfaceService;

  /** The keycloak connector service. */
  @Mock
  private KeycloakConnectorService keycloakConnectorService;

  /** The group info mapper. */
  @Mock
  private GroupInfoMapper groupInfoMapper;

  /** The jwt token provider. */
  @Mock
  private JwtTokenProvider jwtTokenProvider;

  /** The security redis template. */
  @Mock
  private RedisTemplate<String, CacheTokenVO> securityRedisTemplate;

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Do login.
   *
   * @throws VerificationException the verification exception
   */
  @Test
  public void doLogin() throws VerificationException {
    TokenInfo tokenInfo = new TokenInfo();
    tokenInfo.setAccessToken("token");
    tokenInfo.setRefreshExpiresIn(System.currentTimeMillis());
    TokenDataVO tokenDataVO = new TokenDataVO();
    Map<String, Object> claims = new HashMap<>();
    List<String> roles = new ArrayList<>();
    roles.add("/DATA_PROVIDER");
    claims.put("user_groups", roles);
    tokenDataVO.setOtherClaims(claims);
    when(jwtTokenProvider.parseToken("token")).thenReturn(tokenDataVO);
    when(keycloakConnectorService.generateToken(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(tokenInfo);
    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);

    when(securityRedisTemplate.opsForValue()).thenReturn(operations);
    TokenVO token = keycloakSecurityProviderInterfaceService.doLogin("user1", "1234");
    Assert.assertNotNull(token);
    Assert.assertNotNull(token.getAccessToken());
  }

  /**
   * Do admin login.
   *
   * @throws VerificationException the verification exception
   */
  @Test
  public void doAdminLogin() throws VerificationException {
    TokenInfo tokenInfo = new TokenInfo();
    tokenInfo.setAccessToken("token");
    tokenInfo.setRefreshExpiresIn(System.currentTimeMillis());
    TokenDataVO tokenDataVO = new TokenDataVO();
    Map<String, Object> claims = new HashMap<>();
    List<String> roles = new ArrayList<>();
    roles.add("/DATA_PROVIDER");
    claims.put("user_groups", roles);
    tokenDataVO.setOtherClaims(claims);
    when(jwtTokenProvider.parseToken("token")).thenReturn(tokenDataVO);
    when(keycloakConnectorService.generateAdminToken(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(tokenInfo);
    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);

    when(securityRedisTemplate.opsForValue()).thenReturn(operations);

    TokenVO token = keycloakSecurityProviderInterfaceService.doLogin("user1", "1234", true);
    Assert.assertNotNull(token);
    Assert.assertNotNull(token.getAccessToken());
  }

  /**
   * Do not admin login.
   *
   * @throws VerificationException the verification exception
   */
  @Test
  public void doNotAdminLogin() throws VerificationException {
    TokenInfo tokenInfo = new TokenInfo();
    tokenInfo.setAccessToken("token");
    tokenInfo.setRefreshExpiresIn(System.currentTimeMillis());
    TokenDataVO tokenDataVO = new TokenDataVO();
    Map<String, Object> claims = new HashMap<>();
    List<String> roles = new ArrayList<>();
    roles.add("/DATA_PROVIDER");
    claims.put("user_groups", roles);
    tokenDataVO.setOtherClaims(claims);
    when(jwtTokenProvider.parseToken("token")).thenReturn(tokenDataVO);
    when(keycloakConnectorService.generateToken(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(tokenInfo);
    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);

    when(securityRedisTemplate.opsForValue()).thenReturn(operations);

    TokenVO token = keycloakSecurityProviderInterfaceService.doLogin("user1", "1234", false);
    Assert.assertNotNull(token);
    Assert.assertNotNull(token.getAccessToken());
  }

  /**
   * Do login by code.
   *
   * @throws VerificationException the verification exception
   */
  @Test
  public void doLoginByCode() throws VerificationException {
    TokenInfo tokenInfo = new TokenInfo();
    tokenInfo.setAccessToken("token");
    tokenInfo.setRefreshExpiresIn(System.currentTimeMillis());
    TokenDataVO tokenDataVO = new TokenDataVO();

    Map<String, Object> claims = new HashMap<>();
    List<String> roles = new ArrayList<>();
    roles.add("/DATA_PROVIDER");
    claims.put("user_groups", roles);
    tokenDataVO.setOtherClaims(claims);
    when(jwtTokenProvider.parseToken("token")).thenReturn(tokenDataVO);
    when(keycloakConnectorService.generateToken(Mockito.anyString())).thenReturn(tokenInfo);
    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);

    when(securityRedisTemplate.opsForValue()).thenReturn(operations);

    TokenVO token = keycloakSecurityProviderInterfaceService.doLogin("code");
    Assert.assertNotNull(token);
    Assert.assertNotNull(token.getAccessToken());
  }

  /**
   * Refresh token.
   *
   * @throws VerificationException the verification exception
   */
  @Test
  public void refreshToken() throws VerificationException {
    TokenInfo tokenInfo = new TokenInfo();
    tokenInfo.setAccessToken("token");
    tokenInfo.setRefreshExpiresIn(System.currentTimeMillis());
    TokenDataVO tokenDataVO = new TokenDataVO();
    Map<String, Object> claims = new HashMap<>();
    List<String> roles = new ArrayList<>();
    roles.add("/DATA_PROVIDER");
    claims.put("user_groups", roles);
    tokenDataVO.setOtherClaims(claims);
    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);

    when(securityRedisTemplate.opsForValue()).thenReturn(operations);

    when(jwtTokenProvider.parseToken("token")).thenReturn(tokenDataVO);
    when(keycloakConnectorService.refreshToken(Mockito.anyString())).thenReturn(tokenInfo);
    TokenVO token = keycloakSecurityProviderInterfaceService.refreshToken("1234");
    Assert.assertNotNull(token);
    Assert.assertNotNull(token.getAccessToken());
  }

  /**
   * Check access permission.
   */
  @Test
  public void checkAccessPermission() {
    when(keycloakConnectorService.checkUserPermision("Dataflow",
        new AccessScopeEnum[] {AccessScopeEnum.CREATE})).thenReturn("PERMIT");
    AccessScopeEnum[] scopes = new AccessScopeEnum[] {AccessScopeEnum.CREATE};
    boolean checkedAccessPermission =
        keycloakSecurityProviderInterfaceService.checkAccessPermission("Dataflow", scopes);
    Assert.assertTrue(checkedAccessPermission);
  }


  /**
   * Gets the users.
   *
   * @return the users
   */
  @Test(expected = UnsupportedOperationException.class)
  public void getUsers() {
    keycloakSecurityProviderInterfaceService.getUsers("");

  }

  /**
   * Creates the resource instance.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createResourceInstance() throws EEAException {
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    resourceInfoVO.setResourceId(1l);
    resourceInfoVO.setSecurityRoleEnum(SecurityRoleEnum.DATA_PROVIDER);
    resourceInfoVO.setResourceTypeEnum(ResourceTypeEnum.DATAFLOW);

    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);
    CacheTokenVO tokenVO = new CacheTokenVO();

    when(securityRedisTemplate.opsForValue()).thenReturn(operations);
    when(operations.get(Mockito.anyString())).thenReturn(tokenVO);
    keycloakSecurityProviderInterfaceService.createResourceInstance(resourceInfoVO);
    Mockito.verify(this.keycloakConnectorService, Mockito.times(1))
        .createGroupDetail(Mockito.any(GroupInfo.class));
  }


  /**
   * Removes the user from user group.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void removeUserFromUserGroup() {
    keycloakSecurityProviderInterfaceService.removeUserFromUserGroup("", "");
  }

  /**
   * Gets the resources by user.
   *
   * @return the resources by user
   */
  @Test
  public void getResourcesByUser() {
    GroupInfo[] groupInfos = new GroupInfo[1];
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setId("1");
    groupInfo.setName("Dataflow-1-DATA_PROVIDER");
    groupInfo.setPath("/path");
    groupInfos[0] = groupInfo;
    when(keycloakConnectorService.getGroupsByUser(Mockito.anyString())).thenReturn(groupInfos);
    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);
    CacheTokenVO tokenVO = new CacheTokenVO();

    when(securityRedisTemplate.opsForValue()).thenReturn(operations);
    when(operations.get(Mockito.anyString())).thenReturn(tokenVO);
    List<ResourceAccessVO> result =
        keycloakSecurityProviderInterfaceService.getResourcesByUser("user1");
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals(ResourceTypeEnum.DATAFLOW, result.get(0).getResource());
    Assert.assertEquals(SecurityRoleEnum.DATA_PROVIDER, result.get(0).getRole());
  }

  /**
   * Do logout.
   */
  @Test
  public void doLogout() {
    keycloakSecurityProviderInterfaceService.doLogout("authToken");
    Mockito.verify(keycloakConnectorService, Mockito.times(1)).logout("authToken");
  }

  /**
   * Adds the user to user group.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void addUserToUserGroup() throws EEAException {
    GroupInfo[] groupInfos = new GroupInfo[1];
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setId("idGroupInfo");
    groupInfo.setName("Dataflow-1-DATA_CUSTODIAN");
    groupInfos[0] = groupInfo;
    when(keycloakConnectorService.getGroups()).thenReturn(groupInfos);
    keycloakSecurityProviderInterfaceService.addUserToUserGroup("user1",
        "DATAFLOW-1-DATA_CUSTODIAN");
    Mockito.verify(keycloakConnectorService, Mockito.times(1)).addUserToGroup("user1",
        "idGroupInfo");
  }

  /**
   * Gets the group detail.
   *
   * @return the group detail
   */
  @Test
  public void getGroupDetail() {
    GroupInfo[] groupInfos = new GroupInfo[1];
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setId("idGroupInfo");
    groupInfo.setName("Dataflow-1-DATA_CUSTODIAN");
    groupInfos[0] = groupInfo;
    when(keycloakConnectorService.getGroups()).thenReturn(groupInfos);
    when(keycloakConnectorService.getGroupDetail("idGroupInfo")).thenReturn(groupInfo);

    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    resourceInfoVO.setResourceId(1l);
    resourceInfoVO.setName("Dataflow-1-DATA_CUSTODIAN");
    when(groupInfoMapper.entityToClass(Mockito.any(GroupInfo.class))).thenReturn(resourceInfoVO);
    ResourceInfoVO result = this.keycloakSecurityProviderInterfaceService
        .getResourceDetails("Dataflow-1-DATA_CUSTODIAN");
    Assert.assertNotNull(result);
    Assert.assertEquals(result.getName(), resourceInfoVO.getName());
    Assert.assertEquals(result.getResourceTypeEnum(), ResourceTypeEnum.DATAFLOW);
    Assert.assertEquals(result.getSecurityRoleEnum(), SecurityRoleEnum.DATA_CUSTODIAN);
  }

  /**
   * Delete resource instances.
   */
  @Test
  public void deleteResourceInstances() {
    List<ResourceInfoVO> resourceInfoVOs = new ArrayList<>();
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    resourceInfoVO.setName("Dataflow-1-DATA_CUSTODIAN");
    resourceInfoVOs.add(resourceInfoVO);
    GroupInfo[] groupInfos = new GroupInfo[1];
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setId("idGroupInfo");
    groupInfo.setName("Dataflow-1-DATA_CUSTODIAN");
    groupInfos[0] = groupInfo;
    when(keycloakConnectorService.getGroups()).thenReturn(groupInfos);
    keycloakSecurityProviderInterfaceService.deleteResourceInstances(resourceInfoVOs);
    Mockito.verify(keycloakConnectorService, Mockito.times(1)).getGroups();
    Mockito.verify(keycloakConnectorService, Mockito.times(1)).deleteGroupDetail("idGroupInfo");
  }

  /**
   * Delete resource instances by name.
   */
  @Test
  public void deleteResourceInstancesByName() {
    List<String> resourceNames = new ArrayList<>();
    resourceNames.add("Dataflow-1-DATA_CUSTODIAN");

    GroupInfo[] groupInfos = new GroupInfo[1];
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setId("idGroupInfo");
    groupInfo.setName("Dataflow-1-DATA_CUSTODIAN");
    groupInfos[0] = groupInfo;
    when(keycloakConnectorService.getGroups()).thenReturn(groupInfos);
    keycloakSecurityProviderInterfaceService.deleteResourceInstancesByName(resourceNames);
    Mockito.verify(keycloakConnectorService, Mockito.times(1)).getGroups();
    Mockito.verify(keycloakConnectorService, Mockito.times(1)).deleteGroupDetail("idGroupInfo");
  }


  /**
   * Update api key full attributes test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateApiKeyFullAttributesTest() throws EEAException {
    UserRepresentation user = new UserRepresentation();
    Map<String, List<String>> attributes = new HashMap<>();
    List<String> oldKeys = new ArrayList<>();
    oldKeys.add("uuid");
    attributes.put("ApiKeys", oldKeys);
    user.setAttributes(attributes);
    when(keycloakConnectorService.getUser(Mockito.anyString())).thenReturn(user);
    String key = keycloakSecurityProviderInterfaceService.getApiKey("userId", 1L, 1L);
    assertNotNull(key);
  }

  /**
   * Gets the api key empty test.
   *
   * @return the api key empty test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getApiKeyEmptyTest() throws EEAException {
    UserRepresentation user = new UserRepresentation();
    user.setAttributes(null);
    when(keycloakConnectorService.getUser(Mockito.anyString())).thenReturn(user);
    String key = keycloakSecurityProviderInterfaceService.getApiKey("userId", 1L, 1L);
    assertNotNull(key);
  }

  /**
   * Gets the api key attributes test.
   *
   * @return the api key attributes test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getApiKeyAttributesTest() throws EEAException {
    UserRepresentation user = new UserRepresentation();
    Map<String, List<String>> attributes = new HashMap<>();
    attributes.put("ApiKeys", new ArrayList<>());
    user.setAttributes(attributes);
    when(keycloakConnectorService.getUser(Mockito.anyString())).thenReturn(user);
    String key = keycloakSecurityProviderInterfaceService.getApiKey("userId", 1L, 1L);
    assertEquals("", key);
  }

  /**
   * Gets the api key full attributes test.
   *
   * @return the api key full attributes test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getApiKeyFullAttributesTest() throws EEAException {
    UserRepresentation user = new UserRepresentation();
    Map<String, List<String>> attributes = new HashMap<>();
    List<String> oldKeys = new ArrayList<>();
    oldKeys.add("uuid,1,1");
    attributes.put("ApiKeys", oldKeys);
    user.setAttributes(attributes);
    when(keycloakConnectorService.getUser(Mockito.anyString())).thenReturn(user);
    String key = keycloakSecurityProviderInterfaceService.getApiKey("userId", 1L, 1L);
    assertEquals("uuid", key);
  }

  /**
   * Creates the api key.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createApiKey() throws EEAException {
    UserRepresentation user = new UserRepresentation();
    Map<String, List<String>> attributes = new HashMap<>();
    List<String> oldKeys = new ArrayList<>();
    oldKeys.add("uuid,1,1");
    attributes.put("ApiKeys", oldKeys);
    user.setAttributes(attributes);
    when(keycloakConnectorService.getUser(Mockito.anyString())).thenReturn(user);
    String key = keycloakSecurityProviderInterfaceService.createApiKey("userId", 1L, 1L);
    assertNotNull(key);
  }

  /**
   * Creates the api key user not found error.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createApiKeyUserNotFoundError() throws EEAException {

    try {
      keycloakSecurityProviderInterfaceService.createApiKey("userId", 1L, 1L);
    } catch (EEAException e) {
      Assert.assertEquals(String.format(EEAErrorMessage.USER_NOTFOUND, "userId"), e.getMessage());
      throw e;
    }
  }

  /**
   * Authenticate api key.
   */
  @Test
  public void authenticateApiKey() {

    // Configuration of user representations for "userId1"
    UserRepresentation[] userRepresentations = new UserRepresentation[1];
    UserRepresentation userRepresentation = new UserRepresentation();
    userRepresentations[0] = userRepresentation;
    Map<String, List<String>> attributes = new HashMap<>();
    List<String> apiKey = new ArrayList<>();
    apiKey.add("ApiKey1,1,1");
    attributes.put("ApiKeys", apiKey);
    userRepresentation.setAttributes(attributes);
    userRepresentation.setId("userId1");
    userRepresentation.setUsername("userName1");
    when(keycloakConnectorService.getUsers()).thenReturn(userRepresentations);

    // Configuration of group info for the user "userId1"
    GroupInfo[] groupInfos = new GroupInfo[1];
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setName("Dataflow-1-DATA_PROVIDER");
    groupInfo.setPath("/Dataflow-1-DATA_PROVIDER");
    groupInfos[0] = groupInfo;
    when(keycloakConnectorService.getGroupsByUser(Mockito.eq("userId1"))).thenReturn(groupInfos);

    // Configuration of user roles for user "userId1"
    RoleRepresentation[] roleRepresentations = new RoleRepresentation[1];
    RoleRepresentation roleRepresentation = new RoleRepresentation();
    roleRepresentation.setName("DATA_PROVIDER");
    roleRepresentations[0] = roleRepresentation;
    when(keycloakConnectorService.getUserRoles(Mockito.eq("userId1")))
        .thenReturn(roleRepresentations);

    TokenVO result = keycloakSecurityProviderInterfaceService.authenticateApiKey("ApiKey1");
    Assert.assertNotNull(result);
    Assert.assertEquals(result.getUserId(), "userId1");
    Assert.assertEquals(result.getPreferredUsername(), "userName1");
    Assert.assertEquals(result.getGroups().size(), 1);
    Assert.assertEquals(result.getRoles().size(), 1);
    Assert.assertEquals(result.getGroups().iterator().next(), "Dataflow-1-DATA_PROVIDER");
    Assert.assertEquals(result.getRoles().iterator().next(), "DATA_PROVIDER");

  }

  /**
   * Authenticate api key wrong api.
   */
  @Test
  public void authenticateApiKeyWrongApi() {

    // Configuration of user representations for "userId1"
    UserRepresentation[] userRepresentations = new UserRepresentation[1];
    UserRepresentation userRepresentation = new UserRepresentation();
    userRepresentations[0] = userRepresentation;
    userRepresentation.setId("userId1");
    userRepresentation.setUsername("userName1");
    when(keycloakConnectorService.getUsers()).thenReturn(userRepresentations);

    TokenVO result = keycloakSecurityProviderInterfaceService.authenticateApiKey("ApiKey1");
    Assert.assertNull(result);
  }

  /**
   * Gets the user without keys success test.
   *
   * @return the user without keys success test
   */
  @Test
  public void getUserWithoutKeysSuccessTest() {
    UserRepresentation user = new UserRepresentation();
    Map<String, List<String>> attributes = new HashMap<>();
    List<String> oldKeys = new ArrayList<>();
    oldKeys.add("uuid,1,1");
    attributes.put("ApiKeys", oldKeys);
    user.setAttributes(attributes);
    when(keycloakConnectorService.getUser(Mockito.any())).thenReturn(user);
    assertEquals(user, keycloakSecurityProviderInterfaceService.getUserWithoutKeys(""));
  }

  /**
   * Gets the user without keys empty attibutes test.
   *
   * @return the user without keys empty attibutes test
   */
  @Test
  public void getUserWithoutKeysEmptyAttibutesTest() {
    UserRepresentation user = new UserRepresentation();
    Map<String, List<String>> attributes = new HashMap<>();
    user.setAttributes(attributes);
    when(keycloakConnectorService.getUser(Mockito.any())).thenReturn(user);
    assertEquals(user, keycloakSecurityProviderInterfaceService.getUserWithoutKeys(""));
  }

  /**
   * Gets the user without keys user null test.
   *
   * @return the user without keys user null test
   */
  @Test
  public void getUserWithoutKeysUserNullTest() {
    when(keycloakConnectorService.getUser(Mockito.any())).thenReturn(null);
    assertEquals(null, keycloakSecurityProviderInterfaceService.getUserWithoutKeys(""));
  }


  /**
   * Sets the attributes with api key test.
   */
  @Test
  public void setAttributesWithApiKeyTest() {
    UserRepresentation user = new UserRepresentation();
    Map<String, List<String>> attributes = new HashMap<>();
    List<String> oldKeys = new ArrayList<>();
    oldKeys.add("uuid,1,1");
    attributes.put("ApiKeys", oldKeys);
    user.setAttributes(attributes);
    assertEquals(user,
        keycloakSecurityProviderInterfaceService.setAttributesWithApiKey(user, attributes));
  }
}
