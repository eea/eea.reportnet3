package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.mapper.FieldNoValidationMapper;
import org.eea.dataset.mapper.FieldValidationMapper;
import org.eea.dataset.mapper.RecordMapper;
import org.eea.dataset.mapper.RecordNoValidationMapper;
import org.eea.dataset.mapper.RecordValidationMapper;
import org.eea.dataset.mapper.TableNoRecordMapper;
import org.eea.dataset.mapper.TableValidationMapper;
import org.eea.dataset.mapper.TableValueMapper;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValidation;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.DatasetValidationRepository;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.persistence.data.repository.FieldValidationRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.RecordValidationRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.data.repository.TableValidationRepository;
import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.StatisticsRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
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
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.FieldValidationVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.RecordValidationVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
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

/**
 * The Class DatasetServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetServiceTest {

  /** The dataset service. */
  @InjectMocks
  private DatasetServiceImpl datasetService;

  /** The context. */
  @Mock
  private FileParseContextImpl context;

  /** The file parser factory. */
  @Mock
  private FileParserFactory fileParserFactory;

  /** The data set mapper. */
  @Mock
  private DataSetMapper dataSetMapper;

  /** The table value mapper. */
  @Mock
  private TableValueMapper tableValueMapper;

  /** The partition data set metabase repository. */
  @Mock
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  /** The data set metabase repository. */
  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The data collection repository. */
  @Mock
  private DataCollectionRepository dataCollectionRepository;

  /** The reporting dataset repository. */
  @Mock
  private ReportingDatasetRepository reportingDatasetRepository;

  /** The design dataset repository. */
  @Mock
  private DesignDatasetRepository designDatasetRepository;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The schemas repository. */
  @Mock
  private SchemasRepository schemasRepository;

  /** The dataset repository. */
  @Mock
  private DatasetRepository datasetRepository;

  /** The table repository. */
  @Mock
  private TableRepository tableRepository;

  /** The kafka sender. */
  @Mock
  private KafkaSender kafkaSender;

  /** The record repository. */
  @Mock
  private RecordRepository recordRepository;

  /** The record mapper. */
  @Mock
  private RecordMapper recordMapper;

  /** The record no validation mapper. */
  @Mock
  private RecordNoValidationMapper recordNoValidationMapper;

  /** The pageable. */
  @Mock
  private Pageable pageable;

  /** The field repository. */
  @Mock
  private FieldRepository fieldRepository;

  /** The table no record mapper. */
  @Mock
  private TableNoRecordMapper tableNoRecordMapper;

  /** The field validation repository. */
  @Mock
  private FieldValidationRepository fieldValidationRepository;

  /** The record validation repository. */
  @Mock
  private RecordValidationRepository recordValidationRepository;

  /** The table validation repository. */
  @Mock
  private TableValidationRepository tableValidationRepository;

  /** The table validation mapper. */
  @Mock
  private TableValidationMapper tableValidationMapper;

  /** The field validation mapper. */
  @Mock
  private FieldValidationMapper fieldValidationMapper;

  /** The record validation mapper. */
  @Mock
  private RecordValidationMapper recordValidationMapper;

  /** The validation repository. */
  @Mock
  private ValidationRepository validationRepository;

  /** The dataset validation repository. */
  @Mock
  private DatasetValidationRepository datasetValidationRepository;

  /** The file export factory. */
  @Mock
  private IFileExportFactory fileExportFactory;

  /** The context export. */
  @Mock
  private IFileExportContext contextExport;

  /** The file common. */
  @Mock
  private FileCommonUtils fileCommon;

  /** The statistics repository. */
  @Mock
  private StatisticsRepository statisticsRepository;

  /** The dataset metabase service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The representative controller zuul. */
  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The field no validation mapper. */
  @Mock
  private FieldNoValidationMapper fieldNoValidationMapper;

  /** The field value. */
  private FieldValue fieldValue;

  /** The record value. */
  private RecordValue recordValue;

  /** The record values. */
  private ArrayList<RecordValue> recordValues;

  /** The table value. */
  private TableValue tableValue;

  /** The table values. */
  private ArrayList<TableValue> tableValues;

  /** The dataset value. */
  private DatasetValue datasetValue;

  /** The data set VO. */
  private DataSetVO dataSetVO;

  /** The table V os. */
  private ArrayList<TableVO> tableVOs;

  /** The table VO. */
  private TableVO tableVO;


  /** The field list. */
  private List<FieldValue> fieldList;

  /** The sorted list. */
  private List<FieldValue> sortedList;

  /** The field. */
  private FieldValue field;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    fieldValue = new FieldValue();
    recordValues = new ArrayList<>();
    recordValue = new RecordValue();
    recordValue.setIdRecordSchema("");
    recordValue.setLevelError(ErrorTypeEnum.ERROR);
    recordValue.setFields(new ArrayList<>());
    tableValue = new TableValue();
    tableValue.setId(1L);
    tableValue.setTableValidations(new ArrayList<>());
    tableValue.setRecords(Arrays.asList(recordValue));
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

    fieldList = new ArrayList<>();
    sortedList = new ArrayList<>();
    field = new FieldValue();
    field.setId("1");
    field.setIdFieldSchema("123");
    field.setValue("123");

    sortedList.add(field);
    fieldList.add(field);

    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test process file throw exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void testProcessFileThrowException() throws Exception {
    final MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    datasetService.processFile(null, "fileOriginal", fileNoExtension.getInputStream(), null);
  }

  /**
   * Test process filename null throw exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void testProcessFilenameNullThrowException() throws Exception {
    final MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    datasetService.processFile(null, null, fileNoExtension.getInputStream(), null);
  }

  /**
   * Test process file bad extension throw exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void testProcessFileBadExtensionThrowException() throws Exception {
    final MockMultipartFile fileBadExtension =
        new MockMultipartFile("file", "fileOriginal.doc", "doc", "content".getBytes());
    datasetService.processFile(1L, "fileOriginal.doc", fileBadExtension.getInputStream(), null);
  }

  /**
   * Test process file throw exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void testProcessFileThrowException2() throws Exception {
    final MockMultipartFile fileNoExtension =
        new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    datasetService.processFile(1L, "fileOriginal", fileNoExtension.getInputStream(), null);
  }

  /**
   * Test process file empty dataset.
   *
   * @throws Exception the exception
   */
  @Test(expected = IOException.class)
  public void testProcessFileEmptyDataset() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(fileParserFactory.createContext(Mockito.any(), Mockito.any())).thenReturn(context);
    when(context.parse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(null);

    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null);
  }

  /**
   * Test process file empty partition metabase.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabase() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null);
  }

  /**
   * Test process file empty partition metabase xml.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabaseXml() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.xml", "xml", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null);
  }

  /**
   * Test process file empty partition metabase xls.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabaseXls() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.xls", "xls", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null);
  }

  /**
   * Test process file empty partition metabase xlsx.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabaseXlsx() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.xlsx", "xlsx", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    datasetService.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null);
  }



  /**
   * Test process file success update table.
   *
   * @throws Exception the exception
   */
  @Test
  public void testProcessFileSuccessUpdateTable() throws Exception {
    final MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "csv", "content".getBytes());

    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));

    when(fileParserFactory.createContext(Mockito.any(), Mockito.any())).thenReturn(context);
    final DataSetVO dataSetVO = new DataSetVO();
    dataSetVO.setId(1L);
    when(context.parse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(dataSetVO);
    assertEquals(dataSetVO,
        datasetService.processFile(1L, "fileOriginal.csv", file.getInputStream(), ""));
  }

  /**
   * Test save all records.
   */
  @Test
  public void testSaveAllRecords() {
    datasetService.saveAllRecords(1L, new ArrayList<>());
    Mockito.verify(recordRepository, times(1)).saveAll(Mockito.any());
  }

  /**
   * Test save table.
   */
  @Test
  public void testSaveTable() {
    Mockito.when(datasetRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new DatasetValue()));
    datasetService.saveTable(1L, new TableValue());
    Mockito.verify(tableRepository, times(1)).saveAndFlush(Mockito.any());
  }

  /**
   * Test delete import data.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteImportData() throws Exception {

    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    table.setIdTableSchema(new ObjectId());
    schema.setTableSchemas(Arrays.asList(table));
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    datasetService.deleteImportData(1L);
    Mockito.verify(recordRepository, times(1)).deleteRecordWithIdTableSchema(Mockito.any());
  }


  /**
   * Test delete data schema.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteDataSchema() throws Exception {
    doNothing().when(schemasRepository).deleteById(Mockito.any());
    datasetService.deleteDataSchema(new ObjectId().toString());
    Mockito.verify(schemasRepository, times(1)).deleteById(Mockito.any());
  }

  /**
   * Test get table values by id empty.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableValuesByIdEmpty() throws Exception {
    ErrorTypeEnum[] errorfilter = null;
    TableVO result = datasetService.getTableValuesById(1L, "mongoId", pageable, null, errorfilter);
    Assert.assertNotNull("result null", result);
    Assert.assertEquals("wrong number of records", Long.valueOf(0), result.getTotalRecords());
  }

  /**
   * Test get table values by id.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableValuesById() throws Exception {
    when(recordRepository.findByTableValueNoOrder(Mockito.any(), Mockito.any()))
        .thenReturn(recordValues);
    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING,
        ErrorTypeEnum.CORRECT, ErrorTypeEnum.BLOCKER, ErrorTypeEnum.INFO};
    datasetService.getTableValuesById(1L, "mongoId", pageable, null, errorfilter);
    Mockito.verify(recordNoValidationMapper, times(1)).entityListToClass(Mockito.any());
  }

  /**
   * Test get table values by id 2.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableValuesById2() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(1L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(tableVO);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.TEXT);
    ArrayList<FieldVO> fields = new ArrayList<>();
    fields.add(new FieldVO());
    recordVO.setFields(fields);
    recordVO.setId("1L");
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
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING};
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(recordValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);
    tableVO.setRecords(recordVOs);

    assertEquals(tableVO, datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable,
        listFields, errorfilter));
  }

  /**
   * Test get table values by id 3.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableValuesById3() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(1L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(tableVO);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.TEXT);
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
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING};
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(recordValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);
    tableVO.setRecords(recordVOs);

    assertEquals(tableVO, datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable,
        listFields, errorfilter));
  }

  /**
   * Test get table values by id 4.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableValuesById4() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(1L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(tableVO);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.TEXT);
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
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING};
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(recordValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);
    tableVO.setRecords(recordVOs);

    assertEquals(tableVO, datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable,
        listFields, errorfilter));
  }

  /**
   * Test get table values by id 5.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableValuesById5() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(0L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(tableVO);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.TEXT);
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
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING};
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(recordValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);
    tableVO.setRecords(recordVOs);
    assertEquals(tableVO, datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable,
        listFields, errorfilter));
  }

  /**
   * Test get table values by id 6.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableValuesById6() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(0L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(tableVO);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.TEXT);
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
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING};
    List<FieldValidationVO> valFieldVO = new ArrayList<FieldValidationVO>();
    FieldValidationVO fieldVO = new FieldValidationVO();
    ValidationVO validation = new ValidationVO();
    validation.setLevelError(ErrorTypeEnum.ERROR);
    fieldVO.setValidation(validation);
    valFieldVO.add(fieldVO);
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(valFieldVO);
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);
    tableVO.setRecords(recordVOs);
    assertEquals(tableVO, datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable,
        listFields, errorfilter));
  }

  /**
   * Test get table values by id 7.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableValuesById7() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(0L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(tableVO);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.TEXT);
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
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING};
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(null);
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);
    tableVO.setRecords(recordVOs);

    assertEquals(tableVO, datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable,
        listFields, errorfilter));
  }

  /**
   * Test get table values by id 8.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableValuesById8() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(0L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(tableVO);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.TEXT);
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
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING};
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(null);
    when(recordValidationMapper.entityListToClass(Mockito.any())).thenReturn(null);
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);
    tableVO.setRecords(recordVOs);
    assertEquals(tableVO, datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable,
        listFields, errorfilter));
  }

  /**
   * Test get table values by id 9.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableValuesById9() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(0L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(tableVO);
    List<RecordVO> recordVOs = new ArrayList<>();
    RecordVO recordVO = new RecordVO();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setType(DataType.TEXT);
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
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING};
    List<FieldValidationVO> valFieldVO = new ArrayList<FieldValidationVO>();
    FieldValidationVO fieldVO = new FieldValidationVO();
    ValidationVO validation = new ValidationVO();
    validation.setLevelError(ErrorTypeEnum.ERROR);
    fieldVO.setValidation(validation);
    valFieldVO.add(fieldVO);
    List<RecordValidationVO> valRecordsVO = new ArrayList<RecordValidationVO>();
    RecordValidationVO recordvalVO = new RecordValidationVO();
    ValidationVO validationRec = new ValidationVO();
    validationRec.setLevelError(ErrorTypeEnum.ERROR);
    recordvalVO.setValidation(validationRec);
    valRecordsVO.add(recordvalVO);
    when(fieldValidationRepository.findByFieldValue_RecordIdIn(Mockito.any())).thenReturn(fieldV);
    when(recordValidationRepository.findByRecordValueIdIn(Mockito.any())).thenReturn(recV);
    when(fieldValidationMapper.entityListToClass(Mockito.any())).thenReturn(valFieldVO);
    when(recordValidationMapper.entityListToClass(Mockito.any())).thenReturn(valRecordsVO);
    when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(fieldValue);
    tableVO.setRecords(recordVOs);
    assertEquals(tableVO, datasetService.getTableValuesById(1L, new ObjectId().toString(), pageable,
        listFields, errorfilter));
  }

  /**
   * Test get by id.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetById() throws Exception {
    DataSetVO datasetVOtemp = new DataSetVO();
    datasetVOtemp.setId(1L);
    datasetVOtemp.setTableVO(new ArrayList<>());
    when(dataSetMapper.entityToClass(Mockito.any())).thenReturn(datasetVOtemp);
    assertEquals("not equals", datasetVOtemp, datasetService.getById(1L));
  }

  /**
   * Test get by id success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetByIdSuccess() throws Exception {
    when(tableRepository.findAllTables()).thenReturn(tableValues);
    when(dataSetMapper.entityToClass(Mockito.any())).thenReturn(dataSetVO);
    DataSetVO result = datasetService.getById(1L);
    assertEquals("not equals", dataSetVO, result);
  }

  /**
   * Test get data flow id by id success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetDataFlowIdByIdSuccess() throws Exception {
    when(dataSetMetabaseRepository.findDataflowIdById(Mockito.any())).thenReturn(1L);
    Long result = datasetService.getDataFlowIdById(1L);
    assertNotNull("it shouldn't be null", result);
  }

  /**
   * Test update null exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void testUpdateNullException() throws Exception {
    datasetService.updateDataset(1L, null);
  }

  /**
   * Test update success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUpdateSuccess() throws Exception {
    when(dataSetMapper.classToEntity((Mockito.any(DataSetVO.class))))
        .thenReturn(new DatasetValue());
    when(datasetRepository.saveAndFlush(Mockito.any())).thenReturn(new DatasetValue());
    datasetService.updateDataset(1L, new DataSetVO());
    Mockito.verify(datasetRepository, times(1)).saveAndFlush(Mockito.any());

  }


  /**
   * Test get statistics success.
   *
   * @throws Exception the exception
   */
  /**
   * Test get statistics success 2.
   *
   * @throws Exception the exception
   */
  /**
   * Test get statistics success sanitize else.
   *
   * @throws Exception the exception
   */
  /**
   * Test get statistics success 3.
   *
   * @throws Exception the exception
   */

  /**
   * Test get table from any object id.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableFromAnyObjectId() throws Exception {

    when(recordRepository.findByIdAndTableValue_DatasetId_Id(Mockito.any(), Mockito.any()))
        .thenReturn(recordValue);

    datasetService.getPositionFromAnyObjectId("1L", 1L, EntityTypeEnum.RECORD);
    Mockito.verify(recordRepository, times(1)).findByIdAndTableValue_DatasetId_Id(Mockito.any(),
        Mockito.any());


  }

  /**
   * Test get table from any object id 2.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableFromAnyObjectId2() throws Exception {

    when(tableRepository.findByIdAndDatasetId_Id(Mockito.any(), Mockito.any()))
        .thenReturn(tableValue);

    datasetService.getPositionFromAnyObjectId("1", 1L, EntityTypeEnum.TABLE);
    Mockito.verify(tableRepository, times(1)).findByIdAndDatasetId_Id(Mockito.any(), Mockito.any());

  }

  /**
   * Test get table from any object id table.
   *
   * @throws Exception the exception
   */
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

    datasetService.getPositionFromAnyObjectId("1", 1L, EntityTypeEnum.TABLE);
    Mockito.verify(tableRepository, times(1)).findByIdAndDatasetId_Id(Mockito.any(), Mockito.any());

  }

  /**
   * Test get table from any object id 3.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableFromAnyObjectId3() throws Exception {

    when(fieldRepository.findByIdAndRecord_TableValue_DatasetId_Id(Mockito.any(), Mockito.any()))
        .thenReturn(fieldValue);

    datasetService.getPositionFromAnyObjectId("1L", 1L, EntityTypeEnum.FIELD);
    Mockito.verify(fieldRepository, times(1))
        .findByIdAndRecord_TableValue_DatasetId_Id(Mockito.any(), Mockito.any());

  }


  /**
   * Test delete table data.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteTableData() throws Exception {
    doNothing().when(recordRepository).deleteRecordWithIdTableSchema(Mockito.any());
    datasetService.deleteTableBySchema("", 1L);
    Mockito.verify(recordRepository, times(1)).deleteRecordWithIdTableSchema(Mockito.any());
  }

  /**
   * Update records null test.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void updateRecordsNullTest() throws Exception {
    datasetService.updateRecords(null, new ArrayList<RecordVO>());
  }

  /**
   * Update records null 2 test.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void updateRecordsNull2Test() throws Exception {
    datasetService.updateRecords(1L, null);
  }

  /**
   * Delete records null test.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void deleteRecordsNullTest() throws Exception {
    datasetService.deleteRecord(null, "1L");
  }

  /**
   * Delete records null 2 test.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void deleteRecordsNull2Test() throws Exception {
    datasetService.deleteRecord(1L, null);
  }

  /**
   * Delete records test.
   *
   * @throws Exception the exception
   */
  @Test
  public void deleteRecordsTest() throws Exception {
    doNothing().when(recordRepository).deleteRecordWithId(Mockito.any());
    datasetService.deleteRecord(1L, "1L");
    Mockito.verify(recordRepository, times(1)).deleteRecordWithId(Mockito.any());
  }

  /**
   * Update records test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateRecordsTest() throws EEAException {
    when(recordMapper.classListToEntity(Mockito.any())).thenReturn(recordValues);
    datasetService.updateRecords(1L, new ArrayList<RecordVO>());
    Mockito.verify(recordMapper, times(1)).classListToEntity(Mockito.any());
  }


  /**
   * Creates the records test.
   *
   * @throws EEAException the EEA exception
   */
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
    when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    when(representativeControllerZuul.findDataProviderById(Mockito.any()))
        .thenReturn(new DataProviderVO());
    datasetService.createRecords(1L, records, "");
    Mockito.verify(recordMapper, times(1)).classListToEntity(Mockito.any());
  }

  /** The thrown. */
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * Creates the records exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createRecordsExceptionTest() throws EEAException {
    thrown.expectMessage(EEAErrorMessage.TABLE_NOT_FOUND);
    datasetService.createRecords(1L, new ArrayList<RecordVO>(), "");
  }

  /**
   * Creates the records exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createRecordsException2Test() throws EEAException {
    thrown.expectMessage(EEAErrorMessage.RECORD_NOTFOUND);
    datasetService.createRecords(1L, new ArrayList<RecordVO>(), null);
  }

  /**
   * Creates the records exception 3 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createRecordsException3Test() throws EEAException {
    thrown.expectMessage(EEAErrorMessage.RECORD_NOTFOUND);
    datasetService.createRecords(1L, null, "");
  }

  /**
   * Creates the records exception 4 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createRecordsException4Test() throws EEAException {
    thrown.expectMessage(EEAErrorMessage.RECORD_NOTFOUND);
    datasetService.createRecords(null, new ArrayList<RecordVO>(), "");
  }


  /**
   * Export file test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void exportFileTest() throws EEAException, IOException {
    byte[] expectedResult = "".getBytes();
    ReportingDataset dataset = new ReportingDataset();
    dataset.setDataflowId(1L);
    // partition.setId(1L);
    // when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.any(),
    // Mockito.any())).thenReturn(Optional.of(partition));
    // when(reportingDatasetRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(dataset));
    when(fileExportFactory.createContext(Mockito.any())).thenReturn(contextExport);
    when(contextExport.fileWriter(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(expectedResult);
    assertEquals("not equals", expectedResult, datasetService.exportFile(1L, "csv", ""));
  }

  /**
   * Gets the file name test.
   *
   * @return the file name test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getFileNameTest() throws EEAException {
    ReportingDataset dataset = new ReportingDataset();
    when(reportingDatasetRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(dataset));
    when(fileCommon.getDataSetSchema(Mockito.any(), Mockito.any()))
        .thenReturn(new DataSetSchemaVO());
    when(fileCommon.getTableName(Mockito.any(), Mockito.any())).thenReturn("test");
    assertEquals("not equals", "test.csv", datasetService.getFileName("csv", "test", 1L));
  }

  /**
   * Test get file name exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetFileNameException() throws EEAException {

    thrown.expectMessage(EEAErrorMessage.DATASET_NOTFOUND);
    datasetService.getFileName("csv", "test", null);


  }

  /**
   * Update field test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldTest() throws EEAException {
    datasetService.updateField(1L, new FieldVO());
    Mockito.verify(fieldRepository, times(1)).saveValue(Mockito.any(), Mockito.any());
  }

  /**
   * Update field exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldException1Test() throws EEAException {
    thrown.expectMessage(EEAErrorMessage.FIELD_NOT_FOUND);
    datasetService.updateField(null, new FieldVO());
  }

  /**
   * Update field exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldException2Test() throws EEAException {
    thrown.expectMessage(EEAErrorMessage.FIELD_NOT_FOUND);
    datasetService.updateField(1L, null);
  }

  /**
   * Test insert schema.
   *
   * @throws EEAException the EEA exception
   */
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

  /**
   * Test find table id by table schema.
   */
  @Test
  public void testFindTableIdByTableSchema() {
    datasetService.findTableIdByTableSchema(1L, "5cf0e9b3b793310e9ceca190");
    Mockito.verify(tableRepository, times(1)).findIdByIdTableSchema(Mockito.any());
  }

  /**
   * Test delete record values to restore snapshot.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testDeleteRecordValuesToRestoreSnapshot() throws EEAException {
    datasetService.deleteRecordValuesToRestoreSnapshot(1L, 1L);
    Mockito.verify(recordRepository, times(1)).deleteRecordValuesToRestoreSnapshot(Mockito.any());
  }

  /**
   * Test save stats.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testSaveStats() throws EEAException {
    DataSetSchema schema = new DataSetSchema();
    schema.setTableSchemas(new ArrayList<>());
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);

    datasetService.saveStatistics(1L);
    Mockito.verify(statisticsRepository, times(1)).saveAll(Mockito.any());

  }

  /**
   * Delete table value test.
   */
  @Test
  public void deleteTableValueTest() {
    datasetService.deleteTableValue(1L, new ObjectId().toString());
    Mockito.verify(tableRepository, times(1)).deleteByIdTableSchema(Mockito.any());
  }

  /**
   * Save table propagation exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void saveTablePropagationExceptionTest() throws EEAException {
    datasetService.saveTablePropagation(1L, new TableSchemaVO());
  }

  /**
   * Save table propagation test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void saveTablePropagationTest() throws EEAException {
    Mockito.when(datasetRepository.findById(1L)).thenReturn(Optional.of(datasetValue));
    datasetService.saveTablePropagation(1L, new TableSchemaVO());
  }

  /**
   * Delete field values test.
   */
  @Test
  public void deleteFieldValuesTest() {
    Mockito.doNothing().when(fieldRepository).deleteByIdFieldSchemaNative(Mockito.any());
    datasetService.deleteFieldValues(1L, "<id>");
    Mockito.verify(fieldRepository, times(1)).deleteByIdFieldSchemaNative(Mockito.any());
  }

  /**
   * Update field value type test.
   */
  @Test
  public void updateFieldValueTypeTest() {
    Mockito.doNothing().when(fieldRepository).updateFieldValueType(Mockito.any(), Mockito.any());
    datasetService.updateFieldValueType(1L, "<id>", DataType.TEXT);
    Mockito.verify(fieldRepository, times(1)).updateFieldValueType(Mockito.any(), Mockito.any());
  }

  /**
   * Testdelete all table values.
   */
  @Test
  public void testdeleteAllTableValues() {
    datasetService.deleteAllTableValues(1L);
    Mockito.verify(tableRepository, times(1)).removeTableData(Mockito.any());
  }

  /**
   * Test is reporting dataset.
   */
  @Test
  public void testIsReportingDataset() {
    datasetService.isReportingDataset(1L);
    Mockito.verify(reportingDatasetRepository, times(1)).existsById(Mockito.any());
  }

  /**
   * Test save new field propagation.
   */
  @Test
  public void testSaveNewFieldPropagation() {
    when(recordRepository.findByTableValue_IdTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(recordValues);
    datasetService.saveNewFieldPropagation(1L, "5cf0e9b3b793310e9ceca190", pageable,
        "5cf0e9b3b793310e9ceca190", DataType.TEXT);
  }

  /**
   * Test prepare new field propagation.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testPrepareNewFieldPropagation() throws EEAException {

    FieldSchemaVO fs = new FieldSchemaVO();
    fs.setId("5cf0e9b3b793310e9ceca190");
    DataSetSchema schema = new DataSetSchema();
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    FieldSchema fieldSchema = new FieldSchema();
    fieldSchema.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    recordSchema.setFieldSchema(Arrays.asList(fieldSchema));
    tableSchema.setRecordSchema(recordSchema);
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableSchema);
    schema.setTableSchemas(tableSchemas);
    Mockito.when(datasetRepository.findById(1L)).thenReturn(Optional.of(datasetValue));
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    datasetService.prepareNewFieldPropagation(1L, fs);
  }

  /**
   * Test prepare new field propagation exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testPrepareNewFieldPropagationException() throws EEAException {

    FieldSchemaVO fs = new FieldSchemaVO();
    fs.setId("5cf0e9b3b793310e9ceca190");
    DataSetSchema schema = new DataSetSchema();
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    FieldSchema fieldSchema = new FieldSchema();
    fieldSchema.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    recordSchema.setFieldSchema(Arrays.asList(fieldSchema));
    tableSchema.setRecordSchema(recordSchema);
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableSchema);
    schema.setTableSchemas(tableSchemas);
    try {
      datasetService.prepareNewFieldPropagation(2L, fs);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.DATASET_NOTFOUND, e.getMessage());
    }
  }

  /**
   * Delete record values by provider.
   */
  @Test
  public void deleteRecordValuesByProvider() {
    datasetService.deleteRecordValuesByProvider(1L, "ES");
    Mockito.verify(recordRepository, times(1)).deleteByDataProviderCode(Mockito.any());
  }

  /**
   * Gets the field values referenced test cord lat.
   *
   * @return the field values referenced test cord lat
   */
  @Test
  public void getFieldValuesReferencedTestCordLat() {
    field.setType(DataType.COORDINATE_LAT);

    Mockito.when(
        datasetMetabaseService.getDatasetDestinationForeignRelation(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Mockito.when(fieldRepository.findByIdFieldSchemaAndValueContaining(Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(fieldList);

    datasetService.getFieldValuesReferenced(1L, "", "");
    Mockito.verify(fieldNoValidationMapper, times(1)).entityListToClass(sortedList);
  }

  /**
   * Gets the field values referenced test cord long.
   *
   * @return the field values referenced test cord long
   */
  @Test
  public void getFieldValuesReferencedTestCordLong() {
    field.setType(DataType.COORDINATE_LONG);

    Mockito.when(
        datasetMetabaseService.getDatasetDestinationForeignRelation(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Mockito.when(fieldRepository.findByIdFieldSchemaAndValueContaining(Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(fieldList);

    datasetService.getFieldValuesReferenced(1L, "", "");
    Mockito.verify(fieldNoValidationMapper, times(1)).entityListToClass(sortedList);
  }

  /**
   * Gets the field values referenced test number.
   *
   * @return the field values referenced test number
   */
  @Test
  public void getFieldValuesReferencedTestNumber() {
    field.setType(DataType.NUMBER_DECIMAL);

    Mockito.when(
        datasetMetabaseService.getDatasetDestinationForeignRelation(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Mockito.when(fieldRepository.findByIdFieldSchemaAndValueContaining(Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(fieldList);

    datasetService.getFieldValuesReferenced(1L, "", "");
    Mockito.verify(fieldNoValidationMapper, times(1)).entityListToClass(sortedList);
  }

  /**
   * Gets the field values referenced test date.
   *
   * @return the field values referenced test date
   */
  @Test
  public void getFieldValuesReferencedTestDate() {
    field.setType(DataType.DATE);

    Mockito.when(
        datasetMetabaseService.getDatasetDestinationForeignRelation(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Mockito.when(fieldRepository.findByIdFieldSchemaAndValueContaining(Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(fieldList);

    datasetService.getFieldValuesReferenced(1L, "", "");
    Mockito.verify(fieldNoValidationMapper, times(1)).entityListToClass(sortedList);
  }


  /**
   * Gets the field values referenced test string.
   *
   * @return the field values referenced test string
   */
  @Test
  public void getFieldValuesReferencedTestString() {
    field.setType(DataType.TEXT);

    Mockito.when(
        datasetMetabaseService.getDatasetDestinationForeignRelation(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Mockito.when(fieldRepository.findByIdFieldSchemaAndValueContaining(Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(fieldList);

    datasetService.getFieldValuesReferenced(1L, "", "");
    Mockito.verify(fieldNoValidationMapper, times(1)).entityListToClass(sortedList);
  }

  /**
   * Gets the referenced dataset id test.
   *
   * @return the referenced dataset id test
   */
  @Test
  public void getReferencedDatasetIdTest() {

    datasetService.getReferencedDatasetId(1L, "");
    Mockito.verify(datasetMetabaseService, times(1))
        .getDatasetDestinationForeignRelation(Mockito.any(), Mockito.any());
  }

  /**
   * Gets the dataset type enum return design test.
   *
   * @return the dataset type enum return design test
   */
  @Test
  public void getDatasetTypeEnumReturnDesignTest() {
    Mockito.when(designDatasetRepository.existsById(Mockito.any())).thenReturn(true);
    Assert.assertEquals(DatasetTypeEnum.DESIGN, datasetService.getDatasetType(1L));
  }

  /**
   * Gets the dataset type enum return reporting test.
   *
   * @return the dataset type enum return reporting test
   */
  @Test
  public void getDatasetTypeEnumReturnReportingTest() {
    Mockito.when(designDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    Mockito.when(reportingDatasetRepository.existsById(Mockito.any())).thenReturn(true);
    Assert.assertEquals(DatasetTypeEnum.REPORTING, datasetService.getDatasetType(1L));
  }

  /**
   * Gets the dataset type enum return collection test.
   *
   * @return the dataset type enum return collection test
   */
  @Test
  public void getDatasetTypeEnumReturnCollectionTest() {
    Mockito.when(designDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    Mockito.when(reportingDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    Mockito.when(dataCollectionRepository.existsById(Mockito.any())).thenReturn(true);
    Assert.assertEquals(DatasetTypeEnum.COLLECTION, datasetService.getDatasetType(1L));
  }

  /**
   * Gets the dataset type enum return null test.
   *
   * @return the dataset type enum return null test
   */
  @Test
  public void getDatasetTypeEnumReturnNullTest() {
    Mockito.when(designDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    Mockito.when(reportingDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    Mockito.when(dataCollectionRepository.existsById(Mockito.any())).thenReturn(false);
    Assert.assertNull(datasetService.getDatasetType(1L));
  }
}
