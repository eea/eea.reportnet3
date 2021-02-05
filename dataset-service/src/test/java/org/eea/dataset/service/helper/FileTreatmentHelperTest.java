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
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.domain.rule.RulesSchema;
import org.eea.dataset.persistence.schemas.repository.RulesRepository;
import org.eea.dataset.persistence.schemas.repository.UniqueConstraintRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
import org.eea.interfaces.vo.dataflow.integration.IntegrationParams;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class FileTreatmentHelperTest {

  @InjectMocks
  private FileTreatmentHelper fileTreatmentHelper;

  @Mock
  private DatasetService datasetService;

  @Mock
  private DataSetMapper dataSetMapper;

  @Mock
  private IntegrationControllerZuul integrationController;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private RulesRepository rulesRepository;

  @Mock
  private UniqueConstraintRepository uniqueConstraintRepository;

  @Mock
  private RulesControllerZuul rulesControllerZuul;

  @Mock
  private Authentication authentication;

  private SecurityContext securityContext;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    ReflectionTestUtils.setField(fileTreatmentHelper, "importPath",
        this.getClass().getClassLoader().getResource("").getPath());
    ReflectionTestUtils.setField(fileTreatmentHelper, "importExecutorService",
        new CurrentThreadExecutor());
    ReflectionTestUtils.setField(fileTreatmentHelper, "batchSize", 1);
  }

  @Test
  public void importFileDataCsvTest() throws EEAException, IOException {

    FieldValue fieldValue = new FieldValue();
    fieldValue.setValue("value");
    List<FieldValue> fieldValues = new ArrayList<>();
    fieldValues.add(fieldValue);

    RecordValue recordValue = new RecordValue();
    recordValue.setFields(fieldValues);
    List<RecordValue> recordValues = new ArrayList<>();
    recordValues.add(recordValue);
    recordValues.add(recordValue);

    TableValue tableValue = new TableValue();
    tableValue.setIdTableSchema("5cf0e9b3b793310e9ceca190");
    tableValue.setRecords(recordValues);
    List<TableValue> tableValues = new ArrayList<>();
    tableValues.add(tableValue);

    DatasetValue datasetValue = new DatasetValue();
    datasetValue.setTableValues(tableValues);

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    datasetSchema.setIdDataFlow(1L);

    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setOperation(IntegrationOperationTypeEnum.EXPORT_EU_DATASET);
    List<IntegrationVO> integrationVOs = new ArrayList<>();
    integrationVOs.add(integrationVO);

    MultipartFile multipartFile =
        new MockMultipartFile("file", "file.csv", "text/csv", "".getBytes());

    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.any()))
        .thenReturn(datasetSchema);

    Mockito.when(datasetService.getMimetype(Mockito.anyString())).thenReturn("csv");
    // Mockito.when(integrationController.findAllIntegrationsByCriteria(Mockito.any()))
    // .thenReturn(integrationVOs);
    Mockito.doNothing().when(datasetService).deleteTableBySchema(Mockito.anyString(),
        Mockito.anyLong());
    Mockito.when(
        datasetService.processFile(Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new DataSetVO());
    Mockito.when(dataSetMapper.classToEntity(Mockito.any())).thenReturn(datasetValue);
    Mockito.when(datasetService.findTableIdByTableSchema(Mockito.anyLong(), Mockito.any()))
        .thenReturn(null);
    Mockito.doNothing().when(datasetService).saveAllRecords(Mockito.anyLong(), Mockito.any());
    Mockito.when(datasetService.getDatasetType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.REPORTING);
    Mockito.doNothing().when(kafkaSenderUtils).releaseKafkaEvent(Mockito.any(), Mockito.any());
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    fileTreatmentHelper.importFileData(1L, "5cf0e9b3b793310e9ceca190", multipartFile, true);
    FileUtils
        .deleteDirectory(new File(this.getClass().getClassLoader().getResource("").getPath(), "1"));

    Mockito.verify(kafkaSenderUtils, times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
    Mockito.verify(datasetService, times(1)).releaseLock(Mockito.any(), Mockito.any());
  }

  @Test
  public void importFileDataZipTest() throws EEAException, IOException {

    FieldValue fieldValue = new FieldValue();
    fieldValue.setValue("value");
    List<FieldValue> fieldValues = new ArrayList<>();
    fieldValues.add(fieldValue);

    RecordValue recordValue = new RecordValue();
    recordValue.setFields(fieldValues);
    List<RecordValue> recordValues = new ArrayList<>();
    recordValues.add(recordValue);
    recordValues.add(recordValue);

    TableValue tableValue = new TableValue();
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

    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.any()))
        .thenReturn(datasetSchema);

    Mockito.when(datasetService.getMimetype(Mockito.anyString())).thenReturn("zip")
        .thenReturn("csv").thenReturn("txt").thenReturn("csv");
    // Mockito.when(integrationController.findAllIntegrationsByCriteria(Mockito.any()))
    // .thenReturn(integrationVOs);
    Mockito.doNothing().when(datasetService).deleteImportData(Mockito.anyLong());
    Mockito.when(
        datasetService.processFile(Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new DataSetVO());
    Mockito.when(dataSetMapper.classToEntity(Mockito.any())).thenReturn(datasetValue);
    Mockito.when(datasetService.findTableIdByTableSchema(Mockito.anyLong(), Mockito.any()))
        .thenReturn(1L);
    Mockito.doNothing().when(datasetService).saveAllRecords(Mockito.anyLong(), Mockito.any());
    Mockito.when(datasetService.getDatasetType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.REPORTING);
    Mockito.doNothing().when(kafkaSenderUtils).releaseKafkaEvent(Mockito.any(), Mockito.any());
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    fileTreatmentHelper.importFileData(1L, null, multipartFile, true);
    FileUtils
        .deleteDirectory(new File(this.getClass().getClassLoader().getResource("").getPath(), "1"));

    Mockito.verify(kafkaSenderUtils, times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
    Mockito.verify(datasetService, times(1)).releaseLock(Mockito.any(), Mockito.any());
  }

  @Test
  public void importFileDataXlsFMETest() throws EEAException, IOException {

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    datasetSchema.setIdDataFlow(1L);

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

    Mockito.when(datasetService.getMimetype(Mockito.anyString())).thenReturn("xls");
    Mockito.when(integrationController.findAllIntegrationsByCriteria(Mockito.any()))
        .thenReturn(integrationVOs);
    Mockito.when(integrationController.executeIntegrationProcess(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.anyLong(), Mockito.any())).thenReturn(executionResultVO);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    fileTreatmentHelper.importFileData(1L, "5cf0e9b3b793310e9ceca190", multipartFile, false);
    FileUtils
        .deleteDirectory(new File(this.getClass().getClassLoader().getResource("").getPath(), "1"));

    Mockito.verify(integrationController, times(1)).executeIntegrationProcess(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test(expected = EEAException.class)
  public void importFileDataExceptionTest() throws EEAException {
    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.getName()).thenReturn("fileName.csv");
    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.any()))
        .thenReturn(null);
    try {
      fileTreatmentHelper.importFileData(1L, "5cf0e9b3b793310e9ceca190", file, true);
    } catch (EEAException e) {
      Mockito.verify(datasetService, times(1)).releaseLock(Mockito.anyString(), Mockito.anyLong());
      throw e;
    }
  }

  @Test(expected = EEAException.class)
  public void importFileDataFolderExceptionTest() throws EEAException {
    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.anyString()))
        .thenReturn(new DataSetSchema());
    MultipartFile multipartFile =
        new MockMultipartFile("file", "file.xls", "application/vnd.ms-excel", "".getBytes());
    File folder = new File(this.getClass().getClassLoader().getResource("").getPath(), "1");
    folder.mkdirs();
    try {
      fileTreatmentHelper.importFileData(1L, "5cf0e9b3b793310e9ceca190", multipartFile, true);
    } catch (EEAException e) {
      Assert.assertEquals(e.getMessage(), "Folder for dataset 1 already exists");
      throw e;
    }
  }

  @Test(expected = EEAException.class)
  public void importFileDataIOExceptionTest() throws IOException, EEAException {
    Mockito.when(datasetService.getSchemaIfReportable(Mockito.anyLong(), Mockito.anyString()))
        .thenReturn(new DataSetSchema());
    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.getInputStream()).thenThrow(IOException.class);
    Mockito.when(file.getName()).thenReturn("fileName.csv");
    try {
      fileTreatmentHelper.importFileData(1L, "5cf0e9b3b793310e9ceca190", file, true);
    } catch (EEAException e) {
      Mockito.verify(datasetService, times(1)).releaseLock(Mockito.anyString(), Mockito.anyLong());
      throw e;
    }
  }


  @Test
  public void zipSchemaTest() {
    List<DataSetSchema> schemas = new ArrayList<>();
    DataSetSchema schema = new DataSetSchema();
    schema.setIdDataFlow(1L);
    schema.setIdDataSetSchema(new ObjectId());
    schemas.add(schema);
    List<DesignDataset> designs = new ArrayList<>();
    DesignDataset design = new DesignDataset();
    design.setDataSetName("test");
    design.setId(1L);
    design.setDatasetSchema(new ObjectId().toString());
    designs.add(design);

    Map<String, String> internalParameters = new HashMap<>();
    internalParameters.put(IntegrationParams.FILE_EXTENSION, "xls");
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setInternalParameters(internalParameters);
    integrationVO.setOperation(IntegrationOperationTypeEnum.IMPORT);
    List<IntegrationVO> integrationVOs = new ArrayList<>();
    integrationVOs.add(integrationVO);

    Mockito.when(rulesRepository.findByIdDatasetSchema(Mockito.any()))
        .thenReturn(new RulesSchema());
    Mockito.when(uniqueConstraintRepository.findByDatasetSchemaId(Mockito.any()))
        .thenReturn(new ArrayList<>());
    Mockito.when(rulesControllerZuul.getIntegrityRulesByDatasetSchemaId(Mockito.any()))
        .thenReturn(new ArrayList<>());
    Mockito.when(integrationController.findAllIntegrationsByCriteria(Mockito.any()))
        .thenReturn(integrationVOs);


    fileTreatmentHelper.zipSchema(designs, schemas, 1L);
    Mockito.verify(integrationController, times(1)).findAllIntegrationsByCriteria(Mockito.any());
  }


  @Test
  public void unzipSchemaTest() {

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ZipOutputStream zip = new ZipOutputStream(baos);
      ZipEntry entry1 = new ZipEntry("Table.schema");
      ZipEntry entry2 = new ZipEntry("Table.qcrules");
      zip.putNextEntry(entry1);
      zip.putNextEntry(entry2);
      zip.close();
      MultipartFile multipartFile = new MockMultipartFile("file", "file.zip",
          "application/x-zip-compressed", baos.toByteArray());

      Mockito.when(datasetService.getMimetype(Mockito.anyString())).thenReturn("zip");
      fileTreatmentHelper.unZipImportSchema(multipartFile);
    } catch (EEAException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
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
