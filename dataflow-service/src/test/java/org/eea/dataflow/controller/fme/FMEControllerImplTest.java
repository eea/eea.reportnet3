package org.eea.dataflow.controller.fme;

import static org.mockito.Mockito.times;
import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
import org.eea.dataflow.persistence.domain.FMEJob;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.integration.fme.FMEOperationInfoVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

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
  public void operationFinishedTest() throws EEAException {
    FMEOperationInfoVO fmeOperationInfoVO = new FMEOperationInfoVO();
    fmeOperationInfoVO.setApiKey("sampleApiKey");
    fmeOperationInfoVO.setFmeJobId(1L);
    Mockito.when(
        fmeCommunicationService.authenticateAndAuthorize(Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(new FMEJob());
    Mockito.doNothing().when(fmeCommunicationService).releaseNotifications(Mockito.any(),
        Mockito.anyLong());
    Mockito.doNothing().when(fmeCommunicationService).updateJobStatus(Mockito.any(),
        Mockito.anyLong());
    fmeControllerImpl.operationFinished(fmeOperationInfoVO);
    Mockito.verify(fmeCommunicationService, times(1)).updateJobStatus(Mockito.any(),
        Mockito.anyLong());
  }

  @Test(expected = ResponseStatusException.class)
  public void operationFinishedForbiddenTest() throws EEAException {
    FMEOperationInfoVO fmeOperationInfoVO = new FMEOperationInfoVO();
    fmeOperationInfoVO.setApiKey("sampleApiKey");
    fmeOperationInfoVO.setFmeJobId(1L);
    Mockito.when(
        fmeCommunicationService.authenticateAndAuthorize(Mockito.anyString(), Mockito.anyLong()))
        .thenThrow(new EEAException(EEAErrorMessage.FORBIDDEN));
    try {
      fmeControllerImpl.operationFinished(fmeOperationInfoVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void operationFinishedUnauthorizedTest() throws EEAException {
    FMEOperationInfoVO fmeOperationInfoVO = new FMEOperationInfoVO();
    fmeOperationInfoVO.setApiKey("sampleApiKey");
    fmeOperationInfoVO.setFmeJobId(1L);
    Mockito.when(
        fmeCommunicationService.authenticateAndAuthorize(Mockito.anyString(), Mockito.anyLong()))
        .thenThrow(new EEAException(EEAErrorMessage.UNAUTHORIZED));
    try {
      fmeControllerImpl.operationFinished(fmeOperationInfoVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void operationFinishedErrorTest() throws EEAException {
    FMEOperationInfoVO fmeOperationInfoVO = new FMEOperationInfoVO();
    fmeOperationInfoVO.setApiKey("sampleApiKey");
    fmeOperationInfoVO.setFmeJobId(1L);
    Mockito.when(
        fmeCommunicationService.authenticateAndAuthorize(Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(new FMEJob());
    Mockito.doThrow(new EEAException("Error realeasing event: FMEJob={}"))
        .when(fmeCommunicationService).releaseNotifications(Mockito.any(), Mockito.anyLong());
    try {
      fmeControllerImpl.operationFinished(fmeOperationInfoVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }
}
