package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.dataset.service.impl.DataschemaServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataflow.ContributorController.ContributorControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.OrderVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.WebformVO;
import org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO.UniqueConstraintVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataSetSchemaControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetSchemaControllerImplTest {

  /**
   * The data schema controller impl.
   */
  @InjectMocks
  private DatasetSchemaControllerImpl dataSchemaControllerImpl;

  /**
   * The expected ex.
   */
  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  /**
   * The dataschema service.
   */
  @Mock
  private DataschemaServiceImpl dataschemaService;

  /**
   * The dataset service.
   */
  @Mock
  private DatasetService datasetService;

  /**
   * The dataset metabase service.
   */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /**
   * The record store controller zuul.
   */
  @Mock
  private RecordStoreControllerZuul recordStoreControllerZuul;

  /**
   * The dataset snapshot service.
   */
  @Mock
  private DatasetSnapshotService datasetSnapshotService;

  /**
   * The contributor controller zuul.
   */
  @Mock
  private ContributorControllerZuul contributorControllerZuul;

  /**
   * The integration controller zuul.
   */
  @Mock
  private IntegrationControllerZuul integrationControllerZuul;

  /**
   * The rules controller zuul.
   */
  @Mock
  private RulesControllerZuul rulesControllerZuul;

  /**
   * The design dataset service.
   */
  @Mock
  private DesignDatasetService designDatasetService;

  /**
   * The dataflow controller zuul.
   */
  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The notification controller zuul. */
  @Mock
  private NotificationControllerZuul notificationControllerZuul;


  /**
   * The dataset schema VO.
   */
  private DataSetSchemaVO datasetSchemaVO;

  /**
   * The security context.
   */
  private SecurityContext securityContext;

  /**
   * The authentication.
   */
  private Authentication authentication;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    datasetSchemaVO = new DataSetSchemaVO();
    datasetSchemaVO.setDescription("description");
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test find data schema by id.
   */
  @Test
  public void testFindDataSchemaById() {
    when(dataschemaService.getDataSchemaById(Mockito.any())).thenReturn(new DataSetSchemaVO());
    assertNotNull("failed", dataSchemaControllerImpl.findDataSchemaById("id"));
  }

  /**
   * Gets the dataset schema id test.
   *
   * @return the dataset schema id test
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void getDatasetSchemaIdTest() throws EEAException {
    when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("result");
    String result = dataSchemaControllerImpl.getDatasetSchemaId(1L);
    Assert.assertNotNull(result);
  }

  /**
   * Gets the dataset schema id exception test.
   *
   * @return the dataset schema id exception test
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getDatasetSchemaIdExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(dataschemaService).getDatasetSchemaId(Mockito.any());
    try {
      dataSchemaControllerImpl.getDatasetSchemaId(1L);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Find data schema by dataset id test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findDataSchemaByDatasetIdTest() throws EEAException {
    when(dataschemaService.getDataSchemaByDatasetId(Mockito.eq(Boolean.TRUE), Mockito.any()))
        .thenReturn(new DataSetSchemaVO());
    Assert.assertNotNull(dataSchemaControllerImpl.findDataSchemaByDatasetId(1L));
  }

  /**
   * Find data schema by dataset id legacy test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findDataSchemaByDatasetIdLegacyTest() throws EEAException {
    when(dataschemaService.getDataSchemaByDatasetId(Mockito.eq(Boolean.TRUE), Mockito.any()))
        .thenReturn(new DataSetSchemaVO());
    Assert.assertNotNull(dataSchemaControllerImpl.findDataSchemaByDatasetIdLegacy(1L));
  }

  /**
   * Find data schema by dataset id exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void findDataSchemaByDatasetIdExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(dataschemaService)
        .getDataSchemaByDatasetId(Mockito.eq(Boolean.TRUE), Mockito.any());
    try {
      dataSchemaControllerImpl.findDataSchemaByDatasetId(1L);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Test find data schema with no rules by dataflow.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findDataSchemaWithNoRulesByDatasetIdTest() throws EEAException {
    when(dataschemaService.getDataSchemaByDatasetId(Mockito.eq(Boolean.FALSE), Mockito.any()))
        .thenReturn(new DataSetSchemaVO());
    Assert.assertNotNull(dataSchemaControllerImpl.findDataSchemaWithNoRulesByDatasetId(1L));
  }

  /**
   * Find data schema with no rules by dataset id exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void findDataSchemaWithNoRulesByDatasetIdExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(dataschemaService)
        .getDataSchemaByDatasetId(Mockito.eq(Boolean.FALSE), Mockito.any());
    try {
      dataSchemaControllerImpl.findDataSchemaWithNoRulesByDatasetId(1L);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Test schema models.
   */
  @Test
  public void testSchemaModels() {

    FieldSchema field = new FieldSchema();
    field.setHeaderName("test");
    field.setType(DataType.TEXT);

    FieldSchema field2 = new FieldSchema();
    field2.setHeaderName("test");
    field2.setType(DataType.TEXT);

    assertEquals("error, not equals", field, field2);

    RecordSchema record = new RecordSchema();
    record.setNameSchema("test");
    List<FieldSchema> listaFields = new ArrayList<>();
    listaFields.add(field);
    record.setFieldSchema(listaFields);

    RecordSchema record2 = new RecordSchema();
    record2.setNameSchema("test");
    record2.setFieldSchema(listaFields);

    assertEquals("error, not equals", record, record2);

    TableSchema table = new TableSchema();
    table.setNameTableSchema("test");
    table.setRecordSchema(record);

    TableSchema table2 = new TableSchema();
    table2.setNameTableSchema("test");
    table2.setRecordSchema(record2);

    assertEquals("error, not equals", table, table2);

    DataSetSchema schema = new DataSetSchema();
    schema.setIdDataFlow(1L);
    List<TableSchema> listaTables = new ArrayList<>();
    listaTables.add(table);
    schema.setTableSchemas(listaTables);

    DataSetSchema schema2 = new DataSetSchema();
    schema2.setIdDataFlow(1L);
    schema2.setTableSchemas(listaTables);

    assertEquals("error, not equals", schema, schema2);
  }

  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetSchemaForbiddenTest() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    try {
      dataSchemaControllerImpl.createEmptyDatasetSchema(1L, "datasetSchemaName");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  /**
   * Creates the empty data set schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createEmptyDataSetSchemaTest() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(dataschemaService.createEmptyDataSetSchema(Mockito.any()))
        .thenReturn(new ObjectId());
    Mockito
        .when(datasetMetabaseService.createEmptyDataset(Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(CompletableFuture.completedFuture(1L));
    Mockito.doNothing().when(integrationControllerZuul).createDefaultIntegration(Mockito.anyLong(),
        Mockito.any());
    dataSchemaControllerImpl.createEmptyDatasetSchema(1L, "datasetSchemaName");
    Mockito.verify(datasetMetabaseService, times(1)).createEmptyDataset(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetSchemaRepeatTest() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetMetabaseService.countDatasetNameByDataflowId(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    try {
      dataSchemaControllerImpl.createEmptyDatasetSchema(1L, "datasetSchemaName");
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      assertEquals(EEAErrorMessage.DATASET_NAME_DUPLICATED, ex.getReason());
      throw ex;
    }

  }

  /**
   * Creates the empty data set schema exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetSchemaException() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.doThrow(EEAException.class).when(datasetMetabaseService).createEmptyDataset(
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any());
    Mockito.when(dataschemaService.createEmptyDataSetSchema(Mockito.any()))
        .thenReturn(new ObjectId());
    try {
      dataSchemaControllerImpl.createEmptyDatasetSchema(1L, "datasetSchemaName");
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
      throw ex;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetSchemaInterruptedExceptionTest()
      throws EEAException, InterruptedException, ExecutionException {
    Future<Long> futureDatasetId = Mockito.mock(Future.class);
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito
        .when(datasetMetabaseService.createEmptyDataset(Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(futureDatasetId);

    Mockito.when(futureDatasetId.get()).thenThrow(InterruptedException.class);
    Mockito.when(dataschemaService.createEmptyDataSetSchema(Mockito.any()))
        .thenReturn(new ObjectId());
    try {
      dataSchemaControllerImpl.createEmptyDatasetSchema(1L, "datasetSchemaName");
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
      Thread.interrupted();
      throw ex;
    }
  }

  /**
   * Delete dataset schema exception test.
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDatasetSchemaExceptionTest() {
    try {
      dataSchemaControllerImpl.deleteDatasetSchema(null, false);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATASET_INCORRECT_ID, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Delete dataset schema exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDatasetSchemaException2Test() throws EEAException {
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    try {
      dataSchemaControllerImpl.deleteDatasetSchema(1L, false);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATASET_NOTFOUND, ex.getReason());
      assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Delete dataset schema success.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteDatasetSchemaSuccessTest() throws EEAException {
    DataSetSchemaVO dataSetSchemaVO = new DataSetSchemaVO();
    dataSetSchemaVO.setIdDataSetSchema("schemaId");
    when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn(new ObjectId().toString());
    doNothing().when(dataschemaService).deleteDatasetSchema(Mockito.any(), Mockito.any());
    doNothing().when(datasetMetabaseService).deleteDesignDataset(Mockito.any());
    doNothing().when(datasetSnapshotService).deleteAllSchemaSnapshots(Mockito.any());
    DataFlowVO df = new DataFlowVO();
    df.setId(1L);
    df.setStatus(TypeStatusEnum.DESIGN);
    when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(df);
    when(dataschemaService.isSchemaAllowedForDeletion(Mockito.any())).thenReturn(true);
    dataSchemaControllerImpl.deleteDatasetSchema(1L, false);

    Mockito.verify(recordStoreControllerZuul, times(1)).deleteDataset(Mockito.any());
  }

  /**
   * Delete dataset schema exception 4 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDatasetSchemaException4Test() throws EEAException {
    DataSetSchemaVO dataSetSchemaVO = new DataSetSchemaVO();
    dataSetSchemaVO.setIdDataSetSchema("schemaId");
    when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn(new ObjectId().toString());
    when(dataschemaService.isSchemaAllowedForDeletion(Mockito.any())).thenReturn(true);
    DataFlowVO df = new DataFlowVO();
    df.setId(1L);
    df.setStatus(TypeStatusEnum.DRAFT);
    when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(df);
    try {
      dataSchemaControllerImpl.deleteDatasetSchema(1L, false);
    } catch (ResponseStatusException e) {
      assertEquals("The dataflow is not in the correct status", HttpStatus.FORBIDDEN,
          e.getStatus());
      throw e;
    }
  }

  /**
   * Delete dataset schema exception 3 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDatasetSchemaException3Test() throws EEAException {
    DataSetSchemaVO dataSetSchemaVO = new DataSetSchemaVO();
    dataSetSchemaVO.setIdDataSetSchema("schemaId");
    when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn(new ObjectId().toString());
    DataFlowVO df = new DataFlowVO();
    df.setId(1L);
    df.setStatus(TypeStatusEnum.DESIGN);
    when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(df);
    when(dataschemaService.isSchemaAllowedForDeletion(Mockito.any())).thenReturn(true);
    doThrow(new EEAException()).when(datasetSnapshotService)
        .deleteAllSchemaSnapshots(Mockito.any());
    try {
      dataSchemaControllerImpl.deleteDatasetSchema(1L, false);
    } catch (ResponseStatusException ex) {
      assertEquals("Not the same status", HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Delete dataset schema exception not allowed test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDatasetSchemaExceptionNotAllowedTest() throws EEAException {

    when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn(new ObjectId().toString());
    when(dataschemaService.isSchemaAllowedForDeletion(Mockito.any())).thenReturn(false);
    try {
      dataSchemaControllerImpl.deleteDatasetSchema(1L, false);
    } catch (ResponseStatusException e) {
      assertEquals("Not the same status", HttpStatus.UNAUTHORIZED, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteDatasetSchemaForceDeleteNullTest() throws EEAException {
    when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn(new ObjectId().toString());
    when(dataschemaService.isSchemaAllowedForDeletion(Mockito.any())).thenReturn(false);
    try {
      dataSchemaControllerImpl.deleteDatasetSchema(1L, null);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.PK_REFERENCED, ex.getReason());
      assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
      throw ex;
    }
  }

  @Test
  public void deleteDatasetSchemaForceDeleteTrueTest() throws EEAException {
    DataSetSchemaVO dataSetSchemaVO = new DataSetSchemaVO();
    dataSetSchemaVO.setIdDataSetSchema("schemaId");
    when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn(new ObjectId().toString());
    doNothing().when(dataschemaService).deleteDatasetSchema(Mockito.any(), Mockito.any());
    doNothing().when(datasetMetabaseService).deleteDesignDataset(Mockito.any());
    doNothing().when(datasetSnapshotService).deleteAllSchemaSnapshots(Mockito.any());
    DataFlowVO df = new DataFlowVO();
    df.setId(1L);
    df.setStatus(TypeStatusEnum.DESIGN);
    when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(df);
    dataSchemaControllerImpl.deleteDatasetSchema(1L, true);

    Mockito.verify(recordStoreControllerZuul, times(1)).deleteDataset(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void updateTableSchemaForbiddenTest() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    TableSchemaVO tableSchemaVO = new TableSchemaVO();
    tableSchemaVO.setNameTableSchema("tableSchema");
    try {
      dataSchemaControllerImpl.updateTableSchema(1L, tableSchemaVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  /**
   * Update table schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateTableSchemaTest() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doNothing().when(dataschemaService).updateTableSchema(Mockito.any(), Mockito.any());
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    TableSchemaVO tableSchemaVO = new TableSchemaVO();
    tableSchemaVO.setNameTableSchema("tableName");
    dataSchemaControllerImpl.updateTableSchema(1L, tableSchemaVO);
    Mockito.verify(dataschemaService, times(1)).updateTableSchema(Mockito.any(), Mockito.any());
  }

  /**
   * Update table schema test exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateTableSchemaTestException() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    doThrow(EEAException.class).when(dataschemaService).updateTableSchema(Mockito.any(),
        Mockito.any());
    try {
      TableSchemaVO tableSchema = new TableSchemaVO();
      tableSchema.setIdTableSchema("");
      tableSchema.setNameTableSchema("tableName");
      dataSchemaControllerImpl.updateTableSchema(1L, tableSchema);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATASET_INCORRECT_ID, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void updateTableSchemaTestException2() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    EEAException exception =
        new EEAException(String.format(EEAErrorMessage.ERROR_UPDATING_TABLE_SCHEMA, "", 1L));
    doThrow(exception).when(dataschemaService).updateTableSchema(Mockito.any(), Mockito.any());
    try {
      TableSchemaVO tableSchema = new TableSchemaVO();
      tableSchema.setIdTableSchema("");
      tableSchema.setNameTableSchema("tableSchema");
      dataSchemaControllerImpl.updateTableSchema(1L, tableSchema);
    } catch (ResponseStatusException ex) {
      assertEquals(String.format(EEAErrorMessage.ERROR_UPDATING_TABLE_SCHEMA, "", 1L),
          ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void updateTableSchemaTestException3() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    EEAException exception =
        new EEAException(String.format(EEAErrorMessage.TABLE_NOT_FOUND, "", 1L));
    doThrow(exception).when(dataschemaService).updateTableSchema(Mockito.any(), Mockito.any());
    try {
      TableSchemaVO tableSchema = new TableSchemaVO();
      tableSchema.setIdTableSchema("");
      tableSchema.setNameTableSchema("tableSchema");
      dataSchemaControllerImpl.updateTableSchema(1L, tableSchema);
    } catch (ResponseStatusException ex) {
      assertEquals(String.format(EEAErrorMessage.TABLE_NOT_FOUND, "", 1L), ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  @Test
  public void updateTableSchemaNameTableSchemaNullTest() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doNothing().when(dataschemaService).updateTableSchema(Mockito.any(), Mockito.any());
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    TableSchemaVO tableSchemaVO = new TableSchemaVO();
    tableSchemaVO.setNameTableSchema(null);
    dataSchemaControllerImpl.updateTableSchema(1L, tableSchemaVO);
    Mockito.verify(dataschemaService, times(1)).updateTableSchema(Mockito.any(), Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void updateTableSchemaTableNotFoundNotNullTest() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    EEAException exception = Mockito.mock(EEAException.class);
    Mockito.when(exception.getMessage()).thenReturn(null).thenReturn("mesage").thenReturn("mesage");
    doThrow(exception).when(dataschemaService).updateTableSchema(Mockito.any(), Mockito.any());

    try {
      TableSchemaVO tableSchema = new TableSchemaVO();
      tableSchema.setIdTableSchema("");
      tableSchema.setNameTableSchema("tableSchema");
      dataSchemaControllerImpl.updateTableSchema(1L, tableSchema);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATASET_INCORRECT_ID, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteTableSchemaForbiddenTest() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    try {
      dataSchemaControllerImpl.deleteTableSchema(1L, "");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  /**
   * Delete table schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteTableSchemaTest1() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.doNothing().when(dataschemaService).deleteTableSchema(Mockito.any(), Mockito.any(),
        Mockito.any());
    Mockito.doNothing().when(datasetService).deleteTableValue(Mockito.any(), Mockito.any());
    dataSchemaControllerImpl.deleteTableSchema(1L, "");
    Mockito.verify(rulesControllerZuul, times(1)).deleteRuleByReferenceId(Mockito.any(),
        Mockito.any());
  }

  /**
   * Delete table schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteTableSchemaTest2() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.doThrow(EEAException.class).when(dataschemaService).deleteTableSchema(Mockito.any(),
        Mockito.any(), Mockito.any());
    try {
      dataSchemaControllerImpl.deleteTableSchema(1L, "");
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DELETING_TABLE_SCHEMA, ex.getReason());
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void orderTableSchemaForbiddenTest() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    try {
      dataSchemaControllerImpl.orderTableSchema(1L, new OrderVO());
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  /**
   * Order table schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void orderTableSchemaTest1() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.orderTableSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(true);
    dataSchemaControllerImpl.orderTableSchema(1L, new OrderVO());
    Mockito.verify(dataschemaService, times(1)).getDatasetSchemaId(Mockito.any());
  }

  /**
   * Order table schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void orderTableSchemaTest2() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.orderTableSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    expectedEx.expect(ResponseStatusException.class);
    expectedEx.expectMessage(EEAErrorMessage.SCHEMA_NOT_FOUND);
    dataSchemaControllerImpl.orderTableSchema(1L, new OrderVO());
  }

  @Test(expected = ResponseStatusException.class)
  public void createFieldSchemaForbiddenTest() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    try {
      dataSchemaControllerImpl.createFieldSchema(1L, new FieldSchemaVO());
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  /**
   * Creates the field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createFieldSchemaTest1() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.createFieldSchema(Mockito.any(), Mockito.any()))
        .thenThrow(EEAException.class);
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setName("test");

    try {
      dataSchemaControllerImpl.createFieldSchema(1L, fieldSchemaVO);
    } catch (ResponseStatusException ex) {
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
  }

  /**
   * Creates the field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createFieldSchemaTest2() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.createFieldSchema(Mockito.any(), Mockito.any())).thenReturn("");
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setName("test");
    try {
      dataSchemaControllerImpl.createFieldSchema(1L, fieldSchemaVO);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.INVALID_OBJECTID, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
  }

  /**
   * Creates the field schema test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createFieldSchemaTest3() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.createFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn("FieldId");
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setName("test");
    fieldSchemaVO.setRequired(Boolean.TRUE);
    assertEquals("FieldId", dataSchemaControllerImpl.createFieldSchema(1L, fieldSchemaVO));
  }

  /**
   * Creates the field schema test 4.
   *
   * @throws EEAException the EEA exception
   */
  @Test()
  public void createFieldSchemaTest4() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    try {
      dataSchemaControllerImpl.createFieldSchema(1L, new FieldSchemaVO());
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.FIELD_NAME_NULL, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
  }

  @Test
  public void createFieldSchemaFieldSchemaNotRequiredTest() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.createFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn("FieldId");
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setName("test");
    fieldSchemaVO.setRequired(Boolean.FALSE);
    assertEquals("FieldId", dataSchemaControllerImpl.createFieldSchema(1L, fieldSchemaVO));
  }

  @Test(expected = ResponseStatusException.class)
  public void updateFieldSchemaForbiddenTest() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setName("fieldName");
    try {
      dataSchemaControllerImpl.updateFieldSchema(1L, fieldSchemaVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  /**
   * Update field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest1() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("fieldSchemaId");
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("datasetSchemaId");
    Mockito.when(dataschemaService.updateFieldSchema(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean())).thenReturn(DataType.TEXT);
    Mockito.when(dataschemaService.checkPkAllowUpdate(Mockito.any(), Mockito.any()))
        .thenReturn(true);

    dataSchemaControllerImpl.updateFieldSchema(1L, fieldSchemaVO);
  }

  /**
   * Update field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest2() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(false);
    fieldSchemaVO.setId("fieldSchemaId");
    fieldSchemaVO.setName("fieldName");
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("datasetSchemaId");
    Mockito.when(dataschemaService.updateFieldSchema(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean())).thenReturn(DataType.TEXT);
    Mockito.when(dataschemaService.checkPkAllowUpdate(Mockito.any(), Mockito.any()))
        .thenReturn(true);
    dataSchemaControllerImpl.updateFieldSchema(1L, fieldSchemaVO);
    Mockito.verify(dataschemaService, times(1)).propagateRulesAfterUpdateSchema(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Update field schema test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest3() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("fieldSchemaId");
    fieldSchemaVO.setName("fieldName");
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("datasetSchemaId");
    Mockito.when(dataschemaService.updateFieldSchema(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean())).thenReturn(null);
    Mockito.when(dataschemaService.checkPkAllowUpdate(Mockito.any(), Mockito.any()))
        .thenReturn(true);
    dataSchemaControllerImpl.updateFieldSchema(1L, fieldSchemaVO);
    Mockito.verify(dataschemaService, times(1)).propagateRulesAfterUpdateSchema(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Update field schema test 4.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest4() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("fieldSchemaId");
    fieldSchemaVO.setName("fieldName");
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("datasetSchemaId");
    Mockito.when(dataschemaService.updateFieldSchema(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean())).thenReturn(null);
    Mockito.when(dataschemaService.checkPkAllowUpdate(Mockito.any(), Mockito.any()))
        .thenReturn(true);
    dataSchemaControllerImpl.updateFieldSchema(1L, fieldSchemaVO);
    Mockito.verify(rulesControllerZuul, times(0)).createAutomaticRule(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean());
  }

  /**
   * Update field schema test 5.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest5() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(false);
    fieldSchemaVO.setId("fieldSchemaId");
    fieldSchemaVO.setName("fieldName");
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("datasetSchemaId");
    Mockito.when(dataschemaService.updateFieldSchema(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean())).thenReturn(null);
    Mockito.when(dataschemaService.checkPkAllowUpdate(Mockito.any(), Mockito.any()))
        .thenReturn(true);
    dataSchemaControllerImpl.updateFieldSchema(1L, fieldSchemaVO);
    Mockito.verify(dataschemaService, times(1)).propagateRulesAfterUpdateSchema(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Update field schema test 6.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest6() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("datacustodian");
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenThrow(EEAException.class);
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setName("fieldName");
    try {
      dataSchemaControllerImpl.updateFieldSchema(1L, fieldSchemaVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  @Test
  public void updateFieldSchemaCheckClearAttachmentsTrueTest() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("fieldSchemaId");
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("datasetSchemaId");
    Mockito.when(dataschemaService.checkPkAllowUpdate(Mockito.any(), Mockito.any()))
        .thenReturn(true);
    Mockito
        .when(dataschemaService.checkClearAttachments(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(true);

    dataSchemaControllerImpl.updateFieldSchema(1L, fieldSchemaVO);
  }

  @Test
  public void updateFieldSchemacheckPkAllowUpdateFalseTest() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("fieldSchemaId");
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("datasetSchemaId");
    Mockito.when(dataschemaService.checkPkAllowUpdate(Mockito.any(), Mockito.any()))
        .thenReturn(false);

    dataSchemaControllerImpl.updateFieldSchema(1L, fieldSchemaVO);
  }

  @Test(expected = ResponseStatusException.class)
  public void updateFieldSchemaFieldSchemaPkTrueTest() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("fieldSchemaId");
    fieldSchemaVO.setPk(true);
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("datasetSchemaId");
    Mockito.when(dataschemaService.checkPkAllowUpdate(Mockito.any(), Mockito.any()))
        .thenReturn(false);
    try {
      dataSchemaControllerImpl.updateFieldSchema(1L, fieldSchemaVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.PK_ALREADY_EXISTS, e.getReason());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void updateFieldSchemaFieldSchemaPkFalseTest() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("fieldSchemaId");
    fieldSchemaVO.setPk(false);
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("datasetSchemaId");
    Mockito.when(dataschemaService.checkPkAllowUpdate(Mockito.any(), Mockito.any()))
        .thenReturn(false);
    try {
      dataSchemaControllerImpl.updateFieldSchema(1L, fieldSchemaVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.PK_REFERENCED, e.getReason());
      throw e;
    }
  }

  @Test
  public void updateFieldSchemacheckPkAllowUpdateTest() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    FieldSchemaVO fieldSchemaVO = Mockito.mock(FieldSchemaVO.class);
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("fieldSchemaId");
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("datasetSchemaId");
    Mockito.when(dataschemaService.checkPkAllowUpdate(Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(fieldSchemaVO.getPk()).thenReturn(false).thenReturn(false).thenReturn(false)
        .thenReturn(true);

    dataSchemaControllerImpl.updateFieldSchema(1L, fieldSchemaVO);
    Mockito.verify(dataschemaService, times(1)).getDatasetSchemaId(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteFieldSchemaForbiddenTest() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    try {
      dataSchemaControllerImpl.deleteFieldSchema(1L, "");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  /**
   * Delete field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteFieldSchemaTest1() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.deleteFieldSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(true);
    Mockito.doNothing().when(datasetService).deleteFieldValues(Mockito.any(), Mockito.any());
    dataSchemaControllerImpl.deleteFieldSchema(1L, "");
    Mockito.verify(dataschemaService, times(1)).getDatasetSchemaId(Mockito.any());
  }

  /**
   * Delete field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteFieldSchemaTest2() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.deleteFieldSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    try {
      dataSchemaControllerImpl.deleteFieldSchema(1L, "");
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.INVALID_OBJECTID, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Delete field schema test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteFieldSchemaTest3() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.deleteFieldSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenThrow(EEAException.class);
    try {
      dataSchemaControllerImpl.deleteFieldSchema(1L, "");
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DELETING_FIELD_SCHEMA, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void deleteFieldSchemacheckExistingPkReferencedTrueTest() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.checkExistingPkReferenced(Mockito.any())).thenReturn(true);
    try {
      dataSchemaControllerImpl.deleteFieldSchema(1L, "");
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.PK_REFERENCED, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  @Test
  public void deleteFieldSchemaDataTypeLinkTest() throws EEAException {
    FieldSchemaVO fieldVO = new FieldSchemaVO();
    fieldVO.setType(DataType.LINK);

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.getFieldSchema(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(fieldVO);
    Mockito.when(dataschemaService.deleteFieldSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(true);
    Mockito.doNothing().when(datasetService).deleteFieldValues(Mockito.any(), Mockito.any());
    dataSchemaControllerImpl.deleteFieldSchema(1L, "");
    Mockito.verify(rulesControllerZuul, times(1))
        .deleteRuleByReferenceFieldSchemaPKId(Mockito.any(), Mockito.any());
  }

  @Test
  public void deleteFieldSchemaDataTypeExternalLinkTest() throws EEAException {
    FieldSchemaVO fieldVO = new FieldSchemaVO();
    fieldVO.setType(DataType.EXTERNAL_LINK);

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.getFieldSchema(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(fieldVO);
    Mockito.when(dataschemaService.deleteFieldSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(true);
    Mockito.doNothing().when(datasetService).deleteFieldValues(Mockito.any(), Mockito.any());
    dataSchemaControllerImpl.deleteFieldSchema(1L, "");
    Mockito.verify(rulesControllerZuul, times(1))
        .deleteRuleByReferenceFieldSchemaPKId(Mockito.any(), Mockito.any());
  }

  @Test
  public void deleteFieldSchemaDataTypeOtherTest() throws EEAException {
    FieldSchemaVO fieldVO = new FieldSchemaVO();
    fieldVO.setType(DataType.ATTACHMENT);

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.getFieldSchema(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(fieldVO);
    Mockito.when(dataschemaService.deleteFieldSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(true);
    Mockito.doNothing().when(datasetService).deleteFieldValues(Mockito.any(), Mockito.any());
    dataSchemaControllerImpl.deleteFieldSchema(1L, "");
    Mockito.verify(dataschemaService, times(1)).getDatasetSchemaId(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void orderFieldSchemaForbiddenTest() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    try {
      dataSchemaControllerImpl.orderFieldSchema(1L, new OrderVO());
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  /**
   * Order field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void orderFieldSchemaTest1() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.orderFieldSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(true);
    dataSchemaControllerImpl.orderFieldSchema(1L, new OrderVO());
    Mockito.verify(dataschemaService, times(1)).getDatasetSchemaId(Mockito.any());
  }

  /**
   * Order field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void orderFieldSchemaTest2() throws EEAException {

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.orderFieldSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    expectedEx.expect(ResponseStatusException.class);
    expectedEx.expectMessage(EEAErrorMessage.SCHEMA_NOT_FOUND);
    dataSchemaControllerImpl.orderFieldSchema(1L, new OrderVO());
  }

  @Test(expected = ResponseStatusException.class)
  public void createTableSchemaForbiddenTest() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    TableSchemaVO tableSchemaVO = new TableSchemaVO();
    tableSchemaVO.setNameTableSchema("tableName");
    try {
      dataSchemaControllerImpl.createTableSchema(1L, tableSchemaVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  /**
   * Creates the table schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createTableSchemaTest1() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    TableSchemaVO tableSchemaVO = new TableSchemaVO();
    tableSchemaVO.setIdTableSchema("id");
    tableSchemaVO.setNameTableSchema("tableName");
    when(dataschemaService.createTableSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(tableSchemaVO);
    Mockito.doNothing().when(datasetService).saveTablePropagation(Mockito.any(), Mockito.any());
    assertEquals(tableSchemaVO, dataSchemaControllerImpl.createTableSchema(1L, tableSchemaVO));
  }

  /**
   * Creates the table schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createTableSchemaTest2() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    when(dataschemaService.createTableSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new TableSchemaVO());
    Mockito.doThrow(EEAException.class).when(datasetService).saveTablePropagation(Mockito.any(),
        Mockito.any());
    TableSchemaVO tableSchemaVO = new TableSchemaVO();
    tableSchemaVO.setNameTableSchema("tableName");
    try {
      dataSchemaControllerImpl.createTableSchema(1L, tableSchemaVO);
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATASET_INCORRECT_ID, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Update dataset schema description test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateDatasetSchemaDescriptionTest1() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    dataSchemaControllerImpl.updateDatasetSchema(1L, datasetSchemaVO);
    Mockito.verify(dataschemaService, times(1)).updateDatasetSchemaDescription(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void updateDatasetSchemaWebformTest1() throws EEAException {
    WebformVO webform = new WebformVO();
    webform.setName("name");
    datasetSchemaVO.setWebform(webform);
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    dataSchemaControllerImpl.updateDatasetSchema(1L, datasetSchemaVO);
    Mockito.verify(dataschemaService, times(1)).updateDatasetSchemaDescription(Mockito.any(),
        Mockito.any());
  }

  /**
   * Update dataset schema description test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateDatasetSchemaDescriptionTest3() throws EEAException {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenThrow(EEAException.class);
    try {
      dataSchemaControllerImpl.updateDatasetSchema(1L, datasetSchemaVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.SCHEMA_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test
  public void updateDatasetSchemaCompleteTest() throws EEAException {
    WebformVO webform = new WebformVO();
    webform.setName("name");
    datasetSchemaVO.setWebform(webform);
    datasetSchemaVO.setAvailableInPublic(true);
    datasetSchemaVO.setDescription(null);
    datasetSchemaVO.setReferenceDataset(true);
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    dataSchemaControllerImpl.updateDatasetSchema(1L, datasetSchemaVO);
    Mockito.verify(dataschemaService, times(1)).updateReferenceDataset(1L, "", true);
  }

  @Test
  public void updateDatasetSchemaTypeStatusDraftTest() throws EEAException {
    WebformVO webform = new WebformVO();
    webform.setName("name");
    datasetSchemaVO.setWebform(webform);
    datasetSchemaVO.setAvailableInPublic(true);
    datasetSchemaVO.setDescription(null);
    datasetSchemaVO.setReferenceDataset(true);
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    dataSchemaControllerImpl.updateDatasetSchema(1L, datasetSchemaVO);
    Mockito.verify(dataschemaService, times(1)).updateDatasetSchemaExportable("", true);
  }

  /**
   * Test validate schema.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testValidateSchema() throws EEAException {
    Assert.assertFalse(dataSchemaControllerImpl.validateSchema(new ObjectId().toString()));
  }

  /**
   * Test validate schemas.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testValidateSchemas() throws EEAException {
    DataFlowVO df = new DataFlowVO();
    DesignDatasetVO ds = new DesignDatasetVO();
    ds.setDatasetSchema(new ObjectId().toString());
    df.setDesignDatasets(new ArrayList<>());
    df.getDesignDatasets().add(ds);
    when(dataflowControllerZuul.findById(Mockito.any(), Mockito.any())).thenReturn(df);
    Assert.assertFalse(dataSchemaControllerImpl.validateSchemas(1L));
  }

  @Test
  public void ValidateSchemasNullDesignDatasetsTest() throws EEAException {
    DataFlowVO df = new DataFlowVO();
    DesignDatasetVO ds = new DesignDatasetVO();
    ds.setDatasetSchema(new ObjectId().toString());
    when(dataflowControllerZuul.findById(Mockito.any(), Mockito.any())).thenReturn(df);
    Assert.assertFalse(dataSchemaControllerImpl.validateSchemas(1L));
  }

  @Test
  public void ValidateSchemasEmptyDesignDatasetsTest() throws EEAException {
    DataFlowVO df = new DataFlowVO();
    DesignDatasetVO ds = new DesignDatasetVO();
    ds.setDatasetSchema(new ObjectId().toString());
    df.setDesignDatasets(new ArrayList<>());
    when(dataflowControllerZuul.findById(Mockito.any(), Mockito.any())).thenReturn(df);
    Assert.assertFalse(dataSchemaControllerImpl.validateSchemas(1L));
  }

  @Test
  public void ValidateSchemasContainsTrueTest() throws EEAException {
    DataFlowVO df = new DataFlowVO();
    DesignDatasetVO ds = new DesignDatasetVO();
    ds.setDatasetSchema(new ObjectId().toString());
    df.setDesignDatasets(new ArrayList<>());
    df.getDesignDatasets().add(ds);
    when(dataflowControllerZuul.findById(Mockito.any(), Mockito.any())).thenReturn(df);
    Mockito.when(dataschemaService.validateSchema(Mockito.anyString(), Mockito.any()))
        .thenReturn(true);
    Assert.assertTrue(dataSchemaControllerImpl.validateSchemas(1L));
  }

  @Test
  public void ValidateSchemasContainsFalseTest() throws EEAException {
    DataFlowVO df = new DataFlowVO();
    DesignDatasetVO ds = new DesignDatasetVO();
    ds.setDatasetSchema(new ObjectId().toString());
    df.setDesignDatasets(new ArrayList<>());
    df.getDesignDatasets().add(ds);
    df.getDesignDatasets().add(ds);
    when(dataflowControllerZuul.findById(Mockito.any(), Mockito.any())).thenReturn(df);
    Mockito.when(dataschemaService.validateSchema(Mockito.anyString(), Mockito.any()))
        .thenReturn(true).thenReturn(false);
    Assert.assertFalse(dataSchemaControllerImpl.validateSchemas(1L));
  }

  /**
   * Test find data schemas by id dataflow.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testFindDataSchemasByIdDataflow() throws EEAException {
    DesignDatasetVO design = new DesignDatasetVO();
    design.setId(1L);
    when(designDatasetService.getDesignDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(Arrays.asList(design));
    when(dataschemaService.getDataSchemaByDatasetId(Mockito.anyBoolean(), Mockito.any()))
        .thenReturn(datasetSchemaVO);
    assertEquals("failed assertion", Arrays.asList(datasetSchemaVO),
        dataSchemaControllerImpl.findDataSchemasByIdDataflow(1L));
  }

  /**
   * Test create unique constraint test.
   */
  @Test
  public void testCreateUniqueConstraintTest() {
    UniqueConstraintVO uniqueConstraint = new UniqueConstraintVO();
    List<String> fields = new ArrayList<>();
    fields.add(new ObjectId().toString());
    uniqueConstraint.setDatasetSchemaId(new ObjectId().toString());
    uniqueConstraint.setTableSchemaId(new ObjectId().toString());
    uniqueConstraint.setFieldSchemaIds(fields);
    uniqueConstraint.setDataflowId("1");
    dataSchemaControllerImpl.createUniqueConstraint(uniqueConstraint);
    Mockito.verify(dataschemaService, times(1)).createUniqueConstraint(Mockito.any());
  }


  /**
   * Creates the unique constraint schema id error test.
   */
  @Test(expected = ResponseStatusException.class)
  public void createUniqueConstraintSchemaIdErrorTest() {
    UniqueConstraintVO uniqueConstraint = new UniqueConstraintVO();
    uniqueConstraint.setDataflowId("1");
    try {
      dataSchemaControllerImpl.createUniqueConstraint(uniqueConstraint);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
      throw e;
    }
  }

  /**
   * Creates the unique constraint nofields error test.
   */
  @Test(expected = ResponseStatusException.class)
  public void createUniqueConstraintNofieldsErrorTest() {
    UniqueConstraintVO uniqueConstraint = new UniqueConstraintVO();
    uniqueConstraint.setDatasetSchemaId(new ObjectId().toString());
    uniqueConstraint.setTableSchemaId(new ObjectId().toString());
    uniqueConstraint.setDataflowId("1");
    try {
      dataSchemaControllerImpl.createUniqueConstraint(uniqueConstraint);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.UNREPORTED_FIELDSCHEMAS, e.getReason());
      throw e;
    }
  }

  /**
   * Creates the unique constraint table schema id error test.
   */
  @Test(expected = ResponseStatusException.class)
  public void createUniqueConstraintTableSchemaIdErrorTest() {
    UniqueConstraintVO uniqueConstraint = new UniqueConstraintVO();
    uniqueConstraint.setDatasetSchemaId(new ObjectId().toString());
    uniqueConstraint.setDataflowId("1");
    try {
      dataSchemaControllerImpl.createUniqueConstraint(uniqueConstraint);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.IDTABLESCHEMA_INCORRECT, e.getReason());
      throw e;
    }
  }

  /**
   * Creates the unique constraint unreported data error test.
   */
  @Test(expected = ResponseStatusException.class)
  public void createUniqueConstraintUnreportedDataErrorTest() {
    try {
      dataSchemaControllerImpl.createUniqueConstraint(null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.UNREPORTED_DATA, e.getReason());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void createUniqueConstraintEmptyfieldsErrorTest() {
    UniqueConstraintVO uniqueConstraint = new UniqueConstraintVO();
    uniqueConstraint.setDatasetSchemaId(new ObjectId().toString());
    uniqueConstraint.setTableSchemaId(new ObjectId().toString());
    uniqueConstraint.setDataflowId("1");
    uniqueConstraint.setFieldSchemaIds(new ArrayList<>());
    try {
      dataSchemaControllerImpl.createUniqueConstraint(uniqueConstraint);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.UNREPORTED_FIELDSCHEMAS, e.getReason());
      throw e;
    }
  }

  /**
   * Delete unique constraint test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteUniqueConstraintTest() throws EEAException {
    dataSchemaControllerImpl.deleteUniqueConstraint(new ObjectId().toString(), 1L);
    Mockito.verify(dataschemaService, times(1)).deleteUniqueConstraint(Mockito.any());
  }

  /**
   * Delete unique constraint error test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteUniqueConstraintErrorTest() throws EEAException {
    doThrow(EEAException.class).when(dataschemaService).deleteUniqueConstraint(Mockito.any());
    try {
      dataSchemaControllerImpl.deleteUniqueConstraint(new ObjectId().toString(), 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  /**
   * Update unique constraint test.
   */
  @Test
  public void updateUniqueConstraintTest() {
    List<String> fields = new ArrayList<>();
    fields.add(new ObjectId().toString());
    UniqueConstraintVO uniqueConstraint = new UniqueConstraintVO();
    uniqueConstraint.setDatasetSchemaId(new ObjectId().toString());
    uniqueConstraint.setTableSchemaId(new ObjectId().toString());
    uniqueConstraint.setUniqueId(new ObjectId().toString());
    uniqueConstraint.setFieldSchemaIds(fields);
    uniqueConstraint.setDataflowId("1");
    dataSchemaControllerImpl.updateUniqueConstraint(uniqueConstraint);
    Mockito.verify(dataschemaService, times(1)).updateUniqueConstraint(Mockito.any());
  }

  /**
   * Update unique constraint schema id error test.
   */
  @Test(expected = ResponseStatusException.class)
  public void updateUniqueConstraintSchemaIdErrorTest() {
    UniqueConstraintVO uniqueConstraint = new UniqueConstraintVO();
    uniqueConstraint.setUniqueId(new ObjectId().toString());
    uniqueConstraint.setDataflowId("1");
    try {
      dataSchemaControllerImpl.updateUniqueConstraint(uniqueConstraint);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
      throw e;
    }
  }


  /**
   * Update unique constraint table schema id error test.
   */
  @Test(expected = ResponseStatusException.class)
  public void updateUniqueConstraintTableSchemaIdErrorTest() {
    UniqueConstraintVO uniqueConstraint = new UniqueConstraintVO();
    uniqueConstraint.setUniqueId(new ObjectId().toString());
    uniqueConstraint.setDatasetSchemaId(new ObjectId().toString());
    uniqueConstraint.setDataflowId("1");
    try {
      dataSchemaControllerImpl.updateUniqueConstraint(uniqueConstraint);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.IDTABLESCHEMA_INCORRECT, e.getReason());
      throw e;
    }
  }

  /**
   * Update unique constraint id error test.
   */
  @Test(expected = ResponseStatusException.class)
  public void updateUniqueConstraintIdErrorTest() {
    UniqueConstraintVO uniqueConstraint = new UniqueConstraintVO();
    uniqueConstraint.setDatasetSchemaId(new ObjectId().toString());
    uniqueConstraint.setTableSchemaId(new ObjectId().toString());
    uniqueConstraint.setDataflowId("1");
    try {
      dataSchemaControllerImpl.updateUniqueConstraint(uniqueConstraint);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.IDUNQUECONSTRAINT_INCORRECT, e.getReason());
      throw e;
    }
  }

  /**
   * Update unique constraint field error test.
   */
  @Test(expected = ResponseStatusException.class)
  public void updateUniqueConstraintFieldErrorTest() {
    UniqueConstraintVO uniqueConstraint = new UniqueConstraintVO();
    uniqueConstraint.setDatasetSchemaId(new ObjectId().toString());
    uniqueConstraint.setTableSchemaId(new ObjectId().toString());
    uniqueConstraint.setUniqueId(new ObjectId().toString());
    uniqueConstraint.setDataflowId("1");
    try {
      dataSchemaControllerImpl.updateUniqueConstraint(uniqueConstraint);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.UNREPORTED_FIELDSCHEMAS, e.getReason());
      throw e;
    }
  }

  /**
   * Update unique constraint unreported data error test.
   */
  @Test(expected = ResponseStatusException.class)
  public void updateUniqueConstraintUnreportedDataErrorTest() {
    try {
      dataSchemaControllerImpl.updateUniqueConstraint(null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.UNREPORTED_DATA, e.getReason());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void updateUniqueConstraintEmptyFieldErrorTest() {
    UniqueConstraintVO uniqueConstraint = new UniqueConstraintVO();
    uniqueConstraint.setDatasetSchemaId(new ObjectId().toString());
    uniqueConstraint.setTableSchemaId(new ObjectId().toString());
    uniqueConstraint.setUniqueId(new ObjectId().toString());
    uniqueConstraint.setDataflowId("1");
    uniqueConstraint.setFieldSchemaIds(new ArrayList<>());
    try {
      dataSchemaControllerImpl.updateUniqueConstraint(uniqueConstraint);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.UNREPORTED_FIELDSCHEMAS, e.getReason());
      throw e;
    }
  }

  /**
   * Gets the unique constraints tests.
   *
   * @return the unique constraints tests
   */
  @Test
  public void getUniqueConstraintsTests() {
    List<UniqueConstraintVO> uniques = new ArrayList<>();
    Mockito.when(dataschemaService.getUniqueConstraints(Mockito.any())).thenReturn(uniques);
    assertEquals(uniques,
        dataSchemaControllerImpl.getUniqueConstraints(new ObjectId().toString(), null));
  }

  /**
   * Gets the unique constraints error test.
   *
   * @return the unique constraints error test
   */
  @Test(expected = ResponseStatusException.class)
  public void getUniqueConstraintsErrorTest() {
    try {
      dataSchemaControllerImpl.getUniqueConstraints(null, null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
      throw e;
    }
  }

  /**
   * Gets the unique constraint test.
   *
   * @return the unique constraint test
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void getUniqueConstraintTest() throws EEAException {
    UniqueConstraintVO unique = new UniqueConstraintVO();
    Mockito.when(dataschemaService.getUniqueConstraint(Mockito.any())).thenReturn(unique);
    assertEquals(unique, dataSchemaControllerImpl.getUniqueConstraint(new ObjectId().toString()));
  }

  /**
   * Gets the unique constraint error test.
   *
   * @return the unique constraint error test
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getUniqueConstraintErrorTest() throws EEAException {
    doThrow(EEAException.class).when(dataschemaService).getUniqueConstraint(Mockito.any());
    try {
      dataSchemaControllerImpl.getUniqueConstraint(new ObjectId().toString());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  /**
   * Test copy designs from dataflow.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testCopyDesignsFromDataflow() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    dataSchemaControllerImpl.copyDesignsFromDataflow(1L, 1L);
    Mockito.verify(designDatasetService, times(1)).copyDesignDatasets(Mockito.any(), Mockito.any());
  }

  /**
   * Test copy designs from dataflow exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testCopyDesignsFromDataflowException() throws EEAException {
    try {
      doThrow(EEAException.class).when(designDatasetService).copyDesignDatasets(Mockito.anyLong(),
          Mockito.anyLong());
      Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
      Mockito.when(authentication.getName()).thenReturn("user");
      dataSchemaControllerImpl.copyDesignsFromDataflow(1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  /**
   * Gets the simple schema test.
   *
   * @return the simple schema test
   */
  @Test
  public void getSimpleSchemaTest() {
    assertNull(dataSchemaControllerImpl.getSimpleSchema(1L, 1L, 1L));
  }


  /**
   * Gets the simple schema legacy test.
   *
   * @return the simple schema legacy test
   */
  @Test
  public void getSimpleSchemaLegacyTest() {
    assertNull(dataSchemaControllerImpl.getSimpleSchemaLegacy(1L, 1L, 1L));
  }

  /**
   * Gets the simple schema null test.
   *
   * @return the simple schema null test
   */
  @Test(expected = ResponseStatusException.class)
  public void getSimpleSchemaNullTest() {
    try {
      dataSchemaControllerImpl.getSimpleSchema(null, 1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.DATASET_INCORRECT_ID, e.getReason());
      throw e;
    }
  }

  /**
   * Gets the simple schema error test.
   *
   * @return the simple schema error test
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getSimpleSchemaErrorTest() throws EEAException {
    doThrow(EEAException.class).when(dataschemaService).getSimpleSchema(Mockito.anyLong());
    try {
      dataSchemaControllerImpl.getSimpleSchema(1l, 1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void importSchemasForbiddenTest() {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    MultipartFile multipartFile =
        new MockMultipartFile("file", "file.zip", "application/x-zip-compressed", "".getBytes());
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    try {
      dataSchemaControllerImpl.importSchemas(1L, multipartFile);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
      throw e;
    }
  }

  @Test
  public void testImportSchemas() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zip = new ZipOutputStream(baos);
    ZipEntry entry1 = new ZipEntry("Table.schema");
    ZipEntry entry2 = new ZipEntry("Table.qcrules");
    zip.putNextEntry(entry1);
    zip.putNextEntry(entry2);
    zip.close();
    MultipartFile multipartFile = new MockMultipartFile("file", "file.zip",
        "application/x-zip-compressed", baos.toByteArray());

    dataSchemaControllerImpl.importSchemas(1L, multipartFile);
    Mockito.verify(dataschemaService, times(1)).importSchemas(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testImportSchemasException() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);

    try {
      doThrow(EEAException.class).when(dataschemaService).importSchemas(Mockito.any(),
          Mockito.any(), Mockito.any());
      Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
      Mockito.when(authentication.getName()).thenReturn("user");

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ZipOutputStream zip = new ZipOutputStream(baos);
      ZipEntry entry1 = new ZipEntry("Table.schema");
      ZipEntry entry2 = new ZipEntry("Table.qcrules");
      zip.putNextEntry(entry1);
      zip.putNextEntry(entry2);
      zip.close();
      MultipartFile multipartFile = new MockMultipartFile("file", "file.zip",
          "application/x-zip-compressed", baos.toByteArray());

      dataSchemaControllerImpl.importSchemas(1L, multipartFile);

    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  @Test
  public void testExportSchemas() throws EEAException, IOException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    dataSchemaControllerImpl.exportSchemas(1L);
    Mockito.verify(dataschemaService, times(1)).exportSchemas(Mockito.any());
  }


  @Test(expected = ResponseStatusException.class)
  public void testExportSchemasException() throws EEAException, IOException {
    try {
      doThrow(new EEAException("error")).when(dataschemaService).exportSchemas(Mockito.any());
      Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
      Mockito.when(authentication.getName()).thenReturn("user");

      dataSchemaControllerImpl.exportSchemas(1L);

    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }


  @Test
  public void testExportFieldSchemas() throws EEAException, IOException {

    dataSchemaControllerImpl.exportFieldSchemas(new ObjectId().toString(), 1L,
        new ObjectId().toString());
    Mockito.verify(dataschemaService, times(1)).exportFieldsSchema(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Export field schemas legacy test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void exportFieldSchemasLegacyTest() throws EEAException, IOException {

    dataSchemaControllerImpl.exportFieldSchemasLegacy(new ObjectId().toString(), 1L,
        new ObjectId().toString());
    Mockito.verify(dataschemaService, times(1)).exportFieldsSchema(Mockito.any(), Mockito.any(),
        Mockito.any());
  }


  @Test(expected = ResponseStatusException.class)
  public void testExportFieldSchemasException() throws EEAException, IOException {
    try {
      doThrow(new EEAException("error")).when(dataschemaService).exportFieldsSchema(Mockito.any(),
          Mockito.any(), Mockito.any());

      dataSchemaControllerImpl.exportFieldSchemas(new ObjectId().toString(), 1L,
          new ObjectId().toString());

    } catch (EEAException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
      throw e;
    }
  }


  @Test
  public void testImportFieldSchemas() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zip = new ZipOutputStream(baos);
    ZipEntry entry1 = new ZipEntry("Table.schema");
    ZipEntry entry2 = new ZipEntry("Table.qcrules");
    zip.putNextEntry(entry1);
    zip.putNextEntry(entry2);
    zip.close();
    MultipartFile multipartFile = new MockMultipartFile("file", "file.zip",
        "application/x-zip-compressed", baos.toByteArray());

    dataSchemaControllerImpl.importFieldSchemas(new ObjectId().toString(), 1L,
        new ObjectId().toString(), multipartFile, true);
    Mockito.verify(dataschemaService, times(1)).importFieldsSchema(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.anyBoolean());
  }

  @Test(expected = ResponseStatusException.class)
  public void testImportFieldSchemasIOExceptionTest() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zip = new ZipOutputStream(baos);
    ZipEntry entry1 = new ZipEntry("Table.schema");
    ZipEntry entry2 = new ZipEntry("Table.qcrules");
    zip.putNextEntry(entry1);
    zip.putNextEntry(entry2);
    zip.close();
    MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
    Mockito.when(multipartFile.getInputStream()).thenThrow(IOException.class);
    try {
      dataSchemaControllerImpl.importFieldSchemas(new ObjectId().toString(), 1L,
          new ObjectId().toString(), multipartFile, true);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals("Error importing file", e.getReason());
      throw e;
    }

  }

  /**
   * Import field schemas legacy test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void importFieldSchemasLegacyTest() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zip = new ZipOutputStream(baos);
    ZipEntry entry1 = new ZipEntry("Table.schema");
    ZipEntry entry2 = new ZipEntry("Table.qcrules");
    zip.putNextEntry(entry1);
    zip.putNextEntry(entry2);
    zip.close();
    MultipartFile multipartFile = new MockMultipartFile("file", "file.zip",
        "application/x-zip-compressed", baos.toByteArray());

    dataSchemaControllerImpl.importFieldSchemasLegacy(new ObjectId().toString(), 1L,
        new ObjectId().toString(), multipartFile, true);
    Mockito.verify(dataschemaService, times(1)).importFieldsSchema(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.anyBoolean());
  }


  @Test
  public void testExportFieldSchemasFromDataset() throws EEAException, IOException {

    dataSchemaControllerImpl.exportFieldSchemasFromDataset(1L);
    Mockito.verify(dataschemaService, times(1)).exportZipFieldSchemas(Mockito.anyLong());
  }


  /**
   * Export field schemas from dataset legacy test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void exportFieldSchemasFromDatasetLegacyTest() throws EEAException, IOException {
    dataSchemaControllerImpl.exportFieldSchemasFromDatasetLegacy(1L);
    Mockito.verify(dataschemaService, times(1)).exportZipFieldSchemas(Mockito.anyLong());
  }


  @Test(expected = ResponseStatusException.class)
  public void testExportFieldSchemasFromDatasetException() throws EEAException, IOException {
    try {
      doThrow(new EEAException("error")).when(dataschemaService)
          .exportZipFieldSchemas(Mockito.anyLong());

      dataSchemaControllerImpl.exportFieldSchemasFromDataset(1L);

    } catch (EEAException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void getTableSchemasIdsExceptionTest() {
    try {
      dataSchemaControllerImpl.getTableSchemasIds(null, 1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void getTableSchemasIdsInternalServerErrorException() throws EEAException {
    try {
      doThrow(new EEAException("error")).when(dataschemaService).getTableSchemasIds(1L);
      dataSchemaControllerImpl.getTableSchemasIds(1L, 1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  @Test
  public void getTableSchemasIdsTest() throws EEAException {
    dataSchemaControllerImpl.getTableSchemasIds(1L, 1L, 1L);
    Mockito.verify(dataschemaService, times(1)).getTableSchemasIds(Mockito.anyLong());
  }

  /**
   * Gets the table schemas ids legacy test.
   *
   * @return the table schemas ids legacy test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getTableSchemasIdsLegacyTest() throws EEAException {
    dataSchemaControllerImpl.getTableSchemasIdsLegacy(1L, 1L, 1L);
    Mockito.verify(dataschemaService, times(1)).getTableSchemasIds(Mockito.anyLong());
  }

  @Test(expected = ResponseStatusException.class)
  public void findPublicDataSchemaByDatasetIdExceptionTest() throws EEAException {
    try {
      doThrow(new EEAException("error")).when(dataschemaService).getDataSchemaByDatasetId(true, 1L);
      dataSchemaControllerImpl.findDataSchemaByDatasetIdPrivate(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  @Test
  public void findPublicDataSchemaByDatasetIdTest() throws EEAException {
    dataSchemaControllerImpl.findDataSchemaByDatasetIdPrivate(1L);
    Mockito.verify(dataschemaService, times(1)).getDataSchemaByDatasetId(true, 1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void getPublicUniqueConstraintsExceptionTest() {
    try {
      dataSchemaControllerImpl.getPublicUniqueConstraints(null, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test
  public void getPublicUniqueConstraintsTest() {
    dataSchemaControllerImpl.getPublicUniqueConstraints("schema", 1L);
    Mockito.verify(dataschemaService, times(1)).getUniqueConstraints("schema");
  }

  @Test(expected = ResponseStatusException.class)
  public void findDataSchemasByIdDataflowExceptionTest() throws EEAException {
    List<DesignDatasetVO> designs = new ArrayList<>();
    DesignDatasetVO design = new DesignDatasetVO();
    design.setId(1L);
    designs.add(design);
    try {
      doThrow(new EEAException("error")).when(dataschemaService).getDataSchemaByDatasetId(false,
          1L);
      Mockito.when(designDatasetService.getDesignDataSetIdByDataflowId(1L)).thenReturn(designs);
      dataSchemaControllerImpl.findDataSchemasByIdDataflow(1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test
  public void filterNameSchemaNameExceptionTest() throws EEAException {
    try {
      dataSchemaControllerImpl.createEmptyDatasetSchema(1L, "worng$Data%Schema&Name");
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.DATASET_SCHEMA_INVALID_NAME_ERROR, e.getReason());
    }
  }

  @Test
  public void filterNameNotSchemaNameExceptionTest() throws EEAException {
    TableSchemaVO tableSchemaVO = new TableSchemaVO();
    tableSchemaVO.setNameTableSchema("worng$Table%Schema&Name");
    try {
      dataSchemaControllerImpl.createTableSchema(1L, tableSchemaVO);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.DATASET_SCHEMA_INVALID_NAME_ERROR, e.getReason());
    }
  }
}
