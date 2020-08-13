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
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataSetSchemaControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetSchemaControllerImplTest {

  /** The data schema controller impl. */
  @InjectMocks
  private DatasetSchemaControllerImpl dataSchemaControllerImpl;

  /** The expected ex. */
  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  /** The dataschema service. */
  @Mock
  private DataschemaServiceImpl dataschemaService;

  /** The dataset service. */
  @Mock
  private DatasetService datasetService;

  /** The dataset metabase service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The record store controller zuul. */
  @Mock
  private RecordStoreControllerZuul recordStoreControllerZuul;

  /** The dataset snapshot service. */
  @Mock
  private DatasetSnapshotService datasetSnapshotService;

  /** The contributor controller zuul. */
  @Mock
  private ContributorControllerZuul contributorControllerZuul;

  /** The integration controller zuul. */
  @Mock
  private IntegrationControllerZuul integrationControllerZuul;

  /** The rules controller zuul. */
  @Mock
  private RulesControllerZuul rulesControllerZuul;

  /** The design dataset service. */
  @Mock
  private DesignDatasetService designDatasetService;

  /** The dataflow controller zuul. */
  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The dataset schema VO. */
  private DataSetSchemaVO datasetSchemaVO;

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
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
    MockitoAnnotations.initMocks(this);
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
    DataSetSchemaVO result = dataSchemaControllerImpl.findDataSchemaByDatasetId(1L);
    Assert.assertNotNull(result);
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

  /**
   * Creates the empty data set schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createEmptyDataSetSchemaTest() throws EEAException {
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

  /**
   * Creates the empty data set schema exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetSchemaException() throws EEAException {
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

  /**
   * Update table schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateTableSchemaTest() throws EEAException {
    dataSchemaControllerImpl.updateTableSchema(1L, new TableSchemaVO());
    Mockito.verify(dataschemaService, times(1)).updateTableSchema(Mockito.any(), Mockito.any());
  }

  /**
   * Update table schema test exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateTableSchemaTestException() throws EEAException {
    doThrow(EEAException.class).when(dataschemaService).updateTableSchema(Mockito.any(),
        Mockito.any());
    try {
      dataSchemaControllerImpl.updateTableSchema(1L, new TableSchemaVO());
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.DATASET_INCORRECT_ID, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Delete table schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteTableSchemaTest1() throws EEAException {
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
    Mockito.doThrow(EEAException.class).when(dataschemaService).deleteTableSchema(Mockito.any(),
        Mockito.any(), Mockito.any());
    try {
      dataSchemaControllerImpl.deleteTableSchema(1L, "");
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.EXECUTION_ERROR, ex.getReason());
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
    }
  }

  /**
   * Order table schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void orderTableSchemaTest1() throws EEAException {
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
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.orderTableSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    expectedEx.expect(ResponseStatusException.class);
    expectedEx.expectMessage(EEAErrorMessage.SCHEMA_NOT_FOUND);
    dataSchemaControllerImpl.orderTableSchema(1L, new OrderVO());
  }

  /**
   * Creates the field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createFieldSchemaTest1() throws EEAException {
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.createFieldSchema(Mockito.any(), Mockito.any()))
        .thenThrow(EEAException.class);
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
   * Creates the field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createFieldSchemaTest2() throws EEAException {
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
    try {
      dataSchemaControllerImpl.createFieldSchema(1L, new FieldSchemaVO());
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.FIELD_NAME_NULL, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
  }

  /**
   * Update field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest1() throws EEAException {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("fieldSchemaId");
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("datasetSchemaId");
    Mockito.when(dataschemaService.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(DataType.TEXT);
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
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(false);
    fieldSchemaVO.setId("fieldSchemaId");
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("datasetSchemaId");
    Mockito.when(dataschemaService.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(DataType.TEXT);
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
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("fieldSchemaId");
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("datasetSchemaId");
    Mockito.when(dataschemaService.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(null);
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
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("fieldSchemaId");
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("datasetSchemaId");
    Mockito.when(dataschemaService.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(null);
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
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(false);
    fieldSchemaVO.setId("fieldSchemaId");
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("datasetSchemaId");
    Mockito.when(dataschemaService.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(null);
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
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenThrow(EEAException.class);
    try {
      dataSchemaControllerImpl.updateFieldSchema(1L, new FieldSchemaVO());
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.FIELD_SCHEMA_ID_NOT_FOUND, e.getReason());
    }
  }

  /**
   * Delete field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteFieldSchemaTest1() throws EEAException {
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
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.deleteFieldSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenThrow(EEAException.class);
    try {
      dataSchemaControllerImpl.deleteFieldSchema(1L, "");
    } catch (ResponseStatusException ex) {
      assertEquals(EEAErrorMessage.INVALID_OBJECTID, ex.getReason());
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      throw ex;
    }
  }

  /**
   * Order field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void orderFieldSchemaTest1() throws EEAException {
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
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.orderFieldSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    expectedEx.expect(ResponseStatusException.class);
    expectedEx.expectMessage(EEAErrorMessage.SCHEMA_NOT_FOUND);
    dataSchemaControllerImpl.orderFieldSchema(1L, new OrderVO());
  }

  /**
   * Creates the table schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createTableSchemaTest1() throws EEAException {
    TableSchemaVO tableSchemaVO = new TableSchemaVO();
    tableSchemaVO.setIdTableSchema("id");
    when(dataschemaService.createTableSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(tableSchemaVO);
    Mockito.doNothing().when(datasetService).saveTablePropagation(Mockito.any(), Mockito.any());
    assertEquals(tableSchemaVO,
        dataSchemaControllerImpl.createTableSchema(1L, new TableSchemaVO()));
  }

  /**
   * Creates the table schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createTableSchemaTest2() throws EEAException {
    when(dataschemaService.createTableSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new TableSchemaVO());
    Mockito.doThrow(EEAException.class).when(datasetService).saveTablePropagation(Mockito.any(),
        Mockito.any());
    try {
      dataSchemaControllerImpl.createTableSchema(1L, new TableSchemaVO());
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
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.updateDatasetSchemaDescription(Mockito.any(), Mockito.any()))
        .thenReturn(true);
    dataSchemaControllerImpl.updateDatasetSchemaDescription(1L, datasetSchemaVO);
    Mockito.verify(dataschemaService, times(1)).updateDatasetSchemaDescription(Mockito.any(),
        Mockito.any());
  }

  /**
   * Update dataset schema description test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateDatasetSchemaDescriptionTest2() throws EEAException {
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.updateDatasetSchemaDescription(Mockito.any(), Mockito.any()))
        .thenReturn(false);
    try {
      dataSchemaControllerImpl.updateDatasetSchemaDescription(1L, datasetSchemaVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.EXECUTION_ERROR, e.getReason());
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  /**
   * Update dataset schema description test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateDatasetSchemaDescriptionTest3() throws EEAException {
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenThrow(EEAException.class);
    try {
      dataSchemaControllerImpl.updateDatasetSchemaDescription(1L, datasetSchemaVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.SCHEMA_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
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
    when(dataflowControllerZuul.findById(Mockito.any())).thenReturn(df);
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
    dataSchemaControllerImpl.createUniqueConstraint(uniqueConstraint);
    Mockito.verify(dataschemaService, times(1)).createUniqueConstraint(Mockito.any());
  }

  /**
   * Creates the unique constraint schema id error test.
   */
  @Test(expected = ResponseStatusException.class)
  public void createUniqueConstraintSchemaIdErrorTest() {
    UniqueConstraintVO uniqueConstraint = new UniqueConstraintVO();
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
   * Delete unique constraint id error test.
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteUniqueConstraintIdErrorTest() {
    try {
      dataSchemaControllerImpl.deleteUniqueConstraint(null, null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.IDUNQUECONSTRAINT_INCORRECT, e.getReason());
      throw e;
    }
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
}
