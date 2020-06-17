package org.eea.dataflow.integration.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.eea.dataflow.integration.executor.service.AbstractIntegrationExecutorService;
import org.eea.dataflow.integration.executor.service.IntegrationExecutorService;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class IntegrationExecutorFactoryImpl.
 */
@Component
public class IntegrationExecutorFactoryImpl implements IntegrationExecutorFactory {

  /** The abstract integration executor services. */
  @Autowired
  private Set<AbstractIntegrationExecutorService> abstractIntegrationExecutorServices;

  /** The integration map. */
  private Map<IntegrationToolTypeEnum, AbstractIntegrationExecutorService> integrationMap;


  /**
   * Inits the integrationExecutionMap.
   */
  @PostConstruct
  private void init() {
    integrationMap = new HashMap<>();
    if (null != abstractIntegrationExecutorServices) {
      abstractIntegrationExecutorServices.stream().forEach(integrationExecutor -> {
        integrationMap.put(integrationExecutor.getExecutorType(), integrationExecutor);
      });
    }
  }


  /**
   * Gets the executor.
   *
   * @param integrationToolTypeEnum the integration tool type enum
   * @return the executor
   */
  @Override
  public IntegrationExecutorService getExecutor(IntegrationToolTypeEnum integrationToolTypeEnum) {
    return integrationMap.get(integrationToolTypeEnum);
  }

}
