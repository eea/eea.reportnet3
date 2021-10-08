package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.mapper.FieldNoValidationMapper;
import org.eea.dataset.mapper.FieldValidationMapper;
import org.eea.dataset.mapper.RecordMapper;
import org.eea.dataset.mapper.RecordNoValidationMapper;
import org.eea.dataset.mapper.RecordValidationMapper;
import org.eea.dataset.mapper.TableNoRecordMapper;
import org.eea.dataset.mapper.TableValidationMapper;
import org.eea.dataset.mapper.TableValueMapper;
import org.eea.dataset.persistence.data.domain.AttachmentValue;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValidation;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.AttachmentRepository;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.DatasetValidationRepository;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.persistence.data.repository.FieldValidationRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.RecordValidationRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.data.repository.TableValidationRepository;
import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.data.sequence.FieldValueIdGenerator;
import org.eea.dataset.persistence.data.sequence.RecordValueIdGenerator;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReferenceDataset;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.ReferenceDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.StatisticsRepository;
import org.eea.dataset.persistence.metabase.repository.TestDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.domain.pkcatalogue.PkCatalogueSchema;
import org.eea.dataset.persistence.schemas.repository.PkCatalogueRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.file.FileParseContextImpl;
import org.eea.dataset.service.file.FileParserFactory;
import org.eea.dataset.service.file.interfaces.IFileExportContext;
import org.eea.dataset.service.file.interfaces.IFileExportFactory;
import org.eea.dataset.service.helper.UpdateRecordHelper;
import org.eea.dataset.service.impl.DatasetServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
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
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.io.KafkaSender;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.utils.LiteralConstants;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.junit.After;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

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

  /** The lock service. */
  @Mock
  private LockService lockService;

  /** The dataflow controller zull. */
  @Mock
  private DataFlowControllerZuul dataflowControllerZull;

  /** The integration controller. */
  @Mock
  private IntegrationControllerZuul integrationController;

  /** The update record helper. */
  @Mock
  private UpdateRecordHelper updateRecordHelper;

  /** The attachment repository. */
  @Mock
  private AttachmentRepository attachmentRepository;

  /** The pa M service. */
  @Mock
  private PaMService paMService;

  /** The pk catalogue repository. */
  @Mock
  private PkCatalogueRepository pkCatalogueRepository;

  /** The test dataset repository. */
  @Mock
  private TestDatasetRepository testDatasetRepository;

  /** The reference dataset repository. */
  @Mock
  private ReferenceDatasetRepository referenceDatasetRepository;

  /** The output stream. */
  @Mock
  private OutputStream outputStream;

  /** The record store controller zuul. */
  @Mock
  private RecordStoreControllerZuul recordStoreControllerZuul;

  /** The record value id generator. */
  @Mock
  private RecordValueIdGenerator recordValueIdGenerator;

  /** The field value id generator. */
  @Mock
  private FieldValueIdGenerator fieldValueIdGenerator;

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;

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
    ReflectionTestUtils.setField(datasetService, "pathPublicFile", "dato");
    fieldValue = new FieldValue();
    recordValues = new ArrayList<>();
    recordValue = new RecordValue();
    recordValue.setIdRecordSchema("");
    recordValue.setId("123");
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
    field.setIdFieldSchema("5cf0e9b3b793310e9ceca190");
    field.setValue("123");
    field.setRecord(recordValue);

    sortedList.add(field);
    fieldList.add(field);

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    MockitoAnnotations.openMocks(this);
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
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    tableSchema.setReadOnly(true);
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableSchema);
    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setTableSchemas(tableSchemas);
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    datasetService.deleteImportData(1L, false);
    Mockito.verify(recordRepository, times(0)).deleteRecordWithIdTableSchema(Mockito.any());
  }

  /**
   * Test delete import data fixed.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteImportDataFixed() throws Exception {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    List<FieldSchema> fieldSchemaList = new ArrayList<>();
    FieldSchema field = new FieldSchema();
    FieldSchema field2 = new FieldSchema();
    field.setReadOnly(true);
    field.setIdFieldSchema(new ObjectId());
    fieldSchemaList.add(field);
    fieldSchemaList.add(field2);
    record.setFieldSchema(fieldSchemaList);
    table.setIdTableSchema(new ObjectId());
    table.setFixedNumber(true);
    table.setRecordSchema(record);
    schema.setTableSchemas(Arrays.asList(table));
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    datasetService.deleteImportData(1L, false);
    Mockito.verify(fieldRepository, times(1)).saveAll(Mockito.any());
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
    TableVO result = datasetService.getTableValuesById(1L, "mongoId", pageable, null, errorfilter,
        null, null, null);
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
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(1L);
    when(recordRepository.findByTableValueNoOrder(Mockito.any(), Mockito.any()))
        .thenReturn(recordValues);
    when(recordNoValidationMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    ErrorTypeEnum[] errorfilter = new ErrorTypeEnum[] {ErrorTypeEnum.ERROR, ErrorTypeEnum.WARNING,
        ErrorTypeEnum.CORRECT, ErrorTypeEnum.BLOCKER, ErrorTypeEnum.INFO};
    datasetService.getTableValuesById(1L, "mongoId", pageable, null, errorfilter, null, null, null);
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
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(tableVO);
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
        listFields, errorfilter, null, null, null));
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
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(tableVO);
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
        listFields, errorfilter, null, null, null));
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
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(tableVO);
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
        listFields, errorfilter, null, null, null));
  }

  /**
   * Test get table values by id 5.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableValuesById5() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(1L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(tableVO);
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
        listFields, errorfilter, null, null, null));
  }

  /**
   * Test get table values by id 6.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableValuesById6() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(1L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(tableVO);
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
    List<FieldValidationVO> valFieldVO = new ArrayList<>();
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
        listFields, errorfilter, null, null, null));
  }

  /**
   * Test get table values by id 7.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableValuesById7() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(1L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(tableVO);
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
        listFields, errorfilter, null, null, null));
  }

  /**
   * Test get table values by id 8.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableValuesById8() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(1L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(tableVO);
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
        listFields, errorfilter, null, null, null));
  }

  /**
   * Test get table values by id 9.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTableValuesById9() throws Exception {
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(1L);
    when(recordRepository.findByTableValueWithOrder(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(tableVO);
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
    List<FieldValidationVO> valFieldVO = new ArrayList<>();
    FieldValidationVO fieldVO = new FieldValidationVO();
    ValidationVO validation = new ValidationVO();
    validation.setLevelError(ErrorTypeEnum.ERROR);
    fieldVO.setValidation(validation);
    valFieldVO.add(fieldVO);
    List<RecordValidationVO> valRecordsVO = new ArrayList<>();
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
        listFields, errorfilter, null, null, null));
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
    Mockito.when(recordRepository.findByTableValueNoOrder(Mockito.any(), Mockito.any()))
        .thenReturn(recordValues);
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
    fieldValue.setRecord(recordValue);
    when(fieldRepository.findByIdAndRecord_TableValue_DatasetId_Id(Mockito.any(), Mockito.any()))
        .thenReturn(fieldValue);
    Mockito.when(recordRepository.findByTableValueNoOrder(Mockito.any(), Mockito.any()))
        .thenReturn(recordValues);
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
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableSchema);
    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setTableSchemas(tableSchemas);
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    doNothing().when(recordRepository).deleteRecordWithIdTableSchema(Mockito.any());
    datasetService.deleteTableBySchema("5ce524fad31fc52540abae73", 1L);
    Mockito.verify(recordRepository, times(1)).deleteRecordWithIdTableSchema(Mockito.any());
  }

  /**
   * Delete records null 2 test.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void deleteRecordsNull2Test() throws Exception {
    datasetService.deleteRecord(1L, null, false);
  }

  /**
   * Delete records test.
   *
   * @throws Exception the exception
   */
  @Test
  public void deleteRecordsTest() throws Exception {
    doNothing().when(recordRepository).deleteRecordWithId(Mockito.any());
    datasetService.deleteRecord(1L, "1L", false);
    Mockito.verify(recordRepository, times(1)).deleteRecordWithId(Mockito.any());
  }

  /**
   * Delete record cascade test.
   *
   * @throws Exception the exception
   */
  @Test
  public void deleteRecordCascadeTest() throws Exception {
    List<Document> fieldSchemas = new ArrayList<>();
    Document fieldSchema = new Document();
    List<ObjectId> referenced = new ArrayList<>();
    PkCatalogueSchema pkCatalogueSchema = new PkCatalogueSchema();
    fieldSchema.put(LiteralConstants.PK, true);
    fieldSchema.put(LiteralConstants.ID, new ObjectId("5cf0e9b3b793310e9ceca190"));
    fieldSchemas.add(fieldSchema);
    referenced.add(new ObjectId("5cf0e9b3b793310e9ceca190"));
    pkCatalogueSchema.setReferenced(referenced);
    Document recordSchemaDocument = new Document();
    recordSchemaDocument.put(LiteralConstants.FIELD_SCHEMAS, fieldSchemas);
    recordValue.setFields(fieldList);
    when(recordRepository.findById(Mockito.anyString())).thenReturn(recordValue);
    when(schemasRepository.findRecordSchema(Mockito.any(), Mockito.any()))
        .thenReturn(recordSchemaDocument);
    when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(pkCatalogueSchema);
    when(fieldRepository.findByIdFieldSchemaIn(Mockito.any())).thenReturn(fieldList);
    doNothing().when(recordRepository).deleteRecordWithId(Mockito.any());
    doNothing().when(paMService).deleteGroups(Mockito.any(), Mockito.any());
    datasetService.deleteRecord(1L, "1L", true);
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
    when(dataSetMetabaseRepository.findDatasetSchemaIdById(1L))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    new DataSetSchema();
    datasetService.updateRecords(1L, new ArrayList<>(), false);
    Mockito.verify(recordMapper, times(1)).classListToEntity(Mockito.any());
  }

  /**
   * Update records cascade test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateRecordsCascadeTest() throws EEAException {
    List<Document> fieldSchemas = new ArrayList<>();
    Document fieldSchema = new Document();
    List<ObjectId> referenced = new ArrayList<>();
    PkCatalogueSchema pkCatalogueSchema = new PkCatalogueSchema();
    fieldSchema.put(LiteralConstants.PK, true);
    fieldSchema.put(LiteralConstants.ID, new ObjectId("5cf0e9b3b793310e9ceca190"));
    fieldSchemas.add(fieldSchema);
    referenced.add(new ObjectId("5cf0e9b3b793310e9ceca190"));
    pkCatalogueSchema.setReferenced(referenced);
    Document recordSchemaDocument = new Document();
    recordSchemaDocument.put(LiteralConstants.FIELD_SCHEMAS, fieldSchemas);
    recordValue.setFields(fieldList);
    when(recordRepository.findById(Mockito.anyString())).thenReturn(recordValue);
    when(schemasRepository.findRecordSchema(Mockito.any(), Mockito.any()))
        .thenReturn(recordSchemaDocument);
    when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(pkCatalogueSchema);
    when(fieldRepository.findByIdFieldSchemaIn(Mockito.any())).thenReturn(fieldList);

    recordValue.getFields().get(0).setValue("125555");
    FieldValue dataEnd = new FieldValue();
    dataEnd.setValue("123");
    when(fieldRepository.findById(Mockito.anyString())).thenReturn(dataEnd);

    when(dataSetMetabaseRepository.findDatasetSchemaIdById(1L))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    when(recordMapper.classListToEntity(Mockito.any())).thenReturn(recordValues);
    datasetService.updateRecords(1L, new ArrayList<>(), true);
    Mockito.verify(recordMapper, times(1)).classListToEntity(Mockito.any());
  }

  /**
   * Update records cascade test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateRecordsCascadeTypeTest() throws EEAException {
    List<Document> fieldSchemas = new ArrayList<>();
    Document fieldSchema = new Document();
    List<ObjectId> referenced = new ArrayList<>();
    PkCatalogueSchema pkCatalogueSchema = new PkCatalogueSchema();
    fieldSchema.put(LiteralConstants.PK, true);
    fieldSchema.put(LiteralConstants.ID, new ObjectId("5cf0e9b3b793310e9ceca190"));
    fieldSchemas.add(fieldSchema);
    referenced.add(new ObjectId("5cf0e9b3b793310e9ceca190"));
    pkCatalogueSchema.setReferenced(referenced);
    Document recordSchemaDocument = new Document();
    recordSchemaDocument.put(LiteralConstants.FIELD_SCHEMAS, fieldSchemas);
    recordValue.setFields(fieldList);
    when(recordRepository.findById(Mockito.anyString())).thenReturn(recordValue);
    when(schemasRepository.findRecordSchema(Mockito.any(), Mockito.any()))
        .thenReturn(recordSchemaDocument);
    when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(pkCatalogueSchema);
    when(fieldRepository.findByIdFieldSchemaIn(Mockito.any())).thenReturn(fieldList);

    FieldValue point = new FieldValue();
    point.setId("1");
    point.setIdFieldSchema("5cf0e9b3b793310e9ceca190");
    point.setValue("123");
    point.setRecord(recordValue);
    point.setType(DataType.POINT);
    FieldValue polygon = new FieldValue();
    polygon.setId("1");
    polygon.setIdFieldSchema("5cf0e9b3b793310e9ceca190");
    polygon.setValue("123");
    polygon.setRecord(recordValue);
    polygon.setType(DataType.POLYGON);
    FieldValue multipoint = new FieldValue();
    multipoint.setId("1");
    multipoint.setIdFieldSchema("5cf0e9b3b793310e9ceca190");
    multipoint.setValue("123");
    multipoint.setRecord(recordValue);
    multipoint.setType(DataType.MULTIPOINT);
    FieldValue multiline = new FieldValue();
    multiline.setId("1");
    multiline.setIdFieldSchema("5cf0e9b3b793310e9ceca190");
    multiline.setValue("123");
    multiline.setRecord(recordValue);
    multiline.setType(DataType.MULTILINESTRING);
    FieldValue multipolygon = new FieldValue();
    multipolygon.setId("1");
    multipolygon.setIdFieldSchema("5cf0e9b3b793310e9ceca190");
    multipolygon.setValue("123");
    multipolygon.setRecord(recordValue);
    multipolygon.setType(DataType.MULTIPOLYGON);
    FieldValue geometrycollection = new FieldValue();
    geometrycollection.setId("1");
    geometrycollection.setIdFieldSchema("5cf0e9b3b793310e9ceca190");
    geometrycollection.setValue("123");
    geometrycollection.setRecord(recordValue);
    geometrycollection.setType(DataType.GEOMETRYCOLLECTION);
    recordValue.getFields().add(point);
    recordValue.getFields().add(polygon);
    recordValue.getFields().add(multipoint);
    recordValue.getFields().add(multiline);
    recordValue.getFields().add(multipolygon);
    recordValue.getFields().add(geometrycollection);
    recordValue.getFields().get(0).setValue("125555");
    recordValue.getFields().get(0).setType(DataType.LINESTRING);
    FieldValue dataEnd = new FieldValue();
    dataEnd.setValue("123");
    when(fieldRepository.findById(Mockito.anyString())).thenReturn(dataEnd);

    when(dataSetMetabaseRepository.findDatasetSchemaIdById(1L))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    when(recordMapper.classListToEntity(Mockito.any())).thenReturn(recordValues);
    datasetService.updateRecords(1L, new ArrayList<>(), true);
    Mockito.verify(recordMapper, times(1)).classListToEntity(Mockito.any());
  }

  /**
   * The thrown.
   */
  @Rule
  public ExpectedException thrown = ExpectedException.none();

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
    when(fileExportFactory.createContext(Mockito.any())).thenReturn(contextExport);
    when(contextExport.fileWriter(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean(),
        Mockito.anyBoolean())).thenReturn(expectedResult);
    assertEquals("not equals", expectedResult,
        datasetService.exportFile(1L, FileTypeEnum.CSV.getValue(), ""));
  }

  /**
   * Update field test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldTest() throws EEAException {
    Document fieldSchema = new Document();
    fieldSchema.put(LiteralConstants.READ_ONLY, Boolean.FALSE);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);
    datasetService.updateField(1L, new FieldVO(), false);
    Mockito.verify(fieldRepository, times(1)).saveValue(Mockito.any(), Mockito.any());
  }

  /**
   * Update field PK test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldPKTest() throws EEAException {
    FieldVO fieldVO = new FieldVO();
    fieldVO.setId("5cf0e9b3b793310e9ceca190");
    fieldVO.setValue("123");
    FieldValue dataEnd = new FieldValue();
    dataEnd.setValue("123");
    dataEnd.setRecord(recordValue);

    List<Document> fieldSchemas = new ArrayList<>();
    Document fieldSchema = new Document();
    List<ObjectId> referenced = new ArrayList<>();
    PkCatalogueSchema pkCatalogueSchema = new PkCatalogueSchema();
    fieldSchema.put(LiteralConstants.PK, true);
    fieldSchema.put(LiteralConstants.ID, new ObjectId("5cf0e9b3b793310e9ceca190"));
    fieldSchema.put("headerName", "ListOfSinglePams");
    fieldSchemas.add(fieldSchema);
    referenced.add(new ObjectId("5cf0e9b3b793310e9ceca190"));
    pkCatalogueSchema.setReferenced(referenced);
    Document recordSchemaDocument = new Document();
    recordSchemaDocument.put(LiteralConstants.FIELD_SCHEMAS, fieldSchemas);
    recordValue.setFields(fieldList);
    fieldSchema.put(LiteralConstants.READ_ONLY, Boolean.FALSE);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);

    when(schemasRepository.findRecordSchema(Mockito.any(), Mockito.any()))
        .thenReturn(recordSchemaDocument);
    when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(pkCatalogueSchema);
    when(fieldRepository.findByIdFieldSchemaIn(Mockito.any())).thenReturn(fieldList);

    when(fieldRepository.findById("5cf0e9b3b793310e9ceca190")).thenReturn(dataEnd);

    when(dataSetMetabaseRepository.findDatasetSchemaIdById(1L))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    datasetService.updateField(1L, fieldVO, true);
    Mockito.verify(fieldRepository, times(2)).saveValue(Mockito.any(), Mockito.any());
  }

  /**
   * Update field type test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldTypeTest() throws EEAException {
    FieldVO fieldVO = new FieldVO();
    fieldVO.setId("5cf0e9b3b793310e9ceca190");
    fieldVO.setType(DataType.POINT);
    FieldValue dataEnd = new FieldValue();
    dataEnd.setValue("123");
    dataEnd.setRecord(recordValue);

    List<Document> fieldSchemas = new ArrayList<>();
    Document fieldSchema = new Document();
    List<ObjectId> referenced = new ArrayList<>();
    PkCatalogueSchema pkCatalogueSchema = new PkCatalogueSchema();
    fieldSchema.put(LiteralConstants.PK, true);
    fieldSchema.put(LiteralConstants.ID, new ObjectId("5cf0e9b3b793310e9ceca190"));
    fieldSchema.put("headerName", "ListOfSinglePams");
    fieldSchemas.add(fieldSchema);
    referenced.add(new ObjectId("5cf0e9b3b793310e9ceca190"));
    pkCatalogueSchema.setReferenced(referenced);
    Document recordSchemaDocument = new Document();
    recordSchemaDocument.put(LiteralConstants.FIELD_SCHEMAS, fieldSchemas);
    recordValue.setFields(fieldList);
    fieldSchema.put(LiteralConstants.READ_ONLY, Boolean.FALSE);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);

    when(schemasRepository.findRecordSchema(Mockito.any(), Mockito.any()))
        .thenReturn(recordSchemaDocument);
    when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(pkCatalogueSchema);
    when(fieldRepository.findByIdFieldSchemaIn(Mockito.any())).thenReturn(fieldList);

    when(fieldRepository.findById("5cf0e9b3b793310e9ceca190")).thenReturn(dataEnd);

    when(dataSetMetabaseRepository.findDatasetSchemaIdById(1L))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    datasetService.updateField(1L, fieldVO, true);
    Mockito.verify(fieldRepository, times(2)).saveValue(Mockito.any(), Mockito.any());
  }

  /**
   * Update field line test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldLineTest() throws EEAException {
    FieldVO fieldVO = new FieldVO();
    fieldVO.setId("5cf0e9b3b793310e9ceca190");
    fieldVO.setType(DataType.LINESTRING);
    FieldValue dataEnd = new FieldValue();
    dataEnd.setValue("123");
    dataEnd.setRecord(recordValue);

    List<Document> fieldSchemas = new ArrayList<>();
    Document fieldSchema = new Document();
    List<ObjectId> referenced = new ArrayList<>();
    PkCatalogueSchema pkCatalogueSchema = new PkCatalogueSchema();
    fieldSchema.put(LiteralConstants.PK, true);
    fieldSchema.put(LiteralConstants.ID, new ObjectId("5cf0e9b3b793310e9ceca190"));
    fieldSchema.put("headerName", "ListOfSinglePams");
    fieldSchemas.add(fieldSchema);
    referenced.add(new ObjectId("5cf0e9b3b793310e9ceca190"));
    pkCatalogueSchema.setReferenced(referenced);
    Document recordSchemaDocument = new Document();
    recordSchemaDocument.put(LiteralConstants.FIELD_SCHEMAS, fieldSchemas);
    recordValue.setFields(fieldList);
    fieldSchema.put(LiteralConstants.READ_ONLY, Boolean.FALSE);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);

    when(schemasRepository.findRecordSchema(Mockito.any(), Mockito.any()))
        .thenReturn(recordSchemaDocument);
    when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(pkCatalogueSchema);
    when(fieldRepository.findByIdFieldSchemaIn(Mockito.any())).thenReturn(fieldList);

    when(fieldRepository.findById("5cf0e9b3b793310e9ceca190")).thenReturn(dataEnd);

    when(dataSetMetabaseRepository.findDatasetSchemaIdById(1L))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    datasetService.updateField(1L, fieldVO, true);
    Mockito.verify(fieldRepository, times(2)).saveValue(Mockito.any(), Mockito.any());
  }

  /**
   * Update field polygon test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldPolygonTest() throws EEAException {
    FieldVO fieldVO = new FieldVO();
    fieldVO.setId("5cf0e9b3b793310e9ceca190");
    fieldVO.setType(DataType.POLYGON);
    FieldValue dataEnd = new FieldValue();
    dataEnd.setValue("123");
    dataEnd.setRecord(recordValue);

    List<Document> fieldSchemas = new ArrayList<>();
    Document fieldSchema = new Document();
    List<ObjectId> referenced = new ArrayList<>();
    PkCatalogueSchema pkCatalogueSchema = new PkCatalogueSchema();
    fieldSchema.put(LiteralConstants.PK, true);
    fieldSchema.put(LiteralConstants.ID, new ObjectId("5cf0e9b3b793310e9ceca190"));
    fieldSchema.put("headerName", "ListOfSinglePams");
    fieldSchemas.add(fieldSchema);
    referenced.add(new ObjectId("5cf0e9b3b793310e9ceca190"));
    pkCatalogueSchema.setReferenced(referenced);
    Document recordSchemaDocument = new Document();
    recordSchemaDocument.put(LiteralConstants.FIELD_SCHEMAS, fieldSchemas);
    recordValue.setFields(fieldList);
    fieldSchema.put(LiteralConstants.READ_ONLY, Boolean.FALSE);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);

    when(schemasRepository.findRecordSchema(Mockito.any(), Mockito.any()))
        .thenReturn(recordSchemaDocument);
    when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(pkCatalogueSchema);
    when(fieldRepository.findByIdFieldSchemaIn(Mockito.any())).thenReturn(fieldList);

    when(fieldRepository.findById("5cf0e9b3b793310e9ceca190")).thenReturn(dataEnd);

    when(dataSetMetabaseRepository.findDatasetSchemaIdById(1L))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    datasetService.updateField(1L, fieldVO, true);
    Mockito.verify(fieldRepository, times(2)).saveValue(Mockito.any(), Mockito.any());
  }

  /**
   * Update field multipoint type test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldMultipointTypeTest() throws EEAException {
    FieldVO fieldVO = new FieldVO();
    fieldVO.setId("5cf0e9b3b793310e9ceca190");
    fieldVO.setType(DataType.MULTIPOINT);
    FieldValue dataEnd = new FieldValue();
    dataEnd.setValue("123");
    dataEnd.setRecord(recordValue);

    List<Document> fieldSchemas = new ArrayList<>();
    Document fieldSchema = new Document();
    List<ObjectId> referenced = new ArrayList<>();
    PkCatalogueSchema pkCatalogueSchema = new PkCatalogueSchema();
    fieldSchema.put(LiteralConstants.PK, true);
    fieldSchema.put(LiteralConstants.ID, new ObjectId("5cf0e9b3b793310e9ceca190"));
    fieldSchema.put("headerName", "ListOfSinglePams");
    fieldSchemas.add(fieldSchema);
    referenced.add(new ObjectId("5cf0e9b3b793310e9ceca190"));
    pkCatalogueSchema.setReferenced(referenced);
    Document recordSchemaDocument = new Document();
    recordSchemaDocument.put(LiteralConstants.FIELD_SCHEMAS, fieldSchemas);
    recordValue.setFields(fieldList);
    fieldSchema.put(LiteralConstants.READ_ONLY, Boolean.FALSE);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);

    when(schemasRepository.findRecordSchema(Mockito.any(), Mockito.any()))
        .thenReturn(recordSchemaDocument);
    when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(pkCatalogueSchema);
    when(fieldRepository.findByIdFieldSchemaIn(Mockito.any())).thenReturn(fieldList);

    when(fieldRepository.findById("5cf0e9b3b793310e9ceca190")).thenReturn(dataEnd);

    when(dataSetMetabaseRepository.findDatasetSchemaIdById(1L))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    datasetService.updateField(1L, fieldVO, true);
    Mockito.verify(fieldRepository, times(2)).saveValue(Mockito.any(), Mockito.any());
  }

  /**
   * Update field multiline test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldMultilineTest() throws EEAException {
    FieldVO fieldVO = new FieldVO();
    fieldVO.setId("5cf0e9b3b793310e9ceca190");
    fieldVO.setType(DataType.MULTILINESTRING);
    FieldValue dataEnd = new FieldValue();
    dataEnd.setValue("123");
    dataEnd.setRecord(recordValue);

    List<Document> fieldSchemas = new ArrayList<>();
    Document fieldSchema = new Document();
    List<ObjectId> referenced = new ArrayList<>();
    PkCatalogueSchema pkCatalogueSchema = new PkCatalogueSchema();
    fieldSchema.put(LiteralConstants.PK, true);
    fieldSchema.put(LiteralConstants.ID, new ObjectId("5cf0e9b3b793310e9ceca190"));
    fieldSchema.put("headerName", "ListOfSinglePams");
    fieldSchemas.add(fieldSchema);
    referenced.add(new ObjectId("5cf0e9b3b793310e9ceca190"));
    pkCatalogueSchema.setReferenced(referenced);
    Document recordSchemaDocument = new Document();
    recordSchemaDocument.put(LiteralConstants.FIELD_SCHEMAS, fieldSchemas);
    recordValue.setFields(fieldList);
    fieldSchema.put(LiteralConstants.READ_ONLY, Boolean.FALSE);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);

    when(schemasRepository.findRecordSchema(Mockito.any(), Mockito.any()))
        .thenReturn(recordSchemaDocument);
    when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(pkCatalogueSchema);
    when(fieldRepository.findByIdFieldSchemaIn(Mockito.any())).thenReturn(fieldList);

    when(fieldRepository.findById("5cf0e9b3b793310e9ceca190")).thenReturn(dataEnd);

    when(dataSetMetabaseRepository.findDatasetSchemaIdById(1L))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    datasetService.updateField(1L, fieldVO, true);
    Mockito.verify(fieldRepository, times(2)).saveValue(Mockito.any(), Mockito.any());
  }

  /**
   * Update field multipolygon test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldMultipolygonTest() throws EEAException {
    FieldVO fieldVO = new FieldVO();
    fieldVO.setId("5cf0e9b3b793310e9ceca190");
    fieldVO.setType(DataType.MULTIPOLYGON);
    FieldValue dataEnd = new FieldValue();
    dataEnd.setValue("123");
    dataEnd.setRecord(recordValue);

    List<Document> fieldSchemas = new ArrayList<>();
    Document fieldSchema = new Document();
    List<ObjectId> referenced = new ArrayList<>();
    PkCatalogueSchema pkCatalogueSchema = new PkCatalogueSchema();
    fieldSchema.put(LiteralConstants.PK, true);
    fieldSchema.put(LiteralConstants.ID, new ObjectId("5cf0e9b3b793310e9ceca190"));
    fieldSchema.put("headerName", "ListOfSinglePams");
    fieldSchemas.add(fieldSchema);
    referenced.add(new ObjectId("5cf0e9b3b793310e9ceca190"));
    pkCatalogueSchema.setReferenced(referenced);
    Document recordSchemaDocument = new Document();
    recordSchemaDocument.put(LiteralConstants.FIELD_SCHEMAS, fieldSchemas);
    recordValue.setFields(fieldList);
    fieldSchema.put(LiteralConstants.READ_ONLY, Boolean.FALSE);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);

    when(schemasRepository.findRecordSchema(Mockito.any(), Mockito.any()))
        .thenReturn(recordSchemaDocument);
    when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(pkCatalogueSchema);
    when(fieldRepository.findByIdFieldSchemaIn(Mockito.any())).thenReturn(fieldList);

    when(fieldRepository.findById("5cf0e9b3b793310e9ceca190")).thenReturn(dataEnd);

    when(dataSetMetabaseRepository.findDatasetSchemaIdById(1L))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    datasetService.updateField(1L, fieldVO, true);
    Mockito.verify(fieldRepository, times(2)).saveValue(Mockito.any(), Mockito.any());
  }

  /**
   * Update field geometry test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldGeometryTest() throws EEAException {
    FieldVO fieldVO = new FieldVO();
    fieldVO.setId("5cf0e9b3b793310e9ceca190");
    fieldVO.setType(DataType.GEOMETRYCOLLECTION);
    FieldValue dataEnd = new FieldValue();
    dataEnd.setValue("123");
    dataEnd.setRecord(recordValue);

    List<Document> fieldSchemas = new ArrayList<>();
    Document fieldSchema = new Document();
    List<ObjectId> referenced = new ArrayList<>();
    PkCatalogueSchema pkCatalogueSchema = new PkCatalogueSchema();
    fieldSchema.put(LiteralConstants.PK, true);
    fieldSchema.put(LiteralConstants.ID, new ObjectId("5cf0e9b3b793310e9ceca190"));
    fieldSchema.put("headerName", "ListOfSinglePams");
    fieldSchemas.add(fieldSchema);
    referenced.add(new ObjectId("5cf0e9b3b793310e9ceca190"));
    pkCatalogueSchema.setReferenced(referenced);
    Document recordSchemaDocument = new Document();
    recordSchemaDocument.put(LiteralConstants.FIELD_SCHEMAS, fieldSchemas);
    recordValue.setFields(fieldList);
    fieldSchema.put(LiteralConstants.READ_ONLY, Boolean.FALSE);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);

    when(schemasRepository.findRecordSchema(Mockito.any(), Mockito.any()))
        .thenReturn(recordSchemaDocument);
    when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(pkCatalogueSchema);
    when(fieldRepository.findByIdFieldSchemaIn(Mockito.any())).thenReturn(fieldList);

    when(fieldRepository.findById("5cf0e9b3b793310e9ceca190")).thenReturn(dataEnd);

    when(dataSetMetabaseRepository.findDatasetSchemaIdById(1L))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    datasetService.updateField(1L, fieldVO, true);
    Mockito.verify(fieldRepository, times(2)).saveValue(Mockito.any(), Mockito.any());
  }



  /**
   * Update field test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateFieldReadOnlyTest() throws EEAException {
    Document fieldSchema = new Document();
    fieldSchema.put(LiteralConstants.READ_ONLY, Boolean.TRUE);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);
    try {
      datasetService.updateField(1L, new FieldVO(), false);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.FIELD_READ_ONLY, e.getMessage());
      throw e;
    }
  }


  /**
   * Update field test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldMultiSelectTest() throws EEAException {
    Document fieldSchema = new Document();
    fieldSchema.put(LiteralConstants.PK_HAS_MULTIPLE_VALUES, Boolean.TRUE);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);
    FieldVO multiselectField = new FieldVO();
    multiselectField.setType(DataType.MULTISELECT_CODELIST);
    multiselectField.setValue("A;A");
    datasetService.updateField(1L, multiselectField, false);
    Mockito.verify(fieldRepository, times(1)).saveValue(Mockito.any(), Mockito.any());
  }

  /**
   * Update field test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldLinkMulti() throws EEAException {
    FieldVO multiselectField = new FieldVO();
    multiselectField.setType(DataType.LINK);
    multiselectField.setValue("a,a");
    Document fieldSchema = new Document();
    fieldSchema.put(LiteralConstants.PK_HAS_MULTIPLE_VALUES, Boolean.TRUE);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);
    datasetService.updateField(1L, multiselectField, false);
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
    datasetService.updateField(null, new FieldVO(), false);
  }

  /**
   * Update field exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldException2Test() throws EEAException {
    thrown.expectMessage(EEAErrorMessage.FIELD_NOT_FOUND);
    datasetService.updateField(1L, null, false);
  }

  /**
   * Update field exception field schema.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldExceptionFieldSchema() throws EEAException {
    thrown.expectMessage(EEAErrorMessage.FIELD_SCHEMA_ID_NOT_FOUND);
    datasetService.updateField(1L, new FieldVO(), false);
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
    DataSetMetabase dsMetabase = new DataSetMetabase();
    dsMetabase.setDataSetName("prueba");
    dsMetabase.setId(1L);
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    when(dataSetMetabaseRepository.findById(Mockito.any())).thenReturn(Optional.of(dsMetabase));

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
    Mockito.verify(tableRepository, times(1)).saveAndFlush(Mockito.any());
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
  public void deleteAllTableValuesTest() {
    datasetService.deleteAllTableValues(1L);
    Mockito.verify(tableRepository, times(1)).removeTableData(Mockito.any());
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
    Mockito.verify(fieldRepository, times(1)).saveAll(Mockito.any());
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
    Mockito.verify(kafkaSenderUtils, times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
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
   * Gets the field values referenced test number.
   *
   * @return the field values referenced test number
   * @throws PSQLException the PSQL exception
   * @throws EEAException the EEA exception
   * @throws SecurityException the security exception
   */
  @Test
  public void getFieldValuesReferencedTest() throws PSQLException, EEAException {

    Document doc = new Document();
    doc.put("typeData", DataType.LINK.getValue());
    Document referencedDoc = new Document();
    referencedDoc.put("idDatasetSchema", "5ce524fad31fc52540abae73");
    referencedDoc.put("idPk", "5ce524fad31fc52540ab" + "ae73");
    doc.put("referencedField", referencedDoc);
    FieldVO fieldVO = new FieldVO();
    fieldVO.setId("5ce524fad31fc52540abae73");
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any())).thenReturn(doc);
    Mockito.when(
        datasetMetabaseService.getDatasetDestinationForeignRelation(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Mockito
        .when(fieldRepository.findByIdFieldSchemaWithTagOrdered(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(Arrays.asList(fieldVO));

    Mockito.when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any()))
        .thenReturn(new FieldValue());

    Assert.assertEquals(Arrays.asList(fieldVO),
        datasetService.getFieldValuesReferenced(1L, "", "", "", "", null));
  }

  /**
   * Gets the field values referenced catch test.
   *
   * @return the field values referenced catch test
   * @throws PSQLException the PSQL exception
   * @throws EEAException the EEA exception
   */
  @Test
  public void getFieldValuesReferencedCatchTest() throws PSQLException, EEAException {
    Document doc = new Document();
    doc.put("typeData", DataType.LINK.getValue());
    Document referencedDoc = new Document();
    referencedDoc.put("idDatasetSchema", "5ce524fad31fc52540abae73");
    referencedDoc.put("idPk", "5ce524fad31fc52540ab" + "ae73");
    doc.put("referencedField", referencedDoc);
    Document tableSchema = new Document();
    tableSchema.put("nameTableSchema", "");
    tableSchema.put("_id", "");
    FieldVO fieldVO = new FieldVO();
    fieldVO.setId("5ce524fad31fc52540abae73");
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any())).thenReturn(doc);
    Mockito.when(
        datasetMetabaseService.getDatasetDestinationForeignRelation(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Mockito.when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any())).thenReturn(field);
    Mockito
        .when(fieldRepository.findByIdFieldSchemaWithTagOrdered(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenThrow(DataIntegrityViolationException.class);
    Mockito.when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(tableSchema);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    datasetService.getFieldValuesReferenced(1L, "", "", "", "", 1);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Gets the field values referenced label test.
   *
   * @return the field values referenced label test
   * @throws PSQLException the PSQL exception
   * @throws EEAException the EEA exception
   */
  @Test
  public void getFieldValuesReferencedLabelTest() throws PSQLException, EEAException {

    Document doc = new Document();
    doc.put("typeData", DataType.LINK.getValue());
    Document referencedDoc = new Document();
    referencedDoc.put("idDatasetSchema", "5ce524fad31fc52540abae73");
    referencedDoc.put("idPk", "5ce524fad31fc52540ab" + "ae73");
    referencedDoc.put("labelId", "labelId");
    referencedDoc.put("linkedConditionalFieldId", "linkedConditionalFieldId");
    doc.put("referencedField", referencedDoc);
    FieldVO fieldVO = new FieldVO();
    fieldVO.setId("5ce524fad31fc52540abae73");
    new ArrayList<>();
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any())).thenReturn(doc);
    Mockito.when(
        datasetMetabaseService.getDatasetDestinationForeignRelation(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Mockito
        .when(fieldRepository.findByIdFieldSchemaWithTagOrdered(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(Arrays.asList(fieldVO));

    Mockito.when(fieldRepository.findFirstTypeByIdFieldSchema(Mockito.any()))
        .thenReturn(new FieldValue());

    Assert.assertEquals(Arrays.asList(fieldVO),
        datasetService.getFieldValuesReferenced(1L, "", "", "", "", 50));
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
   * Test get table read only.
   */
  @Test
  public void testGetTableReadOnly() {

    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    table.setReadOnly(true);
    table.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    schema.setTableSchemas(Arrays.asList(table));

    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    datasetService.getTableReadOnly(1L, "5ce524fad31fc52540abae73", EntityTypeEnum.TABLE);
    Mockito.verify(schemasRepository, times(1)).findByIdDataSetSchema(Mockito.any());
  }


  /**
   * Test get table fixed records.
   */
  @Test
  public void testGetTableFixedRecords() {

    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    table.setFixedNumber(true);
    table.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    schema.setTableSchemas(Arrays.asList(table));

    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    datasetService.getTableFixedNumberOfRecords(1L, "5ce524fad31fc52540abae73",
        EntityTypeEnum.TABLE);
    Mockito.verify(schemasRepository, times(1)).findByIdDataSetSchema(Mockito.any());
  }


  /**
   * Test get table read only with non matching.
   */
  @Test
  public void testGetTableReadOnlyWithNonMatching() {

    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    table.setReadOnly(false);
    table.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    schema.setTableSchemas(Arrays.asList(table));

    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    datasetService.getTableReadOnly(1L, "5ce524fad31fc52540abae73", EntityTypeEnum.TABLE);
    Mockito.verify(schemasRepository, times(1)).findByIdDataSetSchema(Mockito.any());
  }

  /**
   * Test get table fixed records with non matching.
   */
  @Test
  public void testGetTableFixedRecordsWithNonMatching() {

    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    table.setFixedNumber(false);
    table.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    schema.setTableSchemas(Arrays.asList(table));

    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    datasetService.getTableFixedNumberOfRecords(1L, "5ce524fad31fc52540abae73",
        EntityTypeEnum.TABLE);
    Mockito.verify(schemasRepository, times(1)).findByIdDataSetSchema(Mockito.any());
  }

  /**
   * Test get record read only.
   */
  @Test
  public void testGetRecordReadOnly() {

    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    record.setFieldSchema(Arrays.asList(field));
    record.setIdRecordSchema(new ObjectId("5ce524fad31fc52540abae73"));
    table.setRecordSchema(record);
    table.setReadOnly(true);
    table.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    schema.setTableSchemas(Arrays.asList(table));

    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    datasetService.getTableReadOnly(1L, "5ce524fad31fc52540abae73", EntityTypeEnum.RECORD);
    Mockito.verify(schemasRepository, times(1)).findByIdDataSetSchema(Mockito.any());
  }

  /**
   * Test get record fixed number.
   */
  @Test
  public void testGetRecordFixedNumber() {

    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    record.setFieldSchema(Arrays.asList(field));
    record.setIdRecordSchema(new ObjectId("5ce524fad31fc52540abae73"));
    table.setRecordSchema(record);
    table.setFixedNumber(true);
    table.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    schema.setTableSchemas(Arrays.asList(table));

    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    datasetService.getTableFixedNumberOfRecords(1L, "5ce524fad31fc52540abae73",
        EntityTypeEnum.RECORD);
    Mockito.verify(schemasRepository, times(1)).findByIdDataSetSchema(Mockito.any());
  }

  /**
   * Test get record read only with non matching.
   */
  @Test
  public void testGetRecordReadOnlyWithNonMatching() {

    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    record.setFieldSchema(Arrays.asList(field));
    record.setIdRecordSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    table.setRecordSchema(record);
    table.setReadOnly(false);
    table.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    schema.setTableSchemas(Arrays.asList(table));

    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    datasetService.getTableReadOnly(1L, "5ce524fad31fc52540abae73", EntityTypeEnum.RECORD);
    Mockito.verify(schemasRepository, times(1)).findByIdDataSetSchema(Mockito.any());
  }

  /**
   * Test get record fixed number with non matching.
   */
  @Test
  public void testGetRecordFixedNumberWithNonMatching() {

    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    record.setFieldSchema(Arrays.asList(field));
    record.setIdRecordSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    table.setRecordSchema(record);
    table.setFixedNumber(false);
    table.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    schema.setTableSchemas(Arrays.asList(table));

    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    datasetService.getTableFixedNumberOfRecords(1L, "5ce524fad31fc52540abae73",
        EntityTypeEnum.RECORD);
    Mockito.verify(schemasRepository, times(1)).findByIdDataSetSchema(Mockito.any());
  }

  /**
   * Test get field read only.
   */
  @Test
  public void testGetFieldReadOnly() {

    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    field.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    table.setReadOnly(false);
    table.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    schema.setTableSchemas(Arrays.asList(table));

    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    datasetService.getTableReadOnly(1L, "5ce524fad31fc52540abae73", EntityTypeEnum.FIELD);
    Mockito.verify(schemasRepository, times(1)).findByIdDataSetSchema(Mockito.any());
  }

  /**
   * Test get field read only with non matching.
   */
  @Test
  public void testGetFieldReadOnlyWithNonMatching() {

    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    table.setReadOnly(true);
    table.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    schema.setTableSchemas(Arrays.asList(table));

    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    datasetService.getTableReadOnly(1L, "5ce524fad31fc52540abae73", EntityTypeEnum.FIELD);
    Mockito.verify(schemasRepository, times(1)).findByIdDataSetSchema(Mockito.any());
  }


  /**
   * Update records null test.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void updateRecordsNullTest() throws Exception {
    datasetService.updateRecords(null, new ArrayList<>(), false);
  }

  /**
   * Update records null 2 test.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void updateRecordsNull2Test() throws Exception {
    datasetService.updateRecords(1L, null, false);
  }

  /**
   * Update record test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateRecordTest() throws EEAException {
    FieldValue fieldValue = new FieldValue();
    RecordValue recordValue = new RecordValue();
    fieldValue.setValue("Lorem ipsum");
    recordValue.setFields(Arrays.asList(fieldValue));
    when(dataSetMetabaseRepository.findDatasetSchemaIdById(1L))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    new DataSetSchema();
    when(recordMapper.classListToEntity(Mockito.any())).thenReturn(Arrays.asList(recordValue));
    datasetService.updateRecords(1L, new ArrayList<>(), false);
    Mockito.verify(recordMapper, times(1)).classListToEntity(Mockito.any());
  }

  /**
   * Update record no value test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateRecordNoValueTest() throws EEAException {
    FieldValue fieldValue = new FieldValue();
    RecordValue recordValue = new RecordValue();
    recordValue.setFields(Arrays.asList(fieldValue));
    when(recordMapper.classListToEntity(Mockito.any())).thenReturn(Arrays.asList(recordValue));
    when(dataSetMetabaseRepository.findDatasetSchemaIdById(1L))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    new DataSetSchema();
    datasetService.updateRecords(1L, new ArrayList<>(), false);
    Mockito.verify(recordMapper, times(1)).classListToEntity(Mockito.any());
  }

  /**
   * Checks if is reportable design test.
   */
  @Test
  public void isReportableDesignTest() {
    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(datasetMetabaseService.getDatasetType(Mockito.any()))
        .thenReturn(DatasetTypeEnum.DESIGN);
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.any())).thenReturn(dataflow);
    assertTrue(datasetService.isDatasetReportable(1L));
  }

  /**
   * Checks if is not reportable design test.
   */
  @Test
  public void isNotReportableDesignTest() {
    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setStatus(TypeStatusEnum.DRAFT);

    Mockito.when(dataSetMetabaseRepository.findDataflowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.any())).thenReturn(dataflow);
    Mockito.when(datasetMetabaseService.getDatasetType(Mockito.any()))
        .thenReturn(DatasetTypeEnum.COLLECTION);
    assertFalse(datasetService.isDatasetReportable(1L));
  }

  /**
   * Checks if is reportable test.
   */
  @Test
  public void isReportableTest() {
    Mockito.when(datasetMetabaseService.getDatasetType(Mockito.any()))
        .thenReturn(DatasetTypeEnum.COLLECTION);
    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataSetMetabaseRepository.findDataflowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.any())).thenReturn(dataflow);
    assertFalse(datasetService.isDatasetReportable(1L));
  }

  /**
   * Checks if is reportable reporting test.
   */
  @Test
  public void isReportableReportingTest() {
    Mockito.when(datasetMetabaseService.getDatasetType(Mockito.any()))
        .thenReturn(DatasetTypeEnum.REPORTING);
    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataSetMetabaseRepository.findDataflowIdById(Mockito.any())).thenReturn(1L);
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.any())).thenReturn(dataflow);
    assertTrue(datasetService.isDatasetReportable(1L));
  }

  /**
   * Checks if is not reportable reporting test.
   */
  @Test
  public void isNotReportableReportingTest() {
    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setStatus(TypeStatusEnum.DESIGN);

    Mockito.when(datasetMetabaseService.getDatasetType(Mockito.any()))
        .thenReturn(DatasetTypeEnum.COLLECTION);
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.any())).thenReturn(dataflow);
    assertFalse(datasetService.isDatasetReportable(1L));
  }

  /**
   * Test copy data.
   */
  @Test
  public void testCopyData() {
    Map<String, String> dictionaryOriginTargetObjectId = new HashMap<>();
    dictionaryOriginTargetObjectId.put("5ce524fad31fc52540abae73", "5ce524fad31fc52540abae73");
    dictionaryOriginTargetObjectId.put("5fe06162f81da02a7cddaea5", "5fe06162f81da02a7cddaea5");
    dictionaryOriginTargetObjectId.put("0A07FD45F1CD7965A2B0F13E57948A12",
        "0A07FD45F1CD7965A2B0F13E57948A12");
    Map<Long, Long> dictionaryOriginTargetDatasetsId = new HashMap<>();
    dictionaryOriginTargetDatasetsId.put(1L, 2L);

    DesignDataset designDataset = new DesignDataset();
    designDataset.setId(2L);
    designDataset.setDatasetSchema("5ce524fad31fc52540abae73");
    DataSetSchema schema = new DataSetSchema();
    TableSchema designTableSchema = new TableSchema();

    designTableSchema.setToPrefill(Boolean.TRUE);
    designTableSchema.setIdTableSchema(new ObjectId("5fe06162f81da02a7cddaea5"));
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdTableSchema(designTableSchema.getIdTableSchema());
    recordSchema.setIdRecordSchema(new ObjectId());
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    FieldSchema fieldSchema = new FieldSchema();
    fieldSchema.setIdFieldSchema(new ObjectId());
    fieldSchema.setIdRecord(recordSchema.getIdRecordSchema());
    fieldSchemas.add(fieldSchema);
    recordSchema.setFieldSchema(fieldSchemas);
    designTableSchema.setRecordSchema(recordSchema);
    List<TableSchema> desingTableSchemas = new ArrayList<>();
    desingTableSchemas.add(designTableSchema);
    schema.setTableSchemas(desingTableSchemas);

    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    when(designDatasetRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(designDataset));

    List<RecordValue> recordDesignValues = new ArrayList<>();
    RecordValue record = new RecordValue();
    TableValue table = new TableValue();
    table.setId(1L);
    record.setTableValue(table);
    record.setId("record1");
    record.setIdRecordSchema("0A07FD45F1CD7965A2B0F13E57948A12");
    recordDesignValues.add(record);
    table.setIdTableSchema(recordSchema.getIdTableSchema().toString());
    DatasetValue datasetValue = new DatasetValue();
    datasetValue.setId(1l);
    table.setDatasetId(datasetValue);

    when(this.tableRepository.findByIdTableSchema(Mockito.anyString())).thenReturn(table);
    List<FieldValue> fieldValues = new ArrayList<>();
    FieldValue field = new FieldValue();
    field.setType(DataType.ATTACHMENT);
    field.setId("0A07FD45F1CD7965A2B0F13E57948A13");
    field.setRecord(record);
    fieldValues.add(field);

    when(fieldRepository.findByRecord_IdRecordSchema(Mockito.anyString(),
        Mockito.any(Pageable.class))).then(new Answer<List<FieldValue>>() {

          @Override
          public List<FieldValue> answer(InvocationOnMock invocation) throws Throwable {
            List<FieldValue> result;
            if (((Pageable) invocation.getArgument(1)).getPageNumber() == 0) {
              result = fieldValues;
            } else {
              result = new ArrayList<>();
            }
            return result;
          }
        });
    AttachmentValue attachment = new AttachmentValue();
    attachment.setFieldValue(field);
    when(attachmentRepository.findAll()).thenReturn(Arrays.asList(attachment));
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_id(Mockito.any()))
        .thenReturn(Optional.of(new PartitionDataSetMetabase()));

    datasetService.copyData(dictionaryOriginTargetDatasetsId, dictionaryOriginTargetObjectId);
    Mockito.verify(recordRepository, times(1)).saveAll(Mockito.any());
  }


  /**
   * Test get attachment.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetAttachment() throws EEAException {
    datasetService.getAttachment(1L, "600B66C6483EA7C8B55891DA171A3E7F");
    Mockito.verify(attachmentRepository, times(1)).findByFieldValueId(Mockito.any());
  }

  /**
   * Test delete attachment.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testDeleteAttachment() throws EEAException {
    when(fieldRepository.findById(Mockito.anyString())).thenReturn(new FieldValue());
    datasetService.deleteAttachment(1L, "600B66C6483EA7C8B55891DA171A3E7F");
    Mockito.verify(attachmentRepository, times(1)).deleteByFieldValueId(Mockito.any());
  }

  /**
   * Test update attachment.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void testUpdateAttachment() throws EEAException, IOException {
    final MockMultipartFile file = new MockMultipartFile("file", "fileOriginal.csv",
        FileTypeEnum.CSV.getValue(), "content".getBytes());
    Document fieldSchema = new Document();
    fieldSchema.put(LiteralConstants.READ_ONLY, Boolean.FALSE);
    FieldValue field = new FieldValue();
    field.setId("600B66C6483EA7C8B55891DA171A3E7F");
    RecordValue record = new RecordValue();
    TableValue table = new TableValue();
    DatasetValue dataset = new DatasetValue();
    dataset.setIdDatasetSchema("5ce524fad31fc52540abae73");
    table.setDatasetId(dataset);
    record.setTableValue(table);
    field.setRecord(record);

    when(fieldRepository.findById(Mockito.anyString())).thenReturn(field);
    when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any())).thenReturn(fieldSchema);
    when(attachmentRepository.findByFieldValueId(Mockito.anyString()))
        .thenReturn(new AttachmentValue());
    datasetService.updateAttachment(1L, "600B66C6483EA7C8B55891DA171A3E7F", file.getName(),
        file.getInputStream());
    Mockito.verify(fieldRepository, times(1)).save(Mockito.any());
  }

  /**
   * Test update attachment null.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void testUpdateAttachmentNull() throws EEAException, IOException {
    final MockMultipartFile file = new MockMultipartFile("file", "fileOriginal.csv",
        FileTypeEnum.CSV.getValue(), "content".getBytes());
    Document fieldSchema = new Document();
    fieldSchema.put(LiteralConstants.READ_ONLY, Boolean.FALSE);
    FieldValue field = new FieldValue();
    field.setId("600B66C6483EA7C8B55891DA171A3E7F");
    RecordValue record = new RecordValue();
    TableValue table = new TableValue();
    DatasetValue dataset = new DatasetValue();
    dataset.setIdDatasetSchema("5ce524fad31fc52540abae73");
    table.setDatasetId(dataset);
    record.setTableValue(table);
    field.setRecord(record);

    when(fieldRepository.findById(Mockito.anyString())).thenReturn(field);
    when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any())).thenReturn(fieldSchema);
    datasetService.updateAttachment(1L, "600B66C6483EA7C8B55891DA171A3E7F", file.getName(),
        file.getInputStream());
    Mockito.verify(fieldRepository, times(1)).save(Mockito.any());
  }

  /**
   * Test update attachment.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = EEAException.class)
  public void testUpdateAttachmentReadOnly() throws EEAException, IOException {
    final MockMultipartFile file = new MockMultipartFile("file", "fileOriginal.csv",
        FileTypeEnum.CSV.getValue(), "content".getBytes());
    Document fieldSchema = new Document();
    fieldSchema.put(LiteralConstants.READ_ONLY, Boolean.TRUE);
    FieldValue field = new FieldValue();
    field.setId("600B66C6483EA7C8B55891DA171A3E7F");
    RecordValue record = new RecordValue();
    TableValue table = new TableValue();
    DatasetValue dataset = new DatasetValue();
    dataset.setIdDatasetSchema("5ce524fad31fc52540abae73");
    table.setDatasetId(dataset);
    record.setTableValue(table);
    field.setRecord(record);

    when(fieldRepository.findById(Mockito.anyString())).thenReturn(field);
    when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any())).thenReturn(fieldSchema);
    try {
      datasetService.updateAttachment(1L, "600B66C6483EA7C8B55891DA171A3E7F", file.getName(),
          file.getInputStream());
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.FIELD_READ_ONLY, e.getMessage());
      throw e;
    }
  }

  /**
   * Gets the field by id test.
   *
   * @return the field by id test
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void getFieldByIdTest() throws EEAException {
    FieldVO fieldVO = new FieldVO();
    fieldVO.setIdFieldSchema("600B66C6483EA7C8B55891DA171A3E7F");
    FieldValue fieldValue = new FieldValue();
    fieldValue.setIdFieldSchema("600B66C6483EA7C8B55891DA171A3E7F");
    when(fieldRepository.findById(Mockito.anyString())).thenReturn(fieldValue);
    when(fieldNoValidationMapper.entityToClass(Mockito.any())).thenReturn(fieldVO);
    assertEquals(fieldVO, datasetService.getFieldById(1L, "idField"));
  }

  /**
   * Gets the field by id exception test.
   *
   * @return the field by id exception test
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getFieldByIdExceptionTest() throws EEAException {
    try {
      datasetService.getFieldById(1L, "idField");
    } catch (EEAException e) {
      assertEquals(
          String.format(EEAErrorMessage.FIELD_NOT_FOUND, new ObjectId("5cf0e9b3b793310e9ceca190")),
          e.getMessage());
      throw e;
    }
  }

  /**
   * Test delete attachment by id field schema.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testDeleteAttachmentByIdFieldSchema() throws EEAException {
    datasetService.deleteAttachmentByFieldSchemaId(1L, "5cf0e9b3b793310e9ceca190");
    Mockito.verify(fieldRepository, times(1)).clearFieldValue(Mockito.any());
  }

  /**
   * Export file through integration test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void exportFileThroughIntegrationTest() throws EEAException {
    DataSetMetabase dataSetMetabase = new DataSetMetabase();
    dataSetMetabase.setDatasetSchema("5cf0e9b3b793310e9ceca190");
    Optional<DataSetMetabase> datasetMetabaseOptional = Optional.of(dataSetMetabase);
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseOptional);

    Mockito
        .when(integrationController.findExportIntegration(Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(new IntegrationVO());
    Mockito.when(integrationController.executeIntegrationProcess(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ExecutionResultVO());
    datasetService.exportFileThroughIntegration(1L, 1L);
    Mockito.verify(integrationController, times(1)).executeIntegrationProcess(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Test find record schema by id.
   */
  @Test
  public void testFindRecordSchemaById() {
    RecordValue record = new RecordValue();
    record.setId("0A07FD45F1CD7965A2B0F13E57948A13");
    record.setIdRecordSchema("5cf0e9b3b793310e9ceca190");
    Mockito.when(recordRepository.findById(Mockito.anyString())).thenReturn(record);
    assertEquals("5cf0e9b3b793310e9ceca190",
        datasetService.findRecordSchemaIdById(1L, "0A07FD45F1CD7965A2B0F13E57948A13"));
  }

  /**
   * Test find field schema by id.
   */
  @Test
  public void testFindFieldSchemaById() {
    FieldValue field = new FieldValue();
    field.setId("0A07FD45F1CD7965A2B0F13E57948A13");
    field.setIdFieldSchema("5cf0e9b3b793310e9ceca190");
    Mockito.when(fieldRepository.findById(Mockito.anyString())).thenReturn(field);
    assertEquals("5cf0e9b3b793310e9ceca190",
        datasetService.findFieldSchemaIdById(1L, "0A07FD45F1CD7965A2B0F13E57948A13"));
  }

  /**
   * Gets the dataset type reporting test.
   *
   * @return the dataset type reporting test
   */
  @Test
  public void getDatasetTypeReportingTest() {
    when(reportingDatasetRepository.existsById(Mockito.any())).thenReturn(true);
    assertEquals(DatasetTypeEnum.REPORTING, datasetService.getDatasetType(1L));
  }

  /**
   * Gets the dataset type design test.
   *
   * @return the dataset type design test
   */
  @Test
  public void getDatasetTypeDesignTest() {
    when(reportingDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    when(designDatasetRepository.existsById(Mockito.any())).thenReturn(true);
    assertEquals(DatasetTypeEnum.DESIGN, datasetService.getDatasetType(1L));
  }

  /**
   * Gets the dataset type DC test.
   *
   * @return the dataset type DC test
   */
  @Test
  public void getDatasetTypeDCTest() {
    when(reportingDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    when(designDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    when(dataCollectionRepository.existsById(Mockito.any())).thenReturn(true);
    assertEquals(DatasetTypeEnum.COLLECTION, datasetService.getDatasetType(1L));
  }

  /**
   * Gets the dataset type EU test.
   *
   * @return the dataset type EU test
   */
  @Test
  public void getDatasetTypeEUTest() {
    when(reportingDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    when(designDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    when(testDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    when(dataCollectionRepository.existsById(Mockito.any())).thenReturn(false);
    when(dataSetMetabaseRepository.existsById(Mockito.any())).thenReturn(true);
    assertEquals(DatasetTypeEnum.EUDATASET, datasetService.getDatasetType(1L));
  }

  /**
   * Gets the dataset type test test.
   *
   * @return the dataset type test test
   */
  @Test
  public void getDatasetTypeTestTest() {
    when(reportingDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    when(designDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    when(testDatasetRepository.existsById(Mockito.any())).thenReturn(true);
    assertEquals(DatasetTypeEnum.TEST, datasetService.getDatasetType(1L));
  }

  /**
   * Insert records null record required exeption test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void insertRecordsNullRecordRequiredExeptionTest() throws EEAException {
    try {
      datasetService.insertRecords(1L, null, "5cf0e9b3b793310e9ceca190");
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.RECORD_REQUIRED, e.getMessage());
      throw e;
    }
  }

  /**
   * Insert records record required exeption test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void insertRecordsRecordRequiredExeptionTest() throws EEAException {
    try {
      datasetService.insertRecords(1L, new ArrayList<>(), "5cf0e9b3b793310e9ceca190");
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.RECORD_REQUIRED, e.getMessage());
      throw e;
    }
  }

  /**
   * Insert records read only exeption test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void insertRecordsReadOnlyExeptionTest() throws EEAException {
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDatasetSchema("5cf0e9b3b793310e9ceca190");

    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);

    TableSchema tableSchema = new TableSchema();
    tableSchema.setReadOnly(Boolean.TRUE);
    tableSchema.setFixedNumber(Boolean.FALSE);
    tableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setTableSchemas(Arrays.asList(tableSchema));
    Optional<DataSetSchema> dataSetSchemaOptional = Optional.of(datasetSchema);
    Mockito.when(schemasRepository.findById(Mockito.any(ObjectId.class)))
        .thenReturn(dataSetSchemaOptional);

    Mockito.when(reportingDatasetRepository.existsById(Mockito.anyLong())).thenReturn(Boolean.TRUE);
    try {
      datasetService.insertRecords(1L, Arrays.asList(new RecordVO()), "5cf0e9b3b793310e9ceca190");
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.TABLE_READ_ONLY, e.getMessage());
      throw e;
    }
  }

  /**
   * Insert records fixed records exeption test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void insertRecordsFixedRecordsExeptionTest() throws EEAException {
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDatasetSchema("5cf0e9b3b793310e9ceca190");
    datasetMetabaseVO.setDataProviderId(1L);

    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);

    TableSchema tableSchema = new TableSchema();
    tableSchema.setReadOnly(Boolean.FALSE);
    tableSchema.setFixedNumber(Boolean.TRUE);
    tableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setTableSchemas(Arrays.asList(tableSchema));
    Optional<DataSetSchema> dataSetSchemaOptional = Optional.ofNullable(datasetSchema);

    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(dataSetSchemaOptional);

    Mockito.when(reportingDatasetRepository.existsById(Mockito.anyLong())).thenReturn(Boolean.TRUE);
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.anyLong()))
        .thenReturn(new DataProviderVO());
    try {
      datasetService.insertRecords(1L, Arrays.asList(new RecordVO()), "5cf0e9b3b793310e9ceca190");
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.FIXED_NUMBER_OF_RECORDS, e.getMessage());
      throw e;
    }
  }

  /**
   * Insert records table not found exeption test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void insertRecordsTableNotFoundExeptionTest() throws EEAException {
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDatasetSchema("5cf0e9b3b793310e9ceca190");
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    try {
      datasetService.insertRecords(1L, Arrays.asList(new RecordVO()), "5cf0e9b3b793310e9ceca190");
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.IDTABLESCHEMA_INCORRECT, e.getMessage());
      throw e;
    }
  }

  /**
   * Inser records design test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void inserRecordsDesignTest() throws EEAException {
    FieldVO fieldVO1 = new FieldVO();
    fieldVO1.setIdFieldSchema("5cf0e9b3b793310e9ceca191");
    fieldVO1.setValue("value");
    FieldVO fieldVO2 = new FieldVO();
    fieldVO2.setIdFieldSchema("5cf0e9b3b793310e9ceca192");
    fieldVO2.setValue("value");
    FieldVO fieldVO3 = new FieldVO();
    fieldVO3.setIdFieldSchema("5cf0e9b3b793310e9ceca193");
    fieldVO3.setValue("value1, value2, value3");
    FieldVO fieldVO4 = new FieldVO();
    fieldVO4.setIdFieldSchema("5cf0e9b3b793310e9ceca194");
    fieldVO4.setValue("value1, value2, value3");
    List<FieldVO> fieldVOs = new ArrayList<>();
    fieldVOs.add(fieldVO1);
    fieldVOs.add(fieldVO2);
    fieldVOs.add(fieldVO3);
    fieldVOs.add(fieldVO4);
    RecordVO recordVO = new RecordVO();
    recordVO.setFields(fieldVOs);
    List<RecordVO> recordVOs = new ArrayList<>();
    recordVOs.add(recordVO);
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDatasetSchema("5cf0e9b3b793310e9ceca190");
    FieldSchema fieldSchema1 = new FieldSchema();
    fieldSchema1.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca191"));
    fieldSchema1.setType(DataType.TEXT);
    fieldSchema1.setPkHasMultipleValues(Boolean.FALSE);
    fieldSchema1.setReadOnly(Boolean.FALSE);
    FieldSchema fieldSchema2 = new FieldSchema();
    fieldSchema2.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca192"));
    fieldSchema2.setType(DataType.TEXT);
    fieldSchema2.setPkHasMultipleValues(Boolean.FALSE);
    fieldSchema2.setReadOnly(Boolean.TRUE);
    FieldSchema fieldSchema3 = new FieldSchema();
    fieldSchema3.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca193"));
    fieldSchema3.setType(DataType.MULTISELECT_CODELIST);
    fieldSchema3.setPkHasMultipleValues(Boolean.TRUE);
    fieldSchema3.setReadOnly(Boolean.FALSE);
    FieldSchema fieldSchema4 = new FieldSchema();
    fieldSchema4.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca194"));
    fieldSchema4.setType(DataType.LINK);
    fieldSchema4.setPkHasMultipleValues(Boolean.TRUE);
    fieldSchema4.setReadOnly(Boolean.FALSE);
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    fieldSchemas.add(fieldSchema1);
    fieldSchemas.add(fieldSchema2);
    fieldSchemas.add(fieldSchema3);
    fieldSchemas.add(fieldSchema4);
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    recordSchema.setFieldSchema(fieldSchemas);
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    tableSchema.setRecordSchema(recordSchema);
    tableSchema.setReadOnly(Boolean.FALSE);
    tableSchema.setFixedNumber(Boolean.FALSE);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setTableSchemas(Arrays.asList(tableSchema));
    Optional<DataSetSchema> dataSetSchemaOptional = Optional.of(datasetSchema);
    Mockito.when(schemasRepository.findById(Mockito.any(ObjectId.class)))
        .thenReturn(dataSetSchemaOptional);

    Mockito.when(reportingDatasetRepository.existsById(Mockito.anyLong()))
        .thenReturn(Boolean.FALSE);
    Mockito.when(designDatasetRepository.existsById(Mockito.anyLong())).thenReturn(Boolean.TRUE);
    Mockito
        .when(partitionDataSetMetabaseRepository
            .findFirstByIdDataSet_idAndUsername(Mockito.anyLong(), Mockito.any()))
        .thenReturn(Optional.of(new PartitionDataSetMetabase()));
    Mockito.when(tableRepository.findIdByIdTableSchema(Mockito.anyString())).thenReturn(1L);
    datasetService.insertRecords(1L, recordVOs, "5cf0e9b3b793310e9ceca190");
    Mockito.verify(recordRepository, times(1)).saveAll(Mockito.anyIterable());
  }

  /**
   * Inser records reporting test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void inserRecordsReportingTest() throws EEAException {
    FieldVO fieldVO1 = new FieldVO();
    fieldVO1.setIdFieldSchema("5cf0e9b3b793310e9ceca191");
    fieldVO1.setValue("value");
    FieldVO fieldVO2 = new FieldVO();
    fieldVO2.setIdFieldSchema("5cf0e9b3b793310e9ceca192");
    fieldVO2.setValue("value");
    FieldVO fieldVO3 = new FieldVO();
    fieldVO3.setIdFieldSchema("5cf0e9b3b793310e9ceca193");
    fieldVO3.setValue("value1, value2, value3");
    FieldVO fieldVO4 = new FieldVO();
    fieldVO4.setIdFieldSchema("5cf0e9b3b793310e9ceca194");
    fieldVO4.setValue("value1, value2, value3");
    List<FieldVO> fieldVOs = new ArrayList<>();
    fieldVOs.add(fieldVO1);
    fieldVOs.add(fieldVO2);
    fieldVOs.add(fieldVO3);
    fieldVOs.add(fieldVO4);
    RecordVO recordVO = new RecordVO();
    recordVO.setFields(fieldVOs);
    List<RecordVO> recordVOs = new ArrayList<>();
    recordVOs.add(recordVO);
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDatasetSchema("5cf0e9b3b793310e9ceca190");
    FieldSchema fieldSchema1 = new FieldSchema();
    fieldSchema1.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca191"));
    fieldSchema1.setType(DataType.TEXT);
    fieldSchema1.setPkHasMultipleValues(Boolean.FALSE);
    fieldSchema1.setReadOnly(Boolean.FALSE);
    FieldSchema fieldSchema2 = new FieldSchema();
    fieldSchema2.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca192"));
    fieldSchema2.setType(DataType.TEXT);
    fieldSchema2.setPkHasMultipleValues(Boolean.FALSE);
    fieldSchema2.setReadOnly(Boolean.TRUE);
    FieldSchema fieldSchema3 = new FieldSchema();
    fieldSchema3.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca193"));
    fieldSchema3.setType(DataType.MULTISELECT_CODELIST);
    fieldSchema3.setPkHasMultipleValues(Boolean.TRUE);
    fieldSchema3.setReadOnly(Boolean.FALSE);
    FieldSchema fieldSchema4 = new FieldSchema();
    fieldSchema4.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca194"));
    fieldSchema4.setType(DataType.LINK);
    fieldSchema4.setPkHasMultipleValues(Boolean.TRUE);
    fieldSchema4.setReadOnly(Boolean.FALSE);
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    fieldSchemas.add(fieldSchema1);
    fieldSchemas.add(fieldSchema2);
    fieldSchemas.add(fieldSchema3);
    fieldSchemas.add(fieldSchema4);
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    recordSchema.setFieldSchema(fieldSchemas);
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    tableSchema.setRecordSchema(recordSchema);
    tableSchema.setReadOnly(Boolean.FALSE);
    tableSchema.setFixedNumber(Boolean.FALSE);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setTableSchemas(Arrays.asList(tableSchema));
    Optional<DataSetSchema> dataSetSchemaOptional = Optional.of(datasetSchema);
    Mockito.when(schemasRepository.findById(Mockito.any(ObjectId.class)))
        .thenReturn(dataSetSchemaOptional);

    Mockito.when(reportingDatasetRepository.existsById(Mockito.anyLong())).thenReturn(Boolean.TRUE);
    Mockito
        .when(partitionDataSetMetabaseRepository
            .findFirstByIdDataSet_idAndUsername(Mockito.anyLong(), Mockito.any()))
        .thenReturn(Optional.of(new PartitionDataSetMetabase()));
    Mockito.when(tableRepository.findIdByIdTableSchema(Mockito.anyString())).thenReturn(1L);
    datasetService.insertRecords(1L, recordVOs, "5cf0e9b3b793310e9ceca190");
    Mockito.verify(recordRepository, times(1)).saveAll(Mockito.anyIterable());
  }



  /**
   * Save public files.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidFileException the invalid file exception
   * @throws EEAException the EEA exception
   */
  @Test
  public void savePublicFiles() throws IOException, InvalidFileException, EEAException {
    DataSetMetabase dataSetMetabase = new DataSetMetabase();
    dataSetMetabase.setDataflowId(1L);
    dataSetMetabase.setDataProviderId(1L);
    dataSetMetabase.setDatasetSchema("603362319d49f04fce13b68f");
    List<RepresentativeVO> representativeList = new ArrayList<>();
    RepresentativeVO representativeVO = new RepresentativeVO();
    representativeVO.setDataProviderId(1L);
    representativeVO.setRestrictFromPublic(false);
    representativeList.add(representativeVO);
    DataProviderVO dataProvider = new DataProviderVO();
    dataProvider.setLabel("SPAIN");
    dataProvider.setId(1l);
    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.anyLong()))
        .thenReturn(representativeList);
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.anyLong()))
        .thenReturn(dataProvider);

    List<DesignDataset> desingDataset = new ArrayList<>();
    DesignDataset designDataset = new DesignDataset();
    designDataset.setDatasetSchema("603362319d49f04fce13b68f");
    designDataset.setDataSetName("test");
    desingDataset.add(designDataset);
    Mockito.when(designDatasetRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(desingDataset);
    List<DataSetMetabase> datasetMetabaseList = new ArrayList<>();
    DataSetMetabase dataSetMetabaseEnd = new DataSetMetabase();
    dataSetMetabaseEnd.setDatasetSchema("603362319d49f04fce13b68f");
    datasetMetabaseList.add(dataSetMetabaseEnd);
    Mockito.when(dataSetMetabaseRepository.findByDataflowIdAndDataProviderId(Mockito.anyLong(),
        Mockito.anyLong())).thenReturn(datasetMetabaseList);
    Mockito.when(schemasRepository.findAvailableInPublicByIdDataSetSchema(Mockito.any()))
        .thenReturn(true);
    DataSetSchema dataSetSchema = new DataSetSchema();
    dataSetSchema.setAvailableInPublic(true);
    dataSetSchema.setTableSchemas(new ArrayList<>());
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(dataSetSchema);
    when(fileExportFactory.createContext(Mockito.any())).thenReturn(contextExport);
    byte[] expectedResult = "".getBytes();
    when(contextExport.fileWriter(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean(),
        Mockito.anyBoolean())).thenReturn(expectedResult);
    Mockito.when(dataSetMetabaseRepository.findByDataflowIdAndDataProviderId(Mockito.anyLong(),
        Mockito.anyLong())).thenReturn(datasetMetabaseList);
    datasetService.savePublicFiles(1L, 1L);
    Mockito.verify(fileExportFactory, times(1)).createContext(Mockito.any());
  }


  /**
   * Export public file throws.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void exportPublicFileThrows() throws IOException, EEAException {
    try {
      datasetService.exportPublicFile(1L, 1L, "test");
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.FILE_NOT_FOUND, e.getMessage());
      throw e;
    }
  }

  /**
   * Initialize dataset sucess test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void initializeDatasetSucessTest() throws EEAException {
    DesignDataset desingDataset = new DesignDataset();
    desingDataset.setId(2L);
    desingDataset.setDatasetSchema("5cf0e9b3b793310e9ceca190");
    DataSetSchema schema = new DataSetSchema();
    schema.setIdDataSetSchema(new ObjectId());
    TableSchema desingTableSchema = new TableSchema();
    desingTableSchema.setToPrefill(Boolean.TRUE);
    desingTableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca191"));
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(new ObjectId("5cf0e9b3b793310e9ceca192"));
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    FieldSchema fieldSchema = new FieldSchema();
    fieldSchema.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca193"));
    fieldSchema.setIdRecord(recordSchema.getIdRecordSchema());
    fieldSchemas.add(fieldSchema);
    recordSchema.setFieldSchema(fieldSchemas);
    desingTableSchema.setRecordSchema(recordSchema);
    List<TableSchema> desingTableSchemas = new ArrayList<>();
    desingTableSchemas.add(desingTableSchema);
    schema.setTableSchemas(desingTableSchemas);
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    List<RecordValue> recordDesignValues = new ArrayList<>();
    RecordValue record = new RecordValue();
    TableValue table = new TableValue();
    table.setId(1L);
    record.setTableValue(table);
    record.setIdRecordSchema(recordSchema.getIdRecordSchema().toString());
    recordDesignValues.add(record);
    when(tableRepository.findByIdTableSchema(Mockito.anyString())).thenReturn(table);
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    when(reportingDatasetRepository.existsById(Mockito.any())).thenReturn(true);
    when(designDatasetRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(desingDataset));
    List<FieldValue> fieldValues = new ArrayList<>();
    FieldValue field = new FieldValue();
    field.setType(DataType.ATTACHMENT);
    field.setId("0A07FD45F1CD7965A2B0F13E57948A13");
    field.setRecord(record);
    fieldValues.add(field);
    when(fieldRepository.findByRecord_IdRecordSchema(Mockito.anyString(),
        Mockito.any(Pageable.class))).then(new Answer<List<FieldValue>>() {

          @Override
          public List<FieldValue> answer(InvocationOnMock invocation) throws Throwable {
            List<FieldValue> result;
            if (((Pageable) invocation.getArgument(1)).getPageNumber() == 0) {
              result = fieldValues;
            } else {
              result = new ArrayList<>();
            }
            return result;
          }
        });
    AttachmentValue attachment = new AttachmentValue();
    attachment.setFieldValue(field);
    when(attachmentRepository.findAll()).thenReturn(Arrays.asList(attachment));

    datasetService.initializeDataset(1L, "5cf0e9b3b793310e9ceca190");
    Mockito.verify(attachmentRepository, times(1)).saveAll(Mockito.any());
  }

  /**
   * Initialize dataset fixed records sucess test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void initializeDatasetFixedRecordsSucessTest() throws EEAException {
    DesignDataset desingDataset = new DesignDataset();
    desingDataset.setId(2L);
    desingDataset.setDatasetSchema("5cf0e9b3b793310e9ceca190");
    DataSetSchema schema = new DataSetSchema();
    schema.setIdDataSetSchema(new ObjectId());
    TableSchema desingTableSchema = new TableSchema();
    desingTableSchema.setToPrefill(Boolean.TRUE);
    desingTableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca191"));
    desingTableSchema.setFixedNumber(true);
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(new ObjectId("5cf0e9b3b793310e9ceca192"));
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    FieldSchema fieldSchema = new FieldSchema();
    fieldSchema.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca193"));
    fieldSchema.setIdRecord(recordSchema.getIdRecordSchema());
    fieldSchemas.add(fieldSchema);
    recordSchema.setFieldSchema(fieldSchemas);
    desingTableSchema.setRecordSchema(recordSchema);
    List<TableSchema> desingTableSchemas = new ArrayList<>();
    desingTableSchemas.add(desingTableSchema);
    schema.setTableSchemas(desingTableSchemas);
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    List<RecordValue> recordDesignValues = new ArrayList<>();
    RecordValue record = new RecordValue();
    TableValue table = new TableValue();
    table.setId(1L);
    record.setTableValue(table);
    record.setIdRecordSchema(recordSchema.getIdRecordSchema().toString());
    recordDesignValues.add(record);
    when(tableRepository.findByIdTableSchema(Mockito.anyString())).thenReturn(table);
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    when(reportingDatasetRepository.existsById(Mockito.any())).thenReturn(true);
    when(designDatasetRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(desingDataset));
    when(recordRepository.findOrderedNativeRecord(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(recordDesignValues);
    List<FieldValue> fieldValues = new ArrayList<>();
    FieldValue field = new FieldValue();
    field.setType(DataType.ATTACHMENT);
    field.setId("0A07FD45F1CD7965A2B0F13E57948A13");
    field.setRecord(record);
    record.setFields(fieldValues);
    fieldValues.add(field);
    AttachmentValue attachment = new AttachmentValue();
    attachment.setFieldValue(field);
    when(attachmentRepository.findAll()).thenReturn(Arrays.asList(attachment));

    datasetService.initializeDataset(1L, "5cf0e9b3b793310e9ceca190");
    Mockito.verify(recordRepository, times(1)).saveAll(Mockito.any());
  }

  /**
   * Initialize dataset reference test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void initializeDatasetReferenceTest() throws EEAException, IOException {
    DesignDataset desingDataset = new DesignDataset();
    desingDataset.setId(2L);
    desingDataset.setDatasetSchema("5cf0e9b3b793310e9ceca190");
    DataSetSchema schema = new DataSetSchema();
    schema.setIdDataSetSchema(new ObjectId());
    schema.setAvailableInPublic(true);
    schema.setReferenceDataset(true);
    TableSchema desingTableSchema = new TableSchema();
    desingTableSchema.setToPrefill(Boolean.TRUE);
    desingTableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca191"));
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(new ObjectId("5cf0e9b3b793310e9ceca192"));
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    FieldSchema fieldSchema = new FieldSchema();
    fieldSchema.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca193"));
    fieldSchema.setIdRecord(recordSchema.getIdRecordSchema());
    fieldSchema.setType(DataType.ATTACHMENT);
    fieldSchemas.add(fieldSchema);
    recordSchema.setFieldSchema(fieldSchemas);
    desingTableSchema.setRecordSchema(recordSchema);
    List<TableSchema> desingTableSchemas = new ArrayList<>();
    desingTableSchemas.add(desingTableSchema);
    schema.setTableSchemas(desingTableSchemas);
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    List<RecordValue> recordDesignValues = new ArrayList<>();
    RecordValue record = new RecordValue();
    TableValue table = new TableValue();
    table.setId(1L);
    record.setTableValue(table);
    record.setIdRecordSchema(recordSchema.getIdRecordSchema().toString());
    recordDesignValues.add(record);
    DataSetMetabase dataset = new DataSetMetabase();
    dataset.setDataflowId(1L);
    dataset.setId(1L);
    dataset.setDatasetSchema("5cf0e9b3b793310e9ceca193");
    when(tableRepository.findByIdTableSchema(Mockito.anyString())).thenReturn(table);
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    when(referenceDatasetRepository.existsById(Mockito.any())).thenReturn(true);
    when(dataSetMetabaseRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(dataset));
    when(designDatasetRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(desingDataset));
    when(designDatasetRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<DesignDataset>());
    when(dataSetMetabaseRepository.findDataflowIdById(Mockito.any())).thenReturn(1L);
    List<FieldValue> fieldValues = new ArrayList<>();
    FieldValue field = new FieldValue();
    field.setType(DataType.ATTACHMENT);
    field.setId("0A07FD45F1CD7965A2B0F13E57948A13");
    field.setRecord(record);
    fieldValues.add(field);
    when(fieldRepository.findByRecord_IdRecordSchema(Mockito.anyString(),
        Mockito.any(Pageable.class))).then(new Answer<List<FieldValue>>() {

          @Override
          public List<FieldValue> answer(InvocationOnMock invocation) throws Throwable {
            List<FieldValue> result;
            if (((Pageable) invocation.getArgument(1)).getPageNumber() == 0) {
              result = fieldValues;
            } else {
              result = new ArrayList<>();
            }
            return result;
          }
        });
    AttachmentValue attachment = new AttachmentValue();
    attachment.setFieldValue(field);
    when(attachmentRepository.findAll()).thenReturn(Arrays.asList(attachment));
    "".getBytes();
    when(fileExportFactory.createContext(Mockito.any())).thenReturn(contextExport);
    byte[] expectedResult = "".getBytes();
    when(contextExport.fileWriter(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean(),
        Mockito.anyBoolean())).thenReturn(expectedResult);
    attachment.setContent(expectedResult);
    when(attachmentRepository.findAllByIdFieldSchemaAndValueIsNotNull(Mockito.anyString()))
        .thenReturn(Arrays.asList(attachment));
    datasetService.initializeDataset(1L, "5cf0e9b3b793310e9ceca190");
    Mockito.verify(fileExportFactory, times(1)).createContext(Mockito.any());
  }


  /**
   * Download file exception test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void downloadFileExceptionTest() throws IOException, EEAException {
    try {
      datasetService.downloadExportedFile(1L, "file.xlsx");
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.FILE_NOT_FOUND, e.getMessage());
      throw e;
    }
  }

  /**
   * Etl export dataset test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Test
  public void etlExportDatasetTest() throws IOException, EEAException {
    ObjectId id = new ObjectId();
    DataSetSchema dsSchema = new DataSetSchema();
    TableSchema tSchema = new TableSchema();
    RecordSchema rSchema = new RecordSchema();
    FieldSchema fSchema = new FieldSchema();
    rSchema.setFieldSchema(Arrays.asList(fSchema));
    tSchema.setIdTableSchema(id);
    tSchema.setRecordSchema(rSchema);
    dsSchema.setTableSchemas(Arrays.asList(tSchema));
    Mockito
        .when(recordRepository.findAndGenerateETLJson(Mockito.anyLong(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn("");
    datasetService.etlExportDataset(0l, outputStream, id.toString(), 10, 10, "", "");
    Mockito.verify(outputStream, times(1)).flush();
  }

  /**
   * Etl export dataset exception test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Test
  public void etlExportDatasetExceptionTest() throws IOException, EEAException {
    ObjectId id = new ObjectId();
    DataSetSchema dsSchema = new DataSetSchema();
    TableSchema tSchema = new TableSchema();
    RecordSchema rSchema = new RecordSchema();
    FieldSchema fSchema = new FieldSchema();
    rSchema.setFieldSchema(Arrays.asList(fSchema));
    tSchema.setIdTableSchema(id);
    tSchema.setRecordSchema(rSchema);
    dsSchema.setTableSchemas(Arrays.asList(tSchema));
    Mockito
        .when(recordRepository.findAndGenerateETLJson(Mockito.anyLong(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenThrow(EEAException.class);
    datasetService.etlExportDataset(0l, outputStream, id.toString(), 10, 10, "", "");
    Mockito.verify(recordRepository, times(1)).findAndGenerateETLJson(Mockito.anyLong(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Creates the lock with signature test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createLockWithSignatureTest() throws EEAException {
    datasetService.createLockWithSignature(LockSignature.EMPTY, new HashMap<>(), "");
    Mockito.verify(lockService, times(1)).createLock(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Gets the schema if reportable null test.
   *
   * @return the schema if reportable null test
   */
  @Test
  public void getSchemaIfReportableNullTest() {
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    assertNull(datasetService.getSchemaIfReportable(0L, new ObjectId().toString()));
  }

  /**
   * Gets the schema if reportable desing test.
   *
   * @return the schema if reportable desing test
   */
  @Test
  public void getSchemaIfReportableDesingTest() {
    ObjectId id = new ObjectId();
    DesignDataset dataset = new DesignDataset();
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    dataset.setDatasetSchema(id.toString());
    Mockito.when(designDatasetRepository.existsById(Mockito.any())).thenReturn(true);
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(dataset));
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    assertNull(datasetService.getSchemaIfReportable(0L, id.toString()));
  }

  /**
   * Gets the schema if reportable draft test.
   *
   * @return the schema if reportable draft test
   */
  @Test
  public void getSchemaIfReportableDraftTest() {
    ObjectId id = new ObjectId();
    ReportingDataset dataset = new ReportingDataset();
    DataFlowVO dataflowVO = new DataFlowVO();
    DataSetSchema schema = new DataSetSchema();
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(id);
    schema.setTableSchemas(Arrays.asList(tableSchema));
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    dataset.setDatasetSchema(id.toString());
    Mockito.when(reportingDatasetRepository.existsById(Mockito.any())).thenReturn(true);
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(dataset));
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    assertEquals(schema, datasetService.getSchemaIfReportable(0L, id.toString()));
  }

  /**
   * Gets the schema if reference draft test.
   *
   * @return the schema if reference draft test
   */
  @Test
  public void getSchemaIfReferenceDraftTest() {
    ObjectId id = new ObjectId();
    ReferenceDataset dataset = new ReferenceDataset();
    dataset.setUpdatable(true);
    DataFlowVO dataflowVO = new DataFlowVO();
    DataSetSchema schema = new DataSetSchema();
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(id);
    schema.setTableSchemas(Arrays.asList(tableSchema));
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    dataset.setDatasetSchema(id.toString());
    Mockito.when(referenceDatasetRepository.existsById(Mockito.any())).thenReturn(true);
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(dataset));
    Mockito.when(referenceDatasetRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(dataset));
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    assertEquals(schema, datasetService.getSchemaIfReportable(0L, id.toString()));
  }

  /**
   * Gets the schema if reference design test.
   *
   * @return the schema if reference design test
   */
  @Test
  public void getSchemaIfReferenceDesignTest() {
    ObjectId id = new ObjectId();
    ReferenceDataset dataset = new ReferenceDataset();
    dataset.setUpdatable(true);
    DataFlowVO dataflowVO = new DataFlowVO();
    DataSetSchema schema = new DataSetSchema();
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(id);
    schema.setTableSchemas(Arrays.asList(tableSchema));
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    dataset.setDatasetSchema(id.toString());
    Mockito.when(referenceDatasetRepository.existsById(Mockito.any())).thenReturn(true);
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(dataset));
    Mockito.when(referenceDatasetRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(dataset));
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    assertEquals(schema, datasetService.getSchemaIfReportable(0L, id.toString()));
  }

  /**
   * Gets the schema if reportable draft null test.
   *
   * @return the schema if reportable draft null test
   */
  @Test
  public void getSchemaIfReportableDraftNullTest() {
    ObjectId id = new ObjectId();
    ReportingDataset dataset = new ReportingDataset();
    DataFlowVO dataflowVO = new DataFlowVO();
    DataSetSchema schema = new DataSetSchema();
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId());
    schema.setTableSchemas(Arrays.asList(tableSchema));
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    dataset.setDatasetSchema(id.toString());
    Mockito.when(reportingDatasetRepository.existsById(Mockito.any())).thenReturn(true);
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(dataset));
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    assertNull(datasetService.getSchemaIfReportable(0L, id.toString()));
  }

  /**
   * Check any schema available test.
   */
  @Test
  public void checkAnySchemaAvailableTest() {
    DataSetMetabase datasetMetabase = new DataSetMetabase();
    datasetMetabase.setDatasetSchema(new ObjectId().toString());
    Mockito.when(dataSetMetabaseRepository.findByDataflowIdAndProviderIdNotNull(Mockito.any()))
        .thenReturn(Arrays.asList(datasetMetabase));
    Mockito.when(schemasRepository.findAvailableInPublicByIdDataSetSchema(Mockito.any()))
        .thenReturn(Boolean.TRUE);
    assertTrue(datasetService.checkAnySchemaAvailableInPublic(0L));
  }

  /**
   * Store records test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @Test(expected = SQLException.class)
  public void storeRecordsTest() throws EEAException, IOException, SQLException {

    FieldValue fieldValue = new FieldValue();
    fieldValue.setValue("value");
    fieldValue.setType(DataType.TEXT);
    List<FieldValue> fieldValues = new ArrayList<>();
    fieldValues.add(fieldValue);

    RecordValue recordValue = new RecordValue();
    recordValue.setFields(fieldValues);
    List<RecordValue> recordValues = new ArrayList<>();
    recordValue.setIdRecordSchema("5cf0e9b3b793310e9ceca190");
    recordValues.add(recordValue);
    recordValues.add(recordValue);

    TableValue tableValue = new TableValue();
    recordValue.setTableValue(tableValue);
    tableValue.setIdTableSchema("5cf0e9b3b793310e9ceca190");
    tableValue.setRecords(recordValues);
    List<TableValue> tableValues = new ArrayList<>();
    tableValues.add(tableValue);

    DatasetValue datasetValue = new DatasetValue();
    datasetValue.setTableValues(tableValues);

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(recordStoreControllerZuul.getConnectionToDataset(Mockito.anyString()))
        .thenReturn(new ConnectionDataVO());
    Mockito
        .when(recordValueIdGenerator
            .generate(Mockito.nullable(SharedSessionContractImplementor.class), Mockito.any()))
        .thenReturn("recordId");
    Mockito
        .when(fieldValueIdGenerator
            .generate(Mockito.nullable(SharedSessionContractImplementor.class), Mockito.any()))
        .thenReturn("fieldId");
    datasetService.storeRecords(1L, recordValues);
  }

  /**
   * Update records with conditions test.
   */
  @Test
  public void updateRecordsWithConditionsTest() {
    FieldSchema fieldSchema4 = new FieldSchema();
    fieldSchema4.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca194"));
    fieldSchema4.setType(DataType.LINK);
    fieldSchema4.setPkHasMultipleValues(Boolean.TRUE);
    fieldSchema4.setReadOnly(Boolean.FALSE);
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    fieldSchemas.add(fieldSchema4);
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    recordSchema.setFieldSchema(fieldSchemas);
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    tableSchema.setRecordSchema(recordSchema);
    tableSchema.setReadOnly(Boolean.FALSE);
    tableSchema.setFixedNumber(Boolean.FALSE);
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(1L);
    when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(tableValue);
    when(recordRepository.findOrderedNativeRecord(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(recordValues);
    datasetService.updateRecordsWithConditions(recordValues, 1L, tableSchema);
    Mockito.verify(recordRepository, times(1)).saveAll(Mockito.any());
  }

  /**
   * Update records with conditions read only test.
   */
  @Test
  public void updateRecordsWithConditionsReadOnlyTest() {
    FieldSchema fieldSchema3 = new FieldSchema();
    fieldSchema3.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca193"));
    fieldSchema3.setType(DataType.MULTISELECT_CODELIST);
    fieldSchema3.setPkHasMultipleValues(Boolean.TRUE);
    fieldSchema3.setReadOnly(Boolean.FALSE);
    FieldSchema fieldSchema4 = new FieldSchema();
    fieldSchema4.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca194"));
    fieldSchema4.setType(DataType.LINK);
    fieldSchema4.setPkHasMultipleValues(Boolean.TRUE);
    fieldSchema4.setReadOnly(Boolean.TRUE);
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    fieldSchemas.add(fieldSchema4);
    fieldSchemas.add(fieldSchema3);
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    recordSchema.setFieldSchema(fieldSchemas);
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    tableSchema.setRecordSchema(recordSchema);
    tableSchema.setReadOnly(Boolean.FALSE);
    tableSchema.setFixedNumber(Boolean.FALSE);
    when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(1L);
    when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(tableValue);
    ArrayList<FieldValue> fields = new ArrayList<>();
    fieldValue.setIdFieldSchema("5cf0e9b3b793310e9ceca193");
    fieldValue.setValue("");
    fieldValue.setId("1L");
    fields.add(fieldValue);
    FieldValue field = new FieldValue();
    field.setId("1L");
    field.setIdFieldSchema("5cf0e9b3b793310e9ceca194");
    fields.add(field);
    field.setValue("");
    recordValues.get(0).setFields(fields);
    when(recordRepository.findOrderedNativeRecord(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(recordValues);
    datasetService.updateRecordsWithConditions(recordValues, 1L, tableSchema);
    Mockito.verify(recordRepository, times(1)).saveAll(Mockito.any());
  }


  /**
   * Gets the mimetype test.
   *
   * @return the mimetype test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getMimetypeTest() throws EEAException {
    assertEquals("csv", datasetService.getMimetype("test.csv"));
  }

  /**
   * Gets the mimetype exception test.
   *
   * @return the mimetype exception test
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getMimetypeExceptionTest() throws EEAException {
    try {
      datasetService.getMimetype("test");
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.FILE_EXTENSION, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete import data in design mode test.
   */
  @Test
  public void deleteImportDataInDesignModeTest() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    tableSchema.setReadOnly(true);
    tableSchema.setToPrefill(false);
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableSchema);
    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setTableSchemas(tableSchemas);
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    datasetService.deleteImportData(1L, false);
    Mockito.verify(recordRepository, times(0)).deleteRecordWithId(Mockito.any());
  }

  /**
   * After tests we remove the files.
   */
  @After
  public void afterTests() {
    File file = new File("./dato");
    try {
      FileUtils.deleteDirectory(file);
    } catch (IOException e) {

    }
    final File downloadDirectory = new File("./");
    for (File f : downloadDirectory.listFiles()) {
      if (f.getName().endsWith(".bin")) {
        f.delete();
      }
    }
  }
}
