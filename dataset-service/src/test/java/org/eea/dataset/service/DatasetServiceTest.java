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
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.domain.Validation;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.DatasetValidationRepository;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.persistence.data.repository.FieldValidationRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.RecordValidationRepository;
import org.eea.dataset.persistence.data.repository.StatisticsRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.data.repository.TableValidationRepository;
import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.domain.TableCollection;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseTableRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.file.FileParseContextImpl;
import org.eea.dataset.service.file.FileParserFactory;
import org.eea.dataset.service.file.interfaces.IFileExportContext;
import org.eea.dataset.service.file.interfaces.IFileExportFactory;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.FieldValidationVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.RecordValidationVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationVO;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.metabase.TableCollectionVO;
import org.eea.kafka.io.KafkaSender;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
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
  private ReportingDatasetRepository reportingDatasetRepository;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private SchemasRepository schemasRepository;

  @Mock
  private DatasetRepository datasetRepository;

  @Mock
  private TableRepository tableRepository;

  @Mock
  private KafkaSender kafkaSender;

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

  @Mock
  private ValidationRepository validationRepository;

  @Mock
  private DatasetValidationRepository datasetValidationRepository;

  @Mock
  private IFileExportFactory fileExportFactory;

  @Mock
  private IFileExportContext contextExport;

  @Mock
  private FileCommonUtils fileCommon;

  @Mock
  private StatisticsRepository statisticsRepository;

  private FieldValue fieldValue;
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
    tableValue.setDatasetId(datasetValue);
    tableValue.setIdTableSchema("5cf0e9b3b793310e9ceca190");
    datasetValue.setId(1L);
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
    when(reportingDatasetRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(new ReportingDataset()));
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
    when(reportingDatasetRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null);
  }



  @Test
  public void testProcessFileSuccessUpdateTable() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "csv", "content".getBytes());

    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(reportingDatasetRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(new ReportingDataset()));
    when(fileParserFactory.createContext("csv")).thenReturn(context);
    final DataSetVO dataSetVO = new DataSetVO();
    dataSetVO.setId(1L);
    when(context.parse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(dataSetVO);
    assertEquals(dataSetVO,
        datasetService.processFile(1L, "fileOriginal.csv", file.getInputStream(), ""));
  }

  @Test
  public void testSaveAllRecords() {
    datasetService.saveAllRecords(1L, new ArrayList<>());
    Mockito.verify(recordRepository, times(1)).saveAll(Mockito.any());
  }

  @Test
  public void testSaveTable() {
    Mockito.when(datasetRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new DatasetValue()));
    datasetService.saveTable(1L, new TableValue());
    Mockito.verify(tableRepository, times(1)).saveAndFlush(Mockito.any());
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
    TypeErrorEnum[] errorfilter = new TypeErrorEnum[] {TypeErrorEnum.ERROR, TypeErrorEnum.WARNING};
    TableVO result = datasetService.getTableValuesById(1L, "mongoId", pageable, null, errorfilter);
    Assert.assertNotNull("result null", result);
    Assert.assertEquals("wrong number of records", Long.valueOf(0), result.getTotalRecords());
  }

  @Test
  public void testGetTableValuesById() throws Exception {
    when(recordRepository.findByTableValueNoOrder(Mockito.any(), Mockito.any()))
        .thenReturn(recordValues);
    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    TypeErrorEnum[] errorfilter =
        new TypeErrorEnum[] {TypeErrorEnum.ERROR, TypeErrorEnum.WARNING, TypeErrorEnum.CORRECT};
    datasetService.getTableValuesById(1L, "mongoId", pageable, null, errorfilter);
    Mockito.verify(recordNoValidationMapper, times(1)).entityListToClass(Mockito.any());
  }

  @Test
  public void testGetTableValuesById2() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(1L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(recordValues);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(TypeData.TEXT);
    ArrayList<FieldVO> fields = new ArrayList<>();
    fields.add(new FieldVO());
    recordVO.setFields(fields);
    recordVO.setId(1L);
    recordVOs.add(recordVO);
    List<FieldValidation> fieldV = new ArrayList<>();
    FieldValidation fieldValidation = new FieldValidation();
    fieldValidation.setFieldValue(new FieldValue());
    fieldV.add(fieldValidation);
    List<RecordValidation> recV = new ArrayList<>();
    RecordValidation recValidation = new RecordValidation();
    recValidation.setRecordValue(new RecordValue());
    recV.add(recValidation);
    pageable = PageRequest.of(0, 1);
    String listFields = "field_1:1,fields_2:2,fields_3:3";
    TypeErrorEnum[] errorfilter = new TypeErrorEnum[] {TypeErrorEnum.ERROR, TypeErrorEnum.WARNING};
    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(recordVOs);
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(recordValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);

    datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable, listFields,
        errorfilter);

    Mockito.verify(recordNoValidationMapper, times(1)).entityListToClass(Mockito.any());
  }

  @Test
  public void testGetTableValuesById3() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(1L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(recordValues);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(TypeData.TEXT);
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
    pageable = PageRequest.of(0, 1);
    String listFields = "field_1:1,fields_2:2,fields_3:3";
    TypeErrorEnum[] errorfilter = new TypeErrorEnum[] {TypeErrorEnum.ERROR, TypeErrorEnum.WARNING};
    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(recordVOs);
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(recordValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);
    datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable, listFields,
        errorfilter);

    Mockito.verify(recordNoValidationMapper, times(1)).entityListToClass(Mockito.any());
  }

  @Test
  public void testGetTableValuesById4() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(1L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(recordValues);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(TypeData.TEXT);
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
    pageable = null;
    String listFields = "field_1:1,fields_2:2,fields_3:3";
    TypeErrorEnum[] errorfilter = new TypeErrorEnum[] {TypeErrorEnum.ERROR, TypeErrorEnum.WARNING};
    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(recordVOs);
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(recordValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);
    datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable, listFields,
        errorfilter);

    Mockito.verify(recordNoValidationMapper, times(1)).entityListToClass(Mockito.any());
  }

  @Test
  public void testGetTableValuesById5() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(0L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(recordValues);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(TypeData.TEXT);
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
    pageable = null;
    String listFields = "field_1:1,fields_2:2,fields_3:3";
    TypeErrorEnum[] errorfilter = new TypeErrorEnum[] {TypeErrorEnum.ERROR, TypeErrorEnum.WARNING};
    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(recordVOs);
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(recordValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);
    datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable, listFields,
        errorfilter);

    Mockito.verify(recordNoValidationMapper, times(1)).entityListToClass(Mockito.any());
  }

  @Test
  public void testGetTableValuesById6() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(0L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(recordValues);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(TypeData.TEXT);
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
    pageable = null;
    String listFields = "field_1:1,fields_2:2,fields_3:3";
    TypeErrorEnum[] errorfilter = new TypeErrorEnum[] {TypeErrorEnum.ERROR, TypeErrorEnum.WARNING};
    List<FieldValidationVO> valFieldVO = new ArrayList<FieldValidationVO>();
    FieldValidationVO fieldVO = new FieldValidationVO();
    ValidationVO validation = new ValidationVO();
    validation.setLevelError(TypeErrorEnum.ERROR);
    fieldVO.setValidation(validation);
    valFieldVO.add(fieldVO);
    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(recordVOs);
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(valFieldVO);
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);
    datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable, listFields,
        errorfilter);

    Mockito.verify(recordNoValidationMapper, times(1)).entityListToClass(Mockito.any());
  }

  @Test
  public void testGetTableValuesById7() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(0L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(recordValues);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(TypeData.TEXT);
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
    pageable = null;
    String listFields = "field_1:1,fields_2:2,fields_3:3";
    TypeErrorEnum[] errorfilter = new TypeErrorEnum[] {TypeErrorEnum.ERROR, TypeErrorEnum.WARNING};
    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(recordVOs);
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(null);
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);
    datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable, listFields,
        errorfilter);

    Mockito.verify(recordNoValidationMapper, times(1)).entityListToClass(Mockito.any());
  }

  @Test
  public void testGetTableValuesById8() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(0L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(recordValues);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(TypeData.TEXT);
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
    pageable = null;
    String listFields = "field_1:1,fields_2:2,fields_3:3";
    TypeErrorEnum[] errorfilter = new TypeErrorEnum[] {TypeErrorEnum.ERROR, TypeErrorEnum.WARNING};
    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(recordVOs);
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(null);
    when(recordValidationMapper.entityListToClass(Mockito.any())).thenReturn(null);
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);
    datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable, listFields,
        errorfilter);

    Mockito.verify(recordNoValidationMapper, times(1)).entityListToClass(Mockito.any());
  }

  @Test
  public void testGetTableValuesById9() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(0L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(recordValues);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(TypeData.TEXT);
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
    pageable = null;
    String listFields = "field_1:1,fields_2:2,fields_3:3";
    TypeErrorEnum[] errorfilter = new TypeErrorEnum[] {TypeErrorEnum.ERROR, TypeErrorEnum.WARNING};
    List<FieldValidationVO> valFieldVO = new ArrayList<FieldValidationVO>();
    FieldValidationVO fieldVO = new FieldValidationVO();
    ValidationVO validation = new ValidationVO();
    validation.setLevelError(TypeErrorEnum.ERROR);
    fieldVO.setValidation(validation);
    valFieldVO.add(fieldVO);
    List<RecordValidationVO> valRecordsVO = new ArrayList<RecordValidationVO>();
    RecordValidationVO recordvalVO = new RecordValidationVO();
    ValidationVO validationRec = new ValidationVO();
    validationRec.setLevelError(TypeErrorEnum.ERROR);
    recordvalVO.setValidation(validationRec);
    valRecordsVO.add(recordvalVO);
    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(recordVOs);
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(valFieldVO);
    when(recordValidationMapper.entityListToClass(Mockito.any())).thenReturn(valRecordsVO);
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);
    datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable, listFields,
        errorfilter);

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
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);

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
    when(statisticsRepository.findAllStatistics()).thenReturn(new ArrayList<>());

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
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);

    datasetService.getStatistics(1L);
    Mockito.verify(datasetRepository, times(1)).findById(Mockito.any());
  }


  @Test
  public void testGetTableFromAnyObjectId() throws Exception {

    when(recordRepository.findByIdAndTableValue_DatasetId_Id(Mockito.any(), Mockito.any()))
        .thenReturn(recordValue);

    datasetService.getPositionFromAnyObjectId(1L, 1L, TypeEntityEnum.RECORD);
    Mockito.verify(recordRepository, times(1)).findByIdAndTableValue_DatasetId_Id(Mockito.any(),
        Mockito.any());


  }

  @Test
  public void testGetTableFromAnyObjectId2() throws Exception {

    when(tableRepository.findByIdAndDatasetId_Id(Mockito.any(), Mockito.any()))
        .thenReturn(tableValue);

    datasetService.getPositionFromAnyObjectId(1L, 1L, TypeEntityEnum.TABLE);
    Mockito.verify(tableRepository, times(1)).findByIdAndDatasetId_Id(Mockito.any(), Mockito.any());

  }

  @Test
  public void testGetTableFromAnyObjectIdTable() throws Exception {
    TableValue table = new TableValue();
    List<RecordValue> records = new ArrayList<>();
    records.add(recordValue);
    table.setRecords(records);
    when(tableRepository.findByIdAndDatasetId_Id(Mockito.any(), Mockito.any())).thenReturn(table);

    DataSetSchema schema = new DataSetSchema();
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId());
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableSchema);
    schema.setTableSchemas(tableSchemas);

    datasetService.getPositionFromAnyObjectId(1L, 1L, TypeEntityEnum.TABLE);
    Mockito.verify(tableRepository, times(1)).findByIdAndDatasetId_Id(Mockito.any(), Mockito.any());

  }

  @Test
  public void testGetTableFromAnyObjectId3() throws Exception {

    when(fieldRepository.findByIdAndRecord_TableValue_DatasetId_Id(Mockito.any(), Mockito.any()))
        .thenReturn(fieldValue);

    datasetService.getPositionFromAnyObjectId(1L, 1L, TypeEntityEnum.FIELD);
    Mockito.verify(fieldRepository, times(1))
        .findByIdAndRecord_TableValue_DatasetId_Id(Mockito.any(), Mockito.any());

  }


  @Test
  public void testDeleteTableData() throws Exception {
    doNothing().when(recordRepository).deleteRecordWithIdTableSchema(Mockito.any());
    datasetService.deleteTableBySchema("", 1L);
    Mockito.verify(recordRepository, times(1)).deleteRecordWithIdTableSchema(Mockito.any());
  }

  @Test(expected = EEAException.class)
  public void updateRecordsNullTest() throws Exception {
    datasetService.updateRecords(null, new ArrayList<RecordVO>());
  }

  @Test(expected = EEAException.class)
  public void updateRecordsNull2Test() throws Exception {
    datasetService.updateRecords(1L, null);
  }

  @Test(expected = EEAException.class)
  public void deleteRecordsNullTest() throws Exception {
    datasetService.deleteRecord(null, 1L);
  }

  @Test(expected = EEAException.class)
  public void deleteRecordsNull2Test() throws Exception {
    datasetService.deleteRecord(1L, null);
  }

  @Test
  public void deleteRecordsTest() throws Exception {
    doNothing().when(recordRepository).deleteRecordWithId(Mockito.any());
    datasetService.deleteRecord(1L, 1L);
    Mockito.verify(recordRepository, times(1)).deleteRecordWithId(Mockito.any());
  }

  @Test
  public void updateRecordsTest() throws EEAException {
    when(recordMapper.classListToEntity(Mockito.any())).thenReturn(recordValues);
    datasetService.updateRecords(1L, new ArrayList<RecordVO>());
    Mockito.verify(recordMapper, times(1)).classListToEntity(Mockito.any());
  }


  @Test
  public void createRecordsTest() throws EEAException {
    List<RecordValue> myRecords = new ArrayList<>();
    myRecords.add(new RecordValue());
    Mockito.when(tableRepository.findIdByIdTableSchema(Mockito.any())).thenReturn(1L);
    Mockito.when(partitionDataSetMetabaseRepository
        .findFirstByIdDataSet_idAndUsername(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(new PartitionDataSetMetabase()));
    List<RecordVO> records = new ArrayList<>();

    List<RecordValue> recordsList = new ArrayList<>();
    RecordValue record = new RecordValue();

    List<FieldValue> fields = new ArrayList<>();
    FieldValue field = new FieldValue();

    fields.add(field);
    field.setValue(null);
    fields.add(field);
    record.setFields(fields);
    recordsList.add(record);
    when(recordMapper.classListToEntity(records)).thenReturn(recordsList);
    datasetService.createRecords(1L, records, "");
    Mockito.verify(recordMapper, times(1)).classListToEntity(Mockito.any());
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void createRecordsExceptionTest() throws EEAException {
    thrown.expectMessage(EEAErrorMessage.TABLE_NOT_FOUND);
    datasetService.createRecords(1L, new ArrayList<RecordVO>(), "");
  }

  @Test
  public void createRecordsException2Test() throws EEAException {
    thrown.expectMessage(EEAErrorMessage.RECORD_NOTFOUND);
    datasetService.createRecords(1L, new ArrayList<RecordVO>(), null);
  }

  @Test
  public void createRecordsException3Test() throws EEAException {
    thrown.expectMessage(EEAErrorMessage.RECORD_NOTFOUND);
    datasetService.createRecords(1L, null, "");
  }

  @Test
  public void createRecordsException4Test() throws EEAException {
    thrown.expectMessage(EEAErrorMessage.RECORD_NOTFOUND);
    datasetService.createRecords(null, new ArrayList<RecordVO>(), "");
  }


  @Test
  public void exportFileTest() throws EEAException, IOException {
    byte[] expectedResult = "".getBytes();
    ReportingDataset dataset = new ReportingDataset();
    PartitionDataSetMetabase partition = new PartitionDataSetMetabase();
    dataset.setDataflowId(1L);
    // partition.setId(1L);
    // when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.any(),
    // Mockito.any())).thenReturn(Optional.of(partition));
    when(reportingDatasetRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(dataset));
    when(fileExportFactory.createContext(Mockito.any())).thenReturn(contextExport);
    when(contextExport.fileWriter(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(expectedResult);
    assertEquals("not equals", expectedResult, datasetService.exportFile(1L, "csv", ""));
  }

  @Test
  public void getFileNameTest() throws EEAException {
    ReportingDataset dataset = new ReportingDataset();
    when(reportingDatasetRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(dataset));
    when(fileCommon.getDataSetSchema(Mockito.any())).thenReturn(new DataSetSchemaVO());
    when(fileCommon.getTableName(Mockito.any(), Mockito.any())).thenReturn("test");
    assertEquals("not equals", "test.csv", datasetService.getFileName("csv", "test", 1L));
  }

  @Test
  public void updateFieldTest() throws EEAException {
    datasetService.updateField(1L, new FieldVO());
    Mockito.verify(fieldRepository, times(1)).saveValue(Mockito.any(), Mockito.any());
  }

  @Test
  public void updateFieldException1Test() throws EEAException {
    thrown.expectMessage(EEAErrorMessage.FIELD_NOT_FOUND);
    datasetService.updateField(null, new FieldVO());
  }

  @Test
  public void updateFieldException2Test() throws EEAException {
    thrown.expectMessage(EEAErrorMessage.FIELD_NOT_FOUND);
    datasetService.updateField(1L, null);
  }

  @Test
  public void testInsertSchema() throws EEAException {

    DataSetSchema ds = new DataSetSchema();
    List<TableSchema> tableSchemas = new ArrayList<>();
    TableSchema table = new TableSchema();
    table.setIdTableSchema(new ObjectId());
    tableSchemas.add(table);
    ds.setTableSchemas(tableSchemas);
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(ds);
    datasetService.insertSchema(1L, "5cf0e9b3b793310e9ceca190");
    Mockito.verify(datasetRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void testFindTableIdByTableSchema() {
    datasetService.findTableIdByTableSchema(1L, "5cf0e9b3b793310e9ceca190");
    Mockito.verify(tableRepository, times(1)).findIdByIdTableSchema(Mockito.any());
  }

  @Test
  public void testDeleteRecordValuesToRestoreSnapshot() throws EEAException {
    datasetService.deleteRecordValuesToRestoreSnapshot(1L, 1L);
    Mockito.verify(recordRepository, times(1)).deleteRecordValuesToRestoreSnapshot(Mockito.any());
  }

  @Test
  public void testSaveStats() throws EEAException {
    DataSetSchema schema = new DataSetSchema();
    schema.setTableSchemas(new ArrayList<>());
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);

    datasetService.saveStatistics(1L);
    Mockito.verify(statisticsRepository, times(1)).saveAll(Mockito.any());

  }


}
