package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.mapper.DataSetNoDataMapper;
import org.eea.dataset.mapper.RecordMapper;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.file.FileParseContextImpl;
import org.eea.dataset.service.file.FileParserFactory;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.kafka.io.KafkaSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;
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
  DataSetNoDataMapper dataSetNoDataMapper;

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

  @Mock
  RecordStoreControllerZull recordStoreControllerZull;

  @Mock
  RecordRepository recordRepository;

  @Mock
  RecordMapper recordMapper;

  @Mock
  Pageable pageable;

  private RecordValue recordValue;
  private ArrayList<RecordValue> recordValues;
  private TableValue tableValue;
  private ArrayList<TableValue> tableValues;
  private DatasetValue datasetValue;
  private DataSetVO dataSetVO;
  private ArrayList<TableVO> tableVOs;
  private TableVO tableVO;

  @Before
  public void initMocks() {
    recordValues = new ArrayList<>();
    recordValue = new RecordValue();
    tableValue = new TableValue();
    tableValue.setId(1L);
    recordValue.setTableValue(tableValue);
    recordValues.add(recordValue);
    datasetValue = new DatasetValue();
    tableValues = new ArrayList<>();
    tableValues.add(tableValue);
    datasetValue.setTableValues(tableValues);
    tableVOs = new ArrayList<>();
    tableVO = new TableVO();
    tableVOs.add(tableVO);
    dataSetVO = new DataSetVO();
    dataSetVO.setTableVO(tableVOs);
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = EEAException.class)
  public void testProcessFileThrowException() throws Exception {
    MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    datasetService.processFile(null, "fileOriginal", fileNoExtension.getInputStream());
  }

  @Test(expected = EEAException.class)
  public void testProcessFilenameNullThrowException() throws Exception {
    MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    datasetService.processFile(null, null, fileNoExtension.getInputStream());
  }

  @Test(expected = EEAException.class)
  public void testProcessFileBadExtensionThrowException() throws Exception {
    MockMultipartFile fileBadExtension =
        new MockMultipartFile("file", "fileOriginal.doc", "doc", "content".getBytes());
    datasetService.processFile(1L, "fileOriginal.doc", fileBadExtension.getInputStream());
  }

  @Test(expected = EEAException.class)
  public void testProcessFileThrowException2() throws Exception {
    MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    datasetService.processFile(1L, "fileOriginal", fileNoExtension.getInputStream());
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

    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream());
  }

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabase() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream());
  }

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabaseXml() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.xml", "xml", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream());
  }

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabaseXls() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.xls", "xls", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream());
  }

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabaseXlsx() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.xlsx", "xlsx", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream());
  }

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyMetabase() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(dataSetMetabaseRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream());
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
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream());
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
    DatasetValue entityValue = new DatasetValue();
    ArrayList<TableValue> tableValues = new ArrayList<TableValue>();
    tableValues.add(new TableValue());
    entityValue.setId(1L);
    entityValue.setTableValues(tableValues);
    when(dataSetMapper.classToEntity(Mockito.any(DataSetVO.class))).thenReturn(entityValue);
    when(datasetRepository.save(Mockito.any())).thenReturn(new DatasetValue());
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream());
  }



  @Test
  public void createEmptyDataset() throws Exception {
    doNothing().when(recordStoreControllerZull).createEmptyDataset(Mockito.any());
    datasetService.createEmptyDataset("");
  }

  @Test
  public void countTableData() throws Exception {
    when(recordRepository.countByTableValue_id(Mockito.any())).thenReturn(20L);
    assertEquals((Long) 20L, datasetService.countTableData(1L));
  }

  @Test
  public void deleteImportData() throws Exception {
    doNothing().when(datasetRepository).deleteById(Mockito.any());
    datasetService.deleteImportData(1L);
  }

  @Test
  public void deleteDataSchema() throws Exception {
    doNothing().when(schemasRepository).deleteById(Mockito.any());
    datasetService.deleteDataSchema(new ObjectId().toString());
  }

  @Test(expected = EEAException.class)
  public void getDatasetValuesByIdNotFoundTest() throws Exception {
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    datasetService.getDatasetValuesById(1L);
  }

  @Test
  public void getDatasetValuesByIdTest() throws Exception {
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(dataSetNoDataMapper.entityToClass(Mockito.any())).thenReturn(dataSetVO);
    when(recordRepository.findByTableValue_id(Mockito.any(), Mockito.any()))
        .thenReturn(recordValues);
    when(recordMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<RecordVO>());
    datasetService.getDatasetValuesById(1L);
  }

  @Test(expected = EEAException.class)
  public void getTableValuesByIdTestEmpty() throws Exception {
    when(recordRepository.findByTableValue_idMongo(Mockito.any(), Mockito.any()))
        .thenReturn(new ArrayList<RecordValue>());
    datasetService.getTableValuesById("mongoId", pageable);
  }

  @Test
  public void getTableValuesByIdTest() throws Exception {
    when(recordRepository.findByTableValue_idMongo(Mockito.any(), Mockito.any()))
        .thenReturn(recordValues);
    when(recordRepository.countByTableValue_id(Mockito.any())).thenReturn(20L);
    when(recordMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<RecordVO>());
    datasetService.getTableValuesById("mongoId", pageable);
  }
}
