package org.eea.recordstore.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * The Class DockerClientBuilderBean.
 */
@Configuration
@Component
@ComponentScan("org.eea.recordstore.docker")
@Getter
@Setter
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
  @Bean
  public DockerClient dockerClient() {
    return DockerClientBuilder.getInstance(dockerServerUrl).build();
  }

}
