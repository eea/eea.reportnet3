package org.eea.dataset.controller;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.TableVO;
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

@RunWith(MockitoJUnitRunner.class)
public class DataSetControllerImplTest {

  @InjectMocks
  DataSetControllerImpl dataSetControllerImpl;

  @Mock
  DatasetServiceImpl datasetService;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = ResponseStatusException.class)
  public void testLoadDatasetDataThrowException() throws Exception {
    MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    dataSetControllerImpl.loadDatasetData(null, fileNoExtension);
  }

  @Test(expected = ResponseStatusException.class)
  public void testLoadDatasetDataThrowException2() throws Exception {
    MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", (byte[]) null);
    dataSetControllerImpl.loadDatasetData(null, fileNoExtension);
  }

  @Test(expected = ResponseStatusException.class)
  public void testLoadDatasetDataThrowException3() throws Exception {
    dataSetControllerImpl.loadDatasetData(1L, null);
  }

  @Test
  public void testLoadDatasetDataSuccess() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    // ThreadPoolExecutor threadPoolExecutor =
    // (ThreadPoolExecutor) Mockito.spy(Executors.newFixedThreadPool(1));
    // doReturn(null).when(threadPoolExecutor).submit(Mockito.any(LoadDataCallable.class));
    dataSetControllerImpl.loadDatasetData(1L, file);
  }

  @Test(expected = ResponseStatusException.class)
  public void testDeleteImportDataThrowNonProvided() throws Exception {
    dataSetControllerImpl.deleteImportData(null);
  }

  @Test(expected = ResponseStatusException.class)
  public void testDeleteImportDataThrowinvalid() throws Exception {
    dataSetControllerImpl.deleteImportData(-2L);
  }

  @Test
  public void testDeleteImportDataThrowInternalServer() throws Exception {
    dataSetControllerImpl.deleteImportData(1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void testgetDataTablesValuesExceptionEntry1() throws Exception {
    dataSetControllerImpl.getDataTablesValues(null, "mongoId", 1, 1, "field", true);
  }

  @Test(expected = ResponseStatusException.class)
  public void testgetDataTablesValuesExceptionEntry2() throws Exception {
    dataSetControllerImpl.getDataTablesValues(1L, null, 1, 1, "field", true);
  }

  @Test(expected = ResponseStatusException.class)
  public void testgetDataTablesValuesExceptionEntry3() throws Exception {
    doThrow(new EEAException(EEAErrorMessage.DATASET_NOTFOUND)).when(datasetService)
        .getTableValuesById(Mockito.any(), Mockito.any());
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, null, true);
  }

  @Test(expected = ResponseStatusException.class)
  public void testgetDataTablesValuesExceptionEntry4() throws Exception {
    doThrow(new EEAException(EEAErrorMessage.FILE_FORMAT)).when(datasetService)
        .getTableValuesById(Mockito.any(), Mockito.any());
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, null, true);
  }

  @Test
  public void testgetDataTablesValuesExceptionEntry5() throws Exception {
    when(datasetService.getTableValuesById(Mockito.any(), Mockito.any())).thenReturn(new TableVO());
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, "field", false);
  }

  @Test
  public void testgetDataTablesValuesSuccess() throws Exception {
    when(datasetService.getTableValuesById(Mockito.any(), Mockito.any())).thenReturn(new TableVO());
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, "field", true);
  }

  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetTestExceptionEntry1() throws Exception {
    dataSetControllerImpl.createEmptyDataSet(null);
  }

  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetTestExceptionEntry2() throws Exception {
    dataSetControllerImpl.createEmptyDataSet("");
  }

  @Test
  public void createEmptyDataSetTest() throws Exception {
    doNothing().when(datasetService).createEmptyDataset(Mockito.any());
    dataSetControllerImpl.createEmptyDataSet("datasetName");
  }

  @Test(expected = ResponseStatusException.class)
  public void findByIdTestExceptionEntry1() throws Exception {
    dataSetControllerImpl.findById(-2L);
  }

  @Test(expected = ResponseStatusException.class)
  public void findByIdTestExceptionEntry2() throws Exception {
    when(datasetService.getDatasetValuesById(Mockito.any()))
        .thenThrow(new EEAException(EEAErrorMessage.DATASET_NOTFOUND));
    dataSetControllerImpl.findById(1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void findByIdTestExceptionEntry3() throws Exception {
    when(datasetService.getDatasetValuesById(Mockito.any()))
        .thenThrow(new EEAException(EEAErrorMessage.DATASET_INCORRECT_ID));
    dataSetControllerImpl.findById(1L);
  }

  @Test
  public void findByIdTestSuccess() throws Exception {
    when(datasetService.getDatasetValuesById(Mockito.any())).thenReturn(new DataSetVO());
    dataSetControllerImpl.findById(1L);
    assertNotNull("null result of the datasetValues", dataSetControllerImpl.findById(1L));
  }
}
