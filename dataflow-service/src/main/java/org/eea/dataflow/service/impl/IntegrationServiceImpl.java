package org.eea.dataflow.service.impl;

import java.util.List;
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


}
