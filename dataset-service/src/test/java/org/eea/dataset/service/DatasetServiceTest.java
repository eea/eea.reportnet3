package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.mapper.DataSetTablesMapper;
import org.eea.dataset.mapper.RecordMapper;
import org.eea.dataset.mapper.RecordNoValidationMapper;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.TableCollection;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseTableRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.file.FileParseContextImpl;
import org.eea.dataset.service.file.FileParserFactory;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.metabase.TableCollectionVO;
import org.eea.kafka.io.KafkaSender;
import org.junit.Assert;
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
  private DatasetServiceImpl datasetService;

  @Mock
  private FileParseContextImpl context;

  @Mock
  private FileParserFactory fileParserFactory;

  @Mock
  private DataSetMapper dataSetMapper;

  @Mock
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  @Mock
  private SchemasRepository schemasRepository;

  @Mock
  private DatasetRepository datasetRepository;

  @Mock
  private TableRepository tableRepository;

  @Mock
  private KafkaSender kafkaSender;

  @Mock
  private RecordStoreControllerZull recordStoreControllerZull;

  @Mock
  private RecordRepository recordRepository;

  @Mock
  private RecordMapper recordMapper;
  
  @Mock
  private RecordNoValidationMapper recordNoValidationMapper;

  @Mock
  private Pageable pageable;

  @Mock
  private DataSetMetabaseTableRepository dataSetMetabaseTableCollection;

  @Mock
  private DataSetTablesMapper dataSetTablesMapper;
  
  @Mock
  private FieldRepository fieldRepository;

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
    tableValue.setTableValidations(new ArrayList<>());
    recordValue.setTableValue(tableValue);
    recordValues.add(recordValue);
    datasetValue = new DatasetValue();
    tableValues = new ArrayList<>();
    tableValues.add(tableValue);
    datasetValue.setTableValues(tableValues);
    datasetValue.setIdDatasetSchema("5cf0e9b3b793310e9ceca190");
    datasetValue.setDatasetValidations(new ArrayList<>());
    tableVOs = new ArrayList<>();
    tableVO = new TableVO();
    tableVOs.add(tableVO);
    dataSetVO = new DataSetVO();
    dataSetVO.setTableVO(tableVOs);
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = EEAException.class)
  public void testProcessFileThrowException() throws Exception {
    final MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    datasetService.processFile(null, "fileOriginal", fileNoExtension.getInputStream());
  }

  @Test(expected = EEAException.class)
  public void testProcessFilenameNullThrowException() throws Exception {
    final MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    datasetService.processFile(null, null, fileNoExtension.getInputStream());
  }

  @Test(expected = EEAException.class)
  public void testProcessFileBadExtensionThrowException() throws Exception {
    final MockMultipartFile fileBadExtension =
        new MockMultipartFile("file", "fileOriginal.doc", "doc", "content".getBytes());
    datasetService.processFile(1L, "fileOriginal.doc", fileBadExtension.getInputStream());
  }

  @Test(expected = EEAException.class)
  public void testProcessFileThrowException2() throws Exception {
    final MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    datasetService.processFile(1L, "fileOriginal", fileNoExtension.getInputStream());
  }

  @Test(expected = IOException.class)
  public void testProcessFileEmptyDataset() throws Exception {
    final MockMultipartFile file =
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
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream());
  }

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabaseXml() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.xml", "xml", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream());
  }

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabaseXls() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.xls", "xls", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream());
  }

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabaseXlsx() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.xlsx", "xlsx", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream());
  }

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyMetabase() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(dataSetMetabaseRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream());
  }

  @Test(expected = IOException.class)
  public void testProcessFileMappingError() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(dataSetMetabaseRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(new DataSetMetabase()));
    when(fileParserFactory.createContext("csv")).thenReturn(context);
    final DataSetVO dataSetVO = new DataSetVO();
    dataSetVO.setId(1L);
    when(context.parse(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(dataSetVO);
    when(dataSetMapper.classToEntity(Mockito.any(DataSetVO.class))).thenReturn(null);
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream());
  }

  @Test
  public void testProcessFileSuccess() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(dataSetMetabaseRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(new DataSetMetabase()));
    when(fileParserFactory.createContext("csv")).thenReturn(context);
    final DataSetVO dataSetVO = new DataSetVO();
    dataSetVO.setId(1L);
    when(context.parse(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(dataSetVO);
    final DatasetValue entityValue = new DatasetValue();
    final ArrayList<TableValue> tableValues = new ArrayList<>();
    tableValues.add(new TableValue());
    entityValue.setId(1L);
    entityValue.setTableValues(tableValues);
    when(dataSetMapper.classToEntity(Mockito.any(DataSetVO.class))).thenReturn(entityValue);
    when(datasetRepository.saveAndFlush(Mockito.any())).thenReturn(new DatasetValue());
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream());
    Mockito.verify(kafkaSender, times(1)).sendMessage(Mockito.any());
  }


  @Test
  public void testCreateEmptyDataset() throws Exception {
    doNothing().when(recordStoreControllerZull).createEmptyDataset(Mockito.any());
    datasetService.createEmptyDataset("");
    Mockito.verify(recordStoreControllerZull, times(1)).createEmptyDataset(Mockito.any());
  }


  @Test
  public void testDeleteImportData() throws Exception {
    doNothing().when(datasetRepository).empty(Mockito.any());
    datasetService.deleteImportData(1L);
    Mockito.verify(datasetRepository, times(1)).empty(Mockito.any());
  }

  @Test
  public void testDeleteDataSchema() throws Exception {
    doNothing().when(schemasRepository).deleteById(Mockito.any());
    datasetService.deleteDataSchema(new ObjectId().toString());
    Mockito.verify(schemasRepository, times(1)).deleteById(Mockito.any());
  }

  @Test
  public void testGetTableValuesByIdEmpty() throws Exception {
    when(recordRepository.findByTableValue_IdTableSchema(Mockito.any()))
        .thenReturn(new ArrayList<>());
    TableVO result = datasetService.getTableValuesById(1L, "mongoId", pageable, null, true);
    Assert.assertNotNull("result null", result);
    Assert.assertEquals("wrong number of records", Long.valueOf(0), result.getTotalRecords());
  }

  @Test(expected = EEAException.class)
  public void testGetTableValuesByIdNull() throws Exception {
    when(recordRepository.findByTableValue_IdTableSchema(Mockito.any())).thenReturn(null);
    datasetService.getTableValuesById(1L, "mongoId", pageable, null, true);
  }

  @Test
  public void testGetTableValuesById() throws Exception {
    when(recordRepository.findByTableValue_IdTableSchema(Mockito.any())).thenReturn(recordValues);

    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    datasetService.getTableValuesById(1L, "mongoId", pageable, null, true);
    Mockito.verify(recordNoValidationMapper, times(1)).entityListToClass(Mockito.any());
  }

  @Test
  public void testSetDataschemaTables() throws Exception {
    when(dataSetTablesMapper.classToEntity(Mockito.any())).thenReturn(new TableCollection());
    when(dataSetMetabaseTableCollection.save(Mockito.any())).thenReturn(new TableCollection());
    datasetService.setDataschemaTables(1L, 1L, new TableCollectionVO());
    Mockito.verify(dataSetMetabaseTableCollection, times(1)).save(Mockito.any());
  }

  @Test
  public void testGetByIdException() throws Exception {
    DataSetVO datasetVOtemp = new DataSetVO();
    datasetVOtemp.setId(1L);
    when(tableRepository.findAllTables()).thenReturn(new ArrayList<>());
    when(dataSetMapper.entityToClass(Mockito.any(DatasetValue.class))).thenReturn(datasetVOtemp);
    assertEquals("not equals", datasetVOtemp, datasetService.getById(1L));
  }

  @Test
  public void testGetByIdSuccess() throws Exception {
    when(tableRepository.findAllTables()).thenReturn(tableValues);
    when(dataSetMapper.entityToClass(Mockito.any(DatasetValue.class))).thenReturn(new DataSetVO());
    when(recordRepository.findByTableValue_IdTableSchema(Mockito.any())).thenReturn(recordValues);
    DataSetVO result = datasetService.getById(1L);
    assertEquals("not equals", new DataSetVO(), result);
  }

  @Test
  public void testGetDataFlowIdByIdSuccess() throws Exception {
    when(dataSetMetabaseRepository.findDataflowIdById(Mockito.any())).thenReturn(1L);
    Long result = datasetService.getDataFlowIdById(1L);
    assertNotNull("it shouldn't be null", result);
  }

  @Test(expected = EEAException.class)
  public void testUpdateNullException() throws Exception {
    datasetService.updateDataset(null);
  }

  @Test
  public void testUpdateSuccess() throws Exception {
    when(dataSetMapper.classToEntity((Mockito.any(DataSetVO.class))))
        .thenReturn(new DatasetValue());
    when(datasetRepository.save(Mockito.any())).thenReturn(new DatasetValue());
    datasetService.updateDataset(new DataSetVO());
    Mockito.verify(datasetRepository, times(1)).save(Mockito.any());

  }
  

  @Test
  public void testGetStatisticsSuccess() throws Exception {
    
    DataSetSchema schema = new DataSetSchema();
    schema.setTableSchemas(new ArrayList<>());
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(schemasRepository.findByIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190"))).thenReturn(schema);
    datasetService.getStatistics(1L);
    Mockito.verify(datasetRepository, times(1)).findById(Mockito.any());
  }

}
