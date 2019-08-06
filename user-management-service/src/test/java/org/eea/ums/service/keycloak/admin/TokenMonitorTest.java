package org.eea.ums.service.keycloak.admin;

import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.List;
import java.util.concurrent.ExecutorService;
import org.eea.ums.service.keycloak.model.ResourceInfo;
import org.eea.ums.service.keycloak.service.impl.KeycloakConnectorServiceImpl;
import org.junit.Before;
import org.junit.BeforeClass;
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

  @Mock
  private ExecutorService executorService;

  @Mock
  private TokenGeneratorThread tokenGeneratorThread;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }


  @Test
  public void startTokenGeneratorThread() {
    ReflectionTestUtils
        .invokeMethod(tokenMonitor, "startTokenGeneratorThread");
    Mockito.verify(executorService, times(1)).submit(Mockito.any(TokenGeneratorThread.class));
  }

  @Test
  public void destroy() {
    ReflectionTestUtils.setField(tokenMonitor, "tokenGeneratorThread", tokenGeneratorThread);
    Mockito.when(executorService.isShutdown()).thenReturn(false);
    tokenMonitor.destroy();
    Mockito.verify(tokenGeneratorThread, Mockito.times(1)).stopThread();
    Mockito.verify(executorService, Mockito.times(1)).shutdown();
  }

}