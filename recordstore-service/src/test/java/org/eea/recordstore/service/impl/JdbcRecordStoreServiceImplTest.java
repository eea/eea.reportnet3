package org.eea.recordstore.service.impl;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doThrow;
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
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DataCollectionController.DataCollectionControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.controller.dataset.TestDatasetController.TestDatasetControllerZuul;
import org.eea.interfaces.controller.document.DocumentController.DocumentControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
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

  @Mock
  private DocumentControllerZuul documentControllerZuul;

  @Mock
  private DatasetSchemaControllerZuul datasetSchemaControllerZuul;

  @Mock
  private TestDatasetControllerZuul testDatasetControllerZuul;

  @Mock
  private EUDatasetControllerZuul euDatasetControllerZuul;

  /** The dataflow controller zuul. */
  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  private TableSchemaVO table;

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
    FieldSchemaVO field = new FieldSchemaVO();
    field.setType(DataType.TEXT);
    field.setId("id");
    FieldSchemaVO field2 = new FieldSchemaVO();
    field2.setType(DataType.DATE);
    field2.setId("id2");
    FieldSchemaVO field3 = new FieldSchemaVO();
    field3.setType(DataType.DATETIME);
    field3.setId("id3");
    FieldSchemaVO field4 = new FieldSchemaVO();
    field4.setType(DataType.NUMBER_INTEGER);
    field4.setId("id4");
    FieldSchemaVO field5 = new FieldSchemaVO();
    field5.setType(DataType.POINT);
    field5.setId("id5");

    RecordSchemaVO record = new RecordSchemaVO();
    List<FieldSchemaVO> listFields = new ArrayList<>();
    listFields.add(field);
    listFields.add(field2);
    listFields.add(field3);
    listFields.add(field4);
    listFields.add(field5);
    record.setFieldSchema(listFields);

    RecordSchemaVO record2 = new RecordSchemaVO();
    record2.setFieldSchema(listFields);
    table = new TableSchemaVO();
    table.setNameTableSchema("test");
    table.setRecordSchema(record);


    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("user", "password"));
    ThreadPropertiesManager.setVariable("user", "user");
    MockitoAnnotations.openMocks(this);

    ReflectionTestUtils.setField(jdbcRecordStoreService, "bufferFile", 1024);
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
    Mockito.when(documentControllerZuul.getSnapshotDocument(Mockito.any(), Mockito.any()))
        .thenReturn(new byte[5]);
    Mockito.doReturn(new SnapshotVO()).when(dataSetSnapshotControllerZuul)
        .getSchemaById(Mockito.any());
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
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDataProviderId(1L);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(1L))
        .thenReturn(datasetMetabaseVO);
    DataFlowVO dfVO = new DataFlowVO();
    dfVO.setName("dfName");
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dfVO);


    driverManager.when(() -> DriverManager.getConnection(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(connection);

    Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(PreparedStatementSetter.class),
        Mockito.any(ResultSetExtractor.class))).thenReturn(datasets);

    jdbcRecordStoreService.createDataSnapshot(1L, 1L, 1L, new Date().toString());
    Mockito.verify(jdbcTemplate, Mockito.times(1)).query(Mockito.anyString(),
        Mockito.any(PreparedStatementSetter.class), Mockito.any(ResultSetExtractor.class));
  }

  @Test
  public void testCreateSnapshot2() throws SQLException, IOException, EEAException {
    List<String> datasets = new ArrayList<>();
    Mockito.when(documentControllerZuul.getSnapshotDocument(Mockito.any(), Mockito.any()))
        .thenReturn(new byte[5]);
    datasets.add("dataset_1");
    Mockito.when(datasetControllerZuul.getDatasetType(Mockito.any()))
        .thenReturn(DatasetTypeEnum.COLLECTION);

    Mockito.when(datasetMetabaseControllerZuul.getType(Mockito.any()))
        .thenReturn(DatasetTypeEnum.COLLECTION);
    final Connection connection = Mockito.mock(BaseConnection.class);

    Mockito.when(((BaseConnection) connection).getEncoding())
        .thenReturn(Encoding.defaultEncoding());
    QueryExecutor queryExector = Mockito.mock(QueryExecutor.class);
    CopyOperation copyOut = Mockito.mock(CopyOut.class);
    Mockito.when(copyOut.isActive()).thenReturn(true);
    Mockito.when(queryExector.startCopy(Mockito.anyString(), Mockito.anyBoolean()))
        .thenReturn(copyOut);
    Mockito.when(((BaseConnection) connection).getQueryExecutor()).thenReturn(queryExector);


    driverManager.when(() -> DriverManager.getConnection(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(connection);

    Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(PreparedStatementSetter.class),
        Mockito.any(ResultSetExtractor.class))).thenReturn(datasets);

    jdbcRecordStoreService.createDataSnapshot(1L, 1L, 1L, new Date().toString());
    Mockito.verify(jdbcTemplate, Mockito.times(1)).query(Mockito.anyString(),
        Mockito.any(PreparedStatementSetter.class), Mockito.any(ResultSetExtractor.class));
  }

  @Test
  public void testCreateSnapshot3() throws SQLException, IOException, EEAException {
    List<String> datasets = new ArrayList<>();
    Mockito.when(documentControllerZuul.getSnapshotDocument(Mockito.any(), Mockito.any()))
        .thenReturn(new byte[5]);
    datasets.add("dataset_1");
    Mockito.when(datasetControllerZuul.getDatasetType(Mockito.any()))
        .thenReturn(DatasetTypeEnum.DESIGN);
    Mockito.when(dataSetSnapshotControllerZuul.getById(Mockito.any())).thenReturn(null);
    Mockito.when(dataSetSnapshotControllerZuul.getById(Mockito.any())).thenReturn(new SnapshotVO());
    Mockito.when(datasetMetabaseControllerZuul.getType(Mockito.any()))
        .thenReturn(DatasetTypeEnum.DESIGN);
    final Connection connection = Mockito.mock(BaseConnection.class);

    Mockito.when(((BaseConnection) connection).getEncoding())
        .thenReturn(Encoding.defaultEncoding());
    QueryExecutor queryExector = Mockito.mock(QueryExecutor.class);
    CopyOperation copyOut = Mockito.mock(CopyOut.class);
    Mockito.when(copyOut.isActive()).thenReturn(true);
    Mockito.when(queryExector.startCopy(Mockito.anyString(), Mockito.anyBoolean()))
        .thenReturn(copyOut);
    Mockito.when(((BaseConnection) connection).getQueryExecutor()).thenReturn(queryExector);
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDataProviderId(1L);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(1L))
        .thenReturn(datasetMetabaseVO);
    DataFlowVO dfVO = new DataFlowVO();
    dfVO.setName("dfName");
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dfVO);


    driverManager.when(() -> DriverManager.getConnection(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(connection);

    Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(PreparedStatementSetter.class),
        Mockito.any(ResultSetExtractor.class))).thenReturn(datasets);

    jdbcRecordStoreService.createDataSnapshot(1L, 1L, 1L, new Date().toString());
    Mockito.verify(jdbcTemplate, Mockito.times(1)).query(Mockito.anyString(),
        Mockito.any(PreparedStatementSetter.class), Mockito.any(ResultSetExtractor.class));
  }

  @Test
  public void testCreateSnapshot4() throws SQLException, IOException, EEAException {
    List<String> datasets = new ArrayList<>();
    datasets.add("dataset_1");
    Mockito.when(dataSetSnapshotControllerZuul.getById(Mockito.any())).thenReturn(null);
    Mockito.when(dataSetSnapshotControllerZuul.getById(Mockito.any())).thenReturn(new SnapshotVO());
    Mockito.when(datasetMetabaseControllerZuul.getType(Mockito.any()))
        .thenReturn(DatasetTypeEnum.DESIGN);
    final Connection connection = Mockito.mock(BaseConnection.class);

    Mockito.when(((BaseConnection) connection).getEncoding())
        .thenReturn(Encoding.defaultEncoding());
    QueryExecutor queryExector = Mockito.mock(QueryExecutor.class);
    CopyOperation copyOut = Mockito.mock(CopyOut.class);
    Mockito.when(copyOut.isActive()).thenReturn(true);
    Mockito.when(queryExector.startCopy(Mockito.anyString(), Mockito.anyBoolean()))
        .thenReturn(copyOut);
    Mockito.when(((BaseConnection) connection).getQueryExecutor()).thenReturn(queryExector);
    doThrow(new NullPointerException("")).when(datasetControllerZuul).getDatasetType(Mockito.any());
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDataProviderId(1L);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(1L))
        .thenReturn(datasetMetabaseVO);
    Mockito.doNothing().when(dataSetSnapshotControllerZuul).releaseLocksFromReleaseDatasets(1L, 1L);
    DataFlowVO dfVO = new DataFlowVO();
    dfVO.setName("dfName");
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dfVO);

    driverManager.when(() -> DriverManager.getConnection(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(connection);

    Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(PreparedStatementSetter.class),
        Mockito.any(ResultSetExtractor.class))).thenReturn(datasets);

    jdbcRecordStoreService.createDataSnapshot(1L, 1L, 1L, new Date().toString());
    Mockito.verify(jdbcTemplate, Mockito.times(1)).query(Mockito.anyString(),
        Mockito.any(PreparedStatementSetter.class), Mockito.any(ResultSetExtractor.class));
  }

  @Test
  public void testCreateSnapshot5() throws SQLException, IOException, EEAException {
    List<String> datasets = new ArrayList<>();
    datasets.add("dataset_1");
    Mockito.when(datasetMetabaseControllerZuul.getType(Mockito.any()))
        .thenReturn(DatasetTypeEnum.COLLECTION);
    List<ReportingDatasetVO> reportings = new ArrayList<>();
    ReportingDatasetVO reportingDatasetVO = new ReportingDatasetVO();
    reportingDatasetVO.setDataProviderId(1L);
    reportings.add(reportingDatasetVO);
    Mockito.when(datasetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(reportings);
    final Connection connection = Mockito.mock(BaseConnection.class);

    Mockito.when(((BaseConnection) connection).getEncoding())
        .thenReturn(Encoding.defaultEncoding());
    QueryExecutor queryExector = Mockito.mock(QueryExecutor.class);
    CopyOperation copyOut = Mockito.mock(CopyOut.class);
    Mockito.when(copyOut.isActive()).thenReturn(true);
    Mockito.when(queryExector.startCopy(Mockito.anyString(), Mockito.anyBoolean()))
        .thenReturn(copyOut);
    Mockito.when(((BaseConnection) connection).getQueryExecutor()).thenReturn(queryExector);
    doThrow(new NullPointerException("")).when(datasetControllerZuul).getDatasetType(Mockito.any());
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDataProviderId(1L);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(1L))
        .thenReturn(datasetMetabaseVO);
    DataFlowVO dfVO = new DataFlowVO();
    dfVO.setName("dfName");
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dfVO);

    driverManager.when(() -> DriverManager.getConnection(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(connection);

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

    driverManager.when(() -> DriverManager.getConnection(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(connection);

    List<String> datasets = new ArrayList<>();
    datasets.add("dataset_1");
    Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(PreparedStatementSetter.class),
        Mockito.any(ResultSetExtractor.class))).thenReturn(datasets);

    ReflectionTestUtils.setField(jdbcRecordStoreService, "pathSnapshot", "./src/test/resources/");
    SnapshotVO snap = new SnapshotVO();
    snap.setDatasetId(1L);
    Mockito.when(dataSetSnapshotControllerZuul.getById(Mockito.any())).thenReturn(snap);
    DataSetMetabaseVO datasetMetabase = new DataSetMetabaseVO();
    datasetMetabase.setDataflowId(1L);
    datasetMetabase.setDataProviderId(1L);
    jdbcRecordStoreService.restoreDataSnapshot(1L, 1L, 1L, DatasetTypeEnum.DESIGN, false, false);
    Mockito.verify(kafkaSender, Mockito.times(0)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void restoreSnapshotWithDeleteTest()
      throws SQLException, IOException, URISyntaxException, EEAException {

    final Connection connection = Mockito.mock(BaseConnection.class);

    Mockito.mock(QueryExecutor.class);
    Mockito.mock(CopyIn.class);

    driverManager.when(() -> DriverManager.getConnection(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(connection);

    List<String> datasets = new ArrayList<>();
    datasets.add("dataset_1");
    Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(PreparedStatementSetter.class),
        Mockito.any(ResultSetExtractor.class))).thenReturn(datasets);

    ReflectionTestUtils.setField(jdbcRecordStoreService, "pathSnapshot", "./src/test/resources/");
    SnapshotVO snap = new SnapshotVO();
    snap.setDatasetId(1L);
    Mockito.when(dataSetSnapshotControllerZuul.getById(Mockito.any())).thenReturn(snap);
    DataSetMetabaseVO datasetMetabase = new DataSetMetabaseVO();
    datasetMetabase.setDataflowId(1L);
    datasetMetabase.setDataProviderId(1L);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(datasetMetabase);
    DataFlowVO dfVO = new DataFlowVO();
    dfVO.setName("dfName");
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dfVO);

    jdbcRecordStoreService.restoreDataSnapshot(1L, 1L, 1L, DatasetTypeEnum.EUDATASET, false, true);
    Mockito.verify(lockService, Mockito.times(2)).removeLockByCriteria(Mockito.any());
  }

  @Test
  public void createUpdateQueryViewTest() {
    DataSetSchemaVO datasetSchemaVO = new DataSetSchemaVO();
    List<TableSchemaVO> tableSchemas = new ArrayList<>();
    tableSchemas.add(table);
    datasetSchemaVO.setTableSchemas(tableSchemas);
    Mockito.when(datasetSchemaControllerZuul.findDataSchemaByDatasetId(Mockito.anyLong()))
        .thenReturn(datasetSchemaVO);

    jdbcRecordStoreService.createUpdateQueryView(1L, true);
    Mockito.verify(jdbcTemplate, Mockito.times(4)).execute(Mockito.anyString());
  }

  @Test
  public void createUpdateQueryViewNotMaterialTest() {
    DataSetSchemaVO datasetSchemaVO = new DataSetSchemaVO();
    List<TableSchemaVO> tableSchemas = new ArrayList<>();
    tableSchemas.add(table);
    datasetSchemaVO.setTableSchemas(tableSchemas);
    Mockito.when(datasetSchemaControllerZuul.findDataSchemaByDatasetId(Mockito.anyLong()))
        .thenReturn(datasetSchemaVO);

    jdbcRecordStoreService.createUpdateQueryView(1L, false);
    Mockito.verify(jdbcTemplate, Mockito.times(3)).execute(Mockito.anyString());
  }

  @Test
  public void updateMaterializedQueryViewTest() {
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.REPORTING);
    datasetMetabaseVO.setDataProviderId(1L);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(datasetMetabaseVO);
    ReportingDatasetVO reportingDatasetVO = new ReportingDatasetVO();
    reportingDatasetVO.setId(1L);
    List<ReportingDatasetVO> reportings = new ArrayList<>();
    reportings.add(reportingDatasetVO);
    Mockito
        .when(datasetMetabaseControllerZuul
            .findReportingDataSetIdByDataflowIdAndProviderId(Mockito.any(), Mockito.any()))
        .thenReturn(reportings);
    jdbcRecordStoreService.updateMaterializedQueryView(1L, "user", true);
    Mockito.verify(kafkaSender, Mockito.times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
  }

  @Test
  public void updateMaterializedQueryViewTestDatasetTest() {
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.TEST);
    datasetMetabaseVO.setDataProviderId(1L);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(datasetMetabaseVO);
    TestDatasetVO reportingDatasetVO = new TestDatasetVO();
    reportingDatasetVO.setId(1L);
    List<TestDatasetVO> reportings = new ArrayList<>();
    reportings.add(reportingDatasetVO);
    Mockito.when(testDatasetControllerZuul.findTestDatasetByDataflowId(Mockito.any()))
        .thenReturn(reportings);
    jdbcRecordStoreService.updateMaterializedQueryView(1L, "user", true);
    Mockito.verify(kafkaSender, Mockito.times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
  }

  @Test
  public void updateMaterializedQueryViewCollectionTest() {
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.COLLECTION);
    datasetMetabaseVO.setDataProviderId(1L);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(datasetMetabaseVO);
    DataCollectionVO reportingDatasetVO = new DataCollectionVO();
    reportingDatasetVO.setId(1L);
    List<DataCollectionVO> reportings = new ArrayList<>();
    reportings.add(reportingDatasetVO);
    Mockito.when(dataCollectionControllerZuul.findDataCollectionIdByDataflowId(Mockito.any()))
        .thenReturn(reportings);
    jdbcRecordStoreService.updateMaterializedQueryView(1L, "user", true);
    Mockito.verify(kafkaSender, Mockito.times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
  }

  @Test
  public void updateMaterializedQueryViewEUTest() {
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.EUDATASET);
    datasetMetabaseVO.setDataProviderId(1L);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(datasetMetabaseVO);
    EUDatasetVO reportingDatasetVO = new EUDatasetVO();
    reportingDatasetVO.setId(1L);
    List<EUDatasetVO> reportings = new ArrayList<>();
    reportings.add(reportingDatasetVO);
    Mockito.when(euDatasetControllerZuul.findEUDatasetByDataflowId(Mockito.any()))
        .thenReturn(reportings);
    jdbcRecordStoreService.updateMaterializedQueryView(1L, "user", true);
    Mockito.verify(kafkaSender, Mockito.times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
  }

  @Test
  public void deleteSnapshotTest() throws SQLException, IOException {

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
