package org.eea.ums.service.impl;

import java.util.List;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.ums.mapper.GroupInfoMapper;
import org.eea.ums.service.keycloak.model.GroupInfo;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class KeycloakSecurityProviderInterfaceServiceTest {

  @InjectMocks
  private KeycloakSecurityProviderInterfaceService keycloakSecurityProviderInterfaceService;
  @Mock
  private KeycloakConnectorService keycloakConnectorService;

  @Mock
  private GroupInfoMapper groupInfoMapper;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void doLogin() {
    TokenInfo tokenInfo = new TokenInfo();
    tokenInfo.setAccessToken("token");
    Mockito.when(keycloakConnectorService.generateToken(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(tokenInfo);
    TokenVO token = keycloakSecurityProviderInterfaceService.doLogin("user1", "1234");
    Assert.assertNotNull(token);
    Assert.assertEquals("token", token.getAccessToken());
  }

  @Test
  public void doLoginByCode() {
    TokenInfo tokenInfo = new TokenInfo();
    tokenInfo.setAccessToken("token");
    Mockito.when(keycloakConnectorService.generateToken(Mockito.anyString()))
        .thenReturn(tokenInfo);
    TokenVO token = keycloakSecurityProviderInterfaceService.doLogin("code");
    Assert.assertNotNull(token);
    Assert.assertEquals("token", token.getAccessToken());
  }

  @Test
  public void refreshToken() {
    TokenInfo tokenInfo = new TokenInfo();
    tokenInfo.setAccessToken("token");
    Mockito.when(keycloakConnectorService.refreshToken(Mockito.anyString()))
        .thenReturn(tokenInfo);
    TokenVO token = keycloakSecurityProviderInterfaceService.refreshToken("1234");
    Assert.assertNotNull(token);
    Assert.assertEquals("token", token.getAccessToken());
  }

  @Test
  public void checkAccessPermission() {
    Mockito.when(keycloakConnectorService
        .checkUserPermision("Dataflow", new AccessScopeEnum[]{AccessScopeEnum.CREATE}))
        .thenReturn("PERMIT");
    AccessScopeEnum[] scopes = new AccessScopeEnum[]{AccessScopeEnum.CREATE};
    boolean checkedAccessPermission = keycloakSecurityProviderInterfaceService
        .checkAccessPermission("Dataflow", scopes);
    Assert.assertTrue(checkedAccessPermission);
  }


  @Test(expected = UnsupportedOperationException.class)
  public void getUsers() {
    keycloakSecurityProviderInterfaceService.getUsers("");

  }

  @Test
  public void createResourceInstance() {
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    resourceInfoVO.setResourceId(1l);
    resourceInfoVO.setSecurityRoleEnum(SecurityRoleEnum.DATA_PROVIDER);
    resourceInfoVO.setResourceTypeEnum(ResourceTypeEnum.DATAFLOW);

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
    Mockito.when(keycloakConnectorService
        .getGroupsByUser(Mockito.anyString()))
        .thenReturn(groupInfos);
    List<ResourceAccessVO> result = keycloakSecurityProviderInterfaceService
        .getResourcesByUser("user1");
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals(result.get(0).getResource(), ResourceTypeEnum.DATAFLOW);
    Assert.assertEquals(result.get(0).getRole(), SecurityRoleEnum.DATA_PROVIDER);
  }

  @Test
  public void doLogout() {
    keycloakSecurityProviderInterfaceService.doLogout("refreshToken");
    Mockito.verify(keycloakConnectorService, Mockito.times(1))
        .logout(Mockito.anyString());
  }

  @Test
  public void addUserToUserGroup() {
    GroupInfo[] groupInfos = new GroupInfo[1];
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setId("idGroupInfo");
    groupInfo.setName("Dataflow-1-DATA_CUSTODIAN");
    groupInfos[0] = groupInfo;
    Mockito
        .when(keycloakConnectorService.getGroups()).thenReturn(groupInfos);
    keycloakSecurityProviderInterfaceService
        .addUserToUserGroup("user1", "DATAFLOW-1-DATA_CUSTODIAN");
    Mockito.verify(keycloakConnectorService, Mockito.times(1))
        .addUserToGroup("user1", "idGroupInfo");
  }

  @Test
  public void getGroupDetail() {
    GroupInfo[] groupInfos = new GroupInfo[1];
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setId("idGroupInfo");
    groupInfo.setName("Dataflow-1-DATA_CUSTODIAN");
    groupInfos[0] = groupInfo;
    Mockito
        .when(keycloakConnectorService.getGroups()).thenReturn(groupInfos);
    Mockito
        .when(keycloakConnectorService.getGroupDetail("idGroupInfo")).thenReturn(groupInfo);

    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    resourceInfoVO.setResourceId(1l);
    resourceInfoVO.setName("Dataflow-1-DATA_CUSTODIAN");
    Mockito.when(groupInfoMapper.entityToClass(Mockito.any(GroupInfo.class)))
        .thenReturn(resourceInfoVO);
    ResourceInfoVO result = this.keycloakSecurityProviderInterfaceService
        .getResourceDetails("Dataflow-1-DATA_CUSTODIAN");
    Assert.assertNotNull(result);
    Assert.assertEquals(((ResourceInfoVO) result).getName(), resourceInfoVO.getName());
    Assert.assertEquals(((ResourceInfoVO) result).getResourceTypeEnum(), ResourceTypeEnum.DATAFLOW);
    Assert.assertEquals(((ResourceInfoVO) result).getSecurityRoleEnum(),
        SecurityRoleEnum.DATA_CUSTODIAN);
  }
}