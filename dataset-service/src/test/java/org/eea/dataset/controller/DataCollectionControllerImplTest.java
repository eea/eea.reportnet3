package org.eea.dataset.controller;

import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.Date;
import org.eea.dataset.service.DataCollectionService;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
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

  /** The notification controller zuul. */
  @Mock
  private NotificationControllerZuul notificationControllerZuul;

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
    MockitoAnnotations.openMocks(this);
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
        Mockito.any(), Mockito.anyBoolean());
    dataCollectionControllerImpl.undoDataCollectionCreation(new ArrayList<Long>(), 1L, false);
    Mockito.verify(dataCollectionService, times(1)).undoDataCollectionCreation(Mockito.any(),
        Mockito.any(), Mockito.anyBoolean());
  }

  @Test
  public void createEmptyDataCollectionTestException() {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataCollectionService.getDataflowMetabase(Mockito.any())).thenReturn(dataflow);
    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    try {
      DataCollectionVO dc = new DataCollectionVO();
      dc.setIdDataflow(1L);
      dc.setDueDate(new Date(System.currentTimeMillis() + 100000));
      dataCollectionControllerImpl.createEmptyDataCollection(true, false, false, dc, true);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.NOT_DESIGN_DATAFLOW, e.getReason());
    }
  }

  @Test
  public void createEmptyDataCollectionTest() {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataCollectionService.getDataflowMetabase(Mockito.any())).thenReturn(dataflow);
    Mockito.doNothing().when(dataCollectionService).createEmptyDataCollection(Mockito.any(),
        Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean(),
        Mockito.anyBoolean(), Mockito.anyBoolean());
    DataCollectionVO dc = new DataCollectionVO();
    dc.setIdDataflow(1L);
    dc.setDueDate(new Date(System.currentTimeMillis() + 100000));
    dataCollectionControllerImpl.createEmptyDataCollection(false, false, false, dc, true);
    Mockito.verify(dataCollectionService, times(1)).createEmptyDataCollection(Mockito.any(),
        Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean(),
        Mockito.anyBoolean(), Mockito.anyBoolean());
  }

  @Test(expected = ResponseStatusException.class)
  public void updateDataCollectionTestException() {
    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataCollectionService.getDataflowMetabase(Mockito.any())).thenReturn(dataflow);

    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    try {
      DataCollectionVO dc = new DataCollectionVO();
      dc.setIdDataflow(1L);
      dc.setDueDate(new Date(System.currentTimeMillis() + 100000));
      dataCollectionControllerImpl.updateDataCollection(1L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.NOT_DRAFT_DATAFLOW, e.getReason());
      throw e;
    }
  }

  @Test
  public void updateDataCollectionTest() {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataCollectionService.getDataflowMetabase(Mockito.any())).thenReturn(dataflow);
    Mockito.doNothing().when(dataCollectionService).updateDataCollection(Mockito.any(),
        Mockito.anyBoolean());
    DataCollectionVO dc = new DataCollectionVO();
    dc.setIdDataflow(1L);
    dc.setDueDate(new Date(System.currentTimeMillis() + 100000));
    dataCollectionControllerImpl.updateDataCollection(1L);
    Mockito.verify(dataCollectionService, times(1)).updateDataCollection(Mockito.any(),
        Mockito.anyBoolean());
  }
}
