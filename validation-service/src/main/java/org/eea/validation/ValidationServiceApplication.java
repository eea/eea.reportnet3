package org.eea.validation;

import org.eea.multitenancy.MultiTenantApplication;
import org.eea.security.jwt.configuration.EeaEnableSecurity;
import org.eea.swagger.EnableEEASwagger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * The Class ValidationServiceApplication.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableEEASwagger
@EeaEnableSecurity
public class ValidationServiceApplication {

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(final String[] args) {
    MultiTenantApplication.executeApplication(ValidationServiceApplication.class, args);
  }

}
