package org.eea.dataflow.integration.crud.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * The Class CrudManagerFactoryImpl. {@inheritDoc}
 */
@Component
public class CrudManagerFactoryImpl implements CrudManagerFactory {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CrudManagerFactoryImpl.class);


  /** The crud managers. */
  @Autowired
  private Set<AbstractCrudManager> crudManagers;


  /** The managers map. */
  private Map<IntegrationToolTypeEnum, AbstractCrudManager> managersMap;



  /**
   * Inits the.
   */
  @PostConstruct
  private void init() {
    managersMap = new HashMap<>();
    if (null != crudManagers) {
      crudManagers.stream().forEach(crudManager -> {
        managersMap.put(crudManager.getToolType(), crudManager);
      });
    }
  }


  /**
   * Gets the manager.
   *
   * @param tool the tool
   * @return the manager
   */
  @Override
  public CrudManager getManager(IntegrationToolTypeEnum tool) {

    CrudManager crudManager = null;
    if (this.managersMap.containsKey(tool)) {
      crudManager = this.managersMap.get(tool);
    }
    return crudManager;

  }
}
