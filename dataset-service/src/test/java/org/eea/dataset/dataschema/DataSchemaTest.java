package org.eea.dataset.dataschema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.controller.DataSetSchemaControllerImpl;
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

@RunWith(MockitoJUnitRunner.class)
public class DataSchemaTest {

  @InjectMocks
  DataSetSchemaControllerImpl dataSchemaControllerImpl;

  @Mock
  DataschemaServiceImpl dataschemaService;
  
  @Mock
  SchemasRepository schemasRepository;
  
  @InjectMocks
  DataschemaServiceImpl dataSchemaServiceImpl;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }
  
  @Test
  public void testCreateDataSchema() {
    
    dataSchemaControllerImpl.createDataSchema("test");
    dataSchemaServiceImpl.createDataSchema("test");
  }
  
  @Test
  public void testFindDataSchemaById() {
    when(dataschemaService.getDataSchemaById(Mockito.any())).thenReturn(new DataSetSchemaVO());
    dataSchemaControllerImpl.findDataSchemaById(Mockito.any());
    dataSchemaServiceImpl.getDataSchemaById("5ce524fad31fc52540abae73");
    
    assertEquals(null, schemasRepository.findSchemaByIdFlow(1L));
    
  }
  
  @Test
  public void testFindDataSchemaByDataFlow() {
    when(dataschemaService.getDataSchemaByIdFlow(Mockito.any())).thenReturn(new DataSetSchemaVO());
    dataSchemaControllerImpl.findDataSchemaByDataflow(Mockito.any());
    
    dataSchemaServiceImpl.getDataSchemaByIdFlow(Mockito.any());
 
    
  }
  
  @Test
  public void testSchemaModels() {
    
    FieldSchema field = new FieldSchema();
    field.setHeaderName("test");
    field.setType(TypeData.STRING);
    
    FieldSchema field2 = new FieldSchema();
    field2.setHeaderName("test");
    field2.setType(TypeData.STRING);
  
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
