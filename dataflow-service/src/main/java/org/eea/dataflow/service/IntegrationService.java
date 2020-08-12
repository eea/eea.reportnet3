package org.eea.dataflow.service;

import java.util.List;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.integration.ExecutionResultVO;
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
   * @param integrationId the integration id
   * @throws EEAException the EEA exception
   */
  void deleteIntegration(final Long integrationId) throws EEAException;

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
   */
  List<IntegrationVO> getOnlyExtensionsAndOperations(List<IntegrationVO> integrationVOList);

  /**
   * Copy integrations.
   *
   * @param dataflowIdDestination the dataflow id destination
   * @param originDatasetSchemaIds the origin dataset schema ids
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @throws EEAException the EEA exception
   */
  void copyIntegrations(Long dataflowIdDestination, List<String> originDatasetSchemaIds,
      Map<String, String> dictionaryOriginTargetObjectId) throws EEAException;

  /**
   * Execute EU dataset export.
   *
   * @param dataflowId the dataflow id
   * @return the list
   * @throws EEAException the EEA exception
   */
  List<ExecutionResultVO> executeEUDatasetExport(Long dataflowId) throws EEAException;

  /**
   * Creates the default integration.
   *
   * @param dataflowId the dataflow id
   * @param datasetSchemaId the dataset schema id
   */
  void createDefaultIntegration(Long dataflowId, String datasetSchemaId);

  /**
   * Gets the expor EU dataset integration by dataset id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the expor EU dataset integration by dataset id
   */
  IntegrationVO getExporEUDatasetIntegrationByDatasetId(String datasetSchemaId);

  /**
   * Adds the populate EU dataset lock.
   *
   * @param dataflowId the dataflow id
   * @throws EEAException the EEA exception
   */
  void addPopulateEUDatasetLock(Long dataflowId) throws EEAException;

  /**
   * Release populate EU dataset lock.
   *
   * @param dataflowId the dataflow id
   */
  void releasePopulateEUDatasetLock(Long dataflowId);
}
