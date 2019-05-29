package org.eea.dataset.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.metabase.TableCollectionVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataSetControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataSetControllerImplTest {

  /** The data set controller impl. */
  @InjectMocks
  DataSetControllerImpl dataSetControllerImpl;

  /** The dataset service. */
  @Mock
  DatasetServiceImpl datasetService;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test load dataset data throw exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testLoadDatasetDataThrowException() throws Exception {
    MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    dataSetControllerImpl.loadDatasetData(null, fileNoExtension);
  }

  /**
   * Test load dataset data throw exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testLoadDatasetDataThrowException2() throws Exception {
    MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", (byte[]) null);
    dataSetControllerImpl.loadDatasetData(null, fileNoExtension);
  }

  /**
   * Test load dataset data throw exception 3.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testLoadDatasetDataThrowException3() throws Exception {
    dataSetControllerImpl.loadDatasetData(1L, null);
  }

  /**
   * Test load dataset data success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadDatasetDataSuccess() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    // ThreadPoolExecutor threadPoolExecutor =
    // (ThreadPoolExecutor) Mockito.spy(Executors.newFixedThreadPool(1));
    // doReturn(null).when(threadPoolExecutor).submit(Mockito.any(LoadDataCallable.class));
    dataSetControllerImpl.loadDatasetData(1L, file);
  }

  /**
   * Test delete import data throw non provided.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteImportDataThrowNonProvided() throws Exception {
    dataSetControllerImpl.deleteImportData(null);
  }

  /**
   * Test delete import data throwinvalid.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteImportDataThrowinvalid() throws Exception {
    dataSetControllerImpl.deleteImportData(-2L);
  }

  /**
   * Test delete import data throw internal server.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteImportDataThrowInternalServer() throws Exception {
    dataSetControllerImpl.deleteImportData(1L);
  }

  /**
   * Testget data tables values exception entry 1.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testgetDataTablesValuesExceptionEntry1() throws Exception {
    dataSetControllerImpl.getDataTablesValues(null, "mongoId", 1, 1, "field", true);
  }

  /**
   * Testget data tables values exception entry 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testgetDataTablesValuesExceptionEntry2() throws Exception {
    dataSetControllerImpl.getDataTablesValues(1L, null, 1, 1, "field", true);
  }

  /**
   * Testget data tables values exception entry 3.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testgetDataTablesValuesExceptionEntry3() throws Exception {
    doThrow(new EEAException(EEAErrorMessage.DATASET_NOTFOUND)).when(datasetService)
        .getTableValuesById(Mockito.any(), Mockito.any());
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, null, true);
  }

  /**
   * Testget data tables values exception entry 4.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testgetDataTablesValuesExceptionEntry4() throws Exception {
    doThrow(new EEAException(EEAErrorMessage.FILE_FORMAT)).when(datasetService)
        .getTableValuesById(Mockito.any(), Mockito.any());
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, null, true);
  }

  /**
   * Testget data tables values exception entry 5.
   *
   * @throws Exception the exception
   */
  @Test
  public void testgetDataTablesValuesExceptionEntry5() throws Exception {
    when(datasetService.getTableValuesById(Mockito.any(), Mockito.any())).thenReturn(new TableVO());
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, "field", false);
  }

  /**
   * Testget data tables values success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testgetDataTablesValuesSuccess() throws Exception {
    when(datasetService.getTableValuesById(Mockito.any(), Mockito.any())).thenReturn(new TableVO());
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, "field", true);
  }

  /**
   * Creates the empty data set test exception entry 1.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetTestExceptionEntry1() throws Exception {
    dataSetControllerImpl.createEmptyDataSet(null);
  }

  /**
   * Creates the empty data set test exception entry 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetTestExceptionEntry2() throws Exception {
    dataSetControllerImpl.createEmptyDataSet("");
  }

  /**
   * Creates the empty data set test.
   *
   * @throws Exception the exception
   */
  @Test
  public void createEmptyDataSetTest() throws Exception {
    doNothing().when(datasetService).createEmptyDataset(Mockito.any());
    dataSetControllerImpl.createEmptyDataSet("datasetName");
  }


  /**
   * Load schema mongo exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void loadSchemaMongoException() throws Exception {
    dataSetControllerImpl.loadSchemaMongo(null, 1L, new TableCollectionVO());
  }

  /**
   * Load schema mongo exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void loadSchemaMongoException2() throws Exception {
    dataSetControllerImpl.loadSchemaMongo(1L, null, new TableCollectionVO());
  }

  /**
   * Load schema mongo exception 3.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void loadSchemaMongoException3() throws Exception {
    dataSetControllerImpl.loadSchemaMongo(1L, 1L, null);
  }

  /**
   * Load schema mongo EEA exception.
   *
   * @throws Exception the exception
   */
  @Test
  public void loadSchemaMongoEEAException() throws Exception {
    doThrow(new EEAException()).when(datasetService).setMongoTables(Mockito.any(), Mockito.any(),
        Mockito.any());
    dataSetControllerImpl.loadSchemaMongo(1L, 1L, new TableCollectionVO());
  }

  /**
   * Load schema mongo success.
   *
   * @throws Exception the exception
   */
  @Test
  public void loadSchemaMongoSuccess() throws Exception {
    doNothing().when(datasetService).setMongoTables(Mockito.any(), Mockito.any(), Mockito.any());
    dataSetControllerImpl.loadSchemaMongo(1L, 1L, new TableCollectionVO());
  }
}
