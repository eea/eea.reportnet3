package org.eea.indexsearch;

import org.eea.swagger.EnableEEASwagger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


/**
 * The Class IndexsearchServiceApplication.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableEEASwagger
public class IndexsearchServiceApplication {


  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(IndexsearchServiceApplication.class, args);
  }


}
