package org.eea.dataflow.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import org.eea.dataflow.integration.crud.factory.CrudManager;
import org.eea.dataflow.integration.crud.factory.CrudManagerFactory;
import org.eea.dataflow.service.IntegrationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * The Class IntegrationServiceImpl.
 */
@Service("integrationService")
public class IntegrationServiceImpl implements IntegrationService {

  /** The crud manager factory. */
  @Autowired
  private CrudManagerFactory crudManagerFactory;


  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(IntegrationServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant FILE_EXTENSION: {@value}. */
  private static final String FILE_EXTENSION = "fileExtension";

  /** The Constant DATASETSCHEMAID: {@value}. */
  private static final String DATASETSCHEMAID = "datasetSchemaId";

  /**
   * Creates the integration.
   *
   * @param integrationVO the integration VO
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Override
  public void createIntegration(IntegrationVO integrationVO) throws EEAException {


    CrudManager crudManager = crudManagerFactory.getManager(IntegrationToolTypeEnum.FME);
    crudManager.create(integrationVO);

  }


  /**
   * Delete integration.
   *
   * @param integrationId the integration id
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Override
  public void deleteIntegration(Long integrationId) throws EEAException {
    CrudManager crudManager = crudManagerFactory.getManager(IntegrationToolTypeEnum.FME);
    crudManager.delete(integrationId);

  }

  /**
   * Update integration.
   *
   * @param integrationVO the integration VO
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Override
  public void updateIntegration(IntegrationVO integrationVO) throws EEAException {

    CrudManager crudManager = crudManagerFactory.getManager(IntegrationToolTypeEnum.FME);
    crudManager.update(integrationVO);

  }


  /**
   * Gets the all integrations by criteria.
   *
   * @param integrationVO the integration VO
   * @return the all integrations by criteria
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Override
  public List<IntegrationVO> getAllIntegrationsByCriteria(IntegrationVO integrationVO)
      throws EEAException {
    CrudManager crudManager = crudManagerFactory.getManager(IntegrationToolTypeEnum.FME);
    return crudManager.get(integrationVO);
  }

  /**
   * Gets only extensions and operations.
   *
   * @param integrationVOList the integration VO list
   * @return the only extensions and operations
   * @throws EEAException the EEA exception
   */
  @Override
  public List<IntegrationVO> getOnlyExtensionsAndOperations(List<IntegrationVO> integrationVOList) {
    // Remove all data except operation and fileExtension
    List<IntegrationVO> newIntegrationVOList = new ArrayList<>();
    integrationVOList.stream().forEach(integration -> {
      IntegrationVO integrationVOAux = new IntegrationVO();
      Map<String, String> internalParameters = new HashMap<>();
      internalParameters.put(FILE_EXTENSION,
          integration.getInternalParameters().get(FILE_EXTENSION));
      internalParameters.put(DATASETSCHEMAID,
          integration.getInternalParameters().get(DATASETSCHEMAID));
      integrationVOAux.setOperation(integration.getOperation());
      integrationVOAux.setInternalParameters(internalParameters);
      newIntegrationVOList.add(integrationVOAux);
    });
    return newIntegrationVOList;
  }


  /**
   * Copy integrations.
   *
   * @param dataflowIdDestination the dataflow id destination
   * @param originDatasetSchemaIds the origin dataset schema ids
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @throws EEAException the EEA exception
   */
  @Transactional
  @Override
  public void copyIntegrations(Long dataflowIdDestination, List<String> originDatasetSchemaIds,
      Map<String, String> dictionaryOriginTargetObjectId) throws EEAException {
    for (String originDatasetSchemaId : originDatasetSchemaIds) {
      IntegrationVO integrationCriteria = new IntegrationVO();
      integrationCriteria.getInternalParameters().put(DATASETSCHEMAID, originDatasetSchemaId);
      List<IntegrationVO> integrations = getAllIntegrationsByCriteria(integrationCriteria);
      for (IntegrationVO integration : integrations) {
        // we've got the origin integrations. We intend to change the dataflow and the
        // datasetSchemaId
        // parameters and save all as new integrations of the copied design datasets
        LOG.info(
            "There are integrations to be copied into the datasetSchemaId {} in the dataflowId {}",
            dictionaryOriginTargetObjectId.get(originDatasetSchemaId), dataflowIdDestination);
        integration.getInternalParameters().put(DATASETSCHEMAID,
            dictionaryOriginTargetObjectId.get(originDatasetSchemaId));
        integration.getInternalParameters().put("dataflowId", dataflowIdDestination.toString());
        createIntegration(integration);
      }
    }
  }


}
