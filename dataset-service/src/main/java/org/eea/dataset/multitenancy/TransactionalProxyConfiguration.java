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

  @Autowired
  @Qualifier("datasetService")
  private DatasetService datasetService;

  /**
   * Proxy dataset service dataset service.
   *
   * @return the dataset service
   */
  @Bean
  public DatasetService proxyDatasetService() {
    return (DatasetService) Proxy.newProxyInstance(
        TransactionalProxyConfiguration.class.getClassLoader(),
        new Class[]{DatasetService.class},
        new ProxyMultitenantService<>(datasetService));
  }
}
