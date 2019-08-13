package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.helper.UpdateRecordHelper;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationLinkVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
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

  /** The records. */
  List<RecordVO> records;

  /** The record ids. */
  List<Long> recordIds;

  @Mock
  UpdateRecordHelper updateRecordHelper;

  @Mock
  private FileTreatmentHelper fileTreatmentHelper;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    records = new ArrayList<>();
    records.add(new RecordVO());
    recordIds = new ArrayList<>();
    recordIds.add(1L);
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
    dataSetControllerImpl.loadTableData(null, fileNoExtension, null);
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
    dataSetControllerImpl.loadTableData(null, fileNoExtension, null);
  }

  /**
   * Test load dataset data throw exception 3.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testLoadDatasetDataThrowException3() throws Exception {
    dataSetControllerImpl.loadTableData(1L, null, null);
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
    dataSetControllerImpl.loadTableData(1L, file, "example");
  }


  /**
   * Test load dataset data success 2.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadDatasetDataSuccess2() throws Exception {
    final EEAMockMultipartFile file =
        new EEAMockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes(), false);
    doNothing().when(fileTreatmentHelper).executeFileProcess(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    dataSetControllerImpl.loadTableData(1L, file, "example");
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
    String fields = "field_1,fields_2,fields_3";
    dataSetControllerImpl.getDataTablesValues(null, "mongoId", 1, 1, fields);
  }

  /**
   * Testget data tables values exception entry 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetDataTablesValuesExceptionEntry2() throws Exception {
    List<Boolean> order = new ArrayList<>(Arrays.asList(new Boolean[2]));
    Collections.fill(order, Boolean.TRUE);
    String fields = "field_1,fields_2,fields_3";
    dataSetControllerImpl.getDataTablesValues(1L, null, 1, 1, fields);
  }

  /**
   * Testget data tables values exception entry 3.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetDataTablesValuesExceptionEntry3() throws Exception {
    doThrow(new EEAException(EEAErrorMessage.DATASET_NOTFOUND)).when(datasetService)
        .getTableValuesById(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, null);
  }

  /**
   * Testget data tables values exception entry 4.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetDataTablesValuesExceptionEntry4() throws Exception {
    doThrow(new EEAException(EEAErrorMessage.FILE_FORMAT)).when(datasetService)
        .getTableValuesById(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, null);
  }

  /**
   * Testget data tables values exception entry 5.
   *
   * @throws Exception the exception
   */
  // @Test
  public void testgetDataTablesValuesExceptionEntry5() throws Exception {
    when(datasetService.getTableValuesById(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(new TableVO());
    String fields = "field_1,fields_2,fields_3";
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, fields);
  }

  /**
   * Testget data tables values success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetDataTablesValuesSuccess() throws Exception {
    when(datasetService.getTableValuesById(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(new TableVO());
    List<Boolean> order = new ArrayList<>(Arrays.asList(new Boolean[2]));
    Collections.fill(order, Boolean.TRUE);
    String fields = "field_1,fields_2,fields_3";
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, fields);

    Mockito.verify(datasetService, times(1)).getTableValuesById(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Creates the removeDatasetData data set test exception entry 1.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetTestExceptionEntry1() throws Exception {
    dataSetControllerImpl.createEmptyDataSet(null, null, 1L);
  }

  /**
   * Creates the removeDatasetData data set test exception entry 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetTestExceptionEntry2() throws Exception {
    dataSetControllerImpl.createEmptyDataSet("", "", 1L);
  }

  /**
   * Creates the removeDatasetData data set test.
   *
   * @throws Exception the exception
   */
  @Test
  public void createEmptyDataSetTest() throws Exception {
    doNothing().when(datasetService).createEmptyDataset(Mockito.any(), Mockito.any(),
        Mockito.any());
    dataSetControllerImpl.createEmptyDataSet("datasetName", null, 1L);

    Mockito.verify(datasetService, times(1)).createEmptyDataset(Mockito.any(), Mockito.any(),
        Mockito.any());
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

  /**
   * Test update dataset success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUpdateDatasetSuccess() throws Exception {
    doNothing().when(datasetService).updateDataset(Mockito.any(), Mockito.any());
    dataSetControllerImpl.updateDataset(new DataSetVO());
    Mockito.verify(datasetService, times(1)).updateDataset(Mockito.any(), Mockito.any());
  }

  /**
   * Test update dataset error.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateDatasetError() throws Exception {
    doThrow(new EEAException()).when(datasetService).updateDataset(Mockito.any(), Mockito.any());
    dataSetControllerImpl.updateDataset(new DataSetVO());
    Mockito.verify(datasetService, times(1)).updateDataset(Mockito.any(), Mockito.any());
  }

  /**
   * Test update dataset exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateDatasetException() throws Exception {
    dataSetControllerImpl.updateDataset(null);
  }

  /**
   * Test load statistics.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadStatistics() throws Exception {
    when(datasetService.getStatistics(Mockito.any())).thenReturn(new StatisticsVO());
    dataSetControllerImpl.getStatisticsById(1L);

    Mockito.verify(datasetService, times(1)).getStatistics(Mockito.any());
  }


  /**
   * Test load statistics exception.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadStatisticsException() throws Exception {
    doThrow(new EEAException()).when(datasetService).getStatistics(Mockito.any());
    dataSetControllerImpl.getStatisticsById(null);

    Mockito.verify(datasetService, times(1)).getStatistics(Mockito.any());
  }

  /**
   * Test get position from any object id.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetPositionFromAnyObjectId() throws Exception {

    when(datasetService.getPositionFromAnyObjectId(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new ValidationLinkVO());
    dataSetControllerImpl.getPositionFromAnyObjectId(1L, 1L, TypeEntityEnum.TABLE);
    Mockito.verify(datasetService, times(1)).getPositionFromAnyObjectId(Mockito.any(),
        Mockito.any(), Mockito.any());
  }


  /**
   * Test get position from any object id exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetPositionFromAnyObjectIdException() throws Exception {
    dataSetControllerImpl.getPositionFromAnyObjectId(null, null, null);
  }


  /**
   * Test get position from any object id exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetPositionFromAnyObjectIdException2() throws Exception {

    doThrow(new EEAException(EEAErrorMessage.FILE_FORMAT)).when(datasetService)
        .getPositionFromAnyObjectId(Mockito.any(), Mockito.any(), Mockito.any());
    dataSetControllerImpl.getPositionFromAnyObjectId(1L, 1L, TypeEntityEnum.TABLE);
  }


  /**
   * Test get position from any object id exception 3.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetPositionFromAnyObjectIdException3() throws Exception {
    dataSetControllerImpl.getPositionFromAnyObjectId(1L, null, null);
  }


  /**
   * Test delete import table.
   */
  public void testDeleteImportTable() {
    doNothing().when(datasetService).deleteTableBySchema(Mockito.any(), Mockito.any());
    dataSetControllerImpl.deleteImportTable(1L, "");
    Mockito.verify(datasetService, times(1)).deleteTableBySchema(Mockito.any(), Mockito.any());

  }


  /**
   * Test delete import table throw non provided.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteImportTableThrowNonProvided() throws Exception {
    dataSetControllerImpl.deleteImportTable(null, "");
  }


  /**
   * Test delete import table throw invalid.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteImportTableThrowInvalid() throws Exception {
    dataSetControllerImpl.deleteImportTable(-2L, "");
  }

  @Test(expected = ResponseStatusException.class)
  public void testupdateRecordsNullEntry() throws Exception {
    dataSetControllerImpl.updateRecords(null, new ArrayList<RecordVO>());
  }

  @Test(expected = ResponseStatusException.class)
  public void testupdateRecordsNull() throws Exception {
    dataSetControllerImpl.updateRecords(-2L, null);
  }

  @Test(expected = ResponseStatusException.class)
  public void testupdateRecordsEmpty() throws Exception {
    dataSetControllerImpl.updateRecords(1L, new ArrayList<RecordVO>());
  }

  @Test
  public void testupdateRecordsSuccess() throws Exception {
    doNothing().when(updateRecordHelper).executeUpdateProcess(Mockito.any(), Mockito.any());
    dataSetControllerImpl.updateRecords(1L, records);
    Mockito.verify(updateRecordHelper, times(1)).executeUpdateProcess(Mockito.any(), Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testupdateRecordsNotFoundException() throws Exception {
    doThrow(new EEAException()).when(updateRecordHelper).executeUpdateProcess(Mockito.any(),
        Mockito.any());
    dataSetControllerImpl.updateRecords(1L, records);
  }


  @Test(expected = ResponseStatusException.class)
  public void testdeleteRecordsNullEntry() throws Exception {
    dataSetControllerImpl.deleteRecords(null, new ArrayList<Long>());
  }

  @Test(expected = ResponseStatusException.class)
  public void testdeleteRecordsNull() throws Exception {
    dataSetControllerImpl.deleteRecords(-2L, null);
  }

  @Test(expected = ResponseStatusException.class)
  public void testdeleteRecordsEmpty() throws Exception {
    dataSetControllerImpl.deleteRecords(1L, new ArrayList<Long>());
  }

  @Test
  public void testdeleteRecordsSuccess() throws Exception {
    doNothing().when(updateRecordHelper).executeDeleteProcess(Mockito.any(), Mockito.any());
    dataSetControllerImpl.deleteRecords(1L, recordIds);
    Mockito.verify(updateRecordHelper, times(1)).executeDeleteProcess(Mockito.any(), Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testdeleteRecordsNotFoundException() throws Exception {
    doThrow(new EEAException()).when(updateRecordHelper).executeDeleteProcess(Mockito.any(),
        Mockito.any());
    dataSetControllerImpl.deleteRecords(1L, recordIds);
  }

  @Test(expected = ResponseStatusException.class)
  public void testinsertRecordsNullEntry() throws Exception {
    dataSetControllerImpl.insertRecords(null, "id", new ArrayList<RecordVO>());
  }

  @Test(expected = ResponseStatusException.class)
  public void testinsertRecordsNull() throws Exception {
    dataSetControllerImpl.insertRecords(-2L, "id", null);
  }

  @Test(expected = ResponseStatusException.class)
  public void testinsertRecordsEmpty() throws Exception {
    dataSetControllerImpl.insertRecords(1L, "id", new ArrayList<RecordVO>());
  }

  @Test
  public void testinsertRecordsSuccess() throws Exception {
    doNothing().when(updateRecordHelper).executeCreateProcess(Mockito.any(), Mockito.any(),
        Mockito.any());
    dataSetControllerImpl.insertRecords(1L, "id", records);
    Mockito.verify(updateRecordHelper, times(1)).executeCreateProcess(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testinsertRecordsNotFoundException() throws Exception {
    doThrow(new EEAException()).when(updateRecordHelper).executeCreateProcess(Mockito.any(),
        Mockito.any(), Mockito.any());
    dataSetControllerImpl.insertRecords(1L, "id", records);
  }

  @Test
  public void exportFile() throws Exception {
    Mockito.when(datasetService.exportFile(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn("".getBytes());
    dataSetControllerImpl.exportFile(1L, "id", "csv");
  }


  @Test(expected = ResponseStatusException.class)
  public void testupdateFieldNullEntry() throws Exception {
    dataSetControllerImpl.updateField(null, new FieldVO());
  }

  @Test(expected = ResponseStatusException.class)
  public void testupdateFieldNull() throws Exception {
    dataSetControllerImpl.updateField(-2L, null);
  }

  @Test
  public void testupdateFieldSuccess() throws Exception {
    doNothing().when(updateRecordHelper).executeFieldUpdateProcess(Mockito.any(), Mockito.any());
    dataSetControllerImpl.updateField(1L, new FieldVO());
    Mockito.verify(updateRecordHelper, times(1)).executeFieldUpdateProcess(Mockito.any(),
        Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testupdateFieldNotFoundException() throws Exception {
    doThrow(new EEAException()).when(updateRecordHelper).executeFieldUpdateProcess(Mockito.any(),
        Mockito.any());
    dataSetControllerImpl.updateField(1L, new FieldVO());
  }
}
