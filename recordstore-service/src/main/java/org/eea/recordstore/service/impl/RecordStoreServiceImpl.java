package org.eea.recordstore.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
import org.eea.recordstore.exception.DockerAccessException;
import org.eea.recordstore.service.DockerInterfaceService;
import org.eea.recordstore.service.RecordStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.github.dockerjava.api.model.Container;

/**
 * The Class RecordStoreServiceImpl.
 */
@Service
public class RecordStoreServiceImpl implements RecordStoreService {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant DATASET_NAME_PATTERN. */
  private static final Pattern DATASET_NAME_PATTERN = Pattern.compile("((?)dataset_[0-9]+)");

  /** The docker interface service. */
  @Autowired
  private DockerInterfaceService dockerInterfaceService;

  /** The container name. */
  @Value("${dockerContainerName:crunchy-postgres}")
  private String containerName;

  /** The ip postgre db. */
  @Value("${ipPostgre:localhost}")
  private String ipPostgreDb;

  /** The user postgre db. */
  @Value("${userPostgre:root}")
  private String userPostgreDb;

  /** The pass postgre db. */
  @Value("${passwordPostgre:root}")
  private String passPostgreDb;

  /** The conn string postgre. */
  @Value("${connStringPostgree:jdbc:postgresql://localhost/datasets}")
  private String connStringPostgre;

  /** The sql get datasets name. */
  @Value("${sqlGetAllDatasetsName:select * from pg_namespace where nspname like 'dataset%'}")
  private String sqlGetDatasetsName;



  /** The kafka sender. */
  @Autowired
  private KafkaSender kafkaSender;

  /**
   * Reset dataset database.
   *
   * @throws DockerAccessException the docker access exception
   */
  @Override
  public void resetDatasetDatabase() throws DockerAccessException {
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
      throw new DockerAccessException(
          String.format("Error executing docker command to create the dataset. %s", e.getMessage()),
          e);

    }
  }

  /**
   * Creates the empty data set.
   *
   * @param datasetName the dataset name
   * @throws DockerAccessException the docker access exception
   */
  @Override
  public void createEmptyDataSet(final String datasetName) throws DockerAccessException {
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
      throw new DockerAccessException(
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
        throw new DockerAccessException(String
            .format("Error executing docker command to create the dataset. %s", e.getMessage()), e);

      }
    }

    final EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.CONNECTION_CREATED_EVENT);
    final Map<String, Object> data = new HashMap<>();
    data.put("connectionDataVO", createConnectionDataVO(datasetName));
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
   * @return the connection data for dataset
   * @throws DockerAccessException the docker access exception
   */
  @Override
  public ConnectionDataVO getConnectionDataForDataset(final String datasetName)
      throws DockerAccessException {
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
   * @throws DockerAccessException the docker access exception
   */
  @Override
  public List<ConnectionDataVO> getConnectionDataForDataset() throws DockerAccessException {
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
   * @throws DockerAccessException the docker access exception
   */
  private List<String> getAllDataSetsName() throws DockerAccessException {
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
      throw new DockerAccessException(
          String.format("Error executing docker command to create the dataset. %s", e.getMessage()),
          e);

    }
    return datasets;
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
}
