package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.dataset.service.ReportingDatasetService;
import org.eea.exception.EEAErrorMessage;
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
 * The Class DatasetMetabaseControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetMetabaseControllerImplTest {

  /** The data set metabase controller impl. */
  @InjectMocks
  private DatasetMetabaseControllerImpl datasetMetabaseControllerImpl;

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
    datasetMetabaseControllerImpl.findReportingDataSetIdByDataflowId(Mockito.anyLong());
    Mockito.verify(reportingDatasetService, times(1)).getDataSetIdByDataflowId(Mockito.any());
  }


  /**
   * Creates the removeDatasetData data set test exception entry 1.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetTestException1() throws Exception {
    datasetMetabaseControllerImpl.createEmptyDataSet(DatasetTypeEnum.REPORTING, null, null, 1L);
  }


  /**
   * Creates the empty data set test exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetTestException2() throws Exception {
    Mockito.doThrow(EEAException.class).when(datasetMetabaseService).createEmptyDataset(
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any());
    datasetMetabaseControllerImpl.createEmptyDataSet(null, "notBlank", "", 1L);
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
    datasetMetabaseControllerImpl.createEmptyDataSet(DatasetTypeEnum.REPORTING, "datasetName", null,
        1L);
    Mockito.verify(datasetMetabaseService, times(1)).createEmptyDataset(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Find dataset metabase by id test.
   *
   * @throws Exception the exception
   */
  @Test
  public void findDatasetMetabaseByIdTest() throws Exception {
    when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    datasetMetabaseControllerImpl.findDatasetMetabaseById(Mockito.anyLong());
    Mockito.verify(datasetMetabaseService, times(1)).findDatasetMetabase(Mockito.anyLong());
  }

  /**
   * Find external dataset metabase by id test.
   *
   * @throws Exception the exception
   */
  @Test
  public void findExternalDatasetMetabaseByIdTest() throws Exception {
    datasetMetabaseControllerImpl.findExternalDatasetMetabaseById(Mockito.anyLong());
    Mockito.verify(datasetMetabaseService, times(1)).findDatasetMetabaseExternal(Mockito.anyLong());
  }

  /**
   * Find external dataset metabase by id EEA exception test.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void findExternalDatasetMetabaseByIdEEAExceptionTest() throws Exception {
    Mockito.when(datasetMetabaseService.findDatasetMetabaseExternal(Mockito.anyLong()))
        .thenThrow(EEAException.class);
    try {
      datasetMetabaseControllerImpl.findExternalDatasetMetabaseById(Mockito.anyLong());
    } catch (ResponseStatusException e) {
      assertEquals(e.getStatus(), HttpStatus.UNAUTHORIZED);
      throw e;
    }
  }

  /**
   * Test find design data set id by dataflow id.
   */
  @Test
  public void testFindDesignDataSetIdByDataflowId() {
    when(designDatasetService.getDesignDataSetIdByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    datasetMetabaseControllerImpl.findDesignDataSetIdByDataflowId(Mockito.anyLong());
    Mockito.verify(designDatasetService, times(1)).getDesignDataSetIdByDataflowId(Mockito.any());
  }

  /**
   * Update dataset name test 1.
   */
  @Test(expected = ResponseStatusException.class)
  public void updateDatasetNameTest1() {
    Mockito.when(datasetMetabaseService.updateDatasetName(Mockito.any(), Mockito.any()))
        .thenReturn(false);
    datasetMetabaseControllerImpl.updateDatasetName(1L, "datasetName");
  }

  /**
   * Update dataset name test 2.
   */
  @Test
  public void updateDatasetNameTest2() {
    Mockito.when(datasetMetabaseService.updateDatasetName(Mockito.any(), Mockito.any()))
        .thenReturn(true);
    datasetMetabaseControllerImpl.updateDatasetName(1L, "datasetName");
    Mockito.verify(datasetMetabaseService, times(1)).updateDatasetName(Mockito.any(),
        Mockito.any());
  }

  /**
   * Update dataset name bad name test.
   */
  @Test(expected = ResponseStatusException.class)
  public void updateDatasetNameBadNameTest() {
    try {
      datasetMetabaseControllerImpl.updateDatasetName(1L, "bad$name");
    } catch (ResponseStatusException e) {
      assertEquals(e.getStatus(), HttpStatus.BAD_REQUEST);
      assertEquals(e.getReason(), EEAErrorMessage.DATASET_SCHEMA_INVALID_NAME_ERROR);
      throw e;
    }
  }


  /**
   * Test load statistics.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadStatistics() throws Exception {
    when(datasetMetabaseService.getStatistics(Mockito.any())).thenReturn(new StatisticsVO());
    datasetMetabaseControllerImpl.getStatisticsById(1L);

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
    datasetMetabaseControllerImpl.getStatisticsById(null);

    Mockito.verify(datasetMetabaseService, times(1)).getStatistics(Mockito.any());
  }


  /**
   * Test global statistics.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGlobalStatistics() throws Exception {
    when(datasetMetabaseService.getGlobalStatistics(Mockito.any())).thenReturn(new ArrayList<>());
    datasetMetabaseControllerImpl.getGlobalStatisticsByDataschemaId("5ce524fad31fc52540abae73", 0L);

    Mockito.verify(datasetMetabaseService, times(1)).getGlobalStatistics(Mockito.any());
  }

  /**
   * Test global statistics exception.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGlobalStatisticsException() throws Exception {
    doThrow(new EEAException()).when(datasetMetabaseService).getGlobalStatistics(Mockito.any());
    datasetMetabaseControllerImpl.getGlobalStatisticsByDataschemaId(null, null);

    Mockito.verify(datasetMetabaseService, times(1)).getGlobalStatistics(Mockito.any());
  }

  /**
   * Gets the reportings id by schema id test.
   *
   * @return the reportings id by schema id test
   */
  @Test
  public void getReportingsIdBySchemaIdTest() {
    Mockito.when(reportingDatasetService.getDataSetIdBySchemaId(Mockito.any())).thenReturn(null);
    Assert.assertNull(datasetMetabaseControllerImpl.getReportingsIdBySchemaId(""));
  }

  /**
   * Find dataset schema id by id test.
   */
  @Test
  public void findDatasetSchemaIdByIdTest() {
    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5ce524fad31fc52540abae73");
    Assert.assertEquals("5ce524fad31fc52540abae73",
        datasetMetabaseControllerImpl.findDatasetSchemaIdById(1L));
  }

  /**
   * Gets the integrity dataset id test.
   *
   * @return the integrity dataset id test
   */
  @Test
  public void getIntegrityDatasetIdTest() {
    Mockito.when(
        datasetMetabaseService.getIntegrityDatasetId(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Assert.assertEquals((Long) 1L,
        datasetMetabaseControllerImpl.getIntegrityDatasetId(1L, "1L", "1L"));
  }

  /**
   * Creates the dataset foreign relationship test.
   */
  @Test
  public void createDatasetForeignRelationshipTest() {
    datasetMetabaseControllerImpl.createDatasetForeignRelationship(1L, 1L, "1", "1");
    Mockito.verify(datasetMetabaseService, times(1)).createForeignRelationship(Mockito.anyLong(),
        Mockito.anyLong(), Mockito.any(), Mockito.any());
  }

  /**
   * Update dataset foreign relationship test.
   */
  @Test
  public void updateDatasetForeignRelationshipTest() {
    datasetMetabaseControllerImpl.updateDatasetForeignRelationship(1L, 1L, "1", "1");
    Mockito.verify(datasetMetabaseService, times(1)).updateForeignRelationship(Mockito.anyLong(),
        Mockito.anyLong(), Mockito.any(), Mockito.any());
  }

  /**
   * Gets the design dataset id by dataset schema id test.
   *
   * @return the design dataset id by dataset schema id test
   */
  @Test
  public void getDesignDatasetIdByDatasetSchemaIdTest() {
    Mockito.when(datasetMetabaseService
        .getDatasetIdByDatasetSchemaIdAndDataProviderId(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Assert.assertEquals((Long) 1L,
        datasetMetabaseControllerImpl.getDesignDatasetIdByDatasetSchemaId("1L"));
  }

  /**
   * Delete dataset foreign relationship test.
   */
  @Test
  public void deleteDatasetForeignRelationshipTest() {
    Mockito.when(
        datasetMetabaseService.getIntegrityDatasetId(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    datasetMetabaseControllerImpl.deleteForeignRelationship(1L, 1L, "1", "1");
    Mockito.verify(datasetMetabaseService, times(1)).deleteForeignRelation(Mockito.anyLong(),
        Mockito.anyLong(), Mockito.any(), Mockito.any());
  }

  /**
   * Delete dataset foreign relationship dataset reference id null test.
   */
  @Test
  public void deleteDatasetForeignRelationshipDatasetReferenceIdNullTest() {
    Mockito.when(
        datasetMetabaseService.getIntegrityDatasetId(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    datasetMetabaseControllerImpl.deleteForeignRelationship(1L, null, "1", "1");
    Mockito.verify(datasetMetabaseService, times(1)).deleteForeignRelation(Mockito.anyLong(),
        Mockito.anyLong(), Mockito.any(), Mockito.any());
  }

  /**
   * Delete dataset foreign relationship reference id not equals origin id test.
   */
  @Test
  public void deleteDatasetForeignRelationshipReferenceIdNotEqualsOriginIdTest() {
    datasetMetabaseControllerImpl.deleteForeignRelationship(1L, 2L, "1", "1");
    Mockito.verify(datasetMetabaseService, times(1)).deleteForeignRelation(Mockito.anyLong(),
        Mockito.anyLong(), Mockito.any(), Mockito.any());
  }

  /**
   * Update dataset status test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDatasetStatusTest() throws EEAException {
    datasetMetabaseControllerImpl.updateDatasetStatus(new DatasetStatusMessageVO());
    Mockito.verify(datasetMetabaseService, times(1)).updateDatasetStatus(Mockito.any());
  }


  /**
   * Update dataset status exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateDatasetStatusException() throws EEAException {
    doThrow(new EEAException()).when(datasetMetabaseService).updateDatasetStatus(Mockito.any());
    try {
      datasetMetabaseControllerImpl.updateDatasetStatus(new DatasetStatusMessageVO());
    } catch (ResponseStatusException e) {
      assertEquals(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
      throw e;
    }
  }

  /**
   * Find reporting data set public by dataflow id test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findReportingDataSetPublicByDataflowIdTest() throws EEAException {
    datasetMetabaseControllerImpl.findReportingDataSetPublicByDataflowId(1L);
    Mockito.verify(reportingDatasetService, times(1)).getDataSetPublicByDataflow(Mockito.any());
  }

  /**
   * Find reporting data set id by dataflow id and provider id test.
   */
  @Test
  public void findReportingDataSetIdByDataflowIdAndProviderIdTest() {
    datasetMetabaseControllerImpl.findReportingDataSetIdByDataflowIdAndProviderId(1L, 1L);
    Mockito.verify(reportingDatasetService, times(1))
        .getDataSetIdByDataflowIdAndDataProviderId(Mockito.any(), Mockito.any());
  }

  /**
   * Find reporting data set public by dataflow id and provider id.
   */
  @Test
  public void findReportingDataSetPublicByDataflowIdAndProviderId() {
    datasetMetabaseControllerImpl.findReportingDataSetPublicByDataflowIdAndProviderId(1L, 1L);
    Mockito.verify(reportingDatasetService, times(1))
        .getDataSetPublicByDataflowAndProviderId(Mockito.any(), Mockito.any());
  }

  /**
   * Gets the datasets summary list test.
   *
   * @return the datasets summary list test
   */
  @Test
  public void getDatasetsSummaryListTest() {
    datasetMetabaseControllerImpl.getDatasetsSummaryList(1L);
    Mockito.verify(datasetMetabaseService, times(1)).getDatasetsSummaryList(1L);
  }

  /**
   * Update dataset running status test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDatasetRunningStatusTest() throws EEAException {
    datasetMetabaseControllerImpl.updateDatasetRunningStatus(Mockito.anyLong(), Mockito.any());
    Mockito.verify(datasetMetabaseService, times(1)).updateDatasetRunningStatus(Mockito.anyLong(),
        Mockito.any());
  }

  /**
   * Update dataset running status EEA exception test.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateDatasetRunningStatusEEAExceptionTest() throws Exception {
    Mockito.doThrow(EEAException.class).when(datasetMetabaseService)
        .updateDatasetRunningStatus(Mockito.anyLong(), Mockito.any());
    try {
      datasetMetabaseControllerImpl.updateDatasetRunningStatus(Mockito.anyLong(), Mockito.any());
    } catch (ResponseStatusException e) {
      assertEquals(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR);
      assertEquals(e.getReason(), EEAErrorMessage.UPDATING_DATASET_STATUS);
      throw e;
    }
  }

  /**
   * Find reporting data set by dataflow ids test.
   */
  @Test
  public void findReportingDataSetByDataflowIdsTest() {
    List<Long> dataflowIds = new ArrayList<>();
    dataflowIds.add(1L);
    datasetMetabaseControllerImpl.findReportingDataSetByDataflowIds(dataflowIds);
    Mockito.verify(reportingDatasetService, times(1)).getReportingsByDataflowIds(dataflowIds);
  }

  /**
   * Find data set by dataflow ids test.
   */
  @Test
  public void findDataSetByDataflowIdsTest() {
    List<Long> dataflowIds = new ArrayList<>();
    dataflowIds.add(1L);
    datasetMetabaseControllerImpl.findDataSetByDataflowIds(dataflowIds);
    Mockito.verify(datasetMetabaseService, times(1)).findDataSetByDataflowIds(dataflowIds);
  }

  /**
   * Gets the user provider ids by dataflow id test.
   *
   * @return the user provider ids by dataflow id test
   */
  @Test
  public void getUserProviderIdsByDataflowIdTest() {
    datasetMetabaseControllerImpl.getUserProviderIdsByDataflowId(1L);
    Mockito.verify(datasetMetabaseService, times(1)).getUserProviderIdsByDataflowId(1L);
  }

  /**
   * Gets the dataset ids by dataflow id and data provider id test.
   *
   * @return the dataset ids by dataflow id and data provider id test
   */
  @Test
  public void getDatasetIdsByDataflowIdAndDataProviderIdTest() {
    datasetMetabaseControllerImpl.getDatasetIdsByDataflowIdAndDataProviderId(1L, 1L);
    Mockito.verify(datasetMetabaseService, times(1)).getDatasetIdsByDataflowIdAndDataProviderId(1L,
        1L);
  }

  /**
   * Gets the type test.
   *
   * @return the type test
   */
  @Test
  public void getTypeTest() {
    datasetMetabaseControllerImpl.getType(1L);
    Mockito.verify(datasetMetabaseService, times(1)).getDatasetType(1L);
  }

  /**
   * Find reporting data set by provider ids test.
   */
  @Test
  public void findReportingDataSetByProviderIdsTest() {
    assertNotNull(datasetMetabaseControllerImpl.findReportingDataSetByProviderIds(null));
  }

}
