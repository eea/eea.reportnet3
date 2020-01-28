package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import org.bson.types.ObjectId;
import org.eea.dataset.service.DataCollectionService;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.kafka.utils.KafkaSenderUtils;
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

  /** The security context. */
  SecurityContext securityContext;

  /** The authentication. */
  Authentication authentication;


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
  public void createEmptyDataCollectionTest() throws EEAException {

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    DataCollectionVO dc = new DataCollectionVO();
    dc.setDataSetName("datasetTest");
    dc.setDueDate(new Date());
    dc.setDatasetSchema("");
    dc.setIdDataflow(1L);
    RepresentativeVO representative = new RepresentativeVO();
    representative.setDataProviderId(1L);
    DataProviderVO dataprovider = new DataProviderVO();
    dataprovider.setLabel("test");
    DesignDatasetVO design = new DesignDatasetVO();
    design.setDatasetSchema(new ObjectId().toString());
    DataFlowVO df = new DataFlowVO();
    df.setId(1L);
    df.setStatus(TypeStatusEnum.DESIGN);

    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(df);

    Mockito
        .when(datasetMetabaseService.createEmptyDataset(Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(CompletableFuture.completedFuture(1L));

    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(Arrays.asList(representative));
    Mockito.when(designDatasetService.getDesignDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(Arrays.asList(design));
    Mockito.doNothing().when(dataflowControllerZuul).updateDataFlowStatus(Mockito.any(),
        Mockito.any());

    Mockito.when(schemaService.validateSchema(Mockito.any())).thenReturn(true);

    dataCollectionControllerImpl.createEmptyDataCollection(dc);
    Mockito.verify(dataflowControllerZuul, times(1)).updateDataFlowStatus(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void createEmptyDataCollectionTestException() throws EEAException {

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    DataCollectionVO dc = new DataCollectionVO();
    dc.setDueDate(new Date());
    dc.setDatasetSchema("");
    RepresentativeVO representative = new RepresentativeVO();
    representative.setDataProviderId(1L);
    DataProviderVO dataprovider = new DataProviderVO();
    dataprovider.setLabel("test");

    try {
      dataCollectionControllerImpl.createEmptyDataCollection(dc);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }

  }


  @Test
  public void createEmptyDataCollectionTestException2() throws EEAException {

    DataCollectionVO dc = new DataCollectionVO();
    dc.setDataSetName("datasetTest");
    dc.setIdDataflow(1L);
    RepresentativeVO representative = new RepresentativeVO();
    representative.setDataProviderId(1L);
    DataProviderVO dataprovider = new DataProviderVO();
    dataprovider.setLabel("test");
    DesignDatasetVO design = new DesignDatasetVO();
    design.setDatasetSchema(new ObjectId().toString());
    DataFlowVO df = new DataFlowVO();
    df.setId(1L);
    df.setStatus(TypeStatusEnum.DESIGN);

    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(df);

    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(Arrays.asList(representative));

    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(Arrays.asList(representative));


    Mockito
        .when(datasetMetabaseService.createEmptyDataset(Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenThrow(new EEAException());


    Mockito.when(designDatasetService.getDesignDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(Arrays.asList(design));

    Mockito.when(schemaService.validateSchema(Mockito.any())).thenReturn(true);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    try {
      dataCollectionControllerImpl.createEmptyDataCollection(dc);
    } catch (ResponseStatusException e) {
      assertEquals("Cause is ok", EEAErrorMessage.EXECUTION_ERROR, e.getReason());
    }

  }


  @Test
  public void createEmptyDataCollectionTestException3() throws EEAException {

    DataCollectionVO dc = new DataCollectionVO();
    dc.setDataSetName("datasetTest");
    dc.setIdDataflow(1L);
    RepresentativeVO representative = new RepresentativeVO();
    representative.setDataProviderId(1L);
    DataProviderVO dataprovider = new DataProviderVO();
    dataprovider.setLabel("test");

    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(Arrays.asList(representative));

    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(Arrays.asList(representative));

    Mockito.when(designDatasetService.getDesignDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(new ArrayList<>());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    try {
      dataCollectionControllerImpl.createEmptyDataCollection(dc);
    } catch (ResponseStatusException e) {
      assertEquals("Cause is ok", EEAErrorMessage.DATA_COLLECTION_NOT_CREATED, e.getReason());
    }

  }

  @Test
  public void createEmptyDataCollectionTestExceptionStatusWrong() throws EEAException {

    DataCollectionVO dc = new DataCollectionVO();
    dc.setDataSetName("datasetTest");
    dc.setIdDataflow(1L);
    RepresentativeVO representative = new RepresentativeVO();
    representative.setDataProviderId(1L);
    DataProviderVO dataprovider = new DataProviderVO();
    dataprovider.setLabel("test");
    DesignDatasetVO design = new DesignDatasetVO();
    design.setDatasetSchema(new ObjectId().toString());
    DataFlowVO df = new DataFlowVO();
    df.setId(1L);
    df.setStatus(TypeStatusEnum.DRAFT);

    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(df);

    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(Arrays.asList(representative));

    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(Arrays.asList(representative));

    Mockito.when(designDatasetService.getDesignDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(Arrays.asList(design));

    Mockito.when(schemaService.validateSchema(Mockito.any())).thenReturn(true);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    try {
      dataCollectionControllerImpl.createEmptyDataCollection(dc);
    } catch (ResponseStatusException e) {
      assertEquals("Cause is ok", EEAErrorMessage.DATA_COLLECTION_NOT_CREATED, e.getReason());
    }

  }


  @Test
  public void findDataCollectionIdByDataflowIdTest() {
    Mockito.when(dataCollectionService.getDataCollectionIdByDataflowId(Mockito.any()))
        .thenReturn(null);
    Assert.assertNull(dataCollectionControllerImpl.findDataCollectionIdByDataflowId(1L));
  }



}
