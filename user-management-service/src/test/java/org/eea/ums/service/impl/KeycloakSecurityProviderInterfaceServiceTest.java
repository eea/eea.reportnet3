package org.eea.ums.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

public class KeycloakSecurityProviderInterfaceServiceTest {

  @InjectMocks
  private KeycloakSecurityProviderInterfaceService keycloakSecurityProviderInterfaceService;
  @Mock
  private KeycloakConnectorService keycloakConnectorService;

  @Mock
  private GroupInfoMapper groupInfoMapper;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private RedisTemplate<String, CacheTokenVO> securityRedisTemplate;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

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
    Mockito.when(jwtTokenProvider.parseToken("token")).thenReturn(tokenDataVO);
    Mockito.when(keycloakConnectorService.generateToken(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(tokenInfo);
    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);

    Mockito.when(securityRedisTemplate.opsForValue()).thenReturn(operations);
    TokenVO token = keycloakSecurityProviderInterfaceService.doLogin("user1", "1234");
    Assert.assertNotNull(token);
    Assert.assertNotNull(token.getAccessToken());
  }

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
    Mockito.when(jwtTokenProvider.parseToken("token")).thenReturn(tokenDataVO);
    Mockito
        .when(keycloakConnectorService.generateAdminToken(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(tokenInfo);
    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);

    Mockito.when(securityRedisTemplate.opsForValue()).thenReturn(operations);

    TokenVO token = keycloakSecurityProviderInterfaceService.doLogin("user1", "1234", true);
    Assert.assertNotNull(token);
    Assert.assertNotNull(token.getAccessToken());
  }

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
    Mockito.when(jwtTokenProvider.parseToken("token")).thenReturn(tokenDataVO);
    Mockito.when(keycloakConnectorService.generateToken(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(tokenInfo);
    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);

    Mockito.when(securityRedisTemplate.opsForValue()).thenReturn(operations);

    TokenVO token = keycloakSecurityProviderInterfaceService.doLogin("user1", "1234", false);
    Assert.assertNotNull(token);
    Assert.assertNotNull(token.getAccessToken());
  }

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
    Mockito.when(jwtTokenProvider.parseToken("token")).thenReturn(tokenDataVO);
    Mockito.when(keycloakConnectorService.generateToken(Mockito.anyString())).thenReturn(tokenInfo);
    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);

    Mockito.when(securityRedisTemplate.opsForValue()).thenReturn(operations);

    TokenVO token = keycloakSecurityProviderInterfaceService.doLogin("code");
    Assert.assertNotNull(token);
    Assert.assertNotNull(token.getAccessToken());
  }

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

    Mockito.when(securityRedisTemplate.opsForValue()).thenReturn(operations);

    Mockito.when(jwtTokenProvider.parseToken("token")).thenReturn(tokenDataVO);
    Mockito.when(keycloakConnectorService.refreshToken(Mockito.anyString())).thenReturn(tokenInfo);
    TokenVO token = keycloakSecurityProviderInterfaceService.refreshToken("1234");
    Assert.assertNotNull(token);
    Assert.assertNotNull(token.getAccessToken());
  }

  @Test
  public void checkAccessPermission() {
    Mockito.when(keycloakConnectorService.checkUserPermision("Dataflow",
        new AccessScopeEnum[] {AccessScopeEnum.CREATE})).thenReturn("PERMIT");
    AccessScopeEnum[] scopes = new AccessScopeEnum[] {AccessScopeEnum.CREATE};
    boolean checkedAccessPermission =
        keycloakSecurityProviderInterfaceService.checkAccessPermission("Dataflow", scopes);
    Assert.assertTrue(checkedAccessPermission);
  }


  @Test(expected = UnsupportedOperationException.class)
  public void getUsers() {
    keycloakSecurityProviderInterfaceService.getUsers("");

  }

  @Test
  public void createResourceInstance() throws EEAException {
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    resourceInfoVO.setResourceId(1l);
    resourceInfoVO.setSecurityRoleEnum(SecurityRoleEnum.DATA_PROVIDER);
    resourceInfoVO.setResourceTypeEnum(ResourceTypeEnum.DATAFLOW);

    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);
    CacheTokenVO tokenVO = new CacheTokenVO();

    Mockito.when(securityRedisTemplate.opsForValue()).thenReturn(operations);
    Mockito.when(operations.get(Mockito.anyString())).thenReturn(tokenVO);
    keycloakSecurityProviderInterfaceService.createResourceInstance(resourceInfoVO);
    Mockito.verify(this.keycloakConnectorService, Mockito.times(1))
        .createGroupDetail(Mockito.any(GroupInfo.class));
  }


  @Test(expected = UnsupportedOperationException.class)
  public void removeUserFromUserGroup() {
    keycloakSecurityProviderInterfaceService.removeUserFromUserGroup("", "");
  }

  @Test
  public void getResourcesByUser() {
    GroupInfo[] groupInfos = new GroupInfo[1];
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setId("1");
    groupInfo.setName("Dataflow-1-DATA_PROVIDER");
    groupInfo.setPath("/path");
    groupInfos[0] = groupInfo;
    Mockito.when(keycloakConnectorService.getGroupsByUser(Mockito.anyString()))
        .thenReturn(groupInfos);
    ValueOperations<String, CacheTokenVO> operations = Mockito.mock(ValueOperations.class);
    CacheTokenVO tokenVO = new CacheTokenVO();

    Mockito.when(securityRedisTemplate.opsForValue()).thenReturn(operations);
    Mockito.when(operations.get(Mockito.anyString())).thenReturn(tokenVO);
    List<ResourceAccessVO> result =
        keycloakSecurityProviderInterfaceService.getResourcesByUser("user1");
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals(ResourceTypeEnum.DATAFLOW, result.get(0).getResource());
    Assert.assertEquals(SecurityRoleEnum.DATA_PROVIDER, result.get(0).getRole());
  }

  @Test
  public void doLogout() {
    keycloakSecurityProviderInterfaceService.doLogout("authToken");
    Mockito.verify(keycloakConnectorService, Mockito.times(1)).logout("authToken");
  }

  @Test
  public void addUserToUserGroup() throws EEAException {
    GroupInfo[] groupInfos = new GroupInfo[1];
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setId("idGroupInfo");
    groupInfo.setName("Dataflow-1-DATA_CUSTODIAN");
    groupInfos[0] = groupInfo;
    Mockito.when(keycloakConnectorService.getGroups()).thenReturn(groupInfos);
    keycloakSecurityProviderInterfaceService.addUserToUserGroup("user1",
        "DATAFLOW-1-DATA_CUSTODIAN");
    Mockito.verify(keycloakConnectorService, Mockito.times(1)).addUserToGroup("user1",
        "idGroupInfo");
  }

  @Test
  public void getGroupDetail() {
    GroupInfo[] groupInfos = new GroupInfo[1];
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setId("idGroupInfo");
    groupInfo.setName("Dataflow-1-DATA_CUSTODIAN");
    groupInfos[0] = groupInfo;
    Mockito.when(keycloakConnectorService.getGroups()).thenReturn(groupInfos);
    Mockito.when(keycloakConnectorService.getGroupDetail("idGroupInfo")).thenReturn(groupInfo);

    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    resourceInfoVO.setResourceId(1l);
    resourceInfoVO.setName("Dataflow-1-DATA_CUSTODIAN");
    Mockito.when(groupInfoMapper.entityToClass(Mockito.any(GroupInfo.class)))
        .thenReturn(resourceInfoVO);
    ResourceInfoVO result = this.keycloakSecurityProviderInterfaceService
        .getResourceDetails("Dataflow-1-DATA_CUSTODIAN");
    Assert.assertNotNull(result);
    Assert.assertEquals(result.getName(), resourceInfoVO.getName());
    Assert.assertEquals(result.getResourceTypeEnum(), ResourceTypeEnum.DATAFLOW);
    Assert.assertEquals(result.getSecurityRoleEnum(), SecurityRoleEnum.DATA_CUSTODIAN);
  }

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
    Mockito.when(keycloakConnectorService.getGroups()).thenReturn(groupInfos);
    keycloakSecurityProviderInterfaceService.deleteResourceInstances(resourceInfoVOs);
    Mockito.verify(keycloakConnectorService, Mockito.times(1)).getGroups();
    Mockito.verify(keycloakConnectorService, Mockito.times(1)).deleteGroupDetail("idGroupInfo");
  }

  @Test
  public void deleteResourceInstancesByName() {
    List<String> resourceNames = new ArrayList<>();
    resourceNames.add("Dataflow-1-DATA_CUSTODIAN");

    GroupInfo[] groupInfos = new GroupInfo[1];
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setId("idGroupInfo");
    groupInfo.setName("Dataflow-1-DATA_CUSTODIAN");
    groupInfos[0] = groupInfo;
    Mockito.when(keycloakConnectorService.getGroups()).thenReturn(groupInfos);
    keycloakSecurityProviderInterfaceService.deleteResourceInstancesByName(resourceNames);
    Mockito.verify(keycloakConnectorService, Mockito.times(1)).getGroups();
    Mockito.verify(keycloakConnectorService, Mockito.times(1)).deleteGroupDetail("idGroupInfo");
  }

}
