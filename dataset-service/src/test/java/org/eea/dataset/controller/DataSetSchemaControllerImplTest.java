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


  /**
   * The data schema controller impl.
   */
  @InjectMocks
  private DataSetSchemaControllerImpl dataSchemaControllerImpl;

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
   * The record store controller zull.
   */
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
    dataSchemaControllerImpl.getDatasetSchemaId(1L);
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
    dataSchemaControllerImpl.findDataSchemaByDatasetId(1L);
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
    dataSchemaControllerImpl.findDataSchemaWithNoRulesByDatasetId(1L);
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
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
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
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    doThrow(EEAException.class).when(dataschemaService).updateTableSchema(Mockito.any(),
        Mockito.any());
    dataSchemaControllerImpl.updateTableSchema(1L, new TableSchemaVO());
  }

  /**
   * Delete table schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteTableSchemaTest1() throws EEAException {
    Mockito.doNothing().when(dataschemaService).deleteTableSchema(Mockito.any(), Mockito.any());
    Mockito.doNothing().when(datasetService).deleteTableValue(Mockito.any(), Mockito.any());
    dataSchemaControllerImpl.deleteTableSchema(1L, "");
  }

  /**
   * Delete table schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteTableSchemaTest2() throws EEAException {
    Mockito.doThrow(EEAException.class).when(dataschemaService).deleteTableSchema(Mockito.any(),
        Mockito.any());
    dataSchemaControllerImpl.deleteTableSchema(1L, "");
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
    dataSchemaControllerImpl.orderTableSchema(1L, "", 1);
  }

  /**
   * Order table schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void orderTableSchemaTest2() throws EEAException {
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.orderTableSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    dataSchemaControllerImpl.orderTableSchema(1L, "", 1);
  }

  /**
   * Creates the field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createFieldSchemaTest1() throws EEAException {
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.createFieldSchema(Mockito.any(), Mockito.any()))
        .thenThrow(EEAException.class);
    dataSchemaControllerImpl.createFieldSchema(1L, new FieldSchemaVO());
  }

  /**
   * Creates the field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createFieldSchemaTest2() throws EEAException {
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.createFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(false);
    dataSchemaControllerImpl.createFieldSchema(1L, new FieldSchemaVO());
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
        .thenReturn(true);
    dataSchemaControllerImpl.createFieldSchema(1L, new FieldSchemaVO());
  }

  /**
   * Update field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest1() throws EEAException {
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.updateFieldSchema(Mockito.any(), Mockito.any())).thenReturn("");
    Mockito.doNothing().when(datasetService).updateFieldValueType(Mockito.any(), Mockito.any(),
        Mockito.any());
    dataSchemaControllerImpl.updateFieldSchema(1L, new FieldSchemaVO());
  }

  /**
   * Update field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest2() throws EEAException {
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(null);
    dataSchemaControllerImpl.updateFieldSchema(1L, new FieldSchemaVO());
  }

  /**
   * Update field schema test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateFieldSchemaTest3() throws EEAException {
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenThrow(EEAException.class);
    dataSchemaControllerImpl.updateFieldSchema(1L, new FieldSchemaVO());
  }

  /**
   * Delete field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteFieldSchemaTest1() throws EEAException {
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.deleteFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(true);
    Mockito.doNothing().when(datasetService).deleteFieldValues(Mockito.any(), Mockito.any());
    dataSchemaControllerImpl.deleteFieldSchema(1L, "");
  }

  /**
   * Delete field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteFieldSchemaTest2() throws EEAException {
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.deleteFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(false);
    dataSchemaControllerImpl.deleteFieldSchema(1L, "");
  }

  /**
   * Delete field schema test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteFieldSchemaTest3() throws EEAException {
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.deleteFieldSchema(Mockito.any(), Mockito.any()))
        .thenThrow(EEAException.class);
    dataSchemaControllerImpl.deleteFieldSchema(1L, "");
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
    dataSchemaControllerImpl.orderFieldSchema(1L, new FieldSchemaVO(), 1);
  }

  /**
   * Order field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void orderFieldSchemaTest2() throws EEAException {
    Mockito.when(dataschemaService.getDatasetSchemaId(Mockito.any())).thenReturn("");
    Mockito.when(dataschemaService.orderFieldSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    dataSchemaControllerImpl.orderFieldSchema(1L, new FieldSchemaVO(), 1);
  }

  /**
   * Creates the table schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createTableSchemaTest1() throws EEAException {
    Mockito.doNothing().when(dataschemaService).createTableSchema(Mockito.any(), Mockito.any(),
        Mockito.any());
    Mockito.doNothing().when(datasetService).saveTablePropagation(Mockito.any(), Mockito.any());
    dataSchemaControllerImpl.createTableSchema(1L, new TableSchemaVO());
    Mockito.verify(dataschemaService, times(1)).createTableSchema(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Creates the table schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createTableSchemaTest2() throws EEAException {
    Mockito.doNothing().when(dataschemaService).createTableSchema(Mockito.any(), Mockito.any(),
        Mockito.any());
    Mockito.doThrow(EEAException.class).when(datasetService).saveTablePropagation(Mockito.any(),
        Mockito.any());
    dataSchemaControllerImpl.createTableSchema(1L, new TableSchemaVO());
  }
}
