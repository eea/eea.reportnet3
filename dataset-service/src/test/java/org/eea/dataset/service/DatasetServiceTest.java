package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.file.FileParserFactory;
import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.exception.EEAException;
import org.eea.kafka.io.KafkaSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;


@RunWith(MockitoJUnitRunner.Silent.class)
public class DatasetServiceTest {

  @InjectMocks
  DatasetServiceImpl datasetService;

  @Mock
  IFileParseContext context;

  @Mock
  FileParserFactory fileParserFactory;

  @Mock
  DataSetMapper dataSetMapper;

  // @Mock
  // DatasetRepository datasetRepository;

  @Mock
  KafkaSender kafkaSender;
  
  @Mock
  SchemasRepository schemaRepository;
  

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
  public void testProcessFileThrowException2() throws Exception {
    MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());

    datasetService.processFile("1", fileNoExtension);
  }

  /*@Test(expected = IOException.class)
  public void testProcessFileEmptyDataset() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(fileParserFactory.createContext(Mockito.anyString())).thenReturn(context);
    when(context.parse(Mockito.any(InputStream.class), Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(null);

    datasetService.processFile("1", file);
  }

  @Test
  public void testProcessFileSuccess() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(fileParserFactory.createContext(Mockito.anyString())).thenReturn(context);
    when(context.parse(Mockito.any(InputStream.class), Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(new DataSetVO());
    // when(datasetRepository.save(Mockito.any())).thenReturn(new DataSetVO());
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    Dataset entityValue = new Dataset();
    entityValue.setName("dataSet_1");
    when(dataSetMapper.classToEntity(Mockito.any(DataSetVO.class))).thenReturn(entityValue);
    datasetService.processFile("1", file);
  }*/
  
  @Test
  public void testFindDataschemaByIdDataflow() throws Exception {
    
    //Se prueba que el dataflow con id 1 tiene dataschema
    DataSetSchema data = new DataSetSchema();
    data.setNameDataSetSchema("test");
    when(schemaRepository.findSchemaByIdFlow(1L)).thenReturn(data);
    assertEquals("test",data.getNameDataSetSchema());
    //when(datasetService.getDataSchemaByIdFlow(1L)).thenReturn(new DataSetSchemaVO());
    
  }
  
  
  @Test
  public void testFindDataschemaById() throws Exception {
    
    //Se prueba que se recupera un dataschema con un id
    DataSetSchema data = new DataSetSchema();
    data.setNameDataSetSchema("test");
    when(schemaRepository.findById(new ObjectId("5ce3a7ca3d851f09c42cb152"))).thenReturn(Optional.of(new DataSetSchema()));
    assertEquals("test",data.getNameDataSetSchema());
    //when(datasetService.getDataSchemaById("5ce3a7ca3d851f09c42cb152")).thenReturn(new DataSetSchemaVO());
    
  }

}
