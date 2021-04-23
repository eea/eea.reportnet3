/*
 *
 */
package org.eea.dataset.service.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.persistence.data.domain.AttachmentValue;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.AttachmentRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.data.sequence.FieldValueIdGenerator;
import org.eea.dataset.persistence.data.sequence.RecordValueIdGenerator;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.file.interfaces.IFileExportContext;
import org.eea.dataset.service.file.interfaces.IFileExportFactory;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataflow.integration.IntegrationParams;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.thread.EEADelegatingSecurityContextExecutorService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import feign.FeignException;

/**
 * The Class FileTreatmentHelper.
 */
@Component
public class FileTreatmentHelper implements DisposableBean {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(FileTreatmentHelper.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The import executor service. */
  private ExecutorService importExecutorService;

  /** The max running tasks. */
  @Value("${dataset.task.parallelism}")
  private int maxRunningTasks;

  /** The import path. */
  @Value("${importPath}")
  private String importPath;

  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The integration controller. */
  @Autowired
  private IntegrationControllerZuul integrationController;

  /** The record store controller. */
  @Autowired
  private RecordStoreControllerZuul recordStoreControllerZuul;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The data set mapper. */
  @Autowired
  private DataSetMapper dataSetMapper;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The record value id generator. */
  @Autowired
  private RecordValueIdGenerator recordValueIdGenerator;

  /** The field value id generator. */
  @Autowired
  private FieldValueIdGenerator fieldValueIdGenerator;

  /** The record repository. */
  @Autowired
  private RecordRepository recordRepository;

  /** The table repository. */
  @Autowired
  private TableRepository tableRepository;

  /** The path public file. */
  @Value("${pathPublicFile}")
  private String pathPublicFile;

  /** The file export factory. */
  @Autowired
  private IFileExportFactory fileExportFactory;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The attachment repository. */
  @Autowired
  private AttachmentRepository attachmentRepository;

  /**
   * Inits the.
   */
  @PostConstruct
  private void init() {
    importExecutorService = new EEADelegatingSecurityContextExecutorService(
        Executors.newFixedThreadPool(maxRunningTasks));
  }

  /**
   * Destroy.
   *
   * @throws Exception the exception
   */
  @Override
  public void destroy() throws Exception {
    if (null != importExecutorService) {
      this.importExecutorService.shutdown();
    }
  }

  /**
   * Import file data.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param file the file
   * @param replace the replace
   *
   * @throws EEAException the EEA exception
   */
  public void importFileData(Long datasetId, String tableSchemaId, MultipartFile file,
      boolean replace) throws EEAException {

    DataSetSchema schema = datasetService.getSchemaIfReportable(datasetId, tableSchemaId);

    if (null == schema) {
      Map<String, Object> importFileData = new HashMap<>();
      importFileData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_FILE_DATA.getValue());
      importFileData.put(LiteralConstants.DATASETID, datasetId);
      lockService.removeLockByCriteria(importFileData);
      LOG_ERROR.error("Dataset not reportable: datasetId={}, tableSchemaId={}, fileName={}",
          datasetId, tableSchemaId, file.getOriginalFilename());
      throw new EEAException(
          "Dataset not reportable: datasetId=" + datasetId + ", tableSchemaId=" + tableSchemaId);
    }

    // We add a lock to the Release process
    DataSetMetabaseVO datasetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
    Map<String, Object> mapCriteria = new HashMap<>();
    mapCriteria.put("dataflowId", datasetMetabaseVO.getDataflowId());
    mapCriteria.put("dataProviderId", datasetMetabaseVO.getDataProviderId());
    if (datasetMetabaseVO.getDataProviderId() != null) {
      datasetService.createLockWithSignature(LockSignature.RELEASE_SNAPSHOTS, mapCriteria,
          SecurityContextHolder.getContext().getAuthentication().getName());
    }

    fileManagement(datasetId, tableSchemaId, schema, file, replace);
  }

