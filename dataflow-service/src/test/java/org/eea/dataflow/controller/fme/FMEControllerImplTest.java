package org.eea.dataflow.controller.fme;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
import org.eea.dataflow.persistence.domain.FMEJob;
import org.eea.dataflow.service.IntegrationService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAUnauthorizedException;
import org.eea.interfaces.vo.integration.fme.FMEOperationInfoVO;
import org.eea.lock.service.LockService;
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

  @Mock
  private IntegrationService integrationService;

  @Mock
  private LockService lockService;

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
    Mockito.when(fmeCommunicationService.findRepository(Mockito.anyLong())).thenReturn(null);
    fmeControllerImpl.findRepositories(1L);
    Mockito.verify(fmeCommunicationService, times(1)).findRepository(Mockito.anyLong());
  }

  @Test
  public void findItemsTest() {
    Mockito.when(fmeCommunicationService.findItems(Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(null);
    fmeControllerImpl.findItems(1L, "repository");
    Mockito.verify(fmeCommunicationService, times(1)).findItems(Mockito.anyString(),
        Mockito.anyLong());
  }

  @Test
  public void operationFinishedTest() throws EEAException {
    FMEOperationInfoVO fmeOperationInfoVO = new FMEOperationInfoVO();
    fmeOperationInfoVO.setApiKey("sampleApiKey");
    fmeOperationInfoVO.setRn3JobId(1L);
    fmeOperationInfoVO.setStatusNumber(1L);
    fmeOperationInfoVO.setDatasetId(1L);
    Mockito.when(
        fmeCommunicationService.authenticateAndAuthorize(Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(new FMEJob());
    Mockito.doNothing().when(fmeCommunicationService).releaseNotifications(Mockito.any(),
        Mockito.anyLong(), Mockito.anyBoolean());
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
    fmeOperationInfoVO.setRn3JobId(1L);
    fmeOperationInfoVO.setDatasetId(1L);
    Mockito.when(
        fmeCommunicationService.authenticateAndAuthorize(Mockito.anyString(), Mockito.anyLong()))
        .thenThrow(new EEAForbiddenException(EEAErrorMessage.FORBIDDEN));
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
    fmeOperationInfoVO.setRn3JobId(1L);
    fmeOperationInfoVO.setDatasetId(1L);
    Mockito
        .when(fmeCommunicationService.authenticateAndAuthorize(Mockito.anyString(),
            Mockito.anyLong()))
        .thenThrow(new EEAUnauthorizedException(EEAErrorMessage.UNAUTHORIZED));
    try {
      fmeControllerImpl.operationFinished(fmeOperationInfoVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
      throw e;
    }
  }


  @Test
  public void downloadExportFileTest() {
    assertEquals(HttpStatus.OK, fmeControllerImpl.downloadExportFile(0L, 0L, "").getStatusCode());
  }

  @Test
  public void updateJobStatusByIdTest() {
    fmeControllerImpl.updateJobStatusById(0L, 0L);
    Mockito.verify(fmeCommunicationService, times(1)).updateJobStatusById(Mockito.any(),
        Mockito.any());
  }
}
