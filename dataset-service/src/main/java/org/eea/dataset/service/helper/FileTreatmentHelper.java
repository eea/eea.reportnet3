package org.eea.dataset.service.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataflow.integration.fme.FMEController.FMEControllerZuul;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.integration.IntegrationParams;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
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

  /** The integration controller. */
  @Autowired
  private IntegrationControllerZuul integrationController;

  /** The fme controller zuul. */
  @Autowired
  private FMEControllerZuul fmeControllerZuul;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The data set mapper. */
  @Autowired
  private DataSetMapper dataSetMapper;

  /** The import executor service. */
  private ExecutorService importExecutorService;

  /** The batch size. */
  private int batchSize = 1000;

  /**
   * Inits the.
   */
  @PostConstruct
  private void init() {
    importExecutorService = Executors.newFixedThreadPool(maxRunningTasks);
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
   * @param externalJobId the external job id
   * @throws EEAException the EEA exception
   */
  public void importFileData(Long datasetId, String tableSchemaId, MultipartFile file,
      boolean replace, Long externalJobId) throws EEAException {
    DataSetSchema schema = datasetService.getSchemaIfReportable(datasetId, tableSchemaId);
    if (null == schema) {
      datasetService.releaseLock(LockSignature.IMPORT_FILE_DATA.getValue(), datasetId);
      LOG_ERROR.error("Dataset not reportable: datasetId={}, tableSchemaId={}, fileName={}",
          datasetId, tableSchemaId, file.getName());
      throw new EEAException(
          "Dataset not reportable: datasetId=" + datasetId + ", tableSchemaId=" + tableSchemaId);
    }
    fileManagement(datasetId, tableSchemaId, schema, file, replace, externalJobId);
  }

  /**
   * File management.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param schema the schema
   * @param multipartFile the multipart file
   * @param delete the delete
   * @param externalJobId the external job id
   * @throws EEAException the EEA exception
   */
  private void fileManagement(Long datasetId, String tableSchemaId, DataSetSchema schema,
      MultipartFile multipartFile, boolean delete, Long externalJobId) throws EEAException {

    try (InputStream input = multipartFile.getInputStream()) {

      // Prepare the folder where files will be stored
      File root = new File(importPath);
      File folder = new File(root, datasetId.toString());
      String fileName = multipartFile.getOriginalFilename();
      String saveLocationPath = folder.getCanonicalPath();
      String multipartFileMimeType = datasetService.getMimetype(fileName);

      if (!folder.mkdirs()) {
        finishImportProcessConditionally(datasetId, tableSchemaId, externalJobId, null, null, null,
            true);
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
            wipeData(datasetId, tableSchemaId, delete);
            for (File file : files) {
              IntegrationVO copyIntegrationVO = integrationVOCopyConstructor(integrationVO);
              queueImportProcess(datasetId, tableSchemaId, schema, file, copyIntegrationVO,
                  externalJobId);
            }
          }
        }
      } else {

        File file = new File(folder, fileName);

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
          LOG.info("Stored file {}", file.getPath());
        }

        // Queue import task for the stored file
        wipeData(datasetId, tableSchemaId, delete);
        queueImportProcess(datasetId, tableSchemaId, schema, file, integrationVO, externalJobId);
      }

    } catch (FeignException | IOException e) {
      LOG_ERROR.error("Unexpected exception importing file data: datasetId={}, file={}", datasetId,
          multipartFile.getName(), e);
      finishImportProcessConditionally(datasetId, tableSchemaId, externalJobId, null, null, null,
          true);
      throw new EEAException(e);
    }
  }

  /**
   * Unzip and store.
   *
   * @param folder the folder
   * @param saveLocationPath the save location path
   * @param zip the zip
   * @return the list
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
   * @param file the file
   * @param integrationVO the integration VO
   * @param externalJobId the external job id
   * @throws FeignException the feign exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void queueImportProcess(Long datasetId, String tableSchemaId, DataSetSchema schema,
      File file, IntegrationVO integrationVO, Long externalJobId)
      throws FeignException, IOException {
    String user = SecurityContextHolder.getContext().getAuthentication().getName();
    if (null != integrationVO) {
      fmeFileProcess(datasetId, file, integrationVO);
    } else {
      importExecutorService.submit(
          () -> rn3FileProcess(datasetId, tableSchemaId, schema, file, user, externalJobId));
    }
  }

  /**
   * Fme file process.
   *
   * @param datasetId the dataset id
   * @param file the file
   * @param integrationVO the integration VO
   * @throws FeignException the feign exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void fmeFileProcess(Long datasetId, File file, IntegrationVO integrationVO)
      throws FeignException, IOException {

    LOG.info("Start FME-Import process: datasetId={}, integrationVO={}", datasetId, integrationVO);

    try (InputStream inputStream = new FileInputStream(file)) {
      // TODO. Encode and copy the file content into the IntegrationVO. This method load the entire
      // file in memory. To solve it, the FME connector should be redesigned.
      byte[] byteArray = IOUtils.toByteArray(inputStream);
      String encodedString = Base64.getEncoder().encodeToString(byteArray);
      Map<String, String> externalParameters = new HashMap<>();
      externalParameters.put("fileIS", encodedString);
      integrationVO.setExternalParameters(externalParameters);

      integrationController.executeIntegrationProcess(IntegrationToolTypeEnum.FME,
          IntegrationOperationTypeEnum.IMPORT, file.getName(), datasetId, integrationVO);
    }

    // Remove the file
    Files.delete(file.toPath());

    // Remove the folder
    Files.delete(file.getParentFile().toPath());
  }

  /**
   * Rn 3 file process.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param schema the schema
   * @param file the file
   * @param user the user
   * @param externalJobId the external job id
   */
  private void rn3FileProcess(Long datasetId, String tableSchemaId, DataSetSchema schema, File file,
      String user, Long externalJobId) {

    String fileName = file.getName();
    String error = null;

    try (InputStream inputStream = new FileInputStream(file)) {

      if (null == tableSchemaId && "csv".equalsIgnoreCase(datasetService.getMimetype(fileName))) {
        tableSchemaId = getTableSchemaIdFromFileName(schema, fileName);
      }

      LOG.info("Start RN3-Import process: datasetId={}, tableSchemaId={}, fileName={}", datasetId,
          tableSchemaId, fileName);

      DataSetVO datasetVO =
          datasetService.processFile(datasetId, fileName, inputStream, tableSchemaId);
      datasetVO.setId(datasetId);
      DatasetValue dataset = dataSetMapper.classToEntity(datasetVO);
      if (dataset == null) {
        throw new IOException("Error mapping file");
      }

      // Save empty table
      List<RecordValue> allRecords = dataset.getTableValues().get(0).getRecords();
      dataset.getTableValues().get(0).setRecords(new ArrayList<>());

      // Check if the table with idTableSchema has been populated already
      Long oldTableId = datasetService.findTableIdByTableSchema(datasetId, tableSchemaId);
      fillTableId(tableSchemaId, dataset.getTableValues(), oldTableId);

      if (null == oldTableId) {
        datasetService.saveTable(datasetId, dataset.getTableValues().get(0));
      }

      List<List<RecordValue>> batchedListOfRecords = getListOfRecords(allRecords);

      LOG.info("Inserting {} records into database for dataset {} coming from file {}",
          allRecords.size(), datasetId, fileName);
      batchedListOfRecords.parallelStream()
          .forEach(recordValues -> datasetService.saveAllRecords(datasetId, recordValues));

      LOG.info("File {} processed and saved into DB for dataset {}", fileName, datasetId);
    } catch (Exception e) {
      LOG_ERROR.error("Error loading file: {}", fileName, e);
      error = e.getMessage();
    }
    finishImportProcessConditionally(datasetId, tableSchemaId, externalJobId, file, user, error,
        false);
  }

  /**
   * Gets the table schema id from file name.
   *
   * @param schema the schema
   * @param fileName the file name
   * @return the table schema id from file name
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
   * @param externalJobId the external job id
   * @param file the file
   * @param user the user
   * @param error the error
   * @param finishAll the finish all
   */
  private void finishImportProcessConditionally(Long datasetId, String tableSchemaId,
      Long externalJobId, File file, String user, String error, boolean finishAll) {

    if (null != externalJobId) {
      // Status 0 -> FMEJobstatus.SUCCESS
      fmeControllerZuul.updateJobStatusById(externalJobId, 0L);
    }

    try {
      if (finishAll) {
        LOG_ERROR.error("Exception handler: deleting directory for dataset {}", datasetId);
        FileUtils.deleteDirectory(new File(importPath, datasetId.toString()));
        datasetService.releaseLock(LockSignature.IMPORT_FILE_DATA.getValue(), datasetId);
      } else {
        Path parentPath = file.getParentFile().toPath();
        try (Stream<Path> entries = Files.list(parentPath)) {

          // Remove the file
          Files.delete(file.toPath());

          // Check if the folder is empty
          if (!entries.findFirst().isPresent()) {

            // Remove the folder
            Files.delete(parentPath);

            if (null != error) {
              releaseRn3ImportFailedEvent(datasetId, tableSchemaId, file.getName(), user, error);
            } else {
              releaseRn3ImportCompletedEvent(datasetId, tableSchemaId, file.getName(), user);
            }

            datasetService.releaseLock(LockSignature.IMPORT_FILE_DATA.getValue(), datasetId);
          }
        }
      }
    } catch (IOException ex) {
      LOG_ERROR.error("RN3-Import file error", ex);
    }
  }

  /**
   * Integration VO copy constructor.
   *
   * @param integrationVO the integration VO
   * @return the integration VO
   */
  private IntegrationVO integrationVOCopyConstructor(IntegrationVO integrationVO) {

    IntegrationVO rtn = null;

    if (null != integrationVO) {
      Map<String, String> oldInternalParameters = integrationVO.getInternalParameters();
      Map<String, String> newInternalParameters = new HashMap<>();
      newInternalParameters.put("datasetSchemaId", oldInternalParameters.get("datasetSchemaId"));
      newInternalParameters.put("dataflowId", oldInternalParameters.get("dataflowId"));

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
   * Release rn 3 import completed event.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param fileName the file name
   * @param user the user
   */
  private void releaseRn3ImportCompletedEvent(Long datasetId, String tableSchemaId, String fileName,
      String user) {
    EventType eventType = DatasetTypeEnum.REPORTING.equals(datasetService.getDatasetType(datasetId))
        ? EventType.IMPORT_REPORTING_COMPLETED_EVENT
        : EventType.IMPORT_DESIGN_COMPLETED_EVENT;
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, datasetId);
    value.put(LiteralConstants.USER, user);
    NotificationVO notificationVO = NotificationVO.builder().user(user).datasetId(datasetId)
        .tableSchemaId(tableSchemaId).fileName(fileName).build();
    try {
      kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, value);
      kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value, notificationVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Error realeasing event: notificationVO={}", notificationVO, e);
    }
  }

  /**
   * Release rn 3 import failed event.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param fileName the file name
   * @param user the user
   * @param error the error
   */
  private void releaseRn3ImportFailedEvent(Long datasetId, String tableSchemaId, String fileName,
      String user, String error) {
    EventType eventType = DatasetTypeEnum.REPORTING.equals(datasetService.getDatasetType(datasetId))
        ? EventType.IMPORT_REPORTING_FAILED_EVENT
        : EventType.IMPORT_DESIGN_FAILED_EVENT;
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, datasetId);
    value.put(LiteralConstants.USER, user);
    NotificationVO notificationVO = NotificationVO.builder().user(user).datasetId(datasetId)
        .tableSchemaId(tableSchemaId).fileName(fileName).error(error).build();
    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value, notificationVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Error realeasing fail event: notificationVO={}", notificationVO, e);
    }
  }

  /**
   * Gets the list of records.
   *
   * @param allRecords the all records
   * @return the list of records
   */
  private List<List<RecordValue>> getListOfRecords(List<RecordValue> allRecords) {
    List<List<RecordValue>> generalList = new ArrayList<>();

    // dividing the number of records in different lists
    int nLists = (int) Math.ceil(allRecords.size() / (double) batchSize);
    if (nLists > 1) {
      for (int i = 0; i < (nLists - 1); i++) {
        generalList.add(new ArrayList<>(allRecords.subList(batchSize * i, batchSize * (i + 1))));
      }
    }
    generalList
        .add(new ArrayList<>(allRecords.subList(batchSize * (nLists - 1), allRecords.size())));

    return generalList;
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
}
