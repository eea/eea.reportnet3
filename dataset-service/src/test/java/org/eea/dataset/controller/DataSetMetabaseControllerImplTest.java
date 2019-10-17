package org.eea.dataset.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.dataset.service.ReportingDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
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


  @Mock
  private DesignDatasetService designDatasetService;



  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
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
  public void createEmptyDataSetTestExceptionEntry1() throws Exception {
    dataSetMetabaseControllerImpl.createEmptyDataSet(TypeDatasetEnum.REPORTING, null, null, 1L);
  }

  /**
   * Creates the removeDatasetData data set test exception entry 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetTestExceptionEntry2() throws Exception {
    dataSetMetabaseControllerImpl.createEmptyDataSet(TypeDatasetEnum.REPORTING, "", "", 1L);
  }

  @Test()
  public void createEmptyDataSetTestExceptionFail() throws Exception {
    doThrow(EEAException.class).when(datasetMetabaseService)
        .createEmptyDataset(TypeDatasetEnum.REPORTING, "datasetName", null, 1L);
    dataSetMetabaseControllerImpl.createEmptyDataSet(TypeDatasetEnum.REPORTING, "datasetName", null,
        1L);
  }

  /**
   * Creates the removeDatasetData data set test.
   *
   * @throws Exception the exception
   */
  @Test
  public void createEmptyDataSetTest() throws Exception {
    doNothing().when(datasetMetabaseService).createEmptyDataset(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    dataSetMetabaseControllerImpl.createEmptyDataSet(TypeDatasetEnum.REPORTING, "datasetName", null,
        1L);

    Mockito.verify(datasetMetabaseService, times(1)).createEmptyDataset(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
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

}
