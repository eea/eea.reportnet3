package org.eea.recordstore.service.impl;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.eea.kafka.io.KafkaSender;
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
import com.github.dockerjava.api.command.CopyArchiveFromContainerCmd;
import com.github.dockerjava.api.command.CopyArchiveToContainerCmd;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.command.ExecStartResultCallback;

/**
 * The Class DockerInterfaceServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DockerInterfaceServiceImplTest {

  /** The record store service impl. */
  @InjectMocks
  private RecordStoreServiceImpl recordStoreServiceImpl;

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

  /** The listcontainer. */
  @Mock
  private ListContainersCmd listcontainer;

  /** The exec create cmd. */
  @Mock
  private ExecCreateCmd execCreateCmd;

  /** The exec start cmd. */
  @Mock
  private ExecStartCmd execStartCmd;

  /** The exec create cmd response. */
  @Mock
  private ExecCreateCmdResponse execCreateCmdResponse;

  /** The exec start result callback. */
  @Mock
  private ExecStartResultCallback execStartResultCallback;

  /** The stop container cmd. */
  @Mock
  private StopContainerCmd stopContainerCmd;

  /** The remove container cmd. */
  @Mock
  private RemoveContainerCmd removeContainerCmd;

  /** The start container cmd. */
  @Mock
  private StartContainerCmd startContainerCmd;

  /** The copy archive to container cmd. */
  @Mock
  private CopyArchiveToContainerCmd copyArchiveToContainerCmd;

  /** The copy archive from container cmd. */
  @Mock
  private CopyArchiveFromContainerCmd copyArchiveFromContainerCmd;

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
    ReflectionTestUtils.setField(dockerInterfaceServiceImpl, "containerName", "crunchy-postgres");
    ReflectionTestUtils.setField(dockerInterfaceServiceImpl, "envs", new ArrayList<>());

  }


  /**
   * Test create container.
   *
   * @throws DockerAccessException the docker access exception
   */
  @Test
  public void testCreateContainer() throws DockerAccessException {
    when(dockerClient.dockerClient()).thenReturn(docker);
    when(docker.createContainerCmd(Mockito.any())).thenReturn(command);
    when(command.withEnv((List<String>) Mockito.any())).thenReturn(command);
    when(command.withName(Mockito.any())).thenReturn(command);
    when(command.withName(Mockito.any())).thenReturn(command);
    when(command.withHostConfig(Mockito.any())).thenReturn(command);
    when(dockerClient.dockerClient()).thenReturn(docker);
    when(docker.listContainersCmd()).thenReturn(listcontainer);
    when(listcontainer.withShowSize(Mockito.any())).thenReturn(listcontainer);
    when(listcontainer.withShowAll(Mockito.any())).thenReturn(listcontainer);
    when(listcontainer.withNameFilter(Mockito.any())).thenReturn(listcontainer);
    dockerInterfaceServiceImpl.createContainer("test", "test", "00:00");
  }


  /**
   * Test execute command inside container.
   *
   * @throws DockerAccessException the docker access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testExecuteCommandInsideContainer()
      throws DockerAccessException, InterruptedException {
    when(dockerClient.dockerClient()).thenReturn(docker);
    when(docker.execCreateCmd(Mockito.any())).thenReturn(execCreateCmd);
    when(execCreateCmd.withAttachStdout(Mockito.any())).thenReturn(execCreateCmd);
    when(execCreateCmd.withCmd(Mockito.any())).thenReturn(execCreateCmd);
    when(execCreateCmd.withTty(Mockito.any())).thenReturn(execCreateCmd);
    when(execCreateCmd.exec()).thenReturn(execCreateCmdResponse);
    when(dockerClient.dockerClient()).thenReturn(docker);
    when(docker.execStartCmd(Mockito.any())).thenReturn(execStartCmd);
    when(execStartCmd.withDetach(Mockito.any())).thenReturn(execStartCmd);
    when(execStartCmd.exec(Mockito.any())).thenReturn(execStartResultCallback);
    when(execStartResultCallback.awaitCompletion()).thenReturn(execStartResultCallback);
    dockerInterfaceServiceImpl.executeCommandInsideContainer(new Container(), "test");
  }


  /**
   * Test get connection.
   *
   * @throws DockerAccessException the docker access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testGetConnection() throws DockerAccessException, InterruptedException {
    commonWhens();
    when(execStartResultCallback.awaitCompletion()).thenReturn(execStartResultCallback);
    dockerInterfaceServiceImpl.getConnection();
  }

  /**
   * Test get connection exception.
   *
   * @throws DockerAccessException the docker access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testGetConnectionException() throws DockerAccessException, InterruptedException {
    commonWhens();
    doThrow(new InterruptedException()).when(execStartResultCallback).awaitCompletion();
    dockerInterfaceServiceImpl.getConnection();
  }

  /**
   * Common whens.
   */
  private void commonWhens() {
    ArrayList<Container> listContainers = new ArrayList<Container>();
    listContainers.add(new Container());
    when(dockerClient.dockerClient()).thenReturn(docker);
    when(docker.listContainersCmd()).thenReturn(listcontainer);
    when(listcontainer.withShowSize(Mockito.any())).thenReturn(listcontainer);
    when(listcontainer.withShowAll(Mockito.any())).thenReturn(listcontainer);
    when(listcontainer.withNameFilter(Mockito.any())).thenReturn(listcontainer);
    when(listcontainer.exec()).thenReturn(listContainers);
    when(dockerClient.dockerClient()).thenReturn(docker);
    when(docker.execCreateCmd(Mockito.any())).thenReturn(execCreateCmd);
    when(execCreateCmd.withAttachStdout(Mockito.any())).thenReturn(execCreateCmd);
    when(execCreateCmd.withCmd(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any())).thenReturn(execCreateCmd);
    when(execCreateCmd.withTty(Mockito.any())).thenReturn(execCreateCmd);
    when(execCreateCmd.exec()).thenReturn(execCreateCmdResponse);
    when(dockerClient.dockerClient()).thenReturn(docker);
    when(docker.execStartCmd(Mockito.any())).thenReturn(execStartCmd);
    when(execStartCmd.withDetach(Mockito.any())).thenReturn(execStartCmd);
    when(execStartCmd.exec(Mockito.any())).thenReturn(execStartResultCallback);
  }

  /**
   * Test stop and remove container.
   *
   * @throws DockerAccessException the docker access exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testStopAndRemoveContainer() throws DockerAccessException, InterruptedException {
    when(dockerClient.dockerClient()).thenReturn(docker);
    when(docker.stopContainerCmd(Mockito.any())).thenReturn(stopContainerCmd);
    when(dockerClient.dockerClient()).thenReturn(docker);
    when(docker.removeContainerCmd(Mockito.any())).thenReturn(removeContainerCmd);
    dockerInterfaceServiceImpl.stopAndRemoveContainer(new Container());
  }

  /**
   * Test stop container.
   */
  @Test
  public void testStopContainer() {
    when(dockerClient.dockerClient()).thenReturn(docker);
    when(docker.stopContainerCmd(Mockito.any())).thenReturn(stopContainerCmd);
    dockerInterfaceServiceImpl.stopContainer(new Container());
  }

  /**
   * Test start container.
   */
  @Test
  public void testStartContainer() {
    when(dockerClient.dockerClient()).thenReturn(docker);
    when(docker.startContainerCmd(Mockito.any())).thenReturn(startContainerCmd);
    dockerInterfaceServiceImpl.startContainer(new Container(), 1L, TimeUnit.DAYS);
  }

  /**
   * Test copy file from host to container.
   */
  @Test
  public void testCopyFileFromHostToContainer() {
    when(dockerClient.dockerClient()).thenReturn(docker);
    when(docker.copyArchiveToContainerCmd(Mockito.any())).thenReturn(copyArchiveToContainerCmd);
    when(copyArchiveToContainerCmd.withHostResource(Mockito.any()))
        .thenReturn(copyArchiveToContainerCmd);
    when(copyArchiveToContainerCmd.withRemotePath(Mockito.any()))
        .thenReturn(copyArchiveToContainerCmd);
    dockerInterfaceServiceImpl.copyFileFromHostToContainer("test", "test", "test");
  }

  /**
   * Test copy file from container to host.
   */
  @Test
  public void testCopyFileFromContainerToHost() {
    when(dockerClient.dockerClient()).thenReturn(docker);
    when(docker.copyArchiveFromContainerCmd(Mockito.any(), Mockito.any()))
        .thenReturn(copyArchiveFromContainerCmd);
    when(copyArchiveFromContainerCmd.withHostPath(Mockito.any()))
        .thenReturn(copyArchiveFromContainerCmd);
    dockerInterfaceServiceImpl.copyFileFromContainerToHost("test", "test", "test");
  }

  /**
   * Test close.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void testClose() throws IOException {
    when(dockerClient.dockerClient()).thenReturn(docker);
    dockerInterfaceServiceImpl.close();
  }

}
