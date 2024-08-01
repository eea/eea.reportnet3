package org.eea.dataset.service.impl;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.bson.Document;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.metabase.domain.DatasetTable;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.*;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.model.ImportFileInDremioInfo;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.AttachmentDLVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetRunningStatusEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
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
import org.eea.multitenancy.DatasetId;
import org.eea.multitenancy.TenantResolver;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static org.eea.utils.LiteralConstants.*;


@ImportDataLakeCommons
@Service
public class BigDataDatasetServiceImpl implements BigDataDatasetService {

    private static final Logger LOG = LoggerFactory.getLogger(BigDataDatasetServiceImpl.class);

    @Value("${importPath}")
    private String importPath;

    private static final int defaultImportProcessPriority = 20;

    @Autowired
    DatasetService datasetService;
    ParquetConverterService parquetConverterService;

    @Autowired
    JobControllerZuul jobControllerZuul;

    @Autowired
    JobProcessControllerZuul jobProcessControllerZuul;

    @Autowired
    DatasetMetabaseService datasetMetabaseService;

    @Autowired
    ProcessControllerZuul processControllerZuul;

    private FileTreatmentHelper fileTreatmentHelper;

    @Autowired
    private KafkaSenderUtils kafkaSenderUtils;

    @Autowired
    public RepresentativeControllerZuul representativeControllerZuul;

    @Autowired
    FileCommonUtils fileCommonUtils;

    @Autowired
    DatasetSchemaService datasetSchemaService;

    @Autowired
    SpatialDataHandling  spatialDataHandling;

    @Autowired
    DatasetTableService datasetTableService;

    @Autowired
    DataFlowControllerZuul dataFlowControllerZuul;

    private final S3Service s3ServicePrivate;
    private final S3Service s3ServicePublic;
    private final S3Helper s3HelperPrivate;
    private final S3Helper s3HelperPublic;
    private final DremioHelperService dremioHelperService;
    private JdbcTemplate dremioJdbcTemplate;

    private SchemasRepository schemasRepository;

    private static final String HEADER_NAME = "headerName";
    private static final String TYPE_DATA = "typeData";
    private static final String ID_RECORD = "idRecord";
    private static final String ID_TABLE_SCHEMA = "idTableSchema";
    private static final String NAME_TABLE_SCHEMA = "nameTableSchema";
    private static final String VALUE = "refValue";
    private static final String LABEL = "refLabel";




    public BigDataDatasetServiceImpl(@Qualifier("publicS3Helper") S3Helper s3HelperPublic, S3Helper s3HelperPrivate, DremioHelperService dremioHelperService,
                                     ParquetConverterService parquetConverterService, JdbcTemplate dremioJdbcTemplate, SchemasRepository schemasRepository) {
        this.s3HelperPrivate = s3HelperPrivate;
        this.s3HelperPublic = s3HelperPublic;
        this.s3ServicePublic = s3HelperPublic.getS3Service();
        this.s3ServicePrivate = s3HelperPrivate.getS3Service();
        this.dremioHelperService = dremioHelperService;
        this.parquetConverterService = parquetConverterService;
        this.fileTreatmentHelper = parquetConverterService.getFileTreatmentHelper();
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.schemasRepository = schemasRepository;
    }


