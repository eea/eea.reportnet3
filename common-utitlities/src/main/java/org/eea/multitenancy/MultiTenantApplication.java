package org.eea.multitenancy;


import org.springframework.boot.SpringApplication;


/**
 * The type Multi tenant application. Runs application and when application is ready it sets default
 * tenant to null so it cannot be used during normal execution. This will ensure that the right
 * connection is always used
 */
public class MultiTenantApplication {


  /**
   * Execute application.
   *
   * @param <T> the type parameter
   * @param executionClass the execution class
   * @param args the args
   */
  public static <T> void executeApplication(Class<T> executionClass, final String[] args) {
    SpringApplication.run(executionClass, args);
    TenantResolver.initTenant = "";
  }

}