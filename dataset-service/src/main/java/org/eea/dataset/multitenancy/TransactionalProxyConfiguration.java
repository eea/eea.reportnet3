package org.eea.dataset.multitenancy;

import java.lang.reflect.Proxy;
import org.eea.dataset.service.DatasetService;
import org.eea.multitenancy.ProxyMultitenantService;
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
   * @param datasetService the dataset service
   * @return the dataset service
   */
  @Bean
  @Autowired
  public DatasetService proxyDatasetService(
      @Qualifier("datasetService") DatasetService datasetService) {
    return (DatasetService) Proxy.newProxyInstance(
        TransactionalProxyConfiguration.class.getClassLoader(), new Class[] {DatasetService.class},
        new ProxyMultitenantService<>(datasetService));
  }
}
