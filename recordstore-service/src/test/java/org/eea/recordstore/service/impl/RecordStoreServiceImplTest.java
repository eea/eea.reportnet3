package org.eea.recordstore.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.io.KafkaSender;
import org.eea.recordstore.controller.RecordStoreControllerImpl;
import org.eea.recordstore.docker.DockerClientBuilderBean;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.DockerInterfaceService;
import org.eea.recordstore.service.RecordStoreService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.Container;

/**
 * The Class RecordStoreServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class RecordStoreServiceImplTest {

  /** The record store service impl. */
  @InjectMocks
  private RecordStoreServiceImpl recordStoreServiceImpl;

  /** The record store controller impl. */
  @InjectMocks
  private RecordStoreControllerImpl recordStoreControllerImpl;

  /** The docker interface service impl. */
  @InjectMocks
  private DockerInterfaceServiceImpl dockerInterfaceServiceImpl;

  /** The record store service. */
  @Mock
  private RecordStoreService recordStoreService;

  /** The docker interface service. */
  @Mock
  private DockerInterfaceService dockerInterfaceService;

  /** The kafka sender. */
  @Mock
  private KafkaSender kafkaSender;

  /** The docker client. */
  @Mock
  private DockerClientBuilderBean dockerClient;

  /** The command. */
  @Mock
  private CreateContainerCmd command;

  /** The docker. */
  @Mock
  private DockerClient docker;

  /** The jdbc template. */
  @Mock
  private JdbcTemplate jdbcTemplate;

  /** The Constant DATASET. */
  private static final String DATASET = "dataset_1";

  /** The Constant FAILED. */
  private static final String FAILED = "failed";


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);

    ReflectionTestUtils.setField(recordStoreServiceImpl, "containerName", "crunchy-postgres");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "ipPostgreDb", "localhost");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "userPostgreDb", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "passPostgreDb", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "connStringPostgre",
        "jdbc:postgresql://localhost/datasets");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "sqlGetDatasetsName",
        "select * from pg_namespace where nspname like 'dataset%'");

  }


  /**
   * Test create dataset.
   *
   * @throws RecordStoreAccessException the docker access exception
   */
  @Test
  public void testCreateDataset() throws RecordStoreAccessException {
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    recordStoreServiceImpl.createEmptyDataSet(DATASET, "");
    Mockito.verify(kafkaSender, times(1)).sendMessage(Mockito.any());
  }


  /**
   * Test create dataset exception.
   *
   * @throws RecordStoreAccessException the docker access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test(expected = RecordStoreAccessException.class)
  public void testCreateDatasetException() throws RecordStoreAccessException, InterruptedException {
    doThrow(new InterruptedException()).when(dockerInterfaceService).executeCommandInsideContainer(
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    recordStoreServiceImpl.createEmptyDataSet(DATASET, "");
  }

  /**
   * Test reset dataset database.
   *
   * @throws RecordStoreAccessException the docker access exception
   */
  @Test
  public void testResetDatasetDatabase() throws RecordStoreAccessException {
    Mockito.when(dockerInterfaceService.getContainer(Mockito.any())).thenReturn(new Container());
    recordStoreServiceImpl.resetDatasetDatabase();
    Mockito.verify(dockerInterfaceService, times(1)).getContainer(Mockito.any());
  }

  /**
   * Test reset dataset database exception.
   *
   * @throws RecordStoreAccessException the docker access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test(expected = RecordStoreAccessException.class)
  public void testResetDatasetDatabaseException()
      throws RecordStoreAccessException, InterruptedException {
    doThrow(new InterruptedException()).when(dockerInterfaceService)
        .executeCommandInsideContainer(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    recordStoreServiceImpl.resetDatasetDatabase();
  }

  /**
   * Test connection data.
   *
   * @throws RecordStoreAccessException the docker access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testConnectionData() throws RecordStoreAccessException, InterruptedException {
    byte[] input = DATASET.getBytes();
    Mockito
        .when(dockerInterfaceService.executeCommandInsideContainer(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(input);
    ConnectionDataVO result = recordStoreServiceImpl.getConnectionDataForDataset(DATASET);
    assertEquals(FAILED, DATASET, result.getSchema());
  }


  /**
   * Test connection data exception.
   *
   * @throws RecordStoreAccessException the docker access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test(expected = RecordStoreAccessException.class)
  public void testConnectionDataException()
      throws RecordStoreAccessException, InterruptedException {
    doThrow(new InterruptedException()).when(dockerInterfaceService).executeCommandInsideContainer(
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    recordStoreServiceImpl.getConnectionDataForDataset(DATASET);
  }


  /**
   * Test connection data 2.
   *
   * @throws RecordStoreAccessException the docker access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testConnectionData2() throws RecordStoreAccessException, InterruptedException {
    byte[] input = DATASET.getBytes();
    Mockito
        .when(dockerInterfaceService.executeCommandInsideContainer(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(input);
    List<ConnectionDataVO> result = recordStoreServiceImpl.getConnectionDataForDataset();
    assertEquals(FAILED, DATASET, result.get(0).getSchema());
  }

  /**
   * Test create data set from another.
   *
   * @throws RecordStoreAccessException the docker access exception
   */
  @Test(expected = UnsupportedOperationException.class)
  public void testCreateDataSetFromAnother() throws RecordStoreAccessException {
    recordStoreServiceImpl.createDataSetFromOther(DATASET, DATASET);
  }

  /**
   * Creates the data snapshot test.
   *
   * @throws RecordStoreAccessException the record store access exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void createDataSnapshotTest()
      throws RecordStoreAccessException, SQLException, IOException {
    recordStoreServiceImpl.createDataSnapshot(1L, 1L, 1L, new Date().toString());
  }

  /**
   * Restore data snapshot test.
   *
   * @throws RecordStoreAccessException the record store access exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void restoreDataSnapshotTest()
      throws RecordStoreAccessException, SQLException, IOException {
    recordStoreServiceImpl.restoreDataSnapshot(1L, 1L, 1L, DatasetTypeEnum.DESIGN, false, false);
  }

  /**
   * Delete data snapshot test.
   *
   * @throws RecordStoreAccessException the record store access exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void deleteDataSnapshotTest()
      throws RecordStoreAccessException, SQLException, IOException {
    recordStoreServiceImpl.deleteDataSnapshot(1L, 1L);
  }

  /**
   * Delete dataset test.
   *
   * @throws RecordStoreAccessException the record store access exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void deleteDatasetTest() throws RecordStoreAccessException, SQLException, IOException {
    recordStoreServiceImpl.deleteDataset(DATASET);
  }

  /**
   * Creates the schemas test.
   */
  @Test(expected = UnsupportedOperationException.class)
  public void createSchemasTest() {
    recordStoreServiceImpl.createSchemas(null, 1L, true, true);
  }

  /**
   * Execute query view commands test.
   *
   * @throws RecordStoreAccessException the record store access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test(expected = RecordStoreAccessException.class)
  public void executeQueryViewCommandsTest()
      throws RecordStoreAccessException, InterruptedException {
    when(dockerInterfaceService.getContainer(Mockito.any())).thenReturn(new Container());
    doThrow(new InterruptedException("Error executing docker command to create the dataset. %s"))
        .when(dockerInterfaceService).executeCommandInsideContainer(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    try {
      recordStoreServiceImpl.executeQueryViewCommands("command");
    } catch (RecordStoreAccessException e) {
      assertEquals("Error executing docker command to create the dataset. %s",
          e.getCause().getMessage());
      throw e;
    }
  }


}
