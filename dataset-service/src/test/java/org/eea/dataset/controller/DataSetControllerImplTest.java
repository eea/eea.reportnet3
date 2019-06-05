package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
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

  /**
   * The data set controller impl.
   */
  @InjectMocks
  DataSetControllerImpl dataSetControllerImpl;

  /**
   * The dataset service.
   */
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
    final MockMultipartFile fileNoExtension =
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
    final MockMultipartFile fileNoExtension =
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
  @Test(expected = ResponseStatusException.class)
  public void testLoadDatasetDataSuccess() throws Exception {
    final EEAMockMultipartFile file =
        new EEAMockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes(), true);
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
   * Test delete import data throwInvalid.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteImportDataThrowInvalid() throws Exception {
    dataSetControllerImpl.deleteImportData(-2L);
  }

  /**
   * Test delete import data throw internal server.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteImportDataSuccess() throws Exception {
    doNothing().when(datasetService).deleteImportData(Mockito.any());
    dataSetControllerImpl.deleteImportData(1L);
    Mockito.verify(datasetService, times(1)).deleteImportData(Mockito.any());
  }

  /**
   * Test get data tables values exception entry 1.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetDataTablesValuesExceptionEntry1() throws Exception {
    dataSetControllerImpl.getDataTablesValues(null, "mongoId", 1, 1, "field", true);
  }

  /**
   * Testget data tables values exception entry 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetDataTablesValuesExceptionEntry2() throws Exception {
    dataSetControllerImpl.getDataTablesValues(1L, null, 1, 1, "field", true);
  }

  /**
   * Testget data tables values exception entry 3.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetDataTablesValuesExceptionEntry3() throws Exception {
    doThrow(new EEAException(EEAErrorMessage.DATASET_NOTFOUND)).when(datasetService)
        .getTableValuesById(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any());
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, null, true);
  }

  /**
   * Testget data tables values exception entry 4.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetDataTablesValuesExceptionEntry4() throws Exception {
    doThrow(new EEAException(EEAErrorMessage.FILE_FORMAT)).when(datasetService).getTableValuesById(
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, null, true);
  }

  /**
   * Testget data tables values exception entry 5.
   *
   * @throws Exception the exception
   */
  // @Test
  public void testgetDataTablesValuesExceptionEntry5() throws Exception {
    when(datasetService.getTableValuesById(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(new TableVO());
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, "field", false);
  }

  /**
   * Testget data tables values success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetDataTablesValuesSuccess() throws Exception {
    when(datasetService.getTableValuesById(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(new TableVO());
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, "field", true);

    Mockito.verify(datasetService, times(1)).getTableValuesById(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
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

    Mockito.verify(datasetService, times(1)).createEmptyDataset(Mockito.any());
  }


  /**
   * Load schema mongo exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void loadDatasetSchemaException() throws Exception {
    dataSetControllerImpl.loadDatasetSchema(null, 1L, new TableCollectionVO());
  }

  /**
   * Load schema mongo exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void loadDatasetSchemaException2() throws Exception {
    dataSetControllerImpl.loadDatasetSchema(1L, null, new TableCollectionVO());
  }

  /**
   * Load schema mongo exception 3.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void loadDatasetSchemaException3() throws Exception {
    dataSetControllerImpl.loadDatasetSchema(1L, 1L, null);
  }

  /**
   * Load schema mongo EEA exception.
   *
   * @throws Exception the exception
   */
  @Test
  public void loadDatasetSchemaEEAException() throws Exception {
    doThrow(new EEAException()).when(datasetService).setDataschemaTables(Mockito.any(),
        Mockito.any(), Mockito.any());
    dataSetControllerImpl.loadDatasetSchema(1L, 1L, new TableCollectionVO());

    Mockito.verify(datasetService, times(1)).setDataschemaTables(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Load schema mongo success.
   *
   * @throws Exception the exception
   */
  @Test
  public void loadDatasetSchemaSuccess() throws Exception {
    doNothing().when(datasetService).setDataschemaTables(Mockito.any(), Mockito.any(),
        Mockito.any());
    dataSetControllerImpl.loadDatasetSchema(1L, 1L, new TableCollectionVO());

    Mockito.verify(datasetService, times(1)).setDataschemaTables(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Test get by id success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetByIdSuccess() throws Exception {
    when(datasetService.getById(Mockito.any())).thenReturn(new DataSetVO());
    DataSetVO result = dataSetControllerImpl.getById(1L);
    Mockito.verify(datasetService, times(1)).getById(Mockito.any());
    assertEquals("failed assertion", new DataSetVO(), result);
  }


  /**
   * Test get by id find exception.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetByIdFindException() throws Exception {
    doThrow(new EEAException()).when(datasetService).getById(Mockito.any());
    DataSetVO result = dataSetControllerImpl.getById(1L);
    Mockito.verify(datasetService, times(1)).getById(Mockito.any());
    assertNull("should be null", result);
  }


  /**
   * Test get by id exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetByIdException() throws Exception {
    dataSetControllerImpl.getById(null);
  }

  /**
   * Test get data flow id by id success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetDataFlowIdByIdSuccess() throws Exception {
    when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    assertNotNull("", dataSetControllerImpl.getDataFlowIdById(1L));
    Mockito.verify(datasetService, times(1)).getDataFlowIdById(Mockito.any());
  }

  /**
   * Test get data flow id by id find error.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetDataFlowIdByIdFindError() throws Exception {
    doThrow(new EEAException()).when(datasetService).getDataFlowIdById(Mockito.any());
    Long result = dataSetControllerImpl.getDataFlowIdById(1L);
    Mockito.verify(datasetService, times(1)).getDataFlowIdById(Mockito.any());
    assertNull("should be null", result);
  }

  /**
   * Test get by id exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetDataFlowIdByIdException() throws Exception {
    dataSetControllerImpl.getDataFlowIdById(null);
  }

  @Test
  public void testUpdateDatasetSuccess() throws Exception {
    when(datasetService.updateDataset(Mockito.any())).thenReturn(new DataSetVO());
    assertNotNull("error", dataSetControllerImpl.updateDataset(new DataSetVO()));
    Mockito.verify(datasetService, times(1)).updateDataset(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testUpdateDatasetError() throws Exception {
    doThrow(new EEAException()).when(datasetService).updateDataset(Mockito.any());
    DataSetVO result = dataSetControllerImpl.updateDataset(new DataSetVO());
    Mockito.verify(datasetService, times(1)).updateDataset(Mockito.any());
    assertNull("should be null", result);
  }

  @Test(expected = ResponseStatusException.class)
  public void testUpdateDatasetException() throws Exception {
    dataSetControllerImpl.updateDataset(null);
  }
}
