package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.mapper.DataSetTablesMapper;
import org.eea.dataset.mapper.FieldValidationMapper;
import org.eea.dataset.mapper.RecordMapper;
import org.eea.dataset.mapper.RecordNoValidationMapper;
import org.eea.dataset.mapper.RecordValidationMapper;
import org.eea.dataset.mapper.TableNoRecordMapper;
import org.eea.dataset.mapper.TableValidationMapper;
import org.eea.dataset.mapper.TableValueMapper;
import org.eea.dataset.persistence.data.domain.DatasetValidation;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValidation;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValidation;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.domain.Validation;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.persistence.data.repository.FieldValidationRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.RecordValidationRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.data.repository.TableValidationRepository;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.TableCollection;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseTableRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.file.FileParseContextImpl;
import org.eea.dataset.service.file.FileParserFactory;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
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
  private TableValueMapper tableValueMapper;

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

  @Mock
  private TableNoRecordMapper tableNoRecordMapper;

  @Mock
  private FieldValidationRepository fieldValidationRepository;

  @Mock
  private RecordValidationRepository recordValidationRepository;

  @Mock
  private TableValidationRepository tableValidationRepository;

  @Mock
  private TableValidationMapper tableValidationMapper;

  @Mock
  private FieldValidationMapper fieldValidationMapper;

  @Mock
  private RecordValidationMapper recordValidationMapper;


  private FieldValue fieldValue;
  private RecordValue recordValue;
  private ArrayList<RecordValue> recordValues;
  private TableValue tableValue;
  private ArrayList<TableValue> tableValues;
  private DatasetValue datasetValue;
  private DataSetVO dataSetVO;
  private ArrayList<TableVO> tableVOs;
  private TableVO tableVO;
  private Validation validation;

  @Before
  public void initMocks() {
    validation = new Validation();
    fieldValue = new FieldValue();
    recordValues = new ArrayList<>();
    recordValue = new RecordValue();
    recordValue.setIdRecordSchema("");
    recordValue.setLevelError(TypeErrorEnum.ERROR);
    recordValue.setFields(new ArrayList<>());
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
    dataSetVO.setId(1L);
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = EEAException.class)
  public void testProcessFileThrowException() throws Exception {
    final MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    datasetService.processFile(null, "fileOriginal", fileNoExtension.getInputStream(), null);
  }

  @Test(expected = EEAException.class)
  public void testProcessFilenameNullThrowException() throws Exception {
    final MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    datasetService.processFile(null, null, fileNoExtension.getInputStream(), null);
  }

  @Test(expected = EEAException.class)
  public void testProcessFileBadExtensionThrowException() throws Exception {
    final MockMultipartFile fileBadExtension =
        new MockMultipartFile("file", "fileOriginal.doc", "doc", "content".getBytes());
    datasetService.processFile(1L, "fileOriginal.doc", fileBadExtension.getInputStream(), null);
  }

  @Test(expected = EEAException.class)
  public void testProcessFileThrowException2() throws Exception {
    final MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    datasetService.processFile(1L, "fileOriginal", fileNoExtension.getInputStream(), null);
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
    when(context.parse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(null);

    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null);
  }

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabase() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null);
  }

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabaseXml() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.xml", "xml", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null);
  }

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabaseXls() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.xls", "xls", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null);
  }

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabaseXlsx() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.xlsx", "xlsx", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null);
  }

  @Test(expected = EEAException.class)
  public void testProcessFileEmptyMetabase() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(dataSetMetabaseRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null);
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
    when(context.parse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(dataSetVO);
    when(dataSetMapper.classToEntity(Mockito.any(DataSetVO.class))).thenReturn(null);
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null);
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
    when(context.parse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(dataSetVO);
    final DatasetValue entityValue = new DatasetValue();
    final ArrayList<TableValue> tableValues = new ArrayList<>();
    final TableValue tableValue = new TableValue();
    tableValue.setIdTableSchema("tableSchema");
    tableValues.add(tableValue);
    entityValue.setId(1L);
    entityValue.setTableValues(tableValues);
    when(dataSetMapper.classToEntity(Mockito.any(DataSetVO.class))).thenReturn(entityValue);
    when(tableRepository.findIdByIdTableSchema(Mockito.any())).thenReturn(2L);
    when(datasetRepository.saveAndFlush(Mockito.any())).thenReturn(new DatasetValue());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), "schema");
    Mockito.verify(datasetRepository, times(1)).saveAndFlush(Mockito.any());
  }

  @Test
  public void testProcessFileSuccessUpdateTable() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(dataSetMetabaseRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(new DataSetMetabase()));
    when(fileParserFactory.createContext("csv")).thenReturn(context);
    final DataSetVO dataSetVO = new DataSetVO();
    dataSetVO.setId(1L);
    when(context.parse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(dataSetVO);
    final DatasetValue entityValue = new DatasetValue();
    final ArrayList<TableValue> tableValues = new ArrayList<>();
    final TableValue tableValue = new TableValue();
    tableValue.setIdTableSchema("schema");
    tableValues.add(tableValue);
    entityValue.setId(1L);
    entityValue.setTableValues(tableValues);
    when(dataSetMapper.classToEntity(Mockito.any(DataSetVO.class))).thenReturn(entityValue);
    when(tableRepository.findIdByIdTableSchema(Mockito.any())).thenReturn(2L);
    when(datasetRepository.saveAndFlush(Mockito.any())).thenReturn(new DatasetValue());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), "schema");
    Mockito.verify(datasetRepository, times(1)).saveAndFlush(Mockito.any());
  }

  @Test
  public void testProcessFileSuccessNewTable() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(dataSetMetabaseRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(new DataSetMetabase()));
    when(fileParserFactory.createContext("csv")).thenReturn(context);
    final DataSetVO dataSetVO = new DataSetVO();
    dataSetVO.setId(1L);
    when(context.parse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(dataSetVO);
    final DatasetValue entityValue = new DatasetValue();
    final ArrayList<TableValue> tableValues = new ArrayList<>();
    tableValues.add(new TableValue());
    entityValue.setId(1L);
    entityValue.setTableValues(tableValues);
    when(dataSetMapper.classToEntity(Mockito.any(DataSetVO.class))).thenReturn(entityValue);
    when(tableRepository.findIdByIdTableSchema(Mockito.any())).thenReturn(null);
    when(datasetRepository.saveAndFlush(Mockito.any())).thenReturn(new DatasetValue());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null);
    Mockito.verify(datasetRepository, times(1)).saveAndFlush(Mockito.any());
  }


  @Test
  public void testCreateEmptyDataset() throws Exception {
    doNothing().when(recordStoreControllerZull).createEmptyDataset(Mockito.any());
    datasetService.createEmptyDataset("");
    Mockito.verify(recordStoreControllerZull, times(1)).createEmptyDataset(Mockito.any());
  }


  @Test
  public void testDeleteImportData() throws Exception {
    doNothing().when(datasetRepository).removeDatasetData(Mockito.any());
    datasetService.deleteImportData(1L);
    Mockito.verify(datasetRepository, times(1)).removeDatasetData(Mockito.any());
  }

  @Test
  public void testDeleteDataSchema() throws Exception {
    doNothing().when(schemasRepository).deleteById(Mockito.any());
    datasetService.deleteDataSchema(new ObjectId().toString());
    Mockito.verify(schemasRepository, times(1)).deleteById(Mockito.any());
  }

  @Test
  public void testGetTableValuesByIdEmpty() throws Exception {
    when(recordRepository.findByTableValueIdTableSchema(Mockito.any()))
        .thenReturn(new ArrayList<>());
    TableVO result = datasetService.getTableValuesById(1L, "mongoId", pageable, null, true);
    Assert.assertNotNull("result null", result);
    Assert.assertEquals("wrong number of records", Long.valueOf(0), result.getTotalRecords());
  }

  @Test(expected = EEAException.class)
  public void testGetTableValuesByIdNull() throws Exception {
    when(recordRepository.findByTableValueIdTableSchema(Mockito.any())).thenReturn(null);
    datasetService.getTableValuesById(1L, "mongoId", pageable, null, true);
  }

  @Test
  public void testGetTableValuesById() throws Exception {
    when(recordRepository.findByTableValueIdTableSchema(Mockito.any())).thenReturn(recordValues);

    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    datasetService.getTableValuesById(1L, "mongoId", pageable, null, true);
    Mockito.verify(recordNoValidationMapper, times(1)).entityListToClass(Mockito.any());
  }

  @Test
  public void testGetTableValuesById2() throws Exception {
    when(recordRepository.findByTableValueIdTableSchema(Mockito.any())).thenReturn(recordValues);
    when(pageable.getPageSize()).thenReturn(1);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    ArrayList<FieldVO> fields = new ArrayList<>();
    fields.add(new FieldVO());
    recordVO.setFields(fields);
    recordVOs.add(recordVO);
    List<FieldValidation> fieldV = new ArrayList<>();
    FieldValidation fieldValidation = new FieldValidation();
    fieldValidation.setFieldValue(new FieldValue());
    fieldV.add(fieldValidation);
    List<RecordValidation> recV = new ArrayList<>();
    RecordValidation recValidation = new RecordValidation();
    recValidation.setRecordValue(new RecordValue());
    recV.add(recValidation);
    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(recordVOs);
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(recordValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    datasetService.getTableValuesById(1L, "mongoId", pageable, "123", true);
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
  public void testGetById() throws Exception {
    DataSetVO datasetVOtemp = new DataSetVO();
    datasetVOtemp.setId(1L);
    datasetVOtemp.setTableVO(new ArrayList<>());
    when(dataSetMapper.entityToClass(Mockito.any())).thenReturn(datasetVOtemp);
    assertEquals("not equals", datasetVOtemp, datasetService.getById(1L));
  }

  @Test
  public void testGetByIdSuccess() throws Exception {
    when(tableRepository.findAllTables()).thenReturn(tableValues);
    when(dataSetMapper.entityToClass(Mockito.any())).thenReturn(dataSetVO);
    DataSetVO result = datasetService.getById(1L);
    assertEquals("not equals", dataSetVO, result);
  }

  @Test
  public void testGetDataFlowIdByIdSuccess() throws Exception {
    when(dataSetMetabaseRepository.findDataflowIdById(Mockito.any())).thenReturn(1L);
    Long result = datasetService.getDataFlowIdById(1L);
    assertNotNull("it shouldn't be null", result);
  }

  @Test(expected = EEAException.class)
  public void testUpdateNullException() throws Exception {
    datasetService.updateDataset(1L, null);
  }

  @Test
  public void testUpdateSuccess() throws Exception {
    when(dataSetMapper.classToEntity((Mockito.any(DataSetVO.class))))
        .thenReturn(new DatasetValue());
    when(datasetRepository.saveAndFlush(Mockito.any())).thenReturn(new DatasetValue());
    datasetService.updateDataset(1L, new DataSetVO());
    Mockito.verify(datasetRepository, times(1)).saveAndFlush(Mockito.any());

  }


  @Test
  public void testGetStatisticsSuccess() throws Exception {

    DataSetSchema schema = new DataSetSchema();
    schema.setTableSchemas(new ArrayList<>());
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));

    datasetService.getStatistics(1L);
    Mockito.verify(datasetRepository, times(1)).findById(Mockito.any());
  }


  @Test
  public void testGetStatisticsSuccess2() throws Exception {
    List<DatasetValidation> datasetValidations = new ArrayList<>();
    List<RecordValidation> recordValidations = new ArrayList<>();
    List<FieldValidation> fieldValidations = new ArrayList<>();
    Validation validation = new Validation();
    validation.setLevelError(TypeErrorEnum.ERROR);
    Validation validation2 = new Validation();
    validation2.setLevelError(TypeErrorEnum.WARNING);
    RecordValidation recordV = new RecordValidation();
    RecordValidation recordV2 = new RecordValidation();
    recordV.setValidation(validation);
    recordV2.setValidation(validation2);
    DatasetValidation datasetV = new DatasetValidation();
    datasetV.setValidation(validation);
    DatasetValidation datasetV2 = new DatasetValidation();
    datasetV2.setValidation(validation2);
    FieldValidation fieldV = new FieldValidation();
    FieldValidation fieldV2 = new FieldValidation();
    fieldV.setValidation(validation);
    fieldV2.setValidation(validation2);
    fieldValidations.add(fieldV);
    fieldValidations.add(fieldV2);
    datasetValidations.add(datasetV);
    datasetValidations.add(datasetV2);
    recordValidations.add(recordV);
    recordValidations.add(recordV2);

    datasetValue.setDatasetValidations(datasetValidations);
    datasetValue.setId(1L);
    datasetValue.setIdDatasetSchema(new ObjectId().toString());
    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    table.setIdTableSchema(new ObjectId());
    table.setNameTableSchema("");
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(table);
    schema.setTableSchemas(tableSchemas);
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    when(recordValidationRepository.findRecordValidationsByIdDatasetAndIdTableSchema(Mockito.any(),
        Mockito.any())).thenReturn(recordValidations);
    when(fieldValidationRepository.findFieldValidationsByIdDatasetAndIdTableSchema(Mockito.any(),
        Mockito.any())).thenReturn(fieldValidations);

    datasetService.getStatistics(1L);
    Mockito.verify(datasetRepository, times(1)).findById(Mockito.any());
  }

  @Test
  public void testGetStatisticsSuccessSanitizeElse() throws Exception {
    List<DatasetValidation> datasetValidations = new ArrayList<>();
    List<RecordValidation> recordValidations = new ArrayList<>();
    List<FieldValidation> fieldValidations = new ArrayList<>();
    Validation validation = new Validation();
    validation.setLevelError(TypeErrorEnum.ERROR);
    Validation validation2 = new Validation();
    validation2.setLevelError(TypeErrorEnum.WARNING);
    RecordValidation recordV = new RecordValidation();
    recordV.setValidation(validation);
    DatasetValidation datasetV = new DatasetValidation();
    datasetV.setValidation(validation);
    DatasetValidation datasetV2 = new DatasetValidation();
    datasetV2.setValidation(validation2);
    FieldValidation fieldV = new FieldValidation();
    FieldValidation fieldV2 = new FieldValidation();
    fieldV.setValidation(validation);
    fieldV2.setValidation(validation2);
    fieldValidations.add(fieldV);
    fieldValidations.add(fieldV2);
    datasetValidations.add(datasetV);
    datasetValidations.add(datasetV2);
    recordValidations.add(recordV);
    tableValue.setIdTableSchema("");
    tableValue.setRecords(new ArrayList<>());
    datasetValue.setDatasetValidations(datasetValidations);
    datasetValue.getTableValues().add(tableValue);
    datasetValue.getTableValues().add(tableValue);
    datasetValue.setDatasetValidations(datasetValidations);
    datasetValue.setId(1L);
    datasetValue.setIdDatasetSchema(new ObjectId().toString());
    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    table.setIdTableSchema(new ObjectId());
    table.setNameTableSchema("");
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(table);
    schema.setTableSchemas(tableSchemas);
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    when(recordValidationRepository.findRecordValidationsByIdDatasetAndIdTableSchema(Mockito.any(),
        Mockito.any())).thenReturn(recordValidations);
    when(fieldValidationRepository.findFieldValidationsByIdDatasetAndIdTableSchema(Mockito.any(),
        Mockito.any())).thenReturn(fieldValidations);
    datasetService.getStatistics(1L);
    Mockito.verify(datasetRepository, times(1)).findById(Mockito.any());
  }

  @Test
  public void testGetStatisticsSuccess3() throws Exception {
    List<DatasetValidation> datasetValidations = new ArrayList<>();
    List<RecordValidation> recordValidations = new ArrayList<>();
    List<FieldValidation> fieldValidations = new ArrayList<>();
    Validation validation = new Validation();
    validation.setLevelError(TypeErrorEnum.ERROR);
    Validation validation2 = new Validation();
    validation2.setLevelError(TypeErrorEnum.WARNING);
    RecordValidation recordV = new RecordValidation();
    recordV.setValidation(validation);
    DatasetValidation datasetV = new DatasetValidation();
    datasetV.setValidation(validation);
    DatasetValidation datasetV2 = new DatasetValidation();
    datasetV2.setValidation(validation2);
    FieldValidation fieldV = new FieldValidation();
    FieldValidation fieldV2 = new FieldValidation();
    fieldV.setValidation(validation);
    fieldV2.setValidation(validation2);
    fieldValidations.add(fieldV);
    fieldValidations.add(fieldV2);
    datasetValidations.add(datasetV);
    datasetValidations.add(datasetV2);
    recordValidations.add(recordV);
    tableValue.setIdTableSchema("");
    tableValue.setRecords(new ArrayList<>());
    datasetValue.setDatasetValidations(datasetValidations);
    datasetValue.getTableValues().add(tableValue);
    datasetValue.getTableValues().add(tableValue);
    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    table.setIdTableSchema(new ObjectId());
    table.setNameTableSchema("");
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(table);
    schema.setTableSchemas(tableSchemas);
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));

    datasetService.getStatistics(1L);
    Mockito.verify(datasetRepository, times(1)).findById(Mockito.any());
  }


  @Test
  public void testGetTableFromAnyObjectId() throws Exception {

    when(recordRepository.findByIdAndTableValue_DatasetId_Id(Mockito.any(), Mockito.any()))
        .thenReturn(recordValue);

    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(tableValidationRepository.findByTableValue_IdTableSchema(Mockito.any()))
        .thenReturn(new ArrayList<>());
    when(tableValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());

    datasetService.getTableFromAnyObjectId(1L, 1L, pageable, TypeEntityEnum.RECORD);
    Mockito.verify(recordNoValidationMapper, times(1)).entityListToClass(Mockito.any());

  }

  @Test
  public void testGetTableFromAnyObjectId2() throws Exception {

    when(tableRepository.findByIdAndDatasetId_Id(Mockito.any(), Mockito.any()))
        .thenReturn(tableValue);

    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(tableValidationRepository.findByTableValue_IdTableSchema(Mockito.any()))
        .thenReturn(new ArrayList<>());
    when(tableValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    datasetService.getTableFromAnyObjectId(1L, 1L, pageable, TypeEntityEnum.TABLE);
    Mockito.verify(recordNoValidationMapper, times(1)).entityListToClass(Mockito.any());

  }

  @Test
  public void testGetTableFromAnyObjectIdTable() throws Exception {
    TableValue table = new TableValue();
    List<RecordValue> records = new ArrayList<>();
    records.add(new RecordValue());
    table.setRecords(records);
    when(tableRepository.findByIdAndDatasetId_Id(Mockito.any(), Mockito.any())).thenReturn(table);
    when(tableNoRecordMapper.entityToClass(Mockito.any())).thenReturn(tableVO);
    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(tableValidationRepository.findByTableValue_IdTableSchema(Mockito.any()))
        .thenReturn(new ArrayList<>());
    when(tableValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    datasetService.getTableFromAnyObjectId(1L, 1L, pageable, TypeEntityEnum.TABLE);
    Mockito.verify(recordNoValidationMapper, times(1)).entityListToClass(Mockito.any());

  }

  @Test
  public void testGetTableFromAnyObjectId3() throws Exception {

    when(fieldRepository.findByIdAndRecord_TableValue_DatasetId_Id(Mockito.any(), Mockito.any()))
        .thenReturn(fieldValue);
    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(tableValidationRepository.findByTableValue_IdTableSchema(Mockito.any()))
        .thenReturn(new ArrayList<>());
    when(tableValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    datasetService.getTableFromAnyObjectId(1L, 1L, pageable, TypeEntityEnum.FIELD);
    Mockito.verify(recordNoValidationMapper, times(1)).entityListToClass(Mockito.any());

  }


  @Test
  public void testGetListValidations() throws Exception {

    DataSetSchema schema = new DataSetSchema();
    schema.setTableSchemas(new ArrayList<>());
    schema.setIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(schemasRepository.findByIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190")))
        .thenReturn(schema);
    datasetService.getListValidations(0L, pageable, null, false);
    Mockito.verify(datasetRepository, times(1)).findById(Mockito.any());
  }


  @Test
  public void testGetListValidations2() throws Exception {

    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidation.setTableValue(tableValue);
    validation.setId(1L);
    validation.setLevelError(TypeErrorEnum.ERROR);
    validation.setTypeEntity(TypeEntityEnum.TABLE);
    tableValidation.setValidation(validation);
    List<TableValidation> tableValidations = new ArrayList<>();
    tableValidations.add(tableValidation);
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setRecordValue(recordValue);
    recordValidation.setValidation(validation);
    List<RecordValidation> recordValidations = new ArrayList<>();
    recordValidations.add(recordValidation);
    DatasetValidation datasetValidation = new DatasetValidation();
    datasetValidation.setValidation(validation);
    datasetValidation.setDatasetValue(datasetValue);
    List<DatasetValidation> datasetValidations = new ArrayList<>();
    datasetValidations.add(datasetValidation);
    datasetValue.setDatasetValidations(datasetValidations);
    FieldValidation fieldValidation = new FieldValidation();
    recordValue.setTableValue(tableValue);
    fieldValue.setRecord(recordValue);
    fieldValidation.setFieldValue(fieldValue);
    fieldValidation.setValidation(validation);
    List<FieldValidation> fieldValidations = new ArrayList<>();
    fieldValidations.add(fieldValidation);

    DataSetSchema schema = new DataSetSchema();
    schema.setTableSchemas(new ArrayList<>());
    schema.setIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(schemasRepository.findByIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190")))
        .thenReturn(schema);
    when(tableValidationRepository.findTableValidationsByIdDataset(Mockito.any()))
        .thenReturn(tableValidations);
    when(recordValidationRepository.findRecordValidationsByIdDataset(Mockito.any()))
        .thenReturn(recordValidations);
    when(fieldValidationRepository.findFieldValidationsByIdDataset(Mockito.any()))
        .thenReturn(fieldValidations);
    datasetService.getListValidations(0L, pageable, "typeEntity", false);
    Mockito.verify(datasetRepository, times(1)).findById(Mockito.any());

  }

}
