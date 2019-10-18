package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.impl.DataschemaServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
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

@RunWith(MockitoJUnitRunner.class)
public class DataSetSchemaControllerImplTest {

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }


  @InjectMocks
  private DataSetSchemaControllerImpl dataSchemaControllerImpl;

  @Mock
  private DataschemaServiceImpl dataschemaService;

  @Mock
  private SchemasRepository schemasRepository;

  @Mock
  private DatasetSchemaController dataSchemaController;

  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  @InjectMocks
  private DataschemaServiceImpl dataSchemaServiceImpl;

  @Test
  public void testCreateDataSchema() {

    dataSchemaControllerImpl.createDataSchema(1L, 1L);
    dataSchemaController.createDataSchema(1L, 1L);
    Mockito.verify(dataSchemaController, times(1)).createDataSchema(Mockito.any(), Mockito.any());

  }

  /**
   * Test find data schema by id.
   */
  @Test
  public void testFindDataSchemaById() {
    when(dataschemaService.getDataSchemaById(Mockito.any())).thenReturn(new DataSetSchemaVO());
    dataSchemaControllerImpl.findDataSchemaById(Mockito.any());

    assertNull("failed", schemasRepository.findSchemaByIdFlow(1L));

  }

  @Test
  public void testFindDataSchemaByDataFlow() {

    Mockito.when(dataschemaService.getDataSchemaByIdFlow(Mockito.eq(1l), Mockito.eq(Boolean.TRUE)))
        .thenReturn(new DataSetSchemaVO());
    DataSetSchemaVO result = dataSchemaControllerImpl.findDataSchemaByDataflow(1l);
    Assert.assertNotNull(result);


  }

  @Test
  public void testFindDataSchemaWithNoRulesByDataflow() {

    Mockito.when(dataschemaService.getDataSchemaByIdFlow(Mockito.eq(1l), Mockito.eq(Boolean.FALSE)))
        .thenReturn(new DataSetSchemaVO());
    DataSetSchemaVO result = dataSchemaControllerImpl.findDataSchemaWithNoRulesByDataflow(1l);
    Assert.assertNotNull(result);
  }

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

  @Test
  public void createEmptyDataSetSchemaTest() throws EEAException {
    Mockito.doNothing().when(datasetMetabaseService).createEmptyDataset(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.when(dataschemaService.createEmptyDataSetSchema(Mockito.any(), Mockito.any()))
        .thenReturn(new ObjectId());
    dataSchemaControllerImpl.createEmptyDataSetSchema("nameDataSetSchema", 1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void createEmptyDataSetSchemaException() throws EEAException {
    Mockito.doThrow(EEAException.class).when(datasetMetabaseService)
        .createEmptyDataset(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.when(dataschemaService.createEmptyDataSetSchema(Mockito.any(), Mockito.any()))
        .thenReturn(new ObjectId());
    dataSchemaControllerImpl.createEmptyDataSetSchema("nameDataSetSchema", 1L);
  }
}
