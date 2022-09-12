package org.eea.recordstore.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DataCollectionController.DataCollectionControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.controller.dataset.ReferenceDatasetController.ReferenceDatasetControllerZuul;
import org.eea.interfaces.controller.dataset.TestDatasetController.TestDatasetControllerZuul;
import org.eea.interfaces.controller.document.DocumentController.DocumentControllerZuul;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.utils.LiteralConstants;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.copy.CopyOut;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import feign.FeignException;

/**
 * The Class JdbcRecordStoreServiceImpl.
 */
@Service("jdbcRecordStoreServiceImpl")
public class JdbcRecordStoreServiceImpl implements RecordStoreService {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(JdbcRecordStoreServiceImpl.class);

  /** The Constant DELETE_FROM_DATASET: {@value}. */
  private static final String DELETE_FROM_DATASET = "DELETE FROM dataset_";

  /** The Constant COPY_DATASET: {@value}. */
  private static final String COPY_DATASET = "COPY dataset_";

  /** The Constant SNAPSHOT_QUERY: {@value}. */
  private static final String SNAPSHOT_QUERY = "snapshot_";

  /** The Constant COLLECTION: {@value}. */
  private static final String COLLECTION = "collection";

  /** The Constant SCHEMA: {@value}. */
  private static final String SCHEMA = "schema";

  /** The Constant SNAPSHOT: {@value}. */
  private static final String SNAPSHOT = "snapshot";

  /** The Constant REFERENCE: {@value}. */
  private static final String REFERENCE = "reference";

  /** The Constant FILE_PATTERN_NAME: {@value}. */
  private static final String FILE_PATTERN_NAME = "snapshot_%s%s";

  /** The Constant FILE_CLONE_PATTERN_NAME: {@value}. */
  private static final String FILE_CLONE_PATTERN_NAME = "clone_%s_to_%s%s";

  /** The Constant GRANT_ALL_PRIVILEGES_ON_SCHEMA: {@value}. */
  private static final String GRANT_ALL_PRIVILEGES_ON_SCHEMA =
      "grant all privileges on schema %s to %s;";

  /** The Constant GRANT_ALL_PRIVILEGES_ON_ALL_TABLES_ON_SCHEMA: {@value}. */
  private static final String GRANT_ALL_PRIVILEGES_ON_ALL_TABLES_ON_SCHEMA =
      "grant all privileges on all tables in schema %s to %s;";

  /** The Constant GRANT_ALL_PRIVILEGES_ON_ALL_SEQUENCES_ON_SCHEMA: {@value}. */
  private static final String GRANT_ALL_PRIVILEGES_ON_ALL_SEQUENCES_ON_SCHEMA =
      "grant all privileges on all sequences in schema %s to %s;";

  /** The Constant QUERY_FILTER_BY_ID_RECORD: {@value}. */
  private static final String QUERY_FILTER_BY_ID_RECORD =
      ".field_value fv where fv.id_record=rv.id and fv.id_field_schema = '";

  /** The Constant AS: {@value}. */
  private static final String AS = "') AS ";

  /** The Constant COMMA: {@value}. */
  private static final String COMMA = ", ";

  /** The user postgre db. */
  @Value("${spring.datasource.dataset.username}")
  private String userPostgreDb;

  /** The pass postgre db. */
  @Value("${spring.datasource.dataset.password}")
  private String passPostgreDb;

  /** The conn string postgre. */
  @Value("${spring.datasource.url}")
  private String connStringPostgre;

  /** The sql get datasets name. */
  @Value("${sqlGetAllDatasetsName}")
  private String sqlGetDatasetsName;

  /** The dataset users. */
  @Value("${dataset.users}")
  private String datasetUsers;

  /** The resource file. */
  @Value("classpath:datasetInitCommands.txt")
  private Resource resourceFile;

  /** The resource file. */
  @Value("classpath:datasetInitCommandsCitusComplete.txt")
  private Resource resourceCitusFile;

  /** The resource file. */
  @Value("classpath:datasetDistributeCitus.txt")
  private Resource resourceDistributeFile;

  /** The resource file. */
  @Value("classpath:datasetInitCommandsCitus.txt")
  private Resource resourceDistributeFirstFile;


  /** The path snapshot. */
  @Value("${pathSnapshot}")
  private String pathSnapshot;

  /** The path snapshot disabled. */
  @Value("${pathSnapshotDisabled}")
  private String pathSnapshotDisabled;

  /** The time to wait before releasing notification. */
  @Value("${dataset.creation.notification.ms}")
  private Long timeToWaitBeforeReleasingNotification;

  /** The buffer file. */
  @Value("${snapshot.bufferSize}")
  private Integer bufferFile;

  /** The batch distribute dataset. */
  @Value("${batchDistributeDataset}")
  private Integer batchDistributeDataset;

  /** The jdbc template. */
  @Autowired
  private JdbcTemplate jdbcTemplate;

  /** The data source. */
  @Autowired
  private DataSource dataSource;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The data collection controller zuul. */
  @Autowired
  private DataCollectionControllerZuul dataCollectionControllerZuul;

  /** The data set snapshot controller zuul. */
  @Autowired
  private DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul;

  /** The dataset controller zuul. */
  @Autowired
  private DataSetControllerZuul datasetControllerZuul;

  /** The data set metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  /** The dataset schema controller. */
  @Autowired
  private DatasetSchemaControllerZuul datasetSchemaController;

  /** The eu dataset controller zuul. */
  @Autowired
  private EUDatasetControllerZuul euDatasetControllerZuul;

  /** The test dataset controller zuul. */
  @Autowired
  private TestDatasetControllerZuul testDatasetControllerZuul;

  /** The document controller zuul. */
  @Autowired
  private DocumentControllerZuul documentControllerZuul;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The reference dataset controller zuul. */
  @Autowired
  private ReferenceDatasetControllerZuul referenceDatasetControllerZuul;

