package org.eea.dataset;

import org.eea.swagger.EnableEEASwagger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


/**
 * The type Data set application.
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableEEASwagger
public class DataSetApplication {


  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(DataSetApplication.class, args);
  }


}
