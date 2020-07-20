package org.eea.recordstore.service.impl;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DataCollectionController.DataCollectionControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.copy.CopyOut;
import org.postgresql.core.BaseConnection;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * The Class JdbcRecordStoreServiceImpl.
 */
@Service("jdbcRecordStoreServiceImpl")
public class JdbcRecordStoreServiceImpl implements RecordStoreService {

  /**
   * The Constant FILE_PATTERN_NAME.
   */
  private static final String FILE_PATTERN_NAME = "snapshot_%s%s";

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(JdbcRecordStoreServiceImpl.class);

  /**
   * The constant GRANT_ALL_PRIVILEGES_ON_SCHEMA.
   */
  private static final String GRANT_ALL_PRIVILEGES_ON_SCHEMA =
      "grant all privileges on schema %s to %s;";
  /**
   * The constant GRANT_ALL_PRIVILEGES_ON_ALL_TABLES_ON_SCHEMA.
   */
  private static final String GRANT_ALL_PRIVILEGES_ON_ALL_TABLES_ON_SCHEMA =
      "grant all privileges on all tables in schema %s to %s;";

  /**
   * The constant GRANT_ALL_PRIVILEGES_ON_ALL_SEQUENCES_ON_SCHEMA.
   */
  private static final String GRANT_ALL_PRIVILEGES_ON_ALL_SEQUENCES_ON_SCHEMA =
      "grant all privileges on all sequences in schema %s to %s;";

  /**
   * The user postgre db.
   */
  @Value("${spring.datasource.dataset.username}")
  private String userPostgreDb;

  /**
   * The pass postgre db.
   */
  @Value("${spring.datasource.dataset.password}")
  private String passPostgreDb;

  /**
   * The conn string postgre.
   */
  @Value("${spring.datasource.url}")
  private String connStringPostgre;

  /**
   * The sql get datasets name.
   */
  @Value("${sqlGetAllDatasetsName}")
  private String sqlGetDatasetsName;

  /** the dataset users. */

  @Value("${dataset.users}")
  private String datasetUsers;


  /**
   * The resource file.
   */
  @Value("classpath:datasetInitCommands.txt")
  private Resource resourceFile;

  /**
   * The path snapshot.
   */
  @Value("${pathSnapshot}")
  private String pathSnapshot;

  /**
   * The time to wait before releasing notification.
   */
  @Value("${dataset.creation.notification.ms}")
  private Long timeToWaitBeforeReleasingNotification;


  /**
   * The buffer file.
   */
  @Value("${snapshot.bufferSize}")
  private Integer bufferFile;

  /**
   * The jdbc template.
   */
  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * The data source.
   */
  @Autowired
  private DataSource dataSource;

  /**
   * The lock service.
   */
  @Autowired
  private LockService lockService;


  /**
   * The kafka sender utils.
   */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * The data collection controller zuul.
   */
  @Autowired
  private DataCollectionControllerZuul dataCollectionControllerZuul;

  /** The data set snapshot controller zuul. */
  @Autowired
  private DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul;

  /** The dataset controller zuul. */
  @Autowired
  private DataSetControllerZuul datasetControllerZuul;

  @Autowired
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;


