package org.eea.validation;

import org.eea.security.jwt.configuration.EeaEnableSecurity;
import org.eea.swagger.EnableEEASwagger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * The Class ValidationServiceApplication.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableEEASwagger
@EeaEnableSecurity
@EnableAspectJAutoProxy
public class ValidationServiceApplication {

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(ValidationServiceApplication.class, args);

  }

}
