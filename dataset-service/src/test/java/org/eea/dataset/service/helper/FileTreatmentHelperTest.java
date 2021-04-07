package org.eea.dataset.service.helper;

import static org.mockito.Mockito.times;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.data.sequence.FieldValueIdGenerator;
import org.eea.dataset.persistence.data.sequence.RecordValueIdGenerator;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.RulesRepository;
import org.eea.dataset.persistence.schemas.repository.UniqueConstraintRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.eea.interfaces.vo.dataflow.integration.IntegrationParams;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
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
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.anyLong())).thenReturn(1L);

    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.any()))
        .thenReturn(datasetSchema);

    Mockito.when(datasetService.getMimetype(Mockito.anyString())).thenReturn("csv");
    Mockito.when(
        datasetService.processFile(Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new DataSetVO());
    Mockito.when(dataSetMapper.classToEntity(Mockito.any())).thenReturn(datasetValue);
    Mockito.when(datasetService.findTableIdByTableSchema(Mockito.anyLong(), Mockito.any()))
        .thenReturn(null);
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
   * Import file data zip test.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
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
    Mockito.when(
        datasetService.processFile(Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new DataSetVO());
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
  @Test(expected = EEAException.class)
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
      Assert.assertEquals(e.getMessage(), "Folder for dataset 1 already exists");
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
  @Test
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
    Mockito.when(
        datasetService.processFile(Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new DataSetVO());
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
    Mockito.when(recordRepository.findByTableValueNoOrderOptimized(Mockito.any(), Mockito.any()))
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
  @Test
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
    Mockito.when(
        datasetService.processFile(Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new DataSetVO());
    Mockito.when(dataSetMapper.classToEntity(Mockito.any())).thenReturn(datasetValue);
    Mockito.when(datasetService.findTableIdByTableSchema(Mockito.anyLong(), Mockito.any()))
        .thenReturn(null);
    Mockito.when(datasetService.getDatasetType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.REPORTING);
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    Mockito.when(recordRepository.findByTableValueNoOrderOptimized(Mockito.any(), Mockito.any()))
        .thenReturn(recordValues);
    Mockito.when(tableRepository.countRecordsByIdTableSchema(Mockito.any())).thenReturn(2L);
    fileTreatmentHelper.importFileData(1L, null, multipartFile, true);
    FileUtils
        .deleteDirectory(new File(this.getClass().getClassLoader().getResource("").getPath(), "1"));

    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }
}


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
