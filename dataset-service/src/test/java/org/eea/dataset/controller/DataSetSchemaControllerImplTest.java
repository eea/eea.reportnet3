package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.impl.DataschemaServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.junit.Assert;
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
 * The Class DataSetSchemaControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataSetSchemaControllerImplTest {


  /** The data schema controller impl. */
  @InjectMocks
  private DataSetSchemaControllerImpl dataSchemaControllerImpl;

  /** The dataschema service. */
  @Mock
  private DataschemaServiceImpl dataschemaService;

  /** The dataset service. */
  @Mock
  private DatasetService datasetService;

  /** The dataset metabase service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The record store controller zull. */
  @Mock
  private RecordStoreControllerZull recordStoreControllerZull;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test create data schema.
   */
  @Test
  public void testCreateDataSchema() {
    dataSchemaControllerImpl.createDataSchema(1L, 1L);
    Mockito.verify(dataschemaService, times(1)).createDataSchema(Mockito.any(), Mockito.any());

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
   * Test find data schema by id exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testFindDataSchemaByIdException() throws EEAException {

    Mockito.doThrow(EEAException.class).when(dataschemaService).getDatasetSchemaId(Mockito.any());

    assertNotNull("failed", dataSchemaControllerImpl.getDatasetSchemaId(1L));

  }

  /**
   * Find data schema by dataset id exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void findDataSchemaByDatasetIdException() throws EEAException {

    Mockito.doThrow(EEAException.class).when(dataschemaService)
        .getDataSchemaByDatasetId(Mockito.any(), Mockito.any());

    assertNotNull("failed", dataSchemaControllerImpl.findDataSchemaByDatasetId(1L));

  }


  /**
   * Test find data schema by data flow.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testFindDataSchemaByDataFlow() throws EEAException {

    Mockito
        .when(dataschemaService.getDataSchemaByDatasetId(Mockito.eq(Boolean.TRUE), Mockito.any()))
        .thenReturn(new DataSetSchemaVO());
    DataSetSchemaVO result = dataSchemaControllerImpl.findDataSchemaByDatasetId(1L);
    Assert.assertNotNull(result);


  }

  /**
   * Test find data schema with no rules by dataflow.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testFindDataSchemaWithNoRulesByDataflow() throws EEAException {

    Mockito
        .when(dataschemaService.getDataSchemaByDatasetId(Mockito.eq(Boolean.FALSE), Mockito.any()))
        .thenReturn(new DataSetSchemaVO());
    Assert.assertNotNull(dataSchemaControllerImpl.findDataSchemaWithNoRulesByDatasetId(1L));
  }


  /**
   * Test find data schema with no rules by dataflow exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testFindDataSchemaWithNoRulesByDataflowException() throws EEAException {

    Mockito.doThrow(EEAException.class).when(dataschemaService)
        .getDataSchemaByDatasetId(Mockito.any(), Mockito.any());
    Assert.assertNotNull(dataSchemaControllerImpl.findDataSchemaWithNoRulesByDatasetId(1L));
  }

  /**
   * Test schema models.
   */
  @Test
  public void testSchemaModels() {

    FieldSchema field = new FieldSchema();
    field.setHeaderName("test");
    field.setType(TypeData.TEXT);

    FieldSchema field2 = new FieldSchema();
    field2.setHeaderName("test");
    field2.setType(TypeData.TEXT);

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
    schema.setNameDataSetSchema("test");
    schema.setIdDataFlow(1L);
    List<TableSchema> listaTables = new ArrayList<>();
    listaTables.add(table);
    schema.setTableSchemas(listaTables);

    DataSetSchema schema2 = new DataSetSchema();
    schema2.setNameDataSetSchema("test");
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
    Mockito.when(dataschemaService.createEmptyDataSetSchema(Mockito.any(), Mockito.any()))
        .thenReturn(new ObjectId());
    Mockito.when(datasetMetabaseService.createEmptyDataset(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(1L);
    Mockito.doNothing().when(dataschemaService).createGroupAndAddUser(Mockito.any());
    dataSchemaControllerImpl.createEmptyDatasetSchema(1L, "datasetSchemaName");
    Mockito.verify(dataschemaService, times(1)).createGroupAndAddUser(Mockito.any());
  }

  /**
   * Creates the empty data set schema exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetSchemaException() throws EEAException {
    Mockito.doThrow(EEAException.class).when(datasetMetabaseService)
        .createEmptyDataset(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.when(dataschemaService.createEmptyDataSetSchema(Mockito.any(), Mockito.any()))
        .thenReturn(new ObjectId());
    dataSchemaControllerImpl.createEmptyDatasetSchema(1L, "datasetSchemaName");
  }

  /**
   * Delete table schema test.
   */
  @Test
  public void deleteTableSchemaTest() {
    doNothing().when(dataschemaService).deleteTableSchema(Mockito.any());
    dataSchemaControllerImpl.deleteTableSchema(1L, "objectId");
    Mockito.verify(datasetService, times(1)).deleteTableValue(Mockito.any(), Mockito.any());
  }

  /**
   * Delete table schema exception test.
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteTableSchemaExceptionTest() {
    dataSchemaControllerImpl.deleteTableSchema(null, null);
  }

  /**
   * Delete dataset schema exception test.
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDatasetSchemaExceptionTest() {
    dataSchemaControllerImpl.deleteDatasetSchema(null);
  }

  /**
   * Delete dataset schema exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDatasetSchemaException2Test() throws EEAException {
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    dataSchemaControllerImpl.deleteDatasetSchema(1L);
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
    dataSchemaControllerImpl.deleteDatasetSchema(1L);

    Mockito.verify(recordStoreControllerZull, times(1)).deleteDataset(Mockito.any());
  }

  /**
   * Update table schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateTableSchemaTest() throws EEAException {
    dataSchemaControllerImpl.updateTableSchema("", 1L, new TableSchemaVO());
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
    dataSchemaControllerImpl.updateTableSchema("", 1L, new TableSchemaVO());
  }

  /**
   * Creates the table schema test.
   */
  @Test
  public void createTableSchemaTest() {
    dataSchemaControllerImpl.createTableSchema("", 1L, new TableSchemaVO());
    Mockito.verify(dataschemaService, times(1)).createTableSchema(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Creates the table schema test exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createTableSchemaTestException() throws EEAException {
    doThrow(EEAException.class).when(datasetService).saveTablePropagation(Mockito.any(),
        Mockito.any());
    dataSchemaControllerImpl.createTableSchema("", 1L, new TableSchemaVO());
  }

  /**
   * Creates the field schema test exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createFieldSchemaTestException() throws EEAException {
    doThrow(EEAException.class).when(dataschemaService).createFieldSchema(Mockito.any(),
        Mockito.any(), Mockito.any());
    dataSchemaControllerImpl.createFieldSchema("", 1L, new FieldSchemaVO());
  }

  /**
   * Creates the field schema success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createFieldSchemaSuccessTest() throws EEAException {
    dataSchemaControllerImpl.createFieldSchema("", 1L, new FieldSchemaVO());
    Mockito.verify(dataschemaService, times(1)).createFieldSchema(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Delete field schema test.
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteFieldSchemaTest() {
    Mockito.when(datasetService.deleteFieldValues(Mockito.any(), Mockito.any())).thenReturn("<id>");
    Mockito.when(dataschemaService.deleteFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(false);
    dataSchemaControllerImpl.deleteFieldSchema(1L, "<id>");
  }
}
