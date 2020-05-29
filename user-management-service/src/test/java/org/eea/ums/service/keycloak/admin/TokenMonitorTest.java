package org.eea.ums.service.keycloak.admin;

import org.eea.ums.service.keycloak.model.TokenInfo;
import org.eea.ums.service.keycloak.service.impl.KeycloakConnectorServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class TokenMonitorTest {

  @InjectMocks
  private TokenMonitor tokenMonitor;

  @Mock
  private KeycloakConnectorServiceImpl keycloakConnectorService;


  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }


  @Test
  public void getTokenRefreshing() {
    TokenInfo tokenInfo = new TokenInfo();
    tokenInfo.setAccessToken("accessToken");
    tokenInfo.setRefreshToken("refreshToken2");
    ReflectionTestUtils.setField(tokenMonitor, "refreshToken", "refreshToken");
    ReflectionTestUtils.setField(tokenMonitor, "tokenExpirationTime", 300000l);

    Mockito.when(keycloakConnectorService.refreshToken(Mockito.eq("refreshToken")))
        .thenReturn(tokenInfo);
    String result = tokenMonitor.getToken();
    Assert.assertNotNull(result);
    Assert.assertEquals("accessToken", result);
    Assert.assertEquals(ReflectionTestUtils.getField(tokenMonitor, "refreshToken").toString(),
        "refreshToken2");
  }

  @Test
  public void getTokenNoRefreshing() {
    TokenInfo tokenInfo = new TokenInfo();
    tokenInfo.setAccessToken("accessToken");
    tokenInfo.setRefreshToken("refreshToken2");
    ReflectionTestUtils.setField(tokenMonitor, "refreshToken", "refreshToken");
    ReflectionTestUtils.setField(tokenMonitor, "tokenExpirationTime", Long.MAX_VALUE);
    ReflectionTestUtils.setField(tokenMonitor, "lastUpdateTime", System.currentTimeMillis());

    ReflectionTestUtils.setField(tokenMonitor, "adminToken", "accessToken");

    String result = tokenMonitor.getToken();
    Assert.assertNotNull(result);
    Assert.assertEquals("accessToken", result);
    Assert.assertEquals(ReflectionTestUtils.getField(tokenMonitor, "refreshToken").toString(),
        "refreshToken");
    Mockito.verify(keycloakConnectorService, Mockito.times(0)).refreshToken(Mockito.anyString());
  }


}
