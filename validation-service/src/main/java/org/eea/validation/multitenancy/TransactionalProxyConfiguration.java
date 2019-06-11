package org.eea.validation.multitenancy;

import java.lang.reflect.Proxy;
import org.eea.validation.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The type Transactional proxy configuration.
 */
@Configuration
public class TransactionalProxyConfiguration {

  @Autowired
  @Qualifier("validationService")
  private ValidationService validationService;

  /**
   * Proxy dataset service dataset service.
   *
   * @return the dataset service
   */
  @Bean
  public ValidationService proxyValidationService() {
    return (ValidationService) Proxy.newProxyInstance(
        TransactionalProxyConfiguration.class.getClassLoader(),
        new Class[] {ValidationService.class}, new ProxyValidationServiceImpl(validationService));
  }
}
