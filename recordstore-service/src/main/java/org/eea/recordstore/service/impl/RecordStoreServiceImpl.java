package org.eea.recordstore.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.DockerInterfaceService;
import org.eea.recordstore.service.RecordStoreService;
import org.postgresql.copy.CopyManager;
import org.postgresql.copy.CopyOut;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.github.dockerjava.api.model.Container;

/**
 * The Class RecordStoreServiceImpl.
 */
// @Service
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

  /**
   * The docker interface service.
   */
  @Autowired
  private DockerInterfaceService dockerInterfaceService;

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

    /*
     * dockerInterfaceService //TODO need to determine where to find the init.sql file and where to
     * store inside the container .copyFileFromHostToContainer(CONTAINER_NAME,
     * "C:\\opt\\dump\\init.sql", "/pgwal");
     */
    // "psql -h localhost -U root -p 5432 -d datasets -f /pgwal/init.sql "
    try {
      dockerInterfaceService.executeCommandInsideContainer(container, "/bin/bash", "-c", "psql -h "
          + ipPostgreDb + " -U " + userPostgreDb + " -p 5432 -d datasets -f /pgwal/init.sql ");
    } catch (final InterruptedException e) {
      LOG_ERROR.error("Error executing docker command to create the dataset. {}", e.getMessage());
      throw new RecordStoreAccessException(
          String.format("Error executing docker command to create the dataset. %s", e.getMessage()),
          e);

    }
  }


  /**
   * Creates the empty data set.
   *
   * @param datasetName the dataset name
   * @param idDatasetSchema the id dataset schema
   * @throws RecordStoreAccessException the record store access exception
   */
  @Override
  public void createEmptyDataSet(final String datasetName, final String idDatasetSchema)
      throws RecordStoreAccessException {
    // line to run a crunchy container
    // docker run -d -e PG_DATABASE=datasets -e PG_PRIMARY_PORT=5432 -e PG_MODE=primary -e
    // PG_USER=root -e PG_PASSWORD=root -e PGPASSWORD=root -e PG_PRIMARY_USER=root -e
    // PG_PRIMARY_PASSWORD=root
    // -e PG_ROOT_PASSWORD=root -e PGBACKREST=true -p 5432:5432 --name crunchy-postgres
    // crunchydata/crunchy-postgres-gis:centos7-11.2-2.3.1
    final Container container = dockerInterfaceService.getContainer(containerName);

    final ClassLoader classLoader = this.getClass().getClassLoader();
    final File fileInitCommands =
        new File(classLoader.getResource("datasetInitCommands.txt").getFile());

    final List<String> commands = new ArrayList<>();
    // read file into stream, try-with-resources
    try (final Stream<String> stream = Files.lines(fileInitCommands.toPath())) {

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
        LOG_ERROR.error("Error executing docker command to create the dataset. {}", e.getMessage());
        throw new RecordStoreAccessException(String
            .format("Error executing docker command to create the dataset. %s", e.getMessage()), e);

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
    throw new java.lang.UnsupportedOperationException("Operation not implemented yet");


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
      LOG_ERROR.error("Error executing docker command to create the dataset. {}", e.getMessage());
      throw new RecordStoreAccessException(
          String.format("Error executing docker command to create the dataset. %s", e.getMessage()),
          e);

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


  @Override
  public void createDataSnapshot(Long idReportingDataset, Long idSnapshot, Long idPartitionDataset)
      throws SQLException, IOException, RecordStoreAccessException {

    ConnectionDataVO conexion = getConnectionDataForDataset("dataset_" + idReportingDataset);
    Connection con = null;
    con = DriverManager.getConnection(conexion.getConnectionString(), conexion.getUser(),
        conexion.getPassword());

    CopyManager cm = new CopyManager((BaseConnection) con);


    // Copy dataset_value
    String nameFileDatasetValue =
        "snapshot_" + idSnapshot + "-dataset_" + idReportingDataset + "_table_DatasetValue.snap";
    byte[] buf;
    CopyOut cpOut = cm.copyOut("COPY (SELECT id, id_dataset_schema FROM dataset_"
        + idReportingDataset + ".dataset_value) to STDOUT");
    OutputStream to = new FileOutputStream("C:/snapshots/" + nameFileDatasetValue);
    try {
      while ((buf = cpOut.readFromCopy()) != null) {
        to.write(buf);
      }
    } finally { // see to it that we do not leave the connection locked
      if (cpOut.isActive()) {
        cpOut.cancelCopy();
      }
      to.close();
    }

    // Copy table_value
    String nameFileTableValue =
        "snapshot_" + idSnapshot + "-dataset_" + idReportingDataset + "_table_TableValue.snap";
    byte[] buf2;
    CopyOut cpOut2 = cm.copyOut("COPY (SELECT id, id_table_schema, dataset_id FROM dataset_"
        + idReportingDataset + ".table_value) to STDOUT");
    OutputStream to2 = new FileOutputStream("C:/snapshots/" + nameFileTableValue);
    try {
      while ((buf2 = cpOut2.readFromCopy()) != null) {
        to2.write(buf2);
      }
    } finally { // see to it that we do not leave the connection locked
      if (cpOut2.isActive()) {
        cpOut2.cancelCopy();
      }
      to2.close();
    }

    // Copy record_value
    String nameFileRecordValue =
        "snapshot_" + idSnapshot + "-dataset_" + idReportingDataset + "_table_RecordValue.snap";
    byte[] buf3;
    CopyOut cpOut3 =
        cm.copyOut("COPY (SELECT id, id_record_schema, id_table, dataset_partition_id FROM dataset_"
            + idReportingDataset + ".record_value WHERE dataset_partition_id=" + idPartitionDataset
            + ") to STDOUT");
    OutputStream to3 = new FileOutputStream("C:/snapshots/" + nameFileRecordValue);
    try {
      while ((buf3 = cpOut3.readFromCopy()) != null) {
        to3.write(buf3);
      }
    } finally { // see to it that we do not leave the connection locked
      if (cpOut3.isActive()) {
        cpOut3.cancelCopy();
      }
      to3.close();
    }

    // Copy field_value
    String nameFileFieldValue =
        "snapshot_" + idSnapshot + "-dataset_" + idReportingDataset + "_table_FieldValue.snap";
    byte[] buf4;
    CopyOut cpOut4 = cm.copyOut(
        "COPY (SELECT fv.id, fv.type, fv.value, fv.id_field_schema, fv.id_record from dataset_"
            + idReportingDataset + ".field_value fv inner join dataset_" + idReportingDataset
            + ".record_value rv on fv.id_record = rv.id where rv.dataset_partition_id="
            + idPartitionDataset + ") to STDOUT");
    OutputStream to4 = new FileOutputStream("C:/snapshots/" + nameFileFieldValue);
    try {
      while ((buf4 = cpOut4.readFromCopy()) != null) {
        to4.write(buf4);
      }
    } finally { // see to it that we do not leave the connection locked
      if (cpOut4.isActive()) {
        cpOut4.cancelCopy();
      }
      to4.close();

    }

    con.close();

  }


  @Override
  public void restoreDataSnapshot(Long idReportingDataset, Long idSnapshot)
      throws SQLException, IOException, RecordStoreAccessException {

    ConnectionDataVO conexion = getConnectionDataForDataset("dataset_" + idReportingDataset);
    Connection con = null;
    con = DriverManager.getConnection(conexion.getConnectionString(), conexion.getUser(),
        conexion.getPassword());

    CopyManager cm = new CopyManager((BaseConnection) con);

    // Record value
    String nameFileRecordValue =
        "snapshot_" + idSnapshot + "-dataset_" + idReportingDataset + "_table_RecordValue.snap";
    Path path3 = Paths.get(pathSnapshot + nameFileRecordValue);
    InputStream is3 = Files.newInputStream(path3);
    cm.copyIn(
        "COPY dataset_" + idReportingDataset
            + ".record_value(id, id_record_schema, id_table, dataset_partition_id) FROM STDIN",
        is3);
    is3.close();

    // Field value
    String nameFileFieldValue =
        "snapshot_" + idSnapshot + "-dataset_" + idReportingDataset + "_table_FieldValue.snap";
    Path path4 = Paths.get(pathSnapshot + nameFileFieldValue);
    InputStream is4 = Files.newInputStream(path4);
    cm.copyIn("COPY dataset_" + idReportingDataset
        + ".field_value(id, type, value, id_field_schema, id_record) FROM STDIN", is4);
    is4.close();


    con.close();
  }


}
