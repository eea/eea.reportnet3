package org.eea.dataflow.integration.crud.factory;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.integration.IntegrationVO;


/**
 * The Interface CrudManager.
 */
public interface CrudManager {

  /**
   * Gets the.
   *
   * @param integration the integration
   * @return the list
   * @throws EEAException the EEA exception
   */
  List<IntegrationVO> get(IntegrationVO integration) throws EEAException;

  /**
   * Update.
   *
   * @param integration the integration
   * @throws EEAException the EEA exception
   */
  void update(IntegrationVO integration) throws EEAException;

  /**
   * Creates the.
   *
   * @param integration the integration
   * @throws EEAException the EEA exception
   */
  void create(IntegrationVO integration) throws EEAException;

  /**
   * Delete.
   *
   * @param integrationId the integration id
   * @throws EEAException the EEA exception
   */
  void delete(Long integrationId) throws EEAException;
}
