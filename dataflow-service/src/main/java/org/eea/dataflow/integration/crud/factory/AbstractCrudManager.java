package org.eea.dataflow.integration.crud.factory;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;


/**
 * The Class AbstractCrudManager.
 */
public abstract class AbstractCrudManager implements CrudManager {


  /**
   * Gets the tool type.
   *
   * @return the tool type
   */
  public abstract IntegrationToolTypeEnum getToolType();

  /**
   * Gets the.
   *
   * @param integration the integration
   * @return the list
   * @throws EEAException the EEA exception
   */
  @Override
  public abstract List<IntegrationVO> get(IntegrationVO integration) throws EEAException;

  /**
   * Update.
   *
   * @param integration the integration
   * @throws EEAException the EEA exception
   */
  @Override
  public abstract void update(IntegrationVO integration) throws EEAException;

  /**
   * Creates the.
   *
   * @param integration the integration
   * @throws EEAException the EEA exception
   */
  @Override
  public abstract void create(IntegrationVO integration) throws EEAException;

  /**
   * Delete.
   *
   * @param integration the integration
   * @throws EEAException the EEA exception
   */
  @Override
  public abstract void delete(IntegrationVO integration) throws EEAException;
}