  /**
   * Release lock.
   *
   * @param datasetId the dataset id
   */
  private void releaseLock(Long datasetId) {
    try {
      Map<String, Object> importFileData = new HashMap<>();
      importFileData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_FILE_DATA.getValue());
      importFileData.put(LiteralConstants.DATASETID, datasetId);
      lockService.removeLockByCriteria(importFileData);
      FileUtils.deleteDirectory(new File(importPath, datasetId.toString()));

      releaseLockReleasingProcess(datasetId);
    } catch (IOException e) {
      LOG_ERROR.error("Error deleting files: datasetId={}", datasetId, e);
    }
  }

  /**
   * Release lock releasing process.
   *
   * @param datasetId the dataset id
   */
  private void releaseLockReleasingProcess(Long datasetId) {
    // Release lock to the releasing process
    DataSetMetabaseVO datasetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
    if (datasetMetabaseVO.getDataProviderId() != null) {
      Map<String, Object> importFileData = new HashMap<>();
      importFileData.put(LiteralConstants.SIGNATURE, LockSignature.RELEASE_SNAPSHOTS.getValue());
      importFileData.put(LiteralConstants.DATAFLOWID, datasetMetabaseVO.getDataflowId());
      importFileData.put(LiteralConstants.DATAPROVIDERID, datasetMetabaseVO.getDataProviderId());
      lockService.removeLockByCriteria(importFileData);
    }
  }

  /**
   * File management.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param schema the schema
   * @param multipartFile the multipart file
   * @param delete the delete
   *
   * @throws EEAException the EEA exception
   */
  private void fileManagement(Long datasetId, String tableSchemaId, DataSetSchema schema,
      MultipartFile multipartFile, boolean delete) throws EEAException {

    try (InputStream input = multipartFile.getInputStream()) {

      // Prepare the folder where files will be stored
      File root = new File(importPath);
      File folder = new File(root, datasetId.toString());
      String saveLocationPath = folder.getCanonicalPath();
      String originalFileName = multipartFile.getOriginalFilename();
      String multipartFileMimeType = datasetService.getMimetype(originalFileName);

      if (!folder.mkdirs()) {
        releaseLock(datasetId);
        throw new EEAException("Folder for dataset " + datasetId + " already exists");
      }

      if ("zip".equalsIgnoreCase(multipartFileMimeType)) {

        try (ZipInputStream zip = new ZipInputStream(input)) {

          /*
           * TODO. Since ZIP and CSV files are temporally disabled to be imported from FME, we do
           * not need to look for a matching integration.
           */

          // IntegrationVO integrationVO = getIntegrationVO(schema, "csv");
          IntegrationVO integrationVO = null;

          List<File> files = unzipAndStore(folder, saveLocationPath, zip);

          // Queue import tasks for stored files
          if (!files.isEmpty()) {
            wipeData(datasetId, null, delete);
            IntegrationVO copyIntegrationVO = integrationVOCopyConstructor(integrationVO);
            queueImportProcess(datasetId, null, schema, files, originalFileName, copyIntegrationVO);
          } else {
            releaseLock(datasetId);
            throw new EEAException("Empty zip file");
          }
        }

      } else {

        File file = new File(folder, originalFileName);
        List<File> files = new ArrayList<>();

        /*
         * TOOD. Since ZIP and CSV files are temporally disabled to be imported from FME, we do not
         * need to look for a matching integration.
         */

        IntegrationVO integrationVO;
        if ("csv".equalsIgnoreCase(multipartFileMimeType)) {
          integrationVO = null;
        } else {
          // Look for an integration for the given kind of file.
          integrationVO = getIntegrationVO(schema, multipartFileMimeType);
        }

        // Store the file in the persistence volume
        try (FileOutputStream output = new FileOutputStream(file)) {
          IOUtils.copyLarge(input, output);
          files.add(file);
          LOG.info("Stored file {}", file.getPath());
        }

        // Queue import task for the stored file
        wipeData(datasetId, tableSchemaId, delete);
        queueImportProcess(datasetId, tableSchemaId, schema, files, originalFileName,
            integrationVO);
      }

    } catch (FeignException | IOException e) {
      LOG_ERROR.error("Unexpected exception importing file data: datasetId={}, file={}", datasetId,
          multipartFile.getName(), e);
      releaseLock(datasetId);
      throw new EEAException(e);
    }
  }

  /**
   * Unzip and store.
   *
   * @param folder the folder
   * @param saveLocationPath the save location path
   * @param zip the zip
   *
   * @return the list
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private List<File> unzipAndStore(File folder, String saveLocationPath, ZipInputStream zip)
      throws EEAException, IOException {

    List<File> files = new ArrayList<>();
    ZipEntry entry = zip.getNextEntry();

    while (null != entry) {
      String entryName = entry.getName();
      String mimeType = datasetService.getMimetype(entryName);
      File file = new File(folder, entryName);
      String filePath = file.getCanonicalPath();

      // Prevent Zip Slip attack or skip if the entry is a directory
      if (!"csv".equalsIgnoreCase(mimeType) || entry.isDirectory()
          || !filePath.startsWith(saveLocationPath + File.separator)) {
        LOG_ERROR.error("Ignored file from ZIP: {}", entryName);
        entry = zip.getNextEntry();
        continue;
      }

      // Store the file in the persistence volume
      try (FileOutputStream output = new FileOutputStream(file)) {
        IOUtils.copyLarge(zip, output);
        LOG.info("Stored file {}", file.getPath());
      }

      files.add(file);
      entry = zip.getNextEntry();
    }

    return files;
  }

  /**
   * Queue import process.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param schema the schema
   * @param files the files
   * @param originalFileName the original file name
   * @param integrationVO the integration VO
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   * @throws FeignException the feign exception
   */
  private void queueImportProcess(Long datasetId, String tableSchemaId, DataSetSchema schema,
      List<File> files, String originalFileName, IntegrationVO integrationVO)
      throws IOException, EEAException {
    if (null != integrationVO) {
      fmeFileProcess(datasetId, files.get(0), integrationVO);
    } else {
      importExecutorService.submit(() -> {
        try {
          rn3FileProcess(datasetId, tableSchemaId, schema, files, originalFileName);
        } catch (Exception e) {
          LOG_ERROR.error("RN3-Import: Unexpected error", e);
        }
      });
    }
  }

  /**
   * Fme file process.
   *
   * @param datasetId the dataset id
   * @param file the file
   * @param integrationVO the integration VO
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   * @throws FeignException the feign exception
   */
  private void fmeFileProcess(Long datasetId, File file, IntegrationVO integrationVO)
      throws IOException, EEAException {

    LOG.info("Start FME-Import process: datasetId={}, integrationVO={}", datasetId, integrationVO);
    boolean error = false;

    try (InputStream inputStream = new FileInputStream(file)) {
      // TODO. Encode and copy the file content into the IntegrationVO. This method load the entire
      // file in memory. To solve it, the FME connector should be redesigned.
      byte[] byteArray = IOUtils.toByteArray(inputStream);
      String encodedString = Base64.getEncoder().encodeToString(byteArray);
      Map<String, String> internalParameters = integrationVO.getInternalParameters();
      Map<String, String> externalParameters = new HashMap<>();
      externalParameters.put("fileIS", encodedString);
      integrationVO.setExternalParameters(externalParameters);

      // Remove the lock so FME will not encounter it while calling back importFileData
      if (!"true".equals(internalParameters.get(IntegrationParams.NOTIFICATION_REQUIRED))) {
        Map<String, Object> importFileData = new HashMap<>();
        importFileData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_FILE_DATA.getValue());
        importFileData.put(LiteralConstants.DATASETID, datasetId);
        lockService.removeLockByCriteria(importFileData);
        releaseLockReleasingProcess(datasetId);
      }

      if ((Integer) integrationController
          .executeIntegrationProcess(IntegrationToolTypeEnum.FME,
              IntegrationOperationTypeEnum.IMPORT, file.getName(), datasetId, integrationVO)
          .getExecutionResultParams().get("id") == 0) {
        error = true;
      }
    }

    FileUtils.deleteDirectory(new File(importPath, datasetId.toString()));

    if (error) {
      LOG_ERROR.error("Error executing integration: datasetId={}, fileName={}, IntegrationVO={}",
          datasetId, file.getName(), integrationVO);
      Map<String, Object> importFileData = new HashMap<>();
      importFileData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_FILE_DATA.getValue());
      importFileData.put(LiteralConstants.DATASETID, datasetId);
      lockService.removeLockByCriteria(importFileData);
      releaseLockReleasingProcess(datasetId);
      throw new EEAException("Error executing integration");
    }
  }

  /**
   * Rn 3 file process.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param datasetSchema the schema
   * @param files the files
   * @param originalFileName the original file name
   */
  private void rn3FileProcess(Long datasetId, String tableSchemaId, DataSetSchema datasetSchema,
      List<File> files, String originalFileName) {
    LOG.info("Start RN3-Import process: datasetId={}, files={}", datasetId, files);

    String error = null;
    boolean guessTableName = null == tableSchemaId;

    for (File file : files) {
      String fileName = file.getName();

      try (InputStream inputStream = new FileInputStream(file)) {

        if (guessTableName) {
          tableSchemaId = getTableSchemaIdFromFileName(datasetSchema, fileName);
        }

        LOG.info("Start RN3-Import file: fileName={}, tableSchemaId={}", fileName, tableSchemaId);

        DatasetValue dataset = parseFile(inputStream, datasetId, tableSchemaId, fileName);

        if (dataset == null || CollectionUtils.isEmpty(dataset.getTableValues())) {
          throw new EEAException("Error processing file " + fileName);
        }

        // Check if the table with idTableSchema has been populated already
        Long oldTableId = datasetService.findTableIdByTableSchema(datasetId, tableSchemaId);
        fillTableId(tableSchemaId, dataset.getTableValues(), oldTableId);
        LOG.info("RN3-Import - Filled tableId: datasetId={}, fileName={}", datasetId, fileName);

        // Save empty table
        if (null == oldTableId) {
          LOG.info("RN3-Import - Saving table: datasetId={}, fileName={}", datasetId, fileName);
          datasetService.saveTable(datasetId, dataset.getTableValues().get(0));
          LOG.info("RN3-Import - Table saved: datasetId={}, fileName={}", datasetId, fileName);
        }

        if (schemaContainsFixedRecords(datasetId, datasetSchema, tableSchemaId)) {
          ObjectId tableSchemaIdTemp = new ObjectId(tableSchemaId);
          TableSchema tableSchema = datasetSchema.getTableSchemas().stream()
              .filter(tableSchemaIt -> tableSchemaIt.getIdTableSchema().equals(tableSchemaIdTemp))
              .findFirst().orElse(null);
          if (tableSchema != null) {
            updateRecordsWithConditions(dataset.getTableValues().get(0).getRecords(), datasetId,
                tableSchema);
          }
        } else {
          storeRecords(datasetId, dataset.getTableValues().get(0).getRecords());
        }

        LOG.info("Finish RN3-Import file: fileName={}, tableSchemaId={}", fileName, tableSchemaId);
      } catch (IOException | SQLException | EEAException e) {
        LOG_ERROR.error("RN3-Import file failed: fileName={}, tableSchemaId={}", fileName,
            tableSchemaId, e);
        error = e.getMessage();
      }
    }

    if (files.size() == 1) {
      finishImportProcess(datasetId, tableSchemaId, originalFileName, error);
    } else {
      finishImportProcess(datasetId, null, originalFileName, error);
    }

  }

  /**
   * Gets the table schema id from file name.
   *
   * @param schema the schema
   * @param fileName the file name
   *
   * @return the table schema id from file name
   *
   * @throws EEAException the EEA exception
   */
  private String getTableSchemaIdFromFileName(DataSetSchema schema, String fileName)
      throws EEAException {

    String tableName = fileName.substring(0, fileName.lastIndexOf((".")));
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      if (tableSchema.getNameTableSchema().equalsIgnoreCase(tableName)) {
        return tableSchema.getIdTableSchema().toString();
      }
    }

    throw new EEAException("File name does not match any table name");
  }

  /**
   * Finish import process conditionally.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param originalFileName the original file name
   * @param error the error
   */
  private void finishImportProcess(Long datasetId, String tableSchemaId, String originalFileName,
      String error) {
    try {

      releaseLock(datasetId);

      Map<String, Object> value = new HashMap<>();
      value.put(LiteralConstants.DATASET_ID, datasetId);

      NotificationVO notificationVO = NotificationVO.builder()
          .user(SecurityContextHolder.getContext().getAuthentication().getName())
          .datasetId(datasetId).tableSchemaId(tableSchemaId).fileName(originalFileName).error(error)
          .build();

      EventType eventType;
      DatasetTypeEnum type = datasetService.getDatasetType(datasetId);
      if (null != error) {
        eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
            ? EventType.IMPORT_REPORTING_FAILED_EVENT
            : EventType.IMPORT_DESIGN_FAILED_EVENT;
      } else {
        eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
            ? EventType.IMPORT_REPORTING_COMPLETED_EVENT
            : EventType.IMPORT_DESIGN_COMPLETED_EVENT;
        kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, value);
      }

      kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value, notificationVO);
    } catch (EEAException e) {
      LOG_ERROR.error("RN3-Import file error", e);
    }
  }

  /**
   * Integration VO copy constructor.
   *
   * @param integrationVO the integration VO
   *
   * @return the integration VO
   */
  private IntegrationVO integrationVOCopyConstructor(IntegrationVO integrationVO) {

    IntegrationVO rtn = null;

    if (null != integrationVO) {
      Map<String, String> oldInternalParameters = integrationVO.getInternalParameters();
      Map<String, String> newInternalParameters = new HashMap<>();
      for (Map.Entry<String, String> entry : oldInternalParameters.entrySet()) {
        newInternalParameters.put(entry.getKey(), entry.getValue());
      }

      rtn = new IntegrationVO();
      rtn.setId(integrationVO.getId());
      rtn.setName(integrationVO.getName());
      rtn.setDescription(integrationVO.getDescription());
      rtn.setTool(integrationVO.getTool());
      rtn.setOperation(integrationVO.getOperation());
      rtn.setInternalParameters(newInternalParameters);
    }

    return rtn;
  }

  /**
   * Gets the integration VO.
   *
   * @param datasetSchema the dataset schema
   * @param mimeType the mime type
   *
   * @return the integration VO
   */
  private IntegrationVO getIntegrationVO(DataSetSchema datasetSchema, String mimeType) {

    IntegrationVO rtn = null;

    // Create the IntegrationVO used as criteria.
    String datasetSchemaId = datasetSchema.getIdDataSetSchema().toString();
    String dataflowId = datasetSchema.getIdDataFlow().toString();
    Map<String, String> internalParameters = new HashMap<>();
    internalParameters.put(IntegrationParams.DATASET_SCHEMA_ID, datasetSchemaId);
    internalParameters.put(IntegrationParams.DATAFLOW_ID, dataflowId);
    IntegrationVO criteria = new IntegrationVO();
    criteria.setInternalParameters(internalParameters);

    // Find all integrations matching the criteria.
    for (IntegrationVO integrationVO : integrationController
        .findAllIntegrationsByCriteria(criteria)) {
      if (IntegrationOperationTypeEnum.IMPORT.equals(integrationVO.getOperation())
          && mimeType.equalsIgnoreCase(
              integrationVO.getInternalParameters().get(IntegrationParams.FILE_EXTENSION))) {
        rtn = integrationVO;
        break;
      }
    }

    return rtn;
  }

  /**
   * Wipe data.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param delete the delete
   */
  private void wipeData(Long datasetId, String tableSchemaId, boolean delete) {
    if (delete) {
      if (null != tableSchemaId) {
        datasetService.deleteTableBySchema(tableSchemaId, datasetId);
      } else {
        datasetService.deleteImportData(datasetId);
      }
    }
  }

  /**
   * Fill table id.
   *
   * @param idTableSchema the id table schema
   * @param listTableValues the list table values
   * @param oldTableId the old table id
   */
  private void fillTableId(final String idTableSchema, final List<TableValue> listTableValues,
      Long oldTableId) {
    if (oldTableId != null) {
      listTableValues.stream()
          .filter(tableValue -> tableValue.getIdTableSchema().equals(idTableSchema))
          .forEach(tableValue -> tableValue.setId(oldTableId));
    }
  }

  /**
   * Schema contains fixed records.
   *
   * @param datasetId the dataset id
   * @param schema the schema
   * @param tableSchemaId the table schema id
   * @return true, if successful
   */
  private boolean schemaContainsFixedRecords(Long datasetId, DataSetSchema schema,
      String tableSchemaId) {

    boolean rtn = false;

    if (!TypeStatusEnum.DESIGN.equals(dataflowControllerZuul
        .getMetabaseById(datasetService.getDataFlowIdById(datasetId)).getStatus())) {
      if (null == tableSchemaId) {
        for (TableSchema tableSchema : schema.getTableSchemas()) {
          if (Boolean.TRUE.equals(tableSchema.getFixedNumber())) {
            rtn = true;
            break;
          }
        }
      } else {
        for (TableSchema tableSchema : schema.getTableSchemas()) {
          if (tableSchemaId.equals(tableSchema.getIdTableSchema().toString())) {
            rtn = Boolean.TRUE.equals(tableSchema.getFixedNumber());
            break;
          }
        }
      }
    }

    return rtn;
  }

  /**
   * Parses the file.
   *
   * @param inputStream the input stream
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param fileName the file name
   * @return the dataset value
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private DatasetValue parseFile(InputStream inputStream, Long datasetId, String tableSchemaId,
      String fileName) throws EEAException, IOException {
    DataSetVO datasetVO =
        datasetService.processFile(datasetId, fileName, inputStream, tableSchemaId);
    datasetVO.setId(datasetId);
    DatasetValue dataset = dataSetMapper.classToEntity(datasetVO);
    LOG.info("RN3-Import DataSetVO mapping completed: datasetId={}, tableSchemaId={}, fileName={}",
        datasetId, tableSchemaId, fileName);
    return dataset;
  }

  /**
   * Store records.
   *
   * @param datasetId the dataset id
   * @param recordList the record list
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  private void storeRecords(Long datasetId, List<RecordValue> recordList)
      throws IOException, SQLException {

    String schema = LiteralConstants.DATASET_PREFIX + datasetId;
    LOG.info("RN3-Import - Getting connections: datasetId={}", datasetId);
    ConnectionDataVO connectionDataVO = recordStoreControllerZuul.getConnectionToDataset(schema);

    LOG.info("RN3-Import - Starting PostgresBulkImporter: datasetId={}", datasetId);
    try (
        PostgresBulkImporter recordsImporter =
            new PostgresBulkImporter(connectionDataVO, schema, "record_value", importPath);
        PostgresBulkImporter fieldsImporter =
            new PostgresBulkImporter(connectionDataVO, schema, "field_value", importPath)) {

      LOG.info("RN3-Import - PostgresBulkImporter started: datasetId={}", datasetId);

      for (RecordValue recordValue : recordList) {

        String recordId = (String) recordValueIdGenerator.generate(null, recordValue);
        recordsImporter.addTuple(new Object[] {recordId, recordValue.getIdRecordSchema(),
            recordValue.getTableValue().getId(), recordValue.getDatasetPartitionId(),
            recordValue.getDataProviderCode()});

        for (FieldValue fieldValue : recordValue.getFields()) {
          String fieldId = (String) fieldValueIdGenerator.generate(null, fieldValue);
          fieldsImporter.addTuple(new Object[] {fieldId, fieldValue.getType().getValue(),
              fieldValue.getValue(), fieldValue.getIdFieldSchema(), recordId, null});
        }
      }

      LOG.info("RN3-Import file: Temporary binary files CREATED for datasetId={}", datasetId);
      recordsImporter.copy();
      fieldsImporter.copy();
      LOG.info("RN3-Import file: Temporary binary files IMPORTED for datasetId={}", datasetId);
    }
  }

  /**
   * Update records with conditions.
   *
   * @param recordList the record list
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   */
  private void updateRecordsWithConditions(List<RecordValue> recordList, Long datasetId,
      TableSchema tableSchema) {
    LOG.info("Import dataset table {} with conditions", tableSchema.getNameTableSchema());
    boolean readOnly =
        tableSchema.getRecordSchema().getFieldSchema().stream().anyMatch(FieldSchema::getReadOnly);
    Long totalRecords =
        tableRepository.countRecordsByIdTableSchema(tableSchema.getIdTableSchema().toString());

    // get list paginated of old records to modify
    List<RecordValue> oldRecords = recordRepository.findByTableValueNoOrderOptimized(
        tableSchema.getIdTableSchema().toString(), PageRequest.of(0, totalRecords.intValue()));
    // sublist records to insert
    List<RecordValue> recordsToSave = new ArrayList<>();

    if (!readOnly) {
      Iterator<RecordValue> itr = recordList.iterator();
      for (RecordValue oldRecord : oldRecords) {
        if (itr.hasNext()) {
          refillFields(oldRecord, itr.next().getFields());
        } else {
          refillFields(oldRecord, null);
        }
        recordsToSave.add(oldRecord);
      }
    } else {
      List<ObjectId> readOnlyFields =
          tableSchema.getRecordSchema().getFieldSchema().stream().filter(FieldSchema::getReadOnly)
              .map(FieldSchema::getIdFieldSchema).collect(Collectors.toList());
      if (readOnlyFields.size() != tableSchema.getRecordSchema().getFieldSchema().size()) {
        Map<Integer, Integer> mapPosition = mapPositionReadOnlyFieldsForReference(readOnlyFields,
            oldRecords.get(0), recordList.get(0));
        for (RecordValue oldRecord : oldRecords) {
          findByReadOnlyRecords(mapPosition, oldRecord, recordList);
          recordsToSave.add(oldRecord);
        }
      }
    }
    LOG.info("Import dataset table {} with {} number of records", tableSchema.getNameTableSchema(),
        recordsToSave.size());

    // save
    datasetService.saveAllRecords(datasetId, recordsToSave);
  }

  /**
   * Refill fields.
   *
   * @param oldRecord the old record
   * @param fieldValues the field values
   */
  private void refillFields(RecordValue oldRecord, List<FieldValue> fieldValues) {
    if (fieldValues != null) {
      oldRecord.getFields().stream()
          .forEach(oldField -> oldField.setValue(fieldValues.stream()
              .filter(field -> oldField.getIdFieldSchema().equals(field.getIdFieldSchema()))
              .map(FieldValue::getValue).findFirst().orElse("")));
    } else {
      oldRecord.getFields().forEach(field -> field.setValue(""));
    }
  }


  /**
   * Map position read only fields for reference.
   *
   * @param readOnlyFields the read only fields
   * @param recordValue the record value
   * @param newRecordValues the new record values
   * @return the map
   */
  private Map<Integer, Integer> mapPositionReadOnlyFieldsForReference(List<ObjectId> readOnlyFields,
      RecordValue recordValue, RecordValue newRecordValues) {
    Map<Integer, Integer> mapPosition = new HashMap<>();
    for (ObjectId id : readOnlyFields) {
      mapPosition.put(
          recordValue.getFields().stream().map(FieldValue::getIdFieldSchema)
              .collect(Collectors.toList()).indexOf(id.toString()),
          newRecordValues.getFields().stream().map(FieldValue::getIdFieldSchema)
              .collect(Collectors.toList()).indexOf(id.toString()));
    }
    return mapPosition;
  }

  /**
   * Find by read only records.
   *
   * @param readOnlyPositionFields the read only position fields
   * @param oldRecord the old record
   * @param recordList the record list
   * @return the record value
   */
  private void findByReadOnlyRecords(Map<Integer, Integer> readOnlyPositionFields,
      RecordValue oldRecord, List<RecordValue> recordList) {

    RecordValue recordToUpdate = recordList.stream()
        .filter(record -> readOnlyPositionFields.entrySet().stream()
            .allMatch(entry -> record.getFields().get(entry.getValue()).getValue()
                .equals(oldRecord.getFields().get(entry.getKey()).getValue())))
        .findFirst().orElse(null);
    if (recordToUpdate != null) {
      refillFields(oldRecord, recordToUpdate.getFields());
    }

  }


  /**
   * Export dataset file.
   *
   * @param datasetId the dataset id
   * @param mimeType the mime type
   */
  @Async
  public void exportDatasetFile(Long datasetId, String mimeType) {

    Long dataflowId = datasetService.getDataFlowIdById(datasetId);

    // Look for the dataset type is EU or DC to include the countryCode
    DatasetTypeEnum datasetType = datasetService.getDatasetType(datasetId);
    boolean includeCountryCode = DatasetTypeEnum.EUDATASET.equals(datasetType)
        || DatasetTypeEnum.COLLECTION.equals(datasetType);

    // Extension arrive with zip+xlsx or xlsx, but to the backend arrives with empty space. Split
    // the extensions to know
    // if its a zip or only xlsx
    String[] type = mimeType.split(" ");
    String extension = "";
    if (type.length > 1) {
      extension = type[1];
    } else {
      extension = type[0];
    }
    final IFileExportContext context = fileExportFactory.createContext(extension);

    try {
      byte[] content = context.fileWriter(dataflowId, datasetId, null, includeCountryCode);
      Boolean includeZip = false;
      // If the length after splitting the file type arrives it's more than 1, then there's a
      // zip+xlsx type
      if (type.length > 1) {
        includeZip = true;
      }
      generateFile(datasetId, extension, content, includeZip, datasetType);
      LOG.info("End of exportDatasetFile datasetId {}", datasetId);
    } catch (EEAException | IOException e) {
      LOG_ERROR.error("Error exporting dataset data. DatasetId {}, file type {}. Message {}",
          datasetId, mimeType, e.getMessage(), e);
      // Send notification
      NotificationVO notificationVO = NotificationVO.builder()
          .user(SecurityContextHolder.getContext().getAuthentication().getName())
          .dataflowId(dataflowId).datasetId(datasetId).datasetType(datasetType)
          .error("Error exporting dataset data").build();
      try {
        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.EXPORT_DATASET_FAILED_EVENT, null,
            notificationVO);
      } catch (EEAException ex) {
        LOG_ERROR.error("Error sending export dataset fail notification. Message {}",
            e.getMessage(), ex);
      }
    }

  }


  /**
   * Generate file.
   *
   * @param datasetId the dataset id
   * @param mimeType the mime type
   * @param file the file
   * @param includeZip the include zip
   * @param datasetType the dataset type
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  private void generateFile(Long datasetId, String mimeType, byte[] file, boolean includeZip,
      DatasetTypeEnum datasetType) throws IOException, EEAException {

    DataSetMetabaseVO dataset = datasetMetabaseService.findDatasetMetabase(datasetId);
    String nameDataset = dataset.getDataSetName();
    String nameFile = "";
    // create folder if doesn't exist to save the file
    File fileFolderProvider = new File(pathPublicFile, "dataset-" + datasetId);
    fileFolderProvider.mkdirs();

    // make the zip
    if (includeZip) {
      nameFile = nameDataset + ".zip";
      // we create the file.zip
      File fileWriteZip = new File(new File(pathPublicFile, "dataset-" + datasetId), nameFile);

      try (ZipOutputStream out =
          new ZipOutputStream(new FileOutputStream(fileWriteZip.toString()))) {
        // we get the dataschema and check every table to see if there's any field attachemnt

        DataSetSchema dataSetSchema =
            schemasRepository.findByIdDataSetSchema(new ObjectId(dataset.getDatasetSchema()));
        for (TableSchema tableSchema : dataSetSchema.getTableSchemas()) {

          // we find if in any table have one field type ATTACHMENT
          List<FieldSchema> fieldSchemaAttachment = tableSchema.getRecordSchema().getFieldSchema()
              .stream().filter(field -> DataType.ATTACHMENT.equals(field.getType()))
              .collect(Collectors.toList());
          if (!CollectionUtils.isEmpty(fieldSchemaAttachment)) {
            // We took every field for every table
            for (FieldSchema fieldAttach : fieldSchemaAttachment) {
              List<AttachmentValue> attachmentValue =
                  attachmentRepository.findAllByIdFieldSchemaAndValueIsNotNull(
                      fieldAttach.getIdFieldSchema().toString());

              // if there are filled we create a folder and inside of any folder we create the
              // fields
              if (!CollectionUtils.isEmpty(attachmentValue)) {
                LOG.info(
                    "Generating zip file with attachments. Found in tableSchema with id {}, field {}",
                    tableSchema.getIdTableSchema(), fieldAttach.getIdFieldSchema());

                for (AttachmentValue attachment : attachmentValue) {
                  try {
                    ZipEntry eFieldAttach = new ZipEntry(
                        tableSchema.getNameTableSchema() + "/" + attachment.getFileName());
                    out.putNextEntry(eFieldAttach);
                    out.write(attachment.getContent(), 0, attachment.getContent().length);
                  } catch (ZipException e) {
                    LOG.info("Error adding file to the zip {} because already exists",
                        attachment.getFileName(), e);
                  }
                }
                out.closeEntry();
              }
            }
          }
        }
        // Adding the xlsx/csv file to the zip
        ZipEntry e = new ZipEntry(nameDataset + "." + mimeType);
        out.putNextEntry(e);
        out.write(file, 0, file.length);
        out.closeEntry();
        LOG.info("Creating file {} in the route ", fileWriteZip);
      }
    }
    // only the xlsx file
    else {
      nameFile = nameDataset + "." + mimeType;
      File fileWrite = new File(new File(pathPublicFile, "dataset-" + datasetId), nameFile);
      try (OutputStream out = new FileOutputStream(fileWrite.toString())) {
        out.write(file, 0, file.length);
      }
    }
    // Send notification
    NotificationVO notificationVO = NotificationVO.builder()
        .user(SecurityContextHolder.getContext().getAuthentication().getName()).datasetId(datasetId)
        .datasetName(nameFile).datasetType(datasetType).build();

    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.EXPORT_DATASET_COMPLETED_EVENT, null,
        notificationVO);

  }

}
