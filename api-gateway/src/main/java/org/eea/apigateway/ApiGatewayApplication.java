package org.eea.apigateway;

import org.eea.security.jwt.configuration.EeaEnableSecurity;
import org.eea.swagger.EnableEEASwagger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;


/**
 * The Class ApiGatewayApplication.
 */
@EnableZuulProxy
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableEEASwagger
@EeaEnableSecurity
public class ApiGatewayApplication {


  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(ApiGatewayApplication.class, args);
  }
}
