package org.eea.recordstore.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eea.recordstore.service.DockerInterfaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Binds;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;


/**
 * The type Docker interface service.
 */
@Service
public class DockerInterfaceServiceImpl implements DockerInterfaceService, Closeable {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DockerInterfaceServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The docker client. */
  private final DockerClient dockerClient =
      DockerClientBuilder.getInstance("tcp://localhost:2375").build();

  /** The Constant DATASET_NAME_PATTERN. */
  private static final Pattern DATASET_NAME_PATTERN = Pattern.compile("((?)dataset_[0-9]+)");

  /** The envs. */
  @Value("${dockerVarEnvironments:null}")
  private final List<String> ENVS = null;

  /** The container name. */
  @Value("${dockerContainerName:crunchy-postgres}")
  private String CONTAINER_NAME;


  /**
   * Creates the container.
   *
   * @param containerName the container name
   * @param imageName the image name
   * @param portBinding the port binding
   * @return the container
   */
  @Override
  public Container createContainer(String containerName, String imageName, String portBinding) {

    CreateContainerCmd command =
        dockerClient.createContainerCmd("crunchydata/crunchy-postgres-gis:centos7-11.2-2.3.1")
            .withEnv(ENVS).withName(containerName);
    Bind bind = new Bind("c:/opt/dump", new Volume("/pgwal"));// NO MAPEA... INVESTIGAR
    Binds binds = new Binds();
    HostConfig hostConfig = new HostConfig();
    hostConfig.withBinds(binds);
    if (null != portBinding && !portBinding.isEmpty()) {
      String[] ports = portBinding.split(":");
      Integer hostPort = Integer.valueOf(ports[0]);
      Integer containerPort = Integer.valueOf(ports[1]);
      Ports portBindings = new Ports();
      ExposedPort tcp5432 = ExposedPort.tcp(containerPort);
      portBindings.bind(tcp5432, Ports.Binding.bindPort(hostPort));
      command.withExposedPorts(tcp5432);
      hostConfig.withPortBindings(portBindings);
    }

    CreateContainerResponse containerResponse = command.withHostConfig(hostConfig).exec();


    return getContainer(containerName);
  }


  /**
   * Execute command inside container.
   *
   * @param container the container
   * @param command the command
   * @return the byte[]
   * @throws InterruptedException the interrupted exception
   */
  @Override
  public byte[] executeCommandInsideContainer(Container container, String... command)
      throws InterruptedException {
    OutputStream output = new ByteArrayOutputStream();
    OutputStream errorOutput = new ByteArrayOutputStream();
    /*
     * //"export PGPASSWORD=root && psql -h localhost -U root -p 5432 -d datasets -c \"psql -h localhost -U root -p 5432 -d datasets -c \"create table \"dataset_1\".record(    id integer NOT NULL,    name \"char\",    CONSTRAINT record_pkey PRIMARY KEY (id))\"\""
     * "/bin/bash", "-c", // "psql -h localhost -U root -p 5432 -d datasets -f /pgwal/init.sql" //&&
     * psql -h localhost -U root -p 5432 -d datasets -c
     * "create table "dataset_1".record(    id integer NOT NULL,    name "
     * char",    CONSTRAINT record_pkey PRIMARY KEY (id))" command
     */
    ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(container.getId())
        .withAttachStdout(true).withCmd(command).withTty(true).exec();
    ExecStartResultCallback result = null;// Esto sirve para gestión de eventos. Interesante
    result = dockerClient.execStartCmd(execCreateCmdResponse.getId()).withDetach(false)
        .exec(new ExecStartResultCallback(output, errorOutput)).awaitCompletion();
    result.awaitCompletion().onComplete();
    byte[] commandOutcome = ((ByteArrayOutputStream) output).toByteArray();
    String outcomeOk = new String(commandOutcome);
    String outcomeKo = new String(((ByteArrayOutputStream) errorOutput).toByteArray());
    LOG.info(outcomeOk);
    if (!"".equals(outcomeKo)) {
      LOG_ERROR.error(outcomeKo);
    }

    return commandOutcome;

  }

