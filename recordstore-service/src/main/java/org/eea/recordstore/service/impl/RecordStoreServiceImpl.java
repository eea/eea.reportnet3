package org.eea.recordstore.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.DockerInterfaceService;
import org.eea.recordstore.service.RecordStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import com.github.dockerjava.api.model.Container;

/**
 * The Class RecordStoreServiceImpl.
 */
public class RecordStoreServiceImpl implements RecordStoreService {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RecordStoreServiceImpl.class);

  /**
   * The Constant DATASET_NAME_PATTERN.
   */
  private static final Pattern DATASET_NAME_PATTERN = Pattern.compile("((?)dataset_[0-9]+)");

  /** The Constant OPERATION_NOT_IMPLEMENTED_YET. */
  private static final String OPERATION_NOT_IMPLEMENTED_YET = "Operation not implemented yet";


  /** The Constant ERROR_EXECUTING_DOCKER_COMMAND. */
  private static final String ERROR_EXECUTING_DOCKER_COMMAND =
      "Error executing docker command to create the dataset. %s";

  /** The Constant ERROR_EXECUTING_DOCKER_COMMAND_LOG. */
  private static final String ERROR_EXECUTING_DOCKER_COMMAND_LOG =
      "Error executing docker command to create the dataset. {}";

  /** The Constant QUERY_FILTER_BY_ID_RECORD: {@value}. */
  private static final String QUERY_FILTER_BY_ID_RECORD =
      ".field_value fv where fv.id_record=rv.id and fv.id_field_schema = '";


  /** The Constant AS: {@value}. */
  private static final String AS = "') AS ";


  /** The Constant AS: {@value}. */
  private static final String COMMA = ", ";



  /**
   * The docker interface service.
   */
  @Autowired
  private DockerInterfaceService dockerInterfaceService;


  /** The dataset schema controller. */
  @Autowired
  private DatasetSchemaControllerZuul datasetSchemaController;

  /**
   * The container name.
   */
  @Value("${dockerContainerName}")
  private String containerName;

  /**
   * The ip postgre db.
   */
  @Value("${ipPostgre}")
  private String ipPostgreDb;

  /**
   * The user postgre db.
   */
  @Value("${userPostgre}")
  private String userPostgreDb;

  /**
   * The pass postgre db.
   */
  @Value("${passwordPostgre}")
  private String passPostgreDb;

  /**
   * The conn string postgre.
   */
  @Value("${connStringPostgree}")
  private String connStringPostgre;

  /**
   * The sql get datasets name.
   */
  @Value("${sqlGetAllDatasetsName}")
  private String sqlGetDatasetsName;

  /**
   * The path snapshot.
   */
  @Value("${pathSnapshot}")
  private String pathSnapshot;

  /**
   * The kafka sender.
   */
  @Autowired
  private KafkaSender kafkaSender;

  /**
   * Reset dataset database.
   *
   * @throws RecordStoreAccessException the docker access exception
   */
  @Override
  public void resetDatasetDatabase() throws RecordStoreAccessException {
    // TODO REMOVE THIS PART, THIS IS ONLY FOR TESTING PURPOSES
    final Container oldContainer = dockerInterfaceService.getContainer("crunchy-postgres");
    if (null != oldContainer) {
      dockerInterfaceService.stopAndRemoveContainer(oldContainer);
    }
    // TODO END REMOVE

    final Container container = dockerInterfaceService.createContainer(containerName,
        "crunchydata/crunchy-postgres-gis:centos7-11.2-2.3.1", "5432:5432");

    dockerInterfaceService.startContainer(container, 10l, TimeUnit.SECONDS);
    // create init file in container

    final File fileInitSql =
        new File(getClass().getClassLoader().getResource("init.sql").getFile());

    dockerInterfaceService.copyFileFromHostToContainer(containerName, fileInitSql.getPath(),
        "/pgwal");

    try {
      dockerInterfaceService.executeCommandInsideContainer(container, "/bin/bash", "-c", "psql -h "
          + ipPostgreDb + " -U " + userPostgreDb + " -p 5432 -d datasets -f /pgwal/init.sql ");
    } catch (final InterruptedException e) {
      LOG_ERROR.error(ERROR_EXECUTING_DOCKER_COMMAND_LOG, e.getMessage());
      throw new RecordStoreAccessException(
          String.format(ERROR_EXECUTING_DOCKER_COMMAND, e.getMessage()), e);
    }
  }

  /**
   * Creates the empty data set.
   *
   * @param datasetName the dataset name
   * @param idDatasetSchema the id dataset schema
   *
   * @throws RecordStoreAccessException the record store access exception
   */
  @Override
  public void createEmptyDataSet(final String datasetName, final String idDatasetSchema)
      throws RecordStoreAccessException {
    final Container container = dockerInterfaceService.getContainer(containerName);

    final ClassLoader classLoader = this.getClass().getClassLoader();
    final File fileInitCommands =
        new File(classLoader.getResource("datasetInitCommands.txt").getFile());

    final List<String> commands = new ArrayList<>();
    // read file into stream, try-with-resources
    try (Stream<String> stream = Files.lines(fileInitCommands.toPath())) {

      stream.forEach(commands::add);

    } catch (final IOException e) {
      LOG_ERROR.error("Error reading commands file to create the dataset. {}", e.getMessage());
      throw new RecordStoreAccessException(
          String.format("Error reading commands file to create the dataset. %s", e.getMessage()),
          e);
    }
    for (String command : commands) {
      command = command.replace("%dataset_name%", datasetName);
      try {
        dockerInterfaceService.executeCommandInsideContainer(container, "psql", "-h", ipPostgreDb,
            "-U", userPostgreDb, "-p", "5432", "-d", "datasets", "-c", command);
      } catch (final InterruptedException e) {
        LOG_ERROR.error(ERROR_EXECUTING_DOCKER_COMMAND_LOG, e.getMessage());
        throw new RecordStoreAccessException(
            String.format(ERROR_EXECUTING_DOCKER_COMMAND, e.getMessage()), e);
      }
    }

    LOG.info("Empty dataset created");
    final EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.CONNECTION_CREATED_EVENT);
    final Map<String, Object> data = new HashMap<>();
    data.put("connectionDataVO", createConnectionDataVO(datasetName));
    data.put("dataset_id", datasetName);
    data.put("idDatasetSchema", idDatasetSchema);
    event.setData(data);
    kafkaSender.sendMessage(event);
    LOG.info("Dataset with name {} created", datasetName);
  }

  /**
   * Creates the data set from other.
   *
   * @param sourceDatasetName the source dataset name
   * @param destinationDataSetName the destination data set name
   */
  @Override
  public void createDataSetFromOther(final String sourceDatasetName,
      final String destinationDataSetName) {
    throw new java.lang.UnsupportedOperationException(OPERATION_NOT_IMPLEMENTED_YET);
  }

  /**
   * Gets the connection data for dataset.
   *
   * @param datasetName the dataset name
   *
   * @return the connection data for dataset
   *
   * @throws RecordStoreAccessException the docker access exception
   */
  @Override
  public ConnectionDataVO getConnectionDataForDataset(final String datasetName)
      throws RecordStoreAccessException {
    final List<String> datasets = getAllDataSetsName();
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
   *
   * @throws RecordStoreAccessException the docker access exception
   */
  @Override
  public List<ConnectionDataVO> getConnectionDataForDataset() throws RecordStoreAccessException {
    final List<String> datasets = getAllDataSetsName();
    final List<ConnectionDataVO> result = new ArrayList<>();
    for (final String dataset : datasets) {
      final ConnectionDataVO connection = createConnectionDataVO(dataset);
      result.add(connection);
    }
    return result;
  }

  /**
   * Gets the all data sets name.
   *
   * @return the all data sets name
   *
   * @throws RecordStoreAccessException the docker access exception
   */
  private List<String> getAllDataSetsName() throws RecordStoreAccessException {
    final List<String> datasets = new ArrayList<>();
    final Container container = dockerInterfaceService.getContainer(containerName);

    try {
      final byte[] result =
          dockerInterfaceService.executeCommandInsideContainer(container, "psql", "-h", ipPostgreDb,
              "-U", userPostgreDb, "-p", "5432", "-d", "datasets", "-c", sqlGetDatasetsName);

      if (null != result && result.length > 0) {
        final Matcher dataMatcher = DATASET_NAME_PATTERN.matcher(new String(result));
        while (dataMatcher.find()) {
          datasets.add(dataMatcher.group(0));

        }
      }
    } catch (final InterruptedException e) {
      LOG_ERROR.error(ERROR_EXECUTING_DOCKER_COMMAND_LOG, e.getMessage());
      throw new RecordStoreAccessException(
          String.format(ERROR_EXECUTING_DOCKER_COMMAND, e.getMessage()), e);
    }
    return datasets;
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
   * Creates the data snapshot.
   *
   * @param idReportingDataset the id reporting dataset
   * @param idSnapshot the id snapshot
   * @param idPartitionDataset the id partition dataset
   *
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws RecordStoreAccessException the record store access exception
   */
  @Override
  public void createDataSnapshot(Long idReportingDataset, Long idSnapshot, Long idPartitionDataset)
      throws SQLException, IOException, RecordStoreAccessException {
    throw new java.lang.UnsupportedOperationException(OPERATION_NOT_IMPLEMENTED_YET);
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
   * @throws RecordStoreAccessException the record store access exception
   */
  @Override
  public void restoreDataSnapshot(Long idReportingDataset, Long idSnapshot, Long partitionId,
      DatasetTypeEnum datasetType, Boolean isSchemaSnapshot, Boolean deleteData)
      throws SQLException, IOException, RecordStoreAccessException {
    throw new java.lang.UnsupportedOperationException(OPERATION_NOT_IMPLEMENTED_YET);
  }

  /**
   * Restore data snapshot poc.
   *
   * @param idReportingDataset the id reporting dataset
   * @param idSnapshot the id snapshot
   * @param partitionId the partition id
   * @param datasetType the dataset type
   * @param isSchemaSnapshot the is schema snapshot
   * @param deleteData the delete data
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
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
    throw new java.lang.UnsupportedOperationException(OPERATION_NOT_IMPLEMENTED_YET);
  }

  /**
   * Delete dataset.
   *
   * @param datasetSchemaName the dataset schema name
   */
  @Override
  public void deleteDataset(String datasetSchemaName) {
    throw new java.lang.UnsupportedOperationException(OPERATION_NOT_IMPLEMENTED_YET);
  }

  /**
   * Creates the schemas.
   *
   * @param data the data
   * @param dataflowId the dataflow id
   * @param isCreation the is creation
   */
  @Override
  public void createSchemas(Map<Long, String> data, Long dataflowId, boolean isCreation) {
    throw new java.lang.UnsupportedOperationException(OPERATION_NOT_IMPLEMENTED_YET);
  }


  /**
   * Execute query view commands.
   *
   * @param command the command
   * @throws RecordStoreAccessException the record store access exception
   */
  @Override
  public void executeQueryViewCommands(final String command) throws RecordStoreAccessException {
    final Container container = dockerInterfaceService.getContainer(containerName);
    try {
      dockerInterfaceService.executeCommandInsideContainer(container, "psql", "-h", ipPostgreDb,
          "-U", userPostgreDb, "-p", "5432", "-d", "datasets", "-c", command);
    } catch (final InterruptedException e) {
      LOG_ERROR.error(ERROR_EXECUTING_DOCKER_COMMAND_LOG, e.getMessage());
      throw new RecordStoreAccessException(
          String.format(ERROR_EXECUTING_DOCKER_COMMAND, e.getMessage()), e);
    }
    LOG.info("Command on Query View executed: {}", command);
  }


  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void createUpdateQueryView(Long datasetId) {

    DataSetSchemaVO datasetSchema = datasetSchemaController.findDataSchemaByDatasetId(datasetId);

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
   * Execute view permissions.
   *
   * @param queryViewName the query view name
   * @param datasetId the dataset id
   * @throws RecordStoreAccessException the record store access exception
   */
  private void executeViewPermissions(String queryViewName, Long datasetId)
      throws RecordStoreAccessException {
    String querySelectPermission =
        "GRANT SELECT ON dataset_" + datasetId + "." + queryViewName + " TO validation";
    executeQueryViewCommands(querySelectPermission);

    String queryDeletePermission =
        "GRANT DELETE ON dataset_" + datasetId + "." + queryViewName + " TO recordstore";
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
              .append("(select CAST(fv.value as date) from dataset_" + datasetId
                  + QUERY_FILTER_BY_ID_RECORD)
              .append(schemaId).append(AS).append("\"").append(columns.get(i).getName())
              .append("\" ");
          break;
        case NUMBER_DECIMAL:
        case NUMBER_INTEGER:
          stringQuery
              .append("(select CAST(fv.value as numeric) from dataset_" + datasetId
                  + QUERY_FILTER_BY_ID_RECORD)
              .append(schemaId).append(AS).append("\"").append(columns.get(i).getName())
              .append("\" ");
          break;
        default:
          stringQuery
              .append("(select fv.value from dataset_" + datasetId + QUERY_FILTER_BY_ID_RECORD)
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
