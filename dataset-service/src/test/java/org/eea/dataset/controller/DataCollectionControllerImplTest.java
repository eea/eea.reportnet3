package org.eea.dataset.controller;

import static org.mockito.Mockito.times;
import java.util.ArrayList;
import org.eea.dataset.service.DataCollectionService;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.kafka.utils.KafkaSenderUtils;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataCollectionControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataCollectionControllerImplTest {

  @InjectMocks
  private DataCollectionControllerImpl dataCollectionControllerImpl;

  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  @Mock
  private DataCollectionService dataCollectionService;

  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  @Mock
  private DesignDatasetService designDatasetService;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private DatasetSchemaService schemaService;

  @Mock
  private LockService lockService;

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void findDataCollectionIdByDataflowIdTest() {
    Mockito.when(dataCollectionService.getDataCollectionIdByDataflowId(Mockito.any()))
        .thenReturn(null);
    Assert.assertNull(dataCollectionControllerImpl.findDataCollectionIdByDataflowId(1L));
  }

  @Test
  public void undoDataCollectionCreationTest() {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.doNothing().when(dataCollectionService).undoDataCollectionCreation(Mockito.any(),
        Mockito.any());
    dataCollectionControllerImpl.undoDataCollectionCreation(new ArrayList<Long>(), 1L);
    Mockito.verify(dataCollectionService, times(1)).undoDataCollectionCreation(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void createEmptyDataCollectionTest1() {
    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    try {
      dataCollectionControllerImpl.createEmptyDataCollection(1L, 1L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.DATE_AFTER_INCORRECT, e.getReason());
    }
  }

  @Test
  public void createEmptyDataCollectionTest2() {
    Mockito.when(dataCollectionService.isDesignDataflow(Mockito.any())).thenReturn(false);
    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    try {
      dataCollectionControllerImpl.createEmptyDataCollection(1L,
          System.currentTimeMillis() + 100000);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.NOT_DESIGN_DATAFLOW, e.getReason());
    }
  }

  @Test
  public void createEmptyDataCollectionTest3() {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.when(dataCollectionService.isDesignDataflow(Mockito.any())).thenReturn(true);
    Mockito.doNothing().when(dataCollectionService).createEmptyDataCollection(Mockito.any(),
        Mockito.any());
    dataCollectionControllerImpl.createEmptyDataCollection(1L, System.currentTimeMillis() + 100000);
    Mockito.verify(dataCollectionService, times(1)).createEmptyDataCollection(Mockito.any(),
        Mockito.any());
  }
}
