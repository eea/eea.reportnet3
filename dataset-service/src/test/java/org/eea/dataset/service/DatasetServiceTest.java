package org.eea.dataset.service;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Optional;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.persistence.data.domain.Dataset;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.file.FileParseContextImpl;
import org.eea.dataset.service.file.FileParserFactory;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.kafka.io.KafkaSender;
import org.junit.Before;
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
  public void testProcessFileThrowException2() throws Exception {
    MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());

    datasetService.processFile(1L, fileNoExtension);
  }

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

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabase() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file);
  }

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyMetabase() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(dataSetMetabaseRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file);
  }

  @Test
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
    Dataset entityValue = new Dataset();
    when(dataSetMapper.classToEntity(Mockito.any(DataSetVO.class))).thenReturn(entityValue);
    when(datasetRepository.save(Mockito.any())).thenReturn(new Dataset());
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    datasetService.processFile(1L, file);
  }

}
