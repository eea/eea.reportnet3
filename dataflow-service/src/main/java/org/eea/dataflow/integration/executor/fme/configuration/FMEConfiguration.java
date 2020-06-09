package org.eea.dataflow.integration.executor.fme.configuration;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.auth.BasicAuthRequestInterceptor;

@Configuration
@EnableFeignClients("org.eea.dataflow.integration.fme.repository")
public class FMEConfiguration {

  @Bean
  public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
    return new BasicAuthRequestInterceptor("Reportnet3", "Reportnet3_2020!");
  }

}
