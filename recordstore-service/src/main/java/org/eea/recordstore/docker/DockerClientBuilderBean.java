package org.eea.recordstore.docker;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;

@Configuration
@Component
@ComponentScan("org.eea.recordstore.docker")
public class DockerClientBuilderBean {


  @Bean
  public DockerClient dockerClient() {
    return DockerClientBuilder.getInstance("tcp://localhost:2375").build();
  }

}