  /**
   * Creates a schema for each entry in the list. Also releases events to feed the new schemas.
   * <p>
   * <b>Note:</b> {@literal @}<i>Async</i> annotated method.
   * </p>
   *
   * @param datasetIdsAndSchemaIds Map matching datasetIds with datasetSchemaIds.
   * @param dataflowId The DataCollection's dataflow.
   * @param isCreation the is creation
   */
  @Override
  @Async
  public void createSchemas(Map<Long, String> datasetIdsAndSchemaIds, Long dataflowId,
      boolean isCreation) {

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
      LOG.info("{} Schemas created as part of DataCollection creation",
          datasetIdsAndSchemaIds.size());
      try {
        // waiting X seconds before releasing notifications, so database is able to write the
        // creation of all datasets
        Thread.sleep(timeToWaitBeforeReleasingNotification);
      } catch (InterruptedException e) {
        LOG_ERROR.error("Error sleeping thread before releasing notification kafka events", e);
        Thread.currentThread().interrupt();
      }

      LOG.info("Releasing notifications via Kafka");
      // Release events to initialize databases content
      releaseConnectionCreatedEvents(datasetIdsAndSchemaIds);

      // Release the lock and the notification
      releaseLockAndNotification(dataflowId, isCreation);

    } catch (SQLException | IOException e) {
      LOG_ERROR.error("Error creating schemas. Rolling back: ", e);
      // This method will release the lock
      dataCollectionControllerZuul.undoDataCollectionCreation(
          new ArrayList<>(datasetIdsAndSchemaIds.keySet()), dataflowId, isCreation);
    }
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

