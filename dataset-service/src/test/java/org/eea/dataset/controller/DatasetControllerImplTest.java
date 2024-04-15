package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.eea.dataset.persistence.data.domain.AttachmentValue;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.helper.DeleteHelper;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.helper.UpdateRecordHelper;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.dataset.service.impl.DesignDatasetServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataset.*;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.lock.service.LockService;
import org.eea.utils.LiteralConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DatasetControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetControllerImplTest {

  /** The data set controller impl. */
  @InjectMocks
  private DatasetControllerImpl datasetControllerImpl;

  /** The dataset service. */
  @Mock
  private DatasetServiceImpl datasetService;

  /** The dataset metabase service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The lock service. */
  @Mock
  private LockService lockService;

  /** The design dataset service. */
  @Mock
  private DesignDatasetServiceImpl designDatasetService;

  /** The dataset schema service. */
  @Mock
  private DatasetSchemaService datasetSchemaService;

  /** The update record helper. */
  @Mock
  private UpdateRecordHelper updateRecordHelper;

  /** The file treatment helper. */
  @Mock
  private FileTreatmentHelper fileTreatmentHelper;

  /** The delete helper. */
  @Mock
  private DeleteHelper deleteHelper;

  @Mock
  HttpServletResponse httpServletResponse;

  /** The notification controller zuul. */
  @Mock
  private NotificationControllerZuul notificationControllerZuul;

  /** The data set metabase controller zuul. */
  @Mock
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  /** The data set metabase controller zuul. */
  @Mock
  private JobControllerZuul jobControllerZuul;

  /** The records. */
  private List<RecordVO> records;

  /** The record id. */
  private String recordId;

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;

  /** The file mock. */
  private MockMultipartFile fileMock;

  /** The folder. */
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

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
    fileMock = new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    MockitoAnnotations.openMocks(this);
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
    datasetControllerImpl.getDataTablesValues(null, "mongoId", 1, 1, fields, errorfilter, null,
        null, null);
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
    datasetControllerImpl.getDataTablesValues(1L, null, 1, 1, fields, errorfilter, null, null,
        null);
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
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    datasetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, null, null, null, null, null);
  }

  /**
   * Testget data tables values exception entry 4.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetDataTablesValuesExceptionEntry4() throws Exception {
    doThrow(new EEAException(EEAErrorMessage.FILE_FORMAT)).when(datasetService).getTableValuesById(
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    datasetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, null, null, null, null, null);
  }

  /**
   * Testget data tables values exception entry 5.
   *
   * @throws Exception the exception
   */
  @Test
  public void testgetDataTablesValuesExceptionEntry5() throws Exception {
    TableVO tablevo = new TableVO();
    tablevo.setId(1L);
    tablevo.setLevelError(ErrorTypeEnum.ERROR);
    when(datasetService.getTableValuesById(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(tablevo);
    String fields = "field_1,fields_2,fields_3";
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING};
    assertEquals(tablevo, datasetControllerImpl.getDataTablesValues(1L, "mongoId", 1, null, fields,
        errorfilter, null, null, null));
  }

  /**
   * Testget data tables values success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetDataTablesValuesSuccess() throws Exception {
    when(datasetService.getTableValuesById(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(new TableVO());
    List<Boolean> order = new ArrayList<>(Arrays.asList(new Boolean[2]));
    Collections.fill(order, Boolean.TRUE);
    String fields = "field_1,fields_2,fields_3";
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING};
    datasetControllerImpl.getDataTablesValues(1L, "mongoId", 1, 1, fields, errorfilter, null, null,
        null);

    Mockito.verify(datasetService, times(1)).getTableValuesById(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Test get data flow id by id success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetDataFlowIdByIdSuccess() throws Exception {
    when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    assertNotNull("", datasetControllerImpl.getDataFlowIdById(1L));
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
    datasetControllerImpl.updateDataset(new DataSetVO());
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
    datasetControllerImpl.updateDataset(new DataSetVO());
    Mockito.verify(datasetService, times(1)).updateDataset(Mockito.any(), Mockito.any());
  }

  /**
   * Test update dataset exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateDatasetException() throws Exception {
    datasetControllerImpl.updateDataset(null);
  }

  /**
   * Test import big file data.
   *
   * @throws Exception the exception
   */
  @Test
  public void testImportBigFileData() throws Exception {
    MultipartFile multipartFile =
        new MockMultipartFile("multipartFile", "multipartFile".getBytes());
    Mockito.when(jobControllerZuul.checkEligibilityOfJob(anyString(), anyBoolean(), anyLong(), anyLong(), anyList())).thenReturn(JobStatusEnum.IN_PROGRESS);
    doNothing().when(fileTreatmentHelper).importFileData(1L,2L, "tableSchemaId", multipartFile, true,
        1L, "delimiter", 0L);
    datasetControllerImpl.importBigFileData(1L, 2L, 1L, "tableSchemaId", multipartFile, true, 1L,
        "delimiter",null);
    Mockito.verify(fileTreatmentHelper, times(1)).importFileData(1L,2L, "tableSchemaId", multipartFile,
        true, 1L, "delimiter", 0L);
  }

  /**
   * Test import big file data EEA exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testImportBigFileDataEEAException() throws Exception {
    MultipartFile multipartFile =
        new MockMultipartFile("multipartFile", "multipartFile".getBytes());
    Mockito.when(jobControllerZuul.checkEligibilityOfJob(anyString(), anyBoolean(), anyLong(), anyLong(), anyList())).thenReturn(JobStatusEnum.IN_PROGRESS);
    doThrow(EEAException.class).when(fileTreatmentHelper).importFileData(1L,2L, "tableSchemaId",
        multipartFile, true, 1L, "delimiter", 0L);
    try {
      datasetControllerImpl.importBigFileData(1L, 2L, 1L, "tableSchemaId", multipartFile, true, 1L,
          "delimiter",null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.IMPORTING_FILE_DATASET, e.getReason());
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
    datasetControllerImpl.updateRecords(null, new ArrayList<>(), false);
  }

  /**
   * Testupdate records null.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testupdateRecordsNull() throws Exception {
    datasetControllerImpl.updateRecords(-2L, null, false);
  }

  /**
   * Testupdate records empty.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testupdateRecordsEmpty() throws Exception {
    datasetControllerImpl.updateRecords(1L, new ArrayList<>(), false);
  }

  /**
   * Testupdate records success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testupdateRecordsSuccess() throws Exception {
    doNothing().when(updateRecordHelper).executeUpdateProcess(Mockito.any(), Mockito.any(),
        Mockito.anyBoolean());
    datasetControllerImpl.updateRecords(1L, records, false);
    Mockito.verify(updateRecordHelper, times(1)).executeUpdateProcess(Mockito.any(), Mockito.any(),
        Mockito.anyBoolean());
  }

  /**
   * Test update records read only exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateRecordsReadOnlyException() throws Exception {
    try {
      Mockito.when(datasetService.checkIfDatasetLockedOrReadOnly(Mockito.anyLong(), Mockito.any(),
          Mockito.any())).thenReturn(true);
      datasetControllerImpl.updateRecords(1L, records, false);
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
        Mockito.any(), Mockito.anyBoolean());
    datasetControllerImpl.updateRecords(1L, records, false);
  }


  /**
   * Testdelete record success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testdeleteRecordSuccess() throws Exception {
    doNothing().when(updateRecordHelper).executeDeleteProcess(Mockito.any(), Mockito.any(),
        Mockito.anyBoolean());
    datasetControllerImpl.deleteRecord(1L, recordId, false);
    Mockito.verify(updateRecordHelper, times(1)).executeDeleteProcess(Mockito.any(), Mockito.any(),
        Mockito.anyBoolean());
  }

  /**
   * Testdelete record success dataset type design.
   *
   * @throws Exception the exception
   */
  @Test
  public void testdeleteRecordSuccessDatasetTypeDesign() throws Exception {
    Mockito.when(datasetMetabaseService.getDatasetType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.DESIGN);
    doNothing().when(updateRecordHelper).executeDeleteProcess(Mockito.any(), Mockito.any(),
        Mockito.anyBoolean());
    datasetControllerImpl.deleteRecord(1L, recordId, false);
    Mockito.verify(updateRecordHelper, times(1)).executeDeleteProcess(Mockito.any(), Mockito.any(),
        Mockito.anyBoolean());
  }

  /**
   * Test delete record read only exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteRecordReadOnlyException() throws Exception {
    try {
      Mockito.when(datasetService.checkIfDatasetLockedOrReadOnly(Mockito.anyLong(), Mockito.any(),
          Mockito.any())).thenReturn(true);

      datasetControllerImpl.deleteRecord(1L, recordId, false);
    } catch (ResponseStatusException e) {
      assertEquals(EEAErrorMessage.TABLE_READ_ONLY, e.getReason());
      throw e;
    }
  }

  /**
   * Test delete record fixed number exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteRecordFixedNumberException() throws Exception {
    try {
      Mockito.when(datasetMetabaseService.getDatasetType(Mockito.anyLong()))
          .thenReturn(DatasetTypeEnum.REPORTING);
      Mockito.when(datasetService.getTableFixedNumberOfRecords(Mockito.anyLong(), Mockito.any(),
          Mockito.any())).thenReturn(true);

      datasetControllerImpl.deleteRecord(1L, recordId, false);
    } catch (ResponseStatusException e) {
      assertEquals(String.format(EEAErrorMessage.FIXED_NUMBER_OF_RECORDS,
          datasetService.findRecordSchemaIdById(1L, recordId)), e.getReason());
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
        Mockito.any(), Mockito.anyBoolean());
    datasetControllerImpl.deleteRecord(1L, recordId, false);
  }

  /**
   * Testupdate field success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testupdateFieldSuccess() throws Exception {
    doNothing().when(updateRecordHelper).executeFieldUpdateProcess(Mockito.any(), Mockito.any(),
        Mockito.anyBoolean());
    datasetControllerImpl.updateField(1L, new FieldVO(), false);
    Mockito.verify(updateRecordHelper, times(1)).executeFieldUpdateProcess(Mockito.any(),
        Mockito.any(), Mockito.anyBoolean());
  }


  /**
   * Testupdate field not found exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testupdateFieldNotFoundException() throws Exception {
    doThrow(new EEAException()).when(updateRecordHelper).executeFieldUpdateProcess(Mockito.any(),
        Mockito.any(), Mockito.anyBoolean());
    datasetControllerImpl.updateField(1L, new FieldVO(), false);
  }

  /**
   * Test update field read only exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateFieldReadOnlyException() throws Exception {
    try {
      Mockito.when(datasetService.checkIfDatasetLockedOrReadOnly(Mockito.anyLong(), Mockito.any(),
          Mockito.any())).thenReturn(true);

      datasetControllerImpl.updateField(1L, new FieldVO(), false);
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
    datasetControllerImpl.insertIdDataSchema(Mockito.anyLong(), Mockito.any());
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
    datasetControllerImpl.insertIdDataSchema(Mockito.anyLong(), Mockito.any());
    Mockito.verify(datasetService, times(1)).insertSchema(Mockito.anyLong(), Mockito.any());
  }

  /**
   * Gets the dataset type.
   *
   * @return the dataset type
   */
  @Test
  public void getDatasetTypeTest() {
    Mockito.when(datasetMetabaseService.getDatasetType(Mockito.any()))
        .thenReturn(DatasetTypeEnum.DESIGN);
    Assert.assertEquals(DatasetTypeEnum.DESIGN, datasetControllerImpl.getDatasetType(1L));
  }

  /**
   * Gets the field values referenced.
   *
   * @return the field values referenced
   * @throws EEAException
   */
  @Test
  public void getFieldValuesReferencedTest() throws EEAException {
    List<FieldVO> fields = new ArrayList<>();
    fields.add(new FieldVO());
    Mockito.when(datasetService.getFieldValuesReferenced(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(fields);
    assertEquals("error", fields,
        datasetControllerImpl.getFieldValuesReferenced(1L, "", "", "", "", null));
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
    assertEquals("error", Long.valueOf(1L), datasetControllerImpl.getReferencedDatasetId(1L, ""));
  }

  /**
   * Etl export dataset test.
   *
   * @throws EEAException the EEA exception
   */
  // @Test
  // public void etlExportDatasetTest() throws EEAException {
  // Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
  // datasetControllerImpl.etlExportDataset(1L, 1L, 1L);
  // Mockito.verify(datasetService, times(1)).etlExportDataset(Mockito.anyLong());
  // }

  /**
   * Etl export dataset dataflow exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void etlExportDatasetDataflowExceptionTest() throws EEAException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(null);
    try {
      datasetControllerImpl.etlExportDataset(1L, 1L, 1L, "", 1, 1, "", "", "");
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }


  /**
   * Etl export dataset dataflow legacy test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void etlExportDatasetDataflowLegacyTest() throws EEAException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(null);
    try {
      datasetControllerImpl.etlExportDatasetLegacy(1L, 1L, 1L, "", 1, 1, "", "");
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
  // @Test(expected = ResponseStatusException.class)
  // public void etlExportDatasetExceptionTest() throws EEAException {
  // Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
  // doThrow(new EEAException()).when(datasetService).etlExportDataset(Mockito.any());
  // try {
  // datasetControllerImpl.etlExportDataset(1L, 1L, 1L);
  // } catch (ResponseStatusException e) {
  // assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
  // throw e;
  // }
  // }


  /**
   * Etl import dataset test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void etlImportDatasetTest() throws EEAException {
    Mockito.when(jobControllerZuul.checkEligibilityOfJob(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyList())).thenReturn(JobStatusEnum.IN_PROGRESS);
    Mockito.when(jobControllerZuul.addEtlImportJob(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any())).thenReturn(1L);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(datasetService.isDatasetReportable(Mockito.any())).thenReturn(Boolean.TRUE);
    datasetControllerImpl.etlImportDataset(1L, new ETLDatasetVO(), 1L, 1L, false);
    Mockito.verify(fileTreatmentHelper, times(1)).etlImportDataset(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Etl import dataset legacy test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void etlImportDatasetLegacyTest() throws EEAException {
    Mockito.when(jobControllerZuul.checkEligibilityOfJob(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyList())).thenReturn(JobStatusEnum.IN_PROGRESS);
    Mockito.when(jobControllerZuul.addEtlImportJob(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any())).thenReturn(1L);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(datasetService.isDatasetReportable(Mockito.any())).thenReturn(Boolean.TRUE);
    datasetControllerImpl.etlImportDatasetLegacy(1L, new ETLDatasetVO(), 1L, 1L, false);
    Mockito.verify(fileTreatmentHelper, times(1)).etlImportDataset(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
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
      datasetControllerImpl.etlImportDataset(1L, new ETLDatasetVO(), 1L, 1L, false);
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
    Mockito.when(jobControllerZuul.checkEligibilityOfJob(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyList())).thenReturn(JobStatusEnum.IN_PROGRESS);
    Mockito.when(jobControllerZuul.addEtlImportJob(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any())).thenReturn(1L);
    Mockito.doNothing().when(jobControllerZuul).updateJobStatus(Mockito.any(), Mockito.any());
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(datasetService.isDatasetReportable(Mockito.any())).thenReturn(Boolean.TRUE);
    doThrow(new EEAException()).when(fileTreatmentHelper).etlImportDataset(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    try {
      datasetControllerImpl.etlImportDataset(1L, new ETLDatasetVO(), 1L, 1L, false);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Etl import dataset exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void etlImportDatasetNoReportableTest() throws EEAException {
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(datasetService.isDatasetReportable(Mockito.any())).thenReturn(Boolean.FALSE);
    try {
      datasetControllerImpl.etlImportDataset(1L, new ETLDatasetVO(), 1L, 1L, false);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals(String.format(EEAErrorMessage.DATASET_NOT_REPORTABLE, 1L), e.getReason());
      throw e;
    }
  }

  /**
   * Test get attachment.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetAttachment() throws Exception {

    AttachmentValue attachment = new AttachmentValue();
    attachment.setFileName("test.txt");
    attachment.setContent(fileMock.getBytes());
    when(datasetService.getAttachment(Mockito.any(), Mockito.any())).thenReturn(attachment);
    datasetControllerImpl.getAttachment(1L, "600B66C6483EA7C8B55891DA171A3E7F", 1L, 1L);
    Mockito.verify(datasetService, times(1)).getAttachment(Mockito.any(), Mockito.any());
  }

  /**
   * Gets the attachment legacy test.
   *
   * @return the attachment legacy test
   * @throws Exception the exception
   */
  @Test
  public void getAttachmentLegacyTest() throws Exception {

    AttachmentValue attachment = new AttachmentValue();
    attachment.setFileName("test.txt");
    attachment.setContent(fileMock.getBytes());
    when(datasetService.getAttachment(Mockito.any(), Mockito.any())).thenReturn(attachment);
    datasetControllerImpl.getAttachmentLegacy(1L, "600B66C6483EA7C8B55891DA171A3E7F", 1L, 1L);
    Mockito.verify(datasetService, times(1)).getAttachment(Mockito.any(), Mockito.any());
  }

  /**
   * Test get attachment exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetAttachmentException() throws Exception {

    doThrow(new EEAException()).when(datasetService).getAttachment(Mockito.any(), Mockito.any());
    try {
      datasetControllerImpl.getAttachment(1L, "600B66C6483EA7C8B55891DA171A3E7F", 1L, 1L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  /**
   * Test update attachment.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUpdateAttachment() throws Exception {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setName("test");
    fieldSchemaVO.setId("id");
    FieldVO fieldVO = new FieldVO();
    fieldVO.setIdFieldSchema("600B66C6483EA7C8B55891DA171A3E7F");
    MockMultipartFile file =
        new MockMultipartFile("file.csv", "file.csv", "csv", "content".getBytes());

    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.any())).thenReturn("id");
    Mockito.when(datasetService.getFieldById(Mockito.anyLong(), Mockito.any())).thenReturn(fieldVO);
    Mockito.when(datasetSchemaService.getFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchemaVO);
    datasetControllerImpl.updateAttachment(1L, 0L, 0L, "600B66C6483EA7C8B55891DA171A3E7F", file);
    Mockito.verify(datasetService, times(1)).updateAttachment(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void updateAttachmentLegacyTest() throws Exception {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setName("test");
    fieldSchemaVO.setId("id");
    FieldVO fieldVO = new FieldVO();
    fieldVO.setIdFieldSchema("600B66C6483EA7C8B55891DA171A3E7F");
    MockMultipartFile file =
        new MockMultipartFile("file.csv", "file.csv", "csv", "content".getBytes());
    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.any())).thenReturn("id");
    Mockito.when(datasetService.getFieldById(Mockito.anyLong(), Mockito.any())).thenReturn(fieldVO);
    Mockito.when(datasetSchemaService.getFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchemaVO);
    datasetControllerImpl.updateAttachmentLegacy(1L, 0L, 0L, "600B66C6483EA7C8B55891DA171A3E7F",
        file);
    Mockito.verify(datasetService, times(1)).updateAttachment(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Test update attachment with limits.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUpdateAttachmentWithLimits() throws Exception {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setName("test");
    fieldSchemaVO.setId("id");
    fieldSchemaVO.setMaxSize(100000.1f);
    MockMultipartFile file =
        new MockMultipartFile("file.csv", "file.csv", "csv", "content".getBytes());
    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.any())).thenReturn("id");
    FieldVO fieldVO = new FieldVO();
    fieldVO.setIdFieldSchema("600B66C6483EA7C8B55891DA171A3E7F");
    Mockito.when(datasetService.getFieldById(Mockito.anyLong(), Mockito.any())).thenReturn(fieldVO);
    Mockito.when(datasetSchemaService.getFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchemaVO);
    // Mockito.when(datasetService.getMimetype(Mockito.any())).thenReturn("csv");
    datasetControllerImpl.updateAttachment(1L, 0L, 0L, "600B66C6483EA7C8B55891DA171A3E7F", file);
    Mockito.verify(datasetService, times(1)).updateAttachment(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Test update attachment exception dataset not found.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateAttachmentExceptionDatasetNotFound() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file.csv", "content".getBytes());
    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.any())).thenReturn(null);
    try {
      datasetControllerImpl.updateAttachment(1L, 0L, 0L, "600B66C6483EA7C8B55891DA171A3E7F", file);
    } catch (ResponseStatusException e) {
      assertEquals(EEAErrorMessage.UPDATING_ATTACHMENT_IN_A_DATAFLOW, e.getReason());
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Test update attachment exception field not found.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateAttachmentExceptionFieldNotFound() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file.csv", "content".getBytes());
    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.any())).thenReturn("id");
    FieldVO fieldVO = new FieldVO();
    fieldVO.setIdFieldSchema("600B66C6483EA7C8B55891DA171A3E7F");
    Mockito.when(datasetService.getFieldById(Mockito.anyLong(), Mockito.any())).thenReturn(fieldVO);
    Mockito.when(datasetSchemaService.getFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(null);
    try {
      datasetControllerImpl.updateAttachment(1L, 0L, 0L, "600B66C6483EA7C8B55891DA171A3E7F", file);
    } catch (ResponseStatusException e) {
      assertEquals(EEAErrorMessage.UPDATING_ATTACHMENT_IN_A_DATAFLOW, e.getReason());
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Test update attachment exception invalid attachment.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateAttachmentExceptionInvalidAttachment() throws Exception {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setName("test");
    fieldSchemaVO.setId("id");
    fieldSchemaVO.setMaxSize(100000.1f);
    fieldSchemaVO.setValidExtensions(new String[1]);
    MockMultipartFile file = new MockMultipartFile("file.csv", "content".getBytes());
    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.any())).thenReturn("id");
    FieldVO fieldVO = new FieldVO();
    fieldVO.setIdFieldSchema("600B66C6483EA7C8B55891DA171A3E7F");
    Mockito.when(datasetService.getFieldById(Mockito.anyLong(), Mockito.any())).thenReturn(fieldVO);
    Mockito.when(datasetSchemaService.getFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchemaVO);
    try {
      datasetControllerImpl.updateAttachment(1L, 0L, 0L, "600B66C6483EA7C8B55891DA171A3E7F", file);
    } catch (ResponseStatusException e) {
      assertEquals(EEAErrorMessage.FILE_FORMAT, e.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  /**
   * Test update attachment exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateAttachmentException() throws Exception {

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setName("test");
    fieldSchemaVO.setId("id");
    MockMultipartFile file =
        new MockMultipartFile("file.csv", "file.csv", "csv", "content".getBytes());
    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.any())).thenReturn("id");
    FieldVO fieldVO = new FieldVO();
    fieldVO.setIdFieldSchema("600B66C6483EA7C8B55891DA171A3E7F");
    Mockito.when(datasetService.getFieldById(Mockito.anyLong(), Mockito.any())).thenReturn(fieldVO);
    Mockito.when(datasetSchemaService.getFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchemaVO);
    Mockito.doThrow(new EEAException()).when(datasetService).updateAttachment(Mockito.anyLong(),
        Mockito.any(), Mockito.any(), Mockito.any());
    try {
      datasetControllerImpl.updateAttachment(1L, 0L, 0L, "600B66C6483EA7C8B55891DA171A3E7F", file);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Test update attachment locked or read only exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateAttachmentLockedOrReadOnlyException() throws Exception {
    Mockito.when(datasetService.findFieldSchemaIdById(1L, "600B66C6483EA7C8B55891DA171A3E7F"))
        .thenReturn("600B66C6483EA7C8B55891DA171A3E7F");
    Mockito.when(datasetService.checkIfDatasetLockedOrReadOnly(1L,
        "600B66C6483EA7C8B55891DA171A3E7F", EntityTypeEnum.FIELD)).thenReturn(true);
    MockMultipartFile file =
        new MockMultipartFile("file.csv", "file.csv", "csv", "content".getBytes());
    try {
      datasetControllerImpl.updateAttachment(1L, 1L, 1L, "600B66C6483EA7C8B55891DA171A3E7F", file);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.TABLE_READ_ONLY, e.getReason());
      throw e;
    }
  }

  /**
   * Test update attachment exception field schema id null.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateAttachmentExceptionFieldSchemaIdNull() throws Exception {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setName("test");
    fieldSchemaVO.setId(null);
    fieldSchemaVO.setMaxSize(100000.1f);
    fieldSchemaVO.setValidExtensions(new String[1]);
    MockMultipartFile file = new MockMultipartFile("file.csv", "content".getBytes());
    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.any())).thenReturn("id");
    FieldVO fieldVO = new FieldVO();
    fieldVO.setIdFieldSchema("600B66C6483EA7C8B55891DA171A3E7F");
    Mockito.when(datasetService.getFieldById(Mockito.anyLong(), Mockito.any())).thenReturn(fieldVO);
    Mockito.when(datasetSchemaService.getFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchemaVO);
    try {
      datasetControllerImpl.updateAttachment(1L, 0L, 0L, "600B66C6483EA7C8B55891DA171A3E7F", file);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      assertEquals(EEAErrorMessage.UPDATING_ATTACHMENT_IN_A_DATAFLOW, e.getReason());
      throw e;
    }
  }

  /**
   * Test update attachment exception small file size.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateAttachmentExceptionSmallFileSize() throws Exception {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setName("test");
    fieldSchemaVO.setId("schemaId");
    fieldSchemaVO.setMaxSize(0.000000001f);
    fieldSchemaVO.setValidExtensions(new String[1]);
    MockMultipartFile file = new MockMultipartFile("file.csv", "content".getBytes());
    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.any())).thenReturn("id");
    FieldVO fieldVO = new FieldVO();
    fieldVO.setIdFieldSchema("600B66C6483EA7C8B55891DA171A3E7F");
    Mockito.when(datasetService.getFieldById(Mockito.anyLong(), Mockito.any())).thenReturn(fieldVO);
    Mockito.when(datasetSchemaService.getFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchemaVO);
    try {
      datasetControllerImpl.updateAttachment(1L, 0L, 0L, "600B66C6483EA7C8B55891DA171A3E7F", file);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.FILE_FORMAT, e.getReason());
      throw e;
    }
  }

  /**
   * Test update attachment exception file size equals 0.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateAttachmentExceptionFileSizeEquals0() throws Exception {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setName("test");
    fieldSchemaVO.setId("schemaId");
    fieldSchemaVO.setMaxSize(0f);
    fieldSchemaVO.setValidExtensions(new String[1]);
    MockMultipartFile file = new MockMultipartFile("file.csv", "content".getBytes());
    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.any())).thenReturn("id");
    FieldVO fieldVO = new FieldVO();
    fieldVO.setIdFieldSchema("600B66C6483EA7C8B55891DA171A3E7F");
    Mockito.when(datasetService.getFieldById(Mockito.anyLong(), Mockito.any())).thenReturn(fieldVO);
    Mockito.when(datasetSchemaService.getFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchemaVO);
    try {
      datasetControllerImpl.updateAttachment(1L, 0L, 0L, "600B66C6483EA7C8B55891DA171A3E7F", file);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.FILE_FORMAT, e.getReason());
      throw e;
    }
  }

  /**
   * Test update attachment exception empty extensions.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testUpdateAttachmentExceptionEmptyExtensions() throws Exception {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setName("test");
    fieldSchemaVO.setId("schemaId");
    fieldSchemaVO.setMaxSize(0f);
    fieldSchemaVO.setValidExtensions(new String[0]);
    MockMultipartFile file = new MockMultipartFile("file.csv", "content".getBytes());
    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.any())).thenReturn("id");
    FieldVO fieldVO = new FieldVO();
    fieldVO.setIdFieldSchema("600B66C6483EA7C8B55891DA171A3E7F");
    Mockito.when(datasetService.getFieldById(Mockito.anyLong(), Mockito.any())).thenReturn(fieldVO);
    Mockito.when(datasetSchemaService.getFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchemaVO);
    try {
      datasetControllerImpl.updateAttachment(1L, 0L, 0L, "600B66C6483EA7C8B55891DA171A3E7F", file);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.FILE_FORMAT, e.getReason());
      throw e;
    }
  }

  /**
   * Test delete attachment.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteAttachment() throws Exception {

    datasetControllerImpl.deleteAttachment(1L, 0L, 0L, "600B66C6483EA7C8B55891DA171A3E7F");
    Mockito.verify(datasetService, times(1)).deleteAttachment(Mockito.any(), Mockito.any());
  }

  @Test
  public void deleteAttachmentLegacyTest() throws Exception {

    datasetControllerImpl.deleteAttachmentLegacy(1L, 0L, 0L, "600B66C6483EA7C8B55891DA171A3E7F");
    Mockito.verify(datasetService, times(1)).deleteAttachment(Mockito.any(), Mockito.any());
  }

  /**
   * Test delete attachment exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteAttachmentException() throws Exception {
    Mockito.doThrow(new EEAException()).when(datasetService).deleteAttachment(Mockito.anyLong(),
        Mockito.any());
    try {
      datasetControllerImpl.deleteAttachment(1L, 0L, 0L, "600B66C6483EA7C8B55891DA171A3E7F");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
      throw e;
    }
  }

  /**
   * Test delete attachment locked or read only exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteAttachmentLockedOrReadOnlyException() throws Exception {
    Mockito.when(datasetService.findFieldSchemaIdById(1L, "600B66C6483EA7C8B55891DA171A3E7F"))
        .thenReturn("600B66C6483EA7C8B55891DA171A3E7F");
    Mockito.when(datasetService.checkIfDatasetLockedOrReadOnly(1L,
        "600B66C6483EA7C8B55891DA171A3E7F", EntityTypeEnum.FIELD)).thenReturn(true);
    try {
      datasetControllerImpl.deleteAttachment(1L, 1L, 1L, "600B66C6483EA7C8B55891DA171A3E7F");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.TABLE_READ_ONLY, e.getReason());
      throw e;
    }
  }

  /**
   * Export file test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void exportFileTest() throws EEAException, IOException {
    Mockito.when(datasetSchemaService.getTableSchemaName(Mockito.any(), Mockito.anyString()))
        .thenReturn("tableName");
    Mockito.doNothing().when(fileTreatmentHelper).exportFile(Mockito.anyLong(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
    datasetControllerImpl.exportFile(1L, "5cf0e9b3b793310e9ceca190", FileTypeEnum.CSV.getValue(),
        null);
    Mockito.verify(fileTreatmentHelper, times(1)).exportFile(1L, FileTypeEnum.CSV.getValue(),
        "5cf0e9b3b793310e9ceca190", "tableName", null);
  }

  /**
   * Export file exception invalid schema test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = ResponseStatusException.class)
  public void exportFileExceptionInvalidSchemaTest() throws EEAException, IOException {
    Mockito.when(datasetSchemaService.getTableSchemaName(Mockito.any(), Mockito.anyString()))
        .thenReturn(null);
    try {
      datasetControllerImpl.exportFile(1L, "5cf0e9b3b793310e9ceca190", FileTypeEnum.CSV.getValue(),
          null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.IDTABLESCHEMA_INCORRECT, e.getReason());
      throw e;
    }
  }

  /**
   * Export file exception exporting test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = ResponseStatusException.class)
  public void exportFileExceptionExportingTest() throws EEAException, IOException {
    Mockito.when(datasetSchemaService.getTableSchemaName(Mockito.any(), Mockito.anyString()))
        .thenReturn("tableName");
    Mockito.doThrow(EEAException.class).when(fileTreatmentHelper).exportFile(Mockito.anyLong(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    try {
      datasetControllerImpl.exportFile(1L, "5cf0e9b3b793310e9ceca190", FileTypeEnum.CSV.getValue(),
          null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Export file table name null test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void exportFileTableNameNullTest() throws EEAException, IOException {
    DataSetMetabaseVO ds = new DataSetMetabaseVO();
    ds.setDataSetName("tableName");
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.any())).thenReturn(ds);
    Mockito.doNothing().when(fileTreatmentHelper).exportFile(Mockito.anyLong(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
    datasetControllerImpl.exportFile(1L, null, FileTypeEnum.CSV.getValue(), null);
    Mockito.verify(fileTreatmentHelper, times(1)).exportFile(1L, FileTypeEnum.CSV.getValue(), null,
        "tableName", null);
  }

  /**
   * Export file through integration test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void exportFileThroughIntegrationTest() throws EEAException {
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataSetName("datasetName");
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);

    Mockito.doNothing().when(datasetService).exportFileThroughIntegration(Mockito.anyLong(),
        Mockito.any());
    datasetControllerImpl.exportFileThroughIntegration(1L, 1L);
    Mockito.verify(datasetService, times(1)).exportFileThroughIntegration(Mockito.anyLong(),
        Mockito.any());
  }

  /**
   * Export file through integration exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void exportFileThroughIntegrationExceptionTest() throws EEAException {
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataSetName("datasetName");
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);

    Mockito.doThrow(EEAException.class).when(datasetService)
        .exportFileThroughIntegration(Mockito.anyLong(), Mockito.any());
    try {
      datasetControllerImpl.exportFileThroughIntegration(1L, 1L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Delete data to replace test.
   */
  @Test
  public void deleteDataToReplaceTest() {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    datasetControllerImpl.deleteDataBeforeReplacing(1L, 1L,
        IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM);
    Mockito.verify(deleteHelper, times(1)).executeDeleteImportDataAsyncBeforeReplacing(
        Mockito.anyLong(), Mockito.any(), Mockito.any());
  }

  /**
   * Insert records test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRecordsTest() throws EEAException {
    ArrayList<RecordVO> records = new ArrayList<RecordVO>();
    RecordVO record = new RecordVO();
    record.setId(recordId);
    records.add(record);
    datasetControllerImpl.insertRecords(1L, "", records);
    Mockito.verify(updateRecordHelper, times(1)).executeCreateProcess(Mockito.anyLong(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Inser records exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void inserRecordsExceptionTest() throws EEAException {
    Mockito.doThrow(EEAException.class).when(updateRecordHelper)
        .executeCreateProcess(Mockito.anyLong(), Mockito.any(), Mockito.any());
    try {
      ArrayList<RecordVO> records = new ArrayList<RecordVO>();
      RecordVO record = new RecordVO();
      record.setId(recordId);
      records.add(record);
      datasetControllerImpl.insertRecords(1L, "", records);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  /**
   * Insert records exception locked or read only test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void insertRecordsExceptionLockedOrReadOnlyTest() throws EEAException {
    Mockito.when(
        datasetService.checkIfDatasetLockedOrReadOnly(1L, "recordSchemaId", EntityTypeEnum.RECORD))
        .thenReturn(true);
    try {
      ArrayList<RecordVO> records = new ArrayList<RecordVO>();
      RecordVO record = new RecordVO();
      record.setId(recordId);
      record.setIdRecordSchema("recordSchemaId");
      records.add(record);
      datasetControllerImpl.insertRecords(1L, "", records);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.TABLE_READ_ONLY, e.getReason());
      throw e;
    }
  }

  /**
   * Insert records exception fixed number test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void insertRecordsExceptionFixedNumberTest() throws EEAException {
    Mockito.when(
        datasetService.getTableFixedNumberOfRecords(1L, "recordSchemaId", EntityTypeEnum.RECORD))
        .thenReturn(true);
    try {
      ArrayList<RecordVO> records = new ArrayList<RecordVO>();
      RecordVO record = new RecordVO();
      record.setId(recordId);
      record.setIdRecordSchema("recordSchemaId");
      records.add(record);
      datasetControllerImpl.insertRecords(1L, "", records);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  /**
   * Insert records dataset type design test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRecordsDatasetTypeDesignTest() throws EEAException {
    Mockito.when(datasetMetabaseService.getDatasetType(1L)).thenReturn(DatasetTypeEnum.DESIGN);
    ArrayList<RecordVO> records = new ArrayList<RecordVO>();
    RecordVO record = new RecordVO();
    record.setId(recordId);
    record.setIdRecordSchema("recordSchemaId");
    records.add(record);
    datasetControllerImpl.insertRecords(1L, "", records);
    Mockito.verify(updateRecordHelper, times(1)).executeCreateProcess(Mockito.anyLong(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Insert records dataset type reference test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRecordsDatasetTypeReferenceTest() throws EEAException {
    Mockito.when(datasetMetabaseService.getDatasetType(1L)).thenReturn(DatasetTypeEnum.REFERENCE);
    ArrayList<RecordVO> records = new ArrayList<RecordVO>();
    RecordVO record = new RecordVO();
    record.setId(recordId);
    record.setIdRecordSchema("recordSchemaId");
    records.add(record);
    datasetControllerImpl.insertRecords(1L, "", records);
    Mockito.verify(updateRecordHelper, times(1)).executeCreateProcess(Mockito.anyLong(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Insert records multi table.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRecordsMultiTable() throws EEAException {
    datasetControllerImpl.insertRecordsMultiTable(1L, new ArrayList<TableVO>());
    Mockito.verify(updateRecordHelper, times(1)).executeMultiCreateProcess(Mockito.anyLong(),
        Mockito.any());
  }

  /**
   * Insert records multi table exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void insertRecordsMultiTableExceptionTest() throws EEAException {
    Mockito.doThrow(EEAException.class).when(updateRecordHelper)
        .executeMultiCreateProcess(Mockito.anyLong(), Mockito.any());
    try {
      datasetControllerImpl.insertRecordsMultiTable(1L, new ArrayList<TableVO>());
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  /**
   * Import file data test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void importFileDataTest() throws EEAException {


    MultipartFile multipartFile =
            new MockMultipartFile("multipartFile", "multipartFile".getBytes());
    Mockito.when(jobControllerZuul.checkEligibilityOfJob(anyString(), anyBoolean(), anyLong(), anyLong(), anyList())).thenReturn(JobStatusEnum.IN_PROGRESS);
    Mockito.doNothing().when(fileTreatmentHelper).importFileData(Mockito.anyLong(), Mockito.any(),Mockito.any(),
        Mockito.nullable(MultipartFile.class), Mockito.anyBoolean(), Mockito.any(), Mockito.any(), Mockito.any());
    datasetControllerImpl.importFileData(1L, 1L, 1L, "5cf0e9b3b793310e9ceca190", multipartFile, true, 1L,
        null, null);
    Mockito.verify(fileTreatmentHelper, times(1)).importFileData(Mockito.anyLong(), Mockito.any(),Mockito.any(),
        Mockito.nullable(MultipartFile.class), Mockito.anyBoolean(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Import file data legacy test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void importFileDataLegacyTest() throws EEAException {

    Mockito.doNothing().when(fileTreatmentHelper).importFileData(Mockito.anyLong(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.anyBoolean(), Mockito.any(), Mockito.any(), Mockito.nullable(Long.class));
    Mockito.when(jobControllerZuul.checkEligibilityOfJob(anyString(), anyBoolean(), anyLong(), anyLong(), anyList())).thenReturn(JobStatusEnum.IN_PROGRESS);
    datasetControllerImpl.importFileDataLegacy(1L, 1L, 1L, "5cf0e9b3b793310e9ceca190", fileMock, true,
        1L, null, null);
    Mockito.verify(fileTreatmentHelper, times(1)).importFileData(Mockito.anyLong(), Mockito.any(),Mockito.any(),
            Mockito.any(), Mockito.anyBoolean(), Mockito.any(), Mockito.any(), Mockito.nullable(Long.class));
  }

  /**
   * Import file data exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void importFileDataExceptionTest() throws EEAException {

    Mockito.when(jobControllerZuul.checkEligibilityOfJob(anyString(), anyBoolean(), anyLong(), anyLong(), anyList())).thenReturn(JobStatusEnum.IN_PROGRESS);
    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.doThrow(EEAException.class).when(fileTreatmentHelper).importFileData(Mockito.anyLong(),
        Mockito.any(),Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.when(file.getOriginalFilename()).thenReturn("fileName.csv");
    try {
      datasetControllerImpl.importFileData(1L, 1L, 1L, "5cf0e9b3b793310e9ceca190", file, true, 1L,
          null, null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(e.getStatus(), HttpStatus.BAD_REQUEST);
      throw e;
    }

  }

  /**
   * Export public file throws.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void exportPublicFileThrows() throws EEAException, IOException {
    Mockito.doThrow(EEAException.class).when(datasetService).exportPublicFile(Mockito.anyLong(),
        Mockito.any(), Mockito.anyString());
    ResponseEntity<?> value = datasetControllerImpl.exportPublicFile(Mockito.anyLong(),
        Mockito.any(), Mockito.anyString());
    assertEquals(null, value.getBody());
    assertEquals(HttpStatus.NOT_FOUND, value.getStatusCode());

  }

  @Test
  public void testExportPublicFile() throws EEAException, IOException {
    String toWrite = "content";
    File tmpFile = File.createTempFile("fileName", ".tmp");
    FileWriter writer = new FileWriter(tmpFile);
    writer.write(toWrite);
    writer.close();
    Mockito.when(datasetService.exportPublicFile(1L, 1L, "fileName")).thenReturn(tmpFile);
    datasetControllerImpl.exportPublicFile(1L, 1L, "fileName");
    Mockito.verify(datasetService, times(1)).exportPublicFile(1L, 1L, "fileName");
  }


  @Test
  public void deleteImportDataTest() {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    datasetControllerImpl.deleteDatasetData(1L, null, null, false);
    Mockito.verify(deleteHelper, times(1)).executeDeleteDatasetProcess(Mockito.anyLong(),
        Mockito.anyBoolean(), Mockito.anyBoolean(), null);
  }

  /**
   * Delete import data legacy test.
   */
  @Test
  public void deleteImportDataLegacyTest() {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    datasetControllerImpl.deleteImportDataLegacy(1L, null, null, false);
    Mockito.verify(deleteHelper, times(1)).executeDeleteDatasetProcess(Mockito.anyLong(),
        Mockito.anyBoolean(), Mockito.anyBoolean(), null);
  }


  @Test
  public void deleteImportDataRestApiTest() {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    datasetControllerImpl.deleteDatasetData(1L, 1L, 1L, false);
    Mockito.verify(deleteHelper, times(1)).executeDeleteDatasetProcess(Mockito.anyLong(),
        Mockito.anyBoolean(), Mockito.anyBoolean(), null);
  }

  /**
   * Private delete dataset data test.
   */
  @Test
  public void privateDeleteDatasetDataTest() {
    datasetControllerImpl.privateDeleteDatasetData(1L, null, false);
    Mockito.verify(deleteHelper, times(1)).executeDeleteDatasetProcess(Mockito.anyLong(),
        Mockito.anyBoolean(), Mockito.anyBoolean(), null);
  }

  /**
   * Private delete dataset data dataset id null test.
   */
  @Test
  public void privateDeleteDatasetDataDatasetIdNullTest() {
    try {
      datasetControllerImpl.privateDeleteDatasetData(null, 1L, false);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
    }
  }

  /**
   * Private delete dataset data dataset belongs dataflow test.
   */
  @Test
  public void privateDeleteDatasetDataDatasetBelongsDataflowTest() {
    Mockito.when(datasetService.getDataFlowIdById(1L)).thenReturn(1L);
    try {
      datasetControllerImpl.privateDeleteDatasetData(1L, 1L, false);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteImportDataRestApiForbiddenTest() {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(2L);
    try {
      datasetControllerImpl.deleteDatasetData(1L, 1L, null, false);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  @Test
  public void deleteImportTableTest() {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    datasetControllerImpl.deleteTableData(1L, "5cf0e9b3b793310e9ceca190", null, null);
    Mockito.verify(deleteHelper, times(1)).executeDeleteTableProcess(Mockito.anyLong(),
        Mockito.any(), null);
  }

  /**
   * Delete import table legacy test.
   */
  @Test
  public void deleteImportTableLegacyTest() {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    datasetControllerImpl.deleteImportTableLegacy(1L, "5cf0e9b3b793310e9ceca190", null, null);
    Mockito.verify(deleteHelper, times(1)).executeDeleteTableProcess(Mockito.anyLong(),
        Mockito.any(), null);
  }

  @Test
  public void deleteImportTableRestApiTest() {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    datasetControllerImpl.deleteTableData(1L, "5cf0e9b3b793310e9ceca190", 1L, 1L);
    Mockito.verify(deleteHelper, times(1)).executeDeleteTableProcess(Mockito.anyLong(),
        Mockito.any(), null);
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteImportTableRestApiForbiddenTest() {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(2L);
    try {
      datasetControllerImpl.deleteTableData(1L, "5cf0e9b3b793310e9ceca190", 1L, null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  @Test
  public void exportDatasetTest() throws IOException, EEAException {
    datasetControllerImpl.exportDatasetFile(1L, FileTypeEnum.XLSX.getValue());
    Mockito.verify(fileTreatmentHelper, times(1)).exportDatasetFile(Mockito.anyLong(),
        Mockito.any());
  }

  /**
   * Download file exception test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Test
  public void downloadFileExceptionTest() throws IOException, EEAException {
    Mockito.when(datasetService.downloadExportedFile(Mockito.any(), Mockito.any()))
        .thenReturn(new File(""));
    datasetControllerImpl.downloadFile(0L, recordId, httpServletResponse);
    Mockito.verify(httpServletResponse, times(1)).getOutputStream();
  }

  /**
   * Download file test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Test
  public void downloadFileTest() throws IOException, EEAException {

    File file = folder.newFile("filename.txt");

    ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);

    Mockito.when(datasetService.downloadExportedFile(Mockito.any(), Mockito.any()))
        .thenReturn(file);
    Mockito.when(httpServletResponse.getOutputStream()).thenReturn(outputStream);
    Mockito.doNothing().when(outputStream).close();

    datasetControllerImpl.downloadFile(0L, "", httpServletResponse);
    Mockito.verify(outputStream, times(1)).close();
  }

  /**
   * Export reference dataset file exception test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void exportReferenceDatasetFileExceptionTest() throws EEAException, IOException {
    Mockito.when(datasetService.exportPublicFile(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new File(""));
    ResponseEntity<InputStreamResource> value =
        datasetControllerImpl.exportReferenceDatasetFile(1L, "file.zip");
    assertEquals(null, value.getBody());
    assertEquals(HttpStatus.NOT_FOUND, value.getStatusCode());

  }

  /**
   * Export reference dataset file test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void exportReferenceDatasetFileTest() throws EEAException, IOException {
    String toWrite = "content";
    File tmpFile = File.createTempFile("fileName", ".tmp");
    FileWriter writer = new FileWriter(tmpFile);
    writer.write(toWrite);
    writer.close();
    Mockito.when(datasetService.exportPublicFile(1L, null, "fileName")).thenReturn(tmpFile);
    datasetControllerImpl.exportReferenceDatasetFile(1L, "fileName");
    Mockito.verify(datasetService, times(1)).exportPublicFile(1L, null, "fileName");
  }

  @Test
  public void checkAnySchemaAvailableInPublicTest() {
    datasetControllerImpl.checkAnySchemaAvailableInPublic(1L);
    Mockito.verify(datasetService, times(1)).checkAnySchemaAvailableInPublic(1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void getFieldValuesReferencedExceptionTest() throws EEAException {
    try {
      doThrow(EEAException.class).when(datasetService).getFieldValuesReferenced(Mockito.any(),
          Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
      datasetControllerImpl.getFieldValuesReferenced(1L, "", "", "", "", null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  @Test
  public void updateCheckViewTest() {
    datasetControllerImpl.updateCheckView(1L, true);
    Mockito.verify(datasetService, times(1)).updateCheckView(Mockito.anyLong(), Mockito.any());
  }

  @Test
  public void getCheckViewTest() {
    datasetControllerImpl.getCheckView(1L);
    Mockito.verify(datasetService, times(1)).getCheckView(Mockito.anyLong());
  }

  @Test
  public void checkImportProcessNoImportFileInProgressTest() {

    ResponseEntity<CheckLockVO> checkLockVOResponseEntity = checkImportProcessInit(null, null);

    assertEquals(Boolean.FALSE, checkLockVOResponseEntity.getBody().isImportInProgress());
    assertEquals(LiteralConstants.NO_IMPORT_IN_PROGRESS, checkLockVOResponseEntity.getBody().getMessage());

    Mockito.verify(lockService, times(2)).findByCriteria(Mockito.anyMap());
  }

  @Test
  public void checkImportProcessImportInProgressTest() {

    ResponseEntity<CheckLockVO> checkLockVOResponseEntity = checkImportProcessInit(null, new LockVO());

    assertEquals(Boolean.TRUE, checkLockVOResponseEntity.getBody().isImportInProgress());
    assertEquals(LiteralConstants.IMPORT_LOCKED, checkLockVOResponseEntity.getBody().getMessage());

    Mockito.verify(lockService, times(2)).findByCriteria(Mockito.anyMap());
  }

  @Test
  public void checkImportProcessNoImportBigFileInProgressTest() {

    ResponseEntity<CheckLockVO> checkLockVOResponseEntity = checkImportProcessInit(new LockVO(), null);

    assertEquals(Boolean.TRUE, checkLockVOResponseEntity.getBody().isImportInProgress());
    assertEquals(LiteralConstants.IMPORT_LOCKED, checkLockVOResponseEntity.getBody().getMessage());

    Mockito.verify(lockService, times(2)).findByCriteria(Mockito.anyMap());
  }


  @Test
  public void checkImportProcessBothImportsInProgressTest() {

    ResponseEntity<CheckLockVO> checkLockVOResponseEntity = checkImportProcessInit(new LockVO(), new LockVO());

    assertEquals(Boolean.TRUE, checkLockVOResponseEntity.getBody().isImportInProgress());
    assertEquals(LiteralConstants.IMPORT_LOCKED, checkLockVOResponseEntity.getBody().getMessage());

    Mockito.verify(lockService, times(2)).findByCriteria(Mockito.anyMap());
  }

  private ResponseEntity<CheckLockVO> checkImportProcessInit(LockVO fileData, LockVO bigFileData) {
    Map<String, Object> importData = new HashMap<>();
    importData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_FILE_DATA.getValue());
    importData.put(LiteralConstants.DATASETID, 1L);

    Mockito.when(lockService.findByCriteria(Mockito.anyMap())).thenReturn(fileData);

    importData.clear();
    importData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_BIG_FILE_DATA.getValue());
    importData.put(LiteralConstants.DATASETID, 1L);

    Mockito.when(lockService.findByCriteria(importData)).thenReturn(bigFileData);

    return datasetControllerImpl.checkImportProcess(1L);
  }

  @Test
  public void checkImportProcessExceptionTest() {

    Mockito.when(lockService.findByCriteria(Mockito.anyMap())).thenThrow(new RuntimeException());

    ResponseEntity<CheckLockVO> checkLockVOResponseEntity = datasetControllerImpl.checkImportProcess(1L);

    assertEquals(HttpStatus.NOT_FOUND, checkLockVOResponseEntity.getStatusCode());
  }

  @Test
  public void checkLocksTest() {
    Map<String, Object> lockCriteria = new HashMap<>();
    List<LockVO> results = new ArrayList<>();
    LockVO lockVO = new LockVO();

    lockCriteria.put("key", 1L);
    lockVO.setLockCriteria(lockCriteria);
    results.add(lockVO);

    Mockito.when(lockService.findAll()).thenReturn(results);
    Mockito.when(lockService.findAllByCriteria(results, 1L)).thenReturn(results);

    datasetControllerImpl.checkLocks(1L, 1L, 1L);

    Mockito.verify(lockService, times(3)).findAllByCriteria(Mockito.anyList(), Mockito.anyLong());
  }
}
