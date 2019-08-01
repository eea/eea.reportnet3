package org.eea.ums.service.keycloak.admin;

import org.eea.ums.service.keycloak.service.impl.KeycloakConnectorServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TokenMonitorTest {

  @InjectMocks
  private TokenMonitor tokenMonitor;

  @Mock
  private KeycloakConnectorServiceImpl keycloakConnectorService;


  @Test
  public void destroy() {
  }

  @Test
  public void updateAdminToken() {
  }

  @Test
  public void getToken() {
  }
}