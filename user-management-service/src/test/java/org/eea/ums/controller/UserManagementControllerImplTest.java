package org.eea.ums.controller;

import java.util.HashMap;
import java.util.Map;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.interfaces.vo.ums.enums.ResourceEnum;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class UserManagementControllerImplTest {

  @InjectMocks
  private UserManagementControllerImpl userManagementController;
  @Mock
  private SecurityProviderInterfaceService securityProviderInterfaceService;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void generateTokenTest() {
    TokenVO tokenVO = new TokenVO();
    tokenVO.setAccessToken("token");
    Mockito.when(securityProviderInterfaceService.doLogin(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(tokenVO);
    TokenVO result = userManagementController.generateToken("", "");
    Assert.assertNotNull(result);
    Assert.assertEquals("token", result.getAccessToken());
  }

  @Test
  public void generateTokenByCodeTest() {
    TokenVO tokenVO = new TokenVO();
    tokenVO.setAccessToken("token");
    Mockito.when(securityProviderInterfaceService.doLogin(Mockito.anyString()))
        .thenReturn(tokenVO);
    TokenVO result = userManagementController.generateToken("");
    Assert.assertNotNull(result);
    Assert.assertEquals("token", result.getAccessToken());
  }

  @Test
  public void refreshTokenTest() {
    TokenVO tokenVO = new TokenVO();
    tokenVO.setAccessToken("token");
    Mockito.when(securityProviderInterfaceService.refreshToken(Mockito.anyString()))
        .thenReturn(tokenVO);
    TokenVO result = userManagementController.refreshToken("");
    Assert.assertNotNull(result);
    Assert.assertEquals("token", result.getAccessToken());
  }

  @Test
  public void checkResourceAccessPermissionTest() {
    Mockito.when(securityProviderInterfaceService.checkAccessPermission("Dataflow",
        new AccessScopeEnum[]{AccessScopeEnum.CREATE})).thenReturn(true);
    AccessScopeEnum[] scopes = new AccessScopeEnum[]{AccessScopeEnum.CREATE};
    boolean checkedAccessPermission =
        userManagementController.checkResourceAccessPermission("Dataflow", scopes);
    Assert.assertTrue(checkedAccessPermission);
  }

  @Test
  public void doLogOut() {

    userManagementController.doLogOut("refreshToken");
    Mockito.verify(securityProviderInterfaceService, Mockito.times(1))
        .doLogout(Mockito.anyString());
  }

  @Test
  public void addContributorToResource() {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    userManagementController.addContributorToResource(1l, ResourceGroupEnum.DATAFLOW_CUSTODIAN);
    Mockito.verify(securityProviderInterfaceService, Mockito.times(1))
        .addUserToUserGroup("userId_123", ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(1l));
  }

  @Test
  public void testGetResourcesByUser() {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    userManagementController.getResourcesByUser();

  }

  @Test
  public void testGetResourcesByUser2() {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    userManagementController.getResourcesByUser(ResourceEnum.DATAFLOW);

  }

  @Test
  public void testGetResourcesByUser3() {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    userManagementController.getResourcesByUser(SecurityRoleEnum.DATA_PROVIDER);

  }

  @Test
  public void testGetResourcesByUser4() {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken("user1", null, null);
    Map<String, String> details = new HashMap<>();
    details.put("userId", "userId_123");
    authenticationToken.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    userManagementController.getResourcesByUser(ResourceEnum.DATAFLOW,
        SecurityRoleEnum.DATA_PROVIDER);

  }


  @Test
  public void getGroupDetail() {
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    Mockito.when(securityProviderInterfaceService
        .getGroupDetail(ResourceGroupEnum.DATAFLOW_PROVIDER.getGroupName(1l)))
        .thenReturn(resourceInfoVO);
    ResourceInfoVO result = userManagementController
        .getResourceDetail(1L, ResourceGroupEnum.DATAFLOW_PROVIDER);
    Assert.assertNotNull(result);
  }
}
