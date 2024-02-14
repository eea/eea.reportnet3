package org.eea.ums.configuration;

import org.eea.security.jwt.configuration.EeaEnableSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The Class UserManagementConfiguration.
 */
@Configuration
@EeaEnableSecurity
public class UserManagementConfiguration implements WebMvcConfigurer {


  /**
   * Rest template.
   *
   * @return the rest template
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
