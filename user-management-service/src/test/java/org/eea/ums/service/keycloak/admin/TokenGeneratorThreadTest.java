package org.eea.ums.service.keycloak.admin;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.eea.ums.service.keycloak.model.TokenInfo;
import org.eea.ums.service.keycloak.service.impl.KeycloakConnectorServiceImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class TokenGeneratorThreadTest {

  private TokenGeneratorThread tokenGeneratorThread;
  @Mock
  private KeycloakConnectorServiceImpl keycloakConnectorService;
  ExecutorService executor = Executors.newSingleThreadExecutor();

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    tokenGeneratorThread = new TokenGeneratorThread(keycloakConnectorService, "test", "1234",
        1000l);
  }


  @Test
  public void run() throws InterruptedException {
    TokenInfo tokenInfo = new TokenInfo();
    tokenInfo.setAccessToken("token");
    Mockito
        .when(keycloakConnectorService.generateAdminToken(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(tokenInfo);
    Mockito.when(keycloakConnectorService.refreshToken(Mockito.anyString()))
        .thenReturn(tokenInfo);

    executor.submit(tokenGeneratorThread);
    executor.awaitTermination(2, TimeUnit.SECONDS);
    executor.shutdown();
    Assert.assertEquals(TokenMonitor.getToken(), "token");
  }


  @Test
  public void stopThread() {
    tokenGeneratorThread.stopThread();
    Assert.assertTrue((Boolean) ReflectionTestUtils.getField(tokenGeneratorThread, "exit"));
  }
}