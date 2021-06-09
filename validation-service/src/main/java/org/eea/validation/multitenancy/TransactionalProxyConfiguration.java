package org.eea.validation.multitenancy;

import java.lang.reflect.Proxy;
import org.eea.multitenancy.ProxyMultitenantService;
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


  /**
   * Proxy dataset service dataset service.
   *
   * @param validationService the validation service
   * @return the dataset service
   */
  @Bean
  @Autowired
  public ValidationService proxyValidationService(
      @Qualifier("validationService") ValidationService validationService) {
    return (ValidationService) Proxy.newProxyInstance(
        TransactionalProxyConfiguration.class.getClassLoader(),
        new Class[] {ValidationService.class}, new ProxyMultitenantService<>(validationService));
  }
}
