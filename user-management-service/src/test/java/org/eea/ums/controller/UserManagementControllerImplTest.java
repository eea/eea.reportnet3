package org.eea.ums.controller;

import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
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
    Mockito.when(securityProviderInterfaceService.doLogin(Mockito.anyString(), Mockito.anyString()))
        .thenReturn("token");
    String token = userManagementController.generateToken("", "");
    Assert.assertNotNull(token);
    Assert.assertEquals("token", token);
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