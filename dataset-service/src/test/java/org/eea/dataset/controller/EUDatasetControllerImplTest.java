package org.eea.dataset.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.service.EUDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.junit.Assert;
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


/**
 * The Class EUDatasetControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class EUDatasetControllerImplTest {

  /** The eu dataset controller impl. */
  @InjectMocks
  private EUDatasetControllerImpl euDatasetControllerImpl;

  /** The eu dataset service. */
  @Mock
  private EUDatasetService euDatasetService;

  /** The list EU datasets. */
  private List<EUDatasetVO> listEUDatasets;

  /** The security context. */
  SecurityContext securityContext;

  /** The authentication. */
  Authentication authentication;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    listEUDatasets = new ArrayList<>();
    EUDatasetVO eudataset = new EUDatasetVO();
    eudataset.setId(1L);
    listEUDatasets.add(eudataset);

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    MockitoAnnotations.openMocks(this);
  }

  /**
   * Find EU dataset by dataflow id test.
   */
  @Test
  public void findEUDatasetByDataflowIdTest() {
    Mockito.when(euDatasetService.getEUDatasetByDataflowId(Mockito.any()))
        .thenReturn(listEUDatasets);
    Assert.assertEquals(listEUDatasets, euDatasetControllerImpl.findEUDatasetByDataflowId(1L));
  }

  /**
   * Populate data from data collection test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void populateDataFromDataCollectionTest() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doNothing().when(euDatasetService).populateEUDatasetWithDataCollection(Mockito.any());
    euDatasetControllerImpl.populateDataFromDataCollection(1L);
    Mockito.verify(euDatasetService, times(1)).populateEUDatasetWithDataCollection(Mockito.any());
  }

  /**
   * Populate data from data collection exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void populateDataFromDataCollectionExceptionTest() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException("failed")).when(euDatasetService)
        .populateEUDatasetWithDataCollection(Mockito.any());
    euDatasetControllerImpl.populateDataFromDataCollection(1L);
    Mockito.verify(euDatasetService, times(1)).populateEUDatasetWithDataCollection(Mockito.any());

  }
}
