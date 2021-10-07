package org.eea.recordstore.service;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import com.github.dockerjava.api.model.Container;


/**
 * The interface Docker interface service.
 */
public interface DockerInterfaceService {


  /**
   * Create container with name containerName from the image with name imageName with the given
   * portBinding.
   *
   * If portBinding is passed, it must follow the pattern hostPort:containerPort
   *
   * @param containerName the container name
   * @param imageName the image name
   * @param portBinding the port binding
   *
   * @return Container container
   */
  Container createContainer(String containerName, String imageName, String portBinding);


  /**
   * Execute command inside container.
   *
   * @param container the container
   * @param command the command
   * @return the byte[]
   * @throws InterruptedException the interrupted exception
   */
  byte[] executeCommandInsideContainer(Container container, String... command)
      throws InterruptedException;


  /**
   * Stop and remove container.
   *
   * @param container the container
   */
  void stopAndRemoveContainer(Container container);

  /**
   * Stop container.
   *
   * @param container the container
   */
  void stopContainer(Container container);

  /**
   * Start container and waits timeToWait unit to start (if 0 it doesn't wait at all).
   *
   * @param container the container
   * @param timeToWait the time to wait
   * @param unit the unit
   */
  void startContainer(Container container, Long timeToWait, TimeUnit unit);

  /**
   * Gets container.
   *
   * @param containerName the container name
   *
   * @return the container
   */
  Container getContainer(String containerName);


  /**
   * Copy file from host to container.
   *
   * @param containerName the container name
   * @param filePath the file path
   * @param destinationPath the destination path
   */
  void copyFileFromHostToContainer(String containerName, String filePath, String destinationPath);

  /**
   * Copy file from container to host input stream.
   *
   * @param containerName the container name
   * @param filePath the file path
   * @param destinationPath the destination path
   *
   * @return the input stream
   */
  InputStream copyFileFromContainerToHost(String containerName, String filePath,
      String destinationPath);
}
