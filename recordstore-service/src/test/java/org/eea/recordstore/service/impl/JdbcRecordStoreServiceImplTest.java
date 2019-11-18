package org.eea.recordstore.service.impl;

import static org.mockito.Mockito.times;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyOperation;
import org.postgresql.copy.CopyOut;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Encoding;
import org.postgresql.core.QueryExecutor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.util.ReflectionTestUtils;

// @RunWith(MockitoJUnitRunner.class)
@RunWith(PowerMockRunner.class)
@PrepareForTest({DriverManager.class, JdbcRecordStoreServiceImpl.class})
public class JdbcRecordStoreServiceImplTest {

  @InjectMocks
  private JdbcRecordStoreServiceImpl jdbcRecordStoreService;
  @Mock
  private JdbcTemplate jdbcTemplate;

  @Mock
  private KafkaSenderUtils kafkaSender;


  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);

    ReflectionTestUtils.setField(jdbcRecordStoreService, "resourceFile",
        new ClassPathResource("datasetInitCommands.txt"));
    ReflectionTestUtils.setField(jdbcRecordStoreService, "userPostgreDb", "root");

    ReflectionTestUtils.setField(jdbcRecordStoreService, "passPostgreDb", "root");
    ReflectionTestUtils.setField(jdbcRecordStoreService, "connStringPostgre",
        "jdbc:postgresql://localhost/datasets");
    ReflectionTestUtils.setField(jdbcRecordStoreService, "sqlGetDatasetsName",
        "select * from pg_namespace where nspname like 'dataset%'");

  }

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test(expected = UnsupportedOperationException.class)
  public void resetDatasetDatabase() throws RecordStoreAccessException {
    jdbcRecordStoreService.resetDatasetDatabase();
  }

  @Test
  public void createEmptyDataSet() throws RecordStoreAccessException {
    jdbcRecordStoreService.createEmptyDataSet("", "");
    Mockito.verify(kafkaSender, Mockito.times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
    Mockito.verify(jdbcTemplate, Mockito.times(91)).execute(Mockito.anyString());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void createDataSetFromOther() {
    jdbcRecordStoreService.createDataSetFromOther("", "");
  }

  @Test
  public void getConnectionDataForDataset() throws RecordStoreAccessException {
    List<String> datasets = new ArrayList<>();
    datasets.add("dataset_1");
    Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(PreparedStatementSetter.class),
        Mockito.any(ResultSetExtractor.class))).thenReturn(datasets);

    List<ConnectionDataVO> connections = jdbcRecordStoreService.getConnectionDataForDataset();
    Assert.assertNotNull("Error: Null connections", connections);
    Assert.assertEquals("Error: wrong number of connections", 1, connections.size());
    Assert.assertEquals("Error: wrong name of connection", "dataset_1",
        connections.get(0).getSchema());
  }

  @Test
  public void getConnectionDataForDataset1() throws RecordStoreAccessException {
    List<String> datasets = new ArrayList<>();
    datasets.add("dataset_1");
    Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(PreparedStatementSetter.class),
        Mockito.any(ResultSetExtractor.class))).thenReturn(datasets);

    ConnectionDataVO connection = jdbcRecordStoreService.getConnectionDataForDataset("dataset_1");
    Assert.assertNotNull("Error: Null connections", connection);
    Assert.assertEquals("Error: wrong name of connection", "dataset_1", connection.getSchema());
  }


  @Test
  public void testCreateSnapshot() throws SQLException, IOException {
    List<String> datasets = new ArrayList<>();
    datasets.add("dataset_1");
    PowerMockito.mockStatic(DriverManager.class);

    final Connection connection = Mockito.mock(BaseConnection.class);

    Mockito.when(((BaseConnection) connection).getEncoding())
        .thenReturn(Encoding.defaultEncoding());
    QueryExecutor queryExector = Mockito.mock(QueryExecutor.class);
    CopyOperation copyOut = Mockito.mock(CopyOut.class);
    Mockito.when(copyOut.isActive()).thenReturn(true);
    Mockito.when(queryExector.startCopy(Mockito.anyString(), Mockito.anyBoolean()))
        .thenReturn(copyOut);
    Mockito.when(((BaseConnection) connection).getQueryExecutor()).thenReturn(queryExector);

    PowerMockito.mockStatic(DriverManager.class);

    Mockito.when(
        DriverManager.getConnection(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(connection);

    Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(PreparedStatementSetter.class),
        Mockito.any(ResultSetExtractor.class))).thenReturn(datasets);
    /*
     * Mockito.when( DriverManager.getConnection(Mockito.anyString(), Mockito.anyString(),
     * Mockito.anyString())) .thenReturn(conexion);
     */

    jdbcRecordStoreService.createDataSnapshot(1L, 1L, 1L);
  }

  @Test
  public void testRestoreSnapshot() throws SQLException, IOException, URISyntaxException {
    PowerMockito.mockStatic(DriverManager.class);

    final Connection connection = Mockito.mock(BaseConnection.class);

    Mockito.when(((BaseConnection) connection).getEncoding())
        .thenReturn(Encoding.defaultEncoding());
    QueryExecutor queryExector = Mockito.mock(QueryExecutor.class);
    CopyOperation copyOut = Mockito.mock(CopyIn.class);
    Mockito.when(copyOut.isActive()).thenReturn(true);
    Mockito.when(queryExector.startCopy(Mockito.anyString(), Mockito.anyBoolean()))
        .thenReturn(copyOut);
    Mockito.when(((BaseConnection) connection).getQueryExecutor()).thenReturn(queryExector);

    PowerMockito.mockStatic(DriverManager.class);

    Mockito.when(
        DriverManager.getConnection(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(connection);

    List<String> datasets = new ArrayList<>();
    datasets.add("dataset_1");
    Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(PreparedStatementSetter.class),
        Mockito.any(ResultSetExtractor.class))).thenReturn(datasets);

    ReflectionTestUtils.setField(jdbcRecordStoreService, "pathSnapshot", "./src/test/resources/");

    jdbcRecordStoreService.restoreDataSnapshot(1L, 1L, 1L, TypeDatasetEnum.DESIGN);
    Mockito.verify(kafkaSender, Mockito.times(2))
        .releaseDatasetKafkaEvent(Mockito.any(EventType.class), Mockito.anyLong());
  }


  @Test
  public void testDeleteSnapshot() throws SQLException, IOException {

    jdbcRecordStoreService.deleteDataSnapshot(1L, 1L);

  }

  @After
  public void afterTests() {
    File file = new File("./nullsnapshot_1-dataset_1_table_DatasetValue.snap");
    file.delete();
    file = new File("./nullsnapshot_1-dataset_1_table_FieldValue.snap");
    file.delete();
    file = new File("./nullsnapshot_1-dataset_1_table_RecordValue.snap");
    file.delete();
    file = new File("./nullsnapshot_1-dataset_1_table_TableValue.snap");
    file.delete();
  }

  @Test
  public void testDeleteDataset() throws SQLException, IOException {
    jdbcRecordStoreService.deleteDataset("schema");
    Mockito.verify(jdbcTemplate, times(1)).execute(Mockito.any(String.class));
  }
}
