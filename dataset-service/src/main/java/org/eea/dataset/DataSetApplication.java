package org.eea.dataset;

import org.eea.multitenancy.MultiTenantApplication;
import org.eea.security.jwt.configuration.EeaEnableSecurity;
import org.eea.swagger.EnableEEASwagger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * The Class DataSetApplication.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableEEASwagger
@EeaEnableSecurity
@EnableAspectJAutoProxy
public class DataSetApplication {


  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(final String[] args) {
    MultiTenantApplication.executeApplication(DataSetApplication.class, args);
  }


}
