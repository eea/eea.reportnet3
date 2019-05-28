package org.eea.recordstore.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import java.util.List;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.io.KafkaSender;
import org.eea.recordstore.docker.DockerClientBuilderBean;
import org.eea.recordstore.exception.DockerAccessException;
import org.eea.recordstore.service.DockerInterfaceService;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.recordstore.service.impl.RecordStoreServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;

/**
 * The Class RecordStoreControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class RecordStoreControllerImplTest {


  /** The record store service impl. */
  @InjectMocks
  private RecordStoreServiceImpl recordStoreServiceImpl;

  /** The record store controller impl. */
  @InjectMocks
  private RecordStoreControllerImpl recordStoreControllerImpl;

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

  /** The Constant TEST. */
  public static final String TEST = "test";

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONTAINER_NAME", "crunchy-postgres");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "IP_POSTGRE_DB", "localhost");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "USER_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "PASS_POSTGRE_DB", "root");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "CONN_STRING_POSTGRE",
        "jdbc:postgresql://localhost/datasets");
    ReflectionTestUtils.setField(recordStoreServiceImpl, "SQL_GET_DATASETS_NAME",
        "select * from pg_namespace where nspname like 'dataset%'");
  }



  /**
   * Test create empty data set.
   *
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void testCreateEmptyDataSet() throws DockerAccessException {
    recordStoreControllerImpl.createEmptyDataset(TEST);

  }

  /**
   * Test create empty data set exception.
   *
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void testCreateEmptyDataSetException() throws DockerAccessException {
    doThrow(new DockerAccessException()).when(recordStoreService).createEmptyDataSet(TEST);
    recordStoreControllerImpl.createEmptyDataset(TEST);
  }

  /**
   * Reste data set data base test.
   *
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void resteDataSetDataBaseTest() throws DockerAccessException {
    recordStoreControllerImpl.resteDataSetDataBase();
  }

  /**
   * Reste data set data base test exception.
   *
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void resteDataSetDataBaseTestException() throws DockerAccessException {
    doThrow(new DockerAccessException()).when(recordStoreService).resetDatasetDatabase();
    recordStoreControllerImpl.resteDataSetDataBase();
  }

  /**
   * Gets the connection to dataset test.
   *
   * @return the connection to dataset test
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void getConnectionToDatasetTest() throws DockerAccessException {
    ConnectionDataVO result = recordStoreControllerImpl.getConnectionToDataset(TEST);
  }

  /**
   * Gets the connection to dataset test exception.
   *
   * @return the connection to dataset test exception
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void getConnectionToDatasetTestException() throws DockerAccessException {
    doThrow(new DockerAccessException()).when(recordStoreService).getConnectionDataForDataset(TEST);
    ConnectionDataVO result = recordStoreControllerImpl.getConnectionToDataset(TEST);
    assertNull(result);
  }


  /**
   * Gets the data set connections test.
   *
   * @return the data set connections test
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void getDataSetConnectionsTest() throws DockerAccessException {
    List<ConnectionDataVO> result = recordStoreControllerImpl.getDataSetConnections();
    assertNotNull(result);
  }

  /**
   * Gets the data set connections test exception.
   *
   * @return the data set connections test exception
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void getDataSetConnectionsTestException() throws DockerAccessException {
    doThrow(new DockerAccessException()).when(recordStoreService).getConnectionDataForDataset();
    List<ConnectionDataVO> result = recordStoreControllerImpl.getDataSetConnections();
    assertNull(result);
  }



}
