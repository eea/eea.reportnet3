package org.eea.validation;

import org.eea.utils.swagger.EnableEEASwagger;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

/**
 * The type Data flow application.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableEEASwagger
public class ValidationServiceApplication {


  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(ValidationServiceApplication.class, args);
  }
  
  @Bean
  public KieContainer kieContainer() {
      return KieServices.Factory.get().getKieClasspathContainer();
  }

}
