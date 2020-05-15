package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eea.dataset.service.helper.DeleteHelper;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.helper.UpdateRecordHelper;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.dataset.service.impl.DesignDatasetServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.ETLDatasetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationLinkVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

  /** The design dataset service. */
  @Mock
  DesignDatasetServiceImpl designDatasetService;

  /** The records. */
  List<RecordVO> records;

  /** The record ids. */
  String recordId;

  /** The update record helper. */
  @Mock
  UpdateRecordHelper updateRecordHelper;

  /** The file treatment helper. */
  @Mock
  private FileTreatmentHelper fileTreatmentHelper;

  /** The security context. */
  SecurityContext securityContext;

  /** The authentication. */
  Authentication authentication;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    records = new ArrayList<>();
    records.add(new RecordVO());
    recordId = "1L";
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.initMocks(this);
  }


  /**
   * Test load dataset data throw exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testLoadDatasetDataThrowException2() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
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
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    dataSetControllerImpl.loadTableData(1L, null, null);
  }

  /**
   * Test load dataset data success 2.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLoadDatasetDataSuccess2() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    final EEAMockMultipartFile file =
        new EEAMockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes(), false);
    doNothing().when(fileTreatmentHelper).executeFileProcess(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    dataSetControllerImpl.loadTableData(1L, file, "example");
    Mockito.verify(fileTreatmentHelper, times(1)).executeFileProcess(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Test load data read only exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testLoadDataReadOnlyException() throws Exception {
    try {
      Mockito.when(datasetService.getDatasetType(Mockito.anyLong()))
          .thenReturn(DatasetTypeEnum.REPORTING);
      Mockito.when(datasetService.getTableReadOnly(Mockito.anyLong(), Mockito.any(), Mockito.any()))
          .thenReturn(true);

      Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
      Mockito.when(authentication.getName()).thenReturn("user");
      final EEAMockMultipartFile file =
          new EEAMockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes(), false);
      dataSetControllerImpl.loadTableData(1L, file, "example");
    } catch (ResponseStatusException e) {
      assertEquals(EEAErrorMessage.TABLE_READ_ONLY, e.getReason());
      throw e;
    }
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
    doNothing().when(deleteHelper).executeDeleteDatasetProcess(Mockito.any());
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    dataSetControllerImpl.deleteImportData(1L);
    Mockito.verify(deleteHelper, times(1)).executeDeleteDatasetProcess(Mockito.any());
  }

  /**
   * Test delete dataset values exception deleting.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteDatasetValuesExceptionDeleting() throws EEAException {

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException()).when(deleteHelper).executeDeleteDatasetProcess(Mockito.any());
    try {
      dataSetControllerImpl.deleteImportData(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  /**
   * Test get data tables values exception entry 1.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetDataTablesValuesExceptionEntry1() throws Exception {
    String fields = "field_1,fields_2,fields_3";
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING};
    dataSetControllerImpl.getDataTablesValues(null, "mongoId", 1, 1, fields, errorfilter);
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
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING};
    dataSetControllerImpl.getDataTablesValues(1L, null, 1, 1, fields, errorfilter);
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
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, null, null);
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
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, null, null);
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
    String fields = "field_1,fields_2,fields_3";
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING};
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, fields, errorfilter);
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
    List<Boolean> order = new ArrayList<>(Arrays.asList(new Boolean[2]));
    Collections.fill(order, Boolean.TRUE);
    String fields = "field_1,fields_2,fields_3";
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING};
    dataSetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, fields, errorfilter);

    Mockito.verify(datasetService, times(1)).getTableValuesById(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
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
   * Test get position from any object id.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetPositionFromAnyObjectId() throws Exception {

    when(datasetService.getPositionFromAnyObjectId(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new ValidationLinkVO());
    dataSetControllerImpl.getPositionFromAnyObjectId("1L", 1L, EntityTypeEnum.TABLE);
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
    dataSetControllerImpl.getPositionFromAnyObjectId("1L", 1L, EntityTypeEnum.TABLE);
  }


  /**
   * Test get position from any object id exception 3.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetPositionFromAnyObjectIdException3() throws Exception {
    dataSetControllerImpl.getPositionFromAnyObjectId("1L", null, null);
  }

  /** The delete helper. */
  @Mock
  private DeleteHelper deleteHelper;

  /**
   * Test delete import table.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testDeleteImportTable() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    dataSetControllerImpl.deleteImportTable(1L, "");
    Mockito.verify(deleteHelper, times(1)).executeDeleteTableProcess(Mockito.any(), Mockito.any());
  }


  /**
   * Test delete import table throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteImportTableThrow() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException()).when(deleteHelper).executeDeleteTableProcess(Mockito.any(),
        Mockito.any());
    dataSetControllerImpl.deleteImportTable(1L, "");

  }



  /**
   * Test delete import table read only exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteImportTableReadOnlyException() throws Exception {
    try {
      Mockito.when(datasetService.getDatasetType(Mockito.anyLong()))
          .thenReturn(DatasetTypeEnum.REPORTING);
      Mockito.when(datasetService.getTableReadOnly(Mockito.anyLong(), Mockito.any(), Mockito.any()))
          .thenReturn(true);

      Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
      Mockito.when(authentication.getName()).thenReturn("user");

      dataSetControllerImpl.deleteImportTable(1L, "");
    } catch (ResponseStatusException e) {
      assertEquals(EEAErrorMessage.TABLE_READ_ONLY, e.getReason());
      throw e;
    }
  }

  /**
   * Testupdate records null entry.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testupdateRecordsNullEntry() throws Exception {
    dataSetControllerImpl.updateRecords(null, new ArrayList<RecordVO>());
  }

  /**
   * Testupdate records null.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testupdateRecordsNull() throws Exception {
    dataSetControllerImpl.updateRecords(-2L, null);
  }

  /**
   * Testupdate records empty.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testupdateRecordsEmpty() throws Exception {
    dataSetControllerImpl.updateRecords(1L, new ArrayList<RecordVO>());
  }

  /**
   * Testupdate records success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testupdateRecordsSuccess() throws Exception {
    doNothing().when(updateRecordHelper).executeUpdateProcess(Mockito.any(), Mockito.any());
    dataSetControllerImpl.updateRecords(1L, records);
    Mockito.verify(updateRecordHelper, times(1)).executeUpdateProcess(Mockito.any(), Mockito.any());
  }

  /**
   * Test update records read only exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateRecordsReadOnlyException() throws Exception {
    try {
      Mockito.when(datasetService.getDatasetType(Mockito.anyLong()))
          .thenReturn(DatasetTypeEnum.REPORTING);
      Mockito.when(datasetService.getTableReadOnly(Mockito.anyLong(), Mockito.any(), Mockito.any()))
          .thenReturn(true);

      dataSetControllerImpl.updateRecords(1L, records);
    } catch (ResponseStatusException e) {
      assertEquals(EEAErrorMessage.TABLE_READ_ONLY, e.getReason());
      throw e;
    }
  }

  /**
   * Testupdate records not found exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testupdateRecordsNotFoundException() throws Exception {
    doThrow(new EEAException()).when(updateRecordHelper).executeUpdateProcess(Mockito.any(),
        Mockito.any());
    dataSetControllerImpl.updateRecords(1L, records);
  }



  /**
   * Testdelete record success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testdeleteRecordSuccess() throws Exception {
    doNothing().when(updateRecordHelper).executeDeleteProcess(Mockito.any(), Mockito.any());
    dataSetControllerImpl.deleteRecord(1L, recordId);
    Mockito.verify(updateRecordHelper, times(1)).executeDeleteProcess(Mockito.any(), Mockito.any());
  }

  /**
   * Test delete record read only exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteRecordReadOnlyException() throws Exception {
    try {
      Mockito.when(datasetService.getDatasetType(Mockito.anyLong()))
          .thenReturn(DatasetTypeEnum.REPORTING);
      Mockito.when(datasetService.getTableReadOnly(Mockito.anyLong(), Mockito.any(), Mockito.any()))
          .thenReturn(true);

      dataSetControllerImpl.deleteRecord(1L, recordId);
    } catch (ResponseStatusException e) {
      assertEquals(EEAErrorMessage.TABLE_READ_ONLY, e.getReason());
      throw e;
    }
  }

  /**
   * Testdelete record not found exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testdeleteRecordNotFoundException() throws Exception {
    doThrow(new EEAException()).when(updateRecordHelper).executeDeleteProcess(Mockito.any(),
        Mockito.any());
    dataSetControllerImpl.deleteRecord(1L, recordId);
  }

  /**
   * Testinsert records null entry.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testinsertRecordsNullEntry() throws Exception {
    dataSetControllerImpl.insertRecords(null, "id", new ArrayList<RecordVO>());
  }

  /**
   * Testinsert records null.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testinsertRecordsNull() throws Exception {
    dataSetControllerImpl.insertRecords(-2L, "id", null);
  }

  /**
   * Testinsert records empty.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testinsertRecordsEmpty() throws Exception {
    dataSetControllerImpl.insertRecords(1L, "id", new ArrayList<RecordVO>());
  }

  /**
   * Testinsert records success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testinsertRecordsSuccess() throws Exception {
    doNothing().when(updateRecordHelper).executeCreateProcess(Mockito.any(), Mockito.any(),
        Mockito.any());
    dataSetControllerImpl.insertRecords(1L, "id", records);
    Mockito.verify(updateRecordHelper, times(1)).executeCreateProcess(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Testinsert records not found exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testinsertRecordsNotFoundException() throws Exception {
    doThrow(new EEAException()).when(updateRecordHelper).executeCreateProcess(Mockito.any(),
        Mockito.any(), Mockito.any());
    dataSetControllerImpl.insertRecords(1L, "id", records);
  }


  /**
   * Testinsert records table read only exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testinsertRecordsTableReadOnlyException() throws Exception {
    try {
      Mockito.when(datasetService.getDatasetType(Mockito.anyLong()))
          .thenReturn(DatasetTypeEnum.REPORTING);
      Mockito.when(datasetService.getTableReadOnly(Mockito.anyLong(), Mockito.any(), Mockito.any()))
          .thenReturn(true);

      dataSetControllerImpl.insertRecords(1L, "id", records);
    } catch (ResponseStatusException e) {
      assertEquals(EEAErrorMessage.TABLE_READ_ONLY, e.getReason());
      throw e;
    }
  }

  /**
   * Export file reporting.
   *
   * @throws Exception the exception
   */
  @Test
  public void exportFileReporting() throws Exception {
    Mockito.when(datasetService.exportFile(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn("".getBytes());
    Mockito.when(datasetService.isReportingDataset(Mockito.any())).thenReturn(true);
    dataSetControllerImpl.exportFile(1L, "id", "csv");
    Mockito.verify(datasetService, times(1)).getFileName(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Export file design.
   *
   * @throws Exception the exception
   */
  @Test
  public void exportFileDesign() throws Exception {
    Mockito.when(datasetService.exportFile(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn("".getBytes());
    Mockito.when(datasetService.isReportingDataset(Mockito.any())).thenReturn(false);
    dataSetControllerImpl.exportFile(1L, "id", "csv");
    Mockito.verify(designDatasetService, times(1)).getFileNameDesign(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Export file test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void exportFileTest() throws EEAException, IOException {
    Mockito.when(datasetService.exportFile(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenThrow(EEAException.class);
    try {
      dataSetControllerImpl.exportFile(1L, "id", "csv");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }
  }



  /**
   * Testupdate field success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testupdateFieldSuccess() throws Exception {
    doNothing().when(updateRecordHelper).executeFieldUpdateProcess(Mockito.any(), Mockito.any());
    dataSetControllerImpl.updateField(1L, new FieldVO());
    Mockito.verify(updateRecordHelper, times(1)).executeFieldUpdateProcess(Mockito.any(),
        Mockito.any());
  }

  /**
   * Testupdate field not found exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testupdateFieldNotFoundException() throws Exception {
    doThrow(new EEAException()).when(updateRecordHelper).executeFieldUpdateProcess(Mockito.any(),
        Mockito.any());
    dataSetControllerImpl.updateField(1L, new FieldVO());
  }

  /**
   * Test update field read only exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateFieldReadOnlyException() throws Exception {
    try {
      Mockito.when(datasetService.getDatasetType(Mockito.anyLong()))
          .thenReturn(DatasetTypeEnum.REPORTING);
      Mockito.when(datasetService.getTableReadOnly(Mockito.anyLong(), Mockito.any(), Mockito.any()))
          .thenReturn(true);

      dataSetControllerImpl.updateField(1L, new FieldVO());
    } catch (ResponseStatusException e) {
      assertEquals(EEAErrorMessage.TABLE_READ_ONLY, e.getReason());
      throw e;
    }
  }

  /**
   * Insert id data schema.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertIdDataSchema() throws EEAException {
    doNothing().when(datasetService).insertSchema(Mockito.anyLong(), Mockito.any());
    dataSetControllerImpl.insertIdDataSchema(Mockito.anyLong(), Mockito.any());
    Mockito.verify(datasetService, times(1)).insertSchema(Mockito.anyLong(), Mockito.any());
  }

  /**
   * Insert id data schema throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void insertIdDataSchemaThrow() throws EEAException {
    doThrow(EEAException.class).when(datasetService).insertSchema(Mockito.anyLong(), Mockito.any());
    dataSetControllerImpl.insertIdDataSchema(Mockito.anyLong(), Mockito.any());
    Mockito.verify(datasetService, times(1)).insertSchema(Mockito.anyLong(), Mockito.any());
  }

  /**
   * Gets the dataset type.
   *
   * @return the dataset type
   */
  @Test
  public void getDatasetTypeTest() {
    Mockito.when(datasetService.getDatasetType(Mockito.any())).thenReturn(DatasetTypeEnum.DESIGN);
    Assert.assertEquals(DatasetTypeEnum.DESIGN, dataSetControllerImpl.getDatasetType(1L));
  }

  /**
   * Gets the field values referenced.
   *
   * @return the field values referenced
   */
  @Test
  public void getFieldValuesReferencedTest() {
    List<FieldVO> fields = new ArrayList<>();
    fields.add(new FieldVO());
    Mockito
        .when(datasetService.getFieldValuesReferenced(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(fields);
    assertEquals("error", fields, dataSetControllerImpl.getFieldValuesReferenced(1L, "", ""));
  }

  /**
   * Gets the referenced dataset id.
   *
   * @return the referenced dataset id
   */
  @Test
  public void getReferencedDatasetId() {
    Mockito.when(datasetService.getReferencedDatasetId(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    assertEquals("error", Long.valueOf(1L), dataSetControllerImpl.getReferencedDatasetId(1L, ""));
  }

  /**
   * Etl export dataset test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void etlExportDatasetTest() throws EEAException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    dataSetControllerImpl.etlExportDataset(1L, 1L, 1L);
    Mockito.verify(datasetService, times(1)).etlExportDataset(Mockito.anyLong());
  }

  /**
   * Etl export dataset dataflow exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void etlExportDatasetDataflowExceptionTest() throws EEAException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(null);
    try {
      dataSetControllerImpl.etlExportDataset(1L, 1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  /**
   * Etl export dataset exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void etlExportDatasetExceptionTest() throws EEAException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    doThrow(new EEAException()).when(datasetService).etlExportDataset(Mockito.any());
    try {
      dataSetControllerImpl.etlExportDataset(1L, 1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }


  /**
   * Etl import dataset test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void etlImportDatasetTest() throws EEAException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    dataSetControllerImpl.etlImportDataset(1L, new ETLDatasetVO(), 1L, 1L);
    Mockito.verify(datasetService, times(1)).etlImportDataset(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Etl import dataset dataflow exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void etlImportDatasetDataflowExceptionTest() throws EEAException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(null);
    try {
      dataSetControllerImpl.etlImportDataset(1L, new ETLDatasetVO(), 1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  /**
   * Etl import dataset exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void etlImportDatasetExceptionTest() throws EEAException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    doThrow(new EEAException()).when(datasetService).etlImportDataset(Mockito.any(), Mockito.any(),
        Mockito.any());
    try {
      dataSetControllerImpl.etlImportDataset(1L, new ETLDatasetVO(), 1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

}
