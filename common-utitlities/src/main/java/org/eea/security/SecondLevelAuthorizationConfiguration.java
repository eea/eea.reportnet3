package org.eea.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The Class SecondLevelAuthorizationConfiguration.
 */
@Configuration
public class SecondLevelAuthorizationConfiguration implements WebMvcConfigurer {

  /**
   * Adds the interceptors.
   *
   * @param registry the registry
   */
  @Override
  public void addInterceptors(final InterceptorRegistry registry) {
    registry.addInterceptor(new SecondLevelAuthorizationHandlerInterceptor());
  }

}
