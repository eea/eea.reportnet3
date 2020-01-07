package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import java.util.Arrays;
import java.util.Date;
import org.bson.types.ObjectId;
import org.eea.dataset.service.DataCollectionService;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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


  @Test
  public void createEmptyDataCollectionTest() throws EEAException {

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


    Mockito.when(datasetMetabaseService.createEmptyDataset(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(1L);

    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(Arrays.asList(representative));
    Mockito.when(designDatasetService.getDesignDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(Arrays.asList(design));
    Mockito.doNothing().when(dataflowControllerZuul).updateDataFlowStatus(Mockito.any(),
        Mockito.any());

    dataCollectionControllerImpl.createEmptyDataCollection(dc);
    Mockito.verify(dataflowControllerZuul, times(1)).updateDataFlowStatus(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void createEmptyDataCollectionTestException() throws EEAException {

    DataCollectionVO dc = new DataCollectionVO();
    dc.setDueDate(new Date());
    dc.setDatasetSchema("");
    dc.setIdDataflow(1L);
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
    RepresentativeVO representative = new RepresentativeVO();
    representative.setDataProviderId(1L);
    DataProviderVO dataprovider = new DataProviderVO();
    dataprovider.setLabel("test");
    DesignDatasetVO design = new DesignDatasetVO();
    design.setDatasetSchema(new ObjectId().toString());

    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(Arrays.asList(representative));

    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(Arrays.asList(representative));


    Mockito.when(datasetMetabaseService.createEmptyDataset(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new EEAException());


    Mockito.when(designDatasetService.getDesignDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(Arrays.asList(design));


    try {
      dataCollectionControllerImpl.createEmptyDataCollection(dc);
    } catch (ResponseStatusException e) {
      assertEquals("Cause is ok", EEAErrorMessage.EXECUTION_ERROR, e.getReason());
    }

  }


  @Test
  public void findDataCollectionIdByDataflowIdTest() {
    Mockito.when(dataCollectionService.getDataCollectionIdByDataflowId(Mockito.any()))
        .thenReturn(null);
    Assert.assertNull(dataCollectionControllerImpl.findDataCollectionIdByDataflowId(1L));
  }



}
