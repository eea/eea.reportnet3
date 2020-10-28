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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DataCollectionController.DataCollectionControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * The Class JdbcRecordStoreServiceImpl.
 */
@Service("jdbcRecordStoreServiceImpl")
public class JdbcRecordStoreServiceImpl implements RecordStoreService {

  /**
   * The Constant DELETE_FROM_DATASET: {@value}.
   */
  private static final String DELETE_FROM_DATASET = "DELETE FROM dataset_";

  /**
   * The Constant COPY_DATASET: {@value}.
   */
  private static final String COPY_DATASET = "COPY dataset_";

  /**
   * The Constant SNAPSHOT_: {@value}.
   */
  private static final String SNAPSHOT_QUERY = "snapshot_";

  /**
   * The Constant COLLECTION: {@value}.
   */
  private static final String COLLECTION = "collection";

  /**
   * The Constant SCHEMA: {@value}.
   */
  private static final String SCHEMA = "schema";

  /**
   * The Constant SNAPSHOT: {@value}.
   */
  private static final String SNAPSHOT = "snapshot";

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

  /** The Constant QUERY_FILTER_BY_ID_RECORD: {@value}. */
  private static final String QUERY_FILTER_BY_ID_RECORD =
      ".field_value fv where fv.id_record=rv.id and fv.id_field_schema = '";

  /** The Constant AS: {@value}. */
  private static final String AS = "') AS ";

  /** The Constant AS: {@value}. */
  private static final String COMMA = ", ";

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

  /**
   * the dataset users.
   */

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

  /**
   * The data set snapshot controller zuul.
   */
  @Autowired
  private DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul;

  /**
   * The dataset controller zuul.
   */
  @Autowired
  private DataSetControllerZuul datasetControllerZuul;

