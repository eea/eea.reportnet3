package org.eea.dataflow.integration.executor.fme.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.auth.BasicAuthRequestInterceptor;

@Configuration
@EnableFeignClients("org.eea.dataflow.integration.executor.fme.repository")
public class FMEConfiguration {

  @Value("${integration.fme.user}")
  private String fmeUser;

  @Value("${integration.fme.password}")
  private String fmePassword;

  @Bean
  public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
    return new BasicAuthRequestInterceptor(fmeUser, fmePassword);
  }

}
