package org.eea.recordstore.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

/**
 * The type Jdbc record store service.
 */
@Service("jdbcRecordStoreServiceImpl")
public class JdbcRecordStoreServiceImpl implements RecordStoreService {

  /**
   * The kafka sender.
   */
  @Autowired
  private KafkaSender kafkaSender;

  @Autowired
  private DataSetControllerZuul datasetControllerZuul;


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

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Value("classpath:datasetInitCommands.txt")
  private Resource resourceFile;

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(JdbcRecordStoreServiceImpl.class);

  @Override
  public void resetDatasetDatabase() throws RecordStoreAccessException {
    throw new java.lang.UnsupportedOperationException("Operation not implemented yet");
  }

  @Override
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

  @Override
  public void createDataSetFromOther(String sourceDatasetName, String destinationDataSetName) {
    throw new java.lang.UnsupportedOperationException("Operation not implemented yet");
  }

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
   * @return the all data sets name
   *
   * @throws RecordStoreAccessException the docker access exception
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

  @Override
  public void createSnapshot(Long idDataset) {

    // Runtime rt = Runtime.getRuntime();
    Process p;
    ProcessBuilder pb;

    String nombreFichero = "snap6.dump";
    pb = new ProcessBuilder("C:\\pgsql\\bin\\pg_dump", "-v", "-Fc", "--host=localhost",
        "--port=5432", "--username=postgres", "--dbname=datasets", "--schema=dataset_" + idDataset,
        "--data-only", "--file=C:\\tmp\\" + nombreFichero);
    try {


      pb.redirectErrorStream(true);
      p = pb.start();
      // p.waitFor();
      InputStream is = p.getInputStream();
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      String ll;

      while ((ll = br.readLine()) != null) {
        System.out.println(ll);
      }

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // pg_dump -v -Fc -h localhost -U %user% -p 5432 datasets -n %sourceDatasetName% -f
    // "/tmp/%sourceDatasetName%.dump"

  }


  @Override
  public void restoreSnapshot(Long idDataset) {

    Runtime rt = Runtime.getRuntime();
    Process p;
    ProcessBuilder pb;

    pb = new ProcessBuilder("C:\\pgsql\\bin\\pg_restore", "-v", "--host=localhost", "--port=5432",
        "--username=postgres", "--dbname=datasets", "C:\\tmp\\snap6.dump");
    try {
      // se pueden borrar los datos del dataset directamente desde el pg_restore, con el parametro
      // -c
      datasetControllerZuul.deleteImportData(idDataset);

      pb.redirectErrorStream(true);
      p = pb.start();
      // p.waitFor();
      InputStream is = p.getInputStream();
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      String ll;

      while ((ll = br.readLine()) != null) {
        System.out.println(ll);
      }

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // pg_restore -v -h localhost -U %user% -p 5432 -d datasets "/tmp/%sourceDatasetName%.dump"

  }

}
