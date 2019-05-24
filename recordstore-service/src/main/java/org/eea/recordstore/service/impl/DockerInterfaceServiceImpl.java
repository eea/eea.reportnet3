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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
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
@Service
public class DockerInterfaceServiceImpl implements DockerInterfaceService, Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(DockerInterfaceServiceImpl.class);
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /*private final DockerClient dockerClient = DockerClientBuilder
      .getInstance("tcp://localhost:2375")
      .build();*/
  
  @Autowired
  private DockerClientBuilderBean dockerClient;
  
  

  private static final Pattern DATASET_NAME_PATTERN = Pattern.compile("((?)dataset_[0-9]+)");
  
  @Value("${dockerVarEnvironments:null}")
  private List<String> envs = null;
  
  @Value("${dockerContainerName:crunchy-postgres}")
  private String containerName;
  

  @Override
  public Container createContainer(String containerName, String imageName, String portBinding) {
   
    CreateContainerCmd command = dockerClient.dockerClient()
        .createContainerCmd("crunchydata/crunchy-postgres-gis:centos7-11.2-2.3.1")
        .withEnv(envs).withName(containerName);
    //Bind bind = new Bind("c:/opt/dump", new Volume("/pgwal"));//NO MAPEA... INVESTIGAR
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


  @Override
  public byte[] executeCommandInsideContainer(Container container, String... command)
      throws InterruptedException {
    OutputStream output = new ByteArrayOutputStream();
    OutputStream errorOutput = new ByteArrayOutputStream();
    /*
    //"export PGPASSWORD=root && psql -h localhost -U root -p 5432 -d datasets -c \"psql -h localhost -U root -p 5432 -d datasets -c \"create table \"dataset_1\".record(    id integer NOT NULL,    name \"char\",    CONSTRAINT record_pkey PRIMARY KEY (id))\"\""
            "/bin/bash", "-c",
            // "psql -h localhost -U root -p 5432 -d datasets -f /pgwal/init.sql"
            //&& psql -h localhost -U root -p 5432 -d datasets -c "create table "dataset_1".record(    id integer NOT NULL,    name "char",    CONSTRAINT record_pkey PRIMARY KEY (id))"
            command
     */
    ExecCreateCmdResponse execCreateCmdResponse = dockerClient.dockerClient().execCreateCmd(container.getId())
        .withAttachStdout(true)
        .withCmd(
            command)
        .withTty(true)
        .exec();
    ExecStartResultCallback result = null;//Esto sirve para gestión de eventos. Interesante
    result = dockerClient.dockerClient()
        .execStartCmd(execCreateCmdResponse.getId()).withDetach(false)
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

  @Deprecated
  @Override
  public List<String> getConnection() {
    Container container = getContainer(containerName);
    List<String> result = new ArrayList<>();
    OutputStream output = new ByteArrayOutputStream();
    OutputStream errorOutput = new ByteArrayOutputStream();
    ExecCreateCmdResponse execCreateCmdResponse = dockerClient.dockerClient().execCreateCmd(container.getId())
        .withAttachStdout(true)
        .withCmd(

            // "psql -h localhost -U root -p 5432 -d datasets -f /pgwal/init.sql"
            //&& psql -h localhost -U root -p 5432 -d datasets -c "create table "dataset_1".record(    id integer NOT NULL,    name "char",    CONSTRAINT record_pkey PRIMARY KEY (id))"
            "psql", "-h", "localhost", "-U", "root", "-p", "5432", "-d", "datasets", "-c",
            "select * from pg_namespace where nspname like 'dataset%'")
        .withTty(true)
        .exec();
    ExecStartResultCallback execResult = null;//Esto sirve para gestión de eventos. Interesante
    execResult = dockerClient.dockerClient()
        .execStartCmd(execCreateCmdResponse.getId()).withDetach(false)
        .exec(new ExecStartResultCallback(output,
            errorOutput));
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

  @Override
  public void stopAndRemoveContainer(Container container) {
    dockerClient.dockerClient().stopContainerCmd(container.getId()).exec();
    dockerClient.dockerClient().removeContainerCmd(container.getId()).exec();
  }

  @Override
  public void stopContainer(Container container) {
    dockerClient.dockerClient().stopContainerCmd(container.getId()).exec();

  }

  @Override
  public void startContainer(Container container, Long timeToWait,
      TimeUnit unit) {
    dockerClient.dockerClient().startContainerCmd(container.getId()).exec();
    CountDownLatch completed = new CountDownLatch(1);
    try {
      completed.await(timeToWait,
          TimeUnit.SECONDS);//wait timeToWait seconds for the database to start
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


  @Override
  public Container getContainer(String containerName) {
    List<String> names = new ArrayList<>();
    names.add(containerName);
    List<Container> containers = dockerClient.dockerClient().listContainersCmd()
        .withShowSize(true)
        .withShowAll(true)
        .withNameFilter(names).exec();
    return null != containers && !containers.isEmpty() ? containers.get(0) : null;
  }

  @Override
  public void copyFileFromHostToContainer(String containerName, String filePath,
      String destinationPath) {
    dockerClient.dockerClient().copyArchiveToContainerCmd(containerName)
        .withHostResource(filePath)
        .withRemotePath(destinationPath).exec();
  }

  @Override
  public InputStream copyFileFromContainerToHost(String containerName, String filePath,
      String destinationPath) {

    return dockerClient.dockerClient().copyArchiveFromContainerCmd(containerName, filePath)
        .withHostPath(destinationPath)
        .exec();
  }


  @Override
  public void close() throws IOException {
    if (null != dockerClient) {
      dockerClient.dockerClient().close();
    }
  }
}
