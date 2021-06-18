package org.eea.dataset.service.helper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.data.sequence.FieldValueIdGenerator;
import org.eea.dataset.persistence.data.sequence.RecordValueIdGenerator;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.RulesRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.persistence.schemas.repository.UniqueConstraintRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.file.FileParseContextImpl;
import org.eea.dataset.service.file.FileParserFactory;
import org.eea.dataset.service.file.interfaces.IFileExportContext;
import org.eea.dataset.service.file.interfaces.IFileExportFactory;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.eea.interfaces.vo.dataflow.integration.IntegrationParams;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.ETLDatasetVO;
import org.eea.interfaces.vo.dataset.ETLFieldVO;
import org.eea.interfaces.vo.dataset.ETLRecordVO;
import org.eea.interfaces.vo.dataset.ETLTableVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * The Class FileTreatmentHelperTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class FileTreatmentHelperTest {

  /** The file treatment helper. */
  @InjectMocks
  private FileTreatmentHelper fileTreatmentHelper;

  /** The dataset service. */
  @Mock
  private DatasetService datasetService;

  /** The data set mapper. */
  @Mock
  private DataSetMapper dataSetMapper;

  /** The integration controller. */
  @Mock
  private IntegrationControllerZuul integrationController;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset metabase service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The rules repository. */
  @Mock
  private RulesRepository rulesRepository;

  /** The unique constraint repository. */
  @Mock
  private UniqueConstraintRepository uniqueConstraintRepository;

  /** The rules controller zuul. */
  @Mock
  private RulesControllerZuul rulesControllerZuul;

  /** The dataflow controller zuul. */
  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The record store controller zuul. */
  @Mock
  private RecordStoreControllerZuul recordStoreControllerZuul;

  /** The lock service. */
  @Mock
  private LockService lockService;

  /** The record value id generator. */
  @Mock
  private RecordValueIdGenerator recordValueIdGenerator;

  /** The field value id generator. */
  @Mock
  private FieldValueIdGenerator fieldValueIdGenerator;

  /** The table repository. */
  @Mock
  private TableRepository tableRepository;

  /** The record repository. */
  @Mock
  private RecordRepository recordRepository;

  /** The data set metabase repository. */
  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The file export factory. */
  @Mock
  private IFileExportFactory fileExportFactory;

  /** The context export. */
  @Mock
  private IFileExportContext contextExport;

  /** The dataset repository. */
  @Mock
  private DatasetRepository datasetRepository;

  /** The schemas repository. */
  @Mock
  private SchemasRepository schemasRepository;

  /** The representative controller zuul. */
  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The partition data set metabase repository. */
  @Mock
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  @Mock
  private FileParserFactory fileParserFactory;

  /** The context. */
  @Mock
  private FileParseContextImpl context;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("user", "password"));
    ReflectionTestUtils.setField(fileTreatmentHelper, "importPath",
        this.getClass().getClassLoader().getResource("").getPath());
    ReflectionTestUtils.setField(fileTreatmentHelper, "importExecutorService",
        new CurrentThreadExecutor());
  }

  /**
   * Import file data csv test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void importFileDataCsvTest() throws EEAException, IOException {

    FieldValue fieldValue = new FieldValue();
    fieldValue.setValue("value");
    fieldValue.setType(DataType.TEXT);
    List<FieldValue> fieldValues = new ArrayList<>();
    fieldValues.add(fieldValue);

    RecordValue recordValue = new RecordValue();
    recordValue.setFields(fieldValues);
    List<RecordValue> recordValues = new ArrayList<>();
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

    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    tableSchema.setNameTableSchema("tableSchemaName");
    tableSchema.setFixedNumber(Boolean.FALSE);
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableSchema);

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    datasetSchema.setIdDataFlow(1L);
    datasetSchema.setTableSchemas(tableSchemas);

    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setOperation(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);
    List<IntegrationVO> integrationVOs = new ArrayList<>();
    integrationVOs.add(integrationVO);

    MultipartFile multipartFile =
        new MockMultipartFile("file", "tableSchemaName.csv", "text/csv", "".getBytes());

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.any()))
        .thenReturn(datasetSchema);

    Mockito.when(datasetService.getMimetype(Mockito.anyString())).thenReturn("csv");
    Mockito.when(datasetService.getDatasetType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.REPORTING);
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    Mockito.when(partitionDataSetMetabaseRepository
        .findFirstByIdDataSet_idAndUsername(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(fileParserFactory.createContext(Mockito.any(), Mockito.any())).thenReturn(context);
    fileTreatmentHelper.importFileData(1L, null, multipartFile, true);
    FileUtils
        .deleteDirectory(new File(this.getClass().getClassLoader().getResource("").getPath(), "1"));

    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Import file data zip test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  // @Test
  public void importFileDataZipTest() throws EEAException, IOException {

    FieldValue fieldValue = new FieldValue();
    fieldValue.setValue("value");
    fieldValue.setType(DataType.TEXT);
    List<FieldValue> fieldValues = new ArrayList<>();
    fieldValues.add(fieldValue);

    RecordValue recordValue = new RecordValue();
    recordValue.setFields(fieldValues);
    List<RecordValue> recordValues = new ArrayList<>();
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

    TableSchema tableSchema1 = new TableSchema();
    tableSchema1.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    tableSchema1.setNameTableSchema("Other");
    TableSchema tableSchema2 = new TableSchema();
    tableSchema2.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    tableSchema2.setNameTableSchema("Table");
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableSchema1);
    tableSchemas.add(tableSchema2);

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    datasetSchema.setIdDataFlow(1L);
    datasetSchema.setTableSchemas(tableSchemas);

    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setOperation(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);
    List<IntegrationVO> integrationVOs = new ArrayList<>();
    integrationVOs.add(integrationVO);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zip = new ZipOutputStream(baos);
    ZipEntry entry1 = new ZipEntry("Table.csv");
    ZipEntry entry2 = new ZipEntry("Table.txt");
    zip.putNextEntry(entry1);
    zip.putNextEntry(entry2);
    zip.close();
    MultipartFile multipartFile = new MockMultipartFile("file", "file.zip",
        "application/x-zip-compressed", baos.toByteArray());

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.any()))
        .thenReturn(datasetSchema);

    Mockito.when(datasetService.getMimetype(Mockito.anyString())).thenReturn("zip")
        .thenReturn("csv").thenReturn("txt").thenReturn("csv");
    Mockito.doNothing().when(datasetService).deleteImportData(Mockito.anyLong());
    // doNothing().when(fileTreatmentHelper).processFile(Mockito.anyLong(), Mockito.any(),
    // Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.when(dataSetMapper.classToEntity(Mockito.any())).thenReturn(datasetValue);
    Mockito.when(datasetService.findTableIdByTableSchema(Mockito.anyLong(), Mockito.any()))
        .thenReturn(1L);
    Mockito.when(datasetService.getDatasetType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.REPORTING);
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
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

    fileTreatmentHelper.importFileData(1L, null, multipartFile, true);
    FileUtils
        .deleteDirectory(new File(this.getClass().getClassLoader().getResource("").getPath(), "1"));

    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Import file data xls FME test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void importFileDataXlsFMETest() throws EEAException, IOException {

    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    tableSchema.setFixedNumber(Boolean.FALSE);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableSchema);

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    datasetSchema.setIdDataFlow(1L);
    datasetSchema.setTableSchemas(tableSchemas);

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);

    Map<String, String> internalParameters = new HashMap<>();
    internalParameters.put(IntegrationParams.FILE_EXTENSION, "xls");
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setInternalParameters(internalParameters);
    integrationVO.setOperation(IntegrationOperationTypeEnum.IMPORT);
    List<IntegrationVO> integrationVOs = new ArrayList<>();
    integrationVOs.add(integrationVO);

    Map<String, Object> executionResultParams = new HashMap<>();
    executionResultParams.put("id", 1);
    ExecutionResultVO executionResultVO = new ExecutionResultVO();
    executionResultVO.setExecutionResultParams(executionResultParams);

    MultipartFile multipartFile =
        new MockMultipartFile("file", "file.xls", "application/vnd.ms-excel", "".getBytes());

    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.any()))
        .thenReturn(datasetSchema);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());

    Mockito.when(datasetService.getMimetype(Mockito.anyString())).thenReturn("xls");
    // Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    // Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.when(integrationController.findAllIntegrationsByCriteria(Mockito.any()))
        .thenReturn(integrationVOs);
    Mockito.when(integrationController.executeIntegrationProcess(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.anyLong(), Mockito.any())).thenReturn(executionResultVO);

    fileTreatmentHelper.importFileData(1L, "5cf0e9b3b793310e9ceca190", multipartFile, false);
    FileUtils
        .deleteDirectory(new File(this.getClass().getClassLoader().getResource("").getPath(), "1"));

    Mockito.verify(integrationController, times(1)).executeIntegrationProcess(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Import file data exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void importFileDataExceptionTest() throws EEAException {
    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.getOriginalFilename()).thenReturn("fileName.csv");
    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.any()))
        .thenReturn(null);
    try {
      fileTreatmentHelper.importFileData(1L, "5cf0e9b3b793310e9ceca190", file, true);
    } catch (EEAException e) {
      Assert.assertEquals(
          "Dataset not reportable: datasetId=1, tableSchemaId=5cf0e9b3b793310e9ceca190",
          e.getMessage());
      throw e;
    }
  }

  /**
   * Import file data folder exception test.
   *
   * @throws EEAException the EEA exception
   */
  // @Test(expected = EEAException.class)
  public void importFileDataFolderExceptionTest() throws EEAException {
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    tableSchema.setFixedNumber(Boolean.FALSE);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableSchema);

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setTableSchemas(tableSchemas);

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    // Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    // Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.anyString()))
        .thenReturn(datasetSchema);
    MultipartFile multipartFile =
        new MockMultipartFile("file", "file.xls", "application/vnd.ms-excel", "".getBytes());
    File folder = new File(this.getClass().getClassLoader().getResource("").getPath(), "1");
    folder.mkdirs();
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    try {
      fileTreatmentHelper.importFileData(1L, "5cf0e9b3b793310e9ceca190", multipartFile, true);
    } catch (EEAException e) {
      Assert.assertEquals("Folder for dataset 1 already exists", e.getMessage());
      throw e;
    }
  }

  /**
   * Import file data IO exception test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void importFileDataIOExceptionTest() throws IOException, EEAException {
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    tableSchema.setFixedNumber(Boolean.FALSE);

    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableSchema);

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setTableSchemas(tableSchemas);

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    // Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    // Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.anyString()))
        .thenReturn(datasetSchema);
    MultipartFile file = Mockito.mock(MultipartFile.class);
    IOException returningException = new IOException();
    Mockito.when(file.getInputStream()).thenThrow(returningException);
    Mockito.when(file.getName()).thenReturn("fileName.csv");
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    try {
      fileTreatmentHelper.importFileData(1L, "5cf0e9b3b793310e9ceca190", file, true);
    } catch (EEAException e) {
      Assert.assertEquals(returningException, e.getCause());
      throw e;
    }
  }

  /**
   * Import file data csv fixed number test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  // @Test
  public void importFileDataCsvFixedNumberTest() throws EEAException, IOException {

    FieldValue fieldValue = new FieldValue();
    fieldValue.setValue("value");
    fieldValue.setType(DataType.TEXT);
    fieldValue.setIdFieldSchema("5cf0e9b3b793310e9ceca190");
    FieldValue fieldValue1 = new FieldValue();
    fieldValue1.setValue("value");
    fieldValue1.setType(DataType.TEXT);
    fieldValue1.setIdFieldSchema("606588ab41bf6a1c0c65d36e");
    List<FieldValue> fieldValues = new ArrayList<>();
    fieldValues.add(fieldValue);
    fieldValues.add(fieldValue1);

    RecordValue recordValue = new RecordValue();
    recordValue.setFields(fieldValues);
    List<RecordValue> recordValues = new ArrayList<>();
    recordValues.add(recordValue);

    TableValue tableValue = new TableValue();
    recordValue.setTableValue(tableValue);
    tableValue.setIdTableSchema("5cf0e9b3b793310e9ceca190");
    tableValue.setRecords(recordValues);

    List<TableValue> tableValues = new ArrayList<>();
    tableValues.add(tableValue);

    DatasetValue datasetValue = new DatasetValue();
    datasetValue.setTableValues(tableValues);

    FieldSchema fieldSchema = new FieldSchema();
    fieldSchema.setReadOnly(true);
    fieldSchema.setIdFieldSchema(new ObjectId("606588ab41bf6a1c0c65d36e"));
    FieldSchema fieldSchema2 = new FieldSchema();
    fieldSchema2.setReadOnly(false);
    fieldSchema2.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));

    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(new ObjectId("606588ab41bf6a1c0c65d36e"));
    recordSchema.setFieldSchema(java.util.Arrays.asList(fieldSchema, fieldSchema2));

    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    tableSchema.setNameTableSchema("tableSchemaName");
    tableSchema.setFixedNumber(Boolean.TRUE);
    tableSchema.setRecordSchema(recordSchema);
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableSchema);

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    datasetSchema.setIdDataFlow(1L);
    datasetSchema.setTableSchemas(tableSchemas);

    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setOperation(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);
    List<IntegrationVO> integrationVOs = new ArrayList<>();
    integrationVOs.add(integrationVO);

    MultipartFile multipartFile =
        new MockMultipartFile("file", "tableSchemaName.csv", "text/csv", "".getBytes());

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.any()))
        .thenReturn(datasetSchema);

    Mockito.when(datasetService.getMimetype(Mockito.anyString())).thenReturn("csv");
    // doNothing().when(fileTreatmentHelper).processFile(Mockito.anyLong(), Mockito.any(),
    // Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.when(dataSetMapper.classToEntity(Mockito.any())).thenReturn(datasetValue);
    Mockito.when(datasetService.findTableIdByTableSchema(Mockito.anyLong(), Mockito.any()))
        .thenReturn(null);
    Mockito.when(datasetService.getDatasetType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.REPORTING);
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    Mockito.when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(2L);
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(new TableValue());
    Mockito
        .when(recordRepository.findOrderedNativeRecord(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(recordValues);
    fileTreatmentHelper.importFileData(1L, null, multipartFile, true);
    FileUtils
        .deleteDirectory(new File(this.getClass().getClassLoader().getResource("").getPath(), "1"));

    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Import file data csv fixed number no read only test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  // @Test
  public void importFileDataCsvFixedNumberNoReadOnlyTest() throws EEAException, IOException {

    FieldValue fieldValue = new FieldValue();
    fieldValue.setValue("value");
    fieldValue.setType(DataType.TEXT);
    fieldValue.setIdFieldSchema("5cf0e9b3b793310e9ceca190");
    FieldValue fieldValue1 = new FieldValue();
    fieldValue1.setValue("value");
    fieldValue1.setType(DataType.TEXT);
    fieldValue1.setIdFieldSchema("606588ab41bf6a1c0c65d36e");
    List<FieldValue> fieldValues = new ArrayList<>();
    fieldValues.add(fieldValue);
    fieldValues.add(fieldValue1);

    RecordValue recordValue = new RecordValue();
    recordValue.setFields(fieldValues);
    List<RecordValue> recordValues = new ArrayList<>();
    recordValues.add(recordValue);

    TableValue tableValue = new TableValue();
    recordValue.setTableValue(tableValue);
    tableValue.setIdTableSchema("5cf0e9b3b793310e9ceca190");
    tableValue.setRecords(recordValues);

    List<TableValue> tableValues = new ArrayList<>();
    tableValues.add(tableValue);

    DatasetValue datasetValue = new DatasetValue();
    datasetValue.setTableValues(tableValues);

    FieldSchema fieldSchema = new FieldSchema();
    fieldSchema.setReadOnly(false);
    fieldSchema.setIdFieldSchema(new ObjectId("606588ab41bf6a1c0c65d36e"));
    FieldSchema fieldSchema2 = new FieldSchema();
    fieldSchema2.setReadOnly(false);
    fieldSchema2.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));

    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(new ObjectId("606588ab41bf6a1c0c65d36e"));
    recordSchema.setFieldSchema(java.util.Arrays.asList(fieldSchema, fieldSchema2));

    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    tableSchema.setNameTableSchema("tableSchemaName");
    tableSchema.setFixedNumber(Boolean.TRUE);
    tableSchema.setRecordSchema(recordSchema);
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(tableSchema);

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    datasetSchema.setIdDataFlow(1L);
    datasetSchema.setTableSchemas(tableSchemas);

    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setOperation(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);
    List<IntegrationVO> integrationVOs = new ArrayList<>();
    integrationVOs.add(integrationVO);

    MultipartFile multipartFile =
        new MockMultipartFile("file", "tableSchemaName.csv", "text/csv", "".getBytes());

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.any()))
        .thenReturn(datasetSchema);

    Mockito.when(datasetService.getMimetype(Mockito.anyString())).thenReturn("csv");
    // doNothing().when(fileTreatmentHelper).processFile(Mockito.anyLong(), Mockito.any(),
    // Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.when(dataSetMapper.classToEntity(Mockito.any())).thenReturn(datasetValue);
    Mockito.when(datasetService.findTableIdByTableSchema(Mockito.anyLong(), Mockito.any()))
        .thenReturn(null);
    Mockito.when(datasetService.getDatasetType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.REPORTING);
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    Mockito.when(tableRepository.findByIdTableSchema(Mockito.any())).thenReturn(new TableValue());
    Mockito
        .when(recordRepository.findOrderedNativeRecord(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(recordValues);
    Mockito.when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(2L);
    fileTreatmentHelper.importFileData(1L, null, multipartFile, true);
    FileUtils
        .deleteDirectory(new File(this.getClass().getClassLoader().getResource("").getPath(), "1"));

    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }


  @Test
  public void exportDatasetFileXlsxTest() throws IOException, InvalidFileException, EEAException {
    DataSetMetabaseVO dataSetMetabase = new DataSetMetabaseVO();
    dataSetMetabase.setDataflowId(1L);
    dataSetMetabase.setDataProviderId(1L);
    dataSetMetabase.setDatasetSchema("603362319d49f04fce13b68f");
    dataSetMetabase.setDataSetName("file");

    byte[] expectedResult = null;
    when(fileExportFactory.createContext(Mockito.any())).thenReturn(contextExport);
    when(contextExport.fileWriter(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean(),
        Mockito.anyBoolean())).thenReturn(expectedResult);
    fileTreatmentHelper.exportDatasetFile(1L, "xslx");
    Mockito.verify(fileExportFactory, times(1)).createContext(Mockito.any());
  }

  @Test
  public void exportDatasetFilecsvTest() throws IOException, InvalidFileException, EEAException {
    DataSetMetabaseVO dataSetMetabase = new DataSetMetabaseVO();
    dataSetMetabase.setDataflowId(1L);
    dataSetMetabase.setDataProviderId(1L);
    dataSetMetabase.setDatasetSchema("603362319d49f04fce13b68f");
    dataSetMetabase.setDataSetName("file");

    when(fileExportFactory.createContext(Mockito.any())).thenReturn(contextExport);
    // when(
    // contextExport.fileWriter(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
    // .thenReturn(expectedResult);
    when(datasetRepository.findIdDatasetSchemaById(Mockito.any()))
        .thenReturn("603362319d49f04fce13b68f");
    when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(new DataSetSchema()));
    fileTreatmentHelper.exportDatasetFile(1L, "csv");
    Mockito.verify(fileExportFactory, times(1)).createContext(Mockito.any());
  }

  /**
   * Etl import dataset schema not found test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void etlImportDatasetSchemaIdNotFoundTest() throws EEAException {
    try {
      fileTreatmentHelper.etlImportDataset(1L, new ETLDatasetVO(), 1L);
    } catch (EEAException e) {
      assertEquals(String.format(EEAErrorMessage.DATASET_SCHEMA_ID_NOT_FOUND, 1L), e.getMessage());
      throw e;
    }
  }

  /**
   * Etl import dataset not found test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void etlImportDatasetNotFoundTest() throws EEAException {
    Mockito.when(datasetRepository.findIdDatasetSchemaById(Mockito.any()))
        .thenReturn("5cf0e9b3b793310e9ceca190");
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    try {
      fileTreatmentHelper.etlImportDataset(1L, new ETLDatasetVO(), 1L);
    } catch (EEAException e) {
      assertEquals(
          String.format(EEAErrorMessage.DATASET_SCHEMA_NOT_FOUND, "5cf0e9b3b793310e9ceca190"),
          e.getMessage());
      throw e;
    }
  }

  /**
   * Et import dataset test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void etImportDatasetTest() throws EEAException {
    ETLDatasetVO etlDatasetVO = new ETLDatasetVO();
    List<ETLTableVO> etlTableVOs = new ArrayList<>();
    ETLTableVO etlTableVO = new ETLTableVO();
    List<ETLRecordVO> etlRecordVOs = new ArrayList<>();
    ETLRecordVO etlRecordVO = new ETLRecordVO();
    List<ETLFieldVO> etlFieldVOs = new ArrayList<>();
    ETLFieldVO etlFieldVO = new ETLFieldVO();
    etlDatasetVO.setTables(etlTableVOs);
    etlTableVOs.add(etlTableVO);
    etlTableVO.setTableName("nameTableSchema");
    etlTableVO.setRecords(etlRecordVOs);
    etlRecordVOs.add(etlRecordVO);
    etlRecordVO.setFields(etlFieldVOs);
    etlFieldVOs.add(etlFieldVO);
    etlFieldVO.setFieldName("headerName");
    etlFieldVO.setValue("value");

    DataSetSchema datasetSchema = new DataSetSchema();
    List<TableSchema> tableSchemas = new ArrayList<>();
    TableSchema tableSchema = new TableSchema();
    tableSchema.setReadOnly(Boolean.FALSE);
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(new ObjectId());
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    FieldSchema fieldSchema = new FieldSchema();
    FieldSchema fieldSchema2 = new FieldSchema();
    List<RecordValue> recordValues = new ArrayList<>();
    RecordValue recordValue = new RecordValue();
    List<FieldValue> fieldValues = new ArrayList<>();
    FieldValue fieldValue = new FieldValue();
    datasetSchema.setTableSchemas(tableSchemas);
    tableSchemas.add(tableSchema);
    tableSchema.setIdTableSchema(new ObjectId());
    tableSchema.setNameTableSchema("nameTableSchema");
    tableSchema.setRecordSchema(recordSchema);
    recordSchema.setFieldSchema(fieldSchemas);
    fieldSchema.setHeaderName("headerName");
    fieldSchema.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    fieldSchema.setType(DataType.ATTACHMENT);
    fieldSchema2.setHeaderName("headerName1");
    fieldSchema2.setIdFieldSchema(new ObjectId());
    fieldSchema2.setType(DataType.BOOLEAN);
    fieldSchemas.add(fieldSchema);
    fieldSchemas.add(fieldSchema2);
    recordValues.add(recordValue);
    recordValue.setFields(fieldValues);
    fieldValues.add(fieldValue);
    fieldValue.setIdFieldSchema("5cf0e9b3b793310e9ceca190");
    fieldValue.setValue("value");

    Mockito.when(datasetRepository.findIdDatasetSchemaById(Mockito.any()))
        .thenReturn(new ObjectId().toString());
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetSchema));
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.any()))
        .thenReturn(new DataProviderVO());
    Mockito.when(partitionDataSetMetabaseRepository
        .findFirstByIdDataSet_idAndUsername(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(new PartitionDataSetMetabase()));
    fileTreatmentHelper.etlImportDataset(1L, etlDatasetVO, 1L);
    Mockito.verify(recordRepository, times(1)).saveAll(Mockito.any());
  }

  /**
   * Test process file throw exception.
   *
   * @throws Exception the exception
   */
  // @Test(expected = EEAException.class)
  public void testProcessFileThrowException() throws Exception {
    new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    // fileTreatmentHelper.processFile(null, "fileOriginal", fileNoExtension.getInputStream(), null,
    // true, new DataSetSchema());
    Mockito.verify(fileParserFactory, times(1)).createContext(Mockito.any(), Mockito.any());
  }

  /**
   * Test process file bad extension throw exception.
   *
   * @throws Exception the exception
   */
  // @Test(expected = EEAException.class)
  public void testProcessFileBadExtensionThrowException() throws Exception {
    new MockMultipartFile("file", "fileOriginal.doc", "doc", "content".getBytes());
    // fileTreatmentHelper.processFile(1L, "fileOriginal.doc", fileBadExtension.getInputStream(),
    // null,
    // true, new DataSetSchema());
    Mockito.verify(fileParserFactory, times(1)).createContext(Mockito.any(), Mockito.any());
  }

  /**
   * Test process file throw exception 2.
   *
   * @throws Exception the exception
   */
  // @Test(expected = EEAException.class)
  public void testProcessFileThrowException2() throws Exception {
    new MockMultipartFile("file", "fileOriginal", "cvs", "content".getBytes());
    // fileTreatmentHelper.processFile(1L, "fileOriginal", fileNoExtension.getInputStream(), null,
    // true, new DataSetSchema());
    Mockito.verify(fileParserFactory, times(1)).createContext(Mockito.any(), Mockito.any());
  }

  /**
   * Test process file empty dataset.
   *
   * @throws Exception the exception
   */
  // @Test(expected = IOException.class)
  public void testProcessFileEmptyDataset() throws Exception {
    new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(fileParserFactory.createContext(Mockito.any(), Mockito.any())).thenReturn(context);
    doNothing().when(context).parse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    // fileTreatmentHelper.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null,
    // true, new DataSetSchema());
    Mockito.verify(fileParserFactory, times(1)).createContext(Mockito.any(), Mockito.any());
  }

  /**
   * Test process file empty partition metabase.
   *
   * @throws Exception the exception
   */
  // @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabase() throws Exception {
    new MockMultipartFile("file", "fileOriginal.csv", "cvs", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    // fileTreatmentHelper.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null,
    // true, new DataSetSchema());
    Mockito.verify(fileParserFactory, times(1)).createContext(Mockito.any(), Mockito.any());
  }

  /**
   * Test process file empty partition metabase xml.
   *
   * @throws Exception the exception
   */
  // @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabaseXml() throws Exception {
    new MockMultipartFile("file", "fileOriginal.xml", "xml", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    // fileTreatmentHelper.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null,
    // true, new DataSetSchema());
    Mockito.verify(fileParserFactory, times(1)).createContext(Mockito.any(), Mockito.any());
  }

  /**
   * Test process file empty partition metabase xls.
   *
   * @throws Exception the exception
   */
  // @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabaseXls() throws Exception {
    new MockMultipartFile("file", "fileOriginal.xls", "xls", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    // fileTreatmentHelper.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null,
    // true, new DataSetSchema());
    Mockito.verify(fileParserFactory, times(1)).createContext(Mockito.any(), Mockito.any());

  }

  /**
   * Test process file empty partition metabase xlsx.
   *
   * @throws Exception the exception
   */
  // @Test(expected = EEAException.class)
  public void testProcessFileEmptyPartitionMetabaseXlsx() throws Exception {
    new MockMultipartFile("file", "fileOriginal.xlsx", "xlsx", "content".getBytes());
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.empty());
    // fileTreatmentHelper.processFile(1L, file.getOriginalFilename(), file.getInputStream(), null,
    // true, new DataSetSchema());
    Mockito.verify(fileParserFactory, times(1)).createContext(Mockito.any(), Mockito.any());
  }

  /**
   * Test process file success update table.
   *
   * @throws Exception the exception
   */
  // @Test
  public void testProcessFileSuccessUpdateTable() throws Exception {
    new MockMultipartFile("file", "fileOriginal.csv", "csv", "content".getBytes());

    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));

    when(fileParserFactory.createContext(Mockito.any(), Mockito.any())).thenReturn(context);
    final DataSetVO dataSetVO = new DataSetVO();
    dataSetVO.setId(1L);
    doNothing().when(context).parse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    // fileTreatmentHelper.processFile(1L, "fileOriginal.csv", file.getInputStream(), "", true,
    // new DataSetSchema());
    Mockito.verify(fileParserFactory, times(1)).createContext(Mockito.any(), Mockito.any());
  }

  @After
  public void afterTests() {
    File file = new File("./dataset-1");
    try {
      FileUtils.deleteDirectory(file);
    } catch (IOException e) {

    }
  }

}


/**
 * The Class CurrentThreadExecutor.
 */
class CurrentThreadExecutor extends AbstractExecutorService {

  @Override
  public void shutdown() {}

  @Override
  public List<Runnable> shutdownNow() {
    return null;
  }

  @Override
  public boolean isShutdown() {
    return false;
  }

  @Override
  public boolean isTerminated() {
    return false;
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return false;
  }

  @Override
  public void execute(Runnable command) {
    command.run();
  }
}