    // Now we insert the values into the dataset_value table of the brand new schema
    StringBuilder insertSql = new StringBuilder("INSERT INTO ");
    insertSql.append(datasetName).append(".dataset_value(id, id_dataset_schema) values (?, ?)");
    if (StringUtils.isNotBlank(datasetName) && StringUtils.isNotBlank(idDatasetSchema)) {
      String[] aux = datasetName.split("_");
      Long idDataset = Long.valueOf(aux[aux.length - 1]);
      jdbcTemplate.update(insertSql.toString(), idDataset, idDatasetSchema);
    }
  }

  /**
   * Gets the connection data for dataset.
   *
   * @param datasetName the dataset name
   *
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
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException
   */
  @Override
  @Async
  public void createDataSnapshot(Long idDataset, Long idSnapshot, Long idPartitionDataset)
      throws SQLException, IOException, EEAException {

    ConnectionDataVO connectionDataVO =
        getConnectionDataForDataset(LiteralConstants.DATASET_PREFIX + idDataset);
    String type = null;
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
      if (DatasetTypeEnum.COLLECTION.equals(typeDataset)) {
        copyQueryRecord = "COPY (SELECT id, id_record_schema, id_table, " + idPartitionDataset
            + ",data_provider_code FROM dataset_" + idDataset + ".record_value) to STDOUT";
        copyQueryField =
            "COPY (SELECT fv.id, fv.type, fv.value, fv.id_field_schema, fv.id_record from dataset_"
                + idDataset + ".field_value fv) to STDOUT";
      } else {
        copyQueryRecord =
            "COPY (SELECT id, id_record_schema, id_table, dataset_partition_id, data_provider_code FROM dataset_"
                + idDataset + ".record_value WHERE dataset_partition_id=" + idPartitionDataset
                + ") to STDOUT";
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

      LOG.info("Snapshot {} data files created", idSnapshot);

      notificationAndCheckRelease(idDataset, idSnapshot, type);

      // release snapshot when the user press create+release
    } catch (Exception e) {
      EventType event = null;
      switch (type) {
        case "dataset":
          event = EventType.ADD_DATASET_SNAPSHOT_FAILED_EVENT;
          break;
        case "collection":
          removeLocksRelatedToPopulateEU(
              dataSetMetabaseControllerZuul.findDatasetMetabaseById(idDataset).getDataflowId(),
              idDataset);
          event = EventType.COPY_DATA_TO_EUDATASET_FAILED_EVENT;
          break;
        case "schema":
          event = EventType.ADD_DATASET_SCHEMA_SNAPSHOT_FAILED_EVENT;
          break;
        default:
          break;
      }
      LOG_ERROR.error("Error creating snapshot for dataset {}", idDataset, e);
      Map<String, Object> value = new HashMap<>();
      value.put(LiteralConstants.DATASET_ID, idDataset);
      kafkaSenderUtils.releaseNotificableKafkaEvent(event, value,
          NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
              .datasetId(idDataset).error(e.getMessage()).build());

    } finally {
      // Release the lock manually
      List<Object> criteria = new ArrayList<>();
      if ("dataset".equals(type)) {
        criteria.add(LockSignature.CREATE_SNAPSHOT.getValue());
        criteria.add(idDataset);
        lockService.removeLockByCriteria(criteria);
      }
      if ("schema".equals(type)) {
        List<Object> criteria2 = new ArrayList<>();
        criteria2.add(LockSignature.CREATE_SCHEMA_SNAPSHOT.getValue());
        criteria2.add(idDataset);
        lockService.removeLockByCriteria(criteria);
      }
    }
  }

  /**
   * Removes the locks related to populate EU.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the dataset id
   */
  private void removeLocksRelatedToPopulateEU(Long dataflowId, Long datasetId) {
    List<ReportingDatasetVO> reportings =
        dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(dataflowId);
    List<Object> criteria = new ArrayList<>();
    criteria.add(LockSignature.POPULATE_EU_DATASET.getValue());
    criteria.add(dataflowId);
    lockService.removeLockByCriteria(criteria);

    for (ReportingDatasetVO reporting : reportings) {
      List<Object> criteriaReporting = new ArrayList<>();
      criteriaReporting.add(LockSignature.RELEASE_SNAPSHOT.getValue());
      criteriaReporting.add(reporting.getId());

      lockService.removeLockByCriteria(criteriaReporting);
    }
  }

  /**
   * Check type.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @return the string
   * @throws EEAException the EEA exception
   */
  private String checkType(Long idDataset, Long idSnapshot) throws EEAException {
    String type = "dataset";
    SnapshotVO snapshot = null;
    snapshot = dataSetSnapshotControllerZuul.getSchemaById(idSnapshot);
    if (snapshot != null) {
      type = "schema";
    } else {
      if (DatasetTypeEnum.COLLECTION.equals(dataSetMetabaseControllerZuul.getType(idDataset))) {
        type = "collection";
      }
    }
    return type;
  }

  /**
   * Notification and release.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @return the event type
   * @throws EEAException
   */
  private void notificationAndCheckRelease(Long idDataset, Long idSnapshot, String type)
      throws EEAException {
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, idDataset);

    switch (type) {
      case "dataset":
        SnapshotVO snapshot = dataSetSnapshotControllerZuul.getById(idSnapshot);
        if (Boolean.TRUE.equals(snapshot.getForceRelease())) {
          dataSetSnapshotControllerZuul.releaseSnapshot(idDataset, idSnapshot);
        }
        kafkaSenderUtils.releaseNotificableKafkaEvent(
            EventType.ADD_DATASET_SNAPSHOT_COMPLETED_EVENT, value,
            NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
                .datasetId(idDataset).build());
        break;
      case "collection":
        Map<String, Object> valueEU = new HashMap<>();
        valueEU.put("user", ThreadPropertiesManager.getVariable("user"));
        valueEU.put("dataset_id", idDataset);
        valueEU.put("snapshot_id", idSnapshot);
        kafkaSenderUtils.releaseKafkaEvent(EventType.ADD_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT,
            valueEU);
        break;
      case "schema":
        kafkaSenderUtils.releaseNotificableKafkaEvent(
            EventType.ADD_DATASET_SCHEMA_SNAPSHOT_COMPLETED_EVENT, value,
            NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
                .datasetId(idDataset).build());
        break;
      default:
        break;
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
   *
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Async
  public void restoreDataSnapshot(Long idReportingDataset, Long idSnapshot, Long partitionId,
      DatasetTypeEnum datasetType, Boolean isSchemaSnapshot, Boolean deleteData)
      throws SQLException, IOException {

    EventType successEventType = deleteData
        ? isSchemaSnapshot ? EventType.RESTORE_DATASET_SCHEMA_SNAPSHOT_COMPLETED_EVENT
            : EventType.RESTORE_DATASET_SNAPSHOT_COMPLETED_EVENT
        : EventType.RELEASE_DATASET_SNAPSHOT_COMPLETED_EVENT;
    EventType failEventType = deleteData
        ? isSchemaSnapshot ? EventType.RESTORE_DATASET_SCHEMA_SNAPSHOT_FAILED_EVENT
            : EventType.RESTORE_DATASET_SNAPSHOT_FAILED_EVENT
        : EventType.RELEASE_DATASET_SNAPSHOT_FAILED_EVENT;

    // Call to the private method restoreSnapshot. Method shared with public restoreDataSnapshotPoc.
    // The main difference
    // between both methods is this one releases a notification
    restoreSnapshot(idReportingDataset, idSnapshot, partitionId, datasetType, isSchemaSnapshot,
        deleteData, successEventType, failEventType, true);

  }

  /**
   * Delete data snapshot.
   *
   * @param idReportingDataset the id reporting dataset
   * @param idSnapshot the id snapshot
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public void deleteDataSnapshot(Long idReportingDataset, Long idSnapshot) throws IOException {

    String nameFileDatasetValue =
        "snapshot_" + idSnapshot + LiteralConstants.SNAPSHOT_FILE_DATASET_SUFFIX;
    String nameFileTableValue =
        "snapshot_" + idSnapshot + LiteralConstants.SNAPSHOT_FILE_TABLE_SUFFIX;
    String nameFileRecordValue =
        "snapshot_" + idSnapshot + LiteralConstants.SNAPSHOT_FILE_RECORD_SUFFIX;
    String nameFileFieldValue =
        "snapshot_" + idSnapshot + LiteralConstants.SNAPSHOT_FILE_FIELD_SUFFIX;

    Path path1 = Paths.get(pathSnapshot + nameFileDatasetValue);
    Files.deleteIfExists(path1);
    Path path2 = Paths.get(pathSnapshot + nameFileTableValue);
    Files.deleteIfExists(path2);
    Path path3 = Paths.get(pathSnapshot + nameFileRecordValue);
    Files.deleteIfExists(path3);
    Path path4 = Paths.get(pathSnapshot + nameFileFieldValue);
    Files.deleteIfExists(path4);
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
   * Reset dataset database.
   *
   * @throws RecordStoreAccessException the record store access exception
   */
  @Override
  public void resetDatasetDatabase() throws RecordStoreAccessException {
    throw new java.lang.UnsupportedOperationException("Operation not implemented yet");
  }


  /**
   * Restore snapshot.
   *
   * @param idReportingDataset the id reporting dataset
   * @param idSnapshot the id snapshot
   * @param partitionId the partition id
   * @param datasetType the dataset type
   * @param isSchemaSnapshot the is schema snapshot
   * @param deleteData the delete data
   * @param successEventType the success event type
   * @param failEventType the fail event type
   * @param launchEvent the launch event
   */
  private void restoreSnapshot(Long idReportingDataset, Long idSnapshot, Long partitionId,
      DatasetTypeEnum datasetType, Boolean isSchemaSnapshot, Boolean deleteData,
      EventType successEventType, EventType failEventType, Boolean launchEvent) {

    String signature = Boolean.TRUE.equals(deleteData)
        ? Boolean.TRUE.equals(isSchemaSnapshot) ? LockSignature.RESTORE_SCHEMA_SNAPSHOT.getValue()
            : LockSignature.RESTORE_SNAPSHOT.getValue()
        : LockSignature.RELEASE_SNAPSHOT.getValue();
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, idReportingDataset);
    ConnectionDataVO conexion =
        getConnectionDataForDataset(LiteralConstants.DATASET_PREFIX + idReportingDataset);
    // We get the datasetId from the snapshot
    SnapshotVO snapshot = dataSetSnapshotControllerZuul.getById(idSnapshot);
    if (snapshot == null) {
      snapshot = dataSetSnapshotControllerZuul.getSchemaById(idSnapshot);
    }
    Long datasetIdFromSnapshot = snapshot.getDatasetId();

    try (
        Connection con = DriverManager.getConnection(conexion.getConnectionString(),
            conexion.getUser(), conexion.getPassword());
        Statement stmt = con.createStatement()) {
      con.setAutoCommit(true);

      if (Boolean.TRUE.equals(deleteData)) {
        String sql = "";
        switch (datasetType) {
          case EUDATASET:
            sql = "DELETE FROM dataset_" + idReportingDataset + ".record_value";
            break;
          case REPORTING:
            sql = "DELETE FROM dataset_" + idReportingDataset
                + ".record_value WHERE dataset_partition_id=" + partitionId;
            break;
          case DESIGN:
            sql = "DELETE FROM dataset_" + idReportingDataset + ".table_value";
            break;
          default:
            break;
        }
        LOG.info("Deleting previous data");
        stmt.executeUpdate(sql);
      }

      CopyManager cm = new CopyManager((BaseConnection) con);
      LOG.info("Init restoring the snapshot files from Snapshot {}", idSnapshot);
      switch (datasetType) {
        case DESIGN:
          // If it is a design dataset (schema), we need to restore the table values. Otherwise it's
          // not neccesary
          String nameFileTableValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
              LiteralConstants.SNAPSHOT_FILE_TABLE_SUFFIX);

          String copyQueryTable = "COPY dataset_" + idReportingDataset
              + ".table_value(id, id_table_schema, dataset_id) FROM STDIN";
          copyFromFile(copyQueryTable, nameFileTableValue, cm);
          break;
        default:
          break;
      }
      // Record value
      String nameFileRecordValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
          LiteralConstants.SNAPSHOT_FILE_RECORD_SUFFIX);

      String copyQueryRecord = "COPY dataset_" + idReportingDataset
          + ".record_value(id, id_record_schema, id_table, dataset_partition_id, data_provider_code) FROM STDIN";
      copyFromFile(copyQueryRecord, nameFileRecordValue, cm);

      // Field value
      String nameFileFieldValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
          LiteralConstants.SNAPSHOT_FILE_FIELD_SUFFIX);

      String copyQueryField = "COPY dataset_" + idReportingDataset
          + ".field_value(id, type, value, id_field_schema, id_record) FROM STDIN";
      copyFromFile(copyQueryField, nameFileFieldValue, cm);

      if (Boolean.TRUE.equals(launchEvent)) {
        // Send kafka event to launch Validation
        final EEAEventVO event = new EEAEventVO();
        event.setEventType(successEventType);
        if (!DatasetTypeEnum.EUDATASET.equals(datasetType)) {
          kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION,
              idReportingDataset);
        } else {
          dataSetSnapshotControllerZuul.deleteSnapshot(datasetIdFromSnapshot, idSnapshot);
        }
        if (DatasetTypeEnum.EUDATASET.equals(datasetType)) {
          Map<String, Object> valueEU = new HashMap<>();
          valueEU.put("user", ThreadPropertiesManager.getVariable("user"));
          valueEU.put("dataset_id", idReportingDataset);
          valueEU.put("snapshot_id", idSnapshot);
          kafkaSenderUtils.releaseKafkaEvent(
              EventType.RESTORE_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT, valueEU);
        } else {
          kafkaSenderUtils.releaseNotificableKafkaEvent(successEventType, value,
              NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
                  .datasetId(idReportingDataset).build());
        }
      }
      LOG.info("Snapshot {} restored", idSnapshot);
    } catch (EEAException e) {
      LOG.error("Error realeasing event {}: ", successEventType, e);
    } catch (Exception e) {
      if (DatasetTypeEnum.EUDATASET.equals(datasetType)) {
        value.put(LiteralConstants.DATASET_ID, idReportingDataset);
        failEventType = EventType.COPY_DATA_TO_EUDATASET_FAILED_EVENT;
        removeLocksRelatedToPopulateEU(dataSetMetabaseControllerZuul
            .findDatasetMetabaseById(idReportingDataset).getDataflowId(), idReportingDataset);
      }
      LOG_ERROR.error("Error restoring the snapshot data due to error {}.", e.getMessage(), e);
      if (Boolean.TRUE.equals(launchEvent)) {
        try {
          kafkaSenderUtils.releaseNotificableKafkaEvent(failEventType, value,
              NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
                  .datasetId(idReportingDataset).error("Error restoring the snapshot data")
                  .build());
        } catch (EEAException ex) {
          LOG.error("Error realeasing event {} due to error {}", failEventType, ex.getMessage(),
              ex);
        }
      }
    } finally {
      // Release the lock manually
      List<Object> criteria = new ArrayList<>();
      criteria.add(signature);
      criteria.add(datasetIdFromSnapshot);

      lockService.removeLockByCriteria(criteria);
    }

  }


  /**
   * Release lock and notification.
   *
   * @param dataflowId the dataflow id
   * @param isCreation the is creation
   */
  private void releaseLockAndNotification(Long dataflowId, boolean isCreation) {

    String methodSignature = isCreation ? LockSignature.CREATE_DATA_COLLECTION.getValue()
        : LockSignature.UPDATE_DATA_COLLECTION.getValue();
    EventType successEvent = isCreation ? EventType.ADD_DATACOLLECTION_COMPLETED_EVENT
        : EventType.UPDATE_DATACOLLECTION_COMPLETED_EVENT;

    // Release the lock
    List<Object> criteria = new ArrayList<>();
    criteria.add(methodSignature);
    criteria.add(dataflowId);
    lockService.removeLockByCriteria(criteria);

    // Release the notification
    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(successEvent, null,
          NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
              .dataflowId(dataflowId).build());
    } catch (EEAException e) {
      LOG_ERROR.error("Error releasing {} event: ", successEvent, e);
    }
  }

  /**
   * Releases CONNECTION_CREATED_EVENT Kafka events to initialize databases content. Before that,
   * insert the values into the schema of the dataset_value and table_value
   *
   * @param datasetIdAndSchemaId dataset ids matching schema ids
   */
  private void releaseConnectionCreatedEvents(Map<Long, String> datasetIdAndSchemaId) {
    for (Map.Entry<Long, String> entry : datasetIdAndSchemaId.entrySet()) {
      Map<String, Object> result = new HashMap<>();
      String datasetName = "dataset_" + entry.getKey();
      result.put("connectionDataVO", createConnectionDataVO(datasetName));
      result.put(LiteralConstants.DATASET_ID, datasetName);
      result.put(LiteralConstants.ID_DATASET_SCHEMA, entry.getValue());

      kafkaSenderUtils.releaseKafkaEvent(EventType.CONNECTION_CREATED_EVENT, result);
    }
  }


  /**
   * Creates the connection data VO.
   *
   * @param datasetName the dataset name
   *
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
   *
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
      public List<String> extractData(ResultSet resultSet)
          throws SQLException, DataAccessException {
        List<String> datasets = new ArrayList<>();
        while (resultSet.next()) {
          datasets.add(resultSet.getString(1));
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
   *
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
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  private void copyFromFile(String query, String fileName, CopyManager copyManager)
      throws IOException, SQLException {
    Path path = Paths.get(fileName);
    // bufferFile it's a size in bytes defined in consul variable. It can be 65536
    char[] cbuf = new char[bufferFile];
    int len = 0;
    FileReader from = null;
    CopyIn cp = copyManager.copyIn(query);
    // Copy the data from the file by chunks
    try {
      from = new FileReader(path.toString());
      while ((len = from.read(cbuf)) > 0) {
        byte[] buf = new String(cbuf, 0, len).getBytes();
        cp.writeToCopy(buf, 0, buf.length);
      }
    } catch (PSQLException e) {
      LOG_ERROR.error(
          "Error restoring the file {} executing query {}. Restoring snapshot continues", fileName,
          query, e);
    } finally {
      if (null != from) {
        from.close();
      }
      cp.endCopy();
      if (cp.isActive()) {
        cp.cancelCopy();
      }
    }
  }

}