  /**
   * Creates a schema for each entry in the list. Also releases events to feed the new schemas.
   * <p>
   * <b>Note:</b> {@literal @}<i>Async</i> annotated method.
   * </p>
   *
   * @param datasetIdsAndSchemaIds Map matching datasetIds with datasetSchemaIds.
   * @param dataflowId The DataCollection's dataflow.
   * @param isCreation the is creation
   * @param isMaterialized the is materialized
   */
  @Override
  @Async
  public void createSchemas(Map<Long, String> datasetIdsAndSchemaIds, Long dataflowId,
      boolean isCreation, boolean isMaterialized) {

    // Initialize resources
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        BufferedReader br =
            new BufferedReader(new InputStreamReader(resourceFile.getInputStream()))) {

      // Read file and create queries
      String command;
      while ((command = br.readLine()) != null) {
        for (Long datasetId : datasetIdsAndSchemaIds.keySet()) {
          statement.addBatch(
              command.replace("%dataset_name%", LiteralConstants.DATASET_PREFIX + datasetId)
                  .replace("%user%", userPostgreDb));
        }
      }

      // granting access to the rest of the database users. This way all the micros will be able to
      // use their users
      for (Long datasetId : datasetIdsAndSchemaIds.keySet()) {
        statement.addBatch(String.format(GRANT_ALL_PRIVILEGES_ON_SCHEMA,
            LiteralConstants.DATASET_PREFIX + datasetId, datasetUsers));
        statement.addBatch(String.format(GRANT_ALL_PRIVILEGES_ON_ALL_TABLES_ON_SCHEMA,
            LiteralConstants.DATASET_PREFIX + datasetId, datasetUsers));
        statement.addBatch(String.format(GRANT_ALL_PRIVILEGES_ON_ALL_SEQUENCES_ON_SCHEMA,
            LiteralConstants.DATASET_PREFIX + datasetId, datasetUsers));
      }

      // Execute queries and commit results
      statement.executeBatch();
      statement.clearBatch();
      LOG.info("{} Schemas created as part of DataCollection creation.",
          datasetIdsAndSchemaIds.size());
      // waiting X seconds before releasing notifications, so database is able to write the
      // creation of all datasets
      Thread.sleep(timeToWaitBeforeReleasingNotification);
      LOG.info("Releasing notifications via Kafka");
      // Release events to initialize databases content
      releaseConnectionCreatedEvents(datasetIdsAndSchemaIds, isMaterialized);

      // Release the lock
      String methodSignature = isCreation ? LockSignature.CREATE_DATA_COLLECTION.getValue()
          : LockSignature.UPDATE_DATA_COLLECTION.getValue();
      Map<String, Object> lockCriteria = new HashMap<>();
      lockCriteria.put(LiteralConstants.SIGNATURE, methodSignature);
      lockCriteria.put(LiteralConstants.DATAFLOWID, dataflowId);
      lockService.removeLockByCriteria(lockCriteria);

      // command to assign national coordinators and end the dataCollectionProcess.
      Map<String, Object> result = new HashMap<>();
      result.put("dataflowId", dataflowId);
      result.put("isCreation", isCreation);
      kafkaSenderUtils.releaseKafkaEvent(EventType.DATACOLLECTION_NATIONAL_COORDINATOR_EVENT,
          result);

    } catch (SQLException | IOException e) {
      LOG_ERROR.error("Error creating schemas. Rolling back: ", e);
      // This method will release the lock
      dataCollectionControllerZuul.undoDataCollectionCreation(
          new ArrayList<>(datasetIdsAndSchemaIds.keySet()), dataflowId, isCreation);
    } catch (InterruptedException e) {
      LOG_ERROR.error("Error sleeping thread before releasing notification kafka events", e);
      Thread.currentThread().interrupt();
    }
  }


  /**
   * Distribute tables.
   *
   * @param datasetId the dataset id
   */
  @Override
  @Async
  public void distributeTables(Long datasetId) {

    // Initialize resources
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        BufferedReader br =
            new BufferedReader(new InputStreamReader(resourceDistributeFile.getInputStream()))) {

      final List<String> citusCommands = new ArrayList<>();
      br.lines().forEach(citusCommands::add);

      for (String citusCommand : citusCommands) {
        citusCommand =
            citusCommand.replace("%dataset_name%", LiteralConstants.DATASET_PREFIX + datasetId);
        jdbcTemplate.execute(citusCommand);
      }
    } catch (final IOException | SQLException e) {
      LOG_ERROR.error("Error reading commands file to distribute the dataset. {}", e.getMessage());
      try {
        throw new RecordStoreAccessException(String.format(
            "Error reading commands file to distribute the dataset. %s", e.getMessage()), e);
      } catch (RecordStoreAccessException e1) {
        LOG.info(e1.getMessage(), e);
      }
    }

  }


  /**
   * Distribute tables job.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void distributeTablesJob(Long datasetId) {

    // Initialize resources
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        BufferedReader br =
            new BufferedReader(new InputStreamReader(resourceCitusFile.getInputStream()))) {

      final List<String> citusCommands = new ArrayList<>();
      br.lines().forEach(citusCommands::add);

      for (String citusCommand : citusCommands) {
        citusCommand =
            citusCommand.replace("%dataset_name%", LiteralConstants.DATASET_PREFIX + datasetId);
        jdbcTemplate.execute(citusCommand);
      }
      // After distributing tables the view gets deleted so we need to recreate them again
      createUpdateQueryViewAsync(datasetId, true);
    } catch (final IOException | SQLException e) {
      LOG_ERROR.error("Error reading commands file to distribute the dataset. {}", e.getMessage());
      try {
        throw new RecordStoreAccessException(String.format(
            "Error reading commands file to distribute the dataset. %s", e.getMessage()), e);
      } catch (RecordStoreAccessException e1) {
        LOG.info(e1.getMessage(), e);
      }
    }

  }

  /**
   * Gets the notdistributed datasets.
   *
   * @return the notdistributed datasets
   */
  @Override
  public List<String> getNotdistributedDatasets() {
    String datasetsToDistribute =
        "select schema_name from information_schema.schemata where schema_name like 'dataset_%' and schema_name not in (SELECT replace (logicalrelid::text, '.dataset_value','') from pg_dist_partition where logicalrelid::text like '%dataset_value') order by random() limit "
            + batchDistributeDataset;
    return jdbcTemplate.queryForList(datasetsToDistribute, String.class);
  }


  /**
   * Creates the empty data set. This method is used to create the schema of the design datasets
   *
   * @param datasetName the dataset name
   * @param idDatasetSchema the id dataset schema
   *
   * @throws RecordStoreAccessException the record store access exception
   */
  @Override
  @Async
  public void createEmptyDataSet(String datasetName, String idDatasetSchema)
      throws RecordStoreAccessException {

    final List<String> commands = new ArrayList<>();
    // read file into stream, try-with-resources
    try (BufferedReader br =
        new BufferedReader(new InputStreamReader(resourceFile.getInputStream()))) {

      br.lines().forEach(commands::add);

    } catch (final IOException e) {
      LOG_ERROR.error("Error reading commands file to create the dataset. {}", e.getMessage());
      throw new RecordStoreAccessException(
          String.format("Error reading commands file to create the dataset. %s", e.getMessage()),
          e);
    }
    for (String command : commands) {
      command = command.replace("%dataset_name%", datasetName);
      command = command.replace("%user%", userPostgreDb);
      jdbcTemplate.execute(command);
    }
    // Granting rights to the rest of the users, so every microservice is able to use its own user
    jdbcTemplate.execute(String.format(GRANT_ALL_PRIVILEGES_ON_SCHEMA, datasetName, datasetUsers));
    jdbcTemplate.execute(
        String.format(GRANT_ALL_PRIVILEGES_ON_ALL_TABLES_ON_SCHEMA, datasetName, datasetUsers));
    jdbcTemplate.execute(
        String.format(GRANT_ALL_PRIVILEGES_ON_ALL_SEQUENCES_ON_SCHEMA, datasetName, datasetUsers));

    LOG.info("Empty design dataset created");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      LOG.info("Propagate Error");
    }
    // Now we insert the values into the dataset_value table of the brand new schema
    StringBuilder insertSql = new StringBuilder("INSERT INTO ");
    insertSql.append(datasetName).append(".dataset_value(id, id_dataset_schema) values (?, ?)");
    if (StringUtils.isNotBlank(datasetName) && StringUtils.isNotBlank(idDatasetSchema)) {
      String[] aux = datasetName.split("_");
      Long idDataset = Long.valueOf(aux[aux.length - 1]);
      jdbcTemplate.update(insertSql.toString(), idDataset, idDatasetSchema);
      LOG.info("DS created with the id {} and idDatasetSchema {}", idDataset, idDatasetSchema);
    }
  }

  /**
   * Gets the connection data for dataset.
   *
   * @param datasetName the dataset name
   * @return the connection data for dataset
   */
  @Override
  public ConnectionDataVO getConnectionDataForDataset(String datasetName) {
    final List<String> datasets = getAllDataSetsName(datasetName);
    ConnectionDataVO result = new ConnectionDataVO();
    for (final String dataset : datasets) {

      if (datasetName.equals(dataset)) {
        result = createConnectionDataVO(dataset);
        break;
      }
    }
    return result;
  }

  /**
   * Gets the connection data for dataset.
   *
   * @return the connection data for dataset
   */
  @Override
  public List<ConnectionDataVO> getConnectionDataForDataset() {
    final List<String> datasets = getAllDataSetsName("");
    List<ConnectionDataVO> result = new ArrayList<>();
    for (final String dataset : datasets) {
      result.add(createConnectionDataVO(dataset));
    }
    return result;
  }


  /**
   * Creates the data snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @param idPartitionDataset the id partition dataset
   * @param dateRelease the date release
   * @param prefillingReference the prefilling reference
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void createDataSnapshot(Long idDataset, Long idSnapshot, Long idPartitionDataset,
      String dateRelease, boolean prefillingReference)
      throws SQLException, IOException, EEAException {

    ConnectionDataVO connectionDataVO =
        getConnectionDataForDataset(LiteralConstants.DATASET_PREFIX + idDataset);
    String type = SNAPSHOT;
    try (Connection con = DriverManager.getConnection(connectionDataVO.getConnectionString(),
        connectionDataVO.getUser(), connectionDataVO.getPassword())) {
      type = checkType(idDataset, idSnapshot);
      CopyManager cm = new CopyManager((BaseConnection) con);

      // Copy dataset_value
      String nameFileDatasetValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
          LiteralConstants.SNAPSHOT_FILE_DATASET_SUFFIX);
      String copyQueryDataset = "COPY (SELECT id, id_dataset_schema FROM dataset_" + idDataset
          + ".dataset_value) to STDOUT";

      printToFile(nameFileDatasetValue, copyQueryDataset, cm);
      // Copy table_value
      String nameFileTableValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
          LiteralConstants.SNAPSHOT_FILE_TABLE_SUFFIX);

      String copyQueryTable = "COPY (SELECT id, id_table_schema, dataset_id FROM dataset_"
          + idDataset + ".table_value) to STDOUT";

      printToFile(nameFileTableValue, copyQueryTable, cm);

      DatasetTypeEnum typeDataset = datasetControllerZuul.getDatasetType(idDataset);
      String copyQueryRecord;
      String copyQueryField;
      // Special case to make the snapshot to copy from DataCollection to EUDataset. The sql copy
      // all the values from the DC, no matter what partitionId has the origin, but we need to put
      // in the file the partitionId of the EUDataset destination
      String copyQueryAttachment =
          "COPY (SELECT at.id, at.file_name, at.content, at.field_value_id from dataset_"
              + idDataset + ".attachment_value at) to STDOUT";
      if (DatasetTypeEnum.COLLECTION.equals(typeDataset)) {
        String providersCode = getProvidersCode(idDataset);
        copyQueryRecord = "COPY (SELECT id, id_record_schema, id_table, " + idPartitionDataset
            + ",data_provider_code FROM dataset_" + idDataset
            + ".record_value WHERE data_provider_code in (" + providersCode
            + ") order by data_position) to STDOUT";
        copyQueryField =
            "COPY (SELECT fv.id, fv.type, fv.value, fv.id_field_schema, fv.id_record from dataset_"
                + idDataset + ".field_value fv, dataset_" + idDataset
                + ".record_value rv WHERE fv.id_record = rv.id " + "AND rv.data_provider_code in ("
                + providersCode + ")) to STDOUT";
        copyQueryAttachment =
            "COPY (SELECT at.id, at.file_name, at.content, at.field_value_id from dataset_"
                + idDataset + ".attachment_value at, dataset_" + idDataset
                + ".field_value fv, dataset_" + idDataset
                + ".record_value rv WHERE at.field_value_id = fv.id AND fv.id_record = rv.id "
                + "AND rv.data_provider_code in (" + providersCode + ")) to STDOUT";
      } else if (!DatasetTypeEnum.COLLECTION.equals(typeDataset)
          && Boolean.TRUE.equals(prefillingReference)) {
        copyQueryRecord = "COPY (SELECT id, id_record_schema, id_table, " + idPartitionDataset
            + ",data_provider_code FROM dataset_" + idDataset
            + ".record_value order by data_position) to STDOUT";
        copyQueryField =
            "COPY (SELECT fv.id, fv.type, fv.value, fv.id_field_schema, fv.id_record from dataset_"
                + idDataset + ".field_value fv) to STDOUT";
      } else {
        copyQueryRecord =
            "COPY (SELECT id, id_record_schema, id_table, dataset_partition_id, data_provider_code FROM dataset_"
                + idDataset + ".record_value WHERE dataset_partition_id=" + idPartitionDataset
                + " order by data_position) to STDOUT";
        copyQueryField =
            "COPY (SELECT fv.id, fv.type, fv.value, fv.id_field_schema, fv.id_record from dataset_"
                + idDataset + ".field_value fv inner join dataset_" + idDataset
                + ".record_value rv on fv.id_record = rv.id where rv.dataset_partition_id="
                + idPartitionDataset + ") to STDOUT";
      }

      // Copy record_value
      String nameFileRecordValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
          LiteralConstants.SNAPSHOT_FILE_RECORD_SUFFIX);

      printToFile(nameFileRecordValue, copyQueryRecord, cm);

      // Copy field_value
      String nameFileFieldValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
          LiteralConstants.SNAPSHOT_FILE_FIELD_SUFFIX);

      printToFile(nameFileFieldValue, copyQueryField, cm);

      // Copy attachment_value
      String nameFileAttachmentValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
          LiteralConstants.SNAPSHOT_FILE_ATTACHMENT_SUFFIX);

      printToFile(nameFileAttachmentValue, copyQueryAttachment, cm);

      LOG.info("Snapshot {} data files created", idSnapshot);

      // Check if the snapshot is completed. If it is an schema snapshot, check the rules file.
      // Otherwise check the attachment file
      long startTime = System.currentTimeMillis();
      String nameFileRules =
          String.format("rulesSnapshot_%s-DesignDataset_%s", idSnapshot, idDataset)
              + LiteralConstants.SNAPSHOT_EXTENSION;
      if (DatasetTypeEnum.DESIGN.equals(typeDataset) && Boolean.FALSE.equals(prefillingReference)) {
        while ((System.currentTimeMillis() - startTime) < 30000) {
          try {
            documentControllerZuul.getSnapshotDocument(idDataset, nameFileRules);
            break;
          } catch (FeignException e) {
            LOG.info(
                "Document: {} still not created from dataset: {} and snapshot: {}, wait {} milliseconds",
                nameFileRules, idDataset, idSnapshot, timeToWaitBeforeReleasingNotification);
            Thread.sleep(timeToWaitBeforeReleasingNotification);
          }
        }
      } else {
        while ((System.currentTimeMillis() - startTime) < 30000) {
          try {
            FileUtils.touch(new File(nameFileAttachmentValue));
            break;
          } catch (IOException e) {
            LOG.info(
                "Waiting to finish the snapshot {} from dataset {} to complete before sending the notification",
                idSnapshot, idDataset);
            Thread.sleep(timeToWaitBeforeReleasingNotification);
          }
        }
      }

      notificationCreateAndCheckRelease(idDataset, idSnapshot, type, dateRelease,
          prefillingReference);

      // release snapshot when the user press create+release
    } catch (Exception e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      EventType eventType = null;
      switch (type) {
        case SNAPSHOT:
          eventType = EventType.ADD_DATASET_SNAPSHOT_FAILED_EVENT;
          // Remove the locks just in case there is a releasing datasets process
          DataSetMetabaseVO dataset =
              dataSetMetabaseControllerZuul.findDatasetMetabaseById(idDataset);
          dataSetSnapshotControllerZuul.releaseLocksFromReleaseDatasets(dataset.getDataflowId(),
              dataset.getDataProviderId());
          break;
        case COLLECTION:
          removeLocksRelatedToPopulateEU(
              dataSetMetabaseControllerZuul.findDatasetMetabaseById(idDataset).getDataflowId());
          eventType = EventType.COPY_DATA_TO_EUDATASET_FAILED_EVENT;
          break;
        case SCHEMA:
          eventType = EventType.ADD_DATASET_SCHEMA_SNAPSHOT_FAILED_EVENT;
          break;
        default:
          break;
      }
      LOG_ERROR.error("Error creating snapshot for dataset {}", idDataset, e);
      Map<String, Object> value = new HashMap<>();
      value.put(LiteralConstants.DATASET_ID, idDataset);
      releaseNotificableKafkaEvent(eventType, value, idDataset, e.getMessage());

    } finally {
      // Release the lock manually
      if (SNAPSHOT.equals(type)) {
        SnapshotVO snapshot = dataSetSnapshotControllerZuul.getById(idSnapshot);
        Map<String, Object> createSnapshot = new HashMap<>();
        createSnapshot.put(LiteralConstants.SIGNATURE, LockSignature.CREATE_SNAPSHOT.getValue());
        createSnapshot.put(LiteralConstants.DATASETID, idDataset);
        createSnapshot.put(LiteralConstants.RELEASED, snapshot.getRelease());
        lockService.removeLockByCriteria(createSnapshot);
      }
      if (SCHEMA.equals(type)) {
        Map<String, Object> createSchemaSnapshot = new HashMap<>();
        createSchemaSnapshot.put(LiteralConstants.SIGNATURE,
            LockSignature.CREATE_SCHEMA_SNAPSHOT.getValue());
        createSchemaSnapshot.put(LiteralConstants.DATASETID, idDataset);
        lockService.removeLockByCriteria(createSchemaSnapshot);
      }
    }
  }

  /**
   * Creates the snapshot to clone.
   *
   * @param originDataset the origin dataset
   * @param targetDataset the target dataset
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param partitionDatasetTarget the partition dataset target
   * @param tableSchemasIdPrefill the table schemas id prefill
   */
  @Override
  @Async
  public void createSnapshotToClone(Long originDataset, Long targetDataset,
      Map<String, String> dictionaryOriginTargetObjectId, Long partitionDatasetTarget,
      List<String> tableSchemasIdPrefill) {

    LOG.info(
        "Copying the data from the dataset {} to dataset {} because this is a cloning process and there are tables to prefill",
        originDataset, targetDataset);

    String nameFileTableValue = pathSnapshot + String.format(FILE_CLONE_PATTERN_NAME, originDataset,
        targetDataset, LiteralConstants.SNAPSHOT_FILE_TABLE_SUFFIX);

    String nameFileRecordValue = pathSnapshot + String.format(FILE_CLONE_PATTERN_NAME,
        originDataset, targetDataset, LiteralConstants.SNAPSHOT_FILE_RECORD_SUFFIX);

    String nameFileFieldValue = pathSnapshot + String.format(FILE_CLONE_PATTERN_NAME, originDataset,
        targetDataset, LiteralConstants.SNAPSHOT_FILE_FIELD_SUFFIX);

    String nameFileAttachmentValue = pathSnapshot + String.format(FILE_CLONE_PATTERN_NAME,
        originDataset, targetDataset, LiteralConstants.SNAPSHOT_FILE_ATTACHMENT_SUFFIX);

    ConnectionDataVO connectionDataVO =
        getConnectionDataForDataset(LiteralConstants.DATASET_PREFIX + originDataset);

    // Extract the data from the prefilled tables in the origin dataset
    try (Connection con = DriverManager.getConnection(connectionDataVO.getConnectionString(),
        connectionDataVO.getUser(), connectionDataVO.getPassword())) {

      CopyManager cm = new CopyManager((BaseConnection) con);
      StringBuilder builder = new StringBuilder();
      String placeHolders = "";
      for (int i = 0; i < tableSchemasIdPrefill.size(); i++) {
        builder.append("'" + tableSchemasIdPrefill.get(i) + "',");
      }
      placeHolders = builder.deleteCharAt(builder.length() - 1).toString();

      String copyQueryTable = "COPY (SELECT id, id_table_schema, dataset_id FROM dataset_"
          + originDataset + ".table_value) to STDOUT";
      String copyQueryRecord = "COPY (SELECT rv.id, rv.id_record_schema, rv.id_table, "
          + partitionDatasetTarget + ", rv.data_provider_code FROM dataset_" + originDataset
          + ".record_value rv, dataset_" + originDataset + ".table_value tv "
          + "WHERE rv.id_table=tv.id and tv.id_table_schema in (" + placeHolders + ")) to STDOUT";
      String copyQueryField =
          "COPY (SELECT fv.id, fv.type, fv.value, fv.id_field_schema, fv.id_record from dataset_"
              + originDataset + ".field_value fv, dataset_" + originDataset
              + ".record_value rv, dataset_" + originDataset + ".table_value tv"
              + " WHERE fv.id_record = rv.id and rv.id_table=tv.id" + " and tv.id_table_schema in ("
              + placeHolders + ")) to STDOUT";
      String copyQueryAttachment =
          "COPY (SELECT at.id, at.file_name, at.content, at.field_value_id from dataset_"
              + originDataset + ".attachment_value at, dataset_" + originDataset
              + ".field_value fv, dataset_" + originDataset + ".record_value rv, dataset_"
              + originDataset + ".table_value tv "
              + "WHERE at.field_value_id =fv.id and fv.id_record = rv.id and rv.id_table=tv.id"
              + " and tv.id_table_schema in (" + placeHolders + ")) to STDOUT";

      // Copy table_value
      printToFile(nameFileTableValue, copyQueryTable, cm);
      // Copy record_value
      printToFile(nameFileRecordValue, copyQueryRecord, cm);
      // Copy field_value
      printToFile(nameFileFieldValue, copyQueryField, cm);
      // Copy attachment_value
      printToFile(nameFileAttachmentValue, copyQueryAttachment, cm);

      modifySnapshotFile(dictionaryOriginTargetObjectId, Arrays.asList(nameFileTableValue,
          nameFileRecordValue, nameFileFieldValue, nameFileAttachmentValue), targetDataset);

    } catch (SQLException | IOException e) {
      LOG_ERROR.error("Error creating the data from the origin dataset {}", originDataset, e);
      deleteFile(Arrays.asList(nameFileTableValue, nameFileRecordValue, nameFileFieldValue,
          nameFileAttachmentValue));
    }

    // Copy the data from the snapshot file into the target dataset
    ConnectionDataVO conexion =
        getConnectionDataForDataset(LiteralConstants.DATASET_PREFIX + targetDataset);
    try (
        Connection con = DriverManager.getConnection(conexion.getConnectionString(),
            conexion.getUser(), conexion.getPassword());
        Statement stmt = con.createStatement()) {
      con.setAutoCommit(true);
      CopyManager cm = new CopyManager((BaseConnection) con);

      // Delete the previous table values
      String sql = DELETE_FROM_DATASET + targetDataset + ".table_value";
      stmt.executeUpdate(sql);

      String copyQueryTable =
          COPY_DATASET + targetDataset + ".table_value(id, id_table_schema, dataset_id) FROM STDIN";
      copyFromFile(copyQueryTable, nameFileTableValue, cm);

      String copyQueryRecord = COPY_DATASET + targetDataset
          + ".record_value(id, id_record_schema, id_table, dataset_partition_id, data_provider_code) FROM STDIN";
      copyFromFile(copyQueryRecord, nameFileRecordValue, cm);

      String copyQueryField = COPY_DATASET + targetDataset
          + ".field_value(id, type, value, id_field_schema, id_record) FROM STDIN";
      copyFromFile(copyQueryField, nameFileFieldValue, cm);

      String copyQueryAttachment = COPY_DATASET + targetDataset
          + ".attachment_value(id, file_name, content, field_value_id) FROM STDIN";
      copyFromFile(copyQueryAttachment, nameFileAttachmentValue, cm);

    } catch (SQLException | IOException e) {
      LOG_ERROR.error("Error restoring the data into the target dataset {}", targetDataset, e);
    } finally {
      // Deleting the snapshot files after copy
      deleteFile(Arrays.asList(nameFileRecordValue, nameFileFieldValue, nameFileAttachmentValue));
      LOG.info("Process copying the data prefilled from the dataset {} to dataset {} finished",
          originDataset, targetDataset);
    }
  }


  /**
   * Restore data snapshot.
   *
   * @param idReportingDataset the id reporting dataset
   * @param idSnapshot the id snapshot
   * @param partitionId the partition id
   * @param datasetType the dataset type
   * @param isSchemaSnapshot the is schema snapshot
   * @param deleteData the delete data
   * @param prefillingReference the prefilling reference
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public void restoreDataSnapshot(Long idReportingDataset, Long idSnapshot, Long partitionId,
      DatasetTypeEnum datasetType, Boolean isSchemaSnapshot, Boolean deleteData,
      boolean prefillingReference) throws SQLException, IOException {

    EventType successEventType = Boolean.TRUE.equals(deleteData)
        ? Boolean.TRUE.equals(isSchemaSnapshot)
            ? EventType.RESTORE_DATASET_SCHEMA_SNAPSHOT_COMPLETED_EVENT
            : EventType.RESTORE_DATASET_SNAPSHOT_COMPLETED_EVENT
        : EventType.RELEASE_COMPLETED_EVENT;
    EventType failEventType =
        Boolean.TRUE.equals(deleteData)
            ? Boolean.TRUE.equals(isSchemaSnapshot)
                ? EventType.RESTORE_DATASET_SCHEMA_SNAPSHOT_FAILED_EVENT
                : EventType.RESTORE_DATASET_SNAPSHOT_FAILED_EVENT
            : EventType.RELEASE_FAILED_EVENT;

    restoreSnapshot(idReportingDataset, idSnapshot, partitionId, datasetType, isSchemaSnapshot,
        deleteData, successEventType, failEventType, prefillingReference);

  }

  /**
   * Delete data snapshot.
   *
   * @param idReportingDataset the id reporting dataset
   * @param idSnapshot the id snapshot
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public void deleteDataSnapshot(Long idReportingDataset, Long idSnapshot) throws IOException {

    String nameFileDatasetValue =
        SNAPSHOT_QUERY + idSnapshot + LiteralConstants.SNAPSHOT_FILE_DATASET_SUFFIX;
    String nameFileTableValue =
        SNAPSHOT_QUERY + idSnapshot + LiteralConstants.SNAPSHOT_FILE_TABLE_SUFFIX;
    String nameFileRecordValue =
        SNAPSHOT_QUERY + idSnapshot + LiteralConstants.SNAPSHOT_FILE_RECORD_SUFFIX;
    String nameFileFieldValue =
        SNAPSHOT_QUERY + idSnapshot + LiteralConstants.SNAPSHOT_FILE_FIELD_SUFFIX;
    String nameAttachmentValue =
        SNAPSHOT_QUERY + idSnapshot + LiteralConstants.SNAPSHOT_FILE_ATTACHMENT_SUFFIX;

    Path path1 = Paths.get(pathSnapshot + nameFileDatasetValue);
    Files.deleteIfExists(path1);
    Path path2 = Paths.get(pathSnapshot + nameFileTableValue);
    Files.deleteIfExists(path2);
    Path path3 = Paths.get(pathSnapshot + nameFileRecordValue);
    Files.deleteIfExists(path3);
    Path path4 = Paths.get(pathSnapshot + nameFileFieldValue);
    Files.deleteIfExists(path4);
    Path path5 = Paths.get(pathSnapshot + nameAttachmentValue);
    Files.deleteIfExists(path5);
  }

  /**
   * Delete dataset.
   *
   * @param datasetSchemaName the dataset schema name
   */
  @Override
  public void deleteDataset(String datasetSchemaName) {
    StringBuilder stringBuilder = new StringBuilder("DROP SCHEMA IF EXISTS ");
    stringBuilder.append(datasetSchemaName).append(" CASCADE");
    jdbcTemplate.execute(stringBuilder.toString());
  }

  /**
   * Creates the data set from other.
   *
   * @param sourceDatasetName the source dataset name
   * @param destinationDataSetName the destination data set name
   */
  @Override
  public void createDataSetFromOther(String sourceDatasetName, String destinationDataSetName) {
    throw new java.lang.UnsupportedOperationException("Operation not implemented yet");
  }

  /**
   * Execute query view commands.
   *
   * @param command the command
   * @throws RecordStoreAccessException the record store access exception
   */
  @Override
  public void executeQueryViewCommands(String command) throws RecordStoreAccessException {
    jdbcTemplate.execute(command);
    LOG.info("Command on Query View executed: {}", command);
  }


  /**
   * Creates the update query view.
   *
   * @param datasetId the dataset id
   * @param isMaterialized the is materialized
   */
  @Override
  public void createUpdateQueryView(Long datasetId, boolean isMaterialized) {
    LOG.info("Executing createUpdateQueryView on the datasetId {}. Materialized: {}", datasetId,
        isMaterialized);
    DataSetSchemaVO datasetSchema =
        datasetSchemaController.findDataSchemaByDatasetIdPrivate(datasetId);
    // delete all views because some names can be changed
    try {
      deleteAllViewsFromSchema(datasetId);
      deleteAllMatViewsFromSchema(datasetId);
    } catch (RecordStoreAccessException e1) {
      LOG_ERROR.error("Error deleting Query view: {}", e1.getMessage(), e1);
    }

    datasetSchema.getTableSchemas().stream()
        .filter(table -> !CollectionUtils.isEmpty(table.getRecordSchema().getFieldSchema()))
        .forEach(table -> {
          List<FieldSchemaVO> columns = table.getRecordSchema().getFieldSchema();
          try {
            // create materialiced view or query view of all tableSchemas
            executeViewQuery(columns, table.getNameTableSchema(), table.getIdTableSchema(),
                datasetId, true);
            createIndexMaterializedView(datasetId, table.getNameTableSchema());
            // execute view permission
            executeViewPermissions(table.getNameTableSchema(), datasetId);
          } catch (RecordStoreAccessException e) {
            LOG_ERROR.error("Error creating Query view: {}", e.getMessage(), e);
          }
        });
  }


  /**
   * Creates the update query view async.
   *
   * @param datasetId the dataset id
   * @param isMaterialized the is materialized
   */
  @Async
  @Override
  public void createUpdateQueryViewAsync(Long datasetId, boolean isMaterialized) {
    LOG.info("Executing createUpdateQueryViewAsync on the datasetId {}. Materialized: {}",
        datasetId, isMaterialized);
    DataSetSchemaVO datasetSchema =
        datasetSchemaController.findDataSchemaByDatasetIdPrivate(datasetId);
    // delete all views because some names can be changed
    try {
      deleteAllViewsFromSchema(datasetId);
      deleteAllMatViewsFromSchema(datasetId);
    } catch (RecordStoreAccessException e1) {
      LOG_ERROR.error("Error deleting Query view: {}", e1.getMessage(), e1);
    }

    datasetSchema.getTableSchemas().stream()
        .filter(table -> !CollectionUtils.isEmpty(table.getRecordSchema().getFieldSchema()))
        .forEach(table -> {
          List<FieldSchemaVO> columns = table.getRecordSchema().getFieldSchema();
          try {
            // create materialiced view or query view of all tableSchemas
            executeViewQuery(columns, table.getNameTableSchema(), table.getIdTableSchema(),
                datasetId, true);
            createIndexMaterializedView(datasetId, table.getNameTableSchema());
            // execute view permission
            executeViewPermissions(table.getNameTableSchema(), datasetId);
          } catch (RecordStoreAccessException e) {
            LOG_ERROR.error("Error creating Query view: {}", e.getMessage(), e);
          }
        });
  }


  /**
   * Update materialized query view.
   *
   * @param datasetId the dataset id
   * @param user the user
   * @param released the released
   * @param processId the process id
   */
  @Override
  @Async
  public void updateMaterializedQueryView(Long datasetId, String user, Boolean released,
      String processId) {
    LOG.info(" Update Materialized Views from Dataset id: {}", datasetId);

    DataSetMetabaseVO datasetMetabaseVO =
        dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    Long dataflowId = datasetMetabaseVO.getDataflowId();
    try {
      switch (datasetMetabaseVO.getDatasetTypeEnum()) {
        case DESIGN:
          List<DesignDatasetVO> designDatasets =
              dataSetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(dataflowId);

          for (DesignDatasetVO dataset : designDatasets) {
            launchUpdateMaterializedQueryView(dataset.getId());
          }
          break;
        case REPORTING:
          List<ReportingDatasetVO> reportingDatasets =
              dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowIdAndProviderId(
                  dataflowId, datasetMetabaseVO.getDataProviderId());

          for (ReportingDatasetVO dataset : reportingDatasets) {
            launchUpdateMaterializedQueryView(dataset.getId());
          }
          break;
        case TEST:
          List<TestDatasetVO> testDatasets =
              testDatasetControllerZuul.findTestDatasetByDataflowId(dataflowId);
          for (TestDatasetVO dataset : testDatasets) {
            launchUpdateMaterializedQueryView(dataset.getId());
          }
          break;
        case COLLECTION:
          List<DataCollectionVO> dataCollectionDatasets =
              dataCollectionControllerZuul.findDataCollectionIdByDataflowId(dataflowId);
          for (DataCollectionVO dataset : dataCollectionDatasets) {
            launchUpdateMaterializedQueryView(dataset.getId());
          }
          break;
        case EUDATASET:
          List<EUDatasetVO> euDatasets =
              euDatasetControllerZuul.findEUDatasetByDataflowId(dataflowId);
          for (EUDatasetVO dataset : euDatasets) {
            launchUpdateMaterializedQueryView(dataset.getId());
          }
          break;
        case REFERENCE:
          List<ReferenceDatasetVO> references =
              referenceDatasetControllerZuul.findReferenceDatasetByDataflowId(dataflowId);
          for (ReferenceDatasetVO dataset : references) {
            launchUpdateMaterializedQueryView(dataset.getId());
          }
          break;
        default:
          break;
      }
    } catch (RecordStoreAccessException e) {
      LOG_ERROR.error("Error updating Materialized view: {}", e.getMessage(), e);
    }
    Map<String, Object> values = new HashMap<>();
    values.put(LiteralConstants.DATASET_ID, datasetId);
    values.put(LiteralConstants.USER,
        SecurityContextHolder.getContext().getAuthentication().getName());
    values.put("released", released);
    values.put("updateViews", false);
    values.put("processId", processId);
    LOG.info(
        "The user set on updateMaterializedQueryView threadPropertiesManager is {}, dataset {}",
        SecurityContextHolder.getContext().getAuthentication().getName(), datasetId);
    LOG.info("The user set on securityContext is {}",
        SecurityContextHolder.getContext().getAuthentication().getName());
    kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, values);
  }

  /**
   * Launch update materialized query view.
   *
   * @param datasetId the dataset id
   * @throws RecordStoreAccessException the record store access exception
   */
  @Override
  public void launchUpdateMaterializedQueryView(Long datasetId) throws RecordStoreAccessException {

    if (!Boolean.TRUE.equals(datasetControllerZuul.getCheckView(datasetId))) {
      datasetControllerZuul.updateCheckView(datasetId, true);
      String viewToUpdate =
          "select matviewname from pg_matviews  where schemaname = 'dataset_" + datasetId + "'";
      List<String> viewList = jdbcTemplate.queryForList(viewToUpdate, String.class);

      String updateQuery = "refresh materialized view concurrently dataset_";

      for (String view : viewList) {
        executeQueryViewCommands(updateQuery + datasetId + "." + "\"" + view + "\"");
      }
      LOG.info("These views: {} have been refreshed.", viewList);
    } else {
      LOG.info("The views from the dataset {} are updated, no need to refresh.", datasetId);
    }
  }

  /**
   * Refresh materialized query.
   *
   * @param datasetIds the dataset ids
   * @param continueValidation the continue validation
   * @param released the released
   * @param datasetId the dataset id
   * @param processId the process id
   */
  @Override
  @Async
  public void refreshMaterializedQuery(List<Long> datasetIds, boolean continueValidation,
      boolean released, Long datasetId, String processId) {
    datasetIds.forEach(id -> {
      if (!Boolean.TRUE.equals(datasetControllerZuul.getCheckView(id))) {
        datasetControllerZuul.updateCheckView(id, true);
        String viewToUpdate =
            "select matviewname from pg_matviews  where schemaname = 'dataset_" + id + "'";
        List<String> viewList = jdbcTemplate.queryForList(viewToUpdate, String.class);

        String updateQuery = "refresh materialized view concurrently dataset_";

        for (String view : viewList) {
          try {
            executeQueryViewCommands(updateQuery + id + "." + "\"" + view + "\"");
          } catch (RecordStoreAccessException e) {
            LOG_ERROR.error("Error refreshing materialized view from dataset {}", id);
          }
        }
        LOG.info("These materialized views: {} have been refreshed.", viewList);
      } else {
        LOG.info("The views from the dataset {} are updated, no need to refresh.", id);
      }
    });
    if (Boolean.TRUE.equals(continueValidation)) {
      Map<String, Object> values = new HashMap<>();
      values.put(LiteralConstants.DATASET_ID, datasetId);
      values.put("released", released);
      values.put("processId", processId);
      kafkaSenderUtils.releaseKafkaEvent(EventType.UPDATE_MATERIALIZED_VIEW_EVENT, values);
    }
  }

  /**
   * Update snapshot disabled.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void updateSnapshotDisabled(Long datasetId) {
    List<SnapshotVO> listSnapshotVO =
        dataSetSnapshotControllerZuul.getSnapshotsEnabledByIdDataset(datasetId);

    File directory = new File(pathSnapshotDisabled);
    directory.mkdir();

    File[] matchingFilesToDelete = matchingFilesSnapshot(false, listSnapshotVO);
    for (File file : matchingFilesToDelete) {
      if (file.delete()) {
        LOG.info("File deleted: {}", file.getAbsolutePath());
      }
    }
    dataSetSnapshotControllerZuul.deleteSnapshotByDatasetIdAndDateReleasedIsNull(datasetId);
    LOG.info("Deleted user snapshots files from dataset: {}", datasetId);

    File[] matchingFilesToMove = matchingFilesSnapshot(true, listSnapshotVO);
    for (File file : matchingFilesToMove) {
      if (file.renameTo(new File(pathSnapshotDisabled + file.getName()))) {
        LOG.info("File: {} moved to: {}", file.getName(), file.getAbsolutePath());
      }
    }
    dataSetSnapshotControllerZuul.updateSnapshotDisabled(datasetId);
    LOG.info("Moved released snapshots files to disabled folder: {}, from dataset: {}",
        pathSnapshotDisabled, datasetId);

    Long dataflowId = datasetControllerZuul.getDataFlowIdById(datasetId);
    datasetControllerZuul.privateDeleteDatasetData(datasetId, dataflowId, true);
    LOG.info("Deleted dataset data from dataset: {}, dataflow: {}", datasetId, dataflowId);
  }

  /**
   * Matching files snapshot.
   *
   * @param released the released
   * @param listSnapshotVO the list snapshot VO
   * @return the file[]
   */
  private File[] matchingFilesSnapshot(boolean released, List<SnapshotVO> listSnapshotVO) {
    File snapshotFolder = new File(pathSnapshot);
    return snapshotFolder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        boolean exists = false;
        for (SnapshotVO snapshotVO : listSnapshotVO) {
          if (name.startsWith("snapshot_" + snapshotVO.getId())) {
            boolean validRelease = snapshotVO.getDateReleased() != null;
            if (validRelease == released) {
              exists = true;
            }
          }
        }
        return exists;
      }
    });
  }

  /**
   * Modify snapshot file.
   *
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param nameFiles the name files
   * @param datasetId the dataset id
   */
  private void modifySnapshotFile(Map<String, String> dictionaryOriginTargetObjectId,
      List<String> nameFiles, Long datasetId) {

    if (!CollectionUtils.isEmpty(nameFiles)) {
      nameFiles.stream().forEach(f -> {

        Path pathFile = Paths.get(f);
        List<String> replaced = new ArrayList<>();

        try (Stream<String> lines = Files.lines(pathFile)) {
          replaced = lines
              .map(line -> line = modifyingLine(dictionaryOriginTargetObjectId, line, f, datasetId))
              .collect(Collectors.toList());
          Files.write(pathFile, replaced);
        } catch (IOException e) {
          LOG_ERROR.error("Error modifying the file {} during the data copy in cloning process", f);
        }
      });
    }
  }


  /**
   * Modifying line.
   *
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param line the line
   * @param fileName the file name
   * @param datasetId the dataset id
   * @return the string
   */
  private String modifyingLine(Map<String, String> dictionaryOriginTargetObjectId, String line,
      String fileName, Long datasetId) {

    String[] lineSplitted = line.split("\t");

    if (fileName.contains("RecordValue")) {
      String recordSchema = lineSplitted[1];
      line = line.replace(recordSchema, dictionaryOriginTargetObjectId.get(recordSchema));
    }
    if (fileName.contains("FieldValue")) {
      String fieldSchema = lineSplitted[3];
      line = line.replace(fieldSchema, dictionaryOriginTargetObjectId.get(fieldSchema));
    }
    if (fileName.contains("TableValue")) {
      String oldDatasetId = lineSplitted[2];
      String tableSchemaId = lineSplitted[1];
      line = line.replace(oldDatasetId, datasetId.toString());
      if (null != dictionaryOriginTargetObjectId) {
        line = line.replace(tableSchemaId, dictionaryOriginTargetObjectId.get(tableSchemaId));
      }
    }
    return line;
  }


  /**
   * Delete file.
   *
   * @param fileNames the file names
   */
  private void deleteFile(List<String> fileNames) {

    if (!CollectionUtils.isEmpty(fileNames)) {
      fileNames.stream().forEach(f -> {
        try {
          Path path1 = Paths.get(f);
          Files.deleteIfExists(path1);
        } catch (IOException e) {
          LOG_ERROR.error("Error deleting the file {} during the data copy in cloning process", f);
        }
      });
    }
  }


  /**
   * Removes the locks related to populate EU.
   *
   * @param dataflowId the dataflow id
   */
  private void removeLocksRelatedToPopulateEU(Long dataflowId) {
    List<ReportingDatasetVO> reportings =
        dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(dataflowId);
    Map<String, Object> populateEuDataset = new HashMap<>();
    populateEuDataset.put(LiteralConstants.SIGNATURE, LockSignature.POPULATE_EU_DATASET.getValue());
    populateEuDataset.put(LiteralConstants.DATAFLOWID, dataflowId);
    lockService.removeLockByCriteria(populateEuDataset);

    for (ReportingDatasetVO reporting : reportings) {
      Map<String, Object> relaseSnapshots = new HashMap<>();
      relaseSnapshots.put(LiteralConstants.SIGNATURE, LockSignature.RELEASE_SNAPSHOTS.getValue());
      relaseSnapshots.put(LiteralConstants.DATAFLOWID, dataflowId);
      relaseSnapshots.put(LiteralConstants.DATAPROVIDERID, reporting.getDataProviderId());
      lockService.removeLockByCriteria(relaseSnapshots);
    }
  }

  /**
   * Check type.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @return the string
   */
  private String checkType(Long idDataset, Long idSnapshot) {
    String type = SNAPSHOT;
    SnapshotVO snapshot = null;
    snapshot = dataSetSnapshotControllerZuul.getSchemaById(idSnapshot);
    if (snapshot != null) {
      type = SCHEMA;
    } else {
      if (DatasetTypeEnum.COLLECTION.equals(dataSetMetabaseControllerZuul.getType(idDataset))) {
        type = COLLECTION;
      }
    }
    return type;
  }

  /**
   * Notification create and check release.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @param type the type
   * @param dateRelease the date release
   * @param prefillingReference the prefilling reference
   */
  private void notificationCreateAndCheckRelease(Long idDataset, Long idSnapshot, String type,
      String dateRelease, boolean prefillingReference) {
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, idDataset);
    LOG.info("The user on notificationCreateAndCheckRelease is {} and the datasetId {}",
        SecurityContextHolder.getContext().getAuthentication().getName(), idDataset);
    LOG.info("The user set on threadPropertiesManager is {}",
        SecurityContextHolder.getContext().getAuthentication().getName());
    if (Boolean.TRUE.equals(prefillingReference)) {
      type = REFERENCE;
    }
    switch (type) {
      case SNAPSHOT:
        SnapshotVO snapshot = dataSetSnapshotControllerZuul.getById(idSnapshot);
        if (Boolean.TRUE.equals(snapshot.getRelease())) {
          dataSetSnapshotControllerZuul.releaseSnapshot(idDataset, idSnapshot, dateRelease);
        } else {
          releaseNotificableKafkaEvent(EventType.ADD_DATASET_SNAPSHOT_COMPLETED_EVENT, value,
              idDataset, null);
        }
        break;
      case COLLECTION:
        Map<String, Object> valueEU = new HashMap<>();
        valueEU.put("user", SecurityContextHolder.getContext().getAuthentication().getName());
        valueEU.put("dataset_id", idDataset);
        valueEU.put("snapshot_id", idSnapshot);
        kafkaSenderUtils.releaseKafkaEvent(EventType.ADD_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT,
            valueEU);
        break;
      case REFERENCE:
        Map<String, Object> valueReference = new HashMap<>();
        valueReference.put("user",
            SecurityContextHolder.getContext().getAuthentication().getName());
        valueReference.put("dataset_id", idDataset);
        valueReference.put("snapshot_id", idSnapshot);
        kafkaSenderUtils.releaseKafkaEvent(
            EventType.COPY_REFERENCE_DATASET_SNAPSHOT_COMPLETED_EVENT, valueReference);
        break;
      case SCHEMA:
        releaseNotificableKafkaEvent(EventType.ADD_DATASET_SCHEMA_SNAPSHOT_COMPLETED_EVENT, value,
            idDataset, null);
        break;
      default:
        break;
    }
  }


  /**
   * Restore snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @param partitionId the partition id
   * @param datasetType the dataset type
   * @param isSchemaSnapshot the is schema snapshot
   * @param deleteData the delete data
   * @param successEventType the success event type
   * @param failEventType the fail event type
   * @param prefillingReference the prefilling reference
   */
  private void restoreSnapshot(Long datasetId, Long idSnapshot, Long partitionId,
      DatasetTypeEnum datasetType, Boolean isSchemaSnapshot, Boolean deleteData,
      EventType successEventType, EventType failEventType, boolean prefillingReference) {

    String signature = Boolean.TRUE.equals(deleteData)
        ? Boolean.TRUE.equals(isSchemaSnapshot) ? LockSignature.RESTORE_SCHEMA_SNAPSHOT.getValue()
            : LockSignature.RESTORE_SNAPSHOT.getValue()
        : null;



    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, datasetId);
    ConnectionDataVO conexion =
        getConnectionDataForDataset(LiteralConstants.DATASET_PREFIX + datasetId);
    // We get the datasetId from the snapshot
    Long datasetIdFromSnapshot = Boolean.TRUE.equals(isSchemaSnapshot)
        ? dataSetSnapshotControllerZuul.getSchemaById(idSnapshot).getDatasetId()
        : dataSetSnapshotControllerZuul.getById(idSnapshot).getDatasetId();

    try (
        Connection con = DriverManager.getConnection(conexion.getConnectionString(),
            conexion.getUser(), conexion.getPassword());
        Statement stmt = con.createStatement()) {
      con.setAutoCommit(true);

      if (Boolean.TRUE.equals(deleteData) && !DatasetTypeEnum.EUDATASET.equals(datasetType)
          || (DatasetTypeEnum.REFERENCE.equals(datasetType) && prefillingReference)) {
        String sql = composeDeleteSql(datasetId, partitionId, datasetType, null);
        LOG.info("Deleting previous data");
        stmt.executeUpdate(sql);
      } else if (Boolean.TRUE.equals(deleteData) && DatasetTypeEnum.EUDATASET.equals(datasetType)) {

        String providersCode = getProvidersCode(datasetId);
        String sql = composeDeleteSql(datasetId, partitionId, datasetType, providersCode);
        LOG.info("Deleting previous data of the providers {} in the EU dataset {}", providersCode,
            datasetId);
        stmt.executeUpdate(sql);

        // Delete the temporary etlExport table
        String sqlDeleteTempEtlExport = "truncate table dataset_" + datasetId + ".temp_etlexport";
        stmt.executeUpdate(sqlDeleteTempEtlExport);
      }


      CopyManager cm = new CopyManager((BaseConnection) con);
      LOG.info("Init restoring the snapshot files from Snapshot {}", idSnapshot);
      copyProcess(datasetId, idSnapshot, datasetType, cm);

      if (!DatasetTypeEnum.EUDATASET.equals(datasetType)
          && !successEventType.equals(EventType.RELEASE_COMPLETED_EVENT) && !prefillingReference) {
        releaseNotificableKafkaEvent(successEventType, value, datasetId, null);
      }
      if (DatasetTypeEnum.REFERENCE.equals(datasetType) && prefillingReference) {
        dataSetSnapshotControllerZuul.deleteSnapshot(datasetIdFromSnapshot, idSnapshot);
        Map<String, Object> createXls = new HashMap<>();
        createXls.put(LiteralConstants.DATASET_ID, datasetId);
        kafkaSenderUtils.releaseKafkaEvent(
            EventType.RESTORE_PREFILLING_REFERENCE_SNAPSHOT_COMPLETED_EVENT, createXls);
      }
      if (DatasetTypeEnum.EUDATASET.equals(datasetType)) {
        // We send the notification only when the last eu dataset being filled from the
        // datacollection,
        // ordered by id, is done
        DataSetMetabaseVO ds =
            dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetIdFromSnapshot);
        List<DataCollectionVO> dcs = dataCollectionControllerZuul
            .findDataCollectionIdByDataflowId(ds.getDataflowId()).stream()
            .sorted(Comparator.comparing(DataCollectionVO::getId)).collect(Collectors.toList());
        List<Long> idsDc = dcs.stream().sorted(Comparator.comparing(DataCollectionVO::getId))
            .map(DataCollectionVO::getId).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(idsDc)
            && datasetIdFromSnapshot.equals(idsDc.get(idsDc.size() - 1))) {
          // This last eu dataset ordered by being copied from the Dc in the process "copy data from
          // dc to eu"
          // so send the notification
          Map<String, Object> valueEU = new HashMap<>();
          valueEU.put(LiteralConstants.DATASET_ID, datasetId);
          valueEU.put("snapshot_id", idSnapshot);
          kafkaSenderUtils.releaseKafkaEvent(
              EventType.RESTORE_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT, valueEU);
        }
        dataSetSnapshotControllerZuul.updateSnapshotEURelease(datasetIdFromSnapshot);
        dataSetSnapshotControllerZuul.deleteSnapshot(datasetIdFromSnapshot, idSnapshot);
      }
      LOG.info("Snapshot {} restored", idSnapshot);
    } catch (Exception e) {
      if (!prefillingReference) {
        if (DatasetTypeEnum.EUDATASET.equals(datasetType)) {
          failEventType = EventType.COPY_DATA_TO_EUDATASET_FAILED_EVENT;
          removeLocksRelatedToPopulateEU(
              dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId).getDataflowId());
        }
        LOG_ERROR.error("Error restoring the snapshot data due to error {}.", e.getMessage(), e);
        releaseNotificableKafkaEvent(failEventType, value, datasetId,
            "Error restoring the snapshot data");
        if (EventType.RELEASE_FAILED_EVENT.equals(failEventType)) {
          LOG_ERROR.error(
              "Release datasets operation failed during the restoring snapshot with the message: {}",
              e.getMessage(), e);
          dataSetSnapshotControllerZuul.releaseLocksFromReleaseDatasets(
              dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId).getDataflowId(),
              datasetId);
          releaseNotificableKafkaEvent(failEventType, value, datasetId,
              "Error restoring the snapshot data");
        }
      } else {
        LOG_ERROR.error(
            "Error restoring the snapshot data into the prefilling reference dataset due to error {}.",
            e.getMessage(), e);
      }
    } finally {
      // Release the lock manually
      if (null != signature) {
        Map<String, Object> lockCriteria = new HashMap<>();
        lockCriteria.put(LiteralConstants.SIGNATURE, signature);
        lockCriteria.put(LiteralConstants.DATASETID, datasetIdFromSnapshot);
        lockService.removeLockByCriteria(lockCriteria);
      }
    }
  }


  /**
   * Gets the providers code.
   *
   * @param datasetId the dataset id
   * @return the providers code
   */
  private String getProvidersCode(Long datasetId) {
    List<String> providers =
        dataCollectionControllerZuul.findProvidersPendingInEuDataset(datasetId);
    StringBuilder codes = new StringBuilder();
    for (int i = 0; i < providers.size(); i++) {
      codes.append("'" + providers.get(i) + "'");
      if (i + 1 != providers.size()) {
        codes.append(",");
      }
    }
    if (StringUtils.isBlank(codes.toString())) {
      codes.append("''");
    }
    return codes.toString();
  }

  /**
   * Copy process.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @param datasetType the dataset type
   * @param cm the cm
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  private void copyProcess(Long datasetId, Long idSnapshot, DatasetTypeEnum datasetType,
      CopyManager cm) throws IOException, SQLException {
    if (DatasetTypeEnum.DESIGN.equals(datasetType)
        || DatasetTypeEnum.REFERENCE.equals(datasetType)) {
      // If it is a design dataset (schema), we need to restore the table values. Otherwise it's
      // not neccesary
      String nameFileTableValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
          LiteralConstants.SNAPSHOT_FILE_TABLE_SUFFIX);

      modifySnapshotFile(null, Arrays.asList(nameFileTableValue), datasetId);

      String copyQueryTable =
          COPY_DATASET + datasetId + ".table_value(id, id_table_schema, dataset_id) FROM STDIN";
      copyFromFile(copyQueryTable, nameFileTableValue, cm);
    }
    // Record value
    String nameFileRecordValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
        LiteralConstants.SNAPSHOT_FILE_RECORD_SUFFIX);

    String copyQueryRecord = COPY_DATASET + datasetId
        + ".record_value(id, id_record_schema, id_table, dataset_partition_id, data_provider_code) FROM STDIN";
    copyFromFile(copyQueryRecord, nameFileRecordValue, cm);

    // Field value
    String nameFileFieldValue = pathSnapshot
        + String.format(FILE_PATTERN_NAME, idSnapshot, LiteralConstants.SNAPSHOT_FILE_FIELD_SUFFIX);

    String copyQueryField = COPY_DATASET + datasetId
        + ".field_value(id, type, value, id_field_schema, id_record) FROM STDIN";
    copyFromFile(copyQueryField, nameFileFieldValue, cm);

    // Attachment value
    String nameFileAttachmentValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
        LiteralConstants.SNAPSHOT_FILE_ATTACHMENT_SUFFIX);

    String copyQueryAttachment = COPY_DATASET + datasetId
        + ".attachment_value(id, file_name, content, field_value_id) FROM STDIN";
    copyFromFile(copyQueryAttachment, nameFileAttachmentValue, cm);
  }

  /**
   * Compose delete sql.
   *
   * @param idReportingDataset the id reporting dataset
   * @param partitionId the partition id
   * @param datasetType the dataset type
   * @param providersCode the providers code
   * @return the string
   */
  private String composeDeleteSql(Long idReportingDataset, Long partitionId,
      DatasetTypeEnum datasetType, String providersCode) {
    String sql = "";
    switch (datasetType) {
      case EUDATASET:
        sql = DELETE_FROM_DATASET + idReportingDataset
            + ".record_value WHERE data_provider_code in (" + providersCode + ")";
        break;
      case REPORTING:
      case TEST:
        sql = DELETE_FROM_DATASET + idReportingDataset + ".record_value WHERE dataset_partition_id="
            + partitionId;
        break;
      case DESIGN:
      case REFERENCE:
        sql = DELETE_FROM_DATASET + idReportingDataset + ".table_value";
        break;
      default:
        break;
    }
    return sql;
  }



  /**
   * Releases CONNECTION_CREATED_EVENT Kafka events to initialize databases content. Before that,
   * insert the values into the schema of the dataset_value and table_value
   *
   * @param datasetIdAndSchemaId dataset ids matching schema ids
   * @param isMaterialized the is materialized
   */
  private void releaseConnectionCreatedEvents(Map<Long, String> datasetIdAndSchemaId,
      boolean isMaterialized) {
    for (Map.Entry<Long, String> entry : datasetIdAndSchemaId.entrySet()) {
      Map<String, Object> result = new HashMap<>();
      String datasetName = "dataset_" + entry.getKey();
      result.put("connectionDataVO", createConnectionDataVO(datasetName));
      result.put(LiteralConstants.DATASET_ID, datasetName);
      result.put(LiteralConstants.ID_DATASET_SCHEMA, entry.getValue());
      kafkaSenderUtils.releaseKafkaEvent(EventType.CONNECTION_CREATED_EVENT, result);

      createUpdateQueryView(entry.getKey(), isMaterialized);


    }
  }

  /**
   * Release notificable kafka event.
   *
   * @param event the event
   * @param value the value
   * @param datasetId the dataset id
   * @param error the error
   */
  void releaseNotificableKafkaEvent(EventType event, Map<String, Object> value, Long datasetId,
      String error) {

    if (!EventType.RELEASE_COMPLETED_EVENT.equals(event)) {
      try {
        NotificationVO notificationVO = NotificationVO.builder()
            .user(SecurityContextHolder.getContext().getAuthentication().getName())
            .datasetId(datasetId).error(error).build();
        DataSetMetabaseVO datasetMetabaseVO =
            dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
        notificationVO.setDatasetName(datasetMetabaseVO.getDataSetName());
        notificationVO.setDataflowId(datasetMetabaseVO.getDataflowId());
        notificationVO.setDataflowName(
            dataflowControllerZuul.getMetabaseById(datasetMetabaseVO.getDataflowId()).getName());
        kafkaSenderUtils.releaseNotificableKafkaEvent(event, value, notificationVO);
      } catch (EEAException ex) {
        LOG.error("Error realeasing event {} due to error {}", event, ex.getMessage(), ex);
      }
    }
  }

  /**
   * Creates the connection data VO.
   *
   * @param datasetName the dataset name
   * @return the connection data VO
   */
  private ConnectionDataVO createConnectionDataVO(final String datasetName) {
    final ConnectionDataVO result = new ConnectionDataVO();
    result.setConnectionString(connStringPostgre);
    result.setUser(userPostgreDb);
    result.setPassword(passPostgreDb);
    result.setSchema(datasetName);
    return result;
  }

  /**
   * Gets the all data sets name.
   *
   * @param datasetName the dataset name
   * @return the all data sets name
   */
  private List<String> getAllDataSetsName(String datasetName) {

    return jdbcTemplate.query(sqlGetDatasetsName, new PreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, datasetName);
        ps.setString(2, datasetName);
      }
    }, new ResultSetExtractor<List<String>>() {
      @Override
      public List<String> extractData(ResultSet resultSet) throws SQLException {
        List<String> datasets = new ArrayList<>();
        while (resultSet.next()) {
          datasets.add(resultSet.getString("nspname"));
        }
        return datasets;
      }
    });
  }


  /**
   * Prints the to file.
   *
   * @param fileName the file name
   * @param query the query
   * @param copyManager the copy manager
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void printToFile(String fileName, String query, CopyManager copyManager)
      throws SQLException, IOException {
    byte[] buffer;
    CopyOut copyOut = copyManager.copyOut(query);

    try (OutputStream to = new FileOutputStream(fileName)) {
      while ((buffer = copyOut.readFromCopy()) != null) {
        to.write(buffer);
      }
    } finally {
      if (copyOut.isActive()) {
        copyOut.cancelCopy();
      }
    }
  }


  /**
   * Copy from file.
   *
   * @param query the query
   * @param fileName the file name
   * @param copyManager the copy manager
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  private void copyFromFile(String query, String fileName, CopyManager copyManager)
      throws IOException, SQLException {
    Path path = Paths.get(fileName);
    // bufferFile it's a size in bytes defined in consul variable. It can be 65536
    char[] cbuf = new char[bufferFile];
    int len = 0;
    CopyIn cp = copyManager.copyIn(query);
    // Copy the data from the file by chunks
    try (FileReader from = new FileReader(path.toString())) {
      while ((len = from.read(cbuf)) > 0) {
        byte[] buf = new String(cbuf, 0, len).getBytes();
        cp.writeToCopy(buf, 0, buf.length);
      }
      cp.endCopy();
      if (cp.isActive()) {
        cp.cancelCopy();
      }
    }
  }



  /**
   * Creates the index materialized view.
   *
   * @param datasetId the dataset id
   * @param tableName the table name
   * @throws RecordStoreAccessException the record store access exception
   */
  private void createIndexMaterializedView(Long datasetId, String tableName)
      throws RecordStoreAccessException {
    String indexQuery = " CREATE UNIQUE INDEX " + "\"" + tableName + "_index" + "\""
        + " ON dataset_" + datasetId + "." + "\"" + tableName + "\"" + " (record_id) ";
    executeQueryViewCommands(indexQuery.toLowerCase());
  }


  /**
   * Delete all views from schema.
   *
   * @param datasetId the dataset id
   * @throws RecordStoreAccessException the record store access exception
   */
  private void deleteAllViewsFromSchema(Long datasetId) throws RecordStoreAccessException {
    String selectViews = "select table_name as view_name " + " from information_schema.views "
        + " where table_schema not in ('information_schema', 'pg_catalog') "
        + " and table_schema = 'dataset_" + datasetId + "'";

    List<String> viewList = jdbcTemplate.queryForList(selectViews, String.class);

    String dropQuery = "drop view if exists dataset_";

    for (String view : viewList) {
      executeQueryViewCommands(dropQuery + datasetId + "." + "\"" + view + "\"");
    }
    LOG.info("These views: {} have been deleted.", viewList);
  }


  /**
   * Delete all mat views from schema.
   *
   * @param datasetId the dataset id
   * @throws RecordStoreAccessException the record store access exception
   */
  private void deleteAllMatViewsFromSchema(Long datasetId) throws RecordStoreAccessException {
    String selectMatViews =
        "select matviewname from pg_matviews where schemaname like 'dataset_" + datasetId + "'";

    List<String> matViewList = jdbcTemplate.queryForList(selectMatViews, String.class);

    String dropQuery = "drop materialized view if exists dataset_";

    for (String view : matViewList) {
      executeQueryViewCommands(dropQuery + datasetId + "." + "\"" + view + "\"");
    }
    LOG.info("These views: {} have been deleted.", matViewList);
  }

  /**
   * Execute view permissions.
   *
   * @param queryViewName the query view name
   * @param datasetId the dataset id
   * @throws RecordStoreAccessException the record store access exception
   */
  private void executeViewPermissions(String queryViewName, Long datasetId)
      throws RecordStoreAccessException {
    String querySelectPermission = "GRANT SELECT ON dataset_" + datasetId + "." + "\""
        + queryViewName + "\"" + " TO " + datasetUsers;
    executeQueryViewCommands(querySelectPermission.toLowerCase());

    String queryDeletePermission = "GRANT DELETE ON dataset_" + datasetId + "." + "\""
        + queryViewName + "\"" + " TO " + userPostgreDb;
    executeQueryViewCommands(queryDeletePermission.toLowerCase());

  }

  /**
   * Execute view query.
   *
   * @param columns the columns
   * @param queryViewName the query view name
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   * @param isMaterialized the is materialized
   * @throws RecordStoreAccessException the record store access exception
   */
  private void executeViewQuery(List<FieldSchemaVO> columns, String queryViewName,
      String idTableSchema, Long datasetId, boolean isMaterialized)
      throws RecordStoreAccessException {

    List<String> stringColumns = new ArrayList<>();
    for (FieldSchemaVO column : columns) {
      stringColumns.add(column.getId());
    }
    StringBuilder stringQuery = new StringBuilder();
    if (isMaterialized) {
      stringQuery.append("CREATE MATERIALIZED VIEW if not exists dataset_" + datasetId + "." + "\""
          + queryViewName + "\"" + " as (select rv.id as record_id, ");
    } else {
      stringQuery.append("CREATE OR REPLACE VIEW dataset_" + datasetId + "." + "\"" + queryViewName
          + "\"" + " as (select rv.id as record_id, ");
    }

    Iterator<String> iterator = stringColumns.iterator();
    int i = 0;
    while (iterator.hasNext()) {
      String schemaId = iterator.next();
      // id
      stringQuery.append("(select fv.id from dataset_" + datasetId + QUERY_FILTER_BY_ID_RECORD)
          .append(schemaId).append(AS).append("\"").append(columns.get(i).getName()).append("_id")
          .append("\" ");
      stringQuery.append(COMMA);
      // value
      DataType type = DataType.TEXT;
      for (FieldSchemaVO column : columns) {
        if (column.getId().equals(schemaId)) {
          type = column.getType();
        }
      }
      switch (type) {
        case DATE:
          stringQuery
              .append("(select case when dataset_" + datasetId
                  + ".is_date( fv.value ) then CAST(fv.value as date) else null end from dataset_"
                  + datasetId + QUERY_FILTER_BY_ID_RECORD)
              .append(schemaId).append(AS).append("\"").append(columns.get(i).getName())
              .append("\" ");
          break;
        case DATETIME:
          stringQuery.append("(select case when dataset_" + datasetId
              + ".is_date( fv.value ) then CAST(fv.value as timestamp with time zone) else null end from dataset_"
              + datasetId + QUERY_FILTER_BY_ID_RECORD).append(schemaId).append(AS).append("\"")
              .append(columns.get(i).getName()).append("\" ");
          break;
        case NUMBER_DECIMAL:
        case NUMBER_INTEGER:
          stringQuery.append("(select case when dataset_" + datasetId
              + ".is_numeric( fv.value ) then CAST(fv.value as numeric) else null end from dataset_"
              + datasetId + QUERY_FILTER_BY_ID_RECORD).append(schemaId).append(AS).append("\"")
              .append(columns.get(i).getName()).append("\" ");
          break;
        case MULTIPOLYGON:
        case POINT:
        case LINESTRING:
        case MULTILINESTRING:
        case MULTIPOINT:
        case POLYGON:
        case GEOMETRYCOLLECTION:
          stringQuery
              .append("(select fv.geometry from dataset_" + datasetId + QUERY_FILTER_BY_ID_RECORD)
              .append(schemaId).append(AS).append("\"").append(columns.get(i).getName())
              .append("\" ");
          break;
        default:
          stringQuery
              .append("(select case when fv.value = '' then null else fv.value end from dataset_"
                  + datasetId + QUERY_FILTER_BY_ID_RECORD)
              .append(schemaId).append(AS).append("\"").append(columns.get(i).getName())
              .append("\" ");
          break;
      }
      if (iterator.hasNext()) {
        stringQuery.append(COMMA);
      }
      i++;
    }
    stringQuery.append(" from dataset_" + datasetId + ".record_value rv");
    stringQuery.append(" inner join dataset_" + datasetId
        + ".table_value tv on rv.id_table = tv.id where tv.id_table_schema = '" + idTableSchema
        + "')");

    executeQueryViewCommands(stringQuery.toString().toLowerCase());
  }

}
