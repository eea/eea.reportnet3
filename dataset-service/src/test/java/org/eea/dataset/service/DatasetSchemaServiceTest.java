package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eea.dataset.mapper.DataSchemaMapper;
import org.eea.dataset.persistence.metabase.domain.TableCollection;
import org.eea.dataset.persistence.metabase.domain.TableHeadersCollection;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseTableRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.impl.DataschemaServiceImpl;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class DatasetSchemaServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetSchemaServiceTest {

  /** The dataschema service. */
  @Mock
  private DataschemaServiceImpl dataschemaService;

  /** The schemas repository. */
  @Mock
  private SchemasRepository schemasRepository;

  /** The data set metabase table collection. */
  @Mock
  private DataSetMetabaseTableRepository dataSetMetabaseTableCollection;

  /** The data schema service impl. */
  @InjectMocks
  private DataschemaServiceImpl dataSchemaServiceImpl;

  /** The data schema mapper. */
  @Mock
  private DataSchemaMapper dataSchemaMapper;

  /** The tables. */
  private List<TableCollection> tables;

  /** The data set schema. */
  private DataSetSchema dataSetSchema;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    tables = new ArrayList<>();
    dataSetSchema = new DataSetSchema();

    TableCollection table = new TableCollection();
    TableHeadersCollection header = new TableHeadersCollection();
    List<TableHeadersCollection> headers = new ArrayList<>();

    header.setHeaderName("test");
    header.setId(1L);
    // header.setTableId(1L);
    header.setHeaderType(TypeData.TEXT);
    headers.add(header);

    table.setId(1L);
    table.setDataFlowId(1L);
    table.setDataSetId(1L);
    table.setTableName("test");
    table.setTableHeadersCollections(headers);
    tables.add(table);

    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test create data schema.
   */
  @Test
  public void testCreateDataSchema() {

    when(dataSetMetabaseTableCollection.findAllByDataSetId(Mockito.any())).thenReturn(tables);;
    dataSchemaServiceImpl.createDataSchema(1L);
    dataschemaService.createDataSchema(1L);
    Mockito.verify(dataschemaService, times(1)).createDataSchema(Mockito.any());
  }


  @Test
  public void testCreateDataSchemaInteger() {

    List<TableCollection> tables2 = new ArrayList<>();

    TableCollection table = new TableCollection();
    TableHeadersCollection header = new TableHeadersCollection();
    List<TableHeadersCollection> headers = new ArrayList<>();

    header.setHeaderName("test");
    header.setId(1L);
    header.setHeaderType(TypeData.NUMBER);
    headers.add(header);

    table.setId(1L);
    table.setDataFlowId(1L);
    table.setDataSetId(1L);
    table.setTableName("test");
    table.setTableHeadersCollections(headers);
    tables2.add(table);

    when(dataSetMetabaseTableCollection.findAllByDataSetId(Mockito.any())).thenReturn(tables2);
    dataSchemaServiceImpl.createDataSchema(1L);
    dataschemaService.createDataSchema(1L);
    Mockito.verify(dataschemaService, times(1)).createDataSchema(Mockito.any());
  }


  @Test
  public void testCreateDataSchemaBoolean() {

    List<TableCollection> tables2 = new ArrayList<>();

    TableCollection table = new TableCollection();
    TableHeadersCollection header = new TableHeadersCollection();
    List<TableHeadersCollection> headers = new ArrayList<>();

    header.setHeaderName("test");
    header.setId(1L);
    header.setHeaderType(TypeData.BOOLEAN);
    headers.add(header);

    table.setId(1L);
    table.setDataFlowId(1L);
    table.setDataSetId(1L);
    table.setTableName("test");
    table.setTableHeadersCollections(headers);
    tables2.add(table);

    when(dataSetMetabaseTableCollection.findAllByDataSetId(Mockito.any())).thenReturn(tables2);
    dataSchemaServiceImpl.createDataSchema(1L);
    dataschemaService.createDataSchema(1L);
    Mockito.verify(dataschemaService, times(1)).createDataSchema(Mockito.any());
  }



  @Test
  public void testCreateDataSchemaCoordinateLat() {

    List<TableCollection> tables2 = new ArrayList<>();

    TableCollection table = new TableCollection();
    TableHeadersCollection header = new TableHeadersCollection();
    List<TableHeadersCollection> headers = new ArrayList<>();

    header.setHeaderName("test");
    header.setId(1L);
    header.setHeaderType(TypeData.COORDINATE_LAT);
    headers.add(header);

    table.setId(1L);
    table.setDataFlowId(1L);
    table.setDataSetId(1L);
    table.setTableName("test");
    table.setTableHeadersCollections(headers);
    tables2.add(table);

    when(dataSetMetabaseTableCollection.findAllByDataSetId(Mockito.any())).thenReturn(tables2);
    dataSchemaServiceImpl.createDataSchema(1L);
    dataschemaService.createDataSchema(1L);
    Mockito.verify(dataschemaService, times(1)).createDataSchema(Mockito.any());
  }

  @Test
  public void testCreateDataSchemaCoordinateLong() {

    List<TableCollection> tables2 = new ArrayList<>();

    TableCollection table = new TableCollection();
    TableHeadersCollection header = new TableHeadersCollection();
    List<TableHeadersCollection> headers = new ArrayList<>();

    header.setHeaderName("test");
    header.setId(1L);
    header.setHeaderType(TypeData.COORDINATE_LONG);
    headers.add(header);

    table.setId(1L);
    table.setDataFlowId(1L);
    table.setDataSetId(1L);
    table.setTableName("test");
    table.setTableHeadersCollections(headers);
    tables2.add(table);

    when(dataSetMetabaseTableCollection.findAllByDataSetId(Mockito.any())).thenReturn(tables2);
    dataSchemaServiceImpl.createDataSchema(1L);
    dataschemaService.createDataSchema(1L);
    Mockito.verify(dataschemaService, times(1)).createDataSchema(Mockito.any());
  }

  @Test
  public void testCreateDataSchemaCoordinateDate() {

    List<TableCollection> tables2 = new ArrayList<>();

    TableCollection table = new TableCollection();
    TableHeadersCollection header = new TableHeadersCollection();
    List<TableHeadersCollection> headers = new ArrayList<>();

    header.setHeaderName("test");
    header.setId(1L);
    header.setHeaderType(TypeData.DATE);
    headers.add(header);

    table.setId(1L);
    table.setDataFlowId(1L);
    table.setDataSetId(1L);
    table.setTableName("test");
    table.setTableHeadersCollections(headers);
    tables2.add(table);

    when(dataSetMetabaseTableCollection.findAllByDataSetId(Mockito.any())).thenReturn(tables2);
    dataSchemaServiceImpl.createDataSchema(1L);
    dataschemaService.createDataSchema(1L);
    Mockito.verify(dataschemaService, times(1)).createDataSchema(Mockito.any());
  }

  /**
   * Test find data schema by id.
   */
  @Test
  public void testFindDataSchemaById() {

    dataSchemaServiceImpl.getDataSchemaById("5ce524fad31fc52540abae73");

    assertNull("fail", schemasRepository.findSchemaByIdFlow(1L));

  }

  /**
   * Test find data schema by data flow.
   */
  @Test
  public void testFindDataSchemaByDataFlow() {
    dataSchemaServiceImpl.getDataSchemaByIdFlow(Mockito.any());
    dataschemaService.getDataSchemaByIdFlow(1L);
    Mockito.verify(dataschemaService, times(1)).getDataSchemaByIdFlow(Mockito.any());

  }

  /**
   * Test find data schema not null by id.
   */
  @Test
  public void testFindDataSchemaNotNullById() {
    when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(new DataSetSchema()));
    when(dataSchemaMapper.entityToClass((DataSetSchema) Mockito.any()))
        .thenReturn(new DataSetSchemaVO());
    dataSchemaServiceImpl.getDataSchemaById("5ce524fad31fc52540abae73");

    assertNull("fail", schemasRepository.findSchemaByIdFlow(1L));

  }

  /**
   * Test find data schema not null by data flow.
   */
  @Test
  public void testFindDataSchemaNotNullByDataFlow() {
    when(schemasRepository.findSchemaByIdFlow(Mockito.any())).thenReturn(new DataSetSchema());
    when(dataSchemaMapper.entityToClass((DataSetSchema) Mockito.any()))
        .thenReturn(new DataSetSchemaVO());
    dataSchemaServiceImpl.getDataSchemaByIdFlow(Mockito.any());
    dataschemaService.getDataSchemaByIdFlow(1L);
    Mockito.verify(dataschemaService, times(1)).getDataSchemaByIdFlow(Mockito.any());

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

    assertEquals("Not equals", field, field2);

    RecordSchema record = new RecordSchema();
    record.setNameSchema("test");
    List<FieldSchema> listaFields = new ArrayList<FieldSchema>();
    listaFields.add(field);
    record.setFieldSchema(listaFields);

    RecordSchema record2 = new RecordSchema();
    record2.setNameSchema("test");
    record2.setFieldSchema(listaFields);

    assertEquals("Not equals", record, record2);

    TableSchema table = new TableSchema();
    table.setNameTableSchema("test");
    table.setRecordSchema(record);

    TableSchema table2 = new TableSchema();
    table2.setNameTableSchema("test");
    table2.setRecordSchema(record2);

    assertEquals("Not equals", table, table2);

    DataSetSchema schema = new DataSetSchema();
    schema.setNameDataSetSchema("test");
    schema.setIdDataFlow(1L);
    List<TableSchema> listaTables = new ArrayList<TableSchema>();
    listaTables.add(table);
    schema.setTableSchemas(listaTables);


    DataSetSchema schema2 = new DataSetSchema();
    schema2.setNameDataSetSchema("test");
    schema2.setIdDataFlow(1L);
    schema2.setTableSchemas(listaTables);

    assertEquals("Not equals", schema, schema2);


  }



}
