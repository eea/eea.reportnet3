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
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.eea.recordstore.docker.DockerClientBuilderBean;
import org.eea.recordstore.service.DockerInterfaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Binds;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.command.ExecStartResultCallback;


/**
 * The type Docker interface service.
 */
// @Service
public class DockerInterfaceServiceImpl implements DockerInterfaceService, Closeable {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DockerInterfaceServiceImpl.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * The docker client.
   */
  @Autowired
  private DockerClientBuilderBean dockerClient;


  /**
   * The Constant DATASET_NAME_PATTERN.
   */
  private static final Pattern DATASET_NAME_PATTERN = Pattern.compile("((?)dataset_[0-9]+)");

  /**
   * The envs.
   */
  @Value("${dockerVarEnvironments:null}")
  private final List<String> envs = null;

  /**
   * The container name.
   */
  @Value("${dockerContainerName}")
  private String containerName;


  /**
   * Creates the container.
   *
   * @param containerName the container name
   * @param imageName the image name
   * @param portBinding the port binding
   *
   * @return the container
   */
  @Override
  public Container createContainer(final String containerName, final String imageName,
      final String portBinding) {

    try (CreateContainerCmd command = dockerClient.dockerClient()
        .createContainerCmd("crunchydata/crunchy-postgres-gis:centos7-11.2-2.3.1").withEnv(envs)
        .withName(containerName)) {
      // Bind bind = new Bind("c:/opt/dump", new Volume("/pgwal"));// NO MAPEA... INVESTIGAR

      final Binds binds = new Binds();
      final HostConfig hostConfig = new HostConfig();
      hostConfig.withBinds(binds);
      if (null != portBinding && !portBinding.isEmpty()) {
        final String[] ports = portBinding.split(":");
        final Integer hostPort = Integer.valueOf(ports[0]);
        final Integer containerPort = Integer.valueOf(ports[1]);
        final Ports portBindings = new Ports();
        final ExposedPort tcp5432 = ExposedPort.tcp(containerPort);
        portBindings.bind(tcp5432, Ports.Binding.bindPort(hostPort));
        command.withExposedPorts(tcp5432);
        hostConfig.withPortBindings(portBindings);
      }

      command.withHostConfig(hostConfig).exec();
      LOG.info("Container created with name:{}", containerName);
    }
    return getContainer(containerName);
  }


  /**
   * Execute command inside container.
   *
   * @param container the container
   * @param command the command
   *
   * @return the byte[]
   *
   * @throws InterruptedException the interrupted exception
   */
  @Override
  public byte[] executeCommandInsideContainer(final Container container, final String... command)
      throws InterruptedException {
    final OutputStream output = new ByteArrayOutputStream();
    final OutputStream errorOutput = new ByteArrayOutputStream();
    /*
     * //"export PGPASSWORD=root && psql -h localhost -U root -p 5432 -d datasets -c \"psql -h localhost -U root -p 5432 -d datasets -c \"create table \"dataset_1\".record(    id integer NOT NULL,    name \"char\",    CONSTRAINT record_pkey PRIMARY KEY (id))\"\""
     * "/bin/bash", "-c", // "psql -h localhost -U root -p 5432 -d datasets -f /pgwal/init.sql" //&&
     * psql -h localhost -U root -p 5432 -d datasets -c
     * "create table "dataset_1".record(    id integer NOT NULL,    name "
     * char",    CONSTRAINT record_pkey PRIMARY KEY (id))" command
     */

    final ExecCreateCmdResponse execCreateCmdResponse =
        dockerClient.dockerClient().execCreateCmd(container.getId()).withAttachStdout(true)
            .withCmd(command).withTty(true).exec();
    // This works for event management. Interesting
    ExecStartResultCallback result = null;
    result =
        dockerClient.dockerClient().execStartCmd(execCreateCmdResponse.getId()).withDetach(false)

            .exec(new ExecStartResultCallback(output, errorOutput)).awaitCompletion();
    result.awaitCompletion().onComplete();
    final byte[] commandOutcome = ((ByteArrayOutputStream) output).toByteArray();
    final String outcomeOk = new String(commandOutcome);
    final String outcomeKo = new String(((ByteArrayOutputStream) errorOutput).toByteArray());
    LOG.info(outcomeOk);
    if (StringUtils.isNotBlank(outcomeKo)) {
      LOG_ERROR.error(outcomeKo);
    }

    return commandOutcome;

  }


  /**
   * Stop and remove container.
   *
   * @param container the container
   */
  @Override
  public void stopAndRemoveContainer(final Container container) {
    dockerClient.dockerClient().stopContainerCmd(container.getId()).exec();
    dockerClient.dockerClient().removeContainerCmd(container.getId()).exec();
    LOG.info("Container stopped and removed");

  }

  /**
   * Stop container.
   *
   * @param container the container
   */
  @Override
  public void stopContainer(final Container container) {
    dockerClient.dockerClient().stopContainerCmd(container.getId()).exec();
    LOG.info("Container stopped");
  }

  /**
   * Start container.
   *
   * @param container the container
   * @param timeToWait the time to wait
   * @param unit the unit
   */
  @Override
  public void startContainer(final Container container, final Long timeToWait,
      final TimeUnit unit) {
    dockerClient.dockerClient().startContainerCmd(container.getId()).exec();

    final CountDownLatch completed = new CountDownLatch(1);
    try {
      if (!completed.await(timeToWait, TimeUnit.SECONDS)) {
        LOG_ERROR.error("Container {} could not be started", container.getId());
      }
      // wait timeToWait seconds for the database to
      // start
    } catch (final InterruptedException e) {
      LOG_ERROR.error("Error starting container {}", container.getId(), e);
      Thread.currentThread().interrupt();
    }
  }


  /**
   * Gets the container.
   *
   * @param containerName the container name
   *
   * @return the container
   */
  @Override
  public Container getContainer(final String containerName) {
    final List<String> names = new ArrayList<>();
    names.add(containerName);

    final List<Container> containers = dockerClient.dockerClient().listContainersCmd()
        .withShowSize(true).withShowAll(true).withNameFilter(names).exec();

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
  public void copyFileFromHostToContainer(final String containerName, final String filePath,
      final String destinationPath) {

    dockerClient.dockerClient().copyArchiveToContainerCmd(containerName).withHostResource(filePath)
        .withRemotePath(destinationPath).exec();
  }

  /**
   * Copy file from container to host.
   *
   * @param containerName the container name
   * @param filePath the file path
   * @param destinationPath the destination path
   *
   * @return the input stream
   */
  @Override
  public InputStream copyFileFromContainerToHost(final String containerName, final String filePath,
      final String destinationPath) {

    return dockerClient.dockerClient().copyArchiveFromContainerCmd(containerName, filePath)
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
      dockerClient.dockerClient().close();
    }
  }
}
