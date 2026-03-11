package org.eea.dataset.service.impl;

import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.mapper.HelperMultipartFileMapper;
import org.eea.dataset.persistence.metabase.domain.DatasetTable;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.domain.pkcatalogue.PkCatalogueSchema;
import org.eea.dataset.persistence.schemas.repository.PkCatalogueRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.*;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.file.ZipUtils;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.model.ImportFileInDremioInfo;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.*;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetRunningStatusEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.metabase.ReleaseVO;
import org.eea.interfaces.vo.orchestrator.JobPresignedUrlInfo;
import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.redis.LockEnum;
import org.eea.lock.redis.RedisLockService;
import org.eea.multitenancy.DatasetId;
import org.eea.multitenancy.TenantResolver;
import org.eea.utils.LiteralConstants;
import org.eea.utils.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.transfer.s3.config.DownloadFilter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum.REPORTING;
import static org.eea.utils.LiteralConstants.*;


@ImportDataLakeCommons
@Service
public class BigDataDatasetServiceImpl implements BigDataDatasetService {

    private static final Logger LOG = LoggerFactory.getLogger(BigDataDatasetServiceImpl.class);

    @Value("${importPath}")
    private String importPath;

    /**  The path export DL */
    @Value("${exportDLPath}")
    private String exportDLPath;

    private int defaultFileExportProcessPriority = 20;

    private static final int defaultImportProcessPriority = 20;

    ParquetConverterService parquetConverterService;

    private final JobControllerZuul jobControllerZuul;

    private final JobProcessControllerZuul jobProcessControllerZuul;

    private final DatasetMetabaseService datasetMetabaseService;

    private final DremioAutoPromotionService dremioAutoPromotionService;

    private final ProcessControllerZuul processControllerZuul;

    private FileTreatmentHelper fileTreatmentHelper;

    private KafkaSenderUtils kafkaSenderUtils;

    public RepresentativeControllerZuul representativeControllerZuul;

    private final FileCommonUtils fileCommonUtils;

    private final DatasetSchemaService datasetSchemaService;

    private final SpatialDataHandling  spatialDataHandling;

    private final DatasetTableService datasetTableService;

    private final DataFlowControllerZuul dataFlowControllerZuul;

    private CreateEmptyTables createEmptyTables;

    /** The pk catalogue repository. */
    private PkCatalogueRepository pkCatalogueRepository;

    private DatasetSnapshotService datasetSnapshotService;

    private TableDataRetriever tableDataRetriever;

    private final S3Service s3ServicePrivate;
    private final S3Service s3ServicePublic;
    private final S3Helper s3HelperPrivate;
    private final S3Helper s3HelperPublic;
    private final DatasetService datasetService;
    private final EtlExportV5Service etlExportV5Service;

    private final DremioHelperService dremioHelperService;
    private JdbcTemplate dremioJdbcTemplate;

    private SchemasRepository schemasRepository;

    private NotificationControllerZuul notificationControllerZuul;

    private RedisLockService redisLockService;

    private static final String HEADER_NAME = "headerName";
    private static final String TYPE_DATA = "typeData";
    private static final String ID_RECORD = "idRecord";
    private static final String ID_TABLE_SCHEMA = "idTableSchema";
    private static final String NAME_TABLE_SCHEMA = "nameTableSchema";
    private static final String VALUE = "refValue";
    private static final String LABEL = "refLabel";
    private static final Pattern CSV_WITH_UUID_PATTERN = Pattern.compile(
            "^.+?_[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.csv$"
    );

    private String ETL_IMPORT_FOLDER = "etlImport_%s";