    @Override
    public void importBigData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId,
                              MultipartFile file, Boolean replace, Long integrationId, String delimiter, Long jobId,
                              String fmeJobId, DataFlowVO dataflowVO) throws Exception {
        String filePathInS3 = null;
        String fileName = (file != null) ? file.getOriginalFilename() : null;
        JobStatusEnum jobStatus = JobStatusEnum.IN_PROGRESS;
        ImportFileInDremioInfo importFileInDremioInfo = new ImportFileInDremioInfo();
        File s3File = null;
        JobVO job = null;
        try {
            if (dataflowId == null){
                dataflowId = datasetService.getDataFlowIdById(datasetId);
            }

            if (fmeJobId != null || jobId != null) {
                if (fmeJobId != null){
                    jobControllerZuul.updateFmeCallbackJobParameter(fmeJobId, true);
                    job = jobControllerZuul.findJobByFmeJobId(fmeJobId);
                    if (job != null) {
                        jobId = job.getId();
                        LOG.info("Incoming Fme Related Import job with fmeJobId {}, jobId {} and datasetId {}", fmeJobId, jobId, datasetId);
                    }
                }
                else{
                    job = jobControllerZuul.findJobById(jobId);
                }
            }
            if(job != null){
                if(job.getJobStatus().equals(JobStatusEnum.CANCELED) || job.getJobStatus().equals(JobStatusEnum.CANCELED_BY_ADMIN)) {
                    LOG.info("Job {} is cancelled. Exiting import!", job.getId());
                    return;
                }
                else if(job.getJobStatus().equals(JobStatusEnum.QUEUED)){
                    jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.IN_PROGRESS);
                }
                if(job.getParameters().get("filePathInS3") != null) {
                    filePathInS3 = job.getParameters().get("filePathInS3").toString();
                }
            }else{
                //check if there is already an import job with status IN_PROGRESS for the specific datasetId
                List<Long> datasetIds = new ArrayList<>();
                datasetIds.add(datasetId);
                jobStatus = jobControllerZuul.checkEligibilityOfJob(JobTypeEnum.IMPORT.getValue(), false, dataflowId, providerId, datasetIds);
                jobId = jobControllerZuul.addImportJob(datasetId, dataflowId, providerId, tableSchemaId, fileName, replace, integrationId, delimiter, jobStatus, fmeJobId, null);
                if(jobStatus.getValue().equals(JobStatusEnum.REFUSED.getValue())){
                    LOG.info("Added import job with id {} for datasetId {} with status REFUSED", jobId, datasetId);
                    datasetService.releaseImportRefusedNotification(datasetId, dataflowId, tableSchemaId, fileName);
                    throw new ResponseStatusException(HttpStatus.LOCKED, EEAErrorMessage.IMPORTING_FILE_DATASET);
                }
            }

            if(file == null){
                if(StringUtils.isBlank(filePathInS3)){
                    throw new EEAException("Empty file and file path");
                }
                String fileExtension = getFileExtensionFromFilePath(filePathInS3);
                LOG.info("For jobId {} downloading file from s3 in path {} with fileExtension {}", jobId, filePathInS3, fileExtension);

                String[] filePathInS3Split = filePathInS3.split("/");
                String fileNameInS3 = filePathInS3Split[filePathInS3Split.length - 1];
                String filePathStructure = "/" + datasetId + "/" + fileNameInS3;
                File folder = new File(importPath + "/" + datasetId);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                s3File = s3HelperPublic.getFileFromS3(filePathInS3, filePathStructure.replace(fileExtension, ""), importPath, fileExtension);
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

            if(StringUtils.isNotBlank(fmeJobId)){
                //retrieve the replace data value from the job
                replace = (Boolean) job.getParameters().get("replace");
            }

            importFileInDremioInfo = new ImportFileInDremioInfo(jobId, datasetId, dataflowId, providerId, tableSchemaId, fileName, replace, delimiter, integrationId, providerCode);

            DatasetTypeEnum datasetType = datasetService.getDatasetType(importFileInDremioInfo.getDatasetId());
            if (DatasetTypeEnum.REFERENCE.equals(datasetType) && dataflowVO.getStatus() == TypeStatusEnum.DRAFT) {
                importFileInDremioInfo.setUpdateReferenceFolder(true);
            }
            else{
                importFileInDremioInfo.setUpdateReferenceFolder(false);
            }

            LOG.info("Importing file to s3 {}", importFileInDremioInfo);
            importDatasetDataToDremio(importFileInDremioInfo, file, s3File);
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

    private void importDatasetDataToDremio(ImportFileInDremioInfo importFileInDremioInfo, MultipartFile fileFromApi, File fileFromS3) throws Exception {

        if (importFileInDremioInfo.getDelimiter() != null && importFileInDremioInfo.getDelimiter().length() > 1) {
            LOG.error("Error when importing file data to s3 {}. The size of the delimiter cannot be greater than 1", importFileInDremioInfo);
            datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(), DatasetRunningStatusEnum.ERROR_IN_IMPORT);
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

        // We add a lock to the Release process
        datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(), DatasetRunningStatusEnum.IMPORTING);
        Map<String, Object> mapCriteria = new HashMap<>();
        mapCriteria.put("dataflowId", importFileInDremioInfo.getDataflowId());
        mapCriteria.put("dataProviderId", importFileInDremioInfo.getProviderId());
        if (importFileInDremioInfo.getProviderId() != null) {
            datasetService.createLockWithSignature(LockSignature.RELEASE_SNAPSHOTS, mapCriteria, SecurityContextHolder.getContext().getAuthentication().getName());
        }
        handleZipFile(importFileInDremioInfo, fileFromApi, fileFromS3, schema);
    }

    private void handleZipFile(ImportFileInDremioInfo importFileInDremioInfo, MultipartFile fileFromApi, File fileFromS3, DataSetSchema schema) throws Exception {
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
        if (fileFromApi != null) {
            filesToImport = storeImportFiles(fileFromApi, importFileInDremioInfo, integrationVO, mimeType);
        } else {
            filesToImport = handleAlreadyStoredImportFiles(fileFromS3, importFileInDremioInfo, integrationVO, mimeType);
        }

        if (integrationVO != null && filesToImport.size() != 0) {
            handleFmeRequest(integrationVO, importFileInDremioInfo, filesToImport.get(0), mimeType);
        } else {
            List<File> correctFilesForImport = checkCsvFiles(importFileInDremioInfo, schema, filesToImport, integrationVO, mimeType);
            parquetConverterService.convertCsvFilesToParquetFiles(importFileInDremioInfo, correctFilesForImport, schema);
        }
    }

    private void handleFmeRequest(IntegrationVO integrationVO, ImportFileInDremioInfo importFileInDremioInfo, File file, String mimeType) throws EEAException {
        try {
            fileTreatmentHelper.prepareFmeFileProcess(importFileInDremioInfo.getDatasetId(), file, integrationVO, mimeType, importFileInDremioInfo.getTableSchemaId(),
                    false, importFileInDremioInfo.getJobId());
        }
        catch (Exception e){
            throw new EEAException("Could not prepare fme request for job id " + importFileInDremioInfo.getJobId());
        }
    }

    private List<File> checkCsvFiles(ImportFileInDremioInfo importFileInDremioInfo, DataSetSchema schema, List<File> files, IntegrationVO integrationVO, String mimeType)
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
                else {
                    //TODO handle xlsx files
                }
            } else {
                sendWrongFileNameWarning = true;
                LOG.error("Importing file {} to s3. {}. There's no table with that fileName", fileName, importFileInDremioInfo);
                datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(), DatasetRunningStatusEnum.ERROR_IN_IMPORT);
                numberOfWrongFiles++;
                if (numberOfWrongFiles == files.size()) {
                    sendWrongFileNameWarning = false;
                    DatasetTypeEnum type = datasetService.getDatasetType(importFileInDremioInfo.getDatasetId());
                    EventType eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
                            ? EventType.IMPORT_REPORTING_FAILED_NAMEFILE_EVENT
                            : EventType.IMPORT_DESIGN_FAILED_NAMEFILE_EVENT;

                    datasetService.failImportJobAndProcess(importFileInDremioInfo.getProcessId(), importFileInDremioInfo.getDatasetId(), tableSchemaId, fileName, eventType, JobInfoEnum.ERROR_WRONG_FILE_NAME);
                    importFileInDremioInfo.setErrorMessage(EEAErrorMessage.ERROR_FILE_NAME_MATCHING);
                    throw new EEAException(EEAErrorMessage.ERROR_FILE_NAME_MATCHING);
                }
            }
        }
        if(sendWrongFileNameWarning){
            importFileInDremioInfo.setWarningMessage(JobInfoEnum.WARNING_SOME_FILENAMES_DO_NOT_MATCH_TABLES.getValue(null));
            jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.WARNING_SOME_FILENAMES_DO_NOT_MATCH_TABLES, null);
        }
        return correctFilesForImport;

    }

    private List<File> storeImportFiles(MultipartFile fileFromApi, ImportFileInDremioInfo importFileInDremioInfo, IntegrationVO integrationVO, String multipartFileMimeType) throws Exception {
        List<File> files = new ArrayList<>();

        // Prepare the folder where files will be stored
        File root = new File(importPath);
        File folder = new File(root, importFileInDremioInfo.getDatasetId().toString());
        String saveLocationPath = folder.getCanonicalPath();
        if (!folder.exists()) {
            folder.mkdir();
        }
        //store zip file
        File storedMultipartFile = new File(saveLocationPath + "/" + importFileInDremioInfo.getFileName());
        try (OutputStream os = new FileOutputStream(storedMultipartFile)) {
            os.write(fileFromApi.getBytes());
        }

        try (InputStream input = fileFromApi.getInputStream()) {

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

                        entry = zip.getNextEntry();
                        files.add(file);

                    }
                } catch (Exception e) {
                    LOG.error("Unexpected error! Error in storeImportFiles {}. Message: {}", importFileInDremioInfo, e.getMessage());
                    throw e;
                }
            } else {
                File file = new File(folder, fileFromApi.getOriginalFilename());

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
                    try {
                        ZipFile zipFile = new ZipFile(file);
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

    private List<File> handleAlreadyStoredImportFiles(File fileFromS3, ImportFileInDremioInfo importFileInDremioInfo, IntegrationVO integrationVO, String multipartFileMimeType) throws Exception {
        List<File> files = new ArrayList<>();
        String fileMimeType = datasetService.getMimetype(importFileInDremioInfo.getFileName());

        // Prepare the folder where files will be stored
        File root = new File(importPath);
        File folder = new File(root, importFileInDremioInfo.getDatasetId().toString());
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

                        entry = zip.getNextEntry();
                        files.add(file);

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
                eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
                        ? EventType.IMPORT_REPORTING_FAILED_NAMEFILE_EVENT
                        : EventType.IMPORT_DESIGN_FAILED_NAMEFILE_EVENT;
            } else if (EEAErrorMessage.ERROR_FILE_NO_HEADERS_MATCHING.equals(importFileInDremioInfo.getErrorMessage())) {
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.ERROR_NO_HEADERS_MATCHING, null);
                eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
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
            }
            else {
                eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
                        ? EventType.IMPORT_REPORTING_FAILED_EVENT
                        : EventType.IMPORT_DESIGN_FAILED_EVENT;
            }
            datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(),
                    DatasetRunningStatusEnum.ERROR_IN_IMPORT);
            if(StringUtils.isNotBlank(importFileInDremioInfo.getProcessId())) {
                processControllerZuul.updateProcess(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getDataflowId(),
                        ProcessStatusEnum.CANCELED, ProcessTypeEnum.IMPORT, importFileInDremioInfo.getProcessId(),
                        SecurityContextHolder.getContext().getAuthentication().getName(), defaultImportProcessPriority, null);
            }

            jobStatus = JobStatusEnum.CANCELED;
        } else {
            datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(),
                    DatasetRunningStatusEnum.IMPORTED);

            if(StringUtils.isNotBlank(importFileInDremioInfo.getProcessId())) {
                processControllerZuul.updateProcess(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getDataflowId(),
                        ProcessStatusEnum.FINISHED, ProcessTypeEnum.IMPORT, importFileInDremioInfo.getProcessId(),
                        SecurityContextHolder.getContext().getAuthentication().getName(), defaultImportProcessPriority, null);
            }

            eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
                    ? EventType.IMPORT_REPORTING_COMPLETED_EVENT
                    : EventType.IMPORT_DESIGN_COMPLETED_EVENT;

            jobStatus = JobStatusEnum.FINISHED;

            // Delete the csv files.
            deleteFilesFromDirectoryWithExtension(new String[]{".csv", ".parquet"}, importFileInDremioInfo.getDatasetId().toString());
        }

        if (jobId!=null) {
            jobControllerZuul.updateJobStatus(jobId, jobStatus);
        }

        kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value, notificationVO);

        if(StringUtils.isNotBlank(importFileInDremioInfo.getWarningMessage())) {
            //send warning
            if(importFileInDremioInfo.getWarningMessage().equals(JobInfoEnum.WARNING_SOME_FILENAMES_DO_NOT_MATCH_TABLES.getValue(null))) {
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.WARNING_SOME_FILENAMES_DO_NOT_MATCH_TABLES, null);
                NotificationVO notificationWarning = NotificationVO.builder()
                        .user(SecurityContextHolder.getContext().getAuthentication().getName())
                        .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
                kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_NAMEFILE_WARNING_EVENT,
                        value, notificationWarning);
            }
            else if (importFileInDremioInfo.getWarningMessage().equals(JobInfoEnum.WARNING_SOME_FILES_ARE_EMPTY.getValue(null))){
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.WARNING_SOME_FILES_ARE_EMPTY, null);
                NotificationVO notificationWarning = NotificationVO.builder()
                        .user(SecurityContextHolder.getContext().getAuthentication().getName())
                        .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
                kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_EMPTY_FILES_WARNING_EVENT,
                        value, notificationWarning);
            }
            else if(importFileInDremioInfo.getWarningMessage().equals(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA.getValue(null))){
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.WARNING_SOME_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA, null);
                NotificationVO notificationWarning = NotificationVO.builder()
                        .user(SecurityContextHolder.getContext().getAuthentication().getName())
                        .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
                kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_FIXED_NUM_WITHOUT_REPLACE_DATA_WARNING_EVENT,
                        value, notificationWarning);
            }
            else if(importFileInDremioInfo.getWarningMessage().equals(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_WRONG_NUM_OF_RECORDS.getValue(null))){
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.WARNING_SOME_IMPORT_FAILED_WRONG_NUM_OF_RECORDS, null);
                NotificationVO notificationWarning = NotificationVO.builder()
                        .user(SecurityContextHolder.getContext().getAuthentication().getName())
                        .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
                kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_WRONG_NUM_OF_RECORDS_WARNING_EVENT,
                        value, notificationWarning);
            }
            else if(importFileInDremioInfo.getWarningMessage().equals(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS.getValue(null))){
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.WARNING_SOME_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS, null);
                NotificationVO notificationWarning = NotificationVO.builder()
                        .user(SecurityContextHolder.getContext().getAuthentication().getName())
                        .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
                kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_ONLY_READ_ONLY_FIELDS_WARNING_EVENT,
                        value, notificationWarning);
            }
            else if(importFileInDremioInfo.getWarningMessage().equals(JobInfoEnum.WARNING_SOME_IMPORT_FAILED_READ_ONLY_TABLES.getValue(null))){
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.WARNING_SOME_IMPORT_FAILED_READ_ONLY_TABLES, null);
                NotificationVO notificationWarning = NotificationVO.builder()
                        .user(SecurityContextHolder.getContext().getAuthentication().getName())
                        .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
                kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_READ_ONLY_TABLES_WARNING_EVENT,
                        value, notificationWarning);
            }
        }

        if (importFileInDremioInfo.getProviderId() != null) {
            fileTreatmentHelper.releaseLockReleasingProcess(importFileInDremioInfo.getDatasetId());
        }
    }

    private void deleteFilesFromDirectoryWithExtension(String[] extensionsToDelete, String datasetId){
        File root = new File(importPath);
        File folder = new File(root, datasetId);
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
    public void deleteTableData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId) throws Exception {
        String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
        TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);
        if(tableSchemaVO != null && BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable())
                && BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaId))) {
            throw new Exception("Can not delete table data because iceberg table is created");
        }
        String tableSchemaName = tableSchemaVO.getNameTableSchema();

        if(providerId == null){
            DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
            providerId = dataSetMetabaseVO.getDataProviderId();
        }
        if(providerId == null){
            providerId = 0L;
        }
        S3PathResolver s3ImportPathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaName, tableSchemaName, S3_IMPORT_FILE_PATH);
        //path in s3 for the folder that contains the stored csv files
        String s3PathForCsvFolder = s3ServicePrivate.getTableAsFolderQueryPath(s3ImportPathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH);

        //remove csv files that are related to the table
        parquetConverterService.removeCsvFilesThatWillBeReplaced(s3ImportPathResolver, tableSchemaName, s3PathForCsvFolder);

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
        if(datasetType == DatasetTypeEnum.REFERENCE){
            //demote reference table folder
            S3PathResolver s3ReferenceTablePathResolver = new S3PathResolver(s3TablePathResolver.getDataflowId(), s3TablePathResolver.getDataProviderId(), s3TablePathResolver.getDatasetId(), tableSchemaName, tableSchemaName, S3_DATAFLOW_REFERENCE_FOLDER_PATH);
            if (s3HelperPrivate.checkFolderExist(s3ReferenceTablePathResolver, S3_DATAFLOW_REFERENCE_FOLDER_PATH)) {
                dremioHelperService.demoteFolderOrFile(s3ReferenceTablePathResolver, tableSchemaName);
                //remove folders that contain the previous parquet files because data will be replaced
                s3HelperPrivate.deleteFolder(s3ReferenceTablePathResolver, S3_DATAFLOW_REFERENCE_FOLDER_PATH);
            }
        }
    }

    @Override
    public void deleteDatasetData(Long datasetId, Long dataflowId, Long providerId, Boolean deletePrefilledTables) throws Exception {

        String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
        List<TableSchemaIdNameVO> tableSchemas = datasetSchemaService.getTableSchemasIds(datasetId);
        for(TableSchemaIdNameVO entry: tableSchemas){
            TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(entry.getIdTableSchema(), datasetSchemaId);
            if(tableSchemaVO != null && BooleanUtils.isTrue(tableSchemaVO.getDataAreManuallyEditable())
                    && BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaVO.getIdTableSchema()))) {
                throw new Exception("Can not delete table data because iceberg table is created");
            }
        }

        DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
        if(providerId == null){
            providerId = dataSetMetabaseVO.getDataProviderId();
        }
        List<TableSchemaIdNameVO> tableSchemaIdNameVOs = datasetSchemaService.getTableSchemasIds(datasetId);
        for (TableSchemaIdNameVO tableSchemaIdNameVO: tableSchemaIdNameVOs){
            if(!deletePrefilledTables){
                //do not delete prefilled tables
                TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaIdNameVO.getIdTableSchema(), dataSetMetabaseVO.getDatasetSchema());
                if(tableSchemaVO.getToPrefill()){
                    LOG.info("The data for table with tableSchemaId {} for datasetId {} will not be deleted because the table is prefilled.", tableSchemaIdNameVO.getIdTableSchema(), datasetId );
                    continue;
                }
            }
            deleteTableData(datasetId, dataflowId, providerId, tableSchemaIdNameVO.getIdTableSchema());
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
                                          String fieldName, String fileName, String recordId) {

        byte[] attachmentContent;

        //retrieve file from s3
        String fileNameInS3 = fieldName + "_" + recordId + "." + FilenameUtils.getExtension(fileName);
        S3PathResolver s3PathResolver = new S3PathResolver(dataflowId, (providerId != null)? providerId : 0L, datasetId, tableSchemaName, fileNameInS3, S3_ATTACHMENTS_PATH);
        String attachmentPathInS3 = s3ServicePrivate.getS3Path(s3PathResolver);
        try {
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

    @Override
    public void convertParquetToIcebergTable(Long datasetId, Long dataflowId, Long providerId, TableSchemaVO tableSchemaVO, String datasetSchemaId) throws Exception {
        providerId = providerId != null ? providerId : 0L;
        DatasetTypeEnum datasetType = datasetMetabaseService.getDatasetType(datasetId);
        String parquetTableQueryPathConstant = (datasetType == DatasetTypeEnum.REFERENCE ) ? S3_DATAFLOW_REFERENCE_QUERY_PATH : S3_TABLE_AS_FOLDER_QUERY_PATH;
        String parquetTableS3PathConstant = (datasetType == DatasetTypeEnum.REFERENCE ) ? S3_DATAFLOW_REFERENCE_FOLDER_PATH : S3_TABLE_NAME_FOLDER_PATH;

        S3PathResolver s3TablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), tableSchemaVO.getNameTableSchema(), parquetTableS3PathConstant);
        S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), tableSchemaVO.getNameTableSchema(), S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3IcebergTablePathResolver.setIsIcebergTable(true);
        String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

        //remove old iceberg table because it will be recreated
        if (s3HelperPrivate.checkFolderExist(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH)) {
            LOG.info("Removing iceberg files for table in path {}", icebergTablePath);
            dremioHelperService.demoteFolderOrFile(s3IcebergTablePathResolver, tableSchemaVO.getNameTableSchema());
            s3HelperPrivate.deleteFolder(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH);
        }

        DatasetTable datasetTableEntry = new DatasetTable(datasetId, datasetSchemaId, tableSchemaVO.getIdTableSchema(), true);

        if (!s3HelperPrivate.checkFolderExist(s3TablePathResolver, parquetTableS3PathConstant) ||
                !dremioHelperService.checkFolderPromoted(s3TablePathResolver, tableSchemaVO.getNameTableSchema())) {
            //parquet table does not exist and no iceberg table should be created
            datasetTableService.saveOrUpdateDatasetTableEntry(datasetTableEntry);
            return;
        }

        s3TablePathResolver.setPath(parquetTableQueryPathConstant);
        String parquetTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3TablePathResolver, parquetTableQueryPathConstant);
        dremioHelperService.createTableFromAnotherTable(parquetTablePath, icebergTablePath);

        datasetTableService.saveOrUpdateDatasetTableEntry(datasetTableEntry);
    }

    @Override
    public void convertIcebergToParquetTable(Long datasetId, Long dataflowId, Long providerId, TableSchemaVO tableSchemaVO, String datasetSchemaId) throws Exception {
        providerId = providerId != null ? providerId : 0L;
        DatasetTypeEnum datasetType = datasetMetabaseService.getDatasetType(datasetId);

        S3PathResolver s3TablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), UUID.randomUUID().toString(), S3_TABLE_AS_FOLDER_QUERY_PATH);
        S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), tableSchemaVO.getNameTableSchema(), S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3IcebergTablePathResolver.setIsIcebergTable(true);

        String parquetTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3TablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
        String parquetInnerFolderQueryPath = parquetTablePath + ".\"" + s3TablePathResolver.getFilename() + "\"";
        String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);


        if (s3HelperPrivate.checkFolderExist(s3TablePathResolver, S3_TABLE_NAME_FOLDER_PATH)) {
            //remove old parquet table because it will be recreated
            dremioHelperService.demoteFolderOrFile(s3TablePathResolver, tableSchemaVO.getNameTableSchema());
            LOG.info("Removing parquet files for table in path {}", parquetTablePath);
            s3HelperPrivate.deleteFolder(s3TablePathResolver, S3_TABLE_NAME_FOLDER_PATH);
        }

        DatasetTable datasetTableEntry = new DatasetTable(datasetId, datasetSchemaId, tableSchemaVO.getIdTableSchema(), false);

        if (!s3HelperPrivate.checkFolderExist(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH) ||
                !dremioHelperService.checkFolderPromoted(s3IcebergTablePathResolver, tableSchemaVO.getNameTableSchema())) {
            //iceberg table does not exist and no parquet table should be created
            datasetTableService.saveOrUpdateDatasetTableEntry(datasetTableEntry);
            return;
        }

        dremioHelperService.createTableFromAnotherTable(icebergTablePath, parquetInnerFolderQueryPath);
        //refresh the metadata
        dremioHelperService.refreshTableMetadataAndPromote(null, parquetTablePath, s3TablePathResolver, tableSchemaVO.getNameTableSchema());

        if(datasetType == DatasetTypeEnum.REFERENCE){
            s3TablePathResolver.setPath(S3_TABLE_NAME_FOLDER_PATH);
            createReferenceFolder(s3TablePathResolver);
        }

        //remove iceberg table
        dremioHelperService.demoteFolderOrFile(s3IcebergTablePathResolver, tableSchemaVO.getNameTableSchema());
        LOG.info("Removing iceberg files for table in path {}", icebergTablePath);
        //remove folders that contain the previous parquet files because data will be replaced
        if (s3HelperPrivate.checkFolderExist(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH)) {
            s3HelperPrivate.deleteFolder(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH);
        }

        datasetTableService.saveOrUpdateDatasetTableEntry(datasetTableEntry);
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
            dataProviderCode = (dataProviderVO.getCode() != null) ? "'" + dataProviderCode + "'" : dataProviderCode;
        }

        //check if table exists and if not create it
        if (!s3HelperPrivate.checkFolderExist(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH) || !dremioHelperService.checkFolderPromoted(s3IcebergTablePathResolver, tableSchemaName)) {
            //table does not exist, so we need to create it first
            StringBuilder createIcebergTable = new StringBuilder("CREATE TABLE " + icebergTablePath + " (");
            createIcebergTable.append(PARQUET_RECORD_ID_COLUMN_HEADER + " VARCHAR , " + PARQUET_PROVIDER_CODE_COLUMN_HEADER + " VARCHAR ");

            for(int i=0; i< records.get(0).getFields().size(); i++){
                FieldVO field = records.get(0).getFields().get(i);
                if (spatialDataHandling.getGeoJsonEnums().contains(field.getType())) {
                    createIcebergTable.append(", ").append(field.getName()).append(" VARBINARY ");
                } else {
                    createIcebergTable.append(", ").append(field.getName()).append(" VARCHAR ");
                }
            }
            createIcebergTable.append(" )");

            String createIcebergTableProcessId = dremioHelperService.executeSqlStatement(createIcebergTable.toString());
            dremioHelperService.checkIfDremioProcessFinishedSuccessfully(createIcebergTable.toString(), createIcebergTableProcessId, null);
        }

        for (RecordVO record: records){
            //create update query for the record
            StringBuilder insertQueryBuilder = new StringBuilder().append("INSERT INTO ").append(icebergTablePath).append(" (");
            insertQueryBuilder.append(PARQUET_RECORD_ID_COLUMN_HEADER + ", " + PARQUET_PROVIDER_CODE_COLUMN_HEADER);
            String recordId = UUID.randomUUID().toString();

            StringBuilder insertQueryValuesBuilder = new StringBuilder().append(") VALUES ('").append(recordId).append("', ").append(dataProviderCode);
            for (int i=0; i< record.getFields().size(); i++) {
                FieldVO field = record.getFields().get(i);
                insertQueryBuilder.append(", ").append(field.getName()).append(" ");
                if (spatialDataHandling.getGeoJsonEnums().contains(field.getType())) {
                    String fieldValue = (field.getValue() != null) ? field.getValue() : "";
                    String refactoredValue = spatialDataHandling.refactorQuery(fieldValue);
                    insertQueryValuesBuilder.append(", ").append(refactoredValue).append(" ");
                } else {
                    String fieldValue = "";
                    if (field.getValue() != null) {
                        if (field.getValue().matches(".*[^\u0000-\u007F].*")) {
                            fieldValue = "ENCODE('" + field.getValue() + "', 'UTF-8')";
                            insertQueryValuesBuilder.append(", ").append(fieldValue);
                        } else {
                            fieldValue = field.getValue();
                            insertQueryValuesBuilder.append(", '").append(fieldValue).append("' ");
                        }
                    } else {
                        insertQueryValuesBuilder.append(", '").append(fieldValue).append("' ");
                    }
                }
            }
            insertQueryValuesBuilder.append(" )");
            String finalInsertQuery = insertQueryBuilder + insertQueryValuesBuilder.toString();
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

        for (RecordVO record: records){
            //create update query for the record
            StringBuilder updateQueryBuilder = new StringBuilder().append("UPDATE ").append(icebergTablePath).append(" SET ");
            for(int i=0; i< record.getFields().size(); i++){
                FieldVO field = record.getFields().get(i);
                if (spatialDataHandling.getGeoJsonEnums().contains(field.getType()) && !field.getType().equals(DataType.POINT)) {
                    continue;
                }
                String fieldValue = (field.getValue() != null) ? field.getValue() : "";
                updateQueryBuilder.append(field.getName()).append(" = '").append(fieldValue).append("'");
                updateQueryBuilder.append((i != record.getFields().size() -1) ? ", " : " ");
            }
            if (updateQueryBuilder.length() >= 2 && updateQueryBuilder.substring(updateQueryBuilder.length() - 2).equals(", ")) {
                updateQueryBuilder.delete(updateQueryBuilder.length() - 2, updateQueryBuilder.length());
            }
            updateQueryBuilder.append(" WHERE " + PARQUET_RECORD_ID_COLUMN_HEADER + " = '").append(record.getId()).append("'");
            if (spatialDataHandling.geoJsonHeadersAreNotEmpty(tableSchemaVO)) {
                updateQueryBuilder = spatialDataHandling.fixQueryForUpdateSpatialData(updateQueryBuilder.toString(), true, tableSchemaVO);
            }
            String processId = dremioHelperService.executeSqlStatement(updateQueryBuilder.toString());
            dremioHelperService.checkIfDremioProcessFinishedSuccessfully(updateQueryBuilder.toString(), processId, 2000L);
        }
        //todo handle updateCascadePK
    }

    @Override
    public void updateField(Long dataflowId, Long providerId, Long datasetId, FieldVO field, String recordId, TableSchemaVO tableSchemaVO, boolean updateCascadePK) throws Exception{
        providerId = providerId != null ? providerId : 0L;
        S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), tableSchemaVO.getNameTableSchema(), S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3IcebergTablePathResolver.setIsIcebergTable(true);

        String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

        //create update query for the record
        StringBuilder updateQueryBuilder = new StringBuilder().append("UPDATE ").append(icebergTablePath).append(" SET ");
        String fieldValue = (field.getValue() != null) ? field.getValue() : "";
        updateQueryBuilder.append(field.getName()).append(" = '").append(fieldValue).append("'");
        updateQueryBuilder.append(" WHERE " + PARQUET_RECORD_ID_COLUMN_HEADER + " = '").append(recordId).append("'");
        if (spatialDataHandling.geoJsonHeadersAreNotEmpty(tableSchemaVO)) {
            updateQueryBuilder = spatialDataHandling.fixQueryForUpdateSpatialData(updateQueryBuilder.toString(), true, tableSchemaVO);
        }
        String processId = dremioHelperService.executeSqlStatement(updateQueryBuilder.toString());
        dremioHelperService.checkIfDremioProcessFinishedSuccessfully(updateQueryBuilder.toString(), processId, 2000L);

        //todo handle updateCascadePK
    }

    @Override
    public void deleteRecord(Long dataflowId, Long providerId, Long datasetId, TableSchemaVO tableSchemaVO, String recordId, boolean deleteCascadePK) throws Exception{
        providerId = providerId != null ? providerId : 0L;
        S3PathResolver s3TablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), UUID.randomUUID().toString(), S3_TABLE_AS_FOLDER_QUERY_PATH);
        S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), tableSchemaVO.getNameTableSchema(), S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3IcebergTablePathResolver.setIsIcebergTable(true);

        String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

        //check if we need to remove attachments
        List<FieldSchemaVO> fields = tableSchemaVO.getRecordSchema().getFieldSchema();
        for(FieldSchemaVO field: fields){
            if(field.getType() == DataType.ATTACHMENT){
                //get fileName
                String getFileNameQuery = "SELECT " + field.getName() + " FROM " + icebergTablePath + " WHERE " + PARQUET_RECORD_ID_COLUMN_HEADER + " = '" + recordId + "'";
                String getFileNameResult = dremioJdbcTemplate.queryForObject(getFileNameQuery, String.class);
                if(StringUtils.isNotBlank(getFileNameResult)){
                    removeAttachmentFromS3(dataflowId, providerId, datasetId, tableSchemaVO.getNameTableSchema(), field.getName(), FilenameUtils.getExtension(getFileNameResult), recordId);
                }
            }
        }

        //get current number of records
        String recordsCountQuery = "select count(record_id) from " + icebergTablePath;
        Long numberOfRecords = dremioJdbcTemplate.queryForObject(recordsCountQuery, Long.class);
        if(numberOfRecords != 1) {
            //we can remove the entry
            //create delete query for the record
            StringBuilder deleteQueryBuilder = new StringBuilder().append("DELETE FROM" + icebergTablePath + " ");
            deleteQueryBuilder.append(" WHERE " + PARQUET_RECORD_ID_COLUMN_HEADER + " = '" + recordId + "'");
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
        //todo handle deleteCascadePK
    }

    private void removeAttachmentFromS3(Long dataflowId, Long providerId, Long datasetId, String tableSchemaName, String fieldName, String extension, String recordId){
        String fileNameInS3 = fieldName + "_" + recordId + "." + extension;
        S3PathResolver s3PathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaName, fileNameInS3, S3_ATTACHMENTS_PATH);
        String attachmentPathInS3 = s3ServicePrivate.getS3Path(s3PathResolver);
        s3HelperPrivate.deleteFile(attachmentPathInS3);
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
                tableHeaders += ", " + providerCode + " AS " + PARQUET_PROVIDER_CODE_COLUMN_HEADER + ", ";
                tableHeaders += fieldSchemas.stream()
                        .map(FieldSchemaVO::getName)
                        .collect(Collectors.joining(","));

                //remove previous data if they exist
                S3PathResolver s3TablePathResolver = new S3PathResolver(designDataSetMetabaseVO.getDataflowId(), providerId, datasetIdForCreation, tableSchemaName, tableSchemaName, S3_TABLE_AS_FOLDER_QUERY_PATH);
                if (s3HelperPrivate.checkFolderExist(s3TablePathResolver, S3_TABLE_NAME_FOLDER_PATH)) {
                    //remove old parquet table because it will be recreated
                    dremioHelperService.demoteFolderOrFile(s3TablePathResolver, tableSchemaVO.getNameTableSchema());
                    s3HelperPrivate.deleteFolder(s3TablePathResolver, S3_TABLE_NAME_FOLDER_PATH);
                }


                String queryToCreatePrefilledTable = "CREATE TABLE " + dremioNewTableQueryPath + " AS SELECT " + tableHeaders + " FROM " + dremioDesignTableQueryPath;
                String processId = dremioHelperService.executeSqlStatement(queryToCreatePrefilledTable);
                dremioHelperService.checkIfDremioProcessFinishedSuccessfully(queryToCreatePrefilledTable, processId, null);

                //refresh the metadata
                dremioHelperService.refreshTableMetadataAndPromote(null, dremioNewTableQueryPath, s3NewTablePathResolver, tableSchemaName);
            }
            LOG.info("Created prefilled data for datasetId {} and table {} from designDatasetId {} ", datasetIdForCreation, tableSchemaVO.getNameTableSchema(), designDatasetId);
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
        else if(filePathInS3.endsWith(XML_TYPE)){
            fileExtension = XML_TYPE;
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

        String labelFieldName = null;
        String newLabelField = null;
        if (StringUtils.isNotBlank(conditionalSchemaId)) {
            Document referenceFieldSchema1 = schemasRepository.findFieldSchema(referenceDatasetSchemaId, conditionalSchemaId);
            newLabelField = referenceFieldSchema1.get(HEADER_NAME).toString();
        } else {
            labelFieldName = referenceFieldName;
        }

        if(!labelSchemaId.equals(idPk)){
            Document labelFieldSchema = schemasRepository.findFieldSchema(referenceDatasetSchemaId, labelSchemaId);
            labelFieldName = labelFieldSchema.get(HEADER_NAME).toString();
        }

        try {
            //retrieve the value and label from dremio.
            List<Map<String, Object>> linkValues = getLinkValuesWithLabelsFromReferencedDataset(referenceDatasetId, referenceTableSchemaId, referenceTableSchemaName,
                referenceFieldName, labelFieldName, conditionalValue, dataType, searchValue, newLabelField);

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
        return fieldsVO;
    }

    private List<Map<String, Object>> getLinkValuesWithLabelsFromReferencedDataset(Long datasetId, String tableSchemaId, String tableName,
                                                                                   String fieldName, String labelFieldName, String conditionalValue,
                                                                                   DataType dataType, String searchValue, String newLabelField){
        DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
        Long dataflowId = dataSetMetabaseVO.getDataflowId();
        long providerId = (dataSetMetabaseVO.getDataProviderId() != null) ? dataSetMetabaseVO.getDataProviderId() : 0L;


        S3PathResolver s3PathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableName, tableName, S3_TABLE_AS_FOLDER_QUERY_PATH);
        if(BooleanUtils.isTrue(datasetTableService.icebergTableIsCreated(datasetId, tableSchemaId))){
            s3PathResolver.setIsIcebergTable(true);
        }
        String tablePathInDremio = s3ServicePrivate.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

        String referenceLabel = newLabelField != null && !dataType.equals(DataType.NUMBER_INTEGER) ? newLabelField : labelFieldName;

        String selectQuery = "SELECT \"" + fieldName + "\" as " + VALUE + ", \"" + referenceLabel + "\" as " + LABEL
            + " FROM " + tablePathInDremio +
            " WHERE \"" + fieldName + "\" != '' AND \"" + fieldName + "\" IS NOT NULL";

        if (StringUtils.isNotBlank(searchValue)) {
            selectQuery += " AND ('"
                + searchValue + "' IS NULL "
                + "OR LOWER(" + VALUE + ") LIKE LOWER(CONCAT('%', '" + searchValue + "', '%')) "
                + "OR LOWER(" + LABEL + ") LIKE LOWER(CONCAT('%', '" + searchValue + "', '%'))"
                + ")";
        }

        if (labelFieldName == null && conditionalValue.isBlank()
            || (StringUtils.isNotBlank(labelFieldName) && StringUtils.isNotBlank(fieldName) && StringUtils.isNotBlank(newLabelField) && conditionalValue.isBlank())) {
            conditionalValue = "null";
        }
        if (StringUtils.isNotBlank(conditionalValue)) {
            String[] values = Arrays.stream(conditionalValue.split("[,;]"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toArray(String[]::new);

            String valuesList = Arrays.stream(values)
                .map(value -> "'" + value + "'")
                .collect(Collectors.joining(", "));

            String refValue = newLabelField != null ? newLabelField : VALUE;
            String refLabel = newLabelField != null ? newLabelField : LABEL;
            if (dataType.equals(DataType.NUMBER_INTEGER)) {
                selectQuery = selectQuery + " AND " + refValue + " IN (" + valuesList + ")";
                selectQuery = selectQuery + " ORDER BY " + refValue;
            } else {
                selectQuery = selectQuery + " AND " + refLabel + " IN (" + valuesList + ")";
                selectQuery = selectQuery + " ORDER BY " + refLabel;
            }
        }
        LOG.info("Query to execute in links: {}", selectQuery);
        return dremioJdbcTemplate.queryForList(selectQuery);
    }
}
