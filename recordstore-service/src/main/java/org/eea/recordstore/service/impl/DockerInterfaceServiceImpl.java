package org.eea.recordstore.service.impl;

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
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eea.recordstore.service.DockerInterfaceService;
import org.springframework.stereotype.Service;


/**
 * The type Docker interface service.
 */
@Service
public class DockerInterfaceServiceImpl implements DockerInterfaceService, Closeable {

  private final DockerClient dockerClient = DockerClientBuilder
      .getInstance("tcp://localhost:2375")
      .build();


  @Override
  public Container createContainer(String containerName, String imageName, String portBinding) {
    List<String> envs = new ArrayList<>();
    envs.add("PG_DATABASE=datasets");
    envs.add("PG_PRIMARY_PORT=5432");
    envs.add("PG_MODE=primary");
    envs.add("PG_USER=root");
    envs.add("PG_PASSWORD=root");
    envs.add("PG_PRIMARY_USER=root");
    envs.add("PG_PRIMARY_PASSWORD=root");
    envs.add("PG_ROOT_PASSWORD=root");
    envs.add("PGBACKREST=true");
    envs.add("PGPASSWORD=root");
    CreateContainerCmd command = dockerClient
        .createContainerCmd("crunchydata/crunchy-postgres-gis:centos7-11.2-2.3.1")
        .withEnv(envs).withName(containerName);
    Bind bind = new Bind("c:/opt/dump", new Volume("/pgwal"));//NO MAPEA... INVESTIGAR
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
  public void executeCommandInsideContainer(String command, Container container, Long timeToWait,
      TimeUnit unit) {
    ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(container.getId())
        .withAttachStdout(true)
        .withCmd(
            //"export PGPASSWORD=root && psql -h localhost -U root -p 5432 -d datasets -c \"psql -h localhost -U root -p 5432 -d datasets -c \"create table \"dataset_1\".record(    id integer NOT NULL,    name \"char\",    CONSTRAINT record_pkey PRIMARY KEY (id))\"\""
            "/bin/bash", "-c",
            // "psql -h localhost -U root -p 5432 -d datasets -f /pgwal/init.sql"
            //&& psql -h localhost -U root -p 5432 -d datasets -c "create table "dataset_1".record(    id integer NOT NULL,    name "char",    CONSTRAINT record_pkey PRIMARY KEY (id))"
            command)
        .withTty(true)
        .exec();
    ExecStartResultCallback result = null;//Esto sirve para gestión de eventos. Interesante
    result = dockerClient
        .execStartCmd(execCreateCmdResponse.getId()).withDetach(false)
        .exec(new ExecStartResultCallback(System.out,
            System.err));//se puede redirigir salida de container al log. Mola
    result.onComplete();

  }

  @Override
  public void stopAndRemoveContainer(Container container) {
    dockerClient.stopContainerCmd(container.getId()).exec();
    dockerClient.removeContainerCmd(container.getId()).exec();
  }

  @Override
  public void stopContainer(Container container) {
    dockerClient.stopContainerCmd(container.getId()).exec();

  }

  @Override
  public void startContainer(Container container, Long timeToWait,
      TimeUnit unit) {
    dockerClient.startContainerCmd(container.getId()).exec();
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
    names.add("crunchy-postgres");
    List<Container> containers = dockerClient.listContainersCmd()
        .withShowSize(true)
        .withShowAll(true)
        .withNameFilter(names).exec();
    return null != containers && !containers.isEmpty() ? containers.get(0) : null;
  }

  @Override
  public void copyFileFromHostToContainer(String containerName, String filePath,
      String destinationPath) {
    dockerClient.copyArchiveToContainerCmd(containerName)
        .withHostResource(filePath)
        .withRemotePath(destinationPath).exec();
  }

  @Override
  public InputStream copyFileFromContainerToHost(String containerName, String filePath,
      String destinationPath) {

    return dockerClient.copyArchiveFromContainerCmd(containerName, filePath)
        .withHostPath(destinationPath)
        .exec();
  }


  @Override
  public void close() throws IOException {
    if (null != dockerClient) {
      dockerClient.close();
    }
  }
}
