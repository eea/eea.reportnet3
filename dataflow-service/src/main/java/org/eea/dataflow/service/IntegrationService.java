package org.eea.dataflow.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.integration.IntegrationVO;


/**
 * The Interface IntegrationService.
 */
public interface IntegrationService {


  /**
   * Creates the integration.
   *
   * @param integrationVO the integration VO
   * @throws EEAException the EEA exception
   */
  void createIntegration(final IntegrationVO integrationVO) throws EEAException;


  /**
   * Delete integration.
   *
   * @param integrationVO the integration VO
   * @throws EEAException the EEA exception
   */
  void deleteIntegration(final IntegrationVO integrationVO) throws EEAException;


  /**
   * Update integration.
   *
   * @param integrationVO the integration VO
   * @throws EEAException the EEA exception
   */
  void updateIntegration(final IntegrationVO integrationVO) throws EEAException;

  /**
   * Gets the all integrations by criteria.
   *
   * @param integrationVO the integration VO
   * @return the all integrations by criteria
   * @throws EEAException the EEA exception
   */
  List<IntegrationVO> getAllIntegrationsByCriteria(final IntegrationVO integrationVO)
      throws EEAException;


  /**
   * Gets only extensions and operations.
   *
   * @param integrationVOList the integration VO list
   * @return the only extensions and operations
   * @throws EEAException the EEA exception
   */
  List<IntegrationVO> getOnlyExtensionsAndOperations(List<IntegrationVO> integrationVOList)
      throws EEAException;


}
