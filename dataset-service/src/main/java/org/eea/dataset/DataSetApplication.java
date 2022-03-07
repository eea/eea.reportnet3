package org.eea.dataset;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.eea.lock.annotation.EnableLockAspect;
import org.eea.security.jwt.configuration.EeaEnableSecurity;
import org.eea.swagger.EnableEEASwagger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

/**
 * The Class DataSetApplication.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableEEASwagger
@EeaEnableSecurity
@EnableLockAspect
@EnableCaching
public class DataSetApplication {


  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(DataSetApplication.class, args);
  }

  /**
   * Container factory.
   *
   * @return the tomcat servlet web server factory
   */
  @Bean
  public TomcatServletWebServerFactory containerFactory() {
    return new TomcatServletWebServerFactory() {
      protected void customizeConnector(Connector connector) {
        int maxSize = 7200000;
        super.customizeConnector(connector);
        if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol) {

          ((AbstractHttp11Protocol<?>) connector.getProtocolHandler())
              .setConnectionTimeout(maxSize);
          logger.info("Set time out " + maxSize);
        }
      }
    };

  }


}
