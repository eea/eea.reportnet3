package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.dataset.service.ReportingDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DatasetStatusMessageVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
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
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataSetMetabaseControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataSetMetabaseControllerImplTest {

  /** The data set metabase controller impl. */
  @InjectMocks
  private DataSetMetabaseControllerImpl dataSetMetabaseControllerImpl;

  /** The dataset metabase service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The reporting dataset service. */
  @Mock
  private ReportingDatasetService reportingDatasetService;


  /** The design dataset service. */
  @Mock
  private DesignDatasetService designDatasetService;

  /** The representative controller zuul. */
  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;



  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Find data set id by dataflow id.
   */
  @Test
  public void testFindDataSetIdByDataflowId() {
    when(reportingDatasetService.getDataSetIdByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    dataSetMetabaseControllerImpl.findReportingDataSetIdByDataflowId(Mockito.anyLong());
    Mockito.verify(reportingDatasetService, times(1)).getDataSetIdByDataflowId(Mockito.any());
  }


  /**
   * Creates the removeDatasetData data set test exception entry 1.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetTestException1() throws Exception {
    dataSetMetabaseControllerImpl.createEmptyDataSet(DatasetTypeEnum.REPORTING, null, null, 1L);
  }


  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetTestException2() throws Exception {
    Mockito.doThrow(EEAException.class).when(datasetMetabaseService).createEmptyDataset(
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any());
    dataSetMetabaseControllerImpl.createEmptyDataSet(null, "notBlank", "", 1L);
  }

  /**
   * Creates the removeDatasetData data set test.
   *
   * @throws Exception the exception
   */
  @Test
  public void createEmptyDataSetTest() throws Exception {
    Mockito
        .when(datasetMetabaseService.createEmptyDataset(Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(CompletableFuture.completedFuture(1L));
    dataSetMetabaseControllerImpl.createEmptyDataSet(DatasetTypeEnum.REPORTING, "datasetName", null,
        1L);
    Mockito.verify(datasetMetabaseService, times(1)).createEmptyDataset(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  public void findDatasetMetabaseByIdTest() throws Exception {
    when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    dataSetMetabaseControllerImpl.findDatasetMetabaseById(Mockito.anyLong());
    Mockito.verify(datasetMetabaseService, times(1)).findDatasetMetabase(Mockito.anyLong());
  }

  @Test
  public void testFindDesignDataSetIdByDataflowId() {
    when(designDatasetService.getDesignDataSetIdByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    dataSetMetabaseControllerImpl.findDesignDataSetIdByDataflowId(Mockito.anyLong());
    Mockito.verify(designDatasetService, times(1)).getDesignDataSetIdByDataflowId(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void updateDatasetNameTest1() {
    Mockito.when(datasetMetabaseService.updateDatasetName(Mockito.any(), Mockito.any()))
        .thenReturn(false);
    dataSetMetabaseControllerImpl.updateDatasetName(1L, "datasetName");
  }

  @Test
  public void updateDatasetNameTest2() {
    Mockito.when(datasetMetabaseService.updateDatasetName(Mockito.any(), Mockito.any()))
        .thenReturn(true);
    dataSetMetabaseControllerImpl.updateDatasetName(1L, "datasetName");
    Mockito.verify(datasetMetabaseService, times(1)).updateDatasetName(Mockito.any(),
        Mockito.any());
  }


  /**
   * Test load statistics.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadStatistics() throws Exception {
    when(datasetMetabaseService.getStatistics(Mockito.any())).thenReturn(new StatisticsVO());
    dataSetMetabaseControllerImpl.getStatisticsById(1L);

    Mockito.verify(datasetMetabaseService, times(1)).getStatistics(Mockito.any());
  }


  /**
   * Test load statistics exception.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadStatisticsException() throws Exception {
    doThrow(new EEAException()).when(datasetMetabaseService).getStatistics(Mockito.any());
    dataSetMetabaseControllerImpl.getStatisticsById(null);

    Mockito.verify(datasetMetabaseService, times(1)).getStatistics(Mockito.any());
  }


  @Test
  public void testGlobalStatistics() throws Exception {
    when(datasetMetabaseService.getGlobalStatistics(Mockito.any())).thenReturn(new ArrayList<>());
    dataSetMetabaseControllerImpl.getGlobalStatisticsByDataschemaId("5ce524fad31fc52540abae73", 0L);

    Mockito.verify(datasetMetabaseService, times(1)).getGlobalStatistics(Mockito.any());
  }

  @Test
  public void testGlobalStatisticsException() throws Exception {
    doThrow(new EEAException()).when(datasetMetabaseService).getGlobalStatistics(Mockito.any());
    dataSetMetabaseControllerImpl.getGlobalStatisticsByDataschemaId(null, null);

    Mockito.verify(datasetMetabaseService, times(1)).getGlobalStatistics(Mockito.any());
  }

  @Test
  public void getReportingsIdBySchemaIdTest() {
    Mockito.when(reportingDatasetService.getDataSetIdBySchemaId(Mockito.any())).thenReturn(null);
    Assert.assertNull(dataSetMetabaseControllerImpl.getReportingsIdBySchemaId(""));
  }

  @Test
  public void findDatasetSchemaIdByIdTest() {
    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5ce524fad31fc52540abae73");
    Assert.assertEquals("5ce524fad31fc52540abae73",
        dataSetMetabaseControllerImpl.findDatasetSchemaIdById(1L));
  }

  @Test
  public void getIntegrityDatasetIdTest() {
    Mockito.when(
        datasetMetabaseService.getIntegrityDatasetId(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Assert.assertEquals((Long) 1L,
        dataSetMetabaseControllerImpl.getIntegrityDatasetId(1L, "1L", "1L"));
  }

  @Test
  public void createDatasetForeignRelationshipTest() {
    dataSetMetabaseControllerImpl.createDatasetForeignRelationship(1L, 1L, "1", "1");
    Mockito.verify(datasetMetabaseService, times(1)).createForeignRelationship(Mockito.anyLong(),
        Mockito.anyLong(), Mockito.any(), Mockito.any());
  }

  @Test
  public void updateDatasetForeignRelationshipTest() {
    dataSetMetabaseControllerImpl.updateDatasetForeignRelationship(1L, 1L, "1", "1");
    Mockito.verify(datasetMetabaseService, times(1)).updateForeignRelationship(Mockito.anyLong(),
        Mockito.anyLong(), Mockito.any(), Mockito.any());
  }

  @Test
  public void getDesignDatasetIdByDatasetSchemaIdTest() {
    Mockito.when(datasetMetabaseService
        .getDatasetIdByDatasetSchemaIdAndDataProviderId(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Assert.assertEquals((Long) 1L,
        dataSetMetabaseControllerImpl.getDesignDatasetIdByDatasetSchemaId("1L"));
  }

  @Test
  public void deleteDatasetForeignRelationshipTest() {
    Mockito.when(
        datasetMetabaseService.getIntegrityDatasetId(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    dataSetMetabaseControllerImpl.deleteForeignRelationship(1L, 1L, "1", "1");
    Mockito.verify(datasetMetabaseService, times(1)).deleteForeignRelation(Mockito.anyLong(),
        Mockito.anyLong(), Mockito.any(), Mockito.any());
  }

  @Test
  public void getLastDatasetValidationForRelease() {
    dataSetMetabaseControllerImpl.getLastDatasetValidationForRelease(1L);
    Mockito.when(datasetMetabaseService.getLastDatasetValidationForRelease(Mockito.any()))
        .thenReturn(1L);
    Assert.assertEquals((Long) 1L,
        dataSetMetabaseControllerImpl.getLastDatasetValidationForRelease(1L));
  }

  @Test
  public void updateDatasetStatusTest() throws EEAException {
    dataSetMetabaseControllerImpl.updateDatasetStatus(new DatasetStatusMessageVO());
    Mockito.verify(datasetMetabaseService, times(1)).updateDatasetStatus(Mockito.any());
  }


  @Test(expected = ResponseStatusException.class)
  public void updateDatasetStatusException() throws EEAException {
    doThrow(new EEAException()).when(datasetMetabaseService).updateDatasetStatus(Mockito.any());
    try {
      dataSetMetabaseControllerImpl.updateDatasetStatus(new DatasetStatusMessageVO());
    } catch (ResponseStatusException e) {
      assertEquals(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
      throw e;
    }
  }

  @Test
  public void findReportingDataSetPublicByDataflowIdTest() throws EEAException {
    dataSetMetabaseControllerImpl.findReportingDataSetPublicByDataflowId(1L);
    Mockito.verify(reportingDatasetService, times(1)).getDataSetPublicByDataflow(Mockito.any());
  }

  @Test
  public void findReportingDataSetIdByDataflowIdAndProviderIdTest() {
    dataSetMetabaseControllerImpl.findReportingDataSetIdByDataflowIdAndProviderId(1L, 1L);
    Mockito.verify(reportingDatasetService, times(1))
        .getDataSetIdByDataflowIdAndDataProviderId(Mockito.any(), Mockito.any());
  }

  @Test
  public void findReportingDataSetPublicByDataflowIdAndProviderId() {
    dataSetMetabaseControllerImpl.findReportingDataSetPublicByDataflowIdAndProviderId(1L, 1L);
    Mockito.verify(reportingDatasetService, times(1))
        .getDataSetPublicByDataflowAndProviderId(Mockito.any(), Mockito.any());
  }

  @Test
  public void getDatasetsSummaryListTest() {
    dataSetMetabaseControllerImpl.getDatasetsSummaryList(1L);
    Mockito.verify(datasetMetabaseService, times(1)).getDatasetsSummaryList(1L);
  }

}
