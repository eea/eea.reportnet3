package org.eea.ums.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class UserManagementConfiguration {

  @Value("")
  private String securityProviderUrl;

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }


}
