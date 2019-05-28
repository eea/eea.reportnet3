package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSchemaMapper;
import org.eea.dataset.persistence.metabase.domain.TableCollection;
import org.eea.dataset.persistence.metabase.domain.TableHeadersCollection;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseTableCollection;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.impl.DataschemaServiceImpl;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class DatasetSchemaServiceTest {

  @Mock
  DataschemaServiceImpl dataschemaService;

  @Mock
  SchemasRepository schemasRepository;

  @Mock
  DataSetMetabaseTableCollection dataSetMetabaseTableCollection;

  @InjectMocks
  DataschemaServiceImpl dataSchemaServiceImpl;

  @Mock
  DataSchemaMapper dataSchemaMapper;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCreateDataSchema() {

    DataSetSchema dataSetSchema = new DataSetSchema();
    dataSchemaServiceImpl.createDataSchema(1L);
    Iterable<TableCollection> tables = dataSetMetabaseTableCollection.findAllByDataSetId(1L);
    ArrayList<TableCollection> values = Lists.newArrayList(tables);
    List<TableSchema> tableSchemas = new ArrayList<>();
    dataSetSchema.setNameDataSetSchema("dataSet_" + 1);
    dataSetSchema.setIdDataFlow(1L);
    for (int i = 1; i <= values.size(); i++) {
      TableCollection table = values.get(i - 1);
      TableSchema tableSchema = new TableSchema();
      tableSchema.setIdTableSchema(new ObjectId());

      tableSchema.setNameTableSchema(table.getTableName());

      RecordSchema recordSchema = new RecordSchema();
      recordSchema.setIdRecordSchema(new ObjectId());
      recordSchema.setIdTableSchema(tableSchema.getIdTableSchema());

      List<FieldSchema> fieldSchemas = new ArrayList<>();

      int headersSize = table.getTableHeadersCollections().size();
      for (int j = 1; j <= headersSize; j++) {
        TableHeadersCollection header = table.getTableHeadersCollections().get(j - 1);
        FieldSchema fieldSchema = new FieldSchema();
        fieldSchema = new FieldSchema();
        fieldSchema.setIdFieldSchema(new ObjectId());
        fieldSchema.setIdRecord(recordSchema.getIdRecordSchema());
        fieldSchema.setHeaderName(header.getHeaderName());
        fieldSchema.setType(header.getHeaderType());
        fieldSchemas.add(fieldSchema);
      }
      recordSchema.setFieldSchema(fieldSchemas);
      tableSchema.setRecordSchema(recordSchema);
      tableSchemas.add(tableSchema);
    }
    dataSetSchema.setTableSchemas(tableSchemas);
    schemasRepository.save(dataSetSchema);

  }



  @Test
  public void testFindDataSchemaById() {

    dataSchemaServiceImpl.getDataSchemaById("5ce524fad31fc52540abae73");

    assertEquals(null, schemasRepository.findSchemaByIdFlow(1L));

  }

  @Test
  public void testFindDataSchemaByDataFlow() {
    dataSchemaServiceImpl.getDataSchemaByIdFlow(Mockito.any());

  }

  @Test
  public void testFindDataSchemaNotNullById() {
    when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(new DataSetSchema()));
    when(dataSchemaMapper.entityToClass((DataSetSchema) Mockito.any()))
        .thenReturn(new DataSetSchemaVO());
    dataSchemaServiceImpl.getDataSchemaById("5ce524fad31fc52540abae73");

    assertEquals(null, schemasRepository.findSchemaByIdFlow(1L));

  }

  @Test
  public void testFindDataSchemaNotNullByDataFlow() {
    when(schemasRepository.findSchemaByIdFlow(Mockito.any())).thenReturn(new DataSetSchema());
    when(dataSchemaMapper.entityToClass((DataSetSchema) Mockito.any()))
        .thenReturn(new DataSetSchemaVO());
    dataSchemaServiceImpl.getDataSchemaByIdFlow(Mockito.any());

  }


  @Test
  public void testSchemaModels() {

    FieldSchema field = new FieldSchema();
    field.setHeaderName("test");
    field.setType("string");

    FieldSchema field2 = new FieldSchema();
    field2.setHeaderName("test");
    field2.setType("string");

    assertTrue(field.equals(field2));

    RecordSchema record = new RecordSchema();
    record.setNameSchema("test");
    List<FieldSchema> listaFields = new ArrayList<FieldSchema>();
    listaFields.add(field);
    record.setFieldSchema(listaFields);

    RecordSchema record2 = new RecordSchema();
    record2.setNameSchema("test");
    record2.setFieldSchema(listaFields);

    assertTrue(record.equals(record2));

    TableSchema table = new TableSchema();
    table.setNameTableSchema("test");
    table.setRecordSchema(record);

    TableSchema table2 = new TableSchema();
    table2.setNameTableSchema("test");
    table2.setRecordSchema(record2);

    assertTrue(table.equals(table2));

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

    assertTrue(schema.equals(schema2));


  }



}
