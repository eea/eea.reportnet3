package org.eea.dataflow.integration.crud.factory;

import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;


/**
 * A factory for creating CrudManager objects.
 */
public interface CrudManagerFactory {

  /**
   * Gets the manager.
   *
   * @param tool the tool
   * @return the manager
   */
  CrudManager getManager(IntegrationToolTypeEnum tool);
}
