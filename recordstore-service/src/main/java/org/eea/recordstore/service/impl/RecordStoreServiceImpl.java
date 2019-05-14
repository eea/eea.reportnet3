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

@Service
public class RecordStoreServiceImpl implements RecordStoreService {

  private static final Logger LOG = LoggerFactory.getLogger(RecordStoreServiceImpl.class);
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");
  private static final Pattern DATASET_NAME_PATTERN = Pattern.compile("((?)dataset_[0-9]+)");
  @Autowired
  private DockerInterfaceService dockerInterfaceService;
  
  @Value("${dockerContainerName:crunchy-postgres}")
  private String CONTAINER_NAME;

  @Value("${ipPostgre}")
  private String IP_POSTGRE_DB = "" ;
 
  private static String USER_POSTGRE_DB;
  @Value("${userPostgre:root}")
  public void setUserPostgre(String user) {
    USER_POSTGRE_DB = user;
  }
  
  private static String PASS_POSTGRE_DB;
  @Value("${passwordPostgre:root}")
  public void setPassPostgre(String pass) {
    PASS_POSTGRE_DB = pass;
  }
  
  private static String CONN_STRING_POSTGRE;
  @Value("${connStringPostgree:jdbc:postgresql://localhost/datasets}")
  public void setConnStringPostgre(String connString) {
    CONN_STRING_POSTGRE = connString;
  }
  @Value("${sqlGetAllDatasetsName:select * from pg_namespace where nspname like 'dataset%'}")
  private final String SQL_GET_DATASETS_NAME = "";
  
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

    final File fileInitSql = new File(
        getClass().getClassLoader().getResource("init.sql").getFile());
    
    dockerInterfaceService.copyFileFromHostToContainer(CONTAINER_NAME, fileInitSql.getPath(), "/pgwal");
    
    /*dockerInterfaceService
        //TODO need to determine where to find the init.sql file and where to store inside the container
        .copyFileFromHostToContainer(CONTAINER_NAME, "C:\\opt\\dump\\init.sql", "/pgwal");*/
    //"psql -h localhost -U root -p 5432 -d datasets -f /pgwal/init.sql "
    try {
      dockerInterfaceService.executeCommandInsideContainer(
          container, "/bin/bash", "-c",
          "psql -h "+ IP_POSTGRE_DB +" -U "+USER_POSTGRE_DB+" -p 5432 -d datasets -f /pgwal/init.sql ");
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
        CONTAINER_NAME);
   
    final File fileInitCommands = new File(
        getClass().getClassLoader().getResource("datasetInitCommands.txt").getFile());
    final List<String> commands = new ArrayList<>();
    //read file into stream, try-with-resources
    try (final Stream<String> stream = Files.lines(fileInitCommands.toPath())) {

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
            container, "psql", "-h", IP_POSTGRE_DB, "-U", USER_POSTGRE_DB, "-p",
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
   

    try {
      final byte[] result = dockerInterfaceService
          .executeCommandInsideContainer(container, "psql", "-h", IP_POSTGRE_DB, "-U", USER_POSTGRE_DB, "-p",
              "5432", "-d", "datasets", "-c",
              SQL_GET_DATASETS_NAME);
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

    result.setConnectionString(CONN_STRING_POSTGRE); 
    result.setUser(USER_POSTGRE_DB);
    result.setPassword(PASS_POSTGRE_DB);
    result.setSchema(datasetName);
    return result;
  }
}
