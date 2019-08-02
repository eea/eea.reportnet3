package org.eea.apigateway.configuration;

import org.eea.security.jwt.configuration.EeaEnableSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The type Api gateway configuration.
 */
@Configuration
public class ApiGatewayConfiguration implements WebMvcConfigurer {


  @Override
  public void addCorsMappings(final CorsRegistry registry) {
    registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "POST", "PUT", "DELETE");
  }

}
