package org.eea.recordstore.service.impl;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.recordstore.exception.RecordStoreAccessException;
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
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
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
    Mockito.verify(jdbcTemplate, Mockito.times(85)).execute(Mockito.anyString());
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
    Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(PreparedStatementSetter.class),
        Mockito.any(ResultSetExtractor.class))).thenReturn(datasets);

    jdbcRecordStoreService.createDataSnapshot(1L, 1L, 1L);
    deleteTemporarySnapshotFiles();
  }

  @Test
  public void testRestoreSnapshot() throws SQLException, IOException {
    List<String> datasets = new ArrayList<>();
    datasets.add("dataset_1");
    Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(PreparedStatementSetter.class),
        Mockito.any(ResultSetExtractor.class))).thenReturn(datasets);

    jdbcRecordStoreService.createDataSnapshot(1L, 1L, 1L);

    jdbcRecordStoreService.restoreDataSnapshot(1L, 1L);
    deleteTemporarySnapshotFiles();
  }

  private void deleteTemporarySnapshotFiles() {

    File folder = new File(System.getProperty("user.dir"));
    File fList[] = folder.listFiles();
    for (int i = 0; i < fList.length; i++) {
      File pes = fList[i];
      if (pes.getName().contains(".snap")) {
        boolean success = pes.delete();
      }
    }
  }

  @Test
  public void testDeleteSnapshot() throws SQLException, IOException {

    jdbcRecordStoreService.deleteDataSnapshot(1L, 1L);

  }


}
