package org.eea.recordstore.service.impl;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;


@Component
public class DockerClientBuilderBean {

 
  @Bean
  public DockerClient dockerClient() { 
    return DockerClientBuilder
      .getInstance("tcp://localhost:2375")
      .build();
  }
  
}
