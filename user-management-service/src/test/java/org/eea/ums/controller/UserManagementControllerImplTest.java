package org.eea.ums.controller;

import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.ums.service.SecurityProviderInterfaceService;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

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
    Mockito.when(securityProviderInterfaceService
        .checkAccessPermission("Dataflow", new AccessScopeEnum[]{AccessScopeEnum.CREATE}))
        .thenReturn(true);
    AccessScopeEnum[] scopes = new AccessScopeEnum[]{AccessScopeEnum.CREATE};
    boolean checkedAccessPermission = userManagementController
        .checkResourceAccessPermission("Dataflow", scopes);
    Assert.assertTrue(checkedAccessPermission);
  }

}