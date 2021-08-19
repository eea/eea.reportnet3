package org.eea.recordstore.service.impl;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.times;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DataCollectionController.DataCollectionControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.thread.ThreadPropertiesManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyOperation;
import org.postgresql.copy.CopyOut;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Encoding;
import org.postgresql.core.QueryExecutor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;


 @RunWith(MockitoJUnitRunner.class)
public class JdbcRecordStoreServiceImplTest {

  @InjectMocks
  private JdbcRecordStoreServiceImpl jdbcRecordStoreService;
  @Mock
  private JdbcTemplate jdbcTemplate;

  @Mock
  private KafkaSenderUtils kafkaSender;

  @Mock
  private LockService lockService;

  @Mock
  private DataCollectionControllerZuul dataCollectionControllerZuul;

  @Mock
  private DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul;

  @Mock
  private DataSetControllerZuul datasetControllerZuul;

  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

   private static MockedStatic<DriverManager> driverManager;

   @BeforeClass
   public static void init() {
     driverManager = Mockito.mockStatic(DriverManager.class);
   }
   @AfterClass
   public static void close() {
     driverManager.close();
   }

  @Before
  public void initMocks() {

    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("user", "password"));
    ThreadPropertiesManager.setVariable("user", "user");
    MockitoAnnotations.openMocks(this);

    ReflectionTestUtils.setField(jdbcRecordStoreService,"bufferFile",1024);
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
    Mockito.verify(jdbcTemplate, Mockito.times(93)).execute(Mockito.anyString());
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
  public void testCreateSnapshot() throws SQLException, IOException, EEAException {
    List<String> datasets = new ArrayList<>();
//    Mockito.when(dataSetSnapshotControllerZuul.getSchemaById(Mockito.any()))
//        .thenReturn(new SnapshotVO());
    Mockito.doReturn(new SnapshotVO()).when(dataSetSnapshotControllerZuul).getSchemaById(Mockito.any());
    datasets.add("dataset_1");

    final Connection connection = Mockito.mock(BaseConnection.class);

    Mockito.when(((BaseConnection) connection).getEncoding())
        .thenReturn(Encoding.defaultEncoding());
    QueryExecutor queryExector = Mockito.mock(QueryExecutor.class);
    CopyOperation copyOut = Mockito.mock(CopyOut.class);
    Mockito.when(copyOut.isActive()).thenReturn(true);
    Mockito.when(queryExector.startCopy(Mockito.anyString(), Mockito.anyBoolean()))
        .thenReturn(copyOut);
    Mockito.when(((BaseConnection) connection).getQueryExecutor()).thenReturn(queryExector);


      driverManager.when(() -> DriverManager
          .getConnection(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
          .thenReturn(connection);

      Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(PreparedStatementSetter.class),
        Mockito.any(ResultSetExtractor.class))).thenReturn(datasets);

    jdbcRecordStoreService.createDataSnapshot(1L, 1L, 1L, new Date().toString());
    Mockito.verify(jdbcTemplate, Mockito.times(1)).query(Mockito.anyString(),
        Mockito.any(PreparedStatementSetter.class), Mockito.any(ResultSetExtractor.class));
  }

  @Test
  public void testRestoreSnapshot()
      throws SQLException, IOException, URISyntaxException, EEAException {

    final Connection connection = Mockito.mock(BaseConnection.class);




    Mockito.when(((BaseConnection) connection).getEncoding())
        .thenReturn(Encoding.defaultEncoding());
    QueryExecutor queryExector = Mockito.mock(QueryExecutor.class);
    CopyOperation copyOut = Mockito.mock(CopyIn.class);
    Mockito.when(copyOut.isActive()).thenReturn(true);
    Mockito.when(queryExector.startCopy(Mockito.anyString(), Mockito.anyBoolean()))
        .thenReturn(copyOut);
    Mockito.when(((BaseConnection) connection).getQueryExecutor()).thenReturn(queryExector);

    driverManager.when(() -> DriverManager
        .getConnection(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
        .thenReturn(connection);

    List<String> datasets = new ArrayList<>();
    datasets.add("dataset_1");
    Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(PreparedStatementSetter.class),
        Mockito.any(ResultSetExtractor.class))).thenReturn(datasets);

//    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    ReflectionTestUtils.setField(jdbcRecordStoreService, "pathSnapshot", "./src/test/resources/");
//    Mockito.doNothing().when(kafkaSender).releaseNotificableKafkaEvent(Mockito.any(), Mockito.any(),
//        Mockito.any());
//    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    SnapshotVO snap = new SnapshotVO();
    snap.setDatasetId(1L);
    Mockito.when(dataSetSnapshotControllerZuul.getById(Mockito.any())).thenReturn(snap);
    DataSetMetabaseVO datasetMetabase = new DataSetMetabaseVO();
    datasetMetabase.setDataflowId(1L);
    datasetMetabase.setDataProviderId(1L);
//    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.any()))
//        .thenReturn(datasetMetabase);
    jdbcRecordStoreService.restoreDataSnapshot(1L, 1L, 1L, DatasetTypeEnum.DESIGN, false, false);
    Mockito.verify(kafkaSender, Mockito.times(0)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }


  @Test
  public void testDeleteSnapshot() throws SQLException, IOException {

    jdbcRecordStoreService.deleteDataSnapshot(1L, 1L);
    File file = new File("./snapshot_" + 1L + "-dataset_" + 1L + "_table_DatasetValue.snap");
    assertFalse(file.exists());
  }

  @After
  public void afterTests() {
    File file = new File("./nullsnapshot_1_table_DatasetValue.snap");
    file.delete();
    file = new File("./nullsnapshot_1_table_FieldValue.snap");
    file.delete();
    file = new File("./nullsnapshot_1_table_RecordValue.snap");
    file.delete();
    file = new File("./nullsnapshot_1_table_TableValue.snap");
    file.delete();
    file = new File("./nullsnapshot_1_table_AttachmentValue.snap");
    file.delete();
  }

  @Test
  public void testDeleteDataset() throws SQLException, IOException {
    jdbcRecordStoreService.deleteDataset("schema");
    Mockito.verify(jdbcTemplate, times(1)).execute(Mockito.any(String.class));
  }
}