  /**
   * The data set metabase controller zuul.
   */
  @Autowired
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  /** The dataset schema controller. */
  @Autowired
  private DatasetSchemaControllerZuul datasetSchemaController;


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
      // waiting X seconds before releasing notifications, so database is able to write the
      // creation of all datasets
      Thread.sleep(timeToWaitBeforeReleasingNotification);
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
    } catch (InterruptedException e) {
      LOG_ERROR.error("Error sleeping thread before releasing notification kafka events", e);
      Thread.currentThread().interrupt();
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
      LOG.info("DS created with the id {} and idDatasetSchema {}", idDataset, idDatasetSchema);
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
   *
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void createDataSnapshot(Long idDataset, Long idSnapshot, Long idPartitionDataset)
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

      // Copy attachment_value
      String nameFileAttachmentValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
          LiteralConstants.SNAPSHOT_FILE_ATTACHMENT_SUFFIX);
      String copyQueryAttachment =
          "COPY (SELECT at.id, at.file_name, at.content, at.field_value_id from dataset_"
              + idDataset + ".attachment_value at) to STDOUT";
      printToFile(nameFileAttachmentValue, copyQueryAttachment, cm);

      LOG.info("Snapshot {} data files created", idSnapshot);

      notificationCreateAndCheckRelease(idDataset, idSnapshot, type);

      // release snapshot when the user press create+release
    } catch (Exception e) {
      EventType eventType = null;
      switch (type) {
        case SNAPSHOT:
          eventType = EventType.ADD_DATASET_SNAPSHOT_FAILED_EVENT;
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
        removeLockByCriteria(LockSignature.CREATE_SNAPSHOT.getValue(), idDataset,
            snapshot.getRelease());
      }
      if (SCHEMA.equals(type)) {
        removeLockByCriteria(LockSignature.CREATE_SCHEMA_SNAPSHOT.getValue(), idDataset);
      }
    }
  }

  /**
   * Removes the lock by criteria.
   *
   * @param arguments the arguments
   */
  private void removeLockByCriteria(Object... arguments) {
    List<Object> criteria = new ArrayList<>();
    for (Object object : Arrays.asList(arguments)) {
      criteria.add(object);
    }
    lockService.removeLockByCriteria(criteria);
  }

  /**
   * Removes the locks related to populate EU.
   *
   * @param dataflowId the dataflow id
   */
  private void removeLocksRelatedToPopulateEU(Long dataflowId) {
    List<ReportingDatasetVO> reportings =
        dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(dataflowId);
    removeLockByCriteria(LockSignature.POPULATE_EU_DATASET.getValue(), dataflowId);

    for (ReportingDatasetVO reporting : reportings) {
      removeLockByCriteria(LockSignature.RELEASE_SNAPSHOT.getValue(), reporting.getId());
    }
  }

  /**
   * Check type.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   *
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
   * Notification and release.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @param type the type
   *
   * @return the event type
   */
  private void notificationCreateAndCheckRelease(Long idDataset, Long idSnapshot, String type) {
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, idDataset);

    switch (type) {
      case SNAPSHOT:
        SnapshotVO snapshot = dataSetSnapshotControllerZuul.getById(idSnapshot);
        if (Boolean.TRUE.equals(snapshot.getRelease())) {
          dataSetSnapshotControllerZuul.releaseSnapshot(idDataset, idSnapshot);
        }
        releaseNotificableKafkaEvent(EventType.ADD_DATASET_SNAPSHOT_COMPLETED_EVENT, value,
            idDataset, null);
        break;
      case COLLECTION:
        Map<String, Object> valueEU = new HashMap<>();
        valueEU.put("user", ThreadPropertiesManager.getVariable("user"));
        valueEU.put("dataset_id", idDataset);
        valueEU.put("snapshot_id", idSnapshot);
        kafkaSenderUtils.releaseKafkaEvent(EventType.ADD_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT,
            valueEU);
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
  public void restoreDataSnapshot(Long idReportingDataset, Long idSnapshot, Long partitionId,
      DatasetTypeEnum datasetType, Boolean isSchemaSnapshot, Boolean deleteData)
      throws SQLException, IOException {

    EventType successEventType = Boolean.TRUE.equals(deleteData)
        ? Boolean.TRUE.equals(isSchemaSnapshot)
            ? EventType.RESTORE_DATASET_SCHEMA_SNAPSHOT_COMPLETED_EVENT
            : EventType.RESTORE_DATASET_SNAPSHOT_COMPLETED_EVENT
        : EventType.RELEASE_SNAPSHOT_COMPLETED_EVENT;
    EventType failEventType = Boolean.TRUE.equals(deleteData)
        ? Boolean.TRUE.equals(isSchemaSnapshot)
            ? EventType.RESTORE_DATASET_SCHEMA_SNAPSHOT_FAILED_EVENT
            : EventType.RESTORE_DATASET_SNAPSHOT_FAILED_EVENT
        : EventType.RELEASE_SNAPSHOT_FAILED_EVENT;

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
        SNAPSHOT_QUERY + idSnapshot + LiteralConstants.SNAPSHOT_FILE_DATASET_SUFFIX;
    String nameFileTableValue =
        SNAPSHOT_QUERY + idSnapshot + LiteralConstants.SNAPSHOT_FILE_TABLE_SUFFIX;
    String nameFileRecordValue =
        SNAPSHOT_QUERY + idSnapshot + LiteralConstants.SNAPSHOT_FILE_RECORD_SUFFIX;
    String nameFileFieldValue =
        SNAPSHOT_QUERY + idSnapshot + LiteralConstants.SNAPSHOT_FILE_FIELD_SUFFIX;

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
    Long datasetIdFromSnapshot = Boolean.TRUE.equals(isSchemaSnapshot)
        ? dataSetSnapshotControllerZuul.getSchemaById(idSnapshot).getDatasetId()
        : dataSetSnapshotControllerZuul.getById(idSnapshot).getDatasetId();

    try (
        Connection con = DriverManager.getConnection(conexion.getConnectionString(),
            conexion.getUser(), conexion.getPassword());
        Statement stmt = con.createStatement()) {
      con.setAutoCommit(true);

      if (Boolean.TRUE.equals(deleteData)) {
        String sql = composeDeleteSql(idReportingDataset, partitionId, datasetType);
        LOG.info("Deleting previous data");
        stmt.executeUpdate(sql);
      }

      CopyManager cm = new CopyManager((BaseConnection) con);
      LOG.info("Init restoring the snapshot files from Snapshot {}", idSnapshot);
      if (DatasetTypeEnum.DESIGN.equals(datasetType)) {
        // If it is a design dataset (schema), we need to restore the table values. Otherwise it's
        // not neccesary
        String nameFileTableValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
            LiteralConstants.SNAPSHOT_FILE_TABLE_SUFFIX);

        String copyQueryTable = COPY_DATASET + idReportingDataset
            + ".table_value(id, id_table_schema, dataset_id) FROM STDIN";
        copyFromFile(copyQueryTable, nameFileTableValue, cm);
      }
      // Record value
      String nameFileRecordValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
          LiteralConstants.SNAPSHOT_FILE_RECORD_SUFFIX);

      String copyQueryRecord = COPY_DATASET + idReportingDataset
          + ".record_value(id, id_record_schema, id_table, dataset_partition_id, data_provider_code) FROM STDIN";
      copyFromFile(copyQueryRecord, nameFileRecordValue, cm);

      // Field value
      String nameFileFieldValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
          LiteralConstants.SNAPSHOT_FILE_FIELD_SUFFIX);

      String copyQueryField = COPY_DATASET + idReportingDataset
          + ".field_value(id, type, value, id_field_schema, id_record) FROM STDIN";
      copyFromFile(copyQueryField, nameFileFieldValue, cm);

      // Attachment value
      String nameFileAttachmentValue = pathSnapshot + String.format(FILE_PATTERN_NAME, idSnapshot,
          LiteralConstants.SNAPSHOT_FILE_ATTACHMENT_SUFFIX);

      String copyQueryAttachment = COPY_DATASET + idReportingDataset
          + ".attachment_value(id, file_name, content, field_value_id) FROM STDIN";
      copyFromFile(copyQueryAttachment, nameFileAttachmentValue, cm);

      if (Boolean.TRUE.equals(launchEvent) && !DatasetTypeEnum.EUDATASET.equals(datasetType)) {
        // Send kafka event to launch Validation
        kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION,
            idReportingDataset);
        releaseNotificableKafkaEvent(successEventType, value, idReportingDataset, null);
      }
      if (DatasetTypeEnum.EUDATASET.equals(datasetType)) {
        dataSetSnapshotControllerZuul.deleteSnapshot(datasetIdFromSnapshot, idSnapshot);
        dataSetSnapshotControllerZuul.updateSnapshotEURelease(datasetIdFromSnapshot);
        Map<String, Object> valueEU = new HashMap<>();
        valueEU.put("user", ThreadPropertiesManager.getVariable("user"));
        valueEU.put(LiteralConstants.DATASET_ID, idReportingDataset);
        valueEU.put("snapshot_id", idSnapshot);
        kafkaSenderUtils
            .releaseKafkaEvent(EventType.RESTORE_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT, valueEU);
      }
      LOG.info("Snapshot {} restored", idSnapshot);
    } catch (Exception e) {
      if (DatasetTypeEnum.EUDATASET.equals(datasetType)) {
        failEventType = EventType.COPY_DATA_TO_EUDATASET_FAILED_EVENT;
        removeLocksRelatedToPopulateEU(dataSetMetabaseControllerZuul
            .findDatasetMetabaseById(idReportingDataset).getDataflowId());
      }
      LOG_ERROR.error("Error restoring the snapshot data due to error {}.", e.getMessage(), e);
      if (Boolean.TRUE.equals(launchEvent)) {
        releaseNotificableKafkaEvent(failEventType, value, idReportingDataset,
            "Error restoring the snapshot data");
      }
    } finally {
      // Release the lock manually
      removeLockByCriteria(signature, datasetIdFromSnapshot);
    }

  }

  /**
   * Compose delete sql.
   *
   * @param idReportingDataset the id reporting dataset
   * @param partitionId the partition id
   * @param datasetType the dataset type
   *
   * @return the string
   */
  private String composeDeleteSql(Long idReportingDataset, Long partitionId,
      DatasetTypeEnum datasetType) {
    String sql = "";
    switch (datasetType) {
      case EUDATASET:
        sql = DELETE_FROM_DATASET + idReportingDataset + ".record_value";
        break;
      case REPORTING:
        sql = DELETE_FROM_DATASET + idReportingDataset + ".record_value WHERE dataset_partition_id="
            + partitionId;
        break;
      case DESIGN:
        sql = DELETE_FROM_DATASET + idReportingDataset + ".table_value";
        break;
      default:
        break;
    }
    return sql;
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
    removeLockByCriteria(methodSignature, dataflowId);

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

      createUpdateQueryView(entry.getKey());


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
    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(event, value,
          NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
              .datasetId(datasetId).error(error).build());
    } catch (EEAException ex) {
      LOG.error("Error realeasing event {} due to error {}", event, ex.getMessage(), ex);
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
      public List<String> extractData(ResultSet resultSet) throws SQLException {
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
    CopyIn cp = copyManager.copyIn(query);
    // Copy the data from the file by chunks
    try (FileReader from = new FileReader(path.toString())) {
      while ((len = from.read(cbuf)) > 0) {
        byte[] buf = new String(cbuf, 0, len).getBytes();
        cp.writeToCopy(buf, 0, buf.length);
      }
    } catch (PSQLException e) {
      LOG_ERROR.error(
          "Error restoring the file {} executing query {}. Restoring snapshot continues", fileName,
          query, e);
    } finally {
      cp.endCopy();
      if (cp.isActive()) {
        cp.cancelCopy();
      }
    }
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
   */
  @Override
  public void createUpdateQueryView(Long datasetId) {

    DataSetSchemaVO datasetSchema = datasetSchemaController.findDataSchemaByDatasetId(datasetId);
    // delete all views because some names can be changed
    try {
      deleteAllViewsFromSchema(datasetId);
    } catch (RecordStoreAccessException e1) {
      LOG_ERROR.error("Error deleting Query view: {}", e1.getMessage(), e1);
    }

    datasetSchema.getTableSchemas().stream()
        .filter(table -> !CollectionUtils.isEmpty(table.getRecordSchema().getFieldSchema()))
        .forEach(table -> {
          List<FieldSchemaVO> columns = table.getRecordSchema().getFieldSchema();
          try {
            // create materialiced view of all tableSchemas
            executeViewQuery(columns, table.getNameTableSchema(), table.getIdTableSchema(),
                datasetId);
            // execute view permission
            executeViewPermissions(table.getNameTableSchema(), datasetId);
          } catch (RecordStoreAccessException e) {
            LOG_ERROR.error("Error creating Query view: {}", e.getMessage(), e);
          }
        });
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
    executeQueryViewCommands(querySelectPermission);

    String queryDeletePermission = "GRANT DELETE ON dataset_" + datasetId + "." + "\""
        + queryViewName + "\"" + " TO " + userPostgreDb;
    executeQueryViewCommands(queryDeletePermission);

  }

  /**
   * Query view query.
   *
   * @param columns the columns
   * @param queryViewName the query view name
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   * @throws RecordStoreAccessException the record store access exception
   */
  private void executeViewQuery(List<FieldSchemaVO> columns, String queryViewName,
      String idTableSchema, Long datasetId) throws RecordStoreAccessException {

    List<String> stringColumns = new ArrayList<>();
    for (FieldSchemaVO column : columns) {
      stringColumns.add(column.getId());
    }

    StringBuilder stringQuery = new StringBuilder("CREATE OR REPLACE VIEW dataset_" + datasetId
        + "." + "\"" + queryViewName + "\"" + " as (select rv.id as record_id, ");
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
        case NUMBER_DECIMAL:
        case NUMBER_INTEGER:
          stringQuery.append("(select case when dataset_" + datasetId
              + ".is_numeric( fv.value ) then CAST(fv.value as numeric) else null end from dataset_"
              + datasetId + QUERY_FILTER_BY_ID_RECORD).append(schemaId).append(AS).append("\"")
              .append(columns.get(i).getName()).append("\" ");
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

    executeQueryViewCommands(stringQuery.toString());
  }

}
