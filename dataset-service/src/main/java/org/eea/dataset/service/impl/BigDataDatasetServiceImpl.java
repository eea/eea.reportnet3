package org.eea.dataset.service.impl;

import feign.FeignException;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.impl.S3ServiceImpl;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.service.*;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.model.ImportFileInDremioInfo;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DatasetRunningStatusEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
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
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.util.*;
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

    @Autowired
    ParquetConverterService parquetConverterService;

    @Autowired
    JobControllerZuul jobControllerZuul;

    @Autowired
    JobProcessControllerZuul jobProcessControllerZuul;

    @Autowired
    DatasetMetabaseService datasetMetabaseService;

    @Autowired
    ProcessControllerZuul processControllerZuul;

    @Autowired
    private FileTreatmentHelper fileTreatmentHelper;

    @Autowired
    private KafkaSenderUtils kafkaSenderUtils;

    @Autowired
    private S3ServiceImpl s3Service;

    @Autowired
    public RepresentativeControllerZuul representativeControllerZuul;

    @Autowired
    public DremioHelperService dremioHelperService;

    @Autowired
    public S3Helper s3Helper;

    @Autowired
    FileCommonUtils fileCommonUtils;

    @Autowired
    DatasetSchemaService datasetSchemaService;


    @Override
    public void importBigData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId,
                              MultipartFile file, Boolean replace, Long integrationId, String delimiter, Long jobId,
                              String fmeJobId, String filePathInS3, DataFlowVO dataflowVO) throws Exception {
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
            }else{
                //check if there is already an import job with status IN_PROGRESS for the specific datasetId
                List<Long> datasetIds = new ArrayList<>();
                datasetIds.add(datasetId);
                jobStatus = jobControllerZuul.checkEligibilityOfJob(JobTypeEnum.IMPORT.getValue(), false, dataflowId, providerId, datasetIds);
                jobId = jobControllerZuul.addImportJob(datasetId, dataflowId, providerId, tableSchemaId, fileName, replace, integrationId, delimiter, jobStatus, fmeJobId);
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
                String[] filePathInS3Split = filePathInS3.split("/");
                String fileNameInS3 = filePathInS3Split[filePathInS3Split.length - 1];
                String filePathStructure = "/" + datasetId + "/" + fileNameInS3;
                File folder = new File(importPath + "/" + datasetId);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                if(filePathInS3.endsWith(".csv")) {
                    s3File = s3Helper.getFileFromS3(filePathInS3, filePathStructure.replace(".csv", ""), importPath, LiteralConstants.CSV_TYPE);
                    fileName = s3File.getName();
                }
                else if(filePathInS3.endsWith(".zip")){
                    s3File = s3Helper.getFileFromS3(filePathInS3, filePathStructure.replace(".zip", ""), importPath, LiteralConstants.ZIP_TYPE);
                    fileName = s3File.getName();
                }
                //todo handle other extensions
                //delete objects ?
            }

            //Retrieve providerId and providerCode
            List<Long> datasetIds = new ArrayList<>();
            datasetIds.add(datasetId);
            String providerCode = null;
            if(providerId == null){
                DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
                providerId = dataSetMetabaseVO.getDataProviderId();
            }
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
                    //TODO check read only, prefilled, fixed number of records etc
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
                    importFileInDremioInfo.setSendWrongFileNameWarning(sendWrongFileNameWarning);
                    throw new EEAException(EEAErrorMessage.ERROR_FILE_NAME_MATCHING);
                }
            }
        }
        if(sendWrongFileNameWarning){
            jobControllerZuul.updateJobInfo(importFileInDremioInfo.getJobId(), JobInfoEnum.WARNING_SOME_FILENAMES_DO_NOT_MATCH_TABLES, null);
        }
        importFileInDremioInfo.setSendWrongFileNameWarning(sendWrongFileNameWarning);
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
            } else {
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
        // If importing a zip a file doesn't match with the table and the process ignores it, we send
        // a warning notification
        if (importFileInDremioInfo.getSendWrongFileNameWarning() != null && importFileInDremioInfo.getSendWrongFileNameWarning()) {
            NotificationVO notificationWarning = NotificationVO.builder()
                    .user(SecurityContextHolder.getContext().getAuthentication().getName())
                    .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_NAMEFILE_WARNING_EVENT,
                    value, notificationWarning);
        }

        if(importFileInDremioInfo.getSendEmptyFileWarning()){
            NotificationVO notificationWarning = NotificationVO.builder()
                    .user(SecurityContextHolder.getContext().getAuthentication().getName())
                    .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_EMPTY_FILES_WARNING_EVENT,
                    value, notificationWarning);
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
    public String generateImportPresignedUrl(Long datasetId, Long dataflowId, Long providerId){
        if (dataflowId == null){
            dataflowId = datasetService.getDataFlowIdById(datasetId);
        }
        if (providerId == null){
            providerId = 0L;
        }
        S3PathResolver s3PathResolver = new S3PathResolver(dataflowId, providerId, datasetId);
        s3PathResolver.setPath(LiteralConstants.S3_PROVIDER_IMPORT_PATH);
        String filePath = s3Service.getS3Path(s3PathResolver);
        return s3Helper.generatePresignedUrl(filePath);
    }

    @Override
    public void deleteTableData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId, String tableSchemaName) throws Exception {
        if(tableSchemaName == null) {
            String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
            tableSchemaName = datasetSchemaService.getTableSchemaName(datasetSchemaId, tableSchemaId);
        }
        if(providerId == null){
            DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
            providerId = dataSetMetabaseVO.getDataProviderId();
        }
        if(providerId == null){
            providerId = 0L;
        }
        S3PathResolver s3ImportPathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaName, tableSchemaName, S3_IMPORT_FILE_PATH);
        //path in s3 for the folder that contains the stored csv files
        String s3PathForCsvFolder = s3Service.getTableAsFolderQueryPath(s3ImportPathResolver, S3_IMPORT_TABLE_NAME_FOLDER_PATH);

        //remove csv files that are related to the table
        parquetConverterService.removeCsvFilesThatWillBeReplaced(s3ImportPathResolver, tableSchemaName, s3PathForCsvFolder);

        S3PathResolver s3TablePathResolver = new S3PathResolver(dataflowId, providerId, datasetId, tableSchemaName, tableSchemaName, S3_TABLE_NAME_FOLDER_PATH);
        //remove folders that contain the previous parquet files
        if (s3Helper.checkFolderExist(s3TablePathResolver, S3_TABLE_NAME_FOLDER_PATH)) {
            //demote table folder
            dremioHelperService.demoteFolderOrFile(s3TablePathResolver, tableSchemaName);
            s3Helper.deleteFolder(s3TablePathResolver, S3_TABLE_NAME_FOLDER_PATH);
        }
    }

    @Override
    public void deleteDatasetData(Long datasetId, Long dataflowId, Long providerId, Boolean deletePrefilledTables) throws Exception {
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
            deleteTableData(datasetId, dataflowId, providerId, tableSchemaIdNameVO.getIdTableSchema(), tableSchemaIdNameVO.getNameTableSchema());
        }
    }

}
