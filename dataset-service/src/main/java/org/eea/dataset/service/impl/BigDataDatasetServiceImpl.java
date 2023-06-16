package org.eea.dataset.service.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.service.*;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.model.ImportFileInDremioInfo;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DatasetRunningStatusEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
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
import java.util.zip.ZipInputStream;


@Service
public class BigDataDatasetServiceImpl implements BigDataDatasetService {

    private static final Logger LOG = LoggerFactory.getLogger(BigDataDatasetServiceImpl.class);

    @Value("${importPath}")
    private String importPath;

    private static final int defaultImportProcessPriority = 20;

    @Autowired
    DatasetService datasetService;

    @Autowired
    S3HandlerService s3HandlerService;

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


    @Override
    public void importBigData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId,
                              MultipartFile file, Boolean replace, Long integrationId, String delimiter, String fmeJobId) throws Exception {

        /*
         * Part 2:
         *
         * Add job and handle it
         * */

        /*
         * Part 3:
         *
         * Add checks for wrong filenames or sth
         * */

        /*
         * Part 4:
         *
         * Pass jobId in methods for logging purposes
         * */


        JobStatusEnum jobStatus = JobStatusEnum.IN_PROGRESS;
        Long jobId = null;
        try {
            if (dataflowId == null){
                dataflowId = datasetService.getDataFlowIdById(datasetId);
            }

            JobVO job = null;
            // TODO fme checks
            //check if there is already an import job with status IN_PROGRESS for the specific datasetId
            List<Long> datasetIds = new ArrayList<>();
            datasetIds.add(datasetId);
            jobStatus = jobControllerZuul.checkEligibilityOfJob(JobTypeEnum.IMPORT.getValue(), false, dataflowId, providerId, datasetIds);
            jobId = jobControllerZuul.addImportJob(datasetId, dataflowId, providerId, tableSchemaId, file.getOriginalFilename(), replace, integrationId, delimiter, jobStatus, fmeJobId);
            if(jobStatus.getValue().equals(JobStatusEnum.REFUSED.getValue())){
                LOG.info("Added import job with id {} for datasetId {} with status REFUSED", jobId, datasetId);
                datasetService.releaseImportRefusedNotification(datasetId, dataflowId, tableSchemaId, file.getOriginalFilename());
                throw new ResponseStatusException(HttpStatus.LOCKED, EEAErrorMessage.IMPORTING_FILE_DATASET);
            }

            ImportFileInDremioInfo importFileInDremioInfo = new ImportFileInDremioInfo(jobId, datasetId, dataflowId, providerId, tableSchemaId, file.getOriginalFilename(), replace, delimiter, integrationId);

            //LOG.info("Importing file to s3 for jobId {}, dataflowId {}, datasetId {} and tableSchemaId {}. ReplaceData is {}", jobId, dataflowId, datasetId, tableSchemaId, replace);
            LOG.info("Importing file to s3 {}", importFileInDremioInfo);
            importDatasetDataToDremio(importFileInDremioInfo, file);
            //TODO update geometries
            //TODO set up errorMessage
            finishImportProcess(importFileInDremioInfo);
            LOG.info("Successfully imported file to s3 {}", importFileInDremioInfo);
        } catch (EEAException e) {
            LOG.error("File import failed: for jobId {} dataflowId={} datasetId={}, tableSchemaId={}, fileName={} ", jobId, dataflowId, datasetId,
                    tableSchemaId, file.getOriginalFilename(), e);
            if (jobId!=null) {
                jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.IMPORTING_FILE_DATASET);
        } catch (Exception e) {
            String fileName = (file != null) ? file.getName() : null;
            LOG.error("Unexpected error! Error importing file {} to s3 for jobId {} datasetId {} providerId {} and tableSchemaId {} ", fileName, jobId, datasetId, providerId, tableSchemaId, e);
            if (jobId!=null && jobStatus != JobStatusEnum.REFUSED) {
                jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);
            }
            throw e;
        }



        /*
         * Part 5:
         *
         * Send file to specific folder in s3
         * */

        /*
         * Part 6:
         *
         * Case where zip file is in s3 and we need to download it first
         * */

        /*
         * Part 7:
         *
         * Case where we get notification from s3 that zip file has been uploaded (queued import job must be added)
         * */


        /*
         * Part 8:
         *
         * Add job status info
         * */
    }

    private void importDatasetDataToDremio(ImportFileInDremioInfo importFileInDremioInfo, MultipartFile file) throws Exception {

        if (importFileInDremioInfo.getDelimiter() != null && importFileInDremioInfo.getDelimiter().length() > 1) {
            LOG.error("Error when importing file data to s3 {}. The size of the delimiter cannot be greater than 1", importFileInDremioInfo);
            datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(),
                    DatasetRunningStatusEnum.ERROR_IN_IMPORT);
            throw new EEAException("The size of the delimiter cannot be greater than 1");
        }

        //if there is already a process created for the import then it should be updated instead of creating a new one
        String processUUID = null;
        Boolean processExists = false;
        // TODO fme check if process exists
        processUUID = UUID.randomUUID().toString();
        importFileInDremioInfo.setProcessId(processUUID);

        Boolean defaultReleaseStatusToBeChanged = null;

        DataSetSchema schema = datasetService.getSchemaIfReportable(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getTableSchemaId());
        processControllerZuul.updateProcess(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getDataflowId(), ProcessStatusEnum.IN_QUEUE, ProcessTypeEnum.IMPORT, processUUID,
                SecurityContextHolder.getContext().getAuthentication().getName(), defaultImportProcessPriority, defaultReleaseStatusToBeChanged);

        if(importFileInDremioInfo.getJobId() != null && !processExists){
            JobProcessVO jobProcessVO = new JobProcessVO(null, importFileInDremioInfo.getJobId(), processUUID);
            jobProcessControllerZuul.save(jobProcessVO);
        }

        if (null == schema) {
            throw new EEAException("Dataset is not reportable: datasetId=" + importFileInDremioInfo.getDatasetId() + ", tableSchemaId=" + importFileInDremioInfo.getTableSchemaId() + ", fileName=" + file.getOriginalFilename());
        }

        // We add a lock to the Release process
        DataSetMetabaseVO datasetMetabaseVO = datasetMetabaseService.findDatasetMetabase(importFileInDremioInfo.getDatasetId());
        datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(), DatasetRunningStatusEnum.IMPORTING);
        Map<String, Object> mapCriteria = new HashMap<>();
        mapCriteria.put("dataflowId", datasetMetabaseVO.getDataflowId());
        mapCriteria.put("dataProviderId", datasetMetabaseVO.getDataProviderId());
        if (datasetMetabaseVO.getDataProviderId() != null) {
            datasetService.createLockWithSignature(LockSignature.RELEASE_SNAPSHOTS, mapCriteria, SecurityContextHolder.getContext().getAuthentication().getName());
        }
        // now the view is not updated, update the check to false
        datasetService.updateCheckView(importFileInDremioInfo.getDatasetId(), false);
        // delete the temporary table from etlExport
        datasetService.deleteTempEtlExport(importFileInDremioInfo.getDatasetId());

        handleZipFile(importFileInDremioInfo, file, schema);
    }

    private void handleZipFile(ImportFileInDremioInfo importFileInDremioInfo, MultipartFile file, DataSetSchema schema) throws Exception {
        Boolean processWasUpdated = processControllerZuul.updateProcess(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getDataflowId(),
                ProcessStatusEnum.IN_PROGRESS, ProcessTypeEnum.IMPORT, importFileInDremioInfo.getProcessId(),
                SecurityContextHolder.getContext().getAuthentication().getName(), 0, null);

        if(!processWasUpdated) {
            throw new Exception("Could not update process to status IN_PROGRESS for processId=" + importFileInDremioInfo.getProcessId() + " and jobId "+ importFileInDremioInfo.getJobId());
        }

        String originalFileName = file.getOriginalFilename();
        String mimeType = datasetService.getMimetype(originalFileName);

        IntegrationVO integrationVO = null;
        /* TODO fme
        if (integrationId != null) {
            integrationVO = getIntegrationVO(integrationId);
            if (null == integrationVO) {
                LOG.error("Error. Integration {} not found for datasetId {} dataflowId {} and processId {}", integrationId, datasetId, dataflowId, processId);
            }
        }
         */

        //TODO fme handling
        List<File> filesToImport = storeImportFiles(file, importFileInDremioInfo.getDatasetId());
        checkCsvFiles(importFileInDremioInfo, schema, filesToImport, integrationVO, mimeType);
        Map<String, String> parquetFileNamesAndPaths =  parquetConverterService.convertCsvFilesToParquetFiles(importFileInDremioInfo, filesToImport, schema);
        for (Map.Entry<String, String> parquetFileNameAndPath : parquetFileNamesAndPaths.entrySet()) {
            s3HandlerService.uploadFileToBucket("reportnet", "", parquetFileNameAndPath.getKey(), parquetFileNameAndPath.getValue());
        }
    }

    private void checkCsvFiles(ImportFileInDremioInfo importFileInDremioInfo, DataSetSchema schema, List<File> files, IntegrationVO integrationVO, String mimeType)
            throws EEAException {

        //TODO fme prepareFmeFileProcess
        LOG.info("Checking csv files for datasetId {} and files {}", importFileInDremioInfo.getDatasetId(), files);

        // delete precious data if necessary
        /* TODO handle replaceData
        wipeData(datasetId, tableSchemaId, replace);
        LOG.info("Data has been wiped during rn3FileProcessIntoTasks datasetId {}, files {}", datasetId, files);
         */

        Boolean guessTableName = null == importFileInDremioInfo.getTableSchemaId();
        Boolean sendWrongFileNameWarning = false;
        int numberOfWrongFiles = 0;
        for (File file : files) {
            String fileName = file.getName();

            if (guessTableName) {
                String tableSchemaId = fileTreatmentHelper.getTableSchemaIdFromFileName(schema, fileName);
                if (StringUtils.isNotBlank(tableSchemaId)){
                    importFileInDremioInfo.setTableSchemaId(tableSchemaId);
                }
            }

            if (!guessTableName || StringUtils.isNotBlank(importFileInDremioInfo.getTableSchemaId())) {
                // obtains the file type from the extension
                if (fileName == null) {
                    throw new EEAException(EEAErrorMessage.FILE_NAME);
                }
                final String mimeTypeInDb = datasetService.getMimetype(fileName).toLowerCase();
                // validates file types for the data load
                fileTreatmentHelper.validateFileType(mimeTypeInDb);

                if (FileTypeEnum.getEnum(mimeTypeInDb.toLowerCase()) == FileTypeEnum.CSV) {
                    //TODO check read only, prefilled, fixed number of records etc
                }
                /* TODO handle xlsx files
                if (FileTypeEnum.getEnum(mimeTypeInDb.toLowerCase()) == FileTypeEnum.XLSX) {

                }
                 */
            } else {
                sendWrongFileNameWarning = true;
                LOG.error("Importing file to s3 for datasetId {} failed: fileName={}. There's no table with that fileName", importFileInDremioInfo.getDatasetId(), fileName);
                datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(), DatasetRunningStatusEnum.ERROR_IN_IMPORT);
                numberOfWrongFiles++;
                if (numberOfWrongFiles == files.size()) {
                    sendWrongFileNameWarning = false;
                    DatasetTypeEnum type = datasetService.getDatasetType(importFileInDremioInfo.getDatasetId());
                    EventType eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
                            ? EventType.IMPORT_REPORTING_FAILED_NAMEFILE_EVENT
                            : EventType.IMPORT_DESIGN_FAILED_NAMEFILE_EVENT;

                    datasetService.failImportJobAndProcess(importFileInDremioInfo.getProcessId(), importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getTableSchemaId(), fileName, eventType, JobInfoEnum.ERROR_WRONG_FILE_NAME);
                    throw new EEAException(EEAErrorMessage.ERROR_FILE_NAME_MATCHING);
                }
            }
            LOG.info("Checking csv file for datasetId {} and file {} and tableSchemaId {}", importFileInDremioInfo.getDatasetId(), file.getName(), importFileInDremioInfo.getTableSchemaId());
        }
        importFileInDremioInfo.setSendWrongFileNameWarning(sendWrongFileNameWarning);

    }

    private List<File> storeImportFiles(MultipartFile multipartFile, Long datasetId) throws Exception {
        List<File> files = new ArrayList<>();

        try (InputStream input = multipartFile.getInputStream()) {

            // Prepare the folder where files will be stored
            File root = new File(importPath);
            File folder = new File(root, datasetId.toString());
            String saveLocationPath = folder.getCanonicalPath();

            if (!folder.exists()) {
                folder.mkdir();
            }

            /* TODO fme
            if (null == integrationVO && "zip".equalsIgnoreCase(multipartFileMimeType)) {
             */

            try (ZipInputStream zip = new ZipInputStream(input)) {
                ZipEntry entry = zip.getNextEntry();

                try {
                    while (null != entry) {
                        String entryName = entry.getName();
                        String mimeType = datasetService.getMimetype(entryName);
                        File file = new File(folder, entryName);
                        String filePath = file.getCanonicalPath();

                        // Prevent Zip Slip attack or skip if the entry is a directory
                        if ((entryName.split("/").length > 1)
                                || !FileTypeEnum.CSV.getValue().equalsIgnoreCase(mimeType) || entry.isDirectory()
                                || !filePath.startsWith(saveLocationPath + File.separator)) {
                            LOG.error("Ignored file from ZIP: {}", entryName);
                            entry = zip.getNextEntry();
                            continue;
                        }

                        // Store the file in the persistence volume
                        try (FileOutputStream output = new FileOutputStream(file)) {
                            IOUtils.copyLarge(zip, output);
                            LOG.info("Stored file {}", file.getPath());
                        } catch (Exception e) {
                            LOG.error("Unexpected error! Error in copyLarge for saveLocationPath {}. Message: {}", saveLocationPath, e.getMessage());
                            throw e;
                        }

                        entry = zip.getNextEntry();
                        files.add(file);
                    }
                    // Queue import tasks for stored files
                    if (!files.isEmpty()) {
                        return files;
                    } else {
                        datasetMetabaseService.updateDatasetRunningStatus(datasetId, DatasetRunningStatusEnum.ERROR_IN_IMPORT);
                        throw new EEAException("Error trying to import a zip file to s3 for datasetId " + datasetId + ". Empty zip file");
                    }
                } catch (Exception e) {
                    LOG.error("Unexpected error processing file {} for datasetId {}", multipartFile.getOriginalFilename(), datasetId, e);
                    throw e;
                }
            } catch (Exception e) {
                LOG.error("Unexpected error! Error in unzipAndStore for datasetId {}. Message: {}", datasetId, e.getMessage());
                throw e;
            }
        } catch (Exception e) {
            LOG.error("Unexpected error! Error in fileManagement for datasetId {} and  Message: {}", datasetId, e.getMessage());
            throw e;
        }
    }

    private void finishImportProcess(ImportFileInDremioInfo importFileInDremioInfo) throws EEAException {

        // TODO delete directory in disk?

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

        Long jobId = jobProcessControllerZuul.findJobIdByProcessId(importFileInDremioInfo.getProcessId());
        JobStatusEnum jobStatus;
        if (null != importFileInDremioInfo.getErrorMessage()) {
            if (EEAErrorMessage.ERROR_FILE_NAME_MATCHING.equals(importFileInDremioInfo.getErrorMessage())) {
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.ERROR_WRONG_FILE_NAME);
                eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
                        ? EventType.IMPORT_REPORTING_FAILED_NAMEFILE_EVENT
                        : EventType.IMPORT_DESIGN_FAILED_NAMEFILE_EVENT;
            } else if (EEAErrorMessage.ERROR_FILE_NO_HEADERS_MATCHING.equals(importFileInDremioInfo.getErrorMessage())) {
                jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.ERROR_NO_HEADERS_MATCHING);
                eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
                        ? EventType.IMPORT_REPORTING_FAILED_NO_HEADERS_MATCHING_EVENT
                        : EventType.IMPORT_DESIGN_FAILED_NO_HEADERS_MATCHING_EVENT;
            } else {
                eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
                        ? EventType.IMPORT_REPORTING_FAILED_EVENT
                        : EventType.IMPORT_DESIGN_FAILED_EVENT;
            }
            datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(),
                    DatasetRunningStatusEnum.ERROR_IN_IMPORT);
            processControllerZuul.updateProcess(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getDataflowId(),
                    ProcessStatusEnum.CANCELED, ProcessTypeEnum.IMPORT, importFileInDremioInfo.getProcessId(),
                    SecurityContextHolder.getContext().getAuthentication().getName(), defaultImportProcessPriority, null);

            jobStatus = JobStatusEnum.CANCELED;
        } else {
            datasetMetabaseService.updateDatasetRunningStatus(importFileInDremioInfo.getDatasetId(),
                    DatasetRunningStatusEnum.IMPORTED);

            processControllerZuul.updateProcess(importFileInDremioInfo.getDatasetId(), importFileInDremioInfo.getDataflowId(),
                    ProcessStatusEnum.FINISHED, ProcessTypeEnum.IMPORT, importFileInDremioInfo.getProcessId(),
                    SecurityContextHolder.getContext().getAuthentication().getName(), defaultImportProcessPriority, null);

            eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
                    ? EventType.IMPORT_REPORTING_COMPLETED_EVENT
                    : EventType.IMPORT_DESIGN_COMPLETED_EVENT;

            jobStatus = JobStatusEnum.FINISHED;
        }

        if (jobId!=null) {
            jobControllerZuul.updateJobStatus(jobId, jobStatus);
        }

        kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value, notificationVO);
        // If importing a zip a file doesn't match with the table and the process ignores it, we send
        // a warning notification
        if (importFileInDremioInfo.getSendWrongFileNameWarning()) {
            NotificationVO notificationWarning = NotificationVO.builder()
                    .user(SecurityContextHolder.getContext().getAuthentication().getName())
                    .datasetId(importFileInDremioInfo.getDatasetId()).fileName(importFileInDremioInfo.getFileName()).build();
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_NAMEFILE_WARNING_EVENT,
                    value, notificationWarning);
        }
    }
}
