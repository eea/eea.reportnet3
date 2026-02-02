package org.eea.dataset.service.impl;

import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.repository.PkCatalogueRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.*;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.model.ImportFileInDremioInfo;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.communication.NotificationController;
import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.interfaces.controller.dataflow.RepresentativeController;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController;
import org.eea.interfaces.controller.recordstore.ProcessController;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.redis.RedisLockService;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class BigDataDatasetServiceImplTest {

    @InjectMocks
    private BigDataDatasetServiceImpl bigDataDatasetService;

    // Required dependencies
    @Mock private S3Helper s3HelperPublic;
    @Mock private S3Helper s3HelperPrivate;
    @Mock private DremioHelperService dremioHelperService;
    @Mock private ParquetConverterService parquetConverterService;
    @Mock private JdbcTemplate dremioJdbcTemplate;
    @Mock private SchemasRepository schemasRepository;
    @Mock private DatasetSnapshotService datasetSnapshotService;
    @Mock private DatasetService datasetService;
    @Mock private JobControllerZuul jobControllerZuul;
    @Mock private JobProcessController.JobProcessControllerZuul jobProcessControllerZuul;
    @Mock private DatasetMetabaseService datasetMetabaseService;
    @Mock private ProcessController.ProcessControllerZuul processControllerZuul;
    @Mock private KafkaSenderUtils kafkaSenderUtils;
    @Mock private RepresentativeController.RepresentativeControllerZuul representativeControllerZuul;
    @Mock private FileCommonUtils fileCommonUtils;
    @Mock private DatasetSchemaService datasetSchemaService;
    @Mock private SpatialDataHandling spatialDataHandling;
    @Mock private DatasetTableService datasetTableService;
    @Mock private DataFlowController.DataFlowControllerZuul dataFlowControllerZuul;
    @Mock private CreateEmptyTables createEmptyTables;
    @Mock private PkCatalogueRepository pkCatalogueRepository;
    @Mock private TableDataRetriever tableDataRetriever;
    @Mock private EtlExportV5Service etlExportV5Service;
    @Mock private NotificationController.NotificationControllerZuul notificationControllerZuul;
    @Mock private RedisLockService redisLockService;
    @Mock private DremioAutoPromotionService dremioAutoPromotionService;

    @Mock
    private File mockFile;


    @Test
    public void etlImportDatasetReportingDatasetReadOnlyTableTest() throws Exception {
        Long dataflowId = 1L;
        Long datasetId = 1L;
        Long providerId = 1L;
        Boolean replaceData = false;
        String tableSchemaId = "abc";
        String delimiter = ",";
        String filePathInS3 = "path";
        Long jobId = 1L;
        String datasetSchemaId = "abc";
        DatasetTypeEnum datasetType = DatasetTypeEnum.REPORTING;
        DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
        dataSetMetabaseVO.setDatasetSchema(datasetSchemaId);
        TableSchemaVO tableSchemaVO = new TableSchemaVO();
        tableSchemaVO.setReadOnly(true);
        JobStatusEnum jobStatus = JobStatusEnum.FAILED;
        JobInfoEnum jobInfo = JobInfoEnum.ERROR_IMPORT_FAILED_READ_ONLY_TABLE;
        JobVO jobVO = new JobVO();
        jobVO.setId(jobId);
        jobVO.setJobStatus(jobStatus);
        DataFlowVO dataFlowVO = new DataFlowVO();

        Mockito.when(datasetService.getDatasetType(datasetId)).thenReturn(datasetType);
        Mockito.when(datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId)).thenReturn(tableSchemaVO);
        Mockito.when(jobControllerZuul.findJobById(jobId)).thenReturn(jobVO);
        bigDataDatasetService.etlImportDataset(datasetId, dataflowId, providerId, replaceData, tableSchemaId, delimiter, filePathInS3, jobId, dataFlowVO, dataSetMetabaseVO);
        Mockito.verify(jobControllerZuul, Mockito.times(1)).updateJobStatusAndInfo(jobId, jobStatus, jobInfo, null);
    }

    @Test
    public void etlImportDatasetReportingDatasetReadOnlyTableMultipleTablesToImportsTest() throws Exception {
        Long dataflowId = 1L;
        Long datasetId = 1L;
        Long providerId = 1L;
        Boolean replaceData = false;
        String tableSchemaId1 = "abc1";
        String tableName1 = "name1";
        String tableSchemaId2 = "abc2";
        String tableName2 = "name2";
        String delimiter = ",";
        String filePathInS3 = "path";
        Long jobId = 1L;
        String datasetSchemaId = "abc";
        DatasetTypeEnum datasetType = DatasetTypeEnum.REPORTING;
        DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
        dataSetMetabaseVO.setDatasetSchema(datasetSchemaId);
        TableSchemaIdNameVO tableSchemaIdNameVO1 = new TableSchemaIdNameVO();
        tableSchemaIdNameVO1.setIdTableSchema(tableSchemaId1);
        tableSchemaIdNameVO1.setNameTableSchema(tableName1);
        TableSchemaIdNameVO tableSchemaIdNameVO2 = new TableSchemaIdNameVO();
        tableSchemaIdNameVO2.setIdTableSchema(tableSchemaId2);
        tableSchemaIdNameVO2.setNameTableSchema(tableName2);
        List<TableSchemaIdNameVO> tableSchemaIdNameVOS = new ArrayList<>();
        tableSchemaIdNameVOS.add(tableSchemaIdNameVO1);
        tableSchemaIdNameVOS.add(tableSchemaIdNameVO2);

        FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
        fieldSchemaVO.setReadOnly(false);
        RecordSchemaVO recordSchemaVO = new RecordSchemaVO();
        recordSchemaVO.setFieldSchema(new ArrayList<>(Collections.singleton(fieldSchemaVO)));
        TableSchemaVO tableSchemaVO1 = new TableSchemaVO();
        tableSchemaVO1.setReadOnly(false);
        tableSchemaVO1.setFixedNumber(false);
        tableSchemaVO1.setRecordSchema(recordSchemaVO);


        TableSchemaVO tableSchemaVO2 = new TableSchemaVO();
        tableSchemaVO2.setReadOnly(true);

        JobStatusEnum jobStatus = JobStatusEnum.FAILED;
        JobInfoEnum jobInfo = JobInfoEnum.ERROR_IMPORT_FAILED_READ_ONLY_TABLE;
        JobVO jobVO = new JobVO();
        jobVO.setId(jobId);
        jobVO.setJobStatus(jobStatus);
        DataFlowVO dataFlowVO = new DataFlowVO();

        Mockito.when(datasetService.getDatasetType(datasetId)).thenReturn(datasetType);
        Mockito.when(datasetSchemaService.getTableSchemasIds(datasetId)).thenReturn(tableSchemaIdNameVOS);
        Mockito.when(datasetSchemaService.getTableSchemaVO(tableSchemaId1, datasetSchemaId)).thenReturn(tableSchemaVO1);
        Mockito.when(datasetSchemaService.getTableSchemaVO(tableSchemaId2, datasetSchemaId)).thenReturn(tableSchemaVO2);
        Mockito.when(jobControllerZuul.findJobById(jobId)).thenReturn(jobVO);
        bigDataDatasetService.etlImportDataset(datasetId, dataflowId, providerId, replaceData, null, delimiter, filePathInS3, jobId, dataFlowVO, dataSetMetabaseVO);
        Mockito.verify(jobControllerZuul, Mockito.times(1)).updateJobStatusAndInfo(jobId, jobStatus, jobInfo, null);
    }

    @Test
    public void etlImportDatasetReportingDatasetFixedNumberOfRecordsTest() throws Exception {
        Long dataflowId = 1L;
        Long datasetId = 1L;
        Long providerId = 1L;
        Boolean replaceData = false;
        String tableSchemaId = "abc";
        String delimiter = ",";
        String filePathInS3 = "path";
        Long jobId = 1L;
        String datasetSchemaId = "abc";
        DatasetTypeEnum datasetType = DatasetTypeEnum.REPORTING;
        DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
        dataSetMetabaseVO.setDatasetSchema(datasetSchemaId);
        TableSchemaVO tableSchemaVO = new TableSchemaVO();
        tableSchemaVO.setReadOnly(false);
        tableSchemaVO.setFixedNumber(true);
        JobStatusEnum jobStatus = JobStatusEnum.FAILED;
        JobInfoEnum jobInfo = JobInfoEnum.ERROR_IMPORT_FAILED_FIXED_NUM;
        JobVO jobVO = new JobVO();
        jobVO.setId(jobId);
        jobVO.setJobStatus(jobStatus);
        DataFlowVO dataFlowVO = new DataFlowVO();

        Mockito.when(datasetService.getDatasetType(datasetId)).thenReturn(datasetType);
        Mockito.when(datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId)).thenReturn(tableSchemaVO);
        Mockito.when(jobControllerZuul.findJobById(jobId)).thenReturn(jobVO);
        bigDataDatasetService.etlImportDataset(datasetId, dataflowId, providerId, replaceData, tableSchemaId, delimiter, filePathInS3, jobId, dataFlowVO, dataSetMetabaseVO);
        Mockito.verify(jobControllerZuul, Mockito.times(1)).updateJobStatusAndInfo(jobId, jobStatus, jobInfo, null);
    }

    @Test
    public void etlImportDatasetReportingDatasetReadOnlyFieldsTest() throws Exception {
        Long dataflowId = 1L;
        Long datasetId = 1L;
        Long providerId = 1L;
        Boolean replaceData = false;
        String tableSchemaId = "abc";
        String delimiter = ",";
        String filePathInS3 = "path";
        Long jobId = 1L;
        String datasetSchemaId = "abc";
        DatasetTypeEnum datasetType = DatasetTypeEnum.REPORTING;
        DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
        dataSetMetabaseVO.setDatasetSchema(datasetSchemaId);
        FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
        fieldSchemaVO.setReadOnly(true);
        RecordSchemaVO recordSchemaVO = new RecordSchemaVO();
        recordSchemaVO.setFieldSchema(new ArrayList<>(Collections.singleton(fieldSchemaVO)));
        TableSchemaVO tableSchemaVO = new TableSchemaVO();
        tableSchemaVO.setReadOnly(false);
        tableSchemaVO.setFixedNumber(false);
        tableSchemaVO.setRecordSchema(recordSchemaVO);
        JobStatusEnum jobStatus = JobStatusEnum.FAILED;
        JobInfoEnum jobInfo = JobInfoEnum.ERROR_IMPORT_FAILED_READ_ONLY_FIELDS;
        JobVO jobVO = new JobVO();
        jobVO.setId(jobId);
        jobVO.setJobStatus(jobStatus);
        DataFlowVO dataFlowVO = new DataFlowVO();

        Mockito.when(datasetService.getDatasetType(datasetId)).thenReturn(datasetType);
        Mockito.when(jobControllerZuul.findJobById(jobId)).thenReturn(jobVO);
        Mockito.when(datasetSchemaService.getTableSchemaVO(tableSchemaId, dataSetMetabaseVO.getDatasetSchema())).thenReturn(tableSchemaVO);
        bigDataDatasetService.etlImportDataset(datasetId, dataflowId, providerId, replaceData, tableSchemaId, delimiter, filePathInS3, jobId, dataFlowVO, dataSetMetabaseVO);
        Mockito.verify(jobControllerZuul, Mockito.times(1)).updateJobStatusAndInfo(jobId, jobStatus, jobInfo, null);
    }

    @Test
    public void etlImportDatasetDesignDatasetExtensionNoZipTest() throws Exception {
        Long dataflowId = 1L;
        Long datasetId = 1L;
        Long providerId = 1L;
        Boolean replaceData = false;
        String tableSchemaId = "abc";
        String delimiter = ",";
        String filePathInS3 = "path.csv";
        Long jobId = 1L;
        DatasetTypeEnum datasetType = DatasetTypeEnum.DESIGN;
        JobStatusEnum jobStatus = JobStatusEnum.FAILED;
        JobInfoEnum jobInfo = JobInfoEnum.ERROR_IMPORT_FAILED_FILE_NOT_ZIP;
        JobVO jobVO = new JobVO();
        jobVO.setId(jobId);
        jobVO.setJobStatus(jobStatus);
        DataFlowVO dataFlowVO = new DataFlowVO();
        DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();

        Mockito.when(datasetService.getDatasetType(datasetId)).thenReturn(datasetType);
        Mockito.when(jobControllerZuul.findJobById(jobId)).thenReturn(jobVO);
        bigDataDatasetService.etlImportDataset(datasetId, dataflowId, providerId, replaceData, tableSchemaId, delimiter, filePathInS3, jobId, dataFlowVO, dataSetMetabaseVO);
        Mockito.verify(jobControllerZuul, Mockito.times(1)).updateJobStatusAndInfo(jobId, jobStatus, jobInfo, null);
    }

    @Test
    public void etlImportDatasetDesignDatasetCanceledJobTest() throws Exception {
        Long dataflowId = 1L;
        Long datasetId = 1L;
        Long providerId = 1L;
        Boolean replaceData = false;
        String tableSchemaId = "abc";
        String tableName = "name";
        String delimiter = ",";
        String filePathInS3 = "path.zip";
        Long jobId = 1L;
        DatasetTypeEnum datasetType = DatasetTypeEnum.DESIGN;
        JobStatusEnum jobStatus = JobStatusEnum.CANCELED;
        JobInfoEnum jobInfoEnum = JobInfoEnum.ERROR_IMPORT_FILES_CONTAIN_WRONG_HEADERS;
        String fileExtension = ".zip";
        List<File> fileList = new ArrayList<>();
        JobVO jobVO = new JobVO();
        jobVO.setId(jobId);
        jobVO.setJobStatus(jobStatus);
        DataFlowVO dataFlowVO = new DataFlowVO();
        DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
        DataProviderVO dataProviderVO  = new DataProviderVO();
        Map<String, Boolean> attachmentsExistPerTableName = new HashMap<>();
        TableSchemaIdNameVO tableSchemaIdNameVO = new TableSchemaIdNameVO();
        tableSchemaIdNameVO.setIdTableSchema(tableSchemaId);
        tableSchemaIdNameVO.setNameTableSchema(tableName);
        List<TableSchemaIdNameVO> tableSchemaIdNameVOS = new ArrayList<>();
        tableSchemaIdNameVOS.add(tableSchemaIdNameVO);
        Set<String> tableNamesSet = tableSchemaIdNameVOS.stream().map(vo -> vo.getNameTableSchema().toLowerCase()).collect(Collectors.toSet());
        String errorMessage = EEAErrorMessage.ERROR_IMPORT_FILES_CONTAIN_WRONG_HEADERS;
        DataSetSchema datasetSchema = new DataSetSchema();

        Mockito.when(datasetService.getDatasetType(datasetId)).thenReturn(datasetType);
        Mockito.when(jobControllerZuul.findJobById(jobId)).thenReturn(jobVO);

        // Use spy in order to mock protected methods
        BigDataDatasetServiceImpl bigDataDatasetServiceSpy = Mockito.spy(bigDataDatasetService);
        Mockito.when(datasetSchemaService.getTableSchemasIds(datasetId)).thenReturn(tableSchemaIdNameVOS);
        Mockito.when(datasetService.getSchemaIfReportable(datasetId, tableSchemaId)).thenReturn(datasetSchema);
        Mockito.doReturn(mockFile).when(bigDataDatasetServiceSpy).createEtlImportFolder(datasetId, jobId);
        Mockito.doReturn(fileList).when(bigDataDatasetServiceSpy).storeAndUnzipEtlImportZipFile(datasetId, filePathInS3, fileExtension, jobId, mockFile, tableNamesSet, attachmentsExistPerTableName);
        Mockito.when(representativeControllerZuul.findDataProviderById(providerId)).thenReturn(dataProviderVO);

        Mockito.doAnswer(invocation -> {
                    ImportFileInDremioInfo info = invocation.getArgument(0);
                    info.setErrorMessage(errorMessage);
                    throw new Exception(errorMessage);
                }).when(parquetConverterService).convertCsvFilesToParquetFiles(any(), eq(fileList), eq(datasetSchema));

        bigDataDatasetServiceSpy.etlImportDataset(datasetId, dataflowId, providerId, replaceData, tableSchemaId, delimiter, filePathInS3, jobId, dataFlowVO, dataSetMetabaseVO);

        ArgumentCaptor<ImportFileInDremioInfo> captor = ArgumentCaptor.forClass(ImportFileInDremioInfo.class);
        Mockito.verify(parquetConverterService).convertCsvFilesToParquetFiles(captor.capture(), eq(fileList), eq(datasetSchema));
        assertEquals(errorMessage, captor.getValue().getErrorMessage());
        Mockito.verify(jobControllerZuul).updateJobStatusAndInfo(jobId, jobStatus, jobInfoEnum, null);
    }

    @Test
    public void etlImportDatasetDesignDatasetFailedJobTest() throws Exception {
        Long dataflowId = 1L;
        Long datasetId = 1L;
        Long providerId = 1L;
        Boolean replaceData = false;
        String tableSchemaId = "abc";
        String tableName = "name";
        String delimiter = ",";
        String filePathInS3 = "path.zip";
        Long jobId = 1L;
        DatasetTypeEnum datasetType = DatasetTypeEnum.DESIGN;
        JobStatusEnum jobStatus = JobStatusEnum.FAILED;
        String fileExtension = ".zip";
        List<File> fileList = new ArrayList<>();
        JobVO jobVO = new JobVO();
        jobVO.setId(jobId);
        jobVO.setJobStatus(jobStatus);
        DataFlowVO dataFlowVO = new DataFlowVO();
        DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
        DataProviderVO dataProviderVO  = new DataProviderVO();
        Map<String, Boolean> attachmentsExistPerTableName = new HashMap<>();
        TableSchemaIdNameVO tableSchemaIdNameVO = new TableSchemaIdNameVO();
        tableSchemaIdNameVO.setIdTableSchema(tableSchemaId);
        tableSchemaIdNameVO.setNameTableSchema(tableName);
        List<TableSchemaIdNameVO> tableSchemaIdNameVOS = new ArrayList<>();
        tableSchemaIdNameVOS.add(tableSchemaIdNameVO);
        Set<String> tableNamesSet = tableSchemaIdNameVOS.stream().map(vo -> vo.getNameTableSchema().toLowerCase()).collect(Collectors.toSet());
        String errorMessage = "non existing message";
        DataSetSchema datasetSchema = new DataSetSchema();

        Mockito.when(datasetService.getDatasetType(datasetId)).thenReturn(datasetType);
        Mockito.when(jobControllerZuul.findJobById(jobId)).thenReturn(jobVO);

        // Use spy in order to mock protected methods
        BigDataDatasetServiceImpl bigDataDatasetServiceSpy = Mockito.spy(bigDataDatasetService);
        Mockito.when(datasetSchemaService.getTableSchemasIds(datasetId)).thenReturn(tableSchemaIdNameVOS);
        Mockito.when(datasetService.getSchemaIfReportable(datasetId, tableSchemaId)).thenReturn(datasetSchema);
        Mockito.doReturn(mockFile).when(bigDataDatasetServiceSpy).createEtlImportFolder(datasetId, jobId);
        Mockito.doReturn(fileList).when(bigDataDatasetServiceSpy).storeAndUnzipEtlImportZipFile(datasetId, filePathInS3, fileExtension, jobId, mockFile, tableNamesSet, attachmentsExistPerTableName);
        Mockito.when(representativeControllerZuul.findDataProviderById(providerId)).thenReturn(dataProviderVO);

        Mockito.doAnswer(invocation -> {
            ImportFileInDremioInfo info = invocation.getArgument(0);
            info.setErrorMessage(errorMessage);
            throw new RuntimeException(errorMessage);
        }).when(parquetConverterService).convertCsvFilesToParquetFiles(any(), eq(fileList), eq(datasetSchema));

        bigDataDatasetServiceSpy.etlImportDataset(datasetId, dataflowId, providerId, replaceData, tableSchemaId, delimiter, filePathInS3, jobId, dataFlowVO, dataSetMetabaseVO);

        ArgumentCaptor<ImportFileInDremioInfo> captor = ArgumentCaptor.forClass(ImportFileInDremioInfo.class);
        Mockito.verify(parquetConverterService).convertCsvFilesToParquetFiles(captor.capture(), eq(fileList), eq(datasetSchema));
        assertEquals(errorMessage, captor.getValue().getErrorMessage());
        Mockito.verify(jobControllerZuul).updateJobStatus(jobId, jobStatus);
    }

    @Test
    public void etlImportDatasetDesignDatasetFinishedJobTest() throws Exception {
        Long dataflowId = 1L;
        Long datasetId = 1L;
        Long providerId = 1L;
        Boolean replaceData = false;
        String tableSchemaId = "abc";
        String tableName = "name";
        String delimiter = ",";
        String filePathInS3 = "path.zip";
        Long jobId = 1L;
        DatasetTypeEnum datasetType = DatasetTypeEnum.DESIGN;
        JobStatusEnum jobStatus = JobStatusEnum.FINISHED;
        String fileExtension = ".zip";
        List<File> fileList = new ArrayList<>();
        JobVO jobVO = new JobVO();
        jobVO.setId(jobId);
        jobVO.setJobStatus(jobStatus);
        DataFlowVO dataFlowVO = new DataFlowVO();
        DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
        DataProviderVO dataProviderVO  = new DataProviderVO();
        Map<String, Boolean> attachmentsExistPerTableName = new HashMap<>();
        TableSchemaIdNameVO tableSchemaIdNameVO = new TableSchemaIdNameVO();
        tableSchemaIdNameVO.setIdTableSchema(tableSchemaId);
        tableSchemaIdNameVO.setNameTableSchema(tableName);
        List<TableSchemaIdNameVO> tableSchemaIdNameVOS = new ArrayList<>();
        tableSchemaIdNameVOS.add(tableSchemaIdNameVO);
        Set<String> tableNamesSet = tableSchemaIdNameVOS.stream().map(vo -> vo.getNameTableSchema().toLowerCase()).collect(Collectors.toSet());


        Mockito.when(datasetService.getDatasetType(datasetId)).thenReturn(datasetType);
        Mockito.when(jobControllerZuul.findJobById(jobId)).thenReturn(jobVO);

        // Use spy in order to mock protected methods
        BigDataDatasetServiceImpl bigDataDatasetServiceSpy = Mockito.spy(bigDataDatasetService);
        Mockito.when(datasetSchemaService.getTableSchemasIds(datasetId)).thenReturn(tableSchemaIdNameVOS);
        Mockito.doReturn(mockFile).when(bigDataDatasetServiceSpy).createEtlImportFolder(datasetId, jobId);
        Mockito.doReturn(fileList).when(bigDataDatasetServiceSpy).storeAndUnzipEtlImportZipFile(datasetId, filePathInS3, fileExtension, jobId, mockFile, tableNamesSet, attachmentsExistPerTableName);
        Mockito.when(representativeControllerZuul.findDataProviderById(providerId)).thenReturn(dataProviderVO);

        bigDataDatasetServiceSpy.etlImportDataset(datasetId, dataflowId, providerId, replaceData, tableSchemaId, delimiter, filePathInS3, jobId, dataFlowVO, dataSetMetabaseVO);
        Mockito.verify(jobControllerZuul, Mockito.times(1)).updateJobStatus(jobId, jobStatus);
    }

    @Test
    public void etlImportDatasetDesignDatasetFinishedJobWithWarningTest() throws Exception {
        Long dataflowId = 1L;
        Long datasetId = 1L;
        Long providerId = 1L;
        Boolean replaceData = false;
        String tableSchemaId = "abc";
        String tableName = "name";
        String delimiter = ",";
        String filePathInS3 = "path.zip";
        Long jobId = 1L;
        DatasetTypeEnum datasetType = DatasetTypeEnum.DESIGN;
        JobStatusEnum jobStatus = JobStatusEnum.FINISHED;
        JobInfoEnum jobInfoEnum = JobInfoEnum.WARNING_SOME_IMPORT_FILES_CONTAIN_WRONG_HEADERS;
        String fileExtension = ".zip";
        List<File> fileList = new ArrayList<>();
        JobVO jobVO = new JobVO();
        jobVO.setId(jobId);
        jobVO.setJobStatus(jobStatus);
        DataFlowVO dataFlowVO = new DataFlowVO();
        DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
        DataProviderVO dataProviderVO  = new DataProviderVO();
        Map<String, Boolean> attachmentsExistPerTableName = new HashMap<>();
        TableSchemaIdNameVO tableSchemaIdNameVO = new TableSchemaIdNameVO();
        tableSchemaIdNameVO.setIdTableSchema(tableSchemaId);
        tableSchemaIdNameVO.setNameTableSchema(tableName);
        List<TableSchemaIdNameVO> tableSchemaIdNameVOS = new ArrayList<>();
        tableSchemaIdNameVOS.add(tableSchemaIdNameVO);
        Set<String> tableNamesSet = tableSchemaIdNameVOS.stream().map(vo -> vo.getNameTableSchema().toLowerCase()).collect(Collectors.toSet());
        DataSetSchema datasetSchema = new DataSetSchema();
        List<String> warningMessages = new ArrayList<>();
        warningMessages.add(jobInfoEnum.getValue(null));
        warningMessages.add(JobInfoEnum.WARNING_SOME_FILES_ARE_EMPTY.getValue(null));


        Mockito.when(datasetService.getDatasetType(datasetId)).thenReturn(datasetType);
        Mockito.when(jobControllerZuul.findJobById(jobId)).thenReturn(jobVO);

        // Use spy in order to mock protected methods
        BigDataDatasetServiceImpl bigDataDatasetServiceSpy = Mockito.spy(bigDataDatasetService);
        Mockito.when(datasetSchemaService.getTableSchemasIds(datasetId)).thenReturn(tableSchemaIdNameVOS);
        Mockito.when(datasetService.getSchemaIfReportable(datasetId, tableSchemaId)).thenReturn(datasetSchema);
        Mockito.doReturn(mockFile).when(bigDataDatasetServiceSpy).createEtlImportFolder(datasetId, jobId);
        Mockito.doReturn(fileList).when(bigDataDatasetServiceSpy).storeAndUnzipEtlImportZipFile(datasetId, filePathInS3, fileExtension, jobId, mockFile, tableNamesSet, attachmentsExistPerTableName);
        Mockito.when(representativeControllerZuul.findDataProviderById(providerId)).thenReturn(dataProviderVO);

        Mockito.doAnswer(invocation -> {
            ImportFileInDremioInfo info = invocation.getArgument(0);
            info.setWarningMessages(warningMessages);
            return null;
        }).when(parquetConverterService).convertCsvFilesToParquetFiles(any(), eq(fileList), eq(datasetSchema));

        bigDataDatasetServiceSpy.etlImportDataset(datasetId, dataflowId, providerId, replaceData, tableSchemaId, delimiter, filePathInS3, jobId, dataFlowVO, dataSetMetabaseVO);

        ArgumentCaptor<ImportFileInDremioInfo> captor = ArgumentCaptor.forClass(ImportFileInDremioInfo.class);
        Mockito.verify(parquetConverterService).convertCsvFilesToParquetFiles(captor.capture(), eq(fileList), eq(datasetSchema));
        Mockito.verify(jobControllerZuul).updateJobStatusAndInfo(jobId, jobStatus, jobInfoEnum, null);
    }

    @Test
    public void etlImportDatasetDesignDatasetFinishedJobWithNonExistingWarningTest() throws Exception {
        Long dataflowId = 1L;
        Long datasetId = 1L;
        Long providerId = 1L;
        Boolean replaceData = false;
        String tableSchemaId = "abc";
        String tableName = "name";
        String delimiter = ",";
        String filePathInS3 = "path.zip";
        Long jobId = 1L;
        DatasetTypeEnum datasetType = DatasetTypeEnum.DESIGN;
        JobStatusEnum jobStatus = JobStatusEnum.FINISHED;
        String fileExtension = ".zip";
        List<File> fileList = new ArrayList<>();
        JobVO jobVO = new JobVO();
        jobVO.setId(jobId);
        jobVO.setJobStatus(jobStatus);
        DataFlowVO dataFlowVO = new DataFlowVO();
        DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
        DataProviderVO dataProviderVO  = new DataProviderVO();
        Map<String, Boolean> attachmentsExistPerTableName = new HashMap<>();
        TableSchemaIdNameVO tableSchemaIdNameVO = new TableSchemaIdNameVO();
        tableSchemaIdNameVO.setIdTableSchema(tableSchemaId);
        tableSchemaIdNameVO.setNameTableSchema(tableName);
        List<TableSchemaIdNameVO> tableSchemaIdNameVOS = new ArrayList<>();
        tableSchemaIdNameVOS.add(tableSchemaIdNameVO);
        Set<String> tableNamesSet = tableSchemaIdNameVOS.stream().map(vo -> vo.getNameTableSchema().toLowerCase()).collect(Collectors.toSet());
        DataSetSchema datasetSchema = new DataSetSchema();
        List<String> warningMessages = new ArrayList<>();
        warningMessages.add("warning doesn't exist");
        warningMessages.add(JobInfoEnum.WARNING_SOME_FILES_ARE_EMPTY.getValue(null));


        Mockito.when(datasetService.getDatasetType(datasetId)).thenReturn(datasetType);
        Mockito.when(jobControllerZuul.findJobById(jobId)).thenReturn(jobVO);

        // Use spy in order to mock protected methods
        BigDataDatasetServiceImpl bigDataDatasetServiceSpy = Mockito.spy(bigDataDatasetService);
        Mockito.when(datasetSchemaService.getTableSchemasIds(datasetId)).thenReturn(tableSchemaIdNameVOS);
        Mockito.when(datasetService.getSchemaIfReportable(datasetId, tableSchemaId)).thenReturn(datasetSchema);
        Mockito.doReturn(mockFile).when(bigDataDatasetServiceSpy).createEtlImportFolder(datasetId, jobId);
        Mockito.doReturn(fileList).when(bigDataDatasetServiceSpy).storeAndUnzipEtlImportZipFile(datasetId, filePathInS3, fileExtension, jobId, mockFile, tableNamesSet, attachmentsExistPerTableName);
        Mockito.when(representativeControllerZuul.findDataProviderById(providerId)).thenReturn(dataProviderVO);

        Mockito.doAnswer(invocation -> {
            ImportFileInDremioInfo info = invocation.getArgument(0);
            info.setWarningMessages(warningMessages);
            return null;
        }).when(parquetConverterService).convertCsvFilesToParquetFiles(any(), eq(fileList), eq(datasetSchema));

        bigDataDatasetServiceSpy.etlImportDataset(datasetId, dataflowId, providerId, replaceData, tableSchemaId, delimiter, filePathInS3, jobId, dataFlowVO, dataSetMetabaseVO);

        ArgumentCaptor<ImportFileInDremioInfo> captor = ArgumentCaptor.forClass(ImportFileInDremioInfo.class);
        Mockito.verify(parquetConverterService).convertCsvFilesToParquetFiles(captor.capture(), eq(fileList), eq(datasetSchema));
        Mockito.verify(jobControllerZuul).updateJobStatus(jobId, jobStatus);
    }

    @Test
    public void isValidAttachmentEntryTest() {
        assertTrue(bigDataDatasetService.isValidAttachmentEntry("attachments/tableName/file.pdf", false)); //correct use
        assertFalse(bigDataDatasetService.isValidAttachmentEntry("attachments/file.pdf", false)); //file inside attachments folder
        assertFalse(bigDataDatasetService.isValidAttachmentEntry("attachments/tableName/", true)); //empty table folder inside attachments folder
        assertFalse(bigDataDatasetService.isValidAttachmentEntry("attachments/tableName/dir/file.pdf", false)); //attachments folder with folder inside table folder
        assertFalse(bigDataDatasetService.isValidAttachmentEntry("attachments", true)); //empty attachments folder
        assertFalse(bigDataDatasetService.isValidAttachmentEntry("wrongName/tableName/file.pdf", true)); //parent folder is not named attachments
    }
}
