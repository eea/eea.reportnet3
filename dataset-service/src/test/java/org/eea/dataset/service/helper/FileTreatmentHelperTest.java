package org.eea.dataset.service.helper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eea.dataset.controller.EEAMockMultipartFile;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.persistence.data.domain.AttachmentValue;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.AttachmentRepository;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.data.sequence.FieldValueIdGenerator;
import org.eea.dataset.persistence.data.sequence.RecordValueIdGenerator;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
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
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.eea.interfaces.vo.dataflow.integration.IntegrationParams;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.ETLDatasetVO;
import org.eea.interfaces.vo.dataset.ETLFieldVO;
import org.eea.interfaces.vo.dataset.ETLRecordVO;
import org.eea.interfaces.vo.dataset.ETLTableVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.junit.*;
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

  /** The file parser factory. */
  @Mock
  private FileParserFactory fileParserFactory;

  /** The context. */
  @Mock
  private FileParseContextImpl context;

  /** The attachment repository. */
  @Mock
  private AttachmentRepository attachmentRepository;

  /** The notification controller zuul. */
  @Mock
  private NotificationControllerZuul notificationControllerZuul;

  /** The design dataset repository. */
  @Mock
  private DesignDatasetRepository designDatasetRepository;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
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
  @Ignore
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

    Mockito.when(datasetService.getMimetype(Mockito.anyString()))
        .thenReturn(FileTypeEnum.CSV.getValue());
    // Mockito.when(datasetService.getDatasetType(Mockito.anyLong()))
    // .thenReturn(DatasetTypeEnum.REPORTING);
    // Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
    // Mockito.any(), Mockito.any());
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataProviderId(1L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(partitionDataSetMetabaseRepository
        .findFirstByIdDataSet_idAndUsername(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(new PartitionDataSetMetabase()));
    when(fileParserFactory.createContext(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(context);
    fileTreatmentHelper.importFileData(1L, 2L,null, multipartFile, true, 1L, null, null);

    // Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
    // Mockito.any(), Mockito.any());
    final File downloadDirectory = new File("./");
    for (File f : downloadDirectory.listFiles()) {
      if (f.getName().equals("1")) {
        f.delete();
      }
    }
  }


  /**
   * Import file data zip test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  @Ignore
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

    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.any()))
        .thenReturn(datasetSchema);

    Mockito.when(datasetService.getMimetype(Mockito.anyString())).thenReturn("zip")
        .thenReturn(FileTypeEnum.CSV.getValue()).thenReturn("txt")
        .thenReturn(FileTypeEnum.CSV.getValue());
    Mockito.doNothing().when(datasetService).deleteImportData(Mockito.anyLong(),
        Mockito.anyBoolean());
    // Mockito.when(datasetService.getDatasetType(Mockito.anyLong()))
    // .thenReturn(DatasetTypeEnum.REPORTING);
    // Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
    // Mockito.any(), Mockito.any());

    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());

    fileTreatmentHelper.importFileData(1L,2L, null, multipartFile, true, 1L, null, null);
    final File downloadDirectory = new File("./");
    for (File f : downloadDirectory.listFiles()) {
      if (f.getName().equals("1")) {
        f.delete();
      }
    }

    // Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
    // Mockito.any(), Mockito.any());
  }

  /**
   * Import file data xls FME test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  @Ignore
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
    internalParameters.put(IntegrationParams.FILE_EXTENSION, FileTypeEnum.XLS.getValue());
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

    Mockito.when(datasetService.getMimetype(Mockito.anyString()))
        .thenReturn(FileTypeEnum.XLS.getValue());
    // Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    // Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);
    Mockito.when(integrationController.findIntegrationById(Mockito.anyLong()))
        .thenReturn(integrationVO);
    // Mockito.when(integrationController.executeIntegrationProcess(Mockito.any(), Mockito.any(),
    // Mockito.any(), Mockito.anyLong(), Mockito.any())).thenReturn(executionResultVO);

    fileTreatmentHelper.importFileData(1L,2L, "5cf0e9b3b793310e9ceca190", multipartFile, false, 1L,
        null, null);
    FileUtils
        .deleteDirectory(new File(this.getClass().getClassLoader().getResource("").getPath(), "1"));

    Mockito.verify(kafkaSenderUtils, times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
  }

  /**
   * Import file data exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  @Ignore
  public void importFileDataExceptionTest() throws EEAException {
    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.getOriginalFilename()).thenReturn("fileName.csv");
    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.any()))
        .thenReturn(null);
    try {
      fileTreatmentHelper.importFileData(1L, 2L,"5cf0e9b3b793310e9ceca190", file, true, 1L, null, null);
    } catch (EEAException e) {
      Assert.assertEquals(
          "Dataset not reportable: datasetId=1, tableSchemaId=5cf0e9b3b793310e9ceca190",
          e.getMessage());
      throw e;
    }
  }

  @Test(expected = EEAException.class)
  public void importFileDataDelimiterExceptionTest() throws EEAException {
    MultipartFile file = Mockito.mock(MultipartFile.class);
    try {
      fileTreatmentHelper.importFileData(1L, 2L,"5cf0e9b3b793310e9ceca190", file, true, 1L, "%%", null);
    } catch (EEAException e) {
      Assert.assertEquals("The size of the delimiter cannot be greater than 1", e.getMessage());
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
  @Ignore
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

    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.anyString()))
        .thenReturn(datasetSchema);
    MultipartFile file = new EEAMockMultipartFile("fileName.csv", "".getBytes(), true);

    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    try {
      fileTreatmentHelper.importFileData(1L,2L, "5cf0e9b3b793310e9ceca190", file, true, 1L, null, null);
    } catch (EEAException e) {
      Assert.assertEquals("Controlled Error", e.getCause().getMessage());
      throw e;
    }
  }


  /**
   * Export dataset file xlsx test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidFileException the invalid file exception
   * @throws EEAException the EEA exception
   */
  @Test
  public void exportDatasetFileXlsxTest() throws IOException, InvalidFileException, EEAException {
    DataSetMetabaseVO dataSetMetabase = new DataSetMetabaseVO();
    dataSetMetabase.setDataflowId(1L);
    dataSetMetabase.setDataProviderId(1L);
    dataSetMetabase.setDatasetSchema("603362319d49f04fce13b68f");
    dataSetMetabase.setDataSetName("file");
    DataSetSchema datasetSchema = new DataSetSchema();
    List<TableSchema> tableSchemas = new ArrayList<>();
    TableSchema tableSchema = new TableSchema();
    tableSchema.setReadOnly(Boolean.FALSE);
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(new ObjectId());
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    FieldSchema fieldSchema = new FieldSchema();
    FieldSchema fieldSchema2 = new FieldSchema();
    tableSchema.setNameTableSchema("nameTableSchema");
    tableSchema.setIdTableSchema(new ObjectId());
    tableSchema.setRecordSchema(recordSchema);
    tableSchemas.add(tableSchema);
    fieldSchema.setHeaderName("headerName");
    fieldSchema.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    fieldSchema.setType(DataType.ATTACHMENT);
    fieldSchema2.setHeaderName("headerName1");
    fieldSchema2.setIdFieldSchema(new ObjectId());
    fieldSchema2.setType(DataType.BOOLEAN);
    fieldSchemas.add(fieldSchema);
    fieldSchemas.add(fieldSchema2);
    recordSchema.setFieldSchema(fieldSchemas);
    datasetSchema.setTableSchemas(tableSchemas);

    when(fileExportFactory.createContext(Mockito.any())).thenReturn(contextExport);
    when(contextExport.fileWriter(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean(), Mockito.any())).thenReturn("xlsx".getBytes());
    when(datasetMetabaseService.findDatasetMetabase(Mockito.any())).thenReturn(dataSetMetabase);
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    List<AttachmentValue> attachments = new ArrayList<>();
    AttachmentValue attachment = new AttachmentValue();
    attachment.setContent(" ".getBytes());
    attachments.add(attachment);
    when(attachmentRepository.findAllByIdFieldSchemaAndValueIsNotNull(Mockito.any()))
        .thenReturn(attachments);
    fileTreatmentHelper.exportDatasetFile(1L, "zip xslx");
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void exportDatasetFileXlsxExceptionTest()
      throws IOException, InvalidFileException, EEAException {
    DataSetMetabaseVO dataSetMetabase = new DataSetMetabaseVO();
    dataSetMetabase.setDataflowId(1L);
    dataSetMetabase.setDataProviderId(1L);
    dataSetMetabase.setDatasetSchema("603362319d49f04fce13b68f");
    dataSetMetabase.setDataSetName("file");
    DataSetSchema datasetSchema = new DataSetSchema();
    List<TableSchema> tableSchemas = new ArrayList<>();
    TableSchema tableSchema = new TableSchema();
    tableSchema.setReadOnly(Boolean.FALSE);
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(new ObjectId());
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    FieldSchema fieldSchema = new FieldSchema();
    FieldSchema fieldSchema2 = new FieldSchema();
    tableSchema.setNameTableSchema("nameTableSchema");
    tableSchema.setIdTableSchema(new ObjectId());
    tableSchema.setRecordSchema(recordSchema);
    tableSchemas.add(tableSchema);
    fieldSchema.setHeaderName("headerName");
    fieldSchema.setIdFieldSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    fieldSchema.setType(DataType.ATTACHMENT);
    fieldSchema2.setHeaderName("headerName1");
    fieldSchema2.setIdFieldSchema(new ObjectId());
    fieldSchema2.setType(DataType.BOOLEAN);
    fieldSchemas.add(fieldSchema);
    fieldSchemas.add(fieldSchema2);
    recordSchema.setFieldSchema(fieldSchemas);
    datasetSchema.setTableSchemas(tableSchemas);

    when(fileExportFactory.createContext(Mockito.any())).thenReturn(contextExport);
    when(contextExport.fileWriter(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean(), Mockito.any())).thenReturn("xlsx".getBytes());
    when(datasetMetabaseService.findDatasetMetabase(Mockito.any())).thenReturn(dataSetMetabase);
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    List<AttachmentValue> attachments = new ArrayList<>();
    AttachmentValue attachment = new AttachmentValue();
    attachment.setContent(" ".getBytes());
    attachments.add(attachment);
    when(attachmentRepository.findAllByIdFieldSchemaAndValueIsNotNull(Mockito.any()))
        .thenReturn(attachments);
    doThrow(new EEAException("error")).when(kafkaSenderUtils)
        .releaseNotificableKafkaEvent(Mockito.any(), Mockito.any(), Mockito.any());
    fileTreatmentHelper.exportDatasetFile(1L, "zip xslx");
    Mockito.verify(kafkaSenderUtils, times(2)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Export dataset filecsv test.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidFileException the invalid file exception
   * @throws EEAException the EEA exception
   */
  @Test
  public void exportDatasetFilecsvTest() throws IOException, InvalidFileException, EEAException {
    DataSetMetabaseVO dataSetMetabase = new DataSetMetabaseVO();
    dataSetMetabase.setDataflowId(1L);
    dataSetMetabase.setDataProviderId(1L);
    dataSetMetabase.setDatasetSchema("603362319d49f04fce13b68f");
    dataSetMetabase.setDataSetName("file");
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
    tableSchema.setNameTableSchema("nameTableSchema");
    tableSchema.setIdTableSchema(new ObjectId());
    tableSchema.setRecordSchema(recordSchema);
    tableSchemas.add(tableSchema);
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
    List<byte[]> bytes = new ArrayList<>();
    bytes.add("".getBytes());
    when(fileExportFactory.createContext(Mockito.any())).thenReturn(contextExport);
    when(contextExport.fileListWriter(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean())).thenReturn(bytes);
    when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("603362319d49f04fce13b68f");
    when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetSchema));
    when(datasetMetabaseService.findDatasetMetabase(Mockito.any())).thenReturn(dataSetMetabase);
    fileTreatmentHelper.exportDatasetFile(1L, FileTypeEnum.CSV.getValue());
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
    tableSchema.setNameTableSchema("nameTableSchema");
    tableSchema.setIdTableSchema(new ObjectId());
    tableSchema.setRecordSchema(recordSchema);
    tableSchemas.add(tableSchema);
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
   * Et import dataset test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void etImportDatasetReadOnlyTest() throws EEAException {
    ETLDatasetVO etlDatasetVO = new ETLDatasetVO();
    List<ETLTableVO> etlTableVOs = new ArrayList<>();
    ETLTableVO etlTableVO = new ETLTableVO();
    ETLTableVO etlTableVO2 = new ETLTableVO();
    List<ETLRecordVO> etlRecordVOs = new ArrayList<>();
    ETLRecordVO etlRecordVO = new ETLRecordVO();
    List<ETLFieldVO> etlFieldVOs = new ArrayList<>();
    ETLFieldVO etlFieldVO = new ETLFieldVO();
    etlDatasetVO.setTables(etlTableVOs);
    etlTableVO.setTableName("nameTableSchema");
    etlTableVO2.setTableName("nameTableSchema2");
    etlTableVO.setRecords(etlRecordVOs);
    etlTableVO2.setRecords(etlRecordVOs);
    etlTableVOs.add(etlTableVO);
    etlTableVOs.add(etlTableVO2);
    etlRecordVOs.add(etlRecordVO);
    etlRecordVO.setFields(etlFieldVOs);
    etlFieldVOs.add(etlFieldVO);
    etlFieldVO.setFieldName("headerName");
    etlFieldVO.setValue("value");
    DataSetSchema datasetSchema = new DataSetSchema();
    List<TableSchema> tableSchemas = new ArrayList<>();
    TableSchema tableSchema = new TableSchema();
    TableSchema tableSchema2 = new TableSchema();
    tableSchema.setReadOnly(Boolean.TRUE);
    tableSchema2.setReadOnly(false);
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(new ObjectId());
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    FieldSchema fieldSchema = new FieldSchema();
    FieldSchema fieldSchema2 = new FieldSchema();
    List<RecordValue> recordValues = new ArrayList<>();
    RecordValue recordValue = new RecordValue();
    List<FieldValue> fieldValues = new ArrayList<>();
    FieldValue fieldValue = new FieldValue();
    tableSchema.setIdTableSchema(new ObjectId());
    tableSchema.setNameTableSchema("nameTableSchema");
    tableSchema.setRecordSchema(recordSchema);
    tableSchema2.setIdTableSchema(new ObjectId());
    tableSchema2.setNameTableSchema("nameTableSchema2");
    tableSchema2.setRecordSchema(recordSchema);
    tableSchema2.setFixedNumber(true);
    tableSchemas.add(tableSchema);
    tableSchemas.add(tableSchema2);
    datasetSchema.setTableSchemas(tableSchemas);
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

  @Test
  public void exportFileTest() throws FileNotFoundException, EEAException, IOException {
    byte[] file = "Test".getBytes();

    Mockito.when(fileExportFactory.createContext(Mockito.anyString())).thenReturn(contextExport);
    Mockito.when(contextExport.fileWriter(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(),
        Mockito.any(), Mockito.anyBoolean(), Mockito.any())).thenReturn(file);

    fileTreatmentHelper.exportFile(1L, "csv", "tableschema", "tableName", null);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void exportFileFailedTest() throws FileNotFoundException, EEAException, IOException {
    Mockito.when(fileExportFactory.createContext(Mockito.anyString())).thenReturn(contextExport);
    Mockito.when(contextExport.fileWriter(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
        Mockito.any(), Mockito.anyBoolean(), Mockito.any())).thenThrow(EEAException.class);

    fileTreatmentHelper.exportFile(1L, "csv", "tablesSchema", "tableName", null);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void createFileTest() throws EEAException, IOException {
    byte[] expectedResult = "".getBytes();
    ReportingDataset dataset = new ReportingDataset();
    dataset.setDataflowId(1L);
    when(fileExportFactory.createContext(Mockito.any())).thenReturn(contextExport);
    when(contextExport.fileWriter(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean(), Mockito.any())).thenReturn(expectedResult);
    assertEquals("not equals", expectedResult,
        fileTreatmentHelper.createFile(1L, FileTypeEnum.CSV.getValue(), "", null));
  }

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
    when(contextExport.fileWriter(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean(), Mockito.any())).thenReturn(expectedResult);
    Mockito.when(dataSetMetabaseRepository.findByDataflowIdAndDataProviderId(Mockito.anyLong(),
        Mockito.anyLong())).thenReturn(datasetMetabaseList);
    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setId(1L);
    dataflow.setType(TypeDataflowEnum.REPORTING);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any())).thenReturn(dataflow);
    fileTreatmentHelper.savePublicFiles(1L, 1L);
    Mockito.verify(fileExportFactory, times(1)).createContext(Mockito.any());
  }

  @Test
  public void createReferenceDatasetFiles() throws EEAException, IOException {
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

    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);

    when(designDatasetRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<DesignDataset>());
    List<FieldValue> fieldValues = new ArrayList<>();
    FieldValue field = new FieldValue();
    field.setType(DataType.ATTACHMENT);
    field.setId("0A07FD45F1CD7965A2B0F13E57948A13");
    field.setRecord(record);
    fieldValues.add(field);

    AttachmentValue attachment = new AttachmentValue();
    attachment.setFieldValue(field);
    when(fileExportFactory.createContext(Mockito.any())).thenReturn(contextExport);
    byte[] expectedResult = "".getBytes();
    when(contextExport.fileWriter(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean(), Mockito.any())).thenReturn(expectedResult);
    attachment.setContent(expectedResult);
    when(attachmentRepository.findAllByIdFieldSchemaAndValueIsNotNull(Mockito.anyString()))
        .thenReturn(Arrays.asList(attachment));


    fileTreatmentHelper.createReferenceDatasetFiles(dataset);
    Mockito.verify(fileExportFactory, times(1)).createContext(Mockito.any());

  }

  /**
   * After tests.
   */
  @After
  public void afterTests() {
    File fileDataset = new File("./dataset-1");
    File fileDataflow = new File("./dataflow-1");
    try {
      FileUtils.deleteDirectory(fileDataset);
      FileUtils.deleteDirectory(fileDataflow);
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
