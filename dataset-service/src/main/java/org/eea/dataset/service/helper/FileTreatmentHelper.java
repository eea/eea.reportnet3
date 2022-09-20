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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.persistence.data.domain.AttachmentValue;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.AttachmentRepository;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.file.interfaces.IFileExportContext;
import org.eea.dataset.service.file.interfaces.IFileExportFactory;
import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.file.interfaces.IFileParserFactory;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataflow.integration.IntegrationParams;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.ETLDatasetVO;
import org.eea.interfaces.vo.dataset.ETLFieldVO;
import org.eea.interfaces.vo.dataset.ETLRecordVO;
import org.eea.interfaces.vo.dataset.ETLTableVO;
import org.eea.interfaces.vo.dataset.ExportFilterVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetRunningStatusEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.multitenancy.DatasetId;
import org.eea.multitenancy.TenantResolver;
import org.eea.thread.EEADelegatingSecurityContextExecutorService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

  /** The Constant USER: {@value}. */
  private static final String USER = "root";

  /** The Constant FILE_PUBLIC_DATASET_PATTERN_NAME. */
  private static final String FILE_PUBLIC_DATASET_PATTERN_NAME = "%s-%s";

  /** The import executor service. */
  private ExecutorService importExecutorService;

  /** The max running tasks. */
  @Value("${dataset.task.parallelism}")
  private int maxRunningTasks;

  /** The import path. */
  @Value("${importPath}")
  private String importPath;

  /** The field max length. */
  @Value("${dataset.fieldMaxLength}")
  private int fieldMaxLength;

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

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

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

  /** The dataset repository. */
  @Autowired
  private DatasetRepository datasetRepository;

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The partition data set metabase repository. */
  @Autowired
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  /** The file parser factory. */
  @Autowired
  private IFileParserFactory fileParserFactory;

  /** The record store controller zuul. */
  @Autowired
  private RecordStoreControllerZuul recordStoreControllerZuul;

  /** The design dataset repository. */
  @Autowired
  private DesignDatasetRepository designDatasetRepository;

  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The field repository. */
  @Autowired
  private FieldRepository fieldRepository;

  /**
   * Initialize the executor service.
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
   * @param integrationId the integration id
   * @param delimiter the delimiter
   * @throws EEAException the EEA exception
   */
  public void importFileData(Long datasetId, String tableSchemaId, MultipartFile file,
      boolean replace, Long integrationId, String delimiter) throws EEAException {

    if (delimiter != null && delimiter.length() > 1) {
      LOG_ERROR.error("Error when importing file data for datasetId {} and tableSchemaId {}. ReplaceData is {}. The size of the delimiter cannot be greater than 1", datasetId, tableSchemaId, replace);
      datasetMetabaseService.updateDatasetRunningStatus(datasetId,
          DatasetRunningStatusEnum.ERROR_IN_IMPORT);
      throw new EEAException("The size of the delimiter cannot be greater than 1");
    }

    DataSetSchema schema = datasetService.getSchemaIfReportable(datasetId, tableSchemaId);

    if (null == schema) {
      Map<String, Object> importFileData = new HashMap<>();
      importFileData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_FILE_DATA.getValue());
      importFileData.put(LiteralConstants.DATASETID, datasetId);
      lockService.removeLockByCriteria(importFileData);
      Map<String, Object> importBigFileData = new HashMap<>();
      importBigFileData.put(LiteralConstants.SIGNATURE,
          LockSignature.IMPORT_BIG_FILE_DATA.getValue());
      importBigFileData.put(LiteralConstants.DATASETID, datasetId);
      lockService.removeLockByCriteria(importBigFileData);
      LOG_ERROR.error("Dataset not reportable: datasetId={}, tableSchemaId={}, fileName={}",
          datasetId, tableSchemaId, file.getOriginalFilename());
      throw new EEAException(
          "Dataset not reportable: datasetId=" + datasetId + ", tableSchemaId=" + tableSchemaId);
    }

    // We add a lock to the Release process
    DataSetMetabaseVO datasetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
    datasetMetabaseService.updateDatasetRunningStatus(datasetId,
        DatasetRunningStatusEnum.IMPORTING);
    Map<String, Object> mapCriteria = new HashMap<>();
    mapCriteria.put("dataflowId", datasetMetabaseVO.getDataflowId());
    mapCriteria.put("dataProviderId", datasetMetabaseVO.getDataProviderId());
    if (datasetMetabaseVO.getDataProviderId() != null) {
      datasetService.createLockWithSignature(LockSignature.RELEASE_SNAPSHOTS, mapCriteria,
          SecurityContextHolder.getContext().getAuthentication().getName());
    }
    // now the view is not updated, update the check to false
    datasetService.updateCheckView(datasetId, false);
    // delete the temporary table from etlExport
    datasetService.deleteTempEtlExport(datasetId);
    fileManagement(datasetId, tableSchemaId, schema, file, replace, integrationId, delimiter);
  }

  /**
   * Creates the file.
   *
   * @param datasetId the dataset id
   * @param mimeType the mime type
   * @param tableSchemaId the table schema id
   * @param filters the filters
   * @return the byte[]
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public byte[] createFile(Long datasetId, String mimeType, final String tableSchemaId,
      ExportFilterVO filters) throws EEAException, IOException {
    // Get the dataFlowId from the metabase
    Long idDataflow = datasetService.getDataFlowIdById(datasetId);

    // Find if the dataset type is EU to include the countryCode
    DatasetTypeEnum datasetType = datasetMetabaseService.getDatasetType(datasetId);
    String includeCountryCode = getCode(idDataflow, datasetType);

    final IFileExportContext contextExport = fileExportFactory.createContext(mimeType);
    LOG.info("End of createFile");
    return contextExport.fileWriter(idDataflow, datasetId, tableSchemaId, includeCountryCode, false,
        filters);
  }

  /**
   * Save public files.
   *
   * @param dataflowId the dataflow id
   * @param dataSetDataProvider the data set data provider
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void savePublicFiles(Long dataflowId, Long dataSetDataProvider) throws IOException {

    LOG.info("Start creating files. DataflowId: {} DataProviderId: {}", dataflowId,
        dataSetDataProvider);

    List<RepresentativeVO> representativeList =
        representativeControllerZuul.findRepresentativesByIdDataFlow(dataflowId);

    // we find representative
    RepresentativeVO representative = representativeList.stream().filter(
        representativeData -> representativeData.getDataProviderId().equals(dataSetDataProvider))
        .findAny().orElse(null);

    if (null != representative) {
      // we create the dataflow folder to save it

      File directoryDataflow = new File(pathPublicFile, "dataflow-" + dataflowId);
      File directoryDataProvider =
          new File(directoryDataflow, "dataProvider-" + representative.getDataProviderId());
      // we create the dataprovider folder to save it andwe always delete it and put new files
      FileUtils.deleteDirectory(directoryDataProvider);
      DataFlowVO dataflow = dataflowControllerZuul.getMetabaseById(dataflowId);
      if (!TypeDataflowEnum.BUSINESS.equals(dataflow.getType())) {
        createAllDatasetFiles(dataflowId, representative.getDataProviderId());
      } else {
        // we delete all file names in the table dataset
        List<DataSetMetabase> datasetMetabaseList = dataSetMetabaseRepository
            .findByDataflowIdAndDataProviderId(dataflowId, representative.getDataProviderId());
        datasetMetabaseList.stream().forEach(datasetFileName -> {
          datasetFileName.setPublicFileName(null);
        });
        dataSetMetabaseRepository.saveAll(datasetMetabaseList);
      }
    }
  }

  /**
   * Creates the reference dataset files.
   *
   * @param dataset the dataset
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Async
  public void createReferenceDatasetFiles(DataSetMetabase dataset) throws IOException {

    ExportFilterVO filters = new ExportFilterVO();
    List<DesignDataset> desingDataset =
        designDatasetRepository.findByDataflowId(dataset.getDataflowId());
    // look for the name of the design dataset to put the right name to the file
    String datasetDesingName = "";
    for (DesignDataset designDatasetVO : desingDataset) {
      if (designDatasetVO.getDatasetSchema().equalsIgnoreCase(dataset.getDatasetSchema())) {
        datasetDesingName = designDatasetVO.getDataSetName();
      }
    }

    try {
      // create the excel file
      byte[] file = createFile(dataset.getId(), FileTypeEnum.XLSX.getValue(), null, filters);
      // we save the file in its files
      if (null != file) {
        String nameFileUnique = String.format("%s", datasetDesingName);
        String nameFileScape = nameFileUnique + ".xlsx";

        // we create the files and zip with the attachment if it is necessary
        createFilesAndZip(dataset.getDataflowId(), null, dataset, file, nameFileUnique,
            nameFileScape);

        // we save the file in metabase with the name without the route
        dataset.setPublicFileName(nameFileUnique + ".zip");
        dataSetMetabaseRepository.save(dataset);
      }
    } catch (EEAException e) {
      LOG_ERROR.error("File not created in dataflow {}. Message: {}", dataset.getDataflowId(),
          e.getMessage(), e);
    }
    LOG.info("Reference file created in dataflowId {} for datasetId {}", dataset.getDataflowId(), dataset.getId());
  }

  /**
   * Export file.
   *
   * @param datasetId the dataset id
   * @param mimeType the mime type
   * @param tableSchemaId the table schema id
   * @param tableName the table name
   * @param filters the filters
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Async
  public void exportFile(Long datasetId, String mimeType, String tableSchemaId, String tableName,
      ExportFilterVO filters) throws EEAException, IOException {
    NotificationVO notificationVO = NotificationVO.builder()
        .user(SecurityContextHolder.getContext().getAuthentication().getName()).datasetId(datasetId)
        .fileName(tableName).mimeType(mimeType).datasetSchemaId(tableSchemaId)
        .error("Error exporting table data").build();
    File fileFolder = new File(pathPublicFile, "dataset-" + datasetId);
    String.format("Failed generating file from datasetId {} with schema {}.", datasetId,
        tableSchemaId);
    fileFolder.mkdirs();
    try {
      byte[] file = createFile(datasetId, mimeType, tableSchemaId, filters);
      File fileWrite =
          new File(new File(pathPublicFile, "dataset-" + datasetId), tableName + "." + mimeType);
      try (OutputStream out = new FileOutputStream(fileWrite.toString());) {
        out.write(file);
        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.EXPORT_TABLE_DATA_COMPLETED_EVENT,
            null, notificationVO);
      }
    } catch (IOException | EEAException e) {
      LOG_ERROR.info("Error exporting table data from dataset Id {} with schema {}.", datasetId,
          tableSchemaId);
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.EXPORT_TABLE_DATA_FAILED_EVENT, null,
          notificationVO);
    }
  }

  /**
   * Gets the tables.
   *
   * @param datasetId the dataset id
   * @return the tables
   */
  public List<TableSchema> getTables(Long datasetId) {

    String datasetSchemaId = datasetMetabaseService.findDatasetSchemaIdById(datasetId);
    DataSetSchema datasetSchema = null;
    try {
      datasetSchema = schemasRepository.findById(new ObjectId(datasetSchemaId))
          .orElseThrow(() -> new EEAException(EEAErrorMessage.SCHEMA_NOT_FOUND));
    } catch (EEAException e) {
      LOG_ERROR.error("Error finding datasetSchema by Id. DatasetSchemaId {}. Message {}",
          datasetSchemaId, e.getMessage(), e);
    }
    List<TableSchema> tableSchemaList = new ArrayList<>();
    if (datasetSchema != null) {
      tableSchemaList = datasetSchema.getTableSchemas();
    }

    return tableSchemaList;
  }

  /**
   * Etl import dataset.
   *
   * @param datasetId the dataset id
   * @param etlDatasetVO the etl dataset VO
   * @param providerId the provider id
   * @throws EEAException the EEA exception
   */
  public void etlImportDataset(@DatasetId Long datasetId, ETLDatasetVO etlDatasetVO,
      Long providerId) throws EEAException {
    // Get the datasetSchemaId by the datasetId
    LOG.info("Import data into dataset {}", datasetId);
    String datasetSchemaId = datasetRepository.findIdDatasetSchemaById(datasetId);
    if (null == datasetSchemaId) {
      throw new EEAException(String.format(EEAErrorMessage.DATASET_SCHEMA_ID_NOT_FOUND, datasetId));
    }

    // Get the datasetSchema by the datasetSchemaId
    DataSetSchema datasetSchema =
        schemasRepository.findById(new ObjectId(datasetSchemaId)).orElse(null);
    if (null == datasetSchema) {
      throw new EEAException(
          String.format(EEAErrorMessage.DATASET_SCHEMA_NOT_FOUND, datasetSchemaId));
    }

    // Obtain the data provider code to insert into the record
    DataProviderVO provider =
        providerId != null ? representativeControllerZuul.findDataProviderById(providerId) : null;

    // Get the partition for the partiton id
    final PartitionDataSetMetabase partition = obtainPartition(datasetId, USER);

    // Construct Maps to relate ids
    Map<String, TableSchema> tableMap = new HashMap<>();
    Map<String, FieldSchema> fieldMap = new HashMap<>();
    Set<String> tableWithAttachmentFieldSet = new HashSet<>();
    for (TableSchema tableSchema : datasetSchema.getTableSchemas()) {
      tableMap.put(tableSchema.getNameTableSchema().toLowerCase(), tableSchema);
      // Match each fieldSchemaId with its headerName
      for (FieldSchema field : tableSchema.getRecordSchema().getFieldSchema()) {
        fieldMap.put(field.getHeaderName().toLowerCase() + tableSchema.getIdTableSchema(), field);
        if (DataType.ATTACHMENT.equals(field.getType())) {
          LOG.warn("Table with id schema {} contains attachment field, processing",
              tableSchema.getIdTableSchema());
          tableWithAttachmentFieldSet.add(tableSchema.getIdTableSchema().toString());
        }
      }
    }

    // Construct object to be save
    DatasetValue dataset = new DatasetValue();
    List<TableValue> tables = new ArrayList<>();
    List<String> readOnlyTables = new ArrayList<>();
    List<String> fixedNumberTables = new ArrayList<>();

    // Loops to build the entity
    dataset.setId(datasetId);
    DatasetTypeEnum datasetType = datasetService.getDatasetType(dataset.getId());

    etlTableFor(etlDatasetVO, provider, partition, tableMap, fieldMap, dataset, tables,
        readOnlyTables, fixedNumberTables, datasetType);
    dataset.setTableValues(tables);
    dataset.setIdDatasetSchema(datasetSchemaId);

    List<RecordValue> allRecords = new ArrayList<>();

    tableValueFor(datasetId, dataset, readOnlyTables, fixedNumberTables, allRecords,
        tableWithAttachmentFieldSet, datasetSchema.getTableSchemas());
    recordRepository.saveAll(allRecords);
    LOG.info("Data saved for datasetId {}", datasetId);
    // now the view is not updated, update the check to false
    datasetService.updateCheckView(datasetId, false);
    // delete the temporary table from etlExport
    datasetService.deleteTempEtlExport(datasetId);
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
    ExportFilterVO filters = new ExportFilterVO();

    // Look for the dataset type is EU or DC to include the countryCode
    DatasetTypeEnum datasetType = datasetService.getDatasetType(datasetId);

    String includeCountryCode = getCode(dataflowId, datasetType);

    // Extension arrive with zip+xlsx, zip+csv or xlsx, but to the backend arrives with empty space.
    // Split the extensions to know
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
      Map<String, byte[]> contents = new HashMap<>();
      if (extension.equalsIgnoreCase(FileTypeEnum.CSV.getValue())) {
        List<TableSchema> tablesSchema = getTables(datasetId);
        List<byte[]> dataFile =
            context.fileListWriter(dataflowId, datasetId, includeCountryCode, false);
        for (int i = 0; i < tablesSchema.size(); i++) {
          contents.put(tablesSchema.get(i).getIdTableSchema() + "_"
              + tablesSchema.get(i).getNameTableSchema(), dataFile.get(i));
        }
      } else {
        byte[] dataFile = context.fileWriter(dataflowId, datasetId, null, includeCountryCode,
            extension.equalsIgnoreCase(FileTypeEnum.VALIDATIONS.getValue()), filters);
        contents.put(null, dataFile);
      }

      Boolean includeZip = false;
      // If the length after splitting the file type arrives it's more than 1, then there's a
      // zip+xlsx type
      if (type.length > 1 && !extension.equalsIgnoreCase(FileTypeEnum.VALIDATIONS.getValue())) {
        includeZip = true;
      }
      generateFile(datasetId, extension, contents, includeZip, datasetType);
      LOG.info("Exported dataset data for datasetId {}", datasetId);
    } catch (EEAException | IOException | NullPointerException e) {
      LOG_ERROR.error("Error exporting dataset data. datasetId {}, file type {}. Message {}",
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
        LOG_ERROR.error("Error sending export dataset fail notification for datasetId {}. Message {}",
            datasetId, e.getMessage(), ex);
      }
    }

  }

  /**
   * Release lock releasing process.
   *
   * @param datasetId the dataset id
   */
  public void releaseLockReleasingProcess(Long datasetId) {
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
   * Update geomety.
   *
   * @param datasetId the dataset id
   * @param datasetSchema the dataset schema
   */
  @Async
  public void updateGeometry(Long datasetId, DataSetSchema datasetSchema) {
    // check schema has geometry and check field Value has geometry
    if (checkSchemaGeometry(datasetSchema)) {
      LOG.info("Updating geometries for dataset {}", datasetId);
      // update geometries (native)
      Map<Integer, Map<String, String>> mapFieldValue = getFieldValueGeometry(datasetId);
      int size = mapFieldValue.keySet().size();
      for (int i = 0; i < size; i++) {
        executeUpdateGeometry(datasetId, mapFieldValue.get(i));
      }
    }
  }

  /**
   * Check schema geometry.
   *
   * @param datasetSchema the dataset schema
   * @return true, if successful
   */
  private boolean checkSchemaGeometry(DataSetSchema datasetSchema) {
    boolean result = false;
    for (TableSchema table : datasetSchema.getTableSchemas()) {
      for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
        switch (field.getType()) {
          case GEOMETRYCOLLECTION:
          case MULTILINESTRING:
          case MULTIPOINT:
          case MULTIPOLYGON:
          case POINT:
          case POLYGON:
            result = true;
            break;
          default:
            result = false;
        }
        if (result) {
          break;
        }
      }
      if (result) {
        break;
      }
    }
    return result;
  }

  /**
   * Check field value geometry.
   *
   * @param datasetId the dataset id
   * @return true, if successful
   */
  private boolean checkFieldValueGeometry(Long datasetId) {
    boolean result = false;
    String query = "select count(fv.id) from dataset_" + datasetId
        + ".field_value fv where fv.type in ('POINT','LINESTRING','POLYGON','MULTIPOINT','MULTILINESTRING','MULTIPOLYGON','GEOMETRYCOLLECTION')";
    Integer count = Integer.parseInt(fieldRepository.queryExecutionSingle(query).toString());
    if (count != null && count > 0) {
      result = true;
    }
    return result;
  }

  /**
   * Gets the field value geometry.
   *
   * @param datasetId the dataset id
   * @return the field value geometry
   */
  private Map<Integer, Map<String, String>> getFieldValueGeometry(Long datasetId) {
    String query = "select id, value from dataset_" + datasetId
        + ".field_value fv where fv.type in ('POINT','LINESTRING','POLYGON','MULTIPOINT','MULTILINESTRING','MULTIPOLYGON','GEOMETRYCOLLECTION')";
    List<Object[]> resultQuery = fieldRepository.queryExecutionList(query);
    Map<Integer, Map<String, String>> resultMap = new HashMap<>();
    for (int i = 0; i < resultQuery.size(); i++) {
      Map<String, String> fieldMap = new HashMap<>();
      for (int j = 0; j < 10000 && !resultQuery.isEmpty(); j++) {
        fieldMap.put(resultQuery.get(0)[0].toString(), resultQuery.get(0)[1].toString());
        resultQuery.remove(resultQuery.get(0));
      }
      resultMap.put(i, fieldMap);
    }
    return resultMap;
  }

  /**
   * Execute update geometry.
   *
   * @param datasetId the dataset id
   * @param mapFieldValue the map field value
   */
  private void executeUpdateGeometry(Long datasetId, Map<String, String> mapFieldValue) {
    String query =
        "select public.insert_geometry_function_noTrigger(" + datasetId + ", cast(array[";

    Iterator<Entry<String, String>> iterator = mapFieldValue.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, String> entry = iterator.next();
      query += "row('" + entry.getKey() + "', '" + entry.getValue();
      if (iterator.hasNext()) {
        query += "'), ";
      } else {
        query += "')";
      }
    }
    query += "] as public.geom_update[]));";
    fieldRepository.queryExecutionSingle(query);
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
      Map<String, Object> importBigFileData = new HashMap<>();
      importBigFileData.put(LiteralConstants.SIGNATURE,
          LockSignature.IMPORT_BIG_FILE_DATA.getValue());
      importBigFileData.put(LiteralConstants.DATASETID, datasetId);
      lockService.removeLockByCriteria(importBigFileData);
      FileUtils.deleteDirectory(new File(importPath, datasetId.toString()));

      releaseLockReleasingProcess(datasetId);
    } catch (IOException e) {
      LOG_ERROR.error("Error deleting files: datasetId={}", datasetId, e);
    }
  }

  /**
   * File management.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param schema the schema
   * @param multipartFile the multipart file
   * @param replace the replace
   * @param integrationId the integration id
   * @param delimiter the delimiter
   * @throws EEAException the EEA exception
   */
  private void fileManagement(Long datasetId, String tableSchemaId, DataSetSchema schema,
      MultipartFile multipartFile, boolean replace, Long integrationId, String delimiter)
      throws EEAException {

    String originalFileName = multipartFile.getOriginalFilename();
    String multipartFileMimeType = datasetService.getMimetype(originalFileName);
    IntegrationVO integrationVO;
    if (null == integrationId) {
      integrationVO = null;
    } else {
      integrationVO = getIntegrationVO(integrationId);
      if (null == integrationVO) {
        LOG_ERROR.error("Error in fileManagement. Integration {} not found. datasetId: {} and tableSchemaId: {}", integrationId, datasetId, tableSchemaId);
      }
    }

    try (InputStream input = multipartFile.getInputStream()) {

      // Prepare the folder where files will be stored
      File root = new File(importPath);
      File folder = new File(root, datasetId.toString());
      String saveLocationPath = folder.getCanonicalPath();

      // Delete dataset temporary folder first in case that for any reason still exists before
      // creating again
      FileUtils.deleteQuietly(folder);
      if (!folder.mkdirs()) {
        releaseLock(datasetId);
        datasetMetabaseService.updateDatasetRunningStatus(datasetId,
            DatasetRunningStatusEnum.ERROR_IN_IMPORT);
        throw new EEAException("Folder for dataset " + datasetId + " already exists");
      }

      List<File> files = new ArrayList<>();
      if (null == integrationVO && "zip".equalsIgnoreCase(multipartFileMimeType)) {

        try (ZipInputStream zip = new ZipInputStream(input)) {
          files = unzipAndStore(folder, saveLocationPath, zip);
        }

        // Queue import tasks for stored files
        if (!files.isEmpty()) {
          queueImportProcess(datasetId, null, schema, files, originalFileName, integrationVO,
              replace, delimiter, multipartFileMimeType);
        } else {
          releaseLock(datasetId);
          datasetMetabaseService.updateDatasetRunningStatus(datasetId,
              DatasetRunningStatusEnum.ERROR_IN_IMPORT);
          LOG_ERROR.error("Error trying to import a zip file into datasetId {} and tableSchemaId: {}. Empty zip file",
              datasetId, tableSchemaId);
          throw new EEAException("Empty zip file");
        }
      } else {

        File file = new File(folder, originalFileName);

        // Store the file in the persistence volume
        try (FileOutputStream output = new FileOutputStream(file)) {
          IOUtils.copyLarge(input, output);
          files.add(file);
          LOG.info("Stored file {} in fileManagement. For datasetId {} and tableSchemaId {}", file.getPath(), datasetId, tableSchemaId);
        }

        // if the import goes it's a zip file, check if the zip is not empty to show
        // error and avoid the call to FME
        if (null != integrationVO && "zip".equalsIgnoreCase(multipartFileMimeType)) {
          try {
            ZipFile zipFile = new ZipFile(file);
            if (zipFile.size() == 0) {
              zipFile.close();
              releaseLock(datasetId);
              throw new EEAException("Empty zip file for datasetId " + datasetId + " and tableSchemaId " + tableSchemaId);
            }
            zipFile.close();
          } catch (IOException e) {
            releaseLock(datasetId);
            throw new EEAException("Empty zip file for datasetId " + datasetId + " and tableSchemaId " + tableSchemaId);
          }
        }

        // Queue import task for the stored file
        queueImportProcess(datasetId, tableSchemaId, schema, files, originalFileName, integrationVO,
            replace, delimiter, multipartFileMimeType);

        LOG.info("Queued import process for datasetId {} and tableSchemaId {}", datasetId, tableSchemaId);
      }

    } catch (EEAException | FeignException | IOException e) {
      LOG_ERROR.error(
          "Unexpected exception importing file data: datasetId={}, file={}. Message: {}", datasetId,
          multipartFile.getName(), e.getMessage(), e);
      releaseLock(datasetId);
      datasetMetabaseService.updateDatasetRunningStatus(datasetId,
          DatasetRunningStatusEnum.ERROR_IN_IMPORT);
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
      if ((entryName.split("/").length > 1)
          || !FileTypeEnum.CSV.getValue().equalsIgnoreCase(mimeType) || entry.isDirectory()
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
   * @param replace the replace
   * @param delimiter the delimiter
   * @param mimeType the mime type
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   * @throws FeignException the feign exception
   */
  private void queueImportProcess(Long datasetId, String tableSchemaId, DataSetSchema schema,
      List<File> files, String originalFileName, IntegrationVO integrationVO, boolean replace,
      String delimiter, String mimeType) throws IOException, EEAException {
      LOG.info("Queueing import process for datasetId {} tableSchemaId {} and file {}", datasetId, tableSchemaId, originalFileName);
    if (null != integrationVO) {
      prepareFmeFileProcess(datasetId, files.get(0), integrationVO, mimeType, tableSchemaId,
          replace);
    } else {
      importExecutorService.submit(() -> {
        try {
          rn3FileProcess(datasetId, tableSchemaId, schema, files, originalFileName, replace,
              delimiter);
        } catch (Exception e) {
          LOG_ERROR.error("RN3-Import: Unexpected error in queueImportProcess for datasetId {} and tableSchemaId {}. {}", datasetId, tableSchemaId, e.getMessage(), e);
          if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
          }
        }
      });
    }
    LOG.info("Finished queueing import process for datasetId {} tableSchemaId {} and file {}", datasetId, tableSchemaId, originalFileName);
  }


  /**
   * Fme file process.
   *
   * @param datasetId the dataset id
   * @param file the file
   * @param integrationVO the integration VO
   * @param mimeType the mime type
   * @param tableSchemaId the table schema id
   * @param replace the replace
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  private void prepareFmeFileProcess(Long datasetId, File file, IntegrationVO integrationVO,
      String mimeType, String tableSchemaId, boolean replace) throws IOException, EEAException {

    LOG.info("Start FME-Import process: datasetId={}, integrationVO={}", datasetId, integrationVO);
    Map<String, String> internalParameters = integrationVO.getInternalParameters();

    // Remove the lock so FME will not encounter it while calling back importFileData
    if (!"true".equals(internalParameters.get(IntegrationParams.NOTIFICATION_REQUIRED))
        || IntegrationOperationTypeEnum.IMPORT_FROM_OTHER_SYSTEM
            .equals(integrationVO.getOperation())
        || "zip".equalsIgnoreCase(mimeType)) {
      Map<String, Object> importFileData = new HashMap<>();
      importFileData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_FILE_DATA.getValue());
      importFileData.put(LiteralConstants.DATASETID, datasetId);
      lockService.removeLockByCriteria(importFileData);
      Map<String, Object> importBigFileData = new HashMap<>();
      importBigFileData.put(LiteralConstants.SIGNATURE,
          LockSignature.IMPORT_BIG_FILE_DATA.getValue());
      importBigFileData.put(LiteralConstants.DATASETID, datasetId);
      lockService.removeLockByCriteria(importBigFileData);
      releaseLockReleasingProcess(datasetId);
    }

    // delete precious data if necessary
    if (replace) {
      wipeDataAsync(datasetId, tableSchemaId, file, integrationVO);
      LOG.info("Data has been wiped for datasetId {}", datasetId);
    } else {
      Map<String, Object> valuesFME = new HashMap<>();
      valuesFME.put("datasetId", datasetId);
      valuesFME.put("fileName", file);
      valuesFME.put("integrationId", integrationVO.getId());
      kafkaSenderUtils.releaseKafkaEvent(EventType.CONTINUE_FME_PROCESS_EVENT, valuesFME);
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
   * @param replace the replace
   * @param delimiter the delimiter
   * @throws InterruptedException the interrupted exception
   */
  private void rn3FileProcess(Long datasetId, String tableSchemaId, DataSetSchema datasetSchema,
      List<File> files, String originalFileName, boolean replace, String delimiter)
      throws InterruptedException {
    LOG.info("Start RN3-Import process: datasetId={}, files={}", datasetId, files);

    // delete precious data if necessary
    wipeData(datasetId, tableSchemaId, replace);
    LOG.info("Data has been wiped during rn3FileProcess datasetId {}, files {}", datasetId, files);

    // Wait a second before continue to avoid duplicated insertions
    Thread.sleep(1000);

    String error = null;
    boolean guessTableName = null == tableSchemaId;
    boolean errorWrongFilename = false;
    int numberOfWrongFiles = 0;
    for (File file : files) {
      String fileName = file.getName();

      try (InputStream inputStream = new FileInputStream(file)) {

        if (guessTableName) {
          tableSchemaId = getTableSchemaIdFromFileName(datasetSchema, fileName);
        }
        if (!guessTableName || StringUtils.isNotBlank(tableSchemaId)) {
          LOG.info("Start RN3-Import file: fileName={}, tableSchemaId={}", fileName, tableSchemaId);

          processFile(datasetId, fileName, inputStream, tableSchemaId, replace, datasetSchema,
              delimiter);

          LOG.info("Finish RN3-Import file: fileName={}, tableSchemaId={}", fileName,
              tableSchemaId);
        } else {
          LOG_ERROR.error(
              "RN3-Import file failed: fileName={}. There's no table with that fileName", fileName);
          datasetMetabaseService.updateDatasetRunningStatus(datasetId,
              DatasetRunningStatusEnum.ERROR_IN_IMPORT);
          errorWrongFilename = true;
          numberOfWrongFiles++;
          if (numberOfWrongFiles == files.size()) {
            errorWrongFilename = false;
            throw new EEAException(EEAErrorMessage.ERROR_FILE_NAME_MATCHING);
          }
        }
      } catch (IOException | EEAException e) {
        LOG_ERROR.error("RN3-Import file failed: fileName={}, tableSchemaId={}", fileName,
            tableSchemaId, e);
        error = e.getMessage();
      }
    }

    updateGeometry(datasetId, datasetSchema);

    if (files.size() == 1) {
      finishImportProcess(datasetId, tableSchemaId, originalFileName, error, errorWrongFilename);
    } else {
      finishImportProcess(datasetId, null, originalFileName, error, errorWrongFilename);
    }
    LOG.info("Finished import process for datasetId {} and file {}", datasetId, originalFileName);

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
    String tableSchemaId = "";
    String tableName = fileName.substring(0, fileName.lastIndexOf((".")));
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      if (tableSchema.getNameTableSchema().equalsIgnoreCase(tableName)) {
        tableSchemaId = tableSchema.getIdTableSchema().toString();
      }
    }
    return tableSchemaId;
  }

  /**
   * Finish import process conditionally.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param originalFileName the original file name
   * @param error the error
   * @param errorWrongFilename the error wrong filename
   */
  private void finishImportProcess(Long datasetId, String tableSchemaId, String originalFileName,
      String error, boolean errorWrongFilename) {
    try {

      releaseLock(datasetId);

      Map<String, Object> value = new HashMap<>();
      value.put(LiteralConstants.DATASET_ID, datasetId);
      value.put(LiteralConstants.USER,
          SecurityContextHolder.getContext().getAuthentication().getName());
      NotificationVO notificationVO = NotificationVO.builder()
          .user(SecurityContextHolder.getContext().getAuthentication().getName())
          .datasetId(datasetId).tableSchemaId(tableSchemaId).fileName(originalFileName).error(error)
          .build();

      EventType eventType;
      DatasetTypeEnum type = datasetService.getDatasetType(datasetId);
      if (null != error) {
        if (EEAErrorMessage.ERROR_FILE_NAME_MATCHING.equals(error)) {
          eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
              ? EventType.IMPORT_REPORTING_FAILED_NAMEFILE_EVENT
              : EventType.IMPORT_DESIGN_FAILED_NAMEFILE_EVENT;
        } else if (EEAErrorMessage.ERROR_FILE_NO_HEADERS_MATCHING.equals(error)) {
          eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
              ? EventType.IMPORT_REPORTING_FAILED_NO_HEADERS_MATCHING_EVENT
              : EventType.IMPORT_DESIGN_FAILED_NO_HEADERS_MATCHING_EVENT;
        } else {
          eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
              ? EventType.IMPORT_REPORTING_FAILED_EVENT
              : EventType.IMPORT_DESIGN_FAILED_EVENT;
        }
        datasetMetabaseService.updateDatasetRunningStatus(datasetId,
            DatasetRunningStatusEnum.ERROR_IN_IMPORT);
      } else {
        datasetMetabaseService.updateDatasetRunningStatus(datasetId,
            DatasetRunningStatusEnum.IMPORTED);

        eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
            ? EventType.IMPORT_REPORTING_COMPLETED_EVENT
            : EventType.IMPORT_DESIGN_COMPLETED_EVENT;

      }

      kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value, notificationVO);
      // If importing a zip a file doesn't match with the table and the process ignores it, we send
      // a warning notification
      if (errorWrongFilename) {
        NotificationVO notificationWarning = NotificationVO.builder()
            .user(SecurityContextHolder.getContext().getAuthentication().getName())
            .datasetId(datasetId).fileName(originalFileName).build();
        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_NAMEFILE_WARNING_EVENT,
            value, notificationWarning);
      }
    } catch (EEAException e) {
      LOG_ERROR.error("RN3-Import file error for datasetId {}", datasetId, e);
    }
  }


  /**
   * Gets the integration VO.
   *
   * @param integrationId the integration id
   * @return the integration VO
   */
  private IntegrationVO getIntegrationVO(Long integrationId) {
    return integrationController.findIntegrationById(integrationId);
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
        datasetService.deleteImportData(datasetId, true);
      }
    }
  }

  /**
   * Wipe data async.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param file the file
   * @param integrationVO the integration VO
   */
  @Async
  private void wipeDataAsync(Long datasetId, String tableSchemaId, File file,
      IntegrationVO integrationVO) {
    if (null != tableSchemaId) {
      datasetService.deleteTableBySchema(tableSchemaId, datasetId);
    } else {
      datasetService.deleteImportData(datasetId, true);
    }

    Map<String, Object> valuesFME = new HashMap<>();
    valuesFME.put("datasetId", datasetId);
    valuesFME.put("fileName", file);
    valuesFME.put("integrationId", integrationVO.getId());
    kafkaSenderUtils.releaseKafkaEvent(EventType.CONTINUE_FME_PROCESS_EVENT, valuesFME);
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
   * Process file.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param is the is
   * @param idTableSchema the id table schema
   * @param replace the replace
   * @param schema the schema
   * @param delimiter the delimiter
   * @return the data set VO
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void processFile(@DatasetId Long datasetId, String fileName, InputStream is,
      String idTableSchema, boolean replace, DataSetSchema schema, String delimiter)
      throws EEAException, IOException {
    // obtains the file type from the extension
    if (fileName == null) {
      LOG_ERROR.error("RN3 Import process: Filename is null. DatasetId {}", datasetId);
      throw new EEAException(EEAErrorMessage.FILE_NAME);
    }
    final String mimeType = datasetService.getMimetype(fileName).toLowerCase();
    // validates file types for the data load
    validateFileType(mimeType);
    LOG.info("RN3 Import process: file type has been validated for file {} and datasetId {}",fileName, datasetId);

    try {
      // Get the partition for the partiton id
      final PartitionDataSetMetabase partition = obtainPartition(datasetId, USER);
      LOG.info("RN3 Import process: partition has been obtained for datasetId {}",fileName, datasetId);

      // Get the dataFlowId from the metabase
      final Long dataflowId = datasetService.getDataFlowIdById(datasetId);

      // create the right file parser for the file type
      final IFileParseContext context =
          fileParserFactory.createContext(mimeType, datasetId, delimiter);
      LOG.info("RN3 Import process: context has been created for datasetId {}",fileName, datasetId);

      ConnectionDataVO connectionDataVO = recordStoreControllerZuul
          .getConnectionToDataset(LiteralConstants.DATASET_PREFIX + datasetId);

      context.parse(is, dataflowId, partition.getId(), idTableSchema, datasetId, fileName, replace,
          schema, connectionDataVO);
      LOG.info("RN3 Import process: context has been parsed for datasetId {}",fileName, datasetId);

    } catch (Exception e) {
      LOG.error("Error in RN3 Import process: processing file for datasetId {}", datasetId, e);
      throw e;
    } finally {
      is.close();
    }
  }

  /**
   * Validate file type.
   *
   * @param mimeType the mime type
   * @throws EEAException the EEA exception
   */
  private void validateFileType(final String mimeType) throws EEAException {
    // files that will be accepted: csv, xml, xls, xlsx
    switch (FileTypeEnum.getEnum(mimeType)) {
      case CSV:
        break;
      case XML:
        break;
      case XLS:
        break;
      case XLSX:
        break;
      default:
        throw new InvalidFileException(EEAErrorMessage.FILE_FORMAT);
    }
  }



  /**
   * Generate file.
   *
   * @param datasetId the dataset id
   * @param mimeType the mime type
   * @param files the files
   * @param includeZip the include zip
   * @param datasetType the dataset type
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  private void generateFile(Long datasetId, String mimeType, Map<String, byte[]> files,
      boolean includeZip, DatasetTypeEnum datasetType) throws IOException, EEAException {

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
              TenantResolver
                  .setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, datasetId));
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

        for (Entry<String, byte[]> entry : files.entrySet()) {
          // Creating the name of the files inside the zip
          String nameFileXlsxCsv = "";
          if (entry.getKey() == null) {
            nameFileXlsxCsv = nameDataset;
          } else {
            nameFileXlsxCsv =
                entry.getKey().substring(entry.getKey().indexOf("_") + 1, entry.getKey().length());
          }
          if (FileTypeEnum.VALIDATIONS.getValue().equals(mimeType)) {
            mimeType = FileTypeEnum.XLSX.getValue();
          }
          // Adding the xlsx/csv file to the zip
          ZipEntry e = new ZipEntry(nameFileXlsxCsv + "." + mimeType);
          out.putNextEntry(e);
          out.write(entry.getValue(), 0, entry.getValue().length);
          out.closeEntry();
        }
        LOG.info("Creating file {} in the route ", fileWriteZip);
      }
    }
    // only the xlsx file
    else {
      if (FileTypeEnum.VALIDATIONS.getValue().equals(mimeType)) {
        mimeType = FileTypeEnum.XLSX.getValue();
      }
      nameFile = nameDataset + "." + mimeType;
      File fileWrite = new File(new File(pathPublicFile, "dataset-" + datasetId), nameFile);
      try (OutputStream out = new FileOutputStream(fileWrite.toString())) {
        for (Entry<String, byte[]> entry : files.entrySet()) {
          out.write(entry.getValue(), 0, entry.getValue().length);
        }
      }
    }
    // Send notification
    NotificationVO notificationVO = NotificationVO.builder()
        .user(SecurityContextHolder.getContext().getAuthentication().getName()).datasetId(datasetId)
        .datasetName(nameFile).datasetType(datasetType).build();

    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.EXPORT_DATASET_COMPLETED_EVENT, null,
        notificationVO);

  }



  /**
   * Table value for.
   *
   * @param datasetId the dataset id
   * @param dataset the dataset
   * @param readOnlyTables the read only tables
   * @param fixedNumberTables the fixed number tables
   * @param allRecords the all records
   * @param tableWithAttachmentFieldSet the table with attachment field set
   * @param tableSchemas the table schemas
   */
  private void tableValueFor(Long datasetId, DatasetValue dataset, List<String> readOnlyTables,
      List<String> fixedNumberTables, List<RecordValue> allRecords,
      Set<String> tableWithAttachmentFieldSet, List<TableSchema> tableSchemas) {
    for (TableValue tableValue : dataset.getTableValues()) {
      // Check if the table with idTableSchema has been populated already
      Long oldTableId =
          datasetService.findTableIdByTableSchema(datasetId, tableValue.getIdTableSchema());
      fillTableId(tableValue.getIdTableSchema(), dataset.getTableValues(), oldTableId);
      if (!readOnlyTables.contains(tableValue.getIdTableSchema())
          && !fixedNumberTables.contains(tableValue.getIdTableSchema())) {
        // Put an empty value to the field if it's an attachment type if and only if table has
        // fields of this type
        if (tableWithAttachmentFieldSet.contains(tableValue.getIdTableSchema())) {
          LOG.warn("Table {} and id schema {} contains attachment field, processing",
              tableValue.getId(), tableValue.getIdTableSchema());
          tableValue.getRecords().stream().forEach(r -> {
            r.getFields().stream().forEach(f -> {
              switch (f.getType()) {
                case ATTACHMENT:
                  f.setValue("");
                  break;
                case POINT:
                  break;
                case LINESTRING:
                  break;
                case POLYGON:
                  break;
                case MULTIPOINT:
                  break;
                case MULTILINESTRING:
                  break;
                case MULTIPOLYGON:
                  break;
                case GEOMETRYCOLLECTION:
                  break;
                default:
                  if (null != f.getValue() && f.getValue().length() >= fieldMaxLength) {
                    f.setValue(f.getValue().substring(0, fieldMaxLength));
                  } else {
                    f.setValue(f.getValue());
                  }
              }
            });
          });
        }
        allRecords.addAll(tableValue.getRecords());
      } else if (!readOnlyTables.contains(tableValue.getIdTableSchema())
          && fixedNumberTables.contains(tableValue.getIdTableSchema())) {
        ObjectId tableSchemaIdTemp = new ObjectId(tableValue.getIdTableSchema());
        TableSchema tableSchema = tableSchemas.stream()
            .filter(tableSchemaIt -> tableSchemaIt.getIdTableSchema().equals(tableSchemaIdTemp))
            .findFirst().orElse(null);
        if (tableSchema != null) {
          datasetService.updateRecordsWithConditions(tableValue.getRecords(), datasetId,
              tableSchema);
        }
      }
      if (null == oldTableId) {
        tableRepository.saveAndFlush(tableValue);
      }
    }
  }

  /**
   * Etl table for.
   *
   * @param etlDatasetVO the etl dataset VO
   * @param provider the provider
   * @param partition the partition
   * @param tableMap the table map
   * @param fieldMap the field map
   * @param dataset the dataset
   * @param tables the tables
   * @param readOnlyTables the read only tables
   * @param fixedNumberTables the fixed number tables
   * @param datasetType the dataset type
   */
  private void etlTableFor(ETLDatasetVO etlDatasetVO, DataProviderVO provider,
      final PartitionDataSetMetabase partition, Map<String, TableSchema> tableMap,
      Map<String, FieldSchema> fieldMap, DatasetValue dataset, List<TableValue> tables,
      List<String> readOnlyTables, List<String> fixedNumberTables, DatasetTypeEnum datasetType) {
    for (ETLTableVO etlTable : etlDatasetVO.getTables()) {
      etlBuildEntity(provider, partition, tableMap, fieldMap, dataset, tables, etlTable,
          datasetType);
      // Check if table is read Only and save into a list
      TableSchema tableSchema = tableMap.get(etlTable.getTableName().toLowerCase());
      if (tableSchema != null && Boolean.TRUE.equals(tableSchema.getReadOnly())) {
        readOnlyTables.add(tableSchema.getIdTableSchema().toString());
      }
      if (!DatasetTypeEnum.DESIGN.equals(datasetType) && tableSchema != null
          && Boolean.TRUE.equals(tableSchema.getFixedNumber())) {
        fixedNumberTables.add(tableSchema.getIdTableSchema().toString());
      }
    }
  }

  /**
   * Etl build entity.
   *
   * @param provider the provider
   * @param partition the partition
   * @param tableMap the table map
   * @param fieldMap the field map
   * @param dataset the dataset
   * @param tables the tables
   * @param etlTable the etl table
   * @param datasetType the dataset type
   */
  private void etlBuildEntity(DataProviderVO provider, final PartitionDataSetMetabase partition,
      Map<String, TableSchema> tableMap, Map<String, FieldSchema> fieldMap, DatasetValue dataset,
      List<TableValue> tables, ETLTableVO etlTable, DatasetTypeEnum datasetType) {
    TableValue table = new TableValue();
    TableSchema tableSchema = tableMap.get(etlTable.getTableName().toLowerCase());
    if (tableSchema != null) {
      table.setIdTableSchema(tableSchema.getIdTableSchema().toString());
      List<RecordValue> records = new ArrayList<>();
      for (ETLRecordVO etlRecord : etlTable.getRecords()) {
        RecordValue recordValue = new RecordValue();
        recordValue.setIdRecordSchema(tableMap.get(etlTable.getTableName().toLowerCase())
            .getRecordSchema().getIdRecordSchema().toString());
        recordValue.setTableValue(table);
        List<FieldValue> fieldValues = new ArrayList<>();
        List<String> idSchema = new ArrayList<>();
        etlFieldBuildFor(fieldMap, dataset, tableSchema, etlRecord, recordValue, fieldValues,
            idSchema, datasetType);
        // set the fields if not declared in the records
        setMissingField(
            tableMap.get(etlTable.getTableName().toLowerCase()).getRecordSchema().getFieldSchema(),
            fieldValues, idSchema, recordValue);
        recordValue.setFields(fieldValues);
        recordValue.setDatasetPartitionId(partition.getId());
        recordValue.setDataProviderCode(provider != null ? provider.getCode() : null);
        records.add(recordValue);
      }
      table.setRecords(records);
      tables.add(table);
      table.setDatasetId(dataset);
    }
  }

  /**
   * Etl field build for.
   *
   * @param fieldMap the field map
   * @param dataset the dataset
   * @param tableSchema the table schema
   * @param etlRecord the etl record
   * @param recordValue the record value
   * @param fieldValues the field values
   * @param idFieldSchemas the id field schemas
   * @param datasetType the dataset type
   */
  private void etlFieldBuildFor(Map<String, FieldSchema> fieldMap, DatasetValue dataset,
      TableSchema tableSchema, ETLRecordVO etlRecord, RecordValue recordValue,
      List<FieldValue> fieldValues, List<String> idFieldSchemas, DatasetTypeEnum datasetType) {
    for (ETLFieldVO etlField : etlRecord.getFields()) {
      FieldValue field = new FieldValue();
      FieldSchema fieldSchema =
          fieldMap.get(etlField.getFieldName().toLowerCase() + tableSchema.getIdTableSchema());
      // Fill if is a design dataset or if not readonly
      if (fieldSchema != null && ((!Boolean.TRUE.equals(fieldSchema.getReadOnly())
          && !DatasetTypeEnum.DESIGN.equals(datasetType))
          || DatasetTypeEnum.DESIGN.equals(datasetType) || tableSchema.getFixedNumber())) {

        field.setIdFieldSchema(fieldSchema.getIdFieldSchema().toString());
        field.setType(fieldSchema.getType());
        field.setValue(etlField.getValue());
        field.setRecord(recordValue);
        fieldValues.add(field);
        idFieldSchemas.add(field.getIdFieldSchema());
      }
    }
  }

  /**
   * Sets the missing field.
   *
   * @param headersSchema the headers schema
   * @param fields the fields
   * @param idSchema the id schema
   * @param recordValue the record value
   */
  private void setMissingField(List<FieldSchema> headersSchema, final List<FieldValue> fields,
      List<String> idSchema, RecordValue recordValue) {
    headersSchema.stream().forEach(header -> {
      if (!idSchema.contains(header.getIdFieldSchema().toString())) {
        final FieldValue field = new FieldValue();
        field.setIdFieldSchema(header.getIdFieldSchema().toString());
        field.setType(header.getType());
        field.setValue("");
        field.setRecord(recordValue);
        fields.add(field);
      }
    });
  }

  /**
   * Obtain partition.
   *
   * @param datasetId the dataset id
   * @param user the user
   * @return the partition data set metabase
   * @throws EEAException the EEA exception
   */
  private PartitionDataSetMetabase obtainPartition(final Long datasetId, final String user)
      throws EEAException {
    final PartitionDataSetMetabase partition = partitionDataSetMetabaseRepository
        .findFirstByIdDataSet_idAndUsername(datasetId, user).orElse(null);
    if (partition == null) {
      LOG_ERROR.error(EEAErrorMessage.PARTITION_ID_NOTFOUND);
      throw new EEAException(EEAErrorMessage.PARTITION_ID_NOTFOUND);
    }
    return partition;
  }



  /**
   * Creates the all dataset files.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void createAllDatasetFiles(Long dataflowId, Long dataProviderId) throws IOException {

    ExportFilterVO filters = new ExportFilterVO();
    DataProviderVO dataProvider = representativeControllerZuul.findDataProviderById(dataProviderId);

    List<DataSetMetabase> datasetMetabaseList =
        dataSetMetabaseRepository.findByDataflowIdAndDataProviderId(dataflowId, dataProviderId);

    // now we create all files depends if they are avaliable
    for (DataSetMetabase datasetToFile : datasetMetabaseList) {
      if (schemasRepository
          .findAvailableInPublicByIdDataSetSchema(new ObjectId(datasetToFile.getDatasetSchema()))) {

        // we put the good in the correct field
        List<DesignDataset> desingDataset = designDatasetRepository.findByDataflowId(dataflowId);
        // we find the name of the dataset to asing it for file
        String datasetDesingName = "";
        for (DesignDataset designDatasetVO : desingDataset) {
          if (designDatasetVO.getDatasetSchema()
              .equalsIgnoreCase(datasetToFile.getDatasetSchema())) {
            datasetDesingName = designDatasetVO.getDataSetName();
          }
        }

        try {
          // 1º we create
          byte[] file =
              createFile(datasetToFile.getId(), FileTypeEnum.XLSX.getValue(), null, filters);
          // we save the file in its files
          if (null != file) {
            String nameFileUnique = String.format(FILE_PUBLIC_DATASET_PATTERN_NAME,
                dataProvider.getCode(), datasetDesingName);
            String nameFileScape = nameFileUnique + ".xlsx";

            // we create the files and zip with the document if it is necessary
            createFilesAndZip(dataflowId, dataProviderId, datasetToFile, file, nameFileUnique,
                nameFileScape);


            // we save the file in metabase with the name without the route
            datasetToFile.setPublicFileName(nameFileUnique + ".zip");
            dataSetMetabaseRepository.save(datasetToFile);
          }
        } catch (EEAException e) {
          LOG_ERROR.error(
              "File not created in dataflow {} with dataprovider {} with datasetId {} message {}",
              dataflowId, datasetToFile.getDataProviderId(), datasetToFile.getId(), e.getMessage(),
              e);
        }
        LOG.info("Start files created in DataflowId: {} with DataProviderId: {}", dataflowId,
            datasetToFile.getDataProviderId());
      } else {
        datasetToFile.setPublicFileName(null);
        dataSetMetabaseRepository.save(datasetToFile);
      }
    }
  }


  private void createFilesAndZip(Long dataflowId, Long dataProviderId,
      DataSetMetabase datasetToFile, byte[] file, String nameFileUnique, String nameFileScape)
      throws IOException {

    // we create folder to save the file.zip
    File fileFolderProvider = null;
    if (dataProviderId != null) {
      fileFolderProvider = new File((new File(pathPublicFile, "dataflow-" + dataflowId.toString())),
          "dataProvider-" + dataProviderId.toString());
    } else {
      fileFolderProvider = new File(pathPublicFile, "dataflow-" + dataflowId.toString());
    }
    fileFolderProvider.mkdirs();

    // we create the file.zip
    File fileWriteZip = null;
    if (dataProviderId != null) {
      fileWriteZip =
          new File(new File(new File(pathPublicFile, "dataflow-" + dataflowId.toString()),
              "dataProvider-" + dataProviderId.toString()), nameFileUnique + ".zip");
    } else {
      fileWriteZip = new File(new File(pathPublicFile, "dataflow-" + dataflowId.toString()),
          nameFileUnique + ".zip");
    }
    // create the context to add all files in a treemap inside to attachment and file information
    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(fileWriteZip.toString()))) {
      // we get the dataschema and check every table to see if find any field attachemnt
      DataSetSchema dataSetSchema =
          schemasRepository.findByIdDataSetSchema(new ObjectId(datasetToFile.getDatasetSchema()));
      for (TableSchema tableSchema : dataSetSchema.getTableSchemas()) {

        // we find if in any table have one field type ATTACHMENT
        List<FieldSchema> fieldSchemaAttachment = tableSchema.getRecordSchema().getFieldSchema()
            .stream().filter(field -> DataType.ATTACHMENT.equals(field.getType()))
            .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(fieldSchemaAttachment)) {

          LOG.info("We  are in tableSchema with id {} looking if we have attachments",
              tableSchema.getIdTableSchema());
          // We took every field for every table
          for (FieldSchema fieldAttach : fieldSchemaAttachment) {
            List<AttachmentValue> attachmentValue = attachmentRepository
                .findAllByIdFieldSchemaAndValueIsNotNull(fieldAttach.getIdFieldSchema().toString());

            // if there are filled we create a folder and inside of any folder we create the fields
            if (!CollectionUtils.isEmpty(attachmentValue)) {
              LOG.info(
                  "We  are in tableSchema with id {}, checking field {} and we have attachments files",
                  tableSchema.getIdTableSchema(), fieldAttach.getIdFieldSchema());

              for (AttachmentValue attachment : attachmentValue) {
                try {
                  ZipEntry eFieldAttach = new ZipEntry(
                      tableSchema.getNameTableSchema() + "/" + attachment.getFileName());
                  out.putNextEntry(eFieldAttach);
                  out.write(attachment.getContent(), 0, attachment.getContent().length);
                } catch (ZipException e) {
                  LOG.info("Error creating file {} because already exist", attachment.getFileName(),
                      e);
                }
                out.closeEntry();
              }
            }
          }
        }
      }

      ZipEntry e = new ZipEntry(nameFileScape);
      out.putNextEntry(e);
      out.write(file, 0, file.length);
      out.closeEntry();
      LOG.info("We create file {} in the route ", fileWriteZip);
    }
  }

  /**
   * Gets the code.
   *
   * @param dataflowId the dataflow id
   * @param datasetType the dataset type
   * @return the code
   */
  private String getCode(Long dataflowId, DatasetTypeEnum datasetType) {
    String includeCountryCode = null;
    if (DatasetTypeEnum.EUDATASET.equals(datasetType)
        || DatasetTypeEnum.COLLECTION.equals(datasetType)) {
      TypeDataflowEnum dataflowType = dataflowControllerZuul.getMetabaseById(dataflowId).getType();
      switch (dataflowType) {
        case REPORTING:
          includeCountryCode = "Country Code";
          break;
        case BUSINESS:
          includeCountryCode = "Company Code";
          break;
        case CITIZEN_SCIENCE:
          includeCountryCode = "Citizen Science Code";
          break;
        default:
          includeCountryCode = "Country Code";
          break;
      }
    }
    return includeCountryCode;
  }
}
