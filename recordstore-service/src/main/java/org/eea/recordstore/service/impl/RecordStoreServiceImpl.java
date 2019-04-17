package org.eea.recordstore.service.impl;

import com.github.dockerjava.api.model.Container;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.springframework.stereotype.Service;

@Service
public class RecordStoreServiceImpl implements RecordStoreService {

  private static final Logger LOG = LoggerFactory.getLogger(RecordStoreServiceImpl.class);
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");
  private static final Pattern DATASET_NAME_PATTERN = Pattern.compile("((?)dataset_[0-9]+)");
  @Autowired
  private DockerInterfaceService dockerInterfaceService;
  private static final String CONTAINER_NAME = "crunchy-postgres";//hardCoded at the moment but it will be necessary to have a way to find it out

  @Autowired
  private KafkaSender kafkaSender;

  @Override
  public void resetDatasetDatabase() throws DockerAccessException {
    //TODO REMOVE THIS PART, THIS IS ONLY FOR TESTING PURPOSES
    final Container oldContainer = dockerInterfaceService.getContainer(
        "crunchy-postgres");
    if (null != oldContainer) {
      dockerInterfaceService.stopAndRemoveContainer(oldContainer);
    }
    //TODO END REMOVE

    final Container container = dockerInterfaceService
        .createContainer(CONTAINER_NAME, "crunchydata/crunchy-postgres-gis:centos7-11.2-2.3.1",
            "5432:5432");

    dockerInterfaceService.startContainer(container, 10l, TimeUnit.SECONDS);
    //create init file in container

    dockerInterfaceService
        //TODO need to determine where to find the init.sql file and where to store inside the container
        .copyFileFromHostToContainer(CONTAINER_NAME, "C:\\opt\\dump\\init.sql", "/pgwal");
    //"psql -h localhost -U root -p 5432 -d datasets -f /pgwal/init.sql "
    try {
      dockerInterfaceService.executeCommandInsideContainer(
          container, "/bin/bash", "-c",
          "psql -h localhost -U root -p 5432 -d datasets -f /pgwal/init.sql ");
    } catch (final InterruptedException e) {
      LOG_ERROR.error("Error executing docker command to create the dataset. {}", e.getMessage());
      throw new DockerAccessException(
          String.format("Error executing docker command to create the dataset. %s", e.getMessage()),
          e);

    }
  }

  @Override
  public void createEmptyDataSet(final String datasetName)
      throws DockerAccessException {
//line to run a crunchy container
    //docker run -d -e PG_DATABASE=datasets -e PG_PRIMARY_PORT=5432 -e PG_MODE=primary -e PG_USER=root -e PG_PASSWORD=root -e PG_PRIMARY_USER=root -e PG_PRIMARY_PASSWORD=root
    // -e PG_ROOT_PASSWORD=root -e PGBACKREST=true -p 5432:5432 --name crunchy-postgres
    //crunchydata/crunchy-postgres-gis:centos7-11.2-2.3.1
    final Container container = dockerInterfaceService.getContainer(
        "crunchy-postgres");
    final String fileName = "C:\\opt\\dump\\datasetInitCommands.txt";
    final List<String> commands = new ArrayList<>();
    //read file into stream, try-with-resources
    try (final Stream<String> stream = Files.lines(Paths.get(fileName))) {

      stream.forEach(commands::add);

    } catch (final IOException e) {
      LOG_ERROR.error("Error reading commands file to create the dataset. {}", e.getMessage());
      throw new DockerAccessException(
          String
              .format("Error reading commands file to create the dataset. %s", e.getMessage()),
          e);
    }
    for (String command : commands) {
      command = command.replace("%dataset_name%", datasetName);
      try {
        dockerInterfaceService.executeCommandInsideContainer(
            container, "psql", "-h", "localhost", "-U", "root", "-p",
            "5432", "-d", "datasets", "-c", command);
      } catch (final InterruptedException e) {
        LOG_ERROR.error("Error executing docker command to create the dataset. {}", e.getMessage());
        throw new DockerAccessException(
            String
                .format("Error executing docker command to create the dataset. %s", e.getMessage()),
            e);

      }
    }

    final EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.CONNECTION_CREATED_EVENT);
    final Map<String, Object> data = new HashMap<>();
    data.put("connectionDataVO", createConnectionDataVO(datasetName));
    event.setData(data);
    kafkaSender.sendMessage(event);

  }

  @Override
  public void createDataSetFromOther(final String sourceDatasetName,
      final String destinationDataSetName) {
    throw new java.lang.UnsupportedOperationException("Operation not implemented yet");


  }

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

  private List<String> getAllDataSetsName() throws DockerAccessException {
    final List<String> datasets = new ArrayList<>();
    final Container container = dockerInterfaceService.getContainer(CONTAINER_NAME);
    final ConnectionDataVO connectionDataVO = new ConnectionDataVO();

    try {
      final byte[] result = dockerInterfaceService
          .executeCommandInsideContainer(container, "psql", "-h", "localhost", "-U", "root", "-p",
              "5432", "-d", "datasets", "-c",
              "select * from pg_namespace where nspname like 'dataset%'");
      final String outcome = new String(result);
      if (null != result && result.length > 0) {
        final Matcher dataMatcher = DATASET_NAME_PATTERN.matcher(outcome);
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

  private static ConnectionDataVO createConnectionDataVO(final String datasetName) {
    final ConnectionDataVO result = new ConnectionDataVO();

    result.setConnectionString(
        "jdbc:postgresql://localhost/datasets"); //TODO need to undo this hardcode
    result.setUser("root");
    result.setPassword("root");
    result.setSchema(datasetName);
    return result;
  }
}
