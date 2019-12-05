package org.eea.recordstore.docker;

import org.springframework.beans.factory.annotation.Value;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;

/**
 * The Class DockerClientBuilderBean.
 */

public class DockerClientBuilderBean {

  /**
   * The docker server url.
   */
  @Value("${dockerServerUrl}")
  private String dockerServerUrl;

  /**
   * Docker client.
   *
   * @return the docker client
   */
  // @Bean
  public DockerClient dockerClient() {
    return DockerClientBuilder.getInstance(dockerServerUrl).build();
  }

}