  /**
   * Gets the connection.
   *
   * @return the connection
   */
  @Deprecated
  @Override
  public List<String> getConnection() {
    Container container = getContainer(CONTAINER_NAME);
    List<String> result = new ArrayList<>();
    OutputStream output = new ByteArrayOutputStream();
    OutputStream errorOutput = new ByteArrayOutputStream();
    ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(container.getId())
        .withAttachStdout(true).withCmd(

            // "psql -h localhost -U root -p 5432 -d datasets -f /pgwal/init.sql"
            // && psql -h localhost -U root -p 5432 -d datasets -c "create table "dataset_1".record(
            // id integer NOT NULL, name "char", CONSTRAINT record_pkey PRIMARY KEY (id))"
            "psql", "-h", "localhost", "-U", "root", "-p", "5432", "-d", "datasets", "-c",
            "select * from pg_namespace where nspname like 'dataset%'")
        .withTty(true).exec();
    ExecStartResultCallback execResult = null;// Esto sirve para gestión de eventos. Interesante
    execResult = dockerClient.execStartCmd(execCreateCmdResponse.getId()).withDetach(false)
        .exec(new ExecStartResultCallback(output, errorOutput));
    try {
      execResult.awaitCompletion().onComplete();

    } catch (InterruptedException e) {
      LOG_ERROR.error(e.getMessage());
    }
    String outcomeOk = new String(((ByteArrayOutputStream) output).toByteArray());
    String outcomeKo = new String(((ByteArrayOutputStream) errorOutput).toByteArray());
    LOG.info(outcomeOk);
    if (!"".equals(outcomeKo)) {
      LOG_ERROR.error(outcomeKo);
    }
    Matcher dataMatcher = DATASET_NAME_PATTERN.matcher(outcomeOk);
    while (dataMatcher.find()) {
      result.add(dataMatcher.group(0));
    }
    return result;
  }

  /**
   * Stop and remove container.
   *
   * @param container the container
   */
  @Override
  public void stopAndRemoveContainer(Container container) {
    dockerClient.stopContainerCmd(container.getId()).exec();
    dockerClient.removeContainerCmd(container.getId()).exec();
  }

  /**
   * Stop container.
   *
   * @param container the container
   */
  @Override
  public void stopContainer(Container container) {
    dockerClient.stopContainerCmd(container.getId()).exec();

  }

  /**
   * Start container.
   *
   * @param container the container
   * @param timeToWait the time to wait
   * @param unit the unit
   */
  @Override
  public void startContainer(Container container, Long timeToWait, TimeUnit unit) {
    dockerClient.startContainerCmd(container.getId()).exec();
    CountDownLatch completed = new CountDownLatch(1);
    try {
      completed.await(timeToWait, TimeUnit.SECONDS);// wait timeToWait seconds for the database to
                                                    // start
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


  /**
   * Gets the container.
   *
   * @param containerName the container name
   * @return the container
   */
  @Override
  public Container getContainer(String containerName) {
    List<String> names = new ArrayList<>();
    names.add(containerName);
    List<Container> containers = dockerClient.listContainersCmd().withShowSize(true)
        .withShowAll(true).withNameFilter(names).exec();
    return null != containers && !containers.isEmpty() ? containers.get(0) : null;
  }

  /**
   * Copy file from host to container.
   *
   * @param containerName the container name
   * @param filePath the file path
   * @param destinationPath the destination path
   */
  @Override
  public void copyFileFromHostToContainer(String containerName, String filePath,
      String destinationPath) {
    dockerClient.copyArchiveToContainerCmd(containerName).withHostResource(filePath)
        .withRemotePath(destinationPath).exec();
  }

  /**
   * Copy file from container to host.
   *
   * @param containerName the container name
   * @param filePath the file path
   * @param destinationPath the destination path
   * @return the input stream
   */
  @Override
  public InputStream copyFileFromContainerToHost(String containerName, String filePath,
      String destinationPath) {

    return dockerClient.copyArchiveFromContainerCmd(containerName, filePath)
        .withHostPath(destinationPath).exec();
  }


  /**
   * Close.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public void close() throws IOException {
    if (null != dockerClient) {
      dockerClient.close();
    }
  }
}
