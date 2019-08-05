package org.eea.ums.service.impl;

import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
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

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void doLogin() {
    Mockito.when(keycloakConnectorService.generateToken(Mockito.anyString(), Mockito.anyString()))
        .thenReturn("token");
    String token = keycloakSecurityProviderInterfaceService.doLogin("user1", "1234");
    Assert.assertNotNull(token);
    Assert.assertEquals("token", token);
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
  public void getUserGroupInfo() {
    keycloakSecurityProviderInterfaceService.getUserGroupInfo("");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getUsers() {
    keycloakSecurityProviderInterfaceService.getUsers("", "");

  }

  @Test(expected = UnsupportedOperationException.class)
  public void createUserGroup() {
    keycloakSecurityProviderInterfaceService.createUserGroup("", "", null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void addUserToUserGroup() {
    keycloakSecurityProviderInterfaceService.addUserToUserGroup("", "", "");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void removeUserFromUserGroup() {
    keycloakSecurityProviderInterfaceService.removeUserFromUserGroup("", "", "");
  }
}