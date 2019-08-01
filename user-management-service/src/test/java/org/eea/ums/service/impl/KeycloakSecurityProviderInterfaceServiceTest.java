package org.eea.ums.service.impl;

import static org.junit.Assert.*;

import org.eea.interfaces.vo.ums.enums.AccessScopeEnum;
import org.eea.ums.service.keycloak.service.KeycloakConnectorService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

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

  @Test
  public void getUserGroupInfo() {
  }

  @Test
  public void getUsers() {
  }

  @Test
  public void createUserGroup() {
  }

  @Test
  public void addUserToUserGroup() {
  }

  @Test
  public void removeUserFromUserGroup() {
  }
}