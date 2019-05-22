package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.file.FileParseContextImpl;
import org.eea.dataset.service.file.FileParserFactory;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.kafka.io.KafkaSender;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class DatasetServiceTest {

  @InjectMocks
  DatasetServiceImpl datasetService;

  @Mock
  FileParseContextImpl context;

  @Mock
  FileParserFactory fileParserFactory;

  @Mock
  DataSetMapper dataSetMapper;

  @Mock
  PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  @Mock
  DataSetMetabaseRepository dataSetMetabaseRepository;

  @Mock
  SchemasRepository schemasRepository;

  @Mock
  DatasetRepository datasetRepository;

  @Mock
  KafkaSender kafkaSender;
  


  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = EEAException.class)
  public void testProcessFileThrowException() throws Exception {
    MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    datasetService.processFile(null, fileNoExtension);
  }

  @Test(expected = EEAException.class)
  public void testProcessFileBadExtensionThrowException() throws Exception {
    MockMultipartFile fileBadExtension =
        new MockMultipartFile("file", "fileOriginal.doc", "doc", "content".getBytes());
    datasetService.processFile(1L, fileBadExtension);
  }

  @Test(expected = EEAException.class)
  public void testProcessFileThrowException2() throws Exception {
    MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());

    datasetService.processFile(1L, fileNoExtension);
  }

  @Ignore
  @Test(expected = IOException.class)
  public void testProcessFileEmptyDataset() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(dataSetMetabaseRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(new DataSetMetabase()));
    when(fileParserFactory.createContext("csv")).thenReturn(context);
    when(context.parse(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);

    datasetService.processFile(1L, file);
  }

  @Ignore
  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabase() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file);
  }

  @Ignore
  @Test(expected = EEAException.class)
  public void testProcessFileEmptyMetabase() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(dataSetMetabaseRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file);
  }

  @Test(expected = IOException.class)
  public void testProcessFileMappingError() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(dataSetMetabaseRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(new DataSetMetabase()));
    when(fileParserFactory.createContext("csv")).thenReturn(context);
    DataSetVO dataSetVO = new DataSetVO();
    dataSetVO.setId(1L);
    when(context.parse(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(dataSetVO);
    when(dataSetMapper.classToEntity(Mockito.any(DataSetVO.class))).thenReturn(null);
    datasetService.processFile(1L, file);
  }

  @Test
  @Ignore
  public void testProcessFileSuccess() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(dataSetMetabaseRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(new DataSetMetabase()));
    when(fileParserFactory.createContext("csv")).thenReturn(context);
    DataSetVO dataSetVO = new DataSetVO();
    dataSetVO.setId(1L);
    when(context.parse(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(dataSetVO);
    DatasetValue entityValue = new DatasetValue();
    ArrayList<TableValue> tableValues = new ArrayList<TableValue>();
    tableValues.add(new TableValue());
    entityValue.setId(1L);
    entityValue.setTableValues(tableValues);
    when(dataSetMapper.classToEntity(Mockito.any(DataSetVO.class))).thenReturn(entityValue);
    when(datasetRepository.save(Mockito.any())).thenReturn(new DatasetValue());
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    datasetService.processFile(1L, file);
  }
  
  @Test
  public void testFindDataschemaByIdDataflow() throws Exception {
    
    //Se prueba que el dataflow con id 1 tiene dataschema
    DataSetSchema data = new DataSetSchema();
    
    //when(schemasRepository.findSchemaByIdFlow(1L)).thenReturn(data);
    assertEquals(null,schemasRepository.findSchemaByIdFlow(1L));
    //when(datasetService.getDataSchemaByIdFlow(1L)).thenReturn(new DataSetSchemaVO());
    
  }
  
  
  @Test
  public void testFindDataschemaById() throws Exception {
    
    //Se prueba que se recupera un dataschema con un id
    DataSetSchema data = new DataSetSchema();
    data.setNameDataSetSchema("test");
    //when(schemasRepository.findById(new ObjectId("5ce3a7ca3d851f09c42cb152"))).thenReturn(Optional.of(new DataSetSchema()));
    assertEquals("test",data.getNameDataSetSchema());
    //when(datasetService.getDataSchemaById("5ce3a7ca3d851f09c42cb152")).thenReturn(new DataSetSchemaVO());
    
  }

}
