package org.eea.dataset.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import java.io.IOException;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
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

  @Test(expected = ResponseStatusException.class)
  public void testLoadDatasetDataThrowException4() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    doThrow(new EEAException(EEAErrorMessage.FILE_FORMAT)).when(datasetService)
        .processFile(Mockito.any(), Mockito.any());
    dataSetControllerImpl.loadDatasetData(1L, file);
  }

  @Test(expected = ResponseStatusException.class)
  public void testLoadDatasetDataThrowException5() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    doThrow(new EEAException(EEAErrorMessage.FILE_EXTENSION)).when(datasetService)
        .processFile(Mockito.any(), Mockito.any());
    dataSetControllerImpl.loadDatasetData(1L, file);
  }

  @Test(expected = ResponseStatusException.class)
  public void testLoadDatasetDataThrowException6() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    doThrow(new IOException()).when(datasetService).processFile(Mockito.any(), Mockito.any());
    dataSetControllerImpl.loadDatasetData(1L, file);
  }

  @Test
  public void testLoadDatasetDataSuccess() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    doNothing().when(datasetService).processFile(Mockito.any(), Mockito.any());
    dataSetControllerImpl.loadDatasetData(1L, file);
  }
}
