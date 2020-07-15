package org.eea.dataflow.controller.fme;

import static org.mockito.Mockito.times;
import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
import org.eea.interfaces.vo.integration.fme.FMEOperationInfoVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class FMEControllerImplTest {

  @InjectMocks
  private FMEControllerImpl fmeControllerImpl;

  @Mock
  private FMECommunicationService fmeCommunicationService;

  private SecurityContext securityContext;

  private Authentication authentication;

  @Before
  public void initMocks() {
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void findRepositoriesTest() {
    Mockito.when(fmeCommunicationService.findRepository()).thenReturn(null);
    fmeControllerImpl.findRepositories(1L);
    Mockito.verify(fmeCommunicationService, times(1)).findRepository();
  }

  @Test
  public void findItemsTest() {
    Mockito.when(fmeCommunicationService.findItems(Mockito.anyString())).thenReturn(null);
    fmeControllerImpl.findItems(1L, "repository");
    Mockito.verify(fmeCommunicationService, times(1)).findItems(Mockito.anyString());
  }

  @Test
  public void operationFinishedTest() {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.doNothing().when(fmeCommunicationService).operationFinished(Mockito.any());
    fmeControllerImpl.operationFinished(new FMEOperationInfoVO());
    Mockito.verify(fmeCommunicationService, times(1)).operationFinished(Mockito.any());
  }
}
