package org.eea.dataflow.integration.crud.factory.manager;

import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.integration.crud.factory.AbstractCrudManager;
import org.eea.dataflow.mapper.IntegrationMapper;
import org.eea.dataflow.persistence.domain.Integration;
import org.eea.dataflow.persistence.repository.IntegrationRepository;
import org.eea.dataflow.persistence.repository.OperationParametersRepository;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;


/**
 * The Class FMEIntegrationManager.
 */
@Component
public class FMEIntegrationManager extends AbstractCrudManager {


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(FMEIntegrationManager.class);

  /** The integration repository. */
  @Autowired
  private IntegrationRepository integrationRepository;

  /** The integration mapper. */
  @Autowired
  private IntegrationMapper integrationMapper;

  /** The operation parameters repository. */
  @Autowired
  private OperationParametersRepository operationParametersRepository;


  /** The Constant DATAFLOW_ID: {@value}. */
  private static final String DATAFLOW_ID = "dataflowId";

  /** The Constant DATASETSCHEMA_ID: {@value}. */
  private static final String DATASET_SCHEMA_ID = "datasetSchemaId";

  /**
   * Gets the tool type.
   *
   * @return the tool type
   */
  @Override
  public IntegrationToolTypeEnum getToolType() {
    return IntegrationToolTypeEnum.FME;
  }

  /**
   * Gets the.
   *
   * @param integrationVO the integration VO
   * @return the list
   * @throws EEAException the EEA exception
   */
  @Override
  public List<IntegrationVO> get(IntegrationVO integrationVO) throws EEAException {

    List<IntegrationVO> results = new ArrayList<>();
    if (integrationVO.getId() != null) {
      Integration integration = integrationRepository.findById(integrationVO.getId()).get();
      results.add(integrationMapper.entityToClass(integration));
    } else if (integrationVO.getInternalParameters() != null
        && integrationVO.getInternalParameters().size() > 0) {
      if (integrationVO.getInternalParameters().containsKey(DATAFLOW_ID)) {
        integrationVO.getInternalParameters().remove(DATAFLOW_ID);
      }
      List<String> parameters = new ArrayList<>(integrationVO.getInternalParameters().keySet());
      String parameter = parameters.get(0);
      String value = integrationVO.getInternalParameters().get(parameter);
      List<Integration> integrationList =
          integrationRepository.findByInternalOperationParameter(parameter, value);
      results = integrationMapper.entityListToClass(integrationList);
    } else {
      LOG_ERROR.error("No filters in the query to find integrations");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.MISSING_PARAMETERS_INTEGRATION);
    }

    return results;
  }

  /**
   * Update.
   *
   * @param integrationVO the integration VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void update(IntegrationVO integrationVO) throws EEAException {

    Integration integration = integrationRepository.findById(integrationVO.getId()).orElse(null);
    if (integration == null) {
      throw new EEAException(EEAErrorMessage.INTEGRATION_NOT_FOUND);
    }
    if (integrationVO.getInternalParameters() == null
        || integrationVO.getInternalParameters().size() == 0
        || !integrationVO.getInternalParameters().containsKey(DATAFLOW_ID)
        || !integrationVO.getInternalParameters().containsKey(DATASET_SCHEMA_ID)) {
      LOG_ERROR.error(
          "Error updating an integration: Internal parameters don't have dataflowId or datasetSchemaId");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.MISSING_PARAMETERS_INTEGRATION);
    }
    if (integration.getExternalParameters() != null) {
      integration.getExternalParameters().clear();
    }
    if (integration.getInternalParameters() != null) {
      integration.getInternalParameters().clear();
    }
    operationParametersRepository.deleteByIntegration(integration);

    integration = integrationMapper.classToEntity(integrationVO);
    integrationRepository.save(integration);
    LOG.info("Integration with id {} updated", integration.getId());

  }

  /**
   * Creates the.
   *
   * @param integrationVO the integration VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void create(IntegrationVO integrationVO) throws EEAException {

    if (integrationVO.getInternalParameters() == null
        || integrationVO.getInternalParameters().size() == 0
        || !integrationVO.getInternalParameters().containsKey(DATAFLOW_ID)
        || !integrationVO.getInternalParameters().containsKey(DATASET_SCHEMA_ID)) {
      LOG_ERROR.error(
          "Error creating an integration: Internal parameters don't have dataflowId or datasetSchemaId");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.MISSING_PARAMETERS_INTEGRATION);
    }

    Integration integration = integrationMapper.classToEntity(integrationVO);
    integrationRepository.save(integration);
    LOG.info("New Integration created");
  }


  /**
   * Delete.
   *
   * @param integrationId the integration id
   * @throws EEAException the EEA exception
   */
  @Override
  public void delete(Long integrationId) throws EEAException {
    if (integrationId != null) {
      integrationRepository.deleteById(integrationId);
      LOG.info("Integration with Id {} deleted", integrationId);
    } else {
      LOG_ERROR.error("IntegrationId missing");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.MISSING_PARAMETERS_INTEGRATION);
    }
  }


}
