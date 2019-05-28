package org.eea.recordstore.service.impl;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import org.eea.kafka.io.KafkaSender;
import org.eea.recordstore.controller.RecordStoreControllerImpl;
import org.eea.recordstore.docker.DockerClientBuilderBean;
import org.eea.recordstore.exception.DockerAccessException;
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

  /** The Constant DATASET. */
  private static final String DATASET = "dataset_1";


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
   * Test create dataset.
   *
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void testCreateDataset() throws DockerAccessException {
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    recordStoreServiceImpl.createEmptyDataSet(DATASET);
  }


  /**
   * Test create dataset exception.
   *
   * @throws DockerAccessException the docker access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test(expected = DockerAccessException.class)
  public void testCreateDatasetException() throws DockerAccessException, InterruptedException {
    doThrow(new InterruptedException()).when(dockerInterfaceService).executeCommandInsideContainer(
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    recordStoreServiceImpl.createEmptyDataSet(DATASET);
  }

  /**
   * Test reset dataset database.
   *
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void testResetDatasetDatabase() throws DockerAccessException {

    Mockito.when(dockerInterfaceService.getContainer(Mockito.any())).thenReturn(new Container());
    recordStoreServiceImpl.resetDatasetDatabase();
  }

  /**
   * Test reset dataset database exception.
   *
   * @throws DockerAccessException the docker access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test(expected = DockerAccessException.class)
  public void testResetDatasetDatabaseException()
      throws DockerAccessException, InterruptedException {
    doThrow(new InterruptedException()).when(dockerInterfaceService)
        .executeCommandInsideContainer(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    recordStoreServiceImpl.resetDatasetDatabase();
  }

  /**
   * Test connection data.
   *
   * @throws DockerAccessException the docker access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testConnectionData() throws DockerAccessException, InterruptedException {
    byte[] result = DATASET.getBytes();
    Mockito
        .when(dockerInterfaceService.executeCommandInsideContainer(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(result);
    recordStoreServiceImpl.getConnectionDataForDataset(DATASET);
  }


  /**
   * Test connection data exception.
   *
   * @throws DockerAccessException the docker access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test(expected = DockerAccessException.class)
  public void testConnectionDataException() throws DockerAccessException, InterruptedException {
    doThrow(new InterruptedException()).when(dockerInterfaceService).executeCommandInsideContainer(
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    recordStoreServiceImpl.getConnectionDataForDataset(DATASET);
  }


  /**
   * Test connection data 2.
   *
   * @throws DockerAccessException the docker access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testConnectionData2() throws DockerAccessException, InterruptedException {
    byte[] result = DATASET.getBytes();
    Mockito
        .when(dockerInterfaceService.executeCommandInsideContainer(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(result);
    recordStoreServiceImpl.getConnectionDataForDataset();

  }

  /**
   * Test create data set from another.
   *
   * @throws DockerAccessException the docker access exception
   */
  @Test(expected = UnsupportedOperationException.class)
  public void testCreateDataSetFromAnother() throws DockerAccessException {

    recordStoreServiceImpl.createDataSetFromOther(DATASET, DATASET);

  }



}