    private void deleteCsvFilesWithUuidSuffix(String datasetId, String preparationCode) {
        // this method is matching and deleting all csv files that have an ending of a UUID and then `.csv` like:
        // data_550e8400-e29b-41d4-a716-446655440000.csv
        // table1_7d9f45d3-2e68-4b9e-bfe3-3a472ef4234b.csv
        // Those files have 2 columns added and therefore should be deleted, those are temporary
        File folder = resolveImportFolder(datasetId, preparationCode);

        if (!folder.exists() || !folder.isDirectory()) {
            LOG.warn("Import path does not exist or is not a directory: {}", importPath);
            return;
        }

        try (Stream<Path> paths = Files.walk(folder.toPath())) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> CSV_WITH_UUID_PATTERN.matcher(p.getFileName().toString()).matches())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                            LOG.info("Deleted CSV with UUID suffix: {}", p);
                        } catch (IOException e) {
                            LOG.error("Failed to delete CSV file: {}", p, e);
                        }
                    });
        } catch (IOException e) {
            LOG.error("Error walking import path for CSV cleanup: {}", importPath, e);
        }
    }

    public BigDataDatasetServiceImpl(@Qualifier("publicS3Helper") S3Helper s3HelperPublic, S3Helper s3HelperPrivate, DremioHelperService dremioHelperService,
                                     ParquetConverterService parquetConverterService, JdbcTemplate dremioJdbcTemplate, SchemasRepository schemasRepository, @Lazy DatasetSnapshotService datasetSnapshotService, @Lazy DatasetService datasetService, JobControllerZuul jobControllerZuul,
                                     JobProcessControllerZuul jobProcessControllerZuul, DatasetMetabaseService datasetMetabaseService, ProcessControllerZuul processControllerZuul, KafkaSenderUtils kafkaSenderUtils, RepresentativeControllerZuul representativeControllerZuul,
                                     FileCommonUtils fileCommonUtils, @Lazy DatasetSchemaService datasetSchemaService, SpatialDataHandling  spatialDataHandling, DatasetTableService datasetTableService, DataFlowControllerZuul dataFlowControllerZuul, CreateEmptyTables createEmptyTables,
                                     PkCatalogueRepository pkCatalogueRepository, TableDataRetriever tableDataRetriever, EtlExportV5Service etlExportV5Service, NotificationControllerZuul notificationControllerZuul, RedisLockService redisLockService, DremioAutoPromotionService dremioAutoPromotionService) {
        this.jobControllerZuul =  jobControllerZuul;
        this.jobProcessControllerZuul = jobProcessControllerZuul;
        this.datasetMetabaseService = datasetMetabaseService;
        this.processControllerZuul = processControllerZuul;
        this.kafkaSenderUtils = kafkaSenderUtils;
        this.representativeControllerZuul  = representativeControllerZuul;
        this.fileCommonUtils = fileCommonUtils;
        this.datasetSchemaService = datasetSchemaService;
        this.spatialDataHandling = spatialDataHandling;
        this.datasetTableService = datasetTableService;
        this.dataFlowControllerZuul = dataFlowControllerZuul;
        this.createEmptyTables = createEmptyTables;
        this.pkCatalogueRepository = pkCatalogueRepository;
        this.tableDataRetriever = tableDataRetriever;
        this.s3HelperPrivate = s3HelperPrivate;
        this.s3HelperPublic = s3HelperPublic;
        this.s3ServicePublic = s3HelperPublic.getS3Service();
        this.s3ServicePrivate = s3HelperPrivate.getS3Service();
        this.dremioHelperService = dremioHelperService;
        this.parquetConverterService = parquetConverterService;
        this.fileTreatmentHelper = parquetConverterService.getFileTreatmentHelper();
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.schemasRepository = schemasRepository;
        this.datasetSnapshotService = datasetSnapshotService;
        this.datasetService = datasetService;
        this.etlExportV5Service = etlExportV5Service;
        this.notificationControllerZuul = notificationControllerZuul;
        this.redisLockService = redisLockService;
        this.dremioAutoPromotionService = dremioAutoPromotionService;
    }

    @Override
    @Async
    public void importBigData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId,
                              Boolean replace, Long integrationId, String delimiter, Long jobId,
                              String fmeJobId, DataFlowVO dataflowVO, HelperMultipartFileMapper helperMultipartFileMapper, JobVO job, ImportFileInDremioInfo importFileInDremioInfo, String preparationCode) throws Exception {
        JobStatusEnum jobStatus = JobStatusEnum.IN_PROGRESS;
        String filePathInS3 = null;
        String fileName = helperMultipartFileMapper.getOriginalFilename();
        File s3File = null;
        try {
            jobStatus = job.getJobStatus();
            if(job.getParameters().get("filePathInS3") != null) {
                filePathInS3 = job.getParameters().get("filePathInS3").toString();
            }
            if(helperMultipartFileMapper.isFileNull()){
                if(StringUtils.isBlank(filePathInS3)){
                    throw new EEAException("Empty file and file path");
                }
                String fileExtension = getFileExtensionFromFilePath(filePathInS3);
                LOG.info("For jobId {} downloading file from s3 in path {} with fileExtension {}", jobId, filePathInS3, fileExtension);

                String[] filePathInS3Split = filePathInS3.split("/");
                String fileNameInS3 = filePathInS3Split[filePathInS3Split.length - 1];
                String filePathStructure;

                if (preparationCode!=null) {
                    //Save to preparation directory inside dataset dir
                    filePathStructure = "/" + datasetId + "/" + preparationCode + "/" + fileNameInS3;
                } else{
                    filePathStructure = "/" + datasetId + "/" + fileNameInS3;
                }

                File folder = new File(importPath + "/" + datasetId);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                try {
                    s3File = s3HelperPublic.getFileFromS3(filePathInS3, filePathStructure.replace(fileExtension, ""), importPath, fileExtension);
                }
                catch (Exception e){
                    LOG.error("For jobId {} could not find file {} in public s3. Error: {}", jobId, filePathInS3, e.getMessage());
                    jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.ERROR_NO_FILE_IN_S3, null);
                    throw e;
                }
                fileName = s3File.getName();
            }

            //Retrieve providerId and providerCode
            List<Long> datasetIds = new ArrayList<>();
            datasetIds.add(datasetId);
            String providerCode = null;
            if(providerId != null){
                DataProviderVO dataProviderVO = representativeControllerZuul.findDataProviderById(providerId);
                providerCode = dataProviderVO.getCode();
            }

            if (StringUtils.isNotBlank(fmeJobId) && job!= null && job.getParameters().get("replace") != null) {
                //retrieve replace data value from the job
                replace = (Boolean) job.getParameters().get("replace");
            }

            importFileInDremioInfo.setProviderId(providerId);
            importFileInDremioInfo.setFileName(fileName);
            importFileInDremioInfo.setReplaceData(replace);
            importFileInDremioInfo.setDelimiter(delimiter);
            importFileInDremioInfo.setDataProviderCode(providerCode);
            importFileInDremioInfo.setIsEtlImport(false);
            importFileInDremioInfo.setPreparationCode(preparationCode);

            DatasetTypeEnum datasetType = datasetService.getDatasetType(importFileInDremioInfo.getDatasetId());
            if (DatasetTypeEnum.REFERENCE.equals(datasetType) && dataflowVO.getStatus() == TypeStatusEnum.DRAFT) {
                importFileInDremioInfo.setUpdateReferenceFolder(true);
            }
            else{
                importFileInDremioInfo.setUpdateReferenceFolder(false);
            }

            LOG.info("Importing file to s3 {}", importFileInDremioInfo);
            importDatasetDataToDremio(importFileInDremioInfo, s3File, helperMultipartFileMapper);
            //the fme job for the first iteration should not be finished yet
            if(integrationId == null) {
                finishImportProcess(importFileInDremioInfo);
            }
            //remove file from public S3 if job is finished
            if (jobControllerZuul.findJobById(jobId).getJobStatus() == JobStatusEnum.FINISHED) {
                s3HelperPublic.deleteFileFromS3(getFilePath(datasetId, dataflowId, providerId, fileName, true));
            }
            LOG.info("Successfully imported file to s3 {}", importFileInDremioInfo);
        } catch (EEAException e) {
            LOG.error("File import failed: for jobId {} dataflowId={} datasetId={}, tableSchemaId={}, fileName={} ", jobId, dataflowId, datasetId,
                    tableSchemaId, fileName, e);
            if (jobId != null) {
                if(StringUtils.isBlank(importFileInDremioInfo.getErrorMessage())){
                    importFileInDremioInfo.setErrorMessage(StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : "Error in dremio import" );
                }
                finishImportProcess(importFileInDremioInfo);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.IMPORTING_FILE_DATASET);
        } catch (Exception e) {
            LOG.error("Unexpected error! Error importing file {} to s3 for jobId {} datasetId {} providerId {} and tableSchemaId {} ", fileName, jobId, datasetId, providerId, tableSchemaId, e);
            if (jobId!=null && jobStatus != JobStatusEnum.REFUSED) {
                if(StringUtils.isBlank(importFileInDremioInfo.getErrorMessage())){
                    importFileInDremioInfo.setErrorMessage(StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : "Error in dremio import" );
                }
                finishImportProcess(importFileInDremioInfo);
            }
            throw e;
        }
    }

    private void importDatasetDataToDremio(ImportFileInDremioInfo importFileInDremioInfo, File fileFromS3, HelperMultipartFileMapper helperMultipartFileMapper) throws Exception {

        boolean isPreparationDataset = StringUtils.isNotBlank(importFileInDremioInfo.getPreparationCode());

        if (importFileInDremioInfo.getDelimiter() != null && importFileInDremioInfo.getDelimiter().length() > 1) {
            LOG.error("Error when importing file data to s3 {}. The size of the delimiter cannot be greater than 1", importFileInDremioInfo);

            //skip update running status for preparation
            if(!isPreparationDataset) {
                datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(), DatasetRunningStatusEnum.ERROR_IN_IMPORT);
            }

            jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_WRONG_DELIMITER_SIZE, null);
            throw new EEAException("The size of the delimiter cannot be greater than 1");
        }

        //if there is already a process created for the import then it should be updated instead of creating a new one
        String processUUID = null;
        Boolean processExists = false;
        List<String> processIds = jobProcessControllerZuul.findProcessesByJobId(importFileInDremioInfo.getJobId());
        if(processIds != null && processIds.size() > 0){
            processUUID = processIds.get(0);
            processExists = true;
            LOG.info("Process with id {} already exists for import job {}", processUUID, (importFileInDremioInfo.getJobId()));
        }
        else{
            processUUID = UUID.randomUUID().toString();
        }

        importFileInDremioInfo.setProcessId(processUUID);

        DataSetSchema schema = datasetService.getSchemaIfReportable(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getTableSchemaId());
        Boolean processUpdated = processControllerZuul.updateProcess(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getDataflowId(), ProcessStatusEnum.IN_QUEUE, ProcessTypeEnum.IMPORT, processUUID,
                SecurityContextHolder.getContext().getAuthentication().getName(), defaultImportProcessPriority, null);
        if(!processUpdated){
            jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_UPDATING_PROCESS, null);
            throw new Exception("Could not update process to status IN_QUEUE for processId=" + importFileInDremioInfo.getProcessId() + " and jobId "+ importFileInDremioInfo.getJobId());
        }

        if(importFileInDremioInfo.getJobId() != null && !processExists){
            JobProcessVO jobProcessVO = new JobProcessVO(null, importFileInDremioInfo.getJobId(), processUUID);
            jobProcessControllerZuul.save(jobProcessVO);
        }

        if (null == schema) {
            jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_NOT_REPORTABLE_DATASET, null);
            throw new EEAException("Dataset is not reportable: datasetId=" + importFileInDremioInfo.getDatasetId() + ", tableSchemaId=" + importFileInDremioInfo.getTableSchemaId() + ", fileName=" + importFileInDremioInfo.getFileName());
        }

        // Skip for Preparations
        if(!isPreparationDataset){
            // We add a lock to the Release process
            datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(), DatasetRunningStatusEnum.IMPORTING);
            Map<String, Object> mapCriteria = new HashMap<>();
            mapCriteria.put("dataflowId", importFileInDremioInfo.getDataflowId());
            mapCriteria.put("dataProviderId", importFileInDremioInfo.getProviderId());
            if (importFileInDremioInfo.getProviderId() != null) {
                datasetService.createLockWithSignature(LockSignature.RELEASE_SNAPSHOTS, mapCriteria, SecurityContextHolder.getContext().getAuthentication().getName());
            }
        }
        handleZipFile(importFileInDremioInfo, fileFromS3, schema, helperMultipartFileMapper);
    }

    private void handleZipFile(ImportFileInDremioInfo importFileInDremioInfo, File fileFromS3, DataSetSchema schema, HelperMultipartFileMapper helperMultipartFileMapper) throws Exception {
        Boolean processWasUpdated = processControllerZuul.updateProcess(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getDataflowId(),
                ProcessStatusEnum.IN_PROGRESS, ProcessTypeEnum.IMPORT, importFileInDremioInfo.getProcessId(),
                SecurityContextHolder.getContext().getAuthentication().getName(), 0, null);

        if (!processWasUpdated) {
            jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_UPDATING_PROCESS, null);
            throw new Exception("Could not update process to status IN_PROGRESS for processId=" + importFileInDremioInfo.getProcessId() + " and jobId " + importFileInDremioInfo.getJobId());
        }

        String originalFileName = importFileInDremioInfo.getFileName();
        String mimeType = datasetService.getMimetype(originalFileName);

        IntegrationVO integrationVO = null;
        if (importFileInDremioInfo.getIntegrationId() != null) {
            integrationVO = fileTreatmentHelper.getIntegrationVO(importFileInDremioInfo.getIntegrationId());
            if (integrationVO == null) {
                LOG.error("Error. Integration {} not found for job {}", importFileInDremioInfo.getIntegrationId(), importFileInDremioInfo);
            }
        }
        List<File> filesToImport = null;
        if (!helperMultipartFileMapper.isFileNull()) {
            filesToImport = storeImportFiles(importFileInDremioInfo, integrationVO, mimeType, helperMultipartFileMapper);
        } else {
            filesToImport = handleAlreadyStoredImportFiles(fileFromS3, importFileInDremioInfo, integrationVO, mimeType);
        }

        if (integrationVO != null && filesToImport.size() != 0) {
            handleFmeRequest(integrationVO, importFileInDremioInfo, filesToImport.get(0), mimeType);
        } else {
            List<File> correctFilesForImport = checkCsvFiles(importFileInDremioInfo, schema, filesToImport);
            DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(importFileInDremioInfo.getDatasetId());
            parquetConverterService.convertCsvFilesToParquetFiles(importFileInDremioInfo, correctFilesForImport, schema, dataSetMetabaseVO);

        }
    }

    private void handleFmeRequest(IntegrationVO integrationVO, ImportFileInDremioInfo importFileInDremioInfo, File file, String mimeType) throws EEAException {
        try {// TODO for preparation
            fileTreatmentHelper.prepareFmeFileProcess(importFileInDremioInfo.getDatasetId(), file, integrationVO, mimeType, importFileInDremioInfo.getTableSchemaId(),
                    false, importFileInDremioInfo.getJobId(), importFileInDremioInfo.getPreparationCode());
        }
        catch (Exception e){
            throw new EEAException("Could not prepare fme request for job id " + importFileInDremioInfo.getJobId());
        }
    }

    private List<File> checkCsvFiles(ImportFileInDremioInfo importFileInDremioInfo, DataSetSchema schema, List<File> files)
            throws EEAException {

        LOG.info("Checking csv files {}. {}", files, importFileInDremioInfo);
        List<File> correctFilesForImport = new ArrayList<>();

        Boolean guessTableName = null == importFileInDremioInfo.getTableSchemaId();
        String tableSchemaId = importFileInDremioInfo.getTableSchemaId();
        Boolean sendWrongFileNameWarning = false;
        int numberOfWrongFiles = 0;
        for (File file : files) {
            String fileName = file.getName();
            LOG.info("Checking csv file {}. {}", fileName, importFileInDremioInfo);

            if (guessTableName) {
                tableSchemaId = fileTreatmentHelper.getTableSchemaIdFromFileName(schema, fileName, false);
            }

            if (!guessTableName || StringUtils.isNotBlank(tableSchemaId)) {
                // obtains the file type from the extension
                if (fileName == null) {
                    jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_EMPTY_FILENAME, null);
                    throw new EEAException(EEAErrorMessage.FILE_NAME);
                }
                final String fileMimeType = datasetService.getMimetype(fileName).toLowerCase();
                // validates file types for the data load
                fileTreatmentHelper.validateFileType(fileMimeType);

                if (FileTypeEnum.getEnum(fileMimeType.toLowerCase()) == FileTypeEnum.CSV) {
                    correctFilesForImport.add(file);
                }
            } else {
                sendWrongFileNameWarning = true;
                LOG.error("Importing file {} to s3. {}. There's no table with that fileName", fileName, importFileInDremioInfo);
                datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(), DatasetRunningStatusEnum.ERROR_IN_IMPORT);
                numberOfWrongFiles++;
                if (numberOfWrongFiles == files.size()) {
                    sendWrongFileNameWarning = false;
                    DatasetTypeEnum type = datasetService.getDatasetType(importFileInDremioInfo.getDatasetId());
                    EventType eventType = REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
                            ? EventType.IMPORT_REPORTING_FAILED_NAMEFILE_EVENT
                            : EventType.IMPORT_DESIGN_FAILED_NAMEFILE_EVENT;

                    datasetService.failImportJobAndProcess(importFileInDremioInfo.getProcessId(), importFileInDremioInfo.getDatasetId(), tableSchemaId, fileName, eventType, JobInfoEnum.ERROR_WRONG_FILE_NAME);
                    importFileInDremioInfo.setErrorMessage(EEAErrorMessage.ERROR_FILE_NAME_MATCHING);
                    throw new EEAException(EEAErrorMessage.ERROR_FILE_NAME_MATCHING);
                }
            }
        }
        if(sendWrongFileNameWarning){
            //initialize warning message
            importFileInDremioInfo.setWarningMessages(new ArrayList<>());
            importFileInDremioInfo.getWarningMessages().add(JobInfoEnum.WARNING_SOME_FILENAMES_DO_NOT_MATCH_TABLES.getValue(null));
            jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.WARNING_SOME_FILENAMES_DO_NOT_MATCH_TABLES, null);
        }
        return correctFilesForImport;

    }

    private List<File> storeImportFiles(ImportFileInDremioInfo importFileInDremioInfo, IntegrationVO integrationVO, String multipartFileMimeType, HelperMultipartFileMapper helperMultipartFileMapper) throws Exception {
        List<File> files = new ArrayList<>();

        // Prepare the folder where files will be stored
        File folder = resolveImportFolder(importFileInDremioInfo.getDatasetId().toString(), importFileInDremioInfo.getPreparationCode());
        String saveLocationPath = folder.getCanonicalPath();
        if (!folder.exists()) {
            folder.mkdir();
        }

        if(integrationVO == null && multipartFileMimeType.equalsIgnoreCase("zip")) {
            //store zip file
            File storedMultipartFile = new File(saveLocationPath + "/" + importFileInDremioInfo.getFileName());
            try (InputStream in = helperMultipartFileMapper.getInputStream();
                 OutputStream os = new FileOutputStream(storedMultipartFile)) {
                IOUtils.copyLarge(in, os);
                helperMultipartFileMapper.setFile(storedMultipartFile);
                helperMultipartFileMapper.setInputStream(null);
                LOG.info("Stored file {} job {}", storedMultipartFile.getPath(), importFileInDremioInfo);
            } catch (Exception e) {
                LOG.error("Unexpected error! Error storing file {} for import job {}. Message: {}", storedMultipartFile, importFileInDremioInfo, e.getMessage());
                throw e;
            }
        }


        try (InputStream input = helperMultipartFileMapper.getInputStream()) {

            if (integrationVO == null && multipartFileMimeType.equalsIgnoreCase("zip")) {
                try (ZipInputStream zip = new ZipInputStream(input)) {
                    ZipEntry entry = zip.getNextEntry();
                    while (null != entry) {
                        String entryName = entry.getName();
                        String mimeType = datasetService.getMimetype(entryName);
                        File file = new File(folder, entryName);
                        String filePath = file.getCanonicalPath();

                        // Prevent Zip Slip attack or skip if the entry is a directory
                        if ((entryName.split("/").length > 1)
                                || !FileTypeEnum.CSV.getValue().equalsIgnoreCase(mimeType) || entry.isDirectory()
                                || !filePath.startsWith(saveLocationPath + File.separator)) {
                            LOG.error("Ignored file from ZIP: {}. {}", entryName, importFileInDremioInfo);
                            if (entry != null){
                                zip.closeEntry();
                            }
                            entry = zip.getNextEntry();
                            continue;
                        }

                        // Store the file in the persistence volume
                        try (FileOutputStream output = new FileOutputStream(file)) {
                            IOUtils.copyLarge(zip, output);
                            LOG.info("Stored file {}. {}", file.getPath(), importFileInDremioInfo);
                        } catch (Exception e) {
                            LOG.error("Unexpected error! Error in copyLarge for saveLocationPath {}. {} Message: {}", saveLocationPath, importFileInDremioInfo, e.getMessage());
                            throw e;
                        }
                        if (entry != null){
                            zip.closeEntry();
                        }
                        entry = zip.getNextEntry();
                        files.add(file);

                    }
                    if (entry != null){
                        zip.closeEntry();
                    }
                } catch (Exception e) {
                    LOG.error("Unexpected error! Error in storeImportFiles {}. Message: {}", importFileInDremioInfo, e.getMessage());
                    throw e;
                }
            } else {
                File file = new File(folder, helperMultipartFileMapper.getOriginalFilename());

                // Store the file in the persistence volume
                try (FileOutputStream output = new FileOutputStream(file)) {
                    IOUtils.copyLarge(input, output);
                    files.add(file);
                    LOG.info("Stored file {} job {}", file.getPath(), importFileInDremioInfo);
                } catch (Exception e) {
                    LOG.error("Unexpected error! Error storing file for import job {}. Message: {}", importFileInDremioInfo, e.getMessage());
                    throw e;
                }

                if (integrationVO != null && multipartFileMimeType.equalsIgnoreCase("zip")) {
                    try (ZipFile zipFile = new ZipFile(file)) {
                        if (zipFile.size() == 0) {
                            throw new EEAException("Empty zip file for datasetId " + importFileInDremioInfo.getDatasetId() + " and jobId " + importFileInDremioInfo.getJobId());
                        }
                    } catch (IOException e) {
                        throw new EEAException("Empty zip file for datasetId " + importFileInDremioInfo.getDatasetId() + " and jobId " + importFileInDremioInfo.getJobId());
                    }
                }
            }

            // Queue import tasks for stored files
            if (!files.isEmpty()) {
                return files;
            } else {
                datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(), DatasetRunningStatusEnum.ERROR_IN_IMPORT);
                jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_EMPTY_ZIP, null);
                throw new EEAException("Error trying to import a zip file to s3 for datasetId " + importFileInDremioInfo.getDatasetId() + ". Empty zip file");
            }

        } catch (Exception e) {
            LOG.error("Unexpected error! Error in fileManagement {} Message: {}", importFileInDremioInfo, e.getMessage());
            throw e;
        }

    }

    private List<File> handleAlreadyStoredImportFiles(File fileFromS3, ImportFileInDremioInfo importFileInDremioInfo, IntegrationVO integrationVO, String multipartFileMimeType) throws Exception {
        List<File> files = new ArrayList<>();
        String fileMimeType = datasetService.getMimetype(importFileInDremioInfo.getFileName());

        // Prepare the folder where files will be stored
        File folder = resolveImportFolder(importFileInDremioInfo.getDatasetId().toString(), importFileInDremioInfo.getPreparationCode());
        String saveLocationPath = folder.getCanonicalPath();

        try (InputStream input = new FileInputStream(fileFromS3)) {

            if (integrationVO == null && fileMimeType.equalsIgnoreCase("zip")) {
                try (ZipInputStream zip = new ZipInputStream(input)) {
                    ZipEntry entry = zip.getNextEntry();
                    while (null != entry) {
                        String entryName = entry.getName();
                        String mimeType = datasetService.getMimetype(entryName);
                        File file = new File(folder, entryName);
                        String filePath = file.getCanonicalPath();

                        // Prevent Zip Slip attack or skip if the entry is a directory
                        if ((entryName.split("/").length > 1)
                                || !FileTypeEnum.CSV.getValue().equalsIgnoreCase(mimeType) || entry.isDirectory()
                                || !filePath.startsWith(saveLocationPath + File.separator)) {
                            LOG.error("Ignored file from ZIP: {}. {}", entryName, importFileInDremioInfo);
                            if (entry != null){
                                zip.closeEntry();
                            }
                            entry = zip.getNextEntry();
                            continue;
                        }

                        // Store the file in the persistence volume
                        try (FileOutputStream output = new FileOutputStream(file)) {
                            IOUtils.copyLarge(zip, output);
                            LOG.info("Stored file {}. {}", file.getPath(), importFileInDremioInfo);
                        } catch (Exception e) {
                            LOG.error("Unexpected error! Error in copyLarge for saveLocationPath {}. {} Message: {}", saveLocationPath, importFileInDremioInfo, e.getMessage());
                            throw e;
                        }

                        if (entry != null){
                            zip.closeEntry();
                        }
                        entry = zip.getNextEntry();
                        files.add(file);

                    }
                    if (entry != null){
                        zip.closeEntry();
                    }
                } catch (Exception e) {
                    LOG.error("Unexpected error! Error in storeImportFiles {}. Message: {}", importFileInDremioInfo, e.getMessage());
                    throw e;
                }
            } else {
                files.add(fileFromS3);

                if (integrationVO != null && multipartFileMimeType.equalsIgnoreCase("zip")) {
                    try {
                        ZipFile zipFile = new ZipFile(fileFromS3);
                        if (zipFile.size() == 0) {
                            zipFile.close();
                            throw new EEAException("Empty zip file for datasetId " + importFileInDremioInfo.getDatasetId() + " and jobId " + importFileInDremioInfo.getJobId());
                        }
                        zipFile.close();
                    } catch (IOException e) {
                        throw new EEAException("Empty zip file for datasetId " + importFileInDremioInfo.getDatasetId() + " and jobId " + importFileInDremioInfo.getJobId());
                    }
                }
            }

            // Queue import tasks for stored files
            if (!files.isEmpty()) {
                return files;
            } else {
                datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(), DatasetRunningStatusEnum.ERROR_IN_IMPORT);
                jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.ERROR_EMPTY_ZIP, null);
                throw new EEAException("Error trying to import a zip file to s3 for datasetId " + importFileInDremioInfo.getDatasetId() + ". Empty zip file");
            }

        } catch (Exception e) {
            LOG.error("Unexpected error! Error in fileManagement {} Message: {}", importFileInDremioInfo, e.getMessage());
            throw e;
        }

    }

    private void finishImportProcess(ImportFileInDremioInfo importFileInDremioInfo) throws EEAException {

        boolean isPreparationDataset = StringUtils.isNotBlank(importFileInDremioInfo.getPreparationCode());

        Map<String, Object> value = new HashMap<>();
        value.put(LiteralConstants.DATASET_ID, importFileInDremioInfo.getDatasetId());
        value.put(LiteralConstants.USER,
                SecurityContextHolder.getContext().getAuthentication().getName());
        NotificationVO notificationVO = NotificationVO.builder()
                .user(SecurityContextHolder.getContext().getAuthentication().getName())
                .datasetId(importFileInDremioInfo.getDatasetId()).tableSchemaId(importFileInDremioInfo.getTableSchemaId()).fileName(importFileInDremioInfo.getFileName()).error(importFileInDremioInfo.getErrorMessage())
                .build();

        EventType eventType;
        DatasetTypeEnum type = datasetService.getDatasetType(importFileInDremioInfo.getDatasetId());

        Long jobId = importFileInDremioInfo.getJobId();
        JobStatusEnum jobStatus;
        if (importFileInDremioInfo.getErrorMessage() != null) {
            if (EEAErrorMessage.ERROR_FILE_NAME_MATCHING.equals(importFileInDremioInfo.getErrorMessage())) {
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.ERROR_WRONG_FILE_NAME, null);
                eventType = REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
                        ? EventType.IMPORT_REPORTING_FAILED_NAMEFILE_EVENT
                        : EventType.IMPORT_DESIGN_FAILED_NAMEFILE_EVENT;
            } else if (EEAErrorMessage.ERROR_FILE_NO_HEADERS_MATCHING.equals(importFileInDremioInfo.getErrorMessage())) {
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.ERROR_NO_HEADERS_MATCHING, null);
                eventType = REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
                        ? EventType.IMPORT_REPORTING_FAILED_NO_HEADERS_MATCHING_EVENT
                        : EventType.IMPORT_DESIGN_FAILED_NO_HEADERS_MATCHING_EVENT;
            } else if (EEAErrorMessage.ERROR_IMPORT_EMPTY_FILES.equals(importFileInDremioInfo.getErrorMessage())) {
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.ERROR_ALL_FILES_ARE_EMPTY, null);
                eventType = EventType.IMPORT_EMPTY_FILES_ERROR_EVENT;
            } else if(EEAErrorMessage.ERROR_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA.equals(importFileInDremioInfo.getErrorMessage())){
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.ERROR_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA, null);
                eventType = EventType.IMPORT_FIXED_NUM_WITHOUT_REPLACE_DATA_ERROR_EVENT;
            } else if(EEAErrorMessage.ERROR_IMPORT_FAILED_WRONG_NUM_OF_RECORDS.equals(importFileInDremioInfo.getErrorMessage())){
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.ERROR_IMPORT_FAILED_WRONG_NUM_OF_RECORDS, null);
                eventType = EventType.IMPORT_WRONG_NUM_OF_RECORDS_ERROR_EVENT;
            } else if(EEAErrorMessage.ERROR_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS.equals(importFileInDremioInfo.getErrorMessage())){
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.ERROR_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS, null);
                eventType = EventType.IMPORT_ONLY_READ_ONLY_FIELDS_ERROR_EVENT;
            } else if(EEAErrorMessage.ERROR_IMPORT_FAILED_READ_ONLY_TABLES.equals(importFileInDremioInfo.getErrorMessage())){
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.ERROR_IMPORT_FAILED_READ_ONLY_TABLES, null);
                eventType = EventType.IMPORT_READ_ONLY_TABLES_ERROR_EVENT;
            } else if(EEAErrorMessage.DREMIO_ENDPOINT_ERROR_RESPONSE.equals(importFileInDremioInfo.getErrorMessage())){
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.ERROR_DREMIO_ENDPOINT_RESPONSE, null);
                eventType = EventType.DREMIO_ENDPOINT_ERROR_RESPONSE;
            } else if(EEAErrorMessage.ERROR_IMPORT_FILES_CONTAIN_WRONG_HEADERS.equals(importFileInDremioInfo.getErrorMessage())){
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.ERROR_IMPORT_FILES_CONTAIN_WRONG_HEADERS, null);
                eventType = EventType.IMPORT_WRONG_HEADERS_ERROR_EVENT;
            }
            else {
                eventType = REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
                        ? EventType.IMPORT_REPORTING_DATASET_DATA_FAILED_EVENT
                        : EventType.IMPORT_DESIGN_DATASET_DATA_FAILED_EVENT;
            }

            if (!isPreparationDataset) {
                datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(),
                    DatasetRunningStatusEnum.ERROR_IN_IMPORT);
            }
            if(StringUtils.isNotBlank(importFileInDremioInfo.getProcessId())) {
                processControllerZuul.updateProcess(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getDataflowId(),
                        ProcessStatusEnum.CANCELED, ProcessTypeEnum.IMPORT, importFileInDremioInfo.getProcessId(),
                        SecurityContextHolder.getContext().getAuthentication().getName(), defaultImportProcessPriority, null);
            }

            jobStatus = JobStatusEnum.CANCELED;
        } else {
            if (!isPreparationDataset) {
                datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(),
                        DatasetRunningStatusEnum.IMPORTED);
            }

            if(StringUtils.isNotBlank(importFileInDremioInfo.getProcessId())) {
                processControllerZuul.updateProcess(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getDataflowId(),
                        ProcessStatusEnum.FINISHED, ProcessTypeEnum.IMPORT, importFileInDremioInfo.getProcessId(),
                        SecurityContextHolder.getContext().getAuthentication().getName(), defaultImportProcessPriority, null);
            }

            eventType = REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
                    ? EventType.IMPORT_REPORTING_COMPLETED_EVENT
                    : EventType.IMPORT_DESIGN_COMPLETED_EVENT;

            jobStatus = JobStatusEnum.FINISHED;

            // Delete the parquet files.
            deleteFilesFromDirectoryWithExtension(new String[]{".parquet"}, importFileInDremioInfo.getDatasetId().toString(), importFileInDremioInfo.getPreparationCode());
            // Delete the process generated csv files ending with a uuid
            deleteCsvFilesWithUuidSuffix(importFileInDremioInfo.getDatasetId().toString(), importFileInDremioInfo.getPreparationCode());
        }

        if (jobId!=null) {
            jobControllerZuul.updateJobStatus(jobId, jobStatus);
        }

        kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value, notificationVO);

        if(importFileInDremioInfo.getWarningMessages() != null && !importFileInDremioInfo.getWarningMessages().isEmpty()) {
            Set <String> warningMessages = new HashSet<>(importFileInDremioInfo.getWarningMessages());
            for(String warningMessage : warningMessages) {
                if(warningMessage.equals(JobInfoEnum.WARNING_SOME_FILENAMES_DO_NOT_MATCH_TABLES.getValue(null))) {
                    jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.WARNING_SOME_FILENAMES_DO_NOT_MATCH_TABLES, null);
                    NotificationVO notificationWarning = NotificationVO.builder()
                        .user(SecurityContextHolder.getContext().getAuthentication().getName())
                        .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
                    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_NAMEFILE_WARNING_EVENT,
                        value, notificationWarning);
                }
                if (warningMessage.equals(JobInfoEnum.WARNING_SOME_FILES_ARE_EMPTY.getValue(null))
                        && !EEAErrorMessage.ERROR_IMPORT_EMPTY_FILES.equals(importFileInDremioInfo.getErrorMessage())){
                    jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.WARNING_SOME_FILES_ARE_EMPTY, null);
                    NotificationVO notificationWarning = NotificationVO.builder()
                        .user(SecurityContextHolder.getContext().getAuthentication().getName())
                        .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
                    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_EMPTY_FILES_WARNING_EVENT,
                        value, notificationWarning);
                }
                if(warningMessage.equals(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA.getValue(null))
                        && !EEAErrorMessage.ERROR_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA.equals(importFileInDremioInfo.getErrorMessage())){
                    jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.WARNING_SOME_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA, null);
                    NotificationVO notificationWarning = NotificationVO.builder()
                        .user(SecurityContextHolder.getContext().getAuthentication().getName())
                        .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
                    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_FIXED_NUM_WITHOUT_REPLACE_DATA_WARNING_EVENT,
                        value, notificationWarning);
                }
                if(warningMessage.equals(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_WRONG_NUM_OF_RECORDS.getValue(null))
                        && !EEAErrorMessage.ERROR_IMPORT_FAILED_WRONG_NUM_OF_RECORDS.equals(importFileInDremioInfo.getErrorMessage())){
                    jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.WARNING_SOME_IMPORT_FAILED_WRONG_NUM_OF_RECORDS, null);
                    NotificationVO notificationWarning = NotificationVO.builder()
                        .user(SecurityContextHolder.getContext().getAuthentication().getName())
                        .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
                    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_WRONG_NUM_OF_RECORDS_WARNING_EVENT,
                        value, notificationWarning);
                }
                if(warningMessage.equals(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS.getValue(null))
                        && !EEAErrorMessage.ERROR_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS.equals(importFileInDremioInfo.getErrorMessage())){
                    jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.WARNING_SOME_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS, null);
                    NotificationVO notificationWarning = NotificationVO.builder()
                        .user(SecurityContextHolder.getContext().getAuthentication().getName())
                        .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
                    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_ONLY_READ_ONLY_FIELDS_WARNING_EVENT,
                        value, notificationWarning);
                }
                if(warningMessage.equals(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_READ_ONLY_TABLES.getValue(null))
                        && !EEAErrorMessage.ERROR_IMPORT_FAILED_READ_ONLY_TABLES.equals(importFileInDremioInfo.getErrorMessage())){
                    jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.WARNING_SOME_IMPORT_FAILED_READ_ONLY_TABLES, null);
                    NotificationVO notificationWarning = NotificationVO.builder()
                        .user(SecurityContextHolder.getContext().getAuthentication().getName())
                        .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
                    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_READ_ONLY_TABLES_WARNING_EVENT,
                        value, notificationWarning);
                }
                if(warningMessage.equals(JobInfoEnum.WARNING_SOME_IMPORT_MISMATCH_OF_DATA.getValue(null))){
                    jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.WARNING_SOME_IMPORT_MISMATCH_OF_DATA, null);
                    NotificationVO notificationWarning = NotificationVO.builder()
                        .user(SecurityContextHolder.getContext().getAuthentication().getName())
                        .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
                    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_MISMATCH_OF_DATA_WARNING_EVENT,
                        value, notificationWarning);
                }
                if(warningMessage.equals(JobInfoEnum.WARNING_SOME_IMPORT_FILES_CONTAIN_WRONG_HEADERS.getValue(null))
                        && !EEAErrorMessage.ERROR_IMPORT_FILES_CONTAIN_WRONG_HEADERS.equals(importFileInDremioInfo.getErrorMessage())){
                    jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.WARNING_SOME_IMPORT_FILES_CONTAIN_WRONG_HEADERS, null);
                    NotificationVO notificationWarning = NotificationVO.builder()
                            .user(SecurityContextHolder.getContext().getAuthentication().getName())
                            .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
                    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_WRONG_HEADERS_WARNING_EVENT,
                            value, notificationWarning);
                }
            }
        }
        if(!isPreparationDataset) {
            if (importFileInDremioInfo.getProviderId() != null) {
                fileTreatmentHelper.releaseLockReleasingProcess(importFileInDremioInfo.getDatasetId());
            }
        }
    }

    private void deleteFilesFromDirectoryWithExtension(String[] extensionsToDelete, String datasetId, String preparationCode){
        File folder;
        folder = resolveImportFolder(datasetId, preparationCode);
        Arrays.stream(folder.listFiles((f, p) -> StringUtils.endsWithAny(p, extensionsToDelete))).forEach(File::delete);
    }

    @Override
    public JobPresignedUrlInfo generateImportPreSignedUrl(Long datasetId, Long dataflowId, Long providerId, String fileName) {
        JobPresignedUrlInfo info = new JobPresignedUrlInfo();
        String filePathInS3 = getFilePath(datasetId, dataflowId, providerId, fileName, false);
        info.setFilePathInS3(filePathInS3);
        info.setPresignedUrl(s3HelperPublic.generatePUTPreSignedUrl(filePathInS3));
        return info;
    }

    @Override
    public String generateExportPreSignedUrl(Long datasetId, Long dataflowId, Long providerId, String fileName) {
        return s3HelperPublic.generateGETPreSignedUrl(getFilePath(datasetId, dataflowId, providerId, fileName, false));
    }

    @Override
    public void deleteTableData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId, Long jobId, Boolean createEmptyTablesBool) throws Exception {
        try {
            String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
            TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);
            if (tableSchemaVO != null && BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable())
                    && BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaId))) {
                throw new Exception("Can not delete table data because iceberg table is created");
            }
            String tableSchemaName = tableSchemaVO.getNameTableSchema();
            DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);

            if (providerId == null) {
                providerId = dataSetMetabaseVO.getDataProviderId();
            }
            if (providerId == null) {
                providerId = 0L;
            }
            S3PathResolver s3ImportPathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaName, tableSchemaName, S3_IMPORT_FILE_PATH);
            //path in s3 for the folder that contains the stored csv files
            String s3PathForCsvFolder = s3ServicePrivate.getTableAsFolderQueryPath(s3ImportPathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH);

            //remove csv files that are related to the table
            parquetConverterService.removeCsvFilesThatWillBeReplaced(s3ImportPathResolver, tableSchemaName, s3PathForCsvFolder, datasetId, dataSetMetabaseVO);

            S3PathResolver s3TablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaName, tableSchemaName, S3_TABLE_NAME_FOLDER_PATH);
            //remove folders that contain the previous parquet files
            if (s3HelperPrivate.checkFolderExist(s3TablePathResolver, S3_TABLE_NAME_FOLDER_PATH)) {
                //demote table folder
                dremioHelperService.demoteFolderOrFile(s3TablePathResolver, tableSchemaName);
                s3HelperPrivate.deleteFolder(s3TablePathResolver, S3_TABLE_NAME_FOLDER_PATH);
            }

            //delete attachments if they exist
            if (s3HelperPrivate.checkFolderExist(s3TablePathResolver, S3_ATTACHMENTS_TABLE_PATH)) {
                s3HelperPrivate.deleteFolder(s3TablePathResolver, S3_ATTACHMENTS_TABLE_PATH);
            }

            //if dataset is reference, remove the data from reference folder
            DatasetTypeEnum datasetType = datasetService.getDatasetType(datasetId);
            if (datasetType == DatasetTypeEnum.REFERENCE) {
                //demote reference table folder
                S3PathResolver s3ReferenceTablePathResolver = new S3PathResolver(s3TablePathResolver.getDataflowId(), s3TablePathResolver.getDataProviderId(), s3TablePathResolver.getDatasetId(), tableSchemaName, tableSchemaName, S3_DATAFLOW_REFERENCE_FOLDER_PATH);
                if (s3HelperPrivate.checkFolderExist(s3ReferenceTablePathResolver, S3_DATAFLOW_REFERENCE_FOLDER_PATH)) {
                    dremioHelperService.demoteFolderOrFile(s3ReferenceTablePathResolver, tableSchemaName);
                    //remove folders that contain the previous parquet files because data will be replaced
                    s3HelperPrivate.deleteFolder(s3ReferenceTablePathResolver, S3_DATAFLOW_REFERENCE_FOLDER_PATH);
                }
            }
            if(BooleanUtils.isTrue(createEmptyTablesBool)) {
                createEmptyTables.runCreationForSpecificTableSchema(dataSetMetabaseVO, tableSchemaId);
            }

            if (jobId != null) {
                jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FINISHED);

                // after the table has been deleted, an event is sent to notify it
                EventType eventType = REPORTING.equals(datasetService.getDatasetType(datasetId))
                        ? EventType.DELETE_TABLE_COMPLETED_EVENT
                        : EventType.DELETE_TABLE_SCHEMA_COMPLETED_EVENT;
                Map<String, Object> value = new HashMap<>();
                NotificationVO notificationVO = NotificationVO.builder()
                        .user(SecurityContextHolder.getContext().getAuthentication().getName()).datasetId(datasetId)
                        .tableSchemaId(tableSchemaId).build();
                notificationVO.setDatasetName(dataSetMetabaseVO.getDataSetName());
                notificationVO.setDataflowId(dataSetMetabaseVO.getDataflowId());
                notificationVO.setDataflowName(dataFlowControllerZuul.getMetabaseById(dataSetMetabaseVO.getDataflowId()).getName());

                value.put(LiteralConstants.DATASET_ID, datasetId);

                try {
                    kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value, notificationVO);
                } catch (EEAException e) {
                    LOG.error("Error releasing notification for datasetId {} and tableSchemaId {} Message: {}", datasetId, tableSchemaId, e.getMessage(), e);
                }
            }

        }
        catch (Exception e){
            if (jobId != null) {
                jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);
            }
            throw e;
        }
    }

    @Override
    public void deleteDatasetData(Long datasetId, Long dataflowId, Long providerId, Boolean deletePrefilledTables, Boolean technicallyAccepted, Long jobId) throws Exception {

        try {
            String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
            List<TableSchemaIdNameVO> tableSchemas = datasetSchemaService.getTableSchemasIds(datasetId);
            for (TableSchemaIdNameVO entry : tableSchemas) {
                TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(entry.getIdTableSchema(), datasetSchemaId);
                if (tableSchemaVO != null && BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable())
                        && BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaVO.getIdTableSchema()))) {
                    throw new Exception("Can not delete table data because iceberg table is created");
                }
            }

            DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
            if (providerId == null) {
                providerId = dataSetMetabaseVO.getDataProviderId();
            }
            List<TableSchemaIdNameVO> tableSchemaIdNameVOs = datasetSchemaService.getTableSchemasIds(datasetId);
            for (TableSchemaIdNameVO tableSchemaIdNameVO : tableSchemaIdNameVOs) {
                if (!deletePrefilledTables) {
                    //do not delete prefilled tables
                    TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaIdNameVO.getIdTableSchema(), dataSetMetabaseVO.getDatasetSchema());
                    if (tableSchemaVO.getToPrefill()) {
                        LOG.info("The data for table with tableSchemaId {} for datasetId {} will not be deleted because the table is prefilled.", tableSchemaIdNameVO.getIdTableSchema(), datasetId);
                        continue;
                    }
                }
                //we do not pass a job id because there is a job for the whole dataset data deletion
                deleteTableData(datasetId, dataflowId, providerId, tableSchemaIdNameVO.getIdTableSchema(), null, true);
            }

            if (jobId != null) {
                jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FINISHED);

                EventType eventType = REPORTING.equals(datasetService.getDatasetType(datasetId))
                        ? EventType.DELETE_DATASET_DATA_COMPLETED_EVENT
                        : EventType.DELETE_DATASET_SCHEMA_COMPLETED_EVENT;

                if (!technicallyAccepted) {
                    // after the dataset values have been deleted, an event is sent to notify it
                    Map<String, Object> value = new HashMap<>();
                    NotificationVO notificationVO = NotificationVO.builder()
                            .user(SecurityContextHolder.getContext().getAuthentication().getName())
                            .datasetId(datasetId).build();
                    DataSetMetabaseVO datasetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
                    notificationVO.setDatasetName(datasetMetabaseVO.getDataSetName());
                    notificationVO.setDataflowId(datasetMetabaseVO.getDataflowId());
                    notificationVO.setDataflowName(dataFlowControllerZuul.getMetabaseById(datasetMetabaseVO.getDataflowId()).getName());

                    value.put(LiteralConstants.DATASET_ID, datasetId);

                    try {
                        kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value, notificationVO);
                    } catch (EEAException e) {
                        LOG.error("Error releasing notification for datasetId {} Message: {}", datasetId, e.getMessage());
                    }
                }
            }
        }
        catch (Exception e){
            if (jobId != null) {
                jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);
            }
            throw e;
        }
    }

    private String getFilePath(Long datasetId, Long dataflowId, Long providerId, String fileName, boolean deleteFile) {
        if (dataflowId == null){
            dataflowId = datasetService.getDataFlowIdById(datasetId);
        }
        if (providerId == null){
            providerId = 0L;
        }
        S3PathResolver s3PathResolver = new S3PathResolver(dataflowId, providerId, datasetId, null, fileName);
        s3PathResolver.setPath(LiteralConstants.S3_PROVIDER_IMPORT_PATH);
        s3PathResolver.setDeleteFile(deleteFile);
        return s3ServicePublic.getS3Path(s3PathResolver);
    }

    /**
     * Gets the attachment for big data dataflows.
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataset id
     * @param providerId the dataset id
     * @param tableSchemaName the table name
     * @param fieldName the field name
     * @param fileName the file name
     * @param recordId the recordId
     * @return the attachment
     *
     */
    @SneakyThrows
    @Override
    public AttachmentDLVO getAttachmentDL(Long datasetId, Long dataflowId, Long providerId, String tableSchemaName,
                                          String fieldName, String fileName, String recordId, String dataProviderCode) {

        String fileNameInS3 = fieldName + "_" + recordId + "." + FilenameUtils.getExtension(fileName);
        DatasetTypeEnum datasetType = datasetMetabaseService.getDatasetType(datasetId);
        S3PathResolver s3PathResolver = new S3PathResolver(dataflowId, (providerId != null)? providerId : 0L, datasetId, tableSchemaName, fileNameInS3);
        DataProviderVO providerVO = null;
        if(StringUtils.isNotBlank(dataProviderCode)) {
            Long dataProviderGroupId = dataFlowControllerZuul.findDataProviderGroupIdById(dataflowId);
            providerVO = representativeControllerZuul.findDataProviderByCodeAndGroupId(dataProviderCode, dataProviderGroupId);
        }
        if(datasetType.equals(DatasetTypeEnum.COLLECTION)){
            //get correct providerId
            if(providerVO != null) {
                s3PathResolver.setDataProviderId(providerVO.getId());
            }
            s3PathResolver.setPath(S3_ATTACHMENTS_DC_PATH);
        }
        else if(datasetType.equals(DatasetTypeEnum.EUDATASET)){
            //get correct providerId
            if(providerVO != null) {
                s3PathResolver.setDataProviderId(providerVO.getId());
            }
            s3PathResolver.setPath(S3_ATTACHMENTS_EU_PATH);
        }
        else{
            s3PathResolver.setPath(S3_ATTACHMENTS_PATH);
        }
        byte[] attachmentContent;
        String attachmentPathInS3 = s3ServicePrivate.getS3Path(s3PathResolver);
        try {
            //retrieve file from s3
            File attachmentInS3 = s3HelperPrivate.getFileFromS3(attachmentPathInS3, fileName, importPath, null);
            attachmentContent = FileUtils.readFileToByteArray(attachmentInS3);
        } catch (Exception e) {
            LOG.error("Could not retrieve file {} from s3 {}", attachmentPathInS3, e.getMessage());
            throw e;
        }
        AttachmentDLVO attachmentDLVO = new AttachmentDLVO(fileName, attachmentContent);
        return attachmentDLVO;
    }

    /**
     * Delete attachment for big data dataflows.
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataset id
     * @param providerId the dataset id
     * @param tableSchemaName the table name
     * @param fieldName the field name
     * @param fileName the file name
     * @param recordId the recordId
     *
     * @throws EEAException the EEA exception
     */
    @SneakyThrows
    @Override
    public void deleteAttachmentDL(@DatasetId Long datasetId, Long dataflowId, Long providerId, String tableSchemaName,
                                   String fieldName, String fileName, String recordId) {
        providerId = providerId != null ? providerId : 0L;
        S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaName, tableSchemaName, S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3IcebergTablePathResolver.setIsIcebergTable(true);
        String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

        //update attachment file name in attachment field
        String updateFileNameColumn = "UPDATE " + icebergTablePath + " SET " + fieldName + "=''"
                + " WHERE " + PARQUET_RECORD_ID_COLUMN_HEADER + "='" + recordId + "'";
        String processId = dremioHelperService.executeSqlStatement(updateFileNameColumn);
        dremioHelperService.checkIfDremioProcessFinishedSuccessfully(updateFileNameColumn, processId, 2000L);

        //remove attachment file from s3
        removeAttachmentFromS3(dataflowId, providerId, datasetId, tableSchemaName, fieldName, FilenameUtils.getExtension(fileName), recordId);
    }

    /**
     * Update attachment for big data dataflows.
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataset id
     * @param providerId the dataset id
     * @param tableSchemaName the table name
     * @param fieldName the field name
     * @param multipartFile the file
     * @param recordId the recordId
     */
    @SneakyThrows
    @Override
    public void updateAttachmentDL(@DatasetId Long datasetId, Long dataflowId, Long providerId, String tableSchemaName,
                                   String fieldName, MultipartFile multipartFile, String recordId, String previousFileName){

        providerId = providerId != null ? providerId : 0L;
        S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaName, tableSchemaName, S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3IcebergTablePathResolver.setIsIcebergTable(true);

        String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

        //delete previous file if it exists
        if(previousFileName != null) {
            deleteAttachmentDL(datasetId, dataflowId, providerId, tableSchemaName, fieldName, previousFileName, recordId);
        }

        //update attachment file name in attachment field
        String updateFileNameColumn = "UPDATE " + icebergTablePath + " SET " + fieldName + "='" + multipartFile.getOriginalFilename()
                + "' WHERE " + PARQUET_RECORD_ID_COLUMN_HEADER + "='" + recordId + "'";
        String processId = dremioHelperService.executeSqlStatement(updateFileNameColumn);
        dremioHelperService.checkIfDremioProcessFinishedSuccessfully(updateFileNameColumn, processId, 2000L);

        File folder = new File(importPath + "/" + datasetId);
        if (!folder.exists()) {
            folder.mkdir();
        }
        String filePathInReportnet = folder.getAbsolutePath() + "/" + multipartFile.getOriginalFilename();
        File file = new File(filePathInReportnet);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            FileCopyUtils.copy(multipartFile.getInputStream(), fos);
        }
        catch (Exception e){
            LOG.error("Could not store file to disk for datasetId {} table {} and fileName {}", datasetId, tableSchemaName, file.getName());
            throw e;
        }

        String fileNameInS3 = fieldName + "_" + recordId + "." + FilenameUtils.getExtension(file.getName());
        S3PathResolver s3AttachmentsPathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaName, fileNameInS3, S3_ATTACHMENTS_PATH);
        String attachmentPathInS3 = s3ServicePrivate.getS3Path(s3AttachmentsPathResolver);
        s3HelperPrivate.uploadFileToBucket(attachmentPathInS3, file.getAbsolutePath());
        file.delete();
        LOG.info("Updated dl attachment for datasetId {}, table {} and field {}", datasetId, tableSchemaName, fieldName);
    }

    @Async
    @Override
    public void convertParquetToIcebergTables(Long datasetId, Long dataflowId, Long providerId, List<String> tableSchemaIds, String user, String lockValue) throws Exception{
        String datasetName = null;
        try {
            LOG.info("Converting parquet to iceberg tables for dataflowId {}, datasetId {} providerId {} and tableSchemaIds {} LockValue {}", dataflowId, datasetId, providerId, tableSchemaIds, lockValue);
            DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
            datasetName = dataSetMetabaseVO.getDataSetName();
            String datasetSchemaId = dataSetMetabaseVO.getDatasetSchema();

            List<TableSchemaVO> availableForConversionTables = new ArrayList<>();

            for (String tableSchemaId : tableSchemaIds) {
                TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);

                if (tableSchemaVO != null) {
                    Boolean availableForConversion = convertParquetToIcebergTable(datasetId, dataflowId, providerId, tableSchemaVO, datasetSchemaId, lockValue);
                    if(BooleanUtils.isTrue(availableForConversion)){
                        availableForConversionTables.add(tableSchemaVO);
                    }
                } else {
                    LOG.error("TableSchemaVO not found for tableSchemaId: {}", tableSchemaId);
                }
            }

            //iceberg enabled should be updated to true at the end of the conversion to ensure that all available tables were converted.
            for (TableSchemaVO table : availableForConversionTables) {
                DatasetTable datasetTableEntry = new DatasetTable(datasetId, datasetSchemaId, table.getIdTableSchema(), true, user);
                datasetTableService.saveOrUpdateDatasetTableEntry(datasetTableEntry);
            }

            String lockKey = LockEnum.PARQUET_CONVERSION.getValue() + "_" + datasetId;
            redisLockService.releaseLock(lockKey, lockValue);
            LOG.info("Released lock {} with value {}", lockKey, lockValue);

            // Notify completion event
            kafkaSenderUtils.releaseNotificableKafkaEvent(
                    EventType.PARQUET_TO_ICEBERG_CONVERSION_COMPLETED_EVENT,
                    null,
                    NotificationVO.builder()
                            .user(user)
                            .dataflowId(dataflowId)
                            .datasetId(datasetId)
                            .providerId(providerId)
                            .datasetName(datasetName)
                            .build()
            );

            LOG.info("Successfully completed Parquet to Iceberg conversion for datasetId: {} and user {}", datasetId, user);

        } catch (Exception e) {
            LOG.error("Error processing Kafka event for converting Parquet to Iceberg for datasetId: {} and user {} : {}", datasetId, user, e.getMessage());

            String lockKey = LockEnum.PARQUET_CONVERSION.getValue() + "_" + datasetId;
            redisLockService.releaseLock(lockKey, lockValue);
            LOG.info("Released lock {} with value {}", lockKey, lockValue);

            // Notify failure event
            kafkaSenderUtils.releaseNotificableKafkaEvent(
                    EventType.PARQUET_TO_ICEBERG_CONVERSION_FAILED_EVENT,
                    null,
                    NotificationVO.builder()
                            .user(user)
                            .dataflowId(dataflowId)
                            .datasetId(datasetId)
                            .datasetName(datasetName)
                            .build()
            );
            throw new EEAException(e.getMessage());
        }
    }

    @Override
    public Boolean convertParquetToIcebergTable(Long datasetId, Long dataflowId, Long providerId, TableSchemaVO tableSchemaVO, String datasetSchemaId, String lockValue) throws Exception {
        if(providerId == null) {
            providerId = datasetService.getDataProviderIdById(datasetId);
        }
        providerId = providerId != null ? providerId : 0L;

        if(tableSchemaVO == null || !BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable()) || BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaVO.getIdTableSchema()))) {
            LOG.info("Can not convert parquet to iceberg table for dataflowId {}, providerId {}, datasetId {} and tableSchemaId {} " +
                    "because table data are not manually editable or the parquet table has not been created. LockValue: {}", dataflowId, providerId, datasetId, tableSchemaVO.getIdTableSchema(), lockValue);
            return false;
        }

        DatasetTypeEnum datasetType = datasetMetabaseService.getDatasetType(datasetId);
        String parquetTableQueryPathConstant = (datasetType == DatasetTypeEnum.REFERENCE ) ? S3_DATAFLOW_REFERENCE_QUERY_PATH : S3_TABLE_AS_FOLDER_QUERY_PATH;
        String parquetTableS3PathConstant = (datasetType == DatasetTypeEnum.REFERENCE ) ? S3_DATAFLOW_REFERENCE_FOLDER_PATH : S3_TABLE_NAME_FOLDER_PATH_FOR_VALID_PREFIX;

        S3PathResolver s3TablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), tableSchemaVO.getNameTableSchema(), parquetTableS3PathConstant);
        S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), tableSchemaVO.getNameTableSchema(), S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3IcebergTablePathResolver.setIsIcebergTable(true);
        String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

        s3TablePathResolver.setPath(parquetTableS3PathConstant);
        String parquetTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3TablePathResolver, parquetTableQueryPathConstant);

        Boolean parquetFolderExists = s3HelperPrivate.checkFolderExist(s3TablePathResolver, parquetTableS3PathConstant);
        Boolean parquetFolderIsPromoted = parquetFolderExists ? dremioHelperService.checkFolderPromoted(s3TablePathResolver, tableSchemaVO.getNameTableSchema()) : false;

        if(parquetFolderExists && !parquetFolderIsPromoted){
            //try to promote parquet table. If table can not be promoted, stop the process.
            try {
                LOG.info("Parquet table {} is not promoted. Will try to promote it. LockValue: {}", parquetTablePath, lockValue);
                dremioHelperService.refreshTableMetadataAndPromote(null, parquetTablePath, s3TablePathResolver, tableSchemaVO.getNameTableSchema());
                parquetFolderIsPromoted = dremioHelperService.checkFolderPromoted(s3TablePathResolver, tableSchemaVO.getNameTableSchema());
                if(!parquetFolderIsPromoted){
                    throw new Exception("Promoting table failed");
                }
            }
            catch (Exception e){
                LOG.error("Could not promote parquet table {}. LockValue: {} Error: {}", parquetTablePath, lockValue, e.getMessage());
                throw new Exception("Parquet table " + parquetTablePath + " is not promoted");
            }
        }

        //remove old iceberg table because it will be recreated
        if (s3HelperPrivate.checkFolderExist(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH_FOR_VALID_PREFIX)) {
            LOG.info("Removing iceberg files for table in path {} LockValue: {}", icebergTablePath, lockValue);
            dremioHelperService.demoteFolderOrFile(s3IcebergTablePathResolver, tableSchemaVO.getNameTableSchema());
            s3HelperPrivate.deleteFolder(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH_FOR_VALID_PREFIX);
        }

        String numberOfRecordsInParquetTableQuery = "SELECT COUNT (*) FROM " + parquetTablePath;

        //if table does not exist or has 0 records do not do anything
        if (!parquetFolderExists  || dremioJdbcTemplate.queryForObject(numberOfRecordsInParquetTableQuery, Long.class) == 0) {
            //parquet table does not exist and no iceberg table should be created
            LOG.info("For dataflowId {}, providerId {}, datasetId {} and table {} parquet table does not exist or has 0 records so no iceberg table will be created. LockValue: {}", dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), lockValue);
            return true;
        }

        dremioHelperService.createTableFromAnotherTable(parquetTablePath, icebergTablePath);
        //refresh the metadata
        dremioHelperService.refreshTableMetadataAndPromote(null, icebergTablePath, s3IcebergTablePathResolver, tableSchemaVO.getNameTableSchema());

        //check iceberg table was created successfully and has the same number of records as the parquet table
        try {
            Long numberOfRecords = dremioHelperService.compareNumberOfRecords(icebergTablePath, parquetTablePath);
            LOG.info("Iceberg table {} has been created successfully from parquet table {} Number of records is {}. LockValue: {}", icebergTablePath, parquetTablePath, numberOfRecords, lockValue);
        }
        catch (Exception e){
            LOG.info("Iceberg table {} has not been created successfully from parquet table {}. LockValue: {}", icebergTablePath, parquetTablePath, lockValue);
            throw e;
        }
        return true;
    }

    @Async
    @Override
    public void convertIcebergToParquetTables(Long datasetId, Long dataflowId, Long providerId, List<String> tableSchemaIds, String user, String lockValue) throws Exception{
        String datasetName = null;
        try {
            LOG.info("Converting iceberg to parquet tables for dataflowId {}, datasetId {} providerId {} and tableSchemaIds {} LockValue {}", dataflowId, datasetId, providerId, tableSchemaIds, lockValue);
            DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
            datasetName = dataSetMetabaseVO.getDataSetName();
            String datasetSchemaId = dataSetMetabaseVO.getDatasetSchema();

            List<TableSchemaVO> availableForConversionTables = new ArrayList<>();

            for (String tableSchemaId : tableSchemaIds) {
                TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);

                if (tableSchemaVO != null) {
                    Boolean availableForConversion = convertIcebergToParquetTable(datasetId, dataflowId, providerId, tableSchemaVO, datasetSchemaId, lockValue);
                    if(BooleanUtils.isTrue(availableForConversion)){
                        availableForConversionTables.add(tableSchemaVO);
                    }
                } else {
                    LOG.error("TableSchemaVO not found for tableSchemaId: {}", tableSchemaId);
                }
            }

            //iceberg enabled should be updated to false at the end iceberg files should be deleted also at the end of the conversion to ensure that all available tables were converted.
            for (TableSchemaVO table : availableForConversionTables) {
                Long usedProviderId = (providerId != null) ? providerId : 0L;
                S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(dataflowId, usedProviderId, datasetId, table.getNameTableSchema(), table.getNameTableSchema(), S3_TABLE_AS_FOLDER_QUERY_PATH);
                s3IcebergTablePathResolver.setIsIcebergTable(true);
                String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

                //remove iceberg table
                LOG.info("Removing iceberg files for table in path {}", icebergTablePath);
                if (s3HelperPrivate.checkFolderExist(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH_FOR_VALID_PREFIX)){
                    dremioHelperService.demoteFolderOrFile(s3IcebergTablePathResolver, table.getNameTableSchema());
                    s3HelperPrivate.deleteFolder(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH_FOR_VALID_PREFIX);
                }

                DatasetTable datasetTableEntry = new DatasetTable(datasetId, datasetSchemaId, table.getIdTableSchema(), false, null);
                datasetTableService.saveOrUpdateDatasetTableEntry(datasetTableEntry);
            }

            String lockKey = LockEnum.PARQUET_CONVERSION.getValue() + "_" + datasetId;
            redisLockService.releaseLock(lockKey, lockValue);
            LOG.info("Released lock {} with value {}", lockKey, lockValue);

            kafkaSenderUtils.releaseNotificableKafkaEvent(
                    EventType.ICEBERG_TO_PARQUET_CONVERSION_COMPLETED_EVENT,
                    null,
                    NotificationVO.builder()
                            .user(user)
                            .dataflowId(dataflowId)
                            .datasetId(datasetId)
                            .providerId(providerId)
                            .datasetName(datasetName)
                            .build()
            );

            LOG.info("Successfully completed Iceberg to Parquet conversion for datasetId: {} and user {}", datasetId, user);


        } catch (Exception e) {
            LOG.error("Error processing Kafka event for converting Iceberg to Parquet for datasetId: {} and user {} : {}", datasetId, user, e.getMessage());

            String lockKey = LockEnum.PARQUET_CONVERSION.getValue() + "_" + datasetId;
            redisLockService.releaseLock(lockKey, lockValue);
            LOG.info("Released lock {} with value {}", lockKey, lockValue);

            kafkaSenderUtils.releaseNotificableKafkaEvent(
                    EventType.ICEBERG_TO_PARQUET_CONVERSION_FAILED_EVENT,
                    null,
                    NotificationVO.builder()
                            .user(user)
                            .dataflowId(dataflowId)
                            .datasetId(datasetId)
                            .datasetName(datasetName)
                            .build()
            );

            throw new EEAException(e.getMessage());
        }
    }

    @Override
    public Boolean convertIcebergToParquetTable(Long datasetId, Long dataflowId, Long providerId, TableSchemaVO tableSchemaVO, String datasetSchemaId, String lockValue) throws Exception {
        if(providerId == null) {
            providerId = datasetService.getDataProviderIdById(datasetId);
        }
        providerId = providerId != null ? providerId : 0L;

        if(tableSchemaVO == null || !BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable()) || !BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaVO.getIdTableSchema()))) {
            LOG.info("Can not convert iceberg table to parquet for dataflowId {}, providerId {}, datasetId {} and tableSchemaId {} " +
                    "because table data are not manually editable or the iceberg table has not been created. LockValue: {}", dataflowId, providerId, datasetId, tableSchemaVO.getIdTableSchema(), lockValue);
            return false;
        }

        DatasetTypeEnum datasetType = datasetMetabaseService.getDatasetType(datasetId);

        S3PathResolver s3TablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), UUID.randomUUID().toString(), S3_TABLE_AS_FOLDER_QUERY_PATH);
        S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), tableSchemaVO.getNameTableSchema(), S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3IcebergTablePathResolver.setIsIcebergTable(true);

        String parquetTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3TablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
        String parquetInnerFolderQueryPath = parquetTablePath + ".\"" + s3TablePathResolver.getFilename() + "\"";
        String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

        Boolean icebergFolderExists = s3HelperPrivate.checkFolderExist(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH_FOR_VALID_PREFIX);
        Boolean icebergFolderIsPromoted = icebergFolderExists ? dremioHelperService.checkFolderPromoted(s3IcebergTablePathResolver, tableSchemaVO.getNameTableSchema()) : false;

        if(icebergFolderExists && !icebergFolderIsPromoted){
            //try to promote iceberg table. If table can not be promoted, stop the process.
            try {
                LOG.info("Iceberg table {} is not promoted. Will try to promote it. LockValue: {}", icebergTablePath, lockValue);
                dremioHelperService.refreshTableMetadataAndPromote(null, icebergTablePath, s3IcebergTablePathResolver, tableSchemaVO.getNameTableSchema());
                icebergFolderIsPromoted = dremioHelperService.checkFolderPromoted(s3IcebergTablePathResolver, tableSchemaVO.getNameTableSchema());
                if(!icebergFolderIsPromoted){
                    throw new Exception("Promoting table failed");
                }
            }
            catch (Exception e){
                LOG.error("Could not promote iceberg table {}. LockValue: {} Error: {}", icebergTablePath, lockValue, e.getMessage());
                throw new Exception("Iceberg table " + icebergTablePath + " is not promoted");
            }
        }


        if (s3HelperPrivate.checkFolderExist(s3TablePathResolver, S3_TABLE_NAME_FOLDER_PATH_FOR_VALID_PREFIX)) {
            //remove old parquet table because it will be recreated
            dremioHelperService.demoteFolderOrFile(s3TablePathResolver, tableSchemaVO.getNameTableSchema());
            LOG.info("Removing parquet files for table in path {} LockValue: {}", parquetTablePath, lockValue);
            s3HelperPrivate.deleteFolder(s3TablePathResolver, S3_TABLE_NAME_FOLDER_PATH_FOR_VALID_PREFIX);
        }

        String numberOfRecordsInIcebergTableQuery = "SELECT COUNT (*) FROM " + icebergTablePath;

        //if table does not exist or has 0 records do not do anything
        if (!icebergFolderExists || dremioJdbcTemplate.queryForObject(numberOfRecordsInIcebergTableQuery, Long.class) == 0) {
            //iceberg table does not exist and no parquet table should be created
            LOG.info("For dataflowId {}, providerId {}, datasetId {} and table {} iceberg table does not exist or has 0 records so creating empty table. LockValue: {}", dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), lockValue);
            // In #297461 empty tables in parquet bucket should always exist so recreating it
            DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
            createEmptyTables.runCreationForSpecificTableSchema(dataSetMetabaseVO, tableSchemaVO.getIdTableSchema());
            return true;
        }

        dremioHelperService.createTableFromAnotherTable(icebergTablePath, parquetInnerFolderQueryPath);
        //refresh the metadata
        dremioHelperService.refreshTableMetadataAndPromote(null, parquetTablePath, s3TablePathResolver, tableSchemaVO.getNameTableSchema());

        if(datasetType == DatasetTypeEnum.REFERENCE){
            s3TablePathResolver.setPath(S3_TABLE_NAME_FOLDER_PATH_FOR_VALID_PREFIX);
            createReferenceFolder(s3TablePathResolver);
        }

        //check parquet table was created successfully and has the same number of records as the iceberg table
        try {
            Long numberOfRecords = dremioHelperService.compareNumberOfRecords(parquetTablePath, icebergTablePath);
            LOG.info("Parquet table {} has been created successfully from iceberg table {} Number of records is {}. LockValue: {}", parquetTablePath, icebergTablePath, numberOfRecords, lockValue);
        }
        catch (Exception e){
            LOG.info("Parquet table {} has not been created successfully from iceberg table {}. LockValue: {}", parquetTablePath, icebergTablePath, lockValue);
            throw e;
        }

        return true;
    }

    @Override
    public void initiateParquetToIcebergConversion(Long datasetId, Long dataflowId, Long providerId, List<String> tableSchemaIds, String lockValue) throws Exception {
        if (tableSchemaIds == null || tableSchemaIds.isEmpty()) {
            List<TableSchemaIdNameVO> tableSchemas = datasetSchemaService.getTableSchemasIds(datasetId);
            tableSchemaIds = tableSchemas.stream().map(TableSchemaIdNameVO::getIdTableSchema).collect(Collectors.toList());
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("datasetId", datasetId);
        eventData.put("dataflowId", dataflowId);
        eventData.put("providerId", providerId);
        eventData.put("tableSchemaIds", tableSchemaIds);
        eventData.put("lockValue", lockValue);

        kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_PARQUET_TO_ICEBERG_CONVERSION, eventData);

        LOG.info("Triggered Kafka event for Parquet to Iceberg conversion for dataflowId {}, datasetId {} providerId {} and tableSchemaIds {} LockValue {}", dataflowId, datasetId, providerId, tableSchemaIds, lockValue);
    }

    @Override
    public void initiateIcebergToParquetConversion(Long datasetId, Long dataflowId, Long providerId, List<String> tableSchemaIds, String lockValue) throws Exception {
        if (tableSchemaIds == null || tableSchemaIds.isEmpty()) {
            List<TableSchemaIdNameVO> tableSchemas = datasetSchemaService.getTableSchemasIds(datasetId);
            tableSchemaIds = tableSchemas.stream().map(TableSchemaIdNameVO::getIdTableSchema).collect(Collectors.toList());
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("datasetId", datasetId);
        eventData.put("dataflowId", dataflowId);
        eventData.put("providerId", providerId);
        eventData.put("tableSchemaIds", tableSchemaIds);
        eventData.put("lockValue", lockValue);

        kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_ICEBERG_TO_PARQUET_CONVERSION, eventData);

        LOG.info("Triggered Kafka event for Iceberg to Parquet conversion for dataflowId {}, datasetId {} providerId {} and tableSchemaIds {} LockValue {}", dataflowId, datasetId, providerId, tableSchemaIds, lockValue);
    }

    @Override
    public void insertRecords(Long dataflowId, Long providerId, Long datasetId, String tableSchemaName, List<RecordVO> records) throws Exception{

        if(records.size() == 0){
            return;
        }

        providerId = providerId != null ? providerId : 0L;
        S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaName, tableSchemaName, S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3IcebergTablePathResolver.setIsIcebergTable(true);
        String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

        String dataProviderCode = "''";
        if(providerId != 0L) {
            DataProviderVO dataProviderVO = representativeControllerZuul.findDataProviderById(providerId);
            dataProviderCode = (dataProviderVO.getCode() != null) ? "'" + dataProviderVO.getCode() + "'" : dataProviderCode;
        }

        if (datasetMetabaseService.getDatasetType(datasetId).equals(DatasetTypeEnum.DESIGN)) {
            createEmptyTables.deleteTableIfEmpty(tableSchemaName, s3IcebergTablePathResolver);
        }

        //check if table exists and if not create it
        if (!s3HelperPrivate.checkFolderExist(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH) || !dremioHelperService.checkFolderPromoted(s3IcebergTablePathResolver, tableSchemaName)) {
            //table does not exist, so we need to create it first
            StringBuilder createIcebergTable = new StringBuilder("CREATE TABLE IF NOT EXISTS " + icebergTablePath + " (");
            String fieldNames = PARQUET_RECORD_ID_COLUMN_HEADER + "," + PARQUET_PROVIDER_CODE_COLUMN_HEADER;
            String fieldsWithTypes = Arrays.stream(fieldNames.split(","))
                .map(String::trim)
                .filter(field -> !field.isBlank())
                .map(field -> UtilityClass.addQuotesToFieldNames(field) + " VARCHAR")
                .collect(Collectors.joining(", "));

            createIcebergTable.append(fieldsWithTypes);

            for (int i = 0; i < records.get(0).getFields().size(); i++) {
                FieldVO field = records.get(0).getFields().get(i);
                createIcebergTable.append(", ").append(UtilityClass.addQuotesToFieldNames(field.getName()));

                if (spatialDataHandling.getGeoJsonEnums().contains(field.getType())) {
                    createIcebergTable.append("VARBINARY");
                } else {
                    createIcebergTable.append("VARCHAR");
                }
            }

            createIcebergTable.append(" )");

            String createIcebergTableProcessId = dremioHelperService.executeSqlStatement(createIcebergTable.toString());
            dremioHelperService.checkIfDremioProcessFinishedSuccessfully(createIcebergTable.toString(), createIcebergTableProcessId, null);
        }

        for (RecordVO record : records) {
            StringBuilder insertQueryBuilder = new StringBuilder().append("INSERT INTO ").append(icebergTablePath).append(" (");
            String fieldNames = PARQUET_RECORD_ID_COLUMN_HEADER + "," + PARQUET_PROVIDER_CODE_COLUMN_HEADER;
            insertQueryBuilder.append(UtilityClass.addQuotesToFieldNames(fieldNames));

            String recordId = UUID.randomUUID().toString();
            StringBuilder insertQueryValuesBuilder = new StringBuilder().append(") VALUES ('").append(recordId).append("', ").append(dataProviderCode);

            for (int i = 0; i < record.getFields().size(); i++) {
                FieldVO field = record.getFields().get(i);
                // Wrap the field name in double quotes
                insertQueryBuilder.append(", ").append(UtilityClass.addQuotesToFieldNames(field.getName()));

                if (spatialDataHandling.getGeoJsonEnums().contains(field.getType())) {
                    String fieldValue = (field.getValue() != null) ? field.getValue() : "";
                    String refactoredValue = spatialDataHandling.refactorQuery(fieldValue, i);
                    insertQueryValuesBuilder.append(", ").append(refactoredValue);
                } else {
                    String fieldValue = "";
                    if(BooleanUtils.isTrue(field.getAutoIncrement())){
                        //set up autoincrement value
                        String escapedFieldName = UtilityClass.addQuotesToFieldNames(field.getName());
                        String getPreviousMaxFieldValueQuery =  "SELECT CAST( " + escapedFieldName + "  AS BIGINT) AS numeric_value FROM " + icebergTablePath
                                + " WHERE " + escapedFieldName + " IS NOT NULL AND TRIM(" + escapedFieldName + ") <> '' ORDER BY numeric_value DESC LIMIT 1";

                        String previousMaxFieldValue = dremioJdbcTemplate.query(getPreviousMaxFieldValueQuery, (rs, rowNum) -> rs.getString(1)).stream().findFirst().orElse(null);
                        Long autoIncrementValue = (StringUtils.isNotBlank(previousMaxFieldValue)) ? Long.valueOf(previousMaxFieldValue) + 1 : 1L;
                        fieldValue = String.valueOf(autoIncrementValue);
                        insertQueryValuesBuilder.append(", '").append(fieldValue).append("'");
                    }
                    else{
                        if (field.getValue() != null) {
                            fieldValue = field.getValue().replace("'", "''");
                            if (fieldValue.matches(".*[^\u0000-\u007F].*")) {
                                fieldValue = "ENCODE('" + fieldValue + "', 'UTF-8')";
                                insertQueryValuesBuilder.append(", ").append(fieldValue);
                            } else {
                                insertQueryValuesBuilder.append(", '").append(fieldValue).append("'");
                            }
                        } else {
                            insertQueryValuesBuilder.append(", '").append(fieldValue).append("'");
                        }
                    }

                }
            }

            insertQueryValuesBuilder.append(" )");
            String finalInsertQuery = insertQueryBuilder + insertQueryValuesBuilder.toString();

            // Execute the query
            String processId = dremioHelperService.executeSqlStatement(finalInsertQuery);
            dremioHelperService.checkIfDremioProcessFinishedSuccessfully(finalInsertQuery, processId, 2000L);
        }
    }


    @Override
    public void updateRecords(Long dataflowId, Long providerId, Long datasetId, TableSchemaVO tableSchemaVO, List<RecordVO> records, boolean updateCascadePK) throws Exception {
        providerId = providerId != null ? providerId : 0L;
        S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), tableSchemaVO.getNameTableSchema(), S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3IcebergTablePathResolver.setIsIcebergTable(true);

        String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

        for (RecordVO record : records) {
            // Create update query for the record
            StringBuilder updateQueryBuilder = new StringBuilder().append("UPDATE ").append(icebergTablePath).append(" SET ");
            for (int i = 0; i < record.getFields().size(); i++) {
                FieldVO field = record.getFields().get(i);
                if (spatialDataHandling.getGeoJsonEnums().contains(field.getType()) && !field.getType().equals(DataType.POINT)) {
                    continue;
                }

                // Wrap field name in double quotes
                String fieldName = UtilityClass.addQuotesToFieldNames(field.getName());
                String fieldValue = (field.getValue() != null) ? field.getValue().replace("'", "''") : "";
                updateQueryBuilder.append(fieldName).append(" = '").append(fieldValue).append("'");

                if (i != record.getFields().size() - 1) {
                    updateQueryBuilder.append(", ");
                }
            }

            // Remove trailing comma, if any
            if (updateQueryBuilder.length() >= 2 && updateQueryBuilder.substring(updateQueryBuilder.length() - 2).equals(", ")) {
                updateQueryBuilder.delete(updateQueryBuilder.length() - 2, updateQueryBuilder.length());
            }

            // Wrap PARQUET_RECORD_ID_COLUMN_HEADER in double quotes
            updateQueryBuilder.append(" WHERE ")
                .append(UtilityClass.addQuotesToFieldNames(PARQUET_RECORD_ID_COLUMN_HEADER))
                .append(" = '")
                .append(record.getId())
                .append("'");

            if (spatialDataHandling.geoJsonHeadersAreNotEmpty(tableSchemaVO)) {
                updateQueryBuilder = spatialDataHandling.fixQueryForUpdateSpatialData(updateQueryBuilder.toString(), true, tableSchemaVO, 0);
            }

            // Execute the query
            String processId = dremioHelperService.executeSqlStatement(updateQueryBuilder.toString());
            dremioHelperService.checkIfDremioProcessFinishedSuccessfully(updateQueryBuilder.toString(), processId, 2000L);
        }

        //todo handle updateCascadePK
    }

    @Override
    public void updateField(Long dataflowId, Long providerId, Long datasetId, FieldVO field, String recordId, TableSchemaVO tableSchemaVO, boolean updateCascadePK) throws Exception {
        providerId = providerId != null ? providerId : 0L;
        S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(
            dataflowId,
            providerId,
            datasetId,
            tableSchemaVO.getNameTableSchema(),
            tableSchemaVO.getNameTableSchema(),
            S3_TABLE_AS_FOLDER_QUERY_PATH
        );
        s3IcebergTablePathResolver.setIsIcebergTable(true);

        String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

        // Create update query for the record
        StringBuilder updateQueryBuilder = new StringBuilder().append("UPDATE ").append(icebergTablePath).append(" SET ");

        // Wrap field name in double quotes
        String fieldName = UtilityClass.addQuotesToFieldNames(field.getName());
        String fieldValue = (field.getValue() != null) ? field.getValue().replace("'", "''") : "";
        updateQueryBuilder.append(fieldName).append(" = '").append(fieldValue).append("'");

        // Wrap PARQUET_RECORD_ID_COLUMN_HEADER in double quotes
        updateQueryBuilder.append(" WHERE ")
            .append(UtilityClass.addQuotesToFieldNames(PARQUET_RECORD_ID_COLUMN_HEADER))
            .append(" = '")
            .append(recordId)
            .append("'");

        if (spatialDataHandling.geoJsonHeadersAreNotEmpty(tableSchemaVO)) {
            updateQueryBuilder = spatialDataHandling.fixQueryForUpdateSpatialData(updateQueryBuilder.toString(), true, tableSchemaVO, 0);
        }

        String processId = dremioHelperService.executeSqlStatement(updateQueryBuilder.toString());
        dremioHelperService.checkIfDremioProcessFinishedSuccessfully(updateQueryBuilder.toString(), processId, 2000L);

        //TODO: Handle updateCascadePK
    }

    @Override
    public void deleteRecord(Long dataflowId, Long providerId, Long datasetId, TableSchemaVO tableSchemaVO, List<String> recordIds, boolean deleteCascadePK) throws Exception{
        if(deleteCascadePK){
            //we need to remove all records in sub tables that are linked to the record
            deleteLinkedRecordsWithCascade(dataflowId, providerId, datasetId, tableSchemaVO, recordIds);
        }

        S3PathResolver s3TablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), UUID.randomUUID().toString(), S3_TABLE_AS_FOLDER_QUERY_PATH);
        S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), tableSchemaVO.getNameTableSchema(), S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3IcebergTablePathResolver.setIsIcebergTable(true);

        String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

        // Join items with single quotes and commas
        String recordIdsForQuery = recordIds.stream().map(id -> "'" + id + "'").collect(Collectors.joining(", "));

        //check if we need to remove attachments
        List<FieldSchemaVO> fields = tableSchemaVO.getRecordSchema().getFieldSchema();
        for(FieldSchemaVO field: fields){
            if(field.getType() == DataType.ATTACHMENT){
                //get fileName
                String getFileNameQuery = "SELECT " + field.getName() + " FROM " + icebergTablePath + " WHERE " + PARQUET_RECORD_ID_COLUMN_HEADER + " in (" + recordIdsForQuery + ")";
                String getFileNameResult = dremioJdbcTemplate.queryForObject(getFileNameQuery, String.class);
                if(StringUtils.isNotBlank(getFileNameResult)){
                    for(String recordId: recordIds) {
                        removeAttachmentFromS3(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), field.getName(), FilenameUtils.getExtension(getFileNameResult), recordId);
                    }
                }
            }
        }

        //get current number of records
        String recordsCountQuery = "select count(record_id) from " + icebergTablePath;
        Long numberOfRecords = dremioJdbcTemplate.queryForObject(recordsCountQuery, Long.class);
        if(numberOfRecords != 1) {
            //we can remove the entry
            //create delete query for the record
            StringBuilder deleteQueryBuilder = new StringBuilder().append("DELETE FROM " + icebergTablePath + " ");
            deleteQueryBuilder.append(" WHERE " + PARQUET_RECORD_ID_COLUMN_HEADER + " in (" + recordIdsForQuery + ")");
            String processId = dremioHelperService.executeSqlStatement(deleteQueryBuilder.toString());
            dremioHelperService.checkIfDremioProcessFinishedSuccessfully(deleteQueryBuilder.toString(), processId, 2000L);

        }
        else{
            //we must remove the table
            dremioHelperService.demoteFolderOrFile(s3IcebergTablePathResolver, tableSchemaVO.getNameTableSchema());
            LOG.info("Removing parquet files for table in path {}", icebergTablePath);
            if (s3HelperPrivate.checkFolderExist(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH)) {
                s3HelperPrivate.deleteFolder(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH);
            }

            //delete attachments if they exist
            if (s3HelperPrivate.checkFolderExist(s3TablePathResolver, S3_ATTACHMENTS_TABLE_PATH)) {
                s3HelperPrivate.deleteFolder(s3TablePathResolver, S3_ATTACHMENTS_TABLE_PATH);
            }
        }
    }

    private void removeAttachmentFromS3(Long dataflowId, Long providerId, Long datasetId, String tableSchemaName, String fieldName, String extension, String recordId){
        String fileNameInS3 = fieldName + "_" + recordId + "." + extension;
        S3PathResolver s3PathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaName, fileNameInS3, S3_ATTACHMENTS_PATH);
        String attachmentPathInS3 = s3ServicePrivate.getS3Path(s3PathResolver);
        s3HelperPrivate.deleteFile(attachmentPathInS3);
    }

    @Override
    public void removeRootDataflowFolderFromS3(Long dataflowId) {
        S3PathResolver s3PathResolverParquet = new S3PathResolver(dataflowId);
        s3PathResolverParquet.setIsIcebergTable(false);
        s3PathResolverParquet.setPath(S3_ROOT_DATAFLOW_FOLDER_PATH);

        S3PathResolver s3PathResolverIceberg = new S3PathResolver(dataflowId);
        s3PathResolverIceberg.setIsIcebergTable(true);
        s3PathResolverIceberg.setPath(S3_ROOT_DATAFLOW_FOLDER_PATH);

        if (s3HelperPrivate.checkFolderExist(s3PathResolverParquet, s3PathResolverParquet.getPath())) {
            s3HelperPrivate.deleteFolder(s3PathResolverParquet, s3PathResolverParquet.getPath());
            LOG.info("Removed root Parquet dataflow folder: for dataflow id: {}", dataflowId);
        }

        if (s3HelperPrivate.checkFolderExist(s3PathResolverIceberg, s3PathResolverIceberg.getPath())) {
            s3HelperPrivate.deleteFolder(s3PathResolverIceberg, s3PathResolverIceberg.getPath());
            LOG.info("Removed root Iceberg dataflow folder: for dataflow id: {}", dataflowId);
        }
    }

    @Override
    public void createReferenceFolder(S3PathResolver s3TablePathResolver) throws Exception {
        String tableSchemaName = s3TablePathResolver.getTableName();
        List<S3Object> tableNameFilenames = s3HelperPrivate.getFilenamesFromTableNames(s3TablePathResolver);
        AtomicInteger fileCounter = new AtomicInteger();

        //demote reference table folder
        S3PathResolver s3ReferenceTablePathResolver = new S3PathResolver(s3TablePathResolver.getDataflowId(), s3TablePathResolver.getDataProviderId(), s3TablePathResolver.getDatasetId(), tableSchemaName, tableSchemaName, S3_DATAFLOW_REFERENCE_FOLDER_PATH);
        if (s3HelperPrivate.checkFolderExist(s3ReferenceTablePathResolver, S3_DATAFLOW_REFERENCE_FOLDER_PATH)) {
            dremioHelperService.demoteFolderOrFile(s3ReferenceTablePathResolver, tableSchemaName);
            //remove folders that contain the previous parquet files because data will be replaced
            s3HelperPrivate.deleteFolder(s3ReferenceTablePathResolver, S3_DATAFLOW_REFERENCE_FOLDER_PATH);
        }

        tableNameFilenames.stream().forEach(file -> {
            String key = file.key();
            String filename = new File(key).getName();
            S3PathResolver s3ReferenceParquetPathResolver = new S3PathResolver(s3TablePathResolver.getDataflowId(), s3TablePathResolver.getDataProviderId(), s3TablePathResolver.getDatasetId(), tableSchemaName, filename, S3_DATAFLOW_REFERENCE_PATH);
            s3ReferenceParquetPathResolver.setParquetFolder(key.split("/")[5]);
            try {
                String referenceParquetPath = s3ServicePrivate.getS3Path(s3ReferenceParquetPathResolver);
                //copy file to reference folder
                s3HelperPrivate.copyFileToAnotherDestination(key, referenceParquetPath);

                //path in dremio for the reference folder that represents the table of the dataset
                String dremioPathForReferenceParquetFolder = s3ServicePrivate.getTableAsFolderQueryPath(s3ReferenceParquetPathResolver, S3_DATAFLOW_REFERENCE_QUERY_PATH);

                //refresh the metadata
                dremioHelperService.refreshTableMetadataAndPromote(null, dremioPathForReferenceParquetFolder, s3ReferenceTablePathResolver, tableSchemaName);
                fileCounter.incrementAndGet();
            }
            catch (Exception e) {
                LOG.error("Error copying items to reference folder for dataflowId {} providerId {} datasetId {} and table {}", s3TablePathResolver.getDataflowId(), s3TablePathResolver.getDataProviderId(), s3TablePathResolver.getDatasetId(), tableSchemaName, e);
            }
        });

        if(fileCounter.get() != tableNameFilenames.size()){
            String exceptionMsg = String.format("Error copying items to reference folder for dataflowId %s providerId %s datasetId %s and table %s ", s3TablePathResolver.getDataflowId(), s3TablePathResolver.getDataProviderId(), s3TablePathResolver.getDatasetId(), tableSchemaName);
            throw new Exception(exceptionMsg);
        }
        LOG.info("For dataflowId {} providerId {} datasetId {} and table {} the REFERENCE dataset files have been successfully copied to the reference folder", s3TablePathResolver.getDataflowId(), s3TablePathResolver.getDataProviderId(), s3TablePathResolver.getDatasetId(), tableSchemaName);
    }

    @Override
    public void createPrefilledTables(Long designDatasetId, String designDatasetSchemaId, Long datasetIdForCreation, Long providerId, String tableSchemaId) throws Exception {
        DataSetMetabaseVO designDataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(designDatasetId);

        List<TableSchemaIdNameVO> tables = datasetSchemaService.getTableSchemasIds(designDatasetId);
        for (TableSchemaIdNameVO table : tables) {
            //check if we need to create prefilled data for a specific table only
            if(StringUtils.isNotBlank(tableSchemaId) && !table.getIdTableSchema().equals(tableSchemaId)){
                continue;
            }
            TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(table.getIdTableSchema(), designDatasetSchemaId);
            if (tableSchemaVO != null && BooleanUtils.isTrue(tableSchemaVO.getToPrefill())) {
                //copy table folder from design to dataset with id datasetIdForCreation and promote folder
                String tableSchemaName = tableSchemaVO.getNameTableSchema();

                S3PathResolver s3DesignTablePathResolver = new S3PathResolver(designDataSetMetabaseVO.getDataflowId(), 0L, designDatasetId, tableSchemaName, tableSchemaName, S3_TABLE_AS_FOLDER_QUERY_PATH);
                //query path for the design table
                String dremioDesignTableQueryPath = s3ServicePrivate.getTableAsFolderQueryPath(s3DesignTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

                S3PathResolver s3NewTablePathResolver = new S3PathResolver(designDataSetMetabaseVO.getDataflowId(), providerId, datasetIdForCreation, tableSchemaName, tableSchemaName, S3_TABLE_AS_FOLDER_QUERY_PATH);
                //query path for the new table
                String dremioNewTableQueryPath = s3ServicePrivate.getTableAsFolderQueryPath(s3NewTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

                String providerCode = "''";
                if(providerId != 0L) {
                    DataProviderVO dataProviderVO = representativeControllerZuul.findDataProviderById(providerId);
                    providerCode = "'" + dataProviderVO.getCode() + "'";
                }

                List<FieldSchemaVO> fieldSchemas = tableSchemaVO.getRecordSchema().getFieldSchema();
                String tableHeaders = constructRecordIdCreationForQuery();
                tableHeaders += ", " + providerCode + " AS " + UtilityClass.addQuotesToFieldNames(PARQUET_PROVIDER_CODE_COLUMN_HEADER) + ", ";


                for (FieldSchemaVO fieldSchema : fieldSchemas) {
                    if (fieldSchema.getType().equals(DataType.ATTACHMENT)) {
                        // Remove attachment file name
                        tableHeaders += " '' AS ";
                    }
                    // Wrap field names in double quotes
                    tableHeaders += UtilityClass.addQuotesToFieldNames(fieldSchema.getName()) + ", ";
                }

                // Remove the trailing comma, if any
                if (tableHeaders.endsWith(", ")) {
                    tableHeaders = tableHeaders.substring(0, tableHeaders.length() - 2);
                }

                // Remove previous data if it exists
                S3PathResolver s3TablePathResolver = new S3PathResolver(
                    designDataSetMetabaseVO.getDataflowId(),
                    providerId,
                    datasetIdForCreation,
                    tableSchemaName,
                    tableSchemaName,
                    S3_TABLE_AS_FOLDER_QUERY_PATH
                );

                if (s3HelperPrivate.checkFolderExist(s3TablePathResolver, S3_TABLE_NAME_FOLDER_PATH)) {
                    // Remove old Parquet table because it will be recreated
                    dremioHelperService.demoteFolderOrFile(s3TablePathResolver, tableSchemaVO.getNameTableSchema());
                    s3HelperPrivate.deleteFolder(s3TablePathResolver, S3_TABLE_NAME_FOLDER_PATH);
                }

                if (!s3HelperPrivate.checkFolderExist(s3DesignTablePathResolver, S3_TABLE_NAME_FOLDER_PATH) ||
                    !dremioHelperService.checkFolderPromoted(s3DesignTablePathResolver, tableSchemaName)) {
                    kafkaSenderUtils.releaseNotificableKafkaEvent(
                        EventType.PREFILLED_TABLE_HAS_NO_DATA_ERROR,
                        null,
                        NotificationVO.builder()
                            .user(SecurityContextHolder.getContext().getAuthentication().getName())
                            .dataflowId(designDataSetMetabaseVO.getDataflowId())
                            .datasetId(designDatasetId)
                            .tableSchemaId(tableSchemaId)
                            .build()
                    );

                    String exceptionMsg = "Table marked as prefilled has no data";
                    throw new Exception(exceptionMsg);
                }

                // Construct the query to create the prefilled table
                String queryToCreatePrefilledTable =
                    "CREATE TABLE " + dremioNewTableQueryPath + " AS SELECT " + tableHeaders + " FROM " + dremioDesignTableQueryPath;

                String processId = dremioHelperService.executeSqlStatement(queryToCreatePrefilledTable);
                dremioHelperService.checkIfDremioProcessFinishedSuccessfully(queryToCreatePrefilledTable, processId, null);

                //refresh the metadata
                dremioHelperService.refreshTableMetadataAndPromote(null, dremioNewTableQueryPath, s3NewTablePathResolver, tableSchemaName);
                LOG.info("Created prefilled data for datasetId {} and table {} from designDatasetId {} ", datasetIdForCreation, tableSchemaVO.getNameTableSchema(), designDatasetId);
            }
        }
    }

    private String constructRecordIdCreationForQuery(){
        return "CONCAT(\n" +
                "        LOWER(LPAD(TO_HEX(CAST(RAND() * 4294967295 AS BIGINT)), 8, '0')), '-',\n" +
                "        LOWER(LPAD(TO_HEX(CAST(RAND() * 65535 AS BIGINT)), 4, '0')), '-',\n" +
                "        LOWER(LPAD(TO_HEX(CAST(RAND() * 65535 AS BIGINT)), 4, '0')), '-',\n" +
                "        LOWER(LPAD(TO_HEX(CAST(RAND() * 65535 AS BIGINT)), 4, '0')), '-',\n" +
                "        LOWER(LPAD(TO_HEX(CAST(RAND() * 281474976710655 AS BIGINT)), 12, '0'))\n" +
                "    ) AS " + PARQUET_RECORD_ID_COLUMN_HEADER + " ";
    }

    private String getFileExtensionFromFilePath(String filePathInS3){
        String fileExtension = null;
        if(filePathInS3.endsWith(CSV_TYPE)) {
            fileExtension = CSV_TYPE;
        }
        else if(filePathInS3.endsWith(ZIP_TYPE)){
            fileExtension = ZIP_TYPE;
        }
        else if(filePathInS3.endsWith(GKPG_TYPE)){
            fileExtension = GKPG_TYPE;
        }
        else if(filePathInS3.endsWith(XLSX_TYPE)){
            fileExtension = XLSX_TYPE;
        }
        else if(filePathInS3.endsWith(XLSM_TYPE)){
            fileExtension = XLSM_TYPE;
        }
        else if(filePathInS3.endsWith(XLS_TYPE)){
            fileExtension = XLS_TYPE;
        }
        else if(filePathInS3.endsWith(XML_TYPE)){
            fileExtension = XML_TYPE;
        }
        else if(filePathInS3.endsWith(GML_TYPE)){
            fileExtension = GML_TYPE;
        } else if(filePathInS3.endsWith(DB3_TYPE)){
            fileExtension = DB3_TYPE;
        }
        else{
            LOG.error("Found unhandled file extension for file in path {}", filePathInS3);
        }
        return fileExtension;
    }

    @Override
    public List<FieldVO> getFieldValuesReferencedDL(Long datasetIdOrigin, String datasetSchemaId,
                                                     String fieldSchemaId, String conditionalValue, String searchValue, Integer resultsNumber) throws EEAException {
        List<FieldVO> fieldsVO = new ArrayList<>();
        Document fieldSchema = schemasRepository.findFieldSchema(datasetSchemaId, fieldSchemaId);
        Document referenced = (Document) fieldSchema.get(LiteralConstants.REFERENCED_FIELD);
        if (referenced == null) {
            return fieldsVO;
        }
        String idPk = referenced.get("idPk").toString();
        String labelSchemaId = null;
        String conditionalSchemaId = null;
        if (referenced.get("labelId") != null) {
            labelSchemaId = referenced.get("labelId").toString();
        } else {
            // In case there's no label selected, the label will the same as the Pk
            labelSchemaId = idPk;
        }
        if (referenced.get("linkedConditionalFieldId") != null) {
            conditionalSchemaId = referenced.get("linkedConditionalFieldId").toString();
        }
        if(StringUtils.isBlank(conditionalSchemaId)){
            //ignore conditional value because in mongo we don't have a conditional set up for the link
            conditionalValue = "";
        }
        if (StringUtils.isBlank(searchValue)) {
            searchValue = "";
        }
        //get the dataset id that contains the reference values
        Long referenceDatasetId = datasetMetabaseService.getDatasetDestinationForeignRelation(datasetIdOrigin, idPk);
        String referenceDatasetSchemaId = datasetSchemaService.getDatasetSchemaId(referenceDatasetId);
        Document referenceFieldSchema = schemasRepository.findFieldSchema(referenceDatasetSchemaId, idPk);

        TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, referenceDatasetId));

        DataType dataType = DataType.TEXT;

        if(referenceFieldSchema.get(TYPE_DATA) != null){
            String typeData = (String) referenceFieldSchema.get(TYPE_DATA);
            if(DataType.fromValue(typeData) != null){
                dataType = DataType.fromValue(typeData);
            }
        }

        String record_id = referenceFieldSchema.get(ID_RECORD).toString();
        Document referenceRecordSchema = schemasRepository.findRecordSchemaByRecordSchemaId(referenceDatasetSchemaId, record_id);
        String referenceTableSchemaId = referenceRecordSchema.get(ID_TABLE_SCHEMA).toString();
        // we find reference dataset information to retrieve data
        Document referenceTableSchema = schemasRepository.findTableSchema(referenceDatasetSchemaId, referenceTableSchemaId);
        String referenceTableSchemaName = referenceTableSchema.get(NAME_TABLE_SCHEMA).toString();
        String referenceFieldName = referenceFieldSchema.get(HEADER_NAME).toString();

        String conditionalFieldName = null;
        if (StringUtils.isNotBlank(conditionalSchemaId)) {
            Document referenceFieldSchema1 = schemasRepository.findFieldSchema(referenceDatasetSchemaId, conditionalSchemaId);
            conditionalFieldName = referenceFieldSchema1.get(HEADER_NAME).toString();
        }

        String labelFieldName = referenceFieldName;
        if(!labelSchemaId.equals(idPk)){
            Document labelFieldSchema = schemasRepository.findFieldSchema(referenceDatasetSchemaId, labelSchemaId);
            labelFieldName = labelFieldSchema.get(HEADER_NAME).toString();
        }

        try {
            //retrieve the value and label from dremio.
            List<Map<String, Object>> linkValues = getLinkValuesWithLabelsFromReferencedDataset(referenceDatasetId, referenceTableSchemaId, referenceTableSchemaName,
                referenceFieldName, labelFieldName, conditionalValue, dataType, searchValue, conditionalFieldName);

            for (Map<String, Object> row : linkValues) {
                FieldVO field = new FieldVO();
                field.setValue((String) row.get(VALUE));
                field.setLabel((String) row.get(LABEL));
                field.setIdFieldSchema(fieldSchemaId);
                fieldsVO.add(field);
            }
        } catch (DataIntegrityViolationException e) {

            LOG.error("Error with dataset id {}  field  with id {} because data has not correct format {}",
                    datasetIdOrigin, idPk, dataType);

            String idRecord = fieldSchema.get(ID_RECORD).toString();
            Document recordSchema = schemasRepository.findRecordSchemaByRecordSchemaId(datasetSchemaId, idRecord);
            String tableSchemaId = recordSchema.get(ID_TABLE_SCHEMA).toString();
            // we find table and field to send in notification
            Document tableSchema = schemasRepository.findTableSchema(datasetSchemaId, tableSchemaId);
            String tableSchemaName = tableSchema.get(NAME_TABLE_SCHEMA).toString();
            String fieldSchemaName = fieldSchema.get(HEADER_NAME).toString();


            NotificationVO notificationVO = NotificationVO.builder()
                    .user(SecurityContextHolder.getContext().getAuthentication().getName())
                    .datasetId(datasetIdOrigin).datasetSchemaId(datasetSchemaId)
                    .tableSchemaId(tableSchemaId).tableSchemaName(tableSchemaName)
                    .fieldSchemaId(fieldSchemaId).fieldSchemaName(fieldSchemaName).build();


            // we send 2 dif notification depends on type
            DatasetTypeEnum type = datasetService.getDatasetType(datasetIdOrigin);
            EventType eventType =
                    DatasetTypeEnum.DESIGN.equals(type) ? EventType.SORT_FIELD_DESIGN_FAILED_EVENT
                            : EventType.SORT_FIELD_FAILED_EVENT;


            kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, null, notificationVO);
        }
        removeDuplicateValues(fieldsVO);
        return fieldsVO;
    }

    private List<Map<String, Object>> getLinkValuesWithLabelsFromReferencedDataset(Long datasetId, String tableSchemaId, String tableName,
                                                                                   String fieldName, String labelFieldName, String conditionalValue,
                                                                                   DataType dataType, String searchValue, String conditionalFieldName){
        DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
        Long dataflowId = dataSetMetabaseVO.getDataflowId();
        long providerId = (dataSetMetabaseVO.getDataProviderId() != null) ? dataSetMetabaseVO.getDataProviderId() : 0L;

        String tablePathInDremio = null;

        if(dataSetMetabaseVO.getDatasetTypeEnum().equals(DatasetTypeEnum.REFERENCE) && !BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaId))){
            S3PathResolver s3PathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableName, tableName, S3_DATAFLOW_REFERENCE_QUERY_PATH);
            tablePathInDremio = s3ServicePrivate.getTableAsFolderQueryPath(s3PathResolver, S3_DATAFLOW_REFERENCE_QUERY_PATH);
        }
        else{
            S3PathResolver s3PathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableName, tableName, S3_DATAFLOW_REFERENCE_QUERY_PATH);
            if(BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaId))){
                s3PathResolver.setIsIcebergTable(true);
            }
            tablePathInDremio = s3ServicePrivate.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
        }

        String quotedField = UtilityClass.addQuotesToFieldNames(fieldName);
        String quotedLabelField = UtilityClass.addQuotesToFieldNames(labelFieldName);

        String selectQuery = "SELECT " + quotedField + " as " + VALUE + ", " + quotedLabelField + " as " + LABEL +
            " FROM " + tablePathInDremio +
            " WHERE " + quotedField + " != '' AND " + quotedField + " IS NOT NULL";

        if (StringUtils.isNotBlank(searchValue)) {
            searchValue = searchValue.replace("'", "''");
            selectQuery += " AND ('"
                + searchValue + "' IS NULL "
                + "OR LOWER(" + VALUE + ") LIKE LOWER(CONCAT('%', '" + searchValue + "', '%')) "
                + "OR LOWER(" + LABEL + ") LIKE LOWER(CONCAT('%', '" + searchValue + "', '%'))"
                + ")";
        }

        if (labelFieldName == null && conditionalValue.isBlank()
            || (StringUtils.isNotBlank(labelFieldName) && StringUtils.isNotBlank(fieldName) && StringUtils.isNotBlank(conditionalFieldName) && conditionalValue.isBlank())) {
            conditionalValue = "null";
        }
        if (StringUtils.isNotBlank(conditionalValue)) {
            //Removed comma from regex split due to #282448
            String[] values = Arrays.stream(conditionalValue.split("[;]"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toArray(String[]::new);

            String valuesList = Arrays.stream(values)
                .map(value -> "'" + value + "'")
                .collect(Collectors.joining(", "));

            String refValue = conditionalFieldName != null ? conditionalFieldName : VALUE;
            String refLabel = conditionalFieldName != null ? conditionalFieldName : LABEL;
            String quotedRefValue = UtilityClass.addQuotesToFieldNames(refValue);
            String quotedRefLabel = UtilityClass.addQuotesToFieldNames(refLabel);
            if (dataType.equals(DataType.NUMBER_INTEGER)) {
                selectQuery = selectQuery + " AND " + quotedRefValue + " IN (" + valuesList + ")";
                selectQuery = selectQuery + " ORDER BY " + quotedRefValue;
            } else {
                selectQuery = selectQuery + " AND " + quotedRefLabel + " IN (" + valuesList + ")";
                selectQuery = selectQuery + " ORDER BY " + quotedRefLabel;
            }
        }
        LOG.info("Query to execute in links: {}", selectQuery);
        return dremioJdbcTemplate.queryForList(selectQuery);
    }

    private void removeDuplicateValues(List<FieldVO> fieldsVO) {
        HashSet<String> seen = new HashSet<>();
        fieldsVO.removeIf(e -> !seen.add(e.getValue()));
    }


    @Override
    public List<TableSchemaIdNameVO> getAvailableForManualEditingTables(Long datasetId) throws EEAException {
        String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
        List<TableSchemaIdNameVO> availableTables = new ArrayList<>();
        List<TableSchemaIdNameVO> allDatasetTables = datasetSchemaService.getTableSchemasIds(datasetId);
        for(TableSchemaIdNameVO tableSchemaIdNameVO: allDatasetTables){
            TableSchema tableSchema = datasetSchemaService.getTableSchema(tableSchemaIdNameVO.getIdTableSchema(), datasetSchemaId);
            if(BooleanUtils.isTrue(tableSchema.getDataAreManuallyEditable())){
                availableTables.add(tableSchemaIdNameVO);
            }
        }
        return availableTables;
    }

    @Async
    @Override
    public void insertRecordsInMultipleTables(DataSetMetabaseVO dataSetMetabaseVO, List<TableVO> tableRecords) throws Exception {
        try{
            for (TableVO tableVO : tableRecords) {
                TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableVO.getIdTableSchema(), dataSetMetabaseVO.getDatasetSchema());
                insertRecords(dataSetMetabaseVO.getDataflowId(), dataSetMetabaseVO.getDataProviderId(), dataSetMetabaseVO.getId(),
                        tableSchemaVO.getNameTableSchema(), tableVO.getRecords());
            }
            LOG.info("PaM/Entity group save: Successfully inserted multiple records for datasetId {}", dataSetMetabaseVO.getId());
            //sent completed event
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.INSERT_RECORDS_MULTI_TABLES_COMPLETED,
                    null,
                    NotificationVO.builder()
                            .user(SecurityContextHolder.getContext().getAuthentication().getName()).datasetId(dataSetMetabaseVO.getId())
                            .dataflowId(dataSetMetabaseVO.getDataflowId()).build());
        }
        catch (Exception e){
            LOG.error("Could not insert records in multiple tables for datasetId {} Error {}", dataSetMetabaseVO.getId(), e.getMessage());
            //send failed event
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.INSERT_RECORDS_MULTI_TABLES_FAILED,
                    null,
                    NotificationVO.builder()
                            .user(SecurityContextHolder.getContext().getAuthentication().getName()).datasetId(dataSetMetabaseVO.getId())
                            .dataflowId(dataSetMetabaseVO.getDataflowId()).error("Failed inserting records in multiple tables").build());
            throw e;
        }
    }

    private void deleteLinkedRecordsWithCascade(Long dataflowId, Long providerId, Long datasetId, TableSchemaVO tableSchemaVO, List<String> recordIds) throws Exception{
        // Get the first referenced field
        FieldSchemaVO fieldSchemaPK;
        Optional<FieldSchemaVO> optionalFieldReferenced = tableSchemaVO.getRecordSchema().getFieldSchema().stream().filter(field -> Boolean.TRUE.equals(field.getPkReferenced())).findFirst();
        if (optionalFieldReferenced.isPresent()){
            fieldSchemaPK = optionalFieldReferenced.get();
        }
        else{
            return;
        }

        //get value of field fieldSchemaPK
        S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(),  tableSchemaVO.getNameTableSchema(), S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3IcebergTablePathResolver.setIsIcebergTable(true);

        String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

        for(String recordId: recordIds) {

            String getFieldValue =  "SELECT " + fieldSchemaPK.getName() + " FROM " + icebergTablePath + " WHERE " + PARQUET_RECORD_ID_COLUMN_HEADER + " = '" + recordId + "'";
            String fieldValue = dremioJdbcTemplate.queryForObject(getFieldValue, String.class);

            //get field references from pkCatalogue
            PkCatalogueSchema pkCatalogueSchema = pkCatalogueRepository.findByIdPk(new ObjectId(fieldSchemaPK.getId()));
            if (pkCatalogueSchema == null || pkCatalogueSchema.getReferenced() == null) {
                return;
            }
            Map<FieldSchemaVO, TableSchemaVO> fieldSchemaAndTableSchemaMapping = new HashMap<>();
            List<String> referencedFieldSchemaIds = pkCatalogueSchema.getReferenced().stream().map(ObjectId::toString).collect(Collectors.toList());
            for (String referencedFieldSchemaId : referencedFieldSchemaIds) {
                DataSetSchemaVO dataSetSchemaVO = datasetSchemaService.getDataSchemaByDatasetId(false, datasetId);
                for (TableSchemaVO tableInDataset : dataSetSchemaVO.getTableSchemas()) {

                    Optional<FieldSchemaVO> matchingSchema = tableInDataset.getRecordSchema().getFieldSchema().stream()
                            .filter(field -> referencedFieldSchemaId.equals(field.getId())).findFirst();
                    if (matchingSchema.isEmpty()) {
                        continue;
                    }
                    //found the correct table that is related to referencedFieldSchemaId
                    fieldSchemaAndTableSchemaMapping.put(matchingSchema.get(), tableInDataset);
                }
            }

            //for each reference field we need to find the records associated with the parent table's value in the reference field and delete them
            for (Map.Entry<FieldSchemaVO, TableSchemaVO> entry : fieldSchemaAndTableSchemaMapping.entrySet()) {
                S3PathResolver s3IcebergSubTablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, entry.getValue().getNameTableSchema(), entry.getValue().getNameTableSchema(), S3_TABLE_AS_FOLDER_QUERY_PATH);
                s3IcebergSubTablePathResolver.setIsIcebergTable(true);

                String icebergSubTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergSubTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

                if (!s3HelperPrivate.checkFolderExist(s3IcebergSubTablePathResolver, S3_TABLE_NAME_FOLDER_PATH_FOR_VALID_PREFIX) ||
                        !dremioHelperService.checkFolderPromoted(s3IcebergSubTablePathResolver, entry.getValue().getNameTableSchema())){
                    continue;
                }
                String getRecordIdsQuery = "SELECT " + PARQUET_RECORD_ID_COLUMN_HEADER + " FROM " + icebergSubTablePath + " WHERE " + entry.getKey().getName() + " = '" + fieldValue + "'";
                List<String> subTableRecordIds = dremioJdbcTemplate.queryForList(getRecordIdsQuery, String.class);
                if(subTableRecordIds != null && subTableRecordIds.size() > 0) {
                    deleteRecord(dataflowId, providerId, datasetId, entry.getValue(), subTableRecordIds, true);
                }
            }
        }

    }

    @Override
    public ReleasedDatasetDataInfoVO getReleasedDatasetDataInfoDL(DataSetMetabaseVO collectionDataset, DataSetMetabaseVO reportingDataset, Long dataflowId, DataProviderVO dataProviderVO, String tableSchemaId,
                                                                  DatasetTypeEnum datasetType) throws Exception {
        ReleasedDatasetDataInfoVO releasedDatasetDataInfoVO = new ReleasedDatasetDataInfoVO();

        //find number of records for reporting dataset
        TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, reportingDataset.getDatasetSchema());
        S3PathResolver s3PathResolverReporting;
        if (BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable()) && BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(reportingDataset.getId(), tableSchemaVO.getIdTableSchema()))) {
            s3PathResolverReporting = s3ServicePrivate.getS3PathResolverByDatasetType(reportingDataset, tableSchemaVO.getNameTableSchema(), true);
            s3PathResolverReporting.setIsIcebergTable(true);
        } else {
            s3PathResolverReporting = s3ServicePrivate.getS3PathResolverByDatasetType(reportingDataset, tableSchemaVO.getNameTableSchema(), false);
            s3PathResolverReporting.setIsIcebergTable(false);
        }
        boolean folderExistReporting = s3HelperPrivate.checkFolderExist(s3PathResolverReporting);
        if (folderExistReporting && dremioHelperService.checkFolderPromoted(s3PathResolverReporting, s3PathResolverReporting.getTableName())){
            Long reportingDatasetRecordCount = dremioJdbcTemplate.queryForObject(s3HelperPrivate.buildRecordsCountQuery(s3PathResolverReporting), Long.class);
            releasedDatasetDataInfoVO.setReportingDatasetNumberOfRecords(reportingDatasetRecordCount);
        }
        else{
            releasedDatasetDataInfoVO.setReportingDatasetNumberOfRecords(0L);
        }

        //find number of records for data collection based on provider and table
        S3PathResolver s3PathResolverCollection = s3ServicePrivate.getS3PathResolverByDatasetType(collectionDataset, tableSchemaVO.getNameTableSchema(), false);
        String dremioTableQueryPath;
        if (datasetType.equals(DatasetTypeEnum.COLLECTION)) {
            //data collection
            s3PathResolverCollection.setPath(S3_TABLE_NAME_ROOT_DC_FOLDER_PATH);
            dremioTableQueryPath = S3_TABLE_NAME_DC_QUERY_PATH;
        }
        else{
            //eu dataset
            s3PathResolverCollection.setPath(S3_EU_SNAPSHOT_TABLE_PATH);
            dremioTableQueryPath = S3_TABLE_NAME_EU_QUERY_PATH;
        }
        boolean folderExistCollection = s3HelperPrivate.checkTableNameDCFolderExist(s3PathResolverCollection);
        if (folderExistCollection && dremioHelperService.checkFolderPromoted(s3PathResolverCollection, s3PathResolverCollection.getTableName())) {


            String collectionPathDremio = s3ServicePrivate.getTableDCAsFolderQueryPath(s3PathResolverCollection, dremioTableQueryPath);
            StringBuilder queryCollectionRecordCount = new StringBuilder().append("SELECT COUNT(").append(LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER).append(") FROM ").append(collectionPathDremio)
                    .append(" WHERE ").append(PARQUET_PROVIDER_CODE_COLUMN_HEADER).append(" = '").append(dataProviderVO.getCode()).append("' ");
            Long collectionRecordCount = dremioJdbcTemplate.queryForObject(queryCollectionRecordCount.toString(), Long.class);
            releasedDatasetDataInfoVO.setCollectionDatasetNumberOfRecords(collectionRecordCount);
        }
        else{
            releasedDatasetDataInfoVO.setCollectionDatasetNumberOfRecords(0L);
        }

        //check if reporting dataset has released and if it has updates after release
        List<ReleaseVO> releases = datasetSnapshotService.getReleases(reportingDataset.getId());
        if(releases != null && releases.size() > 0){
            releasedDatasetDataInfoVO.setHasReleased(true);
            ResponseEntity<?> updatedTablesResponse = tableDataRetriever.getTablesUpdatedAfterRelease(reportingDataset.getId());
            HashMap<String, Boolean> updatedTablesHashmap = (HashMap<String, Boolean>) updatedTablesResponse.getBody();
            if(updatedTablesHashmap.get(tableSchemaId) != null){
                releasedDatasetDataInfoVO.setModifiedAfterRelease(updatedTablesHashmap.get(tableSchemaId));
            }
            else{
                releasedDatasetDataInfoVO.setModifiedAfterRelease(false);
            }
        }
        else{
            releasedDatasetDataInfoVO.setHasReleased(false);
            releasedDatasetDataInfoVO.setModifiedAfterRelease(false);
        }

        return releasedDatasetDataInfoVO;
    }

    @Override
    public void etlExportCsv(Long datasetId, Long dataflowId, String tableSchemaId, Long jobId, String user, String processUUID, Boolean includeAttachments, String dataProviderCodes) throws EEAException {
        try {
            // the path of the parent folder which will be zipped
            String folderToZipPath = exportDLPath + DATASET_PREFIX_FOR_EXPORT + datasetId + "/etlExportV4_" + jobId;
            DatasetTypeEnum datasetType = datasetService.getDatasetType(datasetId);
            updateJobProcess(datasetId, dataflowId, jobId, user, processUUID);

          // Split the codes by "," and prevent duplicates.
          Set<String> codes = StringUtils.isBlank(dataProviderCodes) ? Collections.emptySet() : Arrays.stream(dataProviderCodes.split(","))
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .map(String::toUpperCase)
                  .collect(Collectors.toCollection(LinkedHashSet::new));

          // Cancel if codes where given but not collection or eudataset ds.
          if (!codes.isEmpty() && !(DatasetTypeEnum.COLLECTION.equals(datasetType) || DatasetTypeEnum.EUDATASET.equals(datasetType))) {
            throw new IllegalArgumentException("Parameter 'dataProviderCodes' was provided but Dataset is not a Data Collection or EU Dataset.");
          }

          // List of providers that belong to df.
          List<DataProviderVO> providers = resolveProvidersByCodes(dataflowId, codes);
          // Cancel if codes are given but providers are not assigned to df.
          if (!codes.isEmpty() && providers.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'dataProviderCodes' was provided but the corresponding providers were not found in the Dataflow.");
          }

          DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
          Long providerId = (dataSetMetabaseVO.getDataProviderId() != null) ? dataSetMetabaseVO.getDataProviderId() : 0L;

          if (codes.isEmpty()) {
            if (StringUtils.isNotBlank(tableSchemaId)) {
              String tableName = datasetSchemaService.getTableSchemaName(dataSetMetabaseVO.getDatasetSchema(), tableSchemaId);
              fileTreatmentHelper.convertParquetFile(datasetId, CSV, tableSchemaId, tableName, true, jobId);
              if (includeAttachments) {
                //get attachments if they exist
                String path = null;
                if (datasetType.equals(DatasetTypeEnum.DESIGN) || datasetType.equals(DatasetTypeEnum.TEST) || datasetType.equals(REPORTING) || datasetType.equals(DatasetTypeEnum.REFERENCE)) {
                  path = S3_ATTACHMENTS_TABLE_PATH;
                } else if (datasetType.equals(DatasetTypeEnum.COLLECTION)) {
                  path = S3_ATTACHMENTS_DC_TABLE_PATH;
                } else if (datasetType.equals(DatasetTypeEnum.EUDATASET)) {
                  path = S3_ATTACHMENTS_EU_TABLE_PATH;
                }
                S3PathResolver s3TablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableName, tableName, path);
                if (s3HelperPrivate.checkFolderExist(s3TablePathResolver, path)) {
                  String attachmentsPathInS3 = s3ServicePrivate.getTableAsFolderQueryPath(s3TablePathResolver, path);
                  s3HelperPrivate.getAttachmentsFromS3Locally(attachmentsPathInS3, folderToZipPath);
                }
              }
            } else {
              List<TableSchemaIdNameVO> tableSchemaIdNameVOS = datasetSchemaService.getTableSchemasIds(datasetId);
              for (TableSchemaIdNameVO tableSchemaIdNameVO : tableSchemaIdNameVOS) {
                fileTreatmentHelper.convertParquetFile(datasetId, CSV, tableSchemaIdNameVO.getIdTableSchema(), tableSchemaIdNameVO.getNameTableSchema(), true, jobId);
              }
              if (includeAttachments) {
                //get attachments if they exist
                String path = null;
                if (datasetType.equals(DatasetTypeEnum.DESIGN) || datasetType.equals(DatasetTypeEnum.TEST) || datasetType.equals(REPORTING) || datasetType.equals(DatasetTypeEnum.REFERENCE)) {
                  path = S3_ATTACHMENTS_PARENT_FOLDER_PATH;
                } else if (datasetType.equals(DatasetTypeEnum.COLLECTION)) {
                  path = S3_ATTACHMENTS_DC_FOLDER_PATH;
                } else if (datasetType.equals(DatasetTypeEnum.EUDATASET)) {
                  path = S3_ATTACHMENTS_PARENT_FOLDER_EU_PATH;
                }
                S3PathResolver s3TablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, null, null, path);
                if (s3HelperPrivate.checkFolderExist(s3TablePathResolver, path)) {
                  String attachmentsPathInS3 = s3ServicePrivate.getTableAsFolderQueryPath(s3TablePathResolver, path);
                  s3HelperPrivate.getAttachmentsFromS3Locally(attachmentsPathInS3, folderToZipPath);
                }
              }
            }
          } else {
            List<TableSchemaIdNameVO> tables;
            if (StringUtils.isNotBlank(tableSchemaId)) {
              String tableName = datasetSchemaService.getTableSchemaName(
                  dataSetMetabaseVO.getDatasetSchema(), tableSchemaId);
              tables = new ArrayList<>();
              TableSchemaIdNameVO singleTable = new TableSchemaIdNameVO();
              singleTable.setIdTableSchema(tableSchemaId);
              singleTable.setNameTableSchema(tableName);
              tables.add(singleTable);
            } else {
              tables = datasetSchemaService.getTableSchemasIds(datasetId);
            }

            for (DataProviderVO provider : providers) {
              Long pid = (provider.getId() != null) ? provider.getId() : 0L;

              // Create directory for current provider.
              String providerFolderPath = Paths.get(folderToZipPath, "provider_" + pid).toString();
              new File(providerFolderPath).mkdirs();

              // Convert tables for current provider.
              for (TableSchemaIdNameVO t : tables) {
                fileTreatmentHelper.convertParquetFileForProvider(
                    datasetId, pid, t.getIdTableSchema(), t.getNameTableSchema(),
                    datasetType, true, jobId, providerFolderPath);
              }

              // Provider-scoped attachments (per table)
              if (Boolean.TRUE.equals(includeAttachments)) {
                final String tableAttachmentsConst = (datasetType == DatasetTypeEnum.COLLECTION) ? S3_ATTACHMENTS_DC_TABLE_PATH : S3_ATTACHMENTS_EU_TABLE_PATH;

                for (TableSchemaIdNameVO t : tables) {
                  // Build resolver with dataflowId, providerId, datasetId, and current table.
                  S3PathResolver resolver = new S3PathResolver(dataflowId, pid, datasetId, t.getNameTableSchema(), null, tableAttachmentsConst);

                  // Base prefix up to the table folder
                  String tablePrefix = s3ServicePrivate.getTableAsFolderQueryPath(resolver, tableAttachmentsConst);
                  // Format provider folder name.
                  String dpFolder = s3ServicePrivate.formatFolderName(pid, S3_DATA_PROVIDER_PATTERN);
                  // Final prefix for provider-scoped attachments under this table.
                  String attachmentsPrefix = tablePrefix + "/" + dpFolder + "/";

                  LOG.info("Downloading attachments for provider {} table {} from prefix: {}", pid, t.getNameTableSchema(), attachmentsPrefix);
                  s3HelperPrivate.getAttachmentsFromS3Locally(attachmentsPrefix, providerFolderPath);
                }
              }
            }
          }

            zipFolder(jobId, folderToZipPath);
            finishJob(datasetId, dataflowId, jobId, user, processUUID);
        }
        catch (Exception e) {
            exceptionHandling(datasetId, dataflowId, jobId, user, processUUID, e);
        }
    }

  private List<DataProviderVO> resolveProvidersByCodes(Long dataflowId, Set<String> codes) throws EEAException {
    if (codes == null || codes.isEmpty()) {
      return Collections.emptyList();
    }

    List<DataProviderVO> givenProviders = new ArrayList<>();
    List<Long> providerIds = new ArrayList<>();
    Long dataProviderGroupId = dataFlowControllerZuul.findDataProviderGroupIdById(dataflowId);
    // Get the providers that belong to the given codes.
    for (String code : codes) {
      DataProviderVO providerVO = representativeControllerZuul.findDataProviderByCodeAndGroupId(code, dataProviderGroupId);
      if (providerVO == null) {
        LOG.error("The data provider {} does not exist", code);
        continue;
      }
      givenProviders.add(providerVO);
      providerIds.add(providerVO.getId());
    }

    if (givenProviders.isEmpty()) {
      throw new EEAException("No data providers where found with any of the given codes.");
    }

    // Find all the providers that belong to the current dataflow.
    List<RepresentativeVO> dataflowReps =
        representativeControllerZuul.findRepresentativesByDataFlowIdAndProviderIdList(dataflowId, providerIds);

    // Keep only the ids.
    Set<Long> allowedIds = new HashSet<>();
    if (dataflowReps != null) {
      for (RepresentativeVO rep : dataflowReps) {
        if (rep != null && rep.getDataProviderId() != null) {
          allowedIds.add(rep.getDataProviderId());
        }
      }
    }

    // Keep only the providers that were found that belong to the current dataflow.
    List<DataProviderVO> result = new ArrayList<>(givenProviders.size());
    for (DataProviderVO dp : givenProviders) {
      if (allowedIds.contains(dp.getId())) {
        result.add(dp);
      }
    }

    if (result.isEmpty()) {
      throw new EEAException("The given providers don't belong to the dataflow.");
    }

    return result;
  }

    @Override
    public void etlExportParquet(Long datasetId, Long dataflowId, String tableSchemaId, Long jobId, String user, String processUUID, Boolean includeAttachments, String dataProviderCodes) {
        try {
            String folderPathStr =  exportDLPath + DATASET_PREFIX_FOR_EXPORT + datasetId;
            File folderPath = new File(folderPathStr);

            createLocalPathIfNotExists(jobId, folderPath);

            String localPath = exportDLPath + DATASET_PREFIX_FOR_EXPORT + datasetId + PARQUET_EXPORT_NAME + jobId;
            updateJobProcess(datasetId, dataflowId, jobId, user, processUUID);

            DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
            String s3Path = etlExportV5Service.getS3KeyPath(dataSetMetabaseVO, s3ServicePrivate);
            DatasetTypeEnum datasetType = dataSetMetabaseVO.getDatasetTypeEnum();

            String tableName = null;
            if (StringUtils.isNotBlank(tableSchemaId)) {
                tableName = datasetSchemaService.getTableSchemaName(dataSetMetabaseVO.getDatasetSchema(), tableSchemaId);
            }

            // Split the codes by "," and prevent duplicates.
            Set<String> codes = StringUtils.isBlank(dataProviderCodes) ? Collections.emptySet() : Arrays.stream(dataProviderCodes.split(","))
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .map(String::toUpperCase)
                  .collect(Collectors.toCollection(LinkedHashSet::new));

            DownloadFilter filter;

            if (codes.isEmpty()){
              filter = etlExportV5Service.buildParquetFilters(s3Path, includeAttachments, tableName, null, s3ServicePrivate);
            } else {
              // Only valid for Data Collections or EU-Datasets.
              if (!(DatasetTypeEnum.COLLECTION.equals(datasetType) ||
                  DatasetTypeEnum.EUDATASET.equals(datasetType))) {
                throw new IllegalArgumentException("Parameter 'dataProviderCodes' was provided but Dataset is not a Data Collection or EU Dataset.");
              }

              // Providers list from codes.
              List<DataProviderVO> providers = resolveProvidersByCodes(dataflowId, codes);

              // If it passes resolveProvidersByCodes method exception.
              if (providers == null || providers.isEmpty()) {
                LOG.warn("No providers matched codes {} for dataflowId {}. Producing empty ZIP.", codes, dataflowId);
                File emptyZip = new File(localPath + ".zip");
                try (FileOutputStream fos = new FileOutputStream(emptyZip);
                     ZipOutputStream zos = new ZipOutputStream(fos)) {}
                finishJob(datasetId, dataflowId, jobId, user, processUUID);
                return;
              }

              List<Long> providerIds = providers.stream()
                  .map(DataProviderVO::getId)
                  .filter(java.util.Objects::nonNull)
                  .collect(Collectors.toList());

              filter = etlExportV5Service.buildParquetFilters(s3Path, includeAttachments, tableName, providerIds, s3ServicePrivate);
            }


            // Download path based on filtering result.
            File filePath = s3HelperPrivate.downloadFileFromS3Locally(s3Path, localPath, filter);

            if (filePath.exists()) {
                zipFolder(jobId, localPath);
            } else {
                LOG.warn("Creating an empty zip file because path:  {} does not exist", localPath);
                // Create an empty ZIP file
                File emptyZip = new File(localPath + ".zip");
                try (FileOutputStream fos = new FileOutputStream(emptyZip);
                     ZipOutputStream zos = new ZipOutputStream(fos)) {}
            }
            finishJob(datasetId, dataflowId, jobId, user, processUUID);
        }
        catch (Exception e) {
            exceptionHandling(datasetId, dataflowId, jobId, user, processUUID, e);
        }
    }

    @Override
    public JobVO retrieveOrAddImportJob(ImportFileInDremioInfo importFileInDremioInfo, String fmeJobId, Long jobId) throws Exception {
        JobVO job = null;
        JobStatusEnum jobStatus;
        if (fmeJobId != null || jobId != null) {
            if (fmeJobId != null){
                jobControllerZuul.updateFmeCallbackJobParameter(fmeJobId, true);
                job = jobControllerZuul.findJobByFmeJobId(fmeJobId);
                if(job == null){
                    //wait for 3 seconds and try again.
                    Thread.sleep(3000);
                    LOG.info("Retrying finding job with fmeJobId {}", fmeJobId);
                    job = jobControllerZuul.findJobByFmeJobId(fmeJobId);
                }
                if (job != null) {
                    jobId = job.getId();
                    importFileInDremioInfo.setJobId(jobId);
                    LOG.info("Incoming Fme Related Import job with fmeJobId {}, jobId {} and datasetId {}", fmeJobId, jobId, importFileInDremioInfo.getDatasetId());
                }
            }
            else{
                job = jobControllerZuul.findJobById(jobId);
                if(job == null){
                    //wait for 3 seconds and try again.
                    Thread.sleep(3000);
                    LOG.info("Retrying finding job with id {}", jobId);
                    job = jobControllerZuul.findJobById(jobId);
                }
            }
        }

        if(job != null){
            LOG.info("For import {} found job with id {}", importFileInDremioInfo, jobId);

            if(job.getJobStatus().equals(JobStatusEnum.CANCELED) || job.getJobStatus().equals(JobStatusEnum.CANCELED_BY_ADMIN)) {
                LOG.info("Job {} is cancelled. Exiting import!", job.getId());
                return job;
            }
            else if(job.getJobStatus().equals(JobStatusEnum.QUEUED)){
                jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.IN_PROGRESS);
            }
        }else{
            LOG.info("For import {} could not find job with id {}", importFileInDremioInfo, jobId);
            //check if there is already an import job with status IN_PROGRESS for the specific datasetId
            List<Long> datasetIds = new ArrayList<>();
            datasetIds.add(importFileInDremioInfo.getDatasetId());
            jobStatus = jobControllerZuul.checkEligibilityOfJob(JobTypeEnum.IMPORT.getValue(), false, importFileInDremioInfo.getDataflowId(), importFileInDremioInfo.getProviderId(), datasetIds);
            jobId = jobControllerZuul.addImportJob(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getDataflowId(), importFileInDremioInfo.getProviderId(), importFileInDremioInfo.getTableSchemaId(), importFileInDremioInfo.getFileName(),
                    importFileInDremioInfo.getReplaceData(), importFileInDremioInfo.getIntegrationId(),importFileInDremioInfo.getPreparationCode(), importFileInDremioInfo.getDelimiter(), jobStatus, fmeJobId, null);
            importFileInDremioInfo.setJobId(jobId);
            if(jobStatus.getValue().equals(JobStatusEnum.REFUSED.getValue())){
                LOG.info("Added import job with id {} for datasetId {} with status REFUSED", jobId, importFileInDremioInfo.getDatasetId());
                datasetService.releaseImportRefusedNotification(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getDataflowId(), importFileInDremioInfo.getTableSchemaId(), importFileInDremioInfo.getFileName());
                throw new ResponseStatusException(HttpStatus.LOCKED, EEAErrorMessage.IMPORTING_FILE_DATASET);
            }
            job = jobControllerZuul.findJobById(jobId);
            if(job == null){
                //wait for 3 seconds and try again.
                Thread.sleep(3000);
                LOG.info("Retrying finding job with id {}", jobId);
                job = jobControllerZuul.findJobById(jobId);
            }
        }
        return job;
    }

    /**
     * Ensure the folder path exists
     * @param jobId The job id
     * @param folderPath The folder path
     *
     * @throws IOException The exception
     */
    private void createLocalPathIfNotExists(Long jobId, File folderPath) throws IOException {
        if (!folderPath.exists()) {
            LOG.info("Folder {} does not exist. Creating it.", folderPath);
            if (!folderPath.mkdirs()) {
                LOG.error("Failed to create the folder for jobId {} at {}", jobId, folderPath);
                throw new IOException("Failed to create directory: " + folderPath);
            }
        }
    }

    /**
     * Updating the job process
     *
     * @param datasetId The dataset id
     * @param dataflowId The dataflow id
     * @param jobId The job id
     * @param user The user id
     * @param processUUID The process UUID
     */
    private void updateJobProcess(Long datasetId, Long dataflowId, Long jobId, String user, String processUUID) {
        processControllerZuul.updateProcess(datasetId, dataflowId, ProcessStatusEnum.IN_QUEUE, ProcessTypeEnum.FILE_EXPORT,
            processUUID, user, defaultFileExportProcessPriority, false);
        if (jobId !=null) {
            JobProcessVO jobProcessVO = new JobProcessVO(null, jobId, processUUID);
            jobProcessControllerZuul.save(jobProcessVO);
        }
        processControllerZuul.updateProcess(datasetId, dataflowId, ProcessStatusEnum.IN_PROGRESS, ProcessTypeEnum.FILE_EXPORT,
            processUUID, user, defaultFileExportProcessPriority, false);
    }

    /**
     * Handle the exceptions
     *
     * @param datasetId The dataset id
     * @param dataflowId The dataflow id
     * @param jobId The job id
     * @param user The user id
     * @param processUUID The process UUID
     * @param e The exception
     */
    private void exceptionHandling(Long datasetId, Long dataflowId, Long jobId, String user, String processUUID, Exception e) {
        LOG.error("EtlExport failed for jobId {} Error: {}", jobId, e.getMessage());
        processControllerZuul.updateProcess(datasetId, dataflowId, ProcessStatusEnum.CANCELED, ProcessTypeEnum.FILE_EXPORT,
            processUUID, user, defaultFileExportProcessPriority, false);
        if (jobId != null) {
            jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);
        }
    }

    /**
     * Finish the process
     *
     * @param dataflowId The dataflow id
     * @param jobId The job id
     * @param user The user id
     * @param processUUID The process UUID
     */
    private void finishJob(Long datasetId, Long dataflowId, Long jobId, String user, String processUUID) {
        processControllerZuul.updateProcess(datasetId, dataflowId, ProcessStatusEnum.FINISHED, ProcessTypeEnum.FILE_EXPORT,
            processUUID, user, defaultFileExportProcessPriority, false);
        if (jobId !=null) {
            jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FINISHED);
        }
    }

    /***
     * Zipping the folder and deleting unzipped folder
     *
     * @param jobId The job id
     * @param localPath The folder to zip path
     * @throws IOException The Exception
     */
    private void zipFolder(Long jobId, String localPath) throws IOException {
        File unZippedFile = new File(localPath);
        File zippedFile = new File(localPath + ".zip");

        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zippedFile)) {
            ZipUtils.addFolderToZip(unZippedFile, unZippedFile, zos);
        } catch (Exception e) {
            LOG.error("There was an error when zipping the files for etl export jobId {} folderToZipPath {}", jobId, localPath, e);
            throw e;
        } finally {
            FileUtils.deleteDirectory(unZippedFile);
        }
    }

    /**
     * Checks for duplicate value in field
     *
     * @param datasetId The dataset id
     * @param dataflowId The dataflow id
     * @param providerId The provider id
     * @param tableName The table schema id
     * @param fieldVO The field object
     */
    public Boolean duplicateFieldValueExists(Long datasetId, Long dataflowId, Long providerId, String tableName, FieldVO fieldVO){
        S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableName, tableName, S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3IcebergTablePathResolver.setIsIcebergTable(true);
        String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
        //check if table exists
        if (s3HelperPrivate.checkFolderExist(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH) && dremioHelperService.checkFolderPromoted(s3IcebergTablePathResolver, tableName)){
            String getRecordIdOfDuplicate =  "SELECT " + PARQUET_RECORD_ID_COLUMN_HEADER + " FROM " + icebergTablePath + " WHERE " + UtilityClass.addQuotesToFieldNames(fieldVO.getName()) + " = '" + fieldVO.getValue() + "' LIMIT 1";
            try {
                String recordId = dremioJdbcTemplate.queryForObject(getRecordIdOfDuplicate, String.class);
                LOG.info("Found duplicate value in table {} for field {} and value {} in recordId {}", icebergTablePath, fieldVO.getName(), fieldVO.getValue(), recordId);
                return true;
            } catch (EmptyResultDataAccessException e) { //no duplicate
                return false;
            }
        }
        else{
            return false;
        }
    }

    @Override
    public byte[] getGeometryAsGeoJson(
            DataSetMetabaseVO dataset,
            TableSchemaVO tableSchemaVO,
            String geometryColumnFieldId,
            String recordId
    ) throws EEAException {

        // 1. ROOT resolve
        S3PathResolver s3RootResolver =
                s3ServicePrivate.getS3PathResolverByDatasetType(
                        dataset,
                        tableSchemaVO.getNameTableSchema(),
                        false
                );

        if (BooleanUtils.isTrue(
                datasetTableService.icebergTableIsCreated(
                        dataset.getId(),
                        tableSchemaVO.getIdTableSchema()
                )
        )) {
            s3RootResolver.setIsIcebergTable(true);
        } else {
            s3RootResolver.setIsIcebergTable(false);
        }

        // 2. Check folder exists
        if (!s3HelperPrivate.checkTableNameDCFolderExist(s3RootResolver)) {
            throw new EEAException(
                    EEAErrorMessage.RECORD_NOTFOUND +
                            " Table folder does not exist for table " +
                            tableSchemaVO.getNameTableSchema()
            );
        }

        dremioAutoPromotionService.ensureSafeFolderPromotion(dataset, s3RootResolver);

        if (!dremioHelperService.checkFolderPromoted(
                s3RootResolver,
                s3RootResolver.getTableName()
        )) {
            throw new EEAException(
                    EEAErrorMessage.RECORD_NOTFOUND +
                            " Table not promoted in Dremio: " +
                            tableSchemaVO.getNameTableSchema()
            );
        }

        String tablePath =
                s3ServicePrivate.getTableAsFolderQueryPath(
                        s3RootResolver,
                        S3_TABLE_AS_FOLDER_QUERY_PATH
                );

        // 3. Build Dremio SQL
        String sql =
                "SELECT ST_AsGeoJSON(" + UtilityClass.addQuotesToFieldNames(geometryColumnFieldId) + ") " +
                        "FROM " + tablePath + " " +
                        "WHERE " + PARQUET_RECORD_ID_COLUMN_HEADER + " = '" + recordId + "'";

        LOG.info("Executing geometry GeoJSON query: {}", sql);

        try {
            String geoJson =
                    dremioJdbcTemplate.queryForObject(sql, String.class);

            if (geoJson == null || geoJson.trim().isEmpty()) {
                throw new EEAException(
                        EEAErrorMessage.RECORD_NOTFOUND +
                                " Empty geometry for recordId " + recordId
                );
            }

            return geoJson.getBytes(StandardCharsets.UTF_8);

        } catch (EmptyResultDataAccessException e) {
            throw new EEAException(
                    EEAErrorMessage.RECORD_NOTFOUND +
                            " No geometry found for recordId " + recordId
            );
        }
    }

    /**
     * Etl import dataset DL.
     *
     * @param datasetId    the dataset id
     * @param dataflowId the dataflowa id
     * @param providerId   the provider id
     * @param replaceData
     * @param tableSchemaId
     * @param delimiter
     * @param filePathInS3
     * @param jobId
     * @throws Exception
     */
    @Async
    @Override
    public void etlImportDataset(Long datasetId, Long dataflowId, Long providerId, Boolean replaceData, String tableSchemaId, String delimiter, String filePathInS3, Long jobId, DataFlowVO dataFlowVO, DataSetMetabaseVO dataSetMetabaseVO) throws Exception {
        File etlImportFolder = null;
        try {
            DatasetTypeEnum datasetTypeEnum = datasetService.getDatasetType(datasetId);
            List<TableSchemaIdNameVO> tableSchemaIdNameVOS = datasetSchemaService.getTableSchemasIds(datasetId);
            if(StringUtils.isNotBlank(tableSchemaId)){ //check requirements for only one table
                Boolean continueEtlImport = checkSchemaRequirementsForEtlImport(datasetId, dataSetMetabaseVO.getDatasetSchema(), tableSchemaId, jobId, datasetTypeEnum);
                if(!continueEtlImport){
                    return;
                }
            }
            else{ //check requirements for all tables
                for(TableSchemaIdNameVO tableSchemaIdNameVO: tableSchemaIdNameVOS){
                    Boolean continueEtlImport = checkSchemaRequirementsForEtlImport(datasetId, dataSetMetabaseVO.getDatasetSchema(), tableSchemaIdNameVO.getIdTableSchema(), jobId, datasetTypeEnum);
                    if(!continueEtlImport){
                        return;
                    }
                }
            }

            //download file from filePathInS3 and store it in the disk.
            String fileExtension = getFileExtensionFromFilePath(filePathInS3);
            if (!fileExtension.equals(".zip")) {
                LOG.error("Failing etlImport with jobId {} because file is not zip", jobId);
                jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.FAILED, JobInfoEnum.ERROR_IMPORT_FAILED_FILE_NOT_ZIP, null);
                return;
            }

            etlImportFolder = createEtlImportFolder(datasetId, jobId);


            Set<String> tableNamesSet = null;
            if(StringUtils.isBlank(tableSchemaId)){
                tableNamesSet = tableSchemaIdNameVOS.stream().map(vo -> vo.getNameTableSchema().toLowerCase()).collect(Collectors.toSet());
            }
            else{
                tableNamesSet = tableSchemaIdNameVOS.stream().filter(vo -> tableSchemaId.equals(vo.getIdTableSchema()))
                        .map(vo -> vo.getNameTableSchema().toLowerCase()).collect(Collectors.toSet());
            }
            Map<String, Boolean> attachmentsExistPerTableName = new HashMap();

            List<File> csvFiles = storeAndUnzipEtlImportZipFile(datasetId, filePathInS3, fileExtension, jobId, etlImportFolder, tableNamesSet, attachmentsExistPerTableName);
            if(csvFiles == null){ // job has already failed
                return;
            }

            DataSetSchema datasetSchema = schemasRepository.findByIdDataSetSchema(new ObjectId(dataSetMetabaseVO.getDatasetSchema()));
            String providerCode = null;
            if(providerId != null && providerId != 0L){
                DataProviderVO dataProviderVO = representativeControllerZuul.findDataProviderById(providerId);
                providerCode = dataProviderVO.getCode();
            }

            ImportFileInDremioInfo importFileInDremioInfo = new ImportFileInDremioInfo(jobId, datasetId, dataflowId, providerId, tableSchemaId, null, replaceData, delimiter, null, providerCode, null);
            if (DatasetTypeEnum.REFERENCE.equals(datasetTypeEnum) && dataFlowVO.getStatus() == TypeStatusEnum.DRAFT) {
                importFileInDremioInfo.setUpdateReferenceFolder(true);
            }
            else{
                importFileInDremioInfo.setUpdateReferenceFolder(false);
            }
            importFileInDremioInfo.setIsEtlImport(true);
            importFileInDremioInfo.setEtlImportFolderPath(etlImportFolder.getPath());
            importFileInDremioInfo.setAttachmentsExistPerTableName(attachmentsExistPerTableName);

            try {
                parquetConverterService.convertCsvFilesToParquetFiles(importFileInDremioInfo, csvFiles, datasetSchema, dataSetMetabaseVO);
            }
            catch(Exception e){
                LOG.error("Error in convertCsvFilesToParquetFiles for job {} ", importFileInDremioInfo, e);
                if(StringUtils.isBlank(importFileInDremioInfo.getErrorMessage())){
                    //something went wrong but no specific error is documented. job needs to be failed
                    jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);
                }
            }
            finally {
                //update job status and info
                finishEtlImportJob(importFileInDremioInfo);
            }
            LOG.info("Completed etlImport for jobId {}", jobId);
        }
        catch (Exception e){
            LOG.error("Unexpected error! Error in etlImportDatasetDL for jobId {} and filePathInS3 {} Message: {}", jobId, filePathInS3, e.getMessage());
            jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);

        }
        finally {
            //remove csv and attachment files from disk
            if (etlImportFolder != null && etlImportFolder.exists()){
                FileUtils.deleteDirectory(etlImportFolder);
            }
            //remove file from public S3 if job is finished
           if (jobControllerZuul.findJobById(jobId).getJobStatus() == JobStatusEnum.FINISHED) {
                s3HelperPublic.deleteFileFromS3(filePathInS3);
            }

        }
    }

    protected List<File> storeAndUnzipEtlImportZipFile(Long datasetId, String filePathInS3, String fileExtension, Long jobId, File etlImportFolder, Set<String> tableNamesSet, Map<String, Boolean> attachmentsExistPerTableName) throws Exception {
        boolean attachmentsFolderSeen = false;
        String[] filePathInS3Split = filePathInS3.split("/");
        String fileNameInS3 = filePathInS3Split[filePathInS3Split.length - 1];
        String filePathStructure = "/" + datasetId + "/" + fileNameInS3;
        File s3File = null;
        try {
            LOG.info("For jobId {} downloading file from s3 in path {} with fileExtension {}", jobId, filePathInS3, fileExtension);
            s3File = s3HelperPublic.getFileFromS3(filePathInS3, filePathStructure.replace(fileExtension, ""), importPath, fileExtension);
        } catch (Exception e) {
            LOG.error("For jobId {} could not find file {} in public s3. Error: {}", jobId, filePathInS3, e.getMessage());
            jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.FAILED, JobInfoEnum.ERROR_NO_FILE_IN_S3, null);
            return null;
        }

        List<File> files = new ArrayList<>();
        try (InputStream input = new FileInputStream(s3File);
             ZipInputStream zip = new ZipInputStream(input)) {

            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                try {

                    String entryName = entry.getName();

                    if (entryName.equals(ETL_IMPORT_ATTACHMENTS_FOLDER + "/")
                            || entryName.startsWith(ETL_IMPORT_ATTACHMENTS_FOLDER + "/")) {

                        if (!attachmentsFolderSeen) {
                            attachmentsFolderSeen = true;

                            // initialize all tables to false
                            for (String table : tableNamesSet) {
                                attachmentsExistPerTableName.put(table, false);
                            }
                        }
                    }

                    File file = new File(etlImportFolder, entryName);
                    String canonicalPath = file.getCanonicalPath();
                    String basePath = etlImportFolder.getCanonicalPath() + File.separator;

                    // Zip Slip protection
                    if (!canonicalPath.startsWith(basePath)) {
                        LOG.error("Zip slip attempt: {}. jobId {}", entryName, jobId);
                        continue;
                    }

                    boolean isDirectory = entry.isDirectory();
                    boolean isRootLevel = !entryName.contains("/");

                    boolean allowed = false;
                    boolean isCsv = false;

                    if (isRootLevel) {
                        if (!isDirectory) {
                            // root-level files must be CSV
                            String mimeType = datasetService.getMimetype(entryName);
                            isCsv = FileTypeEnum.CSV.getValue().equalsIgnoreCase(mimeType);
                            if (isCsv) {
                                // extract table name from "tableName.csv"
                                String baseName = entryName.substring(0, entryName.lastIndexOf('.')).toLowerCase();
                                allowed = tableNamesSet.contains(baseName);
                            } else {
                                allowed = false;
                            }
                        }
                    } else {
                        // nested entries must follow strict attachment rules
                        allowed = isValidAttachmentEntry(entryName, isDirectory);
                        if (allowed && !isDirectory && attachmentsFolderSeen) {

                            // entryName : attachments/tableName/attachment
                            String relative = entryName.substring((ETL_IMPORT_ATTACHMENTS_FOLDER + "/").length());

                            String tableNameInZip = relative.substring(0, relative.indexOf('/')).toLowerCase();

                            if (attachmentsExistPerTableName.containsKey(tableNameInZip)) {
                                attachmentsExistPerTableName.put(tableNameInZip, true);
                            }
                        }
                    }


                    if (!allowed) {
                        LOG.error("Ignored ZIP entry (invalid contract): {}. jobId {}", entryName, jobId);
                        continue;
                    }

                    // do not create directories yet because we only create them if they are not empty
                    if (isDirectory) {
                        continue;
                    }

                    // Ensure parent directories exist
                    file.getParentFile().mkdirs();

                    // Write file
                    try (FileOutputStream output = new FileOutputStream(file)) {
                        IOUtils.copyLarge(zip, output);
                        LOG.info("Stored file {}. jobId {}", file.getPath(), jobId);
                    }

                    // gather root csv files for import
                    if (isCsv && isRootLevel) {
                        files.add(file);
                    }
                } finally {
                    if(entry != null){
                        zip.closeEntry();
                    }
                }
            }
            // check if csv files for import were provided
            if (!files.isEmpty()) {
                return files;
            } else {
                LOG.error("Failing etlImport with jobId {} because zip does not contain csv files for import", jobId);
                jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.FAILED, JobInfoEnum.ERROR_ZIP_FOLDER_WITHOUT_CSV_FILES, null);
                return null;
            }
        } catch (Exception e) {
            LOG.error("Unexpected error! Error in fileManagement for etlImportDL with jobId {} Message: {}", jobId, e.getMessage());
            throw e;
        }
    }

    protected static boolean isValidAttachmentEntry(String entryName, boolean isDirectory) {
        final String prefix = ETL_IMPORT_ATTACHMENTS_FOLDER + "/";
        // must start with "attachments/"
        if (!entryName.startsWith(prefix)) {
            return false;
        }

        // if entryName is exactly "attachments" or "attachments/" invalid (empty folder)
        if (entryName.equals(ETL_IMPORT_ATTACHMENTS_FOLDER) || entryName.equals(prefix)) {
            return false;
        }

        // now it's safe to take substring
        String relative = entryName.substring(prefix.length());

        // remove trailing slash
        if (relative.endsWith("/")) {
            relative = relative.substring(0, relative.length() - 1);
        }

        String[] parts = relative.split("/");

        // only allow attachments/tableName/attachmentFile
        return parts.length == 2 && !isDirectory;
    }

    protected File createEtlImportFolder(Long datasetId, Long jobId) throws Exception {
        //store zip file
        File importParentfolder = new File(importPath + "/" + datasetId);
        if (!importParentfolder.exists()) {
            importParentfolder.mkdir();
        }

        //create etlImport folder if it doesn't exist
        File etlImportFolder = new File(importParentfolder.getCanonicalPath() + "/" + String.format(ETL_IMPORT_FOLDER, jobId));
        if (!etlImportFolder.exists()) {
            etlImportFolder.mkdir();
        }

        return etlImportFolder;
    }

    protected Boolean checkSchemaRequirementsForEtlImport(Long datasetId, String datasetSchemaId, String tableSchemaId, Long jobId, DatasetTypeEnum datasetTypeEnum){
        TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);
        if (datasetTypeEnum.equals(REPORTING) || datasetTypeEnum.equals((DatasetTypeEnum.TEST))) {
            if (BooleanUtils.isTrue(tableSchemaVO.getReadOnly())) { //table should not be read only
                LOG.error("Failing etlImport with jobId {} because table is read only", jobId);
                jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.FAILED, JobInfoEnum.ERROR_IMPORT_FAILED_READ_ONLY_TABLE, null);
                return false;
            }
            if (BooleanUtils.isTrue(tableSchemaVO.getFixedNumber())) { // table should not have fixed number of records
                LOG.error("Failing etlImport with jobId {} because table has fixed number of records", jobId);
                jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.FAILED, JobInfoEnum.ERROR_IMPORT_FAILED_FIXED_NUM, null);
                return false;
            }
            Boolean readOnlyFieldsExist = tableSchemaVO.getRecordSchema().getFieldSchema().stream().anyMatch(FieldSchemaVO::getReadOnly);
            if (BooleanUtils.isTrue(readOnlyFieldsExist)) { //table should not have read only fields
                LOG.error("Failing etlImport with jobId {} because table contains read only fields", jobId);
                jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.FAILED, JobInfoEnum.ERROR_IMPORT_FAILED_READ_ONLY_FIELDS, null);
                return false;
            }
        }
        return true;
    }

    protected void finishEtlImportJob(ImportFileInDremioInfo importFileInDremioInfo){
        Long jobId = importFileInDremioInfo.getJobId();
        if(StringUtils.isNotBlank(importFileInDremioInfo.getErrorMessage())){
            if (EEAErrorMessage.ERROR_FILE_NAME_MATCHING.equals(importFileInDremioInfo.getErrorMessage())) {
                jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.CANCELED, JobInfoEnum.ERROR_WRONG_FILE_NAME, null);
            } else if (EEAErrorMessage.ERROR_FILE_NO_HEADERS_MATCHING.equals(importFileInDremioInfo.getErrorMessage())) {
                jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.CANCELED, JobInfoEnum.ERROR_NO_HEADERS_MATCHING, null);
            } else if (EEAErrorMessage.ERROR_IMPORT_EMPTY_FILES.equals(importFileInDremioInfo.getErrorMessage())) {
                jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.CANCELED, JobInfoEnum.ERROR_ALL_FILES_ARE_EMPTY, null);
            } else if(EEAErrorMessage.ERROR_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA.equals(importFileInDremioInfo.getErrorMessage())){
                jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.CANCELED, JobInfoEnum.ERROR_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA, null);
            } else if(EEAErrorMessage.ERROR_IMPORT_FAILED_WRONG_NUM_OF_RECORDS.equals(importFileInDremioInfo.getErrorMessage())){
                jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.CANCELED, JobInfoEnum.ERROR_IMPORT_FAILED_WRONG_NUM_OF_RECORDS, null);
            } else if(EEAErrorMessage.ERROR_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS.equals(importFileInDremioInfo.getErrorMessage())){
                jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.CANCELED, JobInfoEnum.ERROR_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS, null);
            } else if(EEAErrorMessage.ERROR_IMPORT_FAILED_READ_ONLY_TABLES.equals(importFileInDremioInfo.getErrorMessage())){
                jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.CANCELED, JobInfoEnum.ERROR_IMPORT_FAILED_READ_ONLY_TABLES, null);
            } else if(EEAErrorMessage.DREMIO_ENDPOINT_ERROR_RESPONSE.equals(importFileInDremioInfo.getErrorMessage())){
                jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.CANCELED, JobInfoEnum.ERROR_DREMIO_ENDPOINT_RESPONSE, null);
            } else if(EEAErrorMessage.ERROR_IMPORT_FILES_CONTAIN_WRONG_HEADERS.equals(importFileInDremioInfo.getErrorMessage())){
                jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.CANCELED, JobInfoEnum.ERROR_IMPORT_FILES_CONTAIN_WRONG_HEADERS, null);
            }
            else{
                jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);
            }
        }
        else{
            if(importFileInDremioInfo.getWarningMessages() != null && !importFileInDremioInfo.getWarningMessages().isEmpty() && StringUtils.isNotBlank(importFileInDremioInfo.getWarningMessages().get(0))) {
                //update job info message by the first warning
                String warningToUpdate = importFileInDremioInfo.getWarningMessages().get(0);
                JobInfoEnum jobInfoWarning = null;
                try {
                    jobInfoWarning = JobInfoEnum.fromValue(warningToUpdate, null);
                    jobControllerZuul.updateJobStatusAndInfo(jobId, JobStatusEnum.FINISHED, jobInfoWarning, null);
                } catch (Exception e) {
                    LOG.error("Could not find job info enum for value {} for job {} Error: {}", warningToUpdate, importFileInDremioInfo, e.getMessage());
                    //will not set up a job info message
                    jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FINISHED);
                }
            }
            else{
                jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FINISHED);
            }
        }
    }

    private String getPreparationsFilePath(Long datasetId, Long dataflowId, Long providerId, String fileName, boolean deleteFile, String preparationCode) {
        if (dataflowId == null){
            dataflowId = datasetService.getDataFlowIdById(datasetId);
        }
        if (providerId == null){
            providerId = 0L;
        }
        S3PathResolver s3PathResolver = new S3PathResolver(dataflowId, providerId, datasetId, null, fileName);
        s3PathResolver.setPath(LiteralConstants.S3_PREPARATION_PROVIDER_IMPORT_PATH);
        s3PathResolver.setDeleteFile(deleteFile);
        s3PathResolver.setPreparationCode(preparationCode);
        return s3ServicePublic.getS3Path(s3PathResolver);
    }
    @Override
    public JobPresignedUrlInfo generatePreparationImportPreSignedUrl(Long datasetId, Long dataflowId, Long providerId, String fileName, String preparationCode) {
        JobPresignedUrlInfo info = new JobPresignedUrlInfo();
        String filePathInS3 = getPreparationsFilePath(datasetId, dataflowId, providerId, fileName, false, preparationCode);
        info.setFilePathInS3(filePathInS3);
        info.setPresignedUrl(s3HelperPublic.generatePUTPreSignedUrl(filePathInS3));
        return info;
    }

    private File resolveImportFolder(String datasetId, String preparationCode) {

        File root = new File(importPath);

        if (StringUtils.isNotBlank(preparationCode)) {
            return new File(root, datasetId + "/" + preparationCode);
        }

        return new File(root, datasetId);
    }
}