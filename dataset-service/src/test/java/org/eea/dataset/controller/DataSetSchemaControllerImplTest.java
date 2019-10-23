package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.impl.DataschemaServiceImpl;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
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
   * Test find data schema by data flow.
   */
  @Test
  public void testFindDataSchemaByDataFlow() {

    Mockito.when(dataschemaService.getDataSchemaByIdFlow(Mockito.eq(1l), Mockito.eq(Boolean.TRUE)))
        .thenReturn(new DataSetSchemaVO());
    DataSetSchemaVO result = dataSchemaControllerImpl.findDataSchemaByDataflow(1l);
    Assert.assertNotNull(result);


  }

  /**
   * Test find data schema with no rules by dataflow.
   */
  @Test
  public void testFindDataSchemaWithNoRulesByDataflow() {

    Mockito.when(dataschemaService.getDataSchemaByIdFlow(Mockito.eq(1l), Mockito.eq(Boolean.FALSE)))
        .thenReturn(new DataSetSchemaVO());
    DataSetSchemaVO result = dataSchemaControllerImpl.findDataSchemaWithNoRulesByDataflow(1l);
    Assert.assertNotNull(result);


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
    dataSchemaControllerImpl.deleteDatasetSchema(null, null);
  }

  /**
   * Delete dataset schema exception 2 test.
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDatasetSchemaException2Test() {
    dataSchemaControllerImpl.deleteDatasetSchema(null, "schema");
  }

  /**
   * Delete dataset schema exception 3 test.
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteDatasetSchemaException3Test() {
    dataSchemaControllerImpl.deleteDatasetSchema(1L, null);
  }

  /**
   * Delete dataset schema success.
   */
  @Test
  public void deleteDatasetSchemaSuccess() {
    doNothing().when(dataschemaService).deleteDatasetSchema(Mockito.any(), Mockito.any());
    doNothing().when(datasetMetabaseService).deleteDesignDataset(Mockito.any());
    dataSchemaControllerImpl.deleteDatasetSchema(1L, "schema");

    Mockito.verify(recordStoreControllerZull, times(1)).deleteDataset(Mockito.any());
  }
}
