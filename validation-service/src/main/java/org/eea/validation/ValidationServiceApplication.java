package org.eea.validation;

import org.eea.lock.annotation.EnableLockAspect;
import org.eea.lock.redis.EnableRedisLock;
import org.eea.security.jwt.configuration.EeaEnableSecurity;
import org.eea.swagger.EnableEEASwagger;
import org.eea.s3configuration.EnableS3Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
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
@EnableLockAspect
@EnableCaching
@EnableRedisLock
@EnableS3Configuration
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
